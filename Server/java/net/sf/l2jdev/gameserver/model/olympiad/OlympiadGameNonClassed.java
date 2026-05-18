package net.sf.l2jdev.gameserver.model.olympiad;

import java.util.Set;

import net.sf.l2jdev.gameserver.config.OlympiadConfig;

public class OlympiadGameNonClassed extends OlympiadGameNormal
{
	public OlympiadGameNonClassed(int id, Participant[] opponents)
	{
		super(id, opponents);
	}

	@Override
	public CompetitionType getType()
	{
		return CompetitionType.NON_CLASSED;
	}

	@Override
	protected int getDivider()
	{
		return OlympiadConfig.OLYMPIAD_DIVIDER_NON_CLASSED;
	}

	protected static OlympiadGameNonClassed createGame(int id, Set<Integer> list)
	{
		Participant[] opponents = OlympiadGameNormal.createListOfParticipants(list);
		return opponents == null ? null : new OlympiadGameNonClassed(id, opponents);
	}
}
