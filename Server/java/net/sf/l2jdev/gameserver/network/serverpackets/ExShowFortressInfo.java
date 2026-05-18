package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Collection;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.FortManager;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.siege.Fort;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExShowFortressInfo extends ServerPacket
{
	public static final ExShowFortressInfo STATIC_PACKET = new ExShowFortressInfo();

	private ExShowFortressInfo()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHOW_FORTRESS_INFO.writeId(this, buffer);
		Collection<Fort> forts = FortManager.getInstance().getForts();
		buffer.writeInt(forts.size());

		for (Fort fort : forts)
		{
			Clan clan = fort.getOwnerClan();
			buffer.writeInt(fort.getResidenceId());
			buffer.writeString(clan != null ? clan.getName() : "");
			buffer.writeInt(fort.getSiege().isInProgress());
			buffer.writeInt(fort.getOwnedTime());
		}
	}
}
