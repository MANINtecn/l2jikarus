package net.sf.l2jdev.gameserver.model.item.henna;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.stats.BaseStat;

public class Henna
{
	private final int _dyeId;
	private final int _dyeItemId;
	private final int _patternLevel;
	private final Map<BaseStat, Integer> _baseStats = new EnumMap<>(BaseStat.class);
	private final int _wearFee;
	private final int _l2CoinFee;
	private final int _wearCount;
	private final int _l2CancelCoinFee;
	private final int _cancelFee;
	private final int _cancelCount;
	private final int _duration;
	private final List<Skill> _skills;
	private final Set<Integer> _wearClass;

	public Henna(StatSet set)
	{
		this._dyeId = set.getInt("dyeId");
		this._dyeItemId = set.getInt("dyeItemId");
		this._patternLevel = set.getInt("patternLevel", -1);
		this._baseStats.put(BaseStat.STR, set.getInt("str", 0));
		this._baseStats.put(BaseStat.CON, set.getInt("con", 0));
		this._baseStats.put(BaseStat.DEX, set.getInt("dex", 0));
		this._baseStats.put(BaseStat.INT, set.getInt("int", 0));
		this._baseStats.put(BaseStat.MEN, set.getInt("men", 0));
		this._baseStats.put(BaseStat.WIT, set.getInt("wit", 0));
		this._wearFee = set.getInt("wear_fee");
		this._l2CoinFee = set.getInt("l2coin_fee", 0);
		this._wearCount = set.getInt("wear_count");
		this._cancelFee = set.getInt("cancel_fee");
		this._l2CancelCoinFee = set.getInt("cancel_l2coin_fee", 0);
		this._cancelCount = set.getInt("cancel_count");
		this._duration = set.getInt("duration", -1);
		this._skills = new ArrayList<>();
		this._wearClass = new HashSet<>();
	}

	public int getDyeId()
	{
		return this._dyeId;
	}

	public int getDyeItemId()
	{
		return this._dyeItemId;
	}

	public int getBaseStats(BaseStat stat)
	{
		return !this._baseStats.containsKey(stat) ? 0 : this._baseStats.get(stat);
	}

	public Map<BaseStat, Integer> getBaseStats()
	{
		return this._baseStats;
	}

	public int getWearFee()
	{
		return this._wearFee;
	}

	public int getL2CoinFee()
	{
		return this._l2CoinFee;
	}

	public int getWearCount()
	{
		return this._wearCount;
	}

	public int getCancelFee()
	{
		return this._cancelFee;
	}

	public int getCancelL2CoinFee()
	{
		return this._l2CancelCoinFee;
	}

	public int getCancelCount()
	{
		return this._cancelCount;
	}

	public int getDuration()
	{
		return this._duration;
	}

	public void setSkills(List<Skill> skillList)
	{
		this._skills.addAll(skillList);
	}

	public List<Skill> getSkills()
	{
		return this._skills;
	}

	public Set<Integer> getAllowedWearClass()
	{
		return this._wearClass;
	}

	public boolean isAllowedClass(Player player)
	{
		return this._wearClass.isEmpty() || this._wearClass.contains(player.getPlayerClass().level());
	}

	public void setWearClassIds(List<Integer> wearClassIds)
	{
		this._wearClass.addAll(wearClassIds);
	}

	public int getPatternLevel()
	{
		return this._patternLevel;
	}
}
