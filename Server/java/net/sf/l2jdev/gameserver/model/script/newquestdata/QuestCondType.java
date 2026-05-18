package net.sf.l2jdev.gameserver.model.script.newquestdata;

public enum QuestCondType
{
	NONE(0),
	STARTED(1),
	ACT(2),
	DONE(3);

	private final int _id;

	private QuestCondType(int id)
	{
		this._id = id;
	}

	public int getId()
	{
		return this._id;
	}
}
