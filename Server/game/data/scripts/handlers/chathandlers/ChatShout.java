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
import net.sf.l2jdev.gameserver.config.custom.FactionSystemConfig;
import net.sf.l2jdev.gameserver.handler.IChatHandler;
import net.sf.l2jdev.gameserver.managers.MapRegionManager;
import net.sf.l2jdev.gameserver.model.BlockList;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.ChatBroadcastType;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.itemcontainer.Inventory;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.ChatType;
import net.sf.l2jdev.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

/**
 * Shout chat handler.
 * @author durgus
 */
public class ChatShout implements IChatHandler
{
	private static final ChatType[] CHAT_TYPES =
	{
		ChatType.SHOUT,
	};

	@Override
	public void onChat(ChatType type, Player activeChar, String target, String text, boolean shareLocation)
	{
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

		if ((activeChar.getLevel() < GeneralConfig.MINIMUM_CHAT_LEVEL) && !activeChar.isGM())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CHARACTERS_OF_LV_S1_OR_LOWER_CANNOT_USE_THE_SHOUT_CHAT).addInt(GeneralConfig.MINIMUM_CHAT_LEVEL));
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

		final CreatureSay cs = new CreatureSay(activeChar, type, activeChar.getName(), text, shareLocation);
		if ((GeneralConfig.DEFAULT_GLOBAL_CHAT == ChatBroadcastType.ON) || ((GeneralConfig.DEFAULT_GLOBAL_CHAT == ChatBroadcastType.GM) && activeChar.isGM()))
		{
			final int region = MapRegionManager.getInstance().getMapRegionLocId(activeChar);
			for (Player player : World.getInstance().getPlayers())
			{
				if ((region == MapRegionManager.getInstance().getMapRegionLocId(player)) && !BlockList.isBlocked(player, activeChar) && (player.getInstanceId() == activeChar.getInstanceId()) && !BlockList.isBlocked(activeChar, player))
				{
					if (FactionSystemConfig.FACTION_SYSTEM_ENABLED)
					{
						if (FactionSystemConfig.FACTION_SPECIFIC_CHAT)
						{
							if ((activeChar.isGood() && player.isGood()) || (activeChar.isEvil() && player.isEvil()))
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
			}
		}
		else if (GeneralConfig.DEFAULT_GLOBAL_CHAT == ChatBroadcastType.GLOBAL)
		{
			if (!activeChar.isGM() && !activeChar.getClient().getFloodProtectors().canUseGlobalChat())
			{
				activeChar.sendMessage("Do not spam shout channel.");
				return;
			}

			for (Player player : World.getInstance().getPlayers())
			{
				if (!BlockList.isBlocked(player, activeChar))
				{
					if (FactionSystemConfig.FACTION_SYSTEM_ENABLED)
					{
						if (FactionSystemConfig.FACTION_SPECIFIC_CHAT)
						{
							if ((activeChar.isGood() && player.isGood()) || (activeChar.isEvil() && player.isEvil()))
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
			}
		}
	}

	@Override
	public ChatType[] getChatTypeList()
	{
		return CHAT_TYPES;
	}
}
