package net.sf.l2jdev.gameserver.network.clientpackets.equipmentupgradenormal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.data.holders.EquipmentUpgradeNormalHolder;
import net.sf.l2jdev.gameserver.data.xml.EquipmentUpgradeNormalData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.enums.UpgradeDataType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemEnchantHolder;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.item.holders.UniqueItemEnchantHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.Inventory;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.equipmentupgradenormal.ExUpgradeSystemNormalResult;

public class ExUpgradeSystemNormalRequest extends ClientPacket
{
	private int _objectId;
	private int _typeId;
	private int _upgradeId;
	private final List<UniqueItemEnchantHolder> _resultItems = new ArrayList<>();
	private final List<UniqueItemEnchantHolder> _bonusItems = new ArrayList<>();
	private final Map<Integer, Long> _discount = new HashMap<>();
	private boolean isNeedToSendUpdate = false;

	@Override
	protected void readImpl()
	{
		this._objectId = this.readInt();
		this._typeId = this.readInt();
		this._upgradeId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Item requestedItem = player.getInventory().getItemByObjectId(this._objectId);
			if (requestedItem == null)
			{
				player.sendPacket(ExUpgradeSystemNormalResult.FAIL);
			}
			else
			{
				EquipmentUpgradeNormalHolder upgradeHolder = EquipmentUpgradeNormalData.getInstance().getUpgrade(this._upgradeId);
				if (upgradeHolder != null && upgradeHolder.getType() == this._typeId)
				{
					Inventory inventory = player.getInventory();
					if (inventory.getItemByItemId(upgradeHolder.getInitialItem().getId()) != null && inventory.getInventoryItemCount(upgradeHolder.getInitialItem().getId(), -1) >= upgradeHolder.getInitialItem().getCount())
					{
						if (upgradeHolder.isHasCategory(UpgradeDataType.MATERIAL))
						{
							for (ItemEnchantHolder material : upgradeHolder.getItems(UpgradeDataType.MATERIAL))
							{
								if (material.getCount() < 0L)
								{
									player.sendPacket(ExUpgradeSystemNormalResult.FAIL);
									PacketLogger.warning(this.getClass().getSimpleName() + ": material -> item -> count in file EquipmentUpgradeNormalData.xml for upgrade id " + upgradeHolder.getId() + " cannot be less than 0! Aborting current request!");
									return;
								}

								if (inventory.getInventoryItemCount(material.getId(), material.getEnchantLevel()) < material.getCount())
								{
									player.sendPacket(ExUpgradeSystemNormalResult.FAIL);
									return;
								}

								for (ItemHolder discount : EquipmentUpgradeNormalData.getInstance().getDiscount())
								{
									if (discount.getId() == material.getId())
									{
										this._discount.put(material.getId(), discount.getCount());
										break;
									}
								}
							}
						}

						long adena = upgradeHolder.getCommission();
						if (adena > 0L && inventory.getAdena() < adena)
						{
							player.sendPacket(ExUpgradeSystemNormalResult.FAIL);
						}
						else
						{
							player.destroyItem(ItemProcessType.FEE, this._objectId, 1L, player, true);
							if (upgradeHolder.isHasCategory(UpgradeDataType.MATERIAL))
							{
								for (ItemHolder material : upgradeHolder.getItems(UpgradeDataType.MATERIAL))
								{
									player.destroyItemByItemId(ItemProcessType.FEE, material.getId(), material.getCount() - (this._discount.isEmpty() ? 0L : this._discount.get(material.getId())), player, true);
								}
							}

							if (adena > 0L)
							{
								player.reduceAdena(ItemProcessType.FEE, adena, player, true);
							}

							if (Rnd.get(100.0) < upgradeHolder.getChance())
							{
								for (ItemEnchantHolder successItem : upgradeHolder.getItems(UpgradeDataType.ON_SUCCESS))
								{
									Item addedSuccessItem = player.addItem(ItemProcessType.REWARD, successItem.getId(), successItem.getCount(), player, true);
									if (successItem.getEnchantLevel() != 0)
									{
										this.isNeedToSendUpdate = true;
										addedSuccessItem.setEnchantLevel(successItem.getEnchantLevel());
									}

									addedSuccessItem.updateDatabase(true);
									this._resultItems.add(new UniqueItemEnchantHolder(successItem, addedSuccessItem.getObjectId()));
								}

								if (upgradeHolder.isHasCategory(UpgradeDataType.BONUS_TYPE) && Rnd.get(100.0) < upgradeHolder.getChanceToReceiveBonusItems())
								{
									for (ItemEnchantHolder bonusItem : upgradeHolder.getItems(UpgradeDataType.BONUS_TYPE))
									{
										Item addedBonusItem = player.addItem(ItemProcessType.REWARD, bonusItem.getId(), bonusItem.getCount(), player, true);
										if (bonusItem.getEnchantLevel() != 0)
										{
											this.isNeedToSendUpdate = true;
											addedBonusItem.setEnchantLevel(bonusItem.getEnchantLevel());
										}

										addedBonusItem.updateDatabase(true);
										this._bonusItems.add(new UniqueItemEnchantHolder(bonusItem, addedBonusItem.getObjectId()));
									}
								}
							}
							else if (upgradeHolder.isHasCategory(UpgradeDataType.ON_FAILURE))
							{
								for (ItemEnchantHolder failureItem : upgradeHolder.getItems(UpgradeDataType.ON_FAILURE))
								{
									Item addedFailureItem = player.addItem(ItemProcessType.COMPENSATE, failureItem.getId(), failureItem.getCount(), player, true);
									if (failureItem.getEnchantLevel() != 0)
									{
										this.isNeedToSendUpdate = true;
										addedFailureItem.setEnchantLevel(failureItem.getEnchantLevel());
									}

									addedFailureItem.updateDatabase(true);
									this._resultItems.add(new UniqueItemEnchantHolder(failureItem, addedFailureItem.getObjectId()));
								}
							}
							else
							{
								player.sendPacket(ExUpgradeSystemNormalResult.FAIL);
							}

							if (this.isNeedToSendUpdate)
							{
								player.sendItemList();
							}

							player.sendPacket(new ExUpgradeSystemNormalResult(1, this._typeId, true, this._resultItems, this._bonusItems));
						}
					}
					else
					{
						player.sendPacket(ExUpgradeSystemNormalResult.FAIL);
					}
				}
				else
				{
					player.sendPacket(ExUpgradeSystemNormalResult.FAIL);
				}
			}
		}
	}
}
