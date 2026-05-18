package net.sf.l2jdev.gameserver.network.clientpackets.dailymission;

import java.util.Collection;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.data.xml.DailyMissionData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.holders.player.DailyMissionDataHolder;
import net.sf.l2jdev.gameserver.model.actor.request.RewardRequest;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.dailymission.ExConnectedTimeAndGettableReward;
import net.sf.l2jdev.gameserver.network.serverpackets.dailymission.ExOneDayReceiveRewardList;

public class RequestOneDayRewardReceive extends ClientPacket
{
	private int _id;

	@Override
	protected void readImpl()
	{
		this._id = this.readShort();
	}

	@Override
	protected void runImpl()
	{
		if (this.getClient().getFloodProtectors().canPerformPlayerAction())
		{
			Player player = this.getPlayer();
			if (player != null)
			{
				if (!player.hasRequest(RewardRequest.class))
				{
					player.addRequest(new RewardRequest(player));
					Collection<DailyMissionDataHolder> rewards = DailyMissionData.getInstance().getDailyMissionData(this._id);
					if (rewards != null && !rewards.isEmpty())
					{
						for (DailyMissionDataHolder holder : rewards)
						{
							if (holder.isDisplayable(player))
							{
								holder.requestReward(player);
							}
						}

						player.sendPacket(new ExOneDayReceiveRewardList(player, true));
						player.sendPacket(new ExConnectedTimeAndGettableReward(player));
						ThreadPool.schedule(() -> player.removeRequest(RewardRequest.class), 300L);
					}
					else
					{
						player.removeRequest(RewardRequest.class);
					}
				}
			}
		}
	}
}
