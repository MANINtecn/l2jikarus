package net.sf.l2jdev.gameserver.network.clientpackets.enchant.single;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.enchant.ResetEnchantItemFailRewardInfo;

public class ExRequestEnchantFailRewardInfo extends ClientPacket
{
	private int _itemobjectid;

	@Override
	protected void readImpl()
	{
		this._itemobjectid = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Item addedItem = player.getInventory().getItemByObjectId(this._itemobjectid);
			if (addedItem != null)
			{
				player.sendPacket(new ResetEnchantItemFailRewardInfo(player));
			}
		}
	}
}
