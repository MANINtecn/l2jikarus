package net.sf.l2jdev.gameserver.model.zone.type;

import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.zone.ZoneType;

public class TeleportZone extends ZoneType
{
	private int _x = -1;
	private int _y = -1;
	private int _z = -1;

	public TeleportZone(int id)
	{
		super(id);
		this.setTargetType(InstanceType.Player);
	}

	@Override
	public void setParameter(String name, String value)
	{
		switch (name)
		{
			case "oustX":
				this._x = Integer.parseInt(value);
				break;
			case "oustY":
				this._y = Integer.parseInt(value);
				break;
			case "oustZ":
				this._z = Integer.parseInt(value);
				break;
			default:
				super.setParameter(name, value);
		}
	}

	@Override
	protected void onEnter(Creature creature)
	{
		creature.teleToLocation(new Location(this._x, this._y, this._z));
	}

	@Override
	protected void onExit(Creature creature)
	{
	}
}
