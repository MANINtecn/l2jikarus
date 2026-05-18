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

import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.effects.AbstractEffect;
import net.sf.l2jdev.gameserver.model.effects.EffectFlag;
import net.sf.l2jdev.gameserver.model.effects.EffectType;
import net.sf.l2jdev.gameserver.model.item.enums.ShotType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.stats.Formulas;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

/**
 * Magical damage MP effect.
 * @author Adry_85
 */
public class MagicalDamageMp extends AbstractEffect
{
	private final double _power;
	private final boolean _critical;
	private final double _criticalLimit;

	public MagicalDamageMp(StatSet params)
	{
		_power = params.getDouble("power");
		_critical = params.getBoolean("critical");
		_criticalLimit = params.getDouble("criticalLimit");

		if (params.contains("amount"))
		{
			throw new IllegalArgumentException(getClass().getSimpleName() + " should use power instead of amount.");
		}
	}

	@Override
	public boolean calcSuccess(Creature effector, Creature effected, Skill skill)
	{
		if (effected.isMpBlocked())
		{
			return false;
		}

		if (effector.isPlayer() && effected.isPlayer() && effected.isAffected(EffectFlag.DUELIST_FURY) && !effector.isAffected(EffectFlag.DUELIST_FURY))
		{
			return false;
		}

		if (!Formulas.calcMagicAffected(effector, effected, skill))
		{
			if (effector.isPlayer())
			{
				effector.sendPacket(SystemMessageId.YOUR_ATTACK_HAS_FAILED);
			}

			if (effected.isPlayer())
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.C1_RESISTED_C2_S_DRAIN);
				sm.addString(effected.getName());
				sm.addString(effector.getName());
				effected.sendPacket(sm);
			}

			return false;
		}

		return true;
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

		final boolean sps = skill.useSpiritShot() && effector.isChargedShot(ShotType.SPIRITSHOTS);
		final boolean bss = skill.useSpiritShot() && effector.isChargedShot(ShotType.BLESSED_SPIRITSHOTS);
		final byte shld = Formulas.calcShldUse(effector, effected);
		final boolean mcrit = _critical && Formulas.calcCrit(skill.getMagicCriticalRate(), effector, effected, skill);
		final double damage = Formulas.calcManaDam(effector, effected, skill, _power, shld, sps, bss, mcrit, _criticalLimit);
		final double mp = Math.min(effected.getCurrentMp(), damage);

		if (damage > 0)
		{
			effected.stopEffectsOnDamage();
			effected.setCurrentMp(effected.getCurrentMp() - mp);
		}

		if (effected.isPlayer())
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S2_S_MP_HAS_BEEN_DRAINED_BY_C1);
			sm.addString(effector.getName());
			sm.addInt((int) mp);
			effected.sendPacket(sm);
		}

		if (effector.isPlayer())
		{
			final SystemMessage sm2 = new SystemMessage(SystemMessageId.YOUR_OPPONENT_S_MP_WAS_REDUCED_BY_S1);
			sm2.addInt((int) mp);
			effector.sendPacket(sm2);
		}
	}
}
