package net.sf.l2jdev.gameserver.network.clientpackets;

import java.util.Arrays;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.EnchantItemAttributeRequest;
import net.sf.l2jdev.gameserver.model.actor.request.EnchantItemRequest;
import net.sf.l2jdev.gameserver.model.item.Armor;
import net.sf.l2jdev.gameserver.model.item.Weapon;
import net.sf.l2jdev.gameserver.model.item.enums.ItemLocation;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.options.VariationFee;
import net.sf.l2jdev.gameserver.model.skill.AbnormalType;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public abstract class AbstractRefinePacket extends ClientPacket
{
	protected static boolean isValid(Player player, Item item, Item mineralItem, Item feeItem, VariationFee fee)
	{
		if (fee == null)
		{
			return false;
		}
		else if (!isValid(player, item, mineralItem))
		{
			return false;
		}
		else
		{
			if (feeItem != null)
			{
				if ((feeItem.getOwnerId() != player.getObjectId()) || (feeItem.getItemLocation() != ItemLocation.INVENTORY))
				{
					return false;
				}
			}

			return true;
		}
	}

	protected static boolean isValid(Player player, Item item, Item mineralItem)
	{
		if (!isValid(player, item))
		{
			return false;
		}
		return mineralItem.getOwnerId() != player.getObjectId() ? false : mineralItem.getItemLocation() == ItemLocation.INVENTORY;
	}

	protected static boolean isValid(Player player, Item item)
	{
		if (!isValid(player))
		{
			return false;
		}
		else if (item.getOwnerId() != player.getObjectId())
		{
			return false;
		}
		else if (item.isHeroItem())
		{
			return false;
		}
		else if (item.isShadowItem())
		{
			return false;
		}
		else if (item.isCommonItem())
		{
			return false;
		}
		else if (item.isEtcItem())
		{
			return false;
		}
		else if (item.isTimeLimitedItem())
		{
			return false;
		}
		else if (item.isPvp() && !PlayerConfig.ALT_ALLOW_AUGMENT_PVP_ITEMS)
		{
			return false;
		}
		else
		{
			switch (item.getItemLocation())
			{
				case INVENTORY:
				case PAPERDOLL:
					if (!(item.getTemplate() instanceof Weapon) && !(item.getTemplate() instanceof Armor))
					{
						return false;
					}
					if (Arrays.binarySearch(PlayerConfig.AUGMENTATION_BLACKLIST, item.getId()) >= 0)
					{
						return false;
					}

					return true;
				default:
					return false;
			}
		}
	}

	protected static boolean isValid(Player player)
	{
		if (player.isInStoreMode())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_IS_IN_OPERATION);
			return false;
		}
		else if (player.getActiveTradeList() != null)
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_ENGAGED_IN_TRADE_ACTIVITIES);
			return false;
		}
		else if (player.isDead())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_DEAD);
			return false;
		}
		else if (player.hasBlockActions() && player.hasAbnormalType(AbnormalType.PARALYZE))
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_PARALYZED);
			return false;
		}
		else if (player.isFishing())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_FISHING);
			return false;
		}
		else if (player.isSitting())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_SITTING);
			return false;
		}
		else
		{
			return player.isCursedWeaponEquipped() ? false : !player.hasRequest(EnchantItemRequest.class, EnchantItemAttributeRequest.class) && !player.isProcessingTransaction();
		}
	}
}
