package net.sf.l2jdev.gameserver.network.clientpackets.prison;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.prison.ExPrisonUserInfo;

public class RequestPrisonUserInfo extends ClientPacket
{
	private int _prisonType;
	private int _itemAmount;
	private int _timeRemain;

	@Override
	protected void readImpl()
	{
		this._prisonType = this.readInt();
		this._itemAmount = this.readInt();
		this._timeRemain = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.sendPacket(new ExPrisonUserInfo(this._prisonType, this._itemAmount, this._timeRemain));
		}
	}
}
