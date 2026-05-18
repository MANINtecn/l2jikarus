package net.sf.l2jdev.gameserver.model.zone;

public enum ZoneId
{
	PVP,
	PEACE,
	SIEGE,
	MOTHER_TREE,
	CLAN_HALL,
	LANDING,
	NO_LANDING,
	WATER,
	JAIL,
	MONSTER_TRACK,
	CASTLE,
	SWAMP,
	NO_SUMMON_FRIEND,
	FORT,
	NO_STORE,
	NO_PVP,
	SCRIPT,
	HQ,
	DANGER_AREA,
	ALTERED,
	NO_BOOKMARK,
	NO_ITEM_DROP,
	NO_RESTART,
	SAYUNE,
	FISHING,
	UNDYING,
	TAX,
	TIMED_HUNTING,
	PRISON;

	public static int getZoneCount()
	{
		return values().length;
	}
}
