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
import net.sf.l2jdev.gameserver.model.actor.enums.player.RankingCategory;
import net.sf.l2jdev.gameserver.model.actor.enums.player.RankingScope;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExPetRankingList extends ServerPacket
{
	private final Player _player;
	private final int _season;
	private final int _tabId;
	private final int _type;
	private final int _petItemObjectId;
	private final Map<Integer, StatSet> _playerList;
	private final Map<Integer, StatSet> _snapshotList;

	public ExPetRankingList(Player player, int season, int tabId, int type, int petItemObjectId)
	{
		this._player = player;
		this._season = season;
		this._tabId = tabId;
		this._type = type;
		this._petItemObjectId = petItemObjectId;
		this._playerList = RankManager.getInstance().getPetRankList();
		this._snapshotList = RankManager.getInstance().getSnapshotPetRankList();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PET_RANKING_LIST.writeId(this, buffer);
		buffer.writeByte(this._season);
		buffer.writeByte(this._tabId);
		buffer.writeShort(this._type);
		buffer.writeInt(this._petItemObjectId);
		if (!this._playerList.isEmpty())
		{
			RankingCategory category = RankingCategory.values()[this._tabId];
			this.writeFilteredRankingData(buffer, category, category.getScopeByGroup(this._season));
		}
		else
		{
			buffer.writeInt(0);
		}
	}

	private void writeFilteredRankingData(WritableBuffer buffer, RankingCategory category, RankingScope scope)
	{
		switch (category)
		{
			case SERVER:
				this.writeScopeData(buffer, scope, new ArrayList<>(this._playerList.entrySet()), new ArrayList<>(this._snapshotList.entrySet()));
				break;
			case RACE:
				this.writeScopeData(buffer, scope, this._playerList.entrySet().stream().filter(it -> it.getValue().getInt("petType") == this._type).collect(Collectors.toList()), this._snapshotList.entrySet().stream().filter(it -> it.getValue().getInt("petType") == this._type).collect(Collectors.toList()));
				break;
			case CLAN:
				this.writeScopeData(buffer, scope, this._player.getClan() == null ? Collections.emptyList() : this._playerList.entrySet().stream().filter(it -> it.getValue().getString("clanName").equals(this._player.getClan().getName())).collect(Collectors.toList()), this._player.getClan() == null ? Collections.emptyList() : this._snapshotList.entrySet().stream().filter(it -> it.getValue().getString("clanName").equals(this._player.getClan().getName())).collect(Collectors.toList()));
				break;
			case FRIEND:
				this.writeScopeData(buffer, scope, this._playerList.entrySet().stream().filter(it -> this._player.getFriendList().contains(it.getValue().getInt("charId"))).collect(Collectors.toList()), this._snapshotList.entrySet().stream().filter(it -> this._player.getFriendList().contains(it.getValue().getInt("charId"))).collect(Collectors.toList()));
		}
	}

	private void writeScopeData(WritableBuffer buffer, RankingScope scope, List<Entry<Integer, StatSet>> list, List<Entry<Integer, StatSet>> snapshot)
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
			StatSet pet = data.getValue();
			buffer.writeSizedString(pet.getString("name", ""));
			buffer.writeSizedString(pet.getString("owner_name", ""));
			buffer.writeSizedString(pet.getString("clanName", ""));
			buffer.writeInt(1000000 + pet.getInt("npcId", 16104));
			buffer.writeShort(pet.getInt("petType", 0));
			buffer.writeShort(pet.getInt("level", 1));
			buffer.writeShort(pet.getInt("owner_race", 0));
			buffer.writeShort(pet.getInt("owner_level", 1));
			buffer.writeInt(scope == RankingScope.SELF ? data.getKey() : curRank);
			if (!snapshot.isEmpty())
			{
				for (Entry<Integer, StatSet> ssData : snapshot.stream().sorted(Entry.comparingByKey()).collect(Collectors.toList()))
				{
					StatSet snapshotData = ssData.getValue();
					if (pet.getInt("controlledItemObjId", 0) == snapshotData.getInt("controlledItemObjId", 0))
					{
						buffer.writeInt(scope == RankingScope.SELF ? ssData.getKey() : curRank);
					}
				}
			}
			else
			{
				buffer.writeInt(scope == RankingScope.SELF ? data.getKey() : curRank);
			}
		}
	}
}
