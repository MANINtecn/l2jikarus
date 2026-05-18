package net.sf.l2jdev.gameserver.network.serverpackets.equipmentupgrade;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.AbstractItemPacket;

public class ExUpgradeSystemResult extends AbstractItemPacket
{
	private final int _objectId;
	private final int _success;

	public ExUpgradeSystemResult(int objectId, int success)
	{
		this._objectId = objectId;
		this._success = success;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_UPGRADE_SYSTEM_RESULT.writeId(this, buffer);
		buffer.writeShort(this._success);
		buffer.writeInt(this._objectId);
	}
}
