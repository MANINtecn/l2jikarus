package net.sf.l2jdev.gameserver.network.holders;

import java.util.List;

import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;

public class PetSkillAcquireHolder
{
	private final int _skillId;
	private final int _skillLevel;
	private final int _reqLvl;
	private final int _evolve;
	private final List<ItemHolder> _items;

	public PetSkillAcquireHolder(int skillId, int skillLevel, int reqLvl, int evolve, List<ItemHolder> items)
	{
		this._skillId = skillId;
		this._skillLevel = skillLevel;
		this._reqLvl = reqLvl;
		this._evolve = evolve;
		this._items = items;
	}

	public int getSkillId()
	{
		return this._skillId;
	}

	public int getSkillLevel()
	{
		return this._skillLevel;
	}

	public int getReqLvl()
	{
		return this._reqLvl;
	}

	public int getEvolve()
	{
		return this._evolve;
	}

	public List<ItemHolder> getItems()
	{
		return this._items;
	}
}
