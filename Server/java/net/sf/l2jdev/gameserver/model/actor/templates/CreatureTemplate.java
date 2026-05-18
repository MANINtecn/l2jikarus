package net.sf.l2jdev.gameserver.model.actor.templates;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Race;
import net.sf.l2jdev.gameserver.model.events.ListenersContainer;
import net.sf.l2jdev.gameserver.model.item.type.WeaponType;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.stats.Stat;

public class CreatureTemplate extends ListenersContainer
{
	private WeaponType _baseAttackType;
	private int _collisionRadius;
	private int _collisionHeight;
	private float _fCollisionRadius;
	private float _fCollisionHeight;
	protected final Map<Stat, Double> _baseValues = new EnumMap<>(Stat.class);
	private Race _race;

	public CreatureTemplate(StatSet set)
	{
		this.set(set);
	}

	public void set(StatSet set)
	{
		this._baseValues.put(Stat.STAT_STR, set.getDouble("baseSTR", 0.0));
		this._baseValues.put(Stat.STAT_CON, set.getDouble("baseCON", 0.0));
		this._baseValues.put(Stat.STAT_DEX, set.getDouble("baseDEX", 0.0));
		this._baseValues.put(Stat.STAT_INT, set.getDouble("baseINT", 0.0));
		this._baseValues.put(Stat.STAT_WIT, set.getDouble("baseWIT", 0.0));
		this._baseValues.put(Stat.STAT_MEN, set.getDouble("baseMEN", 0.0));
		this._baseValues.put(Stat.MAX_HP, set.getDouble("baseHpMax", 0.0));
		this._baseValues.put(Stat.MAX_MP, set.getDouble("baseMpMax", 0.0));
		this._baseValues.put(Stat.MAX_CP, set.getDouble("baseCpMax", 0.0));
		this._baseValues.put(Stat.REGENERATE_HP_RATE, set.getDouble("baseHpReg", 0.0));
		this._baseValues.put(Stat.REGENERATE_MP_RATE, set.getDouble("baseMpReg", 0.0));
		this._baseValues.put(Stat.REGENERATE_CP_RATE, set.getDouble("baseCpReg", 0.0));
		this._baseValues.put(Stat.PHYSICAL_ATTACK, set.getDouble("basePAtk", 0.0));
		this._baseValues.put(Stat.MAGIC_ATTACK, set.getDouble("baseMAtk", 0.0));
		this._baseValues.put(Stat.PHYSICAL_DEFENCE, set.getDouble("basePDef", 0.0));
		this._baseValues.put(Stat.MAGICAL_DEFENCE, set.getDouble("baseMDef", 0.0));
		this._baseValues.put(Stat.PHYSICAL_ATTACK_SPEED, set.getDouble("basePAtkSpd", 300.0));
		this._baseValues.put(Stat.MAGIC_ATTACK_SPEED, set.getDouble("baseMAtkSpd", 333.0));
		this._baseValues.put(Stat.SHIELD_DEFENCE, set.getDouble("baseShldDef", 0.0));
		this._baseValues.put(Stat.PHYSICAL_ATTACK_RANGE, set.getDouble("baseAtkRange", 40.0));
		this._baseValues.put(Stat.RANDOM_DAMAGE, set.getDouble("baseRndDam", 0.0));
		this._baseValues.put(Stat.SHIELD_DEFENCE_RATE, set.getDouble("baseShldRate", 0.0));
		this._baseValues.put(Stat.CRITICAL_RATE, set.getDouble("baseCritRate", 4.0));
		this._baseValues.put(Stat.MAGIC_CRITICAL_RATE, set.getDouble("baseMCritRate", 5.0));
		this._baseValues.put(Stat.CRITICAL_RATE_SKILL, set.getDouble("basePSkillCritRate", 5.0));
		this._baseValues.put(Stat.BREATH, set.getDouble("baseBreath", 100.0));
		this._baseValues.put(Stat.FIRE_POWER, set.getDouble("baseFire", 0.0));
		this._baseValues.put(Stat.WIND_POWER, set.getDouble("baseWind", 0.0));
		this._baseValues.put(Stat.WATER_POWER, set.getDouble("baseWater", 0.0));
		this._baseValues.put(Stat.EARTH_POWER, set.getDouble("baseEarth", 0.0));
		this._baseValues.put(Stat.HOLY_POWER, set.getDouble("baseHoly", 0.0));
		this._baseValues.put(Stat.DARK_POWER, set.getDouble("baseDark", 0.0));
		this._baseValues.put(Stat.FIRE_RES, set.getDouble("baseFireRes", 0.0));
		this._baseValues.put(Stat.WIND_RES, set.getDouble("baseWindRes", 0.0));
		this._baseValues.put(Stat.WATER_RES, set.getDouble("baseWaterRes", 0.0));
		this._baseValues.put(Stat.EARTH_RES, set.getDouble("baseEarthRes", 0.0));
		this._baseValues.put(Stat.HOLY_RES, set.getDouble("baseHolyRes", 0.0));
		this._baseValues.put(Stat.DARK_RES, set.getDouble("baseDarkRes", 0.0));
		this._baseValues.put(Stat.BASE_ATTRIBUTE_RES, set.getDouble("baseElementRes", 0.0));
		this._fCollisionHeight = set.getFloat("collision_height", 0.0F);
		this._fCollisionRadius = set.getFloat("collision_radius", 0.0F);
		this._collisionRadius = (int) this._fCollisionRadius;
		this._collisionHeight = (int) this._fCollisionHeight;
		this._baseValues.put(Stat.RUN_SPEED, set.getDouble("baseRunSpd", 120.0));
		this._baseValues.put(Stat.WALK_SPEED, set.getDouble("baseWalkSpd", 50.0));
		this._baseValues.put(Stat.SWIM_RUN_SPEED, set.getDouble("baseSwimRunSpd", 120.0));
		this._baseValues.put(Stat.SWIM_WALK_SPEED, set.getDouble("baseSwimWalkSpd", 50.0));
		this._baseValues.put(Stat.FLY_RUN_SPEED, set.getDouble("baseFlyRunSpd", 120.0));
		this._baseValues.put(Stat.FLY_WALK_SPEED, set.getDouble("baseFlyWalkSpd", 50.0));
		this._baseAttackType = set.getEnum("baseAtkType", WeaponType.class, WeaponType.FIST);
		this._baseValues.put(Stat.ABNORMAL_RESIST_PHYSICAL, set.getDouble("physicalAbnormalResist", 10.0));
		this._baseValues.put(Stat.ABNORMAL_RESIST_MAGICAL, set.getDouble("magicAbnormalResist", 10.0));
	}

	public int getBaseSTR()
	{
		Double val = this._baseValues.get(Stat.STAT_STR);
		return val != null ? val.intValue() : 0;
	}

	public int getBaseCON()
	{
		Double val = this._baseValues.get(Stat.STAT_CON);
		return val != null ? val.intValue() : 0;
	}

	public int getBaseDEX()
	{
		Double val = this._baseValues.get(Stat.STAT_DEX);
		return val != null ? val.intValue() : 0;
	}

	public int getBaseINT()
	{
		Double val = this._baseValues.get(Stat.STAT_INT);
		return val != null ? val.intValue() : 0;
	}

	public int getBaseWIT()
	{
		Double val = this._baseValues.get(Stat.STAT_WIT);
		return val != null ? val.intValue() : 0;
	}

	public int getBaseMEN()
	{
		Double val = this._baseValues.get(Stat.STAT_MEN);
		return val != null ? val.intValue() : 0;
	}

	public float getBaseHpMax()
	{
		Double val = this._baseValues.get(Stat.MAX_HP);
		return val != null ? val.floatValue() : 0.0F;
	}

	public float getBaseCpMax()
	{
		Double val = this._baseValues.get(Stat.MAX_CP);
		return val != null ? val.floatValue() : 0.0F;
	}

	public float getBaseMpMax()
	{
		Double val = this._baseValues.get(Stat.MAX_MP);
		return val != null ? val.floatValue() : 0.0F;
	}

	public float getBaseHpReg()
	{
		Double val = this._baseValues.get(Stat.REGENERATE_HP_RATE);
		return val != null ? val.floatValue() : 0.0F;
	}

	public float getBaseMpReg()
	{
		Double val = this._baseValues.get(Stat.REGENERATE_MP_RATE);
		return val != null ? val.floatValue() : 0.0F;
	}

	public int getBaseFire()
	{
		Double val = this._baseValues.get(Stat.FIRE_POWER);
		return val != null ? val.intValue() : 0;
	}

	public int getBaseWind()
	{
		Double val = this._baseValues.get(Stat.WIND_POWER);
		return val != null ? val.intValue() : 0;
	}

	public int getBaseWater()
	{
		Double val = this._baseValues.get(Stat.WATER_POWER);
		return val != null ? val.intValue() : 0;
	}

	public int getBaseEarth()
	{
		Double val = this._baseValues.get(Stat.EARTH_POWER);
		return val != null ? val.intValue() : 0;
	}

	public int getBaseHoly()
	{
		Double val = this._baseValues.get(Stat.HOLY_POWER);
		return val != null ? val.intValue() : 0;
	}

	public int getBaseDark()
	{
		Double val = this._baseValues.get(Stat.DARK_POWER);
		return val != null ? val.intValue() : 0;
	}

	public double getBaseFireRes()
	{
		Double val = this._baseValues.get(Stat.FIRE_RES);
		return val != null ? val.intValue() : 0.0;
	}

	public double getBaseWindRes()
	{
		Double val = this._baseValues.get(Stat.WIND_RES);
		return val != null ? val.intValue() : 0.0;
	}

	public double getBaseWaterRes()
	{
		Double val = this._baseValues.get(Stat.WATER_RES);
		return val != null ? val.intValue() : 0.0;
	}

	public double getBaseEarthRes()
	{
		Double val = this._baseValues.get(Stat.EARTH_RES);
		return val != null ? val.intValue() : 0.0;
	}

	public double getBaseHolyRes()
	{
		Double val = this._baseValues.get(Stat.HOLY_RES);
		return val != null ? val.intValue() : 0.0;
	}

	public double getBaseDarkRes()
	{
		Double val = this._baseValues.get(Stat.DARK_RES);
		return val != null ? val.intValue() : 0.0;
	}

	public double getBaseElementRes()
	{
		Double val = this._baseValues.get(Stat.BASE_ATTRIBUTE_RES);
		return val != null ? val.intValue() : 0.0;
	}

	public int getBasePAtk()
	{
		Double val = this._baseValues.get(Stat.PHYSICAL_ATTACK);
		return val != null ? val.intValue() : 0;
	}

	public int getBaseMAtk()
	{
		Double val = this._baseValues.get(Stat.MAGIC_ATTACK);
		return val != null ? val.intValue() : 0;
	}

	public int getBasePDef()
	{
		Double val = this._baseValues.get(Stat.PHYSICAL_DEFENCE);
		return val != null ? val.intValue() : 0;
	}

	public int getBaseMDef()
	{
		Double val = this._baseValues.get(Stat.MAGICAL_DEFENCE);
		return val != null ? val.intValue() : 0;
	}

	public int getBasePAtkSpd()
	{
		Double val = this._baseValues.get(Stat.PHYSICAL_ATTACK_SPEED);
		return val != null ? val.intValue() : 0;
	}

	public int getBaseMAtkSpd()
	{
		Double val = this._baseValues.get(Stat.MAGIC_ATTACK_SPEED);
		return val != null ? val.intValue() : 0;
	}

	public int getRandomDamage()
	{
		Double val = this._baseValues.get(Stat.RANDOM_DAMAGE);
		return val != null ? val.intValue() : 0;
	}

	public int getBaseShldDef()
	{
		Double val = this._baseValues.get(Stat.SHIELD_DEFENCE);
		return val != null ? val.intValue() : 0;
	}

	public int getBaseShldRate()
	{
		Double val = this._baseValues.get(Stat.SHIELD_DEFENCE_RATE);
		return val != null ? val.intValue() : 0;
	}

	public int getBaseCritRate()
	{
		Double val = this._baseValues.get(Stat.CRITICAL_RATE);
		return val != null ? val.intValue() : 0;
	}

	public int getBaseMCritRate()
	{
		Double val = this._baseValues.get(Stat.MAGIC_CRITICAL_RATE);
		return val != null ? val.intValue() : 0;
	}

	public int getPSkillCriticalRate()
	{
		Double val = this._baseValues.get(Stat.CRITICAL_RATE_SKILL);
		return val != null ? val.intValue() : 0;
	}

	public int getBaseBreath()
	{
		Double val = this._baseValues.get(Stat.BREATH);
		return val != null ? val.intValue() : 0;
	}

	public int getBaseAbnormalResistPhysical()
	{
		Double val = this._baseValues.get(Stat.ABNORMAL_RESIST_PHYSICAL);
		return val != null ? val.intValue() : 0;
	}

	public int getBaseAbnormalResistMagical()
	{
		Double val = this._baseValues.get(Stat.ABNORMAL_RESIST_MAGICAL);
		return val != null ? val.intValue() : 0;
	}

	public int getCollisionRadius()
	{
		return this._collisionRadius;
	}

	public int getCollisionHeight()
	{
		return this._collisionHeight;
	}

	public float getFCollisionRadius()
	{
		return this._fCollisionRadius;
	}

	public float getFCollisionHeight()
	{
		return this._fCollisionHeight;
	}

	public WeaponType getBaseAttackType()
	{
		return this._baseAttackType;
	}

	public void setBaseAttackType(WeaponType type)
	{
		this._baseAttackType = type;
	}

	public int getBaseAttackRange()
	{
		Double val = this._baseValues.get(Stat.PHYSICAL_ATTACK_RANGE);
		return val != null ? val.intValue() : 0;
	}

	public Map<Integer, Skill> getSkills()
	{
		return Collections.emptyMap();
	}

	public Race getRace()
	{
		return this._race;
	}

	public void setRace(Race race)
	{
		this._race = race;
	}

	public double getBaseValue(Stat stat, double defaultValue)
	{
		Double val = this._baseValues.get(stat);
		return val != null ? val : defaultValue;
	}
}
