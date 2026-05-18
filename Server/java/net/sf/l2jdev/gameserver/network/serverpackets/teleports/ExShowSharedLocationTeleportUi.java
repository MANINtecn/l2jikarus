package net.sf.l2jdev.gameserver.network.serverpackets.teleports;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.data.holders.SharedTeleportHolder;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExShowSharedLocationTeleportUi extends ServerPacket
{
	private final SharedTeleportHolder _teleport;

	public ExShowSharedLocationTeleportUi(SharedTeleportHolder teleport)
	{
		this._teleport = teleport;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHARED_POSITION_TELEPORT_UI.writeId(this, buffer);
		buffer.writeSizedString(this._teleport.getName());
		buffer.writeInt(this._teleport.getId());
		buffer.writeInt(this._teleport.getCount());
		buffer.writeShort(150);
		Location location = this._teleport.getLocation();
		buffer.writeInt(location.getX());
		buffer.writeInt(location.getY());
		buffer.writeInt(location.getZ());
		buffer.writeLong(GeneralConfig.TELEPORT_SHARE_LOCATION_COST);
	}
}
