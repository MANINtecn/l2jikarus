package net.sf.l2jdev.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.gameserver.data.sql.CharInfoTable;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class ContactList
{
	private static final Logger LOGGER = Logger.getLogger(ContactList.class.getName());
	private final Player _player;
	private final Set<String> _contacts = ConcurrentHashMap.newKeySet();
	public static final String QUERY_ADD = "REPLACE INTO character_contacts (charId, contactId) VALUES (?, ?)";
	public static final String QUERY_REMOVE = "DELETE FROM character_contacts WHERE charId = ? and contactId = ?";
	public static final String QUERY_LOAD = "SELECT contactId FROM character_contacts WHERE charId = ?";

	public ContactList(Player player)
	{
		this._player = player;
		this.restore();
	}

	public void restore()
	{
		this._contacts.clear();

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT contactId FROM character_contacts WHERE charId = ?");)
		{
			statement.setInt(1, this._player.getObjectId());

			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					int contactId = rset.getInt(1);
					String contactName = CharInfoTable.getInstance().getNameById(contactId);
					if (contactName != null && !contactName.equals(this._player.getName()) && contactId != this._player.getObjectId())
					{
						this._contacts.add(contactName);
					}
				}
			}
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.WARNING, "Error found in " + this._player.getName() + "'s ContactsList: " + var12.getMessage(), var12);
		}
	}

	public boolean add(String name)
	{
		int contactId = CharInfoTable.getInstance().getIdByName(name);
		if (this._contacts.contains(name))
		{
			this._player.sendPacket(SystemMessageId.THE_CHARACTER_IS_ALREADY_IN_YOUR_FAVORITE_CONTACTS_LIST);
			return false;
		}
		else if (this._player.getName().equals(name))
		{
			this._player.sendPacket(SystemMessageId.YOU_CANNOT_ADD_YOURSELF);
			return false;
		}
		else if (this._contacts.size() >= 100)
		{
			this._player.sendPacket(SystemMessageId.YOU_CANNOT_ADD_MORE_THAN_100_CHARACTERS_TO_THE_LIST);
			return false;
		}
		else if (contactId < 1)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_NOT_FOUND_PLEASE_TRY_ANOTHER_NAME);
			sm.addString(name);
			this._player.sendPacket(sm);
			return false;
		}
		else
		{
			for (String contactName : this._contacts)
			{
				if (contactName.equalsIgnoreCase(name))
				{
					this._player.sendPacket(SystemMessageId.THE_CHARACTER_IS_ALREADY_IN_YOUR_FAVORITE_CONTACTS_LIST);
					return false;
				}
			}

			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("REPLACE INTO character_contacts (charId, contactId) VALUES (?, ?)");)
			{
				statement.setInt(1, this._player.getObjectId());
				statement.setInt(2, contactId);
				statement.execute();
				this._contacts.add(name);
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_ADDED_TO_YOUR_FAVORITE_CONTACTS_LIST);
				sm.addString(name);
				this._player.sendPacket(sm);
			}
			catch (Exception var12)
			{
				LOGGER.log(Level.WARNING, "Error found in " + this._player.getName() + "'s ContactsList: " + var12.getMessage(), var12);
			}

			return true;
		}
	}

	public void remove(String name)
	{
		int contactId = CharInfoTable.getInstance().getIdByName(name);
		if (!this._contacts.contains(name))
		{
			this._player.sendPacket(SystemMessageId.THE_NAME_IS_NOT_CURRENTLY_REGISTERED);
		}
		else if (contactId >= 1)
		{
			this._contacts.remove(name);

			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("DELETE FROM character_contacts WHERE charId = ? and contactId = ?");)
			{
				statement.setInt(1, this._player.getObjectId());
				statement.setInt(2, contactId);
				statement.execute();
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_REMOVED_FROM_YOUR_FAVORITE_CONTACTS_LIST);
				sm.addString(name);
				this._player.sendPacket(sm);
			}
			catch (Exception var11)
			{
				LOGGER.log(Level.WARNING, "Error found in " + this._player.getName() + "'s ContactsList: " + var11.getMessage(), var11);
			}
		}
	}

	public Set<String> getAllContacts()
	{
		return this._contacts;
	}
}
