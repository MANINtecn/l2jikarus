package net.sf.l2jdev.gameserver.network.serverpackets.ranking;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.RankManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExPledgeRankingMyInfo extends ServerPacket
{
	private final Player _player;

	public ExPledgeRankingMyInfo(Player player)
	{
		this._player = player;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PLEDGE_RANKING_MY_INFO.writeId(this, buffer);
		Clan clan = this._player.getClan();
		buffer.writeInt(clan != null ? (RankManager.getInstance().getClanRankList().entrySet().stream().anyMatch(it -> it.getValue().getInt("clan_id") == this._player.getClanId()) ? RankManager.getInstance().getClanRankList().entrySet().stream().filter(it -> it.getValue().getInt("clan_id") == this._player.getClanId()).findFirst().orElse(null).getKey() : 0) : 0);
		buffer.writeInt(clan != null ? (RankManager.getInstance().getSnapshotClanRankList().entrySet().stream().anyMatch(it -> it.getValue().getInt("clan_id") == this._player.getClanId()) ? RankManager.getInstance().getSnapshotClanRankList().entrySet().stream().filter(it -> it.getValue().getInt("clan_id") == this._player.getClanId()).findFirst().orElse(null).getKey() : 0) : 0);
		buffer.writeInt(clan != null ? clan.getExp() : 0);
	}
}
