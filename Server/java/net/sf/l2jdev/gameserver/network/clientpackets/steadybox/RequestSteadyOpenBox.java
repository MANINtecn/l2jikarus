package net.sf.l2jdev.gameserver.network.clientpackets.steadybox;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class RequestSteadyOpenBox extends ClientPacket
{
	private int _slotId;
	private long _feeBoxPrice;

	@Override
	protected void readImpl()
	{
		this._slotId = this.readInt();
		this._feeBoxPrice = this.readLong();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (this._feeBoxPrice > 0L)
			{
				player.getAchievementBox().skipBoxOpenTime(this._slotId, this._feeBoxPrice);
			}
			else
			{
				player.getAchievementBox().openBox(this._slotId);
			}
		}
	}
}
