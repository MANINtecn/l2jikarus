package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.data.holders.ElementalItemHolder;
import net.sf.l2jdev.gameserver.data.xml.ElementalAttributeData;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.AttributeType;
import net.sf.l2jdev.gameserver.model.actor.request.EnchantItemAttributeRequest;
import net.sf.l2jdev.gameserver.model.item.enchant.attribute.AttributeHolder;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ExAttributeEnchantResult;
import net.sf.l2jdev.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class RequestExEnchantItemAttribute extends ClientPacket
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
			EnchantItemAttributeRequest request = player.getRequest(EnchantItemAttributeRequest.class);
			if (request != null)
			{
				request.setProcessing(true);
				if (this._objectId == -1)
				{
					player.removeRequest(request.getClass());
					player.sendPacket(SystemMessageId.ATTRIBUTE_ITEM_USAGE_HAS_BEEN_CANCELLED);
				}
				else if (!player.isOnline())
				{
					player.removeRequest(request.getClass());
				}
				else if (player.isInStoreMode())
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_ADD_ELEMENTAL_POWER_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
					player.removeRequest(request.getClass());
				}
				else if (player.getActiveRequester() != null)
				{
					player.cancelActiveTrade();
					player.removeRequest(request.getClass());
					player.sendPacket(SystemMessageId.YOU_CANNOT_DO_THAT_WHILE_TRADING);
				}
				else
				{
					Item item = player.getInventory().getItemByObjectId(this._objectId);
					Item stone = request.getEnchantingStone();
					if (item == null || stone == null)
					{
						player.removeRequest(request.getClass());
						player.sendPacket(SystemMessageId.ATTRIBUTE_ITEM_USAGE_HAS_BEEN_CANCELLED);
					}
					else if (!item.isElementable())
					{
						player.sendPacket(SystemMessageId.ELEMENTAL_POWER_ENHANCER_USAGE_REQUIREMENT_IS_NOT_SUFFICIENT);
						player.removeRequest(request.getClass());
					}
					else if (this._count < 1L)
					{
						player.removeRequest(request.getClass());
					}
					else
					{
						switch (item.getItemLocation())
						{
							case INVENTORY:
							case PAPERDOLL:
								if (item.getOwnerId() != player.getObjectId())
								{
									player.removeRequest(request.getClass());
									return;
								}
								int stoneId = stone.getId();
								long count = Math.min(stone.getCount(), this._count);
								AttributeType elementToAdd = ElementalAttributeData.getInstance().getItemElement(stoneId);
								if (item.isArmor())
								{
									elementToAdd = elementToAdd.getOpposite();
								}

								AttributeType opositeElement = elementToAdd.getOpposite();
								AttributeHolder oldElement = item.getAttribute(elementToAdd);
								int elementValue = oldElement == null ? 0 : oldElement.getValue();
								int limit = this.getLimit(item, stoneId);
								int powerToAdd = this.getPowerToAdd(stoneId, elementValue, item);
								if ((!item.isWeapon() || oldElement == null || oldElement.getType() == elementToAdd || oldElement.getType() == AttributeType.NONE) && (!item.isArmor() || item.getAttribute(elementToAdd) != null || item.getAttributes() == null || item.getAttributes().size() < 3))
								{
									if (item.isArmor() && item.getAttributes() != null)
									{
										for (AttributeHolder attribute : item.getAttributes())
										{
											if (attribute.getType() == opositeElement)
											{
												player.removeRequest(request.getClass());
												PunishmentManager.handleIllegalPlayerAction(player, player + " tried to add oposite attribute to item!", GeneralConfig.DEFAULT_PUNISH);
												return;
											}
										}
									}

									int newPower = elementValue + powerToAdd;
									if (newPower > limit)
									{
										powerToAdd = limit - elementValue;
									}

									if (powerToAdd <= 0)
									{
										player.sendPacket(SystemMessageId.ATTRIBUTE_ITEM_USAGE_HAS_BEEN_CANCELLED);
										player.removeRequest(request.getClass());
										return;
									}

									int usedStones = 0;
									int successfulAttempts = 0;
									int failedAttempts = 0;

									for (int i = 0; i < count; i++)
									{
										usedStones++;
										int result = this.addElement(player, stone, item, elementToAdd);
										if (result == 1)
										{
											successfulAttempts++;
										}
										else
										{
											if (result != 0)
											{
												break;
											}

											failedAttempts++;
										}
									}

									item.updateItemElementals();
									player.destroyItem(ItemProcessType.FEE, stone, usedStones, player, true);
									AttributeHolder newElement = item.getAttribute(elementToAdd);
									int newValue = newElement != null ? newElement.getValue() : 0;
									AttributeType realElement = item.isArmor() ? opositeElement : elementToAdd;
									InventoryUpdate iu = new InventoryUpdate();
									if (successfulAttempts > 0)
									{
										SystemMessage sm;
										if (item.getEnchantLevel() == 0)
										{
											if (item.isArmor())
											{
												sm = new SystemMessage(SystemMessageId.S1_ARE_NOW_IMBUED_WITH_S2_ATTRIBUTE_AND_YOUR_S3_RESISTANCE_HAS_INCREASED);
											}
											else
											{
												sm = new SystemMessage(SystemMessageId.S2_ATTRIBUTE_HAS_BEEN_ADDED_TO_S1);
											}

											sm.addItemName(item);
											sm.addAttribute(realElement.getClientId());
											if (item.isArmor())
											{
												sm.addAttribute(realElement.getOpposite().getClientId());
											}
										}
										else
										{
											if (item.isArmor())
											{
												sm = new SystemMessage(SystemMessageId.S3_POWER_HAS_BEEN_ADDED_TO_S1_S2_S4_RESISTANCE_IS_INCREASED);
											}
											else
											{
												sm = new SystemMessage(SystemMessageId.S1_S2_S3_ATTRIBUTE_POWER_IS_ENABLED);
											}

											sm.addInt(item.getEnchantLevel());
											sm.addItemName(item);
											sm.addAttribute(realElement.getClientId());
											if (item.isArmor())
											{
												sm.addAttribute(realElement.getOpposite().getClientId());
											}
										}

										player.sendPacket(sm);
										iu.addModifiedItem(item);
									}
									else
									{
										player.sendPacket(SystemMessageId.YOU_HAVE_FAILED_TO_ADD_ELEMENTAL_POWER);
									}

									int result = 0;
									if (successfulAttempts == 0)
									{
										result = 2;
									}

									if (stone.getCount() == 0L)
									{
										iu.addRemovedItem(stone);
									}
									else
									{
										iu.addModifiedItem(stone);
									}

									player.removeRequest(request.getClass());
									player.sendPacket(new ExAttributeEnchantResult(result, item.isWeapon(), elementToAdd, elementValue, newValue, successfulAttempts, failedAttempts));
									player.updateUserInfo();
									player.sendInventoryUpdate(iu);
									return;
								}

								player.sendPacket(SystemMessageId.ANOTHER_ELEMENTAL_POWER_HAS_ALREADY_BEEN_ADDED_THIS_ELEMENTAL_POWER_CANNOT_BE_ADDED);
								player.removeRequest(request.getClass());
								return;
							default:
								player.removeRequest(request.getClass());
								PunishmentManager.handleIllegalPlayerAction(player, player + " tried to use enchant Exploit!", GeneralConfig.DEFAULT_PUNISH);
						}
					}
				}
			}
		}
	}

	private int addElement(Player player, Item stone, Item item, AttributeType elementToAdd)
	{
		AttributeHolder oldElement = item.getAttribute(elementToAdd);
		int elementValue = oldElement == null ? 0 : oldElement.getValue();
		int limit = this.getLimit(item, stone.getId());
		int powerToAdd = this.getPowerToAdd(stone.getId(), elementValue, item);
		int newPower = elementValue + powerToAdd;
		if (newPower > limit)
		{
			newPower = limit;
			powerToAdd = limit - elementValue;
		}

		if (powerToAdd <= 0)
		{
			player.sendPacket(SystemMessageId.ATTRIBUTE_ITEM_USAGE_HAS_BEEN_CANCELLED);
			player.removeRequest(EnchantItemAttributeRequest.class);
			return -1;
		}
		boolean success = ElementalAttributeData.getInstance().isSuccess(item, stone.getId());
		if (success)
		{
			item.setAttribute(new AttributeHolder(elementToAdd, newPower), false);
		}

		return success ? 1 : 0;
	}

	public int getLimit(Item item, int sotneId)
	{
		ElementalItemHolder elementItem = ElementalAttributeData.getInstance().getItemElemental(sotneId);
		if (elementItem == null)
		{
			return 0;
		}
		return item.isWeapon() ? ElementalAttributeData.WEAPON_VALUES[elementItem.getType().getMaxLevel()] : ElementalAttributeData.ARMOR_VALUES[elementItem.getType().getMaxLevel()];
	}

	public int getPowerToAdd(int stoneId, int oldValue, Item item)
	{
		if (ElementalAttributeData.getInstance().getItemElement(stoneId) != AttributeType.NONE)
		{
			if (ElementalAttributeData.getInstance().getItemElemental(stoneId).getPower() > 0)
			{
				return ElementalAttributeData.getInstance().getItemElemental(stoneId).getPower();
			}

			if (item.isWeapon())
			{
				if (oldValue == 0)
				{
					return 20;
				}

				return 5;
			}

			if (item.isArmor())
			{
				return 6;
			}
		}

		return 0;
	}
}
