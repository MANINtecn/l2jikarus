package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.model.SiegeClan;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.model.siege.SiegeClanType;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class SiegeDefenderList extends ServerPacket
{
	private final Castle _castle;
	private final Clan _owner;
	final List<Clan> _defenders = new ArrayList<>();

	public SiegeDefenderList(Castle castle)
	{
		this._castle = castle;
		this._owner = castle.getOwner();
		if (this._owner != null)
		{
			this._defenders.add(this._owner);
		}

		for (SiegeClan siegeClan : this._castle.getSiege().getDefenderClans())
		{
			Clan clan = ClanTable.getInstance().getClan(siegeClan.getClanId());
			if (clan != null && clan != this._owner)
			{
				this._defenders.add(clan);
			}
		}

		for (SiegeClan siegeClanx : this._castle.getSiege().getDefenderWaitingClans())
		{
			Clan clan = ClanTable.getInstance().getClan(siegeClanx.getClanId());
			if (clan != null)
			{
				this._defenders.add(clan);
			}
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.CASTLE_SIEGE_DEFENDER_LIST.writeId(this, buffer);
		buffer.writeInt(this._castle.getResidenceId());
		buffer.writeInt(0);
		buffer.writeInt(this._owner != null && this._castle.isTimeRegistrationOver());
		buffer.writeInt(0);
		int size = this._defenders.size();
		buffer.writeInt(size);
		buffer.writeInt(size);

		for (Clan clan : this._defenders)
		{
			buffer.writeInt(clan.getId());
			buffer.writeString(clan.getName());
			buffer.writeString(clan.getLeaderName());
			buffer.writeInt(clan.getCrestId());
			buffer.writeInt(0);
			if (clan == this._owner)
			{
				buffer.writeInt(SiegeClanType.OWNER.ordinal() + 1);
			}
			else if (this._castle.getSiege().getDefenderClans().stream().anyMatch(defender -> defender.getClanId() == clan.getId()))
			{
				buffer.writeInt(SiegeClanType.DEFENDER.ordinal() + 1);
			}
			else
			{
				buffer.writeInt(SiegeClanType.DEFENDER_PENDING.ordinal() + 1);
			}

			buffer.writeInt(clan.getAllyId());
			if (clan.getAllyId() != 0)
			{
				AllianceInfo info = new AllianceInfo(clan.getAllyId());
				buffer.writeString(info.getName());
				buffer.writeString(info.getLeaderP());
				buffer.writeInt(clan.getAllyCrestId());
			}
			else
			{
				buffer.writeString("");
				buffer.writeString("");
				buffer.writeInt(0);
			}
		}
	}
}
