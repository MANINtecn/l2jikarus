package net.sf.l2jdev.gameserver.network.enums;

import net.sf.l2jdev.gameserver.model.interfaces.IUpdateTypeComponent;

public enum NpcInfoType implements IUpdateTypeComponent
{
	ID(0, 4),
	ATTACKABLE(1, 1),
	RELATIONS(2, 8),
	NAME(3, 2),
	POSITION(4, 12),
	HEADING(5, 4),
	VEHICLE_ID(6, 4),
	ATK_CAST_SPEED(7, 8),
	SPEED_MULTIPLIER(8, 8),
	EQUIPPED(9, 12),
	ALIVE(10, 1),
	RUNNING(11, 1),
	SWIM_OR_FLY(14, 1),
	TEAM(15, 1),
	ENCHANT(16, 4),
	FLYING(17, 4),
	CLONE(18, 4),
	PET_EVOLUTION_ID(19, 4),
	DISPLAY_EFFECT(22, 4),
	TRANSFORMATION(23, 4),
	CURRENT_HP(24, 8),
	CURRENT_MP(25, 4),
	MAX_HP(26, 8),
	MAX_MP(27, 4),
	SUMMONED(28, 1),
	FOLLOW_INFO(29, 8),
	TITLE(30, 2),
	NAME_NPCSTRINGID(31, 4),
	TITLE_NPCSTRINGID(32, 4),
	PVP_FLAG(33, 1),
	REPUTATION(34, 4),
	CLAN(35, 20),
	ABNORMALS(36, 4),
	VISUAL_STATE(37, 1),
	SHOW_NAME(38, 1);

	private final int _mask;
	private final int _blockLength;

	private NpcInfoType(int mask, int blockLength)
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
