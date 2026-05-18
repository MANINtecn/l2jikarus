package net.sf.l2jdev.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.sf.l2jdev.gameserver.data.sql.CharInfoTable;
import net.sf.l2jdev.gameserver.managers.IdManager;
import net.sf.l2jdev.gameserver.managers.MailManager;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.AttributeType;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.Mail;
import net.sf.l2jdev.gameserver.network.enums.MailType;

public class Message
{
	public static final int EXPIRATION = 360;
	public static final int COD_EXPIRATION = 12;
	public static final int DELETED = 0;
	public static final int READED = 1;
	public static final int REJECTED = 2;
	private final int _messageId;
	private final int _senderId;
	private final int _receiverId;
	private final long _expiration;
	private String _senderName = null;
	private String _receiverName = null;
	private final String _subject;
	private final String _content;
	private boolean _unread;
	private boolean _returned;
	private MailType _messageType = MailType.REGULAR;
	private boolean _deletedBySender;
	private boolean _deletedByReceiver;
	private final long _reqAdena;
	private boolean _hasAttachments;
	private Mail _attachments = null;
	private int _itemId;
	private int _enchantLvl;
	private final int[] _elementals = new int[6];

	public Message(ResultSet rset) throws SQLException
	{
		this._messageId = rset.getInt("messageId");
		this._senderId = rset.getInt("senderId");
		this._receiverId = rset.getInt("receiverId");
		this._subject = rset.getString("subject");
		this._content = rset.getString("content");
		this._expiration = rset.getLong("expiration");
		this._reqAdena = rset.getLong("reqAdena");
		this._hasAttachments = Boolean.parseBoolean(rset.getString("hasAttachments"));
		this._unread = Boolean.parseBoolean(rset.getString("isUnread"));
		this._deletedBySender = Boolean.parseBoolean(rset.getString("isDeletedBySender"));
		this._deletedByReceiver = Boolean.parseBoolean(rset.getString("isDeletedByReceiver"));
		this._messageType = MailType.values()[rset.getInt("sendBySystem")];
		this._returned = Boolean.parseBoolean(rset.getString("isReturned"));
		this._itemId = rset.getInt("itemId");
		this._enchantLvl = rset.getInt("enchantLvl");
		String elemental = rset.getString("elementals");
		if (elemental != null)
		{
			String[] elemDef = elemental.split(";");

			for (int i = 0; i < 6; i++)
			{
				this._elementals[i] = Integer.parseInt(elemDef[i]);
			}
		}
	}

	public Message(int senderId, int receiverId, boolean isCod, String subject, String text, long reqAdena)
	{
		this._messageId = IdManager.getInstance().getNextId();
		this._senderId = senderId;
		this._receiverId = receiverId;
		this._subject = subject;
		this._content = text;
		this._expiration = isCod ? System.currentTimeMillis() + 43200000L : System.currentTimeMillis() + 1296000000L;
		this._hasAttachments = false;
		this._unread = true;
		this._deletedBySender = false;
		this._deletedByReceiver = false;
		this._reqAdena = reqAdena;
		this._messageType = MailType.REGULAR;
	}

	public Message(int receiverId, String subject, String content, MailType sendBySystem)
	{
		this._messageId = IdManager.getInstance().getNextId();
		this._senderId = -1;
		this._receiverId = receiverId;
		this._subject = subject;
		this._content = content;
		this._expiration = System.currentTimeMillis() + 1296000000L;
		this._reqAdena = 0L;
		this._hasAttachments = false;
		this._unread = true;
		this._deletedBySender = true;
		this._deletedByReceiver = false;
		this._messageType = sendBySystem;
		this._returned = false;
	}

	public Message(int senderId, int receiverId, String subject, String content, MailType sendBySystem)
	{
		this._messageId = IdManager.getInstance().getNextId();
		this._senderId = senderId;
		this._receiverId = receiverId;
		this._subject = subject;
		this._content = content;
		this._expiration = System.currentTimeMillis() + 1296000000L;
		this._hasAttachments = false;
		this._unread = true;
		this._deletedBySender = true;
		this._deletedByReceiver = false;
		this._reqAdena = 0L;
		this._messageType = sendBySystem;
	}

	public Message(Message msg)
	{
		this._messageId = IdManager.getInstance().getNextId();
		this._senderId = msg.getSenderId();
		this._receiverId = msg.getSenderId();
		this._subject = "";
		this._content = "";
		this._expiration = System.currentTimeMillis() + 1296000000L;
		this._unread = true;
		this._deletedBySender = true;
		this._deletedByReceiver = false;
		this._messageType = MailType.REGULAR;
		this._returned = true;
		this._reqAdena = 0L;
		this._hasAttachments = true;
		this._attachments = msg.getAttachments();
		msg.removeAttachments();
		this._attachments.setNewMessageId(this._messageId);
	}

	public Message(int receiverId, Item item, MailType mailType)
	{
		this._messageId = IdManager.getInstance().getNextId();
		this._senderId = -1;
		this._receiverId = receiverId;
		this._subject = "";
		this._content = item.getName();
		this._expiration = System.currentTimeMillis() + 1296000000L;
		this._unread = true;
		this._deletedBySender = true;
		this._messageType = mailType;
		this._returned = false;
		this._reqAdena = 0L;
		if (mailType == MailType.COMMISSION_ITEM_SOLD)
		{
			this._hasAttachments = false;
			this._itemId = item.getId();
			this._enchantLvl = item.getEnchantLevel();
			if (item.isArmor())
			{
				for (AttributeType type : AttributeType.ATTRIBUTE_TYPES)
				{
					this._elementals[type.getClientId()] = item.getDefenceAttribute(type);
				}
			}
			else if (item.isWeapon() && item.getAttackAttributeType() != AttributeType.NONE)
			{
				this._elementals[item.getAttackAttributeType().getClientId()] = item.getAttackAttributePower();
			}
		}
		else if (mailType == MailType.COMMISSION_ITEM_RETURNED)
		{
			Mail attachement = this.createAttachments();
			attachement.addItem(ItemProcessType.REFUND, item, null, null);
		}
	}

	public static PreparedStatement getStatement(Message msg, Connection con) throws SQLException
	{
		PreparedStatement stmt = con.prepareStatement("INSERT INTO messages (messageId, senderId, receiverId, subject, content, expiration, reqAdena, hasAttachments, isUnread, isDeletedBySender, isDeletedByReceiver, sendBySystem, isReturned, itemId, enchantLvl, elementals) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		stmt.setInt(1, msg._messageId);
		stmt.setInt(2, msg._senderId);
		stmt.setInt(3, msg._receiverId);
		stmt.setString(4, msg._subject);
		stmt.setString(5, msg._content);
		stmt.setLong(6, msg._expiration);
		stmt.setLong(7, msg._reqAdena);
		stmt.setString(8, String.valueOf(msg._hasAttachments));
		stmt.setString(9, String.valueOf(msg._unread));
		stmt.setString(10, String.valueOf(msg._deletedBySender));
		stmt.setString(11, String.valueOf(msg._deletedByReceiver));
		stmt.setInt(12, msg._messageType.ordinal());
		stmt.setString(13, String.valueOf(msg._returned));
		stmt.setInt(14, msg._itemId);
		stmt.setInt(15, msg._enchantLvl);
		stmt.setString(16, msg._elementals[0] + ";" + msg._elementals[1] + ";" + msg._elementals[2] + ";" + msg._elementals[3] + ";" + msg._elementals[4] + ";" + msg._elementals[5]);
		return stmt;
	}

	public int getId()
	{
		return this._messageId;
	}

	public int getSenderId()
	{
		return this._senderId;
	}

	public int getReceiverId()
	{
		return this._receiverId;
	}

	public String getSenderName()
	{
		switch (this._messageType)
		{
			case REGULAR:
				this._senderName = CharInfoTable.getInstance().getNameById(this._senderId);
				break;
			default:
				this._senderName = "System";
		}

		return this._senderName;
	}

	public String getReceiverName()
	{
		if (this._receiverName == null)
		{
			this._receiverName = CharInfoTable.getInstance().getNameById(this._receiverId);
			if (this._receiverName == null)
			{
				this._receiverName = "";
			}
		}

		return this._receiverName;
	}

	public String getSubject()
	{
		return this._subject;
	}

	public String getContent()
	{
		return this._content;
	}

	public boolean isLocked()
	{
		return this._reqAdena > 0L;
	}

	public long getExpiration()
	{
		return this._expiration;
	}

	public int getExpirationSeconds()
	{
		return (int) (this._expiration / 1000L);
	}

	public boolean isUnread()
	{
		return this._unread;
	}

	public void markAsRead()
	{
		if (this._unread)
		{
			this._unread = false;
			MailManager.getInstance().markAsReadInDb(this._messageId);
		}
	}

	public boolean isDeletedBySender()
	{
		return this._deletedBySender;
	}

	public void setDeletedBySender()
	{
		if (!this._deletedBySender)
		{
			this._deletedBySender = true;
			if (this._deletedByReceiver)
			{
				MailManager.getInstance().deleteMessageInDb(this._messageId);
			}
			else
			{
				MailManager.getInstance().markAsDeletedBySenderInDb(this._messageId);
			}
		}
	}

	public boolean isDeletedByReceiver()
	{
		return this._deletedByReceiver;
	}

	public void setDeletedByReceiver()
	{
		if (!this._deletedByReceiver)
		{
			this._deletedByReceiver = true;
			if (this._deletedBySender)
			{
				MailManager.getInstance().deleteMessageInDb(this._messageId);
			}
			else
			{
				MailManager.getInstance().markAsDeletedByReceiverInDb(this._messageId);
			}
		}
	}

	public MailType getMailType()
	{
		return this._messageType;
	}

	public boolean isReturned()
	{
		return this._returned;
	}

	public void setReturned(boolean value)
	{
		this._returned = value;
	}

	public long getReqAdena()
	{
		return this._reqAdena;
	}

	public synchronized Mail getAttachments()
	{
		if (!this._hasAttachments)
		{
			return null;
		}
		if (this._attachments == null)
		{
			this._attachments = new Mail(this._senderId, this._messageId);
			this._attachments.restore();
		}

		return this._attachments;
	}

	public boolean hasAttachments()
	{
		return this._hasAttachments;
	}

	public int getItemId()
	{
		return this._itemId;
	}

	public int getEnchantLvl()
	{
		return this._enchantLvl;
	}

	public int[] getElementals()
	{
		return this._elementals;
	}

	public synchronized void removeAttachments()
	{
		if (this._attachments != null)
		{
			this._attachments = null;
			this._hasAttachments = false;
			MailManager.getInstance().removeAttachmentsInDb(this._messageId);
		}
	}

	public synchronized Mail createAttachments()
	{
		if (!this._hasAttachments && this._attachments == null)
		{
			this._attachments = new Mail(this._senderId, this._messageId);
			this._hasAttachments = true;
			return this._attachments;
		}
		return null;
	}

	protected final synchronized void unloadAttachments()
	{
		if (this._attachments != null)
		{
			this._attachments.deleteMe();
			MailManager.getInstance().removeAttachmentsInDb(this._messageId);
			this._attachments = null;
		}
	}
}
