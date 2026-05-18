package net.sf.l2jdev.gameserver.network.serverpackets.achievementbox;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.holders.player.AchievementBoxHolder;
import net.sf.l2jdev.gameserver.model.actor.holders.player.AchievementBoxInfoHolder;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExSteadyOneBoxUpdate extends ServerPacket
{
	private final AchievementBoxHolder _achievementBox;
	private final int _slotId;

	public ExSteadyOneBoxUpdate(Player player, int slotId)
	{
		this._achievementBox = player.getAchievementBox();
		this._slotId = slotId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_STEADY_ONE_BOX_UPDATE.writeId(this, buffer);
		buffer.writeInt(this._achievementBox.getMonsterPoints());
		buffer.writeInt(this._achievementBox.getPvpPoints());
		AchievementBoxInfoHolder boxholder = this._achievementBox.getAchievementBox().get(this._slotId - 1);
		buffer.writeInt(this._slotId);
		buffer.writeInt(boxholder.getState().ordinal());
		buffer.writeInt(boxholder.getType().ordinal());
		int rewardTimeStage = (int) ((this._achievementBox.getBoxOpenTime() - System.currentTimeMillis()) / 1000L);
		buffer.writeInt(rewardTimeStage > 0 ? rewardTimeStage : 0);
	}
}
