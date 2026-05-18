package net.sf.l2jdev.gameserver.network.serverpackets.enchant;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.xml.EnchantItemData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.holders.player.ChallengePoint;
import net.sf.l2jdev.gameserver.model.actor.request.EnchantItemRequest;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.enchant.EnchantScroll;
import net.sf.l2jdev.gameserver.model.item.enchant.EnchantSupportItem;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ResetEnchantItemFailRewardInfo extends ServerPacket
{
	private final Item _enchantItem;
	private int _challengeGroup;
	private int _challengePoint;
	private ItemHolder _result;
	private Item _addedItem;

	public ResetEnchantItemFailRewardInfo(Player player)
	{
		EnchantItemRequest request = player.getRequest(EnchantItemRequest.class);
		if (request != null && request.getEnchantingItem() != null && !request.isProcessing() && request.getEnchantingScroll() != null)
		{
			EnchantItemData enchantItemData = EnchantItemData.getInstance();
			EnchantScroll enchantScroll = enchantItemData.getEnchantScroll(request.getEnchantingScroll());
			this._enchantItem = request.getEnchantingItem();
			this._addedItem = new Item(this._enchantItem.getId());
			this._addedItem.setOwnerId(player.getObjectId());
			this._addedItem.setEnchantLevel(this._enchantItem.getEnchantLevel());
			this._result = null;
			EnchantSupportItem enchantSupportItem = null;
			if (request.getSupportItem() != null)
			{
				enchantSupportItem = enchantItemData.getSupportItem(request.getSupportItem());
			}

			if (!enchantScroll.isBlessed() && (request.getSupportItem() == null || enchantSupportItem == null || !enchantSupportItem.isBlessed()))
			{
				if (enchantScroll.isBlessedDown() || enchantScroll.isCursed())
				{
					this._addedItem.setEnchantLevel(this._enchantItem.getEnchantLevel() - 1);
				}
				else if (enchantScroll.isSafe())
				{
					this._addedItem.setEnchantLevel(this._enchantItem.getEnchantLevel());
				}
				else
				{
					this._addedItem = null;
					ItemTemplate template = this._enchantItem.getTemplate();
					if (template.isCrystallizable())
					{
						this._result = new ItemHolder(template.getCrystalItemId(), Math.max(0, this._enchantItem.getCrystalCount() - (template.getCrystalCount() + 1) / 2));
					}
				}
			}
			else
			{
				this._addedItem.setEnchantLevel(0);
			}

			ChallengePoint challenge = player.getChallengeInfo();
			this._challengeGroup = challenge.getNowGroup();
			this._challengePoint = challenge.getNowPoint();
		}
		else
		{
			this._enchantItem = null;
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		if (this._enchantItem != null)
		{
			ServerPackets.EX_RES_ENCHANT_ITEM_FAIL_REWARD_INFO.writeId(this, buffer);
			buffer.writeInt(this._enchantItem.getObjectId());
			buffer.writeInt(this._challengeGroup);
			buffer.writeInt(this._challengePoint);
			if (this._result != null)
			{
				buffer.writeInt(1);
				buffer.writeInt(this._result.getId());
				buffer.writeInt((int) this._result.getCount());
			}
			else if (this._addedItem != null)
			{
				buffer.writeInt(1);
				buffer.writeInt(this._enchantItem.getId());
				buffer.writeInt(1);
			}
			else
			{
				buffer.writeInt(0);
			}
		}
	}
}
