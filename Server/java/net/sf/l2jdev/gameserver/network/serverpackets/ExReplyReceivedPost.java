package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Collection;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.Message;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.ItemContainer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.MailType;

public class ExReplyReceivedPost extends AbstractItemPacket
{
	private final Message _msg;
	private Collection<Item> _items = null;

	public ExReplyReceivedPost(Message msg)
	{
		this._msg = msg;
		if (msg.hasAttachments())
		{
			ItemContainer attachments = msg.getAttachments();
			if (attachments != null && attachments.getSize() > 0)
			{
				this._items = attachments.getItems();
			}
			else
			{
				PacketLogger.warning("Message " + msg.getId() + " has attachments but itemcontainer is empty.");
			}
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_REPLY_RECEIVED_POST.writeId(this, buffer);
		buffer.writeInt(this._msg.getMailType().ordinal());
		if (this._msg.getMailType() == MailType.COMMISSION_ITEM_RETURNED)
		{
			buffer.writeInt(SystemMessageId.THE_REGISTRATION_PERIOD_FOR_THE_ITEM_YOU_REGISTERED_HAS_EXPIRED.getId());
			buffer.writeInt(SystemMessageId.THE_AUCTION_HOUSE_REGISTRATION_PERIOD_HAS_EXPIRED_AND_THE_CORRESPONDING_ITEM_IS_BEING_FORWARDED.getId());
		}
		else if (this._msg.getMailType() == MailType.COMMISSION_ITEM_SOLD)
		{
			buffer.writeInt(this._msg.getItemId());
			buffer.writeInt(this._msg.getEnchantLvl());

			for (int i = 0; i < 6; i++)
			{
				buffer.writeInt(this._msg.getElementals()[i]);
			}

			buffer.writeInt(SystemMessageId.THE_ITEM_YOU_REGISTERED_HAS_BEEN_SOLD.getId());
			buffer.writeInt(SystemMessageId.S1_SOLD.getId());
		}

		buffer.writeInt(this._msg.getId());
		buffer.writeInt(this._msg.isLocked());
		buffer.writeInt(0);
		buffer.writeString(this._msg.getSenderName());
		buffer.writeString(this._msg.getSubject());
		buffer.writeString(this._msg.getContent());
		if (this._items != null && !this._items.isEmpty())
		{
			buffer.writeInt(this._items.size());

			for (Item item : this._items)
			{
				this.writeItem(item, buffer);
				buffer.writeInt(item.getObjectId());
			}
		}
		else
		{
			buffer.writeInt(0);
		}

		buffer.writeLong(this._msg.getReqAdena());
		buffer.writeInt(this._msg.hasAttachments());
		buffer.writeInt(this._msg.isReturned());
	}
}
