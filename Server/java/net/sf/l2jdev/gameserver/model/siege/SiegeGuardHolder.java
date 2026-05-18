package net.sf.l2jdev.gameserver.model.siege;

public class SiegeGuardHolder
{
	private final int _castleId;
	private final int _itemId;
	private final SiegeGuardType _type;
	private final boolean _stationary;
	private final int _npcId;
	private final int _maxNpcAmount;

	public SiegeGuardHolder(int castleId, int itemId, SiegeGuardType type, boolean stationary, int npcId, int maxNpcAmount)
	{
		this._castleId = castleId;
		this._itemId = itemId;
		this._type = type;
		this._stationary = stationary;
		this._npcId = npcId;
		this._maxNpcAmount = maxNpcAmount;
	}

	public int getCastleId()
	{
		return this._castleId;
	}

	public int getItemId()
	{
		return this._itemId;
	}

	public SiegeGuardType getType()
	{
		return this._type;
	}

	public boolean isStationary()
	{
		return this._stationary;
	}

	public int getNpcId()
	{
		return this._npcId;
	}

	public int getMaxNpcAmout()
	{
		return this._maxNpcAmount;
	}
}
