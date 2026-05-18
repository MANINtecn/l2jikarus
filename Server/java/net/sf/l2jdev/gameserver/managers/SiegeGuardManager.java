package net.sf.l2jdev.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.gameserver.data.xml.CastleData;
import net.sf.l2jdev.gameserver.data.xml.NpcData;
import net.sf.l2jdev.gameserver.model.Spawn;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.instance.Defender;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.interfaces.IPositionable;
import net.sf.l2jdev.gameserver.model.item.enums.ItemLocation;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.model.siege.SiegeGuardHolder;

public class SiegeGuardManager
{
	private static final Logger LOGGER = Logger.getLogger(SiegeGuardManager.class.getName());
	private static final Set<Item> _droppedTickets = ConcurrentHashMap.newKeySet();
	private static final Map<Integer, Set<Spawn>> _siegeGuardSpawn = new ConcurrentHashMap<>();

	protected SiegeGuardManager()
	{
		_droppedTickets.clear();
		this.load();
	}

	private void load()
	{
		try (Connection con = DatabaseFactory.getConnection(); ResultSet rs = con.createStatement().executeQuery("SELECT * FROM castle_siege_guards Where isHired = 1");)
		{
			while (rs.next())
			{
				int npcId = rs.getInt("npcId");
				int x = rs.getInt("x");
				int y = rs.getInt("y");
				int z = rs.getInt("z");
				Castle castle = CastleManager.getInstance().getCastle(x, y, z);
				if (castle == null)
				{
					LOGGER.warning("Siege guard ticket cannot be placed! Castle is null at X: " + x + ", Y: " + y + ", Z: " + z);
				}
				else
				{
					SiegeGuardHolder holder = this.getSiegeGuardByNpc(castle.getResidenceId(), npcId);
					if (holder != null && !castle.getSiege().isInProgress())
					{
						Item dropticket = new Item(holder.getItemId());
						dropticket.setItemLocation(ItemLocation.VOID);
						dropticket.dropMe(null, x, y, z);
						World.getInstance().addObject(dropticket);
						_droppedTickets.add(dropticket);
					}
				}
			}

			LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + _droppedTickets.size() + " siege guards tickets.");
		}
		catch (Exception var14)
		{
			LOGGER.log(Level.WARNING, var14.getMessage(), var14);
		}
	}

	public SiegeGuardHolder getSiegeGuardByItem(int castleId, int itemId)
	{
		for (SiegeGuardHolder holder : CastleData.getInstance().getSiegeGuardsForCastle(castleId))
		{
			if (holder.getItemId() == itemId)
			{
				return holder;
			}
		}

		return null;
	}

	public SiegeGuardHolder getSiegeGuardByNpc(int castleId, int npcId)
	{
		for (SiegeGuardHolder holder : CastleData.getInstance().getSiegeGuardsForCastle(castleId))
		{
			if (holder.getNpcId() == npcId)
			{
				return holder;
			}
		}

		return null;
	}

	public boolean isTooCloseToAnotherTicket(Player player)
	{
		for (Item ticket : _droppedTickets)
		{
			if (ticket.calculateDistance3D(player) < 25.0)
			{
				return true;
			}
		}

		return false;
	}

	public boolean isAtNpcLimit(int castleId, int itemId)
	{
		SiegeGuardHolder holder = this.getSiegeGuardByItem(castleId, itemId);
		long count = 0L;

		for (Item ticket : _droppedTickets)
		{
			if (ticket.getId() == itemId)
			{
				count++;
			}
		}

		return count >= holder.getMaxNpcAmout();
	}

	public void addTicket(int itemId, Player player)
	{
		Castle castle = CastleManager.getInstance().getCastle(player);
		if (castle != null)
		{
			if (!this.isAtNpcLimit(castle.getResidenceId(), itemId))
			{
				SiegeGuardHolder holder = this.getSiegeGuardByItem(castle.getResidenceId(), itemId);
				if (holder != null)
				{
					try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("Insert Into castle_siege_guards (castleId, npcId, x, y, z, heading, respawnDelay, isHired) Values (?, ?, ?, ?, ?, ?, ?, ?)");)
					{
						statement.setInt(1, castle.getResidenceId());
						statement.setInt(2, holder.getNpcId());
						statement.setInt(3, player.getX());
						statement.setInt(4, player.getY());
						statement.setInt(5, player.getZ());
						statement.setInt(6, player.getHeading());
						statement.setInt(7, 0);
						statement.setInt(8, 1);
						statement.execute();
					}
					catch (Exception var13)
					{
						LOGGER.log(Level.WARNING, "Error adding siege guard for castle " + castle.getName() + ": " + var13.getMessage(), var13);
					}

					this.spawnMercenary(player, holder);
					Item dropticket = new Item(itemId);
					dropticket.setItemLocation(ItemLocation.VOID);
					dropticket.dropMe(null, player.getX(), player.getY(), player.getZ());
					World.getInstance().addObject(dropticket);
					_droppedTickets.add(dropticket);
				}
			}
		}
	}

	public void spawnMercenary(IPositionable pos, SiegeGuardHolder holder)
	{
		NpcTemplate template = NpcData.getInstance().getTemplate(holder.getNpcId());
		if (template != null)
		{
			Defender npc = new Defender(template);
			npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
			npc.setDecayed(false);
			npc.setHeading(pos.getHeading());
			npc.spawnMe(pos.getX(), pos.getY(), pos.getZ() + 20);
			npc.scheduleDespawn(3000L);
			npc.setImmobilized(holder.isStationary());
		}
	}

	public void deleteTickets(int castleId)
	{
		for (Item ticket : _droppedTickets)
		{
			if (ticket != null && this.getSiegeGuardByItem(castleId, ticket.getId()) != null)
			{
				ticket.decayMe();
				_droppedTickets.remove(ticket);
			}
		}
	}

	public void removeTicket(Item item)
	{
		Castle castle = CastleManager.getInstance().getCastle(item);
		if (castle != null)
		{
			SiegeGuardHolder holder = this.getSiegeGuardByItem(castle.getResidenceId(), item.getId());
			if (holder != null)
			{
				this.removeSiegeGuard(holder.getNpcId(), item);
				_droppedTickets.remove(item);
			}
		}
	}

	private void loadSiegeGuard(Castle castle)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM castle_siege_guards Where castleId = ? And isHired = ?");)
		{
			ps.setInt(1, castle.getResidenceId());
			ps.setInt(2, castle.getOwnerId() > 0 ? 1 : 0);

			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					Spawn spawn = new Spawn(rs.getInt("npcId"));
					spawn.setAmount(1);
					spawn.setXYZ(rs.getInt("x"), rs.getInt("y"), rs.getInt("z"));
					spawn.setHeading(rs.getInt("heading"));
					spawn.setRespawnDelay(rs.getInt("respawnDelay"));
					spawn.setLocationId(0);
					this.getSpawnedGuards(castle.getResidenceId()).add(spawn);
				}
			}
		}
		catch (Exception var13)
		{
			LOGGER.log(Level.WARNING, "Error loading siege guard for castle " + castle.getName() + ": " + var13.getMessage(), var13);
		}
	}

	public void removeSiegeGuard(int npcId, IPositionable pos)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("Delete From castle_siege_guards Where npcId = ? And x = ? AND y = ? AND z = ? AND isHired = 1");)
		{
			ps.setInt(1, npcId);
			ps.setInt(2, pos.getX());
			ps.setInt(3, pos.getY());
			ps.setInt(4, pos.getZ());
			ps.execute();
		}
		catch (Exception var11)
		{
			LOGGER.log(Level.WARNING, "Error deleting hired siege guard at " + pos + " : " + var11.getMessage(), var11);
		}
	}

	public void removeSiegeGuards(Castle castle)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("Delete From castle_siege_guards Where castleId = ? And isHired = 1");)
		{
			ps.setInt(1, castle.getResidenceId());
			ps.execute();
		}
		catch (Exception var10)
		{
			LOGGER.log(Level.WARNING, "Error deleting hired siege guard for castle " + castle.getName() + ": " + var10.getMessage(), var10);
		}
	}

	public void spawnSiegeGuard(Castle castle)
	{
		try
		{
			boolean isHired = castle.getOwnerId() > 0;
			this.loadSiegeGuard(castle);

			for (Spawn spawn : this.getSpawnedGuards(castle.getResidenceId()))
			{
				if (spawn != null)
				{
					spawn.init();
					if (isHired || spawn.getRespawnDelay() == 0)
					{
						spawn.stopRespawn();
					}

					SiegeGuardHolder holder = this.getSiegeGuardByNpc(castle.getResidenceId(), spawn.getLastSpawn().getId());
					if (holder != null)
					{
						spawn.getLastSpawn().setImmobilized(holder.isStationary());
					}
				}
			}
		}
		catch (Exception var6)
		{
			LOGGER.log(Level.SEVERE, "Error spawning siege guards for castle " + castle.getName(), var6);
		}
	}

	public void unspawnSiegeGuard(Castle castle)
	{
		for (Spawn spawn : this.getSpawnedGuards(castle.getResidenceId()))
		{
			if (spawn != null && spawn.getLastSpawn() != null)
			{
				spawn.stopRespawn();
				spawn.getLastSpawn().deleteMe();
			}
		}

		this.getSpawnedGuards(castle.getResidenceId()).clear();
	}

	public Set<Spawn> getSpawnedGuards(int castleId)
	{
		return _siegeGuardSpawn.computeIfAbsent(castleId, _ -> ConcurrentHashMap.newKeySet());
	}

	public static SiegeGuardManager getInstance()
	{
		return SiegeGuardManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final SiegeGuardManager INSTANCE = new SiegeGuardManager();
	}
}
