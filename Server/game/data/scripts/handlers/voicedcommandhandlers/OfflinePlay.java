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

import java.util.function.Consumer;

import net.sf.l2jdev.gameserver.config.custom.OfflinePlayConfig;
import net.sf.l2jdev.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PlayerAction;
import net.sf.l2jdev.gameserver.model.events.Containers;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerLogin;
import net.sf.l2jdev.gameserver.model.events.listeners.ConsumerEventListener;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.ChatType;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.ConfirmDlg;
import net.sf.l2jdev.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2jdev.gameserver.network.serverpackets.ExShowScreenMessage;

/**
 * @author BAN-JDEV
 */
public class OfflinePlay implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"offlineplay"
	};

	private static final Consumer<OnPlayerLogin> ON_PLAYER_LOGIN = event -> {
		if (OfflinePlayConfig.ENABLE_OFFLINE_PLAY_COMMAND && !OfflinePlayConfig.OFFLINE_PLAY_LOGIN_MESSAGE.isEmpty())
		{
			event.getPlayer().sendPacket(new CreatureSay(null, ChatType.ANNOUNCEMENT, "OfflinePlay", OfflinePlayConfig.OFFLINE_PLAY_LOGIN_MESSAGE));
		}
	};

	public OfflinePlay()
	{
		Containers.Players().addListener(new ConsumerEventListener(Containers.Players(), EventType.ON_PLAYER_LOGIN, ON_PLAYER_LOGIN, this));
	}

	@Override
	public boolean onCommand(String command, Player player, String target)
	{
		if (command.equals("offlineplay") && OfflinePlayConfig.ENABLE_OFFLINE_PLAY_COMMAND)
		{
			if (OfflinePlayConfig.OFFLINE_PLAY_PREMIUM && !player.hasPremiumStatus())
			{
				player.sendPacket(new ExShowScreenMessage("This command is only available to premium players.", 5000));
				player.sendMessage("This command is only available to premium players.");
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}

			if (!player.isAutoPlaying())
			{
				player.sendPacket(new ExShowScreenMessage("You need to enable auto play before exiting.", 5000));
				player.sendMessage("You need to enable auto play before exiting.");
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}

			if (player.isInVehicle() || player.isInsideZone(ZoneId.PEACE))
			{
				player.sendPacket(new ExShowScreenMessage("You may not log out from this location.", 5000));
				player.sendPacket(SystemMessageId.YOU_MAY_NOT_LOG_OUT_FROM_THIS_LOCATION);
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}

			if (player.isRegisteredOnEvent())
			{
				player.sendPacket(new ExShowScreenMessage("Cannot use this command while registered on an event.", 5000));
				player.sendMessage("Cannot use this command while registered on an event.");
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}

			if (player.isPrisoner())
			{
				player.sendPacket(new ExShowScreenMessage("Cannot use this command while you are prisoner.", 5000));
				player.sendMessage("Cannot use this command while you are prisoner.");
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}

			player.addAction(PlayerAction.OFFLINE_PLAY);
			player.sendPacket(new ConfirmDlg("Do you wish to exit and continue auto play?"));
		}

		return true;
	}

	@Override
	public String[] getCommandList()
	{
		return VOICED_COMMANDS;
	}
}
