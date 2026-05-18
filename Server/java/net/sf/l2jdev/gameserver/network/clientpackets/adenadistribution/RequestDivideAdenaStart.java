package net.sf.l2jdev.gameserver.network.clientpackets.adenadistribution;

import java.util.List;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.AdenaDistributionRequest;
import net.sf.l2jdev.gameserver.model.groups.CommandChannel;
import net.sf.l2jdev.gameserver.model.groups.Party;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.adenadistribution.ExDivideAdenaStart;

public class RequestDivideAdenaStart extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Party party = player.getParty();
			if (party == null)
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_DISTRIBUTE_ADENA_IF_YOU_ARE_NOT_A_MEMBER_OF_AN_ALLIANCE_OR_A_COMMAND_CHANNEL);
			}
			else
			{
				CommandChannel commandChannel = party.getCommandChannel();
				if (commandChannel != null && !commandChannel.isLeader(player))
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_PROCEED_AS_YOU_ARE_NOT_AN_ALLIANCE_LEADER_OR_PARTY_LEADER);
				}
				else if (!party.isLeader(player))
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_PROCEED_AS_YOU_ARE_NOT_A_PARTY_LEADER);
				}
				else
				{
					List<Player> targets = commandChannel != null ? commandChannel.getMembers() : party.getMembers();
					if (player.getAdena() < targets.size())
					{
						player.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA_2);
					}
					else if (!targets.stream().anyMatch(t -> t.hasRequest(AdenaDistributionRequest.class)))
					{
						int adenaObjectId = player.getInventory().getAdenaInstance().getObjectId();
						targets.forEach(t -> {
							t.sendPacket(SystemMessageId.ADENA_DISTRIBUTION_HAS_STARTED);
							t.addRequest(new AdenaDistributionRequest(t, player, targets, adenaObjectId, player.getAdena()));
						});
						player.sendPacket(ExDivideAdenaStart.STATIC_PACKET);
					}
				}
			}
		}
	}
}
