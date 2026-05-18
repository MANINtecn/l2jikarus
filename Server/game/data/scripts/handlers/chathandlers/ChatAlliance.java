/*
 * Copyright (c) 2013 L2jBAN-JDEV
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
 * Alliance Chat Handler.
 */
public class ChatAlliance implements IChatHandler
{
	private static final ChatType[] CHAT_TYPES =
	{
		ChatType.ALLIANCE,
	};

	@Override
	public void onChat(ChatType type, Player activeChar, String target, String text, boolean shareLocation)
	{
		if ((activeChar.getClan() == null) || ((activeChar.getClan() != null) && (activeChar.getClan().getAllyId() == 0)))
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_IN_AN_ALLIANCE);
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

		activeChar.getClan().broadcastToOnlineAllyMembers(new CreatureSay(activeChar, type, activeChar.getName(), text, shareLocation));
	}

	@Override
	public ChatType[] getChatTypeList()
	{
		return CHAT_TYPES;
	}
}
