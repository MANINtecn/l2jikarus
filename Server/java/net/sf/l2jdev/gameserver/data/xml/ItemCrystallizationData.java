package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.data.enums.CrystallizationType;
import net.sf.l2jdev.gameserver.data.holders.CrystallizationDataHolder;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.Armor;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.Weapon;
import net.sf.l2jdev.gameserver.model.item.enchant.RewardItemsOnFailure;
import net.sf.l2jdev.gameserver.model.item.holders.ItemChanceHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.item.type.CrystalType;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ItemCrystallizationData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(ItemCrystallizationData.class.getName());
	private final Map<CrystalType, Map<CrystallizationType, List<ItemChanceHolder>>> _crystallizationTemplates = new EnumMap<>(CrystalType.class);
	private final Map<Integer, CrystallizationDataHolder> _items = new HashMap<>();
	private RewardItemsOnFailure _weaponDestroyGroup = new RewardItemsOnFailure();
	private RewardItemsOnFailure _armorDestroyGroup = new RewardItemsOnFailure();

	protected ItemCrystallizationData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this._crystallizationTemplates.clear();

		for (CrystalType crystalType : CrystalType.values())
		{
			this._crystallizationTemplates.put(crystalType, new EnumMap<>(CrystallizationType.class));
		}

		this._items.clear();
		this._weaponDestroyGroup = new RewardItemsOnFailure();
		this._armorDestroyGroup = new RewardItemsOnFailure();
		this.parseDatapackFile("data/CrystallizableItems.xml");
		if (this._crystallizationTemplates.size() > 0)
		{
			LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._crystallizationTemplates.size() + " crystallization templates.");
		}

		if (this._items.size() > 0)
		{
			LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._items.size() + " pre-defined crystallizable items.");
		}

		this.generateCrystallizationData();
		if (this._weaponDestroyGroup.size() > 0)
		{
			LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._weaponDestroyGroup.size() + " weapon enchant failure rewards.");
		}

		if (this._armorDestroyGroup.size() > 0)
		{
			LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._armorDestroyGroup.size() + " armor enchant failure rewards.");
		}
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		for (Node n = document.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node o = n.getFirstChild(); o != null; o = o.getNextSibling())
				{
					if ("templates".equalsIgnoreCase(o.getNodeName()))
					{
						for (Node d = o.getFirstChild(); d != null; d = d.getNextSibling())
						{
							if ("crystallizable_template".equalsIgnoreCase(d.getNodeName()))
							{
								CrystalType crystalType = this.parseEnum(d.getAttributes(), CrystalType.class, "crystalType");
								CrystallizationType crystallizationType = this.parseEnum(d.getAttributes(), CrystallizationType.class, "crystallizationType");
								List<ItemChanceHolder> crystallizeRewards = new ArrayList<>();

								for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
								{
									if ("item".equalsIgnoreCase(c.getNodeName()))
									{
										NamedNodeMap attrs = c.getAttributes();
										int itemId = this.parseInteger(attrs, "id");
										long itemCount = this.parseLong(attrs, "count");
										double itemChance = this.parseDouble(attrs, "chance");
										crystallizeRewards.add(new ItemChanceHolder(itemId, itemChance, itemCount));
									}
								}

								this._crystallizationTemplates.get(crystalType).put(crystallizationType, crystallizeRewards);
							}
						}
					}
					else if (!"items".equalsIgnoreCase(o.getNodeName()))
					{
						if ("itemsOnEnchantFailure".equals(o.getNodeName()))
						{
							for (Node dx = o.getFirstChild(); dx != null; dx = dx.getNextSibling())
							{
								if ("armor".equalsIgnoreCase(dx.getNodeName()))
								{
									this._armorDestroyGroup = this.getFormedHolder(dx);
								}
								else if ("weapon".equalsIgnoreCase(dx.getNodeName()))
								{
									this._weaponDestroyGroup = this.getFormedHolder(dx);
								}
							}
						}
					}
					else
					{
						for (Node dxx = o.getFirstChild(); dxx != null; dxx = dxx.getNextSibling())
						{
							if ("crystallizable_item".equalsIgnoreCase(dxx.getNodeName()))
							{
								int id = this.parseInteger(dxx.getAttributes(), "id");
								List<ItemChanceHolder> crystallizeRewards = new ArrayList<>();

								for (Node cx = dxx.getFirstChild(); cx != null; cx = cx.getNextSibling())
								{
									if ("item".equalsIgnoreCase(cx.getNodeName()))
									{
										NamedNodeMap attrs = cx.getAttributes();
										int itemId = this.parseInteger(attrs, "id");
										long itemCount = this.parseLong(attrs, "count");
										double itemChance = this.parseDouble(attrs, "chance");
										crystallizeRewards.add(new ItemChanceHolder(itemId, itemChance, itemCount));
									}
								}

								this._items.put(id, new CrystallizationDataHolder(id, crystallizeRewards));
							}
						}
					}
				}
			}
		}
	}

	private RewardItemsOnFailure getFormedHolder(Node node)
	{
		RewardItemsOnFailure holder = new RewardItemsOnFailure();

		for (Node z = node.getFirstChild(); z != null; z = z.getNextSibling())
		{
			if ("item".equals(z.getNodeName()))
			{
				StatSet failItems = new StatSet(this.parseAttributes(z));
				int rewardId = failItems.getInt("id");
				int enchantLevel = failItems.getInt("enchant");
				double chance = failItems.getDouble("chance");
				String[] targetIds = failItems.getString("targetIds", "").split(",");

				for (String s : targetIds)
				{
					if (!s.isEmpty())
					{
						int destroyedItemId = Integer.parseInt(s.trim());
						long count = failItems.getLong("amountR", 1L);
						holder.addItemToHolder(destroyedItemId, enchantLevel, rewardId, count, chance);
					}
				}
			}
		}

		return holder;
	}

	public List<ItemChanceHolder> getRewardItems(Item item, int enchantLevel)
	{
		if (item == null)
		{
			return Collections.emptyList();
		}
		RewardItemsOnFailure holder = item.isWeapon() ? this._weaponDestroyGroup : this._armorDestroyGroup;
		return holder == null ? Collections.emptyList() : holder.getRewardItems(item.getId(), enchantLevel);
	}

	public int getLoadedCrystallizationTemplateCount()
	{
		return this._crystallizationTemplates.size();
	}

	private static List<ItemChanceHolder> calculateCrystallizeRewards(ItemTemplate item, List<ItemChanceHolder> crystallizeRewards)
	{
		if (crystallizeRewards == null)
		{
			return null;
		}
		List<ItemChanceHolder> rewards = new ArrayList<>();

		for (ItemChanceHolder reward : crystallizeRewards)
		{
			double chance = reward.getChance() * item.getCrystalCount();
			long count = reward.getCount();
			if (chance > 100.0)
			{
				double countMul = Math.ceil(chance / 100.0);
				chance /= countMul;
				count = (long) (count * countMul);
			}

			rewards.add(new ItemChanceHolder(reward.getId(), chance, count));
		}

		return rewards;
	}

	private void generateCrystallizationData()
	{
		int previousCount = this._items.size();

		for (ItemTemplate item : ItemData.getInstance().getAllItems())
		{
			if ((item instanceof Weapon || item instanceof Armor) && item.isCrystallizable() && !this._items.containsKey(item.getId()))
			{
				List<ItemChanceHolder> holder = this._crystallizationTemplates.get(item.getCrystalType()).get(item instanceof Weapon ? CrystallizationType.WEAPON : CrystallizationType.ARMOR);
				if (holder != null)
				{
					this._items.put(item.getId(), new CrystallizationDataHolder(item.getId(), calculateCrystallizeRewards(item, holder)));
				}
			}
		}

		int generated = this._items.size() - previousCount;
		if (generated > 0)
		{
			LOGGER.info(this.getClass().getSimpleName() + ": Generated " + generated + " crystallizable items from templates.");
		}
	}

	public List<ItemChanceHolder> getCrystallizationTemplate(CrystalType crystalType, CrystallizationType crystallizationType)
	{
		return this._crystallizationTemplates.get(crystalType).get(crystallizationType);
	}

	public CrystallizationDataHolder getCrystallizationData(int itemId)
	{
		return this._items.get(itemId);
	}

	public List<ItemChanceHolder> getCrystallizationRewards(Item item)
	{
		List<ItemChanceHolder> result = new ArrayList<>();
		int crystalItemId = item.getTemplate().getCrystalItemId();
		CrystallizationDataHolder data = this.getCrystallizationData(item.getId());
		if (data != null)
		{
			boolean found = false;
			List<ItemChanceHolder> items = data.getItems();

			for (ItemChanceHolder holder : items)
			{
				if (holder.getId() == crystalItemId)
				{
					found = true;
					break;
				}
			}

			if (!found)
			{
				result.add(new ItemChanceHolder(crystalItemId, 100.0, item.getCrystalCount()));
			}

			result.addAll(items);
		}
		else
		{
			result.add(new ItemChanceHolder(crystalItemId, 100.0, item.getCrystalCount()));
		}

		return result;
	}

	public List<ItemChanceHolder> getItemOnDestroy(Player player, Item item)
	{
		if (player != null && item != null)
		{
			RewardItemsOnFailure holder = item.isWeapon() ? this._weaponDestroyGroup : this._armorDestroyGroup;
			int destroyedItemId = item.getId();
			return holder.getRewardItems(destroyedItemId, item.getEnchantLevel()).isEmpty() ? Collections.emptyList() : holder.getRewardItems(destroyedItemId, item.getEnchantLevel());
		}
		return Collections.emptyList();
	}

	public static ItemCrystallizationData getInstance()
	{
		return ItemCrystallizationData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final ItemCrystallizationData INSTANCE = new ItemCrystallizationData();
	}
}
