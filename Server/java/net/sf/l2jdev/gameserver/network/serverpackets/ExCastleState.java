package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.model.siege.CastleSide;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExCastleState extends ServerPacket
{
	private final int _castleId;
	private final CastleSide _castleSide;

	public ExCastleState(Castle castle)
	{
		this._castleId = castle.getResidenceId();
		this._castleSide = castle.getSide();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CASTLE_STATE.writeId(this, buffer);
		buffer.writeInt(this._castleId);
		buffer.writeInt(this._castleSide.ordinal());
	}
}
