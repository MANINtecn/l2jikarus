package net.sf.l2jdev.gameserver.network.clientpackets.pet;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.instance.Pet;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.pet.PetItemList;

public class RequestGiveItemToPet extends ClientPacket
{
	private int _objectId;
	private long _amount;

	@Override
	protected void readImpl()
	{
		this._objectId = this.readInt();
		this._amount = this.readLong();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (this._amount > 0L && player != null && player.hasPet())
		{
			if (!this.getClient().getFloodProtectors().canPerformTransaction())
			{
				player.sendMessage("You are giving items to pet too fast.");
			}
			else if (!player.hasItemRequest())
			{
				if (PlayerConfig.ALT_GAME_KARMA_PLAYER_CAN_TRADE || player.getReputation() >= 0)
				{
					if (player.isInStoreMode())
					{
						player.sendMessage("You cannot exchange items while trading.");
					}
					else
					{
						Item item = player.getInventory().getItemByObjectId(this._objectId);
						if (item != null)
						{
							if (this._amount > item.getCount())
							{
								PunishmentManager.handleIllegalPlayerAction(player, this.getClass().getSimpleName() + ": Character " + player.getName() + " of account " + player.getAccountName() + " tried to get item with oid " + this._objectId + " from pet but has invalid count " + this._amount + " item count: " + item.getCount(), GeneralConfig.DEFAULT_PUNISH);
							}
							else if (!item.isAugmented())
							{
								if (!item.isHeroItem() && item.isDropable() && item.isDestroyable() && item.isTradeable())
								{
									Pet pet = player.getPet();
									if (pet.isDead())
									{
										player.sendPacket(SystemMessageId.YOU_CANNOT_TRANSFER_AN_ITEM_TO_A_DEAD_GUARDIAN);
									}
									else if (!pet.getInventory().validateCapacity(item))
									{
										player.sendPacket(SystemMessageId.YOUR_GUARDIAN_S_INVENTORY_IS_FULL_REMOVE_SOMETHING_FROM_IT_AND_TRY_AGAIN);
									}
									else if (!pet.getInventory().validateWeight(item, this._amount))
									{
										player.sendPacket(SystemMessageId.THE_GUARDIAN_S_INVENTORY_IS_FULL);
									}
									else
									{
										Item transferedItem = player.transferItem(ItemProcessType.TRANSFER, this._objectId, this._amount, pet.getInventory(), pet);
										if (transferedItem != null)
										{
											player.sendPacket(new PetItemList(pet.getInventory().getItems()));
										}
										else
										{
											PacketLogger.warning("Invalid item transfer request: " + pet.getName() + "(pet) --> " + player.getName());
										}
									}
								}
								else
								{
									player.sendPacket(SystemMessageId.THIS_ITEM_CANNOT_BE_TRANSFERRED_TO_A_GUARDIAN);
								}
							}
						}
					}
				}
			}
		}
	}
}
