package net.sf.l2jdev.gameserver.network.clientpackets.castlewar;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.castlewar.MercenaryCastleWarCastleSiegeDefenderList;

public class ExMercenaryCastleWarCastleSiegeDefenderList extends ClientPacket
{
	private int _castleId;

	@Override
	protected void readImpl()
	{
		this._castleId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.sendPacket(new MercenaryCastleWarCastleSiegeDefenderList(this._castleId));
		}
	}
}
