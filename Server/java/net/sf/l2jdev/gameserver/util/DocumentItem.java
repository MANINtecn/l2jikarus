package net.sf.l2jdev.gameserver.util;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.model.ExtractableProduct;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.conditions.Condition;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.enums.ItemSkillType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemSkillHolder;
import net.sf.l2jdev.gameserver.model.stats.Stat;
import net.sf.l2jdev.gameserver.model.stats.functions.FuncTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class DocumentItem extends DocumentBase implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(DocumentItem.class.getName());
	private DocumentItem.DocumentItemDataHolder _currentItem = null;
	private final List<ItemTemplate> _itemsInFile = new ArrayList<>();

	public DocumentItem(File file)
	{
		super(file);
	}

	@Override
	protected StatSet getStatSet()
	{
		return this._currentItem.set;
	}

	@Override
	protected String getTableValue(String name)
	{
		return this._tables.get(name)[this._currentItem.currentLevel];
	}

	@Override
	protected String getTableValue(String name, int idx)
	{
		return this._tables.get(name)[idx - 1];
	}

	@Override
	protected void parseDocument(Document document)
	{
		for (Node n = document.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("item".equalsIgnoreCase(d.getNodeName()))
					{
						try
						{
							this._currentItem = new DocumentItem.DocumentItemDataHolder();
							this.parseItem(d);
							this._itemsInFile.add(this._currentItem.item);
							this.resetTable();
						}
						catch (Exception var5)
						{
							LOGGER.log(Level.WARNING, "Cannot create item " + this._currentItem.id, var5);
						}
					}
				}
			}
		}
	}

	private void parseItem(Node node) throws InvocationTargetException
	{
		int itemId = Integer.parseInt(node.getAttributes().getNamedItem("id").getNodeValue());
		String className = node.getAttributes().getNamedItem("type").getNodeValue();
		String itemName = node.getAttributes().getNamedItem("name").getNodeValue();
		String additionalName = node.getAttributes().getNamedItem("additionalName") != null ? node.getAttributes().getNamedItem("additionalName").getNodeValue() : null;
		this._currentItem.id = itemId;
		this._currentItem.type = className;
		this._currentItem.set = new StatSet();
		this._currentItem.set.set("item_id", itemId);
		this._currentItem.set.set("name", itemName);
		this._currentItem.set.set("additionalName", additionalName);
		Node first = node.getFirstChild();

		for (Node n = first; n != null; n = n.getNextSibling())
		{
			if ("table".equalsIgnoreCase(n.getNodeName()))
			{
				if (this._currentItem.item != null)
				{
					throw new IllegalStateException("Item created but table node found! Item " + itemId);
				}

				this.parseTable(n);
			}
			else if ("set".equalsIgnoreCase(n.getNodeName()))
			{
				if (this._currentItem.item != null)
				{
					throw new IllegalStateException("Item created but set node found! Item " + itemId);
				}

				this.parseBeanSet(n, this._currentItem.set, 1);
			}
			else if ("stats".equalsIgnoreCase(n.getNodeName()))
			{
				this.makeItem();

				for (Node b = n.getFirstChild(); b != null; b = b.getNextSibling())
				{
					if ("stat".equalsIgnoreCase(b.getNodeName()))
					{
						Stat type = Stat.valueOfXml(b.getAttributes().getNamedItem("type").getNodeValue());
						double value = Double.parseDouble(b.getTextContent());
						this._currentItem.item.addFunctionTemplate(new FuncTemplate(null, null, "add", 0, type, value));
					}
				}
			}
			else if ("skills".equalsIgnoreCase(n.getNodeName()))
			{
				this.makeItem();

				for (Node bx = n.getFirstChild(); bx != null; bx = bx.getNextSibling())
				{
					if ("skill".equalsIgnoreCase(bx.getNodeName()))
					{
						int id = this.parseInteger(bx.getAttributes(), "id");
						int level = this.parseInteger(bx.getAttributes(), "level");
						int subLevel = this.parseInteger(bx.getAttributes(), "subLevel", 0);
						ItemSkillType type = this.parseEnum(bx.getAttributes(), ItemSkillType.class, "type", ItemSkillType.NORMAL);
						int chance = this.parseInteger(bx.getAttributes(), "type_chance", 100);
						int value = this.parseInteger(bx.getAttributes(), "type_value", 0);
						if (type == ItemSkillType.ON_ENCHANT)
						{
							int enchantLimit = this._currentItem.item.getEnchantLimit();
							if (enchantLimit > 0 && value > enchantLimit)
							{
								LOGGER.warning(this.getClass().getSimpleName() + ": Item " + itemId + " has ON_ENCHANT value greater than it's enchant limit.");
							}
						}

						this._currentItem.item.addSkill(new ItemSkillHolder(id, level, subLevel, type, chance, value));
					}
				}
			}
			else if ("capsuled_items".equalsIgnoreCase(n.getNodeName()))
			{
				this.makeItem();

				for (Node bxx = n.getFirstChild(); bxx != null; bxx = bxx.getNextSibling())
				{
					if ("item".equals(bxx.getNodeName()))
					{
						int id = this.parseInteger(bxx.getAttributes(), "id");
						long min = this.parseLong(bxx.getAttributes(), "min");
						long max = this.parseLong(bxx.getAttributes(), "max");
						double chance = this.parseDouble(bxx.getAttributes(), "chance");
						int minEnchant = this.parseInteger(bxx.getAttributes(), "minEnchant", 0);
						int maxEnchant = this.parseInteger(bxx.getAttributes(), "maxEnchant", 0);
						this._currentItem.item.addCapsuledItem(new ExtractableProduct(id, min, max, chance, minEnchant, maxEnchant));
					}
				}
			}
			else if ("conditions".equalsIgnoreCase(n.getNodeName()))
			{
				this.makeItem();
				Condition condition = this.parseCondition(n.getFirstChild(), this._currentItem.item);
				Node msg = n.getAttributes().getNamedItem("msg");
				Node msgId = n.getAttributes().getNamedItem("msgId");
				if (condition != null && msg != null)
				{
					condition.setMessage(msg.getNodeValue());
				}
				else if (condition != null && msgId != null)
				{
					condition.setMessageId(Integer.decode(this.getValue(msgId.getNodeValue(), null)));
					Node addName = n.getAttributes().getNamedItem("addName");
					if (addName != null && Integer.decode(this.getValue(msgId.getNodeValue(), null)) > 0)
					{
						condition.addName();
					}
				}

				this._currentItem.item.attachCondition(condition);
			}
		}

		this.makeItem();
	}

	private void makeItem() throws InvocationTargetException
	{
		if (this._currentItem.item != null)
		{
			this._currentItem.item.set(this._currentItem.set);
		}
		else
		{
			try
			{
				Constructor<?> itemClass = Class.forName("net.sf.l2jdev.gameserver.model.item." + this._currentItem.type).getConstructor(StatSet.class);
				this._currentItem.item = (ItemTemplate) itemClass.newInstance(this._currentItem.set);
			}
			catch (Exception var2)
			{
				throw new InvocationTargetException(var2);
			}
		}
	}

	public List<ItemTemplate> getItemList()
	{
		return this._itemsInFile;
	}

	@Override
	public void load()
	{
	}

	@Override
	public void parseDocument(Document document, File file)
	{
	}

	private class DocumentItemDataHolder
	{
		int id;
		String type;
		StatSet set;
		int currentLevel;
		ItemTemplate item;

		public DocumentItemDataHolder()
		{
			Objects.requireNonNull(DocumentItem.this);
			super();
		}
	}
}
