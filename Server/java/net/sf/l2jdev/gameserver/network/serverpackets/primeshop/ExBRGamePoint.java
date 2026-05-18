package net.sf.l2jdev.gameserver.network.serverpackets.primeshop;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExBRGamePoint extends ServerPacket
{
	private final int _charId;
	private final int _charPoints;

	public ExBRGamePoint(Player player)
	{
		this._charId = player.getObjectId();
		this._charPoints = player.getPrimePoints();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_BR_NOTIFY_GAME_POINT.writeId(this, buffer);
		buffer.writeInt(this._charId);
		buffer.writeLong(this._charPoints);
		buffer.writeInt(0);
	}
}
