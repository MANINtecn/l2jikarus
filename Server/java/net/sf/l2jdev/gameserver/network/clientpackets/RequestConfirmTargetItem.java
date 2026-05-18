package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.data.xml.VariationData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPutItemResultForVariationMake;

public class RequestConfirmTargetItem extends AbstractRefinePacket
{
	private int _itemObjId;

	@Override
	protected void readImpl()
	{
		this._itemObjId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Item item = player.getInventory().getItemByObjectId(this._itemObjId);
			if (item != null)
			{
				if (!VariationData.getInstance().hasFeeData(item.getId()))
				{
					player.sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
				}
				else if (!isValid(player, item))
				{
					if (item.isAugmented())
					{
						player.sendPacket(SystemMessageId.ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN);
					}
					else
					{
						player.sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
					}
				}
				else
				{
					player.sendPacket(new ExPutItemResultForVariationMake(this._itemObjId, item.getId()));
				}
			}
		}
	}
}
