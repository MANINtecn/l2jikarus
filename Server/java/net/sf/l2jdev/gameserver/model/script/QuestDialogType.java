package net.sf.l2jdev.gameserver.model.script;

public enum QuestDialogType
{
	NONE(0),
	START(1),
	ACCEPT(2),
	COMPLETE(3),
	END(4),
	AGAIN(5);

	private final int _id;

	private QuestDialogType(int id)
	{
		this._id = id;
	}

	public int getId()
	{
		return this._id;
	}
}
