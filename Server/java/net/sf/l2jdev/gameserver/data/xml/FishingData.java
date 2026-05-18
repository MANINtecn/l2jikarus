package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.model.fishing.FishingBait;
import net.sf.l2jdev.gameserver.model.fishing.FishingCatch;
import net.sf.l2jdev.gameserver.model.fishing.FishingRod;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class FishingData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(FishingData.class.getName());
	private final Map<Integer, FishingBait> _baitData = new HashMap<>();
	private final Map<Integer, FishingRod> _rodData = new HashMap<>();
	private int _baitDistanceMin;
	private int _baitDistanceMax;
	private double _expRateMin;
	private double _expRateMax;
	private double _spRateMin;
	private double _spRateMax;

	protected FishingData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this._baitData.clear();
		this.parseDatapackFile("data/Fishing.xml");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._baitData.size() + " bait and " + this._rodData.size() + " rod data.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		for (Node n = document.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node listItem = n.getFirstChild(); listItem != null; listItem = listItem.getNextSibling())
				{
					String var5 = listItem.getNodeName();
					switch (var5)
					{
						case "baitDistance":
							this._baitDistanceMin = this.parseInteger(listItem.getAttributes(), "min");
							this._baitDistanceMax = this.parseInteger(listItem.getAttributes(), "max");
							break;
						case "xpRate":
							this._expRateMin = this.parseDouble(listItem.getAttributes(), "min");
							this._expRateMax = this.parseDouble(listItem.getAttributes(), "max");
							break;
						case "spRate":
							this._spRateMin = this.parseDouble(listItem.getAttributes(), "min");
							this._spRateMax = this.parseDouble(listItem.getAttributes(), "max");
							break;
						case "baits":
							Node bait = listItem.getFirstChild();

							for (; bait != null; bait = bait.getNextSibling())
							{
								if ("bait".equalsIgnoreCase(bait.getNodeName()))
								{
									NamedNodeMap attrs = bait.getAttributes();
									int itemId = this.parseInteger(attrs, "itemId");
									int level = this.parseInteger(attrs, "level", 1);
									int minPlayerLevel = this.parseInteger(attrs, "minPlayerLevel");
									int maxPlayerLevel = this.parseInteger(attrs, "maxPlayerLevel", PlayerConfig.PLAYER_MAXIMUM_LEVEL);
									double chance = this.parseDouble(attrs, "chance");
									int timeMin = this.parseInteger(attrs, "timeMin");
									int timeMax = this.parseInteger(attrs, "timeMax", timeMin);
									int waitMin = this.parseInteger(attrs, "waitMin");
									int waitMax = this.parseInteger(attrs, "waitMax", waitMin);
									boolean isPremiumOnly = this.parseBoolean(attrs, "isPremiumOnly", false);
									if (ItemData.getInstance().getTemplate(itemId) == null)
									{
										LOGGER.info(this.getClass().getSimpleName() + ": Could not find item with id " + itemId);
									}
									else
									{
										FishingBait baitData = new FishingBait(itemId, level, minPlayerLevel, maxPlayerLevel, chance, timeMin, timeMax, waitMin, waitMax, isPremiumOnly);

										for (Node c = bait.getFirstChild(); c != null; c = c.getNextSibling())
										{
											if ("catch".equalsIgnoreCase(c.getNodeName()))
											{
												NamedNodeMap cAttrs = c.getAttributes();
												int cId = this.parseInteger(cAttrs, "itemId");
												float cChance = this.parseFloat(cAttrs, "chance");
												float cMultiplier = this.parseFloat(cAttrs, "multiplier", 1.0F);
												if (ItemData.getInstance().getTemplate(cId) == null)
												{
													LOGGER.info(this.getClass().getSimpleName() + ": Could not find item with id " + itemId);
												}
												else
												{
													baitData.addReward(new FishingCatch(cId, cChance, cMultiplier));
												}
											}
										}

										this._baitData.put(baitData.getItemId(), baitData);
									}
								}
							}
							break;
						case "rods":
							for (Node rod = listItem.getFirstChild(); rod != null; rod = rod.getNextSibling())
							{
								if ("rod".equalsIgnoreCase(rod.getNodeName()))
								{
									NamedNodeMap attrs = rod.getAttributes();
									int itemId = this.parseInteger(attrs, "itemId");
									int reduceFishingTime = this.parseInteger(attrs, "reduceFishingTime", 0);
									float xpMultiplier = this.parseFloat(attrs, "xpMultiplier", 1.0F);
									float spMultiplier = this.parseFloat(attrs, "spMultiplier", 1.0F);
									if (ItemData.getInstance().getTemplate(itemId) == null)
									{
										LOGGER.info(this.getClass().getSimpleName() + ": Could not find item with id " + itemId);
									}
									else
									{
										this._rodData.put(itemId, new FishingRod(itemId, reduceFishingTime, xpMultiplier, spMultiplier));
									}
								}
							}
					}
				}
			}
		}
	}

	public FishingBait getBaitData(int baitItemId)
	{
		return this._baitData.get(baitItemId);
	}

	public FishingRod getRodData(int rodItemId)
	{
		return this._rodData.get(rodItemId);
	}

	public int getBaitDistanceMin()
	{
		return this._baitDistanceMin;
	}

	public int getBaitDistanceMax()
	{
		return this._baitDistanceMax;
	}

	public double getExpRateMin()
	{
		return this._expRateMin;
	}

	public double getExpRateMax()
	{
		return this._expRateMax;
	}

	public double getSpRateMin()
	{
		return this._spRateMin;
	}

	public double getSpRateMax()
	{
		return this._spRateMax;
	}

	public static FishingData getInstance()
	{
		return FishingData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final FishingData INSTANCE = new FishingData();
	}
}
