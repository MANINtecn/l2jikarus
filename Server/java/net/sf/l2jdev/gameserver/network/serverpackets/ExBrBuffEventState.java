package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExBrBuffEventState extends ServerPacket
{
	private final int _type;
	private final int _value;
	private final int _state;
	private final int _endtime;

	public ExBrBuffEventState(int type, int value, int state, int endtime)
	{
		this._type = type;
		this._value = value;
		this._state = state;
		this._endtime = endtime;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_BR_BUFF_EVENT_STATE.writeId(this, buffer);
		buffer.writeInt(this._type);
		buffer.writeInt(this._value);
		buffer.writeInt(this._state);
		buffer.writeInt(this._endtime);
	}
}
