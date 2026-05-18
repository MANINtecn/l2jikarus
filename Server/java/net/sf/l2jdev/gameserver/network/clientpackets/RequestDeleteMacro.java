package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;

public class RequestDeleteMacro extends ClientPacket
{
	private int _id;

	@Override
	protected void readImpl()
	{
		this._id = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.deleteMacro(this._id);
		}
	}
}
