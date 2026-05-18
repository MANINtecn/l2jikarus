package net.sf.l2jdev.gameserver.data.holders;

import net.sf.l2jdev.gameserver.model.Location;

public class TimedHuntingZoneHolder
{
	private final int _id;
	private final String _name;
	private final int _initialTime;
	private final int _maximumAddedTime;
	private final int _resetDelay;
	private final int _entryItemId;
	private final int _entryFee;
	private final int _minLevel;
	private final int _maxLevel;
	private final int _remainRefillTime;
	private final int _refillTimeMax;
	private final boolean _pvpZone;
	private final boolean _noPvpZone;
	private final int _instanceId;
	private final boolean _soloInstance;
	private final boolean _weekly;
	private final boolean _useWorldPrefix;
	private final boolean _zonePremiumUserOnly;
	private final Location _enterLocation;
	private final Location _exitLocation;
	private final boolean _isEvenWeek;
	private final boolean _isSwapWeek;

	public TimedHuntingZoneHolder(int id, String name, int initialTime, int maximumAddedTime, int resetDelay, int entryItemId, int entryFee, int minLevel, int maxLevel, int remainRefillTime, int refillTimeMax, boolean pvpZone, boolean noPvpZone, int instanceId, boolean soloInstance, boolean weekly, boolean useWorldPrefix, boolean zonePremiumUserOnly, Location enterLocation, Location exitLocation, boolean isEvenWeek, boolean isSwapWeek)
	{
		this._id = id;
		this._name = name;
		this._initialTime = initialTime;
		this._maximumAddedTime = maximumAddedTime;
		this._resetDelay = resetDelay;
		this._entryItemId = entryItemId;
		this._entryFee = entryFee;
		this._minLevel = minLevel;
		this._maxLevel = maxLevel;
		this._remainRefillTime = remainRefillTime;
		this._refillTimeMax = refillTimeMax;
		this._pvpZone = pvpZone;
		this._noPvpZone = noPvpZone;
		this._instanceId = instanceId;
		this._soloInstance = soloInstance;
		this._weekly = weekly;
		this._useWorldPrefix = useWorldPrefix;
		this._zonePremiumUserOnly = zonePremiumUserOnly;
		this._enterLocation = enterLocation;
		this._exitLocation = exitLocation;
		this._isEvenWeek = isEvenWeek;
		this._isSwapWeek = isSwapWeek;
	}

	public int getZoneId()
	{
		return this._id;
	}

	public String getZoneName()
	{
		return this._name;
	}

	public int getInitialTime()
	{
		return this._initialTime;
	}

	public int getMaximumAddedTime()
	{
		return this._maximumAddedTime;
	}

	public int getResetDelay()
	{
		return this._resetDelay;
	}

	public int getEntryItemId()
	{
		return this._entryItemId;
	}

	public int getEntryFee()
	{
		return this._entryFee;
	}

	public int getMinLevel()
	{
		return this._minLevel;
	}

	public int getMaxLevel()
	{
		return this._maxLevel;
	}

	public int getRemainRefillTime()
	{
		return this._remainRefillTime;
	}

	public int getRefillTimeMax()
	{
		return this._refillTimeMax;
	}

	public boolean isPvpZone()
	{
		return this._pvpZone;
	}

	public boolean isNoPvpZone()
	{
		return this._noPvpZone;
	}

	public int getInstanceId()
	{
		return this._instanceId;
	}

	public boolean isSoloInstance()
	{
		return this._soloInstance;
	}

	public boolean isWeekly()
	{
		return this._weekly;
	}

	public boolean useWorldPrefix()
	{
		return this._useWorldPrefix;
	}

	public boolean zonePremiumUserOnly()
	{
		return this._zonePremiumUserOnly;
	}

	public Location getEnterLocation()
	{
		return this._enterLocation;
	}

	public Location getExitLocation()
	{
		return this._exitLocation;
	}

	public boolean isEvenWeek()
	{
		return this._isEvenWeek;
	}

	public boolean isSwapWeek()
	{
		return this._isSwapWeek;
	}
}
