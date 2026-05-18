package net.sf.l2jdev.gameserver.model.events.holders.actor.player;

import net.sf.l2jdev.gameserver.model.Mentee;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;

public class OnPlayerMenteeRemove implements IBaseEvent
{
	private final Player _mentor;
	private final Mentee _mentee;

	public OnPlayerMenteeRemove(Player mentor, Mentee mentee)
	{
		this._mentor = mentor;
		this._mentee = mentee;
	}

	public Player getMentor()
	{
		return this._mentor;
	}

	public Mentee getMentee()
	{
		return this._mentee;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_PLAYER_MENTEE_REMOVE;
	}
}
