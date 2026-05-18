package net.sf.l2jdev.gameserver.network.serverpackets.achievementbox;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.AchievementBoxConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExSteadyBoxUiInit extends ServerPacket
{
	private static final int[] OPEN_PRICE = new int[]
	{
		500,
		1000,
		1500
	};
	private static final int[] WAIT_TIME = new int[]
	{
		0,
		60,
		180,
		360,
		540
	};
	private static final int[] TIME_PRICE = new int[]
	{
		100,
		500,
		1000,
		1500,
		2000
	};
	private final Player _player;

	public ExSteadyBoxUiInit(Player player)
	{
		this._player = player;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_STEADY_BOX_UI_INIT.writeId(this, buffer);
		buffer.writeInt(AchievementBoxConfig.ACHIEVEMENT_BOX_POINTS_FOR_REWARD);
		buffer.writeInt(AchievementBoxConfig.ACHIEVEMENT_BOX_PVP_POINTS_FOR_REWARD);
		if (AchievementBoxConfig.ENABLE_ACHIEVEMENT_PVP)
		{
			buffer.writeInt(2);
		}
		else
		{
			buffer.writeInt(0);
		}

		buffer.writeInt(0);
		buffer.writeInt(this._player.getAchievementBox().pvpEndDate());
		buffer.writeInt(OPEN_PRICE.length);

		for (int i = 0; i < OPEN_PRICE.length; i++)
		{
			buffer.writeInt(i + 1);
			buffer.writeInt(91663);
			buffer.writeLong(OPEN_PRICE[i]);
		}

		buffer.writeInt(TIME_PRICE.length);

		for (int i = 0; i < TIME_PRICE.length; i++)
		{
			buffer.writeInt(WAIT_TIME[i]);
			buffer.writeInt(91663);
			buffer.writeLong(TIME_PRICE[i]);
		}

		int rewardTimeStage = (int) (this._player.getAchievementBox().getBoxOpenTime() - System.currentTimeMillis()) / 1000;
		buffer.writeInt(rewardTimeStage > 0 ? rewardTimeStage : 0);
	}
}
