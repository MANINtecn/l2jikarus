package net.sf.l2jdev.gameserver.model.actor.instance;

import net.sf.l2jdev.gameserver.model.actor.enums.creature.Race;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PlayerClass;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;

public class VillageMasterOrc extends VillageMaster
{
	public VillageMasterOrc(NpcTemplate template)
	{
		super(template);
	}

	@Override
	protected final boolean checkVillageMasterRace(PlayerClass pClass)
	{
		return pClass == null ? false : pClass.getRace() == Race.ORC;
	}
}
