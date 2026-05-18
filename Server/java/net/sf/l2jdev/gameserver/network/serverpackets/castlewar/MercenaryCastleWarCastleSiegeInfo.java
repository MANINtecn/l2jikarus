package net.sf.l2jdev.gameserver.network.serverpackets.castlewar;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.model.siege.Siege;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class MercenaryCastleWarCastleSiegeInfo extends ServerPacket
{
	private final int _castleId;
	private final Castle _castle;

	public MercenaryCastleWarCastleSiegeInfo(int castleId)
	{
		this._castleId = castleId;
		this._castle = CastleManager.getInstance().getCastleById(castleId);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_MERCENARY_CASTLEWAR_CASTLE_SIEGE_INFO.writeId(this, buffer);
		buffer.writeInt(this._castleId);
		if (this._castle == null)
		{
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeSizedString("-");
			buffer.writeSizedString("-");
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
		}
		else
		{
			buffer.writeInt(0);
			buffer.writeInt(0);
			Clan clan = this._castle.getOwner();
			buffer.writeSizedString(clan != null ? clan.getName() : "-");
			buffer.writeSizedString(clan != null ? clan.getLeaderName() : "-");
			buffer.writeInt(0);
			Siege siege = this._castle.getSiege();
			buffer.writeInt(siege.getAttackerClans().size());
			buffer.writeInt(siege.getDefenderClans().size() + siege.getDefenderWaitingClans().size());
		}
	}
}
