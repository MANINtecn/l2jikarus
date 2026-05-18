package net.sf.l2jdev.gameserver.network.clientpackets.ranking;

import net.sf.l2jdev.gameserver.managers.GlobalVariablesManager;
import net.sf.l2jdev.gameserver.managers.RankingPowerManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.ranking.ExRankingBuffZoneNpcInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.ranking.ExRankingBuffZoneNpcPosition;

public class RequestExRankingCharSpawnBuffzoneNpc extends ClientPacket
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
			if (GlobalVariablesManager.getInstance().getLong("RANKING_POWER_COOLDOWN", 0L) > System.currentTimeMillis())
			{
				player.sendPacket(SystemMessageId.LEADER_POWER_COOLDOWN);
			}
			else if (player.isInsideZone(ZoneId.PEACE) && !player.isInStoreMode())
			{
				if (player.getAdena() < 20000000L)
				{
					player.sendPacket(SystemMessageId.NOT_ENOUGH_MONEY_TO_USE_THE_FUNCTION);
				}
				else
				{
					player.destroyItemByItemId(ItemProcessType.FEE, 57, 20000000L, player, true);
					RankingPowerManager.getInstance().activatePower(player);
					player.sendPacket(new ExRankingBuffZoneNpcPosition());
					player.sendPacket(new ExRankingBuffZoneNpcInfo());
				}
			}
			else
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_USE_LEADER_S_POWER_HERE);
			}
		}
	}
}
