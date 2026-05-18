package net.sf.l2jdev.gameserver.model;

public class TeleportLocation
{
	private int _teleId;
	private int _locX;
	private int _locY;
	private int _locZ;
	private int _price;
	private boolean _forNoble;
	private int _itemId;

	public void setTeleId(int id)
	{
		this._teleId = id;
	}

	public void setLocX(int locX)
	{
		this._locX = locX;
	}

	public void setLocY(int locY)
	{
		this._locY = locY;
	}

	public void setLocZ(int locZ)
	{
		this._locZ = locZ;
	}

	public void setPrice(int price)
	{
		this._price = price;
	}

	public void setForNoble(boolean value)
	{
		this._forNoble = value;
	}

	public void setItemId(int value)
	{
		this._itemId = value;
	}

	public int getTeleId()
	{
		return this._teleId;
	}

	public int getLocX()
	{
		return this._locX;
	}

	public int getLocY()
	{
		return this._locY;
	}

	public int getLocZ()
	{
		return this._locZ;
	}

	public int getPrice()
	{
		return this._price;
	}

	public boolean isForNoble()
	{
		return this._forNoble;
	}

	public int getItemId()
	{
		return this._itemId;
	}
}
