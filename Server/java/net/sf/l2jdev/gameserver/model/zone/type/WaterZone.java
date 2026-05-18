package net.sf.l2jdev.gameserver.model.zone.type;

import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.transform.Transform;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.model.zone.ZoneType;
import net.sf.l2jdev.gameserver.network.serverpackets.FakePlayerInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.NpcInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerObjectInfo;

public class WaterZone extends ZoneType
{
	public WaterZone(int id)
	{
		super(id);
	}

	@Override
	protected void onEnter(Creature creature)
	{
		if (!creature.isInsideZone(ZoneId.WATER))
		{
			creature.setInsideZone(ZoneId.WATER, true);
		}

		if (creature.isPlayer())
		{
			Player player = creature.asPlayer();
			Transform transform = player.getTransformation();
			if (transform != null && !transform.canSwim())
			{
				creature.stopTransformation(true);
			}
			else
			{
				player.broadcastUserInfo();
			}
		}
		else if (creature.isNpc())
		{
			World.getInstance().forEachVisibleObject(creature, Player.class, playerx -> {
				if (creature.isFakePlayer())
				{
					playerx.sendPacket(new FakePlayerInfo(creature.asNpc()));
				}
				else if (creature.getRunSpeed() == 0.0)
				{
					playerx.sendPacket(new ServerObjectInfo(creature.asNpc(), playerx));
				}
				else
				{
					playerx.sendPacket(new NpcInfo(creature.asNpc()));
				}
			});
		}
	}

	@Override
	protected void onExit(Creature creature)
	{
		if (creature.isInsideZone(ZoneId.WATER))
		{
			creature.setInsideZone(ZoneId.WATER, false);
		}

		if (creature.isPlayer())
		{
			Player player = creature.asPlayer();
			if (!player.isInsideZone(ZoneId.WATER))
			{
				player.stopWaterTask();
			}

			if (!player.isTeleporting())
			{
				player.broadcastUserInfo();
			}
		}
		else if (creature.isNpc())
		{
			World.getInstance().forEachVisibleObject(creature, Player.class, playerx -> {
				if (creature.isFakePlayer())
				{
					playerx.sendPacket(new FakePlayerInfo(creature.asNpc()));
				}
				else if (creature.getRunSpeed() == 0.0)
				{
					playerx.sendPacket(new ServerObjectInfo(creature.asNpc(), playerx));
				}
				else
				{
					playerx.sendPacket(new NpcInfo(creature.asNpc()));
				}
			});
		}
	}

	public int getWaterZ()
	{
		return this.getZone().getHighZ();
	}
}
