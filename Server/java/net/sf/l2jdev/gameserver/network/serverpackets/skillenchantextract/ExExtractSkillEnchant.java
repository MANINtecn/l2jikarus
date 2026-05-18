package net.sf.l2jdev.gameserver.network.serverpackets.skillenchantextract;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExExtractSkillEnchant extends ServerPacket
{
	private final byte _result;
	private final int _skillId;
	private final int _level;
	private final int _subLevel;

	public ExExtractSkillEnchant(byte result, int skillId, int skillLevel, int skillSubLevel)
	{
		this._result = result;
		this._skillId = skillId;
		this._level = skillLevel;
		this._subLevel = skillSubLevel;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_EXTRACT_SKILL_ENCHANT.writeId(this, buffer);
		buffer.writeByte(this._result);
		buffer.writeInt(this._skillId);
		buffer.writeInt(this._level);
		buffer.writeInt(this._subLevel);
	}
}
