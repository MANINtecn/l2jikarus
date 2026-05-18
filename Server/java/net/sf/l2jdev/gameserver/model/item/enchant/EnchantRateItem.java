package net.sf.l2jdev.gameserver.model.item.enchant;

import java.util.HashSet;
import java.util.Set;

import net.sf.l2jdev.gameserver.model.item.ItemTemplate;

public class EnchantRateItem
{
	private final String _name;
	private final Set<Integer> _items = new HashSet<>();
	private long _slot;
	private Boolean _isMagicWeapon = null;

	public EnchantRateItem(String name)
	{
		this._name = name;
	}

	public String getName()
	{
		return this._name;
	}

	public void addItemId(int id)
	{
		this._items.add(id);
	}

	public void addSlot(long slot)
	{
		this._slot |= slot;
	}

	public void setMagicWeapon(boolean magicWeapon)
	{
		this._isMagicWeapon = magicWeapon ? Boolean.TRUE : Boolean.FALSE;
	}

	public boolean validate(ItemTemplate item)
	{
		if (!this._items.isEmpty() && !this._items.contains(item.getId()))
		{
			return false;
		}
		return this._slot != 0L && (item.getBodyPart().getMask() & this._slot) == 0L ? false : this._isMagicWeapon == null || item.isMagicWeapon() == this._isMagicWeapon;
	}
}
