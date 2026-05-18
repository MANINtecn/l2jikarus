package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExNeedToChangeName extends ServerPacket
{
	private final int _type;
	private final int _subType;
	private final String _name;

	public ExNeedToChangeName(int type, int subType, String name)
	{
		this._type = type;
		this._subType = subType;
		this._name = name;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_NEED_TO_CHANGE_NAME.writeId(this, buffer);
		buffer.writeInt(this._type);
		buffer.writeInt(this._subType);
		buffer.writeString(this._name);
	}
}
