package net.sf.l2jdev.gameserver.network.serverpackets.adenlab;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExAdenLabBossUnlock extends ServerPacket
{
	private final int _bossId;
	private final boolean _success;

	public ExAdenLabBossUnlock(int bossId, boolean success)
	{
		this._bossId = bossId;
		this._success = success;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ADENLAB_UNLOCK_BOSS.writeId(this, buffer);
		buffer.writeInt(this._bossId);
		buffer.writeByte((byte) (this._success ? 1 : 0));
	}
}
