package net.sf.l2jdev.gameserver.model.item.enchant;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.data.xml.EnchantItemData;
import net.sf.l2jdev.gameserver.data.xml.EnchantItemGroupsData;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.item.type.CrystalType;
import net.sf.l2jdev.gameserver.model.item.type.EtcItemType;
import net.sf.l2jdev.gameserver.model.item.type.ItemType;
import net.sf.l2jdev.gameserver.model.stats.Stat;

public class EnchantScroll extends AbstractEnchantItem
{
	private final boolean _isWeapon;
	private final boolean _isBlessed;
	private final boolean _isBlessedDown;
	private final boolean _isSafe;
	private final boolean _isGiant;
	private final boolean _isCursed;
	private final int _scrollGroupId;
	private final Map<Integer, Integer> _items = new HashMap<>();

	public EnchantScroll(StatSet set)
	{
		super(set);
		this._scrollGroupId = set.getInt("scrollGroupId", 0);
		ItemType type = this.getItem().getItemType();
		this._isWeapon = type == EtcItemType.ENCHT_ATTR_ANCIENT_CRYSTAL_ENCHANT_WP || type == EtcItemType.BLESS_ENCHT_WP || type == EtcItemType.ENCHT_WP || type == EtcItemType.GIANT_ENCHT_WP || type == EtcItemType.CURSED_ENCHT_WP;
		this._isBlessed = type == EtcItemType.BLESS_ENCHT_AM || type == EtcItemType.BLESS_ENCHT_WP || type == EtcItemType.BLESSED_ENCHT_ATTR_INC_PROP_ENCHT_WP || type == EtcItemType.BLESSED_ENCHT_ATTR_INC_PROP_ENCHT_AM || type == EtcItemType.BLESSED_GIANT_ENCHT_ATTR_INC_PROP_ENCHT_AM || type == EtcItemType.BLESSED_GIANT_ENCHT_ATTR_INC_PROP_ENCHT_WP;
		this._isBlessedDown = type == EtcItemType.BLESS_ENCHT_AM_DOWN;
		this._isSafe = type == EtcItemType.ENCHT_ATTR_ANCIENT_CRYSTAL_ENCHANT_AM || type == EtcItemType.ENCHT_ATTR_ANCIENT_CRYSTAL_ENCHANT_WP || type == EtcItemType.ENCHT_ATTR_CRYSTAL_ENCHANT_AM || type == EtcItemType.ENCHT_ATTR_CRYSTAL_ENCHANT_WP;
		this._isGiant = type == EtcItemType.GIANT_ENCHT_AM || type == EtcItemType.GIANT_ENCHT_WP;
		this._isCursed = type == EtcItemType.CURSED_ENCHT_AM || type == EtcItemType.CURSED_ENCHT_WP;
	}

	@Override
	public boolean isWeapon()
	{
		return this._isWeapon;
	}

	public boolean isBlessed()
	{
		return this._isBlessed;
	}

	public boolean isBlessedDown()
	{
		return this._isBlessedDown;
	}

	public boolean isSafe()
	{
		return this._isSafe;
	}

	public boolean isGiant()
	{
		return this._isGiant;
	}

	public boolean isCursed()
	{
		return this._isCursed;
	}

	public void addItem(int itemId, int scrollGroupId)
	{
		this._items.put(itemId, scrollGroupId > -1 ? scrollGroupId : this._scrollGroupId);
	}

	public Collection<Integer> getItems()
	{
		return this._items.keySet();
	}

	@Override
	public boolean isValid(Item itemToEnchant, EnchantSupportItem supportItem)
	{
		if (!this._items.isEmpty() && !this._items.containsKey(itemToEnchant.getId()))
		{
			return false;
		}
		if (supportItem != null)
		{
			if (this.isBlessed() && !supportItem.isBlessed() || !this.isBlessed() && supportItem.isBlessed())
			{
				return false;
			}

			if (this.isBlessedDown() && !supportItem.isBlessed() || !this.isBlessedDown() && supportItem.isBlessed())
			{
				return false;
			}

			if (this.isGiant() && !supportItem.isGiant() || !this.isGiant() && supportItem.isGiant())
			{
				return false;
			}

			if (!supportItem.isValid(itemToEnchant, supportItem) || (supportItem.isWeapon() != this.isWeapon()))
			{
				return false;
			}
		}

		if (this._items.isEmpty())
		{
			if (this.isActionBlessed() && itemToEnchant.isWeapon() && itemToEnchant.getTemplate().getCrystalType() == this.getGrade())
			{
				return true;
			}

			for (EnchantScroll scroll : EnchantItemData.getInstance().getScrolls())
			{
				if (scroll.getId() != this.getId())
				{
					Collection<Integer> scrollItems = scroll.getItems();
					if (!scrollItems.isEmpty() && scrollItems.contains(itemToEnchant.getId()))
					{
						return false;
					}
				}
			}
		}

		return super.isValid(itemToEnchant, supportItem);
	}

	public double getChance(Player player, Item enchantItem)
	{
		if (enchantItem == null)
		{
			return -1.0;
		}
		int scrollGroupId = this._items.getOrDefault(enchantItem.getId(), this._scrollGroupId);
		if (EnchantItemGroupsData.getInstance().getScrollGroup(scrollGroupId) == null)
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": Unexistent enchant scroll group specified for enchant scroll: " + this.getId());
			return -1.0;
		}
		EnchantItemGroup group = EnchantItemGroupsData.getInstance().getItemGroup(enchantItem.getTemplate(), scrollGroupId);
		if (group == null)
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": Couldn't find enchant item group for scroll: " + this.getId() + " requested by: " + player);
			return -1.0;
		}
		return this.getSafeEnchant() > 0 && enchantItem.getEnchantLevel() < this.getSafeEnchant() ? 100.0 : group.getChance(enchantItem.getEnchantLevel());
	}

	public EnchantResultType calculateSuccess(Player player, Item enchantItem, EnchantSupportItem supportItem)
	{
		if (!this.isValid(enchantItem, supportItem))
		{
			return EnchantResultType.ERROR;
		}
		double chance = this.getChance(player, enchantItem);
		if (chance == -1.0)
		{
			return EnchantResultType.ERROR;
		}
		int crystalLevel = enchantItem.getTemplate().getCrystalType().getLevel();
		double enchantRateStat = crystalLevel > CrystalType.NONE.getLevel() && crystalLevel < CrystalType.EVENT.getLevel() ? player.getStat().getValue(Stat.ENCHANT_RATE) : 0.0;
		double bonusRate = this.getBonusRate();
		double supportBonusRate = supportItem != null ? supportItem.getBonusRate() : 0.0;
		double finalChance = Math.min(chance + bonusRate + supportBonusRate + enchantRateStat, 100.0);
		double random = 100.0 * Rnd.nextDouble();
		boolean success = random < finalChance;
		return success ? EnchantResultType.SUCCESS : EnchantResultType.FAILURE;
	}
}
