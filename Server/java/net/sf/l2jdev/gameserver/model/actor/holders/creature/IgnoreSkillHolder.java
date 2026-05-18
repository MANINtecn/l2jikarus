package net.sf.l2jdev.gameserver.model.actor.holders.creature;

import java.util.concurrent.atomic.AtomicInteger;

import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;

public class IgnoreSkillHolder extends SkillHolder
{
	private final AtomicInteger _instances = new AtomicInteger(1);

	public IgnoreSkillHolder(int skillId, int skillLevel)
	{
		super(skillId, skillLevel);
	}

	public IgnoreSkillHolder(SkillHolder holder)
	{
		super(holder.getSkill());
	}

	public int getInstances()
	{
		return this._instances.get();
	}

	public int increaseInstances()
	{
		return this._instances.incrementAndGet();
	}

	public int decreaseInstances()
	{
		return this._instances.decrementAndGet();
	}
}
