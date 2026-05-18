package net.sf.l2jdev.gameserver.network.loginserverpackets.game;

import net.sf.l2jdev.commons.network.base.BaseWritablePacket;

public class PlayerLogout extends BaseWritablePacket
{
	 
	private final String _player;

	public PlayerLogout(String player)
	{
		this._player = player != null ? player : "";
	}

	@Override
	public void write()
	{
		this.writeByte(3);
		this.writeString(this._player);
	}
}
