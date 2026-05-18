package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExNewSkillToLearnByLevelUp extends ServerPacket
{
	public static final ExNewSkillToLearnByLevelUp STATIC_PACKET = new ExNewSkillToLearnByLevelUp();

	private ExNewSkillToLearnByLevelUp()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_NEW_SKILL_TO_LEARN_BY_LEVEL_UP.writeId(this, buffer);
	}
}
