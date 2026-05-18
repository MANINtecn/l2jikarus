package net.sf.l2jdev.gameserver.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.gameserver.data.xml.SpawnData;
import net.sf.l2jdev.gameserver.model.CombatFlag;
import net.sf.l2jdev.gameserver.model.FortSiegeSpawn;
import net.sf.l2jdev.gameserver.model.Spawn;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.item.enums.BodyPart;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.siege.Fort;
import net.sf.l2jdev.gameserver.model.siege.FortSiege;
import net.sf.l2jdev.gameserver.model.skill.CommonSkill;
import net.sf.l2jdev.gameserver.model.spawns.NpcSpawnTemplate;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class FortSiegeManager
{
	private static final Logger LOGGER = Logger.getLogger(FortSiegeManager.class.getName());
	public static final String FORTSIEGE_CONFIG_FILE = "./config/FortSiege.ini";
	private int _attackerMaxClans = 500;
	private Map<Integer, List<FortSiegeSpawn>> _commanderSpawnList;
	private Map<Integer, List<CombatFlag>> _flagList;
	private boolean _justToTerritory = true;
	private int _flagMaxCount = 1;
	private int _siegeClanMinLevel = 4;
	private int _siegeLength = 60;
	private int _countDownLength = 10;
	private int _suspiciousMerchantRespawnDelay = 180;
	private final Map<Integer, FortSiege> _sieges = new ConcurrentHashMap<>();

	protected FortSiegeManager()
	{
		this.load();
	}

	public void addSiegeSkills(Player character)
	{
		character.addSkill(CommonSkill.SEAL_OF_RULER.getSkill(), false);
		character.addSkill(CommonSkill.BUILD_HEADQUARTERS.getSkill(), false);
	}

	public void addCombatFlaglagSkills(Player character)
	{
		Clan clan = character.getClan();
		if (clan != null && clan.getLevel() >= this.getSiegeClanMinLevel() && FortManager.getInstance().getFortById(122).getSiege().isInProgress())
		{
			character.addSkill(CommonSkill.FLAG_DISPLAY.getSkill(), false);
			character.addSkill(CommonSkill.REMOTE_FLAG_DISPLAY.getSkill(), false);
			character.addSkill(CommonSkill.FLAG_POWER_FAST_RUN.getSkill(), false);
			character.addSkill(CommonSkill.FLAG_EQUIP.getSkill(), false);
			switch (character.getPlayerClass())
			{
				case DUELIST:
				case DREADNOUGHT:
				case TITAN:
				case GRAND_KHAVATARI:
				case FORTUNE_SEEKER:
				case MAESTRO:
				case DOOMBRINGER:
				case SOUL_HOUND:
				case DEATH_KIGHT_HUMAN:
				case DEATH_KIGHT_ELF:
				case DEATH_KIGHT_DARK_ELF:
					character.addSkill(CommonSkill.FLAG_POWER_WARRIOR.getSkill(), false);
					break;
				case PHOENIX_KNIGHT:
				case HELL_KNIGHT:
				case EVA_TEMPLAR:
				case SHILLIEN_TEMPLAR:
					character.addSkill(CommonSkill.FLAG_POWER_KNIGHT.getSkill(), false);
					break;
				case ADVENTURER:
				case WIND_RIDER:
				case GHOST_HUNTER:
					character.addSkill(CommonSkill.FLAG_POWER_ROGUE.getSkill(), false);
					break;
				case SAGITTARIUS:
				case MOONLIGHT_SENTINEL:
				case GHOST_SENTINEL:
				case TRICKSTER:
					character.addSkill(CommonSkill.FLAG_POWER_ARCHER.getSkill(), false);
					break;
				case ARCHMAGE:
				case SOULTAKER:
				case MYSTIC_MUSE:
				case STORM_SCREAMER:
					character.addSkill(CommonSkill.FLAG_POWER_MAGE.getSkill(), false);
					break;
				case ARCANA_LORD:
				case ELEMENTAL_MASTER:
				case SPECTRAL_MASTER:
					character.addSkill(CommonSkill.FLAG_POWER_SUMMONER.getSkill(), false);
					break;
				case CARDINAL:
				case EVA_SAINT:
				case SHILLIEN_SAINT:
					character.addSkill(CommonSkill.FLAG_POWER_HEALER.getSkill(), false);
					break;
				case HIEROPHANT:
					character.addSkill(CommonSkill.FLAG_POWER_ENCHANTER.getSkill(), false);
					break;
				case SWORD_MUSE:
				case SPECTRAL_DANCER:
					character.addSkill(CommonSkill.FLAG_POWER_BARD.getSkill(), false);
					break;
				case DOMINATOR:
				case DOOMCRYER:
					character.addSkill(CommonSkill.FLAG_POWER_SHAMAN.getSkill(), false);
			}
		}
	}

	public void removeCombatFlagSkills(Player character)
	{
		character.removeSkill(CommonSkill.FLAG_DISPLAY.getSkill());
		character.removeSkill(CommonSkill.REMOTE_FLAG_DISPLAY.getSkill());
		character.removeSkill(CommonSkill.FLAG_POWER_FAST_RUN.getSkill());
		character.removeSkill(CommonSkill.FLAG_EQUIP.getSkill());
		character.removeSkill(CommonSkill.FLAG_POWER_WARRIOR.getSkill());
		character.removeSkill(CommonSkill.FLAG_POWER_KNIGHT.getSkill());
		character.removeSkill(CommonSkill.FLAG_POWER_ROGUE.getSkill());
		character.removeSkill(CommonSkill.FLAG_POWER_ARCHER.getSkill());
		character.removeSkill(CommonSkill.FLAG_POWER_MAGE.getSkill());
		character.removeSkill(CommonSkill.FLAG_POWER_SUMMONER.getSkill());
		character.removeSkill(CommonSkill.FLAG_POWER_HEALER.getSkill());
		character.removeSkill(CommonSkill.FLAG_POWER_ENCHANTER.getSkill());
		character.removeSkill(CommonSkill.FLAG_POWER_BARD.getSkill());
		character.removeSkill(CommonSkill.FLAG_POWER_SHAMAN.getSkill());
		character.removeSkill(CommonSkill.FLAG_POWER_ENCHANTER.getSkill());
		character.removeSkill(CommonSkill.FLAG_EQUIP.getSkill());
	}

	public boolean checkIsRegistered(Clan clan, int fortid)
	{
		if (clan == null)
		{
			return false;
		}
		boolean register = false;

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT clan_id FROM fortsiege_clans where clan_id=? and fort_id=?");)
		{
			ps.setInt(1, clan.getId());
			ps.setInt(2, fortid);

			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					register = true;
				}
			}
		}
		catch (Exception var15)
		{
			LOGGER.log(Level.WARNING, "Exception: checkIsRegistered(): " + var15.getMessage(), var15);
		}

		return register;
	}

	public void removeSiegeSkills(Player character)
	{
		character.removeSkill(CommonSkill.SEAL_OF_RULER.getSkill());
		character.removeSkill(CommonSkill.BUILD_HEADQUARTERS.getSkill());
	}

	private void load()
	{
		Properties siegeSettings = new Properties();
		File file = new File("./config/FortSiege.ini");

		try (InputStream is = new FileInputStream(file))
		{
			siegeSettings.load(is);
		}
		catch (Exception var19)
		{
			LOGGER.log(Level.WARNING, "Error while loading Fort Siege Manager settings!", var19);
		}

		this._justToTerritory = Boolean.parseBoolean(siegeSettings.getProperty("JustToTerritory", "true"));
		this._attackerMaxClans = Integer.decode(siegeSettings.getProperty("AttackerMaxClans", "500"));
		this._flagMaxCount = Integer.decode(siegeSettings.getProperty("MaxFlags", "1"));
		this._siegeClanMinLevel = Integer.decode(siegeSettings.getProperty("SiegeClanMinLevel", "4"));
		this._siegeLength = Integer.decode(siegeSettings.getProperty("SiegeLength", "60"));
		this._countDownLength = Integer.decode(siegeSettings.getProperty("CountDownLength", "10"));
		this._suspiciousMerchantRespawnDelay = Integer.decode(siegeSettings.getProperty("SuspiciousMerchantRespawnDelay", "180"));
		this._commanderSpawnList = new ConcurrentHashMap<>();
		this._flagList = new ConcurrentHashMap<>();

		for (Fort fort : FortManager.getInstance().getForts())
		{
			List<FortSiegeSpawn> commanderSpawns = new CopyOnWriteArrayList<>();
			List<CombatFlag> flagSpawns = new CopyOnWriteArrayList<>();

			for (int i = 1; i < 5; i++)
			{
				String _spawnParams = siegeSettings.getProperty(fort.getName().replace(" ", "") + "Commander" + i, "");
				if (_spawnParams.isEmpty())
				{
					break;
				}

				StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");

				try
				{
					int x = Integer.parseInt(st.nextToken());
					int y = Integer.parseInt(st.nextToken());
					int z = Integer.parseInt(st.nextToken());
					int heading = Integer.parseInt(st.nextToken());
					int npc_id = Integer.parseInt(st.nextToken());
					commanderSpawns.add(new FortSiegeSpawn(fort.getResidenceId(), x, y, z, heading, npc_id, i));
				}
				catch (Exception var15)
				{
					LOGGER.warning("Error while loading commander(s) for " + fort.getName() + " fort.");
				}
			}

			this._commanderSpawnList.put(fort.getResidenceId(), commanderSpawns);

			for (int i = 1; i < 4; i++)
			{
				String _spawnParams = siegeSettings.getProperty(fort.getName().replace(" ", "") + "Flag" + i, "");
				if (_spawnParams.isEmpty())
				{
					break;
				}

				StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");

				try
				{
					int x = Integer.parseInt(st.nextToken());
					int y = Integer.parseInt(st.nextToken());
					int z = Integer.parseInt(st.nextToken());
					int flag_id = Integer.parseInt(st.nextToken());
					flagSpawns.add(new CombatFlag(fort.getResidenceId(), x, y, z, 0, flag_id));
				}
				catch (Exception var16)
				{
					LOGGER.warning("Error while loading flag(s) for " + fort.getName() + " fort.");
				}
			}

			this._flagList.put(fort.getResidenceId(), flagSpawns);
		}
	}

	public List<FortSiegeSpawn> getCommanderSpawnList(int fortId)
	{
		return this._commanderSpawnList.get(fortId);
	}

	public List<CombatFlag> getFlagList(int fortId)
	{
		return this._flagList.get(fortId);
	}

	public int getAttackerMaxClans()
	{
		return this._attackerMaxClans;
	}

	public int getFlagMaxCount()
	{
		return this._flagMaxCount;
	}

	public boolean canRegisterJustTerritory()
	{
		return this._justToTerritory;
	}

	public int getSuspiciousMerchantRespawnDelay()
	{
		return this._suspiciousMerchantRespawnDelay;
	}

	public FortSiege getSiege(WorldObject activeObject)
	{
		return this.getSiege(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}

	public FortSiege getSiege(int x, int y, int z)
	{
		for (Fort fort : FortManager.getInstance().getForts())
		{
			if (fort.getSiege().checkIfInZone(x, y, z))
			{
				return fort.getSiege();
			}
		}

		return null;
	}

	public int getSiegeClanMinLevel()
	{
		return this._siegeClanMinLevel;
	}

	public int getSiegeLength()
	{
		return this._siegeLength;
	}

	public int getCountDownLength()
	{
		return this._countDownLength;
	}

	public Collection<FortSiege> getSieges()
	{
		return this._sieges.values();
	}

	public FortSiege getSiege(int fortId)
	{
		return this._sieges.get(fortId);
	}

	public void addSiege(FortSiege fortSiege)
	{
		this._sieges.put(fortSiege.getFort().getResidenceId(), fortSiege);
	}

	public boolean isCombat(int itemId)
	{
		return itemId == 93331;
	}

	public boolean activateCombatFlag(Player player, Item item)
	{
		if (!this.checkIfCanPickup(player))
		{
			return false;
		}
		if (player.isMounted())
		{
			player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM);
		}
		else
		{
			player.getInventory().equipItem(item);
			InventoryUpdate iu = new InventoryUpdate();
			iu.addItem(item);
			player.sendInventoryUpdate(iu);
			player.broadcastUserInfo();
			player.setCombatFlagEquipped(true);
			this.addCombatFlaglagSkills(player);
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_EQUIPPED);
			sm.addItemName(item);
			player.sendPacket(sm);
		}

		return true;
	}

	public boolean checkIfCanPickup(Player player)
	{
		if (player.isCombatFlagEquipped())
		{
			return false;
		}
		Fort fort = FortManager.getInstance().findNearestFort(player);
		if (fort == null || fort.getResidenceId() <= 0)
		{
			return false;
		}
		else if (!fort.getSiege().isInProgress())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.THE_FORTRESS_BATTLE_OF_S1_HAS_FINISHED).addItemName(93331));
			return false;
		}
		else
		{
			return true;
		}
	}

	public void dropCombatFlag(Player player, int fortId)
	{
		Fort fort = FortManager.getInstance().getFortById(fortId);
		if (player != null)
		{
			this.removeCombatFlagSkills(player);
			BodyPart bodyPart = BodyPart.fromItem(player.getInventory().getItemByItemId(93331));
			player.getInventory().unEquipItemInBodySlot(bodyPart);
			Item flag = player.getInventory().getItemByItemId(93331);
			player.destroyItem(ItemProcessType.DESTROY, flag, null, true);
			player.setCombatFlagEquipped(false);
			player.broadcastUserInfo();
			InventoryUpdate iu = new InventoryUpdate();
			player.sendInventoryUpdate(iu);
			SpawnData.getInstance().getSpawns().forEach(spawnTemplate -> spawnTemplate.getGroupsByName(flag.getVariables().getString("GREG_SPAWN", "orc_fortress_greg_bottom_right")).forEach(holder -> {
				holder.spawnAll();

				for (NpcSpawnTemplate nst : holder.getSpawns())
				{
					for (Npc npc : nst.getSpawnedNpcs())
					{
						Spawn spawn = npc.getSpawn();
						if (spawn != null)
						{
							spawn.stopRespawn();
						}
					}
				}
			}));
		}

		fort.getSiege().addFlagCount(-1);
	}

	public static FortSiegeManager getInstance()
	{
		return FortSiegeManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final FortSiegeManager INSTANCE = new FortSiegeManager();
	}
}
