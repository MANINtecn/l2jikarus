package net.sf.l2jdev.gameserver.model.actor.enums.player;

import net.sf.l2jdev.gameserver.model.interfaces.IUpdateTypeComponent;

public enum GroupType implements IUpdateTypeComponent
{
	NONE(1),
	PARTY(2),
	COMMAND_CHANNEL(4);

	private final int _mask;

	private GroupType(int mask)
	{
		this._mask = mask;
	}

	@Override
	public int getMask()
	{
		return this._mask;
	}

	public static GroupType getByMask(int flag)
	{
		for (GroupType type : values())
		{
			if (type.getMask() == flag)
			{
				return type;
			}
		}

		return null;
	}
}
