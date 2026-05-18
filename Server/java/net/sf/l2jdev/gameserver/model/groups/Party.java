package net.sf.l2jdev.gameserver.model.groups;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.config.MagicLampConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.config.RatesConfig;
import net.sf.l2jdev.gameserver.config.custom.ClassBalanceConfig;
import net.sf.l2jdev.gameserver.config.custom.PremiumSystemConfig;
import net.sf.l2jdev.gameserver.managers.DuelManager;
import net.sf.l2jdev.gameserver.managers.ItemManager;
import net.sf.l2jdev.gameserver.managers.MagicLampManager;
import net.sf.l2jdev.gameserver.managers.PcCafePointsManager;
import net.sf.l2jdev.gameserver.model.HuntPass;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Attackable;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.actor.holders.player.AchievementBoxHolder;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.instancezone.Instance;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.stats.Stat;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.StatusUpdateType;
import net.sf.l2jdev.gameserver.network.serverpackets.ExAskModifyPartyLooting;
import net.sf.l2jdev.gameserver.network.serverpackets.ExCloseMPCC;
import net.sf.l2jdev.gameserver.network.serverpackets.ExOpenMPCC;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPartyPetWindowAdd;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPartyPetWindowDelete;
import net.sf.l2jdev.gameserver.network.serverpackets.ExSetPartyLooting;
import net.sf.l2jdev.gameserver.network.serverpackets.ExTacticalSign;
import net.sf.l2jdev.gameserver.network.serverpackets.PartyMemberPosition;
import net.sf.l2jdev.gameserver.network.serverpackets.PartySmallWindowAdd;
import net.sf.l2jdev.gameserver.network.serverpackets.PartySmallWindowAll;
import net.sf.l2jdev.gameserver.network.serverpackets.PartySmallWindowDelete;
import net.sf.l2jdev.gameserver.network.serverpackets.PartySmallWindowDeleteAll;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.taskmanagers.GameTimeTaskManager;
import net.sf.l2jdev.gameserver.util.LocationUtil;

public class Party extends AbstractPlayerGroup
{
	private static final Logger LOGGER = Logger.getLogger(Party.class.getName());
	private static final double[] BONUS_EXP_SP = new double[]
	{
		1.0,
		1.6,
		1.65,
		1.7,
		1.8,
		1.9,
		2.0,
		2.1,
		2.2
	};
	private static final Duration PARTY_POSITION_BROADCAST_INTERVAL = Duration.ofSeconds(12L);
	private static final Duration PARTY_DISTRIBUTION_TYPE_REQUEST_TIMEOUT = Duration.ofSeconds(15L);
	private final List<Player> _members = new CopyOnWriteArrayList<>();
	private boolean _pendingInvitation = false;
	private long _pendingInviteTimeout;
	private int _partyLvl = 0;
	private PartyDistributionType _distributionType = PartyDistributionType.FINDERS_KEEPERS;
	private PartyDistributionType _changeRequestDistributionType;
	private Future<?> _changeDistributionTypeRequestTask = null;
	private Set<Integer> _changeDistributionTypeAnswers = null;
	private int _itemLastLoot = 0;
	private CommandChannel _commandChannel = null;
	private Future<?> _positionBroadcastTask = null;
	protected PartyMemberPosition _positionPacket;
	private boolean _disbanding = false;
	private Map<Integer, Creature> _tacticalSigns = null;
	private static final int[] TACTICAL_SYS_STRINGS = new int[]
	{
		0,
		2664,
		2665,
		2666,
		2667
	};

	public Party(Player leader, PartyDistributionType partyDistributionType)
	{
		this._members.add(leader);
		this._partyLvl = leader.getLevel();
		this._distributionType = partyDistributionType;
		World.getInstance().incrementParty();
	}

	public boolean getPendingInvitation()
	{
		return this._pendingInvitation;
	}

	public void setPendingInvitation(boolean value)
	{
		this._pendingInvitation = value;
		this._pendingInviteTimeout = GameTimeTaskManager.getInstance().getGameTicks() + 150;
	}

	public boolean isInvitationRequestExpired()
	{
		return this._pendingInviteTimeout <= GameTimeTaskManager.getInstance().getGameTicks();
	}

	private Player getCheckedRandomMember(int itemId, Creature target)
	{
		List<Player> availableMembers = new ArrayList<>();

		for (Player member : this._members)
		{
			if (member.getInventory().validateCapacityByItemId(itemId) && LocationUtil.checkIfInRange(PlayerConfig.ALT_PARTY_RANGE, target, member, true))
			{
				availableMembers.add(member);
			}
		}

		return !availableMembers.isEmpty() ? availableMembers.get(Rnd.get(availableMembers.size())) : null;
	}

	private Player getCheckedNextLooter(int itemId, Creature target)
	{
		for (int i = 0; i < this.getMemberCount(); i++)
		{
			if (++this._itemLastLoot >= this.getMemberCount())
			{
				this._itemLastLoot = 0;
			}

			try
			{
				Player member = this._members.get(this._itemLastLoot);
				if (member.getInventory().validateCapacityByItemId(itemId) && LocationUtil.checkIfInRange(PlayerConfig.ALT_PARTY_RANGE, target, member, true))
				{
					return member;
				}
			}
			catch (Exception var6)
			{
			}
		}

		return null;
	}

	private Player getActualLooter(Player player, int itemId, boolean spoil, Creature target)
	{
		Player looter = null;
		switch (this._distributionType)
		{
			case RANDOM:
				if (!spoil)
				{
					looter = this.getCheckedRandomMember(itemId, target);
				}
				break;
			case RANDOM_INCLUDING_SPOIL:
				looter = this.getCheckedRandomMember(itemId, target);
				break;
			case BY_TURN:
				if (!spoil)
				{
					looter = this.getCheckedNextLooter(itemId, target);
				}
				break;
			case BY_TURN_INCLUDING_SPOIL:
				looter = this.getCheckedNextLooter(itemId, target);
		}

		return looter != null ? looter : player;
	}

	public void broadcastToPartyMembersNewLeader()
	{
		for (Player member : this._members)
		{
			if (member != null)
			{
				member.sendPacket(PartySmallWindowDeleteAll.STATIC_PACKET);
				member.sendPacket(new PartySmallWindowAll(member, this));
				member.broadcastUserInfo();
			}
		}
	}

	public void broadcastToPartyMembers(Player player, ServerPacket packet)
	{
		for (Player member : this._members)
		{
			if (member != null && member.getObjectId() != player.getObjectId())
			{
				member.sendPacket(packet);
			}
		}
	}

	public void addPartyMember(Player player)
	{
		if (!this._members.contains(player))
		{
			if (this._changeRequestDistributionType != null)
			{
				this.finishLootRequest(false);
			}

			this._members.add(player);
			player.sendPacket(new PartySmallWindowAll(player, this));

			for (Player pMember : this._members)
			{
				if (pMember != null)
				{
					Summon pet = pMember.getPet();
					if (pet != null)
					{
						player.sendPacket(new ExPartyPetWindowAdd(pet));
					}

					pMember.getServitors().values().forEach(s -> player.sendPacket(new ExPartyPetWindowAdd(s)));
				}
			}

			SystemMessage msg = new SystemMessage(SystemMessageId.YOU_HAVE_JOINED_A_PARTY);
			player.sendPacket(msg);
			msg = new SystemMessage(SystemMessageId.C1_HAS_JOINED_THE_PARTY);
			msg.addString(player.getAppearance().getVisibleName());
			this.broadcastToPartyMembers(player, msg);

			for (Player member : this._members)
			{
				if (member != player)
				{
					member.sendPacket(new PartySmallWindowAdd(player, this));
				}
			}

			Summon pet = player.getPet();
			if (pet != null)
			{
				this.broadcastPacket(new ExPartyPetWindowAdd(pet));
			}

			player.getServitors().values().forEach(s -> this.broadcastPacket(new ExPartyPetWindowAdd(s)));
			if (player.getLevel() > this._partyLvl)
			{
				this._partyLvl = player.getLevel();
			}

			StatusUpdate su = new StatusUpdate(player);
			su.addUpdate(StatusUpdateType.MAX_HP, (int) player.getMaxHp());
			su.addUpdate(StatusUpdateType.CUR_HP, (int) player.getCurrentHp());

			for (Player memberx : this._members)
			{
				if (memberx != null)
				{
					memberx.updateEffectIcons(true);
					Summon summon = memberx.getPet();
					memberx.broadcastUserInfo();
					if (summon != null)
					{
						summon.updateEffectIcons();
					}

					memberx.getServitors().values().forEach(Creature::updateEffectIcons);
					memberx.sendPacket(su);
				}
			}

			if (this.isInCommandChannel())
			{
				player.sendPacket(ExOpenMPCC.STATIC_PACKET);
			}

			if (this._positionBroadcastTask == null)
			{
				this._positionBroadcastTask = ThreadPool.scheduleAtFixedRate(() -> {
					if (this._positionPacket == null)
					{
						this._positionPacket = new PartyMemberPosition(this);
					}
					else
					{
						this._positionPacket.reuse(this);
					}

					this.broadcastPacket(this._positionPacket);
				}, PARTY_POSITION_BROADCAST_INTERVAL.toMillis() / 2L, PARTY_POSITION_BROADCAST_INTERVAL.toMillis());
			}

			this.applyTacticalSigns(player, false);
			World.getInstance().incrementPartyMember();
		}
	}

	private Map<Integer, Creature> getTacticalSigns()
	{
		if (this._tacticalSigns == null)
		{
			synchronized (this)
			{
				if (this._tacticalSigns == null)
				{
					this._tacticalSigns = new ConcurrentHashMap<>(1);
				}
			}
		}

		return this._tacticalSigns;
	}

	public void applyTacticalSigns(Player player, boolean remove)
	{
		if (this._tacticalSigns != null)
		{
			this._tacticalSigns.entrySet().forEach(entry -> player.sendPacket(new ExTacticalSign(entry.getValue(), remove ? 0 : entry.getKey())));
		}
	}

	public void addTacticalSign(Player player, int tacticalSignId, Creature target)
	{
		Creature tacticalTarget = this.getTacticalSigns().get(tacticalSignId);
		if (tacticalTarget == null)
		{
			this._tacticalSigns.values().remove(target);
			this._tacticalSigns.put(tacticalSignId, target);
			SystemMessage sm = new SystemMessage(SystemMessageId.C1_USED_S3_ON_C2);
			sm.addPcName(player);
			sm.addString(target.getName());
			sm.addSystemString(TACTICAL_SYS_STRINGS[tacticalSignId]);
			this._members.forEach(m -> {
				m.sendPacket(new ExTacticalSign(target, tacticalSignId));
				m.sendPacket(sm);
			});
		}
		else if (tacticalTarget == target)
		{
			this._tacticalSigns.remove(tacticalSignId);
			this._members.forEach(m -> m.sendPacket(new ExTacticalSign(tacticalTarget, 0)));
		}
		else
		{
			this._tacticalSigns.replace(tacticalSignId, target);
			SystemMessage sm = new SystemMessage(SystemMessageId.C1_USED_S3_ON_C2);
			sm.addPcName(player);
			sm.addString(target.getName());
			sm.addSystemString(TACTICAL_SYS_STRINGS[tacticalSignId]);
			this._members.forEach(m -> {
				m.sendPacket(new ExTacticalSign(tacticalTarget, 0));
				m.sendPacket(new ExTacticalSign(target, tacticalSignId));
				m.sendPacket(sm);
			});
		}
	}

	public void setTargetBasedOnTacticalSignId(Player player, int tacticalSignId)
	{
		if (this._tacticalSigns != null)
		{
			Creature tacticalTarget = this._tacticalSigns.get(tacticalSignId);
			if (tacticalTarget != null && !tacticalTarget.isInvisible() && tacticalTarget.isTargetable() && !player.isTargetingDisabled())
			{
				player.setTarget(tacticalTarget);
			}
		}
	}

	public void removePartyMember(String name, PartyMessageType type)
	{
		this.removePartyMember(this.getPlayerByName(name), type);
	}

	public void removePartyMember(Player player, PartyMessageType type)
	{
		if (this._members.contains(player))
		{
			boolean isLeader = this.isLeader(player);
			if (!this._disbanding && (this._members.size() == 2 || isLeader && !PlayerConfig.ALT_LEAVE_PARTY_LEADER && type != PartyMessageType.DISCONNECTED))
			{
				this.disbandParty();
				return;
			}

			this._members.remove(player);
			this.recalculatePartyLevel();
			if (player.isInDuel())
			{
				DuelManager.getInstance().onRemoveFromParty(player);
			}

			try
			{
				if (player.isChanneling() && player.getSkillChannelizer().hasChannelized())
				{
					player.abortCast();
				}
				else if (player.isChannelized())
				{
					player.getSkillChannelized().abortChannelization();
				}
			}
			catch (Exception var7)
			{
				LOGGER.log(Level.WARNING, "", var7);
			}

			if (type == PartyMessageType.EXPELLED)
			{
				player.sendPacket(SystemMessageId.YOU_ARE_DISMISSED_FROM_THE_PARTY);
				SystemMessage msg = new SystemMessage(SystemMessageId.C1_IS_DISMISSED_FROM_THE_PARTY);
				msg.addString(player.getAppearance().getVisibleName());
				this.broadcastPacket(msg);
			}
			else if (type == PartyMessageType.LEFT || type == PartyMessageType.DISCONNECTED)
			{
				player.sendPacket(SystemMessageId.YOU_HAVE_LEFT_THE_PARTY);
				SystemMessage msg = new SystemMessage(SystemMessageId.C1_HAS_LEFT_THE_PARTY);
				msg.addString(player.getAppearance().getVisibleName());
				this.broadcastPacket(msg);
			}

			World.getInstance().decrementPartyMember();
			player.sendPacket(PartySmallWindowDeleteAll.STATIC_PACKET);
			player.setParty(null);
			this.broadcastPacket(new PartySmallWindowDelete(player));
			Summon pet = player.getPet();
			if (pet != null)
			{
				this.broadcastPacket(new ExPartyPetWindowDelete(pet));
			}

			player.getServitors().values().forEach(s -> player.sendPacket(new ExPartyPetWindowDelete(s)));
			if (this.isInCommandChannel())
			{
				player.sendPacket(ExCloseMPCC.STATIC_PACKET);
			}

			if (!isLeader || this._members.size() <= 1 || !PlayerConfig.ALT_LEAVE_PARTY_LEADER && type != PartyMessageType.DISCONNECTED)
			{
				if (this._members.size() == 1)
				{
					if (this.isInCommandChannel())
					{
						if (this._commandChannel.getLeader().getObjectId() == this.getLeader().getObjectId())
						{
							this._commandChannel.disbandChannel();
						}
						else
						{
							this._commandChannel.removeParty(this);
						}
					}

					Player leader = this.getLeader();
					if (leader != null)
					{
						this.applyTacticalSigns(leader, true);
						leader.setParty(null);
						if (leader.isInDuel())
						{
							DuelManager.getInstance().onRemoveFromParty(leader);
						}
					}

					if (this._changeDistributionTypeRequestTask != null)
					{
						this._changeDistributionTypeRequestTask.cancel(true);
						this._changeDistributionTypeRequestTask = null;
					}

					if (this._positionBroadcastTask != null)
					{
						this._positionBroadcastTask.cancel(false);
						this._positionBroadcastTask = null;
					}

					this._members.clear();
				}
			}
			else
			{
				SystemMessage msg = new SystemMessage(SystemMessageId.C1_HAS_BECOME_THE_PARTY_LEADER);
				msg.addString(this.getLeader().getAppearance().getVisibleName());
				this.broadcastPacket(msg);
				this.broadcastToPartyMembersNewLeader();
			}

			this.applyTacticalSigns(player, true);
		}
	}

	public void disbandParty()
	{
		this._disbanding = true;
		this.broadcastPacket(new SystemMessage(SystemMessageId.THE_PARTY_IS_DISBANDED));

		for (Player member : this._members)
		{
			if (member != null)
			{
				this.removePartyMember(member, PartyMessageType.NONE);
			}
		}

		World.getInstance().decrementParty();
	}

	public void changePartyLeader(String name)
	{
		this.setLeader(this.getPlayerByName(name));
	}

	@Override
	public void setLeader(Player player)
	{
		if (player != null && !player.isInDuel())
		{
			if (this._members.contains(player))
			{
				if (this.isLeader(player))
				{
					player.sendPacket(SystemMessageId.SLOW_DOWN_YOU_ARE_ALREADY_THE_PARTY_LEADER);
				}
				else
				{
					Player temp = this.getLeader();
					int p1 = this._members.indexOf(player);
					this._members.set(0, player);
					this._members.set(p1, temp);
					SystemMessage msg = new SystemMessage(SystemMessageId.C1_HAS_BECOME_THE_PARTY_LEADER);
					msg.addString(this.getLeader().getName());
					this.broadcastPacket(msg);
					this.broadcastToPartyMembersNewLeader();
					if (this.isInCommandChannel() && this._commandChannel.isLeader(temp))
					{
						this._commandChannel.setLeader(this.getLeader());
						msg = new SystemMessage(SystemMessageId.COMMAND_CHANNEL_AUTHORITY_HAS_BEEN_TRANSFERRED_TO_C1);
						msg.addString(this._commandChannel.getLeader().getAppearance().getVisibleName());
						this._commandChannel.broadcastPacket(msg);
					}
				}
			}
			else
			{
				player.sendPacket(SystemMessageId.YOU_MAY_ONLY_TRANSFER_PARTY_LEADERSHIP_TO_ANOTHER_MEMBER_OF_THE_PARTY);
			}
		}
	}

	public Player getPlayerByName(String name)
	{
		for (Player member : this._members)
		{
			if (member.getAppearance().getVisibleName().equalsIgnoreCase(name))
			{
				return member;
			}
		}

		return null;
	}

	public void distributeItem(Player player, Item item)
	{
		if (item.getId() == 57)
		{
			this.distributeAdena(player, item.getCount(), player);
			ItemManager.destroyItem(ItemProcessType.LOOT, item, player, null);
		}
		else
		{
			Player target = this.getActualLooter(player, item.getId(), false, player);
			target.addItem(ItemProcessType.LOOT, item, player, true);
			if (item.getCount() > 1L)
			{
				SystemMessage msg = new SystemMessage(SystemMessageId.C1_HAS_OBTAINED_S2_X_S3);
				msg.addString(target.getAppearance().getVisibleName());
				msg.addItemName(item);
				msg.addLong(item.getCount());
				this.broadcastToPartyMembers(target, msg);
			}
			else
			{
				SystemMessage msg = new SystemMessage(SystemMessageId.C1_HAS_OBTAINED_S2);
				msg.addString(target.getAppearance().getVisibleName());
				msg.addItemName(item);
				this.broadcastToPartyMembers(target, msg);
			}
		}
	}

	public void distributeItem(Player player, int itemId, long itemCount, boolean spoil, Attackable target)
	{
		if (itemId == 57)
		{
			this.distributeAdena(player, itemCount, target);
		}
		else
		{
			Player looter = this.getActualLooter(player, itemId, spoil, target);
			looter.addItem(spoil ? ItemProcessType.SWEEP : ItemProcessType.LOOT, itemId, itemCount, target, true);
			if (itemCount > 1L)
			{
				SystemMessage msg = spoil ? new SystemMessage(SystemMessageId.C1_HAS_OBTAINED_S3_S2_S_BY_USING_SWEEPER) : new SystemMessage(SystemMessageId.C1_HAS_OBTAINED_S2_X_S3);
				msg.addString(looter.getAppearance().getVisibleName());
				msg.addItemName(itemId);
				msg.addLong(itemCount);
				this.broadcastToPartyMembers(looter, msg);
			}
			else
			{
				SystemMessage msg = spoil ? new SystemMessage(SystemMessageId.C1_HAS_OBTAINED_S2_BY_USING_SWEEPER) : new SystemMessage(SystemMessageId.C1_HAS_OBTAINED_S2);
				msg.addString(looter.getAppearance().getVisibleName());
				msg.addItemName(itemId);
				this.broadcastToPartyMembers(looter, msg);
			}
		}
	}

	public void distributeItem(Player player, ItemHolder item, boolean spoil, Attackable target)
	{
		this.distributeItem(player, item.getId(), item.getCount(), spoil, target);
	}

	public void distributeAdena(Player player, long adena, Creature target)
	{
		List<Player> toReward = new LinkedList<>();

		for (Player member : this._members)
		{
			if (LocationUtil.checkIfInRange(PlayerConfig.ALT_PARTY_RANGE, target, member, true))
			{
				toReward.add(member);
			}
		}

		if (!toReward.isEmpty() && adena > 0L)
		{
			long baseShare = adena / toReward.size();
			long remainder = adena % toReward.size();
			Map<Player, Long> adenaDistribution = new HashMap<>();

			for (Player memberx : toReward)
			{
				adenaDistribution.put(memberx, baseShare);
			}

			if (remainder > 0L)
			{
				int randomIndex = Rnd.get(toReward.size());

				for (int i = 0; i < remainder; i++)
				{
					Player memberx = toReward.get((randomIndex + i) % toReward.size());
					adenaDistribution.put(memberx, adenaDistribution.get(memberx) + 1L);
				}
			}

			for (Entry<Player, Long> entry : adenaDistribution.entrySet())
			{
				long amount = entry.getValue();
				if (amount > 0L)
				{
					entry.getKey().addAdena(ItemProcessType.LOOT, amount, player, true);
				}
			}
		}
	}

	public void distributeXpAndSp(double xpRewardValue, double spRewardValue, List<Player> rewardedMembers, int topLvl, Attackable target)
	{
		List<Player> validMembers = this.getValidMembers(rewardedMembers, topLvl, target);
		double xpReward = xpRewardValue * this.getExpBonus(validMembers.size(), target.getInstanceWorld());
		double spReward = spRewardValue * this.getSpBonus(validMembers.size(), target.getInstanceWorld());
		int sqLevelSum = 0;

		for (Player member : validMembers)
		{
			sqLevelSum += member.getLevel() * member.getLevel();
		}

		for (Player member : rewardedMembers)
		{
			if (!member.isDead())
			{
				if (validMembers.contains(member))
				{
					float penalty = 1.0F;

					for (Summon summon : member.getServitors().values())
					{
						float expMultiplier = summon.asServitor().getExpMultiplier();
						if (expMultiplier > 1.0F)
						{
							penalty = expMultiplier;
							break;
						}
					}

					double sqLevel = member.getLevel() * member.getLevel();
					double preCalculation = sqLevel / sqLevelSum * penalty;
					double exp = member.getStat().getValue(Stat.EXPSP_RATE, xpReward * preCalculation);
					double sp = member.getStat().getValue(Stat.EXPSP_RATE, spReward * preCalculation);
					exp = this.calculateExpSpPartyCutoff(member.asPlayer(), topLvl, exp, sp, target.useVitalityRate());
					if (exp > 0.0)
					{
						Clan clan = member.getClan();
						if (clan != null)
						{
							double finalExp = exp;
							if (target.useVitalityRate())
							{
								finalExp = exp * member.getStat().getExpBonusMultiplier();
							}

							clan.addHuntingPoints(member, target, finalExp);
						}

						member.updateVitalityPoints(target.getVitalityPoints(member.getLevel(), exp, target.isRaid()), true, false);
						PcCafePointsManager.getInstance().givePcCafePoint(member, exp);
						if (MagicLampConfig.ENABLE_MAGIC_LAMP)
						{
							MagicLampManager.getInstance().addLampExp(member, exp, target.getLevel(), true);
						}

						HuntPass huntpass = member.getHuntPass();
						if (huntpass != null)
						{
							huntpass.addPassPoint();
						}

						AchievementBoxHolder box = member.getAchievementBox();
						if (box != null)
						{
							member.getAchievementBox().addPoints(1);
						}
					}
				}
				else
				{
					member.addExpAndSp(0.0, 0.0);
				}
			}
		}
	}

	public double calculateExpSpPartyCutoff(Player player, int topLvl, double addExpValue, double addSpValue, boolean vit)
	{
		double addExp = addExpValue * ClassBalanceConfig.EXP_AMOUNT_MULTIPLIERS[player.getPlayerClass().getId()];
		double addSp = addSpValue * ClassBalanceConfig.SP_AMOUNT_MULTIPLIERS[player.getPlayerClass().getId()];
		if (player.hasPremiumStatus())
		{
			addExp *= PremiumSystemConfig.PREMIUM_RATE_XP;
			addSp *= PremiumSystemConfig.PREMIUM_RATE_SP;
		}

		double xp = addExp;
		if (PlayerConfig.PARTY_XP_CUTOFF_METHOD == PartyExpType.HIGHFIVE)
		{
			int i = 0;
			int levelDiff = topLvl - player.getLevel();

			for (int[] gap : PlayerConfig.PARTY_XP_CUTOFF_GAPS)
			{
				if (levelDiff >= gap[0] && levelDiff <= gap[1])
				{
					xp = addExp * PlayerConfig.PARTY_XP_CUTOFF_GAP_PERCENTS[i] / 100.0;
					double sp = addSp * PlayerConfig.PARTY_XP_CUTOFF_GAP_PERCENTS[i] / 100.0;
					player.addExpAndSp(xp, sp, vit);
					break;
				}

				i++;
			}
		}
		else
		{
			player.addExpAndSp(addExp, addSp, vit);
		}

		return xp;
	}

	public void recalculatePartyLevel()
	{
		int newLevel = 0;

		for (Player member : this._members)
		{
			if (member == null)
			{
				this._members.remove(member);
			}
			else if (member.getLevel() > newLevel)
			{
				newLevel = member.getLevel();
			}
		}

		this._partyLvl = newLevel;
	}

	public List<Player> getValidMembers(List<Player> members, int topLvl, Attackable target)
	{
		List<Player> validMembers = new ArrayList<>();
		switch (PlayerConfig.PARTY_XP_CUTOFF_METHOD)
		{
			case LEVEL:
				for (Player memberxxxxx : members)
				{
					if (target.getInstanceId() == memberxxxxx.getInstanceId() && topLvl - memberxxxxx.getLevel() <= PlayerConfig.PARTY_XP_CUTOFF_LEVEL)
					{
						validMembers.add(memberxxxxx);
					}
				}
				break;
			case PERCENTAGE:
				int sqLevelSum = 0;

				for (Player memberxxx : members)
				{
					if (target.getInstanceId() == memberxxx.getInstanceId())
					{
						sqLevelSum += memberxxx.getLevel() * memberxxx.getLevel();
					}
				}

				for (Player memberxxxx : members)
				{
					int sqLevel = memberxxxx.getLevel() * memberxxxx.getLevel();
					if (target.getInstanceId() == memberxxxx.getInstanceId() && sqLevel * 100 >= sqLevelSum * PlayerConfig.PARTY_XP_CUTOFF_PERCENT)
					{
						validMembers.add(memberxxxx);
					}
				}
				break;
			case AUTO:
				int autoSqLevelSum = 0;

				for (Player memberxx : members)
				{
					autoSqLevelSum += memberxx.getLevel() * memberxx.getLevel();
				}

				int i = members.size() - 1;
				if (i < 1)
				{
					return members;
				}

				if (i >= BONUS_EXP_SP.length)
				{
					i = BONUS_EXP_SP.length - 1;
				}

				for (Player memberxx : members)
				{
					int sqLevel = memberxx.getLevel() * memberxx.getLevel();
					if (target.getInstanceId() == memberxx.getInstanceId() && sqLevel >= autoSqLevelSum / (members.size() * members.size()))
					{
						validMembers.add(memberxx);
					}
				}
				break;
			case HIGHFIVE:
				for (Player memberx : members)
				{
					if (target.getInstanceId() == memberx.getInstanceId())
					{
						validMembers.add(memberx);
					}
				}
				break;
			case NONE:
				for (Player member : members)
				{
					if (target.getInstanceId() == member.getInstanceId())
					{
						validMembers.add(member);
					}
				}
		}

		return validMembers;
	}

	public double getBaseExpSpBonus(int membersCount)
	{
		int i = membersCount - 1;
		if (i < 1)
		{
			return 1.0;
		}
		if (i >= BONUS_EXP_SP.length)
		{
			i = BONUS_EXP_SP.length - 1;
		}

		return BONUS_EXP_SP[i];
	}

	private double getExpBonus(int membersCount, Instance instance)
	{
		float rateMul = instance != null ? instance.getExpPartyRate() : RatesConfig.RATE_PARTY_XP;
		return membersCount < 2 ? this.getBaseExpSpBonus(membersCount) : this.getBaseExpSpBonus(membersCount) * rateMul;
	}

	private double getSpBonus(int membersCount, Instance instance)
	{
		float rateMul = instance != null ? instance.getSPPartyRate() : RatesConfig.RATE_PARTY_SP;
		return membersCount < 2 ? this.getBaseExpSpBonus(membersCount) : this.getBaseExpSpBonus(membersCount) * rateMul;
	}

	@Override
	public int getLevel()
	{
		return this._partyLvl;
	}

	public PartyDistributionType getDistributionType()
	{
		return this._distributionType;
	}

	public boolean isInCommandChannel()
	{
		return this._commandChannel != null;
	}

	public CommandChannel getCommandChannel()
	{
		return this._commandChannel;
	}

	public void setCommandChannel(CommandChannel channel)
	{
		this._commandChannel = channel;
	}

	@Override
	public Player getLeader()
	{
		if (this._members.isEmpty())
		{
			return null;
		}
		try
		{
			return this._members.get(0);
		}
		catch (Exception var3)
		{
			return null;
		}
	}

	public synchronized void requestLootChange(PartyDistributionType partyDistributionType)
	{
		if (this._changeRequestDistributionType == null)
		{
			this._changeRequestDistributionType = partyDistributionType;
			this._changeDistributionTypeAnswers = new HashSet<>();
			this._changeDistributionTypeRequestTask = ThreadPool.schedule(() -> this.finishLootRequest(false), PARTY_DISTRIBUTION_TYPE_REQUEST_TIMEOUT.toMillis());
			this.broadcastToPartyMembers(this.getLeader(), new ExAskModifyPartyLooting(this.getLeader().getAppearance().getVisibleName(), partyDistributionType));
			SystemMessage sm = new SystemMessage(SystemMessageId.REQUESTING_APPROVAL_FOR_CHANGING_PARTY_LOOT_TO_S1);
			sm.addSystemString(partyDistributionType.getSysStringId());
			this.getLeader().sendPacket(sm);
		}
	}

	public synchronized void answerLootChangeRequest(Player member, boolean answer)
	{
		if (this._changeRequestDistributionType != null)
		{
			if (!this._changeDistributionTypeAnswers.contains(member.getObjectId()))
			{
				if (!answer)
				{
					this.finishLootRequest(false);
				}
				else
				{
					this._changeDistributionTypeAnswers.add(member.getObjectId());
					if (this._changeDistributionTypeAnswers.size() >= this.getMemberCount() - 1)
					{
						this.finishLootRequest(true);
					}
				}
			}
		}
	}

	protected synchronized void finishLootRequest(boolean success)
	{
		if (this._changeRequestDistributionType != null)
		{
			if (this._changeDistributionTypeRequestTask != null)
			{
				this._changeDistributionTypeRequestTask.cancel(false);
				this._changeDistributionTypeRequestTask = null;
			}

			if (success)
			{
				this.broadcastPacket(new ExSetPartyLooting(1, this._changeRequestDistributionType));
				this._distributionType = this._changeRequestDistributionType;
				SystemMessage sm = new SystemMessage(SystemMessageId.PARTY_LOOTING_METHOD_WAS_CHANGED_TO_S1);
				sm.addSystemString(this._changeRequestDistributionType.getSysStringId());
				this.broadcastPacket(sm);
			}
			else
			{
				this.broadcastPacket(new ExSetPartyLooting(0, this._distributionType));
				this.broadcastPacket(new SystemMessage(SystemMessageId.PARTY_LOOT_CHANGE_WAS_CANCELLED));
			}

			this._changeRequestDistributionType = null;
			this._changeDistributionTypeAnswers = null;
		}
	}

	@Override
	public List<Player> getMembers()
	{
		return this._members;
	}
}
