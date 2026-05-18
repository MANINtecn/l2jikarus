package net.sf.l2jdev.gameserver.network.clientpackets.worldexchange;

import net.sf.l2jdev.gameserver.config.WorldExchangeConfig;
import net.sf.l2jdev.gameserver.managers.WorldExchangeManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class ExWorldExchangeSettleRecvResult extends ClientPacket
{
	private long _worldExchangeIndex;

	@Override
	protected void readImpl()
	{
		this._worldExchangeIndex = this.readLong();
	}

	@Override
	protected void runImpl()
	{
		if (WorldExchangeConfig.ENABLE_WORLD_EXCHANGE)
		{
			Player player = this.getPlayer();
			if (player != null)
			{
				WorldExchangeManager.getInstance().getItemStatusAndMakeAction(player, this._worldExchangeIndex);
			}
		}
	}
}
