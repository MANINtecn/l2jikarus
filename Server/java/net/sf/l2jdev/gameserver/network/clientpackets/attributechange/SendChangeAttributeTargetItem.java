package net.sf.l2jdev.gameserver.network.clientpackets.attributechange;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.attributechange.ExChangeAttributeInfo;

public class SendChangeAttributeTargetItem extends ClientPacket
{
	private int _crystalItemId;
	private int _itemObjId;

	@Override
	protected void readImpl()
	{
		this._crystalItemId = this.readInt();
		this._itemObjId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Item item = player.getInventory().getItemByObjectId(this._itemObjId);
			if (item != null && item.isWeapon())
			{
				player.sendPacket(new ExChangeAttributeInfo(this._crystalItemId, item));
			}
			else
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}
}
