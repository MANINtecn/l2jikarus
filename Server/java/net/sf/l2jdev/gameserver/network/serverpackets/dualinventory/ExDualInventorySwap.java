package net.sf.l2jdev.gameserver.network.serverpackets.dualinventory;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExDualInventorySwap extends ServerPacket
{
	private final int _slot;

	public ExDualInventorySwap(int slot)
	{
		this._slot = slot;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_DUAL_INVENTORY_INFO.writeId(this, buffer);
		buffer.writeByte(this._slot);
		buffer.writeByte(1);
		buffer.writeByte(1);
		buffer.writeByte(0);
		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeByte(0);
		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeByte(0);
		buffer.writeByte(0);
	}
}
