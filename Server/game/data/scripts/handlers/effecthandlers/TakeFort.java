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

import net.sf.l2jdev.gameserver.managers.FortManager;
import net.sf.l2jdev.gameserver.managers.FortSiegeManager;
import net.sf.l2jdev.gameserver.managers.MailManager;
import net.sf.l2jdev.gameserver.model.Message;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.effects.AbstractEffect;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.Inventory;
import net.sf.l2jdev.gameserver.model.itemcontainer.Mail;
import net.sf.l2jdev.gameserver.model.siege.Fort;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.enums.MailType;

/**
 * Take Fort effect implementation.
 * @author Adry_85
 */
public class TakeFort extends AbstractEffect
{
	public TakeFort(StatSet params)
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
		if (!effector.isPlayer())
		{
			return;
		}

		final Fort fort = FortManager.getInstance().getFort(effector);
		if ((fort != null) && (fort.getResidenceId() == FortManager.ORC_FORTRESS))
		{
			if (fort.getSiege().isInProgress())
			{
				fort.endOfSiege(effector.getClan());
				if (effector.isPlayer())
				{
					final Player player = effector.asPlayer();
					FortSiegeManager.getInstance().dropCombatFlag(player, FortManager.ORC_FORTRESS);

					final Message mail = new Message(player.getObjectId(), "Orc Fortress", "", MailType.NPC);
					final Mail attachment = mail.createAttachments();
					attachment.addItem(ItemProcessType.REWARD, Inventory.ADENA_ID, 30_000_000, player, player);
					MailManager.getInstance().sendMessage(mail);
				}
			}
		}
	}
}
