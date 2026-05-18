package net.sf.l2jdev.gameserver.data.holders;

import net.sf.l2jdev.gameserver.model.actor.Creature;

public class WarpedSpaceHolder
{
	private final Creature _creature;
	private final int _range;

	public WarpedSpaceHolder(Creature creature, int range)
	{
		this._creature = creature;
		this._range = range;
	}

	public Creature getCreature()
	{
		return this._creature;
	}

	public int getRange()
	{
		return this._range;
	}
}
