package ai.citydomination;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.ai.AttackableAI;
import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.managers.GlobalVariablesManager;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.skill.AbnormalVisualEffect;
import net.sf.l2jdev.gameserver.util.Broadcast;
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

	// cityId → lista de NPCs guard ao redor (pra deletar quando guardian morre/upgrade)
	private static final Map<String, List<Npc>> _cityGuards = new ConcurrentHashMap<>();

	// Arqueiros: NPC 18994 (Boneman Archer - Bow, range 1100, tipo Monster - sem AI nativa de Guard)
	private static final int ARCHER_NPC_ID = 18994;
	private static final int ARCHER_COUNT  = 4;
	private static final int ARCHER_RADIUS = 120;
	// Tanks: NPC 18995 (Boneman Warrior - Sword, range 40, tipo Monster - sem AI nativa de Guard)
	private static final int TANK_NPC_ID = 18995;
	private static final int TANK_COUNT  = 4;
	private static final int TANK_RADIUS = 240;
	private static final int GUARD_AGGRO_RANGE = 600;
	private static final long GUARD_TICK_MS = 1000L; // 1s pra reduzir janela de friendly-fire

	// Anel de fogo no chao: 12 NPCs Folk invisiveis (91011) espalhados no raio da arena.
	// Cada um recebe AVEs de fogo, formando um circulo de chamas visivel a todos.
	private static final int LVL4_GUARDIAN_NPC_ID = 29195; // Valakas Recovery - voa pela cidade em paz
	private static final int FIRE_RING_NPC_ID = 91011;
	private static final int FIRE_RING_COUNT  = 12; // pontos do anel (a cada 30 graus)
	private static final Map<String, List<Npc>> _cityFireRing = new ConcurrentHashMap<>();

	// Animacao imponente ao chegar no waypoint (Lv4 em paz)
	private static final Map<String, Long> _cityFlyAnimUntil = new ConcurrentHashMap<>();
	private static final long FLY_ANIM_PAUSE_MS = 4000L;  // duracao da pose antes do proximo voo
	private static final int  LVL4_ARRIVAL_SOCIAL_ACTION = 110; // social action ID: param MoveAroundSocial do NPC 29195

	// PvP Arena dinamica ao redor do guardian (so ativa em war).
	// Players dentro do raio recebem PVP=true + PEACE=false (override da peace zone da town).
	private static final int ARENA_RADIUS = 500;
	private static final Map<String, java.util.Set<Integer>> _arenaPlayers = new ConcurrentHashMap<>();

	// Pool de nomes aleatorios pros guardas (fantasy / medieval)
	private static final String[] GUARD_NAMES = {
		"Aldric", "Bren", "Cael", "Doran", "Edric", "Faron", "Garrick", "Halren",
		"Ivar", "Jorah", "Kaelen", "Loras", "Mikael", "Norvin", "Orin", "Perrin",
		"Quinn", "Roric", "Sten", "Theron", "Ulric", "Varic", "Wendel", "Yorick"
	};
	private static final java.util.Random RNG = new java.util.Random();

	// Feature flag <features lvl4-enabled="true"/> em data/mods/city_domination.xml
	// Lida 1x no boot via parseLvl4Enabled() e cacheada.
	private static volatile Boolean _lvl4EnabledCache = null;

	private static boolean isLvl4Enabled()
	{
		Boolean cached = _lvl4EnabledCache;
		if (cached != null) return cached.booleanValue();
		boolean parsed = parseLvl4Enabled();
		_lvl4EnabledCache = Boolean.valueOf(parsed);
		LOGGER.info("CityGuardianAI: lvl4-enabled = " + parsed);
		return parsed;
	}

	// Liga/desliga o mod inteiro via <settings enabled="false"/>. Ausente = ligado (compat).
	private static boolean parseModEnabled()
	{
		try
		{
			java.io.File f = new java.io.File("./data/mods/city_domination.xml");
			if (!f.exists()) return false;
			org.w3c.dom.Document doc = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(f);
			org.w3c.dom.NodeList list = doc.getElementsByTagName("settings");
			if (list.getLength() == 0) return true;
			org.w3c.dom.Element el = (org.w3c.dom.Element) list.item(0);
			String v = el.getAttribute("enabled");
			if ((v == null) || v.isEmpty()) return true; // sem atributo = ligado
			return "true".equalsIgnoreCase(v) || "1".equals(v);
		}
		catch (Exception ex)
		{
			LOGGER.warning("CityGuardianAI.parseModEnabled: " + ex.getMessage());
			return true;
		}
	}

	private static boolean parseLvl4Enabled()
	{
		try
		{
			java.io.File f = new java.io.File("./data/mods/city_domination.xml");
			if (!f.exists()) return false;
			org.w3c.dom.Document doc = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(f);
			org.w3c.dom.NodeList list = doc.getElementsByTagName("features");
			if (list.getLength() == 0) return false;
			org.w3c.dom.Element el = (org.w3c.dom.Element) list.item(0);
			String v = el.getAttribute("lvl4-enabled");
			return "true".equalsIgnoreCase(v) || "1".equals(v);
		}
		catch (Exception ex)
		{
			LOGGER.warning("CityGuardianAI.parseLvl4Enabled: " + ex.getMessage());
			return false;
		}
	}

	// Retorna o maxLevel efetivo: se Lvl4 desligado, capa em 3 mesmo que XML tenha 4 niveis.
	private static int effectiveMaxLevel(CityConfig city)
	{
		int total = city.levels.size();
		return isLvl4Enabled() ? total : Math.min(3, total);
	}

	private CityGuardianAI()
	{
		_instance = this; // permite que admin handler chame forceRespawnGuardian
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
				guardian.disableCoreAI(true);
				guardian.setImmobilized(true);
				guardian.setRandomWalking(false);
				guardian.setTargetable(true);
				// Em paz nasce nao-atacavel (click duplo abre HTML via onFirstTalk)
				guardian.setAutoAttackable(mgr.isWarActive());
				applyGuardianAura(guardian, currentLevel);
				// Reaplica apos 5s pra garantir que players conectados depois do boot tambem vejam
				final Npc fGuardian = guardian;
				final int fLevel = currentLevel;
				ThreadPool.schedule(() -> applyGuardianAura(fGuardian, fLevel), 5000L);
				_npcToCity.put(guardian.getObjectId(), city.id);
				mgr.onGuardianRegistered(city.id, guardian);
				spawnCityGuards(city.id, city);
				spawnFireRing(city.id, city);
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
			addAttackId(ids);
		}

		// Tick periodico: gerencia AI dos guardas (imobilizado em paz, aggro contra clans inimigos em guerra)
		ThreadPool.scheduleAtFixedRate(() -> warAggroTick(), GUARD_TICK_MS, GUARD_TICK_MS);
	}

	// ----- Events -----

	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		String cityId = _npcToCity.get(npc.getObjectId());
		if (cityId == null)
			return null;
		return buildPremiumHtml(npc, player, cityId);
	}

	@Override
	public String onEvent(String event, Npc npc, Player player)
	{
		// formato: "donate <cityId> <currency> <amount>"  (currency: adena | lcoins)
		StringTokenizer st = new StringTokenizer(event);
		if (!st.hasMoreTokens()) return null;
		String action = st.nextToken();

		if ("donate".equals(action) && st.countTokens() >= 3)
		{
			String cityId   = st.nextToken();
			String currency = st.nextToken();
			long amount;
			try { amount = Long.parseLong(st.nextToken()); }
			catch (Exception e) { return msgHtml("Quantia invalida."); }
			return processDonation(npc, player, cityId, currency, amount);
		}
		return null;
	}

	@Override
	public void onAttack(Npc npc, Player attacker, int damage, boolean isSummon)
	{
		String cityId = _npcToCity.get(npc.getObjectId());
		if (cityId == null) return;

		CityDominationManager mgr = CityDominationManager.getInstance();
		if (mgr.isWarActive())
		{
			// EM GUERRA: nao abre HTML. Combate normal + ASSIST DOS GUARDAS.
			// Filtra: solo (sem clan) E membros do clan dono NAO disparam assist.
			if (attacker != null && attacker.getClan() != null)
			{
				int ownerClanId = mgr.getOwnerClanId(cityId);
				if (ownerClanId == 0 || attacker.getClan().getId() != ownerClanId)
				{
					alertCityGuards(cityId, attacker);
				}
			}
			return;
		}

		// Paz: tentativa de bater no guardian -> abrir janela de doacao
		String html = buildPremiumHtml(npc, attacker, cityId);
		if (html != null)
		{
			net.sf.l2jdev.gameserver.network.serverpackets.NpcHtmlMessage msg =
				new net.sf.l2jdev.gameserver.network.serverpackets.NpcHtmlMessage(npc.getObjectId());
			msg.setHtml(html);
			attacker.sendPacket(msg);
		}
	}

	// Faz todos os guardas da cidade focarem o atacante (assist ao boss).
	// Chamado quando o guardian e atacado durante a war.
	private static void alertCityGuards(String cityId, Player target)
	{
		List<Npc> guards = _cityGuards.get(cityId);
		if (guards == null || guards.isEmpty()) return;
		for (Npc g : guards)
		{
			if (g == null || g.isDead()) continue;
			try
			{
				// Garante AI ativa
				g.setImmobilized(false);
				g.disableCoreAI(false);
				if (g.getAI() instanceof AttackableAI)
				{
					((AttackableAI) g.getAI()).setGlobalAggro(0);
				}
				g.setRunning();
				if (g.isAttackable())
				{
					g.asAttackable().addDamageHate(target, 0, 9999);
				}
				g.getAI().setIntention(Intention.ATTACK, target);
			}
			catch (Exception ignored) {}
		}
	}

	@Override
	public void onKill(Npc npc, Player killer, boolean isSummon)
	{
		String cityId = _npcToCity.remove(npc.getObjectId());
		if (cityId == null) return;

		CityDominationManager mgr = CityDominationManager.getInstance();
		int killerClanId = (killer != null && killer.getClan() != null) ? killer.getClan().getId() : 0;

		if (killerClanId != 0)
		{
			mgr.onGuardianKilled(cityId, killerClanId);
			// Broadcast extra de parabens
			Clan kc = ClanTable.getInstance().getClan(killerClanId);
			String clanName = kc != null ? kc.getName() : "?";
			CityConfig city0 = mgr.getCity(cityId);
			String cityName = city0 != null ? city0.name : cityId;
			Broadcast.toAllOnlinePlayers(
				">>> PARABENS AO CLAN " + clanName.toUpperCase() + " - CONQUISTOU " + cityName.toUpperCase() + "! <<<", true);
		}

		// Limpa guardas + anel de fogo + estado de animacao de voo e respawn no nivel 1 apos 10 segundos
		clearCityGuards(cityId);
		clearFireRing(cityId);
		_cityFlyAnimUntil.remove(cityId);
		CityConfig city = mgr.getCity(cityId);
		if (city != null)
		{
			final String fCityId = cityId;
			final CityConfig fCity = city;
			ThreadPool.schedule(() -> respawnGuardian(fCityId, fCity, 1), 10_000L);
		}
	}

	// ----- Helpers -----

	private String processDonation(Npc npc, Player player, String cityId, String currency, long amount)
	{
		CityDominationManager mgr = CityDominationManager.getInstance();

		if (mgr.isWarActive())
			return msgHtml("Doacoes indisponiveis durante a guerra!");

		CityConfig city = mgr.getCity(cityId);
		if (city == null)
			return msgHtml("Cidade invalida.");

		if (amount <= 0)
			return msgHtml("Quantia invalida.");

		int currentLevel = mgr.getGuardianLevel(cityId);
		int maxLevel = effectiveMaxLevel(city);
		if (currentLevel >= maxLevel)
			return msgHtml("Guardiao ja esta no nivel maximo (" + maxLevel + ")!");

		int nextLevel    = currentLevel + 1;
		LevelStats next  = mgr.getLevelStats(cityId, nextLevel);
		if (next == null)
			return msgHtml("Erro de configuracao: nivel " + nextLevel);

		boolean useAdena  = "adena".equals(currency);
		boolean useLCoins = "lcoins".equals(currency);

		// Restricao opcional: apos conquista, so clan dono pode contribuir
		int ownerClanId = mgr.getOwnerClanId(cityId);
		Clan playerClan = player.getClan();
		if (ownerClanId != 0 && (playerClan == null || playerClan.getId() != ownerClanId))
			return msgHtml("Apenas membros do clan dono podem contribuir agora.");

		// Verifica saldo
		long playerHas = useAdena
			? player.getAdena()
			: (useLCoins ? player.getInventory().getInventoryItemCount(LCOINS_ID, -1) : 0L);
		if (playerHas < amount)
			return msgHtml(useAdena ? "Adena insuficiente!" : "L-Coins insuficientes!");

		// Acumulado atual via GlobalVariablesManager
		String varKey = "CD_DONATED_" + cityId.toUpperCase() + "_" + (useAdena ? "ADENA" : "LCOINS");
		long currentDonated = GlobalVariablesManager.getInstance().getLong(varKey, 0L);
		long costNext = useAdena ? next.costAdena : next.costLCoins;
		if (costNext <= 0)
			return msgHtml("Esta moeda nao e aceita pra subir de nivel.");

		// Aceita doacao, deduz do player
		int itemId = useAdena ? ADENA_ID : LCOINS_ID;
		player.destroyItemByItemId(ItemProcessType.FEE, itemId, amount, npc, true);

		long newDonated = currentDonated + amount;

		// Se completar custo: sobe level, reseta acumulado, respawna NPC
		if (newDonated >= costNext)
		{
			GlobalVariablesManager.getInstance().set(varKey, 0L);
			mgr.setGuardianLevel(cityId, nextLevel);

			// Broadcast global anunciando o upgrade
			String donorClan = (playerClan != null) ? playerClan.getName() : player.getName();
			Broadcast.toAllOnlinePlayers(
				">>> O Guardiao de " + city.name.toUpperCase() + " foi fortificado para o NIVEL " + nextLevel + " por " + donorClan + "! <<<", true);

			final int oldObjId = npc.getObjectId();
			final String fCityId = cityId;
			final CityConfig fCity = city;
			final int fLevel = nextLevel;
			final Npc fOldNpc = npc;
			// Efeito visual no NPC velho ANTES de deletar (cast de skill p/ "transformacao")
			try
			{
				net.sf.l2jdev.gameserver.network.serverpackets.MagicSkillUse departing =
					new net.sf.l2jdev.gameserver.network.serverpackets.MagicSkillUse(fOldNpc, fOldNpc, 2024, 1, 1500, 0);
				fOldNpc.broadcastPacket(departing);
				fOldNpc.getEffectList().startAbnormalVisualEffect(AbnormalVisualEffect.HEROIC_HOLY_AVE);
			}
			catch (Exception ignored) {}

			ThreadPool.schedule(() ->
			{
				_npcToCity.remove(oldObjId);
				clearCityGuards(fCityId);
				clearFireRing(fCityId);
				fOldNpc.deleteMe();
				respawnGuardian(fCityId, fCity, fLevel);
			}, 1500L); // 1500ms pra dar tempo do efeito visual antes da transicao

			// Retorna janela principal ja atualizada (sem botao Voltar)
			return buildPremiumHtml(npc, player, cityId);
		}

		// Apenas acumulou - recarrega a janela principal com progresso atualizado
		GlobalVariablesManager.getInstance().set(varKey, newDonated);
		return buildPremiumHtml(npc, player, cityId);
	}

	// Spawna 4 arqueiros (cardinais, raio 120) + 4 tanks (diagonais, raio 240) em torno do guardian
	// Cada guarda recebe nome aleatorio para nao ficar tudo igual
	private void spawnCityGuards(String cityId, CityConfig city)
	{
		clearCityGuards(cityId);

		List<Npc> guards = new ArrayList<>();
		List<String> usedNames = new ArrayList<>();

		// Arqueiros - cardinais (N/S/E/O)
		int[][] archerOffsets = { {ARCHER_RADIUS, 0}, {-ARCHER_RADIUS, 0}, {0, ARCHER_RADIUS}, {0, -ARCHER_RADIUS} };
		for (int i = 0; i < ARCHER_COUNT && i < archerOffsets.length; i++)
		{
			Npc g = spawnNamedGuard(ARCHER_NPC_ID, city.x + archerOffsets[i][0], city.y + archerOffsets[i][1], city.z, city.heading, "Archer", usedNames);
			if (g != null) guards.add(g);
		}

		// Tanks - diagonais (NE/NO/SE/SO) mais afastados
		int tankDiag = (int) Math.round(TANK_RADIUS * 0.7071); // sqrt(2)/2 pra distribuir nas diagonais
		int[][] tankOffsets = { {tankDiag, tankDiag}, {-tankDiag, tankDiag}, {tankDiag, -tankDiag}, {-tankDiag, -tankDiag} };
		for (int i = 0; i < TANK_COUNT && i < tankOffsets.length; i++)
		{
			Npc g = spawnNamedGuard(TANK_NPC_ID, city.x + tankOffsets[i][0], city.y + tankOffsets[i][1], city.z, city.heading, "Sentinel", usedNames);
			if (g != null) guards.add(g);
		}

		if (!guards.isEmpty())
			_cityGuards.put(cityId, guards);
	}

	// Spawna um guarda com nome aleatorio (sem repetir entre os ja spawnados desta city) + titulo
	private Npc spawnNamedGuard(int npcId, int x, int y, int z, int heading, String title, List<String> usedNames)
	{
		try
		{
			Npc g = addSpawn(npcId, x, y, z, heading, false, 0);
			if (g == null) return null;

			g.disableCoreAI(true);
			g.setImmobilized(true);
			g.setRandomWalking(false);
			g.setTargetable(true);

			// Nome aleatorio sem repetir
			String name = pickUniqueName(usedNames);
			try { g.setName(name); } catch (Exception ignored) {}
			try { g.setTitle(title); } catch (Exception ignored) {}
			try { g.broadcastInfo(); } catch (Exception ignored) {}
			return g;
		}
		catch (Exception ex)
		{
			LOGGER.warning("CityGuardianAI: Falha ao spawnar guarda " + npcId + ": " + ex.getMessage());
			return null;
		}
	}

	private static String pickUniqueName(List<String> usedNames)
	{
		// Tenta achar um nome ainda nao usado
		for (int tries = 0; tries < 12; tries++)
		{
			String candidate = GUARD_NAMES[RNG.nextInt(GUARD_NAMES.length)];
			if (!usedNames.contains(candidate))
			{
				usedNames.add(candidate);
				return candidate;
			}
		}
		// Fallback: usa qualquer (pode repetir se pool esgotou)
		return GUARD_NAMES[RNG.nextInt(GUARD_NAMES.length)];
	}

	private static void clearCityGuards(String cityId)
	{
		List<Npc> old = _cityGuards.remove(cityId);
		if (old != null)
		{
			for (Npc g : old)
			{
				try { g.deleteMe(); } catch (Exception ignored) {}
			}
		}
	}

	// Spawna 12 NPCs invisiveis (Folk, collision 0.1) distribuidos em circulo no raio da arena.
	// Cada marcador recebe AVEs de fogo, criando um anel de chamas no chao visivel a todos.
	private void spawnFireRing(String cityId, CityConfig city)
	{
		clearFireRing(cityId);
		List<Npc> ring = new ArrayList<>();
		double step = (Math.PI * 2.0) / FIRE_RING_COUNT;
		for (int i = 0; i < FIRE_RING_COUNT; i++)
		{
			double angle = step * i;
			int rx = city.x + (int) Math.round(Math.cos(angle) * ARENA_RADIUS);
			int ry = city.y + (int) Math.round(Math.sin(angle) * ARENA_RADIUS);
			try
			{
				Npc marker = addSpawn(FIRE_RING_NPC_ID, rx, ry, city.z, 0, false, 0);
				if (marker != null)
				{
					marker.setImmobilized(true);
					marker.setRandomWalking(false);
					// Efeito "Burning Field" (campo de fogo no chao) + efeito ground beast fire
					marker.getEffectList().startAbnormalVisualEffect(AbnormalVisualEffect.H_DK_RE_BURNINGFIELD_AVE);
					marker.getEffectList().startAbnormalVisualEffect(AbnormalVisualEffect.V_OR_BURNING_BEAST_GROUND_AVE);
					ring.add(marker);
				}
			}
			catch (Exception ex)
			{
				LOGGER.warning("CityGuardianAI.spawnFireRing[" + i + "]: " + ex.getMessage());
			}
		}
		if (!ring.isEmpty())
			_cityFireRing.put(cityId, ring);
		LOGGER.info("CityGuardianAI: Anel de fogo (" + ring.size() + " pts) criado para " + cityId);
	}

	private static void clearFireRing(String cityId)
	{
		List<Npc> old = _cityFireRing.remove(cityId);
		if (old != null)
		{
			for (Npc n : old)
			{
				try { n.deleteMe(); } catch (Exception ignored) {}
			}
		}
	}

	// Tick periodico: durante a guerra libera AI do guardian + guardas e atira aggro em clans inimigos.
	// Em paz, mantem tudo imobilizado/invulneravel (statua decorativa).
	private static void warAggroTick()
	{
		try
		{
			CityDominationManager mgr = CityDominationManager.getInstance();
			boolean warActive = mgr.isWarActive();

			// 1) GUARDIANS: libera AI + invul off em war; restaura em paz
			for (String cityId : mgr.getCities().keySet())
			{
				Npc guardian = mgr.getGuardian(cityId);
				if (guardian == null || guardian.isDead()) continue;

				if (warActive)
				{
					// CRITICO: disableCoreAI(true) bloqueia auto-attack do player (AbstractAI.clientStartAutoAttack)
					try { guardian.disableCoreAI(false); } catch (Exception ignored) {}
					try { guardian.setImmobilized(false); } catch (Exception ignored) {}
					try { guardian.setRandomWalking(false); } catch (Exception ignored) {} // para de voar em paz
					try { guardian.setInvul(false); } catch (Exception ignored) {}
					try { guardian.setAutoAttackable(true); } catch (Exception ignored) {}

					// Aggro range proativo: guardian detecta e ataca clans inimigos no raio da arena
					if (guardian.isAttackable())
					{
						try
						{
							int ownerClanId = mgr.getOwnerClanId(cityId);
							Player aggroTarget = findEnemyClanPlayerNear(guardian, ownerClanId, ARENA_RADIUS);
							if (aggroTarget != null)
							{
								guardian.asAttackable().addDamageHate(aggroTarget, 0, 9999);
								guardian.getAI().setIntention(Intention.ATTACK, aggroTarget);
							}
						}
						catch (Exception ignored) {}
					}
				}
				else
				{
					try { guardian.setInvul(true); } catch (Exception ignored) {}
					try { guardian.setAutoAttackable(false); } catch (Exception ignored) {}

					if (guardian.getId() == LVL4_GUARDIAN_NPC_ID && isLvl4Enabled())
					{
						// Lv4 (Valakas Recovery): voa pela cidade em paz com raio dobrado (1000u)
						try { guardian.disableCoreAI(false); } catch (Exception ignored) {}
						try { guardian.setImmobilized(false); } catch (Exception ignored) {}
						try { guardian.setRandomWalking(false); } catch (Exception ignored) {} // waypoints manuais
						try { guardian.setRunning(); } catch (Exception ignored) {}
						// Quando parado (IDLE/ACTIVE): toca animacao imponente, depois sorteia proximo destino
						try
						{
							if (guardian.getAI().getIntention() == Intention.IDLE ||
								guardian.getAI().getIntention() == Intention.ACTIVE)
							{
								long animUntil = _cityFlyAnimUntil.getOrDefault(cityId, 0L);
								long now = System.currentTimeMillis();
								if (now > animUntil)
								{
									// Guardiao acabou de chegar: toca a pose imponente e bloqueia o proximo sorteio
									_cityFlyAnimUntil.put(cityId, now + FLY_ANIM_PAUSE_MS);
									playGuardianArrivalAnimation(guardian);
									final String fCityId = cityId;
									final Npc fGuardian  = guardian;
									ThreadPool.schedule(() -> scheduleLvl4NextFly(fCityId, fGuardian), FLY_ANIM_PAUSE_MS);
								}
								// else: animacao ainda tocando, aguarda o ThreadPool iniciar o proximo voo
							}
						}
						catch (Exception ignored) {}
					}
					else
					{
						// Niveis 1-3: estatua decorativa, imobilizada
						try { guardian.disableCoreAI(true); } catch (Exception ignored) {}
						try { guardian.setImmobilized(true); } catch (Exception ignored) {}
						try { guardian.setRandomWalking(false); } catch (Exception ignored) {}
					}
				}
			}

			// 1.5) ARENA PVP: players proximos do guardian ganham PVP=true + PEACE=false em war.
			for (String cityId : mgr.getCities().keySet())
			{
				processArenaPvp(cityId, warActive);
			}

			// 2) GUARDAS (arqueiros + tanks): mesma logica + aggro contra clans inimigos
			for (Map.Entry<String, List<Npc>> entry : _cityGuards.entrySet())
			{
				String cityId = entry.getKey();
				int ownerClanId = mgr.getOwnerClanId(cityId);
				Npc guardian = mgr.getGuardian(cityId); // pra anti-friendly-fire
				for (Npc guard : entry.getValue())
				{
					if (guard == null || guard.isDead()) continue;

					if (warActive)
					{
						try { guard.setImmobilized(false); } catch (Exception ignored) {}
						try { guard.disableCoreAI(false); } catch (Exception ignored) {}
						// CRITICO: zera o _globalAggro=-10 (cooldown padrao da AttackableAI antes de processar aggro)
						try
						{
							if (guard.getAI() instanceof AttackableAI)
							{
								((AttackableAI) guard.getAI()).setGlobalAggro(0);
							}
						}
						catch (Exception ignored) {}

						// ANTI FRIENDLY-FIRE: zera o hate do guard no proprio guardian a cada tick.
						// A AI nativa do Guard adiciona hate em qualquer Monster proximo (incluindo o boss).
						if (guardian != null && guard.isAttackable())
						{
							try { guard.asAttackable().stopHating(guardian); } catch (Exception ignored) {}
							try { guard.asAttackable().getAggroList().remove(guardian); } catch (Exception ignored) {}
						}

						Player target = findEnemyClanPlayerNear(guard, ownerClanId, GUARD_AGGRO_RANGE);
						if (target != null)
						{
							try
							{
								guard.setRunning();
								if (guard.isAttackable())
								{
									guard.asAttackable().addDamageHate(target, 0, 9999);
								}
								guard.getAI().setIntention(Intention.ATTACK, target);
							}
							catch (Exception ignored) {}
						}
						else if (guardian != null && guard.getTarget() == guardian)
						{
							// Nao tem player perto e o guard ta mirando o guardian -> volta pra ACTIVE
							try { guard.getAI().setIntention(Intention.ACTIVE); } catch (Exception ignored) {}
						}
					}
					else
					{
						try { guard.disableCoreAI(true); } catch (Exception ignored) {}
						try { guard.setImmobilized(true); } catch (Exception ignored) {}
					}
				}
			}
		}
		catch (Exception ex)
		{
			LOGGER.warning("CityGuardianAI.warAggroTick: " + ex.getMessage());
		}
	}

	// Gerencia zona PvP dinamica ao redor do guardian.
	// Circulo vermelho: SEMPRE visivel (paz e guerra) para indicar o territorio do guardiao.
	// PvP flags (PEACE=false, PVP=true): APENAS durante war ativa.
	// Ao sair do raio: circulo sumido + PvP liberado (se estava aplicado).
	private static void processArenaPvp(String cityId, boolean warActive)
	{
		java.util.Set<Integer> currentInArena = _arenaPlayers.computeIfAbsent(cityId, k -> java.util.concurrent.ConcurrentHashMap.newKeySet());
		CityDominationManager mgr = CityDominationManager.getInstance();
		Npc guardian = mgr.getGuardian(cityId);

		// Sem guardian: limpa circulo e PvP de todos
		if (guardian == null || guardian.isDead())
		{
			if (currentInArena.isEmpty()) return;
			for (Integer objId : currentInArena)
			{
				Player p = World.getInstance().getPlayer(objId);
				if (p != null)
				{
					clearArenaCircle(p, "cd_arena");
					releaseArenaPvp(p);
				}
			}
			currentInArena.clear();
			return;
		}

		// Detecta quem esta no raio agora
		java.util.Set<Integer> nowInside = new java.util.HashSet<>();
		try
		{
			for (Player p : World.getInstance().getVisibleObjectsInRange(guardian, Player.class, ARENA_RADIUS))
			{
				if (p == null || p.isDead() || p.isGM()) continue;
				nowInside.add(p.getObjectId());
				boolean wasInside = currentInArena.contains(p.getObjectId());

				if (!wasInside)
				{
					// Entrou no raio: desenha circulo e aplica PvP (paz ou guerra)
					drawArenaCircle(p, guardian.getX(), guardian.getY(), guardian.getZ(), ARENA_RADIUS, 0xFF3030, "cd_arena");
					applyArenaPvp(p);
				}
			}
		}
		catch (Exception ex)
		{
			LOGGER.warning("CityGuardianAI.processArenaPvp: " + ex.getMessage());
		}

		// Saiu do raio: remove circulo e PvP
		for (Integer objId : currentInArena)
		{
			if (!nowInside.contains(objId))
			{
				Player p = World.getInstance().getPlayer(objId);
				if (p != null)
				{
					clearArenaCircle(p, "cd_arena");
					releaseArenaPvp(p);
				}
			}
		}

		currentInArena.clear();
		currentInArena.addAll(nowInside);
	}

	// Aplica flags de PvP (override da peace zone da city).
	private static void applyArenaPvp(Player p)
	{
		try
		{
			p.setInsideZone(net.sf.l2jdev.gameserver.model.zone.ZoneId.PVP, true);
			p.setInsideZone(net.sf.l2jdev.gameserver.model.zone.ZoneId.PEACE, false);
			p.setInsideZone(net.sf.l2jdev.gameserver.model.zone.ZoneId.NO_PVP, false);
			p.sendPacket(net.sf.l2jdev.gameserver.network.SystemMessageId.YOU_HAVE_ENTERED_A_COMBAT_ZONE);
			p.sendMessage(">>> Zona do Guardiao: PvP ativo. Heal e buffs liberados. <<<");
		}
		catch (Exception ignored) {}
	}

	// Remove PvP do player (idempotente: so age se PvP estava ativo).
	private static void releaseArenaPvp(Player p)
	{
		try
		{
			if (!p.isInsideZone(net.sf.l2jdev.gameserver.model.zone.ZoneId.PVP)) return;
			p.setInsideZone(net.sf.l2jdev.gameserver.model.zone.ZoneId.PVP, false);
			p.revalidateZone(true); // recupera PEACE original da cidade
			p.sendPacket(net.sf.l2jdev.gameserver.network.SystemMessageId.YOU_HAVE_LEFT_A_COMBAT_ZONE);
		}
		catch (Exception ignored) {}
	}

	// Desenha um circulo no chao via ExServerPrimitive.
	// segments = quantos triangulos formam o circulo (32 = bem suave)
	// color = ARGB (0xRRGGBB)
	// name = identificador unico do desenho (pra poder apagar depois enviando outro com mesmo nome)
	// Anchor no player (padrao do servidor - ver AdminDebug/AdminRegions).
	// Linhas em coordenadas absolutas do mundo, centradas em (cx, cy, cz) = posicao do guardian.
	// Ponto central adicionado para deixar visivel o centro exato no chao/minimap.
	public static void drawArenaCircle(Player p, int cx, int cy, int cz, int radius, int color, String name)
	{
		try
		{
			int segments = 32;
			net.sf.l2jdev.gameserver.network.serverpackets.ExServerPrimitive prim =
				new net.sf.l2jdev.gameserver.network.serverpackets.ExServerPrimitive(name, p.getX(), p.getY(), p.getZ());
			// Ponto central: marca exatamente a posicao do guardian
			prim.addPoint(color, cx, cy, cz);
			double step = (Math.PI * 2.0) / segments;
			int prevX = cx + (int) Math.round(Math.cos(0) * radius);
			int prevY = cy + (int) Math.round(Math.sin(0) * radius);
			for (int i = 1; i <= segments; i++)
			{
				double a = step * i;
				int x = cx + (int) Math.round(Math.cos(a) * radius);
				int y = cy + (int) Math.round(Math.sin(a) * radius);
				prim.addLine(color, prevX, prevY, cz, x, y, cz);
				prevX = x;
				prevY = y;
			}
			p.sendPacket(prim);
		}
		catch (Exception ignored) {}
	}

	// Apaga o circulo (envia primitive vazio com mesmo nome - cliente remove pelo nome)
	public static void clearArenaCircle(Player p, String name)
	{
		try
		{
			net.sf.l2jdev.gameserver.network.serverpackets.ExServerPrimitive prim =
				new net.sf.l2jdev.gameserver.network.serverpackets.ExServerPrimitive(name, p.getX(), p.getY(), p.getZ());
			p.sendPacket(prim);
		}
		catch (Exception ignored) {}
	}

	// Procura o player inimigo mais proximo do guarda dentro do range.
	// Inimigo = tem clan E o clan e diferente do clan dono da city.
	// Solo (sem clan) NUNCA e alvo. Membros do clan dono tambem nao.
	private static Player findEnemyClanPlayerNear(Npc guard, int ownerClanId, int range)
	{
		Player closest = null;
		double closestDist = Double.MAX_VALUE;
		try
		{
			for (Player p : World.getInstance().getVisibleObjectsInRange(guard, Player.class, range))
			{
				if (p == null || p.isDead()) continue;
				// GM sempre e alvo (sem clan) pra permitir testes in-game
				if (!p.isGM())
				{
					Clan pc = p.getClan();
					if (pc == null) continue; // solo sem clan nao e atacado
					if (ownerClanId != 0 && pc.getId() == ownerClanId) continue; // clan dono nao e atacado
				}
				double d = guard.calculateDistance3D(p);
				if (d < closestDist)
				{
					closest = p;
					closestDist = d;
				}
			}
		}
		catch (Exception ignored) {}
		return closest;
	}

	// Toca a pose imponente do Lv4 ao chegar no waypoint.
	// ID 110 = MoveAroundSocial do NPC 29195 (Valakas Recovery) - animacao nativa dele.
	private static void playGuardianArrivalAnimation(Npc guardian)
	{
		try
		{
			LOGGER.info("CityGuardianAI: pose imponente id=" + LVL4_ARRIVAL_SOCIAL_ACTION + " npc=" + guardian.getId());
			guardian.broadcastPacket(
				new net.sf.l2jdev.gameserver.network.serverpackets.SocialAction(
					guardian.getObjectId(), LVL4_ARRIVAL_SOCIAL_ACTION));
		}
		catch (Exception ex)
		{
			LOGGER.warning("CityGuardianAI.playGuardianArrivalAnimation: " + ex.getMessage());
		}
	}

	// Chamado pelo ThreadPool apos FLY_ANIM_PAUSE_MS: sorteia proximo waypoint de voo do Lv4.
	// Verifica se ainda e paz e se o guardian nao morreu/foi substituido antes de mover.
	private static void scheduleLvl4NextFly(String cityId, Npc guardian)
	{
		try
		{
			CityDominationManager mgr = CityDominationManager.getInstance();
			if (mgr.isWarActive()) return;
			Npc current = mgr.getGuardian(cityId);
			if (current == null || current.isDead() || current.getObjectId() != guardian.getObjectId()) return;
			CityConfig city = mgr.getCity(cityId);
			if (city == null) return;
			double angle = RNG.nextDouble() * Math.PI * 2.0;
			double dist  = RNG.nextDouble() * (ARENA_RADIUS * 2);
			int tx = city.x + (int) Math.round(Math.cos(angle) * dist);
			int ty = city.y + (int) Math.round(Math.sin(angle) * dist);
			guardian.getAI().setIntention(Intention.MOVE_TO,
				new net.sf.l2jdev.gameserver.model.Location(tx, ty, city.z));
		}
		catch (Exception ignored) {}
	}

	// Aplica aura visual no guardian baseado no level
	private static void applyGuardianAura(Npc guardian, int level)
	{
		try
		{
			// Limpa auras antigas
			guardian.getEffectList().stopAbnormalVisualEffect(AbnormalVisualEffect.AURA_BUFF);
			guardian.getEffectList().stopAbnormalVisualEffect(AbnormalVisualEffect.AURA_DEBUFF);
			guardian.getEffectList().stopAbnormalVisualEffect(AbnormalVisualEffect.BIG_HEAD);
			guardian.getEffectList().stopAbnormalVisualEffect(AbnormalVisualEffect.BIG_BODY);

			// Aplica conforme level:
			//  Lv1 = AURA_BUFF (aura azul de buff)
			//  Lv2 = AURA_BUFF + BIG_HEAD (cabecudo)
			//  Lv3 = AURA_BUFF apenas (o boss base ja e imponente, sem cabecao nem big body)
			//  Lv4 = AURA_BUFF + AURA_DEBUFF (aura dupla intensa pro Valakas Recovery - lord supremo)
			if (level >= 1)
				guardian.getEffectList().startAbnormalVisualEffect(AbnormalVisualEffect.AURA_BUFF);
			if (level == 2)
				guardian.getEffectList().startAbnormalVisualEffect(AbnormalVisualEffect.BIG_HEAD);
			if (level >= 4)
				guardian.getEffectList().startAbnormalVisualEffect(AbnormalVisualEffect.AURA_DEBUFF);

			guardian.broadcastInfo();
		}
		catch (Exception ex)
		{
			// silent - efeito é cosmetico
		}
	}

	// Versao publica chamavel pelo admin handler (cd_reset / cd_setlevel / cd_conquest).
	// Limpa o guardian/guards anteriores e respawna no level pedido com TODOS os hooks
	// do script (npc map, setAutoAttackable, aura, guards, transition effect).
	public static Npc forceRespawnGuardian(String cityId, int level)
	{
		CityDominationManager mgr = CityDominationManager.getInstance();
		CityConfig city = mgr.getCity(cityId);
		if (city == null) return null;

		// Limpa estado antigo
		Npc old = mgr.getGuardian(cityId);
		if (old != null)
		{
			_npcToCity.remove(old.getObjectId());
			try { old.deleteMe(); } catch (Exception ignored) {}
		}
		clearCityGuards(cityId);
		clearFireRing(cityId);

		// Singleton do script (instance method addSpawn) - precisa de uma referencia.
		if (_instance == null) return null;
		_instance.respawnGuardian(cityId, city, level);
		return mgr.getGuardian(cityId);
	}

	// Singleton self-reference inicializada no construtor pra forceRespawnGuardian poder
	// chamar o instance method addSpawn (que e protected em Script).
	private static CityGuardianAI _instance;

	private void respawnGuardian(String cityId, CityConfig city, int level)
	{
		CityDominationManager mgr = CityDominationManager.getInstance();
		LevelStats ls = mgr.getLevelStats(cityId, level);
		if (ls == null) return;

		Npc guardian = addSpawn(ls.npcId, city.x, city.y, city.z, city.heading, false, 0);
		if (guardian != null)
		{
			guardian.disableCoreAI(true);
			guardian.setImmobilized(true);
			guardian.setRandomWalking(false);
			guardian.setTargetable(true);
			guardian.setAutoAttackable(mgr.isWarActive()); // paz=click abre HTML / war=atacavel
			applyGuardianAura(guardian, level);
			// Reaplica apos 3s pra player garantir efeito
			final Npc fGuardian2 = guardian;
			final int fLevel2 = level;
			ThreadPool.schedule(() -> applyGuardianAura(fGuardian2, fLevel2), 3000L);
			_npcToCity.put(guardian.getObjectId(), cityId);
			mgr.onGuardianRegistered(cityId, guardian);
			spawnCityGuards(cityId, city);
			spawnFireRing(cityId, city);
			// Efeito visual de transicao (aura special + spell cast no spawn)
			playTransitionEffect(guardian, level);
			LOGGER.info("CityGuardianAI: Guardiao de " + city.name + " (Lv" + level + ") spawnado id=" + guardian.getObjectId());
		}
	}

	// Efeito visual de "evolucao" quando o guardian sobe de nivel ou (re)nasce.
	// Aplica aura HOLY temporaria + dispara MagicSkillUse pra todos verem o cast no NPC.
	private static void playTransitionEffect(Npc guardian, int level)
	{
		try
		{
			// Aura especial temporaria (5s) - efeito holy/glow no NPC
			guardian.getEffectList().startAbnormalVisualEffect(AbnormalVisualEffect.HEROIC_HOLY_AVE);
			final Npc fG = guardian;
			ThreadPool.schedule(() ->
			{
				try { fG.getEffectList().stopAbnormalVisualEffect(AbnormalVisualEffect.HEROIC_HOLY_AVE); fG.broadcastInfo(); }
				catch (Exception ignored) {}
			}, 5000L);

			// Broadcast cast de skill no NPC pra criar animacao visual de "transformacao"
			// Skill 2024 = Heroic Valor (player buff cast, animacao de aura sagrada)
			net.sf.l2jdev.gameserver.network.serverpackets.MagicSkillUse pkt =
				new net.sf.l2jdev.gameserver.network.serverpackets.MagicSkillUse(guardian, guardian, 2024, 1, 2000, 0);
			guardian.broadcastPacket(pkt);
		}
		catch (Exception ex)
		{
			LOGGER.warning("CityGuardianAI.playTransitionEffect: " + ex.getMessage());
		}
	}

	public static String buildPremiumHtml(Npc npc, Player player, String cityId)
	{
		CityDominationManager mgr = CityDominationManager.getInstance();
		CityConfig city           = mgr.getCity(cityId);
		if (city == null) return null;

		int   ownerClanId   = mgr.getOwnerClanId(cityId);
		int   guardianLevel = mgr.getGuardianLevel(cityId);
		boolean warActive   = mgr.isWarActive();
		int     maxLevel    = effectiveMaxLevel(city);

		Clan ownerClan  = ownerClanId != 0 ? ClanTable.getInstance().getClan(ownerClanId) : null;
		String ownerName = ownerClan != null ? ownerClan.getName() : "Sem Dono";
		String ownerColor = ownerClanId != 0 ? "FFD700" : "888888";

		LevelStats nextStats = guardianLevel < maxLevel ? mgr.getLevelStats(cityId, guardianLevel + 1) : null;

		Clan   playerClan = player.getClan();
		boolean isOwner   = ownerClanId != 0 && playerClan != null && playerClan.getId() == ownerClanId;
		boolean canDonate = (ownerClanId == 0) || isOwner; // cidade livre = todos | cidade com dono = só clan dono

		// Estrelas do nivel (dinamico)
		StringBuilder stars = new StringBuilder();
		for (int i = 1; i <= maxLevel; i++)
			stars.append(i <= guardianLevel ? "<font color=\"FFD700\">*</font>" : "<font color=\"333333\">*</font>");

		// Progresso de doacao acumulado
		long donatedAdena = GlobalVariablesManager.getInstance().getLong("CD_DONATED_" + cityId.toUpperCase() + "_ADENA", 0L);
		long donatedLCoin = GlobalVariablesManager.getInstance().getLong("CD_DONATED_" + cityId.toUpperCase() + "_LCOINS", 0L);
		long costAdena  = nextStats != null ? nextStats.costAdena  : 0L;
		long costLCoins = nextStats != null ? nextStats.costLCoins : 0L;

		String warColor = warActive ? "FF3030" : "44CCFF";
		String warLabel = warActive ? "GUERRA EM ANDAMENTO" : "PERIODO DE PAZ";

		StringBuilder sb = new StringBuilder(2048);
		sb.append("<html><title>Guardiao de ").append(city.name).append("</title>");
		sb.append("<body>");

		// === HEADER compacto ===
		sb.append("<center>");
		sb.append("<table width=290 cellpadding=2 cellspacing=0 bgcolor=\"1A1A1A\">");
		sb.append("<tr><td align=center>");
		sb.append("<font color=\"LEVEL\"><b>GUARDIAO DE ").append(city.name.toUpperCase()).append("</b></font>");
		sb.append("</td></tr>");
		sb.append("<tr><td align=center bgcolor=\"0A0A0A\">");
		sb.append("<font color=\"").append(warColor).append("\">").append(warLabel).append("</font>");
		sb.append("</td></tr></table>");

		// === DOMINIO compacto (sem HP Maximo) ===
		sb.append("<table width=290 cellpadding=2 cellspacing=1 bgcolor=\"222222\">");
		sb.append("<tr><td width=120><font color=\"888888\">CLAN DOMINANTE</font></td>");
		sb.append("<td align=right><font color=\"").append(ownerColor).append("\"><b>").append(ownerName).append("</b></font></td></tr>");
		sb.append("<tr><td><font color=\"888888\">NIVEL ATUAL</font></td>");
		sb.append("<td align=right>").append(stars).append(" <font color=\"FFAA00\">").append(guardianLevel).append("/").append(maxLevel).append("</font></td></tr>");
		sb.append("</table>");

		// === BLOCO DE ACAO ===
		if (warActive)
		{
			sb.append("<table width=290 cellpadding=8 cellspacing=0 bgcolor=\"3A0000\">");
			sb.append("<tr><td align=center>");
			sb.append("<font color=\"FF4040\"><b>!! GUERRA ATIVA !!</b></font><br1>");
			sb.append("<font color=\"AAAAAA\">O guardiao esta vulneravel.</font><br1>");
			sb.append("<font color=\"AAAAAA\">Derrote-o e conquiste ").append(city.name).append("!</font>");
			sb.append("</td></tr></table>");
		}
		else if (guardianLevel >= maxLevel)
		{
			sb.append("<table width=290 cellpadding=8 cellspacing=0 bgcolor=\"2A2A00\">");
			sb.append("<tr><td align=center>");
			sb.append("<font color=\"FFD700\"><b>NIVEL MAXIMO ATINGIDO</b></font><br1>");
			sb.append("<font color=\"AAAAAA\">O guardiao esta no auge do poder.</font>");
			sb.append("</td></tr></table>");
		}
		else if (canDonate && nextStats != null)
		{
			// Bloco de doacao (qualquer um se cidade livre, so dono se conquistada)
			sb.append("<table width=290 cellpadding=4 cellspacing=0 bgcolor=\"1A2A1A\">");
			sb.append("<tr><td align=center>");
			sb.append("<font color=\"LEVEL\"><b>FORTIFICAR PARA NIVEL ").append(guardianLevel + 1).append("</b></font>");
			sb.append("</td></tr></table>");

			// Barra de progresso Adena
			if (costAdena > 0)
			{
				int pctAdena = (int) Math.min(100L, (donatedAdena * 100L) / Math.max(1L, costAdena));
				sb.append("<table width=290 cellpadding=2 cellspacing=0><tr><td align=center>");
				sb.append("<font color=\"AAAAAA\">ADENA </font>");
				sb.append("<font color=\"FFD700\">").append(fmt(donatedAdena)).append("</font>");
				sb.append("<font color=\"666666\"> / </font>");
				sb.append("<font color=\"AAAAAA\">").append(fmt(costAdena)).append("</font>");
				sb.append(" <font color=\"00FF80\">(").append(pctAdena).append("%)</font>");
				sb.append("</td></tr></table>");
				sb.append(progressBar(pctAdena, "FFD700"));
				sb.append(donateButtonsRow(cityId, "adena"));
			}

			// Barra de progresso L-Coins
			if (costLCoins > 0)
			{
				int pctLC = (int) Math.min(100L, (donatedLCoin * 100L) / Math.max(1L, costLCoins));
				sb.append("<table width=290 cellpadding=2 cellspacing=0><tr><td align=center>");
				sb.append("<font color=\"AAAAAA\">L-COINS </font>");
				sb.append("<font color=\"00CCFF\">").append(donatedLCoin).append("</font>");
				sb.append("<font color=\"666666\"> / </font>");
				sb.append("<font color=\"AAAAAA\">").append(costLCoins).append("</font>");
				sb.append(" <font color=\"00FF80\">(").append(pctLC).append("%)</font>");
				sb.append("</td></tr></table>");
				sb.append(progressBar(pctLC, "00CCFF"));
				sb.append(donateButtonsRow(cityId, "lcoins"));
			}
		}
		else
		{
			sb.append("<table width=290 cellpadding=8 cellspacing=0 bgcolor=\"2A1A1A\">");
			sb.append("<tr><td align=center>");
			sb.append("<font color=\"FF8844\"><b>CIDADE DOMINADA</b></font><br1>");
			sb.append("<font color=\"AAAAAA\">Apenas membros do clan <font color=\"FFD700\">").append(ownerName).append("</font></font><br1>");
			sb.append("<font color=\"AAAAAA\">podem fortificar agora.</font>");
			sb.append("</td></tr></table>");
		}

		// === FOOTER: Banner + schedule (compacto) ===
		sb.append("<table width=290 cellpadding=3 cellspacing=0 bgcolor=\"1A1A1A\">");
		sb.append("<tr><td align=center>");
		sb.append("<font color=\"FFD700\"><b>L2 IKARUS INTERCROW</b></font> <font color=\"666666\">|</font> <font color=\"AAAAAA\">www.l2ikarus.com</font>");
		sb.append("</td></tr>");
		sb.append("<tr><td align=center bgcolor=\"0A0A0A\">");
		sb.append("<font color=\"666666\">Guerra diaria 21:00 - 23:00 (BRT)</font>");
		sb.append("</td></tr></table>");
		sb.append("</center>");
		sb.append("</body></html>");
		return sb.toString();
	}

	private static String msgHtml(String msg)
	{
		return "<html><body><center>" + msg + "</center><br><a action=\"back\">Voltar</a></body></html>";
	}

	// Barra de progresso visual usando tabela com bgcolor
	private static String progressBar(int pct, String fillColor)
	{
		pct = Math.max(0, Math.min(100, pct));
		int filled = pct * 280 / 100;
		int empty  = 280 - filled;
		StringBuilder sb = new StringBuilder(160);
		sb.append("<table width=290 cellpadding=0 cellspacing=0 bgcolor=\"111111\"><tr>");
		if (filled > 0)
			sb.append("<td width=").append(filled).append(" height=10 bgcolor=\"").append(fillColor).append("\"></td>");
		if (empty > 0)
			sb.append("<td width=").append(empty).append(" height=10 bgcolor=\"333333\"></td>");
		sb.append("</tr></table>");
		return sb.toString();
	}

	// Linha de botoes pre-set de doacao
	private static String donateButtonsRow(String cityId, String currency)
	{
		long[] amounts;
		String[] labels;
		if ("adena".equals(currency))
		{
			amounts = new long[]{100_000L, 1_000_000L, 10_000_000L, 100_000_000L};
			labels  = new String[]{"100k", "1kk", "10kk", "100kk"};
		}
		else
		{
			amounts = new long[]{10L, 100L, 500L, 1000L};
			labels  = new String[]{"10", "100", "500", "1000"};
		}
		StringBuilder sb = new StringBuilder(512);
		sb.append("<table width=290 cellpadding=1 cellspacing=0><tr>");
		for (int i = 0; i < amounts.length; i++)
		{
			sb.append("<td align=center><button value=\"").append(labels[i]).append("\" action=\"bypass Quest CityGuardianAI donate ")
				.append(cityId).append(" ").append(currency).append(" ").append(amounts[i])
				.append("\" width=65 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		}
		sb.append("</tr></table>");
		return sb.toString();
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
		if (!parseModEnabled())
		{
			LOGGER.info("CityGuardianAI: mod DESATIVADO (enabled=false em city_domination.xml).");
			return;
		}
		new CityGuardianAI();
	}
}