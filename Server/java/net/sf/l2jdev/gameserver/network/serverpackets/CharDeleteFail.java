package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.enums.CharacterDeleteFailType;

public class CharDeleteFail extends ServerPacket
{
	private final int _error;

	public CharDeleteFail(CharacterDeleteFailType type)
	{
		this._error = type.ordinal();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.CHARACTER_DELETE_FAIL.writeId(this, buffer);
		buffer.writeInt(this._error);
	}
}
