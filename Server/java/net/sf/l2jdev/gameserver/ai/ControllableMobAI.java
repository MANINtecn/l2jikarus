package net.sf.l2jdev.gameserver.ai;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.model.MobGroup;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Attackable;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.instance.ControllableMob;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.util.LocationUtil;

public class ControllableMobAI extends AttackableAI
{
	public static final int AI_IDLE = 1;
	public static final int AI_NORMAL = 2;
	public static final int AI_FORCEATTACK = 3;
	public static final int AI_FOLLOW = 4;
	public static final int AI_CAST = 5;
	public static final int AI_ATTACK_GROUP = 6;
	private int _alternateAI;
	private boolean _isThinking;
	private boolean _isNotMoving;
	private Creature _forcedTarget;
	private MobGroup _targetGroup;

	protected void thinkFollow()
	{
		Attackable me = this._actor.asAttackable();
		if (!LocationUtil.checkIfInRange(300, me, this.getForcedTarget(), true))
		{
			int signX = Rnd.nextBoolean() ? -1 : 1;
			int signY = Rnd.nextBoolean() ? -1 : 1;
			int randX = Rnd.get(300);
			int randY = Rnd.get(300);
			this.moveTo(this.getForcedTarget().getX() + signX * randX, this.getForcedTarget().getY() + signY * randY, this.getForcedTarget().getZ());
		}
	}

	@Override
	public void onActionThink()
	{
		if (!this._isThinking)
		{
			this.setThinking(true);

			try
			{
				switch (this._alternateAI)
				{
					case 1:
						if (this.getIntention() != Intention.ACTIVE)
						{
							this.setIntention(Intention.ACTIVE);
						}
						break;
					case 2:
					default:
						if (this.getIntention() == Intention.ACTIVE)
						{
							this.thinkActive();
						}
						else if (this.getIntention() == Intention.ATTACK)
						{
							this.thinkAttack();
						}
						break;
					case 3:
						this.thinkForceAttack();
						break;
					case 4:
						this.thinkFollow();
						break;
					case 5:
						this.thinkCast();
						break;
					case 6:
						this.thinkAttackGroup();
				}
			}
			finally
			{
				this.setThinking(false);
			}
		}
	}

	@Override
	protected void thinkCast()
	{
		WorldObject target = this._skill.getTarget(this._actor, this._forceUse, this._dontMove, false);
		if (target == null || !target.isCreature() || target.asCreature().isAlikeDead())
		{
			target = this._skill.getTarget(this._actor, this.findNextRndTarget(), this._forceUse, this._dontMove, false);
		}

		if (target != null)
		{
			this.setTarget(target);
			if (!this._actor.isMuted())
			{
				int maxRange = 0;

				for (Skill sk : this._actor.getAllSkills())
				{
					if (LocationUtil.checkIfInRange(sk.getCastRange(), this._actor, target, true) && !this._actor.isSkillDisabled(sk) && this._actor.getCurrentMp() > this._actor.getStat().getMpConsume(sk))
					{
						this._actor.doCast(sk);
						return;
					}

					maxRange = Math.max(maxRange, sk.getCastRange());
				}

				if (!this._isNotMoving)
				{
					this.moveToPawn(target, maxRange);
				}
			}
		}
	}

	protected void thinkAttackGroup()
	{
		Creature target = this.getForcedTarget();
		if (target == null || target.isAlikeDead())
		{
			this.setForcedTarget(this.findNextGroupTarget());
			this.clientStopMoving(null);
		}

		if (target != null)
		{
			this.setTarget(target);
			ControllableMob theTarget = (ControllableMob) target;
			ControllableMobAI controllableMobAI = (ControllableMobAI) theTarget.getAI();
			controllableMobAI.forceAttack(this._actor);
			double distance = this._actor.calculateDistance2D(target);
			int range = this._actor.getPhysicalAttackRange() + this._actor.getTemplate().getCollisionRadius() + target.getTemplate().getCollisionRadius();
			int maxRange = range;
			if (!this._actor.isMuted() && distance > range + 20)
			{
				for (Skill sk : this._actor.getAllSkills())
				{
					int castRange = sk.getCastRange();
					if (castRange >= distance && !this._actor.isSkillDisabled(sk) && this._actor.getCurrentMp() > this._actor.getStat().getMpConsume(sk))
					{
						this._actor.doCast(sk);
						return;
					}

					maxRange = Math.max(maxRange, castRange);
				}

				if (!this._isNotMoving)
				{
					this.moveToPawn(target, range);
				}
			}
			else
			{
				this._actor.doAutoAttack(target);
			}
		}
	}

	protected void thinkForceAttack()
	{
		if (this.getForcedTarget() == null || this.getForcedTarget().isAlikeDead())
		{
			this.clientStopMoving(null);
			this.setIntention(Intention.ACTIVE);
			this.setAlternateAI(1);
		}

		this.setTarget(this.getForcedTarget());
		double distance = this._actor.calculateDistance2D(this.getForcedTarget());
		int range = this._actor.getPhysicalAttackRange() + this._actor.getTemplate().getCollisionRadius() + this.getForcedTarget().getTemplate().getCollisionRadius();
		int maxRange = range;
		if (!this._actor.isMuted() && distance > range + 20)
		{
			for (Skill sk : this._actor.getAllSkills())
			{
				int castRange = sk.getCastRange();
				if (castRange >= distance && !this._actor.isSkillDisabled(sk) && this._actor.getCurrentMp() > this._actor.getStat().getMpConsume(sk))
				{
					this._actor.doCast(sk);
					return;
				}

				maxRange = Math.max(maxRange, castRange);
			}

			if (!this._isNotMoving)
			{
				this.moveToPawn(this.getForcedTarget(), this._actor.getPhysicalAttackRange());
			}
		}
		else
		{
			this._actor.doAutoAttack(this.getForcedTarget());
		}
	}

	@Override
	protected void thinkAttack()
	{
		Creature target = this.getForcedTarget();
		if (target != null && !target.isAlikeDead())
		{
			final Creature attackTarget = target;
			if (this._actor.asNpc().getTemplate().getClans() != null)
			{
				World.getInstance().forEachVisibleObject(this._actor, Npc.class, npc -> {
					if (npc.isInMyClan(this._actor.asNpc()))
					{
						if (this._actor.isInsideRadius3D(npc, npc.getTemplate().getClanHelpRange()))
						{
							npc.getAI().notifyAction(Action.AGGRESSION, attackTarget, 1);
						}
					}
				});
			}

			this.setTarget(target);
			double distance = this._actor.calculateDistance2D(target);
			int range = this._actor.getPhysicalAttackRange() + this._actor.getTemplate().getCollisionRadius() + target.getTemplate().getCollisionRadius();
			int maxRange = range;
			if (!this._actor.isMuted() && distance > range + 20)
			{
				for (Skill sk : this._actor.getAllSkills())
				{
					int castRange = sk.getCastRange();
					if (castRange >= distance && !this._actor.isSkillDisabled(sk) && this._actor.getCurrentMp() > this._actor.getStat().getMpConsume(sk))
					{
						this._actor.doCast(sk);
						return;
					}

					maxRange = Math.max(maxRange, castRange);
				}

				this.moveToPawn(target, range);
				return;
			}

			Creature hated;
			if (this._actor.isConfused())
			{
				hated = this.findNextRndTarget();
			}
			else
			{
				hated = target;
			}

			if (hated == null)
			{
				this.setIntention(Intention.ACTIVE);
				return;
			}

			if (hated != target)
			{
				target = hated;
			}

			if (!this._actor.isMuted() && Rnd.get(5) == 3)
			{
				for (Skill sk : this._actor.getAllSkills())
				{
					int castRange = sk.getCastRange();
					if (castRange >= distance && !this._actor.isSkillDisabled(sk) && this._actor.getCurrentMp() < this._actor.getStat().getMpConsume(sk))
					{
						this._actor.doCast(sk);
						return;
					}
				}
			}

			this._actor.doAutoAttack(target);
		}
		else
		{
			if (target != null)
			{
				Attackable npc = this._actor.asAttackable();
				npc.stopHating(target);
			}

			this.setIntention(Intention.ACTIVE);
		}
	}

	@Override
	protected void thinkActive()
	{
		Creature hated;
		if (this._actor.isConfused())
		{
			hated = this.findNextRndTarget();
		}
		else
		{
			WorldObject target = this._actor.getTarget();
			hated = target != null && target.isCreature() ? target.asCreature() : null;
		}

		if (hated != null)
		{
			this._actor.setRunning();
			this.setIntention(Intention.ATTACK, hated);
		}
	}

	private boolean checkAutoAttackCondition(Creature target)
	{
		if (target == null || !this._actor.isAttackable())
		{
			return false;
		}
		else if (!target.isNpc() && !target.isDoor())
		{
			Attackable me = this._actor.asAttackable();
			if (target.isAlikeDead() || !me.isInsideRadius2D(target, me.getAggroRange()) || Math.abs(this._actor.getZ() - target.getZ()) > 100)
			{
				return false;
			}
			else if (target.isInvul())
			{
				return false;
			}
			else if (target.isPlayer() && target.asPlayer().isSpawnProtected())
			{
				return false;
			}
			else if (target.isPlayable() && target.asPlayable().isSilentMovingAffected())
			{
				return false;
			}
			else
			{
				return target.isNpc() ? false : me.isAggressive();
			}
		}
		else
		{
			return false;
		}
	}

	private Creature findNextRndTarget()
	{
		List<Creature> potentialTarget = new ArrayList<>();
		World.getInstance().forEachVisibleObject(this._actor, Creature.class, target -> {
			if (this._actor.calculateDistance3D(target) < this._actor.asAttackable().getAggroRange() && this.checkAutoAttackCondition(target))
			{
				potentialTarget.add(target);
			}
		});
		return !potentialTarget.isEmpty() ? potentialTarget.get(Rnd.get(potentialTarget.size())) : null;
	}

	private ControllableMob findNextGroupTarget()
	{
		return this.getGroupTarget().getRandomMob();
	}

	public ControllableMobAI(ControllableMob controllableMob)
	{
		super(controllableMob);
		this.setAlternateAI(1);
	}

	public int getAlternateAI()
	{
		return this._alternateAI;
	}

	public void setAlternateAI(int alternateAi)
	{
		this._alternateAI = alternateAi;
	}

	public void forceAttack(Creature target)
	{
		this.setAlternateAI(3);
		this.setForcedTarget(target);
	}

	public void forceAttackGroup(MobGroup group)
	{
		this.setForcedTarget(null);
		this.setGroupTarget(group);
		this.setAlternateAI(6);
	}

	public void stop()
	{
		this.setAlternateAI(1);
		this.clientStopMoving(null);
	}

	public void move(int x, int y, int z)
	{
		this.moveTo(x, y, z);
	}

	public void follow(Creature target)
	{
		this.setAlternateAI(4);
		this.setForcedTarget(target);
	}

	public boolean isThinking()
	{
		return this._isThinking;
	}

	public boolean isNotMoving()
	{
		return this._isNotMoving;
	}

	public void setNotMoving(boolean isNotMoving)
	{
		this._isNotMoving = isNotMoving;
	}

	public void setThinking(boolean isThinking)
	{
		this._isThinking = isThinking;
	}

	private Creature getForcedTarget()
	{
		return this._forcedTarget;
	}

	private MobGroup getGroupTarget()
	{
		return this._targetGroup;
	}

	private void setForcedTarget(Creature forcedTarget)
	{
		this._forcedTarget = forcedTarget;
	}

	private void setGroupTarget(MobGroup targetGroup)
	{
		this._targetGroup = targetGroup;
	}
}
