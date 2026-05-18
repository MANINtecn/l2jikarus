package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.serverpackets.CreateItemProbList;

public class RequestCreateItemProbList extends ClientPacket
{
	private int _itemId;

	@Override
	protected void readImpl()
	{
		this._itemId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.sendPacket(new CreateItemProbList(this._itemId));
		}
	}
}
