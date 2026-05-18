package net.sf.l2jdev.gameserver.network.clientpackets.enchant;

import java.util.List;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.xml.EnchantChallengePointData;
import net.sf.l2jdev.gameserver.data.xml.EnchantItemData;
import net.sf.l2jdev.gameserver.data.xml.ItemCrystallizationData;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.managers.events.BlackCouponManager;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.EnchantItemRequest;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.enchant.EnchantResultType;
import net.sf.l2jdev.gameserver.model.item.enchant.EnchantScroll;
import net.sf.l2jdev.gameserver.model.item.enchant.EnchantSupportItem;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.enums.ItemSkillType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemChanceHolder;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.CommonSkill;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.ExItemAnnounce;
import net.sf.l2jdev.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.enchant.EnchantResult;
import net.sf.l2jdev.gameserver.network.serverpackets.enchant.challengepoint.ExEnchantChallengePointInfo;
import net.sf.l2jdev.gameserver.util.Broadcast;

public class RequestEnchantItem extends ClientPacket
{
	protected static final Logger LOGGER_ENCHANT = Logger.getLogger("enchant.items");
	private int _objectId;

	@Override
	protected void readImpl()
	{
		this._objectId = this.readInt();
		this.readByte();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			EnchantItemRequest request = player.getRequest(EnchantItemRequest.class);
			if (request != null && !request.isProcessing())
			{
				request.setEnchantingItem(this._objectId);
				request.setProcessing(true);
				if (!player.isOnline() || this.getClient().isDetached())
				{
					player.removeRequest(request.getClass());
				}
				else if (!player.isProcessingTransaction() && !player.isInStoreMode())
				{
					Item item = request.getEnchantingItem();
					Item scroll = request.getEnchantingScroll();
					Item support = request.getSupportItem();
					if (item != null && scroll != null)
					{
						EnchantScroll scrollTemplate = EnchantItemData.getInstance().getEnchantScroll(scroll);
						if (scrollTemplate != null)
						{
							EnchantSupportItem supportTemplate = null;
							if (support != null)
							{
								supportTemplate = EnchantItemData.getInstance().getSupportItem(support);
								if (supportTemplate == null)
								{
									player.removeRequest(request.getClass());
									return;
								}
							}

							if (scrollTemplate.isValid(item, supportTemplate) && (!PlayerConfig.DISABLE_OVER_ENCHANTING || item.getEnchantLevel() != scrollTemplate.getMaxEnchantLevel() && (item.getTemplate().getEnchantLimit() == 0 || item.getEnchantLevel() != item.getTemplate().getEnchantLimit())))
							{
								if (player.getInventory().destroyItem(ItemProcessType.FEE, scroll.getObjectId(), 1L, player, item) == null)
								{
									player.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
									PunishmentManager.handleIllegalPlayerAction(player, player + " tried to enchant with a scroll he doesn't have", GeneralConfig.DEFAULT_PUNISH);
									player.removeRequest(request.getClass());
									player.sendPacket(new EnchantResult(2, null, null, 0));
								}
								else if (support != null && player.getInventory().destroyItem(ItemProcessType.FEE, support.getObjectId(), 1L, player, item) == null)
								{
									player.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
									PunishmentManager.handleIllegalPlayerAction(player, player + " tried to enchant with a support item he doesn't have", GeneralConfig.DEFAULT_PUNISH);
									player.removeRequest(request.getClass());
									player.sendPacket(new EnchantResult(2, null, null, 0));
								}
								else
								{
									InventoryUpdate iu = new InventoryUpdate();
									synchronized (item)
									{
										if (item.getOwnerId() == player.getObjectId() && item.isEnchantable())
										{
											EnchantResultType resultType = scrollTemplate.calculateSuccess(player, item, supportTemplate);
											EnchantChallengePointData.EnchantChallengePointsItemInfo info = EnchantChallengePointData.getInstance().getInfoByItemId(item.getId());
											int challengePointsGroupId = -1;
											int challengePointsOptionIndex = -1;
											if (info != null)
											{
												int groupId = info.groupId();
												if (groupId == player.getChallengeInfo().getChallengePointsPendingRecharge()[0])
												{
													challengePointsGroupId = player.getChallengeInfo().getChallengePointsPendingRecharge()[0];
													challengePointsOptionIndex = player.getChallengeInfo().getChallengePointsPendingRecharge()[1];
												}
											}

											switch (resultType)
											{
												case ERROR:
													player.sendPacket(SystemMessageId.AUGMENTATION_REQUIREMENTS_ARE_NOT_FULFILLED);
													player.removeRequest(request.getClass());
													player.sendPacket(new EnchantResult(2, null, null, 0));
													break;
												case SUCCESS:
													ItemTemplate it = item.getTemplate();
													if (scrollTemplate.isCursed())
													{
														player.sendPacket(SystemMessageId.THE_ENCHANT_VALUE_IS_DECREASED_BY_1);
														item.setEnchantLevel(item.getEnchantLevel() - 1);
													}
													else if (scrollTemplate.getChance(player, item) > 0.0)
													{
														if (item.isEquipped())
														{
															item.clearSpecialAbilities();
															item.clearEnchantStats();
														}

														if (supportTemplate != null)
														{
															int randomSupportValue = Rnd.get(1, 100) >= supportTemplate.getRandomEnchantChance() ? supportTemplate.getRandomEnchantMin() : Rnd.get(supportTemplate.getRandomEnchantMin(), supportTemplate.getRandomEnchantMax());
															item.setEnchantLevel(Math.min(item.getEnchantLevel() + randomSupportValue, supportTemplate.getMaxEnchantLevel()));
														}

														if (supportTemplate == null)
														{
															int randomScrollValue = Rnd.get(1, 100) >= scrollTemplate.getRandomEnchantChance() ? scrollTemplate.getRandomEnchantMin() : Rnd.get(scrollTemplate.getRandomEnchantMin(), scrollTemplate.getRandomEnchantMax());
															item.setEnchantLevel(Math.min(item.getEnchantLevel() + randomScrollValue, scrollTemplate.getMaxEnchantLevel()));
														}
														else
														{
															int enchantValue = 1;
															if (challengePointsGroupId > 0 && challengePointsOptionIndex == 2)
															{
																EnchantChallengePointData.EnchantChallengePointsOptionInfo optionInfo = EnchantChallengePointData.getInstance().getOptionInfo(challengePointsGroupId, challengePointsOptionIndex);
																if (optionInfo != null && item.getEnchantLevel() >= optionInfo.minEnchant() && item.getEnchantLevel() <= optionInfo.maxEnchant() && Rnd.get(100) < optionInfo.chance())
																{
																	enchantValue = 2;
																}
															}

															item.setEnchantLevel(item.getEnchantLevel() + enchantValue);
														}

														if (item.isEquipped())
														{
															item.applySpecialAbilities();
															item.applyEnchantStats();
														}

														item.updateDatabase();
														iu.addModifiedItem(item);
														if (scroll.getCount() > 0L)
														{
															iu.addModifiedItem(scroll);
														}
														else
														{
															iu.addRemovedItem(scroll);
														}

														if (support != null)
														{
															if (support.getCount() > 0L)
															{
																iu.addModifiedItem(support);
															}
															else
															{
																iu.addRemovedItem(support);
															}
														}
													}

													player.sendPacket(new EnchantResult(0, new ItemHolder(item.getId(), 1L), null, item.getEnchantLevel()));
													if (GeneralConfig.LOG_ITEM_ENCHANTS)
													{
														StringBuilder sb = new StringBuilder();
														if (item.getEnchantLevel() > 0)
														{
															if (support == null)
															{
																LOGGER_ENCHANT.info(sb.append("Success, Character:").append(player.getName()).append(" [").append(player.getObjectId()).append("] Account:").append(player.getAccountName()).append(" IP:").append(player.getIPAddress()).append(", +").append(item.getEnchantLevel()).append(" ").append(item.getName()).append("(").append(item.getCount()).append(") [").append(item.getObjectId()).append("], ").append(scroll.getName()).append("(").append(scroll.getCount()).append(") [").append(scroll.getObjectId()).append("]").toString());
															}
															else
															{
																LOGGER_ENCHANT.info(sb.append("Success, Character:").append(player.getName()).append(" [").append(player.getObjectId()).append("] Account:").append(player.getAccountName()).append(" IP:").append(player.getIPAddress()).append(", +").append(item.getEnchantLevel()).append(" ").append(item.getName()).append("(").append(item.getCount()).append(") [").append(item.getObjectId()).append("], ").append(scroll.getName()).append("(").append(scroll.getCount()).append(") [").append(scroll.getObjectId()).append("], ").append(support.getName()).append("(").append(support.getCount()).append(") [").append(support.getObjectId()).append("]").toString());
															}
														}
														else if (support == null)
														{
															LOGGER_ENCHANT.info(sb.append("Success, Character:").append(player.getName()).append(" [").append(player.getObjectId()).append("] Account:").append(player.getAccountName()).append(" IP:").append(player.getIPAddress()).append(", ").append(item.getName()).append("(").append(item.getCount()).append(") [").append(item.getObjectId()).append("], ").append(scroll.getName()).append("(").append(scroll.getCount()).append(") [").append(scroll.getObjectId()).append("]").toString());
														}
														else
														{
															LOGGER_ENCHANT.info(sb.append("Success, Character:").append(player.getName()).append(" [").append(player.getObjectId()).append("] Account:").append(player.getAccountName()).append(" IP:").append(player.getIPAddress()).append(", ").append(item.getName()).append("(").append(item.getCount()).append(") [").append(item.getObjectId()).append("], ").append(scroll.getName()).append("(").append(scroll.getCount()).append(") [").append(scroll.getObjectId()).append("], ").append(support.getName()).append("(").append(support.getCount()).append(") [").append(support.getObjectId()).append("]").toString());
														}
													}

													if (item.getEnchantLevel() >= (item.isArmor() ? PlayerConfig.MIN_ARMOR_ENCHANT_ANNOUNCE : PlayerConfig.MIN_WEAPON_ENCHANT_ANNOUNCE) && item.getEnchantLevel() <= (item.isArmor() ? PlayerConfig.MAX_ARMOR_ENCHANT_ANNOUNCE : PlayerConfig.MAX_WEAPON_ENCHANT_ANNOUNCE))
													{
														SystemMessage sm = new SystemMessage(SystemMessageId.C1_HAS_ENCHANTED_S3_UP_TO_S2);
														sm.addString(player.getName());
														sm.addInt(item.getEnchantLevel());
														sm.addItemName(item);
														player.broadcastPacket(sm);
														Broadcast.toAllOnlinePlayers(new ExItemAnnounce(player, item, 0));
														Skill skill = CommonSkill.FIREWORK.getSkill();
														if (skill != null)
														{
															player.broadcastSkillPacket(new MagicSkillUse(player, player, skill.getId(), skill.getLevel(), skill.getHitTime(), skill.getReuseDelay()), player);
														}
													}

													if (item.isEquipped())
													{
														if (item.isArmor())
														{
															it.forEachSkill(ItemSkillType.ON_ENCHANT, holder -> {
																if (item.getEnchantLevel() >= holder.getValue())
																{
																	player.addSkill(holder.getSkill(), false);
																	player.sendSkillList();
																}
															});
														}

														player.broadcastUserInfo();
													}
													break;
												case FAILURE:
													boolean challengePointsSafe = false;
													if (challengePointsGroupId > 0 && challengePointsOptionIndex == 5)
													{
														EnchantChallengePointData.EnchantChallengePointsOptionInfo optionInfo = EnchantChallengePointData.getInstance().getOptionInfo(challengePointsGroupId, challengePointsOptionIndex);
														if (optionInfo != null && item.getEnchantLevel() >= optionInfo.minEnchant() && item.getEnchantLevel() <= optionInfo.maxEnchant() && Rnd.get(100) < optionInfo.chance())
														{
															challengePointsSafe = true;
														}
													}

													if (!challengePointsSafe && !scrollTemplate.isSafe())
													{
														if (item.isEquipped())
														{
															if (item.getEnchantLevel() > 0)
															{
																SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2_UNEQUIPPED);
																sm.addInt(item.getEnchantLevel());
																sm.addItemName(item);
																player.sendPacket(sm);
															}
															else
															{
																SystemMessage sm = new SystemMessage(SystemMessageId.S1_UNEQUIPPED);
																sm.addItemName(item);
																player.sendPacket(sm);
															}

															for (Item itm : player.getInventory().unEquipItemInSlotAndRecord(item.getLocationSlot()))
															{
																iu.addModifiedItem(itm);
															}

															player.sendInventoryUpdate(iu);
															player.broadcastUserInfo();
														}

														boolean challengePointsBlessed = false;
														boolean challengePointsBlessedDown = false;
														if (challengePointsGroupId > 0)
														{
															if (challengePointsOptionIndex == 3)
															{
																EnchantChallengePointData.EnchantChallengePointsOptionInfo optionInfo = EnchantChallengePointData.getInstance().getOptionInfo(challengePointsGroupId, challengePointsOptionIndex);
																if (optionInfo != null && item.getEnchantLevel() >= optionInfo.minEnchant() && item.getEnchantLevel() <= optionInfo.maxEnchant() && Rnd.get(100) < optionInfo.chance())
																{
																	challengePointsBlessed = true;
																}
															}
															else if (challengePointsOptionIndex == 4)
															{
																EnchantChallengePointData.EnchantChallengePointsOptionInfo optionInfo = EnchantChallengePointData.getInstance().getOptionInfo(challengePointsGroupId, challengePointsOptionIndex);
																if (optionInfo != null && item.getEnchantLevel() >= optionInfo.minEnchant() && item.getEnchantLevel() <= optionInfo.maxEnchant() && Rnd.get(100) < optionInfo.chance())
																{
																	challengePointsBlessedDown = true;
																}
															}
														}

														if (!challengePointsBlessed && !challengePointsBlessedDown && !scrollTemplate.isBlessed() && !scrollTemplate.isBlessedDown() && !scrollTemplate.isCursed() && (supportTemplate == null || !supportTemplate.isBlessed()))
														{
															EnchantChallengePointData.getInstance().handleFailure(player, item);
															player.sendPacket(new ExEnchantChallengePointInfo(player));
															BlackCouponManager.getInstance().createNewRecord(player.getObjectId(), item.getId(), (short) item.getEnchantLevel());
															if (player.getInventory().destroyItem(ItemProcessType.FEE, item, player, null) == null)
															{
																PunishmentManager.handleIllegalPlayerAction(player, "Unable to delete item on enchant failure from " + player + ", possible cheater !", GeneralConfig.DEFAULT_PUNISH);
																player.removeRequest(request.getClass());
																player.sendPacket(new EnchantResult(2, null, null, 0));
																if (GeneralConfig.LOG_ITEM_ENCHANTS)
																{
																	StringBuilder sb = new StringBuilder();
																	if (item.getEnchantLevel() > 0)
																	{
																		if (support == null)
																		{
																			LOGGER_ENCHANT.info(sb.append("Unable to destroy, Character:").append(player.getName()).append(" [").append(player.getObjectId()).append("] Account:").append(player.getAccountName()).append(" IP:").append(player.getIPAddress()).append(", +").append(item.getEnchantLevel()).append(" ").append(item.getName()).append("(").append(item.getCount()).append(") [").append(item.getObjectId()).append("], ").append(scroll.getName()).append("(").append(scroll.getCount()).append(") [").append(scroll.getObjectId()).append("]").toString());
																		}
																		else
																		{
																			LOGGER_ENCHANT.info(sb.append("Unable to destroy, Character:").append(player.getName()).append(" [").append(player.getObjectId()).append("] Account:").append(player.getAccountName()).append(" IP:").append(player.getIPAddress()).append(", +").append(item.getEnchantLevel()).append(" ").append(item.getName()).append("(").append(item.getCount()).append(") [").append(item.getObjectId()).append("], ").append(scroll.getName()).append("(").append(scroll.getCount()).append(") [").append(scroll.getObjectId()).append("], ").append(support.getName()).append("(").append(support.getCount()).append(") [").append(support.getObjectId()).append("]").toString());
																		}
																	}
																	else if (support == null)
																	{
																		LOGGER_ENCHANT.info(sb.append("Unable to destroy, Character:").append(player.getName()).append(" [").append(player.getObjectId()).append("] Account:").append(player.getAccountName()).append(" IP:").append(player.getIPAddress()).append(", ").append(item.getName()).append("(").append(item.getCount()).append(") [").append(item.getObjectId()).append("], ").append(scroll.getName()).append("(").append(scroll.getCount()).append(") [").append(scroll.getObjectId()).append("]").toString());
																	}
																	else
																	{
																		LOGGER_ENCHANT.info(sb.append("Unable to destroy, Character:").append(player.getName()).append(" [").append(player.getObjectId()).append("] Account:").append(player.getAccountName()).append(" IP:").append(player.getIPAddress()).append(", ").append(item.getName()).append("(").append(item.getCount()).append(") [").append(item.getObjectId()).append("], ").append(scroll.getName()).append("(").append(scroll.getCount()).append(") [").append(scroll.getObjectId()).append("], ").append(support.getName()).append("(").append(support.getCount()).append(") [").append(support.getObjectId()).append("]").toString());
																	}
																}

																return;
															}

															World.getInstance().removeObject(item);
															int count = 0;
															if (item.getTemplate().isCrystallizable())
															{
																count = Math.max(0, item.getCrystalCount() - (item.getTemplate().getCrystalCount() + 1) / 2);
															}

															Item crystals = null;
															int crystalId = item.getTemplate().getCrystalItemId();
															if (count > 0)
															{
																crystals = player.getInventory().addItem(ItemProcessType.COMPENSATE, crystalId, count, player, item);
																SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1_X_S2);
																sm.addItemName(crystals);
																sm.addLong(count);
																player.sendPacket(sm);
															}

															if (crystalId != 0 && count != 0)
															{
																List<ItemChanceHolder> destroyRewards = ItemCrystallizationData.getInstance().getRewardItems(item, item.getEnchantLevel());
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
															}
															else
															{
																player.sendPacket(new EnchantResult(4, null, null, 0));
															}

															player.sendPacket(new ExEnchantChallengePointInfo(player));
															if (GeneralConfig.LOG_ITEM_ENCHANTS)
															{
																StringBuilder sb = new StringBuilder();
																if (item.getEnchantLevel() > 0)
																{
																	if (support == null)
																	{
																		LOGGER_ENCHANT.info(sb.append("Fail, Character:").append(player.getName()).append(" [").append(player.getObjectId()).append("] Account:").append(player.getAccountName()).append(" IP:").append(player.getIPAddress()).append(", +").append(item.getEnchantLevel()).append(" ").append(item.getName()).append("(").append(item.getCount()).append(") [").append(item.getObjectId()).append("], ").append(scroll.getName()).append("(").append(scroll.getCount()).append(") [").append(scroll.getObjectId()).append("]").toString());
																	}
																	else
																	{
																		LOGGER_ENCHANT.info(sb.append("Fail, Character:").append(player.getName()).append(" [").append(player.getObjectId()).append("] Account:").append(player.getAccountName()).append(" IP:").append(player.getIPAddress()).append(", +").append(item.getEnchantLevel()).append(" ").append(item.getName()).append("(").append(item.getCount()).append(") [").append(item.getObjectId()).append("], ").append(scroll.getName()).append("(").append(scroll.getCount()).append(") [").append(scroll.getObjectId()).append("], ").append(support.getName()).append("(").append(support.getCount()).append(") [").append(support.getObjectId()).append("]").toString());
																	}
																}
																else if (support == null)
																{
																	LOGGER_ENCHANT.info(sb.append("Fail, Character:").append(player.getName()).append(" [").append(player.getObjectId()).append("] Account:").append(player.getAccountName()).append(" IP:").append(player.getIPAddress()).append(", ").append(item.getName()).append("(").append(item.getCount()).append(") [").append(item.getObjectId()).append("], ").append(scroll.getName()).append("(").append(scroll.getCount()).append(") [").append(scroll.getObjectId()).append("]").toString());
																}
																else
																{
																	LOGGER_ENCHANT.info(sb.append("Fail, Character:").append(player.getName()).append(" [").append(player.getObjectId()).append("] Account:").append(player.getAccountName()).append(" IP:").append(player.getIPAddress()).append(", ").append(item.getName()).append("(").append(item.getCount()).append(") [").append(item.getObjectId()).append("], ").append(scroll.getName()).append("(").append(scroll.getCount()).append(") [").append(scroll.getObjectId()).append("], ").append(support.getName()).append("(").append(support.getCount()).append(") [").append(support.getObjectId()).append("]").toString());
																}
															}
														}
														else
														{
															if (!scrollTemplate.isBlessedDown() && !challengePointsBlessedDown && !scrollTemplate.isCursed())
															{
																player.sendPacket(SystemMessageId.THE_BLESSED_ENCHANT_FAILED_THE_ENCHANT_VALUE_OF_THE_ITEM_BECAME_0);
																item.setEnchantLevel(0);
															}
															else
															{
																player.sendPacket(SystemMessageId.THE_ENCHANT_VALUE_IS_DECREASED_BY_1);
																item.setEnchantLevel(Math.max(0, item.getEnchantLevel() - 1));
															}

															player.sendPacket(new EnchantResult(1, new ItemHolder(item.getId(), 1L), null, item.getEnchantLevel()));
															item.updateDatabase();
															if (GeneralConfig.LOG_ITEM_ENCHANTS)
															{
																StringBuilder sb = new StringBuilder();
																if (item.getEnchantLevel() > 0)
																{
																	if (support == null)
																	{
																		LOGGER_ENCHANT.info(sb.append("Blessed Fail, Character:").append(player.getName()).append(" [").append(player.getObjectId()).append("] Account:").append(player.getAccountName()).append(" IP:").append(player.getIPAddress()).append(", +").append(item.getEnchantLevel()).append(" ").append(item.getName()).append("(").append(item.getCount()).append(") [").append(item.getObjectId()).append("], ").append(scroll.getName()).append("(").append(scroll.getCount()).append(") [").append(scroll.getObjectId()).append("]").toString());
																	}
																	else
																	{
																		LOGGER_ENCHANT.info(sb.append("Blessed Fail, Character:").append(player.getName()).append(" [").append(player.getObjectId()).append("] Account:").append(player.getAccountName()).append(" IP:").append(player.getIPAddress()).append(", +").append(item.getEnchantLevel()).append(" ").append(item.getName()).append("(").append(item.getCount()).append(") [").append(item.getObjectId()).append("], ").append(scroll.getName()).append("(").append(scroll.getCount()).append(") [").append(scroll.getObjectId()).append("], ").append(support.getName()).append("(").append(support.getCount()).append(") [").append(support.getObjectId()).append("]").toString());
																	}
																}
																else if (support == null)
																{
																	LOGGER_ENCHANT.info(sb.append("Blessed Fail, Character:").append(player.getName()).append(" [").append(player.getObjectId()).append("] Account:").append(player.getAccountName()).append(" IP:").append(player.getIPAddress()).append(", ").append(item.getName()).append("(").append(item.getCount()).append(") [").append(item.getObjectId()).append("], ").append(scroll.getName()).append("(").append(scroll.getCount()).append(") [").append(scroll.getObjectId()).append("]").toString());
																}
																else
																{
																	LOGGER_ENCHANT.info(sb.append("Blessed Fail, Character:").append(player.getName()).append(" [").append(player.getObjectId()).append("] Account:").append(player.getAccountName()).append(" IP:").append(player.getIPAddress()).append(", ").append(item.getName()).append("(").append(item.getCount()).append(") [").append(item.getObjectId()).append("], ").append(scroll.getName()).append("(").append(scroll.getCount()).append(") [").append(scroll.getObjectId()).append("], ").append(support.getName()).append("(").append(support.getCount()).append(") [").append(support.getObjectId()).append("]").toString());
																}
															}
														}
													}
													else
													{
														player.sendPacket(SystemMessageId.ENCHANT_FAILED_THE_ENCHANT_SKILL_FOR_THE_CORRESPONDING_ITEM_WILL_BE_EXACTLY_RETAINED);
														player.sendPacket(new EnchantResult(10, new ItemHolder(item.getId(), 1L), null, item.getEnchantLevel()));
														if (GeneralConfig.LOG_ITEM_ENCHANTS)
														{
															StringBuilder sb = new StringBuilder();
															if (item.getEnchantLevel() > 0)
															{
																if (support == null)
																{
																	LOGGER_ENCHANT.info(sb.append("Safe Fail, Character:").append(player.getName()).append(" [").append(player.getObjectId()).append("] Account:").append(player.getAccountName()).append(" IP:").append(player.getIPAddress()).append(", +").append(item.getEnchantLevel()).append(" ").append(item.getName()).append("(").append(item.getCount()).append(") [").append(item.getObjectId()).append("], ").append(scroll.getName()).append("(").append(scroll.getCount()).append(") [").append(scroll.getObjectId()).append("]").toString());
																}
																else
																{
																	LOGGER_ENCHANT.info(sb.append("Safe Fail, Character:").append(player.getName()).append(" [").append(player.getObjectId()).append("] Account:").append(player.getAccountName()).append(" IP:").append(player.getIPAddress()).append(", +").append(item.getEnchantLevel()).append(" ").append(item.getName()).append("(").append(item.getCount()).append(") [").append(item.getObjectId()).append("], ").append(scroll.getName()).append("(").append(scroll.getCount()).append(") [").append(scroll.getObjectId()).append("], ").append(support.getName()).append("(").append(support.getCount()).append(") [").append(support.getObjectId()).append("]").toString());
																}
															}
															else if (support == null)
															{
																LOGGER_ENCHANT.info(sb.append("Safe Fail, Character:").append(player.getName()).append(" [").append(player.getObjectId()).append("] Account:").append(player.getAccountName()).append(" IP:").append(player.getIPAddress()).append(", ").append(item.getName()).append("(").append(item.getCount()).append(") [").append(item.getObjectId()).append("], ").append(scroll.getName()).append("(").append(scroll.getCount()).append(") [").append(scroll.getObjectId()).append("]").toString());
															}
															else
															{
																LOGGER_ENCHANT.info(sb.append("Safe Fail, Character:").append(player.getName()).append(" [").append(player.getObjectId()).append("] Account:").append(player.getAccountName()).append(" IP:").append(player.getIPAddress()).append(", ").append(item.getName()).append("(").append(item.getCount()).append(") [").append(item.getObjectId()).append("], ").append(scroll.getName()).append("(").append(scroll.getCount()).append(") [").append(scroll.getObjectId()).append("], ").append(support.getName()).append("(").append(support.getCount()).append(") [").append(support.getObjectId()).append("]").toString());
															}
														}
													}
											}

											if (challengePointsGroupId >= 0)
											{
												player.getChallengeInfo().setChallengePointsPendingRecharge(-1, -1);
												player.getChallengeInfo().addChallengePointsRecharge(challengePointsGroupId, challengePointsOptionIndex, -1);
												player.sendPacket(new ExEnchantChallengePointInfo(player));
											}

											player.sendItemList();
											player.broadcastUserInfo();
											request.setProcessing(false);
										}
										else
										{
											player.sendPacket(SystemMessageId.AUGMENTATION_REQUIREMENTS_ARE_NOT_FULFILLED);
											player.removeRequest(request.getClass());
											player.sendPacket(new EnchantResult(2, null, null, 0));
										}
									}
								}
							}
							else
							{
								player.sendPacket(SystemMessageId.AUGMENTATION_REQUIREMENTS_ARE_NOT_FULFILLED);
								player.removeRequest(request.getClass());
								player.sendPacket(new EnchantResult(2, null, null, 0));
							}
						}
					}
					else
					{
						player.removeRequest(request.getClass());
					}
				}
				else
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_ENCHANT_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
					player.removeRequest(request.getClass());
				}
			}
		}
	}
}
