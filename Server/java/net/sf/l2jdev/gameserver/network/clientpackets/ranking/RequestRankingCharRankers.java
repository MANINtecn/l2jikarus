package net.sf.l2jdev.gameserver.network.clientpackets.ranking;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.ranking.ExRankingCharRankers;

public class RequestRankingCharRankers extends ClientPacket
{
	private int _group;
	private int _scope;
	private int _ordinal;
	private int _baseclass;

	@Override
	protected void readImpl()
	{
		this._group = this.readByte();
		this._scope = this.readByte();
		this._ordinal = this.readInt();
		this._baseclass = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.sendPacket(new ExRankingCharRankers(player, this._group, this._scope, this._ordinal, this._baseclass));
		}
	}
}
