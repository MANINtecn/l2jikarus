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
package handlers.chathandlers;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.handler.IChatHandler;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.itemcontainer.Inventory;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.ChatType;
import net.sf.l2jdev.gameserver.network.serverpackets.CreatureSay;

/**
 * Party chat handler.
 * @author durgus
 */
public class ChatParty implements IChatHandler
{
	private static final ChatType[] CHAT_TYPES =
	{
		ChatType.PARTY,
	};

	@Override
	public void onChat(ChatType type, Player activeChar, String target, String text, boolean shareLocation)
	{
		if (!activeChar.isInParty())
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_IN_A_PARTY_AND_CANNOT_SEND_MESSAGES_TO_THE_PARTY_CHAT);
			return;
		}

		if (activeChar.isChatBanned() && GeneralConfig.BAN_CHAT_CHANNELS.contains(type))
		{
			activeChar.sendPacket(SystemMessageId.IF_YOU_TRY_TO_CHAT_BEFORE_THE_PROHIBITION_IS_REMOVED_THE_PROHIBITION_TIME_WILL_INCREASE_EVEN_FURTHER_S1_SEC_OF_PROHIBITION_IS_LEFT);
			return;
		}

		if (GeneralConfig.JAIL_DISABLE_CHAT && activeChar.isJailed() && !activeChar.isGM())
		{
			activeChar.sendPacket(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED);
			return;
		}

		if (shareLocation)
		{
			if (activeChar.getInventory().getInventoryItemCount(Inventory.LCOIN_ID, -1) < GeneralConfig.SHARING_LOCATION_COST)
			{
				activeChar.sendPacket(SystemMessageId.THERE_ARE_NOT_ENOUGH_L_COINS);
				return;
			}

			if ((activeChar.getMovieHolder() != null) || activeChar.isFishing() || activeChar.isInInstance() || activeChar.isOnEvent() || activeChar.isInOlympiadMode() || activeChar.inObserverMode() || activeChar.isInTraingCamp() || activeChar.isInTimedHuntingZone() || activeChar.isInsideZone(ZoneId.SIEGE))
			{
				activeChar.sendPacket(SystemMessageId.LOCATION_CANNOT_BE_SHARED_SINCE_THE_CONDITIONS_ARE_NOT_MET);
				return;
			}

			activeChar.destroyItemByItemId(ItemProcessType.FEE, Inventory.LCOIN_ID, GeneralConfig.SHARING_LOCATION_COST, activeChar, true);
		}

		activeChar.getParty().broadcastCreatureSay(new CreatureSay(activeChar, type, activeChar.getName(), text, shareLocation), activeChar);
	}

	@Override
	public ChatType[] getChatTypeList()
	{
		return CHAT_TYPES;
	}
}
