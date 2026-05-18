package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.data.xml.HennaData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.henna.Henna;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.HennaItemRemoveInfo;

public class RequestHennaItemRemoveInfo extends ClientPacket
{
	private int _symbolId;

	@Override
	protected void readImpl()
	{
		this._symbolId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null && this._symbolId != 0)
		{
			Henna henna = HennaData.getInstance().getHenna(this._symbolId);
			if (henna == null)
			{
				PacketLogger.warning(this.getClass().getName() + ": Invalid Henna Id: " + this._symbolId + " from " + player);
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else
			{
				player.sendPacket(new HennaItemRemoveInfo(henna, player));
			}
		}
	}
}
