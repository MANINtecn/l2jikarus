package net.sf.l2jdev.gameserver.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import net.sf.l2jdev.gameserver.model.actor.enums.creature.AttributeType;
import net.sf.l2jdev.gameserver.model.ensoul.EnsoulOption;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.instance.Item;

public class TradeItem
{
	private int _objectId;
	private final ItemTemplate _item;
	private final int _location;
	private int _enchant;
	private final int _type1;
	private final int _type2;
	private long _count;
	private long _storeCount;
	private long _price;
	private byte _elemAtkType;
	private int _elemAtkPower;
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
	private Collection<EnsoulOption> _soulCrystalOptions;
	private Collection<EnsoulOption> _soulCrystalSpecialOptions;
	private int _visualId;
	private int _augmentationOption1 = 0;
	private int _augmentationOption2 = 0;
	private int _augmentationOption3 = 0;
	private boolean _isBlessed = false;

	public TradeItem(Item item, long count, long price)
	{
		Objects.requireNonNull(item);
		this._objectId = item.getObjectId();
		this._item = item.getTemplate();
		this._location = item.getLocationSlot();
		this._enchant = item.getEnchantLevel();
		this._type1 = item.getCustomType1();
		this._type2 = item.getCustomType2();
		this._count = count;
		this._price = price;
		this._elemAtkType = item.getAttackAttributeType().getClientId();
		this._elemAtkPower = item.getAttackAttributePower();

		for (AttributeType type : AttributeType.ATTRIBUTE_TYPES)
		{
			this._elemDefAttr[type.getClientId()] = item.getDefenceAttribute(type);
		}

		this._enchantOptions = item.getEnchantOptions();
		this._soulCrystalOptions = item.getSpecialAbilities();
		this._soulCrystalSpecialOptions = item.getAdditionalSpecialAbilities();
		this._visualId = item.getVisualId();
		this._isBlessed = item.isBlessed();
		VariationInstance augment = item.getAugmentation();
		if (augment != null)
		{
			this._augmentationOption1 = augment.getOption1Id();
			this._augmentationOption2 = augment.getOption2Id();
			this._augmentationOption3 = augment.getOption3Id();
		}
	}

	public TradeItem(ItemTemplate item, long count, long price)
	{
		Objects.requireNonNull(item);
		this._objectId = 0;
		this._item = item;
		this._location = 0;
		this._enchant = 0;
		this._type1 = 0;
		this._type2 = 0;
		this._count = count;
		this._storeCount = count;
		this._price = price;
		this._elemAtkType = AttributeType.NONE.getClientId();
		this._elemAtkPower = 0;
		this._enchantOptions = Item.DEFAULT_ENCHANT_OPTIONS;
		this._soulCrystalOptions = Collections.emptyList();
		this._soulCrystalSpecialOptions = Collections.emptyList();
	}

	public TradeItem(TradeItem item, long count, long price)
	{
		Objects.requireNonNull(item);
		this._objectId = item.getObjectId();
		this._item = item.getItem();
		this._location = item.getLocationSlot();
		this._enchant = item.getEnchant();
		this._type1 = item.getCustomType1();
		this._type2 = item.getCustomType2();
		this._count = count;
		this._storeCount = count;
		this._price = price;
		this._elemAtkType = item.getAttackElementType();
		this._elemAtkPower = item.getAttackElementPower();

		for (byte i = 0; i < 6; i++)
		{
			this._elemDefAttr[i] = item.getElementDefAttr(i);
		}

		this._enchantOptions = item.getEnchantOptions();
		this._soulCrystalOptions = item.getSoulCrystalOptions();
		this._soulCrystalSpecialOptions = item.getSoulCrystalSpecialOptions();
		this._visualId = item.getVisualId();
		this._isBlessed = item.isBlessed();
	}

	public void setObjectId(int objectId)
	{
		this._objectId = objectId;
	}

	public int getObjectId()
	{
		return this._objectId;
	}

	public ItemTemplate getItem()
	{
		return this._item;
	}

	public int getLocationSlot()
	{
		return this._location;
	}

	public void setEnchant(int enchant)
	{
		this._enchant = enchant;
	}

	public int getEnchant()
	{
		return this._enchant;
	}

	public int getCustomType1()
	{
		return this._type1;
	}

	public int getCustomType2()
	{
		return this._type2;
	}

	public void setCount(long count)
	{
		this._count = count;
	}

	public long getCount()
	{
		return this._count;
	}

	public long getStoreCount()
	{
		return this._storeCount;
	}

	public void setPrice(long price)
	{
		this._price = price;
	}

	public long getPrice()
	{
		return this._price;
	}

	public void setAttackElementType(AttributeType attackElement)
	{
		this._elemAtkType = attackElement.getClientId();
	}

	public byte getAttackElementType()
	{
		return this._elemAtkType;
	}

	public void setAttackElementPower(int attackElementPower)
	{
		this._elemAtkPower = attackElementPower;
	}

	public int getAttackElementPower()
	{
		return this._elemAtkPower;
	}

	public void setElementDefAttr(AttributeType element, int value)
	{
		this._elemDefAttr[element.getClientId()] = value;
	}

	public int getElementDefAttr(byte i)
	{
		return this._elemDefAttr[i];
	}

	public int[] getEnchantOptions()
	{
		return this._enchantOptions;
	}

	public void setSoulCrystalOptions(Collection<EnsoulOption> soulCrystalOptions)
	{
		this._soulCrystalOptions = soulCrystalOptions;
	}

	@SuppressWarnings("unchecked")
	public Collection<EnsoulOption> getSoulCrystalOptions()
	{
		return (Collection<EnsoulOption>) (this._soulCrystalOptions == null ? Collections.emptyList() : this._soulCrystalOptions);
	}

	public void setSoulCrystalSpecialOptions(Collection<EnsoulOption> soulCrystalSpecialOptions)
	{
		this._soulCrystalSpecialOptions = soulCrystalSpecialOptions;
	}

	@SuppressWarnings("unchecked")
	public Collection<EnsoulOption> getSoulCrystalSpecialOptions()
	{
		return (Collection<EnsoulOption>) (this._soulCrystalSpecialOptions == null ? Collections.emptyList() : this._soulCrystalSpecialOptions);
	}

	public void setAugmentation(int option1, int option2, int option3)
	{
		this._augmentationOption1 = option1;
		this._augmentationOption2 = option2;
		this._augmentationOption3 = option3;
	}

	public int getAugmentationOption1()
	{
		return this._augmentationOption1;
	}

	public int getAugmentationOption2()
	{
		return this._augmentationOption2;
	}

	public int getAugmentationOption3()
	{
		return this._augmentationOption3;
	}

	public void setVisualId(int visualItemId)
	{
		this._visualId = visualItemId;
	}

	public int getVisualId()
	{
		return this._visualId;
	}

	public boolean isBlessed()
	{
		return this._isBlessed;
	}
}
