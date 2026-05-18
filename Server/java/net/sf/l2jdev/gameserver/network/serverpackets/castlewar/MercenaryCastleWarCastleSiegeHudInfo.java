package net.sf.l2jdev.gameserver.network.serverpackets.castlewar;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.managers.SiegeManager;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class MercenaryCastleWarCastleSiegeHudInfo extends ServerPacket
{
	private final Castle _castle;

	public MercenaryCastleWarCastleSiegeHudInfo(int castleId)
	{
		this._castle = CastleManager.getInstance().getCastleById(castleId);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		if (this._castle != null)
		{
			ServerPackets.EX_MERCENARY_CASTLEWAR_CASTLE_SIEGE_HUD_INFO.writeId(this, buffer);
			buffer.writeInt(this._castle.getResidenceId());
			if (this._castle.getSiege().isInProgress())
			{
				buffer.writeInt(1);
				buffer.writeInt(0);
				buffer.writeInt((int) ((this._castle.getSiegeDate().getTimeInMillis() + SiegeManager.getInstance().getSiegeLength() * 60000 - System.currentTimeMillis()) / 1000L));
			}
			else
			{
				buffer.writeInt(0);
				buffer.writeInt(0);
				buffer.writeInt((int) ((this._castle.getSiegeDate().getTimeInMillis() - System.currentTimeMillis()) / 1000L));
			}
		}
	}
}
