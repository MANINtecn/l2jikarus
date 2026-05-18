package net.sf.l2jdev.gameserver.network.clientpackets.compound;

import net.sf.l2jdev.gameserver.data.xml.CombinationItemsData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.CompoundRequest;
import net.sf.l2jdev.gameserver.model.item.combination.CombinationItem;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.compound.ExEnchantOneFail;
import net.sf.l2jdev.gameserver.network.serverpackets.compound.ExEnchantTwoFail;
import net.sf.l2jdev.gameserver.network.serverpackets.compound.ExEnchantTwoOK;

public class RequestNewEnchantPushTwo extends ClientPacket
{
	private int _objectId;

	@Override
	protected void readImpl()
	{
		this._objectId = this.readInt();
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
				player.sendPacket(ExEnchantOneFail.STATIC_PACKET);
			}
			else if (!player.isProcessingTransaction() && !player.isProcessingRequest())
			{
				CompoundRequest request = player.getRequest(CompoundRequest.class);
				if (request != null && !request.isProcessing())
				{
					request.setItemTwo(this._objectId);
					Item itemOne = request.getItemOne();
					Item itemTwo = request.getItemTwo();
					if (itemOne != null && itemTwo != null)
					{
						if (itemOne.getObjectId() != itemTwo.getObjectId() || itemOne.isStackable() && player.getInventory().getInventoryItemCount(itemOne.getTemplate().getId(), -1) >= 2L)
						{
							CombinationItem combinationItem = CombinationItemsData.getInstance().getItemsBySlots(itemOne.getId(), itemOne.getEnchantLevel(), itemTwo.getId(), itemTwo.getEnchantLevel());
							if (combinationItem == null)
							{
								player.sendPacket(ExEnchantTwoFail.STATIC_PACKET);
							}
							else
							{
								player.sendPacket(ExEnchantTwoOK.STATIC_PACKET);
							}
						}
						else
						{
							player.sendPacket(ExEnchantTwoFail.STATIC_PACKET);
						}
					}
					else
					{
						player.sendPacket(ExEnchantTwoFail.STATIC_PACKET);
					}
				}
				else
				{
					player.sendPacket(ExEnchantTwoFail.STATIC_PACKET);
				}
			}
			else
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THIS_SYSTEM_DURING_TRADING_PRIVATE_STORE_AND_WORKSHOP_SETUP);
				player.sendPacket(ExEnchantOneFail.STATIC_PACKET);
			}
		}
	}
}
