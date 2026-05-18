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
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ExVoteSystemInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

/**
 * Give Recommendation effect implementation.
 * @author NosBit
 */
public class GiveRecommendation extends AbstractEffect
{
	private final int _amount;

	public GiveRecommendation(StatSet params)
	{
		_amount = params.getInt("amount", 0);
		if (_amount == 0)
		{
			throw new IllegalArgumentException("amount parameter is missing or set to 0.");
		}
	}

	@Override
	public boolean isInstant()
	{
		return true;
	}

	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		final Player target = (effected != null) && effected.isPlayer() ? effected.asPlayer() : null;
		if (target != null)
		{
			int recommendationsGiven = _amount;
			if ((target.getRecomHave() + _amount) >= 255)
			{
				recommendationsGiven = 255 - target.getRecomHave();
			}

			if (recommendationsGiven > 0)
			{
				target.setRecomHave(target.getRecomHave() + recommendationsGiven);

				final SystemMessage sm = new SystemMessage(SystemMessageId.RECOMMENDATIONS_OBTAINED_S1);
				sm.addInt(recommendationsGiven);
				target.sendPacket(sm);
				target.updateUserInfo();
				target.sendPacket(new ExVoteSystemInfo(target));
			}
			else
			{
				final Player player = (effector != null) && effector.isPlayer() ? effector.asPlayer() : null;
				if (player != null)
				{
					player.sendPacket(SystemMessageId.NOTHING_HAPPENED);
				}
			}
		}
	}
}
