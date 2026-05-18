package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.managers.RecipeManager;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PrivateStoreType;
import net.sf.l2jdev.gameserver.util.LocationUtil;

public class RequestRecipeShopMakeItem extends ClientPacket
{
	private int _id;
	private int _recipeId;
	protected long _unknown;

	@Override
	protected void readImpl()
	{
		this._id = this.readInt();
		this._recipeId = this.readInt();
		this._unknown = this.readLong();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (this.getClient().getFloodProtectors().canManufacture())
			{
				Player manufacturer = World.getInstance().getPlayer(this._id);
				if (manufacturer != null)
				{
					if (manufacturer.getInstanceWorld() == player.getInstanceWorld())
					{
						if (player.isInStoreMode())
						{
							player.sendMessage("You cannot create items while trading.");
						}
						else if (manufacturer.getPrivateStoreType() == PrivateStoreType.MANUFACTURE)
						{
							if (!player.isCrafting() && !manufacturer.isCrafting())
							{
								if (LocationUtil.checkIfInRange(150, player, manufacturer, true))
								{
									RecipeManager.getInstance().requestManufactureItem(manufacturer, this._recipeId, player);
								}
							}
							else
							{
								player.sendMessage("You are currently in Craft Mode.");
							}
						}
					}
				}
			}
		}
	}
}
