package net.sf.l2jdev.gameserver.network.clientpackets;

public class ExGetOnAirShip extends ClientPacket
{
	protected int _x;
	protected int _y;
	protected int _z;
	protected int _shipId;

	@Override
	protected void readImpl()
	{
		this._x = this.readInt();
		this._y = this.readInt();
		this._z = this.readInt();
		this._shipId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
	}
}
