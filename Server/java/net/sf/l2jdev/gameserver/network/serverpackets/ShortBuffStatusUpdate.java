package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ShortBuffStatusUpdate extends ServerPacket
{
	public static final ShortBuffStatusUpdate RESET_SHORT_BUFF = new ShortBuffStatusUpdate(0, 0, 0, 0);
	private final int _skillId;
	private final int _skillLevel;
	private final int _skillSubLevel;
	private final int _duration;

	public ShortBuffStatusUpdate(int skillId, int skillLevel, int skillSubLevel, int duration)
	{
		this._skillId = skillId;
		this._skillLevel = skillLevel;
		this._skillSubLevel = skillSubLevel;
		this._duration = duration;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.SHORT_BUFF_STATUS_UPDATE.writeId(this, buffer);
		buffer.writeInt(this._skillId);
		buffer.writeShort(this._skillLevel);
		buffer.writeShort(this._skillSubLevel);
		buffer.writeInt(this._duration);
	}
}
