package net.sf.l2jdev.gameserver.network.serverpackets.luckygame;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.enums.LuckyGameType;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExStartLuckyGame extends ServerPacket
{
	private final LuckyGameType _type;
	private final int _ticketCount;

	public ExStartLuckyGame(LuckyGameType type, long ticketCount)
	{
		this._type = type;
		this._ticketCount = (int) ticketCount;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_START_LUCKY_GAME.writeId(this, buffer);
		buffer.writeInt(this._type.ordinal());
		buffer.writeInt(this._ticketCount);
	}
}
