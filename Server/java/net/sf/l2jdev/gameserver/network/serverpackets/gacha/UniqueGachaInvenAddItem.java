package net.sf.l2jdev.gameserver.network.serverpackets.gacha;

import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.item.holders.GachaItemHolder;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class UniqueGachaInvenAddItem extends ServerPacket
{
	private final List<GachaItemHolder> _rewards;

	public UniqueGachaInvenAddItem(List<GachaItemHolder> rewards)
	{
		this._rewards = rewards;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_UNIQUE_GACHA_INVEN_ADD_ITEM.writeId(this, buffer);
		buffer.writeInt(this._rewards.size());

		for (ItemHolder item : this._rewards)
		{
			buffer.writeInt(item.getId());
			buffer.writeLong(item.getCount());
		}
	}
}
