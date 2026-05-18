package net.sf.l2jdev.gameserver.model.skill;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.ai.Action;
import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.config.NpcConfig;
import net.sf.l2jdev.gameserver.config.custom.ClassBalanceConfig;
import net.sf.l2jdev.gameserver.config.custom.FakePlayersConfig;
import net.sf.l2jdev.gameserver.data.xml.ActionData;
import net.sf.l2jdev.gameserver.data.xml.ItemData;
import net.sf.l2jdev.gameserver.geoengine.GeoEngine;
import net.sf.l2jdev.gameserver.managers.ScriptManager;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Attackable;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.effects.AbstractEffect;
import net.sf.l2jdev.gameserver.model.effects.EffectFlag;
import net.sf.l2jdev.gameserver.model.effects.EffectType;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureSkillFinishCast;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureSkillUse;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcSkillSee;
import net.sf.l2jdev.gameserver.model.events.returns.TerminateReturn;
import net.sf.l2jdev.gameserver.model.item.Weapon;
import net.sf.l2jdev.gameserver.model.item.enums.BodyPart;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.enums.ItemSkillType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemSkillHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.item.type.ActionType;
import net.sf.l2jdev.gameserver.model.options.OptionSkillHolder;
import net.sf.l2jdev.gameserver.model.options.OptionSkillType;
import net.sf.l2jdev.gameserver.model.skill.enums.FlyType;
import net.sf.l2jdev.gameserver.model.skill.enums.NextActionType;
import net.sf.l2jdev.gameserver.model.skill.enums.SoulType;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillUseHolder;
import net.sf.l2jdev.gameserver.model.skill.targets.AffectScope;
import net.sf.l2jdev.gameserver.model.skill.targets.TargetType;
import net.sf.l2jdev.gameserver.model.stats.Formulas;
import net.sf.l2jdev.gameserver.model.stats.Stat;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.StatusUpdateType;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.ExMagicSkillUseGround;
import net.sf.l2jdev.gameserver.network.serverpackets.ExRotation;
import net.sf.l2jdev.gameserver.network.serverpackets.FlyToLocation;
import net.sf.l2jdev.gameserver.network.serverpackets.MagicSkillCanceled;
import net.sf.l2jdev.gameserver.network.serverpackets.MagicSkillLaunched;
import net.sf.l2jdev.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2jdev.gameserver.network.serverpackets.MoveToPawn;
import net.sf.l2jdev.gameserver.network.serverpackets.SetupGauge;
import net.sf.l2jdev.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.util.LocationUtil;

public class SkillCaster implements Runnable
{
	private static final Logger LOGGER = Logger.getLogger(SkillCaster.class.getName());
	private final WeakReference<Creature> _caster;
	private final WeakReference<WorldObject> _target;
	private final Skill _skill;
	private final Item _item;
	private final SkillCastingType _castingType;
	private final boolean _shiftPressed;
	private int _hitTime;
	private int _cancelTime;
	private int _coolTime;
	private Collection<WorldObject> _targets;
	private ScheduledFuture<?> _task;
	private int _phase;

	private SkillCaster(Creature caster, WorldObject target, Skill skill, Item item, SkillCastingType castingType, boolean ctrlPressed, boolean shiftPressed, int castTime)
	{
		Objects.requireNonNull(caster);
		Objects.requireNonNull(skill);
		Objects.requireNonNull(castingType);
		this._caster = new WeakReference<>(caster);
		this._target = new WeakReference<>(target);
		this._skill = skill;
		this._item = item;
		this._castingType = castingType;
		this._shiftPressed = shiftPressed;
		this.calcSkillTiming(caster, skill, castTime);
	}

	public static SkillCaster castSkill(Creature caster, WorldObject target, Skill skill, Item item, SkillCastingType castingType, boolean ctrlPressed, boolean shiftPressed)
	{
		if (caster.isPlayer() && skill.hasNegativeEffect())
		{
			Player player = caster.asPlayer();
			if (player.isInOlympiadMode() && !player.isOlympiadStart())
			{
				return null;
			}
		}

		return castSkill(caster, target, skill, item, castingType, ctrlPressed, shiftPressed, -1);
	}

	public static SkillCaster castSkill(Creature caster, WorldObject worldObject, Skill skill, Item item, SkillCastingType castingType, boolean ctrlPressed, boolean shiftPressed, int castTime)
	{
		if (caster == null || skill == null || castingType == null)
		{
			return null;
		}
		else if (!checkUseConditions(caster, skill, castingType))
		{
			return null;
		}
		else
		{
			WorldObject target = skill.getTarget(caster, worldObject, ctrlPressed, shiftPressed, false);
			if (target == null)
			{
				return null;
			}
			else if (caster.isPlayer() && target.isMonster() && !target.isFakePlayer() && skill.getEffectPoint() > 0 && !ctrlPressed)
			{
				caster.sendPacket(SystemMessageId.INVALID_TARGET);
				return null;
			}
			else if (skill.getCastRange() > 0 && !LocationUtil.checkIfInRange(skill.getCastRange() + (int) caster.getStat().getValue(Stat.MAGIC_ATTACK_RANGE, 0.0), caster, target, false))
			{
				return null;
			}
			else
			{
				SkillCaster skillCaster = new SkillCaster(caster, target, skill, item, castingType, ctrlPressed, shiftPressed, castTime);
				skillCaster.run();
				return skillCaster;
			}
		}
	}

	@Override
	public void run()
	{
		boolean instantCast = this._castingType == SkillCastingType.SIMULTANEOUS || this._skill.isAbnormalInstant() || this._skill.isWithoutAction() || this._skill.isToggle();
		if (instantCast)
		{
			triggerCast(this._caster.get(), this._target.get(), this._skill, this._item, false);
		}
		else
		{
			long nextTaskDelay = 0L;
			boolean hasNextPhase = false;
			switch (this._phase++)
			{
				case 0:
					hasNextPhase = this.startCasting();
					nextTaskDelay = this._hitTime;
					break;
				case 1:
					hasNextPhase = this.launchSkill();
					nextTaskDelay = this._cancelTime;
					break;
				case 2:
					hasNextPhase = this.finishSkill();
					nextTaskDelay = this._coolTime;
			}

			if (hasNextPhase)
			{
				this._task = ThreadPool.schedule(this, nextTaskDelay);
			}
			else
			{
				this.stopCasting(false);
			}
		}
	}

	public boolean startCasting()
	{
		Creature caster = this._caster.get();
		WorldObject target = this._target.get();
		if (caster != null && target != null)
		{
			this._coolTime = Formulas.calcAtkSpd(caster, this._skill, this._skill.getCoolTime());
			int displayedCastTime = this._hitTime + this._cancelTime;
			boolean instantCast = this._castingType == SkillCastingType.SIMULTANEOUS || this._skill.isAbnormalInstant() || this._skill.isWithoutAction();
			if (!instantCast)
			{
				caster.addSkillCaster(this._castingType, this);
			}

			int reuseDelay = caster.getStat().getReuseTime(this._skill);
			if (caster.isPlayable())
			{
				reuseDelay = (int) (reuseDelay * ClassBalanceConfig.SKILL_REUSE_MULTIPLIERS[caster.asPlayer().getPlayerClass().getId()]);
			}

			if (caster.hasAbnormalType(AbnormalType.TIME_DISTORTION))
			{
				reuseDelay += 30000;
			}
			else if (reuseDelay > 10)
			{
				if (Formulas.calcSkillMastery(caster, this._skill) && !this._skill.isStatic() && this._skill.getReferenceItemId() == 0 && this._skill.getOperateType() == SkillOperateType.A1)
				{
					reuseDelay = 100;
					caster.sendPacket(SystemMessageId.A_SKILL_IS_READY_TO_BE_USED_AGAIN);
				}

				if (reuseDelay > 1000)
				{
					caster.addTimeStamp(this._skill, reuseDelay);
				}
				else
				{
					caster.disableSkill(this._skill, reuseDelay);
				}
			}

			if (!instantCast)
			{
				caster.getAI().clientStopMoving(null);
				if (caster.isPlayer() && !this._skill.hasNegativeEffect())
				{
					caster.getAI().setIntention(Intention.IDLE);
				}
			}

			if (this._skill.getReferenceItemId() > 0 && ItemData.getInstance().getTemplate(this._skill.getReferenceItemId()).getBodyPart() == BodyPart.DECO)
			{
				for (Item item : caster.getInventory().getItems())
				{
					if (item.isEquipped() && item.getId() == this._skill.getReferenceItemId())
					{
						item.decreaseMana(false, item.useSkillDisTime());
						break;
					}
				}
			}

			if (target != caster)
			{
				caster.setHeading(LocationUtil.calculateHeadingFrom(caster, target));
				caster.broadcastPacket(new ExRotation(caster.getObjectId(), caster.getHeading()));
				if (caster.isPlayer() && !caster.isCastingNow() && target.isCreature())
				{
					caster.sendPacket(new MoveToPawn(caster, target, (int) caster.calculateDistance2D(target)));
					caster.sendPacket(ActionFailed.STATIC_PACKET);
				}
			}

			if (!this._skill.isWithoutAction())
			{
				caster.stopEffectsOnAction();
			}

			int initmpcons = caster.getStat().getMpInitialConsume(this._skill);
			if (initmpcons > 0)
			{
				if (initmpcons > caster.getCurrentMp())
				{
					caster.sendPacket(SystemMessageId.NOT_ENOUGH_MP);
					return false;
				}

				caster.getStatus().reduceMp(initmpcons);
				StatusUpdate su = new StatusUpdate(caster);
				su.addUpdate(StatusUpdateType.CUR_MP, (int) caster.getCurrentMp());
				caster.sendPacket(su);
			}

			int actionId = caster.isSummon() ? ActionData.getInstance().getSkillActionId(this._skill.getId()) : -1;
			if (!this._skill.isNotBroadcastable())
			{
				caster.broadcastPacket(new MagicSkillUse(caster, target, this._skill.getDisplayId(), this._skill.getDisplayLevel(), displayedCastTime, reuseDelay, this._skill.getReuseDelayGroup(), actionId, this._castingType));
				if (caster.isPlayer() && this._skill.getTargetType() == TargetType.GROUND && this._skill.getAffectScope() == AffectScope.FAN_PB)
				{
					Player player = caster.asPlayer();
					Location worldPosition = player.getCurrentSkillWorldPosition();
					if (worldPosition != null)
					{
						Location location = new Location(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), worldPosition.getHeading());
						ThreadPool.schedule(() -> player.broadcastPacket(new ExMagicSkillUseGround(player.getObjectId(), this._skill.getDisplayId(), location)), 100L);
					}
				}
			}

			if (caster.isPlayer() && !instantCast)
			{
				if (!this._skill.isHidingMessages())
				{
					caster.sendPacket(this._skill.getId() != 2046 ? new SystemMessage(SystemMessageId.YOU_HAVE_USED_S1).addSkillName(this._skill) : new SystemMessage(SystemMessageId.SUMMONING_THE_GUARDIAN));
				}

				caster.sendPacket(new SetupGauge(caster.getObjectId(), 0, displayedCastTime));
			}

			if (this._skill.getItemConsumeId() > 0 && this._skill.getItemConsumeCount() > 0 && caster.getInventory() != null)
			{
				Item requiredItem = caster.getInventory().getItemByItemId(this._skill.getItemConsumeId());
				if (this._skill.hasNegativeEffect() || requiredItem.getTemplate().getDefaultAction() == ActionType.NONE)
				{
					caster.destroyItem(ItemProcessType.NONE, requiredItem.getObjectId(), this._skill.getItemConsumeCount(), caster, false);
				}
			}

			if (caster.isPlayer())
			{
				Player player = caster.asPlayer();
				if (this._skill.getFamePointConsume() > 0)
				{
					if (player.getFame() < this._skill.getFamePointConsume())
					{
						player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_REPUTATION_POINTS);
						return false;
					}

					player.setFame(player.getFame() - this._skill.getFamePointConsume());
					SystemMessage msg = new SystemMessage(SystemMessageId.S1_FAME_HAS_BEEN_CONSUMED);
					msg.addInt(this._skill.getFamePointConsume());
					player.sendPacket(msg);
				}

				if (this._skill.getClanRepConsume() > 0)
				{
					Clan clan = player.getClan();
					if (clan == null || clan.getReputationScore() < this._skill.getClanRepConsume())
					{
						player.sendPacket(SystemMessageId.THE_CLAN_REPUTATION_IS_TOO_LOW);
						return false;
					}

					clan.takeReputationScore(this._skill.getClanRepConsume());
					SystemMessage msg = new SystemMessage(SystemMessageId.S1_CLAN_REPUTATION_POINTS_SPENT);
					msg.addInt(this._skill.getClanRepConsume());
					player.sendPacket(msg);
				}
			}

			if (target.isCreature())
			{
				List<AbstractEffect> effects = this._skill.getEffects(EffectScope.GENERAL);
				if (effects != null && !effects.isEmpty())
				{
					for (AbstractEffect effect : effects)
					{
						if (effect.getEffectType() == EffectType.DUAL_RANGE)
						{
							effect.instant(caster, target.asCreature(), this._skill, null);
							return false;
						}
					}
				}

				this._skill.applyEffectScope(EffectScope.START, new BuffInfo(caster, target.asCreature(), this._skill, false, this._item, null), true, false);
			}

			if (this._skill.isChanneling())
			{
				caster.getSkillChannelizer().startChanneling(this._skill);
			}

			return true;
		}
		return false;
	}

	public boolean launchSkill()
	{
		Creature caster = this._caster.get();
		WorldObject target = this._target.get();
		if (caster != null && target != null)
		{
			if (this._skill.getEffectRange() > 0 && !LocationUtil.checkIfInRange(this._skill.getEffectRange(), caster, target, true))
			{
				if (caster.isPlayer())
				{
					caster.sendPacket(SystemMessageId.THE_DISTANCE_IS_TOO_FAR_AND_SO_THE_CASTING_HAS_BEEN_CANCELLED);
				}

				return false;
			}
			this._targets = this._skill.getTargetsAffected(caster, target);
			if (this._skill.isFlyType())
			{
				this.handleSkillFly(caster, target);
			}

			if (!this._skill.isNotBroadcastable())
			{
				caster.broadcastPacket(new MagicSkillLaunched(caster, this._skill.getDisplayId(), this._skill.getDisplayLevel(), this._castingType, this._targets));
			}

			return true;
		}
		return false;
	}

	public boolean finishSkill()
	{
		Creature caster = this._caster.get();
		WorldObject target = this._target.get();
		if (caster != null && target != null)
		{
			if (this._targets == null)
			{
				this._targets = Collections.singletonList(target);
			}

			StatusUpdate su = new StatusUpdate(caster);
			double mpConsume = this._skill.getMpConsume() > 0 ? caster.getStat().getMpConsume(this._skill) : 0.0;
			if (mpConsume > 0.0)
			{
				if (mpConsume > caster.getCurrentMp())
				{
					caster.sendPacket(SystemMessageId.NOT_ENOUGH_MP);
					return false;
				}

				caster.getStatus().reduceMp(mpConsume);
				su.addUpdate(StatusUpdateType.CUR_MP, (int) caster.getCurrentMp());
			}

			double consumeHp = this._skill.getHpConsume();
			if (consumeHp > 0.0)
			{
				if (consumeHp >= caster.getCurrentHp())
				{
					caster.sendPacket(SystemMessageId.NOT_ENOUGH_HP);
					return false;
				}

				caster.getStatus().reduceHp(consumeHp, caster, true);
				su.addUpdate(StatusUpdateType.CUR_HP, (int) caster.getCurrentHp());
			}

			if (su.hasUpdates())
			{
				caster.sendPacket(su);
			}

			if (caster.isPlayer())
			{
				if ((this._skill.getMaxLightSoulConsumeCount() > 0 && !caster.asPlayer().decreaseSouls(this._skill.getMaxLightSoulConsumeCount(), SoulType.LIGHT)) || (this._skill.getMaxShadowSoulConsumeCount() > 0 && !caster.asPlayer().decreaseSouls(this._skill.getMaxShadowSoulConsumeCount(), SoulType.SHADOW)))
				{
					return false;
				}

				if (this._skill.getChargeConsumeCount() > 0 && !caster.asPlayer().decreaseCharges(this._skill.getChargeConsumeCount()))
				{
					return false;
				}
			}

			if (this._item != null && this._item.getTemplate().getDefaultAction() == ActionType.SKILL_REDUCE_ON_SKILL_SUCCESS && this._skill.getItemConsumeId() > 0 && this._skill.getItemConsumeCount() > 0 && !caster.destroyItem(ItemProcessType.NONE, this._item.getObjectId(), this._skill.getItemConsumeCount(), target, true))
			{
				return false;
			}
			if (EventDispatcher.getInstance().hasListener(EventType.ON_CREATURE_SKILL_FINISH_CAST, caster))
			{
				if (caster.onCreatureSkillFinishCast == null)
				{
					caster.onCreatureSkillFinishCast = new OnCreatureSkillFinishCast();
				}

				caster.onCreatureSkillFinishCast.setCaster(caster);
				caster.onCreatureSkillFinishCast.setTarget(target);
				caster.onCreatureSkillFinishCast.setSkill(this._skill);
				caster.onCreatureSkillFinishCast.setSimultaneously(this._skill.isWithoutAction());
				EventDispatcher.getInstance().notifyEvent(caster.onCreatureSkillFinishCast, caster);
			}

			callSkill(caster, target, this._targets, this._skill, this._item);
			if (!this._skill.isWithoutAction() && this._skill.hasNegativeEffect() && this._skill.getTargetType() != TargetType.DOOR_TREASURE)
			{
				caster.getAI().clientStartAutoAttack();
			}

			caster.notifyQuestEventSkillFinished(this._skill, target);
			caster.rechargeShots(this._skill.useSoulShot(), this._skill.useSpiritShot(), false);
			if (caster.isPlayer() && this._skill.getTargetType() == TargetType.GROUND && (this._skill.getAffectScope() == AffectScope.FAN_PB || this._skill.getAffectScope() == AffectScope.FAN))
			{
				caster.asPlayer().setCurrentSkillWorldPosition(null);
			}

			return true;
		}
		return false;
	}

	public static void callSkill(Creature caster, WorldObject target, Collection<WorldObject> targets, Skill skill, Item item)
	{
		try
		{
			if ((skill.hasNegativeEffect() && caster.isDisabled()) || (skill.isToggle() && caster.isAffectedBySkill(skill.getId())))
			{
				return;
			}

			for (WorldObject obj : targets)
			{
				if (obj != null && obj.isCreature())
				{
					Creature creature = obj.asCreature();
					if (!NpcConfig.RAID_DISABLE_CURSE && creature.isRaid() && creature.giveRaidCurse() && caster.getLevel() >= creature.getLevel() + 9 && (skill.hasNegativeEffect() || creature.getTarget() == caster && creature.asAttackable().getAggroList().containsKey(caster)))
					{
						CommonSkill curse = skill.hasNegativeEffect() ? CommonSkill.RAID_CURSE2 : CommonSkill.RAID_CURSE;
						Skill curseSkill = curse.getSkill();
						if (curseSkill != null)
						{
							curseSkill.applyEffects(creature, caster);
						}
					}

					if (!skill.isStatic())
					{
						Weapon activeWeapon = caster.getActiveWeaponItem();
						if (activeWeapon != null && !creature.isDead())
						{
							activeWeapon.applyConditionalSkills(caster, creature, skill, ItemSkillType.ON_MAGIC_SKILL);
						}

						if (caster.hasTriggerSkills())
						{
							for (OptionSkillHolder holder : caster.getTriggerSkills().values())
							{
								if ((skill.isMagic() && holder.getSkillType() == OptionSkillType.MAGIC || skill.isPhysical() && holder.getSkillType() == OptionSkillType.ATTACK) && Rnd.get(100) < holder.getChance())
								{
									triggerCast(caster, creature, holder.getSkill(), null, false);
								}
							}
						}
					}
				}
			}

			skill.activateSkill(caster, item, targets);
			Player player = caster.asPlayer();
			if (player != null)
			{
				for (WorldObject objx : targets)
				{
					if (objx.isCreature())
					{
						if (skill.hasNegativeEffect())
						{
							if (objx.isPlayable())
							{
								player.updatePvPStatus(objx.asCreature());
								if (objx.isSummon())
								{
									objx.asSummon().updateAndBroadcastStatus(1);
								}
							}
							else if (objx.isAttackable())
							{
								objx.asAttackable().addDamageHate(caster, 0L, -skill.getEffectPoint());
								objx.asCreature().addAttackerToAttackByList(caster);
								if (objx.isFakePlayer() && !FakePlayersConfig.FAKE_PLAYER_AUTO_ATTACKABLE && (!objx.isServitor() || objx.getObjectId() != player.getFirstServitor().getObjectId()))
								{
									player.updatePvPStatus();
								}
							}

							if (objx.asCreature().hasAI() && !skill.hasEffectType(EffectType.HATE))
							{
								objx.asCreature().getAI().notifyAction(Action.ATTACKED, caster);
							}
						}
						else if (objx != player && (skill.getEffectPoint() > 0 && objx.isMonster() || objx.isPlayable() && (objx.asPlayer().getPvpFlag() > 0 || objx.asCreature().getReputation() < 0)) && (!objx.isFakePlayer() || objx.isFakePlayer() && !FakePlayersConfig.FAKE_PLAYER_AUTO_ATTACKABLE && (!objx.asNpc().isScriptValue(0) || objx.asNpc().getReputation() < 0)))
						{
							player.updatePvPStatus();
						}
					}
				}

				World.getInstance().forEachVisibleObjectInRange(player, Npc.class, 1000, npcMob -> {
					if (EventDispatcher.getInstance().hasListener(EventType.ON_NPC_SKILL_SEE, npcMob))
					{
						EventDispatcher.getInstance().notifyEventAsync(new OnNpcSkillSee(npcMob, player, skill, caster.isSummon(), targets), npcMob);
					}

					if (npcMob.isAttackable() && !npcMob.isFakePlayer())
					{
						Attackable attackable = npcMob.asAttackable();
						if (skill.getEffectPoint() > 0 && attackable.hasAI() && attackable.getAI().getIntention() == Intention.ATTACK)
						{
							WorldObject npcTarget = attackable.getTarget();

							for (WorldObject skillTarget : targets)
							{
								if (npcTarget == skillTarget || npcMob == skillTarget)
								{
									Creature originalCaster = caster.isSummon() ? caster : player;
									attackable.addDamageHate(originalCaster, 0L, skill.getEffectPoint() * 150 / (attackable.getLevel() + 7));
								}
							}
						}
					}
				});
			}
			else if (caster.isFakePlayer() && !FakePlayersConfig.FAKE_PLAYER_AUTO_ATTACKABLE && (target.isPlayable() || target.isFakePlayer()))
			{
				Npc npc = caster.asNpc();
				if (!npc.isScriptValue(1))
				{
					npc.setScriptValue(1);
					npc.broadcastInfo();
					ScriptManager.getInstance().getScript("PvpFlaggingStopTask").notifyEvent("FLAG_CHECK", npc, null);
				}
			}
		}
		catch (Exception var11)
		{
			LOGGER.log(Level.WARNING, caster + " callSkill() failed.", var11);
		}
	}

	public void stopCasting(boolean aborted)
	{
		if (this._task != null)
		{
			this._task.cancel(false);
			this._task = null;
		}

		Creature caster = this._caster.get();
		WorldObject target = this._target.get();
		if (caster != null)
		{
			caster.removeSkillCaster(this._castingType);
			if (caster.isChanneling())
			{
				caster.getSkillChannelizer().stopChanneling();
			}

			if (aborted)
			{
				caster.broadcastPacket(new MagicSkillCanceled(caster.getObjectId()));
				caster.sendPacket(ActionFailed.get(this._castingType));
			}

			if (caster.isPlayer())
			{
				Player currPlayer = caster.asPlayer();
				SkillUseHolder queuedSkill = currPlayer.getQueuedSkill();
				if (queuedSkill != null)
				{
					ThreadPool.execute(() -> {
						currPlayer.setQueuedSkill(null, null, false, false);
						currPlayer.useMagic(queuedSkill.getSkill(), queuedSkill.getItem(), queuedSkill.isCtrlPressed(), queuedSkill.isShiftPressed());
					});
					return;
				}
			}

			if (this._skill.getNextAction() != NextActionType.NONE && caster.getAI().getNextIntention() == null)
			{
				if (this._skill.getNextAction() != NextActionType.ATTACK || target == null || target == caster || !target.isAutoAttackable(caster) || this._shiftPressed)
				{
					if (this._skill.getNextAction() == NextActionType.CAST && target != null && target != caster && target.isAutoAttackable(caster))
					{
						caster.getAI().setIntention(Intention.CAST, this._skill, target, this._item, false, false);
					}
					else
					{
						caster.getAI().notifyAction(Action.FINISH_CASTING);
					}
				}
				else if (caster.isPlayer() && caster.asPlayer().isAutoPlaying())
				{
					caster.getAI().notifyAction(Action.FINISH_CASTING);
				}
				else
				{
					caster.getAI().setIntention(Intention.ATTACK, target);
				}
			}
			else
			{
				caster.getAI().notifyAction(Action.FINISH_CASTING);
			}
		}
	}

	private void calcSkillTiming(Creature creature, Skill skill, int castTime)
	{
		double timeFactor = Formulas.calcSkillTimeFactor(creature, skill);
		double cancelTime = Formulas.calcSkillCancelTime(creature, skill);
		if (skill.getOperateType().isChanneling())
		{
			this._hitTime = (int) Math.max(skill.getHitTime() - cancelTime, 0.0);
			this._cancelTime = 2866;
		}
		else
		{
			int addedTime = 0;
			if (skill.hasEffectType(EffectType.TELEPORT) && creature.isPlayer())
			{
				switch (creature.asPlayer().getEinhasadOverseeingLevel())
				{
					case 6:
						addedTime = 2000;
						break;
					case 7:
						addedTime = 3000;
						break;
					case 8:
						addedTime = 4000;
						break;
					case 9:
						addedTime = 5000;
						break;
					case 10:
						addedTime = 6000;
				}
			}

			if (castTime > -1)
			{
				this._hitTime = (int) Math.max(castTime / timeFactor - cancelTime, 0.0) + addedTime;
			}
			else
			{
				this._hitTime = (int) Math.max(skill.getHitTime() / timeFactor - cancelTime, 0.0) + addedTime;
			}

			this._cancelTime = (int) cancelTime;
		}

		this._coolTime = (int) (skill.getCoolTime() / timeFactor);
	}

	public static void triggerCast(Creature creature, Creature target, Skill skill)
	{
		triggerCast(creature, target, skill, null, true);
	}

	public static void triggerCast(Creature creature, WorldObject target, Skill skill, Item item, boolean ignoreTargetType)
	{
		if (target == null)
		{
			creature.addTriggerCast(new TriggerCastInfo(creature, target, skill, item, ignoreTargetType));
		}
		else
		{
			target.addTriggerCast(new TriggerCastInfo(creature, target, skill, item, ignoreTargetType));
		}
	}

	public static void triggerCast(TriggerCastInfo info)
	{
		Creature creature = info.getCreature();
		WorldObject target = info.getTarget();
		Skill skill = info.getSkill();
		Item item = info.getItem();
		boolean ignoreTargetType = info.isIgnoreTargetType();

		try
		{
			if (creature == null || skill == null)
			{
				return;
			}

			if (skill.checkCondition(creature, target, true))
			{
				if (creature.isSkillDisabled(skill))
				{
					return;
				}

				if (skill.getReuseDelay() > 0)
				{
					creature.disableSkill(skill, skill.getReuseDelay());
				}

				WorldObject currentTarget = target;
				if (!ignoreTargetType)
				{
					WorldObject objTarget = skill.getTarget(creature, false, false, false);
					if (objTarget == null)
					{
						return;
					}

					if (objTarget.isCreature())
					{
						currentTarget = objTarget;
					}
				}

				List<WorldObject> targets = skill.getTargetsAffected(creature, currentTarget);
				if (!skill.isNotBroadcastable() && !creature.isChanneling())
				{
					creature.broadcastPacket(new MagicSkillUse(creature, currentTarget, skill.getDisplayId(), skill.getLevel(), 0, 0));
				}

				skill.activateSkill(creature, item, targets);
				if (EventDispatcher.getInstance().hasListener(EventType.ON_CREATURE_SKILL_FINISH_CAST, creature))
				{
					if (creature.onCreatureSkillFinishCast == null)
					{
						creature.onCreatureSkillFinishCast = new OnCreatureSkillFinishCast();
					}

					creature.onCreatureSkillFinishCast.setCaster(creature);
					creature.onCreatureSkillFinishCast.setTarget(target);
					creature.onCreatureSkillFinishCast.setSkill(skill);
					creature.onCreatureSkillFinishCast.setSimultaneously(skill.isWithoutAction());
					EventDispatcher.getInstance().notifyEvent(creature.onCreatureSkillFinishCast, creature);
				}
			}
		}
		catch (Exception var8)
		{
			LOGGER.log(Level.WARNING, "Failed simultaneous cast: ", var8);
		}
	}

	public Skill getSkill()
	{
		return this._skill;
	}

	public Creature getCaster()
	{
		return this._caster.get();
	}

	public WorldObject getTarget()
	{
		return this._target.get();
	}

	public Item getItem()
	{
		return this._item;
	}

	public boolean canAbortCast()
	{
		return this.getCaster().getTarget() == null;
	}

	public SkillCastingType getCastingType()
	{
		return this._castingType;
	}

	public boolean isNormalFirstType()
	{
		return this._castingType == SkillCastingType.NORMAL;
	}

	public boolean isNormalSecondType()
	{
		return this._castingType == SkillCastingType.NORMAL_SECOND;
	}

	public boolean isAnyNormalType()
	{
		return this._castingType == SkillCastingType.NORMAL || this._castingType == SkillCastingType.NORMAL_SECOND;
	}

	@Override
	public String toString()
	{
		return super.toString() + " [caster: " + this._caster.get() + " skill: " + this._skill + " target: " + this._target.get() + " type: " + this._castingType + "]";
	}

	public static boolean checkUseConditions(Creature caster, Skill skill)
	{
		return checkUseConditions(caster, skill, SkillCastingType.NORMAL);
	}

	public static boolean checkUseConditions(Creature caster, Skill skill, SkillCastingType castingType)
	{
		if (caster == null)
		{
			return false;
		}
		else if (skill != null && !caster.isSkillDisabled(skill) && (!skill.isFlyType() || !caster.isMovementDisabled()))
		{
			if (EventDispatcher.getInstance().hasListener(EventType.ON_CREATURE_SKILL_USE, caster))
			{
				if (caster.onCreatureSkillUse == null)
				{
					caster.onCreatureSkillUse = new OnCreatureSkillUse();
				}

				caster.onCreatureSkillUse.setCaster(caster);
				caster.onCreatureSkillUse.setSkill(skill);
				caster.onCreatureSkillUse.setSimultaneously(skill.isWithoutAction());
				TerminateReturn term = EventDispatcher.getInstance().notifyEvent(caster.onCreatureSkillUse, caster, TerminateReturn.class);
				if (term != null && term.terminate())
				{
					caster.sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}

			if (castingType != null && caster.isCastingNow(castingType))
			{
				caster.sendPacket(ActionFailed.get(castingType));
				return false;
			}
			else if (caster.getCurrentMp() < caster.getStat().getMpConsume(skill) + caster.getStat().getMpInitialConsume(skill))
			{
				caster.sendPacket(SystemMessageId.NOT_ENOUGH_MP);
				caster.sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			else if (caster.getCurrentHp() <= skill.getHpConsume())
			{
				caster.sendPacket(SystemMessageId.NOT_ENOUGH_HP);
				caster.sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			else
			{
				if (!skill.isStatic())
				{
					if (skill.isMagic())
					{
						if (caster.isMuted())
						{
							caster.sendPacket(ActionFailed.STATIC_PACKET);
							return false;
						}
					}
					else if (caster.isPhysicalMuted())
					{
						caster.sendPacket(ActionFailed.STATIC_PACKET);
						return false;
					}
				}

				Weapon weapon = caster.getActiveWeaponItem();
				if (weapon != null && weapon.useWeaponSkillsOnly() && !caster.isGM())
				{
					List<ItemSkillHolder> weaponSkills = weapon.getSkills(ItemSkillType.NORMAL);
					if (weaponSkills != null)
					{
						boolean hasSkill = false;

						for (ItemSkillHolder holder : weaponSkills)
						{
							if (holder.getSkillId() == skill.getId())
							{
								hasSkill = true;
								break;
							}
						}

						if (!hasSkill)
						{
							caster.sendPacket(SystemMessageId.THAT_WEAPON_CANNOT_USE_ANY_OTHER_SKILL_EXCEPT_THE_WEAPON_S_SKILL);
							return false;
						}
					}
				}

				if (skill.getItemConsumeId() > 0 && skill.getItemConsumeCount() > 0 && caster.getInventory() != null)
				{
					Item requiredItem = caster.getInventory().getItemByItemId(skill.getItemConsumeId());
					if (requiredItem == null || requiredItem.getCount() < skill.getItemConsumeCount())
					{
						if (skill.hasEffectType(EffectType.SUMMON))
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.SUMMONING_A_SERVITOR_COSTS_S2_S1);
							sm.addItemName(skill.getItemConsumeId());
							sm.addInt(skill.getItemConsumeCount());
							caster.sendPacket(sm);
						}
						else
						{
							caster.sendPacket(new SystemMessage(SystemMessageId.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL));
						}

						return false;
					}
				}

				if (caster.isPlayer())
				{
					Player player = caster.asPlayer();
					if (player.inObserverMode())
					{
						return false;
					}

					if (player.isInOlympiadMode() && skill.isBlockedInOlympiad())
					{
						player.sendPacket(SystemMessageId.THE_SKILL_CANNOT_BE_USED_IN_THE_OLYMPIAD);
						return false;
					}

					if (player.isInsideZone(ZoneId.SAYUNE))
					{
						player.sendPacket(SystemMessageId.YOU_CANNOT_USE_SKILLS_IN_THE_CORRESPONDING_REGION);
						return false;
					}

					if (player.isInAirShip() && !skill.hasEffectType(EffectType.REFUEL_AIRSHIP))
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_THE_REQUIREMENTS_ARE_NOT_MET);
						sm.addSkillName(skill);
						player.sendPacket(sm);
						return false;
					}

					if (player.getFame() < skill.getFamePointConsume())
					{
						player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_REPUTATION_POINTS);
						return false;
					}

					if (skill.getClanRepConsume() > 0)
					{
						Clan clan = player.getClan();
						if (clan == null || clan.getReputationScore() < skill.getClanRepConsume())
						{
							player.sendPacket(SystemMessageId.THE_CLAN_REPUTATION_IS_TOO_LOW);
							return false;
						}
					}

					if (caster.hasSkillReuse(skill.getReuseHashCode()))
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_NOT_AVAILABLE_AT_THIS_TIME_BEING_PREPARED_FOR_REUSE);
						sm.addSkillName(skill);
						caster.sendPacket(sm);
						return false;
					}

					if (player.isOnEvent() && skill.hasEffectType(EffectType.TELEPORT))
					{
						player.sendMessage("You cannot use " + skill.getName() + " while attending an event.");
						return false;
					}
				}

				return true;
			}
		}
		else
		{
			caster.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
	}

	private void handleSkillFly(Creature creature, WorldObject target)
	{
		if (!creature.isAffected(EffectFlag.BLOCK_RUSH))
		{
			int x = 0;
			int y = 0;
			int z = 0;
			FlyType flyType = FlyType.CHARGE;
			switch (this._skill.getOperateType())
			{
				case DA1:
				case DA2:
					if (creature == target)
					{
						double course = Math.toRadians(180.0);
						double radianx = Math.toRadians(LocationUtil.convertHeadingToDegree(creature.getHeading()));
						x = target.getX() + (int) (Math.cos(Math.PI + radianx + course) * this._skill.getCastRange());
						y = target.getY() + (int) (Math.sin(Math.PI + radianx + course) * this._skill.getCastRange());
						z = target.getZ();
					}
					else
					{
						x = target.getX();
						y = target.getY();
						z = target.getZ();
					}
					break;
				case DA3:
				{
					flyType = FlyType.WARP_BACK;
					double radian = Math.toRadians(LocationUtil.convertHeadingToDegree(creature.getHeading()));
					x = creature.getX() + (int) (Math.cos(Math.PI + radian) * this._skill.getCastRange());
					y = creature.getY() + (int) (Math.sin(Math.PI + radian) * this._skill.getCastRange());
					z = creature.getZ();
					break;
				}
				case DA4:
				case DA5:
				{
					double course = this._skill.getOperateType() == SkillOperateType.DA4 ? Math.toRadians(270.0) : Math.toRadians(90.0);
					double radian = Math.toRadians(LocationUtil.convertHeadingToDegree(target.getHeading()));
					double nRadius = creature.getCollisionRadius();
					if (target.isCreature())
					{
						nRadius += target.asCreature().getCollisionRadius();
					}

					x = target.getX() + (int) (Math.cos(Math.PI + radian + course) * nRadius);
					y = target.getY() + (int) (Math.sin(Math.PI + radian + course) * nRadius);
					z = target.getZ();
					break;
				}
				case DA6:
					int dx = target.getX() - creature.getX();
					int dy = target.getY() - creature.getY();
					double distance = Math.sqrt(dx * dx + dy * dy);
					if (distance > 1.0)
					{
						double range = this._skill.getCastRange();
						double ratio = range / distance;
						x = creature.getX() + (int) (dx * ratio);
						y = creature.getY() + (int) (dy * ratio);
						z = creature.getZ();
					}
					else
					{
						x = creature.getX();
						y = creature.getY();
						z = creature.getZ();
					}
			}

			Location destination = creature.isFlying() ? new Location(x, y, z) : GeoEngine.getInstance().getValidLocation(creature.getX(), creature.getY(), creature.getZ(), x, y, z, creature.getInstanceWorld());
			creature.getAI().setIntention(Intention.IDLE);
			creature.broadcastPacket(new FlyToLocation(creature, destination, flyType, 0, 0, 333));
			creature.setXYZ(destination);
			creature.revalidateZone(true);
		}
	}
}
