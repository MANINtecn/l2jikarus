package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExAskJoinMPCC extends ServerPacket
{
	private final String _requestorName;

	public ExAskJoinMPCC(String requestorName)
	{
		this._requestorName = requestorName;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ASK_JOIN_MPCC.writeId(this, buffer);
		buffer.writeString(this._requestorName);
		buffer.writeInt(0);
	}
}
