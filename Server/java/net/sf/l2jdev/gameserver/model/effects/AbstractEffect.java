package net.sf.l2jdev.gameserver.model.effects;

import java.util.logging.Logger;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public abstract class AbstractEffect
{
	protected static final Logger LOGGER = Logger.getLogger(AbstractEffect.class.getName());
	private int _ticks;

	public int getTicks()
	{
		return this._ticks;
	}

	protected void setTicks(int ticks)
	{
		this._ticks = ticks;
	}

	public double getTicksMultiplier()
	{
		return this.getTicks() * PlayerConfig.EFFECT_TICK_RATIO / 1000.0F;
	}

	public boolean calcSuccess(Creature effector, Creature effected, Skill skill)
	{
		return true;
	}

	public boolean canStart(Creature effector, Creature effected, Skill skill)
	{
		return true;
	}

	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
	}

	public void continuousInstant(Creature effector, Creature effected, Skill skill, Item item)
	{
	}

	public void onStart(Creature effector, Creature effected, Skill skill, Item item)
	{
	}

	public void onExit(Creature effector, Creature effected, Skill skill)
	{
	}

	public boolean onActionTime(Creature effector, Creature effected, Skill skill, Item item)
	{
		return false;
	}

	public long getEffectFlags()
	{
		return EffectFlag.NONE.getMask();
	}

	public boolean checkCondition(Object obj)
	{
		return true;
	}

	public boolean isInstant()
	{
		return false;
	}

	public boolean canPump(Creature effector, Creature effected, Skill skill)
	{
		return true;
	}

	public void pump(Creature effected, Skill skill)
	{
	}

	public boolean delayPump()
	{
		return false;
	}

	public EffectType getEffectType()
	{
		return EffectType.NONE;
	}

	@Override
	public String toString()
	{
		return "Effect " + this.getClass().getSimpleName();
	}
}
