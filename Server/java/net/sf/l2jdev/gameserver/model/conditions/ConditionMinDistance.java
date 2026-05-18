package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.geoengine.GeoEngine;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionMinDistance extends Condition
{
	private final int _distance;

	public ConditionMinDistance(int distance)
	{
		this._distance = distance;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		return effected != null && effector.calculateDistance3D(effected) >= this._distance && GeoEngine.getInstance().canSeeTarget(effector, effected);
	}
}
