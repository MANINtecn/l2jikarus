package net.sf.l2jdev.gameserver.data.holders;

public class RelicEnchantHolder
{
	private final int _enchantLevel;
	private final int _skillId;
	private final int _skillLevel;
	private final int _combatPower;

	public RelicEnchantHolder(int enchantLevel, int skillId, int skillLevel, int combatPower)
	{
		this._enchantLevel = enchantLevel;
		this._skillId = skillId;
		this._skillLevel = skillLevel;
		this._combatPower = combatPower;
	}

	public int getSkillId()
	{
		return this._skillId;
	}

	public int getEnchantLevel()
	{
		return this._enchantLevel;
	}

	public int getSkillLevel()
	{
		return this._skillLevel;
	}

	public int getCombatPower()
	{
		return this._combatPower;
	}
}
