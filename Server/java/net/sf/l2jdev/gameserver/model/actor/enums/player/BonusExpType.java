package net.sf.l2jdev.gameserver.model.actor.enums.player;

public enum BonusExpType
{
	VITALITY(1),
	BUFFS(2),
	PASSIVE(3);

	private final int _id;

	private BonusExpType(int id)
	{
		this._id = id;
	}

	public int getId()
	{
		return this._id;
	}
}
