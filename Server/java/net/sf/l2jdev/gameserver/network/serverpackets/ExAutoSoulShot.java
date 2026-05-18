package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExAutoSoulShot extends ServerPacket
{
	private final int _itemId;
	private final boolean _enable;
	private final int _type;

	public ExAutoSoulShot(int itemId, boolean enable, int type)
	{
		this._itemId = itemId;
		this._enable = enable;
		this._type = type;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_AUTO_SOUL_SHOT.writeId(this, buffer);
		buffer.writeInt(this._itemId);
		buffer.writeInt(this._enable);
		buffer.writeInt(this._type);
	}
}
