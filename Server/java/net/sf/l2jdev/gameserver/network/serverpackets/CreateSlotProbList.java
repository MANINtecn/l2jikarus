package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.holders.RandomCraftRewardItemHolder;
import net.sf.l2jdev.gameserver.data.xml.RandomCraftData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class CreateSlotProbList extends ServerPacket
{
	private final RandomCraftRewardItemHolder _rewardList;
	private final int _slot;
	private final double _chance;

	public CreateSlotProbList(Player player, int slot)
	{
		this._slot = slot;
		this._rewardList = player.getRandomCraft().getRewards().get(this._slot);
		this._chance = RandomCraftData.getInstance().getRewardChance(this._rewardList.getItemId());
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CRAFT_SLOT_PROB_LIST.writeId(this, buffer);
		buffer.writeInt(this._slot);
		buffer.writeInt(1);
		buffer.writeInt(this._rewardList.getItemId());
		buffer.writeLong(this._rewardList.getItemCount());
		buffer.writeInt((int) this._chance * 1000000);
	}
}
