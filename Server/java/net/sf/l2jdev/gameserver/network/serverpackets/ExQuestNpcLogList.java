package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.script.NpcLogListHolder;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.NpcStringId;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExQuestNpcLogList extends ServerPacket
{
	private final int _questId;
	private final List<NpcLogListHolder> _npcLogList = new ArrayList<>();

	public ExQuestNpcLogList(int questId)
	{
		this._questId = questId;
	}

	public void addNpc(int npcId, int count)
	{
		this._npcLogList.add(new NpcLogListHolder(npcId, false, count));
	}

	public void addNpcString(NpcStringId npcStringId, int count)
	{
		this._npcLogList.add(new NpcLogListHolder(npcStringId.getId(), true, count));
	}

	public void add(NpcLogListHolder holder)
	{
		this._npcLogList.add(holder);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_QUEST_NPC_LOG_LIST.writeId(this, buffer);
		buffer.writeInt(this._questId);
		buffer.writeByte(this._npcLogList.size());

		for (NpcLogListHolder holder : this._npcLogList)
		{
			buffer.writeInt(holder.isNpcString() ? holder.getId() : holder.getId() + 1000000);
			buffer.writeByte(holder.isNpcString());
			buffer.writeInt(holder.getCount());
		}
	}
}
