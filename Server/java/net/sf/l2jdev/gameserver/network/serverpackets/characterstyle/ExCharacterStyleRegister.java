package net.sf.l2jdev.gameserver.network.serverpackets.characterstyle;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExCharacterStyleRegister extends ServerPacket
{
	public static final ExCharacterStyleRegister STATIC_PACKET_SUCCESS = new ExCharacterStyleRegister((byte) 1);
	public static final ExCharacterStyleRegister STATIC_PACKET_FAIL = new ExCharacterStyleRegister((byte) 0);
	private final byte _result;

	public ExCharacterStyleRegister(byte result)
	{
		this._result = result;
	}

	@Override
	protected void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CHARACTER_STYLE_REGIST.writeId(this, buffer);
		buffer.writeByte(this._result);
		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeLong(0L);
	}
}
