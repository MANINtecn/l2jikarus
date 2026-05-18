package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.managers.RecipeManager;
import net.sf.l2jdev.gameserver.model.actor.Player;

public class RequestRecipeItemMakeSelf extends ClientPacket
{
	private int _id;

	@Override
	protected void readImpl()
	{
		this._id = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (this.getClient().getFloodProtectors().canManufacture())
			{
				if (player.isInStoreMode())
				{
					player.sendMessage("You cannot create items while trading.");
				}
				else if (player.isCrafting())
				{
					player.sendMessage("You are currently in Craft Mode.");
				}
				else
				{
					RecipeManager.getInstance().requestMakeItem(player, this._id);
				}
			}
		}
	}
}
