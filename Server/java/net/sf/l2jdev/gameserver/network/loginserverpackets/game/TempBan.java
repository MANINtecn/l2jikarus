package net.sf.l2jdev.gameserver.network.loginserverpackets.game;

import net.sf.l2jdev.commons.network.base.BaseWritablePacket;

public class TempBan extends BaseWritablePacket
{
 
	private final String _accountName;
	private final String _ip;
	private final long _minutes;

	public TempBan(String accountName, String ip, long time)
	{
		this._accountName = accountName != null ? accountName : "";
		this._ip = ip != null ? ip : "";
		this._minutes = time > 0L ? time : 0L;
	}

	@Override
	public void write()
	{
		this.writeByte(10);
		this.writeString(this._accountName);
		this.writeString(this._ip);
		this.writeLong(System.currentTimeMillis() + this._minutes * 60000L);
		this.writeByte(0);
	}
}
