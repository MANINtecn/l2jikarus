package net.sf.l2jdev.gameserver.network.clientpackets.compound;

import java.util.List;

import net.sf.l2jdev.gameserver.data.xml.CombinationItemsData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.CompoundRequest;
import net.sf.l2jdev.gameserver.model.item.combination.CombinationItem;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.compound.ExEnchantOneFail;
import net.sf.l2jdev.gameserver.network.serverpackets.compound.ExEnchantOneOK;

public class RequestNewEnchantPushOne extends ClientPacket
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
				CompoundRequest request = new CompoundRequest(player);
				if (!player.addRequest(request))
				{
					player.sendPacket(ExEnchantOneFail.STATIC_PACKET);
				}
				else
				{
					request.setItemOne(this._objectId);
					Item itemOne = request.getItemOne();
					if (itemOne == null)
					{
						player.sendPacket(ExEnchantOneFail.STATIC_PACKET);
						player.removeRequest(request.getClass());
					}
					else
					{
						List<CombinationItem> combinationItems = CombinationItemsData.getInstance().getItemsByFirstSlot(itemOne.getId(), itemOne.getEnchantLevel());
						if (combinationItems.isEmpty())
						{
							player.sendPacket(ExEnchantOneFail.STATIC_PACKET);
							player.removeRequest(request.getClass());
						}
						else
						{
							player.sendPacket(ExEnchantOneOK.STATIC_PACKET);
						}
					}
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
