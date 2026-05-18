package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.model.PremiumItem;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ExGetPremiumItemList;

public class RequestWithDrawPremiumItem extends ClientPacket
{
	private int _itemNum;
	private int _charId;
	private long _itemCount;

	@Override
	protected void readImpl()
	{
		this._itemNum = this.readInt();
		this._charId = this.readInt();
		this._itemCount = this.readLong();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (this._itemCount >= 1L)
			{
				if (player.getObjectId() != this._charId)
				{
					PunishmentManager.handleIllegalPlayerAction(player, "[RequestWithDrawPremiumItem] Incorrect owner, Player: " + player.getName(), GeneralConfig.DEFAULT_PUNISH);
				}
				else if (player.getPremiumItemList().isEmpty())
				{
					PunishmentManager.handleIllegalPlayerAction(player, "[RequestWithDrawPremiumItem] Player: " + player.getName() + " try to get item with empty list!", GeneralConfig.DEFAULT_PUNISH);
				}
				else if (player.getWeightPenalty() >= 3 || !player.isInventoryUnder90(false))
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_GET_THE_SPECIAL_ITEM_AS_YOUR_INVENTORY_IS_FILLED_FOR_MORE_THAN_95);
				}
				else if (player.isProcessingTransaction())
				{
					player.sendPacket(SystemMessageId.ITEMS_FROM_GAME_ASSISTANTS_CANNOT_BE_EXCHANGED);
				}
				else
				{
					PremiumItem item = player.getPremiumItemList().get(this._itemNum);
					if (item != null)
					{
						if (item.getCount() >= this._itemCount)
						{
							long itemsLeft = item.getCount() - this._itemCount;
							player.addItem(ItemProcessType.TRANSFER, item.getItemId(), this._itemCount, player.getTarget(), true);
							if (itemsLeft > 0L)
							{
								item.updateCount(itemsLeft);
								player.updatePremiumItem(this._itemNum, itemsLeft);
							}
							else
							{
								player.getPremiumItemList().remove(this._itemNum);
								player.deletePremiumItem(this._itemNum);
							}

							if (player.getPremiumItemList().isEmpty())
							{
								player.sendPacket(SystemMessageId.THERE_ARE_NO_MORE_DIMENSIONAL_ITEMS_TO_BE_FOUND);
							}
							else
							{
								player.sendPacket(new ExGetPremiumItemList(player));
							}
						}
					}
				}
			}
		}
	}
}
