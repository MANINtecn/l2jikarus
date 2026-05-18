package net.sf.l2jdev.gameserver.model.actor.holders.player;

public class PlayerRelicCollectionData
{
	private final int _relicCollectionId;
	private final int _relicId;
	private final int _relicLevel;
	private final int _index;

	public PlayerRelicCollectionData(int relicCollectionId, int relicId, int relicLevel, int index)
	{
		this._relicCollectionId = relicCollectionId;
		this._relicId = relicId;
		this._relicLevel = relicLevel;
		this._index = index;
	}

	public int getRelicCollectionId()
	{
		return this._relicCollectionId;
	}

	public int getRelicId()
	{
		return this._relicId;
	}

	public int getRelicLevel()
	{
		return this._relicLevel;
	}

	public int getIndex()
	{
		return this._index;
	}
}
