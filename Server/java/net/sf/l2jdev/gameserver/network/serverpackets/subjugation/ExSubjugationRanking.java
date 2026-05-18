package net.sf.l2jdev.gameserver.network.serverpackets.subjugation;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.PurgeRankingManager;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExSubjugationRanking extends ServerPacket
{
	private final Map<String, Integer> _ranking;
	private final int _category;
	private final SimpleEntry<Integer, Integer> _playerPoints;

	public ExSubjugationRanking(int category, int objectId)
	{
		this._ranking = PurgeRankingManager.getInstance().getTop5(category);
		this._category = category;
		this._playerPoints = PurgeRankingManager.getInstance().getPlayerRating(this._category, objectId);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SUBJUGATION_RANKING.writeId(this, buffer);
		buffer.writeInt(this._ranking.entrySet().size());
		int counter = 1;

		for (Entry<String, Integer> data : this._ranking.entrySet())
		{
			buffer.writeSizedString(data.getKey());
			buffer.writeInt(data.getValue());
			buffer.writeInt(counter++);
		}

		buffer.writeInt(this._category);
		buffer.writeInt(this._playerPoints.getValue());
		buffer.writeInt(this._playerPoints.getKey());
	}
}
