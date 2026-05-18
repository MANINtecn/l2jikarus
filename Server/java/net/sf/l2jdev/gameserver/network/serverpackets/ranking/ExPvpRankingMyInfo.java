package net.sf.l2jdev.gameserver.network.serverpackets.ranking;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.RankManager;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExPvpRankingMyInfo extends ServerPacket
{
	private final Player _player;
	private final Map<Integer, StatSet> _playerList;
	private final Map<Integer, StatSet> _snapshotList;

	public ExPvpRankingMyInfo(Player player)
	{
		this._player = player;
		this._playerList = RankManager.getInstance().getPvpRankList();
		this._snapshotList = RankManager.getInstance().getSnapshotPvpRankList();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PVP_RANKING_MY_INFO.writeId(this, buffer);
		if (!this._playerList.isEmpty())
		{
			boolean found = false;

			for (Integer id : this._playerList.keySet())
			{
				StatSet ss = this._playerList.get(id);
				if (ss.getInt("charId") == this._player.getObjectId())
				{
					Optional<Entry<Integer, StatSet>> snapshotValue = this._snapshotList.entrySet().stream().filter(it -> it.getValue().getInt("charId") == this._player.getObjectId()).findFirst();
					found = true;
					buffer.writeLong(ss.getInt("points"));
					buffer.writeInt(id);
					buffer.writeInt(snapshotValue.isPresent() ? snapshotValue.get().getKey() : id);
					buffer.writeInt(ss.getInt("kills"));
					buffer.writeInt(ss.getInt("deaths"));
				}
			}

			if (!found)
			{
				buffer.writeLong(0L);
				buffer.writeInt(0);
				buffer.writeInt(0);
				buffer.writeInt(0);
				buffer.writeInt(0);
			}
		}
		else
		{
			buffer.writeLong(0L);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
		}
	}
}
