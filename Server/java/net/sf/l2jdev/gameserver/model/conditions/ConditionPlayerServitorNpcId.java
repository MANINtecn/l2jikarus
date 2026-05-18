package net.sf.l2jdev.gameserver.model.conditions;

import java.util.List;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionPlayerServitorNpcId extends Condition
{
	private final List<Integer> _npcIds;

	public ConditionPlayerServitorNpcId(List<Integer> npcIds)
	{
		if (npcIds.size() == 1 && npcIds.get(0) == 0)
		{
			this._npcIds = null;
		}
		else
		{
			this._npcIds = npcIds;
		}
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		Player player = effector.asPlayer();
		if (player != null && player.hasSummon())
		{
			if (this._npcIds == null)
			{
				return true;
			}
			for (Summon summon : effector.getServitors().values())
			{
				if (this._npcIds.contains(summon.getId()))
				{
					return true;
				}
			}

			return false;
		}
		return false;
	}
}
