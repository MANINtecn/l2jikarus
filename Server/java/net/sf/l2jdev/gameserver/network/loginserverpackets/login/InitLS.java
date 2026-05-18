package net.sf.l2jdev.gameserver.network.loginserverpackets.login;

import net.sf.l2jdev.commons.network.base.BaseReadablePacket;

public class InitLS extends BaseReadablePacket
{
	private final int _rev;
	private final byte[] _key;

	public int getRevision()
	{
		return this._rev;
	}

	public byte[] getRSAKey()
	{
		return this._key;
	}

	public InitLS(byte[] decrypt)
	{
		super(decrypt);
		this.readByte();
		this._rev = this.readInt();
		int size = this.readInt();
		this._key = this.readBytes(size);
	}
}
