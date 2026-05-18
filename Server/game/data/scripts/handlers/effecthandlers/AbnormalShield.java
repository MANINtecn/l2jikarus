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
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;

/**
 * An effect that blocks a debuff. Acts like DOTA's Linken Sphere.
 * @author Nik
 */
public class AbnormalShield extends AbstractEffect
{
	private final int _times;

	public AbnormalShield(StatSet params)
	{
		_times = params.getInt("times", -1);
	}

	@Override
	public void onStart(Creature effector, Creature effected, Skill skill, Item item)
	{
		effected.setAbnormalShieldBlocks(_times);
	}

	@Override
	public long getEffectFlags()
	{
		return EffectFlag.ABNORMAL_SHIELD.getMask();
	}

	@Override
	public void onExit(Creature effector, Creature effected, Skill skill)
	{
		effected.setAbnormalShieldBlocks(Integer.MIN_VALUE);
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.ABNORMAL_SHIELD;
	}
}
