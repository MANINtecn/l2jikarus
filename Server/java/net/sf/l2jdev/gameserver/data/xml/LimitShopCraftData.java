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

public class LimitShopCraftData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(LimitShopData.class.getName());
	private final List<LimitShopProductHolder> _products = new ArrayList<>();

	protected LimitShopCraftData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this._products.clear();
		this.parseDatapackFile("data/LimitShopCraft.xml");
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
								0
							};
							long[] ingredientQuantities = new long[]
							{
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
								0
							};
							int productionId = 0;
							int productionId2 = 0;
							int productionId3 = 0;
							int productionId4 = 0;
							int productionId5 = 0;
							long count = 1L;
							long count2 = 1L;
							long count3 = 1L;
							long count4 = 1L;
							long count5 = 1L;
							float chance = 100.0F;
							float chance2 = 100.0F;
							float chance3 = 100.0F;
							float chance4 = 100.0F;
							boolean announce = false;
							boolean announce2 = false;
							boolean announce3 = false;
							boolean announce4 = false;
							boolean announce5 = false;
							int enchant = 0;
							int accountDailyLimit = 0;
							int accountWeeklyLimit = 0;
							int accountMonthlyLimit = 0;
							int accountBuyLimit = 0;

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
									else
									{
										ingredientIds[4] = ingredientId;
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
									else
									{
										ingredientQuantities[4] = ingredientQuantity;
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
									else
									{
										ingredientEnchants[4] = ingredientEnchant;
									}
								}
								else if ("production".equalsIgnoreCase(b.getNodeName()))
								{
									productionId = this.parseInteger(attrs, "id");
									count = this.parseLong(attrs, "count", 1L);
									chance = this.parseFloat(attrs, "chance", 100.0F);
									announce = this.parseBoolean(attrs, "announce", false);
									enchant = this.parseInteger(attrs, "enchant", 0);
									productionId2 = this.parseInteger(attrs, "id2", 0);
									count2 = this.parseLong(attrs, "count2", 1L);
									chance2 = this.parseFloat(attrs, "chance2", 100.0F);
									announce2 = this.parseBoolean(attrs, "announce2", false);
									productionId3 = this.parseInteger(attrs, "id3", 0);
									count3 = this.parseLong(attrs, "count3", 1L);
									chance3 = this.parseFloat(attrs, "chance3", 100.0F);
									announce3 = this.parseBoolean(attrs, "announce3", false);
									productionId4 = this.parseInteger(attrs, "id4", 0);
									count4 = this.parseLong(attrs, "count4", 1L);
									chance4 = this.parseFloat(attrs, "chance4", 100.0F);
									announce4 = this.parseBoolean(attrs, "announce4", false);
									productionId5 = this.parseInteger(attrs, "id5", 0);
									count5 = this.parseLong(attrs, "count5", 1L);
									announce5 = this.parseBoolean(attrs, "announce5", false);
									accountDailyLimit = this.parseInteger(attrs, "accountDailyLimit", 0);
									accountWeeklyLimit = this.parseInteger(attrs, "accountWeeklyLimit", 0);
									accountMonthlyLimit = this.parseInteger(attrs, "accountMonthlyLimit", 0);
									accountBuyLimit = this.parseInteger(attrs, "accountBuyLimit", 0);
									if (productionId > 0)
									{
										ItemTemplate templatex = ItemData.getInstance().getTemplate(productionId);
										if (templatex == null)
										{
											LOGGER.severe(this.getClass().getSimpleName() + ": Item template null for itemId: " + productionId + " productId: " + id);
											continue;
										}

										if (count > 1L && !templatex.isStackable() && !templatex.isEquipable())
										{
											LOGGER.warning(this.getClass().getSimpleName() + ": Item template for itemId: " + productionId + " should be stackable!");
										}
									}

									if (productionId2 > 0)
									{
										ItemTemplate templatexx = ItemData.getInstance().getTemplate(productionId2);
										if (templatexx == null)
										{
											LOGGER.severe(this.getClass().getSimpleName() + ": Item template null for itemId: " + productionId2 + " productId: " + id);
											continue;
										}

										if (count2 > 1L && !templatexx.isStackable() && !templatexx.isEquipable())
										{
											LOGGER.warning(this.getClass().getSimpleName() + ": Item template for itemId: " + productionId2 + " should be stackable!");
										}
									}

									if (productionId3 > 0)
									{
										ItemTemplate templatexxx = ItemData.getInstance().getTemplate(productionId3);
										if (templatexxx == null)
										{
											LOGGER.severe(this.getClass().getSimpleName() + ": Item template null for itemId: " + productionId3 + " productId: " + id);
											continue;
										}

										if (count3 > 1L && !templatexxx.isStackable() && !templatexxx.isEquipable())
										{
											LOGGER.warning(this.getClass().getSimpleName() + ": Item template for itemId: " + productionId3 + " should be stackable!");
										}
									}

									if (productionId4 > 0)
									{
										ItemTemplate templatexxxx = ItemData.getInstance().getTemplate(productionId4);
										if (templatexxxx == null)
										{
											LOGGER.severe(this.getClass().getSimpleName() + ": Item template null for itemId: " + productionId4 + " productId: " + id);
											continue;
										}

										if (count4 > 1L && !templatexxxx.isStackable() && !templatexxxx.isEquipable())
										{
											LOGGER.warning(this.getClass().getSimpleName() + ": Item template for itemId: " + productionId4 + " should be stackable!");
										}
									}

									if (productionId5 > 0)
									{
										ItemTemplate templatexxxxx = ItemData.getInstance().getTemplate(productionId5);
										if (templatexxxxx == null)
										{
											LOGGER.severe(this.getClass().getSimpleName() + ": Item template null for itemId: " + productionId5 + " productId: " + id);
											continue;
										}

										if (count5 > 1L && !templatexxxxx.isStackable() && !templatexxxxx.isEquipable())
										{
											LOGGER.warning(this.getClass().getSimpleName() + ": Item template for itemId: " + productionId5 + " should be stackable!");
										}
									}

									if (productionId2 == 0)
									{
										chance = 100.0F;
									}

									if (productionId3 == 0)
									{
										chance2 = 100.0F;
									}

									if (productionId4 == 0)
									{
										chance3 = 100.0F;
									}

									if (productionId5 == 0)
									{
										chance4 = 100.0F;
									}
								}
							}

							this._products.add(new LimitShopProductHolder(id, category, minLevel, maxLevel, ingredientIds, ingredientQuantities, ingredientEnchants, productionId, count, chance, announce, enchant, productionId2, count2, chance2, announce2, productionId3, count3, chance3, announce3, productionId4, count4, chance4, announce4, productionId5, count5, announce5, accountDailyLimit, accountWeeklyLimit, accountMonthlyLimit, accountBuyLimit));
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

	public static LimitShopCraftData getInstance()
	{
		return LimitShopCraftData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final LimitShopCraftData INSTANCE = new LimitShopCraftData();
	}
}
