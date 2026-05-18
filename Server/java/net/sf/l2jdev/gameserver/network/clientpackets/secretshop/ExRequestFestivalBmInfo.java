package net.sf.l2jdev.gameserver.network.clientpackets.secretshop;

import net.sf.l2jdev.gameserver.managers.events.SecretShopEventManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class ExRequestFestivalBmInfo extends ClientPacket
{
	private boolean _isOpenWindow;

	@Override
	protected void readImpl()
	{
		this._isOpenWindow = this.readByte() != 0;
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			SecretShopEventManager.getInstance().show(player, this._isOpenWindow);
		}
	}
}
