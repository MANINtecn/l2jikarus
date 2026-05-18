package net.sf.l2jdev.gameserver.network.serverpackets.fishing;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.interfaces.ILocational;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExUserInfoFishing extends ServerPacket
{
	private final Player _player;
	private final boolean _isFishing;
	private final ILocational _baitLocation;

	public ExUserInfoFishing(Player player, boolean isFishing, ILocational baitLocation)
	{
		this._player = player;
		this._isFishing = isFishing;
		this._baitLocation = baitLocation;
	}

	public ExUserInfoFishing(Player player, boolean isFishing)
	{
		this._player = player;
		this._isFishing = isFishing;
		this._baitLocation = null;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_USER_INFO_FISHING.writeId(this, buffer);
		buffer.writeInt(this._player.getObjectId());
		buffer.writeByte(this._isFishing);
		if (this._baitLocation == null)
		{
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
		}
		else
		{
			buffer.writeInt(this._baitLocation.getX());
			buffer.writeInt(this._baitLocation.getY());
			buffer.writeInt(this._baitLocation.getZ());
		}
	}
}
