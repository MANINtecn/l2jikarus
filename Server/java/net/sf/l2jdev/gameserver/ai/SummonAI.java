package net.sf.l2jdev.gameserver.ai;

import java.util.concurrent.Future;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.geoengine.GeoEngine;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.SkillCaster;

public class SummonAI extends PlayableAI implements Runnable
{
	public static final int AVOID_RADIUS = 70;
	private volatile boolean _thinking;
	private volatile boolean _startFollow = this._actor.asSummon().getFollowStatus();
	private Creature _lastAttack = null;
	private volatile boolean _startAvoid;
	private volatile boolean _isDefending;
	private Future<?> _avoidTask = null;
	private CreatureAI.IntentionCommand _nextIntention = null;

	private void saveNextIntention(Intention intention, Object arg0, Object arg1)
	{
		this._nextIntention = new CreatureAI.IntentionCommand(intention, arg0, arg1);
	}

	@Override
	public CreatureAI.IntentionCommand getNextIntention()
	{
		return this._nextIntention;
	}

	public SummonAI(Summon summon)
	{
		super(summon);
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
		Summon summon = this._actor.asSummon();
		if (this._startFollow)
		{
			this.setIntention(Intention.FOLLOW, summon.getOwner());
		}
		else
		{
			super.onIntentionActive();
		}
	}

	@Override
	synchronized void changeIntention(Intention intention, Object... args)
	{
		switch (intention)
		{
			case ACTIVE:
			case FOLLOW:
				this.startAvoidTask();
				break;
			default:
				this.stopAvoidTask();
		}

		super.changeIntention(intention, args);
	}

	private void thinkAttack()
	{
		WorldObject target = this.getTarget();
		Creature attackTarget = target != null && target.isCreature() ? target.asCreature() : null;
		if (this.checkTargetLostOrDead(attackTarget))
		{
			this.setTarget(null);
			if (this._startFollow)
			{
				this._actor.asSummon().setFollowStatus(true);
			}
		}
		else if (!this.maybeMoveToPawn(attackTarget, this._actor.getPhysicalAttackRange()))
		{
			this.clientStopMoving(null);
			if (this._actor.isAttackingNow())
			{
				this.saveNextIntention(Intention.ATTACK, attackTarget, null);
			}
			else
			{
				this._actor.doAutoAttack(attackTarget);
			}
		}
	}

	private void thinkCast()
	{
		Summon summon = this._actor.asSummon();
		if (!summon.isCastingNow(SkillCaster::isAnyNormalType))
		{
			WorldObject target = this.getCastTarget();
			if (this.checkTargetLost(target))
			{
				this.setTarget(null);
				this.setCastTarget(null);
				summon.setFollowStatus(true);
			}
			else
			{
				boolean val = this._startFollow;
				if (!this.maybeMoveToPawn(target, this._actor.getMagicalAttackRange(this._skill)))
				{
					summon.setFollowStatus(false);
					this.setIntention(Intention.IDLE);
					this._startFollow = val;
					this._actor.doCast(this._skill, this._item, this._skill.hasNegativeEffect(), this._dontMove);
				}
			}
		}
	}

	private void thinkPickUp()
	{
		WorldObject target = this.getTarget();
		if (!this.checkTargetLost(target))
		{
			if (!this.maybeMoveToPawn(target, 36))
			{
				this.setIntention(Intention.IDLE);
				this.getActor().doPickupItem(target);
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
					case PICK_UP:
						this.thinkPickUp();
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
			this._actor.asSummon().setFollowStatus(this._startFollow);
		}
		else
		{
			this.setIntention(Intention.ATTACK, this._lastAttack);
			this._lastAttack = null;
		}
	}

	@Override
	protected void onActionAttacked(Creature attacker)
	{
		super.onActionAttacked(attacker);
		if (this._isDefending)
		{
			this.allServitorsDefend(attacker);
		}
		else
		{
			this.avoidAttack(attacker);
		}
	}

	@Override
	protected void onActionEvaded(Creature attacker)
	{
		super.onActionEvaded(attacker);
		if (this._isDefending)
		{
			this.allServitorsDefend(attacker);
		}
		else
		{
			this.avoidAttack(attacker);
		}
	}

	private void allServitorsDefend(Creature attacker)
	{
		Creature owner = this.getActor().getOwner();
		if (owner != null && owner.asPlayer().hasServitors())
		{
			for (Summon summon : owner.asPlayer().getServitors().values())
			{
				SummonAI ai = (SummonAI) summon.getAI();
				if (ai.isDefending())
				{
					ai.defendAttack(attacker);
				}
			}
		}
		else
		{
			this.defendAttack(attacker);
		}
	}

	private void avoidAttack(Creature attacker)
	{
		if (!this._actor.isCastingNow())
		{
			Creature owner = this.getActor().getOwner();
			if (owner != null && owner != attacker && owner.isInsideRadius3D(this._actor, 140))
			{
				this._startAvoid = true;
			}
		}
	}

	public void defendAttack(Creature attacker)
	{
		if (!this._actor.isAttackingNow() && !this._actor.isCastingNow())
		{
			Summon summon = this.getActor();
			Player owner = summon.getOwner();
			if (owner != null)
			{
				if (summon.calculateDistance3D(owner) > 3000.0)
				{
					summon.getAI().setIntention(Intention.FOLLOW, owner);
				}
				else if (owner != attacker && !summon.isMoving() && summon.canAttack(attacker, false))
				{
					summon.doAttack(attacker);
				}
			}
		}
	}

	@Override
	public void run()
	{
		if (this._startAvoid)
		{
			this._startAvoid = false;
			if (!this._actor.isMoving() && !this._actor.isDead() && !this._actor.isMovementDisabled() && this._actor.getMoveSpeed() > 0.0)
			{
				int ownerX = this._actor.asSummon().getOwner().getX();
				int ownerY = this._actor.asSummon().getOwner().getY();
				double angle = Math.toRadians(Rnd.get(-90, 90)) + Math.atan2(ownerY - this._actor.getY(), ownerX - this._actor.getX());
				int targetX = ownerX + (int) (70.0 * Math.cos(angle));
				int targetY = ownerY + (int) (70.0 * Math.sin(angle));
				if (GeoEngine.getInstance().canMoveToTarget(this._actor.getX(), this._actor.getY(), this._actor.getZ(), targetX, targetY, this._actor.getZ(), this._actor.getInstanceWorld()))
				{
					this.moveTo(targetX, targetY, this._actor.getZ());
				}
			}
		}
	}

	public void notifyFollowStatusChange()
	{
		this._startFollow = !this._startFollow;
		switch (this.getIntention())
		{
			case ACTIVE:
			case FOLLOW:
			case PICK_UP:
			case IDLE:
			case MOVE_TO:
				this._actor.asSummon().setFollowStatus(this._startFollow);
			case ATTACK:
			case CAST:
			case INTERACT:
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

	private void startAvoidTask()
	{
		if (this._avoidTask == null)
		{
			this._avoidTask = ThreadPool.scheduleAtFixedRate(this, 100L, 100L);
		}
	}

	private void stopAvoidTask()
	{
		if (this._avoidTask != null)
		{
			this._avoidTask.cancel(false);
			this._avoidTask = null;
		}
	}

	@Override
	public void stopAITask()
	{
		this.stopAvoidTask();
		super.stopAITask();
	}

	@Override
	public Summon getActor()
	{
		return super.getActor().asSummon();
	}

	public boolean isDefending()
	{
		return this._isDefending;
	}

	public void setDefending(boolean isDefending)
	{
		this._isDefending = isDefending;
	}
}
