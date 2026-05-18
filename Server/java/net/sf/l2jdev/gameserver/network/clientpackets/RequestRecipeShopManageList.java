package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PrivateStoreType;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.RecipeShopManageList;

public class RequestRecipeShopManageList extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (player.isAlikeDead())
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else
			{
				if (player.isInStoreMode())
				{
					player.setPrivateStoreType(PrivateStoreType.NONE);
					player.broadcastUserInfo();
					if (player.isSitting())
					{
						player.standUp();
					}
				}

				player.sendPacket(new RecipeShopManageList(player, true));
			}
		}
	}
}
