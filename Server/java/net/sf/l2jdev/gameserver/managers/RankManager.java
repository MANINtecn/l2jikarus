package net.sf.l2jdev.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.config.ServerConfig;
import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.data.xml.PetDataTable;
import net.sf.l2jdev.gameserver.model.PetData;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.olympiad.Hero;

public class RankManager
{
	private static final Logger LOGGER = Logger.getLogger(RankManager.class.getName());
	public static final Long TIME_LIMIT = 2592000000L;
	public static final long CURRENT_TIME = System.currentTimeMillis();
	public static final int PLAYER_LIMIT = 500;
	private static final String SELECT_CHARACTERS = "SELECT charId,char_name,level,race,base_class, clanid FROM characters WHERE (" + CURRENT_TIME + " - cast(lastAccess as signed) < " + TIME_LIMIT + ") AND accesslevel = 0 AND level > 39 ORDER BY exp DESC, onlinetime DESC LIMIT 500";
	private static final String SELECT_CHARACTERS_PVP = "SELECT charId,char_name,level,race,base_class, clanid, deaths, kills, pvpkills FROM characters WHERE (" + CURRENT_TIME + " - cast(lastAccess as signed) < " + TIME_LIMIT + ") AND accesslevel = 0 AND level > 39 ORDER BY kills DESC, onlinetime DESC LIMIT 500";
	private static final String SELECT_CHARACTERS_BY_RACE = "SELECT charId FROM characters WHERE (" + CURRENT_TIME + " - cast(lastAccess as signed) < " + TIME_LIMIT + ") AND accesslevel = 0 AND level > 39 AND race = ? ORDER BY exp DESC, onlinetime DESC LIMIT 500";
	private static final String SELECT_PETS = "SELECT characters.charId, pets.exp, characters.char_name, pets.level as petLevel, characters.race as char_race, characters.level as char_level, characters.clanId, pet_evolves.index, pet_evolves.level as evolveLevel, pets.item_obj_id, item_id FROM characters, items, pets, pet_evolves WHERE pets.ownerId = characters.charId AND pet_evolves.itemObjId = items.object_id AND pet_evolves.itemObjId = pets.item_obj_id AND (" + CURRENT_TIME + " - cast(characters.lastAccess as signed) < " + TIME_LIMIT + ") AND characters.accesslevel = 0 AND pets.level > 39 ORDER BY pets.exp DESC, characters.onlinetime DESC LIMIT 500";
	public static final String SELECT_CLANS = "SELECT characters.level, characters.char_name, clan_data.clan_id, clan_data.clan_level, clan_data.clan_name, clan_data.reputation_score, clan_data.exp FROM characters, clan_data WHERE characters.charId = clan_data.leader_id AND characters.clanid = clan_data.clan_id AND dissolving_expiry_time = 0 AND characters.accesslevel = 0 ORDER BY exp DESC LIMIT 500";
	public static final String GET_CURRENT_CYCLE_DATA = "SELECT characters.char_name, characters.level, characters.base_class, characters.clanid, olympiad_nobles.charId, olympiad_nobles.olympiad_points, olympiad_nobles.competitions_won, olympiad_nobles.competitions_lost, olympiad_nobles.competitions_drawn FROM characters, olympiad_nobles WHERE characters.charId = olympiad_nobles.charId ORDER BY olympiad_nobles.olympiad_points DESC LIMIT 500";
	public static final String GET_HEROES = "SELECT characters.charId, characters.char_name, characters.race, characters.sex, characters.base_class, characters.level, characters.clanid, olympiad_nobles_eom.competitions_won, olympiad_nobles_eom.competitions_lost, olympiad_nobles_eom.competitions_drawn, olympiad_nobles_eom.olympiad_points, heroes.legend_count, heroes.count FROM heroes, characters, olympiad_nobles_eom WHERE characters.charId = heroes.charId AND characters.charId = olympiad_nobles_eom.charId AND heroes.played = 1 AND characters.accesslevel = 0 ORDER BY olympiad_nobles_eom.olympiad_points DESC, characters.base_class ASC LIMIT 500";
	private static final String GET_CHARACTERS_BY_CLASS = "SELECT charId FROM characters WHERE (" + CURRENT_TIME + " - cast(lastAccess as signed) < " + TIME_LIMIT + ") AND accesslevel = 0 AND level > 39 AND characters.base_class = ? ORDER BY exp DESC, onlinetime DESC LIMIT 500";
	private final Map<Integer, StatSet> _mainList = new ConcurrentHashMap<>();
	private Map<Integer, StatSet> _snapshotList = new ConcurrentHashMap<>();
	private final Map<Integer, StatSet> _mainOlyList = new ConcurrentHashMap<>();
	private Map<Integer, StatSet> _snapshotOlyList = new ConcurrentHashMap<>();
	private final List<RankManager.HeroInfo> _mainHeroList = new LinkedList<>();
	private List<RankManager.HeroInfo> _snapshotHeroList = new LinkedList<>();
	private final Map<Integer, StatSet> _mainPvpList = new ConcurrentHashMap<>();
	private Map<Integer, StatSet> _snapshotPvpList = new ConcurrentHashMap<>();
	private final Map<Integer, StatSet> _mainPetList = new ConcurrentHashMap<>();
	private Map<Integer, StatSet> _snapshotPetList = new ConcurrentHashMap<>();
	private final Map<Integer, StatSet> _mainClanList = new ConcurrentHashMap<>();
	private Map<Integer, StatSet> _snapshotClanList = new ConcurrentHashMap<>();

	protected RankManager()
	{
		ThreadPool.scheduleAtFixedRate(this::update, 0L, 1800000L);
	}

	private synchronized void update()
	{
		this._snapshotList = this._mainList;
		this._mainList.clear();
		this._snapshotOlyList = this._mainOlyList;
		this._mainOlyList.clear();
		this._snapshotHeroList = this._mainHeroList;
		this._mainHeroList.clear();
		this._snapshotPvpList = this._mainPvpList;
		this._mainPvpList.clear();
		this._snapshotPetList = this._mainPetList;
		this._mainPetList.clear();
		this._snapshotClanList = this._mainClanList;
		this._mainClanList.clear();

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement(SELECT_CHARACTERS); ResultSet result = statement.executeQuery();)
		{
			for (int i = 1; result.next(); i++)
			{
				StatSet stats = new StatSet();
				int charId = result.getInt("charId");
				int classId = result.getInt("base_class");
				stats.set("charId", charId);
				stats.set("name", result.getString("char_name"));
				stats.set("level", result.getInt("level"));
				stats.set("classId", result.getInt("base_class"));
				int race = result.getInt("race");
				stats.set("race", race);
				this.loadRaceRank(charId, race, stats);
				this.loadClassRank(charId, classId, stats);
				Clan clan = ClanTable.getInstance().getClan(result.getInt("clanid"));
				if (clan != null)
				{
					stats.set("clanName", clan.getName());
				}
				else
				{
					stats.set("clanName", "");
				}

				this._mainList.put(i, stats);
			}
		}
		catch (Exception var60)
		{
			LOGGER.log(Level.WARNING, "Could not load chars total rank data: " + this + " - " + var60.getMessage(), var60);
		}

		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT characters.char_name, characters.level, characters.base_class, characters.clanid, olympiad_nobles.charId, olympiad_nobles.olympiad_points, olympiad_nobles.competitions_won, olympiad_nobles.competitions_lost, olympiad_nobles.competitions_drawn FROM characters, olympiad_nobles WHERE characters.charId = olympiad_nobles.charId ORDER BY olympiad_nobles.olympiad_points DESC LIMIT 500");
			ResultSet result = statement.executeQuery();)
		{
			for (int i = 1; result.next(); i++)
			{
				StatSet stats = new StatSet();
				int charId = result.getInt("charId");
				stats.set("charId", charId);
				stats.set("name", result.getString("char_name"));
				Clan clan = ClanTable.getInstance().getClan(result.getInt("clanid"));
				if (clan != null)
				{
					stats.set("clanName", clan.getName());
					stats.set("clanLevel", clan.getLevel());
				}
				else
				{
					stats.set("clanName", "");
					stats.set("clanLevel", 0);
				}

				stats.set("level", result.getInt("level"));
				int classId = result.getInt("base_class");
				stats.set("classId", classId);
				stats.set("competitions_won", result.getInt("competitions_won"));
				stats.set("competitions_lost", result.getInt("competitions_lost"));
				stats.set("competitions_drawn", result.getInt("competitions_drawn"));
				stats.set("olympiad_points", result.getInt("olympiad_points"));
				if (Hero.getInstance().getCompleteHeroes().containsKey(charId))
				{
					StatSet heroStats = Hero.getInstance().getCompleteHeroes().get(charId);
					stats.set("count", heroStats.getInt("count", 0));
					stats.set("legend_count", heroStats.getInt("legend_count", 0));
				}
				else
				{
					stats.set("count", 0);
					stats.set("legend_count", 0);
				}

				this.loadClassRank(charId, classId, stats);
				this._mainOlyList.put(i, stats);
			}
		}
		catch (Exception var56)
		{
			LOGGER.log(Level.WARNING, "Could not load olympiad total rank data: " + this + " - " + var56.getMessage(), var56);
		}

		if (!Hero.getInstance().getHeroes().isEmpty())
		{
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT characters.charId, characters.char_name, characters.race, characters.sex, characters.base_class, characters.level, characters.clanid, olympiad_nobles_eom.competitions_won, olympiad_nobles_eom.competitions_lost, olympiad_nobles_eom.competitions_drawn, olympiad_nobles_eom.olympiad_points, heroes.legend_count, heroes.count FROM heroes, characters, olympiad_nobles_eom WHERE characters.charId = heroes.charId AND characters.charId = olympiad_nobles_eom.charId AND heroes.played = 1 AND characters.accesslevel = 0 ORDER BY olympiad_nobles_eom.olympiad_points DESC, characters.base_class ASC LIMIT 500");
				ResultSet result = statement.executeQuery();)
			{
				for (boolean isFirstHero = true; result.next(); isFirstHero = false)
				{
					String charName = result.getString("char_name");
					int clanId = result.getInt("clanid");
					String clanName = clanId > 0 ? ClanTable.getInstance().getClan(clanId).getName() : "";
					int race = result.getInt("race");
					boolean isMale = result.getInt("sex") != 1;
					int baseClass = result.getInt("base_class");
					int level = result.getInt("level");
					int legendCount = result.getInt("legend_count");
					int competitionsWon = result.getInt("competitions_won");
					int competitionsLost = result.getInt("competitions_lost");
					int competitionsDrawn = result.getInt("competitions_drawn");
					int olympiadPoints = result.getInt("olympiad_points");
					int clanLevel = clanId > 0 ? ClanTable.getInstance().getClan(clanId).getLevel() : 0;
					this._mainHeroList.add(new RankManager.HeroInfo(charName, clanName, ServerConfig.SERVER_ID, race, isMale, baseClass, level, legendCount, competitionsWon, competitionsLost, competitionsDrawn, olympiadPoints, clanLevel, isFirstHero));
				}
			}
			catch (Exception var52)
			{
				LOGGER.log(Level.WARNING, "Could not load Hero and Legend Info rank data: " + this + " - " + var52.getMessage(), var52);
			}
		}

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement(SELECT_CHARACTERS_PVP); ResultSet result = statement.executeQuery();)
		{
			for (int i = 1; result.next(); i++)
			{
				StatSet statsx = new StatSet();
				int charIdx = result.getInt("charId");
				statsx.set("charId", charIdx);
				statsx.set("name", result.getString("char_name"));
				statsx.set("level", result.getInt("level"));
				statsx.set("classId", result.getInt("base_class"));
				int race = result.getInt("race");
				statsx.set("race", race);
				statsx.set("kills", result.getInt("kills"));
				statsx.set("deaths", result.getInt("deaths"));
				statsx.set("points", result.getInt("pvpkills"));
				this.loadRaceRank(charIdx, race, statsx);
				Clan clanx = ClanTable.getInstance().getClan(result.getInt("clanid"));
				if (clanx != null)
				{
					statsx.set("clanName", clanx.getName());
				}
				else
				{
					statsx.set("clanName", "");
				}

				this._mainPvpList.put(i, statsx);
			}
		}
		catch (Exception var48)
		{
			LOGGER.log(Level.WARNING, "Could not load pvp total rank data: " + this + " - " + var48.getMessage(), var48);
		}

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement(SELECT_PETS); ResultSet result = statement.executeQuery();)
		{
			int i = 1;

			while (result.next())
			{
				StatSet statsx = new StatSet();
				int controlledItemObjId = result.getInt("item_obj_id");
				statsx.set("controlledItemObjId", controlledItemObjId);
				statsx.set("name", PetDataTable.getInstance().getNameByItemObjectId(controlledItemObjId));
				statsx.set("ownerId", result.getInt("charId"));
				statsx.set("owner_name", result.getString("char_name"));
				statsx.set("owner_race", result.getString("char_race"));
				statsx.set("owner_level", result.getInt("char_level"));
				statsx.set("level", result.getInt("petLevel"));
				statsx.set("evolve_level", result.getInt("evolveLevel"));
				statsx.set("exp", result.getLong("exp"));
				Clan clanx = ClanTable.getInstance().getClan(result.getInt("clanid"));
				if (clanx != null)
				{
					statsx.set("clanName", clanx.getName());
				}
				else
				{
					statsx.set("clanName", "");
				}

				PetData petData = PetDataTable.getInstance().getPetDataByItemId(result.getInt("item_id"));
				statsx.set("petType", petData.getType());
				statsx.set("npcId", petData.getNpcId());
				this._mainPetList.put(i++, statsx);
			}
		}
		catch (Exception var44)
		{
			LOGGER.log(Level.WARNING, "Could not load pet total rank data: " + this + " - " + var44.getMessage(), var44);
		}

		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT characters.level, characters.char_name, clan_data.clan_id, clan_data.clan_level, clan_data.clan_name, clan_data.reputation_score, clan_data.exp FROM characters, clan_data WHERE characters.charId = clan_data.leader_id AND characters.clanid = clan_data.clan_id AND dissolving_expiry_time = 0 AND characters.accesslevel = 0 ORDER BY exp DESC LIMIT 500");
			ResultSet result = statement.executeQuery();)
		{
			for (int i = 1; result.next(); i++)
			{
				StatSet statsx = new StatSet();
				statsx.set("char_name", result.getString("char_name"));
				statsx.set("level", result.getInt("level"));
				statsx.set("clan_level", result.getInt("clan_level"));
				statsx.set("clan_name", result.getString("clan_name"));
				statsx.set("reputation_score", result.getInt("reputation_score"));
				statsx.set("exp", result.getLong("exp"));
				statsx.set("clan_id", result.getInt("clan_id"));
				this._mainClanList.put(i, statsx);
			}
		}
		catch (Exception var40)
		{
			LOGGER.log(Level.WARNING, "Could not load clan total rank data: " + this + " - " + var40.getMessage(), var40);
		}
	}

	private void loadClassRank(int charId, int classId, StatSet stats)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement(GET_CHARACTERS_BY_CLASS);)
		{
			statement.setInt(1, classId);

			try (ResultSet result = statement.executeQuery())
			{
				int i;
				for (i = 0; result.next(); i++)
				{
					if (result.getInt("charId") == charId)
					{
						stats.set("classRank", i + 1);
					}
				}

				if (i == 0)
				{
					stats.set("classRank", 0);
				}
			}
		}
		catch (Exception var15)
		{
			LOGGER.log(Level.WARNING, "Could not load chars classId olympiad rank data: " + this + " - " + var15.getMessage(), var15);
		}
	}

	private void loadRaceRank(int charId, int race, StatSet stats)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement(SELECT_CHARACTERS_BY_RACE);)
		{
			statement.setInt(1, race);

			try (ResultSet result = statement.executeQuery())
			{
				int i;
				for (i = 0; result.next(); i++)
				{
					if (result.getInt("charId") == charId)
					{
						stats.set("raceRank", i + 1);
					}
				}

				if (i == 0)
				{
					stats.set("raceRank", 0);
				}
			}
		}
		catch (Exception var15)
		{
			LOGGER.log(Level.WARNING, "Could not load chars race rank data: " + this + " - " + var15.getMessage(), var15);
		}
	}

	public Map<Integer, StatSet> getRankList()
	{
		return this._mainList;
	}

	public Map<Integer, StatSet> getSnapshotList()
	{
		return this._snapshotList;
	}

	public Map<Integer, StatSet> getOlyRankList()
	{
		return this._mainOlyList;
	}

	public Map<Integer, StatSet> getSnapshotOlyList()
	{
		return this._snapshotOlyList;
	}

	public Collection<RankManager.HeroInfo> getSnapshotHeroList()
	{
		return this._snapshotHeroList;
	}

	public Map<Integer, StatSet> getPvpRankList()
	{
		return this._mainPvpList;
	}

	public Map<Integer, StatSet> getSnapshotPvpRankList()
	{
		return this._snapshotPvpList;
	}

	public Map<Integer, StatSet> getPetRankList()
	{
		return this._mainPetList;
	}

	public Map<Integer, StatSet> getSnapshotPetRankList()
	{
		return this._snapshotPetList;
	}

	public Map<Integer, StatSet> getClanRankList()
	{
		return this._mainClanList;
	}

	public Map<Integer, StatSet> getSnapshotClanRankList()
	{
		return this._snapshotClanList;
	}

	public int getPlayerGlobalRank(Player player)
	{
		int objectId = player.getObjectId();

		for (Entry<Integer, StatSet> entry : this._mainList.entrySet())
		{
			StatSet stats = entry.getValue();
			if (stats.getInt("charId", 0) == objectId)
			{
				return entry.getKey();
			}
		}

		return 0;
	}

	public int getPlayerRaceRank(Player player)
	{
		int objectId = player.getObjectId();

		for (StatSet stats : this._mainList.values())
		{
			if (stats.getInt("charId", 0) == objectId)
			{
				return stats.getInt("raceRank", 0);
			}
		}

		return 0;
	}

	public int getPlayerClassRank(Player player)
	{
		int objectId = player.getObjectId();

		for (StatSet stats : this._mainList.values())
		{
			if (stats.getInt("charId", 0) == objectId)
			{
				return stats.getInt("classRank", 0);
			}
		}

		return 0;
	}

	public Collection<Integer> getTop50()
	{
		List<Integer> result = new LinkedList<>();

		for (int i = 1; i <= 50; i++)
		{
			StatSet stats = this._mainList.get(i);
			if (stats != null)
			{
				result.add(stats.getInt("charId", 0));
			}
		}

		return result;
	}

	public static RankManager getInstance()
	{
		return RankManager.SingletonHolder.INSTANCE;
	}

	public class HeroInfo
	{
		public String charName;
		public String clanName;
		public int serverId;
		public int race;
		public boolean isMale;
		public int baseClass;
		public int level;
		public int legendCount;
		public int competitionsWon;
		public int competitionsLost;
		public int competitionsDrawn;
		public int olympiadPoints;
		public int clanLevel;
		public boolean isTopHero;

		HeroInfo(String charName, String clanName, int serverId, int race, boolean isMale, int baseClass, int level, int legendCount, int competitionsWon, int competitionsLost, int competitionsDrawn, int olympiadPoints, int clanLevel, boolean isTopHero)
		{
			Objects.requireNonNull(RankManager.this);
			super();
			this.charName = charName;
			this.clanName = clanName;
			this.serverId = serverId;
			this.race = race;
			this.isMale = isMale;
			this.baseClass = baseClass;
			this.level = level;
			this.legendCount = legendCount;
			this.competitionsWon = competitionsWon;
			this.competitionsLost = competitionsLost;
			this.competitionsDrawn = competitionsDrawn;
			this.olympiadPoints = olympiadPoints;
			this.clanLevel = clanLevel;
			this.isTopHero = isTopHero;
		}
	}

	private static class SingletonHolder
	{
		protected static final RankManager INSTANCE = new RankManager();
	}
}
