package net.sf.l2jdev.gameserver.model.actor.tasks.npc.walker;

import net.sf.l2jdev.gameserver.managers.WalkingManager;
import net.sf.l2jdev.gameserver.model.WalkInfo;
import net.sf.l2jdev.gameserver.model.actor.Npc;

public class ArrivedTask implements Runnable
{
	private final WalkInfo _walk;
	private final Npc _npc;

	public ArrivedTask(Npc npc, WalkInfo walk)
	{
		this._npc = npc;
		this._walk = walk;
	}

	@Override
	public void run()
	{
		this._npc.broadcastInfo();
		this._walk.setBlocked(false);
		WalkingManager.getInstance().startMoving(this._npc, this._walk.getRoute().getName());
	}
}
