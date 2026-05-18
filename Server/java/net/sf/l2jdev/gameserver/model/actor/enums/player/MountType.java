package net.sf.l2jdev.gameserver.model.actor.enums.player;

import net.sf.l2jdev.gameserver.data.enums.CategoryType;
import net.sf.l2jdev.gameserver.data.xml.CategoryData;

public enum MountType
{
	NONE,
	STRIDER,
	WYVERN,
	WOLF;

	public static MountType findByNpcId(int npcId)
	{
		if (CategoryData.getInstance().isInCategory(CategoryType.STRIDER, npcId))
		{
			return STRIDER;
		}
		else if (CategoryData.getInstance().isInCategory(CategoryType.WYVERN_GROUP, npcId))
		{
			return WYVERN;
		}
		else
		{
			return CategoryData.getInstance().isInCategory(CategoryType.WOLF_GROUP, npcId) ? WOLF : NONE;
		}
	}
}
