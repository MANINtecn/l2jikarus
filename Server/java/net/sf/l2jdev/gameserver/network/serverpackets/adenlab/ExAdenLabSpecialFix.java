package net.sf.l2jdev.gameserver.network.serverpackets.adenlab;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExAdenLabSpecialFix extends ServerPacket
{
	private final int _bossId;
	private final int _pageIndex;
	private final boolean _success;

	public ExAdenLabSpecialFix(int bossID, int slotID, boolean success)
	{
		this._bossId = bossID;
		this._pageIndex = slotID;
		this._success = success;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ADENLAB_SPECIAL_FIX.writeId(this, buffer);
		buffer.writeInt(this._bossId);
		buffer.writeInt(this._pageIndex);
		buffer.writeByte((byte) (this._success ? 1 : 0));
	}
}
