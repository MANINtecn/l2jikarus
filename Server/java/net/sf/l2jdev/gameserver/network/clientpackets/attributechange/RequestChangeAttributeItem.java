package net.sf.l2jdev.gameserver.network.clientpackets.attributechange;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.AttributeType;
import net.sf.l2jdev.gameserver.model.item.enchant.attribute.AttributeHolder;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.PlayerInventory;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.attributechange.ExChangeAttributeFail;
import net.sf.l2jdev.gameserver.network.serverpackets.attributechange.ExChangeAttributeOk;

public class RequestChangeAttributeItem extends ClientPacket
{
	private int _consumeItemId;
	private int _itemObjId;
	private int _newElementId;

	@Override
	protected void readImpl()
	{
		this._consumeItemId = this.readInt();
		this._itemObjId = this.readInt();
		this._newElementId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			PlayerInventory inventory = player.getInventory();
			Item item = inventory.getItemByObjectId(this._itemObjId);
			if (player.getInventory().destroyItemByItemId(ItemProcessType.FEE, this._consumeItemId, 1L, player, item) == null)
			{
				player.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
				player.sendPacket(ExChangeAttributeFail.STATIC);
				PunishmentManager.handleIllegalPlayerAction(player, player + " tried to change attribute without an attribute change crystal.", GeneralConfig.DEFAULT_PUNISH);
			}
			else
			{
				int oldElementId = item.getAttackAttributeType().getClientId();
				int elementValue = item.getAttackAttribute().getValue();
				item.clearAllAttributes();
				item.setAttribute(new AttributeHolder(AttributeType.findByClientId(this._newElementId), elementValue), true);
				SystemMessage msg = new SystemMessage(SystemMessageId.S1_S_S2_ATTRIBUTE_HAS_SUCCESSFULLY_CHANGED_TO_S3_ATTRIBUTE);
				msg.addItemName(item);
				msg.addAttribute(oldElementId);
				msg.addAttribute(this._newElementId);
				player.sendPacket(msg);
				InventoryUpdate iu = new InventoryUpdate();
				iu.addModifiedItem(item);

				for (Item i : player.getInventory().getAllItemsByItemId(this._consumeItemId))
				{
					iu.addItem(i);
				}

				player.sendInventoryUpdate(iu);
				player.broadcastUserInfo();
				player.sendPacket(ExChangeAttributeOk.STATIC);
			}
		}
	}
}
