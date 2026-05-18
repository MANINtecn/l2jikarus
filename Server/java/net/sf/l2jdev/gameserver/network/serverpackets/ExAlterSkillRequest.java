package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExAlterSkillRequest extends ServerPacket
{
	private final int _currentSkillId;
	private final int _nextSkillId;
	private final int _alterTime;

	public ExAlterSkillRequest(int currentSkill, int nextSkill, int alterTime)
	{
		this._currentSkillId = currentSkill;
		this._nextSkillId = nextSkill;
		this._alterTime = alterTime;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ALTER_SKILL_REQUEST.writeId(this, buffer);
		buffer.writeInt(this._nextSkillId);
		buffer.writeInt(this._currentSkillId);
		buffer.writeInt(this._alterTime);
	}
}
