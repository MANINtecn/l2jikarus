package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class OnEventTrigger extends ServerPacket
{
	private final int _emitterId;
	private final boolean _enabled;

	public OnEventTrigger(int emitterId, boolean enabled)
	{
		this._emitterId = emitterId;
		this._enabled = enabled;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EVENT_TRIGGER.writeId(this, buffer);
		buffer.writeInt(this._emitterId);
		buffer.writeByte(this._enabled);
	}
}
