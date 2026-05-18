package net.sf.l2jdev.gameserver.data.holders;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.sf.l2jdev.gameserver.model.StatSet;

public class MultisellListHolder
{
	private final int _listId;
	private final boolean _isChanceMultisell;
	private final boolean _applyTaxes;
	private final boolean _maintainEnchantment;
	private final double _ingredientMultiplier;
	private final double _productMultiplier;
	protected List<MultisellEntryHolder> _entries;
	protected final Set<Integer> _npcsAllowed;

	public MultisellListHolder(int listId, boolean isChanceMultisell, boolean applyTaxes, boolean maintainEnchantment, double ingredientMultiplier, double productMultiplier, List<MultisellEntryHolder> entries, Set<Integer> npcsAllowed)
	{
		this._listId = listId;
		this._isChanceMultisell = isChanceMultisell;
		this._applyTaxes = applyTaxes;
		this._maintainEnchantment = maintainEnchantment;
		this._ingredientMultiplier = ingredientMultiplier;
		this._productMultiplier = productMultiplier;
		this._entries = entries;
		this._npcsAllowed = npcsAllowed;
	}

	@SuppressWarnings("unchecked")
	public MultisellListHolder(StatSet set)
	{
		this._listId = set.getInt("listId");
		this._isChanceMultisell = set.getBoolean("isChanceMultisell", false);
		this._applyTaxes = set.getBoolean("applyTaxes", false);
		this._maintainEnchantment = set.getBoolean("maintainEnchantment", false);
		this._ingredientMultiplier = set.getDouble("ingredientMultiplier", 1.0);
		this._productMultiplier = set.getDouble("productMultiplier", 1.0);
		this._entries = Collections.unmodifiableList(set.getList("entries", MultisellEntryHolder.class, Collections.emptyList()));
		this._npcsAllowed = set.getObject("allowNpc", Set.class);
	}

	public List<MultisellEntryHolder> getEntries()
	{
		return this._entries;
	}

	public int getId()
	{
		return this._listId;
	}

	public boolean isChanceMultisell()
	{
		return this._isChanceMultisell;
	}

	public boolean isApplyTaxes()
	{
		return this._applyTaxes;
	}

	public boolean isMaintainEnchantment()
	{
		return this._maintainEnchantment;
	}

	public double getIngredientMultiplier()
	{
		return this._ingredientMultiplier;
	}

	public double getProductMultiplier()
	{
		return this._productMultiplier;
	}

	public boolean isNpcAllowed(int npcId)
	{
		return this._npcsAllowed != null && this._npcsAllowed.contains(npcId);
	}
}
