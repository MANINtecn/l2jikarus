package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.groups.Party;
import net.sf.l2jdev.gameserver.model.groups.PartyDistributionType;

public class RequestPartyLootModification extends ClientPacket
{
	private int _partyDistributionTypeId;

	@Override
	protected void readImpl()
	{
		this._partyDistributionTypeId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			PartyDistributionType partyDistributionType = PartyDistributionType.findById(this._partyDistributionTypeId);
			if (partyDistributionType != null)
			{
				Party party = player.getParty();
				if (party != null && party.isLeader(player) && partyDistributionType != party.getDistributionType())
				{
					party.requestLootChange(partyDistributionType);
				}
			}
		}
	}
}
