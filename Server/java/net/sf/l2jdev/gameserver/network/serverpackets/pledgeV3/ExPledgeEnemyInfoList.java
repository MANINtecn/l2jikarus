package net.sf.l2jdev.gameserver.network.serverpackets.pledgeV3;

import java.util.List;
import java.util.stream.Collectors;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.ClanWar;
import net.sf.l2jdev.gameserver.model.clan.enums.ClanWarState;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExPledgeEnemyInfoList extends ServerPacket
{
	private final Clan _playerClan;
	private final List<ClanWar> _warList;

	public ExPledgeEnemyInfoList(Clan playerClan)
	{
		this._playerClan = playerClan;
		this._warList = playerClan.getWarList().values().stream().filter(it -> it.getClanWarState(playerClan) == ClanWarState.MUTUAL || it.getAttackerClanId() == playerClan.getId()).collect(Collectors.toList());
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PLEDGE_ENEMY_INFO_LIST.writeId(this, buffer);
		buffer.writeInt(this._warList.size());

		for (ClanWar war : this._warList)
		{
			Clan clan = war.getOpposingClan(this._playerClan);
			buffer.writeInt(clan.getRank());
			buffer.writeInt(clan.getId());
			buffer.writeSizedString(clan.getName());
			buffer.writeSizedString(clan.getLeaderName());
			buffer.writeInt((int) (war.getStartTime() / 1000L));
		}
	}
}
