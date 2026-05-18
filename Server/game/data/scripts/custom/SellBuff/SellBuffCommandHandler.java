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
package custom.SellBuff;

import net.sf.l2jdev.gameserver.config.custom.SellBuffsConfig;
import net.sf.l2jdev.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2jdev.gameserver.handler.VoicedCommandHandler;
import net.sf.l2jdev.gameserver.managers.SellBuffsManager;
import net.sf.l2jdev.gameserver.model.actor.Player;

/**
 * Sell Buffs voiced commands.
 * @author St3eT
 */
public class SellBuffCommandHandler implements IVoicedCommandHandler
{
	private static final String[] COMMANDS =
	{
		"sellbuff",
		"sellbuffs",
	};

	private SellBuffCommandHandler()
	{
		if (SellBuffsConfig.SELLBUFF_ENABLED)
		{
			VoicedCommandHandler.getInstance().registerHandler(this);
		}
	}

	@Override
	public boolean onCommand(String command, Player player, String params)
	{
		switch (command)
		{
			case "sellbuff":
			case "sellbuffs":
			{
				SellBuffsManager.getInstance().sendSellMenu(player);
				break;
			}
		}

		return true;
	}

	@Override
	public String[] getCommandList()
	{
		return COMMANDS;
	}

	public static void main(String[] args)
	{
		new SellBuffCommandHandler();
	}
}
