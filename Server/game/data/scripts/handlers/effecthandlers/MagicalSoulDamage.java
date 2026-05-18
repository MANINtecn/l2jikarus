/*
 * This file is part of the L2J BAN-JDEV project.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.effecthandlers;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.effects.AbstractEffect;
import net.sf.l2jdev.gameserver.model.effects.EffectType;
import net.sf.l2jdev.gameserver.model.item.enums.ShotType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.stats.Formulas;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

/**
 * Magical soul damage effect implementation.
 * @author Adry_85
 */
public class MagicalSoulDamage extends AbstractEffect
{
	private final double _power;

	public MagicalSoulDamage(StatSet params)
	{
		_power = params.getDouble("power", 0);

		if (params.contains("amount"))
		{
			throw new IllegalArgumentException(getClass().getSimpleName() + " should use power instead of amount.");
		}
	}

	@Override
	public boolean calcSuccess(Creature effector, Creature effected, Skill skill)
	{
		return !Formulas.calcSkillEvasion(effector, effected, skill);
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.MAGICAL_ATTACK;
	}

	@Override
	public boolean isInstant()
	{
		return true;
	}

	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		if (effector.isAlikeDead())
		{
			return;
		}

		if (effected.isPlayer() && effected.asPlayer().isFakeDeath() && PlayerConfig.FAKE_DEATH_DAMAGE_STAND)
		{
			effected.stopFakeDeath(true);
		}

		final Player caster = effector.asPlayer();
		final int chargedLightSouls = Math.min(skill.getMaxLightSoulConsumeCount(), caster.getCharges());
		if ((chargedLightSouls > 0) && !caster.decreaseCharges(chargedLightSouls))
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_THE_REQUIREMENTS_ARE_NOT_MET);
			sm.addSkillName(skill);
			effector.sendPacket(sm);
			return;
		}

		final int chargedShadowSouls = Math.min(skill.getMaxShadowSoulConsumeCount(), caster.getCharges());
		if ((chargedShadowSouls > 0) && !caster.decreaseCharges(chargedShadowSouls))
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_THE_REQUIREMENTS_ARE_NOT_MET);
			sm.addSkillName(skill);
			effector.sendPacket(sm);
			return;
		}

		final int chargedSouls = chargedLightSouls + chargedShadowSouls;
		if (chargedSouls < 1)
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_THE_REQUIREMENTS_ARE_NOT_MET);
			sm.addSkillName(skill);
			effector.sendPacket(sm);
			return;
		}

		final boolean sps = skill.useSpiritShot() && effector.isChargedShot(ShotType.SPIRITSHOTS);
		final boolean bss = skill.useSpiritShot() && effector.isChargedShot(ShotType.BLESSED_SPIRITSHOTS);
		final boolean mcrit = Formulas.calcCrit(skill.getMagicCriticalRate(), effector, effected, skill);
		final double mAtk = effector.getMAtk() * (1 + (chargedSouls * 0.022));
		final double damage = Formulas.calcMagicDam(effector, effected, skill, mAtk, _power, effected.getMDef(), sps, bss, mcrit);

		effector.doAttack(damage, effected, skill, false, false, mcrit, false);
	}
}
