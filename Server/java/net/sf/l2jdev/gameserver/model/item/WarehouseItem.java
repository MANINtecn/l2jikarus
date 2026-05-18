package net.sf.l2jdev.gameserver.model.item;

import java.util.Collection;
import java.util.Objects;

import net.sf.l2jdev.gameserver.model.VariationInstance;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.AttributeType;
import net.sf.l2jdev.gameserver.model.ensoul.EnsoulOption;
import net.sf.l2jdev.gameserver.model.item.enums.BodyPart;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.item.type.CrystalType;
import net.sf.l2jdev.gameserver.model.item.type.ItemType;

public class WarehouseItem
{
	private final ItemTemplate _item;
	private final int _object;
	private final long _count;
	private final int _owner;
	private final int _locationSlot;
	private final int _enchant;
	private final CrystalType _grade;
	private final VariationInstance _augmentation;
	private final int _customType1;
	private final int _customType2;
	private final int _mana;
	private byte _elemAtkType = -2;
	private int _elemAtkPower = 0;
	private final int[] _elemDefAttr = new int[]
	{
		0,
		0,
		0,
		0,
		0,
		0
	};
	private final int[] _enchantOptions;
	private final Collection<EnsoulOption> _soulCrystalOptions;
	private final Collection<EnsoulOption> _soulCrystalSpecialOptions;
	private final int _time;
	private final boolean _isBlessed;

	public WarehouseItem(Item item)
	{
		Objects.requireNonNull(item);
		this._item = item.getTemplate();
		this._object = item.getObjectId();
		this._count = item.getCount();
		this._owner = item.getOwnerId();
		this._locationSlot = item.getLocationSlot();
		this._enchant = item.getEnchantLevel();
		this._customType1 = item.getCustomType1();
		this._customType2 = item.getCustomType2();
		this._grade = item.getTemplate().getCrystalType();
		this._augmentation = item.getAugmentation();
		this._mana = item.getMana();
		this._time = item.isTimeLimitedItem() ? (int) (item.getRemainingTime() / 1000L) : -1;
		this._elemAtkType = item.getAttackAttributeType().getClientId();
		this._elemAtkPower = item.getAttackAttributePower();

		for (AttributeType type : AttributeType.ATTRIBUTE_TYPES)
		{
			this._elemDefAttr[type.getClientId()] = item.getDefenceAttribute(type);
		}

		this._enchantOptions = item.getEnchantOptions();
		this._soulCrystalOptions = item.getSpecialAbilities();
		this._soulCrystalSpecialOptions = item.getAdditionalSpecialAbilities();
		this._isBlessed = item.isBlessed();
	}

	public ItemTemplate getItem()
	{
		return this._item;
	}

	public int getObjectId()
	{
		return this._object;
	}

	public int getOwnerId()
	{
		return this._owner;
	}

	public int getLocationSlot()
	{
		return this._locationSlot;
	}

	public long getCount()
	{
		return this._count;
	}

	public int getType1()
	{
		return this._item.getType1();
	}

	public int getType2()
	{
		return this._item.getType2();
	}

	public ItemType getItemType()
	{
		return this._item.getItemType();
	}

	public int getItemId()
	{
		return this._item.getId();
	}

	public BodyPart getBodyPart()
	{
		return this._item.getBodyPart();
	}

	public int getEnchantLevel()
	{
		return this._enchant;
	}

	public CrystalType getItemGrade()
	{
		return this._grade;
	}

	public boolean isWeapon()
	{
		return this._item instanceof Weapon;
	}

	public boolean isArmor()
	{
		return this._item instanceof Armor;
	}

	public boolean isEtcItem()
	{
		return this._item instanceof EtcItem;
	}

	public String getItemName()
	{
		return this._item.getName();
	}

	public VariationInstance getAugmentation()
	{
		return this._augmentation;
	}

	public String getName()
	{
		return this._item.getName();
	}

	public int getCustomType1()
	{
		return this._customType1;
	}

	public int getCustomType2()
	{
		return this._customType2;
	}

	public int getMana()
	{
		return this._mana;
	}

	public byte getAttackElementType()
	{
		return this._elemAtkType;
	}

	public int getAttackElementPower()
	{
		return this._elemAtkPower;
	}

	public int getElementDefAttr(byte i)
	{
		return this._elemDefAttr[i];
	}

	public int[] getEnchantOptions()
	{
		return this._enchantOptions;
	}

	public Collection<EnsoulOption> getSoulCrystalOptions()
	{
		return this._soulCrystalOptions;
	}

	public Collection<EnsoulOption> getSoulCrystalSpecialOptions()
	{
		return this._soulCrystalSpecialOptions;
	}

	public int getTime()
	{
		return this._time;
	}

	public boolean isBlessed()
	{
		return this._isBlessed;
	}

	@Override
	public String toString()
	{
		return this._item.toString();
	}
}
