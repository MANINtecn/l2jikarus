package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.stats.BaseStat;

public class ConditionPlayerBaseStats extends Condition
{
	private final BaseStat _stat;
	private final int _value;

	public ConditionPlayerBaseStats(Creature creature, BaseStat stat, int value)
	{
		this._stat = stat;
		this._value = value;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		Player player = effector.asPlayer();
		if (player == null)
		{
			return false;
		}
		switch (this._stat)
		{
			case INT:
				return player.getINT() >= this._value;
			case STR:
				return player.getSTR() >= this._value;
			case CON:
				return player.getCON() >= this._value;
			case DEX:
				return player.getDEX() >= this._value;
			case MEN:
				return player.getMEN() >= this._value;
			case WIT:
				return player.getWIT() >= this._value;
			default:
				return false;
		}
	}
}
