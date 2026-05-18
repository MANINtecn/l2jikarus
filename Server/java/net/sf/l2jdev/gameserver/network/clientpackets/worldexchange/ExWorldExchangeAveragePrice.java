package net.sf.l2jdev.gameserver.network.clientpackets.worldexchange;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.worldexchange.WorldExchangeAveragePrice;

public class ExWorldExchangeAveragePrice extends ClientPacket
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
			player.sendPacket(new WorldExchangeAveragePrice(this._itemId));
		}
	}
}
