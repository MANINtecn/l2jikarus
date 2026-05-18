package net.sf.l2jdev.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.gameserver.model.Message;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.enums.MailType;
import net.sf.l2jdev.gameserver.network.serverpackets.ExNoticePostArrived;
import net.sf.l2jdev.gameserver.network.serverpackets.ExUnReadMailCount;
import net.sf.l2jdev.gameserver.taskmanagers.MessageDeletionTaskManager;

public class MailManager
{
	private static final Logger LOGGER = Logger.getLogger(MailManager.class.getName());
	private final Map<Integer, Message> _messages = new ConcurrentHashMap<>();

	protected MailManager()
	{
		this.load();
	}

	private void load()
	{
		int count = 0;

		try (Connection con = DatabaseFactory.getConnection(); Statement ps = con.createStatement(); ResultSet rs = ps.executeQuery("SELECT * FROM messages ORDER BY expiration");)
		{
			while (rs.next())
			{
				count++;
				Message msg = new Message(rs);
				int msgId = msg.getId();
				this._messages.put(msgId, msg);
				MessageDeletionTaskManager.getInstance().add(msgId, msg.getExpiration());
			}
		}
		catch (SQLException var13)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Error loading from database:", var13);
		}

		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + count + " messages.");
	}

	public Message getMessage(int msgId)
	{
		return this._messages.get(msgId);
	}

	public Collection<Message> getMessages()
	{
		return this._messages.values();
	}

	public boolean hasUnreadPost(Player player)
	{
		int objectId = player.getObjectId();

		for (Message msg : this._messages.values())
		{
			if (msg != null && msg.getReceiverId() == objectId && msg.isUnread())
			{
				return true;
			}
		}

		return false;
	}

	public int getInboxSize(int objectId)
	{
		int size = 0;

		for (Message msg : this._messages.values())
		{
			if (msg != null && msg.getReceiverId() == objectId && !msg.isDeletedByReceiver())
			{
				size++;
			}
		}

		return size;
	}

	public int getOutboxSize(int objectId)
	{
		int size = 0;

		for (Message msg : this._messages.values())
		{
			if (msg != null && msg.getSenderId() == objectId && !msg.isDeletedBySender())
			{
				size++;
			}
		}

		return size;
	}

	public List<Message> getInbox(int objectId)
	{
		List<Message> inbox = new LinkedList<>();

		for (Message msg : this._messages.values())
		{
			if (msg != null && msg.getReceiverId() == objectId && !msg.isDeletedByReceiver())
			{
				inbox.add(msg);
			}
		}

		return inbox;
	}

	public long getUnreadCount(Player player)
	{
		long count = 0L;

		for (Message message : this.getInbox(player.getObjectId()))
		{
			if (message.isUnread())
			{
				count++;
			}
		}

		return count;
	}

	public int getMailsInProgress(int objectId)
	{
		int count = 0;

		for (Message msg : this._messages.values())
		{
			if (msg != null && msg.getMailType() == MailType.REGULAR)
			{
				if (msg.getReceiverId() == objectId && !msg.isDeletedByReceiver() && !msg.isReturned() && msg.hasAttachments())
				{
					count++;
				}
				else if (msg.getSenderId() == objectId && !msg.isDeletedBySender() && !msg.isReturned() && msg.hasAttachments())
				{
					count++;
				}
			}
		}

		return count;
	}

	public List<Message> getOutbox(int objectId)
	{
		List<Message> outbox = new LinkedList<>();

		for (Message msg : this._messages.values())
		{
			if (msg != null && msg.getSenderId() == objectId && !msg.isDeletedBySender())
			{
				outbox.add(msg);
			}
		}

		return outbox;
	}

	public void sendMessage(Message msg)
	{
		this._messages.put(msg.getId(), msg);

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = Message.getStatement(msg, con);)
		{
			ps.execute();
		}
		catch (SQLException var10)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Error saving message:", var10);
		}

		Player receiver = World.getInstance().getPlayer(msg.getReceiverId());
		if (receiver != null)
		{
			receiver.sendPacket(ExNoticePostArrived.valueOf(true));
			receiver.sendPacket(new ExUnReadMailCount(receiver));
		}

		MessageDeletionTaskManager.getInstance().add(msg.getId(), msg.getExpiration());
	}

	public void markAsReadInDb(int msgId)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE messages SET isUnread = 'false' WHERE messageId = ?");)
		{
			ps.setInt(1, msgId);
			ps.execute();
		}
		catch (SQLException var10)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Error marking as read message:", var10);
		}
	}

	public void markAsDeletedBySenderInDb(int msgId)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE messages SET isDeletedBySender = 'true' WHERE messageId = ?");)
		{
			ps.setInt(1, msgId);
			ps.execute();
		}
		catch (SQLException var10)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Error marking as deleted by sender message:", var10);
		}
	}

	public void markAsDeletedByReceiverInDb(int msgId)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE messages SET isDeletedByReceiver = 'true' WHERE messageId = ?");)
		{
			ps.setInt(1, msgId);
			ps.execute();
		}
		catch (SQLException var10)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Error marking as deleted by receiver message:", var10);
		}
	}

	public void removeAttachmentsInDb(int msgId)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE messages SET hasAttachments = 'false' WHERE messageId = ?");)
		{
			ps.setInt(1, msgId);
			ps.execute();
		}
		catch (SQLException var10)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Error removing attachments in message:", var10);
		}
	}

	public void deleteMessageInDb(int msgId)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM messages WHERE messageId = ?");)
		{
			ps.setInt(1, msgId);
			ps.execute();
		}
		catch (SQLException var10)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Error deleting message:", var10);
		}

		this._messages.remove(msgId);
		IdManager.getInstance().releaseId(msgId);
	}

	public static MailManager getInstance()
	{
		return MailManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final MailManager INSTANCE = new MailManager();
	}
}
