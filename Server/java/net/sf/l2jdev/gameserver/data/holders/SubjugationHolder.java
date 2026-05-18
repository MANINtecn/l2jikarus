package net.sf.l2jdev.gameserver.data.holders;

import java.util.List;
import java.util.Map;

public class SubjugationHolder
{
	private final int _category;
	private final List<int[]> _hottimes;
	private final Map<Integer, Integer> _npcs;

	public SubjugationHolder(int category, List<int[]> hottimes, Map<Integer, Integer> npcs)
	{
		this._category = category;
		this._hottimes = hottimes;
		this._npcs = npcs;
	}

	public int getCategory()
	{
		return this._category;
	}

	public List<int[]> getHottimes()
	{
		return this._hottimes;
	}

	public Map<Integer, Integer> getNpcs()
	{
		return this._npcs;
	}
}
