package net.sf.l2jdev.gameserver.network.serverpackets.dailymission;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.commons.time.SchedulingPattern;
import net.sf.l2jdev.gameserver.data.xml.DailyMissionData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.holders.player.DailyMissionDataHolder;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExOneDayReceiveRewardList extends ServerPacket
{
	private static final SchedulingPattern DAILY_REUSE_PATTERN = new SchedulingPattern("30 6 * * *");
	private static final SchedulingPattern WEEKLY_REUSE_PATTERN = new SchedulingPattern("30 6 * * 1");
	private static final SchedulingPattern MONTHLY_REUSE_PATTERN = new SchedulingPattern("30 6 1 * *");
	private final Player _player;
	private final Collection<DailyMissionDataHolder> _rewards;
	private final int _dayRemainTime;
	private final int _weekRemainTime;
	private final int _monthRemainTime;

	@SuppressWarnings("unchecked")
	public ExOneDayReceiveRewardList(Player player, boolean sendRewards)
	{
		this._player = player;
		this._rewards = (Collection<DailyMissionDataHolder>) (sendRewards ? DailyMissionData.getInstance().getDailyMissionData(player) : Collections.emptyList());
		this._dayRemainTime = (int) ((DAILY_REUSE_PATTERN.next(System.currentTimeMillis()) - System.currentTimeMillis()) / 1000L);
		this._weekRemainTime = (int) ((WEEKLY_REUSE_PATTERN.next(System.currentTimeMillis()) - System.currentTimeMillis()) / 1000L);
		this._monthRemainTime = (int) ((MONTHLY_REUSE_PATTERN.next(System.currentTimeMillis()) - System.currentTimeMillis()) / 1000L);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		if (DailyMissionData.getInstance().isAvailable())
		{
			ServerPackets.EX_ONE_DAY_REWARD_LIST.writeId(this, buffer);
			buffer.writeInt(this._dayRemainTime);
			buffer.writeInt(this._weekRemainTime);
			buffer.writeInt(this._monthRemainTime);
			buffer.writeByte(23);
			buffer.writeInt(this._player.getPlayerClass().getId());
			buffer.writeInt(LocalDate.now().getDayOfWeek().ordinal());
			buffer.writeInt(this._rewards.size());

			for (DailyMissionDataHolder reward : this._rewards)
			{
				buffer.writeShort(reward.getId());
				int status = reward.getStatus(this._player);
				buffer.writeByte(status);
				buffer.writeByte(reward.getRequiredCompletions() > 1);
				buffer.writeInt(reward.getParams().getInt("level", -1) == -1 ? (status == 1 ? 0 : reward.getProgress(this._player)) : this._player.getLevel());
				buffer.writeInt(reward.getRequiredCompletions());
			}
		}
	}
}
