package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExRpItemLink extends AbstractItemPacket
{
	private final Item _item;

	public ExRpItemLink(Item item)
	{
		this._item = item;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_RP_ITEMLINK.writeId(this, buffer);
		Player player = this._item.asPlayer();
		if (player != null && player.isOnline())
		{
			buffer.writeByte(1);
			buffer.writeInt(player.getObjectId());
		}
		else
		{
			buffer.writeByte(0);
			buffer.writeInt(0);
		}

		this.writeItem(this._item, buffer);
	}
}
