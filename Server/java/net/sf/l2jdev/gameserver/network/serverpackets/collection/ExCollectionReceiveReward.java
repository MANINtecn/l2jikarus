package net.sf.l2jdev.gameserver.network.serverpackets.collection;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExCollectionReceiveReward extends ServerPacket
{
	private final int _collectionId;
	private final boolean _success;

	public ExCollectionReceiveReward(int collectionId, boolean success)
	{
		this._collectionId = collectionId;
		this._success = success;
	}

	@Override
	protected void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_COLLECTION_RECEIVE_REWARD.writeId(this, buffer);
		buffer.writeShort(this._collectionId);
		buffer.writeByte(this._success);
	}
}
