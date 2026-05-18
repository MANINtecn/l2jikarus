package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class AutoAttackStop extends ServerPacket
{
	private final int _targetObjId;

	public AutoAttackStop(int targetObjId)
	{
		this._targetObjId = targetObjId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.COMBAT_MODE_FINISH.writeId(this, buffer);
		buffer.writeInt(this._targetObjId);
	}

	@Override
	public boolean canBeDropped(GameClient client)
	{
		return true;
	}
}
