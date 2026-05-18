package net.sf.l2jdev.gameserver.network.clientpackets.mentoring;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.mentoring.ListMenteeWaiting;

public class RequestMenteeWaitingList extends ClientPacket
{
	private int _page;
	private int _minLevel;
	private int _maxLevel;

	@Override
	protected void readImpl()
	{
		this._page = this.readInt();
		this._minLevel = this.readInt();
		this._maxLevel = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.sendPacket(new ListMenteeWaiting(this._page, this._minLevel, this._maxLevel));
		}
	}
}
