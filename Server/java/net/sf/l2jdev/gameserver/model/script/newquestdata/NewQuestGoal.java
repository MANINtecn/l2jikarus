package net.sf.l2jdev.gameserver.model.script.newquestdata;

public class NewQuestGoal
{
	private final int _itemId;
	private final int _count;
	private final String _goalMessage;

	public NewQuestGoal(int itemId, int count, String goalMessage)
	{
		this._itemId = itemId;
		this._count = count;
		this._goalMessage = goalMessage;
	}

	public int getItemId()
	{
		return this._itemId;
	}

	public int getCount()
	{
		return this._count;
	}

	public String getMessage()
	{
		return this._goalMessage;
	}
}
