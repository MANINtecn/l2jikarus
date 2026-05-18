package net.sf.l2jdev.gameserver.model;

public class ActionKey
{
	private final int _cat;
	private int _cmd = 0;
	private int _key = 0;
	private int _tgKey1 = 0;
	private int _tgKey2 = 0;
	private int _show = 1;

	public ActionKey(int cat)
	{
		this._cat = cat;
	}

	public ActionKey(int cat, int cmd, int key, int tgKey1, int tgKey2, int show)
	{
		this._cat = cat;
		this._cmd = cmd;
		this._key = key;
		this._tgKey1 = tgKey1;
		this._tgKey2 = tgKey2;
		this._show = show;
	}

	public int getCategory()
	{
		return this._cat;
	}

	public int getCommandId()
	{
		return this._cmd;
	}

	public void setCommandId(int cmd)
	{
		this._cmd = cmd;
	}

	public int getKeyId()
	{
		return this._key;
	}

	public void setKeyId(int key)
	{
		this._key = key;
	}

	public int getToogleKey1()
	{
		return this._tgKey1;
	}

	public void setToogleKey1(int tKey1)
	{
		this._tgKey1 = tKey1;
	}

	public int getToogleKey2()
	{
		return this._tgKey2;
	}

	public void setToogleKey2(int tKey2)
	{
		this._tgKey2 = tKey2;
	}

	public int getShowStatus()
	{
		return this._show;
	}

	public void setShowStatus(int show)
	{
		this._show = show;
	}

	public String getSqlSaveString(int playerId, int order)
	{
		return "(" + playerId + ", " + this._cat + ", " + order + ", " + this._cmd + "," + this._key + ", " + this._tgKey1 + ", " + this._tgKey2 + ", " + this._show + ")";
	}
}
