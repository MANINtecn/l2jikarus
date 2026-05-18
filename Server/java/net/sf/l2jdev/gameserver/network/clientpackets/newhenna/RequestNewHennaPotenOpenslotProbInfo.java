package net.sf.l2jdev.gameserver.network.clientpackets.newhenna;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.newhenna.NewHennaPotenOpenslotProbInfo;

public class RequestNewHennaPotenOpenslotProbInfo extends ClientPacket
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
			player.sendPacket(new NewHennaPotenOpenslotProbInfo(player, this._slot));
		}
	}
}
