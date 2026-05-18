package net.sf.l2jdev.gameserver.network.serverpackets.relics;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.holders.player.PlayerRelicData;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExRelicsUpdateList extends ServerPacket
{
	private final List<PlayerRelicData> _updatedRelics;

	public ExRelicsUpdateList(int relicListSize, int relicId, int relicLevel, int relicCount)
	{
		PlayerRelicData relic = new PlayerRelicData(relicId, relicLevel, 0, 0, relicCount);
		this._updatedRelics = new ArrayList<>(relicListSize);

		for (int i = 0; i < relicListSize; i++)
		{
			this._updatedRelics.add(relic);
		}
	}

	public ExRelicsUpdateList(List<PlayerRelicData> updatedRelics)
	{
		this._updatedRelics = updatedRelics;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_RELICS_UPDATE_LIST.writeId(this, buffer);
		buffer.writeInt(this._updatedRelics.size());

		for (PlayerRelicData relic : this._updatedRelics)
		{
			buffer.writeInt(relic.getRelicId());
			buffer.writeInt(relic.getRelicLevel());
			buffer.writeInt(relic.getRelicCount());
		}
	}
}
