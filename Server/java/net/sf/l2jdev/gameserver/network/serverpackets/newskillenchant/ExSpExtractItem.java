package net.sf.l2jdev.gameserver.network.serverpackets.newskillenchant;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.AbstractItemPacket;

public class ExSpExtractItem extends AbstractItemPacket
{
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SP_EXTRACT_ITEM.writeId(this, buffer);
		buffer.writeByte(0);
		buffer.writeByte(0);
		buffer.writeInt(98232);
	}
}
