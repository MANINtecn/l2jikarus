package net.sf.l2jdev.gameserver.network.clientpackets.shuttle;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class RequestShuttleGetOff extends ClientPacket
{
	private int _x;
	private int _y;
	private int _z;

	@Override
	protected void readImpl()
	{
		this.readInt();
		this._x = this.readInt();
		this._y = this.readInt();
		this._z = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (player.getShuttle() != null)
			{
				player.getShuttle().removePassenger(player, this._x, this._y, this._z);
			}
		}
	}
}
