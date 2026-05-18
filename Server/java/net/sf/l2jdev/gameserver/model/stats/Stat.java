package net.sf.l2jdev.gameserver.model.stats;

import java.util.NoSuchElementException;
import java.util.OptionalDouble;
import java.util.function.DoubleBinaryOperator;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.AttributeType;
import net.sf.l2jdev.gameserver.model.stats.finalizers.AttributeFinalizer;
import net.sf.l2jdev.gameserver.model.stats.finalizers.BaseStatFinalizer;
import net.sf.l2jdev.gameserver.model.stats.finalizers.MAccuracyFinalizer;
import net.sf.l2jdev.gameserver.model.stats.finalizers.MAttackFinalizer;
import net.sf.l2jdev.gameserver.model.stats.finalizers.MAttackSpeedFinalizer;
import net.sf.l2jdev.gameserver.model.stats.finalizers.MCritRateFinalizer;
import net.sf.l2jdev.gameserver.model.stats.finalizers.MDefenseFinalizer;
import net.sf.l2jdev.gameserver.model.stats.finalizers.MEvasionRateFinalizer;
import net.sf.l2jdev.gameserver.model.stats.finalizers.MaxCpFinalizer;
import net.sf.l2jdev.gameserver.model.stats.finalizers.MaxHpFinalizer;
import net.sf.l2jdev.gameserver.model.stats.finalizers.MaxMpFinalizer;
import net.sf.l2jdev.gameserver.model.stats.finalizers.MpVampiricChanceFinalizer;
import net.sf.l2jdev.gameserver.model.stats.finalizers.PAccuracyFinalizer;
import net.sf.l2jdev.gameserver.model.stats.finalizers.PAttackFinalizer;
import net.sf.l2jdev.gameserver.model.stats.finalizers.PAttackSpeedFinalizer;
import net.sf.l2jdev.gameserver.model.stats.finalizers.PCriticalRateFinalizer;
import net.sf.l2jdev.gameserver.model.stats.finalizers.PDefenseFinalizer;
import net.sf.l2jdev.gameserver.model.stats.finalizers.PEvasionRateFinalizer;
import net.sf.l2jdev.gameserver.model.stats.finalizers.PRangeFinalizer;
import net.sf.l2jdev.gameserver.model.stats.finalizers.PSkillCritRateFinalizer;
import net.sf.l2jdev.gameserver.model.stats.finalizers.RandomDamageFinalizer;
import net.sf.l2jdev.gameserver.model.stats.finalizers.RegenCPFinalizer;
import net.sf.l2jdev.gameserver.model.stats.finalizers.RegenHPFinalizer;
import net.sf.l2jdev.gameserver.model.stats.finalizers.RegenMPFinalizer;
import net.sf.l2jdev.gameserver.model.stats.finalizers.ShieldDefenceFinalizer;
import net.sf.l2jdev.gameserver.model.stats.finalizers.ShieldDefenceRateFinalizer;
import net.sf.l2jdev.gameserver.model.stats.finalizers.ShotsBonusFinalizer;
import net.sf.l2jdev.gameserver.model.stats.finalizers.SpeedFinalizer;
import net.sf.l2jdev.gameserver.model.stats.finalizers.VampiricChanceFinalizer;
import net.sf.l2jdev.gameserver.util.MathUtil;

public enum Stat
{
	HP_LIMIT("hpLimit"),
	MAX_HP("maxHp", new MaxHpFinalizer()),
	MAX_MP("maxMp", new MaxMpFinalizer()),
	MAX_CP("maxCp", new MaxCpFinalizer()),
	MAX_RECOVERABLE_HP("maxRecoverableHp"),
	MAX_RECOVERABLE_MP("maxRecoverableMp"),
	MAX_RECOVERABLE_CP("maxRecoverableCp"),
	REGENERATE_HP_RATE("regHp", new RegenHPFinalizer()),
	REGENERATE_CP_RATE("regCp", new RegenCPFinalizer()),
	REGENERATE_MP_RATE("regMp", new RegenMPFinalizer()),
	ADDITIONAL_POTION_HP("addPotionHp"),
	ADDITIONAL_POTION_MP("addPotionMp"),
	ADDITIONAL_POTION_CP("addPotionCp"),
	ADDITIONAL_POTION_HP_PER("addPotionHpPer"),
	ADDITIONAL_POTION_MP_PER("addPotionMpPer"),
	ADDITIONAL_POTION_CP_PER("addPotionCpPer"),
	MANA_CHARGE("manaCharge"),
	HEAL_EFFECT("healEffect"),
	HEAL_EFFECT_ADD("healEffectAdd"),
	FEED_MODIFY("feedModify"),
	PHYSICAL_DEFENCE("pDef", new PDefenseFinalizer()),
	MAGICAL_DEFENCE("mDef", new MDefenseFinalizer()),
	PHYSICAL_ATTACK("pAtk", new PAttackFinalizer()),
	MAGIC_ATTACK("mAtk", new MAttackFinalizer()),
	WEAPON_BONUS_PHYSICAL_ATTACK("weaponBonusPAtk"),
	WEAPON_BONUS_PHYSICAL_ATTACK_MULTIPIER("weaponBonusPAtkMultiplier"),
	WEAPON_BONUS_MAGIC_ATTACK("weaponBonusMAtk"),
	WEAPON_BONUS_MAGIC_ATTACK_MULTIPIER("weaponBonusMAtkMultiplier"),
	MAGIC_ATTACK_BY_PHYSICAL_ATTACK("mAtkByPAtk", Stat::defaultValue, MathUtil::add, MathUtil::mul, 0.0, 0.0),
	PHYSICAL_ATTACK_SPEED("pAtkSpd", new PAttackSpeedFinalizer()),
	MAGIC_ATTACK_SPEED("mAtkSpd", new MAttackSpeedFinalizer()),
	ATK_REUSE("atkReuse"),
	SHIELD_DEFENCE("sDef", new ShieldDefenceFinalizer()),
	SHIELD_DEFENCE_IGNORE_REMOVAL("shieldDefIgnoreRemoval"),
	SHIELD_DEFENCE_IGNORE_REMOVAL_ADD("shieldDefIgnoreRemovalAdd"),
	CRITICAL_DAMAGE("cAtk"),
	CRITICAL_DAMAGE_ADD("cAtkAdd"),
	HATE_ATTACK("attackHate"),
	REAR_DAMAGE_RATE("rearDamage"),
	AUTO_ATTACK_DAMAGE_BONUS("autoAttackDamageBonus"),
	IGNORE_REDUCE_DAMAGE("ignoreReduceDamage"),
	STATIC_PHYSICAL_ATTACK_SPEED("staticPAtkSpeed"),
	STATIC_MAGICAL_ATTACK_SPEED("staticMAtkSpeed"),
	ELEMENTAL_SPIRIT_FIRE_ATTACK("elementalSpiritFireAttack"),
	ELEMENTAL_SPIRIT_WATER_ATTACK("elementalSpiritWaterAttack"),
	ELEMENTAL_SPIRIT_WIND_ATTACK("elementalSpiritWindAttack"),
	ELEMENTAL_SPIRIT_EARTH_ATTACK("elementalSpiritEarthAttack"),
	ELEMENTAL_SPIRIT_FIRE_DEFENSE("elementalSpiritFireDefense"),
	ELEMENTAL_SPIRIT_WATER_DEFENSE("elementalSpiritWaterDefense"),
	ELEMENTAL_SPIRIT_WIND_DEFENSE("elementalSpiritWindDefense"),
	ELEMENTAL_SPIRIT_EARTH_DEFENSE("elementalSpiritEarthDefense"),
	ELEMENTAL_SPIRIT_CRITICAL_RATE("elementalSpiritCriticalRate"),
	ELEMENTAL_SPIRIT_CRITICAL_DAMAGE("elementalSpiritCriticalDamage"),
	ELEMENTAL_SPIRIT_BONUS_EXP("elementalSpiritExp"),
	PVP_PHYSICAL_ATTACK_DAMAGE("pvpPhysDmg"),
	PVP_MAGICAL_SKILL_DAMAGE("pvpMagicalDmg"),
	PVP_PHYSICAL_SKILL_DAMAGE("pvpPhysSkillsDmg"),
	PVP_PHYSICAL_ATTACK_DEFENCE("pvpPhysDef"),
	PVP_MAGICAL_SKILL_DEFENCE("pvpMagicalDef"),
	PVP_PHYSICAL_SKILL_DEFENCE("pvpPhysSkillsDef"),
	PVE_PHYSICAL_ATTACK_DAMAGE("pvePhysDmg"),
	PVE_PHYSICAL_SKILL_DAMAGE("pvePhysSkillDmg"),
	PVE_MAGICAL_SKILL_DAMAGE("pveMagicalDmg"),
	PVE_PHYSICAL_ATTACK_DEFENCE("pvePhysDef"),
	PVE_PHYSICAL_SKILL_DEFENCE("pvePhysSkillDef"),
	PVE_MAGICAL_SKILL_DEFENCE("pveMagicalDef"),
	PVE_RAID_PHYSICAL_ATTACK_DAMAGE("pveRaidPhysDmg"),
	PVE_RAID_PHYSICAL_SKILL_DAMAGE("pveRaidPhysSkillDmg"),
	PVE_RAID_MAGICAL_SKILL_DAMAGE("pveRaidMagicalDmg"),
	PVE_RAID_PHYSICAL_ATTACK_DEFENCE("pveRaidPhysDef"),
	PVE_RAID_PHYSICAL_SKILL_DEFENCE("pveRaidPhysSkillDef"),
	PVE_RAID_MAGICAL_SKILL_DEFENCE("pveRaidMagicalDef"),
	PVP_DAMAGE_TAKEN("pvpDamageTaken"),
	PVE_DAMAGE_TAKEN("pveDamageTaken"),
	PVE_DAMAGE_TAKEN_MONSTER("pveDamageTakenMonster"),
	PVE_DAMAGE_TAKEN_RAID("pveDamageTakenRaid"),
	MAGIC_CRITICAL_DAMAGE("mCritPower"),
	SKILL_POWER_ADD("skillPowerAdd"),
	PHYSICAL_SKILL_POWER("physicalSkillPower"),
	MAGICAL_SKILL_POWER("magicalSkillPower"),
	PHYSICAL_SKILL_CRITICAL_DAMAGE("cAtkSkill"),
	PHYSICAL_SKILL_CRITICAL_DAMAGE_ADD("cAtkSkillAdd"),
	MAGIC_CRITICAL_DAMAGE_ADD("mCritPowerAdd"),
	SHIELD_DEFENCE_RATE("rShld", new ShieldDefenceRateFinalizer()),
	CRITICAL_RATE("rCrit", new PCriticalRateFinalizer(), MathUtil::add, MathUtil::add, 0.0, 1.0),
	CRITICAL_RATE_SKILL("physicalSkillCriticalRate", new PSkillCritRateFinalizer()),
	ADD_MAX_MAGIC_CRITICAL_RATE("addMaxMagicCritRate"),
	ADD_MAX_PHYSICAL_CRITICAL_RATE("addMaxPhysicalCritRate"),
	MAGIC_CRITICAL_RATE("mCritRate", new MCritRateFinalizer()),
	MAGIC_CRITICAL_RATE_BY_CRITICAL_RATE("mCritRateByRCrit", Stat::defaultValue, MathUtil::add, MathUtil::mul, 0.0, 0.0),
	DEFENCE_CRITICAL_RATE("defCritRate"),
	DEFENCE_CRITICAL_RATE_ADD("defCritRateAdd"),
	DEFENCE_MAGIC_CRITICAL_RATE("defMCritRate"),
	DEFENCE_MAGIC_CRITICAL_RATE_ADD("defMCritRateAdd"),
	DEFENCE_CRITICAL_DAMAGE("defCritDamage"),
	DEFENCE_MAGIC_CRITICAL_DAMAGE("defMCritDamage"),
	DEFENCE_MAGIC_CRITICAL_DAMAGE_ADD("defMCritDamageAdd"),
	DEFENCE_CRITICAL_DAMAGE_ADD("defCritDamageAdd"),
	DEFENCE_PHYSICAL_SKILL_CRITICAL_DAMAGE("defCAtkSkill"),
	DEFENCE_PHYSICAL_SKILL_CRITICAL_DAMAGE_ADD("defCAtkSkillAdd"),
	DEFENCE_PHYSICAL_SKILL_CRITICAL_RATE("defPhysSkillCritRate"),
	DEFENCE_PHYSICAL_SKILL_CRITICAL_RATE_ADD("defPhysSkillCritRateAdd"),
	DEFENCE_IGNORE_REMOVAL("defIgnoreRemoval"),
	DEFENCE_IGNORE_REMOVAL_ADD("defIgnoreRemovalAdd"),
	AREA_OF_EFFECT_DAMAGE_DEFENCE("aoeDamageDefence"),
	AREA_OF_EFFECT_DAMAGE_DEFENCE_ADD("aoeDamageDefenceAdd"),
	AREA_OF_EFFECT_DAMAGE_MODIFY("aoeDamageModify"),
	BLOW_RATE("blowRate"),
	BLOW_RATE_DEFENCE("blowRateDefence"),
	INSTANT_KILL_RESIST("instantKillResist"),
	EXPSP_RATE("rExp"),
	ACTIVE_BONUS_EXP("activeBonusExp"),
	BONUS_EXP_BUFFS("bonusExpBuffs"),
	BONUS_EXP_PASSIVES("bonusExpPassives"),
	BONUS_EXP_PET("bonusExpPet"),
	BONUS_EXP("bonusExp"),
	BONUS_SP("bonusSp"),
	BONUS_DROP_ADENA("bonusDropAdena"),
	BONUS_DROP_AMOUNT("bonusDropAmount"),
	BONUS_DROP_RATE("bonusDropRate"),
	BONUS_DROP_RATE_LCOIN("bonusDropRateLCoin"),
	BONUS_SPOIL_RATE("bonusSpoilRate"),
	BONUS_RAID_POINTS("bonusRaidPoints"),
	ATTACK_CANCEL("cancel"),
	ACCESSORY_MAGICAL_DEFENCE("accessoryMagicalDefence"),
	ARMOR_PHYSICAL_DEFENCE("armorPhysicalDefence"),
	ACCURACY_COMBAT("accCombat", new PAccuracyFinalizer()),
	ACCURACY_MAGIC("accMagic", new MAccuracyFinalizer()),
	EVASION_RATE("rEvas", new PEvasionRateFinalizer()),
	MAGIC_EVASION_RATE("mEvas", new MEvasionRateFinalizer()),
	ACCURACY_BONUS("accBonus"),
	PHYSICAL_ATTACK_RANGE("pAtkRange", new PRangeFinalizer()),
	MAGIC_ATTACK_RANGE("mAtkRange"),
	ATTACK_COUNT_MAX("atkCountMax"),
	PHYSICAL_POLEARM_TARGET_SINGLE("polearmSingleTarget"),
	WEAPON_ATTACK_ANGLE_BONUS("weaponAttackAngleBonus"),
	MOVE_SPEED("moveSpeed"),
	SPEED_LIMIT("speedLimit"),
	RUN_SPEED("runSpd", new SpeedFinalizer()),
	WALK_SPEED("walkSpd", new SpeedFinalizer()),
	SWIM_RUN_SPEED("fastSwimSpd", new SpeedFinalizer()),
	SWIM_WALK_SPEED("slowSimSpd", new SpeedFinalizer()),
	FLY_RUN_SPEED("fastFlySpd", new SpeedFinalizer()),
	FLY_WALK_SPEED("slowFlySpd", new SpeedFinalizer()),
	STATIC_SPEED("staticSpeed"),
	STAT_STR("STR", new BaseStatFinalizer()),
	STAT_CON("CON", new BaseStatFinalizer()),
	STAT_DEX("DEX", new BaseStatFinalizer()),
	STAT_INT("INT", new BaseStatFinalizer()),
	STAT_WIT("WIT", new BaseStatFinalizer()),
	STAT_MEN("MEN", new BaseStatFinalizer()),
	BREATH("breath"),
	FALL("fall"),
	FISHING_EXP_SP_BONUS("fishingExpSpBonus"),
	ENCHANT_RATE("enchantRate"),
	DAMAGE_ZONE_VULN("damageZoneVuln"),
	RESIST_DISPEL_BUFF("cancelVuln"),
	RESIST_ABNORMAL_DEBUFF("debuffVuln"),
	FIRE_RES("fireRes", new AttributeFinalizer(AttributeType.FIRE, false)),
	WIND_RES("windRes", new AttributeFinalizer(AttributeType.WIND, false)),
	WATER_RES("waterRes", new AttributeFinalizer(AttributeType.WATER, false)),
	EARTH_RES("earthRes", new AttributeFinalizer(AttributeType.EARTH, false)),
	HOLY_RES("holyRes", new AttributeFinalizer(AttributeType.HOLY, false)),
	DARK_RES("darkRes", new AttributeFinalizer(AttributeType.DARK, false)),
	BASE_ATTRIBUTE_RES("baseAttrRes"),
	MAGIC_SUCCESS_RES("magicSuccRes"),
	ABNORMAL_RESIST_PHYSICAL("abnormalResPhysical"),
	ABNORMAL_RESIST_MAGICAL("abnormalResMagical"),
	REAL_DAMAGE_RESIST("realDamageResist"),
	FIRE_POWER("firePower", new AttributeFinalizer(AttributeType.FIRE, true)),
	WATER_POWER("waterPower", new AttributeFinalizer(AttributeType.WATER, true)),
	WIND_POWER("windPower", new AttributeFinalizer(AttributeType.WIND, true)),
	EARTH_POWER("earthPower", new AttributeFinalizer(AttributeType.EARTH, true)),
	HOLY_POWER("holyPower", new AttributeFinalizer(AttributeType.HOLY, true)),
	DARK_POWER("darkPower", new AttributeFinalizer(AttributeType.DARK, true)),
	REFLECT_DAMAGE_PERCENT("reflectDam"),
	REFLECT_DAMAGE_PERCENT_DEFENSE("reflectDamDef"),
	REFLECT_SKILL_MAGIC("reflectSkillMagic"),
	REFLECT_SKILL_PHYSIC("reflectSkillPhysic"),
	REFLECT_DEBUFF_RATE("reflectDebuffrate"),
	VENGEANCE_SKILL_MAGIC_DAMAGE("vengeanceMdam"),
	VENGEANCE_SKILL_PHYSICAL_DAMAGE("vengeancePdam"),
	ABSORB_DAMAGE_PERCENT("absorbDam"),
	ABSORB_DAMAGE_CHANCE("absorbDamChance", new VampiricChanceFinalizer()),
	ABSORB_DAMAGE_DEFENCE("absorbDamDefence"),
	TRANSFER_DAMAGE_SUMMON_PERCENT("transDam"),
	MANA_SHIELD_PERCENT("manaShield"),
	TRANSFER_DAMAGE_TO_PLAYER("transDamToPlayer"),
	ABSORB_MANA_DAMAGE_PERCENT("absorbDamMana"),
	ABSORB_MANA_DAMAGE_CHANCE("absorbDamManaChance", new MpVampiricChanceFinalizer()),
	WEIGHT_LIMIT("weightLimit"),
	WEIGHT_PENALTY("weightPenalty"),
	INVENTORY_NORMAL("inventoryLimit"),
	STORAGE_PRIVATE("whLimit"),
	TRADE_SELL("PrivateSellLimit"),
	TRADE_BUY("PrivateBuyLimit"),
	RECIPE_DWARVEN("DwarfRecipeLimit"),
	RECIPE_COMMON("CommonRecipeLimit"),
	SKILL_MASTERY("skillMastery"),
	SKILL_MASTERY_RATE("skillMasteryRate"),
	VITALITY_CONSUME_RATE("vitalityConsumeRate"),
	VITALITY_EXP_RATE("vitalityExpRate"),
	VITALITY_SKILLS("vitalitySkills"),
	MAGIC_LAMP_EXP_RATE("magicLampExpRate"),
	LAMP_BONUS_EXP("LampBonusExp"),
	LAMP_BONUS_BUFFS_COUNT("LampBonusBuffCount"),
	HENNA_SLOTS_AVAILABLE("hennaSlots"),
	MAX_SOULS("maxSouls"),
	REDUCE_EXP_LOST_BY_PVP("reduceExpLostByPvp"),
	REDUCE_EXP_LOST_BY_MOB("reduceExpLostByMob"),
	REDUCE_EXP_LOST_BY_RAID("reduceExpLostByRaid"),
	REDUCE_DEATH_PENALTY_BY_PVP("reduceDeathPenaltyByPvp"),
	REDUCE_DEATH_PENALTY_BY_MOB("reduceDeathPenaltyByMob"),
	REDUCE_DEATH_PENALTY_BY_RAID("reduceDeathPenaltyByRaid"),
	BROOCH_JEWELS("broochJewels"),
	AGATHION_SLOTS("agathionSlots"),
	ARTIFACT_SLOTS("artifactSlots"),
	MAX_SUMMON_POINTS("summonPoints"),
	MAX_CUBIC("cubicCount"),
	BEAST_POINTS_ADD("beastPointsAdd"),
	SPHERIC_BARRIER_RANGE("sphericBarrier"),
	DEBUFF_BLOCK("debuffBlock"),
	RANDOM_DAMAGE("randomDamage", new RandomDamageFinalizer()),
	DAMAGE_LIMIT("damageCap"),
	MAX_MOMENTUM("maxMomentum"),
	STAT_BONUS_SKILL_CRITICAL("statSkillCritical"),
	STAT_BONUS_SPEED("statSpeed"),
	CRAFTING_CRITICAL("craftingCritical"),
	SHOTS_BONUS("shotBonus", new ShotsBonusFinalizer()),
	SOULSHOT_RESISTANCE("soulshotResistance"),
	SPIRITSHOT_RESISTANCE("spiritshotResistance"),
	WORLD_CHAT_POINTS("worldChatPoints"),
	ATTACK_DAMAGE("attackDamage"),
	IMMOBILE_DAMAGE_BONUS("immobileBonus"),
	IMMOBILE_DAMAGE_RESIST("immobileResist"),
	CRAFT_RATE("CraftRate"),
	ELIXIR_USAGE_LIMIT("elixirUsageLimit"),
	RESURRECTION_FEE_MODIFIER("resurrectionFeeModifier"),
	COMBAT_POWER("combatPower");

	public static final int NUM_STATS = values().length;
	private final String _value;
	private final IStatFunction _valueFinalizer;
	private final DoubleBinaryOperator _addFunction;
	private final DoubleBinaryOperator _mulFunction;
	private final Double _resetAddValue;
	private final Double _resetMulValue;

	public String getValue()
	{
		return this._value;
	}

	private Stat(String xmlString)
	{
		this(xmlString, Stat::defaultValue, MathUtil::add, MathUtil::mul, 0.0, 1.0);
	}

	private Stat(String xmlString, IStatFunction valueFinalizer)
	{
		this(xmlString, valueFinalizer, MathUtil::add, MathUtil::mul, 0.0, 1.0);
	}

	private Stat(String xmlString, IStatFunction valueFinalizer, DoubleBinaryOperator addFunction, DoubleBinaryOperator mulFunction, double resetAddValue, double resetMulValue)
	{
		this._value = xmlString;
		this._valueFinalizer = valueFinalizer;
		this._addFunction = addFunction;
		this._mulFunction = mulFunction;
		this._resetAddValue = resetAddValue;
		this._resetMulValue = resetMulValue;
	}

	public static Stat valueOfXml(String name)
	{
		String internName = name.intern();

		for (Stat s : values())
		{
			if (s.getValue().equals(internName))
			{
				return s;
			}
		}

		throw new NoSuchElementException("Unknown name '" + internName + "' for enum " + Stat.class.getSimpleName());
	}

	public double finalize(Creature creature, OptionalDouble baseValue)
	{
		try
		{
			return this._valueFinalizer.calc(creature, baseValue, this);
		}
		catch (Exception var4)
		{
			return defaultValue(creature, baseValue, this);
		}
	}

	public double functionAdd(double oldValue, double value)
	{
		return this._addFunction.applyAsDouble(oldValue, value);
	}

	public double functionMul(double oldValue, double value)
	{
		return this._mulFunction.applyAsDouble(oldValue, value);
	}

	public Double getResetAddValue()
	{
		return this._resetAddValue;
	}

	public Double getResetMulValue()
	{
		return this._resetMulValue;
	}

	public static double weaponBaseValue(Creature creature, Stat stat)
	{
		return stat._valueFinalizer.calcWeaponBaseValue(creature, stat);
	}

	public static double defaultValue(Creature creature, OptionalDouble base, Stat stat)
	{
		double mul = creature.getStat().getMulValue(stat);
		double add = creature.getStat().getAddValue(stat);
		return base.isPresent() ? defaultValue(creature, stat, base.getAsDouble()) : mul * (add + creature.getStat().getMoveTypeValue(stat, creature.getMoveType()));
	}

	public static double defaultValue(Creature creature, Stat stat, double baseValue)
	{
		double mul = creature.getStat().getMulValue(stat);
		double add = creature.getStat().getAddValue(stat);
		return mul * baseValue + add + creature.getStat().getMoveTypeValue(stat, creature.getMoveType());
	}
}
