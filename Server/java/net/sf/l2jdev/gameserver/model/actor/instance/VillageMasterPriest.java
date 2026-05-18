package net.sf.l2jdev.gameserver.model.actor.instance;

import net.sf.l2jdev.gameserver.data.enums.CategoryType;
import net.sf.l2jdev.gameserver.data.xml.CategoryData;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Race;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PlayerClass;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;

public class VillageMasterPriest extends VillageMaster
{
	public VillageMasterPriest(NpcTemplate template)
	{
		super(template);
	}

	@Override
	protected final boolean checkVillageMasterRace(PlayerClass pClass)
	{
		return pClass == null ? false : pClass.getRace() == Race.HUMAN || pClass.getRace() == Race.ELF;
	}

	@Override
	protected final boolean checkVillageMasterTeachType(PlayerClass pClass)
	{
		return pClass == null ? false : CategoryData.getInstance().isInCategory(CategoryType.CLERIC_GROUP, pClass.getId());
	}
}
