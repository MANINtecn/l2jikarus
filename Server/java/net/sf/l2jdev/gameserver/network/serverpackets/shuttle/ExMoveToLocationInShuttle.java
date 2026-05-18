package net.sf.l2jdev.gameserver.network.serverpackets.shuttle;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExMoveToLocationInShuttle extends ServerPacket
{
	private final int _objectId;
	private final int _airShipId;
	private final int _targetX;
	private final int _targetY;
	private final int _targetZ;
	private final int _fromX;
	private final int _fromY;
	private final int _fromZ;

	public ExMoveToLocationInShuttle(Player player, int fromX, int fromY, int fromZ)
	{
		this._objectId = player.getObjectId();
		this._airShipId = player.getShuttle().getObjectId();
		this._targetX = player.getInVehiclePosition().getX();
		this._targetY = player.getInVehiclePosition().getY();
		this._targetZ = player.getInVehiclePosition().getZ();
		this._fromX = fromX;
		this._fromY = fromY;
		this._fromZ = fromZ;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_MOVE_TO_LOCATION_IN_SHUTTLE.writeId(this, buffer);
		buffer.writeInt(this._objectId);
		buffer.writeInt(this._airShipId);
		buffer.writeInt(this._targetX);
		buffer.writeInt(this._targetY);
		buffer.writeInt(this._targetZ);
		buffer.writeInt(this._fromX);
		buffer.writeInt(this._fromY);
		buffer.writeInt(this._fromZ);
	}
}
