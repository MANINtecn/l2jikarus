package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.groups.Party;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExMPCCShowPartyMemberInfo extends ServerPacket
{
	private final Party _party;

	public ExMPCCShowPartyMemberInfo(Party party)
	{
		this._party = party;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_MPCC_SHOW_PARTY_MEMBERS_INFO.writeId(this, buffer);
		buffer.writeInt(this._party.getMemberCount());

		for (Player pc : this._party.getMembers())
		{
			buffer.writeString(pc.getName());
			buffer.writeInt(pc.getObjectId());
			buffer.writeInt(pc.getPlayerClass().getId());
		}
	}
}
