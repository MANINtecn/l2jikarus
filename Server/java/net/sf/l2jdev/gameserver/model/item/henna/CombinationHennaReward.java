package net.sf.l2jdev.gameserver.model.item.henna;

import net.sf.l2jdev.gameserver.model.item.combination.CombinationItemType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;

public class CombinationHennaReward extends ItemHolder
{
	private final int _hennaId;
	private final CombinationItemType _type;

	public CombinationHennaReward(int id, int count, CombinationItemType type)
	{
		super(id, count);
		this._hennaId = 0;
		this._type = type;
	}

	public CombinationHennaReward(int hennaId, int id, int count, CombinationItemType type)
	{
		super(id, count);
		this._hennaId = hennaId;
		this._type = type;
	}

	public int getHennaId()
	{
		return this._hennaId;
	}

	public CombinationItemType getType()
	{
		return this._type;
	}
}
