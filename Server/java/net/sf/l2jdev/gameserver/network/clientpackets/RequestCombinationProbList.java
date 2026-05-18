package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.data.xml.CombinationItemsData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.combination.CombinationItem;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.serverpackets.CombinationProbList;

public class RequestCombinationProbList extends ClientPacket
{
	private int _oneSlotServerId;
	private int _twoSlotServerId;

	@Override
	protected void readImpl()
	{
		this._oneSlotServerId = this.readInt();
		this._twoSlotServerId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Item itemOne = player.getInventory().getItemByObjectId(this._oneSlotServerId);
			if (itemOne != null)
			{
				Item itemTwo = player.getInventory().getItemByObjectId(this._twoSlotServerId);
				if (itemTwo != null)
				{
					CombinationItem combinationItem = CombinationItemsData.getInstance().getItemsBySlots(itemOne.getId(), itemOne.getEnchantLevel(), itemTwo.getId(), itemTwo.getEnchantLevel());
					if (combinationItem != null)
					{
						player.sendPacket(new CombinationProbList(this._oneSlotServerId, this._twoSlotServerId, combinationItem.getChance()));
					}
				}
			}
		}
	}
}
