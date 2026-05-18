package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.commons.util.StringUtil;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.options.EnchantOptions;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class EnchantItemOptionsData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(EnchantItemOptionsData.class.getName());
	private final Map<Integer, Map<Integer, EnchantOptions>> _data = new HashMap<>();

	protected EnchantItemOptionsData()
	{
		this.load();
	}

	@Override
	public synchronized void load()
	{
		this._data.clear();
		this.parseDatapackFile("data/EnchantItemOptions.xml");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		int counter = 0;

		for (Node n = document.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				label68:
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("item".equalsIgnoreCase(d.getNodeName()))
					{
						int itemId = this.parseInteger(d.getAttributes(), "id");
						ItemTemplate template = ItemData.getInstance().getTemplate(itemId);
						if (template == null)
						{
							LOGGER.warning(this.getClass().getSimpleName() + ": Could not find item template for id " + itemId);
						}
						else
						{
							for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
							{
								if ("options".equalsIgnoreCase(cd.getNodeName()))
								{
									EnchantOptions op = new EnchantOptions(this.parseInteger(cd.getAttributes(), "level"));

									for (byte i = 0; i < 3; i++)
									{
										Node att = cd.getAttributes().getNamedItem("option" + (i + 1));
										if (att != null && StringUtil.isNumeric(att.getNodeValue()))
										{
											int id = this.parseInteger(att);
											if (OptionData.getInstance().getOptions(id) == null)
											{
												LOGGER.warning(this.getClass().getSimpleName() + ": Could not find option " + id + " for item " + template);
												continue label68;
											}

											Map<Integer, EnchantOptions> data = this._data.get(itemId);
											if (data == null)
											{
												data = new HashMap<>();
												this._data.put(itemId, data);
											}

											if (!data.containsKey(op.getLevel()))
											{
												data.put(op.getLevel(), op);
											}

											op.setOption(i, id);
										}
									}

									counter++;
								}
							}
						}
					}
				}
			}
		}

		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._data.size() + " items and " + counter + " options.");
	}

	public boolean hasOptions(int itemId)
	{
		return this._data.containsKey(itemId);
	}

	public EnchantOptions getOptions(int itemId, int enchantLevel)
	{
		return this._data.containsKey(itemId) && this._data.get(itemId).containsKey(enchantLevel) ? this._data.get(itemId).get(enchantLevel) : null;
	}

	public EnchantOptions getOptions(Item item)
	{
		return item != null ? this.getOptions(item.getId(), item.getEnchantLevel()) : null;
	}

	public static EnchantItemOptionsData getInstance()
	{
		return EnchantItemOptionsData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final EnchantItemOptionsData INSTANCE = new EnchantItemOptionsData();
	}
}
