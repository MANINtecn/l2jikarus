package net.sf.l2jdev.gameserver.network.serverpackets.sayune;

import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.SayuneEntry;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.enums.SayuneType;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExFlyMove extends ServerPacket
{
	private final int _objectId;
	private final SayuneType _type;
	private final int _mapId;
	private final List<SayuneEntry> _locations;

	public ExFlyMove(Player player, SayuneType type, int mapId, List<SayuneEntry> locations)
	{
		this._objectId = player.getObjectId();
		this._type = type;
		this._mapId = mapId;
		this._locations = locations;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_FLY_MOVE.writeId(this, buffer);
		buffer.writeInt(this._objectId);
		buffer.writeInt(this._type.ordinal());
		buffer.writeInt(0);
		buffer.writeInt(this._mapId);
		buffer.writeInt(this._locations.size());

		for (SayuneEntry loc : this._locations)
		{
			buffer.writeInt(loc.getId());
			buffer.writeInt(0);
			buffer.writeInt(loc.getX());
			buffer.writeInt(loc.getY());
			buffer.writeInt(loc.getZ());
		}
	}
}
