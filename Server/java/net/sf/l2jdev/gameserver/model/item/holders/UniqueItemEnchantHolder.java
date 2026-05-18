package net.sf.l2jdev.gameserver.model.item.holders;

public class UniqueItemEnchantHolder extends ItemEnchantHolder
{
	private final int _objectId;

	public UniqueItemEnchantHolder(int id, int objectId)
	{
		this(id, objectId, 1L);
	}

	public UniqueItemEnchantHolder(int id, int objectId, long count)
	{
		super(id, count);
		this._objectId = objectId;
	}

	public UniqueItemEnchantHolder(ItemEnchantHolder itemHolder, int objectId)
	{
		super(itemHolder.getId(), itemHolder.getCount(), itemHolder.getEnchantLevel());
		this._objectId = objectId;
	}

	public int getObjectId()
	{
		return this._objectId;
	}

	@Override
	public String toString()
	{
		return "[" + this.getClass().getSimpleName() + "] ID: " + this.getId() + ", object ID: " + this._objectId + ", count: " + this.getCount() + ", enchant level: " + this.getEnchantLevel();
	}
}
