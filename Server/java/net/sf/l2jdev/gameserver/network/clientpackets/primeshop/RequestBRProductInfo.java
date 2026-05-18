package net.sf.l2jdev.gameserver.network.clientpackets.primeshop;

import net.sf.l2jdev.gameserver.data.xml.PrimeShopData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class RequestBRProductInfo extends ClientPacket
{
	private int _brId;

	@Override
	protected void readImpl()
	{
		this._brId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			PrimeShopData.getInstance().showProductInfo(player, this._brId);
		}
	}
}
