package net.sf.l2jdev.gameserver.network.serverpackets.enchant.single;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.xml.EnchantItemData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.EnchantItemRequest;
import net.sf.l2jdev.gameserver.model.actor.stat.PlayerStat;
import net.sf.l2jdev.gameserver.model.item.enchant.EnchantScroll;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.item.type.CrystalType;
import net.sf.l2jdev.gameserver.model.stats.Stat;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ChangedEnchantTargetItemProbabilityList extends ServerPacket
{
	private final Player _player;
	private final boolean _isMulti;

	public ChangedEnchantTargetItemProbabilityList(Player player, Boolean isMulti)
	{
		this._player = player;
		this._isMulti = isMulti;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		EnchantItemRequest request = this._player.getRequest(EnchantItemRequest.class);
		if (request != null)
		{
			if ((this._isMulti || request.getEnchantingItem() != null) && !request.isProcessing() && request.getEnchantingScroll() != null)
			{
				int count = 1;
				if (this._isMulti)
				{
					count = request.getMultiEnchantingItemsCount();
				}

				ServerPackets.EX_CHANGED_ENCHANT_TARGET_ITEM_PROB_LIST.writeId(this, buffer);
				buffer.writeInt(count);

				for (int i = 1; i <= count; i++)
				{
					double baseRate;
					double passiveRate;
					if (this._isMulti && request.getMultiEnchantingItemsBySlot(i) == 0)
					{
						baseRate = 0.0;
						passiveRate = 0.0;
					}
					else
					{
						baseRate = this.getBaseRate(request, i);
						passiveRate = this.getPassiveRate(request, i);
					}

					double passiveBaseRate = 0.0;
					double supportRate = this.getSupportRate(request);
					if (passiveRate != 0.0)
					{
						passiveBaseRate = baseRate * passiveRate / 10000.0;
					}

					double totalRate = baseRate + supportRate + passiveBaseRate;
					if (totalRate >= 10000.0)
					{
						totalRate = 10000.0;
					}

					if (!this._isMulti)
					{
						buffer.writeInt(request.getEnchantingItem().getObjectId());
					}
					else
					{
						buffer.writeInt(request.getMultiEnchantingItemsBySlot(i));
					}

					buffer.writeInt((int) totalRate);
					buffer.writeInt((int) baseRate);
					buffer.writeInt((int) supportRate);
					buffer.writeInt((int) passiveBaseRate);
					buffer.writeInt(0);
				}
			}
		}
	}

	private int getBaseRate(EnchantItemRequest request, int iteration)
	{
		EnchantScroll enchantScroll = EnchantItemData.getInstance().getEnchantScroll(request.getEnchantingScroll());
		return enchantScroll == null ? 0 : (int) Math.min(100.0, enchantScroll.getChance(this._player, this._isMulti ? this._player.getInventory().getItemByObjectId(request.getMultiEnchantingItemsBySlot(iteration)) : request.getEnchantingItem()) + enchantScroll.getBonusRate()) * 100;
	}

	private int getSupportRate(EnchantItemRequest request)
	{
		double supportRate = 0.0;
		if (!this._isMulti && request.getSupportItem() != null)
		{
			supportRate = EnchantItemData.getInstance().getSupportItem(request.getSupportItem()).getBonusRate();
			supportRate *= 100.0;
		}

		return (int) supportRate;
	}

	private int getPassiveRate(EnchantItemRequest request, int iteration)
	{
		double passiveRate = 0.0;
		PlayerStat stat = this._player.getStat();
		if (stat.getValue(Stat.ENCHANT_RATE) != 0.0)
		{
			if (!this._isMulti)
			{
				int crystalLevel = request.getEnchantingItem().getTemplate().getCrystalType().getLevel();
				if (crystalLevel != CrystalType.NONE.getLevel() && crystalLevel != CrystalType.EVENT.getLevel())
				{
					passiveRate = stat.getValue(Stat.ENCHANT_RATE);
					passiveRate *= 100.0;
				}
				else
				{
					passiveRate = 0.0;
				}
			}
			else
			{
				Item item = this._player.getInventory().getItemByObjectId(request.getMultiEnchantingItemsBySlot(iteration));
				if (item == null)
				{
					return 0;
				}

				int crystalLevel = item.getTemplate().getCrystalType().getLevel();
				if (crystalLevel != CrystalType.NONE.getLevel() && crystalLevel != CrystalType.EVENT.getLevel())
				{
					passiveRate = stat.getValue(Stat.ENCHANT_RATE);
					passiveRate *= 100.0;
				}
				else
				{
					passiveRate = 0.0;
				}
			}
		}

		return (int) passiveRate;
	}
}
