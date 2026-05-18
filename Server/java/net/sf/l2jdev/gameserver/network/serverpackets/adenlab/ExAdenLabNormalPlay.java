package net.sf.l2jdev.gameserver.network.serverpackets.adenlab;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExAdenLabNormalPlay extends ServerPacket
{
	private final int _bossId;
	private final int _pageIndex;
	private final int _result;

	public ExAdenLabNormalPlay(int bossId, int currentSlot, byte result)
	{
		this._bossId = bossId;
		this._pageIndex = currentSlot;
		this._result = result;
	}

	@Override
	protected void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ADENLAB_NORMAL_PLAY.writeId(this, buffer);
		buffer.writeInt(this._bossId);
		buffer.writeInt(this._pageIndex);
		buffer.writeInt(this._result);
	}
}
