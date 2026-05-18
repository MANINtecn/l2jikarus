package net.sf.l2jdev.gameserver.network.serverpackets.dailymission;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.xml.DailyMissionData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExConnectedTimeAndGettableReward extends ServerPacket
{
	private final int _oneDayRewardAvailableCount;

	public ExConnectedTimeAndGettableReward(Player player)
	{
		this._oneDayRewardAvailableCount = (int) DailyMissionData.getInstance().getDailyMissionData(player).stream().filter(d -> d.getStatus(player) == 1).count();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		if (DailyMissionData.getInstance().isAvailable())
		{
			ServerPackets.EX_ONE_DAY_REWARD_INFO.writeId(this, buffer);
			buffer.writeInt(0);
			buffer.writeInt(this._oneDayRewardAvailableCount);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
		}
	}
}
