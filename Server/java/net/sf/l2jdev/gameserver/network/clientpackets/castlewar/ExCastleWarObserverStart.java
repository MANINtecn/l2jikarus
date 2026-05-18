package net.sf.l2jdev.gameserver.network.clientpackets.castlewar;

import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class ExCastleWarObserverStart extends ClientPacket
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
			if (player.hasSummon())
			{
				player.sendPacket(SystemMessageId.YOU_MAY_NOT_OBSERVE_A_SIEGE_WITH_A_SERVITOR_SUMMONED);
			}
			else if (player.isOnEvent())
			{
				player.sendMessage("Cannot use while on an event.");
			}
			else
			{
				Castle castle = CastleManager.getInstance().getCastleById(this._castleId);
				if (castle != null)
				{
					if (castle.getSiege().isInProgress())
					{
						Player random = castle.getSiege().getPlayersInZone().stream().findAny().orElse(null);
						if (random != null)
						{
							player.enterObserverMode(random.getLocation());
						}
					}
				}
			}
		}
	}
}
