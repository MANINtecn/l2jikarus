package net.sf.l2jdev.gameserver.network.serverpackets.fishing;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.interfaces.ILocational;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExFishingStart extends ServerPacket
{
	private final Player _player;
	private final int _fishType;
	private final ILocational _baitLocation;

	public ExFishingStart(Player player, int fishType, ILocational baitLocation)
	{
		this._player = player;
		this._fishType = fishType;
		this._baitLocation = baitLocation;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_FISHING_START.writeId(this, buffer);
		buffer.writeInt(this._player.getObjectId());
		buffer.writeByte(this._fishType);
		buffer.writeInt(this._baitLocation.getX());
		buffer.writeInt(this._baitLocation.getY());
		buffer.writeInt(this._baitLocation.getZ());
		buffer.writeByte(1);
	}
}
