package net.sf.l2jdev.gameserver.data.holders;

import java.util.List;

import net.sf.l2jdev.gameserver.model.actor.enums.player.RelicGrade;

public class RelicDataHolder
{
	private final int _relicId;
	private final int _parentRelicId;
	private final RelicGrade _grade;
	private final List<RelicEnchantHolder> _enchantHolder;
	private final long _summonChance;
	private final float _compoundChanceModifier;
	private final float _compoundUpGradeChanceModifier;

	public RelicDataHolder(int relicId, int parentRelicId, RelicGrade grade, long summonChance, List<RelicEnchantHolder> enchantHolder, float compoundChanceModifier, float compoundUpGradeChanceModifier)
	{
		this._relicId = relicId;
		this._parentRelicId = parentRelicId;
		this._grade = grade;
		this._summonChance = summonChance;
		this._enchantHolder = enchantHolder;
		this._compoundChanceModifier = compoundChanceModifier;
		this._compoundUpGradeChanceModifier = compoundUpGradeChanceModifier;
	}

	public int getRelicId()
	{
		return this._relicId;
	}

	public int getParentRelicId()
	{
		return this._parentRelicId;
	}

	public RelicGrade getGrade()
	{
		return this._grade;
	}

	public int getGradeOrdinal()
	{
		return this._grade.ordinal();
	}

	public long getSummonChance()
	{
		return this._summonChance;
	}

	public List<RelicEnchantHolder> getEnchantHoldders()
	{
		return this._enchantHolder;
	}

	public RelicEnchantHolder getEnchantHolderByEnchant(int enchant)
	{
		for (RelicEnchantHolder holder : this._enchantHolder)
		{
			if (enchant == holder.getEnchantLevel())
			{
				return holder;
			}
		}

		return this._enchantHolder.getFirst();
	}

	public float getCompoundChanceModifier()
	{
		return this._compoundChanceModifier;
	}

	public float getCompoundUpGradeChanceModifier()
	{
		return this._compoundUpGradeChanceModifier;
	}
}
