package net.sf.l2jdev.gameserver.data.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.enums.EvolveLevel;
import net.sf.l2jdev.gameserver.data.xml.NpcData;
import net.sf.l2jdev.gameserver.data.xml.PetDataTable;
import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.model.PetData;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.actor.holders.creature.PetEvolveHolder;
import net.sf.l2jdev.gameserver.model.actor.instance.Guardian;
import net.sf.l2jdev.gameserver.model.actor.instance.Pet;
import net.sf.l2jdev.gameserver.model.actor.instance.Servitor;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class CharSummonTable
{
	private static final Logger LOGGER = Logger.getLogger(CharSummonTable.class.getName());
	private static final Map<Integer, Integer> _pets = new ConcurrentHashMap<>();
	private static final Map<Integer, Set<Integer>> _servitors = new ConcurrentHashMap<>();
	public static final String INIT_PET = "SELECT ownerId, item_obj_id FROM pets WHERE restore = 'true'";
	public static final String INIT_SUMMONS = "SELECT ownerId, summonId FROM character_summons";
	public static final String LOAD_SUMMON = "SELECT summonSkillId, summonId, curHp, curMp, time FROM character_summons WHERE ownerId = ?";
	public static final String REMOVE_SUMMON = "DELETE FROM character_summons WHERE ownerId = ? and summonId = ?";
	public static final String SAVE_SUMMON = "REPLACE INTO character_summons (ownerId,summonId,summonSkillId,curHp,curMp,time) VALUES (?,?,?,?,?,?)";

	public Map<Integer, Integer> getPets()
	{
		return _pets;
	}

	public Map<Integer, Set<Integer>> getServitors()
	{
		return _servitors;
	}

	public void init()
	{
		if (PlayerConfig.RESTORE_SERVITOR_ON_RECONNECT)
		{
			try (Connection con = DatabaseFactory.getConnection(); Statement s = con.createStatement(); ResultSet rs = s.executeQuery("SELECT ownerId, summonId FROM character_summons");)
			{
				while (rs.next())
				{
					_servitors.computeIfAbsent(rs.getInt("ownerId"), _ -> ConcurrentHashMap.newKeySet()).add(rs.getInt("summonId"));
				}
			}
			catch (Exception var19)
			{
				LOGGER.warning(this.getClass().getSimpleName() + ": Error while loading saved servitor: " + var19);
			}
		}

		if (PlayerConfig.RESTORE_PET_ON_RECONNECT)
		{
			try (Connection con = DatabaseFactory.getConnection(); Statement s = con.createStatement(); ResultSet rs = s.executeQuery("SELECT ownerId, item_obj_id FROM pets WHERE restore = 'true'");)
			{
				while (rs.next())
				{
					_pets.put(rs.getInt("ownerId"), rs.getInt("item_obj_id"));
				}
			}
			catch (Exception var15)
			{
				LOGGER.warning(this.getClass().getSimpleName() + ": Error while loading saved pet: " + var15);
			}
		}
	}

	public void removeServitor(Player player, int summonObjectId)
	{
		_servitors.computeIfPresent(player.getObjectId(), (_, v) -> {
			v.remove(summonObjectId);
			return !v.isEmpty() ? v : null;
		});

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM character_summons WHERE ownerId = ? and summonId = ?");)
		{
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, summonObjectId);
			ps.execute();
		}
		catch (SQLException var11)
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": Summon cannot be removed: " + var11);
		}
	}

	public void restorePet(Player player)
	{
		Item item = player.getInventory().getItemByObjectId(_pets.get(player.getObjectId()));
		if (item == null)
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": Null pet summoning item for: " + player);
		}
		else
		{
			PetEvolveHolder evolveData = player.getPetEvolve(item.getObjectId());
			PetData petData = evolveData.getEvolve() == EvolveLevel.None ? PetDataTable.getInstance().getPetDataByEvolve(item.getId(), evolveData.getEvolve()) : PetDataTable.getInstance().getPetDataByEvolve(item.getId(), evolveData.getEvolve(), evolveData.getIndex());
			if (petData == null)
			{
				LOGGER.warning(this.getClass().getSimpleName() + ": Null pet data for: " + player + " and summoning item: " + item);
			}
			else
			{
				NpcTemplate npcTemplate = NpcData.getInstance().getTemplate(petData.getNpcId());
				if (npcTemplate == null)
				{
					LOGGER.warning(this.getClass().getSimpleName() + ": Null pet NPC template for: " + player + " and pet Id:" + petData.getNpcId());
				}
				else
				{
					Pet pet = Pet.spawnPet(npcTemplate, player, item);
					if (pet == null)
					{
						LOGGER.warning(this.getClass().getSimpleName() + ": Null pet instance for: " + player + " and pet NPC template:" + npcTemplate);
					}
					else
					{
						player.setPet(pet);
						pet.setShowSummonAnimation(true);
						pet.setTitle(player.getName());
						if (!pet.isRespawned())
						{
							pet.setCurrentHp(pet.getMaxHp());
							pet.setCurrentMp(pet.getMaxMp());
							pet.getStat().setExp(pet.getExpForThisLevel());
							pet.setCurrentFed(pet.getMaxFed());
							pet.storeMe();
						}

						pet.setRunning();
						item.setEnchantLevel(pet.getLevel());
						pet.spawnMe(player.getX() + 50, player.getY() + 100, player.getZ());
						pet.startFeed();
					}
				}
			}
		}
	}

	public void restoreServitor(Player player)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT summonSkillId, summonId, curHp, curMp, time FROM character_summons WHERE ownerId = ?");)
		{
			ps.setInt(1, player.getObjectId());

			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					int summonObjId = rs.getInt("summonId");
					int skillId = rs.getInt("summonSkillId");
					int curHp = rs.getInt("curHp");
					int curMp = rs.getInt("curMp");
					int time = rs.getInt("time");
					this.removeServitor(player, summonObjId);
					Skill skill = SkillData.getInstance().getSkill(skillId, player.getSkillLevel(skillId));
					if (skill == null)
					{
						return;
					}

					skill.applyEffects(player, player);
					if (player.hasServitors())
					{
						Servitor servitor = null;
						Iterator<Summon> var12 = player.getServitors().values().iterator();

						while (true)
						{
							if (var12.hasNext())
							{
								Summon summon = var12.next();
								if (!(summon instanceof Servitor))
								{
									continue;
								}

								Servitor s = summon.asServitor();
								if (s.getReferenceSkill() != skillId)
								{
									continue;
								}

								servitor = s;
							}

							if (servitor != null)
							{
								servitor.setCurrentHp(curHp);
								servitor.setCurrentMp(curMp);
								servitor.setLifeTimeRemaining(time);
							}
							break;
						}
					}
				}
			}
		}
		catch (SQLException var21)
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": Servitor cannot be restored: " + var21);
		}
	}

	public void saveSummon(Servitor summon)
	{
		if (summon != null)
		{
			_servitors.computeIfAbsent(summon.getOwner().getObjectId(), _ -> ConcurrentHashMap.newKeySet()).add(summon.getObjectId());

			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("REPLACE INTO character_summons (ownerId,summonId,summonSkillId,curHp,curMp,time) VALUES (?,?,?,?,?,?)");)
			{
				ps.setInt(1, summon.getOwner().getObjectId());
				ps.setInt(2, summon.getObjectId());
				ps.setInt(3, summon.getReferenceSkill());
				ps.setInt(4, (int) Math.round(summon.getCurrentHp()));
				ps.setInt(5, (int) Math.round(summon.getCurrentMp()));
				ps.setInt(6, Math.max(0, summon.getLifeTimeRemaining()));
				ps.execute();
			}
			catch (Exception var10)
			{
				LOGGER.warning(this.getClass().getSimpleName() + ": Failed to store summon: " + summon + " from " + summon.getOwner() + ", error: " + var10);
			}
		}
	}

	public void storeGuardians(Player player)
	{
		Collection<Npc> summonedNpcs = player.getSummonedNpcs();
		if (!summonedNpcs.isEmpty())
		{
			List<Integer> summonedNpcIds = new LinkedList<>();

			for (Npc npc : summonedNpcs)
			{
				if (!npc.isDead() && npc.isSpawned() && npc instanceof Guardian && npc.getCloneObjId() == 0)
				{
					summonedNpcIds.add(npc.getId());
				}
			}

			if (!summonedNpcIds.isEmpty())
			{
				player.getVariables().setIntegerList("SUMMONED_GUARDIAN_NPC_IDS", summonedNpcIds);
			}
		}
	}

	public void restoreGuardians(Player player)
	{
		List<Integer> summonedNpcIds = player.getVariables().getIntegerList("SUMMONED_GUARDIAN_NPC_IDS");
		if (!summonedNpcIds.isEmpty())
		{
			player.getVariables().remove("SUMMONED_GUARDIAN_NPC_IDS");

			for (int npcId : summonedNpcIds)
			{
				NpcTemplate npcTemplate = NpcData.getInstance().getTemplate(npcId);
				if (npcTemplate == null)
				{
					LOGGER.warning(this.getClass().getSimpleName() + ": Spawn of the nonexisting NPC ID: " + npcId + ", skill ID:" + npcId);
					return;
				}

				int x = player.getX();
				int y = player.getY();
				int z = player.getZ();
				x += Rnd.nextBoolean() ? Rnd.get(0, 20) : Rnd.get(-20, 0);
				y += Rnd.nextBoolean() ? Rnd.get(0, 20) : Rnd.get(-20, 0);
				Guardian guardian = new Guardian(npcTemplate, player, false);
				guardian.setCurrentHp(guardian.getMaxHp());
				guardian.setCurrentMp(guardian.getMaxMp());
				guardian.setSummoner(player);
				player.addSummonedNpc(guardian);
				guardian.spawnMe(x, y, z);
				guardian.setInstance(player.getInstanceWorld());
				guardian.setRunning();
				guardian.scheduleDespawn(1200000L);
				guardian.setShowSummonAnimation(true);
				guardian.startAttackTask();
			}

			ThreadPool.schedule(() -> player.sendSkillList(), 3000L);
		}
	}

	public static CharSummonTable getInstance()
	{
		return CharSummonTable.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final CharSummonTable INSTANCE = new CharSummonTable();
	}
}
