package net.sf.l2jdev.gameserver.model.actor.enums.player;

public enum RankingCategory
{
	SERVER,
	RACE,
	CLASS,
	CLAN,
	FRIEND;

	public RankingScope getScopeByGroup(int id)
	{
		switch (this)
		{
			case SERVER:
				return id == 0 ? RankingScope.TOP_150 : RankingScope.SELF;
			case RACE:
			case CLASS:
				return id == 0 ? RankingScope.TOP_100 : RankingScope.SELF;
			case CLAN:
			case FRIEND:
				return RankingScope.ALL;
			default:
				return null;
		}
	}
}
