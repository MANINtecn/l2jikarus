package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.serverpackets.ExListMpccWaiting;

public class RequestExListMpccWaiting extends ClientPacket
{
	private int _page;
	private int _location;
	private int _level;

	@Override
	protected void readImpl()
	{
		this._page = this.readInt();
		this._location = this.readInt();
		this._level = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.sendPacket(new ExListMpccWaiting(this._page, this._location, this._level));
		}
	}
}
