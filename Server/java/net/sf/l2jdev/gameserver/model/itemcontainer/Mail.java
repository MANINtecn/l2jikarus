package net.sf.l2jdev.gameserver.model.itemcontainer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemLocation;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;

public class Mail extends ItemContainer
{
	private final int _ownerId;
	private int _messageId;

	public Mail(int objectId, int messageId)
	{
		this._ownerId = objectId;
		this._messageId = messageId;
	}

	@Override
	public String getName()
	{
		return "Mail";
	}

	@Override
	public Player getOwner()
	{
		return null;
	}

	@Override
	public ItemLocation getBaseLocation()
	{
		return ItemLocation.MAIL;
	}

	public int getMessageId()
	{
		return this._messageId;
	}

	public void setNewMessageId(int messageId)
	{
		this._messageId = messageId;

		for (Item item : this._items)
		{
			item.setItemLocation(this.getBaseLocation(), messageId);
		}

		this.updateDatabase();
	}

	public void returnToWh(ItemContainer wh)
	{
		for (Item item : this._items)
		{
			if (wh == null)
			{
				item.setItemLocation(ItemLocation.WAREHOUSE);
			}
			else
			{
				this.transferItem(ItemProcessType.TRANSFER, item.getObjectId(), item.getCount(), wh, null, null);
			}
		}
	}

	@Override
	protected void addItem(Item item)
	{
		super.addItem(item);
		item.setItemLocation(this.getBaseLocation(), this._messageId);
		item.updateDatabase(true);
	}

	@Override
	public void updateDatabase()
	{
		for (Item item : this._items)
		{
			item.updateDatabase(true);
		}
	}

	@Override
	public void restore()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM items WHERE owner_id=? AND loc=? AND loc_data=?");)
		{
			statement.setInt(1, this._ownerId);
			statement.setString(2, this.getBaseLocation().name());
			statement.setInt(3, this._messageId);

			try (ResultSet inv = statement.executeQuery())
			{
				while (inv.next())
				{
					Item item = new Item(inv);
					World.getInstance().addObject(item);
					if (item.isStackable() && this.getItemByItemId(item.getId()) != null)
					{
						this.addItem(ItemProcessType.RESTORE, item, null, null);
					}
					else
					{
						this.addItem(item);
					}
				}
			}
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.WARNING, "could not restore container:", var12);
		}
	}

	@Override
	public void deleteMe()
	{
		for (Item item : this._items)
		{
			item.updateDatabase(true);
			item.stopAllTasks();
			World.getInstance().removeObject(item);
		}

		this._items.clear();
	}

	@Override
	public int getOwnerId()
	{
		return this._ownerId;
	}
}
