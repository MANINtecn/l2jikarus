package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PrivateStoreType;
import net.sf.l2jdev.gameserver.network.serverpackets.RecipeShopItemInfo;

public class RequestRecipeShopMakeInfo extends ClientPacket
{
	private int _playerObjectId;
	private int _recipeId;

	@Override
	protected void readImpl()
	{
		this._playerObjectId = this.readInt();
		this._recipeId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Player shop = World.getInstance().getPlayer(this._playerObjectId);
			if (shop != null && shop.getPrivateStoreType() == PrivateStoreType.MANUFACTURE)
			{
				player.sendPacket(new RecipeShopItemInfo(shop, this._recipeId));
			}
		}
	}
}
