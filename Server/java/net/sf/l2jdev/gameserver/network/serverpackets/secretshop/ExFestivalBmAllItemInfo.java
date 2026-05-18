package net.sf.l2jdev.gameserver.network.serverpackets.secretshop;

import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.events.SecretShopEventManager;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExFestivalBmAllItemInfo extends ServerPacket
{
	private final List<SecretShopEventManager.SecretShopRewardHolder> _activeRewards;

	public ExFestivalBmAllItemInfo(List<SecretShopEventManager.SecretShopRewardHolder> activeRewards)
	{
		this._activeRewards = activeRewards;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_FESTIVAL_BM_ALL_ITEM_INFO.writeId(this, buffer);
		buffer.writeInt(20);
		buffer.writeInt(this._activeRewards.size());

		for (SecretShopEventManager.SecretShopRewardHolder reward : this._activeRewards)
		{
			if (reward.isTopGrade())
			{
				buffer.writeByte(reward.getGrade());
				buffer.writeInt(reward.getId());
				buffer.writeInt((int) reward.getCurrentAmount());
				buffer.writeInt((int) reward.getTotalAmount());
			}
		}

		for (SecretShopEventManager.SecretShopRewardHolder rewardx : this._activeRewards)
		{
			if (rewardx.isMiddleGrade())
			{
				buffer.writeByte(rewardx.getGrade());
				buffer.writeInt(rewardx.getId());
				buffer.writeInt((int) rewardx.getCurrentAmount());
				buffer.writeInt((int) rewardx.getTotalAmount());
			}
		}

		for (SecretShopEventManager.SecretShopRewardHolder rewardxx : this._activeRewards)
		{
			if (rewardxx.isLowGrade())
			{
				buffer.writeByte(rewardxx.getGrade());
				buffer.writeInt(rewardxx.getId());
				buffer.writeInt((int) rewardxx.getCurrentAmount());
				buffer.writeInt((int) rewardxx.getTotalAmount());
			}
		}
	}
}
