package net.sf.l2jdev.gameserver.network.serverpackets.attendance;

import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.holders.AttendanceItemHolder;
import net.sf.l2jdev.gameserver.data.xml.AttendanceRewardData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.holders.player.AttendanceInfoHolder;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExVipAttendanceList extends ServerPacket
{
	private final int _index;
	private final int _delayreward;
	private final boolean _available;
	private final List<AttendanceItemHolder> _rewardItems;

	public ExVipAttendanceList(Player player)
	{
		AttendanceInfoHolder attendanceInfo = player.getAttendanceInfo();
		this._index = attendanceInfo.getRewardIndex();
		this._delayreward = player.getAttendanceDelay();
		this._available = attendanceInfo.isRewardAvailable();
		this._rewardItems = AttendanceRewardData.getInstance().getRewards();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_VIP_ATTENDANCE_LIST.writeId(this, buffer);
		buffer.writeInt(this._rewardItems.size());

		for (AttendanceItemHolder reward : this._rewardItems)
		{
			buffer.writeInt(reward.getItemId());
			buffer.writeLong(reward.getItemCount());
			buffer.writeByte(reward.getHighlight());
		}

		buffer.writeInt(1);
		buffer.writeInt(this._delayreward);
		if (this._available)
		{
			buffer.writeByte(this._index + 1);
			if (this._delayreward == 0 && this._available)
			{
				buffer.writeByte(this._index + 1);
			}
			else
			{
				buffer.writeByte(this._index);
			}

			buffer.writeByte(this._index);
			buffer.writeByte(0);
			buffer.writeByte(0);
		}
		else
		{
			buffer.writeByte(this._index);
			if (this._delayreward == 0 && this._available)
			{
				buffer.writeByte(this._index + 1);
			}
			else
			{
				buffer.writeByte(this._index);
			}

			buffer.writeByte(this._index);
			buffer.writeByte(0);
			buffer.writeByte(1);
		}
	}
}
