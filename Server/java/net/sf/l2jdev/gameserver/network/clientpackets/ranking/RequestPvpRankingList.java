package net.sf.l2jdev.gameserver.network.clientpackets.ranking;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.ranking.ExPvpRankingList;

public class RequestPvpRankingList extends ClientPacket
{
	private int _season;
	private int _tabId;
	private int _type;
	private int _race;

	@Override
	protected void readImpl()
	{
		this._season = this.readByte();
		this._tabId = this.readByte();
		this._type = this.readByte();
		this._race = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.sendPacket(new ExPvpRankingList(player, this._season, this._tabId, this._type, this._race, player.getBaseClass()));
		}
	}
}
