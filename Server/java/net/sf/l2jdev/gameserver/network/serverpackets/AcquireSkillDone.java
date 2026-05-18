package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class AcquireSkillDone extends ServerPacket
{
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.ACQUIRE_SKILL_DONE.writeId(this, buffer);
	}
}
