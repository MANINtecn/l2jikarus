package net.sf.l2jdev.gameserver.network.clientpackets.dailymission;

import net.sf.l2jdev.gameserver.data.xml.MissionLevel;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.holders.player.MissionLevelPlayerDataHolder;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.dailymission.ExOneDayReceiveRewardList;

public class RequestMissionLevelJumpLevel extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		if (this.getClient().getFloodProtectors().canPerformPlayerAction())
		{
			Player player = this.getPlayer();
			if (player != null)
			{
				if (player.destroyItemByItemId(ItemProcessType.FEE, 91663, 1000L, player, false))
				{
					MissionLevelPlayerDataHolder info = player.getMissionLevelProgress();
					info.setCurrentLevel(30);
					player.getVariables().set("MISSION_LEVEL_PROGRESS_" + MissionLevel.getInstance().getCurrentSeason(), info.getVariablesFromInfo());
					player.sendPacket(new ExOneDayReceiveRewardList(player, true));
				}
				else
				{
					player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_L2_COINS_ADD_MORE_L2_COINS_AND_TRY_AGAIN);
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
			}
		}
	}
}
