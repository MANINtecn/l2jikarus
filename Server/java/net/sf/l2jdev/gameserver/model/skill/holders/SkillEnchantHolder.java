package net.sf.l2jdev.gameserver.model.skill.holders;

public class SkillEnchantHolder
{
	private final int _id;
	private final int _starLevel;
	private final int _maxEnchantLevel;

	public SkillEnchantHolder(int id, int starLevel, int maxEnchantLevel)
	{
		this._id = id;
		this._starLevel = starLevel;
		this._maxEnchantLevel = maxEnchantLevel;
	}

	public int getId()
	{
		return this._id;
	}

	public int getStarLevel()
	{
		return this._starLevel;
	}

	public int getMaxEnchantLevel()
	{
		return this._maxEnchantLevel;
	}
}
