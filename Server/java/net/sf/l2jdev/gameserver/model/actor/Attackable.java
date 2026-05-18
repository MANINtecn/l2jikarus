package net.sf.l2jdev.gameserver.model.actor;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.ai.Action;
import net.sf.l2jdev.gameserver.ai.AttackableAI;
import net.sf.l2jdev.gameserver.ai.CreatureAI;
import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.MagicLampConfig;
import net.sf.l2jdev.gameserver.config.NpcConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.config.RatesConfig;
import net.sf.l2jdev.gameserver.config.custom.ChampionMonstersConfig;
import net.sf.l2jdev.gameserver.config.custom.ClassBalanceConfig;
import net.sf.l2jdev.gameserver.config.custom.FakePlayersConfig;
import net.sf.l2jdev.gameserver.config.custom.PremiumSystemConfig;
import net.sf.l2jdev.gameserver.data.xml.ItemData;
import net.sf.l2jdev.gameserver.data.xml.RaidDropAnnounceData;
import net.sf.l2jdev.gameserver.managers.CursedWeaponsManager;
import net.sf.l2jdev.gameserver.managers.MagicLampManager;
import net.sf.l2jdev.gameserver.managers.PcCafePointsManager;
import net.sf.l2jdev.gameserver.managers.WalkingManager;
import net.sf.l2jdev.gameserver.managers.events.EventDropManager;
import net.sf.l2jdev.gameserver.model.AggroInfo;
import net.sf.l2jdev.gameserver.model.DamageDoneInfo;
import net.sf.l2jdev.gameserver.model.ElementalSpirit;
import net.sf.l2jdev.gameserver.model.HuntPass;
import net.sf.l2jdev.gameserver.model.Seed;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Team;
import net.sf.l2jdev.gameserver.model.actor.enums.npc.DropType;
import net.sf.l2jdev.gameserver.model.actor.enums.player.ElementalSpiritType;
import net.sf.l2jdev.gameserver.model.actor.holders.player.AchievementBoxHolder;
import net.sf.l2jdev.gameserver.model.actor.instance.GrandBoss;
import net.sf.l2jdev.gameserver.model.actor.instance.Monster;
import net.sf.l2jdev.gameserver.model.actor.status.AttackableStatus;
import net.sf.l2jdev.gameserver.model.actor.tasks.attackable.CommandChannelTimer;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnAttackableAggroRangeEnter;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnAttackableAttack;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnAttackableKill;
import net.sf.l2jdev.gameserver.model.groups.CommandChannel;
import net.sf.l2jdev.gameserver.model.groups.Party;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.olympiad.Hero;
import net.sf.l2jdev.gameserver.model.skill.CommonSkill;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.SkillCaster;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;
import net.sf.l2jdev.gameserver.model.stats.Stat;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.ChatType;
import net.sf.l2jdev.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2jdev.gameserver.network.serverpackets.ExMagicAttackInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.ExRaidDropItemAnnounce;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.taskmanagers.DecayTaskManager;
import net.sf.l2jdev.gameserver.util.Broadcast;

public class Attackable extends Npc
{
	private boolean _isRaid = false;
	private boolean _isRaidMinion = false;
	private boolean _champion = false;
	private final Map<Creature, AggroInfo> _aggroList = new ConcurrentHashMap<>();
	private boolean _canReturnToSpawnPoint = true;
	private boolean _seeThroughSilentMove = false;
	private boolean _seeded = false;
	private Seed _seed = null;
	private int _seederObjId = 0;
	private final AtomicReference<ItemHolder> _harvestItem = new AtomicReference<>();
	private int _spoilerObjectId;
	private boolean _plundered = false;
	private final AtomicReference<Collection<ItemHolder>> _sweepItems = new AtomicReference<>();
	private boolean _overhit;
	private double _overhitDamage;
	private Creature _overhitAttacker;
	private CommandChannel _firstCommandChannelAttacked = null;
	private CommandChannelTimer _commandChannelTimer = null;
	private long _commandChannelLastAttack = 0L;
	private boolean _mustGiveExpSp;

	public Attackable(NpcTemplate template)
	{
		super(template);
		this.setInstanceType(InstanceType.Attackable);
		this.setInvul(false);
		this._mustGiveExpSp = true;
	}

	@Override
	public AttackableStatus getStatus()
	{
		return (AttackableStatus) super.getStatus();
	}

	@Override
	public void initCharStatus()
	{
		this.setStatus(new AttackableStatus(this));
	}

	@Override
	protected CreatureAI initAI()
	{
		return new AttackableAI(this);
	}

	public Map<Creature, AggroInfo> getAggroList()
	{
		return this._aggroList;
	}

	public boolean canReturnToSpawnPoint()
	{
		return this._canReturnToSpawnPoint;
	}

	public void setCanReturnToSpawnPoint(boolean value)
	{
		this._canReturnToSpawnPoint = value;
	}

	public boolean canSeeThroughSilentMove()
	{
		return this._seeThroughSilentMove;
	}

	public void setSeeThroughSilentMove(boolean value)
	{
		this._seeThroughSilentMove = value;
	}

	public void useMagic(Skill skill)
	{
		if (SkillCaster.checkUseConditions(this, skill))
		{
			WorldObject target = skill.getTarget(this, false, false, false);
			if (target != null)
			{
				this.getAI().setIntention(Intention.CAST, skill, target);
			}
		}
	}

	@Override
	public void reduceCurrentHp(double value, Creature attacker, Skill skill, boolean isDOT, boolean directlyToHp, boolean critical, boolean reflect)
	{
		if (this._isRaid && !this.isMinion() && attacker != null && attacker.getParty() != null && attacker.getParty().isInCommandChannel() && attacker.getParty().getCommandChannel().meetRaidWarCondition(this))
		{
			if (this._firstCommandChannelAttacked == null)
			{
				synchronized (this)
				{
					if (this._firstCommandChannelAttacked == null)
					{
						this._firstCommandChannelAttacked = attacker.getParty().getCommandChannel();
						if (this._firstCommandChannelAttacked != null)
						{
							this._commandChannelTimer = new CommandChannelTimer(this);
							this._commandChannelLastAttack = System.currentTimeMillis();
							ThreadPool.schedule(this._commandChannelTimer, 10000L);
							this._firstCommandChannelAttacked.broadcastPacket(new CreatureSay(null, ChatType.PARTYROOM_ALL, "", "You have looting rights!"));
						}
					}
				}
			}
			else if (attacker.getParty().getCommandChannel().equals(this._firstCommandChannelAttacked))
			{
				this._commandChannelLastAttack = System.currentTimeMillis();
			}
		}

		if (attacker != null)
		{
			this.addDamage(attacker, (int) value, skill);
			if (this._isRaid && this.giveRaidCurse() && !NpcConfig.RAID_DISABLE_CURSE && attacker.getLevel() > this.getLevel() + 8)
			{
				Skill raidCurse = CommonSkill.RAID_CURSE2.getSkill();
				if (raidCurse != null)
				{
					raidCurse.applyEffects(this, attacker);
				}
			}
		}

		if (this.isMonster())
		{
			Monster master = this.asMonster();
			if (master.hasMinions())
			{
				master.getMinionList().onAssist(this, attacker);
			}

			master = master.getLeader();
			if (master != null && master.hasMinions())
			{
				master.getMinionList().onAssist(this, attacker);
			}
		}

		super.reduceCurrentHp(value, attacker, skill, isDOT, directlyToHp, critical, reflect);
	}

	public synchronized void setMustRewardExpSp(boolean value)
	{
		this._mustGiveExpSp = value;
	}

	public synchronized boolean getMustRewardExpSP()
	{
		return this._mustGiveExpSp && !this.isFakePlayer();
	}

	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		if (killer != null)
		{
			Player player = killer.asPlayer();
			if (player != null)
			{
				if (player.getClan() != null && Rnd.get(100) < 2)
				{
					player.getClan().addExp(player.getObjectId(), 1);
				}

				if (EventDispatcher.getInstance().hasListener(EventType.ON_ATTACKABLE_KILL, this))
				{
					EventDispatcher.getInstance().notifyEventAsync(new OnAttackableKill(player, this, killer.isSummon()), this);
				}
			}
		}

		if (this.isMonster())
		{
			Monster mob = this.asMonster();
			Monster leader = mob.getLeader();
			if (leader != null && leader.hasMinions())
			{
				int respawnTime = NpcConfig.MINIONS_RESPAWN_TIME.containsKey(this.getId()) ? NpcConfig.MINIONS_RESPAWN_TIME.get(this.getId()) * 1000 : -1;
				leader.getMinionList().onMinionDie(mob, respawnTime);
			}

			if (mob.hasMinions())
			{
				mob.getMinionList().onMasterDie(false);
			}
		}

		return true;
	}

	@Override
	protected void calculateRewards(Creature lastAttacker)
	{
		try
		{
			if (this._aggroList.isEmpty())
			{
				return;
			}

			Map<Player, DamageDoneInfo> rewards = new ConcurrentHashMap<>();
			Player maxDealer = null;
			long maxDamage = 0L;
			long totalDamage = 0L;

			for (AggroInfo info : this._aggroList.values())
			{
				Player attacker = info.getAttacker().asPlayer();
				if (attacker != null)
				{
					long damage = info.getDamage();
					if (damage > 1L && !(this.calculateDistance3D(attacker) > PlayerConfig.ALT_PARTY_RANGE))
					{
						totalDamage += damage;
						DamageDoneInfo reward = rewards.computeIfAbsent(attacker, DamageDoneInfo::new);
						reward.addDamage(damage);
						if (reward.getDamage() > maxDamage)
						{
							maxDealer = attacker;
							maxDamage = reward.getDamage();
						}
					}
				}
			}

			List<Attackable.PartyContainer> damagingParties = new ArrayList<>();

			for (AggroInfo infox : this._aggroList.values())
			{
				Creature attacker = infox.getAttacker();
				if (attacker != null)
				{
					long totalMemberDamage = 0L;
					Party party = attacker.getParty();
					if (party != null)
					{
						Optional<Attackable.PartyContainer> partyContainerStream = Optional.empty();
						int i = 0;

						for (int damagingPartiesSize = damagingParties.size(); i < damagingPartiesSize; i++)
						{
							Attackable.PartyContainer p = damagingParties.get(i);
							if (p.party == party)
							{
								partyContainerStream = Optional.of(p);
								break;
							}
						}

						Attackable.PartyContainer container = partyContainerStream.orElse(new Attackable.PartyContainer(party, 0L));

						for (Player e : party.getMembers())
						{
							AggroInfo memberAggro = this._aggroList.get(e);
							if (memberAggro != null && memberAggro.getDamage() > 1L)
							{
								totalMemberDamage += memberAggro.getDamage();
							}
						}

						container.damage = totalMemberDamage;
						if (!partyContainerStream.isPresent())
						{
							damagingParties.add(container);
						}
					}
				}
			}

			damagingParties.sort(Comparator.comparingLong(c -> -c.damage));
			Attackable.PartyContainer mostDamageParty = !damagingParties.isEmpty() ? damagingParties.get(0) : null;
			if (this._isRaid && !this._isRaidMinion)
			{
				Player player = maxDealer != null && maxDealer.isOnline() ? maxDealer : lastAttacker.asPlayer();
				this.broadcastPacket(new SystemMessage(SystemMessageId.CONGRATULATIONS_YOUR_RAID_WAS_SUCCESSFUL));
				int raidbossPoints = (int) (this.getTemplate().getRaidPoints() * RatesConfig.RATE_RAIDBOSS_POINTS);
				Party party = player.getParty();
				if (party == null)
				{
					int points = (int) (Math.max(raidbossPoints, 1) * player.getStat().getValue(Stat.BONUS_RAID_POINTS, 1.0));
					player.increaseRaidbossPoints(points);
					player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S1_RAID_POINT_S).addInt(points));
					if (player.isNoble())
					{
						Hero.getInstance().setRBkilled(player.getObjectId(), this.getId());
					}
				}
				else
				{
					CommandChannel command = party.getCommandChannel();
					List<Player> members = new ArrayList<>();
					if (command != null)
					{
						for (Player p : command.getMembers())
						{
							if (p.calculateDistance3D(this) < PlayerConfig.ALT_PARTY_RANGE)
							{
								members.add(p);
							}
						}
					}
					else
					{
						for (Player px : player.getParty().getMembers())
						{
							if (px.calculateDistance3D(this) < PlayerConfig.ALT_PARTY_RANGE)
							{
								members.add(px);
							}
						}
					}

					members.forEach(pxx -> {
						int points = (int) (Math.max(raidbossPoints / members.size(), 1) * pxx.getStat().getValue(Stat.BONUS_RAID_POINTS, 1.0));
						pxx.increaseRaidbossPoints(points);
						pxx.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S1_RAID_POINT_S).addInt(points));
						if (pxx.isNoble())
						{
							Hero.getInstance().setRBkilled(pxx.getObjectId(), this.getId());
						}
					});
				}
			}

			if (mostDamageParty != null && mostDamageParty.damage > maxDamage)
			{
				Player leader = mostDamageParty.party.getLeader();
				this.doItemDrop(leader);
				EventDropManager.getInstance().doEventDrop(leader, this);
			}
			else
			{
				this.doItemDrop((Creature) (maxDealer != null && maxDealer.isOnline() ? maxDealer : lastAttacker));
				EventDropManager.getInstance().doEventDrop(lastAttacker, this);
			}

			if (!this.getMustRewardExpSP())
			{
				return;
			}

			if (!rewards.isEmpty())
			{
				for (DamageDoneInfo reward : rewards.values())
				{
					if (reward != null)
					{
						Player attacker = reward.getAttacker();
						long damage = reward.getDamage();
						Party attackerParty = attacker.getParty();
						float penalty = 1.0F;

						for (Summon summon : attacker.getServitors().values())
						{
							float expMultiplier = summon.asServitor().getExpMultiplier();
							if (expMultiplier > 1.0F)
							{
								penalty = expMultiplier;
								break;
							}
						}

						if (attackerParty != null)
						{
							long partyDmg = 0L;
							double partyMul = 1.0;
							int partyLvl = 0;
							List<Player> rewardedMembers = new ArrayList<>();

							for (Player partyPlayer : attackerParty.isInCommandChannel() ? attackerParty.getCommandChannel().getMembers() : attackerParty.getMembers())
							{
								if (partyPlayer != null && !partyPlayer.isDead())
								{
									DamageDoneInfo reward2 = rewards.get(partyPlayer);
									if (reward2 != null)
									{
										if (this.calculateDistance3D(partyPlayer) < PlayerConfig.ALT_PARTY_RANGE)
										{
											partyDmg += reward2.getDamage();
											rewardedMembers.add(partyPlayer);
											if (partyPlayer.getLevel() > partyLvl)
											{
												if (attackerParty.isInCommandChannel())
												{
													partyLvl = attackerParty.getCommandChannel().getLevel();
												}
												else
												{
													partyLvl = partyPlayer.getLevel();
												}
											}
										}

										rewards.remove(partyPlayer);
									}
									else if (this.calculateDistance3D(partyPlayer) < PlayerConfig.ALT_PARTY_RANGE)
									{
										rewardedMembers.add(partyPlayer);
										if (partyPlayer.getLevel() > partyLvl)
										{
											if (attackerParty.isInCommandChannel())
											{
												partyLvl = attackerParty.getCommandChannel().getLevel();
											}
											else
											{
												partyLvl = partyPlayer.getLevel();
											}
										}
									}
								}
							}

							if (partyDmg < totalDamage)
							{
								partyMul = (double) partyDmg / totalDamage;
							}

							double[] expSp = this.calculateExpAndSp(partyLvl, partyDmg, totalDamage);
							double exp = expSp[0];
							double sp = expSp[1];
							if (ChampionMonstersConfig.CHAMPION_ENABLE && this._champion)
							{
								exp *= ChampionMonstersConfig.CHAMPION_REWARDS_EXP_SP;
								sp *= ChampionMonstersConfig.CHAMPION_REWARDS_EXP_SP;
							}

							exp *= partyMul;
							sp *= partyMul;
							Creature overhitAttacker = this._overhitAttacker;
							if (this._overhit && overhitAttacker != null)
							{
								Player player = overhitAttacker.asPlayer();
								if (player != null && attacker == player)
								{
									attacker.sendPacket(SystemMessageId.OVER_HIT);
									attacker.sendPacket(new ExMagicAttackInfo(overhitAttacker.getObjectId(), this.getObjectId(), 1));
									exp += this.calculateOverhitExp(exp);
								}
							}

							if (partyDmg > 0L)
							{
								attackerParty.distributeXpAndSp(exp, sp, rewardedMembers, partyLvl, this);

								for (Player rewardedMember : rewardedMembers)
								{
									this.rewardAttributeExp(rewardedMember, damage, totalDamage);
								}
							}
						}
						else if (this.isInSurroundingRegion(attacker))
						{
							double[] expSpx = this.calculateExpAndSp(attacker.getLevel(), damage, totalDamage);
							double expx = expSpx[0];
							double spx = expSpx[1];
							if (ChampionMonstersConfig.CHAMPION_ENABLE && this._champion)
							{
								expx *= ChampionMonstersConfig.CHAMPION_REWARDS_EXP_SP;
								spx *= ChampionMonstersConfig.CHAMPION_REWARDS_EXP_SP;
							}

							expx *= penalty;
							Creature overhitAttackerx = this._overhitAttacker;
							if (this._overhit && overhitAttackerx != null)
							{
								Player player = overhitAttackerx.asPlayer();
								if (player != null && attacker == player)
								{
									attacker.sendPacket(SystemMessageId.OVER_HIT);
									attacker.sendPacket(new ExMagicAttackInfo(overhitAttackerx.getObjectId(), this.getObjectId(), 1));
									expx += this.calculateOverhitExp(expx);
								}
							}

							if (!attacker.isDead())
							{
								expx = attacker.getStat().getValue(Stat.EXPSP_RATE, expx) * ClassBalanceConfig.EXP_AMOUNT_MULTIPLIERS[attacker.getPlayerClass().getId()];
								spx = attacker.getStat().getValue(Stat.EXPSP_RATE, spx) * ClassBalanceConfig.SP_AMOUNT_MULTIPLIERS[attacker.getPlayerClass().getId()];
								if (attacker.hasPremiumStatus())
								{
									expx *= PremiumSystemConfig.PREMIUM_RATE_XP;
									spx *= PremiumSystemConfig.PREMIUM_RATE_SP;
								}

								attacker.addExpAndSp(expx, spx, this.useVitalityRate());
								if (expx > 0.0)
								{
									Clan clan = attacker.getClan();
									if (clan != null)
									{
										double finalExp = expx;
										if (this.useVitalityRate())
										{
											finalExp = expx * attacker.getStat().getExpBonusMultiplier();
										}

										clan.addHuntingPoints(attacker, this, finalExp);
									}

									if (this.useVitalityRate())
									{
										if (attacker.getSayhaGraceSupportEndTime() < System.currentTimeMillis())
										{
											attacker.updateVitalityPoints(this.getVitalityPoints(attacker.getLevel(), expx, this._isRaid), true, false);
										}

										PcCafePointsManager.getInstance().givePcCafePoint(attacker, expx);
										if (MagicLampConfig.ENABLE_MAGIC_LAMP)
										{
											MagicLampManager.getInstance().addLampExp(attacker, expx, this.getLevel(), true);
										}

										HuntPass huntPass = attacker.getHuntPass();
										if (huntPass != null)
										{
											attacker.getHuntPass().addPassPoint();
										}

										AchievementBoxHolder box = attacker.getAchievementBox();
										if (box != null)
										{
											attacker.getAchievementBox().addPoints(1);
										}
									}
								}

								this.rewardAttributeExp(attacker, damage, totalDamage);
							}
						}
					}
				}
			}
		}
		catch (Exception var32)
		{
			LOGGER.log(Level.SEVERE, "", var32);
		}
	}

	private void rewardAttributeExp(Player player, long damage, long totalDamage)
	{
		if (player.getInstanceId() == this.getInstanceId())
		{
			if (this.getAttributeExp() > 0L && this.getElementalSpiritType() != ElementalSpiritType.NONE && player.getActiveElementalSpiritType() > 0)
			{
				ElementalSpirit spirit = player.getElementalSpirit(this.getElementalSpiritType().getSuperior());
				if (spirit != null)
				{
					spirit.addExperience((int) (this.getAttributeExp() * damage / totalDamage * player.getElementalSpiritXpBonus()));
				}
			}
		}
	}

	@Override
	public void addAttackerToAttackByList(Creature creature)
	{
		if (creature != null && creature != this)
		{
			for (WeakReference<Creature> ref : this.getAttackByList())
			{
				if (ref.get() == creature)
				{
					return;
				}
			}

			this.getAttackByList().add(new WeakReference<>(creature));
		}
	}

	public Creature getMainDamageDealer()
	{
		if (this._aggroList.isEmpty())
		{
			return null;
		}
		long damage = 0L;
		Creature damageDealer = null;

		for (AggroInfo info : this._aggroList.values())
		{
			if (info != null && info.getDamage() > damage && this.calculateDistance3D(info.getAttacker()) < PlayerConfig.ALT_PARTY_RANGE)
			{
				damage = info.getDamage();
				damageDealer = info.getAttacker();
			}
		}

		return damageDealer;
	}

	public void addDamage(Creature attacker, int damage, Skill skill)
	{
		if (attacker != null)
		{
			if (!this.isDead())
			{
				try
				{
					if (this.isWalker() && !this.isCoreAIDisabled() && WalkingManager.getInstance().isOnWalk(this))
					{
						WalkingManager.getInstance().stopMoving(this, false, true);
					}

					this.getAI().notifyAction(Action.ATTACKED, attacker);
					long hateValue = damage * 100L / (this.getLevel() + 7);
					if (skill == null)
					{
						hateValue = (long) (hateValue * attacker.getStat().getMul(Stat.HATE_ATTACK, 1.0));
					}

					this.addDamageHate(attacker, damage, (int) hateValue);
					Player player = attacker.asPlayer();
					if (player != null && EventDispatcher.getInstance().hasListener(EventType.ON_ATTACKABLE_ATTACK, this))
					{
						EventDispatcher.getInstance().notifyEventAsync(new OnAttackableAttack(player, this, damage, skill, attacker.isSummon()), this);
					}
				}
				catch (Exception var7)
				{
					LOGGER.log(Level.SEVERE, "", var7);
				}
			}
		}
	}

	public void addDamageHate(Creature creature, long damage, long aggroValue)
	{
		Creature attacker = creature;
		if (creature != null && creature != this)
		{
			if (!this.isFakePlayer() || FakePlayersConfig.FAKE_PLAYER_AGGRO_FPC || !creature.isFakePlayer())
			{
				Player targetPlayer = creature.asPlayer();
				Creature summoner = creature.getSummoner();
				if (creature.isNpc() && summoner != null && summoner.isPlayer() && !creature.isTargetable())
				{
					targetPlayer = summoner.asPlayer();
					attacker = summoner;
				}

				AggroInfo ai = this._aggroList.computeIfAbsent(attacker, AggroInfo::new);
				ai.addDamage(damage);
				long aggro = aggroValue;
				if (targetPlayer == null || targetPlayer.getTrap() == null || !targetPlayer.getTrap().isTriggered())
				{
					ai.addHate(aggroValue);
				}

				if (targetPlayer != null && aggroValue == 0L)
				{
					this.addDamageHate(attacker, 0L, 1L);
					if (this.getAI().getIntention() == Intention.IDLE)
					{
						this.getAI().setIntention(Intention.ACTIVE);
					}

					if (EventDispatcher.getInstance().hasListener(EventType.ON_ATTACKABLE_AGGRO_RANGE_ENTER, this))
					{
						EventDispatcher.getInstance().notifyEventAsync(new OnAttackableAggroRangeEnter(this, targetPlayer, attacker.isSummon()), this);
					}
				}
				else if (targetPlayer == null && aggroValue == 0L)
				{
					aggro = 1L;
					ai.addHate(1L);
				}

				if (aggro != 0L && this.getAI().getIntention() == Intention.IDLE)
				{
					this.getAI().setIntention(Intention.ACTIVE);
				}
			}
		}
	}

	public void reduceHate(Creature target, long amount)
	{
		if (target != null)
		{
			AggroInfo ai = this._aggroList.get(target);
			if (ai != null)
			{
				ai.addHate(amount);
				if (ai.getHate() >= 0L && this.getMostHated() == null)
				{
					((AttackableAI) this.getAI()).setGlobalAggro(-25);
					this.clearAggroList();
					this.getAI().setIntention(Intention.ACTIVE);
					if (!this.isFakePlayer())
					{
						this.setWalking();
					}
				}
			}
		}
		else
		{
			Creature mostHated = this.getMostHated();
			if (mostHated == null)
			{
				((AttackableAI) this.getAI()).setGlobalAggro(-25);
			}
			else
			{
				for (AggroInfo ai : this._aggroList.values())
				{
					ai.addHate(amount);
				}

				if (this.getHating(mostHated) >= 0L)
				{
					((AttackableAI) this.getAI()).setGlobalAggro(-25);
					this.clearAggroList();
					this.getAI().setIntention(Intention.ACTIVE);
					if (!this.isFakePlayer())
					{
						this.setWalking();
					}
				}
			}
		}
	}

	public void stopHating(Creature target)
	{
		if (target != null)
		{
			AggroInfo ai = this._aggroList.get(target);
			if (ai != null)
			{
				ai.stopHate();
			}
		}
	}

	public Creature getMostHated()
	{
		if (!this._aggroList.isEmpty() && !this.isAlikeDead())
		{
			Creature mostHated = null;
			long maxHate = 0L;

			for (AggroInfo ai : this._aggroList.values())
			{
				if (ai != null && ai.checkHate(this) > maxHate)
				{
					mostHated = ai.getAttacker();
					maxHate = ai.getHate();
				}
			}

			return mostHated;
		}
		return null;
	}

	public List<Creature> get2MostHated()
	{
		if (!this._aggroList.isEmpty() && !this.isAlikeDead())
		{
			Creature mostHated = null;
			Creature secondMostHated = null;
			long maxHate = 0L;
			List<Creature> result = new ArrayList<>();

			for (AggroInfo ai : this._aggroList.values())
			{
				if (ai.checkHate(this) > maxHate)
				{
					secondMostHated = mostHated;
					mostHated = ai.getAttacker();
					maxHate = ai.getHate();
				}
			}

			result.add(mostHated);
			Creature secondMostHatedFinal = secondMostHated;
			boolean found = false;

			for (WeakReference<Creature> ref : this.getAttackByList())
			{
				if (ref.get() == secondMostHatedFinal)
				{
					found = true;
					break;
				}
			}

			if (found)
			{
				result.add(secondMostHated);
			}
			else
			{
				result.add(null);
			}

			return result;
		}
		return null;
	}

	public List<Creature> getHateList()
	{
		if (!this._aggroList.isEmpty() && !this.isAlikeDead())
		{
			List<Creature> result = new ArrayList<>();

			for (AggroInfo ai : this._aggroList.values())
			{
				ai.checkHate(this);
				result.add(ai.getAttacker());
			}

			return result;
		}
		return null;
	}

	public long getHating(Creature target)
	{
		if (!this._aggroList.isEmpty() && target != null)
		{
			AggroInfo ai = this._aggroList.get(target);
			if (ai == null)
			{
				return 0L;
			}
			if (ai.getAttacker().isPlayer())
			{
				Player act = ai.getAttacker().asPlayer();
				if (act.isInvisible() || act.isInvul() || act.isSpawnProtected())
				{
					this._aggroList.remove(target);
					return 0L;
				}
			}

			if (!ai.getAttacker().isSpawned() || ai.getAttacker().isInvisible())
			{
				this._aggroList.remove(target);
				return 0L;
			}
			else if (ai.getAttacker().isAlikeDead())
			{
				ai.stopHate();
				return 0L;
			}
			else
			{
				return ai.getHate();
			}
		}
		return 0L;
	}

	public void doItemDrop(Creature mainDamageDealer)
	{
		this.doItemDrop(this.getTemplate(), mainDamageDealer);
	}

	public void doItemDrop(NpcTemplate npcTemplate, Creature mainDamageDealer)
	{
		if (mainDamageDealer != null)
		{
			Player player = mainDamageDealer.asPlayer();
			if (player == null)
			{
				if (mainDamageDealer.isFakePlayer() && FakePlayersConfig.FAKE_PLAYER_CAN_DROP_ITEMS)
				{
					Collection<ItemHolder> deathItems = npcTemplate.calculateDrops(DropType.DROP, this, mainDamageDealer);
					if (deathItems != null)
					{
						for (ItemHolder drop : deathItems)
						{
							ItemTemplate item = ItemData.getInstance().getTemplate(drop.getId());
							if (!PlayerConfig.AUTO_LOOT_ITEM_IDS.contains(item.getId()) && !this.isFlying() && (item.hasExImmediateEffect() || (this._isRaid || !PlayerConfig.AUTO_LOOT) && (!this._isRaid || !PlayerConfig.AUTO_LOOT_RAIDS)))
							{
								if (PlayerConfig.AUTO_LOOT_HERBS && item.hasExImmediateEffect())
								{
									for (SkillHolder skillHolder : item.getAllSkills())
									{
										SkillCaster.triggerCast(mainDamageDealer, null, skillHolder.getSkill(), null, false);
									}

									mainDamageDealer.broadcastInfo();
								}
								else
								{
									Item droppedItem = this.dropItem(mainDamageDealer, drop);
									if (FakePlayersConfig.FAKE_PLAYER_CAN_PICKUP)
									{
										mainDamageDealer.getFakePlayerDrops().add(droppedItem);
									}
								}
							}
						}

						deathItems.clear();
					}
				}
			}
			else
			{
				CursedWeaponsManager.getInstance().checkDrop(this, player);
				if (this.isSpoiled() && !this._plundered)
				{
					this._sweepItems.set(npcTemplate.calculateDrops(DropType.SPOIL, this, player));
				}

				Collection<ItemHolder> deathItems = npcTemplate.calculateDrops(DropType.DROP, this, player);
				if (deathItems != null)
				{
					List<Integer> announceItems = null;

					for (ItemHolder dropx : deathItems)
					{
						ItemTemplate item = ItemData.getInstance().getTemplate(dropx.getId());
						if (!PlayerConfig.AUTO_LOOT_ITEM_IDS.contains(item.getId()) && !this.isFlying() && (item.hasExImmediateEffect() || (this._isRaid || !PlayerConfig.AUTO_LOOT) && (!this._isRaid || !PlayerConfig.AUTO_LOOT_RAIDS)) && (!item.hasExImmediateEffect() || !PlayerConfig.AUTO_LOOT_HERBS))
						{
							this.dropItem(player, dropx);
						}
						else
						{
							player.doAutoLoot(this, dropx);
						}

						if (this._isRaid && !this._isRaidMinion)
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.C1_DIED_AND_DROPPED_S2_X_S3);
							sm.addString(this.getName());
							sm.addItemName(item);
							sm.addLong(dropx.getCount());
							this.broadcastPacket(sm);
							if (RaidDropAnnounceData.getInstance().isAnnounce(item.getId()))
							{
								if (announceItems == null)
								{
									announceItems = new ArrayList<>(3);
								}

								if (announceItems.size() < 3)
								{
									announceItems.add(item.getId());
								}
							}
						}
					}

					if (announceItems != null)
					{
						Broadcast.toAllOnlinePlayers(new ExRaidDropItemAnnounce(player.getName(), this.getId(), announceItems));
					}

					deathItems.clear();
				}

				if (this.isAffectedBySkill(CommonSkill.FORTUNE_SEAKER_MARK.getId()))
				{
					Collection<ItemHolder> fortuneItems = npcTemplate.calculateDrops(DropType.FORTUNE, this, player);
					if (fortuneItems != null)
					{
						for (ItemHolder dropx : fortuneItems)
						{
							ItemTemplate itemx = ItemData.getInstance().getTemplate(dropx.getId());
							if (!PlayerConfig.AUTO_LOOT_ITEM_IDS.contains(itemx.getId()) && !this.isFlying() && (itemx.hasExImmediateEffect() || (this._isRaid || !PlayerConfig.AUTO_LOOT) && (!this._isRaid || !PlayerConfig.AUTO_LOOT_RAIDS)) && (!itemx.hasExImmediateEffect() || !PlayerConfig.AUTO_LOOT_HERBS))
							{
								this.dropItem(player, dropx);
							}
							else
							{
								player.doAutoLoot(this, dropx);
							}

							if (this._isRaid && !this._isRaidMinion)
							{
								SystemMessage sm = new SystemMessage(SystemMessageId.THANKS_TO_C1_S_FORTUNE_TIME_EFFECT_S2_X_S3_DROPPED);
								sm.addString(this.getName());
								sm.addItemName(itemx);
								sm.addLong(dropx.getCount());
								this.broadcastPacket(sm);
							}
						}

						fortuneItems.clear();
					}
				}
			}
		}
	}

	public Item getActiveWeapon()
	{
		return null;
	}

	public boolean isInAggroList(Creature creature)
	{
		return creature != null && this._aggroList.containsKey(creature);
	}

	public void clearAggroList()
	{
		this._aggroList.clear();
		this._overhit = false;
		this._overhitDamage = 0.0;
		this._overhitAttacker = null;
	}

	@Override
	public boolean isSweepActive()
	{
		return this._sweepItems.get() != null;
	}

	public List<ItemTemplate> getSpoilLootItems()
	{
		Collection<ItemHolder> sweepItems = this._sweepItems.get();
		List<ItemTemplate> lootItems = new LinkedList<>();
		if (sweepItems != null)
		{
			for (ItemHolder item : sweepItems)
			{
				lootItems.add(ItemData.getInstance().getTemplate(item.getId()));
			}
		}

		return lootItems;
	}

	public Collection<ItemHolder> takeSweep()
	{
		return this._sweepItems.getAndSet(null);
	}

	public ItemHolder takeHarvest()
	{
		return this._harvestItem.getAndSet(null);
	}

	public boolean isOldCorpse(Player attacker, int remainingTime, boolean sendMessage)
	{
		if (this.isDead() && DecayTaskManager.getInstance().getRemainingTime(this) < remainingTime)
		{
			if (sendMessage && attacker != null)
			{
				attacker.sendPacket(SystemMessageId.THE_CORPSE_IS_TOO_OLD_THE_SKILL_CANNOT_BE_USED);
			}

			return true;
		}
		return false;
	}

	public boolean checkSpoilOwner(Player sweeper, boolean sendMessage)
	{
		if (sweeper.getObjectId() != this._spoilerObjectId && !sweeper.isInLooterParty(this._spoilerObjectId))
		{
			if (sendMessage)
			{
				sweeper.sendPacket(SystemMessageId.THERE_ARE_NO_PRIORITY_RIGHTS_ON_A_SWEEPER);
			}

			return false;
		}
		return true;
	}

	public void overhitEnabled(boolean status)
	{
		this._overhit = status;
	}

	public void setOverhitValues(Creature attacker, double damage)
	{
		double overhitDmg = -(this.getCurrentHp() - damage);
		if (overhitDmg < 0.0)
		{
			this.overhitEnabled(false);
			this._overhitDamage = 0.0;
			this._overhitAttacker = null;
		}
		else
		{
			this.overhitEnabled(true);
			this._overhitDamage = overhitDmg;
			this._overhitAttacker = attacker;
		}
	}

	public Creature getOverhitAttacker()
	{
		return this._overhitAttacker;
	}

	public double getOverhitDamage()
	{
		return this._overhitDamage;
	}

	public boolean isOverhit()
	{
		return this._overhit;
	}

	private double[] calculateExpAndSp(int charLevel, long damage, long totalDamage)
	{
		return charLevel - this.getLevel() > 14 ? new double[]
		{
			0.0,
			0.0
		} : new double[]
		{
			Math.max(0.0, this.getExpReward(charLevel) * damage / totalDamage),
			Math.max(0.0, this.getSpReward(charLevel) * damage / totalDamage)
		};
	}

	public double calculateOverhitExp(double exp)
	{
		double overhitPercentage = this._overhitDamage * 100.0 / this.getMaxHp();
		if (overhitPercentage > 25.0)
		{
			overhitPercentage = 25.0;
		}

		return overhitPercentage / 100.0 * exp;
	}

	@Override
	public boolean canBeAttacked()
	{
		return true;
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		this.setSpoilerObjectId(0);
		this.clearAggroList();
		this._harvestItem.set(null);
		this._sweepItems.set(null);
		this._plundered = false;
		if (this.isFakePlayer())
		{
			this.getFakePlayerDrops().clear();
			this.setReputation(0);
			this.setScriptValue(0);
			this.setRunning();
		}
		else
		{
			this.setWalking();
		}

		this._seeded = false;
		this._seed = null;
		this._seederObjId = 0;
		if (this.hasAI())
		{
			this.getAI().setIntention(Intention.ACTIVE);
			if (!this.isInActiveRegion())
			{
				this.getAI().stopAITask();
			}
		}
	}

	@Override
	public void onRespawn()
	{
		this._champion = false;
		if (ChampionMonstersConfig.CHAMPION_ENABLE && this.isMonster() && !this.isQuestMonster() && !this.getTemplate().isUndying() && !this._isRaid && !this._isRaidMinion && ChampionMonstersConfig.CHAMPION_FREQUENCY > 0 && this.getLevel() >= ChampionMonstersConfig.CHAMP_MIN_LEVEL && this.getLevel() <= ChampionMonstersConfig.CHAMP_MAX_LEVEL && (ChampionMonstersConfig.CHAMPION_ENABLE_IN_INSTANCES || this.getInstanceId() == 0))
		{
			if (Rnd.get(100) < ChampionMonstersConfig.CHAMPION_FREQUENCY)
			{
				this._champion = true;
			}

			if (ChampionMonstersConfig.SHOW_CHAMPION_AURA)
			{
				this.setTeam(this._champion ? Team.RED : Team.NONE, false);
			}
		}

		super.onRespawn();
	}

	public boolean isSpoiled()
	{
		return this._spoilerObjectId != 0;
	}

	public int getSpoilerObjectId()
	{
		return this._spoilerObjectId;
	}

	public void setSpoilerObjectId(int spoilerObjectId)
	{
		this._spoilerObjectId = spoilerObjectId;
	}

	public void setPlundered(Player player)
	{
		this._plundered = true;
		this._spoilerObjectId = player.getObjectId();
		this._sweepItems.set(this.getTemplate().calculateDrops(DropType.SPOIL, this, player));
	}

	public void setSeeded(Player seeder)
	{
		if (this._seed != null && this._seederObjId == seeder.getObjectId())
		{
			this._seeded = true;
			int count = 1;

			for (int skillId : this.getTemplate().getSkills().keySet())
			{
				switch (skillId)
				{
					case 4303:
						count *= 2;
						break;
					case 4304:
						count *= 3;
						break;
					case 4305:
						count *= 4;
						break;
					case 4306:
						count *= 5;
						break;
					case 4307:
						count *= 6;
						break;
					case 4308:
						count *= 7;
						break;
					case 4309:
						count *= 8;
						break;
					case 4310:
						count *= 9;
				}
			}

			int diff = this.getLevel() - this._seed.getLevel() - 5;
			if (diff > 0)
			{
				count += diff;
			}

			this._harvestItem.set(new ItemHolder(this._seed.getCropId(), count * RatesConfig.RATE_DROP_MANOR));
		}
	}

	public void setSeeded(Seed seed, Player seeder)
	{
		if (!this._seeded)
		{
			this._seed = seed;
			this._seederObjId = seeder.getObjectId();
		}
	}

	public int getSeederId()
	{
		return this._seederObjId;
	}

	public Seed getSeed()
	{
		return this._seed;
	}

	public boolean isSeeded()
	{
		return this._seeded;
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return GeneralConfig.MAX_MONSTER_ANIMATION > 0 && this.isRandomAnimationEnabled() && !(this instanceof GrandBoss);
	}

	public void setCommandChannelTimer(CommandChannelTimer commandChannelTimer)
	{
		this._commandChannelTimer = commandChannelTimer;
	}

	public CommandChannelTimer getCommandChannelTimer()
	{
		return this._commandChannelTimer;
	}

	public CommandChannel getFirstCommandChannelAttacked()
	{
		return this._firstCommandChannelAttacked;
	}

	public void setFirstCommandChannelAttacked(CommandChannel firstCommandChannelAttacked)
	{
		this._firstCommandChannelAttacked = firstCommandChannelAttacked;
	}

	public long getCommandChannelLastAttack()
	{
		return this._commandChannelLastAttack;
	}

	public void setCommandChannelLastAttack(long channelLastAttack)
	{
		this._commandChannelLastAttack = channelLastAttack;
	}

	public void returnHome()
	{
		this.clearAggroList();
		if (this.hasAI() && this.getSpawn() != null)
		{
			this.getAI().setIntention(Intention.MOVE_TO, this.getSpawn().getLocation());
		}
	}

	public int getVitalityPoints(int level, double exp, boolean isBoss)
	{
		if (this.getLevel() > 0 && !(this.getExpReward(level) <= 0.0) && (!isBoss || NpcConfig.VITALITY_CONSUME_BY_BOSS != 0))
		{
			int points = Math.max((int) (exp / (isBoss ? NpcConfig.VITALITY_CONSUME_BY_BOSS : NpcConfig.VITALITY_CONSUME_BY_MOB) * Math.max(level - this.getLevel(), 1)), level < 40 ? 5 : 100);
			return -points;
		}
		return 0;
	}

	public boolean useVitalityRate()
	{
		return !this._champion || ChampionMonstersConfig.CHAMPION_ENABLE_VITALITY;
	}

	@Override
	public boolean isRaid()
	{
		return this._isRaid;
	}

	public void setIsRaid(boolean isRaid)
	{
		this._isRaid = isRaid;
	}

	public void setIsRaidMinion(boolean value)
	{
		this._isRaid = value;
		this._isRaidMinion = value;
	}

	@Override
	public boolean isRaidMinion()
	{
		return this._isRaidMinion;
	}

	@Override
	public boolean isMinion()
	{
		return this.getLeader() != null;
	}

	public Attackable getLeader()
	{
		return null;
	}

	@Override
	public boolean isChampion()
	{
		return this._champion;
	}

	@Override
	public boolean isAttackable()
	{
		return true;
	}

	@Override
	public Attackable asAttackable()
	{
		return this;
	}

	@Override
	public void setTarget(WorldObject object)
	{
		if (!this.isDead())
		{
			if (object == null)
			{
				WorldObject target = this.getTarget();
				if (target != null)
				{
					this._aggroList.remove(target);
				}

				if (this._aggroList.isEmpty())
				{
					if (this.getAI() instanceof AttackableAI)
					{
						((AttackableAI) this.getAI()).setGlobalAggro(-25);
					}

					if (!this.isFakePlayer())
					{
						this.setWalking();
					}

					this.clearAggroList();
				}

				this.getAI().setIntention(Intention.ACTIVE);
			}

			super.setTarget(object);
		}
	}

	private static class PartyContainer
	{
		public Party party;
		public long damage;

		public PartyContainer(Party party, long damage)
		{
			this.party = party;
			this.damage = damage;
		}
	}
}
