package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.groups.PartyDistributionType;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExAskModifyPartyLooting extends ServerPacket
{
	private final String _requestor;
	private final PartyDistributionType _partyDistributionType;

	public ExAskModifyPartyLooting(String name, PartyDistributionType partyDistributionType)
	{
		this._requestor = name;
		this._partyDistributionType = partyDistributionType;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ASK_MODIFY_PARTY_LOOTING.writeId(this, buffer);
		buffer.writeString(this._requestor);
		buffer.writeInt(this._partyDistributionType.getId());
	}
}
