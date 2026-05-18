package net.sf.l2jdev.gameserver.network.loginserverpackets.game;

import net.sf.l2jdev.commons.network.base.BaseWritablePacket;
import net.sf.l2jdev.gameserver.LoginServerThread;

public class PlayerAuthRequest extends BaseWritablePacket
{
	 
	private final String _account;
	private final LoginServerThread.SessionKey _key;

	public PlayerAuthRequest(String account, LoginServerThread.SessionKey key)
	{
		this._account = account != null ? account : "";
		this._key = key;
	}

	@Override
	public void write()
	{
		this.writeByte(5);
		this.writeString(this._account);
		this.writeInt(this._key != null ? this._key.playOkID1 : 0);
		this.writeInt(this._key != null ? this._key.playOkID2 : 0);
		this.writeInt(this._key != null ? this._key.loginOkID1 : 0);
		this.writeInt(this._key != null ? this._key.loginOkID2 : 0);
	}
}
