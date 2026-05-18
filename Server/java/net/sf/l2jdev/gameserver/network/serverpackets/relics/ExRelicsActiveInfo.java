package net.sf.l2jdev.gameserver.network.serverpackets.relics;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExRelicsActiveInfo extends ServerPacket
{
	private final int _relicId;
	private final int _relicLevel;

	public ExRelicsActiveInfo(int relicId, int relicLevel)
	{
		this._relicId = relicId;
		this._relicLevel = relicLevel;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_RELICS_ACTIVE_INFO.writeId(this, buffer);
		buffer.writeInt(this._relicId);
		buffer.writeInt(this._relicLevel);
	}
}
