package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.serverpackets.HennaEquipList;

public class RequestHennaItemList extends ClientPacket
{
	@Override
	protected void readImpl()
	{
		this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.sendPacket(new HennaEquipList(player));
		}
	}
}
