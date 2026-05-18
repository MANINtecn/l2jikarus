package net.sf.l2jdev.gameserver.network.serverpackets.ranking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.RankManager;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Race;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PlayerClass;
import net.sf.l2jdev.gameserver.model.actor.enums.player.RankingCategory;
import net.sf.l2jdev.gameserver.model.actor.enums.player.RankingScope;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExPvpRankingList extends ServerPacket
{
	private final Player _player;
	private final int _season;
	private final int _tabId;
	private final int _type;
	private final int _race;
	private final int _class;
	private final Map<Integer, StatSet> _playerList;
	private final Map<Integer, StatSet> _snapshotList;

	public ExPvpRankingList(Player player, int season, int tabId, int type, int race, int baseclass)
	{
		this._player = player;
		this._season = season;
		this._tabId = tabId;
		this._type = type;
		this._race = race;
		this._class = baseclass;
		this._playerList = RankManager.getInstance().getPvpRankList();
		this._snapshotList = RankManager.getInstance().getSnapshotPvpRankList();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PVP_RANKING_LIST.writeId(this, buffer);
		buffer.writeByte(this._season);
		buffer.writeByte(this._tabId);
		buffer.writeByte(this._type);
		buffer.writeInt(this._race);
		if (!this._playerList.isEmpty() && this._type != 255 && this._race != 255)
		{
			RankingCategory category = RankingCategory.values()[this._tabId];
			this.writeFilteredRankingData(category, category.getScopeByGroup(this._type), Race.values()[this._race], PlayerClass.getPlayerClass(this._class), buffer);
		}
		else
		{
			buffer.writeInt(0);
		}
	}

	private void writeFilteredRankingData(RankingCategory category, RankingScope scope, Race race, PlayerClass baseclass, WritableBuffer buffer)
	{
		switch (category)
		{
			case SERVER:
				this.writeScopeData(scope, new ArrayList<>(this._playerList.entrySet()), new ArrayList<>(this._snapshotList.entrySet()), buffer);
				break;
			case RACE:
				this.writeScopeData(scope, this._playerList.entrySet().stream().filter(it -> it.getValue().getInt("race") == race.ordinal()).collect(Collectors.toList()), this._snapshotList.entrySet().stream().filter(it -> it.getValue().getInt("race") == race.ordinal()).collect(Collectors.toList()), buffer);
				break;
			case CLAN:
				this.writeScopeData(scope, this._player.getClan() == null ? Collections.emptyList() : this._playerList.entrySet().stream().filter(it -> it.getValue().getString("clanName").equals(this._player.getClan().getName())).collect(Collectors.toList()), this._player.getClan() == null ? Collections.emptyList() : this._snapshotList.entrySet().stream().filter(it -> it.getValue().getString("clanName").equals(this._player.getClan().getName())).collect(Collectors.toList()), buffer);
				break;
			case FRIEND:
				this.writeScopeData(scope, this._playerList.entrySet().stream().filter(it -> this._player.getFriendList().contains(it.getValue().getInt("charId"))).collect(Collectors.toList()), this._snapshotList.entrySet().stream().filter(it -> this._player.getFriendList().contains(it.getValue().getInt("charId"))).collect(Collectors.toList()), buffer);
				break;
			case CLASS:
				this.writeScopeData(scope, this._playerList.entrySet().stream().filter(it -> it.getValue().getInt("classId") == baseclass.getId()).collect(Collectors.toList()), this._snapshotList.entrySet().stream().filter(it -> it.getValue().getInt("classId") == baseclass.getId()).collect(Collectors.toList()), buffer);
		}
	}

	private void writeScopeData(RankingScope scope, List<Entry<Integer, StatSet>> list, List<Entry<Integer, StatSet>> snapshot, WritableBuffer buffer)
	{
		Entry<Integer, StatSet> playerData = list.stream().filter(it -> it.getValue().getInt("charId", 0) == this._player.getObjectId()).findFirst().orElse(null);
		int indexOf = list.indexOf(playerData);

		List<Entry<Integer, StatSet>> limited = switch (scope)
		{
			case TOP_100 -> list.stream().limit(100L).collect(Collectors.toList());
			case ALL -> list;
			case TOP_150 -> list.stream().limit(150L).collect(Collectors.toList());
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
			buffer.writeInt(player.getInt("level"));
			buffer.writeInt(player.getInt("race"));
			buffer.writeInt(player.getInt("classId"));
			buffer.writeLong(player.getInt("points"));
			if (!snapshot.isEmpty())
			{
				for (Entry<Integer, StatSet> ssData : snapshot.stream().sorted(Entry.comparingByKey()).collect(Collectors.toList()))
				{
					StatSet snapshotData = ssData.getValue();
					if (player.getInt("charId") == snapshotData.getInt("charId"))
					{
						buffer.writeInt(scope == RankingScope.SELF ? ssData.getKey() : curRank);
						buffer.writeInt(snapshotData.getInt("raceRank", 0));
						buffer.writeInt(player.getInt("kills"));
						buffer.writeInt(player.getInt("deaths"));
					}
				}
			}
			else
			{
				buffer.writeInt(scope == RankingScope.SELF ? data.getKey() : curRank);
				buffer.writeInt(0);
				buffer.writeInt(0);
				buffer.writeInt(0);
			}
		}
	}
}
