package net.sf.l2jdev.gameserver.model.actor;

import java.util.concurrent.ScheduledFuture;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.ai.CreatureAI;
import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.ai.SummonAI;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.config.custom.FakePlayersConfig;
import net.sf.l2jdev.gameserver.data.sql.CharSummonTable;
import net.sf.l2jdev.gameserver.data.xml.ExperienceData;
import net.sf.l2jdev.gameserver.data.xml.ItemData;
import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.geoengine.GeoEngine;
import net.sf.l2jdev.gameserver.handler.IItemHandler;
import net.sf.l2jdev.gameserver.handler.ItemHandler;
import net.sf.l2jdev.gameserver.managers.ZoneManager;
import net.sf.l2jdev.gameserver.model.AggroInfo;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Race;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Team;
import net.sf.l2jdev.gameserver.model.actor.stat.SummonStat;
import net.sf.l2jdev.gameserver.model.actor.status.SummonStatus;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.effects.EffectFlag;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerSummonSpawn;
import net.sf.l2jdev.gameserver.model.groups.Party;
import net.sf.l2jdev.gameserver.model.item.EtcItem;
import net.sf.l2jdev.gameserver.model.item.Weapon;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.item.type.ActionType;
import net.sf.l2jdev.gameserver.model.itemcontainer.PetInventory;
import net.sf.l2jdev.gameserver.model.olympiad.OlympiadGameManager;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.SkillCaster;
import net.sf.l2jdev.gameserver.model.skill.targets.TargetType;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.model.zone.ZoneRegion;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.NpcInfoType;
import net.sf.l2jdev.gameserver.network.serverpackets.AbstractMaskPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.ExDamagePopUp;
import net.sf.l2jdev.gameserver.network.serverpackets.ExMagicAttackInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPartyPetWindowAdd;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPartyPetWindowDelete;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPartyPetWindowUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.RelationChanged;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.SummonInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.TeleportToLocation;
import net.sf.l2jdev.gameserver.network.serverpackets.pet.ExPetInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.pet.ExPetSkillList;
import net.sf.l2jdev.gameserver.network.serverpackets.pet.PetDelete;
import net.sf.l2jdev.gameserver.network.serverpackets.pet.PetInventoryUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.pet.PetItemList;
import net.sf.l2jdev.gameserver.network.serverpackets.pet.PetStatusUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.pet.PetSummonInfo;
import net.sf.l2jdev.gameserver.taskmanagers.DecayTaskManager;
import net.sf.l2jdev.gameserver.util.ArrayUtil;

public abstract class Summon extends Playable
{
	private Player _owner;
	private int _attackRange = 36;
	private boolean _follow = true;
	private boolean _previousFollowStatus = true;
	protected boolean _restoreSummon = true;
	private int _summonPoints = 0;
	private ScheduledFuture<?> _abnormalEffectTask;
	private static final int[] PASSIVE_SUMMONS = new int[]
	{
		12564,
		14702,
		14703,
		14704,
		14705,
		14706,
		14707,
		14708,
		14709,
		14710,
		14711,
		14712,
		14713,
		14714,
		14715,
		14716,
		14717,
		14718,
		14719,
		14720,
		14721,
		14722,
		14723,
		14724,
		14725,
		14726,
		14727,
		14728,
		14729,
		14730,
		14731,
		14732,
		14733,
		14734,
		14735,
		14736,
		15955
	};

	public Summon(NpcTemplate template, Player owner)
	{
		super(template);
		this.setInstanceType(InstanceType.Summon);
		this.setInstance(owner.getInstanceWorld());
		this.setShowSummonAnimation(true);
		this._owner = owner;
		this.getAI();
		int x = owner.getX();
		int y = owner.getY();
		int z = owner.getZ();
		Location location = GeoEngine.getInstance().getValidLocation(x, y, z, x + Rnd.get(-100, 100), y + Rnd.get(-100, 100), z, this.getInstanceWorld());
		this.setXYZInvisible(location.getX(), location.getY(), location.getZ());
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		if (PlayerConfig.SUMMON_STORE_SKILL_COOLTIME && !this.isTeleporting())
		{
			this.restoreEffects();
		}

		this.setFollowStatus(true);
		this.updateAndBroadcastStatus(0);
		if (this._owner != null)
		{
			if (this.isPet())
			{
				this.sendPacket(new PetSummonInfo(this, 1));
				this.sendPacket(new ExPetSkillList(true, this.asPet()));
				if (this.getInventory() != null)
				{
					this.sendPacket(new PetItemList(this.getInventory().getItems()));
				}
			}

			this.sendPacket(new RelationChanged(this, this._owner.getRelation(this._owner), false));
			World.getInstance().forEachVisibleObject(this.getOwner(), Player.class, player -> player.sendPacket(new RelationChanged(this, this._owner.getRelation(player), this.isAutoAttackable(player))));
		}

		Party party = this._owner.getParty();
		if (party != null)
		{
			party.broadcastToPartyMembers(this._owner, new ExPartyPetWindowAdd(this));
		}

		this.setShowSummonAnimation(false);
		this._restoreSummon = false;
		this.rechargeShots(true, true, false);
		if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_SUMMON_SPAWN, this))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerSummonSpawn(this), this);
		}
	}

	@Override
	public SummonStat getStat()
	{
		return (SummonStat) super.getStat();
	}

	@Override
	public void initCharStat()
	{
		this.setStat(new SummonStat(this));
	}

	@Override
	public SummonStatus getStatus()
	{
		return (SummonStatus) super.getStatus();
	}

	@Override
	public void initCharStatus()
	{
		this.setStatus(new SummonStatus(this));
	}

	@Override
	protected CreatureAI initAI()
	{
		return new SummonAI(this);
	}

	@Override
	public NpcTemplate getTemplate()
	{
		return (NpcTemplate) super.getTemplate();
	}

	public abstract int getSummonType();

	@Override
	public void stopAllEffects()
	{
		super.stopAllEffects();
		this.updateAndBroadcastStatus(1);
	}

	@Override
	public void stopAllEffectsExceptThoseThatLastThroughDeath()
	{
		super.stopAllEffectsExceptThoseThatLastThroughDeath();
		this.updateAndBroadcastStatus(1);
	}

	@Override
	public void updateAbnormalVisualEffects()
	{
		if (this._abnormalEffectTask == null)
		{
			this._abnormalEffectTask = ThreadPool.schedule(() -> {
				if (this.isSpawned())
				{
					World.getInstance().forEachVisibleObject(this, Player.class, player -> {
						if (player == this._owner)
						{
							player.sendPacket(new PetSummonInfo(this, 1));
						}
						else
						{
							AbstractMaskPacket<NpcInfoType> packet;
							if (this.isPet())
							{
								packet = new ExPetInfo(this, player, 1);
							}
							else
							{
								packet = new SummonInfo(this, player, 1);
							}

							packet.addComponentType(NpcInfoType.ABNORMALS);
							player.sendPacket(packet);
						}
					});
				}

				this._abnormalEffectTask = null;
			}, 50L);
		}
	}

	public boolean isMountable()
	{
		return false;
	}

	public long getExpForThisLevel()
	{
		return this.getLevel() >= ExperienceData.getInstance().getMaxPetLevel() ? 0L : ExperienceData.getInstance().getExpForLevel(this.getLevel());
	}

	public long getExpForNextLevel()
	{
		return this.getLevel() >= ExperienceData.getInstance().getMaxPetLevel() - 1 ? 0L : ExperienceData.getInstance().getExpForLevel(this.getLevel() + 1);
	}

	@Override
	public int getReputation()
	{
		return this._owner != null ? this._owner.getReputation() : 0;
	}

	@Override
	public byte getPvpFlag()
	{
		return this._owner != null ? this._owner.getPvpFlag() : 0;
	}

	@Override
	public Team getTeam()
	{
		return this._owner != null ? this._owner.getTeam() : Team.NONE;
	}

	public Player getOwner()
	{
		return this._owner;
	}

	@Override
	public int getId()
	{
		return this.getTemplate().getId();
	}

	public short getSoulShotsPerHit()
	{
		return this.getTemplate().getSoulShot() > 0 ? (short) this.getTemplate().getSoulShot() : 1;
	}

	public short getSpiritShotsPerHit()
	{
		return this.getTemplate().getSpiritShot() > 0 ? (short) this.getTemplate().getSpiritShot() : 1;
	}

	public void followOwner()
	{
		this.setFollowStatus(true);
	}

	@Override
	public boolean doDie(Creature killer)
	{
		if (this.isNoblesseBlessedAffected())
		{
			this.stopEffects(EffectFlag.NOBLESS_BLESSING);
			this.storeEffect(true);
		}
		else
		{
			this.storeEffect(false);
		}

		if (!super.doDie(killer))
		{
			return false;
		}
		if (this._owner != null)
		{
			World.getInstance().forEachVisibleObject(this, Attackable.class, target -> {
				if (!target.isDead())
				{
					AggroInfo info = target.getAggroList().get(this);
					if (info != null)
					{
						target.addDamageHate(this._owner, info.getDamage(), info.getHate());
					}
				}
			});
		}

		DecayTaskManager.getInstance().add(this);
		return true;
	}

	public boolean doDie(Creature killer, boolean decayed)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		if (!decayed)
		{
			DecayTaskManager.getInstance().add(this);
		}

		return true;
	}

	public void stopDecay()
	{
		DecayTaskManager.getInstance().cancel(this);
	}

	@Override
	public void onDecay()
	{
		this.unSummon(this._owner);
		this.deleteMe(this._owner);
	}

	@Override
	public void broadcastStatusUpdate(Creature caster)
	{
		super.broadcastStatusUpdate(caster);
		this.updateAndBroadcastStatus(1);
	}

	public void deleteMe(Player owner)
	{
		super.deleteMe();
		if (owner != null)
		{
			owner.sendPacket(new PetDelete(this.getSummonType(), this.getObjectId()));
			Party party = owner.getParty();
			if (party != null)
			{
				party.broadcastToPartyMembers(owner, new ExPartyPetWindowDelete(this));
			}

			if (this.isPet())
			{
				owner.setPet(null);
			}
			else
			{
				owner.removeServitor(this.getObjectId());
			}
		}

		if (this.getInventory() != null)
		{
			for (Item item : this.getInventory().getItems())
			{
				World.getInstance().removeObject(item);
			}
		}

		this.decayMe();
		if (!this.isPet())
		{
			CharSummonTable.getInstance().removeServitor(this._owner, this.getObjectId());
		}
	}

	public void unSummon(Player owner)
	{
		if (this.isSpawned())
		{
			if (this.isDead())
			{
				this.stopDecay();
			}

			this.setInvul(true);
			this.abortAttack();
			this.abortCast();
			this.storeMe();
			this.storeEffect(true);
			if (this.hasAI())
			{
				this.getAI().stopAITask();
			}

			this.abortAllSkillCasters();
			this.stopAllEffects();
			this.stopHpMpRegeneration();
			if (owner != null)
			{
				if (this.isPet())
				{
					this.getSkills().forEach((id, skill) -> this.asPet().storePetSkills(id, skill.getLevel()));
					owner.setPet(null);
				}
				else
				{
					owner.removeServitor(this.getObjectId());
				}

				owner.sendPacket(new PetDelete(this.getSummonType(), this.getObjectId()));
				Party party = owner.getParty();
				if (party != null)
				{
					party.broadcastToPartyMembers(owner, new ExPartyPetWindowDelete(this));
				}

				if (this.getInventory() != null && this.getInventory().getSize() > 0)
				{
					this._owner.setPetInvItems(true);
					this.sendPacket(SystemMessageId.CANNOT_BE_SENT_VIA_MAIL_SOLD_AT_A_SHOP_OR_VIA_THE_AUCTION_THE_GUARDIAN_S_INVENTORY_IS_NOT_EMPTY_PLEASE_TAKE_EVERYTHING_FROM_THERE_FIRST);
				}
				else
				{
					this._owner.setPetInvItems(false);
				}
			}

			ZoneRegion oldRegion = ZoneManager.getInstance().getRegion(this);
			this.decayMe();
			oldRegion.removeFromZones(this);
			this.setTarget(null);
			if (owner != null)
			{
				for (int itemId : owner.getAutoSoulShot())
				{
					String handler = ((EtcItem) ItemData.getInstance().getTemplate(itemId)).getHandlerName();
					if (handler != null && handler.contains("Beast"))
					{
						owner.disableAutoShot(itemId);
					}
				}
			}
		}
	}

	public int getAttackRange()
	{
		return this._attackRange;
	}

	public void setAttackRange(int range)
	{
		this._attackRange = range < 36 ? 36 : range;
	}

	public void setFollowStatus(boolean value)
	{
		this._follow = value;
		if (this._follow)
		{
			this.getAI().setIntention(Intention.FOLLOW, this._owner);
		}
		else
		{
			this.getAI().setIntention(Intention.IDLE);
		}
	}

	public boolean getFollowStatus()
	{
		return this._follow;
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return this._owner != null && this._owner.isAutoAttackable(attacker);
	}

	public int getControlObjectId()
	{
		return 0;
	}

	public Weapon getActiveWeapon()
	{
		return null;
	}

	@Override
	public PetInventory getInventory()
	{
		return null;
	}

	public void setRestoreSummon(boolean value)
	{
	}

	@Override
	public Item getActiveWeaponInstance()
	{
		return null;
	}

	@Override
	public Weapon getActiveWeaponItem()
	{
		return null;
	}

	@Override
	public Item getSecondaryWeaponInstance()
	{
		return null;
	}

	@Override
	public Weapon getSecondaryWeaponItem()
	{
		return null;
	}

	@Override
	public boolean isInvul()
	{
		return super.isInvul() || this._owner.isSpawnProtected();
	}

	@Override
	public Party getParty()
	{
		return this._owner == null ? null : this._owner.getParty();
	}

	@Override
	public boolean isInParty()
	{
		return this._owner != null && this._owner.isInParty();
	}

	@Override
	public boolean useMagic(Skill skill, Item item, boolean forceUse, boolean dontMove)
	{
		if (skill == null || this.isDead() || this._owner == null)
		{
			return false;
		}
		else if (skill.isPassive())
		{
			return false;
		}
		else if (this.isCastingNow(SkillCaster::isAnyNormalType))
		{
			return false;
		}
		else
		{
			WorldObject target;
			if (skill.getTargetType() == TargetType.OWNER_PET)
			{
				target = this._owner;
			}
			else
			{
				WorldObject currentTarget = this._owner.getTarget();
				if (currentTarget != null)
				{
					target = skill.getTarget(this, forceUse && (!currentTarget.isPlayable() || !currentTarget.isInsideZone(ZoneId.PEACE) || !currentTarget.isInsideZone(ZoneId.NO_PVP)), dontMove, false);
					Player currentTargetPlayer = currentTarget.asPlayer();
					if (!forceUse && currentTargetPlayer != null && !currentTargetPlayer.isAutoAttackable(this._owner))
					{
						this.sendPacket(SystemMessageId.INVALID_TARGET);
						return false;
					}
				}
				else
				{
					target = skill.getTarget(this, forceUse, dontMove, false);
				}
			}

			if (target == null)
			{
				if (!this.isMovementDisabled())
				{
					this.setTarget(this._owner.getTarget());
					target = skill.getTarget(this, forceUse, dontMove, false);
				}

				if (target == null)
				{
					this.sendPacket(SystemMessageId.YOUR_TARGET_CANNOT_BE_FOUND);
					return false;
				}
			}

			if (this.isSkillDisabled(skill))
			{
				this.sendPacket(SystemMessageId.THAT_SERVITOR_SKILL_CANNOT_BE_USED_BECAUSE_IT_IS_RECHARGING);
				return false;
			}
			else if (this.getCurrentMp() < this.getStat().getMpConsume(skill) + this.getStat().getMpInitialConsume(skill))
			{
				this.sendPacket(SystemMessageId.NOT_ENOUGH_MP);
				return false;
			}
			else if (this.getCurrentHp() <= skill.getHpConsume())
			{
				this.sendPacket(SystemMessageId.NOT_ENOUGH_HP);
				return false;
			}
			else if (!skill.checkCondition(this, target, true))
			{
				this.sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			else if (skill.hasNegativeEffect() && this._owner.isInOlympiadMode() && !this._owner.isOlympiadStart())
			{
				this.sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			else
			{
				this.getAI().setIntention(Intention.CAST, skill, target);
				return true;
			}
		}
	}

	@Override
	public void setImmobilized(boolean value)
	{
		super.setImmobilized(value);
		if (value)
		{
			this._previousFollowStatus = this._follow;
			if (this._previousFollowStatus)
			{
				this.setFollowStatus(false);
			}
		}
		else
		{
			this.setFollowStatus(this._previousFollowStatus);
		}
	}

	public void setOwner(Player newOwner)
	{
		this._owner = newOwner;
	}

	@Override
	public void sendDamageMessage(Creature target, Skill skill, int damage, double elementalDamage, boolean crit, boolean miss, boolean elementalCrit)
	{
		if (!miss && this._owner != null)
		{
			if (target.getObjectId() != this._owner.getObjectId())
			{
				if (crit)
				{
					if (this.isServitor())
					{
						this.sendPacket(SystemMessageId.SUMMONED_MONSTER_S_CRITICAL_HIT);
					}
					else
					{
						this.sendPacket(SystemMessageId.GUARDIAN_S_CRITICAL_HIT);
					}
				}

				if (this._owner.isInOlympiadMode() && target.isPlayer() && target.asPlayer().isInOlympiadMode() && target.asPlayer().getOlympiadGameId() == this._owner.getOlympiadGameId())
				{
					OlympiadGameManager.getInstance().notifyCompetitorDamage(this.getOwner(), damage);
				}

				SystemMessage sm;
				if (target.isHpBlocked() && !target.isNpc() || target.isPlayer() && target.isAffected(EffectFlag.DUELIST_FURY) && !this._owner.isAffected(EffectFlag.FACEOFF))
				{
					if (skill == null)
					{
						this.sendPacket(new ExDamagePopUp(this.getObjectId(), target.getObjectId(), 0, (byte) 12));
					}
					else
					{
						this.sendPacket(new ExMagicAttackInfo(this.getObjectId(), target.getObjectId(), 4));
					}

					sm = new SystemMessage(SystemMessageId.THE_ATTACK_HAS_BEEN_BLOCKED);
				}
				else
				{
					if (crit)
					{
						this.sendPacket(new ExDamagePopUp(this.getObjectId(), target.getObjectId(), damage, (byte) 3));
						target.sendPacket(new ExDamagePopUp(this.getObjectId(), target.getObjectId(), damage, (byte) 3));
					}
					else if (skill != null)
					{
						this.sendPacket(new ExDamagePopUp(this.getObjectId(), target.getObjectId(), damage, (byte) 13));
						target.sendPacket(new ExDamagePopUp(this.getObjectId(), target.getObjectId(), damage, (byte) 13));
					}
					else
					{
						this.sendPacket(new ExDamagePopUp(this.getObjectId(), target.getObjectId(), damage, (byte) 1));
						target.sendPacket(new ExDamagePopUp(this.getObjectId(), target.getObjectId(), damage, (byte) 1));
					}

					sm = new SystemMessage(SystemMessageId.C1_HAS_DEALT_S3_DAMAGE_TO_C2);
					sm.addNpcName(this);
					sm.addString(target.getName());
					sm.addInt(damage);
					sm.addPopup(target.getObjectId(), this.getObjectId(), damage * -1);
				}

				this.sendPacket(sm);
			}
		}
		else
		{
			this.sendPacket(new ExDamagePopUp(this.getObjectId(), target.getObjectId(), 0, (byte) 11));
		}
	}

	@Override
	public void reduceCurrentHp(double damage, Creature attacker, Skill skill)
	{
		super.reduceCurrentHp(damage, attacker, skill);
		if (!this.isDead() && !this.isHpBlocked() && this._owner != null && attacker != null && (!this._owner.isAffected(EffectFlag.DUELIST_FURY) || attacker.isAffected(EffectFlag.FACEOFF)))
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.C1_HAS_RECEIVED_S3_DAMAGE_FROM_C2);
			sm.addNpcName(this);
			sm.addString(attacker.getName());
			sm.addInt((int) damage);
			sm.addPopup(this.getObjectId(), attacker.getObjectId(), (int) (-damage));
			this.sendPacket(sm);
		}
	}

	@Override
	public void doCast(Skill skill)
	{
		if (skill.getTarget(this, false, false, false) == null && !this._owner.getAccessLevel().allowPeaceAttack())
		{
			this._owner.sendPacket(SystemMessageId.THAT_IS_AN_INCORRECT_TARGET);
			this._owner.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			int skillId = skill.getId();
			int replacementSkillId = this.getReplacementSkill(skillId);
			if (skillId != replacementSkillId)
			{
				super.doCast(SkillData.getInstance().getSkill(replacementSkillId, skill.getLevel(), skill.getSubLevel()));
			}
			else
			{
				super.doCast(skill);
			}
		}
	}

	@Override
	public boolean isInCombat()
	{
		return this._owner != null && this._owner.isInCombat();
	}

	@Override
	public Player asPlayer()
	{
		return this._owner;
	}

	public void updateAndBroadcastStatus(int value)
	{
		if (this._owner != null)
		{
			if (this.isSpawned())
			{
				this.sendPacket(new PetSummonInfo(this, value));
				this.sendPacket(new PetStatusUpdate(this));
				this.broadcastNpcInfo(value);
				Party party = this._owner.getParty();
				if (party != null)
				{
					party.broadcastToPartyMembers(this._owner, new ExPartyPetWindowUpdate(this));
				}
			}
		}
	}

	public void broadcastNpcInfo(int value)
	{
		World.getInstance().forEachVisibleObject(this, Player.class, player -> {
			if (player != this._owner)
			{
				if (this.isPet())
				{
					player.sendPacket(new ExPetInfo(this, player, value));
				}
				else
				{
					player.sendPacket(new SummonInfo(this, player, value));
				}
			}
		});
	}

	public boolean isHungry()
	{
		return false;
	}

	public int getWeapon()
	{
		return 0;
	}

	public int getArmor()
	{
		return 0;
	}

	@Override
	public void sendInfo(Player player)
	{
		if (player == this._owner)
		{
			player.sendPacket(new PetSummonInfo(this, this.isDead() ? 0 : 1));
			if (this.isPet())
			{
				player.sendPacket(new PetItemList(this.getInventory().getItems()));
			}
		}
		else if (this.isPet())
		{
			player.sendPacket(new ExPetInfo(this, player, 0));
		}
		else
		{
			player.sendPacket(new SummonInfo(this, player, 0));
		}
	}

	@Override
	public synchronized void onTeleported()
	{
		super.onTeleported();
		this.sendPacket(new TeleportToLocation(this, this.getX(), this.getY(), this.getZ(), this.getHeading()));
	}

	@Override
	public boolean isUndead()
	{
		return this.getTemplate().getRace() == Race.UNDEAD;
	}

	public void switchMode()
	{
	}

	public void cancelAction()
	{
		if (!this.isMovementDisabled())
		{
			this.getAI().setIntention(Intention.ACTIVE);
		}
	}

	public void doAttack(WorldObject target)
	{
		if (this._owner != null && target != null)
		{
			this.setTarget(target);
			this.getAI().setIntention(Intention.ATTACK, target);
			if (target.isFakePlayer() && !FakePlayersConfig.FAKE_PLAYER_AUTO_ATTACKABLE)
			{
				this._owner.updatePvPStatus();
			}
		}
	}

	public boolean canAttack(WorldObject target, boolean ctrlPressed)
	{
		if (this._owner == null)
		{
			return false;
		}
		else if (target != null && this != target && this._owner != target)
		{
			int npcId = this.getId();
			if (ArrayUtil.contains(PASSIVE_SUMMONS, npcId))
			{
				this._owner.sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			else if (this.isBetrayed())
			{
				this.sendPacket(SystemMessageId.YOUR_SERVITOR_IS_UNRESPONSIVE_AND_WILL_NOT_OBEY_ANY_ORDERS);
				this.sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			else if (this.isPet() && this.getLevel() - this._owner.getLevel() > 20)
			{
				this.sendPacket(SystemMessageId.YOUR_GUARDIAN_S_LEVEL_IS_TOO_HIGH_YOU_CANNOT_CONTROL_IT);
				this.sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			else if (this._owner.isInOlympiadMode() && !this._owner.isOlympiadStart())
			{
				this._owner.sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			else if (!this._owner.getAccessLevel().allowPeaceAttack() && this._owner.isInsidePeaceZone(this, target))
			{
				this.sendPacket(SystemMessageId.YOU_CANNOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE);
				return false;
			}
			else if (this.isLockedTarget())
			{
				this.sendPacket(SystemMessageId.FAILED_TO_CHANGE_ENMITY);
				return false;
			}
			else if (!target.isAutoAttackable(this._owner) && !ctrlPressed && !target.isNpc())
			{
				this.setFollowStatus(false);
				this.getAI().setIntention(Intention.FOLLOW, target);
				this.sendPacket(SystemMessageId.INVALID_TARGET);
				return false;
			}
			else
			{
				return !target.isDoor() || this.getTemplate().getRace() == Race.SIEGE_WEAPON;
			}
		}
		else
		{
			return false;
		}
	}

	@Override
	public void sendPacket(ServerPacket packet)
	{
		if (this._owner != null)
		{
			this._owner.sendPacket(packet);
		}
	}

	@Override
	public void sendPacket(SystemMessageId id)
	{
		if (this._owner != null)
		{
			this._owner.sendPacket(id);
		}
	}

	@Override
	public boolean isSummon()
	{
		return true;
	}

	@Override
	public Summon asSummon()
	{
		return this;
	}

	@Override
	public void rechargeShots(boolean physical, boolean magic, boolean fish)
	{
		if (this._owner.getAutoSoulShot() != null && !this._owner.getAutoSoulShot().isEmpty())
		{
			for (int itemId : this._owner.getAutoSoulShot())
			{
				Item item = this._owner.getInventory().getItemByItemId(itemId);
				if (item != null)
				{
					if (magic && (item.getTemplate().getDefaultAction() == ActionType.SPIRITSHOT || item.getTemplate().getDefaultAction() == ActionType.SUMMON_SPIRITSHOT))
					{
						IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
						if (handler != null)
						{
							handler.onItemUse(this._owner, item, false);
						}
					}

					if (physical && (item.getTemplate().getDefaultAction() == ActionType.SOULSHOT || item.getTemplate().getDefaultAction() == ActionType.SUMMON_SOULSHOT))
					{
						IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
						if (handler != null)
						{
							handler.onItemUse(this._owner, item, false);
						}
					}
				}
				else
				{
					this._owner.removeAutoSoulShot(itemId);
				}
			}
		}
	}

	@Override
	public int getClanId()
	{
		return this._owner != null ? this._owner.getClanId() : 0;
	}

	@Override
	public int getAllyId()
	{
		return this._owner != null ? this._owner.getAllyId() : 0;
	}

	public void setSummonPoints(int summonPoints)
	{
		this._summonPoints = summonPoints;
	}

	public int getSummonPoints()
	{
		return this._summonPoints;
	}

	public void sendInventoryUpdate(PetInventoryUpdate iu)
	{
		Player owner = this._owner;
		if (owner != null)
		{
			owner.sendPacket(iu);
			if (this.getInventory() != null)
			{
				owner.sendPacket(new PetItemList(this.getInventory().getItems()));
			}

			owner.sendPacket(new PetSummonInfo(this, 1));
		}
	}

	@Override
	public boolean isMovementDisabled()
	{
		return super.isMovementDisabled() || !this.getTemplate().canMove();
	}

	@Override
	public boolean isTargetable()
	{
		return super.isTargetable() && this.getTemplate().isTargetable();
	}

	@Override
	public boolean isOnEvent()
	{
		return this._owner != null && this._owner.isOnEvent();
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append("(");
		sb.append(this.getId());
		sb.append(") Owner: ");
		sb.append(this._owner);
		return sb.toString();
	}
}
