package net.sf.l2jdev.gameserver.data.holders;

public class RelicSummonCategoryHolder
{
	private final int _categoryId;
	private final int _priceId;
	private final long _priceAmount;
	private final int _summonCount;

	public RelicSummonCategoryHolder(int categoryId, int priceId, long priceAmount, int summonCount)
	{
		this._categoryId = categoryId;
		this._priceId = priceId;
		this._priceAmount = priceAmount;
		this._summonCount = summonCount;
	}

	public int getCategoryId()
	{
		return this._categoryId;
	}

	public int getPriceId()
	{
		return this._priceId;
	}

	public long getPriceCount()
	{
		return this._priceAmount;
	}

	public int getSummonCount()
	{
		return this._summonCount;
	}
}
