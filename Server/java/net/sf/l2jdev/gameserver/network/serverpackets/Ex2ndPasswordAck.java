package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class Ex2ndPasswordAck extends ServerPacket
{
	public static final int SUCCESS = 0;
	public static final int WRONG_PATTERN = 1;
	private final int _status;
	private final int _response;

	public Ex2ndPasswordAck(int status, int response)
	{
		this._status = status;
		this._response = response;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_2ND_PASSWORD_ACK.writeId(this, buffer);
		buffer.writeByte(this._status);
		buffer.writeInt(this._response == 1);
		buffer.writeInt(0);
	}
}
