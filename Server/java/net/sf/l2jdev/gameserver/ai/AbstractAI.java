package net.sf.l2jdev.gameserver.ai;

import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.WorldRegion;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.interfaces.ILocational;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.AutoAttackStart;
import net.sf.l2jdev.gameserver.network.serverpackets.AutoAttackStop;
import net.sf.l2jdev.gameserver.network.serverpackets.Die;
import net.sf.l2jdev.gameserver.network.serverpackets.MoveToLocation;
import net.sf.l2jdev.gameserver.network.serverpackets.MoveToPawn;
import net.sf.l2jdev.gameserver.network.serverpackets.StopMove;
import net.sf.l2jdev.gameserver.taskmanagers.AttackStanceTaskManager;
import net.sf.l2jdev.gameserver.taskmanagers.CreatureFollowTaskManager;
import net.sf.l2jdev.gameserver.taskmanagers.GameTimeTaskManager;

public abstract class AbstractAI
{
	protected final Creature _actor;
	protected Intention _intention = Intention.IDLE;
	protected Object[] _intentionArgs = null;
	private volatile boolean _clientAutoAttacking;
	protected int _clientMovingToPawnOffset;
	private WorldObject _target;
	private WorldObject _castTarget;
	protected Skill _skill;
	protected Item _item;
	protected boolean _forceUse;
	protected boolean _dontMove;
	protected int _moveToPawnTimeout;
	private NextAction _nextAction;

	public NextAction getNextAction()
	{
		return this._nextAction;
	}

	public void setNextAction(NextAction nextAction)
	{
		this._nextAction = nextAction;
	}

	protected AbstractAI(Creature creature)
	{
		this._actor = creature;
	}

	public Creature getActor()
	{
		return this._actor;
	}

	public Intention getIntention()
	{
		return this._intention;
	}

	synchronized void changeIntention(Intention intention, Object... args)
	{
		this._intention = intention;
		this._intentionArgs = args;
	}

	public void setIntention(Intention intention)
	{
		this.setIntention(intention, null, null);
	}

	public void setIntention(Intention intention, Object... args)
	{
		if (intention != Intention.FOLLOW && intention != Intention.ATTACK)
		{
			this.stopFollow();
		}

		switch (intention)
		{
			case IDLE:
				this.onIntentionIdle();
				break;
			case ACTIVE:
				this.onIntentionActive();
				break;
			case REST:
				this.onIntentionRest();
				break;
			case ATTACK:
				this.onIntentionAttack((Creature) args[0]);
				break;
			case CAST:
				this.onIntentionCast((Skill) args[0], (WorldObject) args[1], args.length > 2 ? (Item) args[2] : null, args.length > 3 && (Boolean) args[3], args.length > 4 && (Boolean) args[4]);
				break;
			case MOVE_TO:
				this.onIntentionMoveTo((ILocational) args[0]);
				break;
			case FOLLOW:
				this.onIntentionFollow((Creature) args[0]);
				break;
			case PICK_UP:
				this.onIntentionPickUp((WorldObject) args[0]);
				break;
			case INTERACT:
				this.onIntentionInteract((WorldObject) args[0]);
		}

		NextAction nextAction = this._nextAction;
		if (nextAction != null && nextAction.isRemovedBy(intention))
		{
			this._nextAction = null;
		}
	}

	public void notifyAction(Action action)
	{
		this.notifyAction(action, null, null);
	}

	public void notifyAction(Action action, Object arg0)
	{
		this.notifyAction(action, arg0, null);
	}

	public void notifyAction(Action action, Object arg0, Object arg1)
	{
		if ((this._actor.isSpawned() || this._actor.isTeleporting()) && this._actor.hasAI())
		{
			switch (action)
			{
				case THINK:
					this.onActionThink();
					break;
				case ATTACKED:
					this.onActionAttacked((Creature) arg0);
					break;
				case AGGRESSION:
					this.onActionAggression((Creature) arg0, ((Number) arg1).intValue());
					break;
				case BLOCKED:
					this.onActionBlocked((Creature) arg0);
					break;
				case ROOTED:
					this.onActionRooted((Creature) arg0);
					break;
				case CONFUSED:
					this.onActionConfused((Creature) arg0);
					break;
				case MUTED:
					this.onActionMuted((Creature) arg0);
					break;
				case EVADED:
					this.onActionEvaded((Creature) arg0);
					break;
				case READY_TO_ACT:
					if (!this._actor.isCastingNow())
					{
						this.onActionReadyToAct();
					}
					break;
				case ARRIVED:
					if (!this._actor.isCastingNow())
					{
						this.onActionArrived();
					}
					break;
				case ARRIVED_REVALIDATE:
					if (this._actor.isMoving())
					{
						this.onActionArrivedRevalidate();
					}
					break;
				case ARRIVED_BLOCKED:
					this.onActionArrivedBlocked((Location) arg0);
					break;
				case FORGET_OBJECT:
					WorldObject worldObject = (WorldObject) arg0;
					this._actor.removeSeenCreature(worldObject);
					this.onActionForgetObject(worldObject);
					break;
				case CANCEL:
					this.onActionCancel();
					break;
				case DEATH:
					this.onActionDeath();
					break;
				case FAKE_DEATH:
					this.onActionFakeDeath();
					break;
				case FINISH_CASTING:
					this.onActionFinishCasting();
			}

			NextAction nextAction = this._nextAction;
			if (nextAction != null && nextAction.isTriggeredBy(action))
			{
				nextAction.doAction();
			}
		}
	}

	protected abstract void onIntentionIdle();

	protected abstract void onIntentionActive();

	protected abstract void onIntentionRest();

	protected abstract void onIntentionAttack(Creature var1);

	protected abstract void onIntentionCast(Skill var1, WorldObject var2, Item var3, boolean var4, boolean var5);

	protected abstract void onIntentionMoveTo(ILocational var1);

	protected abstract void onIntentionFollow(Creature var1);

	protected abstract void onIntentionPickUp(WorldObject var1);

	protected abstract void onIntentionInteract(WorldObject var1);

	protected abstract void onActionThink();

	protected abstract void onActionAttacked(Creature var1);

	protected abstract void onActionAggression(Creature var1, int var2);

	protected abstract void onActionBlocked(Creature var1);

	protected abstract void onActionRooted(Creature var1);

	protected abstract void onActionConfused(Creature var1);

	protected abstract void onActionMuted(Creature var1);

	protected abstract void onActionEvaded(Creature var1);

	protected abstract void onActionReadyToAct();

	protected abstract void onActionArrived();

	protected abstract void onActionArrivedRevalidate();

	protected abstract void onActionArrivedBlocked(Location var1);

	protected abstract void onActionForgetObject(WorldObject var1);

	protected abstract void onActionCancel();

	protected abstract void onActionDeath();

	protected abstract void onActionFakeDeath();

	protected abstract void onActionFinishCasting();

	protected void clientActionFailed()
	{
		if (this._actor.isPlayer())
		{
			this._actor.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	public void moveToPawn(WorldObject pawn, int offsetValue)
	{
		if (!this._actor.isMovementDisabled() && !this._actor.isAttackingNow() && !this._actor.isCastingNow())
		{
			int offset = offsetValue;
			if (offsetValue < 10)
			{
				offset = 10;
			}

			int gameTime = GameTimeTaskManager.getInstance().getGameTicks();
			if (this._actor.isMoving() && this._target == pawn)
			{
				if (this._clientMovingToPawnOffset == offset)
				{
					if (gameTime < this._moveToPawnTimeout)
					{
						return;
					}
				}
				else if (this._actor.isOnGeodataPath() && gameTime < this._moveToPawnTimeout + 10)
				{
					return;
				}
			}

			this._clientMovingToPawnOffset = offset;
			this._target = pawn;
			this._moveToPawnTimeout = gameTime;
			this._moveToPawnTimeout += 10;
			if (pawn == null)
			{
				return;
			}

			this._actor.moveToLocation(pawn.getX(), pawn.getY(), pawn.getZ(), offset);
			if (pawn.isCreature())
			{
				if (this._actor.isOnGeodataPath())
				{
					this._actor.broadcastMoveToLocation();
					this._clientMovingToPawnOffset = 0;
				}
				else
				{
					WorldRegion region = this._actor.getWorldRegion();
					if (region != null && region.isActive())
					{
						this._actor.broadcastPacket(new MoveToPawn(this._actor, pawn, offset));
					}
				}
			}
			else
			{
				this._actor.broadcastMoveToLocation();
			}
		}
		else
		{
			this.clientActionFailed();
		}
	}

	public void moveTo(ILocational loc)
	{
		this.moveTo(loc.getX(), loc.getY(), loc.getZ());
	}

	protected void moveTo(int x, int y, int z)
	{
		if (!this._actor.isMovementDisabled())
		{
			this._clientMovingToPawnOffset = 0;
			this._actor.moveToLocation(x, y, z, 0);
			this._actor.broadcastMoveToLocation();
		}
		else
		{
			this.clientActionFailed();
		}
	}

	public void clientStopMoving(Location loc)
	{
		if (this._actor.isMoving())
		{
			this._actor.stopMove(loc);
		}

		this._clientMovingToPawnOffset = 0;
	}

	protected void clientStoppedMoving()
	{
		if (this._clientMovingToPawnOffset > 0)
		{
			this._clientMovingToPawnOffset = 0;
			this._actor.broadcastPacket(new StopMove(this._actor));
		}
	}

	public boolean isAutoAttacking()
	{
		return this._clientAutoAttacking;
	}

	public void setAutoAttacking(boolean isAutoAttacking)
	{
		if (this._actor.isSummon())
		{
			Summon summon = this._actor.asSummon();
			if (summon.getOwner() != null)
			{
				summon.getOwner().getAI().setAutoAttacking(isAutoAttacking);
			}
		}
		else
		{
			this._clientAutoAttacking = isAutoAttacking;
		}
	}

	public void clientStartAutoAttack()
	{
		if (!this._actor.isNpc() || this._actor.isAttackable() && !this._actor.isCoreAIDisabled())
		{
			if (this._actor.isSummon())
			{
				Summon summon = this._actor.asSummon();
				if (summon.getOwner() != null)
				{
					summon.getOwner().getAI().clientStartAutoAttack();
				}
			}
			else
			{
				if (!this._clientAutoAttacking)
				{
					if (this._actor.isPlayer() && this._actor.hasSummon())
					{
						Summon pet = this._actor.getPet();
						if (pet != null)
						{
							pet.broadcastPacket(new AutoAttackStart(pet.getObjectId()));
						}

						this._actor.getServitors().values().forEach(s -> s.broadcastPacket(new AutoAttackStart(s.getObjectId())));
					}

					this._actor.broadcastPacket(new AutoAttackStart(this._actor.getObjectId()));
					this.setAutoAttacking(true);
				}

				AttackStanceTaskManager.getInstance().addAttackStanceTask(this._actor);
			}
		}
	}

	public void clientStopAutoAttack()
	{
		if (this._actor.isSummon())
		{
			Summon summon = this._actor.asSummon();
			if (summon.getOwner() != null)
			{
				summon.getOwner().getAI().clientStopAutoAttack();
			}
		}
		else
		{
			if (this._actor.isPlayer())
			{
				if (!AttackStanceTaskManager.getInstance().hasAttackStanceTask(this._actor) && this.isAutoAttacking())
				{
					AttackStanceTaskManager.getInstance().addAttackStanceTask(this._actor);
				}
			}
			else if (this._clientAutoAttacking)
			{
				this._actor.broadcastPacket(new AutoAttackStop(this._actor.getObjectId()));
				this.setAutoAttacking(false);
			}
		}
	}

	public int getClientMovingToPawnOffset()
	{
		return this._clientMovingToPawnOffset;
	}

	protected void clientNotifyDead()
	{
		this._actor.broadcastPacket(new Die(this._actor));
		this._intention = Intention.IDLE;
		this._target = null;
		this._castTarget = null;
		this.stopFollow();
	}

	public void describeStateToPlayer(Player player)
	{
		if (this._actor.isVisibleFor(player) && this._actor.isMoving())
		{
			if (this._clientMovingToPawnOffset != 0 && this.isFollowing())
			{
				player.sendPacket(new MoveToPawn(this._actor, this._target, this._clientMovingToPawnOffset));
			}
			else
			{
				player.sendPacket(new MoveToLocation(this._actor));
			}
		}
	}

	public boolean isFollowing()
	{
		return this._target != null && this._target.isCreature() && (this._intention == Intention.FOLLOW || CreatureFollowTaskManager.getInstance().isFollowing(this._actor));
	}

	public void startFollow(Creature target)
	{
		this.startFollow(target, -1);
	}

	public void startFollow(Creature target, int range)
	{
		this.stopFollow();
		this.setTarget(target);
		if (range == -1)
		{
			CreatureFollowTaskManager.getInstance().addNormalFollow(this._actor, range);
		}
		else
		{
			CreatureFollowTaskManager.getInstance().addAttackFollow(this._actor, range);
		}
	}

	public void stopFollow()
	{
		CreatureFollowTaskManager.getInstance().remove(this._actor);
	}

	public void setTarget(WorldObject target)
	{
		this._target = target;
	}

	public WorldObject getTarget()
	{
		return this._target;
	}

	protected void setCastTarget(WorldObject target)
	{
		this._castTarget = target;
	}

	public WorldObject getCastTarget()
	{
		return this._castTarget;
	}

	public void stopAITask()
	{
		this.stopFollow();
	}

	@Override
	public String toString()
	{
		return "Actor: " + this._actor;
	}
}
