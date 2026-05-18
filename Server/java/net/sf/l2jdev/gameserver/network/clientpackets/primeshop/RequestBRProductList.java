package net.sf.l2jdev.gameserver.network.clientpackets.primeshop;

import net.sf.l2jdev.gameserver.data.xml.PrimeShopData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.primeshop.ExBRProductList;

public class RequestBRProductList extends ClientPacket
{
	private int _type;

	@Override
	protected void readImpl()
	{
		this._type = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			switch (this._type)
			{
				case 0:
					player.sendPacket(new ExBRProductList(player, 0, PrimeShopData.getInstance().getPrimeItems().values()));
				case 1:
				case 2:
					break;
				default:
					PacketLogger.warning(player + " send unhandled product list type: " + this._type);
			}
		}
	}
}
