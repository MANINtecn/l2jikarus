package net.sf.l2jdev.gameserver.model.actor.tasks.attackable;

import net.sf.l2jdev.gameserver.model.actor.Attackable;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.script.Quest;

public class OnKillNotifyTask implements Runnable
{
	private final Attackable _attackable;
	private final Quest _quest;
	private final Player _killer;
	private final boolean _isSummon;

	public OnKillNotifyTask(Attackable attackable, Quest quest, Player killer, boolean isSummon)
	{
		this._attackable = attackable;
		this._quest = quest;
		this._killer = killer;
		this._isSummon = isSummon;
	}

	@Override
	public void run()
	{
		if (this._quest != null && this._attackable != null && this._killer != null)
		{
			this._quest.onKill(this._attackable, this._killer, this._isSummon);
		}
	}
}
