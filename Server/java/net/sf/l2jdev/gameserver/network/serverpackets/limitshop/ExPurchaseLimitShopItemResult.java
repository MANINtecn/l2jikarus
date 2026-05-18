package net.sf.l2jdev.gameserver.network.serverpackets.limitshop;

import java.util.Collection;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.holders.LimitShopRandomCraftReward;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExPurchaseLimitShopItemResult extends ServerPacket
{
	private final int _category;
	private final int _productId;
	private final boolean _isSuccess;
	private final int _remainingInfo;
	private final Collection<LimitShopRandomCraftReward> _rewards;

	public ExPurchaseLimitShopItemResult(boolean isSuccess, int category, int productId, int remainingInfo, Collection<LimitShopRandomCraftReward> rewards)
	{
		this._isSuccess = isSuccess;
		this._category = category;
		this._productId = productId;
		this._remainingInfo = remainingInfo;
		this._rewards = rewards;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PURCHASE_LIMIT_SHOP_ITEM_BUY.writeId(this, buffer);
		buffer.writeByte(this._isSuccess ? 0 : 1);
		buffer.writeByte(this._category);
		buffer.writeInt(this._productId);
		buffer.writeInt(this._rewards.size());

		for (LimitShopRandomCraftReward entry : this._rewards)
		{
			buffer.writeByte(entry.getRewardIndex());
			buffer.writeInt(entry.getItemId());
			buffer.writeInt(entry.getCount().get());
		}

		buffer.writeInt(this._remainingInfo);
	}
}
