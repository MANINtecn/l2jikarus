package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.FenceState;
import net.sf.l2jdev.gameserver.model.actor.instance.Fence;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExColosseumFenceInfo extends ServerPacket
{
	private final int _objId;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _width;
	private final int _length;
	private final int _clientState;

	public ExColosseumFenceInfo(Fence fence)
	{
		this(fence.getObjectId(), fence.getX(), fence.getY(), fence.getZ(), fence.getWidth(), fence.getLength(), fence.getState());
	}

	public ExColosseumFenceInfo(int objId, double x, double y, double z, int width, int length, FenceState state)
	{
		this._objId = objId;
		this._x = (int) x;
		this._y = (int) y;
		this._z = (int) z;
		this._width = width;
		this._length = length;
		this._clientState = state.getClientId();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_COLOSSEUM_FENCE_INFO.writeId(this, buffer);
		buffer.writeInt(this._objId);
		buffer.writeInt(this._clientState);
		buffer.writeInt(this._x);
		buffer.writeInt(this._y);
		buffer.writeInt(this._z);
		buffer.writeInt(this._width);
		buffer.writeInt(this._length);
	}
}
