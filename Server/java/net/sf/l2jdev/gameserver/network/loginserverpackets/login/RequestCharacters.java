package net.sf.l2jdev.gameserver.network.loginserverpackets.login;

import net.sf.l2jdev.commons.network.base.BaseReadablePacket;

public class RequestCharacters extends BaseReadablePacket
{
	private final String _account;

	public RequestCharacters(byte[] decrypt)
	{
		super(decrypt);
		this.readByte();
		this._account = this.readString();
	}

	public String getAccount()
	{
		return this._account;
	}
}
