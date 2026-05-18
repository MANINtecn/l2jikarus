package net.sf.l2jdev.gameserver.managers;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.config.AdenLaboratoryConfig;
import net.sf.l2jdev.gameserver.data.enums.AdenLabGameType;
import net.sf.l2jdev.gameserver.data.holders.AdenLabHolder;
import net.sf.l2jdev.gameserver.data.holders.AdenLabSkillHolder;
import net.sf.l2jdev.gameserver.data.holders.AdenLabStageHolder;
import net.sf.l2jdev.gameserver.data.xml.AdenLaboratoryData;
import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.AdenLabRequest;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.adenlab.ExAdenLabBossInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.adenlab.ExAdenLabBossList;
import net.sf.l2jdev.gameserver.network.serverpackets.adenlab.ExAdenLabBossUnlock;
import net.sf.l2jdev.gameserver.network.serverpackets.adenlab.ExAdenLabNormalPlay;
import net.sf.l2jdev.gameserver.network.serverpackets.adenlab.ExAdenLabNormalSlot;
import net.sf.l2jdev.gameserver.network.serverpackets.adenlab.ExAdenLabSpecialFix;
import net.sf.l2jdev.gameserver.network.serverpackets.adenlab.ExAdenLabSpecialPlay;
import net.sf.l2jdev.gameserver.network.serverpackets.adenlab.ExAdenLabSpecialProb;
import net.sf.l2jdev.gameserver.network.serverpackets.adenlab.ExAdenLabSpecialSlot;
import net.sf.l2jdev.gameserver.network.serverpackets.adenlab.ExAdenLabTranscendAnnounce;
import net.sf.l2jdev.gameserver.network.serverpackets.adenlab.ExAdenLabTranscendEnchant;
import net.sf.l2jdev.gameserver.network.serverpackets.adenlab.ExAdenLabTranscendProb;
import net.sf.l2jdev.gameserver.util.Broadcast;

public class AdenLaboratoryManager
{
	private static final Logger LOGGER = Logger.getLogger(AdenLaboratoryManager.class.getName());

	public static int sortingComparator(byte p1, byte p2)
	{
		int group1 = p1 / 10;
		int group2 = p2 / 10;
		return group1 != group2 ? Integer.compare(group1, group2) : Byte.compare(p1, p2);
	}

	public static int getTotalCount(Map<Byte, Map<Byte, List<int[]>>> quickLookupMap, boolean countTypePages)
	{
		int totalCount = 0;

		for (Entry<Byte, Map<Byte, List<int[]>>> bossEntry : quickLookupMap.entrySet())
		{
			Map<Byte, List<int[]>> bossSkills = bossEntry.getValue();
			byte maxPageIndex = bossSkills.keySet().stream().max(AdenLaboratoryManager::sortingComparator).orElse((byte) -1);
			if (maxPageIndex != -1)
			{
				if (countTypePages)
				{
					totalCount += maxPageIndex;
				}
				else
				{
					List<int[]> highestPageSkills = bossSkills.get(maxPageIndex);
					if (highestPageSkills != null && !highestPageSkills.isEmpty())
					{
						totalCount += highestPageSkills.size();
					}
				}
			}
		}

		return totalCount;
	}

	public static void addSkillToCache(int bossId, int pageIndex, int optionIndex, int stageLevel, int[] skill)
	{
		int skillId = skill[0];
		int skillLevel = skill[1];
		AdenLabSkillHolder skillHolder = new AdenLabSkillHolder(skillId, skillLevel);
		Map<Byte, Map<Byte, Map<Byte, List<AdenLabSkillHolder>>>> pageMap = AdenLaboratoryData.getInstance().getSkillsLookupTable().computeIfAbsent((byte) bossId, _ -> new HashMap<>());
		Map<Byte, Map<Byte, List<AdenLabSkillHolder>>> optionsMap = pageMap.computeIfAbsent((byte) pageIndex, _ -> new HashMap<>());
		Map<Byte, List<AdenLabSkillHolder>> stageMap = optionsMap.computeIfAbsent((byte) optionIndex, _ -> new HashMap<>());
		List<AdenLabSkillHolder> skillList = stageMap.computeIfAbsent((byte) stageLevel, _ -> new ArrayList<>());
		skillList.add(skillHolder);
	}

	public static void calculateAdenLabCombatPower(Player player)
	{
		int totalCombatPower = 0;

		for (Entry<Byte, Map<Integer, AdenLabHolder>> bossMap : AdenLaboratoryData.getInstance().getAllAdenLabData().entrySet())
		{
			byte bossId = bossMap.getKey();
			Map<Integer, AdenLabHolder> holderMap = bossMap.getValue();
			int currentPageIndex = player.getAdenLabCurrentlyUnlockedPage(bossId);

			label85:
			for (byte pageIndex : AdenLaboratoryData.getInstance().getSpecialStageIndicesByBossId(bossId))
			{
				if (pageIndex <= currentPageIndex)
				{
					AdenLabHolder holder = holderMap.get(Integer.valueOf(pageIndex));
					if (holder != null && holder.getGameType() == AdenLabGameType.SPECIAL)
					{
						Map<Byte, Map<Byte, Integer>> confirmedOptions = player.getAdenLabSpecialGameStagesConfirmedOptions().get(bossId);
						if (confirmedOptions != null)
						{
							Map<Byte, Integer> optionToLevelMap = confirmedOptions.get(pageIndex);
							if (optionToLevelMap != null)
							{
								for (Entry<Byte, Integer> entry : optionToLevelMap.entrySet())
								{
									byte optionIndex = entry.getKey();
									byte stageLevel = entry.getValue().byteValue();
									List<AdenLabStageHolder> stageList = holder.getStageHolderListByLevel(optionIndex, stageLevel);
									if (stageList != null && !stageList.isEmpty())
									{
										for (AdenLabStageHolder stageHolder : stageList)
										{
											if (stageHolder.getStageLevel() == stageLevel)
											{
												totalCombatPower += stageHolder.getCombatPower();
												continue label85;
											}
										}
									}
								}
							}
						}
					}
				}
			}

			byte transcendentStageLevel = (byte) player.getAdenLabCurrentTranscendLevel(bossId);
			if (transcendentStageLevel > 0)
			{
				for (byte pageIndexx : AdenLaboratoryData.getInstance().getTranscendentStageIndicesByBossId(bossId))
				{
					if (pageIndexx > currentPageIndex)
					{
						return;
					}

					totalCombatPower += AdenLaboratoryData.getInstance().getTranscendentCombatPower(bossId, pageIndexx, transcendentStageLevel);
				}
			}
		}

		player.getCombatPower().setAdenLabCombatPower(totalCombatPower);
	}

	private static byte calculateSuccess(byte bossId, byte pageIndex, byte optionIndex, Player player, boolean simulateSpecial, long numberOfSimulations)
	{
		AdenLabHolder holder = AdenLaboratoryData.getInstance().getAdenLabDataByPageIndex(bossId, pageIndex);
		double random = Rnd.nextDouble() * 100.0;
		float successRate = holder.getGameSuccessRate() * 100.0F;
		float bonusChance = player.getAdenLabBonusChance();
		float finalSuccessRate = 0.0F;
		if (holder.getGameType() == AdenLabGameType.SPECIAL)
		{
			int result = getWeightedResult(player, holder, optionIndex, false);
			if (simulateSpecial && numberOfSimulations > 0L)
			{
				player.sendMessage("Initiating simulation of weighted probabilities. Simulating " + numberOfSimulations + " times...");
				Map<Integer, Integer> stageDistribution = new HashMap<>();
				long startTime = System.nanoTime();

				for (int i = 0; i < numberOfSimulations; i++)
				{
					int selectedStage = getWeightedResult(player, holder, optionIndex, true);
					stageDistribution.put(selectedStage, stageDistribution.getOrDefault(selectedStage, 0) + 1);
				}

				long endTime = System.nanoTime();
				player.sendMessage("Simulation Results:");

				for (Entry<Integer, Integer> entry : stageDistribution.entrySet())
				{
					int stage = entry.getKey();
					int count = entry.getValue();
					double percentage = (double) count / numberOfSimulations * 100.0;
					float originalStageChance = 0.0F;

					for (Entry<Byte, List<AdenLabStageHolder>> stages : holder.getOptions().get(optionIndex).entrySet())
					{
						for (AdenLabStageHolder stageHolder : stages.getValue())
						{
							if (stageHolder.getStageLevel() == stage)
							{
								originalStageChance = stageHolder.getStageChance() * 100.0F;
							}
						}
					}

					player.sendMessage("Stage " + stage + " (" + String.format("%.2f", originalStageChance) + "%): Selected [" + count + "] times or " + String.format("%.2f", percentage) + "% (" + String.format("%+.2f", percentage - originalStageChance) + ") of the draws.");
				}

				long elapsedTimeMs = (endTime - startTime) / 1000000L;
				player.sendMessage("Total simulation time: " + elapsedTimeMs + " ms");
			}

			return (byte) result;
		}
		if (holder.getGameType() == AdenLabGameType.NORMAL)
		{
			byte totalCardCount = holder.getCardCount();
			byte openedCardCount = (byte) player.getAdenLabNormalGameOpenedCardsCount(bossId);
			if (openedCardCount >= totalCardCount)
			{
				LOGGER.warning("CHEAT attempt! Player" + player.getName() + "[" + player.getObjectId() + "] tried to open a card that should not exist: total cards=" + totalCardCount + " <= opened cards=" + openedCardCount);
				return 0;
			}

			if (totalCardCount - openedCardCount == 1)
			{
				return 1;
			}

			byte totalCards = holder.getCardCount();
			byte openedCards = (byte) Math.min(player.getAdenLabNormalGameOpenedCardsCount(bossId), 5);
			float cardProgressionBonus = (float) openedCards / totalCards * (25.0F - successRate);
			finalSuccessRate = Math.min(successRate + bonusChance + cardProgressionBonus, 100.0F);
		}
		else
		{
			int stageIndex = player.getAdenLabCurrentTranscendLevel(bossId) + 1;

			for (Entry<Byte, List<AdenLabStageHolder>> stages : holder.getOptions().get(optionIndex).entrySet())
			{
				for (AdenLabStageHolder stageHolderx : stages.getValue())
				{
					if (stageHolderx.getStageLevel() == stageIndex)
					{
						finalSuccessRate = stageHolderx.getStageChance() * 100.0F;
					}
				}
			}
		}

		return (byte) (random <= finalSuccessRate ? 1 : 0);
	}

	private static int getWeightedResult(Player player, AdenLabHolder holder, byte optionIndex, boolean isSimulation)
	{
		List<Float> stageWeights = new ArrayList<>();
		float totalWeight = 0.0F;

		for (Entry<Byte, List<AdenLabStageHolder>> entry : holder.getOptions().get(optionIndex).entrySet())
		{
			for (AdenLabStageHolder stageHolder : entry.getValue())
			{
				float originalStageChance = stageHolder.getStageChance() * 100.0F;
				stageWeights.add(originalStageChance);
				totalWeight += originalStageChance;
			}
		}

		float randomWeight = (float) (Rnd.nextDouble() * totalWeight);
		float cumulativeWeight = 0.0F;

		for (int i = 0; i < stageWeights.size(); i++)
		{
			cumulativeWeight += stageWeights.get(i);
			if (randomWeight <= cumulativeWeight)
			{
				return (byte) (i + 1);
			}
		}

		return 1;
	}

	private static void takeItemsAndUpdateInventory(Player player, Map<Item, Long> feeItemsMap, long adenaAmount)
	{
		if (feeItemsMap != null && !feeItemsMap.isEmpty())
		{
			if (adenaAmount > 0L)
			{
				player.getInventory().reduceAdena(ItemProcessType.ADENLAB, adenaAmount, player, null);
				InventoryUpdate iuAdena = new InventoryUpdate();
				if (player.getInventory().getAdena() - adenaAmount > 0L)
				{
					iuAdena.addModifiedItem(player.getInventory().getAdenaInstance());
				}
				else
				{
					iuAdena.addRemovedItem(player.getInventory().getAdenaInstance());
				}

				player.sendPacket(iuAdena);
			}

			for (Entry<Item, Long> entry : feeItemsMap.entrySet())
			{
				Item item = entry.getKey();
				long feeAmount = entry.getValue();
				player.getInventory().destroyItemByItemId(ItemProcessType.ADENLAB, item.getId(), feeAmount, player, null);
				InventoryUpdate iuItem = new InventoryUpdate();
				if (item.getCount() - feeAmount > 0L)
				{
					iuItem.addModifiedItem(item);
				}
				else
				{
					iuItem.addRemovedItem(item);
				}

				player.sendPacket(iuItem);
			}

			player.updateAdenaAndWeight();
		}
		else
		{
			if (adenaAmount > 0L)
			{
				player.getInventory().reduceAdena(ItemProcessType.ADENLAB, adenaAmount, player, null);
				InventoryUpdate iuAdena = new InventoryUpdate();
				if (player.getInventory().getAdena() - adenaAmount > 0L)
				{
					iuAdena.addModifiedItem(player.getInventory().getAdenaInstance());
				}
				else
				{
					iuAdena.addRemovedItem(player.getInventory().getAdenaInstance());
				}

				player.sendPacket(iuAdena);
				player.updateAdenaAndWeight();
			}
		}
	}

	public static Map<Item, Long> getFeeItemsFromCache(Player player, Map<Integer, Object[]> feeCache, int feeIndex)
	{
		Map<Item, Long> feeItemsMap = new HashMap<>();
		Object[] itemFee = feeCache.get(feeIndex);
		if (itemFee != null && itemFee.length == 2)
		{
			try
			{
				int itemId = (Integer) itemFee[0];
				long itemAmount = (Long) itemFee[1];
				Item item = player.getInventory().getItemByItemId(itemId);
				if (item != null)
				{
					feeItemsMap.put(item, itemAmount);
				}
				else
				{
					LOGGER.warning("Item with ID " + itemId + " not found in inventory. Skipping.");
				}
			}
			catch (ClassCastException var9)
			{
				LOGGER.warning("Invalid fee cache structure for index " + feeIndex + ". Expected (Integer, Long).");
			}
		}
		else
		{
			LOGGER.warning("Fee index " + feeIndex + " not found in cache.");
		}

		return feeItemsMap;
	}

	public static void checkPlayerSkills(Player player)
	{
		AdenLaboratoryData adenLabData = AdenLaboratoryData.getInstance();

		for (Entry<Byte, Map<Integer, AdenLabHolder>> bossEntry : adenLabData.getAllAdenLabData().entrySet())
		{
			byte bossId = bossEntry.getKey();
			int currentUnlockedPage = player.getAdenLabCurrentlyUnlockedPage(bossId);
			processNormalSkills(player, bossId, currentUnlockedPage);
			processSpecialSkills(player, bossId, bossEntry.getValue());
			processTranscendentSkills(player, bossId, bossEntry.getValue());
		}
	}

	public static void giveAdenLabSkills(Player player, byte bossId, byte pageIndex, byte optionIndex, int stageLevel)
	{
		for (AdenLabStageHolder stageHolder : AdenLaboratoryData.getInstance().getAdenLabDataByPageIndex(bossId, pageIndex).getStageHolderListByLevel(optionIndex, stageLevel))
		{
			if (stageHolder.getStageLevel() == stageLevel)
			{
				List<AdenLabSkillHolder> skills = stageHolder.getSkills();
				if (skills != null && !skills.isEmpty())
				{
					addSkillIfNeeded(player, skills.get(optionIndex - 1).getId(), skills.get(optionIndex - 1).getLvl());
				}
			}
		}
	}

	private static void processNormalSkills(Player player, byte bossId, int currentUnlockedPage)
	{
		for (int[] skill : AdenLaboratoryData.getInstance().getNormalStageSkillsUpToPage(bossId, (byte) currentUnlockedPage))
		{
			addSkillIfNeeded(player, skill[0], skill[1]);
		}
	}

	private static void processSpecialSkills(Player player, byte bossId, Map<Integer, AdenLabHolder> adenLabHolders)
	{
		Map<Byte, Map<Byte, Integer>> confirmedStages = player.getAdenLabSpecialGameStagesConfirmedOptions().getOrDefault(bossId, Collections.emptyMap());

		for (int pageIndex : AdenLaboratoryData.getInstance().getSpecialStageIndicesByBossId(bossId))
		{
			AdenLabHolder holder = adenLabHolders.get(pageIndex);
			if (holder != null && holder.getGameType() == AdenLabGameType.SPECIAL)
			{
				confirmedStages.getOrDefault((byte) pageIndex, Collections.emptyMap()).forEach((optionIndex, stageLevel) -> processStageSkills(player, holder, optionIndex, stageLevel));
			}
		}
	}

	private static void processStageSkills(Player player, AdenLabHolder holder, byte optionIndex, int stageLevel)
	{
		List<AdenLabStageHolder> stageHolders = holder.getStageHolderListByLevel(optionIndex, stageLevel);
		if (stageHolders != null && !stageHolders.isEmpty())
		{
			for (AdenLabStageHolder stageHolder : stageHolders)
			{
				if (stageHolder.getStageLevel() == stageLevel)
				{
					List<AdenLabSkillHolder> skills = stageHolder.getSkills();
					if (skills != null && !skills.isEmpty())
					{
						addSkillIfNeeded(player, skills.get(optionIndex - 1).getId(), skills.get(optionIndex - 1).getLvl());
					}
				}
			}
		}
	}

	private static void processTranscendentSkills(Player player, byte bossId, Map<Integer, AdenLabHolder> adenLabHolderMap)
	{
		byte transcendentLevel = (byte) player.getAdenLabCurrentTranscendLevel(bossId);

		for (Entry<Integer, AdenLabHolder> entry : adenLabHolderMap.entrySet())
		{
			if (entry.getValue().getGameType() == AdenLabGameType.INCREDIBLE)
			{
				AdenLabHolder holder = entry.getValue();

				for (Entry<Byte, Map<Byte, List<AdenLabStageHolder>>> option : holder.getOptions().entrySet())
				{
					List<AdenLabStageHolder> stageList = option.getValue().get(transcendentLevel);
					if (stageList != null && !stageList.isEmpty())
					{
						stageList.forEach(stageHolder -> stageHolder.getSkills().forEach(skill -> {
							int skillid = skill.getId();
							int skillLevel = skill.getLvl();
							addSkillIfNeeded(player, skillid, skillLevel);
						}));
					}
				}
			}
		}
	}

	private static void addSkillIfNeeded(Player player, int skillId, int skillLevel)
	{
		Skill existingSkill = player.getKnownSkill(skillId);
		if (existingSkill == null || existingSkill.getLevel() != skillLevel)
		{
			player.addSkill(SkillData.getInstance().getSkill(skillId, skillLevel), false);
		}
	}

	public static void deletePlayerSkills(Player player, byte bossId)
	{
		for (Entry<Integer, AdenLabHolder> entry : AdenLaboratoryData.getInstance().getAdenLabData(bossId).entrySet())
		{
			AdenLabHolder holder = entry.getValue();

			for (Entry<Byte, Map<Byte, List<AdenLabStageHolder>>> optionEntries : holder.getOptions().entrySet())
			{
				Map<Byte, List<AdenLabStageHolder>> stageMap = optionEntries.getValue();

				for (Entry<Byte, List<AdenLabStageHolder>> stageHolder : stageMap.entrySet())
				{
					List<AdenLabStageHolder> data = stageHolder.getValue();
					if (data != null && !data.isEmpty())
					{
						data.forEach(dataHolder -> dataHolder.getSkills().forEach(skill -> {
							int skillid = skill.getId();
							int skillLevel = skill.getLvl();
							if (player.getKnownSkill(skillid) != null)
							{
								Skill toRemove = SkillData.getInstance().getSkill(skillid, skillLevel);
								player.removeSkill(toRemove);
							}
						}));
					}
				}
			}
		}
	}

	public static void processRequestAdenLabBossInfo(Player activeChar, byte bossId)
	{
		Map<Byte, Map<Integer, AdenLabHolder>> holder = AdenLaboratoryData.getInstance().getAllAdenLabData();
		if (holder != null && holder.containsKey(bossId))
		{
			processTranscendentProbabilities(activeChar, bossId);
			activeChar.sendPacket(new ExAdenLabBossInfo(bossId, activeChar));
		}
		else
		{
			LOGGER.warning("Player " + activeChar.getName() + " [" + activeChar.getObjectId() + "] requested INFO for a non-existing boss with ID: " + bossId);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	public static void processRequestAdenLabBossList(Player player, List<Integer> bossList)
	{
		player.addRequest(new AdenLabRequest(player));
		Map<Byte, Map<Integer, AdenLabHolder>> holder = AdenLaboratoryData.getInstance().getAllAdenLabData();
		if (holder != null && !holder.isEmpty())
		{
			for (int bossId : bossList)
			{
				if (!holder.containsKey((byte) bossId))
				{
					player.removeRequest(AdenLabRequest.class);
					LOGGER.warning("Player " + player.getName() + " [" + player.getObjectId() + "] requested the wrong BOSS LIST, specifically missing a boss with ID: " + bossId);
					return;
				}
			}

			player.removeRequest(AdenLabRequest.class);
			player.sendPacket(new ExAdenLabBossList(bossList));
		}
		else
		{
			player.removeRequest(AdenLabRequest.class);
		}
	}

	public static void processRequestAdenLabBossUnlock(Player player, int bossId)
	{
		player.addRequest(new AdenLabRequest(player));
		Map<Byte, Map<Integer, AdenLabHolder>> adenLabHolder = AdenLaboratoryData.getInstance().getAllAdenLabData();
		if (adenLabHolder != null && adenLabHolder.containsKey((byte) bossId))
		{
			player.removeRequest(AdenLabRequest.class);
			player.sendPacket(new ExAdenLabBossUnlock(bossId, false));
		}
		else
		{
			player.removeRequest(AdenLabRequest.class);
			LOGGER.warning("Player " + player.getName() + " [" + player.getObjectId() + "] tried to access missing data for boss with ID " + bossId);
		}
	}

	@SuppressWarnings("unchecked")
	public static void processRequestAdenLabNormalPlay(Player player, byte bossId, byte pageIndex, int feeIndex)
	{
		AdenLabHolder adenLabHolder = AdenLaboratoryData.getInstance().getAdenLabDataByPageIndex(bossId, pageIndex);
		if (adenLabHolder == null)
		{
			LOGGER.warning("Player " + player.getName() + " [" + player.getObjectId() + "] tried to access missing data for boss with ID " + bossId + " and page with ID " + pageIndex);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			player.addRequest(new AdenLabRequest(player));
			long adenaCount = AdenLaboratoryConfig.ADENLAB_NORMAL_ADENA_FEE_AMOUNT;
			if (player.getInventory().getAdena() < adenaCount)
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				player.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA);
				player.removeRequest(AdenLabRequest.class);
			}
			else
			{
				Map<Item, Long> feeItemsMap = getFeeItemsFromCache(player, AdenLaboratoryConfig.ADENLAB_NORMAL_ROLL_FEE_TYPE_CACHE, feeIndex);
				if (!feeItemsMap.isEmpty())
				{
					for (Entry<Item, Long> entry : feeItemsMap.entrySet())
					{
						Item item = entry.getKey();
						long requiredAmount = entry.getValue();
						if (item == null || item.getCount() < requiredAmount)
						{
							player.sendPacket(ActionFailed.STATIC_PACKET);
							player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
							player.removeRequest(AdenLabRequest.class);
							return;
						}
					}
				}

				takeItemsAndUpdateInventory(player, (Map<Item, Long>) (feeItemsMap.isEmpty() ? new HashMap<>() : feeItemsMap), adenaCount);
				byte result = calculateSuccess(bossId, pageIndex, (byte) 1, player, false, 0L);
				if (result == 0)
				{
					player.incrementAdenLabNormalGameOpenedCardsCount(bossId);
				}
				else
				{
					if (player.getAdenLabCurrentlyUnlockedPage(bossId) == pageIndex)
					{
						player.incrementAdenLabCurrentPage(bossId);
					}

					player.setAdenLabNormalGameOpenedCardsCount(bossId, 0);
					giveAdenLabSkills(player, bossId, pageIndex, (byte) 1, 1);
				}

				player.removeRequest(AdenLabRequest.class);
				player.sendPacket(new ExAdenLabNormalPlay(bossId, pageIndex, result));
			}
		}
	}

	public static void processRequestAdenLabNormalSlot(Player player, int bossId, int pageIndex)
	{
		player.addRequest(new AdenLabRequest(player));
		AdenLabHolder adenLabHolder = AdenLaboratoryData.getInstance().getAdenLabDataByPageIndex(bossId, pageIndex);
		if (adenLabHolder == null)
		{
			player.removeRequest(AdenLabRequest.class);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			player.removeRequest(AdenLabRequest.class);
			player.sendPacket(new ExAdenLabNormalSlot(bossId, pageIndex, (byte) (adenLabHolder.getCardCount() - player.getAdenLabNormalGameOpenedCardsCount((byte) bossId))));
		}
	}

	public static void processRequestAdenLabSpecialFix(Player player, byte bossId, byte pageIndex, int feeIndex)
	{
		player.addRequest(new AdenLabRequest(player));
		long adenaCount = AdenLaboratoryConfig.ADENLAB_SPECIAL_CONFIRM_FEE;
		if (player.getInventory().getAdena() < adenaCount)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			player.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA);
			player.removeRequest(AdenLabRequest.class);
			player.sendPacket(new ExAdenLabSpecialFix(bossId, pageIndex, false));
		}
		else
		{
			Map<Byte, Map<Byte, Integer>> drawnOptionsMap = player.getAdenLabSpecialGameStagesDrawnOptions().get(bossId);
			if (drawnOptionsMap == null)
			{
				drawnOptionsMap = new HashMap<>();
			}

			Map<Byte, Integer> drawnOptions = drawnOptionsMap.get(pageIndex);
			if (drawnOptions == null)
			{
				drawnOptions = new HashMap<>();
			}

			Iterator<Entry<Byte, Integer>> iterator = drawnOptions.entrySet().iterator();

			while (iterator.hasNext())
			{
				Entry<Byte, Integer> optionsMap = iterator.next();
				byte optionIndex = optionsMap.getKey();
				int stageLevel = optionsMap.getValue();
				player.setAdenLabSpecialGameConfirmedOptionsIndividual(bossId, pageIndex, optionIndex, stageLevel);
				giveAdenLabSkills(player, bossId, pageIndex, optionIndex, stageLevel);
				iterator.remove();
				player.setAdenLabSpecialGameDrawnOptionsIndividual(bossId, pageIndex, optionsMap.getKey(), -1);
			}

			if (player.getAdenLabCurrentlyUnlockedPage(bossId) == pageIndex)
			{
				player.incrementAdenLabCurrentPage(bossId);
				calculateAdenLabCombatPower(player);
			}

			takeItemsAndUpdateInventory(player, new HashMap<>(), adenaCount);
			player.removeRequest(AdenLabRequest.class);
			player.sendPacket(new ExAdenLabSpecialFix(bossId, pageIndex, true));
		}
	}

	public static void processRequestAdenLabSpecialPlay(Player player, byte bossId, byte pageIndex, int feeIndex)
	{
		player.addRequest(new AdenLabRequest(player));
		AdenLabHolder adenLabHolder = AdenLaboratoryData.getInstance().getAdenLabDataByPageIndex(bossId, pageIndex);
		if (adenLabHolder == null)
		{
			player.removeRequest(AdenLabRequest.class);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			long adenaCount = AdenLaboratoryConfig.ADENLAB_SPECIAL_RESEARCH_FEE;
			if (player.getInventory().getAdena() < adenaCount)
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				player.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA);
				player.removeRequest(AdenLabRequest.class);
			}
			else
			{
				Map<Integer, Integer> drawnOptions = new HashMap<>();

				for (int i = 1; i <= adenLabHolder.getOptions().size(); i++)
				{
					int result = calculateSuccess(bossId, pageIndex, (byte) i, player, false, 0L);
					drawnOptions.put(i, result);
				}

				takeItemsAndUpdateInventory(player, new HashMap<>(), adenaCount);

				for (Entry<Integer, Integer> temp : drawnOptions.entrySet())
				{
					player.setAdenLabSpecialGameDrawnOptionsIndividual(bossId, pageIndex, temp.getKey().byteValue(), temp.getValue());
				}

				player.removeRequest(AdenLabRequest.class);
				player.sendPacket(new ExAdenLabSpecialPlay(bossId, pageIndex, (byte) drawnOptions.size(), drawnOptions));
			}
		}
	}

	public static void processRequestAdenLabSpecialProbability(Player player, int bossId, int pageIndex)
	{
		player.addRequest(new AdenLabRequest(player));
		AdenLabHolder adenLabHolder = AdenLaboratoryData.getInstance().getAdenLabDataByPageIndex(bossId, pageIndex);
		if (adenLabHolder == null)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			player.removeRequest(AdenLabRequest.class);
		}
		else
		{
			Map<Integer, List<Integer>> probMap = new ConcurrentHashMap<>();
			int counter = 0;

			for (Entry<Byte, Map<Byte, List<AdenLabStageHolder>>> option : adenLabHolder.getOptions().entrySet())
			{
				List<Integer> options = new ArrayList<>();

				for (List<AdenLabStageHolder> stages : option.getValue().values())
				{
					for (AdenLabStageHolder stage : stages)
					{
						int levelIndex = stage.getStageLevel() - 1;
						int chance = Math.round(stage.getStageChance() * 10000.0F);

						while (options.size() <= levelIndex)
						{
							options.add(0);
						}

						options.set(levelIndex, chance);
					}
				}

				probMap.put(counter, options);
				counter++;
			}

			player.removeRequest(AdenLabRequest.class);
			player.sendPacket(new ExAdenLabSpecialProb(bossId, pageIndex, probMap));
		}
	}

	public static void processRequestAdenLabSpecialSlot(Player player, int bossId, int pageIndex)
	{
		player.addRequest(new AdenLabRequest(player));
		Map<Byte, Map<Byte, Integer>> drawnOptionsMap = player.getAdenLabSpecialGameStagesDrawnOptions().get((byte) bossId);
		if (drawnOptionsMap == null)
		{
			drawnOptionsMap = new HashMap<>();
		}

		Map<Byte, Integer> drawnOptions = drawnOptionsMap.get((byte) pageIndex);
		if (drawnOptions == null)
		{
			drawnOptions = new HashMap<>();
		}

		for (Entry<Byte, Integer> stage : drawnOptions.entrySet())
		{
			drawnOptions.putIfAbsent(stage.getKey(), stage.getValue());
		}

		Map<Byte, Map<Byte, Integer>> confirmedOptionsMap = player.getAdenLabSpecialGameStagesConfirmedOptions().get((byte) bossId);
		if (confirmedOptionsMap == null)
		{
			confirmedOptionsMap = new HashMap<>();
		}

		Map<Byte, Integer> confirmedOptions = confirmedOptionsMap.get((byte) pageIndex);
		if (confirmedOptions == null)
		{
			confirmedOptions = new HashMap<>();
		}

		for (Entry<Byte, Integer> stage : confirmedOptions.entrySet())
		{
			confirmedOptions.putIfAbsent(stage.getKey(), stage.getValue());
		}

		player.removeRequest(AdenLabRequest.class);
		player.sendPacket(new ExAdenLabSpecialSlot(bossId, pageIndex, drawnOptions, confirmedOptions));
	}

	@SuppressWarnings("unchecked")
	public static void processRequestAdenLabTranscendentEnchant(Player player, byte bossId, int feeIndex)
	{
		if (!AdenLaboratoryConfig.ADENLAB_INCREDIBLE_ROLL_FEE_TYPE_CACHE.containsKey(feeIndex))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			player.sendPacket(SystemMessageId.THE_FUNCTION_IS_UNAVAILABLE);
			LOGGER.warning("Player " + player.getName() + " [" + player.getObjectId() + "] sent unknown fee index [" + feeIndex + "].");
		}
		else
		{
			player.addRequest(new AdenLabRequest(player));
			int currentTranscendLevel = player.getAdenLabCurrentTranscendLevel(bossId);
			int currentUnlockedPage = player.getAdenLabCurrentlyUnlockedPage(bossId);

			for (byte pageIndex : AdenLaboratoryData.getInstance().getTranscendentStageIndicesByBossId(bossId))
			{
				if (currentUnlockedPage < pageIndex)
				{
					player.removeRequest(AdenLabRequest.class);
					return;
				}
			}

			Map<Item, Long> feeItemsMap = getFeeItemsFromCache(player, AdenLaboratoryConfig.ADENLAB_INCREDIBLE_ROLL_FEE_TYPE_CACHE, feeIndex);
			if (feeItemsMap.isEmpty())
			{
				player.removeRequest(AdenLabRequest.class);
				player.sendPacket(SystemMessageId.NOT_WORKING_PLEASE_TRY_AGAIN_LATER);
				LOGGER.warning("Missing or incorrectly set incredible stage fees.");
			}
			else
			{
				for (Entry<Item, Long> entry : feeItemsMap.entrySet())
				{
					Item item = entry.getKey();
					long requiredAmount = entry.getValue();
					if (item == null || item.getCount() < requiredAmount)
					{
						player.sendPacket(ActionFailed.STATIC_PACKET);
						player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
						player.removeRequest(AdenLabRequest.class);
						return;
					}
				}

				takeItemsAndUpdateInventory(player, (Map<Item, Long>) (feeItemsMap.isEmpty() ? new HashMap<>() : feeItemsMap), 0L);
				byte result = calculateSuccess(bossId, (byte) 25, (byte) 1, player, false, 0L);
				if (result == 1)
				{
					player.incrementAdenLabTranscendLevel(bossId);
					int newLevel = currentTranscendLevel + 1;
					giveAdenLabSkills(player, bossId, (byte) 25, (byte) 1, newLevel);
					if (newLevel >= 2)
					{
						player.getInventory().applyItemSkills();
						player.getStat().recalculateStats(false);
					}

					calculateAdenLabCombatPower(player);
					Broadcast.toAllOnlinePlayers(new ExAdenLabTranscendAnnounce(player.getName(), bossId, (byte) newLevel));
				}

				player.removeRequest(AdenLabRequest.class);
				player.sendPacket(new ExAdenLabTranscendEnchant(bossId, result));
			}
		}
	}

	public static void processTranscendentProbabilities(Player player, byte bossId)
	{
		player.addRequest(new AdenLabRequest(player));
		AdenLabHolder adenLabHolder = AdenLaboratoryData.getInstance().getAdenLabDataByPageIndex(bossId, 25);
		if (adenLabHolder == null)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			player.removeRequest(AdenLabRequest.class);
		}
		else
		{
			List<Integer> options = new ArrayList<>();

			for (Entry<Byte, Map<Byte, List<AdenLabStageHolder>>> optionIndices : adenLabHolder.getOptions().entrySet())
			{
				for (List<AdenLabStageHolder> stages : optionIndices.getValue().values())
				{
					for (AdenLabStageHolder holder : stages)
					{
						int levelIndex = holder.getStageLevel() - 1;
						int chance = Math.round(holder.getStageChance() * 10000.0F);

						while (options.size() <= levelIndex)
						{
							options.add(0);
						}

						options.set(levelIndex, chance);
					}
				}
			}

			player.removeRequest(AdenLabRequest.class);
			player.sendPacket(new ExAdenLabTranscendProb(bossId, options));
		}
	}

	public static void ensureAdenLabTableExists()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			DatabaseMetaData metaData = con.getMetaData();
			ResultSet result = metaData.getTables(null, null, "aden_laboratory", null);
			if (!result.next())
			{
				try (Statement statement = con.createStatement())
				{
					statement.executeUpdate("CREATE TABLE aden_laboratory (charId INT NOT NULL, bossId INT NOT NULL, unlockedPage INT NOT NULL, openedCardsCount INT NOT NULL, specialDrawnOptions TEXT, specialConfirmedOptions TEXT, transcendLevel INT NOT NULL, UNIQUE (charId, bossId) );");
					LOGGER.info("Missing 'aden_laboratory' table was successfully created.");
				}
			}
		}
		catch (SQLException var10)
		{
			LOGGER.log(Level.SEVERE, "Error creating 'aden_laboratory' table.", var10);
		}
	}

	public static void storeAdenLabBossData(Player player)
	{
		for (Entry<Byte, Map<Integer, AdenLabHolder>> entry : AdenLaboratoryData.getInstance().getAllAdenLabData().entrySet())
		{
			byte bossId = entry.getKey();
			String drawnOptions = transformSpecialGameOptionsToString(player.getAdenLabSpecialGameStagesDrawnOptions());
			String confirmedOptions = transformSpecialGameOptionsToString(player.getAdenLabSpecialGameStagesConfirmedOptions());

			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("REPLACE INTO aden_laboratory (charId,bossId,unlockedPage,openedCardsCount,specialDrawnOptions,specialConfirmedOptions,transcendLevel) VALUES (?,?,?,?,?,?,?)");)
			{
				statement.setInt(1, player.getObjectId());
				statement.setInt(2, bossId);
				statement.setInt(3, player.getAdenLabCurrentlyUnlockedPage(bossId));
				statement.setInt(4, player.getAdenLabNormalGameOpenedCardsCount(bossId));
				statement.setString(5, drawnOptions);
				statement.setString(6, confirmedOptions);
				statement.setInt(7, player.getAdenLabCurrentTranscendLevel(bossId));
				statement.execute();
			}
			catch (Exception var14)
			{
				LOGGER.log(Level.SEVERE, "Could not update Aden Lab data for player: " + player.getObjectId(), var14);
			}
		}
	}

	public static void restorePlayerData(Player player)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM aden_laboratory WHERE charId=?");)
		{
			ps.setInt(1, player.getObjectId());

			try (ResultSet statement = ps.executeQuery())
			{
				while (statement.next())
				{
					int bossId = statement.getInt("bossId");
					int unlockedPage = statement.getInt("unlockedPage");
					int openedCardsCount = statement.getInt("openedCardsCount");
					String specialDrawnOptions = statement.getString("specialDrawnOptions");
					String specialConfirmedOptions = statement.getString("specialConfirmedOptions");
					int transcendLevel = statement.getInt("transcendLevel");
					setPlayerData(player, (byte) bossId, (byte) unlockedPage, openedCardsCount, specialDrawnOptions, specialConfirmedOptions, transcendLevel);
				}
			}
		}
		catch (Exception var16)
		{
			LOGGER.log(Level.SEVERE, "Could not restore Aden Lab data for player " + player.getName() + "[" + player.getObjectId() + "].", var16);
		}
	}

	private static void setPlayerData(Player player, byte bossId, byte unlockedPage, int openedCardsCount, String specialDrawnOptions, String specialConfirmedOptions, int transcendLevel)
	{
		player.setAdenLabCurrentlyUnlockedPage(bossId, unlockedPage);
		player.setAdenLabNormalGameOpenedCardsCount(bossId, openedCardsCount);
		player.setAdenLabSpecialGameDrawnOptionsBulk(getSpecialGameOptionsFromString(specialDrawnOptions));
		player.setAdenLabSpecialGameConfirmedOptionsBulk(getSpecialGameOptionsFromString(specialConfirmedOptions));
		player.setAdenLabCurrentTranscendLevel(bossId, transcendLevel);
	}

	public static void deletePlayerData(Player player)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("DELETE FROM aden_laboratory WHERE charId=?");)
		{
			statement.setInt(1, player.getObjectId());
			statement.execute();
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.SEVERE, "Could not delete Aden Lab data for player: " + player.getObjectId(), var9);
		}
	}

	private static String transformSpecialGameOptionsToString(Map<Byte, Map<Byte, Map<Byte, Integer>>> options)
	{
		StringBuilder sb = new StringBuilder();

		for (Entry<Byte, Map<Byte, Map<Byte, Integer>>> bossEntry : options.entrySet())
		{
			byte bossId = bossEntry.getKey();

			for (Entry<Byte, Map<Byte, Integer>> pageEntry : bossEntry.getValue().entrySet())
			{
				byte pageIndex = pageEntry.getKey();

				for (Entry<Byte, Integer> optionEntry : pageEntry.getValue().entrySet())
				{
					byte optionIndex = optionEntry.getKey();
					int stageLevel = optionEntry.getValue();
					if (!sb.isEmpty())
					{
						sb.append(";");
					}

					sb.append(bossId).append(",").append(pageIndex).append(",").append(optionIndex).append(",").append(stageLevel);
				}
			}
		}

		return sb.toString();
	}

	public static Map<Byte, Map<Byte, Map<Byte, Integer>>> getSpecialGameOptionsFromString(String data)
	{
		Map<Byte, Map<Byte, Map<Byte, Integer>>> options = new HashMap<>();
		if (data != null && !data.isEmpty())
		{
			String[] entries = data.split(";");

			for (String entry : entries)
			{
				String[] parts = entry.split(",");
				if (parts.length == 4)
				{
					try
					{
						byte bossId = Byte.parseByte(parts[0]);
						byte pageIndex = Byte.parseByte(parts[1]);
						byte optionIndex = Byte.parseByte(parts[2]);
						int stageLevel = Integer.parseInt(parts[3]);
						options.computeIfAbsent(bossId, _ -> new HashMap<>()).computeIfAbsent(pageIndex, _ -> new HashMap<>()).put(optionIndex, stageLevel);
					}
					catch (NumberFormatException var12)
					{
						LOGGER.log(Level.WARNING, "Skipping invalid entry: " + entry);
					}
				}
			}

			return options;
		}
		return options;
	}
}
