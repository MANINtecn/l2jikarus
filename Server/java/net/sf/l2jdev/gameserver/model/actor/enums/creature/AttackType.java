package net.sf.l2jdev.gameserver.model.actor.enums.creature;

public enum AttackType
{
	MISSED(1),
	BLOCKED(2),
	CRITICAL(4),
	SHOT_USED(8);

	private final int _mask;

	private AttackType(int mask)
	{
		this._mask = mask;
	}

	public int getMask()
	{
		return this._mask;
	}
}
