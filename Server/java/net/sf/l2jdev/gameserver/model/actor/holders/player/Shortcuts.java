package net.sf.l2jdev.gameserver.model.actor.holders.player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.ShortcutType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.serverpackets.ShortcutRegister;

public class Shortcuts
{
	private static final Logger LOGGER = Logger.getLogger(Shortcuts.class.getName());
	public static final int MAX_SHORTCUTS_PER_BAR = 12;
	private final Player _owner;
	private final Map<Integer, Shortcut> _shortcuts = new ConcurrentHashMap<>();

	public Shortcuts(Player owner)
	{
		this._owner = owner;
	}

	public Collection<Shortcut> getAllShortcuts()
	{
		return this._shortcuts.values();
	}

	public Shortcut getShortcut(int slot, int page)
	{
		Shortcut sc = this._shortcuts.get(slot + page * 12);
		if (sc != null && sc.getType() == ShortcutType.ITEM && this._owner.getInventory().getItemByObjectId(sc.getId()) == null)
		{
			this.deleteShortcut(sc.getSlot(), sc.getPage());
			sc = null;
		}

		return sc;
	}

	public void registerShortcut(Shortcut shortcut)
	{
		if (shortcut.getType() == ShortcutType.ITEM)
		{
			Item item = this._owner.getInventory().getItemByObjectId(shortcut.getId());
			if (item == null)
			{
				return;
			}

			shortcut.setSharedReuseGroup(item.getSharedReuseGroup());
		}

		this.registerShortcutInDb(shortcut, this._shortcuts.put(shortcut.getSlot() + shortcut.getPage() * 12, shortcut));
	}

	private void registerShortcutInDb(Shortcut shortcut, Shortcut oldShortcut)
	{
		if (oldShortcut != null)
		{
			this.deleteShortcutFromDb(oldShortcut);
		}

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("REPLACE INTO character_shortcuts (charId,slot,page,type,shortcut_id,level,sub_level,class_index) values(?,?,?,?,?,?,?,?)");)
		{
			statement.setInt(1, this._owner.getObjectId());
			statement.setInt(2, shortcut.getSlot());
			statement.setInt(3, shortcut.getPage());
			statement.setInt(4, shortcut.getType().ordinal());
			statement.setInt(5, shortcut.getId());
			statement.setInt(6, shortcut.getLevel());
			statement.setInt(7, shortcut.getSubLevel());
			statement.setInt(8, this._owner.getClassIndex());
			statement.execute();
		}
		catch (Exception var11)
		{
			LOGGER.log(Level.WARNING, "Could not store character shortcut: " + var11.getMessage(), var11);
		}
	}

	public void deleteShortcut(int slot, int page)
	{
		Shortcut old = this._shortcuts.remove(slot + page * 12);
		if (old != null && this._owner != null)
		{
			this.deleteShortcutFromDb(old);
		}
	}

	public void deleteShortcutByObjectId(int objectId)
	{
		for (Shortcut shortcut : this._shortcuts.values())
		{
			if (shortcut.getType() == ShortcutType.ITEM && shortcut.getId() == objectId)
			{
				this.deleteShortcut(shortcut.getSlot(), shortcut.getPage());
				break;
			}
		}
	}

	private void deleteShortcutFromDb(Shortcut shortcut)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE charId=? AND slot=? AND page=? AND class_index=?");)
		{
			statement.setInt(1, this._owner.getObjectId());
			statement.setInt(2, shortcut.getSlot());
			statement.setInt(3, shortcut.getPage());
			statement.setInt(4, this._owner.getClassIndex());
			statement.execute();
		}
		catch (Exception var10)
		{
			LOGGER.log(Level.WARNING, "Could not delete character shortcut: " + var10.getMessage(), var10);
		}
	}

	public boolean restoreMe()
	{
		this._shortcuts.clear();

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT charId, slot, page, type, shortcut_id, level, sub_level FROM character_shortcuts WHERE charId=? AND class_index=?");)
		{
			statement.setInt(1, this._owner.getObjectId());
			statement.setInt(2, this._owner.getClassIndex());

			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					int slot = rset.getInt("slot");
					int page = rset.getInt("page");
					int type = rset.getInt("type");
					int id = rset.getInt("shortcut_id");
					int level = rset.getInt("level");
					int subLevel = rset.getInt("sub_level");
					this._shortcuts.put(slot + page * 12, new Shortcut(slot, page, ShortcutType.values()[type], id, level, subLevel, 1));
				}
			}
		}
		catch (Exception var16)
		{
			LOGGER.log(Level.WARNING, "Could not restore character shortcuts: " + var16.getMessage(), var16);
			return false;
		}

		for (Shortcut sc : this.getAllShortcuts())
		{
			if (sc.getType() == ShortcutType.ITEM)
			{
				Item item = this._owner.getInventory().getItemByObjectId(sc.getId());
				if (item == null)
				{
					this.deleteShortcut(sc.getSlot(), sc.getPage());
				}
				else if (item.isEtcItem())
				{
					sc.setSharedReuseGroup(item.getEtcItem().getSharedReuseGroup());
				}
			}
		}

		return true;
	}

	public void updateShortcuts(int skillId, int skillLevel, int skillSubLevel)
	{
		for (Shortcut sc : this._shortcuts.values())
		{
			if (sc.getId() == skillId && sc.getType() == ShortcutType.SKILL)
			{
				Shortcut newsc = new Shortcut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), skillLevel, skillSubLevel, 1);
				newsc.setAutoUse(sc.isAutoUse());
				this._owner.sendPacket(new ShortcutRegister(newsc, this._owner));
				this._owner.registerShortcut(newsc);
			}
		}
	}
}
