package net.sf.l2jdev.gameserver.data.holders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.item.type.WeaponType;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;

public final class CharacterStyleDataHolder
{
	public final int _styleId;
	public final String _name;
	public final WeaponType _weaponType;
	public final int _shiftWeaponId;
	private final SkillHolder _skillHolder;
	public final List<ItemHolder> _cost;

	public CharacterStyleDataHolder(int styleId, String name, List<ItemHolder> cost)
	{
		this._styleId = styleId;
		this._name = name;
		this._shiftWeaponId = 0;
		this._skillHolder = null;
		this._weaponType = WeaponType.NONE;
		this._cost = Collections.unmodifiableList(new ArrayList<>(cost));
	}

	public CharacterStyleDataHolder(int styleId, String name, int shiftWeaponId, WeaponType weaponType, List<ItemHolder> cost)
	{
		this._styleId = styleId;
		this._name = name;
		this._shiftWeaponId = shiftWeaponId;
		this._skillHolder = null;
		this._weaponType = weaponType;
		this._cost = Collections.unmodifiableList(new ArrayList<>(cost));
	}

	public CharacterStyleDataHolder(int styleId, String name, SkillHolder skillHolder, List<ItemHolder> cost)
	{
		this._styleId = styleId;
		this._name = name;
		this._shiftWeaponId = 0;
		this._weaponType = WeaponType.NONE;
		this._skillHolder = skillHolder;
		this._cost = Collections.unmodifiableList(new ArrayList<>(cost));
	}

	public WeaponType getWeaponType()
	{
		return this._weaponType;
	}

	public SkillHolder getSkillHolder()
	{
		return this._skillHolder;
	}

	public int getShiftWeaponId()
	{
		return this._shiftWeaponId;
	}

	public int getStyleId()
	{
		return this._styleId;
	}

	public List<ItemHolder> getCosts()
	{
		return this._cost;
	}

	@Override
	public String toString()
	{
		return "Style{id=" + this._styleId + ", name='" + this._name + "', shiftWeaponId=" + this._shiftWeaponId + ", cost=" + this._cost + "}";
	}
}
