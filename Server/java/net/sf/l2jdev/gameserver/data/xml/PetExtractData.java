package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.data.holders.PetExtractionHolder;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class PetExtractData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(PetExtractData.class.getName());
	private final Map<Integer, Map<Integer, PetExtractionHolder>> _extractionData = new HashMap<>();

	protected PetExtractData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this._extractionData.clear();
		this.parseDatapackFile("data/PetExtractData.xml");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._extractionData.size() + " pet extraction data.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		for (Node n = document.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("extraction".equalsIgnoreCase(d.getNodeName()))
					{
						NamedNodeMap attrs = d.getAttributes();
						int petId = this.parseInteger(attrs, "petId");
						int petLevel = this.parseInteger(attrs, "petLevel");
						long extractExp = this.parseLong(attrs, "extractExp");
						int extractItem = this.parseInteger(attrs, "extractItem");
						int defaultCostId = this.parseInteger(attrs, "defaultCostId");
						int defaultCostCount = this.parseInteger(attrs, "defaultCostCount");
						int extractCostId = this.parseInteger(attrs, "extractCostId");
						int extractCostCount = this.parseInteger(attrs, "extractCostCount");
						Map<Integer, PetExtractionHolder> data = this._extractionData.get(petId);
						if (data == null)
						{
							data = new HashMap<>();
							this._extractionData.put(petId, data);
						}

						data.put(petLevel, new PetExtractionHolder(petId, petLevel, extractExp, extractItem, new ItemHolder(defaultCostId, defaultCostCount), new ItemHolder(extractCostId, extractCostCount)));
					}
				}
			}
		}
	}

	public PetExtractionHolder getExtraction(int petId, int petLevel)
	{
		Map<Integer, PetExtractionHolder> map = this._extractionData.get(petId);
		if (map == null)
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": Missing pet extraction data: [PetId: " + petId + "] [PetLevel: " + petLevel + "]");
			return null;
		}
		return map.get(petLevel);
	}

	public static PetExtractData getInstance()
	{
		return PetExtractData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final PetExtractData INSTANCE = new PetExtractData();
	}
}
