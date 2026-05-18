package net.sf.l2jdev.gameserver.network.serverpackets.pledgeV3;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExAllianceCreateResult extends ServerPacket
{
	private final int _nResult;

	public ExAllianceCreateResult(int nResult)
	{
		this._nResult = nResult;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ALLIANCE_CREATE.writeId(this, buffer);
		buffer.writeInt(this._nResult);
	}
}
