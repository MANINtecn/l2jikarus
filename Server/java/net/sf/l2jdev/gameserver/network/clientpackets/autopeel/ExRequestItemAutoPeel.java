package net.sf.l2jdev.gameserver.network.clientpackets.autopeel;

import java.util.Collections;

import net.sf.l2jdev.gameserver.handler.IItemHandler;
import net.sf.l2jdev.gameserver.handler.ItemHandler;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.AutoPeelRequest;
import net.sf.l2jdev.gameserver.model.item.EtcItem;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.autopeel.ExResultItemAutoPeel;

public class ExRequestItemAutoPeel extends ClientPacket
{
	private int _itemObjectId;
	private long _totalPeelCount;
	private long _remainingPeelCount;

	@Override
	protected void readImpl()
	{
		this._itemObjectId = this.readInt();
		this._totalPeelCount = this.readLong();
		this._remainingPeelCount = this.readLong();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (this._totalPeelCount >= 1L && this._remainingPeelCount >= 0L)
			{
				AutoPeelRequest request = player.getRequest(AutoPeelRequest.class);
				if (request == null)
				{
					Item item = player.getInventory().getItemByObjectId(this._itemObjectId);
					if (item == null || !item.isEtcItem() || item.getEtcItem().getExtractableItems() == null || item.getEtcItem().getExtractableItems().isEmpty())
					{
						return;
					}

					request = new AutoPeelRequest(player, item);
					player.addRequest(request);
				}
				else if (request.isProcessing())
				{
					return;
				}

				request.setProcessing(true);
				Item item = request.getItem();
				if (item.getObjectId() != this._itemObjectId || item.getOwnerId() != player.getObjectId())
				{
					player.removeRequest(request.getClass());
				}
				else if (!item.getTemplate().checkCondition(player, item, true))
				{
					player.sendPacket(new ExResultItemAutoPeel(false, this._totalPeelCount, this._remainingPeelCount, Collections.emptyList()));
					player.removeRequest(request.getClass());
				}
				else
				{
					request.setTotalPeelCount(this._totalPeelCount);
					request.setRemainingPeelCount(this._remainingPeelCount);
					EtcItem etcItem = (EtcItem) item.getTemplate();
					if (etcItem.getExtractableItems() != null && !etcItem.getExtractableItems().isEmpty())
					{
						IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
						if (handler != null && !handler.onItemUse(player, item, false))
						{
							request.setProcessing(false);
							player.sendPacket(new ExResultItemAutoPeel(false, this._totalPeelCount, this._remainingPeelCount, Collections.emptyList()));
						}
					}
				}
			}
		}
	}
}
