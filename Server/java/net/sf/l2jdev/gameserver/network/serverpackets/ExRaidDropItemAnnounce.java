package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Collection;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExRaidDropItemAnnounce extends ServerPacket
{
	private final String _killerName;
	private final int _npcId;
	private final Collection<Integer> _items;

	public ExRaidDropItemAnnounce(String killerName, int npcId, Collection<Integer> items)
	{
		this._killerName = killerName;
		this._npcId = npcId + 1000000;
		this._items = items;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_RAID_DROP_ITEM_ANNOUNCE.writeId(this, buffer);
		buffer.writeSizedString(this._killerName);
		buffer.writeInt(this._npcId);
		buffer.writeInt(this._items.size());

		for (int itemId : this._items)
		{
			buffer.writeInt(itemId);
		}
	}
}
