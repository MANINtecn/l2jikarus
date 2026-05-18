package net.sf.l2jdev.gameserver.model.events.holders.clan;

import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;

public class OnClanWarStart implements IBaseEvent
{
	private final Clan _clan1;
	private final Clan _clan2;

	public OnClanWarStart(Clan clan1, Clan clan2)
	{
		this._clan1 = clan1;
		this._clan2 = clan2;
	}

	public Clan getClan1()
	{
		return this._clan1;
	}

	public Clan getClan2()
	{
		return this._clan2;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_CLAN_WAR_START;
	}
}
