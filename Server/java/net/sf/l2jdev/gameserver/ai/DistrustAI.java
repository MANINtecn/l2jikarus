package net.sf.l2jdev.gameserver.ai;

import net.sf.l2jdev.gameserver.geoengine.GeoEngine;
import net.sf.l2jdev.gameserver.model.actor.Attackable;
import net.sf.l2jdev.gameserver.model.actor.Creature;

public class DistrustAI extends AttackableAI
{
	private final Creature _forcedTarget;

	public DistrustAI(Attackable actor, Creature forcedTarget)
	{
		super(actor);
		this._forcedTarget = forcedTarget;
	}

	@Override
	public void thinkAttack()
	{
		if (this._forcedTarget != null && !this._forcedTarget.isDead())
		{
			this._actor.setTarget(this._forcedTarget);
			this.setIntention(Intention.ATTACK, this._forcedTarget);
			int range = this._actor.getPhysicalAttackRange() + this._forcedTarget.getTemplate().getCollisionRadius();
			if (this._actor.calculateDistance3D(this._forcedTarget) > range)
			{
				this.moveToPawn(this._forcedTarget, range);
			}
			else if (GeoEngine.getInstance().canSeeTarget(this._actor, this._forcedTarget))
			{
				this._actor.doAutoAttack(this._forcedTarget);
			}
		}
		else
		{
			this._actor.setTarget(null);
			this.setIntention(Intention.ACTIVE);
		}
	}
}
