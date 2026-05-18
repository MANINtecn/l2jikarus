package net.sf.l2jdev.gameserver.model.actor.holders.creature;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.effects.AbstractEffect;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class DelayedPumpHolder
{
	private final AbstractEffect _effect;
	private final Creature _effected;
	private final Skill _skill;

	public DelayedPumpHolder(AbstractEffect effect, Creature effected, Skill skill)
	{
		this._effect = effect;
		this._effected = effected;
		this._skill = skill;
	}

	public AbstractEffect getEffect()
	{
		return this._effect;
	}

	public Creature getEffected()
	{
		return this._effected;
	}

	public Skill getSkill()
	{
		return this._skill;
	}
}
