package net.sf.l2jdev.gameserver.model.ensoul;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;

public class EnsoulFee
{
	private final Integer _stoneId;
	private final ItemHolder[] _ensoulFee = new ItemHolder[3];
	private final ItemHolder[] _resoulFees = new ItemHolder[3];
	private final List<ItemHolder> _removalFee = new ArrayList<>();

	public EnsoulFee(Integer stoneId)
	{
		this._stoneId = stoneId;
	}

	public Integer getStoneId()
	{
		return this._stoneId;
	}

	public void setEnsoul(int index, ItemHolder item)
	{
		this._ensoulFee[index] = item;
	}

	public void setResoul(int index, ItemHolder item)
	{
		this._resoulFees[index] = item;
	}

	public void addRemovalFee(ItemHolder itemHolder)
	{
		this._removalFee.add(itemHolder);
	}

	public ItemHolder getEnsoul(int index)
	{
		return this._ensoulFee[index];
	}

	public ItemHolder getResoul(int index)
	{
		return this._resoulFees[index];
	}

	public List<ItemHolder> getRemovalFee()
	{
		return this._removalFee;
	}
}
