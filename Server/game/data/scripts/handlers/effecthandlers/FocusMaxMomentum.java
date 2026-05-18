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
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.effects.AbstractEffect;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.stats.Stat;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.EtcStatusUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

/**
 * Focus Max Energy effect implementation.
 * @author Adry_85
 */
public class FocusMaxMomentum extends AbstractEffect
{
	public FocusMaxMomentum(StatSet params)
	{
	}

	@Override
	public boolean isInstant()
	{
		return true;
	}

	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		if (effected.isPlayer())
		{
			final Player player = effected.asPlayer();

			final int count = (int) effected.getStat().getValue(Stat.MAX_MOMENTUM, 1);

			player.setCharges(count);

			final SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_FORCE_HAS_INCREASED_TO_LEVEL_S1);
			sm.addInt(count);
			player.sendPacket(sm);

			player.sendPacket(new EtcStatusUpdate(player));
		}
	}
}
