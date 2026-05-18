package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class MagicSkillCanceled extends ServerPacket
{
	private final int _objectId;

	public MagicSkillCanceled(int objectId)
	{
		this._objectId = objectId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.MAGIC_SKILL_CANCELED.writeId(this, buffer);
		buffer.writeInt(this._objectId);
	}
}
