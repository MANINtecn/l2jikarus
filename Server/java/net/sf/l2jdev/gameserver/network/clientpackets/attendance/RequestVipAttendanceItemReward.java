package net.sf.l2jdev.gameserver.network.clientpackets.attendance;

import java.util.List;

import net.sf.l2jdev.gameserver.config.AttendanceRewardsConfig;
import net.sf.l2jdev.gameserver.config.custom.OfflinePlayConfig;
import net.sf.l2jdev.gameserver.config.custom.OfflineTradeConfig;
import net.sf.l2jdev.gameserver.data.holders.AttendanceItemHolder;
import net.sf.l2jdev.gameserver.data.xml.AttendanceRewardData;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.holders.player.AttendanceInfoHolder;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.attendance.ExVipAttendanceList;
import net.sf.l2jdev.gameserver.network.serverpackets.attendance.ExVipAttendanceReward;

public class RequestVipAttendanceItemReward extends ClientPacket
{
	private int _day;

	@Override
	protected void readImpl()
	{
		this._day = this.readByte();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (!AttendanceRewardsConfig.ENABLE_ATTENDANCE_REWARDS)
			{
				player.sendPacket(SystemMessageId.DUE_TO_A_SYSTEM_ERROR_THE_ATTENDANCE_REWARD_CANNOT_BE_RECEIVED_PLEASE_TRY_AGAIN_LATER_BY_GOING_TO_MENU_ATTENDANCE_CHECK);
			}
			else if (AttendanceRewardsConfig.PREMIUM_ONLY_ATTENDANCE_REWARDS && !player.hasPremiumStatus())
			{
				player.sendPacket(SystemMessageId.YOUR_VIP_RANK_IS_TOO_LOW_TO_RECEIVE_THE_REWARD);
			}
			else if (AttendanceRewardsConfig.VIP_ONLY_ATTENDANCE_REWARDS && player.getVipTier() <= 0)
			{
				player.sendPacket(SystemMessageId.YOUR_VIP_RANK_IS_TOO_LOW_TO_RECEIVE_THE_REWARD);
			}
			else
			{
				AttendanceInfoHolder attendanceInfo = player.getAttendanceInfo();
				int rewardIndex = attendanceInfo.getRewardIndex();
				List<AttendanceItemHolder> rewards = AttendanceRewardData.getInstance().getRewards();
				if (this._day > 0 && this._day <= rewards.size())
				{
					for (int i = rewardIndex; i < this._day - 1; i++)
					{
						AttendanceItemHolder unreclaimedReward = rewards.get(i);
						player.addItem(ItemProcessType.REWARD, unreclaimedReward.getItemId(), unreclaimedReward.getItemCount(), player, true);
					}

					AttendanceItemHolder reward = rewards.get(this._day - 1);
					player.addItem(ItemProcessType.REWARD, reward.getItemId(), reward.getItemCount(), player, true);
					player.setAttendanceInfo(this._day);
					SystemMessage msg = new SystemMessage(SystemMessageId.YOU_CAN_GET_S1_AS_A_VIP_REWARD_FOR_USING_PA_CLICK_ON_THE_REWARD_ICON);
					msg.addInt(this._day);
					player.sendPacket(msg);
					player.sendPacket(new ExVipAttendanceReward());
					if (AttendanceRewardsConfig.ATTENDANCE_REWARDS_SHARE_ACCOUNT && (!OfflineTradeConfig.OFFLINE_DISCONNECT_SAME_ACCOUNT || !OfflinePlayConfig.OFFLINE_PLAY_DISCONNECT_SAME_ACCOUNT))
					{
						for (Player worldPlayer : World.getInstance().getPlayers())
						{
							if (worldPlayer.getAccountName().equals(player.getAccountName()))
							{
								worldPlayer.setAttendanceInfo(this._day);
								worldPlayer.sendPacket(new ExVipAttendanceList(worldPlayer));
							}
						}
					}
				}
			}
		}
	}
}
