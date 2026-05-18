package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.model.VariationInstance;
import net.sf.l2jdev.gameserver.model.item.EtcItem;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.options.OptionDataCategory;
import net.sf.l2jdev.gameserver.model.options.OptionDataGroup;
import net.sf.l2jdev.gameserver.model.options.Options;
import net.sf.l2jdev.gameserver.model.options.Variation;
import net.sf.l2jdev.gameserver.model.options.VariationFee;
import org.w3c.dom.Document;

public class VariationData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(VariationData.class.getSimpleName());
	private final Map<Integer, Set<Integer>> _itemGroups = new HashMap<>();
	private final Map<Integer, List<Variation>> _variations = new ConcurrentHashMap<>();
	private final Map<Integer, Map<Integer, VariationFee>> _fees = new ConcurrentHashMap<>();

	protected VariationData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this._itemGroups.clear();
		this._variations.clear();
		this._fees.clear();
		this.parseDatapackFile("data/stats/augmentation/Variations.xml");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._itemGroups.size() + " item groups.");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._variations.size() + " variations.");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._fees.size() + " fees.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		this.forEach(document, "list", listNode -> {
			this.forEach(listNode, "variations", variationsNode -> this.forEach(variationsNode, "variation", variationNode -> {
				int mineralId = this.parseInteger(variationNode.getAttributes(), "mineralId");
				int itemGroup = this.parseInteger(variationNode.getAttributes(), "itemGroup", -1);
				if (ItemData.getInstance().getTemplate(mineralId) == null)
				{
					LOGGER.warning(this.getClass().getSimpleName() + ": Mineral with item id " + mineralId + " was not found.");
				}

				Variation variation = new Variation(mineralId, itemGroup);
				this.forEach(variationNode, "optionGroup", groupNode -> {
					int order = this.parseInteger(groupNode.getAttributes(), "order");
					List<OptionDataCategory> sets = new ArrayList<>();
					this.forEach(groupNode, "optionCategory", categoryNode -> {
						double chance = this.parseDouble(categoryNode.getAttributes(), "chance");
						Map<Options, Double> options = new HashMap<>();
						this.forEach(categoryNode, "option", optionNode -> {
							double optionChance = this.parseDouble(optionNode.getAttributes(), "chance");
							int optionId = this.parseInteger(optionNode.getAttributes(), "id");
							Options opt = OptionData.getInstance().getOptions(optionId);
							if (opt == null)
							{
								LOGGER.warning(this.getClass().getSimpleName() + ": Null option for id " + optionId + " mineral " + mineralId);
							}
							else
							{
								options.put(opt, optionChance);
							}
						});
						this.forEach(categoryNode, "optionRange", optionNode -> {
							double optionChance = this.parseDouble(optionNode.getAttributes(), "chance");
							int fromId = this.parseInteger(optionNode.getAttributes(), "from");
							int toId = this.parseInteger(optionNode.getAttributes(), "to");

							for (int id = fromId; id <= toId; id++)
							{
								Options op = OptionData.getInstance().getOptions(id);
								if (op == null)
								{
									LOGGER.warning(this.getClass().getSimpleName() + ": Null option for id " + id + " mineral " + mineralId);
									return;
								}

								options.put(op, optionChance);
							}
						});
						Set<Integer> itemIds = new HashSet<>();
						this.forEach(categoryNode, "item", optionNode -> {
							int itemId = this.parseInteger(optionNode.getAttributes(), "id");
							itemIds.add(itemId);
						});
						this.forEach(categoryNode, "items", optionNode -> {
							int fromId = this.parseInteger(optionNode.getAttributes(), "from");
							int toId = this.parseInteger(optionNode.getAttributes(), "to");

							for (int id = fromId; id <= toId; id++)
							{
								itemIds.add(id);
							}
						});
						sets.add(new OptionDataCategory(options, itemIds, chance));
					});
					variation.setEffectGroup(order, new OptionDataGroup(order, sets));
				});
				List<Variation> list = this._variations.get(mineralId);
				if (list == null)
				{
					list = new ArrayList<>();
				}

				list.add(variation);
				this._variations.put(mineralId, list);
				((EtcItem) ItemData.getInstance().getTemplate(mineralId)).setMineral();
			}));
			this.forEach(listNode, "itemGroups", variationsNode -> this.forEach(variationsNode, "itemGroup", variationNode -> {
				int id = this.parseInteger(variationNode.getAttributes(), "id");
				Set<Integer> items = new HashSet<>();
				this.forEach(variationNode, "item", itemNode -> {
					int itemId = this.parseInteger(itemNode.getAttributes(), "id");
					if (ItemData.getInstance().getTemplate(itemId) == null)
					{
						LOGGER.warning(this.getClass().getSimpleName() + ": Item with id " + itemId + " was not found.");
					}

					items.add(itemId);
				});
				if (this._itemGroups.containsKey(id))
				{
					this._itemGroups.get(id).addAll(items);
				}
				else
				{
					this._itemGroups.put(id, items);
				}
			}));
			this.forEach(listNode, "fees", variationNode -> this.forEach(variationNode, "fee", feeNode -> {
				int itemGroupId = this.parseInteger(feeNode.getAttributes(), "itemGroup");
				Set<Integer> itemGroup = this._itemGroups.get(itemGroupId);
				int itemId = this.parseInteger(feeNode.getAttributes(), "itemId", 0);
				long itemCount = this.parseLong(feeNode.getAttributes(), "itemCount", 0L);
				long adenaFee = this.parseLong(feeNode.getAttributes(), "adenaFee", 0L);
				long cancelFee = this.parseLong(feeNode.getAttributes(), "cancelFee", 0L);
				if (itemId != 0 && ItemData.getInstance().getTemplate(itemId) == null)
				{
					LOGGER.warning(this.getClass().getSimpleName() + ": Item with id " + itemId + " was not found.");
				}

				VariationFee fee = new VariationFee(itemId, itemCount, adenaFee, cancelFee);
				Map<Integer, VariationFee> feeByMinerals = new HashMap<>();
				this.forEach(feeNode, "mineral", mineralNode -> {
					int mId = this.parseInteger(mineralNode.getAttributes(), "id");
					feeByMinerals.put(mId, fee);
				});
				this.forEach(feeNode, "mineralRange", mineralNode -> {
					int fromId = this.parseInteger(mineralNode.getAttributes(), "from");
					int toId = this.parseInteger(mineralNode.getAttributes(), "to");

					for (int id = fromId; id <= toId; id++)
					{
						feeByMinerals.put(id, fee);
					}
				});

				for (int item : itemGroup)
				{
					Map<Integer, VariationFee> fees = this._fees.get(item);
					if (fees == null)
					{
						fees = new HashMap<>();
					}

					fees.putAll(feeByMinerals);
					this._fees.put(item, fees);
				}
			}));
		});
	}

	public int getVariationCount()
	{
		return this._variations.size();
	}

	public int getFeeCount()
	{
		return this._fees.size();
	}

	public VariationInstance generateRandomVariation(Variation variation, Item targetItem)
	{
		return VariationData.generateRandomVariation(variation, targetItem.getId());
	}

	private static VariationInstance generateRandomVariation(Variation variation, int targetItemId)
	{
		Options option1 = variation.getRandomEffect(0, targetItemId);
		Options option2 = variation.getRandomEffect(1, targetItemId);
		Options option3 = variation.getRandomEffect(2, targetItemId);
		return new VariationInstance(variation.getMineralId(), option1, option2, option3);
	}

	public Variation getVariation(int mineralId, Item item)
	{
		List<Variation> variations = this._variations.get(mineralId);
		if (variations != null && !variations.isEmpty())
		{
			for (Variation variation : variations)
			{
				Set<Integer> group = this._itemGroups.get(variation.getItemGroup());
				if (group != null && group.contains(item.getId()))
				{
					return variation;
				}
			}

			return variations.get(0);
		}
		return null;
	}

	public boolean hasVariation(int mineralId)
	{
		List<Variation> variations = this._variations.get(mineralId);
		return variations != null && !variations.isEmpty();
	}

	public VariationFee getFee(int itemId, int mineralId)
	{
		return this._fees.getOrDefault(itemId, Collections.emptyMap()).get(mineralId);
	}

	public long getCancelFee(int itemId, int mineralId)
	{
		Map<Integer, VariationFee> fees = this._fees.get(itemId);
		if (fees == null)
		{
			return -1L;
		}
		VariationFee fee = fees.get(mineralId);
		if (fee == null)
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": Cancellation fee not found for item [" + itemId + "] and mineral [" + mineralId + "]");
			fee = fees.values().iterator().next();
			if (fee == null)
			{
				return -1L;
			}
		}

		return fee.getCancelFee();
	}

	public boolean hasFeeData(int itemId)
	{
		Map<Integer, VariationFee> itemFees = this._fees.get(itemId);
		return itemFees != null && !itemFees.isEmpty();
	}

	public static VariationData getInstance()
	{
		return VariationData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final VariationData INSTANCE = new VariationData();
	}
}
