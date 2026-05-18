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

import java.util.EnumSet;
import java.util.Set;

import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.effects.AbstractEffect;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.AbnormalType;
import net.sf.l2jdev.gameserver.model.skill.Skill;

/**
 * Block Buff Slot effect implementation.
 * @author Zoey76
 */
public class BlockAbnormalSlot extends AbstractEffect
{
	private final Set<AbnormalType> _blockAbnormalSlots = EnumSet.noneOf(AbnormalType.class);

	public BlockAbnormalSlot(StatSet params)
	{
		for (String slot : params.getString("slot").split(";"))
		{
			if (!slot.isEmpty())
			{
				_blockAbnormalSlots.add(Enum.valueOf(AbnormalType.class, slot));
			}
		}
	}

	@Override
	public void onStart(Creature effector, Creature effected, Skill skill, Item item)
	{
		effected.getEffectList().addBlockedAbnormalTypes(_blockAbnormalSlots);
	}

	@Override
	public void onExit(Creature effector, Creature effected, Skill skill)
	{
		effected.getEffectList().removeBlockedAbnormalTypes(_blockAbnormalSlots);
	}
}
