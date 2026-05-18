package net.sf.l2jdev.gameserver.network.clientpackets.crystalization;

import java.util.List;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.xml.ItemCrystallizationData;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.holders.ItemChanceHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.item.type.CrystalType;
import net.sf.l2jdev.gameserver.model.skill.CommonSkill;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.crystalization.ExGetCrystalizingEstimation;

public class RequestCrystallizeEstimate extends ClientPacket
{
	private int _objectId;
	private long _count;

	@Override
	protected void readImpl()
	{
		this._objectId = this.readInt();
		this._count = this.readLong();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null && !player.isInCrystallize())
		{
			if (this._count < 1L)
			{
				PunishmentManager.handleIllegalPlayerAction(player, "[RequestCrystallizeItem] count <= 0! ban! oid: " + this._objectId + " owner: " + player.getName(), GeneralConfig.DEFAULT_PUNISH);
			}
			else if (!player.isInStoreMode() && !player.isInCrystallize())
			{
				int skillLevel = player.getSkillLevel(CommonSkill.CRYSTALLIZE.getId());
				if (skillLevel <= 0)
				{
					player.sendPacket(SystemMessageId.YOU_MAY_NOT_CRYSTALLIZE_THIS_ITEM_YOUR_CRYSTALLIZATION_SKILL_LEVEL_IS_TOO_LOW);
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else
				{
					Item item = player.getInventory().getItemByObjectId(this._objectId);
					if (item != null && !item.isShadowItem() && !item.isTimeLimitedItem() && !item.isHeroItem() && (PlayerConfig.ALT_ALLOW_AUGMENT_DESTROY || !item.isAugmented()))
					{
						if (item.getTemplate().isCrystallizable() && item.getTemplate().getCrystalCount() > 0 && item.getTemplate().getCrystalType() != CrystalType.NONE)
						{
							if (this._count > item.getCount())
							{
								this._count = player.getInventory().getItemByObjectId(this._objectId).getCount();
							}

							if (!player.getInventory().canManipulateWithItemId(item.getId()))
							{
								player.sendMessage("You cannot use this item.");
							}
							else
							{
								boolean canCrystallize = true;
								switch (item.getTemplate().getCrystalTypePlus())
								{
									case D:
										if (skillLevel < 1)
										{
											canCrystallize = false;
										}
										break;
									case C:
										if (skillLevel < 2)
										{
											canCrystallize = false;
										}
										break;
									case B:
										if (skillLevel < 3)
										{
											canCrystallize = false;
										}
										break;
									case A:
										if (skillLevel < 4)
										{
											canCrystallize = false;
										}
										break;
									case S:
										if (skillLevel < 5)
										{
											canCrystallize = false;
										}
										break;
									case R:
										if (skillLevel < 6)
										{
											canCrystallize = false;
										}
								}

								if (!canCrystallize)
								{
									player.sendPacket(SystemMessageId.YOU_MAY_NOT_CRYSTALLIZE_THIS_ITEM_YOUR_CRYSTALLIZATION_SKILL_LEVEL_IS_TOO_LOW);
									player.sendPacket(ActionFailed.STATIC_PACKET);
								}
								else
								{
									List<ItemChanceHolder> crystallizationRewards = ItemCrystallizationData.getInstance().getCrystallizationRewards(item);
									if (crystallizationRewards != null && !crystallizationRewards.isEmpty())
									{
										player.setInCrystallize(true);
										player.sendPacket(new ExGetCrystalizingEstimation(crystallizationRewards));
									}
									else
									{
										player.sendPacket(SystemMessageId.ANGEL_NEVIT_S_DESCENT_BONUS_TIME_S1);
									}
								}
							}
						}
						else
						{
							player.sendPacket(ActionFailed.STATIC_PACKET);
							PacketLogger.warning(player + ": tried to crystallize " + item.getTemplate());
						}
					}
					else
					{
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
				}
			}
			else
			{
				player.sendPacket(SystemMessageId.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			}
		}
	}
}
