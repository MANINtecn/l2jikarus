package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.network.serverpackets.SiegeDefenderList;

public class RequestSiegeDefenderList extends ClientPacket
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
		Castle castle = CastleManager.getInstance().getCastleById(this._castleId);
		if (castle != null)
		{
			Player player = this.getPlayer();
			if (player != null)
			{
				player.sendPacket(new SiegeDefenderList(castle));
			}
		}
	}
}
