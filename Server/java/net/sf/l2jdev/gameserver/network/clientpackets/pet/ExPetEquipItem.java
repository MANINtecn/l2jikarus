package net.sf.l2jdev.gameserver.network.clientpackets.pet;

import java.util.concurrent.TimeUnit;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.ai.Action;
import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.ai.NextAction;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.instance.Pet;
import net.sf.l2jdev.gameserver.model.item.enums.BodyPart;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.item.type.ArmorType;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.pet.ExPetSkillList;
import net.sf.l2jdev.gameserver.network.serverpackets.pet.PetSummonInfo;

public class ExPetEquipItem extends ClientPacket
{
	private int _objectId;
	private int _itemId;

	@Override
	protected void readImpl()
	{
		this._objectId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Pet pet = player.getPet();
			if (pet != null)
			{
				if (this.getClient().getFloodProtectors().canUseItem())
				{
					if (player.isInsideZone(ZoneId.JAIL))
					{
						player.sendMessage("You cannot use items while jailed.");
					}
					else
					{
						if (player.getActiveTradeList() != null)
						{
							player.cancelActiveTrade();
						}

						if (player.isInStoreMode())
						{
							player.sendPacket(SystemMessageId.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
							player.sendPacket(ActionFailed.STATIC_PACKET);
						}
						else
						{
							Item item = player.getInventory().getItemByObjectId(this._objectId);
							if (item != null)
							{
								if (!player.hasBlockActions() && !player.isControlBlocked() && !player.isAlikeDead())
								{
									if (player.isDead() || pet.isDead() || !player.getInventory().canManipulateWithItemId(item.getId()))
									{
										SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_THE_REQUIREMENTS_ARE_NOT_MET);
										sm.addItemName(item);
										player.sendPacket(sm);
									}
									else if (item.isEquipable())
									{
										this._itemId = item.getId();
										if (!player.isFishing() || this._itemId >= 6535 && this._itemId <= 6540)
										{
											player.onActionRequest();
											if (item.isEquipable())
											{
												if (pet.getInventory().isItemSlotBlocked(item.getTemplate().getBodyPart()) || (item.getTemplate().getBodyPart().getMask() > BodyPart.HAIRALL.getMask()) || (item.isWeapon() && item.isEnchanted()))
												{
													player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM);
													return;
												}

												if (item.isArmor() && (item.getArmorItem().getItemType() == ArmorType.SHIELD || item.getArmorItem().getItemType() == ArmorType.SIGIL))
												{
													player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM);
													return;
												}

												if (item.isTimeLimitedItem() || item.isShadowItem())
												{
													player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM);
													return;
												}

												if (item.getTemplate().getAdditionalName() != null && item.getTemplate().getAdditionalName().toLowerCase().contains("event"))
												{
													player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM);
													return;
												}

												Item oldItem = pet.getInventory().getPaperdollItemByBodyPart(item.getTemplate().getBodyPart());
												if (oldItem != null)
												{
													pet.transferItem(ItemProcessType.TRANSFER, oldItem.getObjectId(), 1L, player.getInventory(), player, null);
												}

												if (player.isCastingNow())
												{
													player.getAI().setNextAction(new NextAction(Action.FINISH_CASTING, Intention.CAST, () -> {
														Item transferedItemx = player.transferItem(ItemProcessType.TRANSFER, item.getObjectId(), 1L, pet.getInventory(), null);
														pet.useEquippableItem(transferedItemx, false);
														this.sendInfos(pet, player);
													}));
												}
												else if (player.isAttackingNow())
												{
													ThreadPool.schedule(() -> {
														Item transferedItemx = player.transferItem(ItemProcessType.TRANSFER, item.getObjectId(), 1L, pet.getInventory(), null);
														pet.useEquippableItem(transferedItemx, false);
														this.sendInfos(pet, player);
													}, player.getAttackEndTime() - TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis()));
												}
												else
												{
													Item transferedItem = player.transferItem(ItemProcessType.TRANSFER, item.getObjectId(), 1L, pet.getInventory(), null);
													pet.useEquippableItem(transferedItem, false);
													this.sendInfos(pet, player);
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
			}
		}
	}

	protected void sendInfos(Pet pet, Player player)
	{
		pet.getStat().recalculateStats(true);
		player.sendPacket(new PetSummonInfo(pet, 1));
		player.sendPacket(new ExPetSkillList(false, pet));
	}
}
