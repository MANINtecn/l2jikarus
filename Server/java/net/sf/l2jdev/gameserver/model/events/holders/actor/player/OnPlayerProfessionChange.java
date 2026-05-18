package net.sf.l2jdev.gameserver.model.events.holders.actor.player;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.templates.PlayerTemplate;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;

public class OnPlayerProfessionChange implements IBaseEvent
{
	private final Player _player;
	private final PlayerTemplate _template;
	private final boolean _isSubClass;

	public OnPlayerProfessionChange(Player player, PlayerTemplate template, boolean isSubClass)
	{
		this._player = player;
		this._template = template;
		this._isSubClass = isSubClass;
	}

	public Player getPlayer()
	{
		return this._player;
	}

	public PlayerTemplate getTemplate()
	{
		return this._template;
	}

	public boolean isSubClass()
	{
		return this._isSubClass;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_PLAYER_PROFESSION_CHANGE;
	}
}
