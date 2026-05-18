package net.sf.l2jdev.gameserver.ai;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.geoengine.GeoEngine;
import net.sf.l2jdev.gameserver.managers.WalkingManager;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.WorldRegion;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.actor.transform.Transform;
import net.sf.l2jdev.gameserver.model.effects.EffectType;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcMoveFinished;
import net.sf.l2jdev.gameserver.model.interfaces.ILocational;
import net.sf.l2jdev.gameserver.model.item.enums.ItemLocation;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.AutoAttackStop;
import net.sf.l2jdev.gameserver.taskmanagers.AttackStanceTaskManager;

public class CreatureAI extends AbstractAI
{
	private OnNpcMoveFinished _onNpcMoveFinished = null;

	public CreatureAI(Creature creature)
	{
		super(creature);
	}

	public CreatureAI.IntentionCommand getNextIntention()
	{
		return null;
	}

	@Override
	protected void onActionAttacked(Creature attacker)
	{
		this.clientStartAutoAttack();
	}

	@Override
	protected void onIntentionIdle()
	{
		this.changeIntention(Intention.IDLE);
		this.setCastTarget(null);
		this.clientStopMoving(null);
		this.clientStopAutoAttack();
	}

	@Override
	protected void onIntentionActive()
	{
		if (this.getIntention() != Intention.ACTIVE)
		{
			this.changeIntention(Intention.ACTIVE);
			WorldRegion region = this._actor.getWorldRegion();
			if (region != null && region.areNeighborsActive())
			{
				this.setCastTarget(null);
				this.clientStopMoving(null);
				this.clientStopAutoAttack();
				this.onActionThink();
			}
		}
	}

	@Override
	protected void onIntentionRest()
	{
		this.setIntention(Intention.IDLE);
	}

	@Override
	protected void onIntentionAttack(Creature target)
	{
		if (target != null && target.isTargetable())
		{
			if (this.getIntention() == Intention.REST)
			{
				this.clientActionFailed();
			}
			else if (!this._actor.isAllSkillsDisabled() && !this._actor.isCastingNow() && !this._actor.isControlBlocked())
			{
				if (this.getIntention() == Intention.ATTACK)
				{
					if (this.getTarget() != target)
					{
						this.setTarget(target);
						this.notifyAction(Action.THINK, null);
					}
					else
					{
						this.clientActionFailed();
					}
				}
				else
				{
					this.changeIntention(Intention.ATTACK, target);
					this.setTarget(target);
					this.notifyAction(Action.THINK, null);
				}
			}
			else
			{
				this.clientActionFailed();
			}
		}
		else
		{
			this.clientActionFailed();
		}
	}

	@Override
	protected void onIntentionCast(Skill skill, WorldObject target, Item item, boolean forceUse, boolean dontMove)
	{
		if (this.getIntention() == Intention.REST && skill.isMagic())
		{
			this.clientActionFailed();
		}
		else
		{
			long currentTime = System.nanoTime();
			long attackEndTime = this._actor.getAttackEndTime();
			if (attackEndTime > currentTime)
			{
				ThreadPool.schedule(new CreatureAI.CastTask(this._actor, skill, target, item, forceUse, dontMove), TimeUnit.NANOSECONDS.toMillis(attackEndTime - currentTime));
			}
			else
			{
				this.changeIntentionToCast(skill, target, item, forceUse, dontMove);
			}
		}
	}

	protected void changeIntentionToCast(Skill skill, WorldObject target, Item item, boolean forceUse, boolean dontMove)
	{
		this.setCastTarget(target);
		this._skill = skill;
		this._item = item;
		this._forceUse = forceUse;
		this._dontMove = dontMove;
		this.changeIntention(Intention.CAST, skill);
		this.notifyAction(Action.THINK, null);
	}

	@Override
	protected void onIntentionMoveTo(ILocational loc)
	{
		if (this.getIntention() == Intention.REST)
		{
			this.clientActionFailed();
		}
		else if (!this._actor.isAllSkillsDisabled() && !this._actor.isCastingNow())
		{
			this.changeIntention(Intention.MOVE_TO, loc);
			this.clientStopAutoAttack();
			this._actor.abortAttack();
			this.moveTo(loc.getX(), loc.getY(), loc.getZ());
		}
		else
		{
			this.clientActionFailed();
		}
	}

	@Override
	protected void onIntentionFollow(Creature target)
	{
		if (this.getIntention() == Intention.REST)
		{
			this.clientActionFailed();
		}
		else if (!this._actor.isAllSkillsDisabled() && !this._actor.isCastingNow())
		{
			if (this._actor.isMovementDisabled() || this._actor.getMoveSpeed() <= 0.0)
			{
				this.clientActionFailed();
			}
			else if (this._actor.isDead())
			{
				this.clientActionFailed();
			}
			else if (this._actor == target)
			{
				this.clientActionFailed();
			}
			else
			{
				this.clientStopAutoAttack();
				this.changeIntention(Intention.FOLLOW, target);
				this.startFollow(target);
			}
		}
		else
		{
			this.clientActionFailed();
		}
	}

	@Override
	protected void onIntentionPickUp(WorldObject object)
	{
		if (this.getIntention() == Intention.REST)
		{
			this.clientActionFailed();
		}
		else if (!this._actor.isAllSkillsDisabled() && !this._actor.isCastingNow())
		{
			this.clientStopAutoAttack();
			if (!object.isItem() || ((Item) object).getItemLocation() == ItemLocation.VOID)
			{
				this.changeIntention(Intention.PICK_UP, object);
				this.setTarget(object);
				if (object.getX() == 0 && object.getY() == 0)
				{
					object.setXYZ(this.getActor().getX(), this.getActor().getY(), this.getActor().getZ() + 5);
				}

				this.moveToPawn(object, 20);
			}
		}
		else
		{
			this.clientActionFailed();
		}
	}

	@Override
	protected void onIntentionInteract(WorldObject object)
	{
		if (this.getIntention() == Intention.REST)
		{
			this.clientActionFailed();
		}
		else if (!this._actor.isAllSkillsDisabled() && !this._actor.isCastingNow())
		{
			this.clientStopAutoAttack();
			if (this.getIntention() != Intention.INTERACT)
			{
				this.changeIntention(Intention.INTERACT, object);
				this.setTarget(object);
				this.moveToPawn(object, 60);
			}
		}
		else
		{
			this.clientActionFailed();
		}
	}

	@Override
	public void onActionThink()
	{
	}

	@Override
	protected void onActionAggression(Creature target, int aggro)
	{
	}

	@Override
	protected void onActionBlocked(Creature attacker)
	{
		this._actor.broadcastPacket(new AutoAttackStop(this._actor.getObjectId()));
		if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(this._actor))
		{
			AttackStanceTaskManager.getInstance().removeAttackStanceTask(this._actor);
		}

		this.setAutoAttacking(false);
		this.clientStopMoving(null);
	}

	@Override
	protected void onActionRooted(Creature attacker)
	{
		this.clientStopMoving(null);
		this.onActionAttacked(attacker);
	}

	@Override
	protected void onActionConfused(Creature attacker)
	{
		this.clientStopMoving(null);
		this.onActionAttacked(attacker);
	}

	@Override
	protected void onActionMuted(Creature attacker)
	{
		this.onActionAttacked(attacker);
	}

	@Override
	protected void onActionEvaded(Creature attacker)
	{
	}

	@Override
	protected void onActionReadyToAct()
	{
		this.onActionThink();
	}

	@Override
	protected void onActionArrived()
	{
		this.getActor().revalidateZone(true);
		if (!this.getActor().moveToNextRoutePoint())
		{
			this.clientStoppedMoving();
			if (this.getIntention() == Intention.MOVE_TO)
			{
				this.setIntention(Intention.ACTIVE);
			}

			if (this._actor.isNpc())
			{
				Npc npc = this._actor.asNpc();
				WalkingManager.getInstance().onArrived(npc);
				if (EventDispatcher.getInstance().hasListener(EventType.ON_NPC_MOVE_FINISHED, npc))
				{
					if (this._onNpcMoveFinished == null)
					{
						this._onNpcMoveFinished = new OnNpcMoveFinished(npc);
					}

					EventDispatcher.getInstance().notifyEventAsync(this._onNpcMoveFinished, npc);
				}
			}

			this.onActionThink();
		}
	}

	@Override
	protected void onActionArrivedRevalidate()
	{
		this.onActionThink();
	}

	@Override
	protected void onActionArrivedBlocked(Location location)
	{
		if (this.getIntention() == Intention.MOVE_TO || this.getIntention() == Intention.CAST)
		{
			this.setIntention(Intention.ACTIVE);
		}

		this.clientStopMoving(location);
		this.onActionThink();
	}

	@Override
	protected void onActionForgetObject(WorldObject object)
	{
		WorldObject target = this.getTarget();
		this.getActor().abortCast(sc -> sc.getTarget() == object);
		if (target == object)
		{
			this.setTarget(null);
			if (this.isFollowing())
			{
				this.clientStopMoving(null);
				this.stopFollow();
			}

			if (this.getIntention() != Intention.MOVE_TO)
			{
				this.setIntention(Intention.ACTIVE);
			}
		}

		if (this._actor == object)
		{
			this.setTarget(null);
			this.setCastTarget(null);
			this.stopFollow();
			this.clientStopMoving(null);
			this.changeIntention(Intention.IDLE);
		}
	}

	@Override
	protected void onActionCancel()
	{
		this._actor.abortCast();
		this.stopFollow();
		if (!AttackStanceTaskManager.getInstance().hasAttackStanceTask(this._actor))
		{
			this._actor.broadcastPacket(new AutoAttackStop(this._actor.getObjectId()));
		}

		this.onActionThink();
	}

	@Override
	protected void onActionDeath()
	{
		this.stopAITask();
		if (this._actor.isNpc())
		{
			this._actor.asNpc().setDisplayEffect(0);
		}

		this.clientNotifyDead();
		if (!this._actor.isPlayable() && !this._actor.isFakePlayer())
		{
			this._actor.setWalking();
		}
	}

	@Override
	protected void onActionFakeDeath()
	{
		this.stopFollow();
		this.clientStopMoving(null);
		this._intention = Intention.IDLE;
		this.setTarget(null);
		this.setCastTarget(null);
	}

	@Override
	protected void onActionFinishCasting()
	{
	}

	protected boolean maybeMoveToPosition(ILocational worldPosition, int offset)
	{
		if (worldPosition == null)
		{
			return false;
		}
		else if (offset < 0)
		{
			return false;
		}
		else if (!this._actor.isInsideRadius2D(worldPosition, offset + this._actor.getTemplate().getCollisionRadius()))
		{
			if (!this._actor.isMovementDisabled() && !(this._actor.getMoveSpeed() <= 0.0))
			{
				if (!this._actor.isRunning() && !(this instanceof PlayerAI) && !(this instanceof SummonAI))
				{
					this._actor.setRunning();
				}

				this.stopFollow();
				int x = this._actor.getX();
				int y = this._actor.getY();
				double dx = worldPosition.getX() - x;
				double dy = worldPosition.getY() - y;
				double dist = Math.hypot(dx, dy);
				double sin = dy / dist;
				double cos = dx / dist;
				dist -= offset - 5;
				x += (int) (dist * cos);
				y += (int) (dist * sin);
				this.moveTo(x, y, worldPosition.getZ());
				return true;
			}
			return true;
		}
		else
		{
			if (this.isFollowing())
			{
				this.stopFollow();
			}

			return false;
		}
	}

	protected boolean maybeMoveToPawn(WorldObject target, int offsetValue)
	{
		if (target == null)
		{
			return false;
		}
		else if (offsetValue < 0)
		{
			return false;
		}
		else
		{
			int offsetWithCollision = offsetValue + this._actor.getTemplate().getCollisionRadius();
			if (target.isCreature())
			{
				offsetWithCollision += target.asCreature().getTemplate().getCollisionRadius();
			}

			if (!this._actor.isInsideRadius2D(target, offsetWithCollision))
			{
				if (this.isFollowing())
				{
					if (!this._actor.isInsideRadius2D(target, offsetWithCollision + 100))
					{
						return true;
					}
					this.stopFollow();
					return false;
				}
				else if (!this._actor.isMovementDisabled() && !(this._actor.getMoveSpeed() <= 0.0))
				{
					if (this._actor.isPlayer() && this._actor.getAI().getIntention() == Intention.CAST)
					{
						Transform transform = this._actor.getTransformation();
						if (transform != null && !transform.canUseWeaponStats())
						{
							this._actor.sendPacket(SystemMessageId.THE_DISTANCE_IS_TOO_FAR_AND_SO_THE_CASTING_HAS_BEEN_CANCELLED);
							this._actor.sendPacket(ActionFailed.STATIC_PACKET);
							return true;
						}
					}

					if (!this._actor.isRunning() && !(this instanceof PlayerAI) && !(this instanceof SummonAI))
					{
						this._actor.setRunning();
					}

					this.stopFollow();
					int offset = offsetValue;
					if (target.isCreature() && !target.isDoor())
					{
						if (target.asCreature().isMoving())
						{
							offset = offsetValue - 100;
						}

						if (offset < 5)
						{
							offset = 5;
						}

						this.startFollow(target.asCreature(), offset);
					}
					else
					{
						this.moveToPawn(target, offsetValue);
					}

					return true;
				}
				else
				{
					if (this._actor.getAI().getIntention() == Intention.ATTACK)
					{
						this._actor.getAI().setIntention(Intention.IDLE);
					}

					return true;
				}
			}
			if (this.isFollowing())
			{
				this.stopFollow();
			}

			return false;
		}
	}

	protected boolean checkTargetLostOrDead(Creature target)
	{
		if (target != null && !target.isDead())
		{
			return false;
		}
		this.setIntention(Intention.ACTIVE);
		return true;
	}

	protected boolean checkTargetLost(WorldObject target)
	{
		if (target == null)
		{
			this.setIntention(Intention.ACTIVE);
			return true;
		}
		if (this._actor != null)
		{
			if (this._skill != null && this._skill.hasNegativeEffect() && this._skill.getAffectRange() > 0)
			{
				if (this._actor.isPlayer() && this._actor.isMoving())
				{
					if (!GeoEngine.getInstance().canMoveToTarget(this._actor, target))
					{
						this.setIntention(Intention.ACTIVE);
						return true;
					}
				}
				else if (!GeoEngine.getInstance().canSeeTarget(this._actor, target))
				{
					this.setIntention(Intention.ACTIVE);
					return true;
				}
			}

			if (this._actor.isSummon())
			{
				if (GeoEngine.getInstance().canMoveToTarget(this._actor, target))
				{
					return false;
				}

				this.setIntention(Intention.ACTIVE);
				return true;
			}
		}

		return false;
	}

	public static class CastTask implements Runnable
	{
		private final Creature _creature;
		private final WorldObject _target;
		private final Skill _skill;
		private final Item _item;
		private final boolean _forceUse;
		private final boolean _dontMove;

		public CastTask(Creature actor, Skill skill, WorldObject target, Item item, boolean forceUse, boolean dontMove)
		{
			this._creature = actor;
			this._target = target;
			this._skill = skill;
			this._item = item;
			this._forceUse = forceUse;
			this._dontMove = dontMove;
		}

		@Override
		public void run()
		{
			if (this._creature.isAttackingNow())
			{
				this._creature.abortAttack();
			}

			this._creature.getAI().changeIntentionToCast(this._skill, this._target, this._item, this._forceUse, this._dontMove);
		}
	}

	public static class IntentionCommand
	{
		protected final Intention _intention;
		protected final Object _arg0;
		protected final Object _arg1;

		protected IntentionCommand(Intention pIntention, Object pArg0, Object pArg1)
		{
			this._intention = pIntention;
			this._arg0 = pArg0;
			this._arg1 = pArg1;
		}

		public Intention getIntention()
		{
			return this._intention;
		}
	}

	protected class SelfAnalysis
	{
		public boolean isMage;
		public boolean isBalanced;
		public boolean isArcher;
		public boolean isHealer;
		public boolean isFighter;
		public boolean cannotMoveOnLand;
		public Set<Skill> generalSkills;
		public Set<Skill> buffSkills;
		public int lastBuffTick;
		public Set<Skill> debuffSkills;
		public int lastDebuffTick;
		public Set<Skill> cancelSkills;
		public Set<Skill> healSkills;
		public Set<Skill> generalDisablers;
		public Set<Skill> sleepSkills;
		public Set<Skill> rootSkills;
		public Set<Skill> muteSkills;
		public Set<Skill> resurrectSkills;
		public boolean hasHealOrResurrect;
		public boolean hasLongRangeSkills;
		public boolean hasLongRangeDamageSkills;
		public int maxCastRange;

		public SelfAnalysis()
		{
			Objects.requireNonNull(CreatureAI.this);
			super();
			this.isMage = false;
			this.isArcher = false;
			this.isHealer = false;
			this.isFighter = false;
			this.cannotMoveOnLand = false;
			this.generalSkills = ConcurrentHashMap.newKeySet();
			this.buffSkills = ConcurrentHashMap.newKeySet();
			this.lastBuffTick = 0;
			this.debuffSkills = ConcurrentHashMap.newKeySet();
			this.lastDebuffTick = 0;
			this.cancelSkills = ConcurrentHashMap.newKeySet();
			this.healSkills = ConcurrentHashMap.newKeySet();
			this.generalDisablers = ConcurrentHashMap.newKeySet();
			this.sleepSkills = ConcurrentHashMap.newKeySet();
			this.rootSkills = ConcurrentHashMap.newKeySet();
			this.muteSkills = ConcurrentHashMap.newKeySet();
			this.resurrectSkills = ConcurrentHashMap.newKeySet();
			this.hasHealOrResurrect = false;
			this.hasLongRangeSkills = false;
			this.hasLongRangeDamageSkills = false;
			this.maxCastRange = 0;
		}

		public void init()
		{
			switch (((NpcTemplate) CreatureAI.this._actor.getTemplate()).getAIType())
			{
				case FIGHTER:
					this.isFighter = true;
					break;
				case MAGE:
					this.isMage = true;
					break;
				case CORPSE:
				case BALANCED:
					this.isBalanced = true;
					break;
				case ARCHER:
					this.isArcher = true;
					break;
				case HEALER:
					this.isHealer = true;
					break;
				default:
					this.isFighter = true;
			}

			if (CreatureAI.this._actor.isNpc())
			{
				switch (CreatureAI.this._actor.getId())
				{
					case 20314:
					case 20849:
						this.cannotMoveOnLand = true;
						break;
					default:
						this.cannotMoveOnLand = false;
				}
			}

			for (Skill sk : CreatureAI.this._actor.getAllSkills())
			{
				if (!sk.isPassive())
				{
					int castRange = sk.getCastRange();
					boolean hasLongRangeDamageSkill = false;
					if (sk.isContinuous())
					{
						if (!sk.isDebuff())
						{
							this.buffSkills.add(sk);
						}
						else
						{
							this.debuffSkills.add(sk);
						}
					}
					else
					{
						if (sk.hasEffectType(EffectType.DISPEL, EffectType.DISPEL_BY_SLOT))
						{
							this.cancelSkills.add(sk);
						}
						else if (sk.hasEffectType(EffectType.HEAL))
						{
							this.healSkills.add(sk);
							this.hasHealOrResurrect = true;
						}
						else if (sk.hasEffectType(EffectType.SLEEP))
						{
							this.sleepSkills.add(sk);
						}
						else if (sk.hasEffectType(EffectType.BLOCK_ACTIONS))
						{
							switch (sk.getId())
							{
								case 367:
								case 4111:
								case 4383:
								case 4578:
								case 4616:
									this.sleepSkills.add(sk);
									break;
								default:
									this.generalDisablers.add(sk);
							}
						}
						else if (sk.hasEffectType(EffectType.ROOT))
						{
							this.rootSkills.add(sk);
						}
						else if (sk.hasEffectType(EffectType.BLOCK_CONTROL))
						{
							this.debuffSkills.add(sk);
						}
						else if (sk.hasEffectType(EffectType.MUTE))
						{
							this.muteSkills.add(sk);
						}
						else if (sk.hasEffectType(EffectType.RESURRECTION))
						{
							this.resurrectSkills.add(sk);
							this.hasHealOrResurrect = true;
						}
						else
						{
							this.generalSkills.add(sk);
							hasLongRangeDamageSkill = true;
						}

						if (castRange > 150)
						{
							this.hasLongRangeSkills = true;
							if (hasLongRangeDamageSkill)
							{
								this.hasLongRangeDamageSkills = true;
							}
						}

						if (castRange > this.maxCastRange)
						{
							this.maxCastRange = castRange;
						}
					}
				}
			}

			if (!this.hasLongRangeDamageSkills && this.isMage)
			{
				this.isBalanced = true;
				this.isMage = false;
				this.isFighter = false;
			}

			if (!this.hasLongRangeSkills && (this.isMage || this.isBalanced))
			{
				this.isBalanced = false;
				this.isMage = false;
				this.isFighter = true;
			}

			if (this.generalSkills.isEmpty() && this.isMage)
			{
				this.isBalanced = true;
				this.isMage = false;
			}
		}
	}
}
