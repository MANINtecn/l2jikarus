package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionPlayerSex extends Condition
{
	private final int _sex;

	public ConditionPlayerSex(int sex)
	{
		this._sex = sex;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		Player player = effector.asPlayer();
		return player == null ? false : (player.getAppearance().isFemale() ? 1 : 0) == this._sex;
	}
}
