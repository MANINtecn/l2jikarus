package net.sf.l2jdev.gameserver.data.holders;

public class AdenLabSkillHolder
{
	private int _id;
	private int _lvl;

	public AdenLabSkillHolder()
	{
	}

	public AdenLabSkillHolder(int id, int lvl)
	{
		this._id = id;
		this._lvl = lvl;
	}

	public int getId()
	{
		return this._id;
	}

	public int getLvl()
	{
		return this._lvl;
	}

	public void setId(int id)
	{
		this._id = id;
	}

	public void setLvl(int lvl)
	{
		this._lvl = lvl;
	}
}
