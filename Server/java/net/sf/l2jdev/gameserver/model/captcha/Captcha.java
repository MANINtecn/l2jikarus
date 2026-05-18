package net.sf.l2jdev.gameserver.model.captcha;

public class Captcha
{
	private final int _code;
	private final byte[] _data;
	private final int _id;

	public Captcha(int id, int code, byte[] data)
	{
		this._id = id;
		this._code = code;
		this._data = data;
	}

	public int getCode()
	{
		return this._code;
	}

	public byte[] getData()
	{
		return this._data;
	}

	public int getId()
	{
		return this._id;
	}
}
