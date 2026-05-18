package net.sf.l2jdev.gameserver.model.actor.holders.player;

public class PlayerCollectionData
{
	private final int _collectionId;
	private final int _itemId;
	private final int _index;

	public PlayerCollectionData(int collectionId, int itemId, int index)
	{
		this._collectionId = collectionId;
		this._itemId = itemId;
		this._index = index;
	}

	public int getCollectionId()
	{
		return this._collectionId;
	}

	public int getItemId()
	{
		return this._itemId;
	}

	public int getIndex()
	{
		return this._index;
	}
}
