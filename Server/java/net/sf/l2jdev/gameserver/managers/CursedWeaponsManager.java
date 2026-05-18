package net.sf.l2jdev.gameserver.managers;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.model.CursedWeapon;
import net.sf.l2jdev.gameserver.model.actor.Attackable;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.instance.Defender;
import net.sf.l2jdev.gameserver.model.actor.instance.FeedableBeast;
import net.sf.l2jdev.gameserver.model.actor.instance.FortCommander;
import net.sf.l2jdev.gameserver.model.actor.instance.GrandBoss;
import net.sf.l2jdev.gameserver.model.actor.instance.Guard;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.util.Broadcast;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class CursedWeaponsManager implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(CursedWeaponsManager.class.getName());
	private final Map<Integer, CursedWeapon> _cursedWeapons = new HashMap<>();

	protected CursedWeaponsManager()
	{
		this.load();
	}

	@Override
	public void load()
	{
		if (GeneralConfig.ALLOW_CURSED_WEAPONS)
		{
			this.parseDatapackFile("data/CursedWeapons.xml");
			this.restore();
			this.controlPlayers();
			LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._cursedWeapons.size() + " cursed weapons.");
		}
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		for (Node n = document.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("item".equalsIgnoreCase(d.getNodeName()))
					{
						NamedNodeMap attrs = d.getAttributes();
						int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
						int skillId = Integer.parseInt(attrs.getNamedItem("skillId").getNodeValue());
						String name = attrs.getNamedItem("name").getNodeValue();
						CursedWeapon cw = new CursedWeapon(id, skillId, name);

						for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
						{
							if ("dropRate".equalsIgnoreCase(cd.getNodeName()))
							{
								attrs = cd.getAttributes();
								int val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
								cw.setDropRate(val);
							}
							else if ("duration".equalsIgnoreCase(cd.getNodeName()))
							{
								attrs = cd.getAttributes();
								int val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
								cw.setDuration(val);
							}
							else if ("durationLost".equalsIgnoreCase(cd.getNodeName()))
							{
								attrs = cd.getAttributes();
								int val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
								cw.setDurationLost(val);
							}
							else if ("disapearChance".equalsIgnoreCase(cd.getNodeName()))
							{
								attrs = cd.getAttributes();
								int val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
								cw.setDisapearChance(val);
							}
							else if ("stageKills".equalsIgnoreCase(cd.getNodeName()))
							{
								attrs = cd.getAttributes();
								int val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
								cw.setStageKills(val);
							}
						}

						this._cursedWeapons.put(id, cw);
					}
				}
			}
		}
	}

	private void restore()
	{
		try (Connection con = DatabaseFactory.getConnection(); Statement s = con.createStatement(); ResultSet rs = s.executeQuery("SELECT itemId, charId, playerReputation, playerPkKills, nbKills, endTime FROM cursed_weapons");)
		{
			while (rs.next())
			{
				CursedWeapon cw = this._cursedWeapons.get(rs.getInt("itemId"));
				cw.setPlayerId(rs.getInt("charId"));
				cw.setPlayerReputation(rs.getInt("playerReputation"));
				cw.setPlayerPkKills(rs.getInt("playerPkKills"));
				cw.setNbKills(rs.getInt("nbKills"));
				cw.setEndTime(rs.getLong("endTime"));
				cw.reActivate();
			}
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Could not restore CursedWeapons data: ", var12);
		}
	}

	private void controlPlayers()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT owner_id FROM items WHERE item_id=?");)
		{
			for (CursedWeapon cw : this._cursedWeapons.values())
			{
				if (!cw.isActivated())
				{
					int itemId = cw.getItemId();
					ps.setInt(1, itemId);

					try (ResultSet rset = ps.executeQuery())
					{
						if (rset.next())
						{
							int playerId = rset.getInt("owner_id");
							LOGGER.info("PROBLEM : Player " + playerId + " owns the cursed weapon " + itemId + " but he shouldn't.");

							try (PreparedStatement delete = con.prepareStatement("DELETE FROM items WHERE owner_id=? AND item_id=?"))
							{
								delete.setInt(1, playerId);
								delete.setInt(2, itemId);
								if (delete.executeUpdate() != 1)
								{
									LOGGER.warning("Error while deleting cursed weapon " + itemId + " from userId " + playerId);
								}
							}

							try (PreparedStatement update = con.prepareStatement("UPDATE characters SET reputation=?, pkkills=? WHERE charId=?"))
							{
								update.setInt(1, cw.getPlayerReputation());
								update.setInt(2, cw.getPlayerPkKills());
								update.setInt(3, playerId);
								if (update.executeUpdate() != 1)
								{
									LOGGER.warning("Error while updating karma & pkkills for userId " + cw.getPlayerId());
								}
							}

							removeFromDb(itemId);
						}
					}
				}
			}
		}
		catch (Exception var21)
		{
			LOGGER.log(Level.WARNING, "Could not check CursedWeapons data: ", var21);
		}
	}

	public synchronized void checkDrop(Attackable attackable, Player player)
	{
		if (!(attackable instanceof Defender) && !(attackable instanceof Guard) && !(attackable instanceof GrandBoss) && !(attackable instanceof FeedableBeast) && !(attackable instanceof FortCommander))
		{
			for (CursedWeapon cw : this._cursedWeapons.values())
			{
				if (!cw.isActive() && cw.checkDrop(attackable, player))
				{
					break;
				}
			}
		}
	}

	public void activate(Player player, Item item)
	{
		CursedWeapon cw = this._cursedWeapons.get(item.getId());
		if (player.isCursedWeaponEquipped())
		{
			CursedWeapon cw2 = this._cursedWeapons.get(player.getCursedWeaponEquippedId());
			cw2.setNbKills(cw2.getStageKills() - 1);
			cw2.increaseKills();
			cw.setPlayer(player);
			cw.endOfLife();
		}
		else
		{
			cw.activate(player, item);
		}
	}

	public void drop(int itemId, Creature killer)
	{
		CursedWeapon cw = this._cursedWeapons.get(itemId);
		cw.dropIt(killer);
	}

	public void increaseKills(int itemId)
	{
		CursedWeapon cw = this._cursedWeapons.get(itemId);
		cw.increaseKills();
	}

	public int getLevel(int itemId)
	{
		CursedWeapon cw = this._cursedWeapons.get(itemId);
		return cw.getLevel();
	}

	public static void announce(SystemMessage sm)
	{
		Broadcast.toAllOnlinePlayers(sm);
	}

	public void checkPlayer(Player player)
	{
		if (player != null)
		{
			for (CursedWeapon cw : this._cursedWeapons.values())
			{
				if (cw.isActivated() && player.getObjectId() == cw.getPlayerId())
				{
					cw.setPlayer(player);
					cw.setItem(player.getInventory().getItemByItemId(cw.getItemId()));
					cw.giveSkill();
					player.setCursedWeaponEquippedId(cw.getItemId());
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_S2_MIN_OF_USAGE_TIME_REMAINING);
					sm.addString(cw.getName());
					sm.addInt((int) ((cw.getEndTime() - System.currentTimeMillis()) / 60000L));
					player.sendPacket(sm);
				}
			}
		}
	}

	public int checkOwnsWeaponId(int ownerId)
	{
		for (CursedWeapon cw : this._cursedWeapons.values())
		{
			if (cw.isActivated() && ownerId == cw.getPlayerId())
			{
				return cw.getItemId();
			}
		}

		return -1;
	}

	public static void removeFromDb(int itemId)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM cursed_weapons WHERE itemId = ?");)
		{
			ps.setInt(1, itemId);
			ps.executeUpdate();
		}
		catch (SQLException var9)
		{
			LOGGER.log(Level.SEVERE, "Failed to remove data: " + var9.getMessage(), var9);
		}
	}

	public void saveData()
	{
		for (CursedWeapon cw : this._cursedWeapons.values())
		{
			cw.saveData();
		}
	}

	public boolean isCursed(int itemId)
	{
		return this._cursedWeapons.containsKey(itemId);
	}

	public Collection<CursedWeapon> getCursedWeapons()
	{
		return this._cursedWeapons.values();
	}

	public Set<Integer> getCursedWeaponsIds()
	{
		return this._cursedWeapons.keySet();
	}

	public CursedWeapon getCursedWeapon(int itemId)
	{
		return this._cursedWeapons.get(itemId);
	}

	public void givePassive(int itemId)
	{
		try
		{
			this._cursedWeapons.get(itemId).giveSkill();
		}
		catch (Exception var3)
		{
		}
	}

	public static CursedWeaponsManager getInstance()
	{
		return CursedWeaponsManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final CursedWeaponsManager INSTANCE = new CursedWeaponsManager();
	}
}
