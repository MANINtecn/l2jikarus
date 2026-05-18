package net.sf.l2jdev.gameserver.model.actor.tasks.npc.trap;

import net.sf.l2jdev.gameserver.model.actor.instance.Trap;

public class TrapUnsummonTask implements Runnable
{
	private final Trap _trap;

	public TrapUnsummonTask(Trap trap)
	{
		this._trap = trap;
	}

	@Override
	public void run()
	{
		this._trap.unSummon();
	}
}
