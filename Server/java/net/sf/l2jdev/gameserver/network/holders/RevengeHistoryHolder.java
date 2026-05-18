package net.sf.l2jdev.gameserver.network.holders;

import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.enums.RevengeType;

public class RevengeHistoryHolder
{
	private final String _killerName;
	private final String _killerClanName;
	private final int _killerLevel;
	private final int _killerRaceId;
	private final int _killerClassId;
	private final long _killTime;
	private final String _victimName;
	private final String _victimClanName;
	private final int _victimLevel;
	private final int _victimRaceId;
	private final int _victimClassId;
	private RevengeType _type;
	private boolean _wasShared;
	private long _shareTime;
	private int _showLocationRemaining;
	private int _teleportRemaining;
	private int _sharedTeleportRemaining;

	public RevengeHistoryHolder(Player killer, Player victim, RevengeType type)
	{
		this._type = type;
		this._wasShared = false;
		this._killerName = killer.getName();
		this._killerClanName = killer.getClan() == null ? "" : killer.getClan().getName();
		this._killerLevel = killer.getLevel();
		this._killerRaceId = killer.getRace().ordinal();
		this._killerClassId = killer.getPlayerClass().getId();
		this._killTime = System.currentTimeMillis();
		this._shareTime = 0L;
		this._showLocationRemaining = 5;
		this._teleportRemaining = 5;
		this._sharedTeleportRemaining = 1;
		this._victimName = victim.getName();
		this._victimClanName = victim.getClan() == null ? "" : victim.getClan().getName();
		this._victimLevel = victim.getLevel();
		this._victimRaceId = victim.getRace().ordinal();
		this._victimClassId = victim.getPlayerClass().getId();
	}

	public RevengeHistoryHolder(Player killer, Player victim, RevengeType type, int sharedTeleportRemaining, long killTime, long shareTime)
	{
		this._type = type;
		this._wasShared = true;
		this._killerName = killer.getName();
		this._killerClanName = killer.getClan() == null ? "" : killer.getClan().getName();
		this._killerLevel = killer.getLevel();
		this._killerRaceId = killer.getRace().ordinal();
		this._killerClassId = killer.getPlayerClass().getId();
		this._killTime = killTime;
		this._shareTime = shareTime;
		this._showLocationRemaining = 0;
		this._teleportRemaining = 0;
		this._sharedTeleportRemaining = sharedTeleportRemaining;
		this._victimName = victim.getName();
		this._victimClanName = victim.getClan() == null ? "" : victim.getClan().getName();
		this._victimLevel = victim.getLevel();
		this._victimRaceId = victim.getRace().ordinal();
		this._victimClassId = victim.getPlayerClass().getId();
	}

	public RevengeHistoryHolder(StatSet killer, StatSet victim, RevengeType type, boolean wasShared, int showLocationRemaining, int teleportRemaining, int sharedTeleportRemaining, long killTime, long shareTime)
	{
		this._type = type;
		this._wasShared = wasShared;
		this._killerName = killer.getString("name");
		this._killerClanName = killer.getString("clan");
		this._killerLevel = killer.getInt("level");
		this._killerRaceId = killer.getInt("race");
		this._killerClassId = killer.getInt("class");
		this._killTime = killTime;
		this._shareTime = shareTime;
		this._showLocationRemaining = showLocationRemaining;
		this._teleportRemaining = teleportRemaining;
		this._sharedTeleportRemaining = sharedTeleportRemaining;
		this._victimName = victim.getString("name");
		this._victimClanName = victim.getString("clan");
		this._victimLevel = victim.getInt("level");
		this._victimRaceId = victim.getInt("race");
		this._victimClassId = victim.getInt("class");
	}

	public RevengeType getType()
	{
		return this._type;
	}

	public void setType(RevengeType type)
	{
		this._type = type;
	}

	public boolean wasShared()
	{
		return this._wasShared;
	}

	public void setShared(boolean wasShared)
	{
		this._wasShared = wasShared;
	}

	public String getKillerName()
	{
		return this._killerName;
	}

	public String getKillerClanName()
	{
		return this._killerClanName;
	}

	public int getKillerLevel()
	{
		return this._killerLevel;
	}

	public int getKillerRaceId()
	{
		return this._killerRaceId;
	}

	public int getKillerClassId()
	{
		return this._killerClassId;
	}

	public long getKillTime()
	{
		return this._killTime;
	}

	public long getShareTime()
	{
		return this._shareTime;
	}

	public void setShareTime(long shareTime)
	{
		this._shareTime = shareTime;
	}

	public int getShowLocationRemaining()
	{
		return this._showLocationRemaining;
	}

	public void setShowLocationRemaining(int count)
	{
		this._showLocationRemaining = count;
	}

	public int getTeleportRemaining()
	{
		return this._teleportRemaining;
	}

	public void setTeleportRemaining(int count)
	{
		this._teleportRemaining = count;
	}

	public int getSharedTeleportRemaining()
	{
		return this._sharedTeleportRemaining;
	}

	public void setSharedTeleportRemaining(int count)
	{
		this._sharedTeleportRemaining = count;
	}

	public String getVictimName()
	{
		return this._victimName;
	}

	public String getVictimClanName()
	{
		return this._victimClanName;
	}

	public int getVictimLevel()
	{
		return this._victimLevel;
	}

	public int getVictimRaceId()
	{
		return this._victimRaceId;
	}

	public int getVictimClassId()
	{
		return this._victimClassId;
	}
}
