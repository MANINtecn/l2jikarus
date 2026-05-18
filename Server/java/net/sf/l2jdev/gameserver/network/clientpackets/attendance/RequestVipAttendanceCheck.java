package net.sf.l2jdev.gameserver.network.clientpackets.attendance;

import net.sf.l2jdev.gameserver.config.AttendanceRewardsConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.attendance.ExVipAttendanceCheck;

public class RequestVipAttendanceCheck extends ClientPacket
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
			if (AttendanceRewardsConfig.PREMIUM_ONLY_ATTENDANCE_REWARDS && !player.hasPremiumStatus())
			{
				player.sendPacket(SystemMessageId.YOUR_VIP_RANK_IS_TOO_LOW_TO_RECEIVE_THE_REWARD);
			}
			else if (AttendanceRewardsConfig.VIP_ONLY_ATTENDANCE_REWARDS && player.getVipTier() <= 0)
			{
				player.sendPacket(SystemMessageId.YOUR_VIP_RANK_IS_TOO_LOW_TO_RECEIVE_THE_REWARD);
			}
			else if (!player.destroyItemByItemId(ItemProcessType.FEE, 91663, 100L, player, true))
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_MONEY_TO_USE_THE_FUNCTION);
			}
			else
			{
				player.sendPacket(new ExVipAttendanceCheck(true));
			}
		}
	}
}
