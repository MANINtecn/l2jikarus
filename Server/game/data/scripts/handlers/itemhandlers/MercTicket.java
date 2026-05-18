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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2jdev.gameserver.handler.IItemHandler;
import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.managers.SiegeGuardManager;
import net.sf.l2jdev.gameserver.model.actor.Playable;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PlayerAction;
import net.sf.l2jdev.gameserver.model.clan.ClanAccess;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.ListenerRegisterType;
import net.sf.l2jdev.gameserver.model.events.annotations.RegisterEvent;
import net.sf.l2jdev.gameserver.model.events.annotations.RegisterType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerDlgAnswer;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.script.Script;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.model.siege.SiegeGuardHolder;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ConfirmDlg;

/**
 * Mercenary Ticket Item Handler.
 * @author St3eT
 */
public class MercTicket extends Script implements IItemHandler
{
	private final Map<Integer, Item> _items = new ConcurrentHashMap<>();

	@Override
	public boolean onItemUse(Playable playable, Item item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.THIS_ITEM_CANNOT_BE_TRANSFERRED_TO_A_GUARDIAN);
			return false;
		}

		final Player player = playable.asPlayer();
		final Castle castle = CastleManager.getInstance().getCastle(player);
		if ((castle == null) || (player.getClan() == null) || (castle.getOwnerId() != player.getClanId()) || !player.hasAccess(ClanAccess.CASTLE_MERCENARIES))
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_POSITION_MERCENARIES);
			return false;
		}

		final int castleId = castle.getResidenceId();
		final SiegeGuardHolder holder = SiegeGuardManager.getInstance().getSiegeGuardByItem(castleId, item.getId());
		if ((holder == null) || (castleId != holder.getCastleId()))
		{
			player.sendPacket(SystemMessageId.MERCENARIES_CANNOT_BE_POSITIONED_HERE);
			return false;
		}
		else if (castle.getSiege().isInProgress())
		{
			player.sendPacket(SystemMessageId.THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE);
			return false;
		}
		else if (SiegeGuardManager.getInstance().isTooCloseToAnotherTicket(player))
		{
			player.sendPacket(SystemMessageId.POSITIONING_CANNOT_BE_DONE_HERE_BECAUSE_THE_DISTANCE_BETWEEN_MERCENARIES_IS_TOO_SHORT);
			return false;
		}
		else if (SiegeGuardManager.getInstance().isAtNpcLimit(castleId, item.getId()))
		{
			player.sendPacket(SystemMessageId.THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE);
			return false;
		}

		_items.put(player.getObjectId(), item);
		final ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.PLACE_S1_IN_THE_CURRENT_LOCATION_AND_DIRECTION_DO_YOU_WISH_TO_CONTINUE);
		dlg.addTime(15000);
		dlg.getSystemMessage().addNpcName(holder.getNpcId());
		player.sendPacket(dlg);
		player.addAction(PlayerAction.MERCENARY_CONFIRM);
		return true;
	}

	@RegisterEvent(EventType.ON_PLAYER_DLG_ANSWER)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerDlgAnswer(OnPlayerDlgAnswer event)
	{
		final Player player = event.getPlayer();
		if (player.removeAction(PlayerAction.MERCENARY_CONFIRM) && _items.containsKey(player.getObjectId()))
		{
			if (SiegeGuardManager.getInstance().isTooCloseToAnotherTicket(player))
			{
				player.sendPacket(SystemMessageId.POSITIONING_CANNOT_BE_DONE_HERE_BECAUSE_THE_DISTANCE_BETWEEN_MERCENARIES_IS_TOO_SHORT);
				return;
			}

			if (event.getAnswer() == 1)
			{
				final Item item = _items.get(player.getObjectId());
				SiegeGuardManager.getInstance().addTicket(item.getId(), player);
				player.destroyItem(ItemProcessType.NONE, item.getObjectId(), 1, null, false); // Remove item from char's inventory
			}

			_items.remove(player.getObjectId());
		}
	}
}
