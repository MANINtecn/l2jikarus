package ai.citydomination;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.citydomination.CityDominationManager;
import net.sf.l2jdev.gameserver.model.citydomination.CityDominationManager.CityConfig;
import net.sf.l2jdev.gameserver.model.citydomination.CityDominationManager.LevelStats;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.script.Script;

/**
 * City Domination - AI do Guardiao.
 * Spawna o guardiao de cada cidade, gerencia interacao (doacoes) e conquista.
 */
public class CityGuardianAI extends Script
{
	private static final Logger LOGGER = Logger.getLogger(CityGuardianAI.class.getName());

	private static final int ADENA_ID  = 57;
	private static final int LCOINS_ID = 3509;

	// objectId do NPC guardiao → cityId
	private static final Map<Integer, String> _npcToCity = new ConcurrentHashMap<>();

	private CityGuardianAI()
	{
		CityDominationManager mgr = CityDominationManager.getInstance();
		List<Integer> allNpcIds   = new ArrayList<>();

		for (CityConfig city : mgr.getCities().values())
		{
			// Coletar todos os npcIds dos niveis para registrar eventos
			for (LevelStats ls : city.levels.values())
			{
				if (!allNpcIds.contains(ls.npcId))
					allNpcIds.add(ls.npcId);
			}

			// Spawnar no nivel atual salvo
			int currentLevel = mgr.getGuardianLevel(city.id);
			LevelStats ls    = mgr.getLevelStats(city.id, currentLevel);
			if (ls == null)
			{
				LOGGER.warning("CityGuardianAI: nivel " + currentLevel + " nao encontrado para cidade " + city.name);
				continue;
			}

			Npc guardian = addSpawn(ls.npcId, city.x, city.y, city.z, city.heading, false, 0);
			if (guardian != null)
			{
				_npcToCity.put(guardian.getObjectId(), city.id);
				mgr.onGuardianRegistered(city.id, guardian);
				LOGGER.info("CityGuardianAI: Guardiao de " + city.name + " (Lv" + currentLevel + ") spawnado id=" + guardian.getObjectId());
			}
			else
			{
				LOGGER.warning("CityGuardianAI: Falha ao spawnar guardiao de " + city.name + " (npcId=" + ls.npcId + ")");
			}
		}

		if (!allNpcIds.isEmpty())
		{
			int[] ids = allNpcIds.stream().mapToInt(i -> i).toArray();
			addFirstTalkId(ids);
			addKillId(ids);
		}
	}

	// ----- Events -----

	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		String cityId = _npcToCity.get(npc.getObjectId());
		if (cityId == null)
			return null;
		return buildHtml(npc, player, cityId);
	}

	@Override
	public String onEvent(String event, Npc npc, Player player)
	{
		// formato: "donate <cityId> <currency>"  (currency: adena | lcoins)
		StringTokenizer st = new StringTokenizer(event);
		if (!st.hasMoreTokens()) return null;
		String action = st.nextToken();

		if ("donate".equals(action) && st.countTokens() >= 2)
		{
			String cityId   = st.nextToken();
			String currency = st.nextToken();
			return processDonation(npc, player, cityId, currency);
		}
		return null;
	}

	@Override
	public void onKill(Npc npc, Player killer, boolean isSummon)
	{
		String cityId = _npcToCity.remove(npc.getObjectId());
		if (cityId == null) return;

		CityDominationManager mgr = CityDominationManager.getInstance();
		int killerClanId = (killer != null && killer.getClan() != null) ? killer.getClan().getId() : 0;

		if (killerClanId != 0)
			mgr.onGuardianKilled(cityId, killerClanId);

		// Respawnar no nivel 1 apos 10 segundos
		CityConfig city = mgr.getCity(cityId);
		if (city != null)
		{
			final String fCityId = cityId;
			final CityConfig fCity = city;
			ThreadPool.schedule(() -> respawnGuardian(fCityId, fCity, 1), 10_000L);
		}
	}

	// ----- Helpers -----

	private String processDonation(Npc npc, Player player, String cityId, String currency)
	{
		CityDominationManager mgr = CityDominationManager.getInstance();

		if (mgr.isWarActive())
			return msgHtml("Doacoes indisponiveis durante a guerra!");

		CityConfig city = mgr.getCity(cityId);
		if (city == null)
			return msgHtml("Cidade invalida.");

		int ownerClanId = mgr.getOwnerClanId(cityId);
		Clan playerClan = player.getClan();
		if (ownerClanId == 0 || playerClan == null || playerClan.getId() != ownerClanId)
			return msgHtml("Apenas membros do clan dominante podem contribuir.");

		int currentLevel = mgr.getGuardianLevel(cityId);
		if (currentLevel >= 10)
			return msgHtml("O guardiao ja esta no nivel maximo!");

		int nextLevel    = currentLevel + 1;
		LevelStats next  = mgr.getLevelStats(cityId, nextLevel);
		if (next == null)
			return msgHtml("Erro de configuracao: nivel " + nextLevel);

		boolean useAdena  = "adena".equals(currency);
		boolean useLCoins = "lcoins".equals(currency);

		if (useAdena && next.costAdena > 0 && player.getAdena() < next.costAdena)
			return msgHtml("Adena insuficiente! Necessario: " + fmt(next.costAdena));
		if (useLCoins && next.costLCoins > 0 && player.getInventory().getInventoryItemCount(LCOINS_ID, -1) < next.costLCoins)
			return msgHtml("L-Coins insuficientes! Necessario: " + next.costLCoins);

		if (useAdena && next.costAdena > 0)
			player.destroyItemByItemId(ItemProcessType.FEE, ADENA_ID, next.costAdena, npc, true);
		if (useLCoins && next.costLCoins > 0)
			player.destroyItemByItemId(ItemProcessType.FEE, LCOINS_ID, next.costLCoins, npc, true);

		mgr.setGuardianLevel(cityId, nextLevel);

		// Substituir NPC atual pelo do proximo nivel (agendado para evitar ConcurrentModification)
		final int oldObjId = npc.getObjectId();
		final String fCityId = cityId;
		final CityConfig fCity = city;
		final int fLevel = nextLevel;
		ThreadPool.schedule(() ->
		{
			_npcToCity.remove(oldObjId);
			npc.deleteMe();
			respawnGuardian(fCityId, fCity, fLevel);
		}, 500L);

		return msgHtml("Guardiao evoluindo para nivel " + nextLevel + "! Aguarde...");
	}

	private void respawnGuardian(String cityId, CityConfig city, int level)
	{
		CityDominationManager mgr = CityDominationManager.getInstance();
		LevelStats ls = mgr.getLevelStats(cityId, level);
		if (ls == null) return;

		Npc guardian = addSpawn(ls.npcId, city.x, city.y, city.z, city.heading, false, 0);
		if (guardian != null)
		{
			_npcToCity.put(guardian.getObjectId(), cityId);
			mgr.onGuardianRegistered(cityId, guardian);
			LOGGER.info("CityGuardianAI: Guardiao de " + city.name + " (Lv" + level + ") spawnado id=" + guardian.getObjectId());
		}
	}

	private String buildHtml(Npc npc, Player player, String cityId)
	{
		CityDominationManager mgr = CityDominationManager.getInstance();
		CityConfig city           = mgr.getCity(cityId);
		if (city == null) return null;

		int   ownerClanId   = mgr.getOwnerClanId(cityId);
		int   guardianLevel = mgr.getGuardianLevel(cityId);
		boolean warActive   = mgr.isWarActive();

		Clan ownerClan  = ownerClanId != 0 ? ClanTable.getInstance().getClan(ownerClanId) : null;
		String ownerName = ownerClan != null ? ownerClan.getName() : "Nenhum";

		LevelStats stats    = mgr.getLevelStats(cityId, guardianLevel);
		LevelStats nextStats = guardianLevel < 10 ? mgr.getLevelStats(cityId, guardianLevel + 1) : null;

		Clan   playerClan = player.getClan();
		boolean isOwner   = ownerClanId != 0 && playerClan != null && playerClan.getId() == ownerClanId;

		StringBuilder sb = new StringBuilder();
		sb.append("<html><title>Guardiao de ").append(city.name).append("</title><body>");
		sb.append("<center><font color=\"LEVEL\">Guardiao de ").append(city.name).append("</font></center><br>");
		sb.append("<table border=0 width=270 cellpadding=4><tr><td align=center>");

		sb.append("<table border=0 width=260>");
		sb.append("<tr><td><font color=\"AAAAAA\">Clan Dominante:</font></td><td><font color=\"FFAA00\">").append(ownerName).append("</font></td></tr>");
		sb.append("<tr><td><font color=\"AAAAAA\">Nivel:</font></td><td><font color=\"FFAA00\">").append(guardianLevel).append(" / 10</font></td></tr>");
		if (stats != null)
			sb.append("<tr><td><font color=\"AAAAAA\">HP Maximo:</font></td><td><font color=\"00FF00\">").append(fmt(stats.hp)).append("</font></td></tr>");
		sb.append("<tr><td><font color=\"AAAAAA\">Guerra:</font></td><td><font color=\"").append(warActive ? "FF4444\">ATIVA" : "44AAFF\">INATIVA").append("</font></td></tr>");
		sb.append("</table><br>");

		if (!warActive && isOwner && nextStats != null)
		{
			sb.append("<font color=\"LEVEL\">Evoluir para Nivel ").append(guardianLevel + 1).append("</font><br1>");
			sb.append("<font color=\"AAAAAA\">HP: ").append(fmt(nextStats.hp)).append("</font><br>");
			sb.append("<table border=0 width=260>");

			if (nextStats.costAdena > 0)
				sb.append("<tr><td><button value=\"Adena (").append(fmt(nextStats.costAdena)).append(")\" action=\"bypass Quest CityGuardianAI donate ").append(cityId).append(" adena\" width=260 height=28 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
			if (nextStats.costLCoins > 0)
				sb.append("<tr><td><button value=\"L-Coins (").append(nextStats.costLCoins).append(")\" action=\"bypass Quest CityGuardianAI donate ").append(cityId).append(" lcoins\" width=260 height=28 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");

			sb.append("</table>");
		}
		else if (warActive)
		{
			sb.append("<font color=\"FF4444\">GUERRA EM ANDAMENTO!</font><br1>");
			sb.append("Derrote o guardiao para conquistar ").append(city.name).append(".");
		}
		else if (guardianLevel >= 10)
		{
			sb.append("<font color=\"FFD700\">Guardiao no nivel MAXIMO!</font>");
		}
		else if (!isOwner && ownerClanId != 0)
		{
			sb.append("Apenas o clan ").append(ownerName).append(" pode contribuir.");
		}
		else if (ownerClanId == 0)
		{
			sb.append("Esta cidade esta sem dono.<br>");
			sb.append("Conquiste-a durante a guerra (21h-23h)!");
		}

		sb.append("</td></tr></table>");
		sb.append("</body></html>");
		return sb.toString();
	}

	private static String msgHtml(String msg)
	{
		return "<html><body><center>" + msg + "</center><br><a action=\"back\">Voltar</a></body></html>";
	}

	private static String fmt(long n)
	{
		if (n >= 1_000_000_000) return (n / 1_000_000_000) + "b";
		if (n >= 1_000_000)     return (n / 1_000_000) + "kk";
		if (n >= 1_000)         return (n / 1_000) + "k";
		return String.valueOf(n);
	}

	public static void main(String[] args)
	{
		new CityGuardianAI();
	}
}