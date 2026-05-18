package net.sf.l2jdev.gameserver.ai;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.geoengine.GeoEngine;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Attackable;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.enums.npc.AIType;

public class FriendlyNpcAI extends AttackableAI
{
	public FriendlyNpcAI(Attackable attackable)
	{
		super(attackable);
	}

	@Override
	protected void onActionAttacked(Creature attacker)
	{
	}

	@Override
	protected void onActionAggression(Creature target, int aggro)
	{
	}

	@Override
	protected void onIntentionAttack(Creature target)
	{
		if (target == null)
		{
			this.clientActionFailed();
		}
		else if (this.getIntention() == Intention.REST)
		{
			this.clientActionFailed();
		}
		else if (!this._actor.isAllSkillsDisabled() && !this._actor.isCastingNow() && !this._actor.isControlBlocked())
		{
			this.changeIntention(Intention.ATTACK, target);
			this.setTarget(target);
			this.stopFollow();
			this.notifyAction(Action.THINK, null);
		}
		else
		{
			this.clientActionFailed();
		}
	}

	@Override
	protected void thinkAttack()
	{
		Attackable npc = this.getActiveChar();
		if (!npc.isCastingNow() && !npc.isCoreAIDisabled())
		{
			WorldObject target = this.getTarget();
			Creature originalAttackTarget = target != null && target.isCreature() ? target.asCreature() : null;
			if (originalAttackTarget != null && !originalAttackTarget.isAlikeDead())
			{
				int collision = npc.getTemplate().getCollisionRadius();
				this.setTarget(originalAttackTarget);
				int combinedCollision = collision + originalAttackTarget.getTemplate().getCollisionRadius();
				if (!npc.isMovementDisabled() && Rnd.get(100) <= 3)
				{
					for (Attackable nearby : World.getInstance().getVisibleObjects(npc, Attackable.class))
					{
						if (npc.isInsideRadius2D(nearby, collision) && nearby != originalAttackTarget)
						{
							int newX = combinedCollision + Rnd.get(40);
							if (Rnd.nextBoolean())
							{
								newX += originalAttackTarget.getX();
							}
							else
							{
								newX = originalAttackTarget.getX() - newX;
							}

							int newY = combinedCollision + Rnd.get(40);
							if (Rnd.nextBoolean())
							{
								newY += originalAttackTarget.getY();
							}
							else
							{
								newY = originalAttackTarget.getY() - newY;
							}

							if (!npc.isInsideRadius2D(newX, newY, 0, collision))
							{
								int newZ = npc.getZ() + 30;
								if (GeoEngine.getInstance().canMoveToTarget(npc.getX(), npc.getY(), npc.getZ(), newX, newY, newZ, npc.getInstanceWorld()))
								{
									this.moveTo(newX, newY, newZ);
								}
							}

							return;
						}
					}
				}

				if (!npc.isMovementDisabled() && npc.getAiType() == AIType.ARCHER && Rnd.get(100) < 15)
				{
					double distance = npc.calculateDistance2D(originalAttackTarget);
					if (distance <= 60 + combinedCollision)
					{
						int posX = npc.getX();
						int posY = npc.getY();
						int posZ = npc.getZ() + 30;
						if (originalAttackTarget.getX() < posX)
						{
							posX += 300;
						}
						else
						{
							posX -= 300;
						}

						if (originalAttackTarget.getY() < posY)
						{
							posY += 300;
						}
						else
						{
							posY -= 300;
						}

						if (GeoEngine.getInstance().canMoveToTarget(npc.getX(), npc.getY(), npc.getZ(), posX, posY, posZ, npc.getInstanceWorld()))
						{
							this.setIntention(Intention.MOVE_TO, new Location(posX, posY, posZ, 0));
						}

						return;
					}
				}

				double dist = npc.calculateDistance2D(originalAttackTarget);
				int dist2 = (int) dist - collision;
				int range = npc.getPhysicalAttackRange() + combinedCollision;
				if (originalAttackTarget.isMoving())
				{
					range += 50;
					if (npc.isMoving())
					{
						range += 50;
					}
				}

				if (dist2 <= range && GeoEngine.getInstance().canSeeTarget(npc, originalAttackTarget))
				{
					this._actor.doAutoAttack(originalAttackTarget);
				}
				else
				{
					if (originalAttackTarget.isMoving())
					{
						range -= 100;
					}

					if (range < 5)
					{
						range = 5;
					}

					this.moveToPawn(originalAttackTarget, range);
				}
			}
			else
			{
				if (originalAttackTarget != null)
				{
					npc.stopHating(originalAttackTarget);
				}

				this.setIntention(Intention.ACTIVE);
				npc.setWalking();
			}
		}
	}

	@Override
	protected void thinkCast()
	{
		WorldObject target = this.getCastTarget();
		if (this.checkTargetLost(target))
		{
			this.setCastTarget(null);
			this.setTarget(null);
		}
		else if (!this.maybeMoveToPawn(target, this._actor.getMagicalAttackRange(this._skill)))
		{
			this._actor.doCast(this._skill, this._item, this._forceUse, this._dontMove);
		}
	}
}
