package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExPledgeCoinInfo extends ServerPacket
{
	private final long _count;

	public ExPledgeCoinInfo(Player player)
	{
		this._count = player.getHonorCoins();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PLEDGE_COIN_INFO.writeId(this, buffer);
		buffer.writeLong(this._count);
	}
}
