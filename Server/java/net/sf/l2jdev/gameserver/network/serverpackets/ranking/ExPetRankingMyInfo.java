package net.sf.l2jdev.gameserver.network.serverpackets.ranking;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.xml.PetDataTable;
import net.sf.l2jdev.gameserver.managers.RankManager;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExPetRankingMyInfo extends ServerPacket
{
	private final int _petId;
	private final Player _player;
	private final Optional<Entry<Integer, StatSet>> _ranking;
	private final Optional<Entry<Integer, StatSet>> _snapshotRanking;
	private final Map<Integer, StatSet> _rankingList;
	private final Map<Integer, StatSet> _snapshotList;

	public ExPetRankingMyInfo(Player player, int petId)
	{
		this._player = player;
		this._petId = petId;
		this._ranking = RankManager.getInstance().getPetRankList().entrySet().stream().filter(it -> it.getValue().getInt("controlledItemObjId") == petId).findFirst();
		this._snapshotRanking = RankManager.getInstance().getSnapshotPetRankList().entrySet().stream().filter(it -> it.getValue().getInt("controlledItemObjId") == petId).findFirst();
		this._rankingList = RankManager.getInstance().getPetRankList();
		this._snapshotList = RankManager.getInstance().getSnapshotPetRankList();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PET_RANKING_MY_INFO.writeId(this, buffer);
		buffer.writeInt(this._petId);
		buffer.writeShort(1);
		buffer.writeInt(-1);
		buffer.writeInt(0);
		buffer.writeInt(this._ranking.isPresent() ? this._ranking.get().getKey() : 0);
		buffer.writeInt(this._snapshotRanking.isPresent() ? this._snapshotRanking.get().getKey() : 0);
		if (this._petId > 0)
		{
			int typeRank = 1;
			boolean found = false;

			for (StatSet ss : this._rankingList.values())
			{
				if (ss.getInt("petType", -1) == PetDataTable.getInstance().getTypeByIndex(this._player.getPetEvolve(this._petId).getIndex()))
				{
					if (ss.getInt("controlledItemObjId", -1) == this._petId)
					{
						found = true;
						buffer.writeInt(typeRank);
						break;
					}

					typeRank++;
				}
			}

			if (!found)
			{
				buffer.writeInt(0);
			}

			int snapshotTypeRank = 1;
			boolean snapshotFound = false;

			for (StatSet ssx : this._snapshotList.values())
			{
				if (ssx.getInt("petType", -1) == PetDataTable.getInstance().getTypeByIndex(this._player.getPetEvolve(this._petId).getIndex()))
				{
					if (ssx.getInt("controlledItemObjId", -1) == this._petId)
					{
						snapshotFound = true;
						buffer.writeInt(snapshotTypeRank);
						break;
					}

					snapshotTypeRank++;
				}
			}

			if (!snapshotFound)
			{
				buffer.writeInt(0);
			}
		}
		else
		{
			buffer.writeInt(0);
			buffer.writeInt(0);
		}
	}
}
