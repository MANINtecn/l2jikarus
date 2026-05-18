package net.sf.l2jdev.gameserver.cache;

public class RelationCache
{
	private final long _relation;
	private final boolean _isAutoAttackable;

	public RelationCache(long relation, boolean isAutoAttackable)
	{
		this._relation = relation;
		this._isAutoAttackable = isAutoAttackable;
	}

	public long getRelation()
	{
		return this._relation;
	}

	public boolean isAutoAttackable()
	{
		return this._isAutoAttackable;
	}
}
