package net.sf.l2jdev.gameserver.network.serverpackets.subjugation;

import java.util.Map;
import java.util.Map.Entry;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExSubjugationGacha extends ServerPacket
{
	private final Map<Integer, Integer> _rewards;

	public ExSubjugationGacha(Map<Integer, Integer> rewards)
	{
		this._rewards = rewards;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SUBJUGATION_GACHA.writeId(this, buffer);
		buffer.writeInt(this._rewards.size());

		for (Entry<Integer, Integer> entry : this._rewards.entrySet())
		{
			buffer.writeInt(entry.getKey());
			buffer.writeInt(entry.getValue());
		}
	}
}
