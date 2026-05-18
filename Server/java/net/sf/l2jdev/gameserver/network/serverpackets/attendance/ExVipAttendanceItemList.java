package net.sf.l2jdev.gameserver.network.serverpackets.attendance;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.holders.AttendanceItemHolder;
import net.sf.l2jdev.gameserver.data.xml.AttendanceRewardData;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExVipAttendanceItemList extends ServerPacket
{
	@Override
	protected void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_VIP_ATTENDANCE_ITEMLIST.writeId(this, buffer);
		buffer.writeByte(0);
		buffer.writeByte(0);
		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeByte(0);
		buffer.writeByte(0);
		buffer.writeByte(0);
		buffer.writeByte(AttendanceRewardData.getInstance().getRewardsCount());
		int rewardCounter = 0;

		for (AttendanceItemHolder reward : AttendanceRewardData.getInstance().getRewards())
		{
			rewardCounter++;
			buffer.writeInt(reward.getItemId());
			buffer.writeLong(reward.getItemCount());
			buffer.writeByte(reward.getHighlight());
			buffer.writeByte(rewardCounter % 7 == 0);
		}

		buffer.writeByte(0);
		buffer.writeInt(0);
	}
}
