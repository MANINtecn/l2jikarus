package net.sf.l2jdev.gameserver.model.zone.type;

import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.model.zone.ZoneType;

public class TaxZone extends ZoneType
{
	private int _domainId;
	private Castle _castle;

	public TaxZone(int id)
	{
		super(id);
	}

	@Override
	public void setParameter(String name, String value)
	{
		if (name.equalsIgnoreCase("domainId"))
		{
			this._domainId = Integer.parseInt(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}

	@Override
	protected void onEnter(Creature creature)
	{
		creature.setInsideZone(ZoneId.TAX, true);
		if (creature.isNpc())
		{
			creature.asNpc().setTaxZone(this);
		}
	}

	@Override
	protected void onExit(Creature creature)
	{
		creature.setInsideZone(ZoneId.TAX, false);
		if (creature.isNpc())
		{
			creature.asNpc().setTaxZone(null);
		}
	}

	public Castle getCastle()
	{
		if (this._castle == null)
		{
			this._castle = CastleManager.getInstance().getCastleById(this._domainId);
		}

		return this._castle;
	}
}
