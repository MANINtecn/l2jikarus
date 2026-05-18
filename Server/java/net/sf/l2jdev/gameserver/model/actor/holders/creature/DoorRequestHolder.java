package net.sf.l2jdev.gameserver.model.actor.holders.creature;

import net.sf.l2jdev.gameserver.model.actor.instance.Door;

public class DoorRequestHolder
{
	private final Door _target;

	public DoorRequestHolder(Door door)
	{
		this._target = door;
	}

	public Door getDoor()
	{
		return this._target;
	}
}
