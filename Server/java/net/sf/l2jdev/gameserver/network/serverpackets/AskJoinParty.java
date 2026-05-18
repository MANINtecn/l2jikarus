package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.groups.PartyDistributionType;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class AskJoinParty extends ServerPacket
{
	private final String _requestorName;
	private final PartyDistributionType _partyDistributionType;

	public AskJoinParty(String requestorName, PartyDistributionType partyDistributionType)
	{
		this._requestorName = requestorName;
		this._partyDistributionType = partyDistributionType;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.ASK_JOIN_PARTY.writeId(this, buffer);
		buffer.writeString(this._requestorName);
		buffer.writeInt(this._partyDistributionType.getId());
	}
}
