package net.sf.l2jdev.gameserver.network.serverpackets.olympiad;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExOlympiadMatchMakingResult extends ServerPacket
{
	private final int _gameRuleType;
	private final int _type;

	public ExOlympiadMatchMakingResult(int gameRuleType, int type)
	{
		this._gameRuleType = gameRuleType;
		this._type = type;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_OLYMPIAD_MATCH_MAKING_RESULT.writeId(this, buffer);
		buffer.writeByte(this._type);
		buffer.writeInt(this._gameRuleType);
	}
}
