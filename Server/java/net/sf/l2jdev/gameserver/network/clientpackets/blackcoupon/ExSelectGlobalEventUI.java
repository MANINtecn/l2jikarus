package net.sf.l2jdev.gameserver.network.clientpackets.blackcoupon;

import net.sf.l2jdev.gameserver.data.xml.MultisellData;
import net.sf.l2jdev.gameserver.managers.events.BlackCouponManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class ExSelectGlobalEventUI extends ClientPacket
{
	private int _eventIndex;

	@Override
	public void readImpl()
	{
		this._eventIndex = this.readInt();
	}

	@Override
	public void runImpl()
	{
		Player player = this.getClient().getPlayer();
		if (player != null)
		{
			if (this._eventIndex == 1000001)
			{
				MultisellData.getInstance().separateAndSend(BlackCouponManager.getInstance().getMultisellId(), player, null, false);
			}
		}
	}
}
