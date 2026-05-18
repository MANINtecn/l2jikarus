package net.sf.l2jdev.gameserver.model.actor.transform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.item.type.WeaponType;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;
import net.sf.l2jdev.gameserver.model.stats.Stat;

public class TransformTemplate
{
	private final Float _collisionRadius;
	private final Float _collisionHeight;
	private final WeaponType _baseAttackType;
	private List<SkillHolder> _skills;
	private List<AdditionalSkillHolder> _additionalSkills;
	private List<AdditionalItemHolder> _additionalItems;
	private Map<Integer, Integer> _baseDefense;
	private Map<Integer, Double> _baseStats;
	private int[] _actions;
	private final Map<Integer, TransformLevelData> _data = new LinkedHashMap<>(100);

	public TransformTemplate(StatSet set)
	{
		this._collisionRadius = set.contains("radius") ? set.getFloat("radius") : null;
		this._collisionHeight = set.contains("height") ? set.getFloat("height") : null;
		this._baseAttackType = set.getEnum("attackType", WeaponType.class, null);
		if (set.contains("range"))
		{
			this.addStats(Stat.PHYSICAL_ATTACK_RANGE, set.getDouble("range", 0.0));
		}

		if (set.contains("randomDamage"))
		{
			this.addStats(Stat.RANDOM_DAMAGE, set.getDouble("randomDamage", 0.0));
		}

		if (set.contains("walk"))
		{
			this.addStats(Stat.WALK_SPEED, set.getDouble("walk", 0.0));
		}

		if (set.contains("run"))
		{
			this.addStats(Stat.RUN_SPEED, set.getDouble("run", 0.0));
		}

		if (set.contains("waterWalk"))
		{
			this.addStats(Stat.SWIM_WALK_SPEED, set.getDouble("waterWalk", 0.0));
		}

		if (set.contains("waterRun"))
		{
			this.addStats(Stat.SWIM_RUN_SPEED, set.getDouble("waterRun", 0.0));
		}

		if (set.contains("flyWalk"))
		{
			this.addStats(Stat.FLY_WALK_SPEED, set.getDouble("flyWalk", 0.0));
		}

		if (set.contains("flyRun"))
		{
			this.addStats(Stat.FLY_RUN_SPEED, set.getDouble("flyRun", 0.0));
		}

		if (set.contains("pAtk"))
		{
			this.addStats(Stat.PHYSICAL_ATTACK, set.getDouble("pAtk", 0.0));
		}

		if (set.contains("mAtk"))
		{
			this.addStats(Stat.MAGIC_ATTACK, set.getDouble("mAtk", 0.0));
		}

		if (set.contains("range"))
		{
			this.addStats(Stat.PHYSICAL_ATTACK_RANGE, set.getInt("range", 0));
		}

		if (set.contains("attackSpeed"))
		{
			this.addStats(Stat.PHYSICAL_ATTACK_SPEED, set.getInt("attackSpeed", 0));
		}

		if (set.contains("critRate"))
		{
			this.addStats(Stat.CRITICAL_RATE, set.getInt("critRate", 0));
		}

		if (set.contains("str"))
		{
			this.addStats(Stat.STAT_STR, set.getInt("str", 0));
		}

		if (set.contains("int"))
		{
			this.addStats(Stat.STAT_INT, set.getInt("int", 0));
		}

		if (set.contains("con"))
		{
			this.addStats(Stat.STAT_CON, set.getInt("con", 0));
		}

		if (set.contains("dex"))
		{
			this.addStats(Stat.STAT_DEX, set.getInt("dex", 0));
		}

		if (set.contains("wit"))
		{
			this.addStats(Stat.STAT_WIT, set.getInt("wit", 0));
		}

		if (set.contains("men"))
		{
			this.addStats(Stat.STAT_MEN, set.getInt("men", 0));
		}

		if (set.contains("chest"))
		{
			this.addDefense(6, set.getInt("chest", 0));
		}

		if (set.contains("legs"))
		{
			this.addDefense(11, set.getInt("legs", 0));
		}

		if (set.contains("head"))
		{
			this.addDefense(1, set.getInt("head", 0));
		}

		if (set.contains("feet"))
		{
			this.addDefense(12, set.getInt("feet", 0));
		}

		if (set.contains("gloves"))
		{
			this.addDefense(10, set.getInt("gloves", 0));
		}

		if (set.contains("underwear"))
		{
			this.addDefense(0, set.getInt("underwear", 0));
		}

		if (set.contains("cloak"))
		{
			this.addDefense(28, set.getInt("cloak", 0));
		}

		if (set.contains("rear"))
		{
			this.addDefense(8, set.getInt("rear", 0));
		}

		if (set.contains("lear"))
		{
			this.addDefense(9, set.getInt("lear", 0));
		}

		if (set.contains("rfinger"))
		{
			this.addDefense(13, set.getInt("rfinger", 0));
		}

		if (set.contains("lfinger"))
		{
			this.addDefense(14, set.getInt("lfinger", 0));
		}

		if (set.contains("neck"))
		{
			this.addDefense(4, set.getInt("neck", 0));
		}
	}

	private void addDefense(int type, int value)
	{
		if (this._baseDefense == null)
		{
			this._baseDefense = new HashMap<>();
		}

		this._baseDefense.put(type, value);
	}

	public int getDefense(int type, int defaultValue)
	{
		return this._baseDefense == null ? defaultValue : this._baseDefense.getOrDefault(type, defaultValue);
	}

	private void addStats(Stat stat, double value)
	{
		if (this._baseStats == null)
		{
			this._baseStats = new HashMap<>();
		}

		this._baseStats.put(stat.ordinal(), value);
	}

	public double getStats(Stat stat, double defaultValue)
	{
		return this._baseStats == null ? defaultValue : this._baseStats.getOrDefault(stat.ordinal(), defaultValue);
	}

	public Float getCollisionRadius()
	{
		return this._collisionRadius;
	}

	public Float getCollisionHeight()
	{
		return this._collisionHeight;
	}

	public WeaponType getBaseAttackType()
	{
		return this._baseAttackType;
	}

	public void addSkill(SkillHolder holder)
	{
		if (this._skills == null)
		{
			this._skills = new ArrayList<>();
		}

		this._skills.add(holder);
	}

	public List<SkillHolder> getSkills()
	{
		return this._skills != null ? this._skills : Collections.emptyList();
	}

	public void addAdditionalSkill(AdditionalSkillHolder holder)
	{
		if (this._additionalSkills == null)
		{
			this._additionalSkills = new ArrayList<>();
		}

		this._additionalSkills.add(holder);
	}

	public List<AdditionalSkillHolder> getAdditionalSkills()
	{
		return this._additionalSkills != null ? this._additionalSkills : Collections.emptyList();
	}

	public void addAdditionalItem(AdditionalItemHolder holder)
	{
		if (this._additionalItems == null)
		{
			this._additionalItems = new ArrayList<>();
		}

		this._additionalItems.add(holder);
	}

	public List<AdditionalItemHolder> getAdditionalItems()
	{
		return this._additionalItems != null ? this._additionalItems : Collections.emptyList();
	}

	public void setBasicActionList(int[] actions)
	{
		this._actions = actions;
	}

	public int[] getBasicActionList()
	{
		return this._actions;
	}

	public boolean hasBasicActionList()
	{
		return this._actions != null;
	}

	public void addLevelData(TransformLevelData data)
	{
		this._data.put(data.getLevel(), data);
	}

	public TransformLevelData getData(int level)
	{
		return this._data.get(level);
	}
}
