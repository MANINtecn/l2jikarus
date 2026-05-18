package net.sf.l2jdev.gameserver.network.clientpackets.appearance;

import net.sf.l2jdev.gameserver.data.xml.AppearanceItemData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.ShapeShiftingItemRequest;
import net.sf.l2jdev.gameserver.model.item.appearance.AppearanceStone;
import net.sf.l2jdev.gameserver.model.item.appearance.AppearanceType;
import net.sf.l2jdev.gameserver.model.item.enums.BodyPart;
import net.sf.l2jdev.gameserver.model.item.enums.ItemLocation;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.PlayerInventory;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.appearance.ExPutShapeShiftingExtractionItemResult;
import net.sf.l2jdev.gameserver.network.serverpackets.appearance.ExPutShapeShiftingTargetItemResult;

public class RequestExTryToPutShapeShiftingEnchantSupportItem extends ClientPacket
{
	private int _targetItemObjId;
	private int _extracItemObjId;

	@Override
	protected void readImpl()
	{
		this._targetItemObjId = this.readInt();
		this._extracItemObjId = this.readInt();
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
				Item extractItem = inventory.getItemByObjectId(this._extracItemObjId);
				Item stone = request.getAppearanceStone();
				if (targetItem != null && extractItem != null && stone != null)
				{
					if (stone.getOwnerId() != player.getObjectId() || targetItem.getOwnerId() != player.getObjectId() || extractItem.getOwnerId() != player.getObjectId())
					{
						player.removeRequest(ShapeShiftingItemRequest.class);
					}
					else if (!extractItem.getTemplate().isAppearanceable())
					{
						player.sendPacket(SystemMessageId.THIS_ITEM_DOES_NOT_MEET_REQUIREMENTS);
						player.sendPacket(ExPutShapeShiftingExtractionItemResult.FAILED);
					}
					else if (extractItem.getItemLocation() != ItemLocation.INVENTORY && extractItem.getItemLocation() != ItemLocation.PAPERDOLL)
					{
						player.removeRequest(ShapeShiftingItemRequest.class);
					}
					else if ((stone = inventory.getItemByObjectId(stone.getObjectId())) == null)
					{
						player.removeRequest(ShapeShiftingItemRequest.class);
					}
					else
					{
						AppearanceStone appearanceStone = AppearanceItemData.getInstance().getStone(stone.getId());
						if (appearanceStone == null)
						{
							player.removeRequest(ShapeShiftingItemRequest.class);
						}
						else if (appearanceStone.getType() == AppearanceType.RESTORE || appearanceStone.getType() == AppearanceType.FIXED)
						{
							player.removeRequest(ShapeShiftingItemRequest.class);
						}
						else if (extractItem.getVisualId() > 0)
						{
							player.sendPacket(ExPutShapeShiftingExtractionItemResult.FAILED);
							player.sendPacket(SystemMessageId.YOU_CANNOT_EXTRACT_FROM_A_MODIFIED_ITEM);
							player.removeRequest(ShapeShiftingItemRequest.class);
						}
						else if (extractItem.getItemLocation() != ItemLocation.INVENTORY && extractItem.getItemLocation() != ItemLocation.PAPERDOLL)
						{
							player.sendPacket(ExPutShapeShiftingExtractionItemResult.FAILED);
							player.removeRequest(ShapeShiftingItemRequest.class);
						}
						else if (extractItem.getItemType() == targetItem.getItemType() && extractItem.getId() != targetItem.getId() && extractItem.getObjectId() != targetItem.getObjectId())
						{
							if (extractItem.getTemplate().getBodyPart() == targetItem.getTemplate().getBodyPart() || extractItem.getTemplate().getBodyPart() == BodyPart.FULL_ARMOR && targetItem.getTemplate().getBodyPart() == BodyPart.CHEST)
							{
								if (extractItem.getTemplate().getCrystalType().isGreater(targetItem.getTemplate().getCrystalType()))
								{
									player.sendPacket(SystemMessageId.YOU_CANNOT_EXTRACT_FROM_ITEMS_THAT_ARE_HIGHER_GRADE_THAN_ITEMS_TO_BE_MODIFIED);
									player.sendPacket(ExPutShapeShiftingExtractionItemResult.FAILED);
								}
								else if (!appearanceStone.checkConditions(player, targetItem))
								{
									player.sendPacket(ExPutShapeShiftingTargetItemResult.FAILED);
								}
								else
								{
									request.setAppearanceExtractItem(extractItem);
									player.sendPacket(ExPutShapeShiftingExtractionItemResult.SUCCESS);
								}
							}
							else
							{
								player.sendPacket(SystemMessageId.THIS_ITEM_DOES_NOT_MEET_REQUIREMENTS);
								player.sendPacket(ExPutShapeShiftingExtractionItemResult.FAILED);
							}
						}
						else
						{
							player.sendPacket(SystemMessageId.THIS_ITEM_DOES_NOT_MEET_REQUIREMENTS);
							player.sendPacket(ExPutShapeShiftingExtractionItemResult.FAILED);
						}
					}
				}
				else
				{
					player.removeRequest(ShapeShiftingItemRequest.class);
				}
			}
			else
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THIS_SYSTEM_DURING_TRADING_PRIVATE_STORE_AND_WORKSHOP_SETUP);
			}
		}
	}
}
