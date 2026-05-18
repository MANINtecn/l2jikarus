package net.sf.l2jdev.gameserver.network.clientpackets.appearance;

import net.sf.l2jdev.gameserver.data.xml.AppearanceItemData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.ShapeShiftingItemRequest;
import net.sf.l2jdev.gameserver.model.item.appearance.AppearanceHolder;
import net.sf.l2jdev.gameserver.model.item.appearance.AppearanceStone;
import net.sf.l2jdev.gameserver.model.item.appearance.AppearanceType;
import net.sf.l2jdev.gameserver.model.item.enums.BodyPart;
import net.sf.l2jdev.gameserver.model.item.enums.ItemLocation;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.PlayerInventory;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.enums.InventorySlot;
import net.sf.l2jdev.gameserver.network.serverpackets.ExAdenaInvenCount;
import net.sf.l2jdev.gameserver.network.serverpackets.ExUserInfoEquipSlot;
import net.sf.l2jdev.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.appearance.ExShapeShiftingResult;

public class RequestShapeShiftingItem extends ClientPacket
{
	private int _targetItemObjId;

	@Override
	protected void readImpl()
	{
		this._targetItemObjId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			ShapeShiftingItemRequest request = player.getRequest(ShapeShiftingItemRequest.class);
			if (request != null && !player.isInStoreMode() && !player.isCrafting() && !player.isProcessingRequest() && !player.isProcessingTransaction())
			{
				PlayerInventory inventory = player.getInventory();
				Item targetItem = inventory.getItemByObjectId(this._targetItemObjId);
				Item stone = request.getAppearanceStone();
				if (targetItem != null && stone != null)
				{
					if (stone.getOwnerId() != player.getObjectId() || targetItem.getOwnerId() != player.getObjectId())
					{
						player.sendPacket(ExShapeShiftingResult.CLOSE);
						player.removeRequest(ShapeShiftingItemRequest.class);
					}
					else if (!targetItem.getTemplate().isAppearanceable())
					{
						player.sendPacket(SystemMessageId.THIS_ITEM_CANNOT_BE_MODIFIED_OR_RESTORED);
						player.sendPacket(ExShapeShiftingResult.CLOSE);
						player.removeRequest(ShapeShiftingItemRequest.class);
					}
					else if (targetItem.getItemLocation() != ItemLocation.INVENTORY && targetItem.getItemLocation() != ItemLocation.PAPERDOLL)
					{
						player.sendPacket(ExShapeShiftingResult.CLOSE);
						player.removeRequest(ShapeShiftingItemRequest.class);
					}
					else if ((stone = inventory.getItemByObjectId(stone.getObjectId())) == null)
					{
						player.sendPacket(ExShapeShiftingResult.CLOSE);
						player.removeRequest(ShapeShiftingItemRequest.class);
					}
					else
					{
						AppearanceStone appearanceStone = AppearanceItemData.getInstance().getStone(stone.getId());
						if (appearanceStone == null)
						{
							player.sendPacket(ExShapeShiftingResult.CLOSE);
							player.removeRequest(ShapeShiftingItemRequest.class);
						}
						else if (!appearanceStone.checkConditions(player, targetItem))
						{
							player.sendPacket(ExShapeShiftingResult.CLOSE);
							player.removeRequest(ShapeShiftingItemRequest.class);
						}
						else
						{
							Item extractItem = request.getAppearanceExtractItem();
							int extracItemId = 0;
							if (appearanceStone.getType() != AppearanceType.RESTORE && appearanceStone.getType() != AppearanceType.FIXED)
							{
								if ((extractItem == null) || (extractItem.getOwnerId() != player.getObjectId()) || !extractItem.getTemplate().isAppearanceable())
								{
									player.sendPacket(ExShapeShiftingResult.CLOSE);
									player.removeRequest(ShapeShiftingItemRequest.class);
									return;
								}

								if (extractItem.getItemLocation() != ItemLocation.INVENTORY && extractItem.getItemLocation() != ItemLocation.PAPERDOLL)
								{
									player.sendPacket(ExShapeShiftingResult.CLOSE);
									player.removeRequest(ShapeShiftingItemRequest.class);
									return;
								}

								if (extractItem.getTemplate().getCrystalType().isGreater(targetItem.getTemplate().getCrystalType()))
								{
									player.sendPacket(ExShapeShiftingResult.CLOSE);
									player.removeRequest(ShapeShiftingItemRequest.class);
									return;
								}

								if (extractItem.getVisualId() > 0)
								{
									player.sendPacket(ExShapeShiftingResult.CLOSE);
									player.removeRequest(ShapeShiftingItemRequest.class);
									return;
								}

								if (extractItem.getItemType() != targetItem.getItemType() || extractItem.getId() == targetItem.getId() || extractItem.getObjectId() == targetItem.getObjectId())
								{
									player.sendPacket(ExShapeShiftingResult.CLOSE);
									player.removeRequest(ShapeShiftingItemRequest.class);
									return;
								}

								if (extractItem.getTemplate().getBodyPart() != targetItem.getTemplate().getBodyPart() && (extractItem.getTemplate().getBodyPart() != BodyPart.FULL_ARMOR || targetItem.getTemplate().getBodyPart() != BodyPart.CHEST))
								{
									player.sendPacket(ExShapeShiftingResult.CLOSE);
									player.removeRequest(ShapeShiftingItemRequest.class);
									return;
								}

								extracItemId = extractItem.getId();
							}

							long cost = appearanceStone.getCost();
							if (cost > player.getAdena())
							{
								player.sendPacket(SystemMessageId.YOU_CANNOT_MODIFY_AS_YOU_DO_NOT_HAVE_ENOUGH_ADENA);
								player.sendPacket(ExShapeShiftingResult.CLOSE);
								player.removeRequest(ShapeShiftingItemRequest.class);
							}
							else if (stone.getCount() < 1L)
							{
								player.sendPacket(ExShapeShiftingResult.CLOSE);
								player.removeRequest(ShapeShiftingItemRequest.class);
							}
							else if (appearanceStone.getType() == AppearanceType.NORMAL && inventory.destroyItem(ItemProcessType.FEE, extractItem, 1L, player, this) == null)
							{
								player.sendPacket(ExShapeShiftingResult.FAILED);
								player.removeRequest(ShapeShiftingItemRequest.class);
							}
							else
							{
								inventory.destroyItem(ItemProcessType.FEE, stone, 1L, player, this);
								player.reduceAdena(ItemProcessType.FEE, cost, extractItem, false);
								switch (appearanceStone.getType())
								{
									case RESTORE:
										targetItem.setVisualId(0);
										targetItem.getVariables().set("visualAppearanceStoneId", 0);
										break;
									case NORMAL:
										targetItem.setVisualId(extractItem.getId());
										break;
									case BLESSED:
										targetItem.setVisualId(extractItem.getId());
										break;
									case FIXED:
										targetItem.removeVisualSetSkills();
										if (appearanceStone.getVisualIds().isEmpty())
										{
											extracItemId = appearanceStone.getVisualId();
											targetItem.setVisualId(appearanceStone.getVisualId());
											targetItem.getVariables().set("visualAppearanceStoneId", appearanceStone.getId());
										}
										else
										{
											AppearanceHolder holder = appearanceStone.findVisualChange(targetItem);
											if (holder != null)
											{
												extracItemId = holder.getVisualId();
												targetItem.setVisualId(holder.getVisualId());
												targetItem.getVariables().set("visualAppearanceStoneId", appearanceStone.getId());
											}
										}

										targetItem.applyVisualSetSkills();
								}

								if (appearanceStone.getType() != AppearanceType.RESTORE && appearanceStone.getLifeTime() > 0L)
								{
									targetItem.getVariables().set("visualAppearanceLifetime", System.currentTimeMillis() + appearanceStone.getLifeTime());
									targetItem.scheduleVisualLifeTime();
								}

								targetItem.getVariables().storeMe();
								InventoryUpdate iu = new InventoryUpdate();
								iu.addModifiedItem(targetItem);
								if (extractItem != null)
								{
									iu.addModifiedItem(extractItem);
								}

								if (inventory.getItemByObjectId(stone.getObjectId()) == null)
								{
									iu.addRemovedItem(stone);
								}
								else
								{
									iu.addModifiedItem(stone);
								}

								player.sendInventoryUpdate(iu);
								player.removeRequest(ShapeShiftingItemRequest.class);
								player.sendPacket(new ExShapeShiftingResult(1, targetItem.getId(), extracItemId));
								if (targetItem.isEquipped())
								{
									player.broadcastUserInfo();
									ExUserInfoEquipSlot slots = new ExUserInfoEquipSlot(player, false);

									for (InventorySlot slot : InventorySlot.values())
									{
										if (slot.getSlot() == targetItem.getLocationSlot())
										{
											slots.addComponentType(slot);
										}
									}

									player.sendPacket(slots);
								}

								player.sendPacket(new ExAdenaInvenCount(player));
							}
						}
					}
				}
				else
				{
					player.sendPacket(ExShapeShiftingResult.CLOSE);
					player.removeRequest(ShapeShiftingItemRequest.class);
				}
			}
			else
			{
				player.sendPacket(ExShapeShiftingResult.CLOSE);
				player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THIS_SYSTEM_DURING_TRADING_PRIVATE_STORE_AND_WORKSHOP_SETUP);
			}
		}
	}
}
