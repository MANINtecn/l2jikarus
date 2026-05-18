package net.sf.l2jdev.gameserver.model.item.enums;

public enum BlackCouponRestoreCategory
{
	WEAPON(5, 98022),
	ARMOR(6, 98022),
	BOSS_ACCESSORIES(7, 98022),
	MISC(8, 98022);

	final int _id;
	final int _couponId;

	private BlackCouponRestoreCategory(int id, int couponId)
	{
		this._id = id;
		this._couponId = couponId;
	}

	public int getCategoryID()
	{
		return this._id;
	}

	public int getCouponId()
	{
		return this._couponId;
	}

	public static BlackCouponRestoreCategory getCategoryById(int id)
	{
		for (BlackCouponRestoreCategory category : values())
		{
			if (category.getCategoryID() == id)
			{
				return category;
			}
		}

		return null;
	}
}
