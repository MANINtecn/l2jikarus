package net.sf.l2jdev.gameserver.model.actor.status;

import net.sf.l2jdev.gameserver.model.actor.instance.Door;

public class DoorStatus extends CreatureStatus
{
	public DoorStatus(Door activeChar)
	{
		super(activeChar);
	}

	@Override
	public Door getActiveChar()
	{
		return super.getActiveChar().asDoor();
	}
}
