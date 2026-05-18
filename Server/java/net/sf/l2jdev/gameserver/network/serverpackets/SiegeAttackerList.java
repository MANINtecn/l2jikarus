package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.model.SiegeClan;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class SiegeAttackerList extends ServerPacket
{
	private final Castle _castle;
	private final List<Clan> _attackers = new ArrayList<>();

	public SiegeAttackerList(Castle castle)
	{
		this._castle = castle;

		for (SiegeClan siegeClan : this._castle.getSiege().getAttackerClans())
		{
			Clan clan = ClanTable.getInstance().getClan(siegeClan.getClanId());
			if (clan != null)
			{
				this._attackers.add(clan);
			}
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.CASTLE_SIEGE_ATTACKER_LIST.writeId(this, buffer);
		buffer.writeInt(this._castle.getResidenceId());
		buffer.writeInt(0);
		buffer.writeInt(1);
		buffer.writeInt(0);
		int size = this._attackers.size();
		if (size > 0)
		{
			buffer.writeInt(size);
			buffer.writeInt(size);

			for (Clan clan : this._attackers)
			{
				buffer.writeInt(clan.getId());
				buffer.writeString(clan.getName());
				buffer.writeString(clan.getLeaderName());
				buffer.writeInt(clan.getCrestId());
				buffer.writeInt(0);
				buffer.writeInt(clan.getAllyId());
				buffer.writeString(clan.getAllyName());
				buffer.writeString("");
				buffer.writeInt(clan.getAllyCrestId());
			}
		}
		else
		{
			buffer.writeInt(0);
			buffer.writeInt(0);
		}
	}
}
