package net.sf.l2jdev.gameserver.model.actor.tasks.npc.trap;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.model.actor.instance.Trap;

public class TrapTriggerTask implements Runnable
{
	private final Trap _trap;

	public TrapTriggerTask(Trap trap)
	{
		this._trap = trap;
	}

	@Override
	public void run()
	{
		try
		{
			this._trap.doCast(this._trap.getSkill());
			ThreadPool.schedule(new TrapUnsummonTask(this._trap), this._trap.getSkill().getHitTime() + 300);
		}
		catch (Exception var2)
		{
			this._trap.unSummon();
		}
	}
}
