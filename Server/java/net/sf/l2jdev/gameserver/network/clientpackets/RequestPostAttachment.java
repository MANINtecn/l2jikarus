package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.managers.ItemManager;
import net.sf.l2jdev.gameserver.managers.MailManager;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.model.Message;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemLocation;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.ItemContainer;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ExChangePostState;
import net.sf.l2jdev.gameserver.network.serverpackets.ExShowReceivedPostList;
import net.sf.l2jdev.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class RequestPostAttachment extends ClientPacket
{
	private int _msgId;

	@Override
	protected void readImpl()
	{
		this._msgId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		if (GeneralConfig.ALLOW_MAIL && GeneralConfig.ALLOW_ATTACHMENTS)
		{
			Player player = this.getPlayer();
			if (player != null)
			{
				if (this.getClient().getFloodProtectors().canPerformTransaction())
				{
					if (!player.getAccessLevel().allowTransaction())
					{
						player.sendMessage("Transactions are disabled for your Access Level");
					}
					else if (player.isInCombat())
					{
						player.sendPacket(SystemMessageId.NOT_AVAILABLE_IN_COMBAT);
					}
					else if (player.isDead())
					{
						player.sendPacket(SystemMessageId.YOU_ARE_DEAD_AND_CANNOT_PERFORM_THIS_ACTION);
					}
					else if (player.getActiveTradeList() != null)
					{
						player.sendPacket(SystemMessageId.YOU_CANNOT_RECEIVE_DURING_AN_EXCHANGE);
					}
					else if (player.hasItemRequest())
					{
						player.sendPacket(SystemMessageId.YOU_CANNOT_RECEIVE_ANYTHING_WHILE_ENCHANTING_IMBUING_WITH_ATTRIBUTES_OR_COMPOUNDING_WITH_JEWELS);
					}
					else if (player.isInStoreMode())
					{
						player.sendPacket(SystemMessageId.YOU_CANNOT_RECEIVE_BECAUSE_THE_PRIVATE_STORE_OR_WORKSHOP_IS_IN_PROGRESS);
					}
					else
					{
						Message msg = MailManager.getInstance().getMessage(this._msgId);
						if (msg != null)
						{
							if (msg.getReceiverId() != player.getObjectId())
							{
								PunishmentManager.handleIllegalPlayerAction(player, player + " tried to get not own attachment!", GeneralConfig.DEFAULT_PUNISH);
							}
							else if (msg.hasAttachments())
							{
								ItemContainer attachments = msg.getAttachments();
								if (attachments != null)
								{
									int weight = 0;
									int slots = 0;

									for (Item item : attachments.getItems())
									{
										if (item != null)
										{
											if (item.getOwnerId() != msg.getSenderId())
											{
												PunishmentManager.handleIllegalPlayerAction(player, player + " tried to get wrong item (ownerId != senderId) from attachment!", GeneralConfig.DEFAULT_PUNISH);
												return;
											}

											if (item.getItemLocation() != ItemLocation.MAIL)
											{
												PunishmentManager.handleIllegalPlayerAction(player, player + " tried to get wrong item (Location != MAIL) from attachment!", GeneralConfig.DEFAULT_PUNISH);
												return;
											}

											if (item.getLocationSlot() != msg.getId())
											{
												PunishmentManager.handleIllegalPlayerAction(player, player + " tried to get items from different attachment!", GeneralConfig.DEFAULT_PUNISH);
												return;
											}

											weight = (int) (weight + item.getCount() * item.getTemplate().getWeight());
											if (!item.isStackable())
											{
												slots = (int) (slots + item.getCount());
											}
											else if (player.getInventory().getItemByItemId(item.getId()) == null)
											{
												slots++;
											}
										}
									}

									if (!player.getInventory().validateCapacity(slots))
									{
										player.sendPacket(SystemMessageId.YOU_COULD_NOT_RECEIVE_BECAUSE_YOUR_INVENTORY_IS_FULL);
									}
									else if (!player.getInventory().validateWeight(weight))
									{
										player.sendPacket(SystemMessageId.YOU_COULD_NOT_RECEIVE_BECAUSE_YOUR_INVENTORY_IS_FULL);
									}
									else
									{
										long adena = msg.getReqAdena();
										if (adena > 0L && !player.reduceAdena(ItemProcessType.FEE, adena, null, true))
										{
											player.sendPacket(SystemMessageId.YOU_CANNOT_RECEIVE_BECAUSE_YOU_DON_T_HAVE_ENOUGH_ADENA);
										}
										else
										{
											InventoryUpdate playerIU = new InventoryUpdate();

											for (Item itemx : attachments.getItems())
											{
												if (itemx != null)
												{
													if (itemx.getOwnerId() != msg.getSenderId())
													{
														PunishmentManager.handleIllegalPlayerAction(player, player + " tried to get items with owner != sender !", GeneralConfig.DEFAULT_PUNISH);
														return;
													}

													long count = itemx.getCount();
													Item newItem = attachments.transferItem(ItemProcessType.TRANSFER, itemx.getObjectId(), itemx.getCount(), player.getInventory(), player, null);
													if (newItem == null)
													{
														return;
													}

													if (newItem.isStackable() && newItem.getCount() > count)
													{
														playerIU.addModifiedItem(newItem);
													}
													else
													{
														playerIU.addNewItem(newItem);
													}

													SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1_X_S2);
													sm.addItemName(itemx.getId());
													sm.addLong(count);
													player.sendPacket(sm);
												}
											}

											player.sendInventoryUpdate(playerIU);
											player.sendItemList();
											msg.removeAttachments();
											Player sender = World.getInstance().getPlayer(msg.getSenderId());
											if (adena > 0L)
											{
												if (sender != null)
												{
													sender.addAdena(ItemProcessType.TRANSFER, adena, player, false);
													SystemMessage sm = new SystemMessage(SystemMessageId.S2_COMPLETED_THE_PAYMENT_AND_YOU_RECEIVE_S1_ADENA);
													sm.addLong(adena);
													sm.addString(player.getName());
													sender.sendPacket(sm);
												}
												else
												{
													Item paidAdena = ItemManager.createItem(ItemProcessType.FEE, 57, adena, player, null);
													paidAdena.setOwnerId(msg.getSenderId());
													paidAdena.setItemLocation(ItemLocation.INVENTORY);
													paidAdena.updateDatabase(true);
													World.getInstance().removeObject(paidAdena);
												}
											}
											else if (sender != null)
											{
												SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_OBTAINED_THE_ITEM_ATTACHED_TO_THE_MAIL);
												sm.addString(player.getName());
												sender.sendPacket(sm);
											}

											player.sendPacket(new ExChangePostState(true, this._msgId, 1));
											player.sendPacket(SystemMessageId.THE_ITEM_IS_RECEIVED);
											player.sendPacket(new ExShowReceivedPostList(player.getObjectId()));
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
