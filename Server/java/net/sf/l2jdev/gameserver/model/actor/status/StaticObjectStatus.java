package net.sf.l2jdev.gameserver.model.actor.status;

import net.sf.l2jdev.gameserver.model.actor.instance.StaticObject;

public class StaticObjectStatus extends CreatureStatus
{
	public StaticObjectStatus(StaticObject activeChar)
	{
		super(activeChar);
	}

	@Override
	public StaticObject getActiveChar()
	{
		return (StaticObject) super.getActiveChar();
	}
}
