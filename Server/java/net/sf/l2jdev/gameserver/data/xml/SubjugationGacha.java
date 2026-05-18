package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.model.StatSet;
import org.w3c.dom.Document;

public class SubjugationGacha implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(SubjugationGacha.class.getName());
	private static final Map<Integer, Map<Integer, Double>> _subjugations = new HashMap<>();

	public SubjugationGacha()
	{
		this.load();
	}

	@Override
	public void load()
	{
		_subjugations.clear();
		this.parseDatapackFile("data/SubjugationGacha.xml");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + _subjugations.size() + " data.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		this.forEach(document, "list", listNode -> this.forEach(listNode, "purge", purgeNode -> {
			StatSet set = new StatSet(this.parseAttributes(purgeNode));
			int category = set.getInt("category");
			Map<Integer, Double> items = new LinkedHashMap<>();
			this.forEach(purgeNode, "item", npcNode -> {
				StatSet stats = new StatSet(this.parseAttributes(npcNode));
				int itemId = stats.getInt("id");
				double rate = stats.getDouble("rate");
				items.put(itemId, rate);
			});
			_subjugations.put(category, items);
		}));
	}

	public Map<Integer, Double> getSubjugation(int category)
	{
		return _subjugations.get(category);
	}

	public static SubjugationGacha getInstance()
	{
		return SubjugationGacha.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final SubjugationGacha INSTANCE = new SubjugationGacha();
	}
}
