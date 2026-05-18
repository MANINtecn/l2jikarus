package net.sf.l2jdev.gameserver.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import net.sf.l2jdev.gameserver.data.xml.PetDataTable;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.AttributeType;
import net.sf.l2jdev.gameserver.model.actor.holders.creature.PetEvolveHolder;
import net.sf.l2jdev.gameserver.model.buylist.Product;
import net.sf.l2jdev.gameserver.model.ensoul.EnsoulOption;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.WarehouseItem;
import net.sf.l2jdev.gameserver.model.item.instance.Item;

public class ItemInfo
{
	private int _objectId;
	private ItemTemplate _item;
	private int _enchantLevel;
	private VariationInstance _augmentation;
	private long _count;
	private int _price;
	private int _type1;
	private int _type2;
	private int _equipped;
	private boolean _petEquipped;
	private int _change;
	private int _mana;
	private int _time;
	private boolean _isBlessed = false;
	private boolean _available = true;
	private int _location;
	private byte _elemAtkType = -2;
	private int _elemAtkPower = 0;
	private final int[] _attributeDefence = new int[]
	{
		0,
		0,
		0,
		0,
		0,
		0
	};
	private int[] _option;
	private Collection<EnsoulOption> _soulCrystalOptions;
	private Collection<EnsoulOption> _soulCrystalSpecialOptions;
	private int _visualId;
	private long _visualExpiration;
	private int _reuseDelay;
	private Player _owner;
	private PetEvolveHolder _petData;

	public ItemInfo(Item item)
	{
		Objects.requireNonNull(item);
		this._objectId = item.getObjectId();
		this._item = item.getTemplate();
		this._enchantLevel = item.getEnchantLevel();
		this._augmentation = item.getAugmentation();
		this._count = item.getCount();
		this._type1 = item.getCustomType1();
		this._type2 = item.getCustomType2();
		this._equipped = item.isEquipped() ? 1 : 0;
		this._petEquipped = item.isPetEquipped();
		switch (item.getLastChange())
		{
			case 1:
				this._change = 1;
				break;
			case 2:
				this._change = 2;
				break;
			case 3:
				this._change = 3;
		}

		this._mana = item.getMana();
		this._time = item.isTimeLimitedItem() ? (int) (item.getRemainingTime() / 1000L) : -9999;
		this._available = item.isAvailable();
		this._location = item.getLocationSlot();
		this._elemAtkType = item.getAttackAttributeType().getClientId();
		this._elemAtkPower = item.getAttackAttributePower();

		for (AttributeType type : AttributeType.ATTRIBUTE_TYPES)
		{
			this._attributeDefence[type.getClientId()] = item.getDefenceAttribute(type);
		}

		this._isBlessed = item.isBlessed();
		this._option = item.getEnchantOptions();
		this._soulCrystalOptions = item.getSpecialAbilities();
		this._soulCrystalSpecialOptions = item.getAdditionalSpecialAbilities();
		this._visualId = item.getVisualId();
		this._visualExpiration = item.getVisualLifeTime() > 0L ? (item.getVisualLifeTime() - System.currentTimeMillis()) / 1000L : 0L;
		this._reuseDelay = item.getReuseDelay();
		this._owner = item.asPlayer();
		this._petData = this._owner != null ? (PetDataTable.getInstance().getPetDataByItemId(item.getId()) != null ? this._owner.getPetEvolve(this.getObjectId()) : null) : null;
	}

	public ItemInfo(Item item, int change)
	{
		this(item);
		this._change = change;
		this._visualExpiration = item.getVisualLifeTime() > 0L ? (item.getVisualLifeTime() - System.currentTimeMillis()) / 1000L : 0L;
	}

	public ItemInfo(TradeItem item)
	{
		if (item != null)
		{
			this._objectId = item.getObjectId();
			this._item = item.getItem();
			this._enchantLevel = item.getEnchant();
			if (item.getAugmentationOption1() > 0 || item.getAugmentationOption2() > 0 || item.getAugmentationOption3() > 0)
			{
				this._augmentation = new VariationInstance(0, item.getAugmentationOption1(), item.getAugmentationOption2(), item.getAugmentationOption3());
			}

			this._count = item.getCount();
			this._type1 = item.getCustomType1();
			this._type2 = item.getCustomType2();
			this._equipped = 0;
			this._petEquipped = false;
			this._change = 0;
			this._mana = -1;
			this._time = -9999;
			this._location = item.getLocationSlot();
			this._elemAtkType = item.getAttackElementType();
			this._elemAtkPower = item.getAttackElementPower();

			for (byte i = 0; i < 6; i++)
			{
				this._attributeDefence[i] = item.getElementDefAttr(i);
			}

			this._option = item.getEnchantOptions();
			this._soulCrystalOptions = item.getSoulCrystalOptions();
			this._soulCrystalSpecialOptions = item.getSoulCrystalSpecialOptions();
			this._visualId = item.getVisualId();
			this._isBlessed = item.isBlessed();
		}
	}

	public ItemInfo(Product item)
	{
		if (item != null)
		{
			this._objectId = 0;
			this._item = item.getItem();
			this._enchantLevel = 0;
			this._augmentation = null;
			this._count = item.getCount();
			this._type1 = item.getItem().getType1();
			this._type2 = item.getItem().getType2();
			this._equipped = 0;
			this._petEquipped = false;
			this._change = 0;
			this._mana = -1;
			this._time = -9999;
			this._location = 0;
			this._soulCrystalOptions = Collections.emptyList();
			this._soulCrystalSpecialOptions = Collections.emptyList();
		}
	}

	public ItemInfo(WarehouseItem item)
	{
		if (item != null)
		{
			this._objectId = item.getObjectId();
			this._item = item.getItem();
			this._enchantLevel = item.getEnchantLevel();
			this._augmentation = item.getAugmentation();
			this._count = item.getCount();
			this._type1 = item.getCustomType1();
			this._type2 = item.getCustomType2();
			this._equipped = 0;
			this._petEquipped = false;
			this._mana = item.getMana();
			this._time = item.getTime();
			this._location = item.getLocationSlot();
			this._elemAtkType = item.getAttackElementType();
			this._elemAtkPower = item.getAttackElementPower();

			for (byte i = 0; i < 6; i++)
			{
				this._attributeDefence[i] = item.getElementDefAttr(i);
			}

			this._option = item.getEnchantOptions();
			this._soulCrystalOptions = item.getSoulCrystalOptions();
			this._soulCrystalSpecialOptions = item.getSoulCrystalSpecialOptions();
			this._isBlessed = item.isBlessed();
		}
	}

	public int getObjectId()
	{
		return this._objectId;
	}

	public ItemTemplate getItem()
	{
		return this._item;
	}

	public int getEnchantLevel()
	{
		return this._enchantLevel;
	}

	public VariationInstance getAugmentation()
	{
		return this._augmentation;
	}

	public long getCount()
	{
		return this._count;
	}

	public int getPrice()
	{
		return this._price;
	}

	public int getCustomType1()
	{
		return this._type1;
	}

	public int getCustomType2()
	{
		return this._type2;
	}

	public int getEquipped()
	{
		return this._equipped;
	}

	public boolean isPetEquipped()
	{
		return this._petEquipped;
	}

	public int getChange()
	{
		return this._change;
	}

	public int getMana()
	{
		return this._mana;
	}

	public int getTime()
	{
		return this._time > 0 ? this._time : (this._visualExpiration > 0L ? (int) this._visualExpiration : -9999);
	}

	public boolean isAvailable()
	{
		return this._available;
	}

	public int getLocation()
	{
		return this._location;
	}

	public int getAttackElementType()
	{
		return this._elemAtkType;
	}

	public int getAttackElementPower()
	{
		return this._elemAtkPower;
	}

	public int getAttributeDefence(AttributeType attribute)
	{
		return this._attributeDefence[attribute.getClientId()];
	}

	public int[] getEnchantOptions()
	{
		return this._option;
	}

	public int getVisualId()
	{
		return this._visualId;
	}

	@SuppressWarnings("unchecked")
	public Collection<EnsoulOption> getSoulCrystalOptions()
	{
		return (Collection<EnsoulOption>) (this._soulCrystalOptions != null ? this._soulCrystalOptions : Collections.emptyList());
	}

	public boolean soulCrystalOptionsMatch(EnsoulOption[] soulCrystalOptions)
	{
		if (this._soulCrystalOptions == null)
		{
			return false;
		}
		for (EnsoulOption soulCrystalOption1 : this._soulCrystalOptions)
		{
			boolean found = false;

			for (EnsoulOption soulCrystalOption2 : soulCrystalOptions)
			{
				if (soulCrystalOption1.equals(soulCrystalOption2))
				{
					found = true;
					break;
				}
			}

			if (!found)
			{
				return false;
			}
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	public Collection<EnsoulOption> getSoulCrystalSpecialOptions()
	{
		return (Collection<EnsoulOption>) (this._soulCrystalSpecialOptions != null ? this._soulCrystalSpecialOptions : Collections.emptyList());
	}

	public boolean soulCrystalSpecialOptionsMatch(EnsoulOption[] soulCrystalSpecialOptions)
	{
		if (this._soulCrystalSpecialOptions == null)
		{
			return false;
		}
		for (EnsoulOption soulCrystalSpecialOption1 : this._soulCrystalSpecialOptions)
		{
			boolean found = false;

			for (EnsoulOption soulCrystalSpecialOption2 : soulCrystalSpecialOptions)
			{
				if (soulCrystalSpecialOption1.equals(soulCrystalSpecialOption2))
				{
					found = true;
					break;
				}
			}

			if (!found)
			{
				return false;
			}
		}

		return true;
	}

	public long getVisualExpiration()
	{
		return this._visualExpiration;
	}

	public boolean isBlessed()
	{
		return this._isBlessed;
	}

	public int getReuseDelay()
	{
		return this._reuseDelay;
	}

	public Player getOwner()
	{
		return this._owner;
	}

	public PetEvolveHolder getPetData()
	{
		return this._petData;
	}

	@Override
	public String toString()
	{
		return this._item + "[objId: " + this._objectId + ", count: " + this._count + "]";
	}
}
