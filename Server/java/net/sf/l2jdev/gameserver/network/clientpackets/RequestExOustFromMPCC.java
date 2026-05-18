package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.groups.Party;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class RequestExOustFromMPCC extends ClientPacket
{
	private String _name;

	@Override
	protected void readImpl()
	{
		this._name = this.readString();
	}

	@Override
	protected void runImpl()
	{
		Player target = World.getInstance().getPlayer(this._name);
		Player player = this.getPlayer();
		Party party = player.getParty();
		if (target != null && target.isInParty() && party != null && party.isInCommandChannel() && target.getParty().isInCommandChannel() && party.getCommandChannel().getLeader().equals(player) && party.getCommandChannel().equals(target.getParty().getCommandChannel()))
		{
			if (player.equals(target))
			{
				return;
			}

			target.getParty().getCommandChannel().removeParty(target.getParty());
			SystemMessage sm = new SystemMessage(SystemMessageId.YOU_ARE_DISMISSED_FROM_THE_COMMAND_CHANNEL);
			target.getParty().broadcastPacket(sm);
			if (party.isInCommandChannel())
			{
				sm = new SystemMessage(SystemMessageId.C1_S_PARTY_IS_DISMISSED_FROM_THE_COMMAND_CHANNEL);
				sm.addString(target.getParty().getLeader().getName());
				party.getCommandChannel().broadcastPacket(sm);
			}
		}
		else
		{
			player.sendPacket(SystemMessageId.YOUR_TARGET_CANNOT_BE_FOUND);
		}
	}
}
