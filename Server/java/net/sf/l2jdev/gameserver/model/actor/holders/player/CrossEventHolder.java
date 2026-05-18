package net.sf.l2jdev.gameserver.model.actor.holders.player;

public class CrossEventHolder
{
	private final int _cellId;
	private int _horizontal;
	private int _vertical;

	public CrossEventHolder(int cellId, int horizontal, int vertical)
	{
		this._cellId = cellId;
		this._vertical = vertical;
		this._horizontal = horizontal;
	}

	public CrossEventHolder(int cellId)
	{
		this._cellId = cellId;
		switch (cellId)
		{
			case 0:
				this._vertical = 0;
				this._horizontal = 0;
				break;
			case 1:
				this._vertical = 0;
				this._horizontal = 1;
				break;
			case 2:
				this._vertical = 0;
				this._horizontal = 2;
				break;
			case 3:
				this._vertical = 0;
				this._horizontal = 3;
				break;
			case 4:
				this._vertical = 1;
				this._horizontal = 0;
				break;
			case 5:
				this._vertical = 1;
				this._horizontal = 1;
				break;
			case 6:
				this._vertical = 1;
				this._horizontal = 2;
				break;
			case 7:
				this._vertical = 1;
				this._horizontal = 3;
				break;
			case 8:
				this._vertical = 2;
				this._horizontal = 0;
				break;
			case 9:
				this._vertical = 2;
				this._horizontal = 1;
				break;
			case 10:
				this._vertical = 2;
				this._horizontal = 2;
				break;
			case 11:
				this._vertical = 2;
				this._horizontal = 3;
				break;
			case 12:
				this._vertical = 3;
				this._horizontal = 0;
				break;
			case 13:
				this._vertical = 3;
				this._horizontal = 1;
				break;
			case 14:
				this._vertical = 3;
				this._horizontal = 2;
				break;
			case 15:
				this._vertical = 3;
				this._horizontal = 3;
		}
	}

	public int cellId()
	{
		return this._cellId;
	}

	public int getVertical()
	{
		return this._vertical;
	}

	public int getHorizontal()
	{
		return this._horizontal;
	}
}
