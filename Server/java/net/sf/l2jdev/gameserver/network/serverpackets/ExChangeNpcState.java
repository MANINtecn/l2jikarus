package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExChangeNpcState extends ServerPacket
{
	private final int _objId;
	private final int _state;

	public ExChangeNpcState(int objId, int state)
	{
		this._objId = objId;
		this._state = state;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CHANGE_NPC_STATE.writeId(this, buffer);
		buffer.writeInt(this._objId);
		buffer.writeInt(this._state);
	}
}
