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

import java.util.StringTokenizer;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.custom.FactionSystemConfig;
import net.sf.l2jdev.gameserver.handler.IChatHandler;
import net.sf.l2jdev.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2jdev.gameserver.handler.VoicedCommandHandler;
import net.sf.l2jdev.gameserver.model.BlockList;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.itemcontainer.Inventory;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.ChatType;
import net.sf.l2jdev.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

/**
 * General Chat Handler.
 * @author durgus
 */
public class ChatGeneral implements IChatHandler
{
	private static final ChatType[] CHAT_TYPES =
	{
		ChatType.GENERAL,
	};

	@Override
	public void onChat(ChatType type, Player activeChar, String paramsValue, String text, boolean shareLocation)
	{
		boolean vcdUsed = false;
		if (text.startsWith("."))
		{
			final StringTokenizer st = new StringTokenizer(text);
			final IVoicedCommandHandler vch;
			String command = "";
			String params = paramsValue;
			if (st.countTokens() > 1)
			{
				command = st.nextToken().substring(1);
				params = text.substring(command.length() + 2);
			}
			else
			{
				command = text.substring(1);
			}

			vch = VoicedCommandHandler.getInstance().getHandler(command);
			if (vch != null)
			{
				vch.onCommand(command, activeChar, params);
				vcdUsed = true;
			}
			else
			{
				vcdUsed = false;
			}
		}

		if (!vcdUsed)
		{
			if (activeChar.isChatBanned() && GeneralConfig.BAN_CHAT_CHANNELS.contains(type))
			{
				activeChar.sendPacket(SystemMessageId.IF_YOU_TRY_TO_CHAT_BEFORE_THE_PROHIBITION_IS_REMOVED_THE_PROHIBITION_TIME_WILL_INCREASE_EVEN_FURTHER_S1_SEC_OF_PROHIBITION_IS_LEFT);
				return;
			}

			if ((activeChar.getLevel() < GeneralConfig.MINIMUM_CHAT_LEVEL) && !activeChar.isGM())
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.CHARACTERS_OF_LV_S1_OR_LOWER_CANNOT_USE_THE_GENERAL_CHAT).addInt(GeneralConfig.MINIMUM_CHAT_LEVEL));
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

			final CreatureSay cs = new CreatureSay(activeChar, type, activeChar.getAppearance().getVisibleName(), text, shareLocation);
			final CreatureSay csRandom = new CreatureSay(activeChar, type, activeChar.getAppearance().getVisibleName(), ChatRandomizer.randomize(text), shareLocation);

			World.getInstance().forEachVisibleObjectInRange(activeChar, Player.class, 1250, player -> {
				if ((player != null) && !BlockList.isBlocked(player, activeChar))
				{
					if (FactionSystemConfig.FACTION_SYSTEM_ENABLED)
					{
						if (FactionSystemConfig.FACTION_SPECIFIC_CHAT)
						{
							if ((activeChar.isGood() && player.isEvil()) || (activeChar.isEvil() && player.isGood()))
							{
								player.sendPacket(csRandom);
							}
							else
							{
								player.sendPacket(cs);
							}
						}
						else
						{
							player.sendPacket(cs);
						}
					}
					else
					{
						player.sendPacket(cs);
					}
				}
			});

			activeChar.sendPacket(cs);
		}
	}

	@Override
	public ChatType[] getChatTypeList()
	{
		return CHAT_TYPES;
	}
}
