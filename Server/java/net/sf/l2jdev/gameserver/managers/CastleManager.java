package net.sf.l2jdev.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.ClanMember;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.siege.Castle;

public class CastleManager
{
	private static final Logger LOGGER = Logger.getLogger(CastleManager.class.getName());
	private final Map<Integer, Castle> _castles = new ConcurrentSkipListMap<>();
	private final Map<Integer, Long> _castleSiegeDate = new ConcurrentHashMap<>();
	private static final int[] _castleCirclets = new int[]
	{
		0,
		6838,
		6835,
		6839,
		6837,
		6840,
		6834,
		6836,
		8182,
		8183
	};

	public Castle findNearestCastle(WorldObject obj)
	{
		return this.findNearestCastle(obj, Long.MAX_VALUE);
	}

	public Castle findNearestCastle(WorldObject obj, long maxDistanceValue)
	{
		Castle nearestCastle = this.getCastle(obj);
		if (nearestCastle == null)
		{
			long maxDistance = maxDistanceValue;

			for (Castle castle : this._castles.values())
			{
				double distance = castle.getDistance(obj);
				if (maxDistance > distance)
				{
					maxDistance = (long) distance;
					nearestCastle = castle;
				}
			}
		}

		return nearestCastle;
	}

	public Castle getCastleById(int castleId)
	{
		return this._castles.get(castleId);
	}

	public Castle getCastleByOwner(Clan clan)
	{
		if (clan == null)
		{
			return null;
		}
		for (Castle temp : this._castles.values())
		{
			if (temp.getOwnerId() == clan.getId())
			{
				return temp;
			}
		}

		return null;
	}

	public Castle getCastle(String name)
	{
		for (Castle temp : this._castles.values())
		{
			if (temp.getName().equalsIgnoreCase(name.trim()))
			{
				return temp;
			}
		}

		return null;
	}

	public Castle getCastle(int x, int y, int z)
	{
		for (Castle temp : this._castles.values())
		{
			if (temp.checkIfInZone(x, y, z))
			{
				return temp;
			}
		}

		return null;
	}

	public Castle getCastle(WorldObject activeObject)
	{
		return this.getCastle(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}

	public Collection<Castle> getCastles()
	{
		return this._castles.values();
	}

	public boolean hasOwnedCastle()
	{
		boolean hasOwnedCastle = false;

		for (Castle castle : this._castles.values())
		{
			if (castle.getOwnerId() > 0)
			{
				hasOwnedCastle = true;
				break;
			}
		}

		return hasOwnedCastle;
	}

	public int getCircletByCastleId(int castleId)
	{
		return castleId > 0 && castleId < 10 ? _castleCirclets[castleId] : 0;
	}

	public void removeCirclet(Clan clan, int castleId)
	{
		for (ClanMember member : clan.getMembers())
		{
			this.removeCirclet(member, castleId);
		}
	}

	public void removeCirclet(ClanMember member, int castleId)
	{
		if (member != null)
		{
			Player player = member.getPlayer();
			int circletId = this.getCircletByCastleId(castleId);
			if (circletId != 0)
			{
				if (player != null)
				{
					try
					{
						Item circlet = player.getInventory().getItemByItemId(circletId);
						if (circlet != null)
						{
							if (circlet.isEquipped())
							{
								player.getInventory().unEquipItemInSlot(circlet.getLocationSlot());
							}

							player.destroyItemByItemId(ItemProcessType.DESTROY, circletId, 1L, player, true);
						}

						return;
					}
					catch (NullPointerException var14)
					{
					}
				}

				try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM items WHERE owner_id = ? and item_id = ?");)
				{
					ps.setInt(1, member.getObjectId());
					ps.setInt(2, circletId);
					ps.execute();
				}
				catch (Exception var13)
				{
					LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Failed to remove castle circlets offline for player " + member.getName() + ": ", var13);
				}
			}
		}
	}

	public void loadInstances()
	{
		try (Connection con = DatabaseFactory.getConnection(); Statement s = con.createStatement(); ResultSet rs = s.executeQuery("SELECT id FROM castle ORDER BY id");)
		{
			while (rs.next())
			{
				int castleId = rs.getInt("id");
				this._castles.put(castleId, new Castle(castleId));
			}

			LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._castles.values().size() + " castles.");
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception: loadCastleData():", var12);
		}
	}

	public void activateInstances()
	{
		for (Castle castle : this._castles.values())
		{
			castle.activateInstance();
		}
	}

	public void registerSiegeDate(int castleId, long siegeDate)
	{
		this._castleSiegeDate.put(castleId, siegeDate);
	}

	public int getSiegeDates(long siegeDate)
	{
		int count = 0;

		for (long date : this._castleSiegeDate.values())
		{
			if (Math.abs(date - siegeDate) < 1000L)
			{
				count++;
			}
		}

		return count;
	}

	public static CastleManager getInstance()
	{
		return CastleManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final CastleManager INSTANCE = new CastleManager();
	}
}
