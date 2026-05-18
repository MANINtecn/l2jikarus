package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class PledgeSkillListAdd extends ServerPacket
{
	private final int _id;
	private final int _level;

	public PledgeSkillListAdd(int id, int level)
	{
		this._id = id;
		this._level = level;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PLEDGE_SKILL_ADD.writeId(this, buffer);
		buffer.writeInt(this._id);
		buffer.writeInt(this._level);
	}
}
