package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Collection;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.Message;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.ItemContainer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExReplySentPost extends AbstractItemPacket
{
	private final Message _msg;
	private Collection<Item> _items = null;

	public ExReplySentPost(Message msg)
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
		ServerPackets.EX_REPLY_SENT_POST.writeId(this, buffer);
		buffer.writeInt(0);
		buffer.writeInt(this._msg.getId());
		buffer.writeInt(this._msg.isLocked());
		buffer.writeString(this._msg.getReceiverName());
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
