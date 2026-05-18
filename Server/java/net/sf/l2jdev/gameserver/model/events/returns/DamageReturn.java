package net.sf.l2jdev.gameserver.model.events.returns;

public class DamageReturn extends TerminateReturn
{
	private final double _damage;

	public DamageReturn(boolean terminate, boolean override, boolean abort, double damage)
	{
		super(terminate, override, abort);
		this._damage = damage;
	}

	public double getDamage()
	{
		return this._damage;
	}
}
