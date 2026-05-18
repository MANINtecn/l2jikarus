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
package handlers.itemhandlers;

import net.sf.l2jdev.gameserver.cache.HtmCache;
import net.sf.l2jdev.gameserver.handler.IItemHandler;
import net.sf.l2jdev.gameserver.model.actor.Playable;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.item.OnItemTalk;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author JIV
 */
public class Bypass implements IItemHandler
{
	@Override
	public boolean onItemUse(Playable playable, Item item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			return false;
		}

		final Player player = playable.asPlayer();
		final int itemId = item.getId();
		final String filename = "data/html/item/" + itemId + ".htm";
		final String content = HtmCache.getInstance().getHtm(player, filename);
		final NpcHtmlMessage html = new NpcHtmlMessage(item.getId());
		if (content == null)
		{
			html.setHtml("<html><body>My Text is missing:<br>" + filename + "</body></html>");
			player.sendPacket(html);
		}
		else
		{
			html.setHtml(content);
			html.replace("%itemId%", String.valueOf(item.getObjectId()));
			player.sendPacket(html);
		}

		// Notify events.
		if (EventDispatcher.getInstance().hasListener(EventType.ON_ITEM_TALK, item.getTemplate()))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnItemTalk(item, player), item.getTemplate());
		}

		return true;
	}
}
