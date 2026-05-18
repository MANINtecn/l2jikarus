package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.data.holders.RelicCouponHolder;
import net.sf.l2jdev.gameserver.data.holders.RelicDataHolder;
import net.sf.l2jdev.gameserver.model.actor.enums.player.RelicGrade;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class RelicCouponData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(RelicCouponData.class.getName());
	private static final Map<Integer, RelicCouponHolder> RELIC_COUPONS = new HashMap<>();
	private static final Map<Integer, LinkedHashMap<Integer, Long>> CACHED_CHANCES = new LinkedHashMap<>();

	protected RelicCouponData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		RELIC_COUPONS.clear();
		this.parseDatapackFile("data/RelicCouponData.xml");
		cacheChances();
		if (!RELIC_COUPONS.isEmpty())
		{
			LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + RELIC_COUPONS.size() + " relic coupon data.");
		}
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		this.forEach(document, "list", listNode -> this.forEach(listNode, "coupon", couponNode -> {
			Element couponElement = (Element) couponNode;
			int itemId = Integer.parseInt(couponElement.getAttribute("itemId"));
			if (ItemData.getInstance().getTemplate(itemId) == null)
			{
				LOGGER.info(this.getClass().getSimpleName() + ": Could not find coupon with item id " + itemId + ".");
			}
			else
			{
				int summonCount = couponElement.hasAttribute("summonCount") ? Integer.parseInt(couponElement.getAttribute("summonCount")) : 1;
				if (couponElement.hasAttribute("relicId"))
				{
					int relicId = Integer.parseInt(couponElement.getAttribute("relicId"));
					RELIC_COUPONS.put(itemId, new RelicCouponHolder(itemId, relicId, summonCount));
				}
				else
				{
					Map<RelicGrade, Integer> groups = new HashMap<>();
					Set<Integer> disabledDollsIds = new HashSet<>();
					Map<Integer, Integer> chanceRolls = new HashMap<>();
					this.forEach(couponNode, "disabledDolls", disabledDollsNode -> this.forEach(disabledDollsNode, "disabled", disabledNode -> {
						int id = Integer.parseInt(((Element) disabledNode).getAttribute("id"));
						disabledDollsIds.add(id);
					}));
					this.forEach(couponNode, "chanceGroups", groupChanceNode -> this.forEach(groupChanceNode, "group", groupNode -> {
						Element groupElement = (Element) groupNode;
						RelicGrade grade = RelicGrade.valueOf(groupElement.getAttribute("grade"));
						int chance = Integer.parseInt(groupElement.getAttribute("chance"));
						groups.put(grade, chance);
					}));
					if (!groups.isEmpty())
					{
						RELIC_COUPONS.put(itemId, new RelicCouponHolder(itemId, summonCount, groups, disabledDollsIds));
					}
					else
					{
						this.forEach(couponNode, "chanceRollGroups", chanceRollGroupsNode -> this.forEach(chanceRollGroupsNode, "chanceRoll", chanceRollNode -> {
							Element chanceRollElement = (Element) chanceRollNode;
							int dollId = Integer.parseInt(chanceRollElement.getAttribute("dollId"));
							int chance = Integer.parseInt(chanceRollElement.getAttribute("chance"));
							chanceRolls.put(dollId, chance);
						}));
					}

					if (!chanceRolls.isEmpty())
					{
						RELIC_COUPONS.put(itemId, new RelicCouponHolder(itemId, summonCount, chanceRolls));
					}
				}
			}
		}));
	}

	public RelicCouponHolder getCouponFromCouponItemId(int itemId)
	{
		return RELIC_COUPONS.get(itemId);
	}

	public int getRelicIdByCouponItemId(int itemId)
	{
		return RELIC_COUPONS.get(itemId) != null ? RELIC_COUPONS.get(itemId).getRelicId() : 0;
	}

	private static int getRelicIdFromSummon(RelicCouponHolder coupon)
	{
		if (coupon.getRelicId() != 0)
		{
			return coupon.getRelicId();
		}
		Map<RelicGrade, Integer> grades = new HashMap<>();
		grades.putAll(coupon.getCouponRelicGrades());
		int curAttempt = 0;
		if (!grades.isEmpty())
		{
			RelicGrade rolledGrade = null;

			while (rolledGrade == null)
			{
				curAttempt++;

				for (Entry<RelicGrade, Integer> entry : grades.entrySet())
				{
					if (Math.max(1, entry.getValue()) < Rnd.get(100) || curAttempt > 1000)
					{
						rolledGrade = entry.getKey();
						break;
					}
				}
			}

			List<RelicDataHolder> relicsByGrade = new ArrayList<>();
			relicsByGrade.addAll(RelicData.getInstance().getRelicsByGrade(rolledGrade).stream().filter(r -> !coupon.getDisabledIds().contains(r.getRelicId())).collect(Collectors.toList()));
			long totalWeight = relicsByGrade.stream().mapToLong(RelicDataHolder::getSummonChance).sum();
			if (totalWeight <= 0L)
			{
				LOGGER.warning("No valid relics available for summoning with coupon " + coupon.getItemId());
				return 0;
			}

			long rng = Rnd.get(totalWeight);
			long cumulativeWeight = 0L;

			for (RelicDataHolder relic : relicsByGrade)
			{
				cumulativeWeight += relic.getSummonChance();
				if (rng < cumulativeWeight)
				{
					return relic.getRelicId();
				}
			}
		}

		if (coupon.getChanceRolls().isEmpty())
		{
			return 0;
		}
		curAttempt = 0;
		Map<Integer, Integer> chanceRolls = new HashMap<>();
		chanceRolls.putAll(chanceRolls);

		while (true)
		{
			curAttempt++;

			for (Entry<Integer, Integer> entryx : chanceRolls.entrySet())
			{
				if (entryx.getValue() < Rnd.get(100) || curAttempt > 1000)
				{
					return entryx.getKey();
				}
			}
		}
	}

	private static void cacheChances()
	{
		CACHED_CHANCES.clear();

		for (RelicCouponHolder holder : RELIC_COUPONS.values())
		{
			Map<Integer, Long> results = new HashMap<>();
			if (holder.getRelicId() != 0)
			{
				results.put(holder.getRelicId(), 10000000000L);
			}

			Map<RelicGrade, Integer> grades = new HashMap<>();
			grades.putAll(holder.getCouponRelicGrades());
			if (!grades.isEmpty())
			{
				Map<Integer, Integer> tempResults = new HashMap<>();

				for (Entry<RelicGrade, Integer> entry : grades.entrySet())
				{
					for (RelicDataHolder rh : RelicData.getInstance().getRelicsByGrade(entry.getKey()).stream().filter(r -> !holder.getDisabledIds().contains(r.getRelicId())).collect(Collectors.toList()))
					{
						tempResults.put(rh.getRelicId(), entry.getValue());
					}
				}

				for (Entry<Integer, Integer> entry : tempResults.entrySet())
				{
					results.put(entry.getKey(), entry.getValue().intValue() * 100000000L / tempResults.size());
				}
			}

			if (!holder.getChanceRolls().isEmpty())
			{
				Map<Integer, Integer> chanceRolls = new HashMap<>();
				chanceRolls.putAll(chanceRolls);

				for (Entry<Integer, Integer> entry : chanceRolls.entrySet())
				{
					results.put(entry.getKey(), entry.getValue().intValue() * 10000000L);
				}
			}

			LinkedHashMap<Integer, Long> sortedRelics = results.entrySet().stream().sorted(Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, _) -> e1, LinkedHashMap::new));
			CACHED_CHANCES.put(holder.getItemId(), sortedRelics);
		}
	}

	public Map<Integer, Long> getCachedChances(int itemId)
	{
		return CACHED_CHANCES.get(itemId);
	}

	public List<Integer> generateSummonRelics(RelicCouponHolder coupon)
	{
		List<Integer> relics = new ArrayList<>();

		for (int i = 1; i <= coupon.getRelicSummonCount(); i++)
		{
			int obtainedRelicId = getRelicIdFromSummon(coupon);
			relics.add(obtainedRelicId);
		}

		return relics;
	}

	public static RelicCouponData getInstance()
	{
		return RelicCouponData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final RelicCouponData INSTANCE = new RelicCouponData();
	}
}
