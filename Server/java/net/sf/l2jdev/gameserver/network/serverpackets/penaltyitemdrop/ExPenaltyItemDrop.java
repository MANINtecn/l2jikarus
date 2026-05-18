package net.sf.l2jdev.gameserver.network.serverpackets.penaltyitemdrop;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExPenaltyItemDrop extends ServerPacket
{
	private final Location _dropLoc;
	private final int _itemId;

	public ExPenaltyItemDrop(Location dropLoc, int itemId)
	{
		this._dropLoc = dropLoc;
		this._itemId = itemId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PENALTY_ITEM_DROP.writeId(this, buffer);
		buffer.writeInt(this._dropLoc.getX());
		buffer.writeInt(this._dropLoc.getY());
		buffer.writeInt(this._dropLoc.getZ());
		buffer.writeInt(this._itemId);
	}
}
