package net.sf.l2jdev.gameserver.network.clientpackets.revenge;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.revenge.ExPvpBookShareRevengeList;

public class RequestExPvpBookShareRevengeList extends ClientPacket
{
	private int _objectId;

	@Override
	protected void readImpl()
	{
		this._objectId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (this._objectId == player.getObjectId())
			{
				player.sendPacket(new ExPvpBookShareRevengeList(player));
			}
		}
	}
}
