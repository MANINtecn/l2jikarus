package net.sf.l2jdev.gameserver.network.serverpackets.relics;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class RelicsProbList extends ServerPacket
{
	private final int _type;
	private final int _grade;
	private Map<Integer, Long> _relics = new LinkedHashMap<>();

	public RelicsProbList(int type, int grade, Map<Integer, Long> relics)
	{
		this._type = type;
		this._grade = grade;
		this._relics = relics;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_RELICS_PROB_LIST.writeId(this, buffer);
		buffer.writeInt(this._type);
		buffer.writeInt(this._grade);
		buffer.writeInt(this._relics.size());

		for (Entry<Integer, Long> entry : this._relics.entrySet())
		{
			buffer.writeInt(entry.getKey());
			buffer.writeLong(entry.getValue());
		}
	}
}
