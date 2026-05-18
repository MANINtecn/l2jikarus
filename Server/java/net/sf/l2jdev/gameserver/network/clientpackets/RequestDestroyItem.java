package net.sf.l2jdev.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.handler.AdminCommandHandler;
import net.sf.l2jdev.gameserver.managers.CursedWeaponsManager;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class RequestDestroyItem extends ClientPacket
{
	private int _objectId;
	private long _count;

	@Override
	protected void readImpl()
	{
		this._objectId = this.readInt();
		this._count = this.readLong();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (this._count < 1L)
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_DESTROY_IT_BECAUSE_THE_NUMBER_IS_INCORRECT);
				if (this._count < 0L)
				{
					PunishmentManager.handleIllegalPlayerAction(player, "[RequestDestroyItem] Character " + player.getName() + " of account " + player.getAccountName() + " tried to destroy item with oid " + this._objectId + " but has count < 0!", GeneralConfig.DEFAULT_PUNISH);
				}
			}
			else if (!this.getClient().getFloodProtectors().canPerformTransaction())
			{
				player.sendMessage("You are destroying items too fast.");
			}
			else
			{
				long count = this._count;
				if (player.isProcessingTransaction() || player.isInStoreMode())
				{
					player.sendPacket(SystemMessageId.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
				}
				else if (player.hasItemRequest())
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_DESTROY_OR_CRYSTALLIZE_ITEMS_WHILE_ENCHANTING_ATTRIBUTES);
				}
				else
				{
					Item itemToRemove = player.getInventory().getItemByObjectId(this._objectId);
					if (itemToRemove == null)
					{
						if (player.isGM())
						{
							WorldObject obj = World.getInstance().findObject(this._objectId);
							if (obj != null && obj.isItem())
							{
								if (this._count > ((Item) obj).getCount())
								{
									count = ((Item) obj).getCount();
								}

								AdminCommandHandler.getInstance().onCommand(player, "admin_delete_item " + this._objectId + " " + count, true);
							}
						}
						else
						{
							player.sendPacket(SystemMessageId.THE_ITEM_CANNOT_BE_DROPPED);
						}
					}
					else if (player.isCastingNow(s -> s.getSkill().getItemConsumeId() == itemToRemove.getId()))
					{
						player.sendPacket(SystemMessageId.THE_ITEM_CANNOT_BE_DROPPED);
					}
					else
					{
						int itemId = itemToRemove.getId();
						if (GeneralConfig.DESTROY_ALL_ITEMS || (player.isGM() || itemToRemove.isDestroyable()) && !CursedWeaponsManager.getInstance().isCursed(itemId))
						{
							if (!itemToRemove.isStackable() && count > 1L)
							{
								PunishmentManager.handleIllegalPlayerAction(player, "[RequestDestroyItem] Character " + player.getName() + " of account " + player.getAccountName() + " tried to destroy a non-stackable item with oid " + this._objectId + " but has count > 1!", GeneralConfig.DEFAULT_PUNISH);
							}
							else if (!player.getInventory().canManipulateWithItemId(itemToRemove.getId()))
							{
								player.sendMessage("You cannot use this item.");
							}
							else
							{
								if (this._count > itemToRemove.getCount())
								{
									count = itemToRemove.getCount();
								}

								if (itemToRemove.getTemplate().isPetItem())
								{
									Summon pet = player.getPet();
									if (pet != null && pet.getControlObjectId() == this._objectId || player.isMounted() && player.getMountObjectID() == this._objectId)
									{
										player.sendPacket(SystemMessageId.THE_GUARDIAN_CANNOT_BE_DESTROYED_AS_IT_IS_CURRENTLY_SUMMONED);
										return;
									}

									try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");)
									{
										statement.setInt(1, this._objectId);
										statement.execute();
									}
									catch (Exception var15)
									{
										PacketLogger.warning("Could not delete pet objectid: " + var15.getMessage());
									}
								}

								if (itemToRemove.isEquipped())
								{
									if (itemToRemove.getEnchantLevel() > 0)
									{
										SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2_UNEQUIPPED);
										sm.addInt(itemToRemove.getEnchantLevel());
										sm.addItemName(itemToRemove);
										player.sendPacket(sm);
									}
									else
									{
										SystemMessage sm = new SystemMessage(SystemMessageId.S1_UNEQUIPPED);
										sm.addItemName(itemToRemove);
										player.sendPacket(sm);
									}

									InventoryUpdate iu = new InventoryUpdate();

									for (Item itm : player.getInventory().unEquipItemInSlotAndRecord(itemToRemove.getLocationSlot()))
									{
										iu.addModifiedItem(itm);
									}

									player.sendPacket(iu);
								}

								Item removedItem = player.getInventory().destroyItem(ItemProcessType.DESTROY, itemToRemove, count, player, null);
								if (removedItem != null)
								{
									InventoryUpdate iu = new InventoryUpdate();
									if (removedItem.getCount() == 0L)
									{
										iu.addRemovedItem(removedItem);
									}
									else
									{
										iu.addModifiedItem(removedItem);
									}

									player.sendPacket(iu);
									player.updateAdenaAndWeight();
									SystemMessage sm;
									if (count > 1L)
									{
										sm = new SystemMessage(SystemMessageId.S1_X_S2_DISAPPEARED);
										sm.addItemName(removedItem);
										sm.addLong(count);
									}
									else
									{
										sm = new SystemMessage(SystemMessageId.S1_HAS_DISAPPEARED);
										sm.addItemName(removedItem);
									}

									player.sendPacket(sm);
								}
							}
						}
						else
						{
							if (itemToRemove.isHeroItem())
							{
								player.sendPacket(SystemMessageId.HERO_S_WEAPONS_CANNOT_BE_DESTROYED);
							}
							else
							{
								player.sendPacket(SystemMessageId.THE_ITEM_CANNOT_BE_DROPPED);
							}
						}
					}
				}
			}
		}
	}
}
