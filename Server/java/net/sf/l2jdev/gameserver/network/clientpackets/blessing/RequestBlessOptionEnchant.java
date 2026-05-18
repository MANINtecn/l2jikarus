package net.sf.l2jdev.gameserver.network.clientpackets.blessing;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.config.RatesConfig;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.BlessingItemRequest;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.enums.ItemSkillType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.CommonSkill;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.blessing.ExBlessOptionEnchant;
import net.sf.l2jdev.gameserver.network.serverpackets.blessing.ExOpenBlessOptionScroll;

public class RequestBlessOptionEnchant extends ClientPacket
{
	private int _scrollId;
	private int _itemObjectId;

	@Override
	protected void readImpl()
	{
		this._scrollId = this.readInt();
		this._itemObjectId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (player.isOnline() && !this.getClient().isDetached())
			{
				Item scroll = player.getInventory().getItemByItemId(this._scrollId);
				if (scroll == null)
				{
					player.sendPacket(new ExBlessOptionEnchant(0, 2));
				}
				else
				{
					Item item = player.getInventory().getItemByObjectId(this._itemObjectId);
					if (item == null)
					{
						player.sendPacket(new ExBlessOptionEnchant(0, 2));
					}
					else
					{
						BlessingItemRequest request = player.getRequest(BlessingItemRequest.class);
						if (request != null && !request.isProcessing())
						{
							request.setProcessing(true);
							request.setTimestamp(System.currentTimeMillis());
							if (player.isInStoreMode())
							{
								player.sendPacket(SystemMessageId.YOU_CANNOT_ENCHANT_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
								player.sendPacket(new ExBlessOptionEnchant(0, 2));
								request.setProcessing(false);
								player.sendItemList();
								player.broadcastUserInfo();
							}
							else if (item.isBlessed())
							{
								ThreadPool.schedule(() -> {
									player.sendPacket(new ExBlessOptionEnchant(0, 0));
									player.sendPacket(new ExOpenBlessOptionScroll(scroll.getId()));
								}, 3000L);
								request.setProcessing(false);
								player.sendItemList();
								player.broadcastUserInfo();
							}
							else
							{
								Item targetScroll = player.getInventory().getItemByItemId(request.getBlessScrollId());
								if (targetScroll == null)
								{
									player.sendPacket(new ExBlessOptionEnchant(0, 2));
									request.setProcessing(false);
									player.sendItemList();
									player.broadcastUserInfo();
								}
								else if (player.getInventory().destroyItem(ItemProcessType.FEE, targetScroll.getObjectId(), 1L, player, item) == null)
								{
									player.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
									PunishmentManager.handleIllegalPlayerAction(player, player + " tried to bless with a scroll he doesn't have", GeneralConfig.DEFAULT_PUNISH);
									player.sendPacket(new ExBlessOptionEnchant(0, 2));
									request.setProcessing(false);
									player.sendItemList();
									player.broadcastUserInfo();
								}
								else
								{
									if (Rnd.get(100) < RatesConfig.BLESSING_CHANCE)
									{
										item.setBlessed(true);
										item.updateDatabase();
										player.sendPacket(new ExBlessOptionEnchant(targetScroll.getId(), 0));
										if (item.getEnchantLevel() >= (item.isArmor() ? PlayerConfig.MIN_ARMOR_ENCHANT_ANNOUNCE : PlayerConfig.MIN_WEAPON_ENCHANT_ANNOUNCE) && item.getEnchantLevel() <= (item.isArmor() ? PlayerConfig.MAX_ARMOR_ENCHANT_ANNOUNCE : PlayerConfig.MAX_WEAPON_ENCHANT_ANNOUNCE))
										{
											SystemMessage sm = new SystemMessage(SystemMessageId.C1_HAS_ENCHANTED_S3_UP_TO_S2);
											sm.addString(player.getName());
											sm.addInt(item.getEnchantLevel());
											sm.addItemName(item);
											player.broadcastPacket(sm);
											Skill skill = CommonSkill.FIREWORK.getSkill();
											if (skill != null)
											{
												player.broadcastSkillPacket(new MagicSkillUse(player, player, skill.getId(), skill.getLevel(), skill.getHitTime(), skill.getReuseDelay()), player);
											}
										}
										else
										{
											player.sendMessage(item.getName() + " has been blessed.");
										}

										if (item.isEquipped())
										{
											item.getTemplate().forEachSkill(ItemSkillType.ON_BLESSING, holder -> player.addSkill(holder.getSkill()));
											player.sendSkillList();
										}
									}
									else
									{
										player.sendPacket(new ExBlessOptionEnchant(targetScroll.getId(), 0));
									}

									request.setProcessing(false);
									player.sendItemList();
									player.broadcastUserInfo();
								}
							}
						}
						else
						{
							player.sendPacket(new ExBlessOptionEnchant(0, 2));
						}
					}
				}
			}
		}
	}
}
