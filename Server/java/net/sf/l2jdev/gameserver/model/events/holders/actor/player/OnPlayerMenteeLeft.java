package net.sf.l2jdev.gameserver.model.events.holders.actor.player;

import net.sf.l2jdev.gameserver.model.Mentee;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;

public class OnPlayerMenteeLeft implements IBaseEvent
{
	private final Mentee _mentor;
	private final Player _mentee;

	public OnPlayerMenteeLeft(Mentee mentor, Player mentee)
	{
		this._mentor = mentor;
		this._mentee = mentee;
	}

	public Mentee getMentor()
	{
		return this._mentor;
	}

	public Player getMentee()
	{
		return this._mentee;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_PLAYER_MENTEE_LEFT;
	}
}
