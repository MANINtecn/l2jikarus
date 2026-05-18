package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.data.xml.ClanHallData;
import net.sf.l2jdev.gameserver.model.residences.ClanHall;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExShowAgitInfo extends ServerPacket
{
	public static final ExShowAgitInfo STATIC_PACKET = new ExShowAgitInfo();
	private final List<ExShowAgitInfo.ClanHallHolder> _clanHalls;

	private ExShowAgitInfo()
	{
		Collection<ClanHall> clanHalls = ClanHallData.getInstance().getClanHalls();
		this._clanHalls = new ArrayList<>(clanHalls.size());

		for (ClanHall clanHall : clanHalls)
		{
			this._clanHalls.add(new ExShowAgitInfo.ClanHallHolder(clanHall.getResidenceId(), clanHall.getOwnerId() <= 0 ? "" : ClanTable.getInstance().getClan(clanHall.getOwnerId()).getName(), clanHall.getOwnerId() <= 0 ? "" : ClanTable.getInstance().getClan(clanHall.getOwnerId()).getLeaderName(), clanHall.getType().getClientVal()));
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHOW_AGIT_INFO.writeId(this, buffer);
		buffer.writeInt(this._clanHalls.size());

		for (ExShowAgitInfo.ClanHallHolder holder : this._clanHalls)
		{
			buffer.writeInt(holder.getResidenceId());
			buffer.writeString(holder.getOwnerClanName());
			buffer.writeString(holder.getLeaderName());
			buffer.writeInt(holder.getClanHallType());
		}
	}

	public class ClanHallHolder
	{
		private final int _residenceId;
		private final String _ownerClanName;
		private final String _leaderName;
		private final int _clanHallType;

		public ClanHallHolder(int residenceId, String ownerClanName, String leaderName, int clanHallType)
		{
			Objects.requireNonNull(ExShowAgitInfo.this);
			super();
			this._residenceId = residenceId;
			this._ownerClanName = ownerClanName;
			this._leaderName = leaderName;
			this._clanHallType = clanHallType;
		}

		public int getResidenceId()
		{
			return this._residenceId;
		}

		public String getOwnerClanName()
		{
			return this._ownerClanName;
		}

		public String getLeaderName()
		{
			return this._leaderName;
		}

		public int getClanHallType()
		{
			return this._clanHallType;
		}
	}
}
