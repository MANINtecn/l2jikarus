package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.data.xml.VariationData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.options.VariationFee;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPutIntensiveResultForVariationMake;

public class RequestConfirmRefinerItem extends AbstractRefinePacket
{
	private int _targetItemObjId;
	private int _refinerItemObjId;

	@Override
	protected void readImpl()
	{
		this._targetItemObjId = this.readInt();
		this._refinerItemObjId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Item targetItem = player.getInventory().getItemByObjectId(this._targetItemObjId);
			if (targetItem != null)
			{
				Item refinerItem = player.getInventory().getItemByObjectId(this._refinerItemObjId);
				if (refinerItem != null)
				{
					VariationFee fee = VariationData.getInstance().getFee(targetItem.getId(), refinerItem.getId());
					if (fee != null && isValid(player, targetItem, refinerItem))
					{
						player.sendPacket(new ExPutIntensiveResultForVariationMake(this._refinerItemObjId, refinerItem.getId(), 1));
					}
					else
					{
						player.sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
					}
				}
			}
		}
	}
}
