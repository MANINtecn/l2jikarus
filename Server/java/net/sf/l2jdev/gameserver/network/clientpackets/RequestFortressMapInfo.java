package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.managers.FortManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.siege.Fort;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.ExShowFortressMapInfo;

public class RequestFortressMapInfo extends ClientPacket
{
	private int _fortressId;

	@Override
	protected void readImpl()
	{
		this._fortressId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Fort fort = FortManager.getInstance().getFortById(this._fortressId);
			if (fort == null)
			{
				PacketLogger.warning("Fort is not found with id (" + this._fortressId + ") in all forts with size of (" + FortManager.getInstance().getForts().size() + ") called by player (" + player + ")");
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else
			{
				player.sendPacket(new ExShowFortressMapInfo(fort));
			}
		}
	}
}
