package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.interfaces.ILocational;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExShowTrace extends ServerPacket
{
	private final List<Location> _locations = new ArrayList<>();

	public void addLocation(int x, int y, int z)
	{
		this._locations.add(new Location(x, y, z));
	}

	public void addLocation(ILocational loc)
	{
		this.addLocation(loc.getX(), loc.getY(), loc.getZ());
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHOW_TRACE.writeId(this, buffer);
		buffer.writeShort(0);
		buffer.writeInt(0);
		buffer.writeShort(this._locations.size());

		for (Location loc : this._locations)
		{
			buffer.writeInt(loc.getX());
			buffer.writeInt(loc.getY());
			buffer.writeInt(loc.getZ());
		}
	}
}
