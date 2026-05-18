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
package handlers.voicedcommandhandlers;

import net.sf.l2jdev.gameserver.config.custom.OfflineTradeConfig;
import net.sf.l2jdev.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.ConfirmDlg;

/**
 * @author BAN-JDEV
 */
public class Offline implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"offline"
	};

	@Override
	public boolean onCommand(String command, Player player, String target)
	{
		if (command.equals("offline") && OfflineTradeConfig.ENABLE_OFFLINE_COMMAND && (OfflineTradeConfig.OFFLINE_TRADE_ENABLE || OfflineTradeConfig.OFFLINE_CRAFT_ENABLE))
		{
			if (player.isPrisoner())
			{
				player.sendMessage("You can't use .offline as a prisoner.");
				return false;
			}

			if (!player.isInStoreMode())
			{
				player.sendPacket(SystemMessageId.PRIVATE_STORE_ALREADY_CLOSED);
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}

			if (player.isInInstance() || player.isInVehicle() || !player.canLogout())
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}

			player.sendPacket(new ConfirmDlg(SystemMessageId.THE_GAME_WILL_BE_CLOSED_CONTINUE));
		}

		return true;
	}

	@Override
	public String[] getCommandList()
	{
		return VOICED_COMMANDS;
	}
}
