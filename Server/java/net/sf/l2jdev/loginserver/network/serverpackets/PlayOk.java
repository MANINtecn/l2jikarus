package net.sf.l2jdev.loginserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.loginserver.SessionKey;
import net.sf.l2jdev.loginserver.network.LoginClient;

public class PlayOk extends LoginServerPacket
{
	private final int _playOkPart1;
	private final int _playOkPart2;
	
	public PlayOk(SessionKey sessionKey)
	{
		this._playOkPart1 = sessionKey.getPlayOkID1();
		this._playOkPart2 = sessionKey.getPlayOkID2();
	}
	
	@Override
	protected void writeImpl(LoginClient client, WritableBuffer buffer)
	{
		buffer.writeByte(7);
		buffer.writeInt(this._playOkPart1);
		buffer.writeInt(this._playOkPart2);
	}
}
