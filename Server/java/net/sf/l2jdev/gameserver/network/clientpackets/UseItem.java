package net.sf.l2jdev.gameserver.network.clientpackets;

import java.util.List;
import java.util.concurrent.TimeUnit;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.ai.CreatureAI;
import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.ai.NextAction;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.xml.EnchantItemGroupsData;
import net.sf.l2jdev.gameserver.handler.AdminCommandHandler;
import net.sf.l2jdev.gameserver.handler.IItemHandler;
import net.sf.l2jdev.gameserver.handler.ItemHandler;
import net.sf.l2jdev.gameserver.managers.FortSiegeManager;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.IllegalActionPunishmentType;
import net.sf.l2jdev.gameserver.model.actor.request.AutoPeelRequest;
import net.sf.l2jdev.gameserver.model.effects.EffectType;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.item.OnItemUse;
import net.sf.l2jdev.gameserver.model.item.EtcItem;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.enums.ItemSkillType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemSkillHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.item.type.ActionType;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPutIntensiveResultForVariationMake;
import net.sf.l2jdev.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.ExUseSharedGroupItem;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.ensoul.ExShowEnsoulWindow;
import net.sf.l2jdev.gameserver.network.serverpackets.variation.ExShowVariationMakeWindow;

public class UseItem extends ClientPacket
{
	private int _objectId;
	private boolean _ctrlPressed;
	private int _itemId;

	@Override
	protected void readImpl()
	{
		this._objectId = this.readInt();
		this._ctrlPressed = this.readInt() != 0;
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (this.getClient().getFloodProtectors().canUseItem())
			{
				if (player.isInsideZone(ZoneId.JAIL))
				{
					player.sendMessage("You cannot use items while jailed.");
				}
				else if (player.getActiveTradeList() != null)
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_PICK_UP_OR_USE_ITEMS_WHILE_TRADING);
				}
				else if (player.isInStoreMode())
				{
					player.sendPacket(SystemMessageId.ITEMS_IN_A_PRIVATE_STORE_OR_A_PRIVATE_WORKSHOP_CANNOT_BE_USED);
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else
				{
					Item item = player.getInventory().getItemByObjectId(this._objectId);
					if (item == null)
					{
						if (player.isGM())
						{
							WorldObject obj = World.getInstance().findObject(this._objectId);
							if (obj != null && obj.isItem())
							{
								AdminCommandHandler.getInstance().onCommand(player, "admin_use_item " + this._objectId, true);
							}
						}
					}
					else if (item.isQuestItem() && !item.getTemplate().isQuestUsableItem() && item.getTemplate().getDefaultAction() != ActionType.NONE)
					{
						player.sendPacket(SystemMessageId.YOU_CANNOT_USE_QUEST_ITEMS);
					}
					else if (!player.hasBlockActions() && !player.isControlBlocked() && !player.isAlikeDead())
					{
						if (player.isDead() || !player.getInventory().canManipulateWithItemId(item.getId()))
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_THE_REQUIREMENTS_ARE_NOT_MET);
							sm.addItemName(item);
							player.sendPacket(sm);
						}
						else if (item.isEquipped() || item.getTemplate().checkCondition(player, player, true))
						{
							this._itemId = item.getId();
							if (!player.isFishing() || this._itemId >= 6535 && this._itemId <= 6540)
							{
								if (!PlayerConfig.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT && player.getReputation() < 0)
								{
									List<ItemSkillHolder> skills = item.getTemplate().getSkills(ItemSkillType.NORMAL);
									if (skills != null)
									{
										for (ItemSkillHolder holder : skills)
										{
											if (holder.getSkill().hasEffectType(EffectType.TELEPORT))
											{
												return;
											}
										}
									}
								}

								int reuseDelay = item.getReuseDelay();
								int sharedReuseGroup = item.getSharedReuseGroup();
								if (reuseDelay > 0)
								{
									long reuse = player.getItemRemainingReuseTime(item.getObjectId());
									if (reuse > 0L)
									{
										this.reuseData(player, item, reuse);
										this.sendSharedGroupUpdate(player, sharedReuseGroup, reuse, reuseDelay);
										return;
									}

									long reuseOnGroup = player.getReuseDelayOnGroup(sharedReuseGroup);
									if (reuseOnGroup > 0L)
									{
										this.reuseData(player, item, reuseOnGroup);
										this.sendSharedGroupUpdate(player, sharedReuseGroup, reuseOnGroup, reuseDelay);
										return;
									}
								}

								player.onActionRequest();
								if (item.isEquipable())
								{
									if ((player.isCursedWeaponEquipped() && this._itemId == 6408) || FortSiegeManager.getInstance().isCombat(this._itemId) || player.isCombatFlagEquipped())
									{
										return;
									}

									if (player.getInventory().isItemSlotBlocked(item.getTemplate().getBodyPart()))
									{
										player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM);
										return;
									}

									if (!player.canUseEquipment(item))
									{
										player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM);
										return;
									}

									switch (item.getTemplate().getBodyPart())
									{
										case LR_HAND:
										case L_HAND:
										case R_HAND:
											if (player.getActiveWeaponItem() != null && player.getActiveWeaponItem().getId() == 93331)
											{
												player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM);
												return;
											}

											if (player.isMounted() || player.isDisarmed())
											{
												player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM);
												return;
											}

											if (player.isCursedWeaponEquipped())
											{
												return;
											}
											break;
										case DECO:
											if (!item.isEquipped() && player.getInventory().getTalismanSlots() == 0)
											{
												player.sendPacket(SystemMessageId.NO_EQUIPMENT_SLOT_AVAILABLE);
												return;
											}
											break;
										case BROOCH_JEWEL:
											if (!item.isEquipped() && player.getInventory().getBroochJewelSlots() == 0)
											{
												SystemMessage sm = new SystemMessage(SystemMessageId.YOU_CANNOT_EQUIP_S1_WITHOUT_EQUIPPING_A_BROOCH);
												sm.addItemName(item);
												player.sendPacket(sm);
												return;
											}
											break;
										case AGATHION:
											if (!item.isEquipped() && player.getInventory().getAgathionSlots() == 0)
											{
												player.sendPacket(SystemMessageId.NO_EQUIPMENT_SLOT_AVAILABLE);
												return;
											}
											break;
										case ARTIFACT:
											if (!item.isEquipped() && player.getInventory().getArtifactSlots() == 0)
											{
												SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM);
												sm.addItemName(item);
												player.sendPacket(sm);
												return;
											}
									}

									if (PlayerConfig.OVER_ENCHANT_PROTECTION && !player.isGM() && (item.isWeapon() && item.getEnchantLevel() > EnchantItemGroupsData.getInstance().getMaxWeaponEnchant() || item.getTemplate().getType2() == 2 && item.getEnchantLevel() > EnchantItemGroupsData.getInstance().getMaxAccessoryEnchant() || item.isArmor() && item.getTemplate().getType2() != 2 && item.getEnchantLevel() > EnchantItemGroupsData.getInstance().getMaxArmorEnchant()))
									{
										PacketLogger.info("Over-enchanted (+" + item.getEnchantLevel() + ") " + item + " has been removed from " + player);
										player.getInventory().destroyItem(ItemProcessType.DESTROY, item, player, null);
										if (PlayerConfig.OVER_ENCHANT_PUNISHMENT != IllegalActionPunishmentType.NONE)
										{
											player.sendMessage("[Server]: You have over-enchanted items!");
											player.sendMessage("[Server]: Respect our server rules.");
											player.sendPacket(new ExShowScreenMessage("You have over-enchanted items!", 6000));
											PunishmentManager.handleIllegalPlayerAction(player, player.getName() + " has over-enchanted items.", PlayerConfig.OVER_ENCHANT_PUNISHMENT);
										}

										return;
									}

									if (player.isCastingNow())
									{
										CreatureAI ai = player.getAI();
										ai.setNextAction(new NextAction(net.sf.l2jdev.gameserver.ai.Action.FINISH_CASTING, Intention.CAST, () -> {
											ai.setNextAction(null);
											player.useEquippableItem(item, !player.isAutoPlaying());
										}));
									}
									else
									{
										long currentTime = System.nanoTime();
										long attackEndTime = player.getAttackEndTime();
										if (attackEndTime > currentTime)
										{
											ThreadPool.schedule(() -> player.useEquippableItem(item, false), TimeUnit.NANOSECONDS.toMillis(attackEndTime - currentTime));
										}
										else
										{
											player.useEquippableItem(item, true);
										}
									}
								}
								else
								{
									EtcItem etcItem = item.getEtcItem();
									if (etcItem != null && etcItem.getExtractableItems() != null && player.hasRequest(AutoPeelRequest.class))
									{
										return;
									}

									IItemHandler handler = ItemHandler.getInstance().getHandler(etcItem);
									if (handler == null)
									{
										if (etcItem != null && etcItem.getHandlerName() != null)
										{
											PacketLogger.warning("Unmanaged Item handler: " + etcItem.getHandlerName() + " for Item Id: " + this._itemId + "!");
										}
									}
									else if (handler.onItemUse(player, item, this._ctrlPressed))
									{
										if (reuseDelay > 0)
										{
											player.addTimeStampItem(item, reuseDelay);
											this.sendSharedGroupUpdate(player, sharedReuseGroup, reuseDelay, reuseDelay);
										}

										if (EventDispatcher.getInstance().hasListener(EventType.ON_ITEM_USE, item.getTemplate()))
										{
											EventDispatcher.getInstance().notifyEventAsync(new OnItemUse(player, item), item.getTemplate());
										}
									}

									if (etcItem != null)
									{
										if (etcItem.isMineral())
										{
											player.sendPacket(ExShowVariationMakeWindow.STATIC_PACKET);
											player.sendPacket(new ExPutIntensiveResultForVariationMake(item.getObjectId()));
										}
										else if (etcItem.isEnsoulStone())
										{
											player.sendPacket(ExShowEnsoulWindow.STATIC_PACKET);
										}
									}
								}
							}
							else
							{
								player.sendPacket(SystemMessageId.YOU_CANNOT_DO_THAT_WHILE_FISHING_3);
							}
						}
					}
				}
			}
		}
	}

	protected void reuseData(Player player, Item item, long remainingTime)
	{
		int hours = (int) (remainingTime / 3600000L);
		int minutes = (int) (remainingTime % 3600000L) / 60000;
		int seconds = (int) (remainingTime / 1000L % 60L);
		SystemMessage sm;
		if (hours > 0)
		{
			sm = new SystemMessage(SystemMessageId.S1_WILL_BE_AVAILABLE_AGAIN_IN_S2_H_S3_MIN_S4_SEC);
			sm.addItemName(item);
			sm.addInt(hours);
			sm.addInt(minutes);
		}
		else if (minutes > 0)
		{
			sm = new SystemMessage(SystemMessageId.S1_WILL_BE_AVAILABLE_AGAIN_IN_S2_MIN_S3_SEC);
			sm.addItemName(item);
			sm.addInt(minutes);
		}
		else
		{
			sm = new SystemMessage(SystemMessageId.S1_WILL_BE_AVAILABLE_AGAIN_IN_S2_SEC);
			sm.addItemName(item);
		}

		sm.addInt(seconds);
		player.sendPacket(sm);
	}

	private void sendSharedGroupUpdate(Player player, int group, long remaining, int reuse)
	{
		if (group > 0)
		{
			player.sendPacket(new ExUseSharedGroupItem(this._itemId, group, remaining, reuse));
		}
	}
}
