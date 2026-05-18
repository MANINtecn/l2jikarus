package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.instance.ControllableAirShip;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionPlayerCanRefuelAirship extends Condition
{
	private final int _value;

	public ConditionPlayerCanRefuelAirship(int value)
	{
		this._value = value;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		boolean canRefuelAirship = true;
		Player player = effector.asPlayer();
		if (player == null || player.getAirShip() == null || !(player.getAirShip() instanceof ControllableAirShip) || player.getAirShip().getFuel() + this._value > player.getAirShip().getMaxFuel())
		{
			canRefuelAirship = false;
		}

		return canRefuelAirship;
	}
}
