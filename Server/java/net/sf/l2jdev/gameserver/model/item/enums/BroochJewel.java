package net.sf.l2jdev.gameserver.model.item.enums;

public enum BroochJewel
{
	RUBY_LV1(70451, 17817, 1, 0.02, true, false),
	RUBY_LV2(70452, 17817, 1, 0.03, true, false),
	RUBY_LV3(70453, 17817, 1, 0.05, true, false),
	RUBY_LV4(70454, 17817, 1, 0.08, true, false),
	RUBY_LV5(70455, 17817, 1, 0.16, true, false),
	GREATER_RUBY_LV1(71368, 17817, 1, 0.17, true, false),
	GREATER_RUBY_LV2(71369, 17817, 1, 0.18, true, false),
	GREATER_RUBY_LV3(71370, 17817, 1, 0.19, true, false),
	GREATER_RUBY_LV4(71371, 17817, 1, 0.2, true, false),
	GREATER_RUBY_LV5(71372, 17817, 1, 0.2, true, false),
	RUBY_LV1_2(90328, 59150, 1, 0.02, true, false),
	RUBY_LV2_2(90329, 59150, 1, 0.03, true, false),
	RUBY_LV3_2(90330, 59150, 1, 0.05, true, false),
	RUBY_LV4_2(90331, 59150, 1, 0.08, true, false),
	RUBY_LV5_2(90332, 59150, 1, 0.16, true, false),
	GREATER_RUBY_LV1_2(91320, 59150, 1, 0.17, true, false),
	GREATER_RUBY_LV2_2(91321, 59150, 1, 0.18, true, false),
	GREATER_RUBY_LV3_2(91322, 59150, 1, 0.19, true, false),
	GREATER_RUBY_LV4_2(91323, 59150, 1, 0.2, true, false),
	GREATER_RUBY_LV5_2(91324, 59150, 1, 0.2, true, false),
	ONYX_LV1(92066, 50198, 1, 0.02, true, true),
	ONYX_LV2(92067, 50198, 2, 0.03, true, true),
	ONYX_LV3(92068, 50198, 3, 0.05, true, true),
	ONYX_LV4(92069, 50198, 4, 0.08, true, true),
	ONYX_LV5(94521, 50198, 5, 0.12, true, true),
	ONYX_LV6(92070, 50198, 6, 0.16, true, true),
	ONYX_LV7(92071, 50198, 7, 0.2, true, true),
	ONYX_LV8(92072, 50198, 8, 0.25, true, true),
	SAPPHIRE_LV1(70456, 17821, 1, 0.02, false, true),
	SAPPHIRE_LV2(70457, 17821, 1, 0.03, false, true),
	SAPPHIRE_LV3(70458, 17821, 1, 0.05, false, true),
	SAPPHIRE_LV4(70459, 17821, 1, 0.08, false, true),
	SAPPHIRE_LV5(70460, 17821, 1, 0.16, false, true),
	GREATER_SAPPHIRE_LV1(71373, 17821, 1, 0.17, false, true),
	GREATER_SAPPHIRE_LV2(71374, 17821, 1, 0.18, false, true),
	GREATER_SAPPHIRE_LV3(71375, 17821, 1, 0.19, false, true),
	GREATER_SAPPHIRE_LV4(71376, 17821, 1, 0.2, false, true),
	GREATER_SAPPHIRE_LV5(71377, 17821, 1, 0.2, false, true),
	SAPPHIRE_LV1_2(90333, 59151, 1, 0.02, false, true),
	SAPPHIRE_LV2_2(90334, 59151, 1, 0.03, false, true),
	SAPPHIRE_LV3_2(90335, 59151, 1, 0.05, false, true),
	SAPPHIRE_LV4_2(90336, 59151, 1, 0.08, false, true),
	SAPPHIRE_LV5_2(90337, 59151, 1, 0.16, false, true),
	GREATER_SAPPHIRE_LV1_2(91325, 59151, 1, 0.17, false, true),
	GREATER_SAPPHIRE_LV2_2(91326, 59151, 1, 0.18, false, true),
	GREATER_SAPPHIRE_LV3_2(91327, 59151, 1, 0.19, false, true),
	GREATER_SAPPHIRE_LV4_2(91328, 59151, 1, 0.2, false, true),
	GREATER_SAPPHIRE_LV5_2(91329, 59151, 1, 0.2, false, true);

	private final int _itemId;
	private final int _skillId;
	private final int _skillLevel;
	private final double _bonus;
	private final boolean _isRuby;
	private final boolean _isSapphire;

	private BroochJewel(int itemId, int skillId, int skillLevel, double bonus, boolean isRuby, boolean isSapphire)
	{
		this._itemId = itemId;
		this._skillId = skillId;
		this._skillLevel = skillLevel;
		this._bonus = bonus;
		this._isRuby = isRuby;
		this._isSapphire = isSapphire;
	}

	public int getItemId()
	{
		return this._itemId;
	}

	public int getSkillId()
	{
		return this._skillId;
	}

	public int getSkillLevel()
	{
		return this._skillLevel;
	}

	public double getBonus()
	{
		return this._bonus;
	}

	public boolean isRuby()
	{
		return this._isRuby;
	}

	public boolean isSapphire()
	{
		return this._isSapphire;
	}
}
