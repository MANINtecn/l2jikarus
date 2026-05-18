package net.sf.l2jdev.gameserver.network.serverpackets.relics;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExRelicsUpgrade extends ServerPacket
{
	private final boolean _success;
	private final int _relicId;
	private final int _relicLevel;

	public ExRelicsUpgrade(boolean success, int relicId, int relicLevel)
	{
		this._success = success;
		this._relicId = relicId;
		this._relicLevel = relicLevel;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_RELICS_UPGRADE.writeId(this, buffer);
		buffer.writeByte(this._success);
		buffer.writeInt(this._relicId);
		buffer.writeInt(this._relicLevel);
	}
}
