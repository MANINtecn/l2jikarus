package net.sf.l2jdev.gameserver.ai;

import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.actor.instance.StaticObject;
import net.sf.l2jdev.gameserver.model.interfaces.ILocational;
import net.sf.l2jdev.gameserver.model.item.enums.ShotType;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillUseHolder;
import net.sf.l2jdev.gameserver.model.skill.targets.TargetType;

public class PlayerAI extends PlayableAI
{
	private boolean _thinking;
	private CreatureAI.IntentionCommand _nextIntention = null;

	public PlayerAI(Player player)
	{
		super(player);
	}

	private void saveNextIntention(Intention intention, Object arg0, Object arg1)
	{
		this._nextIntention = new CreatureAI.IntentionCommand(intention, arg0, arg1);
	}

	@Override
	public CreatureAI.IntentionCommand getNextIntention()
	{
		return this._nextIntention;
	}

	@Override
	protected synchronized void changeIntention(Intention intention, Object... args)
	{
		if (intention == Intention.CAST && !((Skill) args[0]).hasNegativeEffect())
		{
			Object localArg0 = args.length > 0 ? args[0] : null;
			Object localArg1 = args.length > 1 ? args[1] : null;
			Object globalArg0 = this._intentionArgs != null && this._intentionArgs.length > 0 ? this._intentionArgs[0] : null;
			Object globalArg1 = this._intentionArgs != null && this._intentionArgs.length > 1 ? this._intentionArgs[1] : null;
			if (intention == this._intention && globalArg0 == localArg0 && globalArg1 == localArg1)
			{
				super.changeIntention(intention, args);
			}
			else
			{
				this.saveNextIntention(this._intention, globalArg0, globalArg1);
				super.changeIntention(intention, args);
			}
		}
		else
		{
			this._nextIntention = null;
			super.changeIntention(intention, args);
		}
	}

	@Override
	protected void onActionReadyToAct()
	{
		if (this._nextIntention != null)
		{
			this.setIntention(this._nextIntention._intention, this._nextIntention._arg0, this._nextIntention._arg1);
			this._nextIntention = null;
		}

		super.onActionReadyToAct();
	}

	@Override
	protected void onActionCancel()
	{
		this._nextIntention = null;
		super.onActionCancel();
	}

	@Override
	protected void onActionFinishCasting()
	{
		if (this.getIntention() == Intention.CAST)
		{
			CreatureAI.IntentionCommand nextIntention = this._nextIntention;
			if (nextIntention != null)
			{
				if (nextIntention._intention != Intention.CAST)
				{
					this.setIntention(nextIntention._intention, nextIntention._arg0, nextIntention._arg1);
				}
				else
				{
					this.setIntention(Intention.IDLE);
				}
			}
			else
			{
				this.setIntention(Intention.IDLE);
			}
		}
	}

	@Override
	protected void onActionAttacked(Creature attacker)
	{
		super.onActionAttacked(attacker);
		Player player = this._actor.asPlayer();
		if (player.hasServitors())
		{
			for (Summon summon : player.getServitors().values())
			{
				SummonAI ai = (SummonAI) summon.getAI();
				if (ai.isDefending())
				{
					ai.defendAttack(attacker);
				}
			}
		}
	}

	@Override
	protected void onActionEvaded(Creature attacker)
	{
		super.onActionEvaded(attacker);
		Player player = this._actor.asPlayer();
		if (player.hasServitors())
		{
			for (Summon summon : player.getServitors().values())
			{
				SummonAI ai = (SummonAI) summon.getAI();
				if (ai.isDefending())
				{
					ai.defendAttack(attacker);
				}
			}
		}
	}

	@Override
	protected void onIntentionRest()
	{
		if (this.getIntention() != Intention.REST)
		{
			this.changeIntention(Intention.REST);
			this.setTarget(null);
			this.clientStopMoving(null);
		}
	}

	@Override
	protected void onIntentionActive()
	{
		this.setIntention(Intention.IDLE);
	}

	@Override
	protected void onIntentionMoveTo(ILocational loc)
	{
		if (this.getIntention() == Intention.REST)
		{
			this.clientActionFailed();
		}
		else if (!this._actor.isAllSkillsDisabled() && !this._actor.isCastingNow() && !this._actor.isAttackingNow())
		{
			this.changeIntention(Intention.MOVE_TO, loc);
			this.clientStopAutoAttack();
			this._actor.abortAttack();
			this.moveTo(loc.getX(), loc.getY(), loc.getZ());
		}
		else
		{
			this.clientActionFailed();
			this.saveNextIntention(Intention.MOVE_TO, loc, null);
		}
	}

	@Override
	protected void clientNotifyDead()
	{
		this._clientMovingToPawnOffset = 0;
		super.clientNotifyDead();
	}

	private void thinkAttack()
	{
		Player player = this._actor.asPlayer();
		SkillUseHolder queuedSkill = player.getQueuedSkill();
		if (queuedSkill != null)
		{
			player.setQueuedSkill(null, null, false, false);
			if (player.getCurrentMp() >= player.getStat().getMpInitialConsume(queuedSkill.getSkill()))
			{
				player.abortAttack();
				if (!player.isChargedShot(ShotType.SOULSHOTS) && !player.isChargedShot(ShotType.BLESSED_SOULSHOTS))
				{
					player.rechargeShots(true, false, false);
				}

				player.useMagic(queuedSkill.getSkill(), queuedSkill.getItem(), queuedSkill.isCtrlPressed(), queuedSkill.isShiftPressed());
				return;
			}
		}

		WorldObject target = this.getTarget();
		if (target != null && target.isCreature())
		{
			if (this.checkTargetLostOrDead(target.asCreature()))
			{
				this.setTarget(null);
			}
			else if (!this.maybeMoveToPawn(target, this._actor.getPhysicalAttackRange()))
			{
				this.clientStopMoving(null);
				this._actor.doAutoAttack(target.asCreature());
			}
		}
	}

	private void thinkCast()
	{
		WorldObject target = this.getCastTarget();
		if (this._skill.getTargetType() == TargetType.GROUND && this._actor.isPlayer())
		{
			if (this.maybeMoveToPosition(this._actor.asPlayer().getCurrentSkillWorldPosition(), this._actor.getMagicalAttackRange(this._skill)))
			{
				return;
			}
		}
		else
		{
			if (this.checkTargetLost(target))
			{
				if (this._skill.hasNegativeEffect() && target != null)
				{
					this.setCastTarget(null);
					this.setTarget(null);
				}

				return;
			}

			if (target != null && this.maybeMoveToPawn(target, this._actor.getMagicalAttackRange(this._skill)))
			{
				return;
			}
		}

		WorldObject currentTarget = this._actor.getTarget();
		if (currentTarget != target && currentTarget != null && target != null)
		{
			this._actor.setTarget(target);
			this._actor.doCast(this._skill, this._item, this._forceUse, this._dontMove);
			this._actor.setTarget(currentTarget);
		}
		else
		{
			this._actor.doCast(this._skill, this._item, this._forceUse, this._dontMove);
		}
	}

	private void thinkPickUp()
	{
		if (!this._actor.isAllSkillsDisabled() && !this._actor.isCastingNow())
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
	}

	private void thinkInteract()
	{
		if (!this._actor.isAllSkillsDisabled() && !this._actor.isCastingNow())
		{
			WorldObject target = this.getTarget();
			if (!this.checkTargetLost(target))
			{
				if (!this.maybeMoveToPawn(target, 36))
				{
					if (!(target instanceof StaticObject))
					{
						this.getActor().doInteract(target.asCreature());
					}

					this.setIntention(Intention.IDLE);
				}
			}
		}
	}

	@Override
	public void onActionThink()
	{
		if (!this._thinking || this.getIntention() == Intention.CAST)
		{
			this._thinking = true;

			try
			{
				if (this.getIntention() == Intention.ATTACK)
				{
					this.thinkAttack();
				}
				else if (this.getIntention() == Intention.CAST)
				{
					this.thinkCast();
				}
				else if (this.getIntention() == Intention.PICK_UP)
				{
					this.thinkPickUp();
				}
				else if (this.getIntention() == Intention.INTERACT)
				{
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
	public Player getActor()
	{
		return super.getActor().asPlayer();
	}
}
