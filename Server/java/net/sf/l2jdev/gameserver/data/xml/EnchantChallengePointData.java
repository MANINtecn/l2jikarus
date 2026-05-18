package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class EnchantChallengePointData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(EnchantChallengePointData.class.getName());
	public static final int OPTION_PROB_INC1 = 0;
	public static final int OPTION_PROB_INC2 = 1;
	public static final int OPTION_OVER_UP_PROB = 2;
	public static final int OPTION_NUM_RESET_PROB = 3;
	public static final int OPTION_NUM_DOWN_PROB = 4;
	public static final int OPTION_NUM_PROTECT_PROB = 5;
	private final Map<Integer, Map<Integer, EnchantChallengePointData.EnchantChallengePointsOptionInfo>> _groupOptions = new HashMap<>();
	private final Map<Integer, Integer> _fees = new HashMap<>();
	private final Map<Integer, EnchantChallengePointData.EnchantChallengePointsItemInfo> _items = new HashMap<>();
	private int _maxPoints;
	private int _maxTicketCharge;

	public EnchantChallengePointData()
	{
		this.load();
	}

	@Override
	public synchronized void load()
	{
		this._groupOptions.clear();
		this._fees.clear();
		this._items.clear();
		this.parseDatapackFile("data/EnchantChallengePoints.xml");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._groupOptions.size() + " groups and " + this._fees.size() + " options.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		for (Node n = document.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node z = n.getFirstChild(); z != null; z = z.getNextSibling())
				{
					if ("maxPoints".equalsIgnoreCase(z.getNodeName()))
					{
						this._maxPoints = Integer.parseInt(z.getTextContent());
					}
					else if ("maxTicketCharge".equalsIgnoreCase(z.getNodeName()))
					{
						this._maxTicketCharge = Integer.parseInt(z.getTextContent());
					}
					else if ("fees".equalsIgnoreCase(z.getNodeName()))
					{
						for (Node d = z.getFirstChild(); d != null; d = d.getNextSibling())
						{
							if ("option".equalsIgnoreCase(d.getNodeName()))
							{
								NamedNodeMap attrs = d.getAttributes();
								int index = this.parseInteger(attrs, "index");
								int fee = this.parseInteger(attrs, "fee");
								this._fees.put(index, fee);
							}
						}
					}
					else if ("groups".equalsIgnoreCase(z.getNodeName()))
					{
						for (Node dx = z.getFirstChild(); dx != null; dx = dx.getNextSibling())
						{
							if ("group".equalsIgnoreCase(dx.getNodeName()))
							{
								NamedNodeMap attrs = dx.getAttributes();
								int groupId = this.parseInteger(attrs, "id");
								Map<Integer, EnchantChallengePointData.EnchantChallengePointsOptionInfo> options = this._groupOptions.get(groupId);
								if (options == null)
								{
									options = new HashMap<>();
									this._groupOptions.put(groupId, options);
								}

								for (Node e = dx.getFirstChild(); e != null; e = e.getNextSibling())
								{
									if ("option".equalsIgnoreCase(e.getNodeName()))
									{
										NamedNodeMap optionAttrs = e.getAttributes();
										int index = this.parseInteger(optionAttrs, "index");
										int chance = this.parseInteger(optionAttrs, "chance");
										int minEnchant = this.parseInteger(optionAttrs, "minEnchant");
										int maxEnchant = this.parseInteger(optionAttrs, "maxEnchant");
										options.put(index, new EnchantChallengePointData.EnchantChallengePointsOptionInfo(index, chance, minEnchant, maxEnchant));
									}
									else if ("item".equals(e.getNodeName()))
									{
										NamedNodeMap itemAttrs = e.getAttributes();
										String[] itemIdsStr = this.parseString(itemAttrs, "id").split(";");
										Map<Integer, Integer> enchantLevels = new HashMap<>();

										for (Node g = e.getFirstChild(); g != null; g = g.getNextSibling())
										{
											if ("enchant".equals(g.getNodeName()))
											{
												NamedNodeMap enchantAttrs = g.getAttributes();
												int enchantLevel = this.parseInteger(enchantAttrs, "level");
												int points = this.parseInteger(enchantAttrs, "points");
												enchantLevels.put(enchantLevel, points);
											}
										}

										for (String itemIdStr : itemIdsStr)
										{
											int itemId = Integer.parseInt(itemIdStr);
											if (ItemData.getInstance().getTemplate(itemId) == null)
											{
												LOGGER.info(this.getClass().getSimpleName() + ": Item with id " + itemId + " does not exist.");
											}
											else
											{
												this._items.put(itemId, new EnchantChallengePointData.EnchantChallengePointsItemInfo(itemId, groupId, enchantLevels));
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public int getMaxPoints()
	{
		return this._maxPoints;
	}

	public int getMaxTicketCharge()
	{
		return this._maxTicketCharge;
	}

	public EnchantChallengePointData.EnchantChallengePointsOptionInfo getOptionInfo(int groupId, int optionIndex)
	{
		return this._groupOptions.get(groupId).get(optionIndex);
	}

	public EnchantChallengePointData.EnchantChallengePointsItemInfo getInfoByItemId(int itemId)
	{
		return this._items.get(itemId);
	}

	public int getFeeForOptionIndex(int optionIndex)
	{
		return this._fees.get(optionIndex);
	}

	public int[] handleFailure(Player player, Item item)
	{
		EnchantChallengePointData.EnchantChallengePointsItemInfo info = this.getInfoByItemId(item.getId());
		if (info == null)
		{
			return new int[]
			{
				-1,
				-1
			};
		}
		int groupId = info.groupId();
		int pointsToGive = info.pointsByEnchantLevel().getOrDefault(item.getEnchantLevel(), 0);
		if (pointsToGive > 0)
		{
			player.getChallengeInfo().getChallengePoints().compute(groupId, (_, v) -> v == null ? Math.min(this.getMaxPoints(), pointsToGive) : Math.min(this.getMaxPoints(), v + pointsToGive));
			player.getChallengeInfo().setNowGroup(groupId);
			player.getChallengeInfo().setNowGroup(pointsToGive);
		}

		return new int[]
		{
			groupId,
			pointsToGive
		};
	}

	public static EnchantChallengePointData getInstance()
	{
		return EnchantChallengePointData.SingletonHolder.INSTANCE;
	}

	public record EnchantChallengePointsItemInfo(int itemId, int groupId, Map<Integer, Integer> pointsByEnchantLevel)
	{
	}

	public record EnchantChallengePointsOptionInfo(int index, int chance, int minEnchant, int maxEnchant)
	{
	}

	private static class SingletonHolder
	{
		protected static final EnchantChallengePointData INSTANCE = new EnchantChallengePointData();
	}
}
