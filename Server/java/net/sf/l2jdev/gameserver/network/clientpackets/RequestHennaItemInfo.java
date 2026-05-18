package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.data.xml.HennaData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.henna.Henna;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.HennaItemDrawInfo;

public class RequestHennaItemInfo extends ClientPacket
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
		if (player != null)
		{
			Henna henna = HennaData.getInstance().getHennaByDyeId(this._symbolId);
			if (henna == null)
			{
				if (this._symbolId != 0)
				{
					PacketLogger.warning(this.getClass().getSimpleName() + ": Invalid Henna Id: " + this._symbolId + " from " + player);
				}

				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else
			{
				player.sendPacket(new HennaItemDrawInfo(henna, player));
			}
		}
	}
}
