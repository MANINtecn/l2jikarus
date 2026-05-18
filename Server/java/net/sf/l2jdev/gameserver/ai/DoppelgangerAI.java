package net.sf.l2jdev.gameserver.ai;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.instance.Doppelganger;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.SkillCaster;
import net.sf.l2jdev.gameserver.taskmanagers.GameTimeTaskManager;

public class DoppelgangerAI extends CreatureAI
{
	private volatile boolean _thinking;
	private volatile boolean _startFollow;
	private Creature _lastAttack = null;

	public DoppelgangerAI(Doppelganger clone)
	{
		super(clone);
	}

	@Override
	protected void onIntentionIdle()
	{
		this.stopFollow();
		this._startFollow = false;
		this.onIntentionActive();
	}

	@Override
	protected void onIntentionActive()
	{
		if (this._startFollow)
		{
			this.setIntention(Intention.FOLLOW, this.getActor().getSummoner());
		}
		else
		{
			super.onIntentionActive();
		}
	}

	private void thinkAttack()
	{
		WorldObject target = this.getTarget();
		Creature attackTarget = target != null && target.isCreature() ? target.asCreature() : null;
		if (this.checkTargetLostOrDead(attackTarget))
		{
			this.setTarget(null);
		}
		else if (!this.maybeMoveToPawn(target, this._actor.getPhysicalAttackRange()))
		{
			this.clientStopMoving(null);
			this._actor.doAutoAttack(attackTarget);
		}
	}

	private void thinkCast()
	{
		if (!this._actor.isCastingNow(SkillCaster::isAnyNormalType))
		{
			WorldObject target = this.getCastTarget();
			if (this.checkTargetLost(target))
			{
				this.setCastTarget(null);
				this.setTarget(null);
			}
			else
			{
				boolean val = this._startFollow;
				if (!this.maybeMoveToPawn(target, this._actor.getMagicalAttackRange(this._skill)))
				{
					this.getActor().followSummoner(false);
					this.setIntention(Intention.IDLE);
					this._startFollow = val;
					this._actor.doCast(this._skill, this._item, this._forceUse, this._dontMove);
				}
			}
		}
	}

	private void thinkInteract()
	{
		WorldObject target = this.getTarget();
		if (!this.checkTargetLost(target))
		{
			if (!this.maybeMoveToPawn(target, 36))
			{
				this.setIntention(Intention.IDLE);
			}
		}
	}

	@Override
	public void onActionThink()
	{
		if (!this._thinking && !this._actor.isCastingNow() && !this._actor.isAllSkillsDisabled())
		{
			this._thinking = true;

			try
			{
				switch (this.getIntention())
				{
					case ATTACK:
						this.thinkAttack();
						break;
					case CAST:
						this.thinkCast();
						break;
					case INTERACT:
						this.thinkInteract();
				}
			}
			finally
			{
				this._thinking = false;
			}
		}
	}

	@Override
	protected void onActionFinishCasting()
	{
		if (this._lastAttack == null)
		{
			this.getActor().followSummoner(this._startFollow);
		}
		else
		{
			this.setIntention(Intention.ATTACK, this._lastAttack);
			this._lastAttack = null;
		}
	}

	public void notifyFollowStatusChange()
	{
		this._startFollow = !this._startFollow;
		switch (this.getIntention())
		{
			case ACTIVE:
			case FOLLOW:
			case IDLE:
			case MOVE_TO:
			case PICK_UP:
				this.getActor().followSummoner(this._startFollow);
		}
	}

	public void setStartFollowController(boolean value)
	{
		this._startFollow = value;
	}

	@Override
	protected void onIntentionCast(Skill skill, WorldObject target, Item item, boolean forceUse, boolean dontMove)
	{
		if (this.getIntention() == Intention.ATTACK)
		{
			this._lastAttack = this.getTarget() != null && this.getTarget().isCreature() ? this.getTarget().asCreature() : null;
		}
		else
		{
			this._lastAttack = null;
		}

		super.onIntentionCast(skill, target, item, forceUse, dontMove);
	}

	@Override
	public void moveToPawn(WorldObject pawn, int offsetValue)
	{
		if (!this._actor.isMovementDisabled() && this._actor.getMoveSpeed() > 0.0)
		{
			int offset = offsetValue;
			if (offsetValue < 10)
			{
				offset = 10;
			}

			boolean sendPacket = true;
			if (this._actor.isMoving() && this.getTarget() == pawn)
			{
				if (this._clientMovingToPawnOffset == offset)
				{
					if (GameTimeTaskManager.getInstance().getGameTicks() < this._moveToPawnTimeout)
					{
						return;
					}

					sendPacket = false;
				}
				else if (this._actor.isOnGeodataPath() && GameTimeTaskManager.getInstance().getGameTicks() < this._moveToPawnTimeout + 10)
				{
					return;
				}
			}

			this._clientMovingToPawnOffset = offset;
			this.setTarget(pawn);
			this._moveToPawnTimeout = GameTimeTaskManager.getInstance().getGameTicks();
			this._moveToPawnTimeout += 10;
			if (pawn == null)
			{
				return;
			}

			Location loc = new Location(pawn.getX() + Rnd.get(-offset, offset), pawn.getY() + Rnd.get(-offset, offset), pawn.getZ());
			this._actor.moveToLocation(loc.getX(), loc.getY(), loc.getZ(), 0);
			if (!this._actor.isMoving())
			{
				this.clientActionFailed();
				return;
			}

			if (sendPacket)
			{
				this._actor.broadcastMoveToLocation();
			}
		}
		else
		{
			this.clientActionFailed();
		}
	}

	@Override
	public Doppelganger getActor()
	{
		return (Doppelganger) super.getActor();
	}
}
