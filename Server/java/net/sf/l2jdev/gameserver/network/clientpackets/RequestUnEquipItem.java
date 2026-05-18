package net.sf.l2jdev.gameserver.network.clientpackets;

import java.util.List;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.EtcItem;
import net.sf.l2jdev.gameserver.model.item.enums.BodyPart;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class RequestUnEquipItem extends ClientPacket
{
	private BodyPart _slot;

	@Override
	protected void readImpl()
	{
		this._slot = BodyPart.fromPaperdollSlot(this.readInt());
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Item item = player.getInventory().getPaperdollItemByBodyPart(this._slot);
			if (item != null)
			{
				if (player.isAttackingNow() || player.isCastingNow())
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_EQUIP_ITEMS_WHEN_USING_SKILLS);
				}
				else if (this._slot != BodyPart.L_HAND || !(item.getTemplate() instanceof EtcItem))
				{
					if (this._slot != BodyPart.LR_HAND || !player.isCursedWeaponEquipped() && !player.isCombatFlagEquipped())
					{
						if (!player.hasBlockActions() && !player.isAlikeDead())
						{
							if (!player.getInventory().canManipulateWithItemId(item.getId()))
							{
								player.sendPacket(SystemMessageId.THE_ITEM_CANNOT_BE_DROPPED_2);
							}
							else if (item.isWeapon() && item.getWeaponItem().isForceEquip() && !player.isGM())
							{
								player.sendPacket(SystemMessageId.THE_ITEM_CANNOT_BE_DROPPED_2);
							}
							else
							{
								List<Item> unequipped = player.getInventory().unEquipItemInBodySlotAndRecord(this._slot);
								player.broadcastUserInfo();
								if (!unequipped.isEmpty())
								{
									SystemMessage sm = null;
									Item unequippedItem = unequipped.get(0);
									if (unequippedItem.getEnchantLevel() > 0)
									{
										sm = new SystemMessage(SystemMessageId.S1_S2_UNEQUIPPED);
										sm.addInt(unequippedItem.getEnchantLevel());
									}
									else
									{
										sm = new SystemMessage(SystemMessageId.S1_UNEQUIPPED);
									}

									sm.addItemName(unequippedItem);
									player.sendPacket(sm);
									InventoryUpdate iu = new InventoryUpdate();
									iu.addItems(unequipped);
									player.sendInventoryUpdate(iu);
								}
							}
						}
					}
				}
			}
		}
	}
}
