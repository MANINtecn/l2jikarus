package net.sf.l2jdev.gameserver.network.clientpackets.steadybox;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class RequestSteadyGetReward extends ClientPacket
{
	private int _slotId;

	@Override
	protected void readImpl()
	{
		this._slotId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.getAchievementBox().getReward(this._slotId);
		}
	}
}
