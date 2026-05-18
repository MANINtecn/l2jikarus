package net.sf.l2jdev.gameserver.network.loginserverpackets.game;

import net.sf.l2jdev.commons.network.base.BaseWritablePacket;

public class SendMail extends BaseWritablePacket
{
	 
	private final String _accountName;
	private final String _mailId;
	private final String[] _args;

	public SendMail(String accountName, String mailId, String... args)
	{
		this._accountName = accountName != null ? accountName : "";
		this._mailId = mailId != null ? mailId : "";
		this._args = args != null ? args : new String[0];
	}

	@Override
	public void write()
	{
		this.writeByte(9);
		this.writeString(this._accountName);
		this.writeString(this._mailId);
		this.writeByte(this._args.length);

		for (String a : this._args)
		{
			this.writeString(a != null ? a : "");
		}
	}
}
