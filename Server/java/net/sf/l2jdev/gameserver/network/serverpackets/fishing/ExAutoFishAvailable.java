package net.sf.l2jdev.gameserver.network.serverpackets.fishing;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExAutoFishAvailable extends ServerPacket
{
	public static final ExAutoFishAvailable YES = new ExAutoFishAvailable(true);
	public static final ExAutoFishAvailable NO = new ExAutoFishAvailable(false);
	private final boolean _available;

	private ExAutoFishAvailable(boolean available)
	{
		this._available = available;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_AUTOFISH_AVAILABLE.writeId(this, buffer);
		buffer.writeByte(this._available);
	}
}
