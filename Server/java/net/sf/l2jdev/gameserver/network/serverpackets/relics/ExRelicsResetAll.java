package net.sf.l2jdev.gameserver.network.serverpackets.relics;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExRelicsResetAll extends ServerPacket
{
	private final int _relicId;

	public ExRelicsResetAll(int relicId)
	{
		this._relicId = relicId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ALL_RESET_RELICS.writeId(this, buffer);
		buffer.writeInt(this._relicId);
	}
}
