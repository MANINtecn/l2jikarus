package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.data.holders.LimitShopProductHolder;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class LimitShopData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(LimitShopData.class.getName());
	private final List<LimitShopProductHolder> _products = new ArrayList<>();

	protected LimitShopData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this._products.clear();
		this.parseDatapackFile("data/LimitShop.xml");
		if (!this._products.isEmpty())
		{
			LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._products.size() + " items.");
		}
		else
		{
			LOGGER.info(this.getClass().getSimpleName() + ": System is disabled.");
		}
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		for (Node n = document.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				NamedNodeMap at = n.getAttributes();
				Node attribute = at.getNamedItem("enabled");
				if (attribute != null && Boolean.parseBoolean(attribute.getNodeValue()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("product".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();
							StatSet set = new StatSet();

							for (int i = 0; i < attrs.getLength(); i++)
							{
								Node att = attrs.item(i);
								set.set(att.getNodeName(), att.getNodeValue());
							}

							int id = this.parseInteger(attrs, "id");
							int category = this.parseInteger(attrs, "category");
							int minLevel = this.parseInteger(attrs, "minLevel", 1);
							int maxLevel = this.parseInteger(attrs, "maxLevel", 999);
							int[] ingredientIds = new int[]
							{
								0,
								0,
								0,
								0,
								0,
								0,
								0,
								0
							};
							long[] ingredientQuantities = new long[]
							{
								0L,
								0L,
								0L,
								0L,
								0L,
								0L,
								0L,
								0L
							};
							int[] ingredientEnchants = new int[]
							{
								0,
								0,
								0,
								0,
								0,
								0,
								0,
								0
							};
							int productionId = 0;
							int accountDailyLimit = 0;
							int accountWeeklyLimit = 0;
							int accountMonthlyLimit = 0;
							int accountBuyLimit = 0;
							int productionCount = 0;

							for (Node b = d.getFirstChild(); b != null; b = b.getNextSibling())
							{
								attrs = b.getAttributes();
								if ("ingredient".equalsIgnoreCase(b.getNodeName()))
								{
									int ingredientId = this.parseInteger(attrs, "id");
									long ingredientQuantity = this.parseLong(attrs, "count", 1L);
									int ingredientEnchant = this.parseInteger(attrs, "enchant", 0);
									if (ingredientId > 0)
									{
										ItemTemplate template = ItemData.getInstance().getTemplate(ingredientId);
										if (template == null)
										{
											LOGGER.severe(this.getClass().getSimpleName() + ": Item template null for itemId: " + productionId + " productId: " + id);
											continue;
										}

										if (ingredientQuantity > 1L && !template.isStackable() && !template.isEquipable())
										{
											LOGGER.warning(this.getClass().getSimpleName() + ": Item template for itemId: " + ingredientId + " should be stackable!");
										}
									}

									if (ingredientIds[0] == 0)
									{
										ingredientIds[0] = ingredientId;
									}
									else if (ingredientIds[1] == 0)
									{
										ingredientIds[1] = ingredientId;
									}
									else if (ingredientIds[2] == 0)
									{
										ingredientIds[2] = ingredientId;
									}
									else if (ingredientIds[3] == 0)
									{
										ingredientIds[3] = ingredientId;
									}
									else if (ingredientIds[4] == 0)
									{
										ingredientIds[4] = ingredientId;
									}
									else if (ingredientIds[5] == 0)
									{
										ingredientIds[5] = ingredientId;
									}
									else if (ingredientIds[6] == 0)
									{
										ingredientIds[6] = ingredientId;
									}
									else
									{
										ingredientIds[7] = ingredientId;
									}

									if (ingredientQuantities[0] == 0L)
									{
										ingredientQuantities[0] = ingredientQuantity;
									}
									else if (ingredientQuantities[1] == 0L)
									{
										ingredientQuantities[1] = ingredientQuantity;
									}
									else if (ingredientQuantities[2] == 0L)
									{
										ingredientQuantities[2] = ingredientQuantity;
									}
									else if (ingredientQuantities[3] == 0L)
									{
										ingredientQuantities[3] = ingredientQuantity;
									}
									else if (ingredientQuantities[4] == 0L)
									{
										ingredientQuantities[4] = ingredientQuantity;
									}
									else if (ingredientQuantities[5] == 0L)
									{
										ingredientQuantities[5] = ingredientQuantity;
									}
									else if (ingredientQuantities[6] == 0L)
									{
										ingredientQuantities[6] = ingredientQuantity;
									}
									else
									{
										ingredientQuantities[7] = ingredientQuantity;
									}

									if (ingredientEnchants[0] == 0)
									{
										ingredientEnchants[0] = ingredientEnchant;
									}
									else if (ingredientEnchants[1] == 0)
									{
										ingredientEnchants[1] = ingredientEnchant;
									}
									else if (ingredientEnchants[2] == 0)
									{
										ingredientEnchants[2] = ingredientEnchant;
									}
									else if (ingredientEnchants[3] == 0)
									{
										ingredientEnchants[3] = ingredientEnchant;
									}
									else if (ingredientEnchants[4] == 0)
									{
										ingredientEnchants[4] = ingredientEnchant;
									}
									else if (ingredientEnchants[5] == 0)
									{
										ingredientEnchants[5] = ingredientEnchant;
									}
									else if (ingredientEnchants[6] == 0)
									{
										ingredientEnchants[6] = ingredientEnchant;
									}
									else
									{
										ingredientEnchants[7] = ingredientEnchant;
									}
								}
								else if ("production".equalsIgnoreCase(b.getNodeName()))
								{
									productionId = this.parseInteger(attrs, "id");
									accountDailyLimit = this.parseInteger(attrs, "accountDailyLimit", 0);
									accountWeeklyLimit = this.parseInteger(attrs, "accountWeeklyLimit", 0);
									accountMonthlyLimit = this.parseInteger(attrs, "accountMonthlyLimit", 0);
									accountBuyLimit = this.parseInteger(attrs, "accountBuyLimit", 0);
									productionCount = this.parseInteger(attrs, "count", 1);
									ItemTemplate item = ItemData.getInstance().getTemplate(productionId);
									if (item == null)
									{
										LOGGER.severe(this.getClass().getSimpleName() + ": Item template null for itemId: " + productionId + " productId: " + id);
									}
								}
							}

							this._products.add(new LimitShopProductHolder(id, category, minLevel, maxLevel, ingredientIds, ingredientQuantities, ingredientEnchants, productionId, productionCount, 100.0F, false, 0, 0, 0L, 0.0F, false, 0, 0L, 0.0F, false, 0, 0L, 0.0F, false, 0, 0L, false, accountDailyLimit, accountWeeklyLimit, accountMonthlyLimit, accountBuyLimit));
						}
					}
				}
			}
		}
	}

	public LimitShopProductHolder getProduct(int id)
	{
		for (LimitShopProductHolder product : this._products)
		{
			if (product.getId() == id)
			{
				return product;
			}
		}

		return null;
	}

	public List<LimitShopProductHolder> getProducts()
	{
		return this._products;
	}

	public static LimitShopData getInstance()
	{
		return LimitShopData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final LimitShopData INSTANCE = new LimitShopData();
	}
}
