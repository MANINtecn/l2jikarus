package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.MailManager;
import net.sf.l2jdev.gameserver.model.Message;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.MailType;

public class ExShowReceivedPostList extends ServerPacket
{
	 
	private final List<Message> _inbox;

	public ExShowReceivedPostList(int objectId)
	{
		this._inbox = MailManager.getInstance().getInbox(objectId);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHOW_RECEIVED_POST_LIST.writeId(this, buffer);
		buffer.writeInt((int) (System.currentTimeMillis() / 1000L));
		if (this._inbox != null && !this._inbox.isEmpty())
		{
			buffer.writeInt(this._inbox.size());

			for (Message msg : this._inbox)
			{
				buffer.writeInt(msg.getMailType().ordinal());
				if (msg.getMailType() == MailType.COMMISSION_ITEM_SOLD)
				{
					buffer.writeInt(SystemMessageId.THE_ITEM_YOU_REGISTERED_HAS_BEEN_SOLD.getId());
				}
				else if (msg.getMailType() == MailType.COMMISSION_ITEM_RETURNED)
				{
					buffer.writeInt(SystemMessageId.THE_REGISTRATION_PERIOD_FOR_THE_ITEM_YOU_REGISTERED_HAS_EXPIRED.getId());
				}

				buffer.writeInt(msg.getId());
				buffer.writeString(msg.getSubject());
				buffer.writeString(msg.getSenderName());
				buffer.writeInt(msg.isLocked());
				buffer.writeInt(msg.getExpirationSeconds());
				buffer.writeInt(msg.isUnread());
				buffer.writeInt(msg.getMailType() != MailType.COMMISSION_ITEM_SOLD && msg.getMailType() != MailType.COMMISSION_ITEM_RETURNED);
				buffer.writeInt(msg.hasAttachments());
				buffer.writeInt(msg.isReturned());
				buffer.writeInt(0);
			}
		}
		else
		{
			buffer.writeInt(0);
		}

		buffer.writeInt(100);
		buffer.writeInt(1000);
	}
}
