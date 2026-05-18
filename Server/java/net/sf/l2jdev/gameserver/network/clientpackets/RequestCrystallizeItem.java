package net.sf.l2jdev.gameserver.network.clientpackets;

import java.util.List;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.xml.ItemCrystallizationData;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Race;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemChanceHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.item.type.CrystalType;
import net.sf.l2jdev.gameserver.model.itemcontainer.PlayerInventory;
import net.sf.l2jdev.gameserver.model.skill.CommonSkill;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class RequestCrystallizeItem extends ClientPacket
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
				PunishmentManager.handleIllegalPlayerAction(player, "[RequestCrystallizeItem] count <= 0! ban! oid: " + this._objectId + " owner: " + player.getName(), GeneralConfig.DEFAULT_PUNISH);
			}
			else if (!player.isInStoreMode() && player.isInCrystallize())
			{
				int skillLevel = player.getSkillLevel(CommonSkill.CRYSTALLIZE.getId());
				if (skillLevel <= 0)
				{
					player.sendPacket(SystemMessageId.YOU_MAY_NOT_CRYSTALLIZE_THIS_ITEM_YOUR_CRYSTALLIZATION_SKILL_LEVEL_IS_TOO_LOW);
					player.sendPacket(ActionFailed.STATIC_PACKET);
					if (player.getRace() != Race.DWARF && player.getPlayerClass().getId() != 117 && player.getPlayerClass().getId() != 55)
					{
						PacketLogger.info(player + " used crystalize with classid: " + player.getPlayerClass().getId());
					}
				}
				else
				{
					PlayerInventory inventory = player.getInventory();
					if (inventory != null)
					{
						Item item = inventory.getItemByObjectId(this._objectId);
						if (item == null || item.isHeroItem() || !PlayerConfig.ALT_ALLOW_AUGMENT_DESTROY && item.isAugmented())
						{
							player.sendPacket(ActionFailed.STATIC_PACKET);
							return;
						}

						if (this._count > item.getCount())
						{
							this._count = player.getInventory().getItemByObjectId(this._objectId).getCount();
						}
					}

					Item itemToRemove = player.getInventory().getItemByObjectId(this._objectId);
					if (itemToRemove == null || itemToRemove.isShadowItem() || itemToRemove.isTimeLimitedItem())
					{
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
					else if (!itemToRemove.getTemplate().isCrystallizable() || itemToRemove.getTemplate().getCrystalCount() <= 0 || itemToRemove.getTemplate().getCrystalType() == CrystalType.NONE)
					{
						player.sendPacket(SystemMessageId.THIS_ITEM_CANNOT_BE_CRYSTALLIZED);
					}
					else if (!player.getInventory().canManipulateWithItemId(itemToRemove.getId()))
					{
						player.sendPacket(SystemMessageId.THIS_ITEM_CANNOT_BE_CRYSTALLIZED);
					}
					else
					{
						boolean canCrystallize = true;
						switch (itemToRemove.getTemplate().getCrystalTypePlus())
						{
							case D:
								if (skillLevel < 1)
								{
									canCrystallize = false;
								}
								break;
							case C:
								if (skillLevel < 2)
								{
									canCrystallize = false;
								}
								break;
							case B:
								if (skillLevel < 3)
								{
									canCrystallize = false;
								}
								break;
							case A:
								if (skillLevel < 4)
								{
									canCrystallize = false;
								}
								break;
							case S:
								if (skillLevel < 5)
								{
									canCrystallize = false;
								}
								break;
							case R:
								if (skillLevel < 6)
								{
									canCrystallize = false;
								}
						}

						if (!canCrystallize)
						{
							player.sendPacket(SystemMessageId.YOU_MAY_NOT_CRYSTALLIZE_THIS_ITEM_YOUR_CRYSTALLIZATION_SKILL_LEVEL_IS_TOO_LOW);
							player.sendPacket(ActionFailed.STATIC_PACKET);
						}
						else
						{
							List<ItemChanceHolder> crystallizationRewards = ItemCrystallizationData.getInstance().getCrystallizationRewards(itemToRemove);
							if (crystallizationRewards != null && !crystallizationRewards.isEmpty())
							{
								if (itemToRemove.isEquipped())
								{
									InventoryUpdate iu = new InventoryUpdate();

									for (Item itemx : player.getInventory().unEquipItemInSlotAndRecord(itemToRemove.getLocationSlot()))
									{
										iu.addModifiedItem(itemx);
									}

									player.sendPacket(iu);
									SystemMessage sm;
									if (itemToRemove.getEnchantLevel() > 0)
									{
										sm = new SystemMessage(SystemMessageId.S1_S2_UNEQUIPPED);
										sm.addInt(itemToRemove.getEnchantLevel());
										sm.addItemName(itemToRemove);
									}
									else
									{
										sm = new SystemMessage(SystemMessageId.S1_UNEQUIPPED);
										sm.addItemName(itemToRemove);
									}

									player.sendPacket(sm);
								}

								Item removedItem = player.getInventory().destroyItem(ItemProcessType.DESTROY, this._objectId, this._count, player, null);
								InventoryUpdate iu = new InventoryUpdate();
								iu.addRemovedItem(removedItem);
								player.sendPacket(iu);
								player.updateAdenaAndWeight();

								for (ItemChanceHolder holder : crystallizationRewards)
								{
									double rand = Rnd.nextDouble() * 100.0;
									if (rand < holder.getChance())
									{
										Item createdItem = player.getInventory().addItem(ItemProcessType.COMPENSATE, holder.getId(), holder.getCount(), player, player);
										SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1_X_S2);
										sm.addItemName(createdItem);
										sm.addLong(holder.getCount());
										player.sendPacket(sm);
									}
								}

								SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_BEEN_CRYSTALLIZED);
								sm.addItemName(removedItem);
								player.sendPacket(sm);
								player.broadcastUserInfo();
								player.setInCrystallize(false);
							}
							else
							{
								player.sendPacket(SystemMessageId.ANGEL_NEVIT_S_DESCENT_BONUS_TIME_S1);
							}
						}
					}
				}
			}
			else
			{
				player.sendPacket(SystemMessageId.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			}
		}
	}
}
