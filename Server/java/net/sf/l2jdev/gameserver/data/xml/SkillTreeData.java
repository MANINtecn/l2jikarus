package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.model.SkillLearn;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Race;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PlayerClass;
import net.sf.l2jdev.gameserver.model.actor.enums.player.SocialClass;
import net.sf.l2jdev.gameserver.model.actor.enums.player.SubclassType;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.item.holders.ItemSkillHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.CommonSkill;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.enums.AcquireSkillType;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class SkillTreeData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(SkillTreeData.class.getName());
	private static final Map<PlayerClass, Map<Long, SkillLearn>> _classSkillTrees = new ConcurrentHashMap<>();
	private static final Map<PlayerClass, Map<Long, SkillLearn>> _completeClassSkillTree = new HashMap<>();
	private static final Map<PlayerClass, NavigableMap<Integer, Integer>> _maxClassSkillLevels = new HashMap<>();
	private static final Map<PlayerClass, Map<Long, SkillLearn>> _transferSkillTrees = new ConcurrentHashMap<>();
	private static final Map<Race, Map<Long, SkillLearn>> _raceSkillTree = new ConcurrentHashMap<>();
	private static final Map<SubclassType, Map<Long, SkillLearn>> _revelationSkillTree = new ConcurrentHashMap<>();
	private static final Map<PlayerClass, Set<Integer>> _awakeningSaveSkillTree = new ConcurrentHashMap<>();
	private static final Map<Long, SkillLearn> _collectSkillTree = new ConcurrentHashMap<>();
	private static final Map<Long, SkillLearn> _fishingSkillTree = new ConcurrentHashMap<>();
	private static final Map<Long, SkillLearn> _pledgeSkillTree = new ConcurrentHashMap<>();
	private static final Map<Long, SkillLearn> _subClassSkillTree = new ConcurrentHashMap<>();
	private static final Map<Long, SkillLearn> _subPledgeSkillTree = new ConcurrentHashMap<>();
	private static final Map<Long, SkillLearn> _transformSkillTree = new ConcurrentHashMap<>();
	private static final Map<Long, SkillLearn> _commonSkillTree = new ConcurrentHashMap<>();
	private static final Map<Long, SkillLearn> _abilitySkillTree = new ConcurrentHashMap<>();
	private static final Map<Long, SkillLearn> _alchemySkillTree = new ConcurrentHashMap<>();
	private static final Map<Long, SkillLearn> _dualClassSkillTree = new ConcurrentHashMap<>();
	private static final Map<Long, SkillLearn> _nobleSkillTree = new ConcurrentHashMap<>();
	private static final Map<Long, SkillLearn> _heroSkillTree = new ConcurrentHashMap<>();
	private static final Map<Long, SkillLearn> _gameMasterSkillTree = new ConcurrentHashMap<>();
	private static final Map<Long, SkillLearn> _gameMasterAuraSkillTree = new ConcurrentHashMap<>();
	private static final Map<PlayerClass, Set<Integer>> _removeSkillCache = new ConcurrentHashMap<>();
	private Map<Integer, long[]> _skillsByClassIdHashCodes;
	private Map<Integer, long[]> _skillsByRaceHashCodes;
	private long[] _allSkillsHashCodes;
	private static final Map<PlayerClass, PlayerClass> _parentClassMap = new ConcurrentHashMap<>();
	private boolean _loading = true;

	protected SkillTreeData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this._loading = true;
		_parentClassMap.clear();
		_classSkillTrees.clear();
		_collectSkillTree.clear();
		_fishingSkillTree.clear();
		_pledgeSkillTree.clear();
		_subClassSkillTree.clear();
		_subPledgeSkillTree.clear();
		_transferSkillTrees.clear();
		_transformSkillTree.clear();
		_nobleSkillTree.clear();
		_abilitySkillTree.clear();
		_alchemySkillTree.clear();
		_heroSkillTree.clear();
		_gameMasterSkillTree.clear();
		_gameMasterAuraSkillTree.clear();
		_raceSkillTree.clear();
		_revelationSkillTree.clear();
		_dualClassSkillTree.clear();
		_removeSkillCache.clear();
		_awakeningSaveSkillTree.clear();
		this.parseDatapackDirectory("data/stats/players/skillTrees/", true);
		_completeClassSkillTree.clear();

		for (Entry<PlayerClass, Map<Long, SkillLearn>> entry : _classSkillTrees.entrySet())
		{
			Map<Long, SkillLearn> skillTree = new HashMap<>();
			skillTree.putAll(_commonSkillTree);
			PlayerClass entryPlayerClass = entry.getKey();

			for (PlayerClass currentPlayerClass = entryPlayerClass; currentPlayerClass != null && _classSkillTrees.get(currentPlayerClass) != null; currentPlayerClass = _parentClassMap.get(currentPlayerClass))
			{
				skillTree.putAll(_classSkillTrees.get(currentPlayerClass));
			}

			_completeClassSkillTree.put(entryPlayerClass, skillTree);
		}

		_maxClassSkillLevels.clear();

		for (Entry<PlayerClass, Map<Long, SkillLearn>> entry : _completeClassSkillTree.entrySet())
		{
			PlayerClass playerClass = entry.getKey();
			if (!_maxClassSkillLevels.containsKey(playerClass))
			{
				_maxClassSkillLevels.put(playerClass, new TreeMap<>());
			}

			Map<Integer, Integer> playerClassSkillLevels = _maxClassSkillLevels.get(playerClass);

			for (SkillLearn skillLearn : entry.getValue().values())
			{
				Integer playerLevel = skillLearn.getGetLevel();
				if (!playerClassSkillLevels.containsKey(playerLevel))
				{
					playerClassSkillLevels.put(playerLevel, 0);
				}

				Integer currentMaxLevel = playerClassSkillLevels.get(playerLevel);
				Integer skillLevel = skillLearn.getSkillLevel();
				if (skillLevel > currentMaxLevel)
				{
					playerClassSkillLevels.put(playerLevel, skillLevel);
				}
			}
		}

		this.generateCheckArrays();
		this.report();
		this._loading = false;
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		String type = null;
		Race race = null;
		SubclassType subType = null;
		int cId = -1;
		int parentClassId = -1;
		PlayerClass playerClass = null;

		for (Node n = document.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("skillTree".equalsIgnoreCase(d.getNodeName()))
					{
						Map<Long, SkillLearn> classSkillTree = new HashMap<>();
						Map<Long, SkillLearn> transferSkillTree = new HashMap<>();
						Map<Long, SkillLearn> raceSkillTree = new HashMap<>();
						Map<Long, SkillLearn> revelationSkillTree = new HashMap<>();
						type = d.getAttributes().getNamedItem("type").getNodeValue();
						Node attr = d.getAttributes().getNamedItem("classId");
						if (attr != null)
						{
							cId = Integer.parseInt(attr.getNodeValue());
							playerClass = PlayerClass.getPlayerClass(cId);
						}
						else
						{
							cId = -1;
						}

						attr = d.getAttributes().getNamedItem("race");
						if (attr != null)
						{
							race = this.parseEnum(attr, Race.class);
						}

						attr = d.getAttributes().getNamedItem("subType");
						if (attr != null)
						{
							subType = this.parseEnum(attr, SubclassType.class);
						}

						attr = d.getAttributes().getNamedItem("parentClassId");
						if (attr != null)
						{
							parentClassId = Integer.parseInt(attr.getNodeValue());
							if (cId > -1 && cId != parentClassId && parentClassId > -1 && !_parentClassMap.containsKey(playerClass))
							{
								_parentClassMap.put(playerClass, PlayerClass.getPlayerClass(parentClassId));
							}
						}

						for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
						{
							if ("skill".equalsIgnoreCase(c.getNodeName()))
							{
								StatSet learnSkillSet = new StatSet();
								NamedNodeMap attrs = c.getAttributes();

								for (int i = 0; i < attrs.getLength(); i++)
								{
									attr = attrs.item(i);
									learnSkillSet.set(attr.getNodeName(), attr.getNodeValue());
								}

								SkillLearn skillLearn = new SkillLearn(learnSkillSet);
								SkillData.getInstance().getSkill(skillLearn.getSkillId(), skillLearn.getSkillLevel());

								for (Node b = c.getFirstChild(); b != null; b = b.getNextSibling())
								{
									attrs = b.getAttributes();
									String var21 = b.getNodeName();
									switch (var21)
									{
										case "item":
											List<ItemHolder> itemList = new ArrayList<>(1);
											int count = this.parseInteger(attrs, "count");

											for (String id : this.parseString(attrs, "id").split(","))
											{
												itemList.add(new ItemHolder(Integer.parseInt(id), count));
											}

											skillLearn.addRequiredItem(itemList);
											break;
										case "preRequisiteSkill":
											skillLearn.addPreReqSkill(new SkillHolder(this.parseInteger(attrs, "id"), this.parseInteger(attrs, "lvl")));
											break;
										case "race":
											skillLearn.addRace(Race.valueOf(b.getTextContent()));
											break;
										case "residenceId":
											skillLearn.addResidenceId(Integer.parseInt(b.getTextContent()));
											break;
										case "socialClass":
											skillLearn.setSocialClass(Enum.valueOf(SocialClass.class, b.getTextContent()));
											break;
										case "removeSkill":
											int removeSkillId = this.parseInteger(attrs, "id");
											skillLearn.addRemoveSkills(removeSkillId);
											if (!this.parseBoolean(attrs, "onlyReplaceByLearn", false))
											{
												_removeSkillCache.computeIfAbsent(playerClass, _ -> new HashSet<>()).add(removeSkillId);
											}
									}
								}

								long skillHashCode = SkillData.getSkillHashCode(skillLearn.getSkillId(), skillLearn.getSkillLevel());
								switch (type)
								{
									case "classSkillTree":
										if (cId != -1)
										{
											classSkillTree.put(skillHashCode, skillLearn);
										}
										else
										{
											_commonSkillTree.put(skillHashCode, skillLearn);
										}
										break;
									case "transferSkillTree":
										transferSkillTree.put(skillHashCode, skillLearn);
										break;
									case "collectSkillTree":
										_collectSkillTree.put(skillHashCode, skillLearn);
										break;
									case "raceSkillTree":
										raceSkillTree.put(skillHashCode, skillLearn);
										break;
									case "revelationSkillTree":
										revelationSkillTree.put(skillHashCode, skillLearn);
										break;
									case "fishingSkillTree":
										_fishingSkillTree.put(skillHashCode, skillLearn);
										break;
									case "pledgeSkillTree":
										_pledgeSkillTree.put(skillHashCode, skillLearn);
										break;
									case "subClassSkillTree":
										_subClassSkillTree.put(skillHashCode, skillLearn);
										break;
									case "subPledgeSkillTree":
										_subPledgeSkillTree.put(skillHashCode, skillLearn);
										break;
									case "transformSkillTree":
										_transformSkillTree.put(skillHashCode, skillLearn);
										break;
									case "nobleSkillTree":
										_nobleSkillTree.put(skillHashCode, skillLearn);
										break;
									case "abilitySkillTree":
										_abilitySkillTree.put(skillHashCode, skillLearn);
										break;
									case "alchemySkillTree":
										_alchemySkillTree.put(skillHashCode, skillLearn);
										break;
									case "heroSkillTree":
										_heroSkillTree.put(skillHashCode, skillLearn);
										break;
									case "gameMasterSkillTree":
										_gameMasterSkillTree.put(skillHashCode, skillLearn);
										break;
									case "gameMasterAuraSkillTree":
										_gameMasterAuraSkillTree.put(skillHashCode, skillLearn);
										break;
									case "dualClassSkillTree":
										_dualClassSkillTree.put(skillHashCode, skillLearn);
										break;
									case "awakeningSaveSkillTree":
										_awakeningSaveSkillTree.computeIfAbsent(playerClass, _ -> new HashSet<>()).add(skillLearn.getSkillId());
										break;
									default:
										LOGGER.warning(this.getClass().getSimpleName() + ": Unknown Skill Tree type: " + type + "!");
								}
							}
						}

						if (type.equals("transferSkillTree"))
						{
							_transferSkillTrees.put(playerClass, transferSkillTree);
						}
						else if (type.equals("classSkillTree") && cId > -1)
						{
							Map<Long, SkillLearn> classSkillTrees = _classSkillTrees.get(playerClass);
							if (classSkillTrees == null)
							{
								_classSkillTrees.put(playerClass, classSkillTree);
							}
							else
							{
								classSkillTrees.putAll(classSkillTree);
							}
						}
						else if (type.equals("raceSkillTree") && race != null)
						{
							Map<Long, SkillLearn> raceSkillTrees = _raceSkillTree.get(race);
							if (raceSkillTrees == null)
							{
								_raceSkillTree.put(race, raceSkillTree);
							}
							else
							{
								raceSkillTrees.putAll(raceSkillTree);
							}
						}
						else if (type.equals("revelationSkillTree") && subType != null)
						{
							Map<Long, SkillLearn> revelationSkillTrees = _revelationSkillTree.get(subType);
							if (revelationSkillTrees == null)
							{
								_revelationSkillTree.put(subType, revelationSkillTree);
							}
							else
							{
								revelationSkillTrees.putAll(revelationSkillTree);
							}
						}
					}
				}
			}
		}
	}

	public Map<Long, SkillLearn> getCompleteClassSkillTree(PlayerClass playerClass)
	{
		return _completeClassSkillTree.getOrDefault(playerClass, Collections.emptyMap());
	}

	public Map<Long, SkillLearn> getTransferSkillTree(PlayerClass playerClass)
	{
		return _transferSkillTrees.get(playerClass);
	}

	@SuppressWarnings("unchecked")
	public Collection<SkillLearn> getRaceSkillTree(Race race)
	{
		return (Collection<SkillLearn>) (_raceSkillTree.containsKey(race) ? _raceSkillTree.get(race).values() : Collections.emptyList());
	}

	public Map<Long, SkillLearn> getCommonSkillTree()
	{
		return _commonSkillTree;
	}

	public Map<Long, SkillLearn> getCollectSkillTree()
	{
		return _collectSkillTree;
	}

	public Map<Long, SkillLearn> getFishingSkillTree()
	{
		return _fishingSkillTree;
	}

	public Map<Long, SkillLearn> getPledgeSkillTree()
	{
		return _pledgeSkillTree;
	}

	public Map<Long, SkillLearn> getSubClassSkillTree()
	{
		return _subClassSkillTree;
	}

	public Map<Long, SkillLearn> getSubPledgeSkillTree()
	{
		return _subPledgeSkillTree;
	}

	public Map<Long, SkillLearn> getTransformSkillTree()
	{
		return _transformSkillTree;
	}

	public Map<Long, SkillLearn> getAbilitySkillTree()
	{
		return _abilitySkillTree;
	}

	public Map<Long, SkillLearn> getAlchemySkillTree()
	{
		return _alchemySkillTree;
	}

	public List<Skill> getNobleSkillTree()
	{
		List<Skill> result = new LinkedList<>();

		for (SkillLearn skill : _nobleSkillTree.values())
		{
			result.add(SkillData.getInstance().getSkill(skill.getSkillId(), skill.getSkillLevel()));
		}

		return result;
	}

	public List<Skill> getNobleSkillAutoGetTree()
	{
		List<Skill> result = new LinkedList<>();

		for (SkillLearn skill : _nobleSkillTree.values())
		{
			if (skill.isAutoGet())
			{
				result.add(SkillData.getInstance().getSkill(skill.getSkillId(), skill.getSkillLevel()));
			}
		}

		return result;
	}

	public List<Skill> getHeroSkillTree()
	{
		List<Skill> result = new LinkedList<>();

		for (SkillLearn skill : _heroSkillTree.values())
		{
			result.add(SkillData.getInstance().getSkill(skill.getSkillId(), skill.getSkillLevel()));
		}

		return result;
	}

	public List<Skill> getGMSkillTree()
	{
		List<Skill> result = new LinkedList<>();

		for (SkillLearn skill : _gameMasterSkillTree.values())
		{
			result.add(SkillData.getInstance().getSkill(skill.getSkillId(), skill.getSkillLevel()));
		}

		return result;
	}

	public List<Skill> getGMAuraSkillTree()
	{
		List<Skill> result = new LinkedList<>();

		for (SkillLearn skill : _gameMasterAuraSkillTree.values())
		{
			result.add(SkillData.getInstance().getSkill(skill.getSkillId(), skill.getSkillLevel()));
		}

		return result;
	}

	public boolean hasAvailableSkills(Player player, PlayerClass playerClass)
	{
		Map<Long, SkillLearn> skills = this.getCompleteClassSkillTree(playerClass);

		for (SkillLearn skill : skills.values())
		{
			if (skill.getSkillId() != CommonSkill.DIVINE_INSPIRATION.getId() && !skill.isAutoGet() && !skill.isLearnedByFS() && skill.getGetLevel() <= player.getLevel())
			{
				Skill oldSkill = player.getKnownSkill(skill.getSkillId());
				if ((oldSkill != null && oldSkill.getLevel() == skill.getSkillLevel() - 1) || (oldSkill == null && skill.getSkillLevel() == 1))
				{
					return true;
				}
			}
		}

		return false;
	}

	public Collection<SkillLearn> getAvailableSkills(Player player, PlayerClass playerClass, boolean includeByFs, boolean includeAutoGet)
	{
		return this.getAvailableSkills(player, playerClass, includeByFs, includeAutoGet, true, player.getSkills());
	}

	private Collection<SkillLearn> getAvailableSkills(Player player, PlayerClass playerClass, boolean includeByFs, boolean includeAutoGet, boolean includeRequiredItems, Map<Integer, Skill> existingSkills)
	{
		Set<SkillLearn> result = new HashSet<>();
		Map<Long, SkillLearn> skills = this.getCompleteClassSkillTree(playerClass);
		if (skills.isEmpty())
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": Skilltree for class " + playerClass + " is not defined!");
			return result;
		}
		for (Entry<Long, SkillLearn> entry : skills.entrySet())
		{
			SkillLearn skill = entry.getValue();
			if ((skill.getSkillId() != CommonSkill.DIVINE_INSPIRATION.getId() || PlayerConfig.AUTO_LEARN_DIVINE_INSPIRATION || !includeAutoGet || player.isGM()) && (includeAutoGet || !skill.isAutoGet()) && (includeByFs || !skill.isLearnedByFS()) && !this.isRemoveSkill(playerClass, skill.getSkillId()) && (includeRequiredItems || skill.getRequiredItems().isEmpty() || skill.isLearnedByFS()) && player.getLevel() >= skill.getGetLevel())
			{
				if (skill.getSkillLevel() > SkillData.getInstance().getMaxLevel(skill.getSkillId()))
				{
					LOGGER.severe(this.getClass().getSimpleName() + ": SkillTreesData found learnable skill " + skill.getSkillId() + " with level higher than max skill level!");
				}
				else
				{
					Skill oldSkill = existingSkills.get(player.getReplacementSkill(skill.getSkillId()));
					if (oldSkill != null)
					{
						if (oldSkill.getLevel() == skill.getSkillLevel() - 1)
						{
							result.add(skill);
						}
					}
					else if (skill.getSkillLevel() == 1)
					{
						result.add(skill);
					}
				}
			}
		}

		Iterator<?> var17 = player.getSkillList().iterator();

		while (true)
		{
			Set<Integer> removeSkills;
			while (true)
			{
				if (!var17.hasNext())
				{
					for (int skillId : player.getReplacedSkills())
					{
						SkillLearn skillLearn = this.getClassSkill(skillId, 1, playerClass);
						if (skillLearn != null)
						{
							removeSkills = skillLearn.getRemoveSkills();
							if (removeSkills != null)
							{
								for (int removeId : removeSkills)
								{
									for (SkillLearn knownLearn : result)
									{
										if (knownLearn.getSkillId() == removeId)
										{
											result.remove(knownLearn);
											break;
										}
									}
								}
							}
						}
					}

					return result;
				}

				Skill knownSkill = (Skill) var17.next();
				SkillLearn skillLearn = this.getClassSkill(player.getOriginalSkill(knownSkill.getId()), knownSkill.getLevel(), playerClass);
				if (skillLearn != null)
				{
					removeSkills = skillLearn.getRemoveSkills();
					if (!removeSkills.isEmpty())
					{
						break;
					}

					if (knownSkill.getLevel() > 1)
					{
						skillLearn = this.getClassSkill(knownSkill.getId(), 1, playerClass);
						if (skillLearn != null)
						{
							removeSkills = skillLearn.getRemoveSkills();
							if (removeSkills.isEmpty())
							{
								continue;
							}
							break;
						}
					}
				}
			}

			for (int removeId : removeSkills)
			{
				for (SkillLearn knownLearnx : result)
				{
					if (knownLearnx.getSkillId() == removeId)
					{
						result.remove(knownLearnx);
						break;
					}
				}
			}
		}
	}

	public Collection<Skill> getAllAvailableSkills(Player player, PlayerClass playerClass, boolean includeByFs, boolean includeAutoGet, boolean includeRequiredItems)
	{
		Map<Integer, Skill> result = new HashMap<>();

		for (Skill skill : player.getSkills().values())
		{
			if (this.isSkillAllowed(player, skill))
			{
				result.put(skill.getId(), skill);
			}
		}

		NavigableMap<Integer, Integer> classSkillLevels = _maxClassSkillLevels.get(playerClass);
		if (classSkillLevels == null)
		{
			return result.values();
		}
		Entry<Integer, Integer> maxPlayerSkillLevel = classSkillLevels.floorEntry(player.getLevel());
		if (maxPlayerSkillLevel == null)
		{
			return result.values();
		}
		Set<Integer> removed = new HashSet<>();

		for (int i = 0; i < maxPlayerSkillLevel.getValue(); i++)
		{
			Collection<SkillLearn> learnable = this.getAvailableSkills(player, playerClass, includeByFs, includeAutoGet, includeRequiredItems, result);
			if (learnable.isEmpty())
			{
				break;
			}

			boolean allRemoved = true;

			for (SkillLearn skillLearn : learnable)
			{
				if (!removed.contains(skillLearn.getSkillId()))
				{
					allRemoved = false;
					break;
				}
			}

			if (allRemoved)
			{
				break;
			}

			for (SkillLearn skillLearnx : learnable)
			{
				for (int skillId : skillLearnx.getRemoveSkills())
				{
					removed.add(skillId);
					Skill playerSkillToRemove = player.getKnownSkill(skillId);
					Skill holderSkillToRemove = result.get(skillId);
					if (playerSkillToRemove != null)
					{
						player.removeSkill(playerSkillToRemove);
					}

					if (holderSkillToRemove != null)
					{
						result.remove(skillId);
					}
				}

				if (!removed.contains(skillLearnx.getSkillId()))
				{
					Skill skillx = SkillData.getInstance().getSkill(skillLearnx.getSkillId(), skillLearnx.getSkillLevel());
					result.put(skillx.getId(), skillx);
				}
			}
		}

		return result.values();
	}

	public List<SkillLearn> getAvailableAutoGetSkills(Player player)
	{
		List<SkillLearn> result = new LinkedList<>();
		Map<Long, SkillLearn> skills = this.getCompleteClassSkillTree(player.getPlayerClass());
		if (skills.isEmpty())
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": Skill Tree for this class Id(" + player.getPlayerClass() + ") is not defined!");
			return result;
		}
		Race race = player.getRace();

		for (SkillLearn skill : skills.values())
		{
			if (skill.isAutoGet() && player.getLevel() >= skill.getGetLevel() && (skill.getRaces().isEmpty() || skill.getRaces().contains(race)))
			{
				Skill oldSkill = player.getKnownSkill(player.getReplacementSkill(skill.getSkillId()));
				if (oldSkill != null)
				{
					if (oldSkill.getLevel() < skill.getSkillLevel())
					{
						result.add(skill);
					}
				}
				else
				{
					result.add(skill);
				}
			}
		}

		Iterator<?> var13 = player.getSkillList().iterator();

		while (true)
		{
			Set<Integer> removeSkills;
			while (true)
			{
				if (!var13.hasNext())
				{
					return result;
				}

				Skill knownSkill = (Skill) var13.next();
				SkillLearn skillLearn = this.getClassSkill(player.getOriginalSkill(knownSkill.getId()), knownSkill.getLevel(), player.getPlayerClass());
				if (skillLearn != null)
				{
					removeSkills = skillLearn.getRemoveSkills();
					if (!removeSkills.isEmpty())
					{
						break;
					}

					if (knownSkill.getLevel() > 1)
					{
						skillLearn = this.getClassSkill(knownSkill.getId(), 1, player.getPlayerClass());
						if (skillLearn != null)
						{
							removeSkills = skillLearn.getRemoveSkills();
							if (removeSkills.isEmpty())
							{
								continue;
							}
							break;
						}
					}
				}
			}

			for (int removeId : removeSkills)
			{
				for (SkillLearn knownLearn : result)
				{
					if (knownLearn.getSkillId() == removeId)
					{
						result.remove(knownLearn);
						break;
					}
				}
			}
		}
	}

	public List<SkillLearn> getAvailableFishingSkills(Player player)
	{
		List<SkillLearn> result = new LinkedList<>();
		Race playerRace = player.getRace();

		for (SkillLearn skill : _fishingSkillTree.values())
		{
			if ((skill.getRaces().isEmpty() || skill.getRaces().contains(playerRace)) && skill.isLearnedByNpc() && player.getLevel() >= skill.getGetLevel())
			{
				Skill oldSkill = player.getSkills().get(skill.getSkillId());
				if (oldSkill != null)
				{
					if (oldSkill.getLevel() == skill.getSkillLevel() - 1)
					{
						result.add(skill);
					}
				}
				else if (skill.getSkillLevel() == 1)
				{
					result.add(skill);
				}
			}
		}

		return result;
	}

	public List<SkillLearn> getAvailableRevelationSkills(Player player, SubclassType type)
	{
		List<SkillLearn> result = new LinkedList<>();
		Map<Long, SkillLearn> revelationSkills = _revelationSkillTree.get(type);

		for (SkillLearn skill : revelationSkills.values())
		{
			Skill oldSkill = player.getSkills().get(skill.getSkillId());
			if (oldSkill == null)
			{
				result.add(skill);
			}
		}

		return result;
	}

	public List<SkillLearn> getAvailableAlchemySkills(Player player)
	{
		List<SkillLearn> result = new LinkedList<>();

		for (SkillLearn skill : _alchemySkillTree.values())
		{
			if (skill.isLearnedByNpc() && player.getLevel() >= skill.getGetLevel())
			{
				Skill oldSkill = player.getSkills().get(skill.getSkillId());
				if (oldSkill != null)
				{
					if (oldSkill.getLevel() == skill.getSkillLevel() - 1)
					{
						result.add(skill);
					}
				}
				else if (skill.getSkillLevel() == 1)
				{
					result.add(skill);
				}
			}
		}

		return result;
	}

	public List<SkillLearn> getAvailableCollectSkills(Player player)
	{
		List<SkillLearn> result = new LinkedList<>();

		for (SkillLearn skill : _collectSkillTree.values())
		{
			Skill oldSkill = player.getSkills().get(skill.getSkillId());
			if (oldSkill != null)
			{
				if (oldSkill.getLevel() == skill.getSkillLevel() - 1)
				{
					result.add(skill);
				}
			}
			else if (skill.getSkillLevel() == 1)
			{
				result.add(skill);
			}
		}

		return result;
	}

	public List<SkillLearn> getAvailableTransferSkills(Player player)
	{
		List<SkillLearn> result = new LinkedList<>();
		PlayerClass playerClass = player.getPlayerClass();
		if (!_transferSkillTrees.containsKey(playerClass))
		{
			return result;
		}
		for (SkillLearn skill : _transferSkillTrees.get(playerClass).values())
		{
			if (player.getKnownSkill(skill.getSkillId()) == null)
			{
				result.add(skill);
			}
		}

		return result;
	}

	public List<SkillLearn> getAvailableTransformSkills(Player player)
	{
		List<SkillLearn> result = new LinkedList<>();
		Race race = player.getRace();

		for (SkillLearn skill : _transformSkillTree.values())
		{
			if (player.getLevel() >= skill.getGetLevel() && (skill.getRaces().isEmpty() || skill.getRaces().contains(race)))
			{
				Skill oldSkill = player.getSkills().get(skill.getSkillId());
				if (oldSkill != null)
				{
					if (oldSkill.getLevel() == skill.getSkillLevel() - 1)
					{
						result.add(skill);
					}
				}
				else if (skill.getSkillLevel() == 1)
				{
					result.add(skill);
				}
			}
		}

		return result;
	}

	public List<SkillLearn> getAvailablePledgeSkills(Clan clan)
	{
		List<SkillLearn> result = new LinkedList<>();

		for (SkillLearn skill : _pledgeSkillTree.values())
		{
			if (!skill.isResidencialSkill() && clan.getLevel() >= skill.getGetLevel())
			{
				Skill oldSkill = clan.getSkills().get(skill.getSkillId());
				if (oldSkill != null)
				{
					if (oldSkill.getLevel() + 1 == skill.getSkillLevel())
					{
						result.add(skill);
					}
				}
				else if (skill.getSkillLevel() == 1)
				{
					result.add(skill);
				}
			}
		}

		return result;
	}

	public Map<Integer, SkillLearn> getMaxPledgeSkills(Clan clan, boolean includeSquad)
	{
		Map<Integer, SkillLearn> result = new HashMap<>();

		for (SkillLearn skill : _pledgeSkillTree.values())
		{
			if (!skill.isResidencialSkill() && clan.getLevel() >= skill.getGetLevel())
			{
				Skill oldSkill = clan.getSkills().get(skill.getSkillId());
				if (oldSkill == null || oldSkill.getLevel() < skill.getSkillLevel())
				{
					result.put(skill.getSkillId(), skill);
				}
			}
		}

		if (includeSquad)
		{
			for (SkillLearn skillx : _subPledgeSkillTree.values())
			{
				if (clan.getLevel() >= skillx.getGetLevel())
				{
					Skill oldSkill = clan.getSkills().get(skillx.getSkillId());
					if (oldSkill == null || oldSkill.getLevel() < skillx.getSkillLevel())
					{
						result.put(skillx.getSkillId(), skillx);
					}
				}
			}
		}

		return result;
	}

	public List<SkillLearn> getAvailableSubPledgeSkills(Clan clan)
	{
		List<SkillLearn> result = new LinkedList<>();

		for (SkillLearn skill : _subPledgeSkillTree.values())
		{
			if (clan.getLevel() >= skill.getGetLevel() && clan.isLearnableSubSkill(skill.getSkillId(), skill.getSkillLevel()))
			{
				result.add(skill);
			}
		}

		return result;
	}

	public List<SkillLearn> getAvailableSubClassSkills(Player player)
	{
		List<SkillLearn> result = new LinkedList<>();

		for (SkillLearn skill : _subClassSkillTree.values())
		{
			Skill oldSkill = player.getSkills().get(skill.getSkillId());
			if (oldSkill == null && skill.getSkillLevel() == 1 || oldSkill != null && oldSkill.getLevel() == skill.getSkillLevel() - 1)
			{
				result.add(skill);
			}
		}

		return result;
	}

	public List<SkillLearn> getAvailableDualClassSkills(Player player)
	{
		List<SkillLearn> result = new LinkedList<>();

		for (SkillLearn skill : _dualClassSkillTree.values())
		{
			Skill oldSkill = player.getSkills().get(skill.getSkillId());
			if (oldSkill == null && skill.getSkillLevel() == 1 || oldSkill != null && oldSkill.getLevel() == skill.getSkillLevel() - 1)
			{
				result.add(skill);
			}
		}

		result.sort(Comparator.comparing(SkillLearn::getSkillId));
		return result;
	}

	public List<SkillLearn> getAvailableResidentialSkills(int residenceId)
	{
		List<SkillLearn> result = new LinkedList<>();

		for (SkillLearn skill : _pledgeSkillTree.values())
		{
			if (skill.isResidencialSkill() && skill.getResidenceIds().contains(residenceId))
			{
				result.add(skill);
			}
		}

		return result;
	}

	public SkillLearn getSkillLearn(AcquireSkillType skillType, int id, int lvl, Player player)
	{
		SkillLearn sl = null;
		switch (skillType)
		{
			case CLASS:
				sl = getClassSkill(id, lvl, player.getPlayerClass());
				break;
			case TRANSFORM:
				sl = getTransformSkill(id, lvl);
				break;
			case FISHING:
				sl = getFishingSkill(id, lvl);
				break;
			case PLEDGE:
				sl = getPledgeSkill(id, lvl);
				break;
			case SUBPLEDGE:
				sl = getSubPledgeSkill(id, lvl);
				break;
			case TRANSFER:
				sl = getTransferSkill(id, lvl, player.getPlayerClass());
				break;
			case SUBCLASS:
				sl = getSubClassSkill(id, lvl);
				break;
			case COLLECT:
				sl = getCollectSkill(id, lvl);
				break;
			case REVELATION:
				sl = getRevelationSkill(SubclassType.BASECLASS, id, lvl);
				break;
			case REVELATION_DUALCLASS:
				sl = getRevelationSkill(SubclassType.DUALCLASS, id, lvl);
				break;
			case ALCHEMY:
				sl = getAlchemySkill(id, lvl);
				break;
			case DUALCLASS:
				sl = getDualClassSkill(id, lvl);
		}

		return sl;
	}

	private static SkillLearn getTransformSkill(int id, int lvl)
	{
		return _transformSkillTree.get(SkillData.getSkillHashCode(id, lvl));
	}

	public SkillLearn getAbilitySkill(int id, int lvl)
	{
		return _abilitySkillTree.get(SkillData.getSkillHashCode(id, lvl));
	}

	private static SkillLearn getAlchemySkill(int id, int lvl)
	{
		return _alchemySkillTree.get(SkillData.getSkillHashCode(id, lvl));
	}

	public SkillLearn getClassSkill(int id, int lvl, PlayerClass playerClass)
	{
		return this.getCompleteClassSkillTree(playerClass).get(SkillData.getSkillHashCode(id, lvl));
	}

	private static SkillLearn getFishingSkill(int id, int lvl)
	{
		return _fishingSkillTree.get(SkillData.getSkillHashCode(id, lvl));
	}

	public SkillLearn getPledgeSkill(int id, int lvl)
	{
		return _pledgeSkillTree.get(SkillData.getSkillHashCode(id, lvl));
	}

	public SkillLearn getSubPledgeSkill(int id, int lvl)
	{
		return _subPledgeSkillTree.get(SkillData.getSkillHashCode(id, lvl));
	}

	private static SkillLearn getTransferSkill(int id, int lvl, PlayerClass playerClass)
	{
		return _transferSkillTrees.get(playerClass) != null ? _transferSkillTrees.get(playerClass).get(SkillData.getSkillHashCode(id, lvl)) : null;
	}

	private SkillLearn getRaceSkill(int id, int lvl, Race race)
	{
		for (SkillLearn skill : this.getRaceSkillTree(race))
		{
			if (skill.getSkillId() == id && skill.getSkillLevel() == lvl)
			{
				return skill;
			}
		}

		return null;
	}

	private static SkillLearn getSubClassSkill(int id, int lvl)
	{
		return _subClassSkillTree.get(SkillData.getSkillHashCode(id, lvl));
	}

	public SkillLearn getDualClassSkill(int id, int lvl)
	{
		return _dualClassSkillTree.get(SkillData.getSkillHashCode(id, lvl));
	}

	public SkillLearn getCommonSkill(int id, int lvl)
	{
		return _commonSkillTree.get(SkillData.getSkillHashCode(id, lvl));
	}

	public SkillLearn getCollectSkill(int id, int lvl)
	{
		return _collectSkillTree.get(SkillData.getSkillHashCode(id, lvl));
	}

	public SkillLearn getRevelationSkill(SubclassType type, int id, int lvl)
	{
		return _revelationSkillTree.get(type).get(SkillData.getSkillHashCode(id, lvl));
	}

	public int getMinLevelForNewSkill(Player player, Map<Long, SkillLearn> skillTree)
	{
		int minLevel = 0;
		if (skillTree.isEmpty())
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": SkillTree is not defined for getMinLevelForNewSkill!");
		}
		else
		{
			for (SkillLearn s : skillTree.values())
			{
				if (player.getLevel() < s.getGetLevel() && (minLevel == 0 || minLevel > s.getGetLevel()))
				{
					minLevel = s.getGetLevel();
				}
			}
		}

		return minLevel;
	}

	public Collection<SkillLearn> getNextAvailableSkills(Player player, PlayerClass playerClass, boolean includeByFs, boolean includeAutoGet)
	{
		Map<Long, SkillLearn> completeClassSkillTree = this.getCompleteClassSkillTree(playerClass);
		Set<SkillLearn> result = new HashSet<>();
		if (completeClassSkillTree.isEmpty())
		{
			return result;
		}
		int minLevelForNewSkill = this.getMinLevelForNewSkill(player, completeClassSkillTree);
		if (minLevelForNewSkill > 0)
		{
			for (SkillLearn skill : completeClassSkillTree.values())
			{
				if (skill.getGetLevel() <= PlayerConfig.PLAYER_MAXIMUM_LEVEL && (includeAutoGet || !skill.isAutoGet()) && (includeByFs || !skill.isLearnedByFS()) && minLevelForNewSkill <= skill.getGetLevel())
				{
					Skill oldSkill = player.getKnownSkill(player.getReplacementSkill(skill.getSkillId()));
					if (oldSkill != null)
					{
						if (oldSkill.getLevel() == skill.getSkillLevel() - 1)
						{
							result.add(skill);
						}
					}
					else if (skill.getSkillLevel() == 1)
					{
						result.add(skill);
					}
				}
			}
		}

		Iterator<?> var16 = player.getSkillList().iterator();

		while (true)
		{
			Set<Integer> removeSkills;
			while (true)
			{
				if (!var16.hasNext())
				{
					return result;
				}

				Skill knownSkill = (Skill) var16.next();
				SkillLearn skillLearn = this.getClassSkill(player.getOriginalSkill(knownSkill.getId()), knownSkill.getLevel(), playerClass);
				if (skillLearn != null)
				{
					removeSkills = skillLearn.getRemoveSkills();
					if (!removeSkills.isEmpty())
					{
						break;
					}

					if (knownSkill.getLevel() > 1)
					{
						skillLearn = this.getClassSkill(knownSkill.getId(), 1, playerClass);
						if (skillLearn != null)
						{
							removeSkills = skillLearn.getRemoveSkills();
							if (removeSkills.isEmpty())
							{
								continue;
							}
							break;
						}
					}
				}
			}

			for (int removeId : removeSkills)
			{
				for (SkillLearn knownLearn : result)
				{
					if (knownLearn.getSkillId() == removeId)
					{
						result.remove(knownLearn);
						break;
					}
				}
			}
		}
	}

	public void cleanSkillUponChangeClass(Player player)
	{
		PlayerClass currentClass = player.getPlayerClass();

		for (Skill skill : player.getAllSkills())
		{
			int maxLevel = SkillData.getInstance().getMaxLevel(skill.getId());
			long hashCode = SkillData.getSkillHashCode(skill.getId(), maxLevel);
			if (!this.isCurrentClassSkillNoParent(currentClass, hashCode) && !this.isRemoveSkill(currentClass, skill.getId()) && !this.isAwakenSaveSkill(currentClass, skill.getId()) && !this.isAlchemySkill(skill.getId(), skill.getLevel()))
			{
				boolean isItemSkill = false;

				label75:
				for (Item item : player.getInventory().getItems())
				{
					List<ItemSkillHolder> itemSkills = item.getTemplate().getAllSkills();
					if (itemSkills != null)
					{
						for (ItemSkillHolder itemSkillHolder : itemSkills)
						{
							if (itemSkillHolder.getSkillId() == skill.getId())
							{
								isItemSkill = true;
								break label75;
							}
						}
					}
				}

				if (!isItemSkill)
				{
					player.removeSkill(skill, true, true);
				}
			}
		}

		for (; currentClass.getParent() != null; currentClass = currentClass.getParent())
		{
			Set<Integer> removedList = _removeSkillCache.get(currentClass);
			if (removedList != null)
			{
				for (Integer skillId : removedList)
				{
					int currentLevel = player.getSkillLevel(skillId);
					if (currentLevel > 0)
					{
						player.removeSkill(SkillData.getInstance().getSkill(skillId, currentLevel));
					}
				}
			}
		}
	}

	public boolean isAlchemySkill(int skillId, int skillLevel)
	{
		return _alchemySkillTree.containsKey(SkillData.getSkillHashCode(skillId, skillLevel));
	}

	public boolean isHeroSkill(int skillId, int skillLevel)
	{
		return _heroSkillTree.containsKey(SkillData.getSkillHashCode(skillId, skillLevel));
	}

	public boolean isGMSkill(int skillId, int skillLevel)
	{
		long hashCode = SkillData.getSkillHashCode(skillId, skillLevel);
		return _gameMasterSkillTree.containsKey(hashCode) || _gameMasterAuraSkillTree.containsKey(hashCode);
	}

	public boolean isClanSkill(int skillId, int skillLevel)
	{
		long hashCode = SkillData.getSkillHashCode(skillId, skillLevel);
		return _pledgeSkillTree.containsKey(hashCode) || _subPledgeSkillTree.containsKey(hashCode);
	}

	public boolean isRemoveSkill(PlayerClass playerClass, int skillId)
	{
		return _removeSkillCache.getOrDefault(playerClass, Collections.emptySet()).contains(skillId);
	}

	public boolean isCurrentClassSkillNoParent(PlayerClass playerClass, Long hashCode)
	{
		return _classSkillTrees.getOrDefault(playerClass, Collections.emptyMap()).containsKey(hashCode);
	}

	public boolean isAwakenSaveSkill(PlayerClass playerClass, int skillId)
	{
		return _awakeningSaveSkillTree.getOrDefault(playerClass, Collections.emptySet()).contains(skillId);
	}

	public void addSkills(Player gmchar, boolean auraSkills)
	{
		Collection<SkillLearn> skills = auraSkills ? _gameMasterAuraSkillTree.values() : _gameMasterSkillTree.values();
		SkillData st = SkillData.getInstance();

		for (SkillLearn sl : skills)
		{
			gmchar.addSkill(st.getSkill(sl.getSkillId(), sl.getSkillLevel()), false);
		}
	}

	private void generateCheckArrays()
	{
		Set<PlayerClass> playerClassSet = _classSkillTrees.keySet();
		this._skillsByClassIdHashCodes = new HashMap<>(playerClassSet.size());

		for (PlayerClass playerClass : playerClassSet)
		{
			int index = 0;
			Map<Long, SkillLearn> skillLearnMap = new HashMap<>(this.getCompleteClassSkillTree(playerClass));
			long[] skillHashes = new long[skillLearnMap.size()];

			for (long skillHash : skillLearnMap.keySet())
			{
				skillHashes[index++] = skillHash;
			}

			skillLearnMap.clear();
			Arrays.sort(skillHashes);
			this._skillsByClassIdHashCodes.put(playerClass.getId(), skillHashes);
		}

		List<Long> skillHashList = new LinkedList<>();
		this._skillsByRaceHashCodes = new HashMap<>(Race.values().length);

		for (Race race : Race.values())
		{
			for (SkillLearn skillLearn : _fishingSkillTree.values())
			{
				if (skillLearn.getRaces().contains(race))
				{
					skillHashList.add(SkillData.getSkillHashCode(skillLearn.getSkillId(), skillLearn.getSkillLevel()));
				}
			}

			for (SkillLearn skillLearnx : _transformSkillTree.values())
			{
				if (skillLearnx.getRaces().contains(race))
				{
					skillHashList.add(SkillData.getSkillHashCode(skillLearnx.getSkillId(), skillLearnx.getSkillLevel()));
				}
			}

			int index = 0;
			long[] skillHashes = new long[skillHashList.size()];

			for (long skillHash : skillHashList)
			{
				skillHashes[index++] = skillHash;
			}

			Arrays.sort(skillHashes);
			this._skillsByRaceHashCodes.put(race.ordinal(), skillHashes);
			skillHashList.clear();
		}

		for (SkillLearn skillLearnxx : _commonSkillTree.values())
		{
			if (skillLearnxx.getRaces().isEmpty())
			{
				skillHashList.add(SkillData.getSkillHashCode(skillLearnxx.getSkillId(), skillLearnxx.getSkillLevel()));
			}
		}

		for (SkillLearn skillLearnxxx : _fishingSkillTree.values())
		{
			if (skillLearnxxx.getRaces().isEmpty())
			{
				skillHashList.add(SkillData.getSkillHashCode(skillLearnxxx.getSkillId(), skillLearnxxx.getSkillLevel()));
			}
		}

		for (SkillLearn skillLearnxxxx : _transformSkillTree.values())
		{
			if (skillLearnxxxx.getRaces().isEmpty())
			{
				skillHashList.add(SkillData.getSkillHashCode(skillLearnxxxx.getSkillId(), skillLearnxxxx.getSkillLevel()));
			}
		}

		for (SkillLearn skillLearnxxxxx : _collectSkillTree.values())
		{
			skillHashList.add(SkillData.getSkillHashCode(skillLearnxxxxx.getSkillId(), skillLearnxxxxx.getSkillLevel()));
		}

		for (SkillLearn skillLearnxxxxx : _abilitySkillTree.values())
		{
			skillHashList.add(SkillData.getSkillHashCode(skillLearnxxxxx.getSkillId(), skillLearnxxxxx.getSkillLevel()));
		}

		for (SkillLearn skillLearnxxxxx : _alchemySkillTree.values())
		{
			skillHashList.add(SkillData.getSkillHashCode(skillLearnxxxxx.getSkillId(), skillLearnxxxxx.getSkillLevel()));
		}

		this._allSkillsHashCodes = new long[skillHashList.size()];
		int hashIndex = 0;

		for (long skillHash : skillHashList)
		{
			this._allSkillsHashCodes[hashIndex++] = skillHash;
		}

		Arrays.sort(this._allSkillsHashCodes);
	}

	public boolean isSkillAllowed(Player player, Skill skill)
	{
		if (skill.isExcludedFromCheck())
		{
			return true;
		}
		else if (player.isGM() && skill.isGMSkill())
		{
			return true;
		}
		else if (this._loading)
		{
			return true;
		}
		else
		{
			int maxLevel = SkillData.getInstance().getMaxLevel(skill.getId());
			long hashCode = SkillData.getSkillHashCode(skill.getId(), Math.min(skill.getLevel(), maxLevel));
			if (Arrays.binarySearch(this._skillsByClassIdHashCodes.get(player.getPlayerClass().getId()), hashCode) >= 0)
			{
				return true;
			}
			else if (Arrays.binarySearch(this._skillsByRaceHashCodes.get(player.getRace().ordinal()), hashCode) >= 0)
			{
				return true;
			}
			else if (Arrays.binarySearch(this._allSkillsHashCodes, hashCode) >= 0)
			{
				return true;
			}
			else
			{
				return getTransferSkill(skill.getId(), Math.min(skill.getLevel(), maxLevel), player.getPlayerClass()) != null ? true : this.getRaceSkill(skill.getId(), Math.min(skill.getLevel(), maxLevel), player.getRace()) != null;
			}
		}
	}

	private void report()
	{
		int classSkillTreeCount = 0;

		for (Map<Long, SkillLearn> classSkillTree : _classSkillTrees.values())
		{
			classSkillTreeCount += classSkillTree.size();
		}

		int transferSkillTreeCount = 0;

		for (Map<Long, SkillLearn> trasferSkillTree : _transferSkillTrees.values())
		{
			transferSkillTreeCount += trasferSkillTree.size();
		}

		int raceSkillTreeCount = 0;

		for (Map<Long, SkillLearn> raceSkillTree : _raceSkillTree.values())
		{
			raceSkillTreeCount += raceSkillTree.size();
		}

		int revelationSkillTreeCount = 0;

		for (Map<Long, SkillLearn> revelationSkillTree : _revelationSkillTree.values())
		{
			revelationSkillTreeCount += revelationSkillTree.size();
		}

		int dwarvenOnlyFishingSkillCount = 0;

		for (SkillLearn fishSkill : _fishingSkillTree.values())
		{
			if (fishSkill.getRaces().contains(Race.DWARF))
			{
				dwarvenOnlyFishingSkillCount++;
			}
		}

		int resSkillCount = 0;

		for (SkillLearn pledgeSkill : _pledgeSkillTree.values())
		{
			if (pledgeSkill.isResidencialSkill())
			{
				resSkillCount++;
			}
		}

		String className = this.getClass().getSimpleName();
		LOGGER.info(className + ": Loaded " + classSkillTreeCount + " Class skills for " + _classSkillTrees.size() + " class skill trees.");
		LOGGER.info(className + ": Loaded " + _subClassSkillTree.size() + " sub-class skills.");
		LOGGER.info(className + ": Loaded " + _dualClassSkillTree.size() + " dual-class skills.");
		LOGGER.info(className + ": Loaded " + transferSkillTreeCount + " transfer skills for " + _transferSkillTrees.size() + " transfer skill trees.");
		LOGGER.info(className + ": Loaded " + raceSkillTreeCount + " race skills for " + _raceSkillTree.size() + " race skill trees.");
		LOGGER.info(className + ": Loaded " + _fishingSkillTree.size() + " fishing skills, " + dwarvenOnlyFishingSkillCount + " Dwarven only fishing skills.");
		LOGGER.info(className + ": Loaded " + _collectSkillTree.size() + " collect skills.");
		LOGGER.info(className + ": Loaded " + _pledgeSkillTree.size() + " clan skills, " + (_pledgeSkillTree.size() - resSkillCount) + " for clan and " + resSkillCount + " residential.");
		LOGGER.info(className + ": Loaded " + _subPledgeSkillTree.size() + " sub-pledge skills.");
		LOGGER.info(className + ": Loaded " + _transformSkillTree.size() + " transform skills.");
		LOGGER.info(className + ": Loaded " + _nobleSkillTree.size() + " noble skills.");
		LOGGER.info(className + ": Loaded " + _heroSkillTree.size() + " hero skills.");
		LOGGER.info(className + ": Loaded " + _gameMasterSkillTree.size() + " game master skills.");
		LOGGER.info(className + ": Loaded " + _gameMasterAuraSkillTree.size() + " game master aura skills.");
		LOGGER.info(className + ": Loaded " + _abilitySkillTree.size() + " ability skills.");
		LOGGER.info(className + ": Loaded " + _alchemySkillTree.size() + " alchemy skills.");
		LOGGER.info(className + ": Loaded " + _awakeningSaveSkillTree.size() + " class awaken save skills.");
		LOGGER.info(className + ": Loaded " + revelationSkillTreeCount + " Revelation skills.");
		int commonSkills = _commonSkillTree.size();
		if (commonSkills > 0)
		{
			LOGGER.info(className + ": Loaded " + commonSkills + " common skills.");
		}
	}

	public static SkillTreeData getInstance()
	{
		return SkillTreeData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final SkillTreeData INSTANCE = new SkillTreeData();
	}
}
