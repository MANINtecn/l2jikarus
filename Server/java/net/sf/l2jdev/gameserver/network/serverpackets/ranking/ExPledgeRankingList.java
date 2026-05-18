package net.sf.l2jdev.gameserver.network.serverpackets.ranking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.managers.RankManager;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExPledgeRankingList extends ServerPacket
{
	private final Player _player;
	private final int _category;
	private final Map<Integer, StatSet> _rankingClanList;
	private final Map<Integer, StatSet> _snapshotClanList;

	public ExPledgeRankingList(Player player, int category)
	{
		this._player = player;
		this._category = category;
		this._rankingClanList = RankManager.getInstance().getClanRankList();
		this._snapshotClanList = RankManager.getInstance().getSnapshotClanRankList();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PLEDGE_RANKING_LIST.writeId(this, buffer);
		buffer.writeByte(this._category);
		if (!this._rankingClanList.isEmpty())
		{
			this.writeScopeData(buffer, this._category == 0, new ArrayList<>(this._rankingClanList.entrySet()), new ArrayList<>(this._snapshotClanList.entrySet()));
		}
		else
		{
			buffer.writeInt(0);
		}
	}

	private void writeScopeData(WritableBuffer buffer, boolean isTop150, List<Entry<Integer, StatSet>> list, List<Entry<Integer, StatSet>> snapshot)
	{
		Entry<Integer, StatSet> playerData = list.stream().filter(it -> it.getValue().getInt("clan_id", 0) == this._player.getClanId()).findFirst().orElse(null);
		int indexOf = list.indexOf(playerData);
		List<Entry<Integer, StatSet>> limited = isTop150 ? list.stream().limit(150L).collect(Collectors.toList()) : (playerData == null ? Collections.emptyList() : list.subList(Math.max(0, indexOf - 10), Math.min(list.size(), indexOf + 10)));
		buffer.writeInt(limited.size());
		int rank = 1;

		for (Entry<Integer, StatSet> data : limited.stream().sorted(Entry.comparingByKey()).collect(Collectors.toList()))
		{
			int curRank = rank++;
			StatSet player = data.getValue();
			buffer.writeInt(!isTop150 ? data.getKey() : curRank);

			for (Entry<Integer, StatSet> ssData : snapshot.stream().sorted(Entry.comparingByKey()).collect(Collectors.toList()))
			{
				StatSet snapshotData = ssData.getValue();
				if (player.getInt("clan_id") == snapshotData.getInt("clan_id"))
				{
					buffer.writeInt(!isTop150 ? ssData.getKey() : curRank);
				}
			}

			buffer.writeSizedString(player.getString("clan_name"));
			buffer.writeInt(player.getInt("clan_level"));
			buffer.writeSizedString(player.getString("char_name"));
			buffer.writeInt(player.getInt("level"));
			buffer.writeInt(ClanTable.getInstance().getClan(player.getInt("clan_id")) != null ? ClanTable.getInstance().getClan(player.getInt("clan_id")).getMembersCount() : 0);
			buffer.writeInt((int) Math.min(2147483647L, player.getLong("exp")));
		}
	}
}
