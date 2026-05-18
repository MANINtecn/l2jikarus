package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.data.holders.TimedHuntingZoneHolder;
import net.sf.l2jdev.gameserver.model.Location;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class TimedHuntingZoneData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(TimedHuntingZoneData.class.getName());
	private final Map<Integer, TimedHuntingZoneHolder> _timedHuntingZoneData = new HashMap<>();

	protected TimedHuntingZoneData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this._timedHuntingZoneData.clear();
		this.parseDatapackFile("data/TimedHuntingZoneData.xml");
		if (!this._timedHuntingZoneData.isEmpty())
		{
			LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._timedHuntingZoneData.size() + " timed hunting zones.");
		}
		else
		{
			LOGGER.info(this.getClass().getSimpleName() + ": System is disabled.");
		}
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		for (Node xmlNode = document.getFirstChild(); xmlNode != null; xmlNode = xmlNode.getNextSibling())
		{
			if ("list".equalsIgnoreCase(xmlNode.getNodeName()))
			{
				NamedNodeMap listAttributes = xmlNode.getAttributes();
				Node attribute = listAttributes.getNamedItem("enabled");
				if (attribute != null && Boolean.parseBoolean(attribute.getNodeValue()))
				{
					for (Node listNode = xmlNode.getFirstChild(); listNode != null; listNode = listNode.getNextSibling())
					{
						if ("zone".equalsIgnoreCase(listNode.getNodeName()))
						{
							NamedNodeMap zoneAttributes = listNode.getAttributes();
							int id = this.parseInteger(zoneAttributes, "id");
							String name = this.parseString(zoneAttributes, "name", "");
							int initialTime = 0;
							int maxAddedTime = 0;
							int resetDelay = 0;
							int entryItemId = 57;
							int entryFee = 10000;
							int minLevel = 1;
							int maxLevel = 999;
							int remainRefillTime = 3600;
							int refillTimeMax = 3600;
							boolean pvpZone = false;
							boolean noPvpZone = false;
							int instanceId = 0;
							boolean soloInstance = true;
							boolean weekly = false;
							boolean useWorldPrefix = false;
							boolean zonePremiumUserOnly = false;
							Location enterLocation = null;
							Location exitLocation = null;
							boolean isEvenWeek = true;
							boolean isSwapWeek = false;

							for (Node zoneNode = listNode.getFirstChild(); zoneNode != null; zoneNode = zoneNode.getNextSibling())
							{
								String var31 = zoneNode.getNodeName();
								switch (var31)
								{
									case "enterLocation":
									{
										String[] coordinates = zoneNode.getTextContent().split(",");
										enterLocation = new Location(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1]), Integer.parseInt(coordinates[2]));
										break;
									}
									case "exitLocation":
									{
										String[] coordinates = zoneNode.getTextContent().split(",");
										exitLocation = new Location(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1]), Integer.parseInt(coordinates[2]));
										break;
									}
									case "initialTime":
										initialTime = Integer.parseInt(zoneNode.getTextContent()) * 1000;
										break;
									case "maxAddedTime":
										maxAddedTime = Integer.parseInt(zoneNode.getTextContent()) * 1000;
										break;
									case "resetDelay":
										resetDelay = Integer.parseInt(zoneNode.getTextContent()) * 1000;
										break;
									case "entryItemId":
										entryItemId = Integer.parseInt(zoneNode.getTextContent());
										break;
									case "entryFee":
										entryFee = Integer.parseInt(zoneNode.getTextContent());
										break;
									case "minLevel":
										minLevel = Integer.parseInt(zoneNode.getTextContent());
										break;
									case "maxLevel":
										maxLevel = Integer.parseInt(zoneNode.getTextContent());
										break;
									case "remainRefillTime":
										remainRefillTime = Integer.parseInt(zoneNode.getTextContent());
										break;
									case "refillTimeMax":
										refillTimeMax = Integer.parseInt(zoneNode.getTextContent());
										break;
									case "pvpZone":
										pvpZone = Boolean.parseBoolean(zoneNode.getTextContent());
										break;
									case "noPvpZone":
										noPvpZone = Boolean.parseBoolean(zoneNode.getTextContent());
										break;
									case "instanceId":
										instanceId = Integer.parseInt(zoneNode.getTextContent());
										break;
									case "soloInstance":
										soloInstance = Boolean.parseBoolean(zoneNode.getTextContent());
										break;
									case "weekly":
										weekly = Boolean.parseBoolean(zoneNode.getTextContent());
										break;
									case "useWorldPrefix":
										useWorldPrefix = Boolean.parseBoolean(zoneNode.getTextContent());
										break;
									case "zonePremiumUserOnly":
										zonePremiumUserOnly = Boolean.parseBoolean(zoneNode.getTextContent());
										break;
									case "isEvenWeek":
										isEvenWeek = Boolean.parseBoolean(zoneNode.getTextContent());
										break;
									case "isSwapWeek":
										isSwapWeek = Boolean.parseBoolean(zoneNode.getTextContent());
								}
							}

							this._timedHuntingZoneData.put(id, new TimedHuntingZoneHolder(id, name, initialTime, maxAddedTime, resetDelay, entryItemId, entryFee, minLevel, maxLevel, remainRefillTime, refillTimeMax, pvpZone, noPvpZone, instanceId, soloInstance, weekly, useWorldPrefix, zonePremiumUserOnly, enterLocation, exitLocation, isEvenWeek, isSwapWeek));
						}
					}
				}
			}
		}
	}

	public TimedHuntingZoneHolder getHuntingZone(int zoneId)
	{
		return this._timedHuntingZoneData.get(zoneId);
	}

	public Collection<TimedHuntingZoneHolder> getAllHuntingZones()
	{
		return this._timedHuntingZoneData.values();
	}

	public int getSize()
	{
		return this._timedHuntingZoneData.size();
	}

	public static TimedHuntingZoneData getInstance()
	{
		return TimedHuntingZoneData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final TimedHuntingZoneData INSTANCE = new TimedHuntingZoneData();
	}
}
