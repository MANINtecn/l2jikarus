package net.sf.l2jdev.gameserver.network.serverpackets.achievementbox;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.holders.player.AchievementBoxHolder;
import net.sf.l2jdev.gameserver.model.actor.holders.player.AchievementBoxInfoHolder;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExSteadyAllBoxUpdate extends ServerPacket
{
	private final AchievementBoxHolder _achievementBox;

	public ExSteadyAllBoxUpdate(Player player)
	{
		this._achievementBox = player.getAchievementBox();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_STEADY_ALL_BOX_UPDATE.writeId(this, buffer);
		buffer.writeInt(this._achievementBox.getMonsterPoints());
		buffer.writeInt(this._achievementBox.getPvpPoints());
		buffer.writeInt(this._achievementBox.getBoxOwned());

		for (int i = 1; i <= this._achievementBox.getBoxOwned(); i++)
		{
			AchievementBoxInfoHolder boxholder = this._achievementBox.getAchievementBox().get(i - 1);
			buffer.writeInt(i);
			buffer.writeInt(boxholder.getState().ordinal());
			buffer.writeInt(boxholder.getType().ordinal());
		}

		int rewardTimeStage = (int) ((this._achievementBox.getBoxOpenTime() - System.currentTimeMillis()) / 1000L);
		buffer.writeInt(rewardTimeStage > 0 ? rewardTimeStage : 0);
	}
}
