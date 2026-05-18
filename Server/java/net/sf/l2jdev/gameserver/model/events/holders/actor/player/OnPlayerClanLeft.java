package net.sf.l2jdev.gameserver.model.events.holders.actor.player;

import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.ClanMember;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;

public class OnPlayerClanLeft implements IBaseEvent
{
	private final ClanMember _clanMember;
	private final Clan _clan;

	public OnPlayerClanLeft(ClanMember clanMember, Clan clan)
	{
		this._clanMember = clanMember;
		this._clan = clan;
	}

	public ClanMember getClanMember()
	{
		return this._clanMember;
	}

	public Clan getClan()
	{
		return this._clan;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_PLAYER_CLAN_LEFT;
	}
}
