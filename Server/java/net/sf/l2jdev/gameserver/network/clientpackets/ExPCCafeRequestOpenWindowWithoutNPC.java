package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.custom.PremiumSystemConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.serverpackets.NpcHtmlMessage;

public class ExPCCafeRequestOpenWindowWithoutNPC extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null && PremiumSystemConfig.PC_CAFE_ENABLED)
		{
			NpcHtmlMessage html = new NpcHtmlMessage();
			html.setFile(player, "data/html/pccafe.htm");
			player.sendPacket(html);
		}
	}
}
