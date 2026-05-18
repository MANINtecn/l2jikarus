package net.sf.l2jdev.gameserver.model.actor.holders.creature;

import net.sf.l2jdev.gameserver.data.enums.EvolveLevel;

public class PetEvolveHolder
{
	private final int _index;
	private final int _level;
	private final EvolveLevel _evolve;
	private final long _exp;
	private final String _name;

	public PetEvolveHolder(int index, int evolve, String name, int level, long exp)
	{
		this._index = index;
		this._evolve = EvolveLevel.values()[evolve];
		this._level = level;
		this._exp = exp;
		this._name = name;
	}

	public int getIndex()
	{
		return this._index;
	}

	public EvolveLevel getEvolve()
	{
		return this._evolve;
	}

	public int getLevel()
	{
		return this._level;
	}

	public long getExp()
	{
		return this._exp;
	}

	public String getName()
	{
		return this._name;
	}
}
