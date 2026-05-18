package net.sf.l2jdev.gameserver.network.serverpackets.limitshop;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExBloodyCoinCount extends ServerPacket
{
	private final long _count;

	public ExBloodyCoinCount(Player player)
	{
		this._count = player.getInventory().getInventoryItemCount(91663, -1);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_BLOODY_COIN_COUNT.writeId(this, buffer);
		buffer.writeLong(this._count);
	}
}
