package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExMagicSkillUseGround extends ServerPacket
{
	private final int _playerObjectId;
	private final int _skillId;
	private final Location _location;

	public ExMagicSkillUseGround(int playerObjectId, int skillId, Location location)
	{
		this._playerObjectId = playerObjectId;
		this._skillId = skillId;
		this._location = location;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_MAGIC_SKILL_USE_GROUND.writeId(this, buffer);
		buffer.writeInt(this._playerObjectId);
		buffer.writeInt(this._skillId);
		buffer.writeInt(this._location.getX());
		buffer.writeInt(this._location.getY());
		buffer.writeInt(this._location.getZ());
	}
}
