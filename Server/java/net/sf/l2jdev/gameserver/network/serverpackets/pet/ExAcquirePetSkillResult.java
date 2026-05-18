package net.sf.l2jdev.gameserver.network.serverpackets.pet;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExAcquirePetSkillResult extends ServerPacket
{
	private final int _skillId;
	private final int _skillLevel;
	private final boolean _success;

	public ExAcquirePetSkillResult(int skillId, int skillLevel, boolean success)
	{
		this._skillId = skillId;
		this._skillLevel = skillLevel;
		this._success = success;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ACQUIRE_PET_SKILL_RESULT.writeId(this, buffer);
		buffer.writeByte(this._success);
		buffer.writeInt(this._skillId);
		buffer.writeInt(this._skillLevel);
	}
}
