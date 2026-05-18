package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Collection;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.RecipeList;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class RecipeBookItemList extends ServerPacket
{
	private Collection<RecipeList> _recipes;
	private final boolean _isDwarvenCraft;
	private final int _maxMp;

	public RecipeBookItemList(boolean isDwarvenCraft, int maxMp)
	{
		this._isDwarvenCraft = isDwarvenCraft;
		this._maxMp = maxMp;
	}

	public void addRecipes(Collection<RecipeList> recipeBook)
	{
		this._recipes = recipeBook;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.RECIPE_BOOK_ITEM_LIST.writeId(this, buffer);
		buffer.writeInt(!this._isDwarvenCraft);
		buffer.writeInt(this._maxMp);
		if (this._recipes == null)
		{
			buffer.writeInt(0);
		}
		else
		{
			buffer.writeInt(this._recipes.size());
			int count = 1;

			for (RecipeList recipe : this._recipes)
			{
				buffer.writeInt(recipe.getId());
				buffer.writeInt(count++);
			}
		}
	}
}
