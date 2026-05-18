package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.data.holders.MultisellListHolder;
import net.sf.l2jdev.gameserver.data.xml.MultisellData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.PacketLogger;

public class RequestMultisellList extends ClientPacket
{
	private int _multiSellId;

	@Override
	protected void readImpl()
	{
		this._multiSellId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			MultisellListHolder multisell = MultisellData.getInstance().getMultisell(this._multiSellId);
			if (multisell == null)
			{
				PacketLogger.warning("RequestMultisellList: " + player + " requested non-existent list " + this._multiSellId + ".");
			}
			else
			{
				MultisellData.getInstance().separateAndSend(this._multiSellId, player, null, false, Double.NaN, Double.NaN, 4);
			}
		}
	}
}
