package net.sf.l2jdev.gameserver.network.serverpackets.sayune;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.interfaces.ILocational;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.enums.SayuneType;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExFlyMoveBroadcast extends ServerPacket
{
	private final int _objectId;
	private final int _mapId;
	private final ILocational _currentLoc;
	private final ILocational _targetLoc;
	private final SayuneType _type;

	public ExFlyMoveBroadcast(Player player, SayuneType type, int mapId, ILocational targetLoc)
	{
		this._objectId = player.getObjectId();
		this._type = type;
		this._mapId = mapId;
		this._currentLoc = player;
		this._targetLoc = targetLoc;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_FLY_MOVE_BROADCAST.writeId(this, buffer);
		buffer.writeInt(this._objectId);
		buffer.writeInt(this._type.ordinal());
		buffer.writeInt(this._mapId);
		buffer.writeInt(this._targetLoc.getX());
		buffer.writeInt(this._targetLoc.getY());
		buffer.writeInt(this._targetLoc.getZ());
		buffer.writeInt(0);
		buffer.writeInt(this._currentLoc.getX());
		buffer.writeInt(this._currentLoc.getY());
		buffer.writeInt(this._currentLoc.getZ());
	}
}
