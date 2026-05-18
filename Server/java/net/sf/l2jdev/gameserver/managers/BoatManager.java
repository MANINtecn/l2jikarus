package net.sf.l2jdev.gameserver.managers;

import java.util.HashMap;
import java.util.Map;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.VehiclePathPoint;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.instance.Boat;
import net.sf.l2jdev.gameserver.model.actor.templates.CreatureTemplate;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class BoatManager
{
	private final Map<Integer, Boat> _boats = new HashMap<>();
	private final boolean[] _docksBusy = new boolean[3];
	public static final int TALKING_ISLAND = 1;
	public static final int GLUDIN_HARBOR = 2;
	public static final int RUNE_HARBOR = 3;

	public static BoatManager getInstance()
	{
		return BoatManager.SingletonHolder.INSTANCE;
	}

	protected BoatManager()
	{
		for (int i = 0; i < this._docksBusy.length; i++)
		{
			this._docksBusy[i] = false;
		}
	}

	public Boat getNewBoat(int boatId, int x, int y, int z, int heading)
	{
		if (!GeneralConfig.ALLOW_BOAT)
		{
			return null;
		}
		StatSet npcDat = new StatSet();
		npcDat.set("npcId", boatId);
		npcDat.set("level", 0);
		npcDat.set("jClass", "boat");
		npcDat.set("baseSTR", 0);
		npcDat.set("baseCON", 0);
		npcDat.set("baseDEX", 0);
		npcDat.set("baseINT", 0);
		npcDat.set("baseWIT", 0);
		npcDat.set("baseMEN", 0);
		npcDat.set("baseShldDef", 0);
		npcDat.set("baseShldRate", 0);
		npcDat.set("baseAccCombat", 38);
		npcDat.set("baseEvasRate", 38);
		npcDat.set("baseCritRate", 38);
		npcDat.set("collision_radius", 0);
		npcDat.set("collision_height", 0);
		npcDat.set("sex", "male");
		npcDat.set("type", "");
		npcDat.set("baseAtkRange", 0);
		npcDat.set("baseMpMax", 0);
		npcDat.set("baseCpMax", 0);
		npcDat.set("rewardExp", 0);
		npcDat.set("rewardSp", 0);
		npcDat.set("basePAtk", 0);
		npcDat.set("baseMAtk", 0);
		npcDat.set("basePAtkSpd", 0);
		npcDat.set("aggroRange", 0);
		npcDat.set("baseMAtkSpd", 0);
		npcDat.set("rhand", 0);
		npcDat.set("lhand", 0);
		npcDat.set("armor", 0);
		npcDat.set("baseWalkSpd", 0);
		npcDat.set("baseRunSpd", 0);
		npcDat.set("baseHpMax", 50000);
		npcDat.set("baseHpReg", 0.003F);
		npcDat.set("baseMpReg", 0.003F);
		npcDat.set("basePDef", 100);
		npcDat.set("baseMDef", 100);
		CreatureTemplate template = new CreatureTemplate(npcDat);
		Boat boat = new Boat(template);
		this._boats.put(boat.getObjectId(), boat);
		boat.setHeading(heading);
		boat.setXYZInvisible(x, y, z);
		boat.spawnMe();
		return boat;
	}

	public Boat getBoat(int boatId)
	{
		return this._boats.get(boatId);
	}

	public void dockShip(int h, boolean value)
	{
		try
		{
			this._docksBusy[h] = value;
		}
		catch (ArrayIndexOutOfBoundsException var4)
		{
		}
	}

	public boolean dockBusy(int h)
	{
		try
		{
			return this._docksBusy[h];
		}
		catch (ArrayIndexOutOfBoundsException var3)
		{
			return false;
		}
	}

	public void broadcastPacket(VehiclePathPoint point1, VehiclePathPoint point2, ServerPacket packet)
	{
		broadcastPacketsToPlayers(point1, point2, packet);
	}

	public void broadcastPackets(VehiclePathPoint point1, VehiclePathPoint point2, ServerPacket... packets)
	{
		broadcastPacketsToPlayers(point1, point2, packets);
	}

	private static void broadcastPacketsToPlayers(VehiclePathPoint point1, VehiclePathPoint point2, ServerPacket... packets)
	{
		for (Player player : World.getInstance().getPlayers())
		{
			if (Math.hypot(player.getX() - point1.getX(), player.getY() - point1.getY()) < GeneralConfig.BOAT_BROADCAST_RADIUS || Math.hypot(player.getX() - point2.getX(), player.getY() - point2.getY()) < GeneralConfig.BOAT_BROADCAST_RADIUS)
			{
				for (ServerPacket p : packets)
				{
					player.sendPacket(p);
				}
			}
		}
	}

	private static class SingletonHolder
	{
		protected static final BoatManager INSTANCE = new BoatManager();
	}
}
