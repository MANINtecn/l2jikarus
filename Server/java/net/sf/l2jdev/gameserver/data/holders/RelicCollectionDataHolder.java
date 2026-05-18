package net.sf.l2jdev.gameserver.data.holders;

import java.util.Collection;
import java.util.List;

public class RelicCollectionDataHolder
{
	private final int _relicCollectionId;
	private final int _optionId;
	private final int _category;
	private final int _completeCount;
	private final int _combatPower;
	private final List<RelicCollectionInfoHolder> _relics;

	public RelicCollectionDataHolder(int relicCollectionId, int optionId, int category, int completeCount, int combatPower, List<RelicCollectionInfoHolder> relics)
	{
		this._relicCollectionId = relicCollectionId;
		this._optionId = optionId;
		this._category = category;
		this._completeCount = completeCount;
		this._combatPower = combatPower;
		this._relics = relics;
	}

	public int getCollectionId()
	{
		return this._relicCollectionId;
	}

	public int getOptionId()
	{
		return this._optionId;
	}

	public int getCategory()
	{
		return this._category;
	}

	public int getCompleteCount()
	{
		return this._completeCount;
	}

	public int getCombatPower()
	{
		return this._combatPower;
	}

	public Collection<RelicCollectionInfoHolder> getRelics()
	{
		return this._relics;
	}

	public RelicCollectionInfoHolder getRelic(int index)
	{
		return this._relics.get(index);
	}
}
