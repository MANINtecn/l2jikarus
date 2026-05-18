package net.sf.l2jdev.gameserver.data.holders;

import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;

public class PetExtractionHolder
{
	private final int _petId;
	private final int _petLevel;
	private final long _extractExp;
	private final int _extractItem;
	private final ItemHolder _defaultCost;
	private final ItemHolder _extractCost;

	public PetExtractionHolder(int petId, int petLevel, long extractExp, int extractItem, ItemHolder defaultCost, ItemHolder extractCost)
	{
		this._petId = petId;
		this._petLevel = petLevel;
		this._extractExp = extractExp;
		this._extractItem = extractItem;
		this._defaultCost = defaultCost;
		this._extractCost = extractCost;
	}

	public int getPetId()
	{
		return this._petId;
	}

	public int getPetLevel()
	{
		return this._petLevel;
	}

	public long getExtractExp()
	{
		return this._extractExp;
	}

	public int getExtractItem()
	{
		return this._extractItem;
	}

	public ItemHolder getDefaultCost()
	{
		return this._defaultCost;
	}

	public ItemHolder getExtractCost()
	{
		return this._extractCost;
	}
}
