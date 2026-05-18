package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.managers.ClanEntryManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPledgeRecruitBoardSearch;

public class RequestPledgeRecruitBoardSearch extends ClientPacket
{
	private int _clanLevel;
	private int _karma;
	private int _type;
	private String _query;
	private int _sort;
	private boolean _descending;
	private int _page;
	protected int _applicationType;

	@Override
	protected void readImpl()
	{
		this._clanLevel = this.readInt();
		this._karma = this.readInt();
		this._type = this.readInt();
		this._query = this.readString();
		this._sort = this.readInt();
		this._descending = this.readInt() == 2;
		this._page = this.readInt();
		this._applicationType = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (this._query.isEmpty())
			{
				if (this._karma < 0 && this._clanLevel < 0)
				{
					player.sendPacket(new ExPledgeRecruitBoardSearch(ClanEntryManager.getInstance().getUnSortedClanList(), this._page));
				}
				else
				{
					player.sendPacket(new ExPledgeRecruitBoardSearch(ClanEntryManager.getInstance().getSortedClanList(this._clanLevel, this._karma, this._sort, this._descending), this._page));
				}
			}
			else
			{
				player.sendPacket(new ExPledgeRecruitBoardSearch(ClanEntryManager.getInstance().getSortedClanListByName(this._query.toLowerCase(), this._type), this._page));
			}
		}
	}
}
