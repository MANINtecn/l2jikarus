package net.sf.l2jdev.gameserver.model.events.holders.actor.player;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;
import net.sf.l2jdev.gameserver.model.item.henna.Henna;

public class OnPlayerHennaAdd implements IBaseEvent
{
	private final Player _player;
	private final Henna _henna;

	public OnPlayerHennaAdd(Player player, Henna henna)
	{
		this._player = player;
		this._henna = henna;
	}

	public Player getPlayer()
	{
		return this._player;
	}

	public Henna getHenna()
	{
		return this._henna;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_PLAYER_HENNA_ADD;
	}
}
