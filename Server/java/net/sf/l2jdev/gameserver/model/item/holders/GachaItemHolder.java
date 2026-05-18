package net.sf.l2jdev.gameserver.model.item.holders;

import net.sf.l2jdev.gameserver.model.item.enums.UniqueGachaRank;

public class GachaItemHolder extends ItemHolder
{
	private final int _itemChance;
	private final int _enchantLevel;
	private final UniqueGachaRank _rank;

	public GachaItemHolder(int itemId, long itemCount, int itemChance, int enchantLevel, UniqueGachaRank rank)
	{
		super(itemId, itemCount);
		this._itemChance = itemChance;
		this._enchantLevel = enchantLevel;
		this._rank = rank;
	}

	public int getItemChance()
	{
		return this._itemChance;
	}

	public int getEnchantLevel()
	{
		return this._enchantLevel;
	}

	public UniqueGachaRank getRank()
	{
		return this._rank;
	}
}
