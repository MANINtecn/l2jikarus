package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Player;

public class CannotMoveAnymore extends ClientPacket
{
	private int _x;
	private int _y;
	private int _z;
	private int _heading;

	@Override
	protected void readImpl()
	{
		this._x = this.readInt();
		this._y = this.readInt();
		this._z = this.readInt();
		this._heading = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (player.getAI() != null)
			{
				player.getAI().notifyAction(net.sf.l2jdev.gameserver.ai.Action.ARRIVED_BLOCKED, new Location(this._x, this._y, this._z, this._heading));
			}
		}
	}
}
