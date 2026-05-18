package net.sf.l2jdev.gameserver.model.actor.enums.player;

public enum RankingOlympiadCategory
{
	SERVER,
	CLASS;

	public RankingOlympiadScope getScopeByGroup(int id)
	{
		switch (this)
		{
			case SERVER:
				return id == 0 ? RankingOlympiadScope.TOP_100 : RankingOlympiadScope.SELF;
			case CLASS:
				return id == 0 ? RankingOlympiadScope.TOP_50 : RankingOlympiadScope.SELF;
			default:
				return null;
		}
	}
}
