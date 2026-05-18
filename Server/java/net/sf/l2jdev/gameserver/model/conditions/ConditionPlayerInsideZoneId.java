package net.sf.l2jdev.gameserver.model.conditions;

import java.util.Set;

import net.sf.l2jdev.gameserver.managers.ZoneManager;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.zone.ZoneType;

public class ConditionPlayerInsideZoneId extends Condition
{
	private final Set<Integer> _zones;

	public ConditionPlayerInsideZoneId(Set<Integer> zones)
	{
		this._zones = zones;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		if (effector.asPlayer() == null)
		{
			return false;
		}
		for (ZoneType zone : ZoneManager.getInstance().getZones(effector))
		{
			if (this._zones.contains(zone.getId()))
			{
				return true;
			}
		}

		return false;
	}
}
