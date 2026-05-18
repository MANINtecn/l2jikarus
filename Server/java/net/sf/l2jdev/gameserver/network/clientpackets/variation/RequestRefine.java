package net.sf.l2jdev.gameserver.network.clientpackets.variation;

import net.sf.l2jdev.gameserver.data.xml.VariationData;
import net.sf.l2jdev.gameserver.model.VariationInstance;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.options.Variation;
import net.sf.l2jdev.gameserver.model.options.VariationFee;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.AbstractRefinePacket;
import net.sf.l2jdev.gameserver.network.serverpackets.ExVariationResult;
import net.sf.l2jdev.gameserver.network.serverpackets.InventoryUpdate;

public class RequestRefine extends AbstractRefinePacket
{
	private int _targetItemObjId;
	private int _mineralItemObjId;

	@Override
	protected void readImpl()
	{
		this._targetItemObjId = this.readInt();
		this._mineralItemObjId = this.readInt();
		this.readByte();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Item targetItem = player.getInventory().getItemByObjectId(this._targetItemObjId);
			if (targetItem != null)
			{
				Item mineralItem = player.getInventory().getItemByObjectId(this._mineralItemObjId);
				if (mineralItem != null)
				{
					VariationFee fee = VariationData.getInstance().getFee(targetItem.getId(), mineralItem.getId());
					if (fee != null)
					{
						Item feeItem = player.getInventory().getItemByItemId(fee.getItemId());
						if (feeItem == null && fee.getItemId() != 0)
						{
							PacketLogger.warning(this.getClass().getSimpleName() + ": " + player.getName() + " does not have required fee item (ID: " + fee.getItemId() + ") for mineral ID: " + mineralItem.getId());
							player.sendPacket(ExVariationResult.FAIL);
							player.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
						}
						else if (!isValid(player, targetItem, mineralItem, feeItem, fee))
						{
							player.sendPacket(ExVariationResult.FAIL);
							player.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
						}
						else if (fee.getAdenaFee() <= 0L)
						{
							player.sendPacket(ExVariationResult.FAIL);
							player.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
						}
						else
						{
							long adenaFee = fee.getAdenaFee();
							if (adenaFee > 0L && player.getAdena() < adenaFee)
							{
								player.sendPacket(ExVariationResult.FAIL);
								player.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
							}
							else
							{
								Variation variation = VariationData.getInstance().getVariation(mineralItem.getId(), targetItem);
								if (variation == null)
								{
									player.sendPacket(ExVariationResult.FAIL);
								}
								else
								{
									VariationInstance augment = VariationData.getInstance().generateRandomVariation(variation, targetItem);
									if (augment == null)
									{
										player.sendPacket(ExVariationResult.FAIL);
									}
									else
									{
										int option1 = augment.getOption1Id();
										int option2 = augment.getOption2Id();
										int option3 = augment.getOption3Id();
										VariationInstance oldAugment = targetItem.getAugmentation();
										if (oldAugment != null)
										{
											int newOption1 = option1 > 0 ? option1 : 0;
											int newOption2 = option2 > 0 ? option2 : 0;
											int newOption3 = option3 > 0 ? option3 : 0;
											augment = new VariationInstance(augment.getMineralId(), newOption1, newOption2, newOption3);
										}

										targetItem.setAugmentation(augment, true);
										InventoryUpdate iu = new InventoryUpdate();
										iu.addModifiedItem(targetItem);
										player.sendInventoryUpdate(iu);
										player.sendPacket(new ExVariationResult(augment.getOption1Id(), augment.getOption2Id(), augment.getOption3Id(), true));
										player.destroyItem(ItemProcessType.FEE, mineralItem, 1L, null, false);
										if (feeItem != null)
										{
											player.destroyItem(ItemProcessType.FEE, feeItem, fee.getItemCount(), null, false);
										}

										if (adenaFee > 0L)
										{
											player.reduceAdena(ItemProcessType.FEE, adenaFee, player, false);
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
}
