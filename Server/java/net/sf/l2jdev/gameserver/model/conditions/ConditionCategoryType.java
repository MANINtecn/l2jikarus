package net.sf.l2jdev.gameserver.model.conditions;

import java.util.Set;

import net.sf.l2jdev.gameserver.data.enums.CategoryType;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionCategoryType extends Condition
{
	private final Set<CategoryType> _categoryTypes;

	public ConditionCategoryType(Set<CategoryType> categoryTypes)
	{
		this._categoryTypes = categoryTypes;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		for (CategoryType type : this._categoryTypes)
		{
			if (effector.isInCategory(type))
			{
				return true;
			}
		}

		return false;
	}
}
