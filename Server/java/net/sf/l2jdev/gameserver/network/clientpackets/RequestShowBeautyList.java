package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.serverpackets.ExResponseBeautyList;

public class RequestShowBeautyList extends ClientPacket
{
	private int _type;

	@Override
	protected void readImpl()
	{
		this._type = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.sendPacket(new ExResponseBeautyList(player, this._type));
		}
	}
}
