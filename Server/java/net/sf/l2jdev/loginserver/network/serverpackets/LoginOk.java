package net.sf.l2jdev.loginserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.loginserver.SessionKey;
import net.sf.l2jdev.loginserver.network.LoginClient;

public class LoginOk extends LoginServerPacket
{
	private static final byte[] RESERVED_BYTES_16 = new byte[16];
	private final int _loginOkPart1;
	private final int _loginOkPart2;
	
	public LoginOk(SessionKey sessionKey)
	{
		this._loginOkPart1 = sessionKey.getLoginOkID1();
		this._loginOkPart2 = sessionKey.getLoginOkID2();
	}
	
	@Override
	protected void writeImpl(LoginClient client, WritableBuffer buffer)
	{
		buffer.writeByte(3);
		buffer.writeInt(this._loginOkPart1);
		buffer.writeInt(this._loginOkPart2);
		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeInt(1002);
		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeBytes(RESERVED_BYTES_16);
	}
}
