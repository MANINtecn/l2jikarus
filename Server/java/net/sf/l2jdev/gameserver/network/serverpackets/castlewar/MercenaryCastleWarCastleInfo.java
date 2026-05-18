package net.sf.l2jdev.gameserver.network.serverpackets.castlewar;

import java.util.Calendar;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.model.siege.TaxType;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class MercenaryCastleWarCastleInfo extends ServerPacket
{
	private final Castle _castle;

	public MercenaryCastleWarCastleInfo(int castleId)
	{
		this._castle = CastleManager.getInstance().getCastleById(castleId);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_MERCENARY_CASTLEWAR_CASTLE_INFO.writeId(this, buffer);
		if (this._castle == null)
		{
			buffer.writeInt(this._castle.getResidenceId());
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeSizedString("");
			buffer.writeSizedString("");
			buffer.writeInt(0);
			buffer.writeLong(0L);
			buffer.writeLong(0L);
			buffer.writeInt(0);
		}
		else
		{
			Clan clan = this._castle.getOwner();
			buffer.writeInt(this._castle.getResidenceId());
			buffer.writeInt(clan != null ? clan.getCrestId() : 0);
			buffer.writeInt(clan != null ? clan.getCrestLargeId() : 0);
			buffer.writeSizedString(clan != null ? clan.getName() : "-");
			buffer.writeSizedString(clan != null ? clan.getLeaderName() : "-");
			buffer.writeInt(this._castle.getTaxPercent(TaxType.BUY));
			buffer.writeLong(this._castle.getTempTreasury());
			long treasury = this._castle.getTreasury();
			buffer.writeLong((long) (treasury + treasury * this._castle.getTaxRate(TaxType.BUY)));
			Calendar siegeDate = this._castle.getSiegeDate();
			buffer.writeInt(siegeDate != null ? (int) (siegeDate.getTimeInMillis() / 1000L) : 0);
		}
	}
}
