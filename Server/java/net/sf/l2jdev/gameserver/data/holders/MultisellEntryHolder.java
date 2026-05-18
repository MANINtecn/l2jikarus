package net.sf.l2jdev.gameserver.data.holders;

import java.util.Collections;
import java.util.List;

import net.sf.l2jdev.gameserver.data.xml.ItemData;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.holders.ItemChanceHolder;

public class MultisellEntryHolder
{
	private final boolean _stackable;
	private final List<ItemChanceHolder> _ingredients;
	private final List<ItemChanceHolder> _products;

	public MultisellEntryHolder(List<ItemChanceHolder> ingredients, List<ItemChanceHolder> products)
	{
		this._ingredients = Collections.unmodifiableList(ingredients);
		this._products = Collections.unmodifiableList(products);

		for (ItemChanceHolder product : products)
		{
			ItemTemplate item = ItemData.getInstance().getTemplate(product.getId());
			if (item == null || !item.isStackable())
			{
				this._stackable = false;
				return;
			}
		}

		this._stackable = true;
	}

	public List<ItemChanceHolder> getIngredients()
	{
		return this._ingredients;
	}

	public List<ItemChanceHolder> getProducts()
	{
		return this._products;
	}

	public boolean isStackable()
	{
		return this._stackable;
	}
}
