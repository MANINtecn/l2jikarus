package net.sf.l2jdev.gameserver.network.clientpackets.dualinventory;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class RequestExDualInventorySwap extends ClientPacket
{
	private int _slot;

	@Override
	protected void readImpl()
	{
		this._slot = this.readByte() == 0 ? 0 : 1;
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.setDualInventorySlot(this._slot);
		}
	}
}
