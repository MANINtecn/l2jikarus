package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.residences.ResidenceFunctionTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ResidenceFunctionsData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(ResidenceFunctionsData.class.getName());
	private final Map<Integer, List<ResidenceFunctionTemplate>> _functions = new HashMap<>();

	protected ResidenceFunctionsData()
	{
		this.load();
	}

	@Override
	public synchronized void load()
	{
		this._functions.clear();
		this.parseDatapackFile("data/ResidenceFunctions.xml");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._functions.size() + " functions.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		this.forEach(document, "list", list -> this.forEach(list, "function", func -> {
			NamedNodeMap attrs = func.getAttributes();
			StatSet set = new StatSet(HashMap::new);

			for (int i = 0; i < attrs.getLength(); i++)
			{
				Node node = attrs.item(i);
				set.set(node.getNodeName(), node.getNodeValue());
			}

			this.forEach(func, "function", levelNode -> {
				NamedNodeMap levelAttrs = levelNode.getAttributes();
				StatSet levelSet = new StatSet(HashMap::new);
				levelSet.merge(set);

				for (int ix = 0; ix < levelAttrs.getLength(); ix++)
				{
					Node nodex = levelAttrs.item(ix);
					levelSet.set(nodex.getNodeName(), nodex.getNodeValue());
				}

				ResidenceFunctionTemplate template = new ResidenceFunctionTemplate(levelSet);
				this._functions.computeIfAbsent(template.getId(), _ -> new ArrayList<>()).add(template);
			});
		}));
	}

	public ResidenceFunctionTemplate getFunction(int id, int level)
	{
		if (this._functions.containsKey(id))
		{
			for (ResidenceFunctionTemplate template : this._functions.get(id))
			{
				if (template.getLevel() == level)
				{
					return template;
				}
			}
		}

		return null;
	}

	public List<ResidenceFunctionTemplate> getFunctions(int id)
	{
		return this._functions.get(id);
	}

	public static ResidenceFunctionsData getInstance()
	{
		return ResidenceFunctionsData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final ResidenceFunctionsData INSTANCE = new ResidenceFunctionsData();
	}
}
