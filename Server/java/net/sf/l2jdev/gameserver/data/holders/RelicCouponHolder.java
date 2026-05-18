package net.sf.l2jdev.gameserver.data.holders;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.l2jdev.gameserver.model.actor.enums.player.RelicGrade;

public class RelicCouponHolder
{
	private final int _itemId;
	private final int _relicId;
	private final int _summonCount;
	private final Set<Integer> _disabledIds = new HashSet<>();
	private final Map<RelicGrade, Integer> _grades = new HashMap<>();
	private final Map<Integer, Integer> _chanceRolls = new HashMap<>();

	public RelicCouponHolder(int itemId, int relicId, int summonCount)
	{
		this._itemId = itemId;
		this._relicId = relicId;
		this._summonCount = summonCount;
	}

	public RelicCouponHolder(int itemId, int summonCount, Map<RelicGrade, Integer> grades, Set<Integer> disabledIds)
	{
		this._itemId = itemId;
		this._relicId = 0;
		this._summonCount = summonCount;
		this._grades.putAll(grades);
		this._disabledIds.addAll(disabledIds);
	}

	public RelicCouponHolder(int itemId, int summonCount, Map<Integer, Integer> chanceRolls)
	{
		this._itemId = itemId;
		this._relicId = 0;
		this._summonCount = summonCount;
		this._chanceRolls.putAll(chanceRolls);
	}

	public int getItemId()
	{
		return this._itemId;
	}

	public int getRelicId()
	{
		return this._relicId;
	}

	public int getRelicSummonCount()
	{
		return this._summonCount;
	}

	public Map<RelicGrade, Integer> getCouponRelicGrades()
	{
		return this._grades;
	}

	public Collection<Integer> getDisabledIds()
	{
		return this._disabledIds;
	}

	public Map<Integer, Integer> getChanceRolls()
	{
		return this._chanceRolls;
	}
}
