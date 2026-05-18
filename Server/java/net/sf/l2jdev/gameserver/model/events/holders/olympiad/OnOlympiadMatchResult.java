package net.sf.l2jdev.gameserver.model.events.holders.olympiad;

import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;
import net.sf.l2jdev.gameserver.model.olympiad.CompetitionType;
import net.sf.l2jdev.gameserver.model.olympiad.Participant;

public class OnOlympiadMatchResult implements IBaseEvent
{
	private final Participant _winner;
	private final Participant _loser;
	private final CompetitionType _type;

	public OnOlympiadMatchResult(Participant winner, Participant looser, CompetitionType type)
	{
		this._winner = winner;
		this._loser = looser;
		this._type = type;
	}

	public Participant getWinner()
	{
		return this._winner;
	}

	public Participant getLoser()
	{
		return this._loser;
	}

	public CompetitionType getCompetitionType()
	{
		return this._type;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_OLYMPIAD_MATCH_RESULT;
	}
}
