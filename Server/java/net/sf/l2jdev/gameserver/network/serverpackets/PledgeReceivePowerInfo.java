package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.clan.ClanMember;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class PledgeReceivePowerInfo extends ServerPacket
{
	private final ClanMember _member;

	public PledgeReceivePowerInfo(ClanMember member)
	{
		this._member = member;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_VIEW_PLEDGE_POWER.writeId(this, buffer);
		buffer.writeInt(this._member.getPowerGrade());
		buffer.writeString(this._member.getName());
		buffer.writeInt(this._member.getClan().getRankPrivs(this._member.getPowerGrade()).getMask());
	}
}
