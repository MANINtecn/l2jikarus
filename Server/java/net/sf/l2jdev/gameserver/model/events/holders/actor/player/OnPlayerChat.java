package net.sf.l2jdev.gameserver.model.events.holders.actor.player;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;
import net.sf.l2jdev.gameserver.network.enums.ChatType;

public class OnPlayerChat implements IBaseEvent
{
	private final Player _player;
	private final String _target;
	private final String _text;
	private final ChatType _type;

	public OnPlayerChat(Player player, String target, String text, ChatType type)
	{
		this._player = player;
		this._target = target;
		this._text = text;
		this._type = type;
	}

	public Player getPlayer()
	{
		return this._player;
	}

	public String getTarget()
	{
		return this._target;
	}

	public String getText()
	{
		return this._text;
	}

	public ChatType getChatType()
	{
		return this._type;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_PLAYER_CHAT;
	}
}
