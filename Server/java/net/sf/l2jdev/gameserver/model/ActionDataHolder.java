package net.sf.l2jdev.gameserver.model;

public class ActionDataHolder
{
	private final int _id;
	private final String _handler;
	private final int _optionId;

	public ActionDataHolder(StatSet set)
	{
		this._id = set.getInt("id");
		this._handler = set.getString("handler");
		this._optionId = set.getInt("option", 0);
	}

	public int getId()
	{
		return this._id;
	}

	public String getHandler()
	{
		return this._handler;
	}

	public int getOptionId()
	{
		return this._optionId;
	}
}
