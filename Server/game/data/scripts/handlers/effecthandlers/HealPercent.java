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
import net.sf.l2jdev.gameserver.model.effects.EffectType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.CommonSkill;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.stats.Stat;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

/**
 * Heal Percent effect implementation.
 * @author UnAfraid
 */
public class HealPercent extends AbstractEffect
{
	private final double _power;

	public HealPercent(StatSet params)
	{
		_power = params.getDouble("power", 0);

		if (params.contains("amount"))
		{
			throw new IllegalArgumentException(getClass().getSimpleName() + " should use power instead of amount.");
		}
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.HEAL;
	}

	@Override
	public boolean isInstant()
	{
		return true;
	}

	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		if (effected.isDead() || effected.isDoor() || effected.isHpBlocked() || effected.isRaid())
		{
			return;
		}

		double amount = 0;
		final double power = _power;
		final boolean full = (power == 100.0);

		amount = full ? effected.getMaxHp() : (effected.getMaxHp() * power) / 100.0;
		if ((item != null) && (item.isPotion() || item.isElixir()))
		{
			amount += effected.getStat().getValue(Stat.ADDITIONAL_POTION_HP, 0);

			// Classic Potion Mastery
			// TODO: Create an effect if more mastery skills are added.
			amount *= 1 + (effected.getAffectedSkillLevel(CommonSkill.POTION_MASTERY.getId()) / 100);
		}

		if (amount > 0)
		{
			final double healEffectVal = effected.getStat().getValue(Stat.HEAL_EFFECT, 1);
			if (healEffectVal > 1)
			{
				amount *= healEffectVal;
			}

			final double healEffectAddVal = effected.getStat().getValue(Stat.HEAL_EFFECT_ADD, 0);
			if (healEffectAddVal > 0)
			{
				amount += healEffectAddVal;
			}
		}

		// Prevents overheal
		amount = Math.min(amount, Math.max(0, effected.getMaxRecoverableHp() - effected.getCurrentHp()));
		if (amount >= 0)
		{
			if (amount != 0)
			{
				final double newHp = amount + effected.getCurrentHp();
				effected.setCurrentHp(newHp, false);
				effected.broadcastStatusUpdate(effector);
			}

			SystemMessage sm;
			if (effector.getObjectId() != effected.getObjectId())
			{
				sm = new SystemMessage(SystemMessageId.S2_HP_HAS_BEEN_RECOVERED_BY_C1);
				sm.addString(effector.getName());
			}
			else
			{
				sm = new SystemMessage(SystemMessageId.YOU_VE_RECOVERED_S1_HP);
			}

			sm.addInt((int) amount);
			effected.sendPacket(sm);
		}
		else
		{
			final double damage = -amount;
			effected.reduceCurrentHp(damage, effector, skill, false, false, false, false);
			effector.sendDamageMessage(effected, skill, (int) damage, 0, false, false, false);
		}
	}
}
