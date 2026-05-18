package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.groups.Party;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class RequestChangePartyLeader extends ClientPacket
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
		Player player = this.getPlayer();
		if (player != null)
		{
			Party party = player.getParty();
			if (party != null)
			{
				Player newLeader = party.getPlayerByName(this._name);
				if (party.isLeader(player))
				{
					if (newLeader != null && party.getMembers().contains(newLeader))
					{
						party.changePartyLeader(this._name);
					}
					else
					{
						player.sendPacket(SystemMessageId.YOU_MAY_ONLY_TRANSFER_PARTY_LEADERSHIP_TO_ANOTHER_MEMBER_OF_THE_PARTY);
					}
				}
				else if (newLeader != null && party.getMembers().contains(newLeader))
				{
					player.sendPacket(SystemMessageId.ONLY_THE_LEADER_OF_THE_PARTY_CAN_TRANSFER_PARTY_LEADERSHIP_TO_ANOTHER_PLAYER);
				}
			}
		}
	}
}
