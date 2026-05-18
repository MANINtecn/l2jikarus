package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2jdev.gameserver.util.LocationUtil;

public class RequestLinkHtml extends ClientPacket
{
	private String _link;

	@Override
	protected void readImpl()
	{
		this._link = this.readString();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (this._link.isEmpty())
			{
				PacketLogger.warning(player + " sent empty html link!");
			}
			else if (this._link.contains(".."))
			{
				PacketLogger.warning(player + " sent invalid html link: link " + this._link);
			}
			else
			{
				int htmlObjectId = player.validateHtmlAction("link " + this._link);
				if (htmlObjectId == -1)
				{
					PacketLogger.warning(player + " sent non cached html link: link " + this._link);
				}
				else if (htmlObjectId <= 0 || LocationUtil.isInsideRangeOfObjectId(player, htmlObjectId, 250))
				{
					String filename = "data/html/" + this._link;
					NpcHtmlMessage msg = new NpcHtmlMessage(htmlObjectId);
					msg.setFile(player, filename);
					player.sendPacket(msg);
				}
			}
		}
	}
}
