package net.sf.l2jdev.gameserver.data.holders;

import java.util.List;

import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;

public class EquipmentUpgradeHolder
{
	private final int _id;
	private final int _requiredItemId;
	private final int _requiredItemEnchant;
	private final List<ItemHolder> _materials;
	private final long _adena;
	private final int _resultItemId;
	private final int _resultItemEnchant;
	private final boolean _announce;

	public EquipmentUpgradeHolder(int id, int requiredItemId, int requiredItemEnchant, List<ItemHolder> materials, long adena, int resultItemId, int resultItemEnchant, boolean announce)
	{
		this._id = id;
		this._requiredItemId = requiredItemId;
		this._requiredItemEnchant = requiredItemEnchant;
		this._materials = materials;
		this._adena = adena;
		this._resultItemId = resultItemId;
		this._resultItemEnchant = resultItemEnchant;
		this._announce = announce;
	}

	public int getId()
	{
		return this._id;
	}

	public int getRequiredItemId()
	{
		return this._requiredItemId;
	}

	public int getRequiredItemEnchant()
	{
		return this._requiredItemEnchant;
	}

	public List<ItemHolder> getMaterials()
	{
		return this._materials;
	}

	public long getAdena()
	{
		return this._adena;
	}

	public int getResultItemId()
	{
		return this._resultItemId;
	}

	public int getResultItemEnchant()
	{
		return this._resultItemEnchant;
	}

	public boolean isAnnounce()
	{
		return this._announce;
	}
}
