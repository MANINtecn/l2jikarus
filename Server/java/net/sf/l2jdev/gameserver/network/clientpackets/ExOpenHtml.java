package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.cache.HtmCache;
import net.sf.l2jdev.gameserver.config.GameAssistantConfig;
import net.sf.l2jdev.gameserver.config.custom.PremiumSystemConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPremiumManagerShowHtml;
import net.sf.l2jdev.gameserver.network.serverpackets.NpcHtmlMessage;

public class ExOpenHtml extends ClientPacket
{
	private int _type;

	@Override
	protected void readImpl()
	{
		this._type = this.readByte();
	}

	@Override
	protected void runImpl()
	{
		GameClient client = this.getClient();
		Player player = client.getPlayer();
		if (player != null)
		{
			switch (this._type)
			{
				case 1:
					if (PremiumSystemConfig.PC_CAFE_ENABLED)
					{
						NpcHtmlMessage html = new NpcHtmlMessage();
						html.setFile(player, "data/html/pccafe.htm");
						player.sendPacket(html);
					}
					break;
				case 5:
					if (GameAssistantConfig.GAME_ASSISTANT_ENABLED)
					{
						client.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/32478.html")));
					}
					break;
				default:
					PacketLogger.warning("Unknown ExOpenHtml type (" + this._type + ")");
			}
		}
	}
}
