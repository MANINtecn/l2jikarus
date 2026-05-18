package net.sf.l2jdev.gameserver.model.actor.tasks.npc;

import java.util.concurrent.ScheduledFuture;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;

public class MpRewardTask
{
	private int _count;
	private final double _value;
	private final ScheduledFuture<?> _task;
	private final Creature _creature;

	public MpRewardTask(Creature creature, Npc npc)
	{
		NpcTemplate template = npc.getTemplate();
		this._creature = creature;
		this._count = template.getMpRewardTicks();
		this._value = this.calculateBaseValue(npc, creature);
		this._task = ThreadPool.scheduleAtFixedRate(this::run, PlayerConfig.EFFECT_TICK_RATIO, PlayerConfig.EFFECT_TICK_RATIO);
	}

	public double calculateBaseValue(Npc npc, Creature creature)
	{
		NpcTemplate template = npc.getTemplate();
		switch (template.getMpRewardType())
		{
			case PER:
				return creature.getMaxMp() * (template.getMpRewardValue() / 100.0) / template.getMpRewardTicks();
			default:
				return template.getMpRewardValue() / template.getMpRewardTicks();
		}
	}

	private void run()
	{
		if (--this._count > 0 && (!this._creature.isPlayer() || this._creature.asPlayer().isOnline()))
		{
			this._creature.setCurrentMp(this._creature.getCurrentMp() + this._value);
		}
		else
		{
			this._task.cancel(false);
		}
	}
}
