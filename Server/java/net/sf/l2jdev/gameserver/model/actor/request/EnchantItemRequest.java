package net.sf.l2jdev.gameserver.model.actor.request;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;

public class EnchantItemRequest extends AbstractRequest
{
	private volatile int _enchantingItemObjectId;
	private volatile int _enchantingScrollObjectId;
	private volatile int _supportItemObjectId;
	private volatile int _enchantingItemCurrentEnchantLevel;
	private final Map<Integer, Integer> _multiEnchantingItems = new ConcurrentHashMap<>();
	private final Map<Integer, ItemHolder> _multiFailRewardItems = new ConcurrentHashMap<>();
	private final Map<Integer, int[]> _successEnchant = new ConcurrentHashMap<>();
	private final Map<Integer, Integer> _failureEnchant = new ConcurrentHashMap<>();

	public EnchantItemRequest(Player player, int enchantingScrollObjectId)
	{
		super(player);
		this._enchantingScrollObjectId = enchantingScrollObjectId;
	}

	public void setMultiSuccessEnchantList(Map<Integer, int[]> list)
	{
		this._successEnchant.putAll(list);
	}

	public void setMultiFailureEnchantList(Map<Integer, Integer> list)
	{
		this._failureEnchant.putAll(list);
	}

	public void clearMultiSuccessEnchantList()
	{
		this._successEnchant.clear();
	}

	public void clearMultiFailureEnchantList()
	{
		this._failureEnchant.clear();
	}

	public Map<Integer, int[]> getMultiSuccessEnchantList()
	{
		return this._successEnchant;
	}

	public Map<Integer, Integer> getMultiFailureEnchantList()
	{
		return this._failureEnchant;
	}

	public Item getEnchantingItem()
	{
		return this.getPlayer().getInventory().getItemByObjectId(this._enchantingItemObjectId);
	}

	public void setEnchantingItem(int objectId)
	{
		this._enchantingItemObjectId = objectId;
	}

	public Item getEnchantingScroll()
	{
		return this.getPlayer().getInventory().getItemByObjectId(this._enchantingScrollObjectId);
	}

	public void setEnchantingScroll(int objectId)
	{
		this._enchantingScrollObjectId = objectId;
	}

	public Item getSupportItem()
	{
		return this.getPlayer().getInventory().getItemByObjectId(this._supportItemObjectId);
	}

	public void setSupportItem(int objectId)
	{
		this._supportItemObjectId = objectId;
	}

	public void setEnchantLevel(int enchantLevel)
	{
		this._enchantingItemCurrentEnchantLevel = enchantLevel;
	}

	public int getEnchantLevel()
	{
		return this._enchantingItemCurrentEnchantLevel;
	}

	public void addMultiEnchantingItems(int slot, int objectId)
	{
		this._multiEnchantingItems.put(slot, objectId);
	}

	public int getMultiEnchantingItemsBySlot(int slot)
	{
		return this._multiEnchantingItems.getOrDefault(slot, -1);
	}

	public void changeMultiEnchantingItemsBySlot(int slot, int objectId)
	{
		this._multiEnchantingItems.replace(slot, objectId);
	}

	public boolean checkMultiEnchantingItemsByObjectId(int objectId)
	{
		return this._multiEnchantingItems.containsValue(objectId);
	}

	public int getMultiEnchantingItemsCount()
	{
		return this._multiEnchantingItems.size();
	}

	public void clearMultiEnchantingItemsBySlot()
	{
		this._multiEnchantingItems.clear();
	}

	public String getMultiEnchantingItemsLits()
	{
		return this._multiEnchantingItems.toString();
	}

	public void addMultiEnchantFailItems(ItemHolder itemHolder)
	{
		this._multiFailRewardItems.put(this.getMultiFailItemsCount() + 1, itemHolder);
	}

	public int getMultiFailItemsCount()
	{
		return this._multiFailRewardItems.size();
	}

	public void clearMultiFailReward()
	{
		this._multiFailRewardItems.clear();
	}

	public Map<Integer, ItemHolder> getMultiEnchantFailItems()
	{
		return this._multiFailRewardItems;
	}

	@Override
	public boolean isItemRequest()
	{
		return true;
	}

	@Override
	public boolean canWorkWith(AbstractRequest request)
	{
		return !request.isItemRequest();
	}

	@Override
	public boolean isUsing(int objectId)
	{
		return objectId > 0 && (objectId == this._enchantingItemObjectId || objectId == this._enchantingScrollObjectId || objectId == this._supportItemObjectId);
	}
}
