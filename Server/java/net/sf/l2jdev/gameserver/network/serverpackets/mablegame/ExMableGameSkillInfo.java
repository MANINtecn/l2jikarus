package net.sf.l2jdev.gameserver.network.serverpackets.mablegame;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExMableGameSkillInfo extends ServerPacket
{
	private final int _skillId;
	private final int _skillLevel;

	public ExMableGameSkillInfo(int skillId, int skillLevel)
	{
		this._skillId = skillId;
		this._skillLevel = skillLevel;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_MABLE_GAME_SKILL_INFO.writeId(this, buffer);
		buffer.writeInt(this._skillId);
		buffer.writeInt(this._skillLevel);
	}
}
