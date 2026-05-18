package net.sf.l2jdev.gameserver.network.serverpackets.balok;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.sql.CharInfoTable;
import net.sf.l2jdev.gameserver.managers.BattleWithBalokManager;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class BalrogWarShowRanking extends ServerPacket
{
	private final List<BalrogWarShowRanking.RankHolder> _ranking = new LinkedList<>();

	public BalrogWarShowRanking()
	{
		int rank = 1;

		for (Entry<Integer, Integer> entry : BattleWithBalokManager.getInstance().getTopPlayers(150).entrySet())
		{
			this._ranking.add(new BalrogWarShowRanking.RankHolder(rank++, CharInfoTable.getInstance().getNameById(entry.getKey()), entry.getValue()));
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_BALROGWAR_SHOW_RANKING.writeId(this, buffer);
		buffer.writeInt(this._ranking.size());

		for (BalrogWarShowRanking.RankHolder holder : this._ranking)
		{
			buffer.writeInt(holder.getRank());
			buffer.writeSizedString(holder.getName());
			buffer.writeInt(holder.getScore());
		}
	}

	private class RankHolder
	{
		private final int _score;
		private final String _name;
		private final int _rank;

		public RankHolder(int score, String name, int rank)
		{
			Objects.requireNonNull(BalrogWarShowRanking.this);
			super();
			this._score = score;
			this._name = name;
			this._rank = rank;
		}

		public int getScore()
		{
			return this._score;
		}

		public String getName()
		{
			return this._name;
		}

		public int getRank()
		{
			return this._rank;
		}
	}
}
