package net.sf.l2jdev.gameserver.network.enums;

import net.sf.l2jdev.gameserver.model.interfaces.IUpdateTypeComponent;

public enum PartySmallWindowUpdateType implements IUpdateTypeComponent
{
	CURRENT_CP(1),
	MAX_CP(2),
	CURRENT_HP(4),
	MAX_HP(8),
	CURRENT_MP(16),
	MAX_MP(32),
	LEVEL(64),
	CLASS_ID(128),
	PARTY_SUBSTITUTE(256),
	VITALITY_POINTS(512);

	private final int _mask;

	private PartySmallWindowUpdateType(int mask)
	{
		this._mask = mask;
	}

	@Override
	public int getMask()
	{
		return this._mask;
	}
}
