package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExSubPledgeSkillAdd extends ServerPacket
{
	private final int _type;
	private final int _skillId;
	private final int _skillLevel;

	public ExSubPledgeSkillAdd(int type, int skillId, int skillLevel)
	{
		this._type = type;
		this._skillId = skillId;
		this._skillLevel = skillLevel;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SUBPLEDGE_SKILL_ADD.writeId(this, buffer);
		buffer.writeInt(this._type);
		buffer.writeInt(this._skillId);
		buffer.writeInt(this._skillLevel);
	}
}
