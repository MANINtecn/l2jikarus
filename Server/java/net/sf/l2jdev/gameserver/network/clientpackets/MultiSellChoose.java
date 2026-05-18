package net.sf.l2jdev.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.data.holders.MultisellEntryHolder;
import net.sf.l2jdev.gameserver.data.holders.PreparedMultisellListHolder;
import net.sf.l2jdev.gameserver.data.xml.EnsoulData;
import net.sf.l2jdev.gameserver.data.xml.ItemData;
import net.sf.l2jdev.gameserver.data.xml.MultisellData;
import net.sf.l2jdev.gameserver.model.ItemInfo;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.AttributeType;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.ensoul.EnsoulOption;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.item.OnMultisellBuyItem;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.enchant.attribute.AttributeHolder;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.enums.SpecialItemType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemChanceHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.PlayerInventory;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ExMultiSellResult;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPCCafePointInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.util.ArrayUtil;

public class MultiSellChoose extends ClientPacket
{
	private int _listId;
	private int _entryId;
	private long _amount;
	private int _enchantLevel;
	private int _augmentOption1;
	private int _augmentOption2;
	private int _augmentOption3;
	private short _attackAttribute;
	private short _attributePower;
	private short _fireDefence;
	private short _waterDefence;
	private short _windDefence;
	private short _earthDefence;
	private short _holyDefence;
	private short _darkDefence;
	private EnsoulOption[] _soulCrystalOptions;
	private EnsoulOption[] _soulCrystalSpecialOptions;

	@Override
	protected void readImpl()
	{
		this._listId = this.readInt();
		this._entryId = this.readInt();
		this._amount = this.readLong();
		this._enchantLevel = this.readShort();
		this._augmentOption1 = this.readInt();
		this._augmentOption2 = this.readInt();
		this._augmentOption3 = this.readInt();
		this._attackAttribute = this.readShort();
		this._attributePower = this.readShort();
		this._fireDefence = this.readShort();
		this._waterDefence = this.readShort();
		this._windDefence = this.readShort();
		this._earthDefence = this.readShort();
		this._holyDefence = this.readShort();
		this._darkDefence = this.readShort();
		this._soulCrystalOptions = new EnsoulOption[this.readByte()];

		for (int i = 0; i < this._soulCrystalOptions.length; i++)
		{
			int ensoulId = this.readInt();
			this._soulCrystalOptions[i] = EnsoulData.getInstance().getOption(ensoulId);
		}

		this._soulCrystalSpecialOptions = new EnsoulOption[this.readByte()];

		for (int i = 0; i < this._soulCrystalSpecialOptions.length; i++)
		{
			int ensoulId = this.readInt();
			this._soulCrystalSpecialOptions[i] = EnsoulData.getInstance().getOption(ensoulId);
		}
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (!this.getClient().getFloodProtectors().canUseMultiSell())
			{
				player.setMultiSell(null);
			}
			else if (this._amount >= 1L && this._amount <= GeneralConfig.MULTISELL_AMOUNT_LIMIT)
			{
				PreparedMultisellListHolder list = player.getMultiSell();
				if (list != null && list.getId() == this._listId)
				{
					Npc npc = player.getLastFolkNPC();
					if (!list.isNpcAllowed(-1) && (npc == null || !list.isNpcAllowed(npc.getId()) || !list.checkNpcObjectId(npc.getObjectId()) || player.getInstanceId() != npc.getInstanceId() || !player.isInsideRadius3D(npc, 250)))
					{
						if (!player.isGM())
						{
							player.setMultiSell(null);
							return;
						}

						player.sendMessage("Multisell " + this._listId + " is restricted. Under current conditions cannot be used. Only GMs are allowed to use it.");
					}

					if ((this._soulCrystalOptions == null || !ArrayUtil.contains(this._soulCrystalOptions, null)) && (this._soulCrystalSpecialOptions == null || !ArrayUtil.contains(this._soulCrystalSpecialOptions, null)))
					{
						List<MultisellEntryHolder> entries = list.getEntries();
						if (entries == null)
						{
							PacketLogger.warning("Character: " + player.getName() + " requested null multisell entry. Multisell: " + this._listId + " entry: " + this._entryId);
						}
						else if (entries.isEmpty())
						{
							PacketLogger.warning("Character: " + player.getName() + " requested empty multisell entry. Multisell: " + this._listId + " entry: " + this._entryId);
						}
						else if (this._entryId - 1 >= entries.size())
						{
							PacketLogger.warning("Character: " + player.getName() + " requested out of bounds multisell entry. Multisell: " + this._listId + " entry: " + this._entryId);
						}
						else
						{
							MultisellEntryHolder entry = entries.get(this._entryId - 1);
							if (entry == null)
							{
								PacketLogger.warning("Character: " + player.getName() + " requested inexistant prepared multisell entry. Multisell: " + this._listId + " entry: " + this._entryId);
								player.setMultiSell(null);
							}
							else if (!entry.isStackable() && this._amount > 1L)
							{
								PacketLogger.warning("Character: " + player.getName() + " is trying to set amount > 1 on non-stackable multisell. Id: " + this._listId + " entry: " + this._entryId);
								player.setMultiSell(null);
							}
							else
							{
								ItemInfo itemEnchantment = list.getItemEnchantment(this._entryId - 1);
								if (itemEnchantment == null || this._amount <= 1L && itemEnchantment.getEnchantLevel() == this._enchantLevel && itemEnchantment.getAttackElementType() == this._attackAttribute && itemEnchantment.getAttackElementPower() == this._attributePower && itemEnchantment.getAttributeDefence(AttributeType.FIRE) == this._fireDefence && itemEnchantment.getAttributeDefence(AttributeType.WATER) == this._waterDefence && itemEnchantment.getAttributeDefence(AttributeType.WIND) == this._windDefence && itemEnchantment.getAttributeDefence(AttributeType.EARTH) == this._earthDefence && itemEnchantment.getAttributeDefence(AttributeType.HOLY) == this._holyDefence && itemEnchantment.getAttributeDefence(AttributeType.DARK) == this._darkDefence && (itemEnchantment.getAugmentation() != null || this._augmentOption1 == 0 && this._augmentOption2 == 0 && this._augmentOption3 == 0) && (itemEnchantment.getAugmentation() == null || itemEnchantment.getAugmentation().getOption1Id() == this._augmentOption1 && itemEnchantment.getAugmentation().getOption2Id() == this._augmentOption2 && itemEnchantment.getAugmentation().getOption3Id() == this._augmentOption3) && (this._soulCrystalOptions == null || itemEnchantment.soulCrystalOptionsMatch(this._soulCrystalOptions)) && (this._soulCrystalOptions != null || itemEnchantment.getSoulCrystalOptions().isEmpty()) && (this._soulCrystalSpecialOptions == null || itemEnchantment.soulCrystalSpecialOptionsMatch(this._soulCrystalSpecialOptions)) && (this._soulCrystalSpecialOptions != null || itemEnchantment.getSoulCrystalSpecialOptions().isEmpty()))
								{
									Clan clan = player.getClan();
									PlayerInventory inventory = player.getInventory();

									try
									{
										int slots = 0;
										int weight = 0;

										for (ItemChanceHolder product : entry.getProducts())
										{
											if (product.getId() < 0)
											{
												if (clan == null && SpecialItemType.CLAN_REPUTATION.getClientId() == product.getId())
												{
													player.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER_2);
													return;
												}
											}
											else
											{
												ItemTemplate template = ItemData.getInstance().getTemplate(product.getId());
												if (template == null)
												{
													player.setMultiSell(null);
													return;
												}

												long totalCount = Math.multiplyExact(list.getProductCount(product), this._amount);
												if (totalCount < 1L || totalCount > 2147483647L)
												{
													player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
													return;
												}

												if (!template.isStackable() || player.getInventory().getItemByItemId(product.getId()) == null)
												{
													slots++;
												}

												weight = (int) (weight + totalCount * template.getWeight());
												if (!inventory.validateWeight(weight))
												{
													player.sendPacket(SystemMessageId.WEIGHT_LIMIT_IS_EXCEEDED);
													return;
												}

												if (slots > 0 && !inventory.validateCapacity(slots))
												{
													player.sendPacket(SystemMessageId.YOUR_INVENTORY_IS_FULL);
													return;
												}

												if (list.isChanceMultisell())
												{
													slots = 0;
													weight = 0;
												}
											}
										}

										if (itemEnchantment != null && inventory.getItemByObjectId(itemEnchantment.getObjectId()) == null)
										{
											SystemMessage sm = new SystemMessage(SystemMessageId.REQUIRED_S1);
											sm.addItemName(itemEnchantment.getItem().getId());
											player.sendPacket(sm);
											return;
										}

										List<ItemChanceHolder> summedIngredients = new ArrayList<>();

										for (ItemChanceHolder ingredient : entry.getIngredients())
										{
											boolean added = false;

											for (ItemChanceHolder summedIngredient : summedIngredients)
											{
												if (summedIngredient.getId() == ingredient.getId() && summedIngredient.getEnchantmentLevel() == ingredient.getEnchantmentLevel())
												{
													summedIngredients.add(new ItemChanceHolder(ingredient.getId(), ingredient.getChance(), ingredient.getCount() + summedIngredient.getCount(), ingredient.getEnchantmentLevel(), ingredient.isMaintainIngredient()));
													summedIngredients.remove(summedIngredient);
													added = true;
												}
											}

											if (!added)
											{
												summedIngredients.add(ingredient);
											}
										}

										for (ItemChanceHolder ingredient : summedIngredients)
										{
											if (ingredient.getEnchantmentLevel() > 0)
											{
												int found = 0;

												for (Item item : inventory.getAllItemsByItemId(ingredient.getId(), ingredient.getEnchantmentLevel()))
												{
													if (item.getEnchantLevel() >= ingredient.getEnchantmentLevel())
													{
														found++;
													}
												}

												if (found < ingredient.getCount())
												{
													SystemMessage sm = new SystemMessage(SystemMessageId.REQUIRED_S1);
													sm.addString("+" + ingredient.getEnchantmentLevel() + " " + ItemData.getInstance().getTemplate(ingredient.getId()).getName());
													player.sendPacket(sm);
													return;
												}
											}
											else if (!this.checkIngredients(player, list, inventory, clan, ingredient.getId(), Math.multiplyExact(ingredient.getCount(), this._amount)))
											{
												return;
											}
										}

										InventoryUpdate iu = new InventoryUpdate();
										boolean itemEnchantmentProcessed = itemEnchantment == null;

										for (ItemChanceHolder ingredientx : entry.getIngredients())
										{
											if (!ingredientx.isMaintainIngredient())
											{
												long totalCountx = Math.multiplyExact(list.getIngredientCount(ingredientx), this._amount);
												SpecialItemType specialItem = SpecialItemType.getByClientId(ingredientx.getId());
												if (specialItem != null)
												{
													switch (specialItem)
													{
														case CLAN_REPUTATION:
															if (clan != null)
															{
																clan.takeReputationScore((int) totalCountx);
																SystemMessage smsg = new SystemMessage(SystemMessageId.CLAN_REPUTATION_POINTS_S1_2);
																smsg.addLong(totalCountx);
																player.sendPacket(smsg);
															}
															break;
														case FAME:
															player.setFame(player.getFame() - (int) totalCountx);
															player.updateUserInfo();
															break;
														case RAIDBOSS_POINTS:
															player.setRaidbossPoints(player.getRaidbossPoints() - (int) totalCountx);
															player.updateUserInfo();
															player.sendPacket(new SystemMessage(SystemMessageId.YOU_CONSUMED_S1_RAID_POINTS).addLong(totalCountx));
															break;
														case PC_CAFE_POINTS:
															player.setPcCafePoints((int) (player.getPcCafePoints() - totalCountx));
															player.sendPacket(new ExPCCafePointInfo(player.getPcCafePoints(), (int) (-totalCountx), 1));
															break;
														case HONOR_COINS:
															player.setHonorCoins(player.getHonorCoins() - totalCountx);
															break;
														default:
															PacketLogger.warning("Character: " + player.getName() + " has suffered possible item loss by using multisell " + this._listId + " which has non-implemented special ingredient with id: " + ingredientx.getId() + ".");
															return;
													}
												}
												else if (ingredientx.getEnchantmentLevel() > 0)
												{
													Item destroyedItem = inventory.destroyItem(ItemProcessType.FEE, inventory.getAllItemsByItemId(ingredientx.getId(), ingredientx.getEnchantmentLevel()).iterator().next(), totalCountx, player, npc);
													if (destroyedItem == null)
													{
														SystemMessage sm = new SystemMessage(SystemMessageId.REQUIRED_S1);
														sm.addItemName(ingredientx.getId());
														player.sendPacket(sm);
														return;
													}

													itemEnchantmentProcessed = true;
													iu.addItem(destroyedItem);
													if (itemEnchantmentProcessed && destroyedItem.isEquipable())
													{
														itemEnchantment = new ItemInfo(destroyedItem);
													}
												}
												else if (!itemEnchantmentProcessed && itemEnchantment != null && itemEnchantment.getItem().getId() == ingredientx.getId())
												{
													Item destroyedItemx = inventory.destroyItem(ItemProcessType.FEE, itemEnchantment.getObjectId(), totalCountx, player, npc);
													if (destroyedItemx == null)
													{
														SystemMessage sm = new SystemMessage(SystemMessageId.REQUIRED_S1);
														sm.addItemName(ingredientx.getId());
														player.sendPacket(sm);
														return;
													}

													itemEnchantmentProcessed = true;
													iu.addItem(destroyedItemx);
													if (itemEnchantmentProcessed && destroyedItemx.isEquipable())
													{
														itemEnchantment = new ItemInfo(destroyedItemx);
													}
												}
												else
												{
													Item destroyedItemxx = inventory.destroyItemByItemId(ItemProcessType.FEE, ingredientx.getId(), totalCountx, player, npc);
													if (destroyedItemxx == null)
													{
														SystemMessage sm = new SystemMessage(SystemMessageId.YOU_NEED_S1_X_S2);
														sm.addItemName(ingredientx.getId());
														sm.addLong(totalCountx);
														player.sendPacket(sm);
														return;
													}

													iu.addItem(destroyedItemxx);
													if (itemEnchantmentProcessed && destroyedItemxx.isEquipable())
													{
														itemEnchantment = new ItemInfo(destroyedItemxx);
													}
												}
											}
										}

										List<ItemChanceHolder> products = entry.getProducts();
										if (list.isChanceMultisell())
										{
											ItemChanceHolder randomProduct = ItemChanceHolder.getRandomHolder(entry.getProducts());
											products = randomProduct != null ? Collections.singletonList(randomProduct) : Collections.emptyList();
										}

										Iterator<ItemChanceHolder> var43 = products.iterator();

										while (true)
										{
											if (!var43.hasNext())
											{
												if (EventDispatcher.getInstance().hasListener(EventType.ON_MULTISELL_BUY_ITEM, player))
												{
													EventDispatcher.getInstance().notifyEventAsync(new OnMultisellBuyItem(player, this._listId, this._amount, entry.getIngredients(), products), player);
												}
												break;
											}

											ItemChanceHolder productx = var43.next();
											long totalCountx = Math.multiplyExact(list.getProductCount(productx), this._amount);
											SpecialItemType specialItem = SpecialItemType.getByClientId(productx.getId());
											if (specialItem != null)
											{
												switch (specialItem)
												{
													case CLAN_REPUTATION:
														if (clan != null)
														{
															clan.addReputationScore((int) totalCountx);
														}
														break;
													case FAME:
														player.setFame((int) (player.getFame() + totalCountx));
														player.updateUserInfo();
														break;
													case RAIDBOSS_POINTS:
														player.increaseRaidbossPoints((int) totalCountx);
														player.updateUserInfo();
														break;
													case PC_CAFE_POINTS:
													default:
														PacketLogger.warning("Character: " + player.getName() + " has suffered possible item loss by using multisell " + this._listId + " which has non-implemented special product with id: " + productx.getId() + ".");
														return;
													case HONOR_COINS:
														player.setHonorCoins(player.getHonorCoins() + totalCountx);
												}
											}
											else
											{
												Item addedItem = inventory.addItem(ItemProcessType.BUY, productx.getId(), totalCountx, player, npc, false);
												if (itemEnchantmentProcessed && list.isMaintainEnchantment() && itemEnchantment != null && addedItem.isEquipable() && addedItem.getTemplate().getClass().equals(itemEnchantment.getItem().getClass()))
												{
													addedItem.setEnchantLevel(itemEnchantment.getEnchantLevel());
													addedItem.setAugmentation(itemEnchantment.getAugmentation(), false);
													if (addedItem.isWeapon())
													{
														if (itemEnchantment.getAttackElementPower() > 0)
														{
															addedItem.setAttribute(new AttributeHolder(AttributeType.findByClientId(itemEnchantment.getAttackElementType()), itemEnchantment.getAttackElementPower()), false);
														}
													}
													else
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

													if (this._soulCrystalOptions != null)
													{
														int pos = -1;

														for (EnsoulOption ensoul : this._soulCrystalOptions)
														{
															addedItem.addSpecialAbility(ensoul, ++pos, 1, false);
														}
													}

													if (this._soulCrystalSpecialOptions != null)
													{
														for (EnsoulOption ensoul : this._soulCrystalSpecialOptions)
														{
															addedItem.addSpecialAbility(ensoul, 0, 2, false);
														}
													}

													addedItem.updateDatabase(true);
													itemEnchantmentProcessed = false;
												}

												if (productx.getEnchantmentLevel() > 0)
												{
													addedItem.setEnchantLevel(productx.getEnchantmentLevel());
													addedItem.updateDatabase(true);
												}

												if (addedItem.getCount() > 1L)
												{
													SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1_X_S2);
													sm.addItemName(addedItem.getId());
													sm.addLong(totalCountx);
													player.sendPacket(sm);
												}
												else if (addedItem.getEnchantLevel() > 0)
												{
													SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1_S2_2);
													sm.addLong(addedItem.getEnchantLevel());
													sm.addItemName(addedItem.getId());
													player.sendPacket(sm);
												}
												else
												{
													SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1_2);
													sm.addItemName(addedItem);
													player.sendPacket(sm);
												}

												iu.addItem(addedItem);
												player.sendPacket(new ExMultiSellResult(1, 0, (int) addedItem.getCount()));
											}
										}

										player.sendInventoryUpdate(iu);
										if (npc != null && list.isApplyTaxes())
										{
											long taxPaid = 0L;

											for (ItemChanceHolder ingredientxx : entry.getIngredients())
											{
												if (ingredientxx.getId() == 57)
												{
													taxPaid += Math.round(ingredientxx.getCount() * list.getIngredientMultiplier() * list.getTaxRate()) * this._amount;
												}
											}

											if (taxPaid > 0L)
											{
												npc.handleTaxPayment(taxPaid);
											}
										}
									}
									catch (ArithmeticException var26)
									{
										player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
										return;
									}

									if (list.isInventoryOnly() || list.isMaintainEnchantment())
									{
										MultisellData.getInstance().separateAndSend(list.getId(), player, npc, list.isInventoryOnly(), list.getProductMultiplier(), list.getIngredientMultiplier(), 0);
									}
								}
								else
								{
									PacketLogger.warning("Character: " + player.getName() + " is trying to upgrade equippable item, but the stats doesn't match. Id: " + this._listId + " entry: " + this._entryId);
									player.setMultiSell(null);
								}
							}
						}
					}
					else
					{
						PacketLogger.warning("Character: " + player.getName() + " requested multisell entry with invalid soul crystal options. Multisell: " + this._listId + " entry: " + this._entryId);
						player.setMultiSell(null);
					}
				}
				else
				{
					player.setMultiSell(null);
				}
			}
			else
			{
				player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			}
		}
	}

	private boolean checkIngredients(Player player, PreparedMultisellListHolder list, PlayerInventory inventory, Clan clan, int ingredientId, long totalCount)
	{
		SpecialItemType specialItem = SpecialItemType.getByClientId(ingredientId);
		if (specialItem != null)
		{
			switch (specialItem)
			{
				case CLAN_REPUTATION:
					if (clan == null)
					{
						player.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER_2);
						return false;
					}
					else if (!player.isClanLeader())
					{
						player.sendPacket(SystemMessageId.AVAILABLE_ONLY_TO_THE_CLAN_LEADER);
						return false;
					}
					else
					{
						if (clan.getReputationScore() < totalCount)
						{
							player.sendPacket(SystemMessageId.THE_CLAN_REPUTATION_IS_TOO_LOW);
							return false;
						}

						return true;
					}
				case FAME:
					if (player.getFame() < totalCount)
					{
						player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_REPUTATION_POINTS);
						return false;
					}

					return true;
				case RAIDBOSS_POINTS:
					if (player.getRaidbossPoints() < totalCount)
					{
						player.sendPacket(SystemMessageId.NOT_ENOUGH_RAID_POINTS);
						return false;
					}

					return true;
				case PC_CAFE_POINTS:
					if (player.getPcCafePoints() < totalCount)
					{
						player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_ABUNDANCE_POINTS);
						return false;
					}

					return true;
				case HONOR_COINS:
					if (player.getHonorCoins() < totalCount)
					{
						player.sendMessage("You are short of Honor Points.");
						return false;
					}

					return true;
				default:
					PacketLogger.warning("Multisell: " + this._listId + " is using a non-implemented special ingredient with id: " + ingredientId + ".");
					return false;
			}
		}
		else if (inventory.getInventoryItemCount(ingredientId, list.isMaintainEnchantment() ? -1 : 0, false) < totalCount)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.YOU_NEED_S1_X_S2);
			sm.addItemName(ingredientId);
			sm.addLong(totalCount);
			player.sendPacket(sm);
			return false;
		}
		else
		{
			return true;
		}
	}
}
