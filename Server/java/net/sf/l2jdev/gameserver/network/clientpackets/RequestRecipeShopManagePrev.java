package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.RecipeShopSellList;

public class RequestRecipeShopManagePrev extends ClientPacket
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
			if (!player.isAlikeDead() && player.getTarget() != null && player.getTarget().isPlayer())
			{
				player.sendPacket(new RecipeShopSellList(player, player.getTarget().asPlayer()));
			}
			else
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}
}
