package net.sf.l2jdev.gameserver.network.serverpackets.crossevent;

import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.events.CrossEventManager;
import net.sf.l2jdev.gameserver.model.actor.holders.player.CrossEventAdvancedRewardHolder;
import net.sf.l2jdev.gameserver.model.actor.holders.player.CrossEventRegularRewardHolder;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExCrossEventData extends ServerPacket
{
	private final List<CrossEventRegularRewardHolder> _cellRewards;
	private final List<CrossEventAdvancedRewardHolder> _advancedRewards;
	private final long _resetCostAmount;
	private final int _displayId;

	public ExCrossEventData()
	{
		CrossEventManager manager = CrossEventManager.getInstance();
		this._cellRewards = manager.getRegularRewardsList();
		this._advancedRewards = manager.getAdvancedRewardList();
		this._resetCostAmount = manager.getResetCostAmount();
		this._displayId = manager.getDisplayId();
	}

	@Override
	protected void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CROSS_EVENT_DATA.writeId(this, buffer);
		buffer.writeInt(this._cellRewards.size());

		for (CrossEventRegularRewardHolder item : this._cellRewards)
		{
			buffer.writeInt(item.cellReward());
			buffer.writeLong(item.cellAmount());
		}

		buffer.writeInt(this._advancedRewards.size());

		for (CrossEventAdvancedRewardHolder reward : this._advancedRewards)
		{
			buffer.writeInt(reward.getItemId());
			buffer.writeLong(reward.getCount());
		}

		buffer.writeByte(0);
		buffer.writeByte(1);
		buffer.writeInt(57);
		buffer.writeLong(this._resetCostAmount);
		buffer.writeInt(this._displayId);
	}
}
