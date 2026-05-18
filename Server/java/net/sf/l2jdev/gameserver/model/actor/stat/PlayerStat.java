package net.sf.l2jdev.gameserver.model.actor.stat;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.config.RatesConfig;
import net.sf.l2jdev.gameserver.data.enums.CategoryType;
import net.sf.l2jdev.gameserver.data.sql.CharInfoTable;
import net.sf.l2jdev.gameserver.data.xml.ExperienceData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.actor.enums.player.ElementalSpiritType;
import net.sf.l2jdev.gameserver.model.actor.holders.player.SubClassHolder;
import net.sf.l2jdev.gameserver.model.actor.instance.Pet;
import net.sf.l2jdev.gameserver.model.actor.transform.Transform;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.effects.EffectType;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerLevelChanged;
import net.sf.l2jdev.gameserver.model.groups.Party;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.holders.ItemSkillHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.item.type.WeaponType;
import net.sf.l2jdev.gameserver.model.skill.AbnormalType;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.stats.Formulas;
import net.sf.l2jdev.gameserver.model.stats.Stat;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.PartySmallWindowUpdateType;
import net.sf.l2jdev.gameserver.network.enums.UserInfoType;
import net.sf.l2jdev.gameserver.network.serverpackets.AcquireSkillList;
import net.sf.l2jdev.gameserver.network.serverpackets.ExVitalityPointInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.ExVoteSystemInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.PartySmallWindowUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.SocialAction;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.TutorialShowQuestionMark;
import net.sf.l2jdev.gameserver.network.serverpackets.dailymission.ExConnectedTimeAndGettableReward;
import net.sf.l2jdev.gameserver.network.serverpackets.dailymission.ExOneDayReceiveRewardList;

public class PlayerStat extends PlayableStat
{
	public static final int MAX_VITALITY_POINTS = 3500000;
	public static final int MIN_VITALITY_POINTS = 0;
	private long _startingXp;
	private final AtomicInteger _talismanSlots = new AtomicInteger();
	private boolean _cloakSlot = false;
	private int _vitalityPoints = 0;
	private ScheduledFuture<?> _onRecalculateStatsTask;
	
	public PlayerStat(Player player)
	{
		super(player);
	}
	
	@Override
	public boolean addExp(long value)
	{
		Player player = this.getActiveChar();
		if (!player.getAccessLevel().canGainExp())
		{
			return false;
		}
		else if (!super.addExp(value))
		{
			return false;
		}
		else
		{
			if (!player.isCursedWeaponEquipped() && player.getReputation() < 0 && (player.isGM() || !player.isInsideZone(ZoneId.PVP)))
			{
				int karmaLost = Formulas.calculateKarmaLost(player, value);
				if (karmaLost > 0)
				{
					player.setReputation(Math.min(player.getReputation() + karmaLost, 0));
				}
			}
			
			player.updateUserInfo();
			return true;
		}
	}
	
	public void addExpAndSp(double addToExpValue, double addToSpValue, boolean useBonuses)
	{
		Player player = this.getActiveChar();
		if (player.getAccessLevel().canGainExp())
		{
			double bonusExp = 1.0;
			double bonusSp = 1.0;
			if (useBonuses)
			{
				if (player.isFishing())
				{
					Item rod = player.getActiveWeaponInstance();
					if (rod != null && rod.getItemType() == WeaponType.FISHINGROD && rod.getTemplate().getAllSkills() != null)
					{
						for (ItemSkillHolder s : rod.getTemplate().getAllSkills())
						{
							if (s.getSkill().getId() == 21484)
							{
								bonusExp *= 1.5;
								bonusSp *= 1.5;
							}
						}
					}
				}
				else
				{
					bonusExp = this.getExpBonusMultiplier();
					bonusSp = this.getSpBonusMultiplier();
				}
			}
			
			double addToExp = addToExpValue * bonusExp;
			double addToSp = addToSpValue * bonusSp;
			double ratioTakenByPlayer = 0.0;
			Summon sPet = player.getPet();
			if (sPet != null && player.calculateDistance3D(sPet) < PlayerConfig.ALT_PARTY_RANGE)
			{
				Pet pet = sPet.asPet();
				ratioTakenByPlayer = pet.getPetLevelData().getOwnerExpTaken() / 100.0F;
				if (ratioTakenByPlayer > 1.0)
				{
					ratioTakenByPlayer = 1.0;
				}
				
				if (!pet.isDead())
				{
					pet.addExpAndSp(addToExp * (1.0 - ratioTakenByPlayer), addToSp * (1.0 - ratioTakenByPlayer));
				}
				
				addToExp *= ratioTakenByPlayer;
				addToSp *= ratioTakenByPlayer;
			}
			
			long finalExp = Math.round(addToExp);
			long finalSp = Math.round(addToSp);
			boolean expAdded = this.addExp(finalExp);
			boolean spAdded = this.addSp(finalSp);
			if (!expAdded && spAdded)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_ACQUIRED_S1_SP);
				sm.addLong(finalSp);
				player.sendPacket(sm);
			}
			else if (expAdded && !spAdded)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_ACQUIRED_S1_XP);
				sm.addLong(finalExp);
				player.sendPacket(sm);
			}
			else if (finalExp > 0L || finalSp > 0L)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_ACQUIRED_S1_XP_BONUS_S2_AND_S3_SP_BONUS_S4);
				sm.addLong(finalExp);
				sm.addLong(Math.round(addToExp - addToExpValue));
				sm.addLong(finalSp);
				sm.addLong(Math.round(addToSp - addToSpValue));
				player.sendPacket(sm);
			}
		}
	}
	
	@Override
	public boolean removeExpAndSp(long addToExp, long addToSp)
	{
		return this.removeExpAndSp(addToExp, addToSp, true);
	}
	
	public boolean removeExpAndSp(long addToExp, long addToSp, boolean sendMessage)
	{
		int level = this.getLevel();
		if (!super.removeExpAndSp(addToExp, addToSp))
		{
			return false;
		}
		if (sendMessage)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_XP_HAS_DECREASED_BY_S1);
			sm.addLong(addToExp);
			Player player = this.getActiveChar();
			player.sendPacket(sm);
			sm = new SystemMessage(SystemMessageId.YOUR_SP_HAS_DECREASED_BY_S1);
			sm.addLong(addToSp);
			player.sendPacket(sm);
			if (this.getLevel() < level)
			{
				player.broadcastStatusUpdate();
			}
		}
		
		return true;
	}
	
	@Override
	public boolean addLevel(int value)
	{
		if (this.getLevel() + value > ExperienceData.getInstance().getMaxLevel() - 1)
		{
			return false;
		}
		Player player = this.getActiveChar();
		boolean levelIncreased = super.addLevel(value);
		if (levelIncreased)
		{
			player.setCurrentCp(this.getMaxCp());
			player.broadcastPacket(new SocialAction(player.getObjectId(), 2122));
			player.sendPacket(SystemMessageId.YOUR_LEVEL_HAS_INCREASED);
			player.notifyFriends(2);
		}
		
		if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_LEVEL_CHANGED, player))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerLevelChanged(player, this.getLevel() - value, this.getLevel()), player);
		}
		
		player.sendPacket(new ExConnectedTimeAndGettableReward(player));
		player.rewardSkills();
		Clan clan = player.getClan();
		if (clan != null)
		{
			clan.updateClanMember(player);
			clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(player));
		}
		
		if (player.isInParty())
		{
			player.getParty().recalculatePartyLevel();
		}
		
		Transform transform = player.getTransformation();
		if (transform != null)
		{
			transform.onLevelUp(player);
		}
		
		Summon sPet = player.getPet();
		if (sPet != null)
		{
			Pet pet = sPet.asPet();
			if (pet.getPetData().isSynchLevel() && pet.getLevel() != this.getLevel())
			{
				int availableLevel = Math.min(pet.getPetData().getMaxLevel(), this.getLevel());
				pet.getStat().setLevel(availableLevel);
				pet.getStat().getExpForLevel(availableLevel);
				pet.setCurrentHp(pet.getMaxHp());
				pet.setCurrentMp(pet.getMaxMp());
				pet.broadcastPacket(new SocialAction(player.getObjectId(), 2122));
				pet.updateAndBroadcastStatus(1);
			}
		}
		
		if ((player.isInCategory(CategoryType.FIRST_CLASS_GROUP) && (player.getLevel() >= 20)) || ((player.isInCategory(CategoryType.SECOND_CLASS_GROUP) || player.isInCategory(CategoryType.FIRST_CLASS_GROUP)) && (player.getLevel() >= 40)) || (player.isInCategory(CategoryType.THIRD_CLASS_GROUP) && (player.getLevel() >= 76)))
		{
			player.sendPacket(new TutorialShowQuestionMark(102, 0));
		}
		
		player.broadcastStatusUpdate();
		player.refreshOverloaded(true);
		player.updateUserInfo();
		player.sendPacket(new AcquireSkillList(player));
		player.sendPacket(new ExVoteSystemInfo(player));
		player.sendPacket(new ExOneDayReceiveRewardList(player, true));
		return levelIncreased;
	}
	
	@Override
	public boolean addSp(long value)
	{
		if (!super.addSp(value))
		{
			return false;
		}
		this.getActiveChar().broadcastUserInfo(UserInfoType.CURRENT_HPMPCP_EXP_SP);
		return true;
	}
	
	@Override
	public long getExpForLevel(int level)
	{
		return ExperienceData.getInstance().getExpForLevel(level);
	}
	
	@Override
	public Player getActiveChar()
	{
		return super.getActiveChar().asPlayer();
	}
	
	@Override
	public long getExp()
	{
		Player player = this.getActiveChar();
		return player.isSubClassActive() ? player.getSubClasses().get(player.getClassIndex()).getExp() : super.getExp();
	}
	
	public long getBaseExp()
	{
		return super.getExp();
	}
	
	@Override
	public void setExp(long value)
	{
		Player player = this.getActiveChar();
		if (player.isSubClassActive())
		{
			player.getSubClasses().get(player.getClassIndex()).setExp(value);
		}
		else
		{
			super.setExp(value);
		}
	}
	
	public void setStartingExp(long value)
	{
		if (GeneralConfig.BOTREPORT_ENABLE)
		{
			this._startingXp = value;
		}
	}
	
	public long getStartingExp()
	{
		return this._startingXp;
	}
	
	public int getTalismanSlots()
	{
		return !this.getActiveChar().hasEnteredWorld() ? 6 : this._talismanSlots.get();
	}
	
	public void addTalismanSlots(int count)
	{
		this._talismanSlots.addAndGet(count);
	}
	
	public boolean canEquipCloak()
	{
		return !this.getActiveChar().hasEnteredWorld() ? true : this._cloakSlot;
	}
	
	public void setCloakSlotStatus(boolean cloakSlot)
	{
		this._cloakSlot = cloakSlot;
	}
	
	@Override
	public int getLevel()
	{
		Player player = this.getActiveChar();
		if (player.isDualClassActive())
		{
			return player.getDualClass().getLevel();
		}
		if (player.isSubClassActive())
		{
			SubClassHolder holder = player.getSubClasses().get(player.getClassIndex());
			if (holder != null)
			{
				return holder.getLevel();
			}
		}
		
		return super.getLevel();
	}
	
	public int getBaseLevel()
	{
		return super.getLevel();
	}
	
	@Override
	public void setLevel(int value)
	{
		int level = value;
		if (value > ExperienceData.getInstance().getMaxLevel() - 1)
		{
			level = ExperienceData.getInstance().getMaxLevel() - 1;
		}
		
		Player player = this.getActiveChar();
		CharInfoTable.getInstance().setLevel(player.getObjectId(), level);
		if (player.isSubClassActive())
		{
			player.getSubClasses().get(player.getClassIndex()).setLevel(level);
		}
		else
		{
			super.setLevel(level);
		}
	}
	
	@Override
	public long getSp()
	{
		Player player = this.getActiveChar();
		return player.isSubClassActive() ? player.getSubClasses().get(player.getClassIndex()).getSp() : super.getSp();
	}
	
	public long getBaseSp()
	{
		return super.getSp();
	}
	
	@Override
	public void setSp(long value)
	{
		Player player = this.getActiveChar();
		if (player.isSubClassActive())
		{
			player.getSubClasses().get(player.getClassIndex()).setSp(value);
		}
		else
		{
			super.setSp(value);
		}
	}
	
	public int getVitalityPoints()
	{
		Player player = this.getActiveChar();
		if (player.isSubClassActive())
		{
			SubClassHolder subClassHolder = player.getSubClasses().get(player.getClassIndex());
			return subClassHolder == null ? 0 : Math.min(3500000, subClassHolder.getVitalityPoints());
		}
		return Math.min(Math.max(this._vitalityPoints, 0), 3500000);
	}
	
	public int getBaseVitalityPoints()
	{
		return Math.min(Math.max(this._vitalityPoints, 0), 3500000);
	}
	
	public double getVitalityExpBonus()
	{
		if (this.getVitalityPoints() > 0)
		{
			return this.getValue(Stat.VITALITY_EXP_RATE, RatesConfig.RATE_VITALITY_EXP_MULTIPLIER);
		}
		return this.getActiveChar().getLimitedSayhaGraceEndTime() > System.currentTimeMillis() ? RatesConfig.RATE_LIMITED_SAYHA_GRACE_EXP_MULTIPLIER : 1.0;
	}
	
	public void setVitalityPoints(int value)
	{
		Player player = this.getActiveChar();
		if (player.isSubClassActive())
		{
			player.getSubClasses().get(player.getClassIndex()).setVitalityPoints(value);
		}
		else
		{
			this._vitalityPoints = Math.min(Math.max(value, 0), 3500000);
			player.sendPacket(new ExVitalityPointInfo(this._vitalityPoints));
		}
	}
	
	public void setVitalityPoints(int value, boolean quiet)
	{
		int points = Math.min(Math.max(value, 0), 3500000);
		if (points != this.getVitalityPoints())
		{
			if (!quiet)
			{
				if (points < this.getVitalityPoints())
				{
					this.getActiveChar().sendPacket(SystemMessageId.YOUR_SAYHA_S_GRACE_HAS_DECREASED);
				}
				else
				{
					this.getActiveChar().sendPacket(SystemMessageId.YOUR_SAYHA_S_GRACE_HAS_INCREASED);
				}
			}
			
			this.setVitalityPoints(points);
			if (points == 0)
			{
				this.getActiveChar().sendPacket(SystemMessageId.YOUR_SAYHA_S_GRACE_IS_FULLY_EXHAUSTED);
			}
			else if (points == 3500000)
			{
				this.getActiveChar().sendPacket(SystemMessageId.YOUR_SAYHA_S_GRACE_IS_AT_MAXIMUM);
			}
			
			Player player = this.getActiveChar();
			player.sendPacket(new ExVitalityPointInfo(this.getVitalityPoints()));
			player.broadcastUserInfo(UserInfoType.VITA_FAME);
			Party party = player.getParty();
			if (party != null)
			{
				PartySmallWindowUpdate partyWindow = new PartySmallWindowUpdate(player, false);
				partyWindow.addComponentType(PartySmallWindowUpdateType.VITALITY_POINTS);
				party.broadcastToPartyMembers(player, partyWindow);
			}
			
			List<Item> items = new LinkedList<>();
			
			for (Item item : player.getInventory().getItems())
			{
				ItemTemplate template = item.getTemplate();
				if (template.hasSkills())
				{
					for (ItemSkillHolder holder : template.getAllSkills())
					{
						if (holder.getSkill().hasEffectType(EffectType.VITALITY_POINT_UP))
						{
							items.add(item);
							break;
						}
					}
				}
			}
			
			if (!items.isEmpty())
			{
				InventoryUpdate iu = new InventoryUpdate();
				iu.addItems(items);
				player.sendInventoryUpdate(iu);
			}
		}
	}
	
	public synchronized void updateVitalityPoints(int value, boolean useRates, boolean quiet)
	{
		if (value != 0 && PlayerConfig.ENABLE_VITALITY)
		{
			int points = value;
			if (useRates)
			{
				if (this.getActiveChar().isLucky())
				{
					return;
				}
				
				if (value < 0)
				{
					double consumeRate = this.getMul(Stat.VITALITY_CONSUME_RATE, 1.0);
					if (consumeRate <= 0.0)
					{
						return;
					}
					
					points = (int) (value * consumeRate);
				}
				
				if (points > 0)
				{
					points = (int) (points * RatesConfig.RATE_VITALITY_GAIN);
				}
				else
				{
					points = (int) (points * RatesConfig.RATE_VITALITY_LOST);
				}
			}
			
			if (points > 0)
			{
				points = Math.min(this.getVitalityPoints() + points, 3500000);
			}
			else
			{
				points = Math.max(this.getVitalityPoints() + points, 0);
			}
			
			if (!(Math.abs(points - this.getVitalityPoints()) <= 1.0E-6))
			{
				this.setVitalityPoints(points);
			}
		}
	}
	
	public double getExpBonusMultiplier()
	{
		double bonus = 1.0;
		double vitality = 1.0;
		double bonusExp = 1.0;
		vitality = this.getVitalityExpBonus();
		bonusExp = 1.0 + this.getValue(Stat.BONUS_EXP, 0.0) / 100.0;
		if (vitality > 1.0)
		{
			bonus += vitality - 1.0;
		}
		
		if (bonusExp > 1.0)
		{
			bonus += bonusExp - 1.0;
		}
		
		bonus = Math.max(bonus, 1.0);
		if (PlayerConfig.MAX_BONUS_EXP > 0.0)
		{
			bonus = Math.min(bonus, PlayerConfig.MAX_BONUS_EXP);
		}
		
		return bonus;
	}
	
	public double getSpBonusMultiplier()
	{
		double bonus = 1.0;
		double vitality = 1.0;
		double bonusSp = 1.0;
		vitality = this.getVitalityExpBonus();
		bonusSp = 1.0 + this.getValue(Stat.BONUS_SP, 0.0) / 100.0;
		if (vitality > 1.0)
		{
			bonus += vitality - 1.0;
		}
		
		if (bonusSp > 1.0)
		{
			bonus += bonusSp - 1.0;
		}
		
		bonus = Math.max(bonus, 1.0);
		if (PlayerConfig.MAX_BONUS_SP > 0.0)
		{
			bonus = Math.min(bonus, PlayerConfig.MAX_BONUS_SP);
		}
		
		return bonus;
	}
	
	public int getBroochJewelSlots()
	{
		return !this.getActiveChar().hasEnteredWorld() ? 6 : (int) this.getValue(Stat.BROOCH_JEWELS, 0.0);
	}
	
	public int getAgathionSlots()
	{
		return !this.getActiveChar().hasEnteredWorld() ? 5 : (int) this.getValue(Stat.AGATHION_SLOTS, 0.0);
	}
	
	public int getArtifactSlots()
	{
		return !this.getActiveChar().hasEnteredWorld() ? 21 : (int) this.getValue(Stat.ARTIFACT_SLOTS, 0.0);
	}
	
	public double getElementalSpiritXpBonus()
	{
		return this.getValue(Stat.ELEMENTAL_SPIRIT_BONUS_EXP, 1.0);
	}
	
	public double getElementalSpiritPower(ElementalSpiritType type, double base)
	{
		return type == null ? 0.0 : this.getValue(type.getAttackStat(), base);
	}
	
	public double getElementalSpiritCriticalRate(int base)
	{
		return this.getValue(Stat.ELEMENTAL_SPIRIT_CRITICAL_RATE, base);
	}
	
	public double getElementalSpiritCriticalDamage(double base)
	{
		return this.getValue(Stat.ELEMENTAL_SPIRIT_CRITICAL_DAMAGE, base);
	}
	
	public double getElementalSpiritDefense(ElementalSpiritType type, double base)
	{
		return type == null ? 0.0 : this.getValue(type.getDefenseStat(), base);
	}
	
	public double getElementSpiritAttack(ElementalSpiritType type, double base)
	{
		return type == null ? 0.0 : this.getValue(type.getAttackStat(), base);
	}
	
	@Override
	public int getReuseTime(Skill skill)
	{
		int addedReuse = 0;
		if (skill.hasEffectType(EffectType.TELEPORT))
		{
			switch (this.getActiveChar().asPlayer().getEinhasadOverseeingLevel())
			{
				case 6:
					addedReuse = 20000;
					break;
				case 7:
					addedReuse = 30000;
					break;
				case 8:
					addedReuse = 40000;
					break;
				case 9:
					addedReuse = 50000;
					break;
				case 10:
					addedReuse = 60000;
			}
		}
		
		return super.getReuseTime(skill) + addedReuse;
	}
	
	@Override
	public void recalculateStats(boolean broadcast)
	{
		if (!this.getActiveChar().isChangingClass())
		{
			super.recalculateStats(broadcast);
		}
	}
	
	@Override
	protected void onRecalculateStats(boolean broadcast)
	{
		if (this._onRecalculateStatsTask == null)
		{
			this._onRecalculateStatsTask = ThreadPool.schedule(() -> {
				super.onRecalculateStats(broadcast);
				this._onRecalculateStatsTask = null;
			}, 50L);
		}
		
		Player player = this.getActiveChar();
		if (player.hasAbnormalType(AbnormalType.ABILITY_CHANGE) && player.hasServitors())
		{
			player.getServitors().values().forEach(servitor -> servitor.getStat().recalculateStats(broadcast));
		}
	}
}
