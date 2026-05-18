package net.sf.l2jdev.gameserver.network.clientpackets.pet;

import net.sf.l2jdev.gameserver.handler.IItemHandler;
import net.sf.l2jdev.gameserver.handler.ItemHandler;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.instance.Pet;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.pet.PetItemList;

public class RequestPetUseItem extends ClientPacket
{
	private int _objectId;

	@Override
	protected void readImpl()
	{
		this._objectId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null && player.hasPet())
		{
			if (this.getClient().getFloodProtectors().canUseItem())
			{
				Pet pet = player.getPet();
				Item item = pet.getInventory().getItemByObjectId(this._objectId);
				if (item != null)
				{
					if (!item.getTemplate().isForNpc())
					{
						player.sendPacket(SystemMessageId.THE_GUARDIAN_CANNOT_USE_THIS_ITEM);
					}
					else if (!player.isAlikeDead() && !pet.isDead())
					{
						int reuseDelay = item.getReuseDelay();
						if (reuseDelay > 0)
						{
							long reuse = pet.getItemRemainingReuseTime(item.getObjectId());
							if (reuse > 0L)
							{
								return;
							}
						}

						if (item.isEquipped() || item.getTemplate().checkCondition(pet, pet, true))
						{
							this.useItem(pet, item, player);
						}
					}
					else
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_THE_REQUIREMENTS_ARE_NOT_MET);
						sm.addItemName(item);
						player.sendPacket(sm);
					}
				}
			}
		}
	}

	protected void useItem(Pet pet, Item item, Player player)
	{
		if (item.isEquipable())
		{
			if (!item.getTemplate().isConditionAttached())
			{
				player.sendPacket(SystemMessageId.THE_GUARDIAN_CANNOT_USE_THIS_ITEM);
				return;
			}

			if (item.isEquipped())
			{
				pet.getInventory().unEquipItemInSlot(item.getLocationSlot());
				SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_GUARDIAN_HAS_UNEQUIPPED_S1);
				sm.addItemName(item);
				player.sendPacket(sm);
			}
			else
			{
				pet.getInventory().equipItem(item);
				SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_GUARDIAN_HAS_EQUIPPED_S1);
				sm.addItemName(item);
				player.sendPacket(sm);
			}

			player.sendPacket(new PetItemList(pet.getInventory().getItems()));
			pet.updateAndBroadcastStatus(1);
		}
		else
		{
			IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
			if (handler != null)
			{
				if (handler.onItemUse(pet, item, false))
				{
					int reuseDelay = item.getReuseDelay();
					if (reuseDelay > 0)
					{
						player.addTimeStampItem(item, reuseDelay);
					}

					player.sendPacket(new PetItemList(pet.getInventory().getItems()));
					pet.updateAndBroadcastStatus(1);
				}
			}
			else
			{
				player.sendPacket(SystemMessageId.THE_GUARDIAN_CANNOT_USE_THIS_ITEM);
				PacketLogger.warning("No item handler registered for itemId: " + item.getId());
			}
		}
	}
}
