package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExCubeGameEnd extends ServerPacket
{
	private final boolean _isRedTeamWin;

	public ExCubeGameEnd(boolean isRedTeamWin)
	{
		this._isRedTeamWin = isRedTeamWin;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_BLOCK_UPSET_STATE.writeId(this, buffer);
		buffer.writeInt(1);
		buffer.writeInt(this._isRedTeamWin);
		buffer.writeInt(0);
	}
}
