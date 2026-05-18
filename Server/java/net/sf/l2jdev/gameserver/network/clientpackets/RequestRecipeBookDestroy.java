package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.data.xml.RecipeData;
import net.sf.l2jdev.gameserver.model.RecipeList;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.serverpackets.RecipeBookItemList;

public class RequestRecipeBookDestroy extends ClientPacket
{
	private int _recipeID;

	@Override
	protected void readImpl()
	{
		this._recipeID = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (this.getClient().getFloodProtectors().canPerformTransaction())
			{
				RecipeList rp = RecipeData.getInstance().getRecipeList(this._recipeID);
				if (rp != null)
				{
					player.unregisterRecipeList(this._recipeID);
					RecipeBookItemList response = new RecipeBookItemList(rp.isDwarvenRecipe(), player.getMaxMp());
					if (rp.isDwarvenRecipe())
					{
						response.addRecipes(player.getDwarvenRecipeBook());
					}
					else
					{
						response.addRecipes(player.getCommonRecipeBook());
					}

					player.sendPacket(response);
				}
			}
		}
	}
}
