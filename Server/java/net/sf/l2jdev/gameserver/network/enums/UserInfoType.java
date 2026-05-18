package net.sf.l2jdev.gameserver.network.enums;

import net.sf.l2jdev.gameserver.model.interfaces.IUpdateTypeComponent;

public enum UserInfoType implements IUpdateTypeComponent
{
	RELATION(0, 4),
	BASIC_INFO(1, 23),
	BASE_STATS(2, 18),
	MAX_HPCPMP(3, 14),
	CURRENT_HPMPCP_EXP_SP(4, 39),
	ENCHANTLEVEL(5, 7),
	APPAREANCE(6, 19),
	STATUS(7, 6),
	STATS(8, 76),
	ELEMENTALS(9, 14),
	POSITION(10, 18),
	SPEED(11, 18),
	MULTIPLIER(12, 18),
	COL_RADIUS_HEIGHT(13, 18),
	ATK_ELEMENTAL(14, 5),
	CLAN(15, 32),
	SOCIAL(16, 34),
	VITA_FAME(17, 19),
	SLOTS(18, 16),
	MOVEMENTS(19, 4),
	COLOR(20, 10),
	INVENTORY_LIMIT(21, 17),
	TRUE_HERO(22, 9),
	ATT_SPIRITS(23, 34),
	RANKING(24, 6),
	STAT_POINTS(25, 28),
	STAT_ABILITIES(26, 18),
	ELIXIR_USED(27, 1),
	VANGUARD_MOUNT(28, 1),
	UNK_414(29, 1);

	private final int _mask;
	private final int _blockLength;

	private UserInfoType(int mask, int blockLength)
	{
		this._mask = mask;
		this._blockLength = blockLength;
	}

	@Override
	public int getMask()
	{
		return this._mask;
	}

	public int getBlockLength()
	{
		return this._blockLength;
	}
}
