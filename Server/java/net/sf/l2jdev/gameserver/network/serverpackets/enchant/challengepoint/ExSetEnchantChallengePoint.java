package net.sf.l2jdev.gameserver.network.serverpackets.enchant.challengepoint;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExSetEnchantChallengePoint extends ServerPacket
{
	private final boolean _result;

	public ExSetEnchantChallengePoint(boolean result)
	{
		this._result = result;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SET_ENCHANT_CHALLENGE_POINT.writeId(this, buffer);
		buffer.writeByte(this._result);
	}
}
