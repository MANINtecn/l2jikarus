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
package handlers.usercommandhandlers;

import net.sf.l2jdev.gameserver.handler.IUserCommandHandler;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.groups.CommandChannel;
import net.sf.l2jdev.gameserver.model.groups.Party;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

/**
 * Channel Delete user command.
 * @author Chris
 */
public class ChannelDelete implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		93
	};

	@Override
	public boolean onCommand(int id, Player player)
	{
		if (id != COMMAND_IDS[0])
		{
			return false;
		}

		final Party party = player.getParty();
		if ((party != null) && party.isLeader(player) && party.isInCommandChannel() && party.getCommandChannel().getLeader().equals(player))
		{
			final CommandChannel channel = party.getCommandChannel();
			channel.broadcastPacket(new SystemMessage(SystemMessageId.THE_COMMAND_CHANNEL_IS_DISBANDED));
			channel.disbandChannel();
			return true;
		}

		return false;
	}

	@Override
	public int[] getCommandList()
	{
		return COMMAND_IDS;
	}
}
