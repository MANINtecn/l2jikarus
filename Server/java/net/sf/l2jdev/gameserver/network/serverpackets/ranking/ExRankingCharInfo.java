package net.sf.l2jdev.gameserver.network.serverpackets.ranking;

import java.util.Map;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.RankManager;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExRankingCharInfo extends ServerPacket
{
	private final Player _player;
	private final Map<Integer, StatSet> _playerList;
	private final Map<Integer, StatSet> _snapshotList;

	public ExRankingCharInfo(Player player)
	{
		this._player = player;
		this._playerList = RankManager.getInstance().getRankList();
		this._snapshotList = RankManager.getInstance().getSnapshotList();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_RANKING_CHAR_INFO.writeId(this, buffer);
		if (!this._playerList.isEmpty())
		{
			for (Integer id : this._playerList.keySet())
			{
				StatSet player = this._playerList.get(id);
				if (player.getInt("charId") == this._player.getObjectId())
				{
					buffer.writeInt(id);
					buffer.writeInt(player.getInt("raceRank"));
					buffer.writeInt(player.getInt("classRank"));

					for (Integer id2 : this._snapshotList.keySet())
					{
						StatSet snapshot = this._snapshotList.get(id2);
						if (player.getInt("charId") == snapshot.getInt("charId"))
						{
							buffer.writeInt(id2);
							buffer.writeInt(snapshot.getInt("classRank"));
							buffer.writeInt(player.getInt("classRank"));
							buffer.writeInt(0);
							buffer.writeInt(0);
							buffer.writeInt(0);
							return;
						}
					}
				}
			}

			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
		}
		else
		{
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
		}
	}
}
