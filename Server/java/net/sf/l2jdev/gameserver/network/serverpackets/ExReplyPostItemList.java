package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Collection;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExReplyPostItemList extends AbstractItemPacket
{
	private final int _sendType;
	private final Player _player;
	private final Collection<Item> _itemList;

	public ExReplyPostItemList(int sendType, Player player)
	{
		this._sendType = sendType;
		this._player = player;
		this._itemList = this._player.getInventory().getAvailableItems(true, false, false);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_REPLY_POST_ITEM_LIST.writeId(this, buffer);
		buffer.writeByte(this._sendType);
		buffer.writeInt(this._itemList.size());
		if (this._sendType == 2)
		{
			buffer.writeInt(this._itemList.size());

			for (Item item : this._itemList)
			{
				this.writeItem(item, buffer);
			}
		}
	}
}
