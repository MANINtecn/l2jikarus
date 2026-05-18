package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.ClanMember;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class GMViewPledgeInfo extends ServerPacket
{
	private final Clan _clan;
	private final Player _player;

	public GMViewPledgeInfo(Clan clan, Player player)
	{
		this._clan = clan;
		this._player = player;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.GM_VIEW_PLEDGE_INFO.writeId(this, buffer);
		buffer.writeInt(0);
		buffer.writeString(this._player.getName());
		buffer.writeInt(this._clan.getId());
		buffer.writeInt(0);
		buffer.writeString(this._clan.getName());
		buffer.writeString(this._clan.getLeaderName());
		buffer.writeInt(this._clan.getCrestId());
		buffer.writeInt(this._clan.getLevel());
		buffer.writeInt(this._clan.getCastleId());
		buffer.writeInt(this._clan.getHideoutId());
		buffer.writeInt(this._clan.getFortId());
		buffer.writeInt(this._clan.getRank());
		buffer.writeInt(this._clan.getReputationScore());
		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeInt(this._clan.getAllyId());
		buffer.writeString(this._clan.getAllyName());
		buffer.writeInt(this._clan.getAllyCrestId());
		buffer.writeInt(this._clan.isAtWar());
		buffer.writeInt(0);
		buffer.writeInt(this._clan.getMembers().size());

		for (ClanMember member : this._clan.getMembers())
		{
			if (member != null)
			{
				buffer.writeString(member.getName());
				buffer.writeInt(member.getLevel());
				buffer.writeInt(member.getClassId());
				buffer.writeInt(member.getSex());
				buffer.writeInt(member.getRaceOrdinal());
				buffer.writeByte(member.isOnline() ? member.getObjectId() : 0);
				buffer.writeInt(member.getSponsor() != 0);
				buffer.writeInt(0);
			}
		}
	}
}
