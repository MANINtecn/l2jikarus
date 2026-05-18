package net.sf.l2jdev.gameserver.model.zone.type;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.model.zone.ZoneType;

public class ConditionZone extends ZoneType
{
	private boolean NO_ITEM_DROP = false;
	private boolean NO_BOOKMARK = false;

	public ConditionZone(int id)
	{
		super(id);
	}

	@Override
	public void setParameter(String name, String value)
	{
		if (name.equalsIgnoreCase("NoBookmark"))
		{
			this.NO_BOOKMARK = Boolean.parseBoolean(value);
		}
		else if (name.equalsIgnoreCase("NoItemDrop"))
		{
			this.NO_ITEM_DROP = Boolean.parseBoolean(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}

	@Override
	protected void onEnter(Creature creature)
	{
		if (creature.isPlayer())
		{
			if (this.NO_BOOKMARK)
			{
				creature.setInsideZone(ZoneId.NO_BOOKMARK, true);
			}

			if (this.NO_ITEM_DROP)
			{
				creature.setInsideZone(ZoneId.NO_ITEM_DROP, true);
			}
		}
	}

	@Override
	protected void onExit(Creature creature)
	{
		if (creature.isPlayer())
		{
			if (this.NO_BOOKMARK)
			{
				creature.setInsideZone(ZoneId.NO_BOOKMARK, false);
			}

			if (this.NO_ITEM_DROP)
			{
				creature.setInsideZone(ZoneId.NO_ITEM_DROP, false);
			}
		}
	}
}
