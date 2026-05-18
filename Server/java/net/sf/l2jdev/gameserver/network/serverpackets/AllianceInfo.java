package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Collection;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.ClanInfo;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class AllianceInfo extends ServerPacket
{
	private final String _name;
	private final int _total;
	private final int _online;
	private final String _leaderC;
	private final String _leaderP;
	private final ClanInfo[] _allies;

	public AllianceInfo(int allianceId)
	{
		Clan leader = ClanTable.getInstance().getClan(allianceId);
		this._name = leader.getAllyName();
		this._leaderC = leader.getName();
		this._leaderP = leader.getLeaderName();
		Collection<Clan> allies = ClanTable.getInstance().getClanAllies(allianceId);
		this._allies = new ClanInfo[allies.size()];
		int idx = 0;
		int total = 0;
		int online = 0;

		for (Clan clan : allies)
		{
			ClanInfo ci = new ClanInfo(clan);
			this._allies[idx++] = ci;
			total += ci.getTotal();
			online += ci.getOnline();
		}

		this._total = total;
		this._online = online;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.ALLIANCE_INFO.writeId(this, buffer);
		buffer.writeString(this._name);
		buffer.writeInt(this._total);
		buffer.writeInt(this._online);
		buffer.writeString(this._leaderC);
		buffer.writeString(this._leaderP);
		buffer.writeInt(this._allies.length);

		for (ClanInfo aci : this._allies)
		{
			buffer.writeString(aci.getClan().getName());
			buffer.writeInt(0);
			buffer.writeInt(aci.getClan().getLevel());
			buffer.writeString(aci.getClan().getLeaderName());
			buffer.writeInt(aci.getTotal());
			buffer.writeInt(aci.getOnline());
		}
	}

	public String getName()
	{
		return this._name;
	}

	public int getTotal()
	{
		return this._total;
	}

	public int getOnline()
	{
		return this._online;
	}

	public String getLeaderC()
	{
		return this._leaderC;
	}

	public String getLeaderP()
	{
		return this._leaderP;
	}

	public ClanInfo[] getAllies()
	{
		return this._allies;
	}
}
