package net.sf.l2jdev.gameserver.model.events.holders.actor.player;

import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.ClanMember;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;

public class OnPlayerClanLeaderChange implements IBaseEvent
{
	private final ClanMember _oldLeader;
	private final ClanMember _newLeader;
	private final Clan _clan;

	public OnPlayerClanLeaderChange(ClanMember oldLeader, ClanMember newLeader, Clan clan)
	{
		this._oldLeader = oldLeader;
		this._newLeader = newLeader;
		this._clan = clan;
	}

	public ClanMember getOldLeader()
	{
		return this._oldLeader;
	}

	public ClanMember getNewLeader()
	{
		return this._newLeader;
	}

	public Clan getClan()
	{
		return this._clan;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_PLAYER_CLAN_LEADER_CHANGE;
	}
}
