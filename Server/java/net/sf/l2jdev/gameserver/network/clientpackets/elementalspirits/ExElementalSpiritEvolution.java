package net.sf.l2jdev.gameserver.network.clientpackets.elementalspirits;

import java.util.Iterator;
import java.util.stream.Collectors;

import net.sf.l2jdev.gameserver.model.ElementalSpirit;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.ElementalSpiritType;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.itemcontainer.InventoryBlockType;
import net.sf.l2jdev.gameserver.model.itemcontainer.PlayerInventory;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.enums.UserInfoType;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.UserInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.elementalspirits.ElementalSpiritEvolution;

public class ExElementalSpiritEvolution extends ClientPacket
{
	private byte _type;

	@Override
	protected void readImpl()
	{
		this._type = this.readByte();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			ElementalSpirit spirit = player.getElementalSpirit(ElementalSpiritType.of(this._type));
			if (spirit == null)
			{
				player.sendPacket(SystemMessageId.NO_SPIRITS_ARE_AVAILABLE);
			}
			else
			{
				boolean canEvolve = this.checkConditions(player, spirit);
				if (canEvolve)
				{
					spirit.upgrade();
					player.sendPacket(new SystemMessage(SystemMessageId.S1_HAS_EVOLVED_TO_LV_S2).addElementalSpiritName(this._type).addInt(spirit.getStage()));
					UserInfo userInfo = new UserInfo(player);
					userInfo.addComponentType(UserInfoType.ATT_SPIRITS);
					player.sendPacket(userInfo);
				}

				player.sendPacket(new ElementalSpiritEvolution(player, this._type, canEvolve));
			}
		}
	}

	private boolean checkConditions(Player player, ElementalSpirit spirit)
	{
		if (player.isInStoreMode())
		{
			player.sendPacket(SystemMessageId.CANNOT_EVOLVE_ABSORB_EXTRACT_WHILE_USING_THE_PRIVATE_STORE_WORKSHOP);
			return false;
		}
		else if (player.isInBattle())
		{
			player.sendPacket(SystemMessageId.UNABLE_TO_EVOLVE_DURING_BATTLE);
			return false;
		}
		else if (!spirit.canEvolve())
		{
			player.sendPacket(SystemMessageId.THIS_SPIRIT_CANNOT_EVOLVE);
			return false;
		}
		else if (!this.consumeEvolveItems(player, spirit))
		{
			player.sendPacket(SystemMessageId.NOT_ENOUGH_INGREDIENTS_FOR_EVOLUTION);
			return false;
		}
		else
		{
			return true;
		}
	}

	protected boolean consumeEvolveItems(Player player, ElementalSpirit spirit)
	{
		PlayerInventory inventory = player.getInventory();

		boolean var6;
		try
		{
			inventory.setInventoryBlock(spirit.getItemsToEvolve().stream().map(ItemHolder::getId).collect(Collectors.toList()), InventoryBlockType.BLACKLIST);
			Iterator<?> var4 = spirit.getItemsToEvolve().iterator();

			ItemHolder itemHolder;
			do
			{
				if (!var4.hasNext())
				{
					for (ItemHolder itemHolderx : spirit.getItemsToEvolve())
					{
						player.destroyItemByItemId(ItemProcessType.FEE, itemHolderx.getId(), itemHolderx.getCount(), player, true);
					}

					return true;
				}

				itemHolder = (ItemHolder) var4.next();
			}
			while (inventory.getInventoryItemCount(itemHolder.getId(), -1) >= itemHolder.getCount());

			var6 = false;
		}
		finally
		{
			inventory.unblock();
		}

		return var6;
	}
}
