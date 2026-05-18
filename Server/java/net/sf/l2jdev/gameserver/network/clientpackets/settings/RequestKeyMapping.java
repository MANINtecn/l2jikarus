package net.sf.l2jdev.gameserver.network.clientpackets.settings;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.settings.ExUISetting;

public class RequestKeyMapping extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (PlayerConfig.STORE_UI_SETTINGS)
			{
				player.sendPacket(new ExUISetting(player));
			}
		}
	}
}
