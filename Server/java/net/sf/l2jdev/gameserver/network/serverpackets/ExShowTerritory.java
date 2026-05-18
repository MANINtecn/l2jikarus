package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.interfaces.ILocational;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExShowTerritory extends ServerPacket
{
	private final int _minZ;
	private final int _maxZ;
	private final List<ILocational> _vertices = new ArrayList<>();

	public ExShowTerritory(int minZ, int maxZ)
	{
		this._minZ = minZ;
		this._maxZ = maxZ;
	}

	public void addVertice(ILocational loc)
	{
		this._vertices.add(loc);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHOW_TERRITORY.writeId(this, buffer);
		buffer.writeInt(this._vertices.size());
		buffer.writeInt(this._minZ);
		buffer.writeInt(this._maxZ);

		for (ILocational loc : this._vertices)
		{
			buffer.writeInt(loc.getX());
			buffer.writeInt(loc.getY());
		}
	}
}
