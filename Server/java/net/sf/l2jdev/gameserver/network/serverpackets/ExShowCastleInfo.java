package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Collection;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.model.siege.TaxType;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExShowCastleInfo extends ServerPacket
{
	private final Collection<Castle> _castles = CastleManager.getInstance().getCastles();

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHOW_CASTLE_INFO.writeId(this, buffer);
		buffer.writeInt(this._castles.size());

		for (Castle castle : this._castles)
		{
			buffer.writeInt(castle.getResidenceId());
			if (castle.getOwnerId() > 0)
			{
				if (ClanTable.getInstance().getClan(castle.getOwnerId()) != null)
				{
					buffer.writeString(ClanTable.getInstance().getClan(castle.getOwnerId()).getName());
				}
				else
				{
					PacketLogger.warning("Castle owner with no name! Castle: " + castle.getName() + " has an OwnerId = " + castle.getOwnerId() + " who does not have a  name!");
					buffer.writeString("");
				}
			}
			else
			{
				buffer.writeString("");
			}

			buffer.writeInt(castle.getTaxPercent(TaxType.BUY));
			buffer.writeInt((int) (castle.getSiege().getSiegeDate().getTimeInMillis() / 1000L));
			buffer.writeByte(castle.getSiege().isInProgress());
			buffer.writeByte(castle.getSide().ordinal());
		}
	}
}
