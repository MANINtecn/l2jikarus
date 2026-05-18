package net.sf.l2jdev.gameserver.model.stats;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.NpcConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.config.RatesConfig;
import net.sf.l2jdev.gameserver.config.custom.ClassBalanceConfig;
import net.sf.l2jdev.gameserver.data.xml.HitConditionBonusData;
import net.sf.l2jdev.gameserver.data.xml.KarmaLossData;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.AttributeType;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Position;
import net.sf.l2jdev.gameserver.model.actor.enums.player.ElementalSpiritType;
import net.sf.l2jdev.gameserver.model.actor.instance.SiegeFlag;
import net.sf.l2jdev.gameserver.model.actor.instance.StaticObject;
import net.sf.l2jdev.gameserver.model.actor.transform.Transform;
import net.sf.l2jdev.gameserver.model.effects.EffectFlag;
import net.sf.l2jdev.gameserver.model.effects.EffectType;
import net.sf.l2jdev.gameserver.model.interfaces.ILocational;
import net.sf.l2jdev.gameserver.model.item.Armor;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.Weapon;
import net.sf.l2jdev.gameserver.model.item.enums.BroochJewel;
import net.sf.l2jdev.gameserver.model.item.enums.ShotType;
import net.sf.l2jdev.gameserver.model.item.type.ArmorType;
import net.sf.l2jdev.gameserver.model.item.type.WeaponType;
import net.sf.l2jdev.gameserver.model.skill.AbnormalType;
import net.sf.l2jdev.gameserver.model.skill.BuffInfo;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.SkillCaster;
import net.sf.l2jdev.gameserver.model.skill.enums.BasicProperty;
import net.sf.l2jdev.gameserver.model.skill.enums.DispelSlotType;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ExMagicAttackInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.util.LocationUtil;
import net.sf.l2jdev.gameserver.util.MathUtil;

public class Formulas
{
	protected static final int HP_REGENERATE_PERIOD = 3000;
	public static final byte SHIELD_DEFENSE_FAILED = 0;
	public static final byte SHIELD_DEFENSE_SUCCEED = 1;
	public static final byte SHIELD_DEFENSE_PERFECT_BLOCK = 2;
	public static final int SKILL_LAUNCH_TIME = 500;
	protected static final byte MELEE_ATTACK_RANGE = 40;

	public static int getRegeneratePeriod(Creature creature)
	{
		return creature.isDoor() ? 300000 : 3000;
	}

	public static double calcBlowDamage(Creature attacker, Creature target, Skill skill, boolean backstab, double power, byte shld, boolean ss)
	{
		double defence = target.getPDef();
		switch (shld)
		{
			case 1:
				defence += target.getShldDef();
			default:
				double criticalMod = attacker.getStat().getValue(Stat.CRITICAL_DAMAGE, 1.0);
				double criticalPositionMod = attacker.getStat().getPositionTypeValue(Stat.CRITICAL_DAMAGE, Position.getPosition(attacker, target));
				double criticalVulnMod = target.getStat().getValue(Stat.DEFENCE_CRITICAL_DAMAGE, 1.0);
				double criticalAddMod = attacker.getStat().getValue(Stat.CRITICAL_DAMAGE_ADD, 0.0);
				double criticalAddVuln = target.getStat().getValue(Stat.DEFENCE_CRITICAL_DAMAGE_ADD, 0.0);
				double criticalSkillMod = calcCritDamage(attacker, target, skill) / 2.0;
				double weaponTraitMod = calcWeaponTraitBonus(attacker, target);
				double generalTraitMod = calcGeneralTraitBonus(attacker, target, skill.getTraitType(), true);
				double weaknessMod = calcWeaknessBonus(attacker, target, skill.getTraitType());
				double attributeMod = calcAttributeBonus(attacker, target, skill);
				double randomMod = attacker.getRandomDamageMultiplier();
				double pvpPveMod = calculatePvpPveBonus(attacker, target, skill, true);
				double ssmod = ss ? 2.0 + attacker.getStat().getValue(Stat.SHOTS_BONUS) / 100.0 - target.getStat().getValue(Stat.SPIRITSHOT_RESISTANCE, 0.0) / 100.0 : 1.0;
				double cdMult = criticalMod * ((criticalPositionMod - 1.0) / 2.0 + 1.0) * ((criticalVulnMod - 1.0) / 2.0 + 1.0);
				double cdPatk = (criticalAddMod + criticalAddVuln) * criticalSkillMod;
				Position position = Position.getPosition(attacker, target);
				double isPosition = position == Position.BACK ? 0.2 : (position == Position.SIDE ? 0.05 : 0.0);
				double balanceMod = 1.0;
				if (attacker.isPlayable())
				{
					balanceMod = target.isPlayable() ? ClassBalanceConfig.PVP_BLOW_SKILL_DAMAGE_MULTIPLIERS[attacker.asPlayer().getPlayerClass().getId()] : ClassBalanceConfig.PVE_BLOW_SKILL_DAMAGE_MULTIPLIERS[attacker.asPlayer().getPlayerClass().getId()];
				}

				if (target.isPlayable())
				{
					defence *= attacker.isPlayable() ? ClassBalanceConfig.PVP_BLOW_SKILL_DEFENCE_MULTIPLIERS[target.asPlayer().getPlayerClass().getId()] : ClassBalanceConfig.PVE_BLOW_SKILL_DEFENCE_MULTIPLIERS[target.asPlayer().getPlayerClass().getId()];
				}

				double skillPower = power + attacker.getStat().getValue(Stat.SKILL_POWER_ADD, 0.0);
				double baseMod = 77.0 * ((skillPower + attacker.getPAtk()) * 0.666 + isPosition * (skillPower + attacker.getPAtk()) * randomMod + 6.0 * cdPatk) / defence;
				return baseMod * ssmod * cdMult * weaponTraitMod * generalTraitMod * weaknessMod * attributeMod * randomMod * pvpPveMod * balanceMod;
			case 2:
				return 1.0;
		}
	}

	public static double calcMagicDam(Creature attacker, Creature target, Skill skill, double mAtk, double power, double mDef, boolean sps, boolean bss, boolean mcrit)
	{
		double shotsBonus;
		if (bss)
		{
			shotsBonus = 4.0 + (attacker.getStat().getValue(Stat.SHOTS_BONUS) / 100.0 - target.getStat().getValue(Stat.SPIRITSHOT_RESISTANCE, 1.0) / 100.0);
		}
		else if (sps)
		{
			shotsBonus = 2.0 + (attacker.getStat().getValue(Stat.SHOTS_BONUS) / 100.0 - target.getStat().getValue(Stat.SPIRITSHOT_RESISTANCE, 1.0) / 100.0);
		}
		else
		{
			shotsBonus = 1.0;
		}

		double critMod = mcrit ? calcCritDamage(attacker, target, skill) : 1.0;
		double critMagicAdd = mcrit ? calcCritDamageAdd(attacker, target, skill) : 0.0;
		double generalTraitMod = calcGeneralTraitBonus(attacker, target, skill.getTraitType(), true);
		double weaknessMod = calcWeaknessBonus(attacker, target, skill.getTraitType());
		double attributeMod = calcAttributeBonus(attacker, target, skill);
		double randomMod = attacker.getRandomDamageMultiplier();
		double pvpPveMod = calculatePvpPveBonus(attacker, target, skill, mcrit);
		double damage = 77.0 * (power + attacker.getStat().getValue(Stat.SKILL_POWER_ADD, 0.0)) * Math.sqrt(mAtk) / mDef * shotsBonus;
		if (PlayerConfig.ALT_GAME_MAGICFAILURES && !calcMagicSuccess(attacker, target, skill))
		{
			if (attacker.isPlayer())
			{
				if (calcMagicSuccess(attacker, target, skill))
				{
					if (skill.hasEffectType(EffectType.HP_DRAIN))
					{
						attacker.sendPacket(SystemMessageId.DRAIN_WAS_ONLY_50_SUCCESSFUL);
					}
					else
					{
						attacker.sendPacket(SystemMessageId.YOUR_ATTACK_HAS_FAILED);
					}

					damage /= 2.0;
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.C1_HAS_RESISTED_YOUR_S2);
					sm.addString(target.getName());
					sm.addSkillName(skill);
					attacker.sendPacket(sm);
					damage = 1.0;
				}
			}

			if (target.isPlayer())
			{
				SystemMessage sm = skill.hasEffectType(EffectType.HP_DRAIN) ? new SystemMessage(SystemMessageId.YOU_RESISTED_C1_S_DRAIN) : new SystemMessage(SystemMessageId.YOU_RESISTED_C1_S_MAGIC);
				sm.addString(attacker.getName());
				target.sendPacket(sm);
			}
		}

		damage = damage * critMod * (generalTraitMod == 0.0 ? 1.0 : generalTraitMod) * weaknessMod * attributeMod * randomMod * pvpPveMod;
		damage *= attacker.getStat().getValue(Stat.MAGICAL_SKILL_POWER, 1.0);
		damage += critMagicAdd;
		if (skill.hasNegativeEffect() && skill.getAffectLimit() > 0)
		{
			damage *= Math.max(attacker.getStat().getMul(Stat.AREA_OF_EFFECT_DAMAGE_MODIFY, 1.0) - target.getStat().getValue(Stat.AREA_OF_EFFECT_DAMAGE_DEFENCE, 0.0), 0.01);
			damage -= target.getStat().getValue(Stat.AREA_OF_EFFECT_DAMAGE_DEFENCE_ADD, 0.0);
		}

		return Math.max(1.0, damage);
	}

	public static boolean calcCrit(double rateValue, Creature creature, Creature target, Skill skill)
	{
		if (skill != null)
		{
			if (skill.isMagic())
			{
				double magicRate = creature.getStat().getValue(Stat.MAGIC_CRITICAL_RATE);
				if (target != null && skill.hasNegativeEffect())
				{
					double defenceMagicCriticalRate = target.getStat().getValue(Stat.DEFENCE_MAGIC_CRITICAL_RATE, 1.0) - 1.0;
					double magicRateFinal = (magicRate - target.getStat().getValue(Stat.DEFENCE_MAGIC_CRITICAL_RATE_ADD, 0.0)) * (1.0 - Math.min(0.8, defenceMagicCriticalRate));
					if (creature.getLevel() >= 78 || target.getLevel() >= 78)
					{
						magicRateFinal += Math.sqrt(creature.getLevel()) + (creature.getLevel() - target.getLevel()) / 25;
					}

					double balanceMod = 1.0;
					if (creature.isPlayable())
					{
						balanceMod = target.isPlayable() ? ClassBalanceConfig.PVP_MAGICAL_SKILL_CRITICAL_CHANCE_MULTIPLIERS[creature.asPlayer().getPlayerClass().getId()] : ClassBalanceConfig.PVE_MAGICAL_SKILL_CRITICAL_CHANCE_MULTIPLIERS[creature.asPlayer().getPlayerClass().getId()];
					}

					return MathUtil.clamp(magicRateFinal * balanceMod, 30.0, PlayerConfig.MAX_MCRIT_RATE) > Rnd.get(1000);
				}
				return Math.min(magicRate, 320.0) > Rnd.get(1000);
			}
			byte skillCritRateStat = (byte) creature.getStat().getValue(Stat.STAT_BONUS_SKILL_CRITICAL);
			double statBonus;
			if (skillCritRateStat >= 0 && skillCritRateStat < BaseStat.values().length)
			{
				statBonus = BaseStat.values()[skillCritRateStat].calcBonus(creature);
			}
			else
			{
				statBonus = BaseStat.STR.calcBonus(creature);
			}

			double physicalSkillRate = creature.getStat().getValue(Stat.CRITICAL_RATE_SKILL);
			double defencePhysicalSkillCriticalRate = target.getStat().getValue(Stat.DEFENCE_PHYSICAL_SKILL_CRITICAL_RATE, 1.0) - 1.0;
			double physicalSkillRateFinal = (physicalSkillRate - target.getStat().getValue(Stat.DEFENCE_PHYSICAL_SKILL_CRITICAL_RATE_ADD, 0.0)) * statBonus * (1.0 - Math.min(0.8, defencePhysicalSkillCriticalRate));
			double balanceMod = 1.0;
			if (creature.isPlayable())
			{
				balanceMod = target.isPlayable() ? ClassBalanceConfig.PVP_PHYSICAL_SKILL_CRITICAL_CHANCE_MULTIPLIERS[creature.asPlayer().getPlayerClass().getId()] : ClassBalanceConfig.PVE_PHYSICAL_SKILL_CRITICAL_CHANCE_MULTIPLIERS[creature.asPlayer().getPlayerClass().getId()];
			}

			return MathUtil.clamp((rateValue / 100.0 + 1.0) * physicalSkillRateFinal * balanceMod, 30.0, PlayerConfig.MAX_PSKILLCRIT_RATE) > Rnd.get(1000);
		}
		double defenceCriticalRate = target.getStat().getValue(Stat.DEFENCE_CRITICAL_RATE, 1.0) - 1.0;
		double attackRateMod = (rateValue - target.getStat().getValue(Stat.DEFENCE_CRITICAL_RATE_ADD, 0.0)) * (1.0 - Math.min(0.8, defenceCriticalRate));
		double criticalPositionBonus = calcCriticalPositionBonus(creature, target);
		double criticalHeightBonus = calcCriticalHeightBonus(creature, target);
		double attackRate = attackRateMod * criticalPositionBonus * criticalHeightBonus;
		if (creature.getLevel() >= 78 || target.getLevel() >= 78)
		{
			attackRate += Math.sqrt(creature.getLevel()) * (creature.getLevel() - target.getLevel()) * 0.125;
		}

		double balanceMod = 1.0;
		if (creature.isPlayable())
		{
			balanceMod = target.isPlayable() ? ClassBalanceConfig.PVP_PHYSICAL_ATTACK_CRITICAL_CHANCE_MULTIPLIERS[creature.asPlayer().getPlayerClass().getId()] : ClassBalanceConfig.PVE_PHYSICAL_ATTACK_CRITICAL_CHANCE_MULTIPLIERS[creature.asPlayer().getPlayerClass().getId()];
		}

		return MathUtil.clamp(attackRate * balanceMod, 30.0, PlayerConfig.MAX_PCRIT_RATE) > Rnd.get(1000);
	}

	public static double calcCriticalPositionBonus(Creature creature, Creature target)
	{
		switch (Position.getPosition(creature, target))
		{
			case SIDE:
				return 1.1 * creature.getStat().getPositionTypeValue(Stat.CRITICAL_RATE, Position.SIDE);
			case BACK:
				return 1.3 * creature.getStat().getPositionTypeValue(Stat.CRITICAL_RATE, Position.BACK);
			default:
				return creature.getStat().getPositionTypeValue(Stat.CRITICAL_RATE, Position.FRONT);
		}
	}

	public static double calcCriticalHeightBonus(ILocational from, ILocational target)
	{
		return (MathUtil.clamp(from.getZ() - target.getZ(), -25, 25) * 4 / 5 + 10) / 100 + 1;
	}

	public static double calcCritDamage(Creature attacker, Creature target, Skill skill)
	{
		double balanceMod = 1.0;
		double criticalDamage;
		double defenceCriticalDamage;
		if (skill != null)
		{
			if (skill.isMagic())
			{
				criticalDamage = attacker.getStat().getValue(Stat.MAGIC_CRITICAL_DAMAGE, 1.0);
				defenceCriticalDamage = target.getStat().getValue(Stat.DEFENCE_MAGIC_CRITICAL_DAMAGE, 1.0);
				if (attacker.isPlayable())
				{
					balanceMod = target.isPlayable() ? ClassBalanceConfig.PVP_MAGICAL_SKILL_CRITICAL_DAMAGE_MULTIPLIERS[attacker.asPlayer().getPlayerClass().getId()] : ClassBalanceConfig.PVE_MAGICAL_SKILL_CRITICAL_DAMAGE_MULTIPLIERS[attacker.asPlayer().getPlayerClass().getId()];
				}
			}
			else
			{
				criticalDamage = attacker.getStat().getValue(Stat.PHYSICAL_SKILL_CRITICAL_DAMAGE, 1.0);
				defenceCriticalDamage = target.getStat().getValue(Stat.DEFENCE_PHYSICAL_SKILL_CRITICAL_DAMAGE, 1.0);
				if (attacker.isPlayable())
				{
					balanceMod = target.isPlayable() ? ClassBalanceConfig.PVP_PHYSICAL_SKILL_CRITICAL_DAMAGE_MULTIPLIERS[attacker.asPlayer().getPlayerClass().getId()] : ClassBalanceConfig.PVE_PHYSICAL_SKILL_CRITICAL_DAMAGE_MULTIPLIERS[attacker.asPlayer().getPlayerClass().getId()];
				}
			}
		}
		else
		{
			criticalDamage = attacker.getStat().getValue(Stat.CRITICAL_DAMAGE, 1.0) * attacker.getStat().getPositionTypeValue(Stat.CRITICAL_DAMAGE, Position.getPosition(attacker, target));
			defenceCriticalDamage = target.getStat().getValue(Stat.DEFENCE_CRITICAL_DAMAGE, 1.0);
			if (attacker.isPlayable())
			{
				balanceMod = target.isPlayable() ? ClassBalanceConfig.PVP_PHYSICAL_ATTACK_CRITICAL_DAMAGE_MULTIPLIERS[attacker.asPlayer().getPlayerClass().getId()] : ClassBalanceConfig.PVE_PHYSICAL_ATTACK_CRITICAL_DAMAGE_MULTIPLIERS[attacker.asPlayer().getPlayerClass().getId()];
			}
		}

		return 1.0 + 1.0 * criticalDamage * balanceMod * Math.max(0.2, 2.0 - defenceCriticalDamage);
	}

	public static double calcCritDamageAdd(Creature attacker, Creature target, Skill skill)
	{
		double criticalDamageAdd;
		double defenceCriticalDamageAdd;
		if (skill != null)
		{
			if (skill.isMagic())
			{
				criticalDamageAdd = attacker.getStat().getValue(Stat.MAGIC_CRITICAL_DAMAGE_ADD, 0.0);
				defenceCriticalDamageAdd = target.getStat().getValue(Stat.DEFENCE_MAGIC_CRITICAL_DAMAGE_ADD, 0.0);
			}
			else
			{
				criticalDamageAdd = attacker.getStat().getValue(Stat.PHYSICAL_SKILL_CRITICAL_DAMAGE_ADD, 0.0);
				defenceCriticalDamageAdd = target.getStat().getValue(Stat.DEFENCE_PHYSICAL_SKILL_CRITICAL_DAMAGE_ADD, 0.0);
			}
		}
		else
		{
			criticalDamageAdd = attacker.getStat().getValue(Stat.CRITICAL_DAMAGE_ADD, 0.0);
			defenceCriticalDamageAdd = target.getStat().getValue(Stat.DEFENCE_CRITICAL_DAMAGE_ADD, 0.0);
		}

		return Math.max(0.0, criticalDamageAdd - defenceCriticalDamageAdd);
	}

	public static boolean calcAtkBreak(Creature target, double dmg)
	{
		if (target.isChanneling())
		{
			return false;
		}
		else if (target.hasAbnormalType(AbnormalType.DC_MOD))
		{
			return false;
		}
		else
		{
			double init = 0.0;
			if (PlayerConfig.ALT_GAME_CANCEL_CAST && target.isCastingNow(SkillCaster::canAbortCast))
			{
				init = 15.0;
			}

			if (PlayerConfig.ALT_GAME_CANCEL_BOW && target.isAttackingNow())
			{
				Weapon wpn = target.getActiveWeaponItem();
				if (wpn != null && wpn.getItemType() == WeaponType.BOW)
				{
					init = 15.0;
				}
			}

			if (!target.isRaid() && !target.isHpBlocked() && !(init <= 0.0))
			{
				init += Math.sqrt(13.0 * dmg);
				init -= BaseStat.MEN.calcBonus(target) * 100.0 - 100.0;
				double rate = target.getStat().getValue(Stat.ATTACK_CANCEL, init);
				rate = Math.max(Math.min(rate, 99.0), 1.0);
				return Rnd.get(100) < rate;
			}
			return false;
		}
	}

	public static int calcAtkSpd(Creature attacker, Skill skill, double skillTime)
	{
		return skill.isMagic() ? (int) (skillTime / attacker.getMAtkSpd() * 333.0) : (int) (skillTime / attacker.getPAtkSpd() * 300.0);
	}

	public static double calcAtkSpdMultiplier(Creature creature)
	{
		double dexBonus = BaseStat.DEX.calcBonus(creature);
		double weaponAttackSpeed = Stat.weaponBaseValue(creature, Stat.PHYSICAL_ATTACK_SPEED) / 1.0;
		double attackSpeedPerBonus = creature.getStat().getMul(Stat.PHYSICAL_ATTACK_SPEED);
		double attackSpeedDiffBonus = creature.getStat().getAdd(Stat.PHYSICAL_ATTACK_SPEED);
		return dexBonus * (weaponAttackSpeed / 333.0) * attackSpeedPerBonus + attackSpeedDiffBonus / 333.0;
	}

	public static double calcMAtkSpdMultiplier(Creature creature)
	{
		double witBonus = BaseStat.WIT.calcBonus(creature);
		double castingSpeedPerBonus = creature.getStat().getMul(Stat.MAGIC_ATTACK_SPEED);
		double castingSpeedDiffBonus = creature.getStat().getAdd(Stat.MAGIC_ATTACK_SPEED);
		return 1.0 * witBonus * castingSpeedPerBonus + castingSpeedDiffBonus / 333.0;
	}

	public static double calcSkillTimeFactor(Creature creature, Skill skill)
	{
		if (!skill.getOperateType().isChanneling() && skill.getMagicType() != 2 && skill.getMagicType() != 4 && skill.getMagicType() != 21)
		{
			double factor = 0.0;
			if (skill.getMagicType() == 1)
			{
				double spiritshotHitTime = !creature.isChargedShot(ShotType.SPIRITSHOTS) && !creature.isChargedShot(ShotType.BLESSED_SPIRITSHOTS) ? 0.0 : 0.4;
				factor = creature.getStat().getMAttackSpeedMultiplier() + creature.getStat().getMAttackSpeedMultiplier() * spiritshotHitTime;
			}
			else
			{
				factor = creature.getAttackSpeedMultiplier();
			}

			if (creature.isNpc())
			{
				double npcFactor = creature.asNpc().getTemplate().getHitTimeFactorSkill();
				if (npcFactor > 0.0)
				{
					factor /= npcFactor;
				}
			}

			return Math.max(0.01, factor);
		}
		return 1.0;
	}

	public static double calcSkillCancelTime(Creature creature, Skill skill)
	{
		return Math.max(skill.getHitCancelTime() * 1000.0 / calcSkillTimeFactor(creature, skill), 500.0);
	}

	public static boolean calcHitMiss(Creature attacker, Creature target)
	{
		int chance = (80 + 2 * (attacker.getAccuracy() - target.getEvasionRate())) * 10;
		chance = (int) (chance * HitConditionBonusData.getInstance().getConditionBonus(attacker, target));
		chance = Math.max(chance, 200);
		chance = Math.min(chance, 980);
		return chance < Rnd.get(1000);
	}

	public static byte calcShldUse(Creature attacker, Creature target, boolean sendSysMsg)
	{
		ItemTemplate item = target.getSecondaryWeaponItem();
		if (item instanceof Armor && ((Armor) item).getItemType() != ArmorType.SIGIL)
		{
			double shldRate = target.getStat().getValue(Stat.SHIELD_DEFENCE_RATE) * BaseStat.CON.calcBonus(target);
			if (attacker.getAttackType().isRanged())
			{
				shldRate *= 1.3;
			}

			int degreeside = target.isAffected(EffectFlag.PHYSICAL_SHIELD_ANGLE_ALL) ? 360 : 120;
			if (degreeside < 360 && Math.abs(target.calculateDirectionTo(attacker) - LocationUtil.convertHeadingToDegree(target.getHeading())) > degreeside / 2)
			{
				return 0;
			}
			byte shldSuccess = 0;
			if (shldRate > Rnd.get(100))
			{
				if (100.0 - 2.0 * BaseStat.CON.calcBonus(target) < Rnd.get(100))
				{
					shldSuccess = 2;
				}
				else
				{
					shldSuccess = 1;
				}
			}

			if (sendSysMsg && target.isPlayer())
			{
				Player enemy = target.asPlayer();
				switch (shldSuccess)
				{
					case 1:
						enemy.sendPacket(SystemMessageId.YOU_VE_BLOCKED_THE_ATTACK);
						break;
					case 2:
						enemy.sendPacket(SystemMessageId.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
				}
			}

			return shldSuccess;
		}
		return 0;
	}

	public static byte calcShldUse(Creature attacker, Creature target)
	{
		return calcShldUse(attacker, target, true);
	}

	public static boolean calcMagicAffected(Creature actor, Creature target, Skill skill)
	{
		double defence = 0.0;
		if (skill.isActive() && skill.hasNegativeEffect())
		{
			defence = target.getMDef();
		}

		if (skill.isDebuff())
		{
			if (target.getAbnormalShieldBlocks() > 0)
			{
				target.decrementAbnormalShieldBlocks();
				return false;
			}

			if (target.isAffected(EffectFlag.DEBUFF_BLOCK))
			{
				return false;
			}
		}

		double attack = 2 * actor.getMAtk() * calcGeneralTraitBonus(actor, target, skill.getTraitType(), false);
		double d = (attack - defence) / (attack + defence);
		d += 0.5 * Rnd.nextGaussian();
		return d > 0.0;
	}

	public static double calcLvlBonusMod(Creature attacker, Creature target, Skill skill)
	{
		int attackerLvl = skill.getMagicLevel() > 0 ? skill.getMagicLevel() : attacker.getLevel();
		double skillLevelBonusRateMod = 1.0 + skill.getLvlBonusRate() / 100.0;
		double lvlMod = 1.0 + (attackerLvl - target.getLevel()) / 100.0;
		return skillLevelBonusRateMod * lvlMod;
	}

	public static boolean calcEffectSuccess(Creature attacker, Creature target, Skill skill)
	{
		if (!target.isDoor() && !(target instanceof SiegeFlag) && !(target instanceof StaticObject))
		{
			if (skill.isDebuff())
			{
				boolean resisted = target.isCastingNow(s -> s.getSkill().getAbnormalResists().contains(skill.getAbnormalType()));
				if (!resisted && target.getAbnormalShieldBlocks() > 0)
				{
					target.decrementAbnormalShieldBlocks();
					resisted = true;
				}

				if (!resisted)
				{
					double sphericBarrierRange = target.getStat().getValue(Stat.SPHERIC_BARRIER_RANGE, 0.0);
					if (sphericBarrierRange > 0.0)
					{
						resisted = attacker.calculateDistance3D(target) > sphericBarrierRange;
					}
				}

				if (resisted)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.C1_HAS_RESISTED_YOUR_S2);
					sm.addString(target.getName());
					sm.addSkillName(skill);
					attacker.sendPacket(sm);
					attacker.sendPacket(new ExMagicAttackInfo(attacker.getObjectId(), target.getObjectId(), 4));
					return false;
				}
			}

			int activateRate = skill.getActivateRate();
			if (activateRate != -1 && activateRate <= 99)
			{
				int magicLevel = skill.getMagicLevel();
				if (magicLevel <= -1)
				{
					magicLevel = target.getLevel() + 3;
				}

				double targetBasicProperty = getAbnormalResist(skill.getBasicProperty(), target);
				double baseMod = (magicLevel - target.getLevel() + 3) * skill.getLvlBonusRate() + activateRate + 30.0 - targetBasicProperty;
				double elementMod = calcAttributeBonus(attacker, target, skill);
				double traitMod = calcGeneralTraitBonus(attacker, target, skill.getTraitType(), false);
				double basicPropertyResist = getBasicPropertyResistBonus(skill.getBasicProperty(), target);
				double buffDebuffMod = skill.isDebuff() ? target.getStat().getValue(Stat.RESIST_ABNORMAL_DEBUFF, 1.0) : 1.0;
				double rate = baseMod * elementMod * traitMod * buffDebuffMod;
				double finalRate = traitMod > 0.0 ? MathUtil.clamp(rate, skill.getMinChance(), skill.getMaxChance()) * basicPropertyResist : 0.0;
				if (finalRate <= Rnd.get(100) && target != attacker)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.C1_HAS_RESISTED_YOUR_S2);
					sm.addString(target.getName());
					sm.addSkillName(skill);
					attacker.sendPacket(sm);
					attacker.sendPacket(new ExMagicAttackInfo(attacker.getObjectId(), target.getObjectId(), 4));
					return false;
				}
				return true;
			}
			return true;
		}
		return false;
	}

	public static boolean calcMagicSuccess(Creature attacker, Creature target, Skill skill)
	{
		double lvlModifier = 1.0;
		float targetModifier = 1.0F;
		int mAccModifier = 1;
		if (!attacker.isAttackable() && !target.isAttackable())
		{
			int mAccDiff = attacker.getMagicAccuracy() - target.getMagicEvasionRate();
			mAccModifier = 100;
			if (mAccDiff > -20)
			{
				mAccModifier = 2;
			}
			else if (mAccDiff > -25)
			{
				mAccModifier = 30;
			}
			else if (mAccDiff > -30)
			{
				mAccModifier = 60;
			}
			else if (mAccDiff > -35)
			{
				mAccModifier = 90;
			}
		}
		else
		{
			lvlModifier = Math.pow(1.3, target.getLevel() - (PlayerConfig.CALCULATE_MAGIC_SUCCESS_BY_SKILL_MAGIC_LEVEL && skill.getMagicLevel() > 0 ? skill.getMagicLevel() : attacker.getLevel()));
			Player attackerPlayer = attacker.asPlayer();
			if (attackerPlayer != null && !target.isRaid() && !target.isRaidMinion() && target.getLevel() >= NpcConfig.MIN_NPC_LEVEL_MAGIC_PENALTY && target.getLevel() - attackerPlayer.getLevel() >= 3)
			{
				int levelDiff = target.getLevel() - attackerPlayer.getLevel() - 2;
				if (levelDiff >= NpcConfig.NPC_SKILL_CHANCE_PENALTY.length)
				{
					targetModifier = NpcConfig.NPC_SKILL_CHANCE_PENALTY[NpcConfig.NPC_SKILL_CHANCE_PENALTY.length - 1];
				}
				else
				{
					targetModifier = NpcConfig.NPC_SKILL_CHANCE_PENALTY[levelDiff];
				}
			}
		}

		double resModifier = target.getStat().getMul(Stat.MAGIC_SUCCESS_RES, 1.0);
		int rate = 100 - Math.round((float) (mAccModifier * lvlModifier * targetModifier * resModifier));
		return Rnd.get(100) < rate;
	}

	public static double calcManaDam(Creature attacker, Creature target, Skill skill, double power, byte shld, boolean sps, boolean bss, boolean mcrit, double critLimit)
	{
		double mAtk = attacker.getMAtk();
		double mDef = target.getMDef();
		double mp = target.getMaxMp();
		switch (shld)
		{
			case 1:
				mDef += target.getShldDef();
			default:
				double shotsBonus = attacker.getStat().getValue(Stat.SHOTS_BONUS) / 100.0 - target.getStat().getValue(Stat.SPIRITSHOT_RESISTANCE, 0.0) / 100.0;
				double sapphireBonus = 0.0;
				if (attacker.isPlayer())
				{
					BroochJewel jewel = attacker.asPlayer().getActiveShappireJewel();
					if (jewel != null)
					{
						sapphireBonus = jewel.getBonus();
					}
				}

				mAtk *= bss ? 4.0 + (shotsBonus + sapphireBonus) : (sps ? 2.0 + (shotsBonus + sapphireBonus) : 1.0);
				double damage = Math.sqrt(mAtk) * power * (mp / 97.0) / mDef;
				damage *= calcGeneralTraitBonus(attacker, target, skill.getTraitType(), false);
				damage *= calculatePvpPveBonus(attacker, target, skill, mcrit);
				if (PlayerConfig.ALT_GAME_MAGICFAILURES && !calcMagicSuccess(attacker, target, skill))
				{
					if (attacker.isPlayer())
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.C1_HAS_RESISTED_C2_S_MAGIC_DAMAGE_IS_DECREASED);
						sm.addString(target.getName());
						sm.addString(attacker.getName());
						attacker.sendPacket(sm);
						damage /= 2.0;
					}

					if (target.isPlayer())
					{
						SystemMessage sm2 = new SystemMessage(SystemMessageId.C1_WEAKLY_RESISTED_C2_S_MAGIC);
						sm2.addString(target.getName());
						sm2.addString(attacker.getName());
						target.sendPacket(sm2);
					}
				}

				if (mcrit)
				{
					damage *= 3.0;
					damage = Math.min(damage, critLimit);
					attacker.sendPacket(SystemMessageId.M_CRITICAL);
				}

				return damage;
			case 2:
				return 1.0;
		}
	}

	public static double calculateSkillResurrectRestorePercent(double baseRestorePercent, Creature caster)
	{
		if (baseRestorePercent != 0.0 && baseRestorePercent != 100.0)
		{
			double restorePercent = baseRestorePercent * BaseStat.WIT.calcBonus(caster);
			if (restorePercent - baseRestorePercent > 20.0)
			{
				restorePercent += 20.0;
			}

			restorePercent = Math.max(restorePercent, baseRestorePercent);
			return Math.min(restorePercent, 90.0);
		}
		return baseRestorePercent;
	}

	public static boolean calcSkillEvasion(Creature creature, Creature target, Skill skill)
	{
		if (Rnd.get(100) < target.getStat().getSkillEvasionTypeValue(skill.getMagicType()))
		{
			if (creature.isPlayer())
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.C1_DODGED_THE_ATTACK);
				sm.addString(target.getName());
				creature.asPlayer().sendPacket(sm);
			}

			if (target.isPlayer())
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_DODGED_C1_S_ATTACK);
				sm.addString(creature.getName());
				target.asPlayer().sendPacket(sm);
			}

			return true;
		}
		return false;
	}

	public static boolean calcSkillMastery(Creature actor, Skill skill)
	{
		if (!actor.isPlayer())
		{
			return false;
		}
		int val = (int) actor.getStat().getAdd(Stat.SKILL_MASTERY, -1.0);
		if (val == -1)
		{
			return false;
		}
		double chance = BaseStat.values()[val].calcBonus(actor) * actor.getStat().getMul(Stat.SKILL_MASTERY_RATE, 1.0);
		return Rnd.nextDouble() * 100.0 < chance * ClassBalanceConfig.SKILL_MASTERY_CHANCE_MULTIPLIERS[actor.asPlayer().getPlayerClass().getId()];
	}

	public static double calcAttributeBonus(Creature attacker, Creature target, Skill skill)
	{
		int attackAttribute;
		int defenceAttribute;
		if (skill != null && skill.getAttributeType() != AttributeType.NONE)
		{
			attackAttribute = attacker.getAttackElementValue(skill.getAttributeType()) + skill.getAttributeValue();
			defenceAttribute = target.getDefenseElementValue(skill.getAttributeType());
		}
		else
		{
			attackAttribute = attacker.getAttackElementValue(attacker.getAttackElement());
			defenceAttribute = target.getDefenseElementValue(attacker.getAttackElement());
		}

		int diff = attackAttribute - defenceAttribute;
		if (diff > 0)
		{
			return Math.min(1.025 + Math.sqrt(Math.pow(diff, 3.0) / 2.0) * 1.0E-4, 1.25);
		}
		return diff < 0 ? Math.max(0.975 - Math.sqrt(Math.pow(-diff, 3.0) / 2.0) * 1.0E-4, 0.75) : 1.0;
	}

	public static void calcCounterAttack(Creature attacker, Creature target, Skill skill, boolean crit)
	{
		if (!skill.isMagic() && skill.getCastRange() <= 40)
		{
			double chance = target.getStat().getValue(Stat.VENGEANCE_SKILL_PHYSICAL_DAMAGE, 0.0);
			if (Rnd.get(100) < chance)
			{
				if (target.isPlayer())
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_COUNTERED_C1_S_ATTACK);
					sm.addString(attacker.getName());
					target.sendPacket(sm);
				}

				if (attacker.isPlayer())
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.C1_IS_PERFORMING_A_COUNTERATTACK);
					sm.addString(target.getName());
					attacker.sendPacket(sm);
				}

				double counterdmg = target.getPAtk() * 873 / attacker.getPDef();
				counterdmg *= calcWeaponTraitBonus(attacker, target);
				counterdmg *= calcGeneralTraitBonus(attacker, target, skill.getTraitType(), true);
				counterdmg *= calcAttributeBonus(attacker, target, skill);
				attacker.reduceCurrentHp(counterdmg, target, skill);
			}
		}
	}

	public static boolean calcBuffDebuffReflection(Creature target, Skill skill)
	{
		return skill.isDebuff() && skill.getActivateRate() != -1 ? target.getStat().getValue(skill.isMagic() ? Stat.REFLECT_SKILL_MAGIC : Stat.REFLECT_SKILL_PHYSIC, 0.0) > Rnd.get(100) : false;
	}

	public static double calcFallDam(Creature creature, int fallHeight)
	{
		return GeneralConfig.ENABLE_FALLING_DAMAGE && fallHeight >= 0 ? creature.getStat().getValue(Stat.FALL, fallHeight * creature.getMaxHp() / 1000.0) : 0.0;
	}

	public static boolean calcBlowSuccess(Creature creature, Creature target, Skill skill, double chanceBoost)
	{
		Weapon weapon = creature.getActiveWeaponItem();
		double weaponCritical = weapon != null ? weapon.getStats(Stat.CRITICAL_RATE, creature.getTemplate().getBaseCritRate()) : creature.getTemplate().getBaseCritRate();
		double critHeightBonus = calcCriticalHeightBonus(creature, target);
		double criticalPosition = calcCriticalPositionBonus(creature, target);
		double chanceBoostMod = (100.0 + chanceBoost) / 100.0;
		double blowRateMod = creature.getStat().getValue(Stat.BLOW_RATE, 1.0);
		double blowRateDefenseMod = target.getStat().getValue(Stat.BLOW_RATE_DEFENCE, 1.0);
		double rate = criticalPosition * critHeightBonus * weaponCritical * chanceBoostMod * blowRateMod * blowRateDefenseMod;
		return Rnd.get(100) < Math.min(rate, PlayerConfig.BLOW_RATE_CHANCE_LIMIT);
	}

	public static List<BuffInfo> calcCancelStealEffects(Creature creature, Creature target, Skill skill, DispelSlotType slot, int rate, int max)
	{
		List<BuffInfo> canceled = new ArrayList<>(max);
		switch (slot)
		{
			case BUFF:
				int cancelMagicLvl = skill == null ? creature.getLevel() : skill.getMagicLevel();
				List<BuffInfo> dances = target.getEffectList().getDances();

				for (int ix = dances.size() - 1; ix >= 0; ix--)
				{
					BuffInfo info = dances.get(ix);
					if (info.getSkill().canBeStolen() && (rate >= 100 || calcCancelSuccess(info, cancelMagicLvl, rate, skill, target)))
					{
						canceled.add(info);
						if (canceled.size() >= max)
						{
							break;
						}
					}
				}

				if (canceled.size() < max)
				{
					List<BuffInfo> buffs = target.getEffectList().getBuffs();

					for (int ixx = buffs.size() - 1; ixx >= 0; ixx--)
					{
						BuffInfo info = buffs.get(ixx);
						if (info.getSkill().canBeStolen() && (rate >= 100 || calcCancelSuccess(info, cancelMagicLvl, rate, skill, target)))
						{
							canceled.add(info);
							if (canceled.size() >= max)
							{
								return canceled;
							}
						}
					}
				}
				break;
			case DEBUFF:
				List<BuffInfo> debuffs = target.getEffectList().getDebuffs();

				for (int i = debuffs.size() - 1; i >= 0; i--)
				{
					BuffInfo info = debuffs.get(i);
					if (info.getSkill().canBeDispelled() && Rnd.get(100) <= rate)
					{
						canceled.add(info);
						if (canceled.size() >= max)
						{
							break;
						}
					}
				}
		}

		return canceled;
	}

	public static boolean calcCancelSuccess(BuffInfo info, int cancelMagicLvl, int rate, Skill skill, Creature target)
	{
		int chance = (int) (rate + (cancelMagicLvl - info.getSkill().getMagicLevel()) * 2 + info.getAbnormalTime() / 120 * target.getStat().getValue(Stat.RESIST_DISPEL_BUFF, 1.0));
		return Rnd.get(100) < MathUtil.clamp(chance, 25, 75);
	}

	public static boolean calcCancelSuccess(int rate, Creature caster, Creature target)
	{
		int chance = (int) (rate + (caster.getLevel() - target.getLevel()) * 2 + target.getStat().getValue(Stat.RESIST_DISPEL_BUFF, 1.0));
		return Rnd.get(100) < chance;
	}

	public static int calcEffectAbnormalTime(Creature caster, Creature target, Skill skill)
	{
		int time = skill != null && !skill.isPassive() && !skill.isToggle() ? skill.getAbnormalTime() : -1;
		if (skill != null && !skill.isStatic())
		{
			if (time > 0)
			{
				double multipliedAbnormalTime = target.getMultipliedAbnormalTime(skill.getId());
				if (multipliedAbnormalTime != 0.0)
				{
					time = (int) (time * multipliedAbnormalTime);
				}

				int addedAbnormalTime = target.getAddedAbnormalTime(skill.getId());
				if (addedAbnormalTime != 0)
				{
					time += addedAbnormalTime;
				}
			}

			if (calcSkillMastery(caster, skill))
			{
				time *= 2;
			}
		}

		return time;
	}

	public static boolean calcProbability(double baseChance, Creature attacker, Creature target, Skill skill)
	{
		return Double.isNaN(baseChance) ? calcGeneralTraitBonus(attacker, target, skill.getTraitType(), true) > 0.0 : Rnd.get(100) < (skill.getMagicLevel() + baseChance - target.getLevel() - getAbnormalResist(skill.getBasicProperty(), target)) * calcAttributeBonus(attacker, target, skill) * calcGeneralTraitBonus(attacker, target, skill.getTraitType(), false);
	}

	public static int calculateKarmaLost(Player player, double finalExp)
	{
		double karmaLossMod = KarmaLossData.getInstance().getModifier(player.getLevel());
		return finalExp > 0.0 ? (int) (Math.abs(finalExp / RatesConfig.RATE_KARMA_LOST) / karmaLossMod / 30.0) : (int) (Math.abs(finalExp) / karmaLossMod / 30.0);
	}

	public static int calculateKarmaGain(int pkCount, boolean isSummon)
	{
		int result = 43200;
		if (isSummon)
		{
			result = (int) ((pkCount * 0.375 + 1.0) * 60.0 * 4.0) - 150;
			if (result > 10800)
			{
				return 10800;
			}
		}

		if (pkCount < 99)
		{
			result = (int) ((pkCount * 0.5 + 1.0) * 60.0 * 12.0);
		}
		else if (pkCount < 180)
		{
			result = (int) ((pkCount * 0.125 + 37.75) * 60.0 * 12.0);
		}

		return result;
	}

	public static double calcGeneralTraitBonus(Creature attacker, Creature target, TraitType traitType, boolean ignoreResistance)
	{
		if (traitType == TraitType.NONE)
		{
			return 1.0;
		}
		else if (target.getStat().isInvulnerableTrait(traitType))
		{
			return 0.0;
		}
		else
		{
			switch (traitType.getType())
			{
				case 2:
					if (!attacker.getStat().hasAttackTrait(traitType) || !target.getStat().hasDefenceTrait(traitType))
					{
						return 1.0;
					}
					break;
				case 3:
					if (ignoreResistance)
					{
						return 1.0;
					}
					break;
				default:
					return 1.0;
			}

			return Math.max(attacker.getStat().getAttackTrait(traitType) - target.getStat().getDefenceTrait(traitType), 0.05);
		}
	}

	public static double calcWeaknessBonus(Creature attacker, Creature target, TraitType traitType)
	{
		double result = 1.0;

		for (TraitType trait : TraitType.getAllWeakness())
		{
			if (traitType != trait && target.getStat().hasDefenceTrait(trait) && attacker.getStat().hasAttackTrait(trait) && !target.getStat().isInvulnerableTrait(traitType))
			{
				result *= Math.max(attacker.getStat().getAttackTrait(trait) - target.getStat().getDefenceTrait(trait), 0.05);
			}
		}

		return result;
	}

	public static double calcWeaponTraitBonus(Creature attacker, Creature target)
	{
		return Math.max(0.22, 1.0 - target.getStat().getDefenceTrait(attacker.getAttackType().getTraitType()));
	}

	public static double calcAttackTraitBonus(Creature attacker, Creature target)
	{
		double weaponTraitBonus = calcWeaponTraitBonus(attacker, target);
		if (weaponTraitBonus == 0.0)
		{
			return 0.0;
		}
		double weaknessBonus = 1.0;

		for (TraitType traitType : TraitType.values())
		{
			if (traitType.getType() == 2)
			{
				weaknessBonus *= calcGeneralTraitBonus(attacker, target, traitType, true);
				if (weaknessBonus == 0.0)
				{
					return 0.0;
				}
			}
		}

		return Math.max(weaponTraitBonus * weaknessBonus, 0.05);
	}

	public static double getBasicPropertyResistBonus(BasicProperty basicProperty, Creature target)
	{
		if (basicProperty != BasicProperty.NONE && target.hasBasicPropertyResist())
		{
			BasicPropertyResist resist = target.getBasicPropertyResist(basicProperty);
			switch (resist.getResistLevel())
			{
				case 0:
					return 1.0;
				case 1:
					return 0.6;
				case 2:
					return 0.3;
				default:
					return 0.0;
			}
		}
		return 1.0;
	}

	public static double calcAutoAttackDamage(Creature attacker, Creature target, byte shld, boolean crit, boolean ss, boolean ssBlessed)
	{
		double defence = target.getPDef();
		switch (shld)
		{
			case 1:
				defence += target.getShldDef();
				break;
			case 2:
				return 1.0;
		}

		Weapon weapon = attacker.getActiveWeaponItem();
		boolean isRanged = weapon != null && weapon.getItemType().isRanged();
		double shotsBonus = attacker.getStat().getValue(Stat.SHOTS_BONUS) / 100.0 - target.getStat().getValue(Stat.SOULSHOT_RESISTANCE, 0.0) / 100.0;
		double cAtk = crit ? calcCritDamage(attacker, target, null) : 1.0;
		double cAtkAdd = crit ? calcCritDamageAdd(attacker, target, null) : 0.0;
		double critMod = crit ? (isRanged ? 0.5 : 1.0) : 0.0;
		double ssBonus = ss ? (ssBlessed ? 4 : 2) + shotsBonus : 1.0;
		double randomDamage = attacker.getRandomDamageMultiplier();
		double proxBonus = (attacker.isInFrontOf(target) ? 0.0 : (attacker.isBehind(target) ? 0.2 : 0.05)) * attacker.getPAtk();
		double attack = attacker.getPAtk() * randomDamage + proxBonus;
		attack = (attack * cAtk * ssBonus + cAtkAdd) * critMod * (isRanged ? 154 : 77) + attack * (1.0 - critMod) * ssBonus * (isRanged ? 154 : 77);
		double damage = attack / defence;
		damage *= calcAttackTraitBonus(attacker, target);
		damage *= calcAttributeBonus(attacker, target, null);
		damage *= calculatePvpPveBonus(attacker, target, null, crit);
		damage *= attacker.getStat().getMul(Stat.AUTO_ATTACK_DAMAGE_BONUS);
		return Math.max(1.0, damage);
	}

	public static double getAbnormalResist(BasicProperty basicProperty, Creature target)
	{
		switch (basicProperty)
		{
			case PHYSICAL:
				return target.getStat().getValue(Stat.ABNORMAL_RESIST_PHYSICAL);
			case MAGIC:
				return target.getStat().getValue(Stat.ABNORMAL_RESIST_MAGICAL);
			default:
				return 0.0;
		}
	}

	public static boolean calcStunBreak(Creature creature)
	{
		return PlayerConfig.ALT_GAME_STUN_BREAK && creature.hasBlockActions() && Rnd.get(14) == 0 ? creature.getEffectList().hasAbnormalType(AbnormalType.STUN, info -> info.getTime() <= info.getSkill().getAbnormalTime()) : false;
	}

	public static boolean calcRealTargetBreak()
	{
		return Rnd.get(100) <= 3;
	}

	public static int calculateTimeBetweenAttacks(int attackSpeed)
	{
		return Math.max(50, 500000 / attackSpeed);
	}

	public static int calculateTimeToHit(int totalAttackTime, WeaponType attackType, boolean twoHanded, boolean secondHit)
	{
		switch (attackType)
		{
			case BOW:
			case CROSSBOW:
			case TWOHANDCROSSBOW:
				return (int) (totalAttackTime * 0.95);
			case DUALBLUNT:
			case DUALDAGGER:
			case DUAL:
			case DUALFIST:
				if (secondHit)
				{
					return (int) (totalAttackTime * 0.6);
				}

				return (int) (totalAttackTime * 0.2726);
			default:
				return twoHanded ? (int) (totalAttackTime * 0.735) : (int) (totalAttackTime * 0.644);
		}
	}

	public static int calculateReuseTime(Creature creature, Weapon weapon)
	{
		if (weapon == null)
		{
			return 0;
		}
		WeaponType defaultAttackType = weapon.getItemType();
		Transform transform = creature.getTransformation();
		WeaponType weaponType = transform == null ? defaultAttackType : transform.getBaseAttackType(creature, defaultAttackType);
		return !weaponType.isRanged() ? 0 : 900000 / creature.getStat().getPAtkSpd();
	}

	public static double calculatePvpPveBonus(Creature attacker, Creature target, Skill skill, boolean crit)
	{
		Player attackerPlayer = attacker.asPlayer();
		Player targetPlayer = attacker.asPlayer();
		if (attacker.isPlayable() && target.isPlayable())
		{
			double pvpAttack;
			double pvpDefense;
			if (skill != null)
			{
				if (skill.isMagic())
				{
					pvpAttack = attacker.getStat().getMul(Stat.PVP_MAGICAL_SKILL_DAMAGE, 1.0) * ClassBalanceConfig.PVP_MAGICAL_SKILL_DAMAGE_MULTIPLIERS[attackerPlayer.getPlayerClass().getId()];
					pvpDefense = target.getStat().getMul(Stat.PVP_MAGICAL_SKILL_DEFENCE, 1.0) * ClassBalanceConfig.PVP_MAGICAL_SKILL_DEFENCE_MULTIPLIERS[targetPlayer.getPlayerClass().getId()];
				}
				else
				{
					pvpAttack = attacker.getStat().getMul(Stat.PVP_PHYSICAL_SKILL_DAMAGE, 1.0) * ClassBalanceConfig.PVP_PHYSICAL_SKILL_DAMAGE_MULTIPLIERS[attackerPlayer.getPlayerClass().getId()];
					pvpDefense = target.getStat().getMul(Stat.PVP_PHYSICAL_SKILL_DEFENCE, 1.0) * ClassBalanceConfig.PVP_PHYSICAL_SKILL_DEFENCE_MULTIPLIERS[targetPlayer.getPlayerClass().getId()];
				}
			}
			else
			{
				pvpAttack = attacker.getStat().getMul(Stat.PVP_PHYSICAL_ATTACK_DAMAGE, 1.0) * ClassBalanceConfig.PVP_PHYSICAL_ATTACK_DAMAGE_MULTIPLIERS[attackerPlayer.getPlayerClass().getId()];
				pvpDefense = target.getStat().getMul(Stat.PVP_PHYSICAL_ATTACK_DEFENCE, 1.0) * ClassBalanceConfig.PVP_PHYSICAL_ATTACK_DEFENCE_MULTIPLIERS[targetPlayer.getPlayerClass().getId()];
			}

			return Math.max(0.05, 1.0 + (pvpAttack - pvpDefense));
		}
		else if (!target.isAttackable() && !attacker.isAttackable())
		{
			return 1.0;
		}
		else
		{
			double pvePenalty = 1.0;
			if (!target.isRaid() && !target.isRaidMinion() && target.getLevel() >= NpcConfig.MIN_NPC_LEVEL_DMG_PENALTY && attackerPlayer != null && target.getLevel() - attackerPlayer.getLevel() >= 2)
			{
				int levelDiff = target.getLevel() - attackerPlayer.getLevel() - 1;
				if (levelDiff >= NpcConfig.NPC_SKILL_DMG_PENALTY.length)
				{
					pvePenalty = NpcConfig.NPC_SKILL_DMG_PENALTY[NpcConfig.NPC_SKILL_DMG_PENALTY.length - 1];
				}
				else
				{
					pvePenalty = NpcConfig.NPC_SKILL_DMG_PENALTY[levelDiff];
				}
			}

			double pveAttack;
			double pveDefense;
			double pveRaidAttack;
			double pveRaidDefense;
			if (skill != null)
			{
				if (skill.isMagic())
				{
					pveAttack = attacker.getStat().getMul(Stat.PVE_MAGICAL_SKILL_DAMAGE, 1.0) * (attackerPlayer == null ? 1.0F : ClassBalanceConfig.PVE_MAGICAL_SKILL_DAMAGE_MULTIPLIERS[attackerPlayer.getPlayerClass().getId()]);
					pveDefense = target.getStat().getMul(Stat.PVE_MAGICAL_SKILL_DEFENCE, 1.0) * (targetPlayer == null ? 1.0F : ClassBalanceConfig.PVE_MAGICAL_SKILL_DEFENCE_MULTIPLIERS[targetPlayer.getPlayerClass().getId()]);
					pveRaidAttack = attacker.isRaid() ? attacker.getStat().getMul(Stat.PVE_RAID_MAGICAL_SKILL_DAMAGE, 1.0) : 1.0;
					pveRaidDefense = attacker.isRaid() ? attacker.getStat().getMul(Stat.PVE_RAID_MAGICAL_SKILL_DEFENCE, 1.0) : 1.0;
				}
				else
				{
					pveAttack = attacker.getStat().getMul(Stat.PVE_PHYSICAL_SKILL_DAMAGE, 1.0) * (attackerPlayer == null ? 1.0F : ClassBalanceConfig.PVE_PHYSICAL_SKILL_DAMAGE_MULTIPLIERS[attackerPlayer.getPlayerClass().getId()]);
					pveDefense = target.getStat().getMul(Stat.PVE_PHYSICAL_SKILL_DEFENCE, 1.0) * (targetPlayer == null ? 1.0F : ClassBalanceConfig.PVE_PHYSICAL_SKILL_DEFENCE_MULTIPLIERS[targetPlayer.getPlayerClass().getId()]);
					pveRaidAttack = attacker.isRaid() ? attacker.getStat().getMul(Stat.PVE_RAID_PHYSICAL_SKILL_DAMAGE, 1.0) : 1.0;
					pveRaidDefense = attacker.isRaid() ? attacker.getStat().getMul(Stat.PVE_RAID_PHYSICAL_SKILL_DEFENCE, 1.0) : 1.0;
				}
			}
			else
			{
				pveAttack = attacker.getStat().getMul(Stat.PVE_PHYSICAL_ATTACK_DAMAGE, 1.0) * (attackerPlayer == null ? 1.0F : ClassBalanceConfig.PVE_PHYSICAL_ATTACK_DAMAGE_MULTIPLIERS[attackerPlayer.getPlayerClass().getId()]);
				pveDefense = target.getStat().getMul(Stat.PVE_PHYSICAL_ATTACK_DEFENCE, 1.0) * (targetPlayer == null ? 1.0F : ClassBalanceConfig.PVE_PHYSICAL_ATTACK_DEFENCE_MULTIPLIERS[targetPlayer.getPlayerClass().getId()]);
				pveRaidAttack = attacker.isRaid() ? attacker.getStat().getMul(Stat.PVE_RAID_PHYSICAL_ATTACK_DAMAGE, 1.0) : 1.0;
				pveRaidDefense = attacker.isRaid() ? attacker.getStat().getMul(Stat.PVE_RAID_PHYSICAL_ATTACK_DEFENCE, 1.0) : 1.0;
			}

			return Math.max(0.05, (1.0 + (pveAttack * pveRaidAttack - pveDefense * pveRaidDefense)) * pvePenalty);
		}
	}

	public static boolean calcSpiritElementalCrit(Creature attacker, Creature target)
	{
		if (attacker.isPlayer())
		{
			Player attackerPlayer = attacker.asPlayer();
			ElementalSpiritType type = ElementalSpiritType.of(attackerPlayer.getActiveElementalSpiritType());
			if (ElementalSpiritType.NONE == type)
			{
				return false;
			}
			double critRate = attackerPlayer.getElementalSpiritCritRate();
			return Math.min(critRate * 10.0, 380.0) > Rnd.get(1000);
		}
		return false;
	}

	public static double calcSpiritElementalDamage(Creature attacker, Creature target, double baseDamage, boolean isCrit)
	{
		if (attacker.isPlayer())
		{
			Player attackerPlayer = attacker.asPlayer();
			ElementalSpiritType type = ElementalSpiritType.of(attackerPlayer.getActiveElementalSpiritType());
			if (ElementalSpiritType.NONE == type)
			{
				return 0.0;
			}
			double critDamage = attackerPlayer.getElementalSpiritCritDamage();
			double attack = attackerPlayer.getActiveElementalSpiritAttack() - target.getElementalSpiritDefenseOf(type) + Rnd.get(-2, 6);
			return target.isPlayer() ? calcSpiritElementalPvPDamage(attack, critDamage, isCrit, baseDamage) : calcSpiritElementalPvEDamage(type, target.getElementalSpiritType(), attack, critDamage, isCrit, baseDamage);
		}
		return 0.0;
	}

	private static double calcSpiritElementalPvPDamage(double attack, double critDamage, boolean isCrit, double baseDamage)
	{
		double damage = Math.min(Math.max(0.0, (attack * 1.3 + baseDamage * 0.03 * attack) / Math.log(Math.max(attack, 5.0))), 2295.0);
		if (isCrit)
		{
			damage *= 1.0 + (Rnd.get(13, 20) + critDamage) / 100.0;
		}

		return damage;
	}

	private static double calcSpiritElementalPvEDamage(ElementalSpiritType attackerType, ElementalSpiritType targetType, double attack, double critDamage, boolean isCrit, double baseDamage)
	{
		double damage = Math.abs(attack * 0.8);
		double bonus;
		if (attackerType.isSuperior(targetType))
		{
			damage *= 1.3;
			bonus = 1.3;
		}
		else if (targetType == attackerType)
		{
			bonus = 1.1;
		}
		else
		{
			damage *= 1.1;
			bonus = 1.1;
		}

		if (isCrit)
		{
			damage += Math.abs((40.0 + (9.2 + attack * 0.048) * critDamage) * bonus + Rnd.get(-10, 30));
		}

		return (damage + baseDamage) * bonus / Math.log(20.0 + baseDamage + damage);
	}
}
