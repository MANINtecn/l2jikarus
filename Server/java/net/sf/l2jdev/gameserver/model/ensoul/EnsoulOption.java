package net.sf.l2jdev.gameserver.model.ensoul;

import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;

public class EnsoulOption extends SkillHolder
{
	private final int _id;
	private final String _name;
	private final String _desc;

	public EnsoulOption(int id, String name, String desc, int skillId, int skillLevel)
	{
		super(skillId, skillLevel);
		this._id = id;
		this._name = name;
		this._desc = desc;
	}

	public int getId()
	{
		return this._id;
	}

	public String getName()
	{
		return this._name;
	}

	public String getDesc()
	{
		return this._desc;
	}

	@Override
	public String toString()
	{
		return "Ensoul Id: " + this._id + " Name: " + this._name + " Desc: " + this._desc;
	}
}
