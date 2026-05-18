package net.sf.l2jdev.gameserver.network.clientpackets.limitshop;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.config.VipSystemConfig;
import net.sf.l2jdev.gameserver.data.holders.LimitShopProductHolder;
import net.sf.l2jdev.gameserver.data.holders.LimitShopRandomCraftReward;
import net.sf.l2jdev.gameserver.data.xml.LimitShopClanData;
import net.sf.l2jdev.gameserver.data.xml.LimitShopData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.PrimeShopRequest;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.enums.SpecialItemType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.enums.ExBrProductReplyType;
import net.sf.l2jdev.gameserver.network.serverpackets.ExItemAnnounce;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPCCafePointInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.limitshop.ExPurchaseLimitShopItemResult;
import net.sf.l2jdev.gameserver.network.serverpackets.primeshop.ExBRBuyProduct;
import net.sf.l2jdev.gameserver.util.Broadcast;

public class RequestPurchaseLimitShopItemBuy extends ClientPacket
{
	private int _shopIndex;
	private int _productId;
	private int _amount;
	private LimitShopProductHolder _product;

	@Override
	protected void readImpl()
	{
		this._shopIndex = this.readByte();
		this._productId = this.readInt();
		this._amount = this.readInt();
		switch (this._shopIndex)
		{
			case 3:
				this._product = LimitShopData.getInstance().getProduct(this._productId);
				break;
			case 100:
				this._product = LimitShopClanData.getInstance().getProduct(this._productId);
				break;
			default:
				this._product = null;
		}
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (this._product != null)
			{
				if (this._amount < 1 || this._amount > 10000)
				{
					player.sendPacket(new ExBRBuyProduct(ExBrProductReplyType.INVENTORY_OVERFLOW));
					player.sendPacket(new ExPurchaseLimitShopItemResult(false, this._shopIndex, this._productId, 0, Collections.emptyList()));
				}
				else if (!player.isInventoryUnder80(false))
				{
					player.sendPacket(new ExBRBuyProduct(ExBrProductReplyType.INVENTORY_OVERFLOW));
					player.sendPacket(new ExPurchaseLimitShopItemResult(false, this._shopIndex, this._productId, 0, Collections.emptyList()));
				}
				else if (player.getLevel() >= this._product.getMinLevel() && player.getLevel() <= this._product.getMaxLevel())
				{
					if (!player.hasItemRequest() && !player.hasRequest(PrimeShopRequest.class))
					{
						player.addRequest(new PrimeShopRequest(player));
						if (this._product.getAccountDailyLimit() > 0)
						{
							long amount = this._product.getAccountDailyLimit() * this._amount;
							if (amount < 1L)
							{
								player.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
								player.removeRequest(PrimeShopRequest.class);
								player.sendPacket(new ExPurchaseLimitShopItemResult(false, this._shopIndex, this._productId, 0, Collections.emptyList()));
								return;
							}

							int currentPurchaseCount = player.getAccountVariables().getInt("LCSDailyCount" + this._product.getProductionId(), 0);
							if (currentPurchaseCount + this._amount > this._product.getAccountDailyLimit())
							{
								player.sendMessage("You have reached your daily limit.");
								player.removeRequest(PrimeShopRequest.class);
								player.sendPacket(new ExPurchaseLimitShopItemResult(false, this._shopIndex, this._productId, 0, Collections.emptyList()));
								return;
							}
						}
						else if (this._product.getAccountWeeklyLimit() > 0)
						{
							long amountx = this._product.getAccountWeeklyLimit() * this._amount;
							if (amountx < 1L)
							{
								player.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
								player.removeRequest(PrimeShopRequest.class);
								player.sendPacket(new ExPurchaseLimitShopItemResult(false, this._shopIndex, this._productId, 0, Collections.emptyList()));
								return;
							}

							int currentPurchaseCount = player.getAccountVariables().getInt("LCSWeeklyCount" + this._product.getProductionId(), 0);
							if (currentPurchaseCount + this._amount > this._product.getAccountWeeklyLimit())
							{
								player.sendMessage("You have reached your weekly limit.");
								player.removeRequest(PrimeShopRequest.class);
								player.sendPacket(new ExPurchaseLimitShopItemResult(false, this._shopIndex, this._productId, 0, Collections.emptyList()));
								return;
							}
						}
						else if (this._product.getAccountMonthlyLimit() > 0)
						{
							long amountxx = this._product.getAccountMonthlyLimit() * this._amount;
							if (amountxx < 1L)
							{
								player.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
								player.removeRequest(PrimeShopRequest.class);
								player.sendPacket(new ExPurchaseLimitShopItemResult(false, this._shopIndex, this._productId, 0, Collections.emptyList()));
								return;
							}

							int currentPurchaseCount = player.getAccountVariables().getInt("LCSMonthlyCount" + this._product.getProductionId(), 0);
							if (currentPurchaseCount + this._amount > this._product.getAccountMonthlyLimit())
							{
								player.sendMessage("You have reached your monthly limit.");
								player.removeRequest(PrimeShopRequest.class);
								player.sendPacket(new ExPurchaseLimitShopItemResult(false, this._shopIndex, this._productId, 0, Collections.emptyList()));
								return;
							}
						}
						else if (this._product.getAccountBuyLimit() > 0)
						{
							long amountxxx = this._product.getAccountBuyLimit() * this._amount;
							if (amountxxx < 1L)
							{
								player.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
								player.removeRequest(PrimeShopRequest.class);
								player.sendPacket(new ExPurchaseLimitShopItemResult(false, this._shopIndex, this._productId, 0, Collections.emptyList()));
								return;
							}

							int currentPurchaseCount = player.getAccountVariables().getInt("LCSCount" + this._product.getProductionId(), 0);
							if (currentPurchaseCount + this._amount > this._product.getAccountBuyLimit())
							{
								player.sendMessage("You cannot buy any more of this item.");
								player.removeRequest(PrimeShopRequest.class);
								player.sendPacket(new ExPurchaseLimitShopItemResult(false, this._shopIndex, this._productId, 0, Collections.emptyList()));
								return;
							}
						}

						int remainingInfo = Math.max(0, Math.max(this._product.getAccountBuyLimit(), Math.max(this._product.getAccountDailyLimit(), this._product.getAccountMonthlyLimit())));

						for (int i = 0; i < this._product.getIngredientIds().length; i++)
						{
							if (this._product.getIngredientIds()[i] != 0)
							{
								if (this._product.getIngredientIds()[i] == 57)
								{
									long amountxxxx = this._product.getIngredientQuantities()[i] * this._amount;
									if ((amountxxxx < 1L) || (player.getAdena() < amountxxxx))
									{
										player.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
										player.removeRequest(PrimeShopRequest.class);
										player.sendPacket(new ExPurchaseLimitShopItemResult(false, this._shopIndex, this._productId, remainingInfo, Collections.emptyList()));
										return;
									}
								}
								else if (this._product.getIngredientIds()[i] == SpecialItemType.HONOR_COINS.getClientId())
								{
									long amountxxxxx = this._product.getIngredientQuantities()[i] * this._amount;
									if ((amountxxxxx < 1L) || (player.getHonorCoins() < amountxxxxx))
									{
										player.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
										player.removeRequest(PrimeShopRequest.class);
										player.sendPacket(new ExPurchaseLimitShopItemResult(false, this._shopIndex, this._productId, remainingInfo, Collections.emptyList()));
										return;
									}
								}
								else if (this._product.getIngredientIds()[i] == SpecialItemType.PC_CAFE_POINTS.getClientId())
								{
									long amountxxxxxx = this._product.getIngredientQuantities()[i] * this._amount;
									if ((amountxxxxxx < 1L) || (player.getPcCafePoints() < amountxxxxxx))
									{
										player.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
										player.removeRequest(PrimeShopRequest.class);
										player.sendPacket(new ExPurchaseLimitShopItemResult(false, this._shopIndex, this._productId, remainingInfo, Collections.emptyList()));
										return;
									}
								}
								else
								{
									long amountxxxxxxx = this._product.getIngredientQuantities()[i] * this._amount;
									if ((amountxxxxxxx < 1L) || (player.getInventory().getInventoryItemCount(this._product.getIngredientIds()[i], this._product.getIngredientEnchants()[i] == 0 ? -1 : this._product.getIngredientEnchants()[i], true) < amountxxxxxxx))
									{
										player.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
										player.removeRequest(PrimeShopRequest.class);
										player.sendPacket(new ExPurchaseLimitShopItemResult(false, this._shopIndex, this._productId, remainingInfo, Collections.emptyList()));
										return;
									}
								}
							}
						}

						for (int ix = 0; ix < this._product.getIngredientIds().length; ix++)
						{
							if (this._product.getIngredientIds()[ix] != 0)
							{
								long ingredientQuantity = this._product.getIngredientQuantities()[ix];
								if (this._product.getIngredientIds()[ix] == 57)
								{
									player.reduceAdena(ItemProcessType.FEE, ingredientQuantity * this._amount, player, true);
								}
								else if (this._product.getIngredientIds()[ix] == SpecialItemType.HONOR_COINS.getClientId())
								{
									player.setHonorCoins(player.getHonorCoins() - ingredientQuantity * this._amount);
								}
								else if (this._product.getIngredientIds()[ix] == SpecialItemType.PC_CAFE_POINTS.getClientId())
								{
									int newPoints = (int) (player.getPcCafePoints() - ingredientQuantity * this._amount);
									player.setPcCafePoints(newPoints);
									player.sendPacket(new ExPCCafePointInfo(player.getPcCafePoints(), (int) (-(ingredientQuantity * this._amount)), 1));
								}
								else if (this._product.getIngredientEnchants()[ix] > 0)
								{
									int count = 0;

									for (Item item : player.getInventory().getAllItemsByItemId(this._product.getIngredientIds()[ix], this._product.getIngredientEnchants()[ix]))
									{
										if (count == ingredientQuantity)
										{
											break;
										}

										count++;
										player.destroyItem(ItemProcessType.FEE, item, player, true);
									}
								}
								else
								{
									long amountxxxxxxxx = ingredientQuantity * this._amount;
									if (amountxxxxxxxx < 1L)
									{
										player.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
										player.removeRequest(PrimeShopRequest.class);
										player.sendPacket(new ExPurchaseLimitShopItemResult(false, this._shopIndex, this._productId, remainingInfo, Collections.emptyList()));
										return;
									}

									player.destroyItemByItemId(ItemProcessType.FEE, this._product.getIngredientIds()[ix], amountxxxxxxxx, player, true);
								}

								if (VipSystemConfig.VIP_SYSTEM_L_SHOP_AFFECT)
								{
									player.updateVipPoints(this._amount);
								}
							}
						}

						Map<Integer, LimitShopRandomCraftReward> rewards = new HashMap<>();
						if (this._product.getProductionId2() > 0)
						{
							for (int ixx = 0; ixx < this._amount; ixx++)
							{
								double chance = Rnd.get(100.0);
								if (chance < this._product.getChance())
								{
									rewards.computeIfAbsent(0, _ -> new LimitShopRandomCraftReward(this._product.getProductionId(), 0, 0)).getCount().addAndGet((int) this._product.getCount());
									Item item = player.addItem(ItemProcessType.BUY, this._product.getProductionId(), this._product.getCount(), this._product.getEnchant(), player, true);
									if (this._product.isAnnounce())
									{
										Broadcast.toAllOnlinePlayers(new ExItemAnnounce(player, item, 3));
									}
								}
								else if (chance < this._product.getChance2() || this._product.getProductionId3() == 0)
								{
									rewards.computeIfAbsent(1, _ -> new LimitShopRandomCraftReward(this._product.getProductionId2(), 0, 1)).getCount().addAndGet((int) this._product.getCount2());
									Item item = player.addItem(ItemProcessType.BUY, this._product.getProductionId2(), this._product.getCount2(), player, true);
									if (this._product.isAnnounce2())
									{
										Broadcast.toAllOnlinePlayers(new ExItemAnnounce(player, item, 3));
									}
								}
								else if (chance < this._product.getChance3() || this._product.getProductionId4() == 0)
								{
									rewards.computeIfAbsent(2, _ -> new LimitShopRandomCraftReward(this._product.getProductionId3(), 0, 2)).getCount().addAndGet((int) this._product.getCount3());
									Item item = player.addItem(ItemProcessType.BUY, this._product.getProductionId3(), this._product.getCount3(), player, true);
									if (this._product.isAnnounce3())
									{
										Broadcast.toAllOnlinePlayers(new ExItemAnnounce(player, item, 3));
									}
								}
								else if (chance < this._product.getChance4() || this._product.getProductionId5() == 0)
								{
									rewards.computeIfAbsent(3, _ -> new LimitShopRandomCraftReward(this._product.getProductionId4(), 0, 3)).getCount().addAndGet((int) this._product.getCount4());
									Item item = player.addItem(ItemProcessType.BUY, this._product.getProductionId4(), this._product.getCount4(), player, true);
									if (this._product.isAnnounce4())
									{
										Broadcast.toAllOnlinePlayers(new ExItemAnnounce(player, item, 3));
									}
								}
								else if (this._product.getProductionId5() > 0)
								{
									rewards.computeIfAbsent(4, _ -> new LimitShopRandomCraftReward(this._product.getProductionId5(), 0, 4)).getCount().addAndGet((int) this._product.getCount5());
									Item item = player.addItem(ItemProcessType.BUY, this._product.getProductionId5(), this._product.getCount5(), player, true);
									if (this._product.isAnnounce5())
									{
										Broadcast.toAllOnlinePlayers(new ExItemAnnounce(player, item, 3));
									}
								}
							}
						}
						else if (Rnd.get(100.0) < this._product.getChance())
						{
							rewards.put(0, new LimitShopRandomCraftReward(this._product.getProductionId(), (int) (this._product.getCount() * this._amount), 0));
							Item item = player.addItem(ItemProcessType.BUY, this._product.getProductionId(), this._product.getCount() * this._amount, this._product.getEnchant(), player, true);
							if (this._product.isAnnounce())
							{
								Broadcast.toAllOnlinePlayers(new ExItemAnnounce(player, item, 3));
							}
						}

						if (this._product.getAccountDailyLimit() > 0)
						{
							player.getAccountVariables().set("LCSDailyCount" + this._product.getProductionId(), player.getAccountVariables().getInt("LCSDailyCount" + this._product.getProductionId(), 0) + this._amount);
						}
						else if (this._product.getAccountWeeklyLimit() > 0)
						{
							player.getAccountVariables().set("LCSWeeklyCount" + this._product.getProductionId(), player.getAccountVariables().getInt("LCSWeeklyCount" + this._product.getProductionId(), 0) + this._amount);
						}
						else if (this._product.getAccountMonthlyLimit() > 0)
						{
							player.getAccountVariables().set("LCSMonthlyCount" + this._product.getProductionId(), player.getAccountVariables().getInt("LCSMonthlyCount" + this._product.getProductionId(), 0) + this._amount);
						}
						else if (this._product.getAccountBuyLimit() > 0)
						{
							player.getAccountVariables().set("LCSCount" + this._product.getProductionId(), player.getAccountVariables().getInt("LCSCount" + this._product.getProductionId(), 0) + this._amount);
						}

						player.sendPacket(new ExPurchaseLimitShopItemResult(true, this._shopIndex, this._productId, Math.max(remainingInfo - this._amount, 0), rewards.values()));
						player.sendItemList();
						ThreadPool.schedule(() -> player.removeRequest(PrimeShopRequest.class), 1000L);
					}
					else
					{
						player.sendPacket(new ExBRBuyProduct(ExBrProductReplyType.INVALID_USER_STATE));
						player.sendPacket(new ExPurchaseLimitShopItemResult(false, this._shopIndex, this._productId, 0, Collections.emptyList()));
					}
				}
				else
				{
					player.sendPacket(SystemMessageId.YOUR_LEVEL_CANNOT_PURCHASE_THIS_ITEM);
					player.sendPacket(new ExPurchaseLimitShopItemResult(false, this._shopIndex, this._productId, 0, Collections.emptyList()));
				}
			}
		}
	}
}
