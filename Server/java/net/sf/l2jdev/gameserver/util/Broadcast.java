package net.sf.l2jdev.gameserver.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.gameserver.cache.RelationCache;
import net.sf.l2jdev.gameserver.managers.ZoneManager;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.zone.ZoneType;
import net.sf.l2jdev.gameserver.network.enums.ChatType;
import net.sf.l2jdev.gameserver.network.serverpackets.CharInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2jdev.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.RelationChanged;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class Broadcast
{
	private static final Logger LOGGER = Logger.getLogger(Broadcast.class.getName());

	public static void toPlayersTargettingMyself(Creature creature, ServerPacket packet)
	{
		packet.sendInBroadcast();
		World.getInstance().forEachVisibleObject(creature, Player.class, player -> {
			if (player.getTarget() == creature)
			{
				player.sendPacket(packet);
			}
		});
	}

	public static void toKnownPlayers(Creature creature, ServerPacket packet)
	{
		packet.sendInBroadcast();
		World.getInstance().forEachVisibleObject(creature, Player.class, player -> {
			try
			{
				player.sendPacket(packet);
				if (packet instanceof CharInfo && creature.isPlayer())
				{
					long relation = creature.asPlayer().getRelation(player);
					boolean isAutoAttackable = creature.isAutoAttackable(player);
					RelationCache oldrelation = creature.getKnownRelations().get(player.getObjectId());
					if (oldrelation == null || oldrelation.getRelation() != relation || oldrelation.isAutoAttackable() != isAutoAttackable)
					{
						RelationChanged rc = new RelationChanged();
						rc.addRelation(creature.asPlayer(), relation, isAutoAttackable);
						if (creature.hasSummon())
						{
							Summon pet = creature.getPet();
							if (pet != null)
							{
								rc.addRelation(pet, relation, isAutoAttackable);
							}

							if (creature.hasServitors())
							{
								creature.getServitors().values().forEach(s -> rc.addRelation(s, relation, isAutoAttackable));
							}
						}

						player.sendPacket(rc);
						creature.getKnownRelations().put(player.getObjectId(), new RelationCache(relation, isAutoAttackable));
					}
				}
			}
			catch (NullPointerException var9)
			{
				LOGGER.log(Level.WARNING, var9.getMessage(), var9);
			}
		});
	}

	public static void toKnownPlayersInRadius(Creature creature, ServerPacket packet, int radiusValue)
	{
		int radius = radiusValue;
		if (radiusValue < 0)
		{
			radius = 1500;
		}

		packet.sendInBroadcast();
		World.getInstance().forEachVisibleObjectInRange(creature, Player.class, radius, player -> player.sendPacket(packet));
	}

	public static void toSelfAndKnownPlayers(Creature creature, ServerPacket packet)
	{
		packet.sendInBroadcast();
		if (creature.isPlayer())
		{
			creature.sendPacket(packet);
		}

		toKnownPlayers(creature, packet);
	}

	public static void toSelfAndKnownPlayersInRadius(Creature creature, ServerPacket packet, int radiusValue)
	{
		int radius = radiusValue;
		if (radiusValue < 0)
		{
			radius = 600;
		}

		packet.sendInBroadcast();
		if (creature.isPlayer())
		{
			creature.sendPacket(packet);
		}

		World.getInstance().forEachVisibleObjectInRange(creature, Player.class, radius, player -> player.sendPacket(packet));
	}

	public static void toAllOnlinePlayers(ServerPacket packet)
	{
		packet.sendInBroadcast();

		for (Player player : World.getInstance().getPlayers())
		{
			if (player.isOnline())
			{
				player.sendPacket(packet);
			}
		}
	}

	public static void toAllOnlinePlayers(String text)
	{
		toAllOnlinePlayers(text, false);
	}

	public static void toAllOnlinePlayers(String text, boolean isCritical)
	{
		toAllOnlinePlayers(new CreatureSay(null, isCritical ? ChatType.CRITICAL_ANNOUNCE : ChatType.ANNOUNCEMENT, "", text));
	}

	public static void toAllOnlinePlayersOnScreen(String text)
	{
		toAllOnlinePlayers(new ExShowScreenMessage(text, 10000));
	}

	public static <T extends ZoneType> void toAllPlayersInZoneType(Class<T> zoneType, ServerPacket... packets)
	{
		for (ServerPacket packet : packets)
		{
			packet.sendInBroadcast();
		}

		for (ZoneType zone : ZoneManager.getInstance().getAllZones(zoneType))
		{
			for (Creature creature : zone.getCharactersInside())
			{
				if (creature != null)
				{
					for (ServerPacket packet : packets)
					{
						creature.sendPacket(packet);
					}
				}
			}
		}
	}
}
