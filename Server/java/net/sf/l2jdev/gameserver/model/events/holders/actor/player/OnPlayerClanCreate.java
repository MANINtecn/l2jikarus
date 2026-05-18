package net.sf.l2jdev.gameserver.model.events.holders.actor.player;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;

public class OnPlayerClanCreate implements IBaseEvent
{
	private final Player _player;
	private final Clan _clan;

	public OnPlayerClanCreate(Player player, Clan clan)
	{
		this._player = player;
		this._clan = clan;
	}

	public Player getPlayer()
	{
		return this._player;
	}

	public Clan getClan()
	{
		return this._clan;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_PLAYER_CLAN_CREATE;
	}
}
