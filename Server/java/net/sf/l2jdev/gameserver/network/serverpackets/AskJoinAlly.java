package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class AskJoinAlly extends ServerPacket
{
	private final String _requestorName;
	private final String _requestorAllyName;
	private final int _requestorObjId;

	public AskJoinAlly(int requestorObjId, String requestorName, String requestorAllyName)
	{
		this._requestorName = requestorName;
		this._requestorObjId = requestorObjId;
		this._requestorAllyName = requestorAllyName;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.ASK_JOIN_ALLIANCE.writeId(this, buffer);
		buffer.writeInt(this._requestorObjId);
		buffer.writeString(this._requestorAllyName);
		buffer.writeString(null);
		buffer.writeString(this._requestorName);
	}
}
