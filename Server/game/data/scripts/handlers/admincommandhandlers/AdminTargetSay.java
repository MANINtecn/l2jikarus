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
package handlers.admincommandhandlers;

import net.sf.l2jdev.gameserver.handler.IAdminCommandHandler;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.instance.StaticObject;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.ChatType;
import net.sf.l2jdev.gameserver.network.serverpackets.CreatureSay;

/**
 * This class handles following admin commands: - targetsay <message> = makes talk a Creature
 * @author nonom
 */
public class AdminTargetSay implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_targetsay"
	};

	@Override
	public boolean onCommand(String command, Player activeChar)
	{
		if (command.startsWith("admin_targetsay"))
		{
			try
			{
				final WorldObject obj = activeChar.getTarget();
				if ((obj instanceof StaticObject) || !obj.isCreature())
				{
					activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
					return false;
				}

				final String message = command.substring(16);
				final Creature target = obj.asCreature();
				target.broadcastPacket(new CreatureSay(target, target.isPlayer() ? ChatType.GENERAL : ChatType.NPC_GENERAL, target.getName(), message));
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendSysMessage("Usage: //targetsay <text>");
				return false;
			}
		}

		return true;
	}

	@Override
	public String[] getCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
