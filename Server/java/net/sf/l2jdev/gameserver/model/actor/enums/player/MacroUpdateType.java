package net.sf.l2jdev.gameserver.model.actor.enums.player;

public enum MacroUpdateType
{
	ADD(1),
	LIST(1),
	MODIFY(2),
	DELETE(0);

	private final int _id;

	private MacroUpdateType(int id)
	{
		this._id = id;
	}

	public int getId()
	{
		return this._id;
	}
}
