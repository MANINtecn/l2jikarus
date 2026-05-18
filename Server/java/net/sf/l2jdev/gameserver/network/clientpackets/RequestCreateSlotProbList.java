package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.serverpackets.CreateSlotProbList;

public class RequestCreateSlotProbList extends ClientPacket
{
	private int _slot;

	@Override
	protected void readImpl()
	{
		this._slot = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.sendPacket(new CreateSlotProbList(player, this._slot));
		}
	}
}
