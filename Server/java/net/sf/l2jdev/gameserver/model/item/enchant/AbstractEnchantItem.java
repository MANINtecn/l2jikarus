package net.sf.l2jdev.gameserver.model.item.enchant;

import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Logger;

import net.sf.l2jdev.gameserver.data.xml.ItemData;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.item.type.CrystalType;
import net.sf.l2jdev.gameserver.model.item.type.EtcItemType;

public abstract class AbstractEnchantItem
{
	protected static final Logger LOGGER = Logger.getLogger(AbstractEnchantItem.class.getName());
	private static final Set<EtcItemType> ENCHANT_TYPES = EnumSet.noneOf(EtcItemType.class);
	private final int _id;
	private final CrystalType _grade;
	private final int _minEnchantLevel;
	private final int _maxEnchantLevel;
	private final int _safeEnchantLevel;
	private final int _randomEnchantMin;
	private final int _randomEnchantMax;
	private final int _randomEnchantChance;
	private final double _bonusRate;
	private final boolean _isBlessed;

	public AbstractEnchantItem(StatSet set)
	{
		this._id = set.getInt("id");
		if (this.getItem() == null)
		{
			throw new NullPointerException();
		}
		else if (!ENCHANT_TYPES.contains(this.getItem().getItemType()))
		{
			throw new IllegalAccessError();
		}
		else
		{
			this._grade = set.getEnum("targetGrade", CrystalType.class, CrystalType.NONE);
			this._minEnchantLevel = set.getInt("minEnchant", 0);
			this._maxEnchantLevel = set.getInt("maxEnchant", 127);
			this._safeEnchantLevel = set.getInt("safeEnchant", 0);
			this._randomEnchantMin = set.getInt("randomEnchantMin", 1);
			this._randomEnchantMax = set.getInt("randomEnchantMax", this._randomEnchantMin);
			this._randomEnchantChance = set.getInt("randomEnchantChance", 50);
			this._bonusRate = set.getDouble("bonusRate", 0.0);
			this._isBlessed = set.getBoolean("isBlessed", false);
		}
	}

	public int getId()
	{
		return this._id;
	}

	public double getBonusRate()
	{
		return this._bonusRate;
	}

	public ItemTemplate getItem()
	{
		return ItemData.getInstance().getTemplate(this._id);
	}

	public CrystalType getGrade()
	{
		return this._grade;
	}

	public abstract boolean isWeapon();

	public int getMinEnchantLevel()
	{
		return this._minEnchantLevel;
	}

	public int getMaxEnchantLevel()
	{
		return this._maxEnchantLevel;
	}

	public int getSafeEnchant()
	{
		return this._safeEnchantLevel;
	}

	public int getRandomEnchantMin()
	{
		return this._randomEnchantMin;
	}

	public int getRandomEnchantMax()
	{
		return this._randomEnchantMax;
	}

	public int getRandomEnchantChance()
	{
		return this._randomEnchantChance;
	}

	public boolean isActionBlessed()
	{
		return this._isBlessed;
	}

	public boolean isValid(Item itemToEnchant, EnchantSupportItem supportItem)
	{
		if (itemToEnchant == null)
		{
			return false;
		}
		else if (itemToEnchant.isEnchantable() && (itemToEnchant.getTemplate().getEnchantLimit() == 0 || itemToEnchant.getEnchantLevel() != itemToEnchant.getTemplate().getEnchantLimit()))
		{
			if (!this.isValidItemType(itemToEnchant.getTemplate().getType2()))
			{
				return false;
			}
			return (this._minEnchantLevel == 0 || itemToEnchant.getEnchantLevel() >= this._minEnchantLevel) && (this._maxEnchantLevel == 0 || itemToEnchant.getEnchantLevel() < this._maxEnchantLevel) ? this._grade == itemToEnchant.getTemplate().getCrystalTypePlus() : false;
		}
		else
		{
			return false;
		}
	}

	private boolean isValidItemType(int type2)
	{
		if (type2 == 0)
		{
			return this.isWeapon();
		}
		return type2 != 1 && type2 != 2 ? false : !this.isWeapon();
	}

	static
	{
		ENCHANT_TYPES.add(EtcItemType.ENCHT_ATTR_ANCIENT_CRYSTAL_ENCHANT_AM);
		ENCHANT_TYPES.add(EtcItemType.ENCHT_ATTR_ANCIENT_CRYSTAL_ENCHANT_WP);
		ENCHANT_TYPES.add(EtcItemType.BLESS_ENCHT_AM);
		ENCHANT_TYPES.add(EtcItemType.BLESS_ENCHT_AM_DOWN);
		ENCHANT_TYPES.add(EtcItemType.BLESS_ENCHT_WP);
		ENCHANT_TYPES.add(EtcItemType.ENCHT_AM);
		ENCHANT_TYPES.add(EtcItemType.ENCHT_WP);
		ENCHANT_TYPES.add(EtcItemType.GIANT_ENCHT_AM);
		ENCHANT_TYPES.add(EtcItemType.GIANT_ENCHT_WP);
		ENCHANT_TYPES.add(EtcItemType.ENCHT_ATTR_INC_PROP_ENCHT_AM);
		ENCHANT_TYPES.add(EtcItemType.ENCHT_ATTR_INC_PROP_ENCHT_WP);
		ENCHANT_TYPES.add(EtcItemType.GIANT_ENCHT_ATTR_INC_PROP_ENCHT_AM);
		ENCHANT_TYPES.add(EtcItemType.GIANT_ENCHT_ATTR_INC_PROP_ENCHT_WP);
		ENCHANT_TYPES.add(EtcItemType.BLESSED_ENCHT_ATTR_INC_PROP_ENCHT_AM);
		ENCHANT_TYPES.add(EtcItemType.BLESSED_ENCHT_ATTR_INC_PROP_ENCHT_WP);
		ENCHANT_TYPES.add(EtcItemType.BLESSED_GIANT_ENCHT_ATTR_INC_PROP_ENCHT_AM);
		ENCHANT_TYPES.add(EtcItemType.BLESSED_GIANT_ENCHT_ATTR_INC_PROP_ENCHT_WP);
		ENCHANT_TYPES.add(EtcItemType.CURSED_ENCHT_AM);
		ENCHANT_TYPES.add(EtcItemType.CURSED_ENCHT_WP);
	}
}
