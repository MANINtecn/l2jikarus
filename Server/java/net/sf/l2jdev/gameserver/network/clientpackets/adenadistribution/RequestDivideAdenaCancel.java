package net.sf.l2jdev.gameserver.network.clientpackets.adenadistribution;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.AdenaDistributionRequest;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.adenadistribution.ExDivideAdenaCancel;

public class RequestDivideAdenaCancel extends ClientPacket
{
	private boolean _cancel;

	@Override
	protected void readImpl()
	{
		this._cancel = this.readByte() == 0;
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (this._cancel)
			{
				AdenaDistributionRequest request = player.getRequest(AdenaDistributionRequest.class);
				if (request == null)
				{
					return;
				}

				for (Player p : request.getPlayers())
				{
					if (p != null)
					{
						p.sendPacket(SystemMessageId.ADENA_DISTRIBUTION_HAS_BEEN_CANCELLED);
						p.sendPacket(ExDivideAdenaCancel.STATIC_PACKET);
						p.removeRequest(AdenaDistributionRequest.class);
					}
				}
			}
		}
	}
}
