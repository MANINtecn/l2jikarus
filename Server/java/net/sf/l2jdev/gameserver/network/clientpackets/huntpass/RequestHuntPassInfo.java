package net.sf.l2jdev.gameserver.network.clientpackets.huntpass;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.huntpass.HuntPassInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.huntpass.HuntPassSayhasSupportInfo;

public class RequestHuntPassInfo extends ClientPacket
{
	private int _passType;

	@Override
	protected void readImpl()
	{
		this._passType = this.readByte();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.sendPacket(new HuntPassInfo(player, this._passType));
			player.sendPacket(new HuntPassSayhasSupportInfo(player));
		}
	}
}
