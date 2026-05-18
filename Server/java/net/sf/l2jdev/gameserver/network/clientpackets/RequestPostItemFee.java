package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPostItemFee;

public class RequestPostItemFee extends ClientPacket
{
	private long _fee = 0L;

	@Override
	protected void readImpl()
	{
		int totalItems = this.readInt();

		for (int i = 0; i < totalItems; i++)
		{
			this.readInt();
			long itemCount = this.readLong();
			if (itemCount < 1L)
			{
				this._fee += 10000L;
			}
			else if (itemCount == 1L)
			{
				this._fee += 100L;
			}
			else
			{
				this._fee += itemCount * 10L;
			}
		}

		if (this._fee < 0L || this._fee > 100000L)
		{
			this._fee = 100000L;
		}
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.sendPacket(new ExPostItemFee(this._fee));
		}
	}
}
