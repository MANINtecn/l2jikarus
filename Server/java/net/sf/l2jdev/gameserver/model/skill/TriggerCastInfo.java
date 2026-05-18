package net.sf.l2jdev.gameserver.model.skill;

import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.instance.Item;

public class TriggerCastInfo
{
	private final Creature _creature;
	private final WorldObject _target;
	private final Skill _skill;
	private final Item _item;
	private final boolean _ignoreTargetType;

	public TriggerCastInfo(Creature creature, WorldObject target, Skill skill, Item item, boolean ignoreTargetType)
	{
		this._creature = creature;
		this._target = target;
		this._skill = skill;
		this._item = item;
		this._ignoreTargetType = ignoreTargetType;
	}

	public Creature getCreature()
	{
		return this._creature;
	}

	public WorldObject getTarget()
	{
		return this._target;
	}

	public Skill getSkill()
	{
		return this._skill;
	}

	public Item getItem()
	{
		return this._item;
	}

	public boolean isIgnoreTargetType()
	{
		return this._ignoreTargetType;
	}
}
