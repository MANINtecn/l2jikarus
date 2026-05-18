package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Collection;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.ClanWar;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class PledgeReceiveWarList extends ServerPacket
{
	private final Clan _clan;
	private final int _tab;
	private final Collection<ClanWar> _clanList;

	public PledgeReceiveWarList(Clan clan, int tab)
	{
		this._clan = clan;
		this._tab = tab;
		this._clanList = clan.getWarList().values();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_VIEW_PLEDGE_WARLIST.writeId(this, buffer);
		buffer.writeInt(this._tab);
		buffer.writeInt(this._clanList.size());

		for (ClanWar clanWar : this._clanList)
		{
			Clan clan = clanWar.getOpposingClan(this._clan);
			if (clan != null)
			{
				buffer.writeString(clan.getName());
				buffer.writeInt(clanWar.getState().ordinal());
				buffer.writeInt(clanWar.getRemainingTime());
				buffer.writeInt(clanWar.getKillDifference(this._clan));
				buffer.writeInt(clanWar.calculateWarProgress(this._clan).ordinal());
				buffer.writeInt(clanWar.getKillToStart());
			}
		}
	}
}
