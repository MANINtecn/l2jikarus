package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class PledgeReceiveSubPledgeCreated extends ServerPacket
{
	private final Clan.SubPledge _subPledge;
	private final Clan _clan;

	public PledgeReceiveSubPledgeCreated(Clan.SubPledge subPledge, Clan clan)
	{
		this._subPledge = subPledge;
		this._clan = clan;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SUBPLEDGE_UPDATED.writeId(this, buffer);
		buffer.writeInt(1);
		buffer.writeInt(this._subPledge.getId());
		buffer.writeString(this._subPledge.getName());
		buffer.writeString(this.getLeaderName());
	}

	private String getLeaderName()
	{
		int LeaderId = this._subPledge.getLeaderId();
		if (this._subPledge.getId() == -1 || LeaderId == 0)
		{
			return "";
		}
		else if (this._clan.getClanMember(LeaderId) == null)
		{
			PacketLogger.warning("SubPledgeLeader: " + LeaderId + " is missing from clan: " + this._clan.getName() + "[" + this._clan.getId() + "]");
			return "";
		}
		else
		{
			return this._clan.getClanMember(LeaderId).getName();
		}
	}
}
