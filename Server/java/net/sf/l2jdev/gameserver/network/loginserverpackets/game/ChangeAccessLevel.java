package net.sf.l2jdev.gameserver.network.loginserverpackets.game;

import net.sf.l2jdev.commons.network.base.BaseWritablePacket;

public class ChangeAccessLevel extends BaseWritablePacket
{
	 
	private final String _player;
	private final int _access;

	public ChangeAccessLevel(String player, int access)
	{
		this._player = player != null ? player : "";
		this._access = access;
	}

	@Override
	public void write()
	{
		this.writeByte(4);
		this.writeInt(this._access);
		this.writeString(this._player);
	}
}
