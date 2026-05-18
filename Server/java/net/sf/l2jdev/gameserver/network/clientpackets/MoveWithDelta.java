package net.sf.l2jdev.gameserver.network.clientpackets;

public class MoveWithDelta extends ClientPacket
{
	protected int _dx;
	protected int _dy;
	protected int _dz;

	@Override
	protected void readImpl()
	{
		this._dx = this.readInt();
		this._dy = this.readInt();
		this._dz = this.readInt();
	}

	@Override
	protected void runImpl()
	{
	}
}
