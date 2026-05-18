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
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.stats.Stat;

/**
 * Transfer Damage effect implementation.
 * @author UnAfraid
 */
public class TransferDamageToPlayer extends AbstractStatAddEffect
{
	public TransferDamageToPlayer(StatSet params)
	{
		super(params, Stat.TRANSFER_DAMAGE_TO_PLAYER);
	}

	@Override
	public void onExit(Creature effector, Creature effected, Skill skill)
	{
		if (effected.isPlayable() && effector.isPlayer())
		{
			effected.asPlayable().setTransferDamageTo(null);
		}
	}

	@Override
	public void onStart(Creature effector, Creature effected, Skill skill, Item item)
	{
		if (effected.isPlayable() && effector.isPlayer())
		{
			effected.asPlayable().setTransferDamageTo(effector.asPlayer());
		}
	}
}
