package net.sf.l2jdev.gameserver.network.serverpackets.castlewar;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.model.SiegeClan;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.model.siege.SiegeClanType;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class MercenaryCastleWarCastleSiegeDefenderList extends ServerPacket
{
	private final int _castleId;
	private final Castle _castle;
	private final Clan _owner;
	private final List<Clan> _defenders = new ArrayList<>();
	private final List<Clan> _defendersWaiting = new ArrayList<>();

	public MercenaryCastleWarCastleSiegeDefenderList(int castleId)
	{
		this._castleId = castleId;
		this._castle = CastleManager.getInstance().getCastleById(castleId);
		this._owner = this._castle.getOwner();

		for (SiegeClan siegeClan : this._castle.getSiege().getDefenderClans())
		{
			Clan clan = ClanTable.getInstance().getClan(siegeClan.getClanId());
			if (clan != null && clan != this._castle.getOwner())
			{
				this._defenders.add(clan);
			}
		}

		for (SiegeClan siegeClanx : this._castle.getSiege().getDefenderWaitingClans())
		{
			Clan clan = ClanTable.getInstance().getClan(siegeClanx.getClanId());
			if (clan != null)
			{
				this._defendersWaiting.add(clan);
			}
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_MERCENARY_CASTLEWAR_CASTLE_SIEGE_DEFENDER_LIST.writeId(this, buffer);
		buffer.writeInt(this._castleId);
		buffer.writeInt(0);
		buffer.writeInt(1);
		buffer.writeInt(0);
		if (this._castle == null)
		{
			buffer.writeInt(0);
			buffer.writeInt(0);
		}
		else
		{
			int size = (this._owner != null ? 1 : 0) + this._defenders.size() + this._defendersWaiting.size();
			buffer.writeInt(size);
			buffer.writeInt(size);
			if (this._owner != null)
			{
				buffer.writeInt(this._owner.getId());
				buffer.writeString(this._owner.getName());
				buffer.writeString(this._owner.getLeaderName());
				buffer.writeInt(this._owner.getCrestId());
				buffer.writeInt(0);
				buffer.writeInt(SiegeClanType.OWNER.ordinal());
				buffer.writeInt(this._owner.isRecruitMercenary());
				buffer.writeLong(this._owner.getRewardMercenary());
				buffer.writeInt(this._owner.getMapMercenary().size());
				buffer.writeLong(0L);
				buffer.writeLong(0L);
				if (this._owner.getAllyId() != 0)
				{
					buffer.writeInt(this._owner.getAllyId());
					buffer.writeString(this._owner.getAllyName());
					buffer.writeString("");
					buffer.writeInt(this._owner.getAllyCrestId());
				}
				else
				{
					buffer.writeInt(0);
					buffer.writeString("");
					buffer.writeString("");
					buffer.writeInt(0);
				}
			}

			for (Clan defender : this._defenders)
			{
				buffer.writeInt(defender.getId());
				buffer.writeString(defender.getName());
				buffer.writeString(defender.getLeaderName());
				buffer.writeInt(defender.getCrestId());
				buffer.writeInt(0);
				buffer.writeInt(SiegeClanType.DEFENDER.ordinal());
				buffer.writeInt(defender.isRecruitMercenary());
				buffer.writeLong(defender.getRewardMercenary());
				buffer.writeInt(defender.getMapMercenary().size());
				buffer.writeLong(0L);
				buffer.writeLong(0L);
				if (defender.getAllyId() != 0)
				{
					buffer.writeInt(defender.getAllyId());
					buffer.writeString(defender.getAllyName());
					buffer.writeString("");
					buffer.writeInt(defender.getAllyCrestId());
				}
				else
				{
					buffer.writeInt(0);
					buffer.writeString("");
					buffer.writeString("");
					buffer.writeInt(0);
				}
			}

			for (Clan defenderx : this._defendersWaiting)
			{
				buffer.writeInt(defenderx.getId());
				buffer.writeString(defenderx.getName());
				buffer.writeString(defenderx.getLeaderName());
				buffer.writeInt(defenderx.getCrestId());
				buffer.writeInt(0);
				buffer.writeInt(SiegeClanType.DEFENDER_PENDING.ordinal());
				buffer.writeInt(defenderx.isRecruitMercenary());
				buffer.writeLong(defenderx.getRewardMercenary());
				buffer.writeInt(defenderx.getMapMercenary().size());
				buffer.writeLong(0L);
				buffer.writeLong(0L);
				if (defenderx.getAllyId() != 0)
				{
					buffer.writeInt(defenderx.getAllyId());
					buffer.writeString(defenderx.getAllyName());
					buffer.writeString("");
					buffer.writeInt(defenderx.getAllyCrestId());
				}
				else
				{
					buffer.writeInt(0);
					buffer.writeString("");
					buffer.writeString("");
					buffer.writeInt(0);
				}
			}
		}
	}
}
