package net.sf.l2jdev.gameserver.model.olympiad;

import java.util.List;
import java.util.Set;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.config.OlympiadConfig;

public class OlympiadGameClassed extends OlympiadGameNormal
{
	private OlympiadGameClassed(int id, Participant[] opponents)
	{
		super(id, opponents);
	}

	@Override
	public CompetitionType getType()
	{
		return CompetitionType.CLASSED;
	}

	@Override
	protected int getDivider()
	{
		return OlympiadConfig.OLYMPIAD_DIVIDER_CLASSED;
	}

	protected static OlympiadGameClassed createGame(int id, List<Set<Integer>> classList)
	{
		if (classList != null && !classList.isEmpty())
		{
			while (!classList.isEmpty())
			{
				Set<Integer> list = classList.get(Rnd.get(classList.size()));
				if (list != null && list.size() >= 2)
				{
					Participant[] opponents = OlympiadGameNormal.createListOfParticipants(list);
					if (opponents != null)
					{
						return new OlympiadGameClassed(id, opponents);
					}

					classList.remove(list);
				}
				else
				{
					classList.remove(list);
				}
			}

			return null;
		}
		return null;
	}
}
