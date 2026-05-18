package net.sf.l2jdev.gameserver.network.serverpackets.ranking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.OlympiadConfig;
import net.sf.l2jdev.gameserver.config.ServerConfig;
import net.sf.l2jdev.gameserver.managers.RankManager;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PlayerClass;
import net.sf.l2jdev.gameserver.model.actor.enums.player.RankingOlympiadCategory;
import net.sf.l2jdev.gameserver.model.actor.enums.player.RankingOlympiadScope;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExOlympiadRankingInfo extends ServerPacket
{
	private final Player _player;
	private final int _tabId;
	private final int _rankingType;
	private final int _unk;
	private final int _classId;
	private final int _serverId;
	private final Map<Integer, StatSet> _playerList;
	private final Map<Integer, StatSet> _snapshotList;

	public ExOlympiadRankingInfo(Player player, int tabId, int rankingType, int unk, int classId, int serverId)
	{
		this._player = player;
		this._tabId = tabId;
		this._rankingType = rankingType;
		this._unk = unk;
		this._classId = classId;
		this._serverId = serverId;
		this._playerList = RankManager.getInstance().getOlyRankList();
		this._snapshotList = RankManager.getInstance().getSnapshotOlyList();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_OLYMPIAD_RANKING_INFO.writeId(this, buffer);
		buffer.writeByte(this._tabId);
		buffer.writeByte(this._rankingType);
		buffer.writeByte(this._unk);
		buffer.writeInt(this._classId);
		buffer.writeInt(this._serverId);
		buffer.writeInt(933);
		buffer.writeInt(OlympiadConfig.OLYMPIAD_MIN_MATCHES);
		if (!this._playerList.isEmpty())
		{
			RankingOlympiadCategory category = RankingOlympiadCategory.values()[this._tabId];
			this.writeFilteredRankingData(category, category.getScopeByGroup(this._rankingType), PlayerClass.getPlayerClass(this._classId), buffer);
		}
	}

	private void writeFilteredRankingData(RankingOlympiadCategory category, RankingOlympiadScope scope, PlayerClass playerClass, WritableBuffer buffer)
	{
		switch (category)
		{
			case SERVER:
				this.writeScopeData(scope, new ArrayList<>(this._playerList.entrySet()), new ArrayList<>(this._snapshotList.entrySet()), buffer);
				break;
			case CLASS:
				this.writeScopeData(scope, this._playerList.entrySet().stream().filter(it -> it.getValue().getInt("classId") == playerClass.getId()).collect(Collectors.toList()), this._snapshotList.entrySet().stream().filter(it -> it.getValue().getInt("classId") == playerClass.getId()).collect(Collectors.toList()), buffer);
		}
	}

	private void writeScopeData(RankingOlympiadScope scope, List<Entry<Integer, StatSet>> list, List<Entry<Integer, StatSet>> snapshot, WritableBuffer buffer)
	{
		Entry<Integer, StatSet> playerData = list.stream().filter(it -> it.getValue().getInt("charId", 0) == this._player.getObjectId()).findFirst().orElse(null);
		int indexOf = list.indexOf(playerData);

		List<Entry<Integer, StatSet>> limited = switch (scope)
		{
			case TOP_100 -> list.stream().limit(100L).collect(Collectors.toList());
			case ALL -> list;
			case TOP_50 -> list.stream().limit(50L).collect(Collectors.toList());
			case SELF -> playerData == null ? Collections.emptyList() : list.subList(Math.max(0, indexOf - 10), Math.min(list.size(), indexOf + 10));
			default -> Collections.emptyList();
		};
		buffer.writeInt(limited.size());
		int rank = 1;

		for (Entry<Integer, StatSet> data : limited.stream().sorted(Entry.comparingByKey()).collect(Collectors.toList()))
		{
			int curRank = rank++;
			StatSet player = data.getValue();
			buffer.writeSizedString(player.getString("name"));
			buffer.writeSizedString(player.getString("clanName"));
			buffer.writeInt(scope == RankingOlympiadScope.SELF ? data.getKey() : curRank);
			if (!snapshot.isEmpty())
			{
				int snapshotRank = 1;

				for (Entry<Integer, StatSet> ssData : snapshot.stream().sorted(Entry.comparingByKey()).collect(Collectors.toList()))
				{
					StatSet snapshotData = ssData.getValue();
					if (player.getInt("charId") == snapshotData.getInt("charId"))
					{
						buffer.writeInt(scope == RankingOlympiadScope.SELF ? ssData.getKey() : snapshotRank++);
					}
				}
			}
			else
			{
				buffer.writeInt(scope == RankingOlympiadScope.SELF ? data.getKey() : curRank);
			}

			buffer.writeInt(OlympiadConfig.OLYMPIAD_MIN_MATCHES);
			buffer.writeInt(ServerConfig.SERVER_ID);
			buffer.writeInt(player.getInt("level"));
			buffer.writeInt(player.getInt("classId"));
			buffer.writeInt(player.getInt("clanLevel"));
			buffer.writeInt(player.getInt("competitions_won"));
			buffer.writeInt(player.getInt("competitions_lost"));
			buffer.writeInt(player.getInt("competitions_drawn"));
			buffer.writeInt(player.getInt("olympiad_points"));
			buffer.writeInt(player.getInt("legend_count"));
			buffer.writeInt(player.getInt("count"));
		}
	}
}
