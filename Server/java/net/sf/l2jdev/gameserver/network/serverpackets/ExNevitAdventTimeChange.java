package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExNevitAdventTimeChange extends ServerPacket
{
	private final boolean _paused;
	private final int _time;

	public ExNevitAdventTimeChange(int time)
	{
		this._time = time > 240000 ? 240000 : time;
		this._paused = this._time < 1;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_RESPONSE_CRYSTALITEM_INFO.writeId(this, buffer);
		buffer.writeByte(!this._paused);
		buffer.writeInt(this._time);
	}
}
