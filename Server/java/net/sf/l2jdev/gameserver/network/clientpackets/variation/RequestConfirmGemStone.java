package net.sf.l2jdev.gameserver.network.clientpackets.variation;

import net.sf.l2jdev.gameserver.data.xml.VariationData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.options.VariationFee;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.AbstractRefinePacket;
import net.sf.l2jdev.gameserver.network.serverpackets.variation.ExPutCommissionResultForVariationMake;

public class RequestConfirmGemStone extends AbstractRefinePacket
{
	private int _targetItemObjId;
	private int _mineralItemObjId;
	private int _feeItemObjId;
	private long _feeCount;

	@Override
	protected void readImpl()
	{
		this._targetItemObjId = this.readInt();
		this._mineralItemObjId = this.readInt();
		this._feeItemObjId = this.readInt();
		this._feeCount = this.readLong();
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
				Item refinerItem = player.getInventory().getItemByObjectId(this._mineralItemObjId);
				if (refinerItem != null)
				{
					Item gemStoneItem = player.getInventory().getItemByObjectId(this._feeItemObjId);
					if (gemStoneItem != null)
					{
						VariationFee fee = VariationData.getInstance().getFee(targetItem.getId(), refinerItem.getId());
						if (!isValid(player, targetItem, refinerItem, gemStoneItem, fee))
						{
							player.sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
						}
						else if (this._feeCount != fee.getItemCount())
						{
							player.sendPacket(SystemMessageId.GEMSTONE_QUANTITY_IS_INCORRECT);
						}
						else
						{
							player.sendPacket(new ExPutCommissionResultForVariationMake(this._feeItemObjId, this._feeCount, gemStoneItem.getId()));
						}
					}
				}
			}
		}
	}
}
