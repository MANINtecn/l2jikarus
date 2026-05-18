package net.sf.l2jdev.gameserver.model;

import net.sf.l2jdev.gameserver.model.actor.Player;

public class DamageDoneInfo
{
	private final Player _attacker;
	private long _damage = 0L;

	public DamageDoneInfo(Player attacker)
	{
		this._attacker = attacker;
	}

	public Player getAttacker()
	{
		return this._attacker;
	}

	public void addDamage(long damage)
	{
		this._damage += damage;
	}

	public long getDamage()
	{
		return this._damage;
	}

	@Override
	public boolean equals(Object obj)
	{
		return this == obj || obj instanceof DamageDoneInfo && ((DamageDoneInfo) obj).getAttacker() == this._attacker;
	}

	@Override
	public int hashCode()
	{
		return this._attacker.getObjectId();
	}
}
