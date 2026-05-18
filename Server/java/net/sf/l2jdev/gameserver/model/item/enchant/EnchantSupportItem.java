package net.sf.l2jdev.gameserver.model.item.enchant;

import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.item.type.EtcItemType;
import net.sf.l2jdev.gameserver.model.item.type.ItemType;

public class EnchantSupportItem extends AbstractEnchantItem
{
	private final boolean _isWeapon;
	private final boolean _isBlessed;
	private final boolean _isGiant;
	private final ItemType type = this.getItem().getItemType();

	public EnchantSupportItem(StatSet set)
	{
		super(set);
		this._isWeapon = this.type == EtcItemType.ENCHT_ATTR_INC_PROP_ENCHT_WP || this.type == EtcItemType.BLESSED_ENCHT_ATTR_INC_PROP_ENCHT_WP || this.type == EtcItemType.GIANT_ENCHT_ATTR_INC_PROP_ENCHT_WP || this.type == EtcItemType.BLESSED_GIANT_ENCHT_ATTR_INC_PROP_ENCHT_WP;
		this._isBlessed = this.type == EtcItemType.BLESSED_ENCHT_ATTR_INC_PROP_ENCHT_AM || this.type == EtcItemType.BLESSED_ENCHT_ATTR_INC_PROP_ENCHT_WP || this.type == EtcItemType.BLESSED_GIANT_ENCHT_ATTR_INC_PROP_ENCHT_AM || this.type == EtcItemType.BLESSED_GIANT_ENCHT_ATTR_INC_PROP_ENCHT_WP;
		this._isGiant = this.type == EtcItemType.GIANT_ENCHT_ATTR_INC_PROP_ENCHT_AM || this.type == EtcItemType.GIANT_ENCHT_ATTR_INC_PROP_ENCHT_WP || this.type == EtcItemType.BLESSED_GIANT_ENCHT_ATTR_INC_PROP_ENCHT_AM || this.type == EtcItemType.BLESSED_GIANT_ENCHT_ATTR_INC_PROP_ENCHT_WP;
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

	public boolean isGiant()
	{
		return this._isGiant;
	}
}
