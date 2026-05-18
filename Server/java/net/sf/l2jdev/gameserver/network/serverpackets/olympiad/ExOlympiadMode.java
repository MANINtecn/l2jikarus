package net.sf.l2jdev.gameserver.network.serverpackets.olympiad;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.olympiad.OlympiadMode;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExOlympiadMode extends ServerPacket
{
	private final OlympiadMode _mode;

	public ExOlympiadMode(OlympiadMode mode)
	{
		this._mode = mode;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_OLYMPIAD_MODE.writeId(this, buffer);
		buffer.writeByte(this._mode.ordinal());
	}
}
