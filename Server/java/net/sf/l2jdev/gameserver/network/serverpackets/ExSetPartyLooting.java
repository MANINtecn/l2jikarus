package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.groups.PartyDistributionType;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExSetPartyLooting extends ServerPacket
{
	private final int _result;
	private final PartyDistributionType _partyDistributionType;

	public ExSetPartyLooting(int result, PartyDistributionType partyDistributionType)
	{
		this._result = result;
		this._partyDistributionType = partyDistributionType;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SET_PARTY_LOOTING.writeId(this, buffer);
		buffer.writeInt(this._result);
		buffer.writeInt(this._partyDistributionType.getId());
	}
}
