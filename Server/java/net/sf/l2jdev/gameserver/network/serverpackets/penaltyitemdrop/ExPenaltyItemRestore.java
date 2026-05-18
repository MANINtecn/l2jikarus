package net.sf.l2jdev.gameserver.network.serverpackets.penaltyitemdrop;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExPenaltyItemRestore extends ServerPacket
{
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PENALTY_ITEM_RESTORE.writeId(this, buffer);
		buffer.writeByte(1);
	}
}
