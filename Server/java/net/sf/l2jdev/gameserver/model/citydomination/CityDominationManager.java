package net.sf.l2jdev.gameserver.model.citydomination;

import java.io.File;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.managers.GlobalVariablesManager;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.util.Broadcast;

public class CityDominationManager
{
	private static final Logger LOGGER = Logger.getLogger(CityDominationManager.class.getName());

	private static final ZoneId BRT = ZoneId.of("America/Sao_Paulo");

	private static final String VAR_CLAN  = "CD_CLAN_";
	private static final String VAR_LEVEL = "CD_LEVEL_";

	// ----- Data holders -----

	public static class LevelStats
	{
		public final int    id;
		public final int    npcId;
		public final long   hp;
		public final long   costAdena;
		public final long   costLCoins;

		LevelStats(int id, int npcId, long hp, long costAdena, long costLCoins)
		{
			this.id         = id;
			this.npcId      = npcId;
			this.hp         = hp;
			this.costAdena  = costAdena;
			this.costLCoins = costLCoins;
		}
	}

	public static class CityConfig
	{
		public final String                  id;
		public final String                  name;
		public final int                     x, y, z, heading;
		public final Map<Integer, LevelStats> levels;

		CityConfig(String id, String name, int x, int y, int z, int heading, Map<Integer, LevelStats> levels)
		{
			this.id      = id;
			this.name    = name;
			this.x       = x;
			this.y       = y;
			this.z       = z;
			this.heading = heading;
			this.levels  = Collections.unmodifiableMap(levels);
		}

		public int getNpcIdForLevel(int level)
		{
			LevelStats ls = levels.get(level);
			return ls != null ? ls.npcId : -1;
		}
	}

	// ----- State -----

	private final Map<String, CityConfig> _cities    = new LinkedHashMap<>();
	private final Map<String, Npc>        _guardians = new ConcurrentHashMap<>();

	private int     _warStartHour   = 21;
	private int     _warStartMinute = 0;
	private int     _warEndHour     = 23;
	private int     _warEndMinute   = 0;
	private boolean _warActive      = false;

	// ----- Singleton -----

	private CityDominationManager()
	{
		loadConfig();
		initWarSchedule();
	}

	private static final class SingletonHolder
	{
		static final CityDominationManager INSTANCE = new CityDominationManager();
	}

	public static CityDominationManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	// ----- Config -----

	private void loadConfig()
	{
		File file = new File("./data/mods/city_domination.xml");
		if (!file.exists())
		{
			LOGGER.warning("CityDominationManager: data/mods/city_domination.xml nao encontrado.");
			return;
		}
		try
		{
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);

			NodeList settingsList = doc.getElementsByTagName("settings");
			if (settingsList.getLength() > 0)
			{
				Element settings = (Element) settingsList.item(0);
				NodeList startList = settings.getElementsByTagName("war-start");
				if (startList.getLength() > 0)
				{
					Element ws = (Element) startList.item(0);
					_warStartHour   = Integer.parseInt(ws.getAttribute("hour"));
					_warStartMinute = Integer.parseInt(ws.getAttribute("minute"));
				}
				NodeList endList = settings.getElementsByTagName("war-end");
				if (endList.getLength() > 0)
				{
					Element we = (Element) endList.item(0);
					_warEndHour   = Integer.parseInt(we.getAttribute("hour"));
					_warEndMinute = Integer.parseInt(we.getAttribute("minute"));
				}
			}

			NodeList cityNodes = doc.getElementsByTagName("city");
			for (int i = 0; i < cityNodes.getLength(); i++)
			{
				Element cityElem = (Element) cityNodes.item(i);
				String  cityId   = cityElem.getAttribute("id");
				String  name     = cityElem.getAttribute("name");
				int     x        = Integer.parseInt(cityElem.getAttribute("x"));
				int     y        = Integer.parseInt(cityElem.getAttribute("y"));
				int     z        = Integer.parseInt(cityElem.getAttribute("z"));
				int     heading  = Integer.parseInt(cityElem.getAttribute("heading"));

				Map<Integer, LevelStats> levels = new LinkedHashMap<>();
				NodeList levelNodes = cityElem.getElementsByTagName("level");
				for (int j = 0; j < levelNodes.getLength(); j++)
				{
					Element le       = (Element) levelNodes.item(j);
					int     lid      = Integer.parseInt(le.getAttribute("id"));
					int     npcId    = Integer.parseInt(le.getAttribute("npcId"));
					long    hp       = Long.parseLong(le.getAttribute("hp"));
					long    adena    = Long.parseLong(le.getAttribute("costAdena"));
					long    lcoins   = Long.parseLong(le.getAttribute("costLCoins"));
					levels.put(lid, new LevelStats(lid, npcId, hp, adena, lcoins));
				}

				_cities.put(cityId, new CityConfig(cityId, name, x, y, z, heading, levels));
				LOGGER.info("CityDominationManager: cidade carregada: " + name + " (" + levels.size() + " niveis)");
			}

			LOGGER.info("CityDominationManager: " + _cities.size() + " cidade(s), guerra " + _warStartHour + "h-" + _warEndHour + "h BRT.");
		}
		catch (Exception e)
		{
			LOGGER.warning("CityDominationManager: Erro ao ler city_domination.xml: " + e.getMessage());
		}
	}

	// ----- War schedule -----

	private void initWarSchedule()
	{
		if (isCurrentlyWarTime())
		{
			startWar();
			long msToEnd = msUntil(_warEndHour, _warEndMinute);
			if (msToEnd <= 0)
				msToEnd += 24L * 3_600_000;
			ThreadPool.schedule(this::endWar, msToEnd);
		}
		scheduleNextWarStart();
	}

	private void scheduleNextWarStart()
	{
		long msToStart = msUntil(_warStartHour, _warStartMinute);
		if (msToStart <= 0)
			msToStart += 24L * 3_600_000;

		long warDurationMs = ((_warEndHour * 60L + _warEndMinute) - (_warStartHour * 60L + _warStartMinute)) * 60_000L;

		ThreadPool.schedule(() ->
		{
			startWar();
			ThreadPool.schedule(() ->
			{
				endWar();
				scheduleNextWarStart();
			}, warDurationMs);
		}, msToStart);

		LOGGER.info("CityDominationManager: proxima guerra em " + (msToStart / 60_000) + " min.");
	}

	private long msUntil(int hour, int minute)
	{
		ZonedDateTime now    = ZonedDateTime.now(BRT);
		ZonedDateTime target = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0);
		return target.toInstant().toEpochMilli() - System.currentTimeMillis();
	}

	private boolean isCurrentlyWarTime()
	{
		ZonedDateTime now = ZonedDateTime.now(BRT);
		int nowM   = now.getHour() * 60 + now.getMinute();
		int startM = _warStartHour * 60 + _warStartMinute;
		int endM   = _warEndHour   * 60 + _warEndMinute;
		return nowM >= startM && nowM < endM;
	}

	private void startWar()
	{
		_warActive = true;
		for (Npc guardian : _guardians.values())
		{
			if (guardian != null && !guardian.isDead())
				guardian.setInvul(false);
		}
		Broadcast.toAllOnlinePlayers("=== GUERRA DE CIDADES INICIADA! Conquiste os guardioes! ===", true);
		LOGGER.info("CityDominationManager: Guerra iniciada.");
	}

	private void endWar()
	{
		_warActive = false;
		for (Map.Entry<String, Npc> entry : _guardians.entrySet())
		{
			Npc guardian = entry.getValue();
			if (guardian != null && !guardian.isDead())
			{
				guardian.setInvul(true);
				guardian.setCurrentHp(guardian.getMaxHp());
			}
		}
		Broadcast.toAllOnlinePlayers("=== FIM DA GUERRA! Guardioes sobreviventes recuperaram sua vitalidade! ===", true);
		LOGGER.info("CityDominationManager: Guerra encerrada.");
	}

	// ----- Public API -----

	public boolean isWarActive()                       { return _warActive; }
	public Map<String, CityConfig> getCities()         { return Collections.unmodifiableMap(_cities); }
	public CityConfig getCity(String cityId)           { return _cities.get(cityId); }

	public int getOwnerClanId(String cityId)
	{
		return GlobalVariablesManager.getInstance().getInt(VAR_CLAN + cityId.toUpperCase(), 0);
	}

	public void setOwnerClanId(String cityId, int clanId)
	{
		GlobalVariablesManager.getInstance().set(VAR_CLAN + cityId.toUpperCase(), clanId);
		GlobalVariablesManager.getInstance().storeMe();
	}

	public int getGuardianLevel(String cityId)
	{
		return GlobalVariablesManager.getInstance().getInt(VAR_LEVEL + cityId.toUpperCase(), 1);
	}

	public void setGuardianLevel(String cityId, int level)
	{
		GlobalVariablesManager.getInstance().set(VAR_LEVEL + cityId.toUpperCase(), level);
		GlobalVariablesManager.getInstance().storeMe();
	}

	public LevelStats getLevelStats(String cityId, int level)
	{
		CityConfig city = _cities.get(cityId);
		return city != null ? city.levels.get(level) : null;
	}

	public void setGuardian(String cityId, Npc npc)
	{
		_guardians.put(cityId, npc);
	}

	public Npc getGuardian(String cityId)
	{
		return _guardians.get(cityId);
	}

	public void onGuardianRegistered(String cityId, Npc npc)
	{
		_guardians.put(cityId, npc);
		npc.setInvul(!_warActive);
	}

	public void onGuardianKilled(String cityId, int killerClanId)
	{
		CityConfig city = _cities.get(cityId);
		if (city == null) return;

		int prevClanId = getOwnerClanId(cityId);
		setOwnerClanId(cityId, killerClanId);
		setGuardianLevel(cityId, 1);
		_guardians.remove(cityId);

		Clan killerClan = ClanTable.getInstance().getClan(killerClanId);
		String killerName = killerClan != null ? killerClan.getName() : "?";

		String prevInfo = "";
		if (prevClanId != 0)
		{
			Clan prev = ClanTable.getInstance().getClan(prevClanId);
			if (prev != null)
				prevInfo = " (antes: " + prev.getName() + ")";
		}

		Broadcast.toAllOnlinePlayers(
			">>> CLAN " + killerName.toUpperCase() + " CONQUISTOU " + city.name.toUpperCase() + "!" + prevInfo + " <<<", true);
		LOGGER.info("CityDominationManager: " + city.name + " conquistada por " + killerName + " (clan=" + killerClanId + ").");
	}
}