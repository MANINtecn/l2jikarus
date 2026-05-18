package net.sf.l2jdev.gameserver.network.clientpackets.equipmentupgrade;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.data.holders.EquipmentUpgradeHolder;
import net.sf.l2jdev.gameserver.data.xml.EquipmentUpgradeData;
import net.sf.l2jdev.gameserver.model.ItemInfo;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.AttributeType;
import net.sf.l2jdev.gameserver.model.ensoul.EnsoulOption;
import net.sf.l2jdev.gameserver.model.item.enchant.attribute.AttributeHolder;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.variables.ItemVariables;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.ExItemAnnounce;
import net.sf.l2jdev.gameserver.network.serverpackets.equipmentupgrade.ExUpgradeSystemResult;
import net.sf.l2jdev.gameserver.util.Broadcast;

public class RequestUpgradeSystemResult extends ClientPacket
{
	private int _objectId;
	private int _upgradeId;

	@Override
	protected void readImpl()
	{
		this._objectId = this.readInt();
		this._upgradeId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Item existingItem = player.getInventory().getItemByObjectId(this._objectId);
			if (existingItem == null)
			{
				player.sendPacket(new ExUpgradeSystemResult(0, 0));
			}
			else
			{
				EquipmentUpgradeHolder upgradeHolder = EquipmentUpgradeData.getInstance().getUpgrade(this._upgradeId);
				if (upgradeHolder == null)
				{
					player.sendPacket(new ExUpgradeSystemResult(0, 0));
				}
				else
				{
					for (ItemHolder material : upgradeHolder.getMaterials())
					{
						if (player.getInventory().getInventoryItemCount(material.getId(), -1) < material.getCount())
						{
							player.sendPacket(new ExUpgradeSystemResult(0, 0));
							return;
						}
					}

					long adena = upgradeHolder.getAdena();
					if (adena > 0L && player.getAdena() < adena)
					{
						player.sendPacket(new ExUpgradeSystemResult(0, 0));
					}
					else if (existingItem.getTemplate().getId() == upgradeHolder.getRequiredItemId() && existingItem.getEnchantLevel() == upgradeHolder.getRequiredItemEnchant())
					{
						ItemInfo itemEnchantment = new ItemInfo(existingItem);
						player.destroyItem(ItemProcessType.FEE, this._objectId, 1L, player, true);

						for (ItemHolder materialx : upgradeHolder.getMaterials())
						{
							player.destroyItemByItemId(ItemProcessType.FEE, materialx.getId(), materialx.getCount(), player, true);
						}

						if (adena > 0L)
						{
							player.reduceAdena(ItemProcessType.FEE, adena, player, true);
						}

						Item addedItem = player.addItem(ItemProcessType.REWARD, upgradeHolder.getResultItemId(), 1L, player, true);
						if (upgradeHolder.isAnnounce())
						{
							Broadcast.toAllOnlinePlayers(new ExItemAnnounce(player, addedItem, 10));
						}

						if (addedItem.isEquipable())
						{
							addedItem.setAugmentation(itemEnchantment.getAugmentation(), false);
							if (addedItem.isWeapon() && addedItem.getTemplate().getAttributes() == null)
							{
								if (itemEnchantment.getAttackElementPower() > 0)
								{
									addedItem.setAttribute(new AttributeHolder(AttributeType.findByClientId(itemEnchantment.getAttackElementType()), itemEnchantment.getAttackElementPower()), false);
								}
							}
							else if (addedItem.getTemplate().getAttributes() == null)
							{
								if (itemEnchantment.getAttributeDefence(AttributeType.FIRE) > 0)
								{
									addedItem.setAttribute(new AttributeHolder(AttributeType.FIRE, itemEnchantment.getAttributeDefence(AttributeType.FIRE)), false);
								}

								if (itemEnchantment.getAttributeDefence(AttributeType.WATER) > 0)
								{
									addedItem.setAttribute(new AttributeHolder(AttributeType.WATER, itemEnchantment.getAttributeDefence(AttributeType.WATER)), false);
								}

								if (itemEnchantment.getAttributeDefence(AttributeType.WIND) > 0)
								{
									addedItem.setAttribute(new AttributeHolder(AttributeType.WIND, itemEnchantment.getAttributeDefence(AttributeType.WIND)), false);
								}

								if (itemEnchantment.getAttributeDefence(AttributeType.EARTH) > 0)
								{
									addedItem.setAttribute(new AttributeHolder(AttributeType.EARTH, itemEnchantment.getAttributeDefence(AttributeType.EARTH)), false);
								}

								if (itemEnchantment.getAttributeDefence(AttributeType.HOLY) > 0)
								{
									addedItem.setAttribute(new AttributeHolder(AttributeType.HOLY, itemEnchantment.getAttributeDefence(AttributeType.HOLY)), false);
								}

								if (itemEnchantment.getAttributeDefence(AttributeType.DARK) > 0)
								{
									addedItem.setAttribute(new AttributeHolder(AttributeType.DARK, itemEnchantment.getAttributeDefence(AttributeType.DARK)), false);
								}
							}

							if (itemEnchantment.getSoulCrystalOptions() != null)
							{
								int pos = -1;

								for (EnsoulOption ensoul : itemEnchantment.getSoulCrystalOptions())
								{
									addedItem.addSpecialAbility(ensoul, ++pos, 1, false);
								}
							}

							if (itemEnchantment.getSoulCrystalSpecialOptions() != null)
							{
								for (EnsoulOption ensoul : itemEnchantment.getSoulCrystalSpecialOptions())
								{
									addedItem.addSpecialAbility(ensoul, 0, 2, false);
								}
							}

							if (itemEnchantment.getVisualId() > 0)
							{
								ItemVariables oldVars = existingItem.getVariables();
								ItemVariables newVars = addedItem.getVariables();
								newVars.set("visualId", oldVars.getInt("visualId", 0));
								newVars.set("visualAppearanceStoneId", oldVars.getInt("visualAppearanceStoneId", 0));
								newVars.set("visualAppearanceLifetime", oldVars.getLong("visualAppearanceLifetime", 0L));
								newVars.storeMe();
								addedItem.scheduleVisualLifeTime();
							}
						}

						int enchantLevel = upgradeHolder.getResultItemEnchant();
						if (enchantLevel > 0)
						{
							addedItem.setEnchantLevel(enchantLevel);
						}

						addedItem.updateDatabase(true);
						ThreadPool.schedule(() -> player.sendPacket(new ExUpgradeSystemResult(addedItem.getObjectId(), 1)), 500L);
						player.sendItemList();
					}
					else
					{
						player.sendPacket(new ExUpgradeSystemResult(0, 0));
					}
				}
			}
		}
	}
}
