package net.sf.l2jdev.gameserver.network.clientpackets.ensoul;

import net.sf.l2jdev.gameserver.data.xml.EnsoulData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.ensoul.EnsoulOption;
import net.sf.l2jdev.gameserver.model.ensoul.EnsoulStone;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.AbnormalType;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.ensoul.ExEnsoulResult;
import net.sf.l2jdev.gameserver.taskmanagers.AttackStanceTaskManager;

public class RequestItemEnsoul extends ClientPacket
{
	private int _itemObjectId;
	private int _type;
	private RequestItemEnsoul.EnsoulItemOption[] _options;

	@Override
	protected void readImpl()
	{
		this._itemObjectId = this.readInt();
		int options = this.readByte();
		if (options > 0 && options <= 3)
		{
			this._options = new RequestItemEnsoul.EnsoulItemOption[options];

			for (int i = 0; i < options; i++)
			{
				this._type = this.readByte();
				int position = this.readByte();
				int soulCrystalObjectId = this.readInt();
				int soulCrystalOption = this.readInt();
				if (position > 0 && position < 3 && (this._type == 1 || this._type == 2))
				{
					this._options[i] = new RequestItemEnsoul.EnsoulItemOption(this._type, position, soulCrystalObjectId, soulCrystalOption);
				}
			}
		}
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (player.isInStoreMode())
			{
				player.sendPacket(SystemMessageId.SOUL_CRYSTAL_INSERTION_IS_IMPOSSIBLE_WHEN_PRIVATE_STORE_AND_WORKSHOP_ARE_OPENED);
			}
			else
			{
				if (player.hasAbnormalType(AbnormalType.FREEZING))
				{
					player.sendPacket(SystemMessageId.SOUL_CRYSTAL_INSERTION_IS_IMPOSSIBLE_WHILE_IN_FROZEN_STATE);
				}

				if (player.isDead())
				{
					player.sendPacket(SystemMessageId.SOUL_CRYSTAL_INSERTION_IS_IMPOSSIBLE_IF_THE_CHARACTER_IS_DEAD);
				}
				else if (player.getActiveTradeList() != null || player.hasItemRequest())
				{
					player.sendPacket(SystemMessageId.SOUL_CRYSTAL_INSERTION_IS_IMPOSSIBLE_DURING_EXCHANGE);
				}
				else if (player.hasAbnormalType(AbnormalType.PARALYZE))
				{
					player.sendPacket(SystemMessageId.SOUL_CRYSTAL_INSERTION_IS_IMPOSSIBLE_WHILE_THE_CHARACTER_IS_PETRIFIED);
				}
				else if (player.isFishing())
				{
					player.sendPacket(SystemMessageId.SOUL_CRYSTAL_INSERTION_IS_IMPOSSIBLE_DURING_FISHING);
				}
				else if (player.isSitting())
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_INSERT_SOUL_CRYSTALS_WHILE_SITTING);
				}
				else if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(player))
				{
					player.sendPacket(SystemMessageId.SOUL_CRYSTAL_INSERTION_IS_IMPOSSIBLE_DURING_COMBAT);
				}
				else
				{
					Item item = player.getInventory().getItemByObjectId(this._itemObjectId);
					if (item == null)
					{
						PacketLogger.warning("Player: " + player + " attempting to ensoul item without having it!");
					}
					else
					{
						ItemTemplate template = item.getTemplate();
						if (this._type == 1 && template.getEnsoulSlots() == 0)
						{
							PacketLogger.warning("Player: " + player + " attempting to ensoul non ensoulable item: " + item + "!");
						}
						else if (this._type == 2 && template.getSpecialEnsoulSlots() == 0)
						{
							PacketLogger.warning("Player: " + player + " attempting to special ensoul non special ensoulable item: " + item + "!");
						}
						else if (!item.isEquipable())
						{
							PacketLogger.warning("Player: " + player + " attempting to ensoul non equippable item: " + item + "!");
						}
						else if (item.isCommonItem())
						{
							PacketLogger.warning("Player: " + player + " attempting to ensoul common item: " + item + "!");
						}
						else if (item.isShadowItem())
						{
							PacketLogger.warning("Player: " + player + " attempting to ensoul shadow item: " + item + "!");
						}
						else if (item.isHeroItem())
						{
							PacketLogger.warning("Player: " + player + " attempting to ensoul hero item: " + item + "!");
						}
						else if (this._options != null && this._options.length != 0)
						{
							int success = 0;
							InventoryUpdate iu = new InventoryUpdate();

							for (RequestItemEnsoul.EnsoulItemOption itemOption : this._options)
							{
								int position = itemOption.getPosition() - 1;
								Item soulCrystal = player.getInventory().getItemByObjectId(itemOption.getSoulCrystalObjectId());
								if (soulCrystal == null)
								{
									player.sendPacket(SystemMessageId.INVALID_SOUL_CRYSTAL);
								}
								else
								{
									EnsoulStone stone = EnsoulData.getInstance().getStone(soulCrystal.getId());
									if (stone != null)
									{
										if (!stone.getOptions().contains(itemOption.getSoulCrystalOption()))
										{
											PacketLogger.warning("Player: " + player + " attempting to ensoul item option that stone doesn't contains!");
										}
										else
										{
											EnsoulOption option = EnsoulData.getInstance().getOption(itemOption.getSoulCrystalOption());
											if (option == null)
											{
												PacketLogger.warning("Player: " + player + " attempting to ensoul item option that doesn't exists!");
											}
											else
											{
												ItemHolder fee;
												if (itemOption.getType() == 1)
												{
													fee = EnsoulData.getInstance().getEnsoulFee(stone.getId(), position);
													if ((itemOption.getPosition() == 1 || itemOption.getPosition() == 2) && item.getSpecialAbility(position) != null)
													{
														fee = EnsoulData.getInstance().getResoulFee(stone.getId(), position);
													}
												}
												else
												{
													if (itemOption.getType() != 2)
													{
														PacketLogger.warning("Player: " + player + " attempting to ensoul item option with unhandled type: " + itemOption.getType() + "!");
														continue;
													}

													fee = EnsoulData.getInstance().getEnsoulFee(stone.getId(), position + 2);
													if (itemOption.getPosition() == 1 && item.getAdditionalSpecialAbility(position) != null)
													{
														fee = EnsoulData.getInstance().getResoulFee(stone.getId(), position + 2);
													}
												}

												if (fee == null)
												{
													PacketLogger.warning("Player: " + player + " attempting to ensoul item option that doesn't exists! (unknown fee)");
												}
												else
												{
													Item gemStones = player.getInventory().getItemByItemId(fee.getId());
													if (gemStones != null && gemStones.getCount() >= fee.getCount())
													{
														if (player.destroyItem(ItemProcessType.FEE, soulCrystal, 1L, player, true) && player.destroyItem(ItemProcessType.FEE, gemStones, fee.getCount(), player, true))
														{
															item.addSpecialAbility(option, position, stone.getSlotType(), true);
															success = 1;
														}

														if (soulCrystal.isStackable() && soulCrystal.getCount() > 0L)
														{
															iu.addModifiedItem(soulCrystal);
														}
														else
														{
															iu.addRemovedItem(soulCrystal);
														}

														if (gemStones.isStackable() && gemStones.getCount() > 0L)
														{
															iu.addModifiedItem(gemStones);
														}
														else
														{
															iu.addRemovedItem(gemStones);
														}

														iu.addModifiedItem(item);
													}
												}
											}
										}
									}
								}
							}

							player.sendInventoryUpdate(iu);
							if (item.isEquipped())
							{
								item.applySpecialAbilities();
							}

							player.sendPacket(new ExEnsoulResult(success, item));
							item.updateDatabase(true);
						}
						else
						{
							PacketLogger.warning("Player: " + player + " attempting to ensoul item without any special ability declared!");
						}
					}
				}
			}
		}
	}

	private static class EnsoulItemOption
	{
		private final int _type;
		private final int _position;
		private final int _soulCrystalObjectId;
		private final int _soulCrystalOption;

		EnsoulItemOption(int type, int position, int soulCrystalObjectId, int soulCrystalOption)
		{
			this._type = type;
			this._position = position;
			this._soulCrystalObjectId = soulCrystalObjectId;
			this._soulCrystalOption = soulCrystalOption;
		}

		public int getType()
		{
			return this._type;
		}

		public int getPosition()
		{
			return this._position;
		}

		public int getSoulCrystalObjectId()
		{
			return this._soulCrystalObjectId;
		}

		public int getSoulCrystalOption()
		{
			return this._soulCrystalOption;
		}
	}
}
