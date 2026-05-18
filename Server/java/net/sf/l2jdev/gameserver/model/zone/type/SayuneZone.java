package net.sf.l2jdev.gameserver.model.zone.type;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.config.custom.SayuneForAllConfig;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.tasks.player.FlyMoveStartTask;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.model.zone.ZoneType;

public class SayuneZone extends ZoneType
{
	private int _mapId = -1;

	public SayuneZone(int id)
	{
		super(id);
	}

	@Override
	public void setParameter(String name, String value)
	{
		byte var4 = -1;
		switch (name.hashCode())
		{
			case 103663511:
				if (name.equals("mapId"))
				{
					var4 = 0;
				}
			default:
				switch (var4)
				{
					case 0:
						this._mapId = Integer.parseInt(value);
						break;
					default:
						super.setParameter(name, value);
				}
		}
	}

	@Override
	protected void onEnter(Creature creature)
	{
		if (creature.isPlayer() && SayuneForAllConfig.FREE_JUMPS_FOR_ALL && !creature.isTransformed())
		{
			Player player = creature.asPlayer();
			if (!player.isMounted())
			{
				creature.setInsideZone(ZoneId.SAYUNE, true);
				ThreadPool.execute(new FlyMoveStartTask(this, player));
			}
		}
	}

	@Override
	protected void onExit(Creature creature)
	{
		if (creature.isPlayer())
		{
			creature.setInsideZone(ZoneId.SAYUNE, false);
		}
	}

	public int getMapId()
	{
		return this._mapId;
	}
}
