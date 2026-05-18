package net.sf.l2jdev.gameserver.network.serverpackets.relics;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExRelicsCollectionCompleteAnnounce extends ServerPacket
{
	private final int _relicCollectionId;

	public ExRelicsCollectionCompleteAnnounce(int relicCollectionId)
	{
		this._relicCollectionId = relicCollectionId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_RELICS_COLLECTION_COMPLETE_ANNOUNCE.writeId(this, buffer);
		buffer.writeInt(this._relicCollectionId);
	}
}
