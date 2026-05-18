package net.sf.l2jdev.gameserver.data.holders;

public class AttendanceItemHolder
{
	private final int _itemId;
	private final int _itemCount;
	private final int _highlight;

	public AttendanceItemHolder(int id, int count, int highlight)
	{
		this._itemId = id;
		this._itemCount = count;
		this._highlight = highlight;
	}

	public int getItemId()
	{
		return this._itemId;
	}

	public int getItemCount()
	{
		return this._itemCount;
	}

	public int getHighlight()
	{
		return this._highlight;
	}
}
