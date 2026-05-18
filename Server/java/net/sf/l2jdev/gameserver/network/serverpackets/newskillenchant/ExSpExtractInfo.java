package net.sf.l2jdev.gameserver.network.serverpackets.newskillenchant;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.ItemInfo;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.AbstractItemPacket;

public class ExSpExtractInfo extends AbstractItemPacket
{
	private final Player _player;

	public ExSpExtractInfo(Player player)
	{
		this._player = player;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SP_EXTRACT_INFO.writeId(this, buffer);
		buffer.writeInt(98232);
		buffer.writeInt(1);
		buffer.writeLong(50000000L);
		buffer.writeInt(100);
		buffer.writeInt(0);
		buffer.writeShort(this.calculatePacketSize(new ItemInfo(new Item(15624))));
		buffer.writeInt(15624);
		buffer.writeLong(50000000L);
		buffer.writeShort(this.calculatePacketSize(new ItemInfo(new Item(57))));
		buffer.writeInt(57);
		buffer.writeLong(3000000L);
		buffer.writeShort(this.calculatePacketSize(new ItemInfo(new Item(57))));
		buffer.writeInt(57);
		buffer.writeLong(1L);
		buffer.writeInt(this._player.getVariables().getInt("DAILY_EXTRACT_ITEM98232", 5));
		buffer.writeInt(5);
	}
}
