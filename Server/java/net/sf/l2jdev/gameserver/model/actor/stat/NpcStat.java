package net.sf.l2jdev.gameserver.model.actor.stat;

import net.sf.l2jdev.gameserver.model.actor.Npc;

public class NpcStat extends CreatureStat
{
	public NpcStat(Npc activeChar)
	{
		super(activeChar);
	}

	@Override
	public int getLevel()
	{
		return this.getActiveChar().getTemplate().getLevel();
	}

	@Override
	public Npc getActiveChar()
	{
		return super.getActiveChar().asNpc();
	}

	@Override
	public int getPhysicalAttackAngle()
	{
		return this.getActiveChar().getTemplate().getBaseAttackAngle();
	}
}
