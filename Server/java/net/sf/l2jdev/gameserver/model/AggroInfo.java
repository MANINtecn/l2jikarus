package net.sf.l2jdev.gameserver.model;

import net.sf.l2jdev.gameserver.model.actor.Creature;

public class AggroInfo
{
	public static final long MAX_VALUE = 1000000000000000L;
	private final Creature _attacker;
	private long _hate = 0L;
	private long _damage = 0L;

	public AggroInfo(Creature pAttacker)
	{
		this._attacker = pAttacker;
	}

	public Creature getAttacker()
	{
		return this._attacker;
	}

	public long getHate()
	{
		return this._hate;
	}

	public long checkHate(Creature owner)
	{
		if (this._attacker.isAlikeDead() || !this._attacker.isSpawned() || !owner.isInSurroundingRegion(this._attacker))
		{
			this._hate = 0L;
		}

		return this._hate;
	}

	public void addHate(long value)
	{
		this._hate = Math.min(this._hate + value, 1000000000000000L);
	}

	public void stopHate()
	{
		this._hate = 0L;
	}

	public long getDamage()
	{
		return this._damage;
	}

	public void addDamage(long value)
	{
		this._damage = Math.min(this._damage + value, 1000000000000000L);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		return obj instanceof AggroInfo ? ((AggroInfo) obj).getAttacker() == this._attacker : false;
	}

	@Override
	public int hashCode()
	{
		return this._attacker.getObjectId();
	}

	@Override
	public String toString()
	{
		return "AggroInfo [attacker=" + this._attacker + ", hate=" + this._hate + ", damage=" + this._damage + "]";
	}
}
