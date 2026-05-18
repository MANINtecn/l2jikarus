package net.sf.l2jdev.gameserver.network.clientpackets.compound;

import net.sf.l2jdev.gameserver.data.xml.CombinationItemsData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.CompoundRequest;
import net.sf.l2jdev.gameserver.model.item.combination.CombinationItem;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.compound.ExEnchantRetryToPutItemFail;
import net.sf.l2jdev.gameserver.network.serverpackets.compound.ExEnchantRetryToPutItemOk;

public class RequestNewEnchantRetryToPutItems extends ClientPacket
{
	private int _firstItemObjectId;
	private int _secondItemObjectId;

	@Override
	protected void readImpl()
	{
		this._firstItemObjectId = this.readInt();
		this._secondItemObjectId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (player.isInStoreMode())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_DO_THAT_WHILE_IN_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
				player.sendPacket(ExEnchantRetryToPutItemFail.STATIC_PACKET);
			}
			else if (!player.isProcessingTransaction() && !player.isProcessingRequest())
			{
				CompoundRequest request = new CompoundRequest(player);
				if (!player.addRequest(request))
				{
					player.sendPacket(ExEnchantRetryToPutItemFail.STATIC_PACKET);
				}
				else
				{
					request.setItemOne(this._firstItemObjectId);
					Item itemOne = request.getItemOne();
					if (itemOne == null)
					{
						player.sendPacket(ExEnchantRetryToPutItemFail.STATIC_PACKET);
						player.removeRequest(request.getClass());
					}
					else
					{
						request.setItemTwo(this._secondItemObjectId);
						Item itemTwo = request.getItemTwo();
						if (itemTwo == null)
						{
							player.sendPacket(ExEnchantRetryToPutItemFail.STATIC_PACKET);
							player.removeRequest(request.getClass());
						}
						else
						{
							CombinationItem combinationItem = CombinationItemsData.getInstance().getItemsBySlots(itemOne.getId(), itemOne.getEnchantLevel(), itemTwo.getId(), itemTwo.getEnchantLevel());
							if (combinationItem == null)
							{
								player.sendPacket(ExEnchantRetryToPutItemFail.STATIC_PACKET);
								player.removeRequest(request.getClass());
							}
							else
							{
								player.sendPacket(ExEnchantRetryToPutItemOk.STATIC_PACKET);
							}
						}
					}
				}
			}
			else
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THIS_SYSTEM_DURING_TRADING_PRIVATE_STORE_AND_WORKSHOP_SETUP);
				player.sendPacket(ExEnchantRetryToPutItemFail.STATIC_PACKET);
			}
		}
	}
}
