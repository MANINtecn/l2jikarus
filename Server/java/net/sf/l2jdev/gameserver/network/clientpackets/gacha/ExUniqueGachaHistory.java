package net.sf.l2jdev.gameserver.network.clientpackets.gacha;

import java.util.List;

import net.sf.l2jdev.gameserver.managers.events.UniqueGachaManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.holders.GachaItemTimeStampHolder;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.gacha.UniqueGachaHistory;

public class ExUniqueGachaHistory extends ClientPacket
{
	@Override
	protected void readImpl()
	{
		this.readByte();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			List<GachaItemTimeStampHolder> itemHistory = UniqueGachaManager.getInstance().getGachaCharacterHistory(player);
			player.sendPacket(new UniqueGachaHistory(itemHistory));
		}
	}
}
