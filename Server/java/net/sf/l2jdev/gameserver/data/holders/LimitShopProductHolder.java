package net.sf.l2jdev.gameserver.data.holders;

public class LimitShopProductHolder
{
	private final int _id;
	private final int _category;
	private final int _minLevel;
	private final int _maxLevel;
	private final int[] _ingredientIds;
	private final long[] _ingredientQuantities;
	private final int[] _ingredientEnchants;
	private final int _productionId;
	private final long _count;
	private final float _chance;
	private final boolean _announce;
	private final int _enchant;
	private final int _productionId2;
	private final long _count2;
	private final float _chance2;
	private final boolean _announce2;
	private final int _productionId3;
	private final long _count3;
	private final float _chance3;
	private final boolean _announce3;
	private final int _productionId4;
	private final long _count4;
	private final float _chance4;
	private final boolean _announce4;
	private final int _productionId5;
	private final long _count5;
	private final boolean _announce5;
	private final int _accountDailyLimit;
	private final int _accountWeeklyLimit;
	private final int _accountMonthlyLimit;
	private final int _accountBuyLimit;

	public LimitShopProductHolder(int id, int category, int minLevel, int maxLevel, int[] ingredientIds, long[] ingredientQuantities, int[] ingredientEnchants, int productionId, long count, float chance, boolean announce, int enchant, int productionId2, long count2, float chance2, boolean announce2, int productionId3, long count3, float chance3, boolean announce3, int productionId4, long count4, float chance4, boolean announce4, int productionId5, long count5, boolean announce5, int accountDailyLimit, int accountWeeklyLimit, int accountMonthlyLimit, int accountBuyLimit)
	{
		this._id = id;
		this._category = category;
		this._minLevel = minLevel;
		this._maxLevel = maxLevel;
		this._ingredientIds = ingredientIds;
		this._ingredientQuantities = ingredientQuantities;
		this._ingredientEnchants = ingredientEnchants;
		this._productionId = productionId;
		this._count = count;
		this._chance = chance;
		this._announce = announce;
		this._enchant = enchant;
		this._productionId2 = productionId2;
		this._count2 = count2;
		this._chance2 = chance2;
		this._announce2 = announce2;
		this._productionId3 = productionId3;
		this._count3 = count3;
		this._chance3 = chance3;
		this._announce3 = announce3;
		this._productionId4 = productionId4;
		this._count4 = count4;
		this._chance4 = chance4;
		this._announce4 = announce4;
		this._productionId5 = productionId5;
		this._count5 = count5;
		this._announce5 = announce5;
		this._accountDailyLimit = accountDailyLimit;
		this._accountWeeklyLimit = accountWeeklyLimit;
		this._accountMonthlyLimit = accountMonthlyLimit;
		this._accountBuyLimit = accountBuyLimit;
	}

	public int getId()
	{
		return this._id;
	}

	public int getCategory()
	{
		return this._category;
	}

	public int getMinLevel()
	{
		return this._minLevel;
	}

	public int getMaxLevel()
	{
		return this._maxLevel;
	}

	public int[] getIngredientIds()
	{
		return this._ingredientIds;
	}

	public long[] getIngredientQuantities()
	{
		return this._ingredientQuantities;
	}

	public int[] getIngredientEnchants()
	{
		return this._ingredientEnchants;
	}

	public int getProductionId()
	{
		return this._productionId;
	}

	public long getCount()
	{
		return this._count;
	}

	public float getChance()
	{
		return this._chance;
	}

	public boolean isAnnounce()
	{
		return this._announce;
	}

	public int getEnchant()
	{
		return this._enchant;
	}

	public int getProductionId2()
	{
		return this._productionId2;
	}

	public long getCount2()
	{
		return this._count2;
	}

	public float getChance2()
	{
		return this._chance2;
	}

	public boolean isAnnounce2()
	{
		return this._announce2;
	}

	public int getProductionId3()
	{
		return this._productionId3;
	}

	public long getCount3()
	{
		return this._count3;
	}

	public float getChance3()
	{
		return this._chance3;
	}

	public boolean isAnnounce3()
	{
		return this._announce3;
	}

	public int getProductionId4()
	{
		return this._productionId4;
	}

	public long getCount4()
	{
		return this._count4;
	}

	public float getChance4()
	{
		return this._chance4;
	}

	public boolean isAnnounce4()
	{
		return this._announce4;
	}

	public int getProductionId5()
	{
		return this._productionId5;
	}

	public long getCount5()
	{
		return this._count5;
	}

	public boolean isAnnounce5()
	{
		return this._announce5;
	}

	public int getAccountDailyLimit()
	{
		return this._accountDailyLimit;
	}

	public int getAccountWeeklyLimit()
	{
		return this._accountWeeklyLimit;
	}

	public int getAccountMonthlyLimit()
	{
		return this._accountMonthlyLimit;
	}

	public int getAccountBuyLimit()
	{
		return this._accountBuyLimit;
	}
}
