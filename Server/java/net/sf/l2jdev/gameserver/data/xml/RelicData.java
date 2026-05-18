package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.config.RelicSystemConfig;
import net.sf.l2jdev.gameserver.data.holders.RelicCompoundFeeHolder;
import net.sf.l2jdev.gameserver.data.holders.RelicDataHolder;
import net.sf.l2jdev.gameserver.data.holders.RelicEnchantHolder;
import net.sf.l2jdev.gameserver.data.holders.RelicSummonCategoryHolder;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.enums.player.RelicGrade;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class RelicData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(RelicData.class.getName());
	public static final int MAX_ACTIVE_CATEGORIES = 2;
	private static final List<Integer> ENCHANT_CHANCES = new ArrayList<>();
	private static ItemHolder ENCHANT_FEE_HOLDER;
	private static final Map<Integer, RelicSummonCategoryHolder> SUMMON_CATEGORIES = new HashMap<>(4);
	private static final Set<RelicSummonCategoryHolder> ACTIVE_SUMMON_CATEGORIES = new HashSet<>(2);
	private static final Map<Integer, RelicDataHolder> RELICS = new HashMap<>();
	private static final Map<RelicGrade, Set<RelicDataHolder>> GRADE_RELICS = new HashMap<>();
	private static final Map<RelicGrade, RelicCompoundFeeHolder> GRADE_COMPOUND_FEES = new HashMap<>();

	protected RelicData()
	{
		if (RelicSystemConfig.RELIC_SYSTEM_ENABLED)
		{
			this.load();
		}
	}

	@Override
	public void load()
	{
		RELICS.clear();
		if (RelicSystemConfig.RELIC_SYSTEM_ENABLED)
		{
			this.parseDatapackFile("data/RelicData.xml");
			generateGradeRelics();
		}

		if (!RELICS.isEmpty())
		{
			LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + RELICS.size() + " relics.");
		}
		else
		{
			LOGGER.info(this.getClass().getSimpleName() + ": System is disabled.");
		}
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
					if ("relicSummonCategoryData".equalsIgnoreCase(d.getNodeName()))
					{
						for (Node b = d.getFirstChild(); b != null; b = b.getNextSibling())
						{
							if ("summonCategory".equalsIgnoreCase(b.getNodeName()))
							{
								NamedNodeMap attrs = b.getAttributes();
								int categoryId = this.parseInteger(attrs, "id");
								int priceId = this.parseInteger(attrs, "priceId");
								long amount = this.parseLong(attrs, "amount");
								boolean active = this.parseBoolean(attrs, "active");
								int summonCount = this.parseInteger(attrs, "summonCount");
								RelicSummonCategoryHolder holder = new RelicSummonCategoryHolder(categoryId, priceId, amount, summonCount);
								SUMMON_CATEGORIES.put(categoryId, holder);
								if (active && ACTIVE_SUMMON_CATEGORIES.size() < 2)
								{
									ACTIVE_SUMMON_CATEGORIES.add(holder);
								}
							}
						}
					}
					else if ("relicEnchantData".equalsIgnoreCase(d.getNodeName()))
					{
						for (Node bx = d.getFirstChild(); bx != null; bx = bx.getNextSibling())
						{
							if ("enchantFee".equalsIgnoreCase(bx.getNodeName()))
							{
								NamedNodeMap attrs = bx.getAttributes();
								int feeId = this.parseInteger(attrs, "feeItemId", 57);
								long feeCount = this.parseLong(attrs, "feeCount", 100L);
								ENCHANT_FEE_HOLDER = new ItemHolder(feeId, feeCount);
							}

							if ("enchantData".equalsIgnoreCase(bx.getNodeName()))
							{
								NamedNodeMap attrs = bx.getAttributes();
								String[] chances = this.parseString(attrs, "chancePerIngredients").split(",");

								for (String chance : chances)
								{
									ENCHANT_CHANCES.add(Integer.parseInt(chance.trim()));
								}
							}
						}
					}
					else if ("relicCombineData".equalsIgnoreCase(d.getNodeName()))
					{
						for (Node bx = d.getFirstChild(); bx != null; bx = bx.getNextSibling())
						{
							if ("combineCategory".equalsIgnoreCase(bx.getNodeName()))
							{
								NamedNodeMap attrs = bx.getAttributes();
								RelicGrade grade = RelicGrade.valueOf(this.parseString(attrs, "grade"));
								int feeId = this.parseInteger(attrs, "feeItemId", 57);
								long feeCount = this.parseLong(attrs, "feeCount", 100L);
								RelicCompoundFeeHolder holder = new RelicCompoundFeeHolder(grade, feeId, feeCount);
								GRADE_COMPOUND_FEES.put(grade, holder);
							}
						}
					}
					else if ("relic".equalsIgnoreCase(d.getNodeName()))
					{
						NamedNodeMap attrs = d.getAttributes();
						StatSet set = new StatSet();

						for (int i = 0; i < attrs.getLength(); i++)
						{
							Node att = attrs.item(i);
							set.set(att.getNodeName(), att.getNodeValue());
						}

						int relicId = this.parseInteger(attrs, "id");
						int parentRelicId = this.parseInteger(attrs, "baseRelicId");
						RelicGrade grade = RelicGrade.valueOf(this.parseString(attrs, "grade"));
						long summonChance = this.parseLong(attrs, "summonChance");
						float compoundChanceModifier = this.parseFloat(attrs, "compoundChanceModifier", 3.3F);
						float compoundUpGradeChanceModifier = this.parseFloat(attrs, "compoundUpGradeChanceModifier", 3.3F);
						int enchantLevel = 0;
						int skillId = 0;
						int skillLevel = 0;
						int combatPower = 0;
						List<RelicEnchantHolder> enchantHolder = new ArrayList<>();

						for (Node bxx = d.getFirstChild(); bxx != null; bxx = bxx.getNextSibling())
						{
							attrs = bxx.getAttributes();
							if ("relicStat".equalsIgnoreCase(bxx.getNodeName()))
							{
								enchantLevel = this.parseInteger(attrs, "enchantLevel");
								skillId = this.parseInteger(attrs, "skillId");
								skillLevel = this.parseInteger(attrs, "skillLevel");
								combatPower = this.parseInteger(attrs, "combatPower", 0);
								enchantHolder.add(new RelicEnchantHolder(enchantLevel, skillId, skillLevel, combatPower));
							}
						}

						RelicDataHolder template = new RelicDataHolder(relicId, parentRelicId, grade, summonChance, enchantHolder, compoundChanceModifier, compoundUpGradeChanceModifier);
						RELICS.put(relicId, template);
					}
				}
			}
		}
	}

	private static void generateGradeRelics()
	{
		for (RelicDataHolder holder : RELICS.values())
		{
			Set<RelicDataHolder> existingSet = GRADE_RELICS.get(holder.getGrade());
			if (existingSet == null)
			{
				existingSet = new HashSet<>();
				GRADE_RELICS.put(holder.getGrade(), existingSet);
			}

			existingSet.add(holder);
		}
	}

	public Collection<RelicDataHolder> getRelicsByGrade(RelicGrade grade)
	{
		return GRADE_RELICS.get(grade);
	}

	public RelicDataHolder getRelic(int id)
	{
		return RELICS.get(id);
	}

	public int getRelicSkillId(int id, int enchant)
	{
		return RELICS.get(id).getEnchantHolderByEnchant(enchant).getSkillId();
	}

	public int getRelicSkillLevel(int id, int enchant)
	{
		return RELICS.get(id).getEnchantHolderByEnchant(enchant).getSkillLevel();
	}

	public RelicCompoundFeeHolder getCompoundFeeHolderByGrade(RelicGrade grade)
	{
		return GRADE_COMPOUND_FEES.get(grade);
	}

	public Collection<RelicDataHolder> getRelics()
	{
		return RELICS.values();
	}

	public Map<Integer, RelicSummonCategoryHolder> getRelicSummonCategories()
	{
		return SUMMON_CATEGORIES;
	}

	public Collection<RelicSummonCategoryHolder> getRelicActiveCategories()
	{
		return ACTIVE_SUMMON_CATEGORIES;
	}

	public RelicSummonCategoryHolder getRelicSummonCategory(int categoryId)
	{
		return SUMMON_CATEGORIES.get(categoryId);
	}

	public ItemHolder getEnchantFee()
	{
		return ENCHANT_FEE_HOLDER;
	}

	public int getEnchantRateByIngredientCount(int count)
	{
		return ENCHANT_CHANCES.get(count - 1);
	}

	public int getRelicBySummon()
	{
		List<RelicDataHolder> relics = getInstance().getRelics().stream().filter(relicx -> relicx.getSummonChance() > 0L).toList();
		long totalWeight = relics.stream().mapToLong(RelicDataHolder::getSummonChance).sum();
		if (totalWeight <= 0L)
		{
			LOGGER.warning("No valid relics available for summoning.");
			return 0;
		}
		long rng = Rnd.get(totalWeight);
		long cumulativeWeight = 0L;

		for (RelicDataHolder relic : relics)
		{
			cumulativeWeight += relic.getSummonChance();
			if (rng < cumulativeWeight)
			{
				return relic.getRelicId();
			}
		}

		return 0;
	}

	public Entry<Boolean, Integer> getRelicByCompound(RelicGrade grade)
	{
		RelicGrade successGrade = RelicGrade.values()[grade.ordinal() + 1];
		long successWeight = this.getRelicsByGrade(successGrade).stream().mapToLong(relicx -> {
			float div = Math.max(1.0F, relicx.getCompoundUpGradeChanceModifier());
			long summonChance = relicx.getSummonChance() / 100000000L;
			long scaledSummonChance = Math.max(1L, summonChance);
			return Math.max(1L, (long) (scaledSummonChance / div));
		}).sum();
		if (successWeight <= 0L)
		{
			return new SimpleEntry<>(false, 0);
		}
		int normalizedSuccessWeight = (int) Math.min(100L, successWeight);
		boolean success = Rnd.get(100) < normalizedSuccessWeight;
		RelicGrade resultGrade = success ? successGrade : grade;
		Collection<RelicDataHolder> relics = this.getRelicsByGrade(resultGrade);
		if (relics.isEmpty())
		{
			return new SimpleEntry<>(success, 0);
		}
		long totalWeight = Math.max(1L, relics.stream().mapToLong(relicx -> this.calculateCompoundChance(relicx.getRelicId(), grade)).sum());
		long relicRng = Rnd.get(totalWeight);
		long cumulativeWeight = 0L;
		List<RelicDataHolder> relicList = new ArrayList<>(relics);

		for (RelicDataHolder relic : relicList)
		{
			cumulativeWeight += this.calculateCompoundChance(relic.getRelicId(), grade);
			if (relicRng < cumulativeWeight)
			{
				return new SimpleEntry<>(success, relic.getRelicId());
			}
		}

		RelicDataHolder randomRelic = relicList.get(Rnd.get(relicList.size() - 1));
		return new SimpleEntry<>(success, randomRelic.getRelicId());
	}

	public long calculateCompoundChance(int relicId, RelicGrade grade)
	{
		RelicDataHolder relic = getInstance().getRelic(relicId);
		float div = relic.getGrade() == grade ? relic.getCompoundChanceModifier() : relic.getCompoundUpGradeChanceModifier();
		long combineChance = relic.getSummonChance() == 0L ? (relic.getGrade() == grade ? 1000000000L : 100000000L) : relic.getSummonChance();
		return new BigDecimal(combineChance / div / 1.0E8F).setScale(4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100000000L)).longValue();
	}

	public List<Integer> generateSummonRelics(int summonCount)
	{
		List<Integer> relics = new ArrayList<>();

		for (int i = 1; i <= summonCount; i++)
		{
			int obtainedRelicId = this.getRelicBySummon();
			relics.add(obtainedRelicId);
		}

		return relics;
	}

	public List<RelicDataHolder> getRelicsByParentId(int parentRelicId)
	{
		List<RelicDataHolder> relics = new ArrayList<>();

		for (RelicDataHolder relic : RELICS.values())
		{
			if (relic.getParentRelicId() == parentRelicId)
			{
				relics.add(relic);
			}
		}

		return relics;
	}

	public static RelicData getInstance()
	{
		return RelicData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final RelicData INSTANCE = new RelicData();
	}
}
