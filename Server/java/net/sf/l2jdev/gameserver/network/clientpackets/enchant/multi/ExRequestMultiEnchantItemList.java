package net.sf.l2jdev.gameserver.network.clientpackets.enchant.multi;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.data.xml.EnchantChallengePointData;
import net.sf.l2jdev.gameserver.data.xml.EnchantItemData;
import net.sf.l2jdev.gameserver.data.xml.ItemCrystallizationData;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.managers.events.BlackCouponManager;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.EnchantItemRequest;
import net.sf.l2jdev.gameserver.model.item.enchant.EnchantResultType;
import net.sf.l2jdev.gameserver.model.item.enchant.EnchantScroll;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemChanceHolder;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.ShortcutInit;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.enchant.EnchantResult;
import net.sf.l2jdev.gameserver.network.serverpackets.enchant.challengepoint.ExEnchantChallengePointInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.enchant.multi.ExResultMultiEnchantItemList;
import net.sf.l2jdev.gameserver.network.serverpackets.enchant.multi.ExResultSetMultiEnchantItemList;
import net.sf.l2jdev.gameserver.network.serverpackets.enchant.single.ChangedEnchantTargetItemProbabilityList;

public class ExRequestMultiEnchantItemList extends ClientPacket
{
	private int _useLateAnnounce;
	private int _slotId;
	private final Map<Integer, Integer> _itemObjectId = new HashMap<>();
	private final Map<Integer, String> _result = new HashMap<>();
	private final Map<Integer, int[]> _successEnchant = new HashMap<>();
	private final Map<Integer, Integer> _failureEnchant = new HashMap<>();
	final Map<Integer, Integer> failChallengePointInfoList = new LinkedHashMap<>();
	private final Map<Integer, ItemHolder> _failureReward = new HashMap<>();
	protected static final Logger LOGGER_ENCHANT = Logger.getLogger("enchant.items");

	@Override
	protected void readImpl()
	{
		this._useLateAnnounce = this.readByte();
		this._slotId = this.readInt();

		for (int i = 1; this.remaining() != 0; i++)
		{
			this._itemObjectId.put(i, this.readInt());
		}
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.getChallengeInfo().setChallengePointsPendingRecharge(-1, -1);
			EnchantItemRequest request = player.getRequest(EnchantItemRequest.class);
			if (request != null)
			{
				if (request.getEnchantingScroll() != null && !request.isProcessing())
				{
					Item scroll = request.getEnchantingScroll();
					if (scroll.getCount() < this._slotId)
					{
						player.removeRequest(request.getClass());
						player.sendPacket(new ExResultSetMultiEnchantItemList(player, 1));
						Logger.getLogger("MultiEnchant - player " + player.getObjectId() + " " + player.getName() + " trying enchant items, when scroll count is less than items!");
					}
					else
					{
						EnchantScroll scrollTemplate = EnchantItemData.getInstance().getEnchantScroll(scroll);
						if (scrollTemplate != null)
						{
							int[] slots = new int[this._slotId];

							for (int i = 1; i <= this._slotId; i++)
							{
								if (!request.checkMultiEnchantingItemsByObjectId(this._itemObjectId.get(i)))
								{
									player.removeRequest(request.getClass());
									return;
								}

								slots[i - 1] = this.getMultiEnchantingSlotByObjectId(request, this._itemObjectId.get(i));
							}

							this._itemObjectId.clear();
							request.setProcessing(true);

							for (int i : slots)
							{
								if (i == -1 || request.getMultiEnchantingItemsBySlot(i) == -1)
								{
									player.sendPacket(new ExResultMultiEnchantItemList(player, true));
									player.removeRequest(request.getClass());
									return;
								}

								Item enchantItem = player.getInventory().getItemByObjectId(request.getMultiEnchantingItemsBySlot(i));
								if (enchantItem == null)
								{
									player.removeRequest(request.getClass());
									return;
								}

								if (scrollTemplate.getMaxEnchantLevel() < enchantItem.getEnchantLevel())
								{
									Logger.getLogger("MultiEnchant - player " + player.getObjectId() + " " + player.getName() + " trying over-enchant item " + enchantItem.getItemName() + " " + enchantItem.getObjectId());
									player.removeRequest(request.getClass());
									return;
								}

								if (player.getInventory().destroyItemByItemId(ItemProcessType.FEE, scroll.getId(), 1L, player, enchantItem) == null)
								{
									player.removeRequest(request.getClass());
									return;
								}

								synchronized (enchantItem)
								{
									if (enchantItem.getOwnerId() != player.getObjectId() || !enchantItem.isEnchantable())
									{
										player.sendPacket(SystemMessageId.AUGMENTATION_REQUIREMENTS_ARE_NOT_FULFILLED);
										player.removeRequest(request.getClass());
										player.sendPacket(new ExResultMultiEnchantItemList(player, true));
										return;
									}

									EnchantResultType resultType = scrollTemplate.calculateSuccess(player, enchantItem, null);
									switch (resultType)
									{
										case ERROR:
											player.sendPacket(SystemMessageId.AUGMENTATION_REQUIREMENTS_ARE_NOT_FULFILLED);
											player.removeRequest(request.getClass());
											this._result.put(slots[i - 1], "ERROR");
											break;
										case SUCCESS:
											if (scrollTemplate.isCursed())
											{
												player.sendPacket(SystemMessageId.THE_ENCHANT_VALUE_IS_DECREASED_BY_1);
												enchantItem.setEnchantLevel(enchantItem.getEnchantLevel() - 1);
											}
											else if (scrollTemplate.getChance(player, enchantItem) > 0.0)
											{
												enchantItem.setEnchantLevel(enchantItem.getEnchantLevel() + Math.min(Rnd.get(scrollTemplate.getRandomEnchantMin(), scrollTemplate.getRandomEnchantMax()), scrollTemplate.getMaxEnchantLevel()));
												enchantItem.updateDatabase();
											}

											this._result.put(i, "SUCCESS");
											if (GeneralConfig.LOG_ITEM_ENCHANTS)
											{
												StringBuilder sb = new StringBuilder();
												if (enchantItem.getEnchantLevel() > 0)
												{
													LOGGER_ENCHANT.info(sb.append("Success, Character:").append(player.getName()).append(" [").append(player.getObjectId()).append("] Account:").append(player.getAccountName()).append(" IP:").append(player.getIPAddress()).append(", +").append(enchantItem.getEnchantLevel()).append(" ").append(enchantItem.getName()).append("(").append(enchantItem.getCount()).append(") [").append(enchantItem.getObjectId()).append("], ").append(scroll.getName()).append("(").append(scroll.getCount()).append(") [").append(scroll.getObjectId()).append("]").toString());
												}
												else
												{
													LOGGER_ENCHANT.info(sb.append("Success, Character:").append(player.getName()).append(" [").append(player.getObjectId()).append("] Account:").append(player.getAccountName()).append(" IP:").append(player.getIPAddress()).append(", ").append(enchantItem.getName()).append("(").append(enchantItem.getCount()).append(") [").append(enchantItem.getObjectId()).append("], ").append(scroll.getName()).append("(").append(scroll.getCount()).append(") [").append(scroll.getObjectId()).append("]").toString());
												}
											}
											break;
										case FAILURE:
											if (scrollTemplate.isSafe())
											{
												player.sendPacket(SystemMessageId.ENCHANT_FAILED_THE_ENCHANT_SKILL_FOR_THE_CORRESPONDING_ITEM_WILL_BE_EXACTLY_RETAINED);
												player.sendPacket(new EnchantResult(5, new ItemHolder(enchantItem.getId(), 1L), null, 0));
												if (GeneralConfig.LOG_ITEM_ENCHANTS)
												{
													StringBuilder sb = new StringBuilder();
													if (enchantItem.getEnchantLevel() > 0)
													{
														LOGGER_ENCHANT.info(sb.append("Safe Fail, Character:").append(player.getName()).append(" [").append(player.getObjectId()).append("] Account:").append(player.getAccountName()).append(" IP:").append(player.getIPAddress()).append(", +").append(enchantItem.getEnchantLevel()).append(" ").append(enchantItem.getName()).append("(").append(enchantItem.getCount()).append(") [").append(enchantItem.getObjectId()).append("], ").append(scroll.getName()).append("(").append(scroll.getCount()).append(") [").append(scroll.getObjectId()).append("]").toString());
													}
													else
													{
														LOGGER_ENCHANT.info(sb.append("Safe Fail, Character:").append(player.getName()).append(" [").append(player.getObjectId()).append("] Account:").append(player.getAccountName()).append(" IP:").append(player.getIPAddress()).append(", ").append(enchantItem.getName()).append("(").append(enchantItem.getCount()).append(") [").append(enchantItem.getObjectId()).append("], ").append(scroll.getName()).append("(").append(scroll.getCount()).append(") [").append(scroll.getObjectId()).append("]").toString());
													}
												}
											}

											if (!scrollTemplate.isBlessed() && !scrollTemplate.isBlessedDown() && !scrollTemplate.isCursed())
											{
												int[] challengePoints = EnchantChallengePointData.getInstance().handleFailure(player, enchantItem);
												if (challengePoints[0] != -1 && challengePoints[1] != -1)
												{
													this.failChallengePointInfoList.compute(challengePoints[0], (_, v) -> v == null ? challengePoints[1] : v + challengePoints[1]);
												}

												BlackCouponManager.getInstance().createNewRecord(player.getObjectId(), enchantItem.getId(), (short) enchantItem.getEnchantLevel());
												if (player.getInventory().destroyItem(ItemProcessType.FEE, enchantItem, player, null) == null)
												{
													PunishmentManager.handleIllegalPlayerAction(player, "Unable to delete item on enchant failure from " + player + ", possible cheater !", GeneralConfig.DEFAULT_PUNISH);
													player.removeRequest(request.getClass());
													this._result.put(i, "ERROR");
													if (GeneralConfig.LOG_ITEM_ENCHANTS)
													{
														StringBuilder sb = new StringBuilder();
														if (enchantItem.getEnchantLevel() > 0)
														{
															LOGGER_ENCHANT.info(sb.append("Unable to destroy, Character:").append(player.getName()).append(" [").append(player.getObjectId()).append("] Account:").append(player.getAccountName()).append(" IP:").append(player.getIPAddress()).append(", +").append(enchantItem.getEnchantLevel()).append(" ").append(enchantItem.getName()).append("(").append(enchantItem.getCount()).append(") [").append(enchantItem.getObjectId()).append("], ").append(scroll.getName()).append("(").append(scroll.getCount()).append(") [").append(scroll.getObjectId()).append("]").toString());
														}
														else
														{
															LOGGER_ENCHANT.info(sb.append("Unable to destroy, Character:").append(player.getName()).append(" [").append(player.getObjectId()).append("] Account:").append(player.getAccountName()).append(" IP:").append(player.getIPAddress()).append(", ").append(enchantItem.getName()).append("(").append(enchantItem.getCount()).append(") [").append(enchantItem.getObjectId()).append("], ").append(scroll.getName()).append("(").append(scroll.getCount()).append(") [").append(scroll.getObjectId()).append("]").toString());
														}
													}

													return;
												}

												World.getInstance().removeObject(enchantItem);
												int count = 0;
												if (enchantItem.getTemplate().isCrystallizable())
												{
													count = Math.max(0, enchantItem.getCrystalCount() - (enchantItem.getTemplate().getCrystalCount() + 1) / 2);
												}

												Item crystals = null;
												int crystalId = enchantItem.getTemplate().getCrystalItemId();
												if (count > 0)
												{
													crystals = player.getInventory().addItem(ItemProcessType.COMPENSATE, crystalId, count, player, enchantItem);
													SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1_X_S2);
													sm.addItemName(crystals);
													sm.addLong(count);
													player.sendPacket(sm);
													ItemHolder itemHolder = new ItemHolder(crystalId, count);
													this._failureReward.put(this._failureReward.size() + 1, itemHolder);
												}

												if (crystalId != 0 && count != 0)
												{
													ItemHolder itemHolder = new ItemHolder(0, 0L);
													this._failureReward.put(this._failureReward.size() + 1, itemHolder);
													this._result.put(i, "FAIL");
												}
												else
												{
													ItemHolder itemHolder = new ItemHolder(0, 0L);
													this._failureReward.put(this._failureReward.size() + 1, itemHolder);
													this._result.put(i, "NO_CRYSTAL");
												}

												List<ItemChanceHolder> destroyRewards = ItemCrystallizationData.getInstance().getRewardItems(enchantItem, enchantItem.getEnchantLevel());
												ItemHolder crystalss = new ItemHolder(crystalId, count);

												for (ItemChanceHolder reward : destroyRewards)
												{
													if (Rnd.get(100.0) < reward.getChance())
													{
														SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1_X_S2);
														sm.addItemName(reward.getId());
														sm.addLong(reward.getCount());
														player.sendPacket(sm);
														player.sendPacket(new EnchantResult(1, crystalss, reward, 0));
													}
												}

												if (destroyRewards.isEmpty())
												{
													player.sendPacket(new EnchantResult(1, crystalss, null, 0));
												}

												if (GeneralConfig.LOG_ITEM_ENCHANTS)
												{
													StringBuilder sb = new StringBuilder();
													if (enchantItem.getEnchantLevel() > 0)
													{
														LOGGER_ENCHANT.info(sb.append("Fail, Character:").append(player.getName()).append(" [").append(player.getObjectId()).append("] Account:").append(player.getAccountName()).append(" IP:").append(player.getIPAddress()).append(", +").append(enchantItem.getEnchantLevel()).append(" ").append(enchantItem.getName()).append("(").append(enchantItem.getCount()).append(") [").append(enchantItem.getObjectId()).append("], ").append(scroll.getName()).append("(").append(scroll.getCount()).append(") [").append(scroll.getObjectId()).append("]").toString());
													}
													else
													{
														LOGGER_ENCHANT.info(sb.append("Fail, Character:").append(player.getName()).append(" [").append(player.getObjectId()).append("] Account:").append(player.getAccountName()).append(" IP:").append(player.getIPAddress()).append(", ").append(enchantItem.getName()).append("(").append(enchantItem.getCount()).append(") [").append(enchantItem.getObjectId()).append("], ").append(scroll.getName()).append("(").append(scroll.getCount()).append(") [").append(scroll.getObjectId()).append("]").toString());
													}
												}
											}
											else
											{
												if (!scrollTemplate.isBlessedDown() && !scrollTemplate.isCursed())
												{
													player.sendPacket(SystemMessageId.THE_BLESSED_ENCHANT_FAILED_THE_ENCHANT_VALUE_OF_THE_ITEM_BECAME_0);
													enchantItem.setEnchantLevel(0);
												}
												else
												{
													player.sendPacket(SystemMessageId.THE_ENCHANT_VALUE_IS_DECREASED_BY_1);
													enchantItem.setEnchantLevel(enchantItem.getEnchantLevel() - 1);
												}

												this._result.put(i, "BLESSED_FAIL");
												enchantItem.updateDatabase();
												if (GeneralConfig.LOG_ITEM_ENCHANTS)
												{
													StringBuilder sb = new StringBuilder();
													if (enchantItem.getEnchantLevel() > 0)
													{
														LOGGER_ENCHANT.info(sb.append("Blessed Fail, Character:").append(player.getName()).append(" [").append(player.getObjectId()).append("] Account:").append(player.getAccountName()).append(" IP:").append(player.getIPAddress()).append(", +").append(enchantItem.getEnchantLevel()).append(" ").append(enchantItem.getName()).append("(").append(enchantItem.getCount()).append(") [").append(enchantItem.getObjectId()).append("], ").append(scroll.getName()).append("(").append(scroll.getCount()).append(") [").append(scroll.getObjectId()).append("]").toString());
													}
													else
													{
														LOGGER_ENCHANT.info(sb.append("Blessed Fail, Character:").append(player.getName()).append(" [").append(player.getObjectId()).append("] Account:").append(player.getAccountName()).append(" IP:").append(player.getIPAddress()).append(", ").append(enchantItem.getName()).append("(").append(enchantItem.getCount()).append(") [").append(enchantItem.getObjectId()).append("], ").append(scroll.getName()).append("(").append(scroll.getCount()).append(") [").append(scroll.getObjectId()).append("]").toString());
													}
												}
											}
									}
								}
							}

							for (int ix : slots)
							{
								if (this._result.get(ix).equals("SUCCESS"))
								{
									int[] intArray = new int[]
									{
										request.getMultiEnchantingItemsBySlot(ix),
										player.getInventory().getItemByObjectId(request.getMultiEnchantingItemsBySlot(ix)).getEnchantLevel()
									};
									this._successEnchant.put(ix, intArray);
								}
								else
								{
									if (!this._result.get(ix).equals("NO_CRYSTAL") && !this._result.get(ix).equals("FAIL"))
									{
										player.sendPacket(new ExResultMultiEnchantItemList(player, this._successEnchant, this._failureEnchant, this.failChallengePointInfoList, true));
										player.sendPacket(new ShortcutInit(player));
										return;
									}

									this._failureEnchant.put(ix, request.getMultiEnchantingItemsBySlot(ix));
									request.changeMultiEnchantingItemsBySlot(ix, 0);
								}
							}

							for (ItemHolder failure : this._failureReward.values())
							{
								request.addMultiEnchantFailItems(failure);
							}

							request.setProcessing(false);
							player.sendItemList();
							player.broadcastUserInfo();
							player.sendPacket(new ChangedEnchantTargetItemProbabilityList(player, true));
							if (this._useLateAnnounce == 1)
							{
								request.setMultiSuccessEnchantList(this._successEnchant);
								request.setMultiFailureEnchantList(this._failureEnchant);
							}

							player.sendPacket(new ExResultMultiEnchantItemList(player, this._successEnchant, this._failureEnchant, this.failChallengePointInfoList, true));
							player.sendPacket(new ShortcutInit(player));
							player.sendPacket(new ExEnchantChallengePointInfo(player));
						}
					}
				}
			}
		}
	}

	public int getMultiEnchantingSlotByObjectId(EnchantItemRequest request, int objectId)
	{
		int slotId = -1;

		for (int i = 1; i <= request.getMultiEnchantingItemsCount(); i++)
		{
			if (request.getMultiEnchantingItemsCount() == 0 || objectId == 0)
			{
				return slotId;
			}

			if (request.getMultiEnchantingItemsBySlot(i) == objectId)
			{
				return i;
			}
		}

		return slotId;
	}
}
