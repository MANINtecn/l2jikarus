package net.sf.l2jdev.gameserver.network.loginserverpackets.login;

import net.sf.l2jdev.commons.network.base.BaseReadablePacket;

public class AuthResponse extends BaseReadablePacket
{
	private final int _serverId;
	private final String _serverName;

	public AuthResponse(byte[] decrypt)
	{
		super(decrypt);
		this.readByte();
		this._serverId = this.readByte();
		this._serverName = this.readString();
	}

	public int getServerId()
	{
		return this._serverId;
	}

	public String getServerName()
	{
		return this._serverName;
	}
}
