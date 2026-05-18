package net.sf.l2jdev.gameserver.model.events.holders.actor.creature;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;
import net.sf.l2jdev.gameserver.model.instancezone.Instance;

public class OnCreatureTeleport implements IBaseEvent
{
	private final Creature _creature;
	private final int _destX;
	private final int _destY;
	private final int _destZ;
	private final int _destHeading;
	private final Instance _destInstance;

	public OnCreatureTeleport(Creature creature, int destX, int destY, int destZ, int destHeading, Instance destInstance)
	{
		this._creature = creature;
		this._destX = destX;
		this._destY = destY;
		this._destZ = destZ;
		this._destHeading = destHeading;
		this._destInstance = destInstance;
	}

	public Creature getCreature()
	{
		return this._creature;
	}

	public int getDestX()
	{
		return this._destX;
	}

	public int getDestY()
	{
		return this._destY;
	}

	public int getDestZ()
	{
		return this._destZ;
	}

	public int getDestHeading()
	{
		return this._destHeading;
	}

	public Instance getDestInstance()
	{
		return this._destInstance;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_CREATURE_TELEPORT;
	}
}
