package net.sf.l2jdev.gameserver.network.clientpackets.friend;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.friend.ExBlockDetailInfo;

public class RequestBlockDetailInfo extends ClientPacket
{
	private String _name;

	@Override
	protected void readImpl()
	{
		this._name = this.readString();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.sendPacket(new ExBlockDetailInfo(player, this._name));
		}
	}
}
