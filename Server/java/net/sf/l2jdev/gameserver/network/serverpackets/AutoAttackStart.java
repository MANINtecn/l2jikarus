package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class AutoAttackStart extends ServerPacket
{
	private final int _targetObjId;

	public AutoAttackStart(int targetId)
	{
		this._targetObjId = targetId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.COMBAT_MODE_START.writeId(this, buffer);
		buffer.writeInt(this._targetObjId);
	}

	@Override
	public boolean canBeDropped(GameClient client)
	{
		return true;
	}
}
