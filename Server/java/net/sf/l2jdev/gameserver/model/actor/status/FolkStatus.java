package net.sf.l2jdev.gameserver.model.actor.status;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.instance.Folk;

public class FolkStatus extends NpcStatus
{
	public FolkStatus(Npc activeChar)
	{
		super(activeChar);
	}

	@Override
	public void reduceHp(double value, Creature attacker)
	{
		this.reduceHp(value, attacker, true, false, false);
	}

	@Override
	public void reduceHp(double value, Creature attacker, boolean awake, boolean isDOT, boolean isHpConsumption)
	{
	}

	@Override
	public Folk getActiveChar()
	{
		return (Folk) super.getActiveChar();
	}
}
