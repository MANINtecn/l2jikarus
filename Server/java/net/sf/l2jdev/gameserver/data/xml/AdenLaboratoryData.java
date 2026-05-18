package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.data.enums.AdenLabGameType;
import net.sf.l2jdev.gameserver.data.holders.AdenLabHolder;
import net.sf.l2jdev.gameserver.data.holders.AdenLabSkillHolder;
import net.sf.l2jdev.gameserver.data.holders.AdenLabStageHolder;
import net.sf.l2jdev.gameserver.managers.AdenLaboratoryManager;
import net.sf.l2jdev.gameserver.model.StatSet;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class AdenLaboratoryData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(AdenLaboratoryData.class.getName());
	private final Map<Byte, Map<Integer, AdenLabHolder>> _adenLabData = new HashMap<>();
	private final Map<Byte, Map<Byte, Map<Byte, Map<Byte, List<AdenLabSkillHolder>>>>> _skillsLookupTable = new HashMap<>();
	private final Map<Byte, Map<Byte, List<int[]>>> _normalStageSkillsUntilSpecificPageSnapshot = new HashMap<>();
	private final Map<Byte, Map<Byte, Integer>> _specialStagesAndCombatPower = new ConcurrentHashMap<>();
	private final Map<Byte, Map<Byte, Map<Byte, Integer>>> _transcendentStagesAndCombatPower = new ConcurrentHashMap<>();

	protected AdenLaboratoryData()
	{
	}

	public void reload()
	{
		LOGGER.info(this.getClass().getSimpleName() + ": reload initiated.");
		this.load();
		LOGGER.info(this.getClass().getSimpleName() + ": reload completed.");
	}

	@Override
	public void load()
	{
		this._adenLabData.clear();
		this._skillsLookupTable.clear();
		this._specialStagesAndCombatPower.clear();
		this._transcendentStagesAndCombatPower.clear();
		this._normalStageSkillsUntilSpecificPageSnapshot.clear();
		this.parseDatapackFile("data/AdenLaboratoryData.xml");
		this.initializeNormalSkillsCache();
		int bossCount = this._normalStageSkillsUntilSpecificPageSnapshot.size();
		byte specialStages = 0;

		for (byte i = 1; i <= bossCount; i++)
		{
			specialStages += (byte) this.getSpecialStageIndicesByBossId(i).size();
		}

		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + bossCount + " boss" + (bossCount > 1 ? "es" : "") + " and " + (AdenLaboratoryManager.getTotalCount(this._normalStageSkillsUntilSpecificPageSnapshot, true) - 1 + specialStages) + " stages.");
		AdenLaboratoryManager.ensureAdenLabTableExists();
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		for (Node n = document.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if (IXmlReader.isNode(n) && "list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node bossNode = n.getFirstChild(); bossNode != null; bossNode = bossNode.getNextSibling())
				{
					if (IXmlReader.isNode(bossNode))
					{
						if (!"boss".equalsIgnoreCase(bossNode.getNodeName()))
						{
							LOGGER.warning("Missing or incorrectly set `boss` element in data/AdenLabData.xml.");
						}
						else
						{
							Node bossAttributes = bossNode.getAttributes().getNamedItem("index");
							int bossId = this.parseInt(bossAttributes, -1);

							for (Node gameTypeNode = bossNode.getFirstChild(); gameTypeNode != null; gameTypeNode = gameTypeNode.getNextSibling())
							{
								if (IXmlReader.isNode(gameTypeNode))
								{
									if (!"game".equalsIgnoreCase(gameTypeNode.getNodeName()))
									{
										LOGGER.warning("Missing or incorrectly set `game` element in data/AdenLabData.xml.");
									}
									else
									{
										Node gameTypeAttributes = gameTypeNode.getAttributes().getNamedItem("type");
										AdenLabGameType gameType = this.parseEnum(gameTypeAttributes, AdenLabGameType.class, AdenLabGameType.NORMAL);

										for (Node pageNode = gameTypeNode.getFirstChild(); pageNode != null; pageNode = pageNode.getNextSibling())
										{
											if (IXmlReader.isNode(pageNode))
											{
												if (!"page".equalsIgnoreCase(pageNode.getNodeName()))
												{
													LOGGER.warning("Missing or incorrectly set `page` element in data/AdenLabData.xml.");
												}
												else
												{
													AdenLabHolder adenLabHolder = new AdenLabHolder();
													NamedNodeMap pageAttributes = pageNode.getAttributes();
													adenLabHolder.setBossId((byte) bossId);
													adenLabHolder.setGameType(gameType);
													if (!this.handlePageIndex(pageAttributes, adenLabHolder) || !this.handleCardCount(pageAttributes, adenLabHolder) || !this.handleSuccessRate(pageAttributes, adenLabHolder))
													{
														return;
													}

													int firstSkillId = this.parseInteger(pageAttributes, "primarySkillId", -1);
													if (firstSkillId == -1)
													{
														LOGGER.warning("Missing or incorrectly set `primarySkillId` attribute for page index " + adenLabHolder.getPageIndex());
													}

													int secondSkillId = this.parseInteger(pageAttributes, "secondarySkillId", -1);
													byte optionIndex = 0;

													for (Node probabilitiesNode = pageNode.getFirstChild(); probabilitiesNode != null; probabilitiesNode = probabilitiesNode.getNextSibling())
													{
														if (IXmlReader.isNode(probabilitiesNode) && "options".equalsIgnoreCase(probabilitiesNode.getNodeName()))
														{
															optionIndex++;

															for (Node stageNode = probabilitiesNode.getFirstChild(); stageNode != null; stageNode = stageNode.getNextSibling())
															{
																if (IXmlReader.isNode(stageNode) && "stage".equalsIgnoreCase(stageNode.getNodeName()))
																{
																	StatSet stageAttributes = new StatSet(this.parseAttributes(stageNode));
																	AdenLabStageHolder stageHolder = new AdenLabStageHolder();
																	AdenLabSkillHolder skillHolder = new AdenLabSkillHolder();
																	int stageLevel = stageAttributes.getInt("level", -1);
																	if (stageLevel != -1)
																	{
																		stageHolder.setStageLevel(stageLevel);
																	}
																	else
																	{
																		LOGGER.warning("Missing or incorrectly set `level` attribute in the `stage` element.");
																	}

																	float stageChance = stageAttributes.getFloat("chance", -1.0F);
																	if (stageChance != -1.0F)
																	{
																		stageHolder.setStageChance(stageChance);
																	}
																	else
																	{
																		LOGGER.warning("Missing or incorrectly set `chance` attribute in the `stage` element.");
																	}

																	int firstSkillLevel = stageAttributes.getInt("primarySkillLevel", -1);
																	if (firstSkillLevel != -1)
																	{
																		skillHolder.setId(firstSkillId);
																		skillHolder.setLvl(firstSkillLevel);
																		stageHolder.addSkill(skillHolder);
																		AdenLaboratoryManager.addSkillToCache(bossId, adenLabHolder.getPageIndex(), optionIndex, stageLevel, new int[]
																		{
																			firstSkillId,
																			firstSkillLevel
																		});
																	}
																	else
																	{
																		LOGGER.warning("Missing or incorrectly set `primarySkillLevel` attribute in the `stage` element.");
																	}

																	if (gameType != AdenLabGameType.NORMAL)
																	{
																		int combatPower = stageAttributes.getInt("combatPower", 0);
																		if (combatPower > 0)
																		{
																			stageHolder.setCombatPower(combatPower);
																			byte pageIndex = (byte) adenLabHolder.getPageIndex();
																			if (gameType == AdenLabGameType.SPECIAL)
																			{
																				this._specialStagesAndCombatPower.computeIfAbsent((byte) bossId, _ -> new HashMap<>()).put(pageIndex, combatPower);
																			}
																			else if (gameType == AdenLabGameType.INCREDIBLE)
																			{
																				this._transcendentStagesAndCombatPower.computeIfAbsent((byte) bossId, _ -> new HashMap<>()).computeIfAbsent(pageIndex, _ -> new HashMap<>()).put((byte) stageLevel, combatPower);
																			}
																		}

																		int secondSkillLevel = stageAttributes.getInt("secondarySkillLevel", -1);
																		if (secondSkillLevel != -1)
																		{
																			AdenLabSkillHolder skillHolder2 = new AdenLabSkillHolder();
																			skillHolder2.setId(secondSkillId);
																			skillHolder2.setLvl(secondSkillLevel);
																			stageHolder.addSkill(skillHolder2);
																			AdenLaboratoryManager.addSkillToCache(bossId, adenLabHolder.getPageIndex(), optionIndex, stageLevel, new int[]
																			{
																				secondSkillId,
																				secondSkillLevel
																			});
																		}
																	}

																	adenLabHolder.addStage(optionIndex, (byte) stageLevel, stageHolder);
																}
															}
														}
													}

													this._adenLabData.computeIfAbsent((byte) bossId, _ -> new HashMap<>()).put(adenLabHolder.getPageIndex(), adenLabHolder);
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
	}

	public Map<Byte, Map<Integer, AdenLabHolder>> getAllAdenLabData()
	{
		return this._adenLabData;
	}

	public Map<Integer, AdenLabHolder> getAdenLabData(byte bossId)
	{
		return this._adenLabData.get(bossId);
	}

	public AdenLabHolder getAdenLabDataByPageIndex(int bossId, int pageIndex)
	{
		Map<Integer, AdenLabHolder> bossData = this.getAdenLabData((byte) bossId);
		if (bossData == null)
		{
			LOGGER.warning("AdenLabData: No data found for bossId " + bossId);
			return null;
		}
		AdenLabHolder adenLabHolder = bossData.get(pageIndex);
		if (adenLabHolder == null)
		{
			LOGGER.warning("AdenLabData: No data found for pageIndex " + pageIndex + " under bossId " + bossId);
		}

		return adenLabHolder;
	}

	public Map<Byte, Map<Byte, Map<Byte, Map<Byte, List<AdenLabSkillHolder>>>>> getSkillsLookupTable()
	{
		return this._skillsLookupTable;
	}

	public Map<Byte, Map<Byte, List<AdenLabSkillHolder>>> getSkillsLookupTableByBossAndPageIndex(byte bossId, byte pageIndex)
	{
		return this._skillsLookupTable.containsKey(bossId) ? this._skillsLookupTable.get(bossId).getOrDefault(pageIndex, Collections.emptyMap()) : Collections.emptyMap();
	}

	public Map<Byte, List<AdenLabSkillHolder>> getSkillsByOptionIndex(byte bossId, byte pageIndex, byte optionIndex)
	{
		return this._skillsLookupTable.containsKey(bossId) && this._skillsLookupTable.get(bossId).containsKey(pageIndex) ? this._skillsLookupTable.get(bossId).get(pageIndex).get(optionIndex) : null;
	}

	protected boolean handlePageIndex(NamedNodeMap attributes, AdenLabHolder adenLabHolder)
	{
		byte pageIndex = this.parseByte(attributes, "index", (byte) -1);
		if (pageIndex == -1)
		{
			LOGGER.warning("Missing or incorrectly set `pageIndex` attribute in the `page` element.");
			return false;
		}
		if (adenLabHolder.getGameType() == AdenLabGameType.SPECIAL)
		{
			this._specialStagesAndCombatPower.computeIfAbsent(adenLabHolder.getBossId(), _ -> new ConcurrentHashMap<>()).putIfAbsent(pageIndex, 0);
		}
		else if (adenLabHolder.getGameType() == AdenLabGameType.INCREDIBLE)
		{
			this._transcendentStagesAndCombatPower.computeIfAbsent(adenLabHolder.getBossId(), _ -> new ConcurrentHashMap<>()).computeIfAbsent(pageIndex, _ -> new ConcurrentHashMap<>());
		}

		adenLabHolder.setPageIndex(pageIndex);
		return true;
	}

	protected boolean handleCardCount(NamedNodeMap attributes, AdenLabHolder adenLabHolder)
	{
		byte cardCount = this.parseByte(attributes, "cardCount", (byte) -1);
		adenLabHolder.setCardCount(cardCount);
		if (adenLabHolder.getGameType() == AdenLabGameType.NORMAL && cardCount == -1)
		{
			LOGGER.warning("Missing `cardCount` value for page index " + adenLabHolder.getPageIndex() + ". You better fix it ASAP, because it will break things. ;)");
			return false;
		}
		return true;
	}

	protected boolean handleSuccessRate(NamedNodeMap attributes, AdenLabHolder adenLabHolder)
	{
		float successRateAttribute = this.parseFloat(attributes, "successRate", -1.0F);
		if (successRateAttribute == -1.0F)
		{
			LOGGER.warning("Missing or incorrectly set `successRate` for page index " + adenLabHolder.getPageIndex() + ". Assigning default value: -1f");
			return false;
		}
		adenLabHolder.setGameSuccessRate(successRateAttribute);
		return true;
	}

	public void initializeNormalSkillsCache()
	{
		for (Entry<Byte, Map<Byte, Map<Byte, Map<Byte, List<AdenLabSkillHolder>>>>> bossEntry : this._skillsLookupTable.entrySet())
		{
			byte bossId = bossEntry.getKey();
			Map<Byte, Map<Byte, Map<Byte, List<AdenLabSkillHolder>>>> bossSkills = bossEntry.getValue();
			Map<Byte, List<int[]>> pageCache = new HashMap<>();
			List<int[]> accumulatedSkills = new ArrayList<>();

			for (byte pageIndex : bossSkills.keySet().stream().sorted(AdenLaboratoryManager::sortingComparator).toList())
			{
				if (!this.getSpecialStageIndicesByBossId(bossId).contains(pageIndex) && !this.getTranscendentStageIndicesByBossId(bossId).contains(pageIndex))
				{
					Map<Byte, Map<Byte, List<AdenLabSkillHolder>>> pageSkills = bossSkills.get(pageIndex);

					for (Entry<Byte, Map<Byte, List<AdenLabSkillHolder>>> optionEntry : pageSkills.entrySet())
					{
						Map<Byte, List<AdenLabSkillHolder>> stageMap = optionEntry.getValue();

						for (List<AdenLabSkillHolder> holderList : stageMap.values())
						{
							for (AdenLabSkillHolder skill : holderList)
							{
								accumulatedSkills.add(new int[]
								{
									skill.getId(),
									skill.getLvl()
								});
							}
						}
					}

					pageCache.put(pageIndex, new ArrayList<>(accumulatedSkills));
				}
			}

			this._normalStageSkillsUntilSpecificPageSnapshot.put(bossId, pageCache);
		}
	}

	public List<int[]> getNormalStageSkillsUpToPage(byte bossId, byte pageIndex)
	{
		Map<Byte, List<int[]>> pageCache = this._normalStageSkillsUntilSpecificPageSnapshot.getOrDefault(bossId, Collections.emptyMap());
		if (pageCache.isEmpty())
		{
			return Collections.emptyList();
		}
		byte highestAvailablePage = pageCache.keySet().stream().filter(index -> index <= pageIndex).max(Byte::compare).orElse((byte) -1);
		return highestAvailablePage == -1 ? Collections.emptyList() : pageCache.get(highestAvailablePage);
	}

	public List<Byte> getSpecialStageIndicesByBossId(byte bossId)
	{
		return new ArrayList<>(this._specialStagesAndCombatPower.getOrDefault(bossId, Collections.emptyMap()).keySet());
	}

	public int getSpecialStageCombatPower(byte bossId, byte pageIndex)
	{
		return this._specialStagesAndCombatPower.get(bossId).get(pageIndex);
	}

	public List<Byte> getTranscendentStageIndicesByBossId(byte bossId)
	{
		return new ArrayList<>(this._transcendentStagesAndCombatPower.getOrDefault(bossId, Collections.emptyMap()).keySet());
	}

	public int getTranscendentCombatPower(byte bossId, byte pageIndex, byte currentTranscendLevel)
	{
		return this._transcendentStagesAndCombatPower.getOrDefault(bossId, Collections.emptyMap()).getOrDefault(pageIndex, Collections.emptyMap()).getOrDefault(currentTranscendLevel, 0);
	}

	public static AdenLaboratoryData getInstance()
	{
		return AdenLaboratoryData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final AdenLaboratoryData INSTANCE = new AdenLaboratoryData();
	}
}
