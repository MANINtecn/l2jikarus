package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.data.xml.VariationData;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ExVariationCancelResult;
import net.sf.l2jdev.gameserver.network.serverpackets.InventoryUpdate;

public class RequestRefineCancel extends ClientPacket
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
			Item targetItem = player.getInventory().getItemByObjectId(this._targetItemObjId);
			if (targetItem == null)
			{
				player.sendPacket(ExVariationCancelResult.STATIC_PACKET_FAILURE);
			}
			else if (targetItem.getOwnerId() != player.getObjectId())
			{
				PunishmentManager.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tryied to augment item that doesn't own.", GeneralConfig.DEFAULT_PUNISH);
			}
			else if (!targetItem.isAugmented())
			{
				player.sendPacket(SystemMessageId.AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM);
				player.sendPacket(ExVariationCancelResult.STATIC_PACKET_FAILURE);
			}
			else
			{
				long price = VariationData.getInstance().getCancelFee(targetItem.getId(), targetItem.getAugmentation().getMineralId());
				if (price < 0L)
				{
					player.sendPacket(ExVariationCancelResult.STATIC_PACKET_FAILURE);
				}
				else if (!player.reduceAdena(ItemProcessType.FEE, price, targetItem, true))
				{
					player.sendPacket(ExVariationCancelResult.STATIC_PACKET_FAILURE);
					player.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA);
				}
				else
				{
					InventoryUpdate iu = new InventoryUpdate();
					if (targetItem.isEquipped())
					{
						for (Item itm : player.getInventory().unEquipItemInSlotAndRecord(targetItem.getLocationSlot()))
						{
							iu.addModifiedItem(itm);
						}
					}

					targetItem.removeAugmentation();
					player.sendPacket(ExVariationCancelResult.STATIC_PACKET_SUCCESS);
					iu.addModifiedItem(targetItem);
					player.sendInventoryUpdate(iu);
				}
			}
		}
	}
}
