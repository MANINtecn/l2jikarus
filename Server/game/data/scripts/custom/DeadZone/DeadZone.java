/*
 * MOD ZONA MORTAL (Dead Zone) - L2 Ikarus Intercrow
 *
 * FASE 1 (esta versao): zona + entrada/saida + avisos + bloqueio de autoplay + alertas.
 * FASE 2 (proxima): morte -> perde tudo -> corpo saqueavel com delay.
 * FASE 3 (proxima): controle de acesso por grade/nivel + verificacao 60s + jail 3-strikes.
 *
 * Documentacao completa: ZONA_MORTAL_DESIGN.md (nesta pasta).
 */
package custom.DeadZone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.ConfigReader;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.data.xml.ItemData;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.managers.ZoneManager;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.Containers;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureDeath;
import net.sf.l2jdev.gameserver.model.events.listeners.ConsumerEventListener;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.type.CrystalType;
import net.sf.l2jdev.gameserver.model.punishment.PunishmentAffect;
import net.sf.l2jdev.gameserver.model.punishment.PunishmentTask;
import net.sf.l2jdev.gameserver.model.punishment.PunishmentType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.script.Script;
import net.sf.l2jdev.gameserver.model.skill.AbnormalVisualEffect;
import net.sf.l2jdev.gameserver.model.stats.Stat;
import net.sf.l2jdev.gameserver.model.zone.ZoneType;
import net.sf.l2jdev.gameserver.network.enums.ChatType;
import net.sf.l2jdev.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2jdev.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.ExUserInfoEquipSlot;
import net.sf.l2jdev.gameserver.network.serverpackets.MagicSkillCanceled;
import net.sf.l2jdev.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2jdev.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2jdev.gameserver.taskmanagers.AutoPlayTaskManager;
import net.sf.l2jdev.gameserver.util.Broadcast;

/**
 * Zona Mortal - mod L2 Ikarus.
 * @author L2 Ikarus
 */
public class DeadZone extends Script
{
	private static final Logger LOGGER = Logger.getLogger(DeadZone.class.getName());

	// ---- Config (lido de config/Custom/DeadZone.ini) ----
	private static boolean ENABLED;
	private static int ZONE_D_ID;
	private static String ZONE_D_NAME;
	private static String SCREEN_MESSAGE;
	private static int ANNOUNCE_INTERVAL; // minutos
	private static String ANNOUNCE_TEXT;
	private static int CRITICAL_INTERVAL; // segundos
	private static String CRITICAL_TEXT;
	private static boolean DROP_ADENA;
	private static Set<Integer> PROTECTED_ITEMS;
	private static boolean BREAK_ENABLED;
	private static int BREAK_MIN_PCT;
	private static int BREAK_MAX_PCT;
	private static int DEBRIS_ITEM_ID;
	private static int GRAVE_NPC_ID;
	private static int CORPSE_DURATION; // segundos
	// Controle de acesso
	private static CrystalType MAX_GRADE;
	private static boolean CHECK_LEVEL;
	private static int MIN_LEVEL;
	private static int MAX_LEVEL;
	private static int EXIT_X;
	private static int EXIT_Y;
	private static int EXIT_Z;
	private static int CHECK_INTERVAL; // segundos
	private static int MAX_VIOLATIONS;
	private static int JAIL_MINUTES;
	// Gatekeeper
	private static int GATEKEEPER_NPC_ID;
	private static int GK_X;
	private static int GK_Y;
	private static int GK_Z;
	private static int GK_HEADING;
	private static int ENTRANCE_X;
	private static int ENTRANCE_Y;
	private static int ENTRANCE_Z;
	private static String KICK_WARN1;
	private static String KICK_WARN2;
	private static String JAIL_MSG;

	// Contador de violacoes por jogador (objectId -> qtd)
	private static final Map<Integer, Integer> VIOLATIONS = new ConcurrentHashMap<>();

	// Raid bosses lvl 20-34 reaproveitados como mini-bosses da zona
	private static final int[] BOSS_IDS =
	{
		25372, 25375, 25378, 25357, 25373, 25380, 25001, 25362, 25060, 25019,
		25426, 25429, 25787, 25360, 25038, 25272, 25333, 25076, 25112, 25188,
		25352, 25401, 25391, 25404, 25023, 25189, 25383
	};
	private static double BOSS_HP_FACTOR;
	private static double BOSS_DMG_FACTOR;
	private static double BOSS_DEF_FACTOR;
	private static int BOSS_ADENA_MIN;
	private static int BOSS_ADENA_MAX;
	private static int BOSS_LCOIN_MIN;
	private static int BOSS_LCOIN_MAX;
	private static int BOSS_JEWEL_BOX_ID;
	private static int BOSS_JEWEL_CHANCE;
	private static int[] BOSS_CRAFT_MATERIALS;
	private static int BOSS_CRAFT_DROP_COUNT;

	// Bosses ja ajustados (evita mergeMul acumular no respawn do mesmo objeto)
	private static final Set<Integer> ADJUSTED_BOSSES = Collections.newSetFromMap(new ConcurrentHashMap<>());

	// ===== TELEGRAPH AoE (estilo Albion: marca o chao, espera, da dano) =====
	private static boolean TELEGRAPH_ENABLED = false; // desligado: boss retail, sem telegraph nem dano em area
	private static final long TELEGRAPH_INTERVAL_MS = 9000L;  // verifica a cada 9s
	private static final int TELEGRAPH_CHANCE = 40;           // 40% de chance de disparar por ciclo
	private static final long TELEGRAPH_DELAY_MS = 2500L;     // janela pra fugir (2.5s)
	private static final int TELEGRAPH_RADIUS = 220;          // raio da zona de perigo
	private static final double TELEGRAPH_HP_PERCENT = 0.45;  // dano = 45% do HP max do alvo
	private static final int TELEGRAPH_COLOR_WARN = 0xFF3030; // vermelho (aviso)
	private static final int TELEGRAPH_COLOR_HIT = 0xFFAA00;  // laranja (impacto)
	private static final Map<Integer, ScheduledFuture<?>> TELEGRAPH_TASKS = new ConcurrentHashMap<>();

	// objectIds dos jogadores que estao dentro da zona agora
	private static final Set<Integer> PLAYERS_INSIDE = Collections.newSetFromMap(new ConcurrentHashMap<>());

	/** Guarda um item do tumulo com enchant level preservado. */
	private static class LootEntry
	{
		final int id;
		final long count;
		final int enchant;
		LootEntry(int id, long count, int enchant)
		{
			this.id = id;
			this.count = count;
			this.enchant = enchant;
		}
	}

	// Loot coletado dos mortos (chave = objectId da lapide)
	private static final Map<Integer, List<LootEntry>> CORPSE_LOOT = new ConcurrentHashMap<>();

	// Saques em andamento (chave = objectId do saqueador)
	private static final Map<Integer, ScheduledFuture<?>> ACTIVE_LOOTS = new ConcurrentHashMap<>();

	// Posicao do saqueador no inicio do saque (para verificar movimento)
	private static final Map<Integer, int[]> LOOT_POSITIONS = new ConcurrentHashMap<>();

	private static int LOOT_DELAY; // segundos
	private static boolean HIDE_ITEM_NAMES;

	public DeadZone()
	{
		loadConfig();

		if (!ENABLED)
		{
			LOGGER.info("DeadZone: mod desativado (DeadZoneEnabled=False).");
			return;
		}

		final ZoneType zone = ZoneManager.getInstance().getZoneById(ZONE_D_ID);
		if (zone == null)
		{
			LOGGER.warning("DeadZone: zona id=" + ZONE_D_ID + " nao encontrada no DeadZone.xml. Mod nao iniciado.");
			return;
		}

		addEnterZoneId(ZONE_D_ID);
		addExitZoneId(ZONE_D_ID);

		// Listener global de morte: detecta quem morreu dentro da zona
		Containers.Global().addListener(new ConsumerEventListener(Containers.Global(), EventType.ON_CREATURE_DEATH, (OnCreatureDeath event) -> onPlayerDeath(event), this));

		// Interacao com a lapide (corpo saqueavel)
		addFirstTalkId(GRAVE_NPC_ID);
		addTalkId(GRAVE_NPC_ID);

		// Bosses da zona: ajusta HP/dano/aura no spawn (mini-bosses solaveis) + drops no kill
		addSpawnId(BOSS_IDS);
		addKillId(BOSS_IDS);

		// Gatekeeper (Portador da Morte) - spawn + interacao + chamas vermelhas
		addFirstTalkId(GATEKEEPER_NPC_ID);
		addTalkId(GATEKEEPER_NPC_ID);
		final Npc gatekeeper = addSpawn(GATEKEEPER_NPC_ID, GK_X, GK_Y, GK_Z, GK_HEADING, false, 0);
		gatekeeper.setTitle("Portador da Morte");
		gatekeeper.setTargetable(true); // Hermit e targetable=false por padrao - libera pro jogador clicar
		gatekeeper.getEffectList().startAbnormalVisualEffect(AbnormalVisualEffect.DK_IGNITION_DARKELF_AVE);
		gatekeeper.updateAbnormalVisualEffects();
		gatekeeper.broadcastInfo();

		// Alerta global de atracao (servidor inteiro)
		if (ANNOUNCE_INTERVAL > 0)
		{
			final long period = TimeUnit.MINUTES.toMillis(ANNOUNCE_INTERVAL);
			ThreadPool.scheduleAtFixedRate(this::announceGlobal, period, period);
		}

		// Alerta interno de perigo (so quem esta dentro)
		if (CRITICAL_INTERVAL > 0)
		{
			final long period = TimeUnit.SECONDS.toMillis(CRITICAL_INTERVAL);
			ThreadPool.scheduleAtFixedRate(this::criticalInternal, period, period);
		}

		// Bloqueio de auto-farm: verifica a cada 1s quem esta dentro e desliga na hora
		ThreadPool.scheduleAtFixedRate(this::blockAutoPlayTick, 1000, 1000);

		// Controle de acesso anti-burla: revalida grade de quem esta dentro
		if (CHECK_INTERVAL > 0)
		{
			final long period = TimeUnit.SECONDS.toMillis(CHECK_INTERVAL);
			ThreadPool.scheduleAtFixedRate(this::checkAccessTick, period, period);
		}

		LOGGER.info("DeadZone: '" + ZONE_D_NAME + "' ativa (zona " + ZONE_D_ID + ").");
	}

	private void loadConfig()
	{
		final ConfigReader cfg = new ConfigReader("./config/Custom/DeadZone.ini");
		ENABLED = cfg.getBoolean("DeadZoneEnabled", false);
		BOSS_HP_FACTOR = Double.parseDouble(cfg.getString("DeadZoneBossHpFactor", "0.08"));
		BOSS_DMG_FACTOR = Double.parseDouble(cfg.getString("DeadZoneBossDamageFactor", "0.6"));
		BOSS_DEF_FACTOR = Double.parseDouble(cfg.getString("DeadZoneBossDefenceFactor", "0.3"));
		BOSS_ADENA_MIN = cfg.getInt("DeadZoneBossAdenaMin", 40000);
		BOSS_ADENA_MAX = cfg.getInt("DeadZoneBossAdenaMax", 120000);
		BOSS_LCOIN_MIN = cfg.getInt("DeadZoneBossLCoinMin", 5);
		BOSS_LCOIN_MAX = cfg.getInt("DeadZoneBossLCoinMax", 15);
		BOSS_JEWEL_BOX_ID = cfg.getInt("DeadZoneBossJewelBoxId", 102817);
		BOSS_JEWEL_CHANCE = cfg.getInt("DeadZoneBossJewelChance", 3);
		BOSS_CRAFT_DROP_COUNT = cfg.getInt("DeadZoneBossCraftDropCount", 2);
		final String[] mats = cfg.getString("DeadZoneBossCraftMaterials", "").split(",");
		final java.util.List<Integer> matList = new ArrayList<>();
		for (String m : mats)
		{
			final String t = m.trim();
			if (!t.isEmpty())
			{
				matList.add(Integer.parseInt(t));
			}
		}
		BOSS_CRAFT_MATERIALS = matList.stream().mapToInt(Integer::intValue).toArray();
		ZONE_D_ID = cfg.getInt("DeadZoneD_ZoneId", 90001);
		ZONE_D_NAME = cfg.getString("DeadZoneD_Name", "Zona Mortal");
		SCREEN_MESSAGE = cfg.getString("DeadZoneScreenMessage", "ATENCAO! Voce entrou na ZONA MORTAL.");
		ANNOUNCE_INTERVAL = cfg.getInt("DeadZoneAnnounceInterval", 30);
		ANNOUNCE_TEXT = cfg.getString("DeadZoneAnnounceText", "A ZONA MORTAL esta ativa!");
		CRITICAL_INTERVAL = cfg.getInt("DeadZoneCriticalInterval", 90);
		CRITICAL_TEXT = cfg.getString("DeadZoneCriticalText", "PERIGO! Voce esta na ZONA MORTAL.");
		DROP_ADENA = cfg.getBoolean("DeadZoneDropAdena", true);
		BREAK_ENABLED = cfg.getBoolean("DeadZoneBreakEnabled", true);
		BREAK_MIN_PCT = cfg.getInt("DeadZoneBreakMinPercent", 10);
		BREAK_MAX_PCT = cfg.getInt("DeadZoneBreakMaxPercent", 30);
		DEBRIS_ITEM_ID = cfg.getInt("DeadZoneDebrisItemId", 92911);
		GRAVE_NPC_ID = cfg.getInt("DeadZoneGraveNpcId", 18727);
		CORPSE_DURATION = cfg.getInt("DeadZoneCorpseDuration", 120);
		LOOT_DELAY = cfg.getInt("DeadZoneLootDelay", 2);
		MAX_GRADE = CrystalType.valueOf(cfg.getString("DeadZoneD_MaxGrade", "D").trim().toUpperCase());
		CHECK_LEVEL = cfg.getBoolean("DeadZoneCheckLevel", false);
		MIN_LEVEL = cfg.getInt("DeadZoneD_MinLevel", 20);
		MAX_LEVEL = cfg.getInt("DeadZoneD_MaxLevel", 40);
		EXIT_X = cfg.getInt("DeadZoneExitX", 15670);
		EXIT_Y = cfg.getInt("DeadZoneExitY", 142983);
		EXIT_Z = cfg.getInt("DeadZoneExitZ", -2705);
		CHECK_INTERVAL = cfg.getInt("DeadZoneCheckInterval", 60);
		MAX_VIOLATIONS = cfg.getInt("DeadZoneMaxViolations", 3);
		JAIL_MINUTES = cfg.getInt("DeadZoneJailMinutes", 5);
		KICK_WARN1 = cfg.getString("DeadZoneKickWarn1", "Equipamento acima da grade! Violacao 1/3.");
		KICK_WARN2 = cfg.getString("DeadZoneKickWarn2", "Equipamento acima da grade! Violacao 2/3 - ultima chance.");
		JAIL_MSG = cfg.getString("DeadZoneJailMsg", "Voce burlou a regra da Zona Mortal e foi preso.");
		GATEKEEPER_NPC_ID = cfg.getInt("DeadZoneGatekeeperNpcId", 18424);
		GK_X = cfg.getInt("DeadZoneGatekeeperX", 83400);
		GK_Y = cfg.getInt("DeadZoneGatekeeperY", 147943);
		GK_Z = cfg.getInt("DeadZoneGatekeeperZ", -3404);
		GK_HEADING = cfg.getInt("DeadZoneGatekeeperHeading", 0);
		ENTRANCE_X = cfg.getInt("DeadZoneEntranceX", -46768);
		ENTRANCE_Y = cfg.getInt("DeadZoneEntranceY", 54706);
		ENTRANCE_Z = cfg.getInt("DeadZoneEntranceZ", -2400);
		HIDE_ITEM_NAMES = cfg.getBoolean("DeadZoneHideItemNames", true);
		PROTECTED_ITEMS = new java.util.HashSet<>();
		for (String idStr : cfg.getString("DeadZoneProtectedItems", "").split(","))
		{
			final String trimmed = idStr.trim();
			if (!trimmed.isEmpty())
			{
				PROTECTED_ITEMS.add(Integer.parseInt(trimmed));
			}
		}
	}

	private static boolean isProtected(int itemId)
	{
		return PROTECTED_ITEMS.contains(itemId);
	}

	@Override
	public void onEnterZone(Creature creature, ZoneType zone)
	{
		if (!creature.isPlayer())
		{
			return;
		}

		final Player player = creature.asPlayer();

		// GMs entram sem restricao
		if (!player.isGM())
		{
			final String denial = validateAccess(player);
			if (denial != null)
			{
				player.teleToLocation(EXIT_X, EXIT_Y, EXIT_Z);
				player.sendPacket(new CreatureSay(null, ChatType.CRITICAL_ANNOUNCE, "", denial));
				player.sendMessage(denial);
				return;
			}
		}

		PLAYERS_INSIDE.add(player.getObjectId());

		// Bloqueia auto-farm
		AutoPlayTaskManager.getInstance().stopAutoPlay(player);

		// Aviso: a janela bonita aparece TODA vez que entra/teleporta para a zona
		showWarningWindow(player);
		player.sendPacket(new CreatureSay(null, ChatType.CRITICAL_ANNOUNCE, "", SCREEN_MESSAGE));
	}

	/**
	 * Valida se o jogador pode estar na zona. Retorna o motivo da recusa, ou null se liberado.
	 * Verifica grade (sempre) e nivel (se DeadZoneCheckLevel=True), cobrindo os 2 sets via isEquipable.
	 */
	private static String validateAccess(Player player)
	{
		// Nivel (opcional)
		if (CHECK_LEVEL)
		{
			final int lvl = player.getLevel();
			if ((lvl < MIN_LEVEL) || (lvl > MAX_LEVEL))
			{
				return "Esta zona e para niveis " + MIN_LEVEL + " a " + MAX_LEVEL + ". Voce nao pode entrar.";
			}
		}

		// Grade: nenhum equipavel (dos 2 sets) pode exceder a grade-teto
		for (Item item : player.getInventory().getItems())
		{
			if (item.isEquipable() && item.getTemplate().getCrystalType().isGreater(MAX_GRADE))
			{
				return "Seu equipamento excede a grade " + MAX_GRADE.name() + " permitida nesta zona!";
			}
		}

		return null;
	}

	@Override
	public void onExitZone(Creature creature, ZoneType zone)
	{
		if (!creature.isPlayer())
		{
			return;
		}
		PLAYERS_INSIDE.remove(creature.getObjectId());
	}

	private void showWarningWindow(Player player)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(player, "data/html/mods/deadzone/warning.htm");
		player.sendPacket(html);
	}

	/** Alerta global no chat de todos os online (atracao). */
	private void announceGlobal()
	{
		Broadcast.toAllOnlinePlayers(ANNOUNCE_TEXT, false);
	}

	/** Alerta critical so para quem esta dentro (perigo) + reforca bloqueio de autoplay. */
	private void criticalInternal()
	{
		if (PLAYERS_INSIDE.isEmpty())
		{
			return;
		}

		for (Integer objectId : PLAYERS_INSIDE)
		{
			final Player player = World.getInstance().getPlayer(objectId);
			if ((player == null) || !player.isOnline())
			{
				continue;
			}
			// mensagem central (branca, chama atencao pela posicao)
			player.sendPacket(new ExShowScreenMessage(CRITICAL_TEXT, ExShowScreenMessage.TOP_CENTER, 5000));
			// versao vermelha no chat (cor de perigo)
			player.sendPacket(new CreatureSay(null, ChatType.CRITICAL_ANNOUNCE, "", CRITICAL_TEXT));
		}
	}

	/** Roda a cada 1s: desliga o auto-farm de quem tentar usar dentro da zona. */
	private void blockAutoPlayTick()
	{
		if (PLAYERS_INSIDE.isEmpty())
		{
			return;
		}

		for (Integer objectId : PLAYERS_INSIDE)
		{
			final Player player = World.getInstance().getPlayer(objectId);
			if ((player == null) || !player.isOnline() || !player.isAutoPlaying())
			{
				continue;
			}
			AutoPlayTaskManager.getInstance().stopAutoPlay(player);
			player.sendMessage("Auto-farme nao funciona na Zona Mortal!");
		}
	}

	/** Roda a cada 60s: revalida a grade de quem esta dentro e aplica o sistema 3-strikes. */
	private void checkAccessTick()
	{
		if (PLAYERS_INSIDE.isEmpty())
		{
			return;
		}

		for (Integer objectId : PLAYERS_INSIDE)
		{
			final Player player = World.getInstance().getPlayer(objectId);
			if ((player == null) || !player.isOnline() || player.isGM())
			{
				continue;
			}

			final String denial = validateAccess(player);
			if (denial == null)
			{
				continue; // ok, segue dentro
			}

			// Violacao detectada: incrementa contador e pune progressivamente
			final int violations = VIOLATIONS.merge(objectId, 1, Integer::sum);

			// Kick da zona
			PLAYERS_INSIDE.remove(objectId);
			player.teleToLocation(EXIT_X, EXIT_Y, EXIT_Z);

			if (violations >= MAX_VIOLATIONS)
			{
				// JAIL na ultima violacao + reseta contador
				applyJail(player);
				VIOLATIONS.remove(objectId);
				player.sendPacket(new ExShowScreenMessage(JAIL_MSG, ExShowScreenMessage.MIDDLE_CENTER, 8000));
				player.sendPacket(new CreatureSay(null, ChatType.CRITICAL_ANNOUNCE, "", JAIL_MSG));
				LOGGER.info("DeadZone: " + player.getName() + " JAIL (" + violations + "/" + MAX_VIOLATIONS + ") - burla de grade.");
			}
			else
			{
				// Aviso progressivo
				final String warn = (violations == 1) ? KICK_WARN1 : KICK_WARN2;
				player.sendPacket(new ExShowScreenMessage(warn, ExShowScreenMessage.MIDDLE_CENTER, 8000));
				player.sendPacket(new CreatureSay(null, ChatType.CRITICAL_ANNOUNCE, "", warn));
				LOGGER.info("DeadZone: " + player.getName() + " kick por burla (" + violations + "/" + MAX_VIOLATIONS + ").");
			}
		}
	}

	/** Aplica jail no jogador via PunishmentManager. */
	private void applyJail(Player player)
	{
		final long expiration = System.currentTimeMillis() + (JAIL_MINUTES * 60_000L);
		PunishmentManager.getInstance().startPunishment(new PunishmentTask(player.getObjectId(), PunishmentAffect.CHARACTER, PunishmentType.JAIL, expiration, "Zona Mortal: burla de grade", "DeadZone"));
	}

	/** Boss spawnou: transforma em mini-boss solavel (HP reduzido, sem aura de raid). */
	@Override
	public void onSpawn(Npc npc)
	{
		// Ajusta cada boss SO UMA VEZ por objeto (mergeMul acumula no respawn = HP some)
		if (!ADJUSTED_BOSSES.add(npc.getObjectId()))
		{
			return;
		}

		// remove a aura/curse de raid boss (permite solo)
		if (npc.isAttackable())
		{
			npc.asAttackable().setIsRaid(false);
		}
		// reduz HP pra valor solavel (escala por nivel naturalmente)
		npc.getStat().mergeMul(Stat.MAX_HP, BOSS_HP_FACTOR);
		npc.setCurrentHp(npc.getMaxHp());
		// reduz dano pra nao matar instantaneo
		npc.getStat().mergeMul(Stat.PHYSICAL_ATTACK, BOSS_DMG_FACTOR);
		npc.getStat().mergeMul(Stat.MAGIC_ATTACK, BOSS_DMG_FACTOR);
		// reduz defesa pra skills/ataque do jogador causarem mais dano (mata mais rapido)
		npc.getStat().mergeMul(Stat.PHYSICAL_DEFENCE, BOSS_DEF_FACTOR);
		npc.getStat().mergeMul(Stat.MAGICAL_DEFENCE, BOSS_DEF_FACTOR);

		// inicia o telegraph AoE deste boss
		if (TELEGRAPH_ENABLED)
		{
			startTelegraph(npc);
		}
	}

	// ===== TELEGRAPH AoE =====

	private void startTelegraph(Npc boss)
	{
		final int objId = boss.getObjectId();
		stopTelegraph(objId);
		final ScheduledFuture<?> task = ThreadPool.scheduleAtFixedRate(() ->
		{
			try
			{
				if (boss == null || boss.isDead() || !boss.isSpawned())
				{
					stopTelegraph(objId);
					return;
				}
				// SO dispara se o boss esta em combate com players (quem nao luta nao toma)
				if (!boss.isInCombat())
				{
					return;
				}
				final List<Player> attackers = getBossAttackers(boss);
				if (attackers.isEmpty())
				{
					return;
				}
				// 40% de chance por ciclo
				if (Rnd.get(100) >= TELEGRAPH_CHANCE)
				{
					return;
				}
				// padrao aleatorio: 0=1 circulo grande, 1=3-4 circulos pequenos, 2=linha (pode ser diagonal)
				fireTelegraph(boss, attackers, Rnd.get(3));
			}
			catch (Exception ignored)
			{
			}
		}, TELEGRAPH_INTERVAL_MS, TELEGRAPH_INTERVAL_MS);
		TELEGRAPH_TASKS.put(objId, task);
	}

	/** Executa um padrao de telegraph: desenha a marca, espera, aplica dano em quem ficou (so attackers). */
	private void fireTelegraph(Npc boss, List<Player> attackers, int pattern)
	{
		final List<Player> viewers = World.getInstance().getVisibleObjectsInRange(boss, Player.class, 1800);
		final int z = boss.getZ();

		// ===== PADRAO 2: LINHA (varredura, pode ser diagonal) =====
		if (pattern == 2)
		{
			final double ang = Math.toRadians(Rnd.get(360));
			final int len = 900;
			final int ax = boss.getX();
			final int ay = boss.getY();
			final int bx = ax + (int) Math.round(Math.cos(ang) * len);
			final int by = ay + (int) Math.round(Math.sin(ang) * len);
			final int halfW = 130;
			for (Player p : viewers)
			{
				drawDangerLine(p, ax, ay, bx, by, z, halfW, TELEGRAPH_COLOR_WARN, "dz_tele0");
				p.sendPacket(new net.sf.l2jdev.gameserver.network.serverpackets.ExShowScreenMessage("PERIGO! Varredura incoming!", 2000));
			}
			ThreadPool.schedule(() ->
			{
				try
				{
					for (Player p : viewers)
					{
						clearTelegraph(p);
					}
					if (boss.isDead())
					{
						return;
					}
					for (Player p : getBossAttackers(boss))
					{
						if (!p.isDead() && pointInBand(p.getX(), p.getY(), ax, ay, bx, by, halfW))
						{
							p.reduceCurrentHp(p.getMaxHp() * TELEGRAPH_HP_PERCENT, boss, null);
							p.sendPacket(new net.sf.l2jdev.gameserver.network.serverpackets.ExShowScreenMessage("Voce foi atingido!", 1500));
						}
					}
				}
				catch (Exception ignored)
				{
				}
			}, TELEGRAPH_DELAY_MS);
			return;
		}

		// ===== PADRAO 0/1: CIRCULOS (1 grande ou 3-4 pequenos) =====
		final int[][] centers;
		if (pattern == 1)
		{
			final int n = 3 + Rnd.get(2); // 3 ou 4 circulos
			centers = new int[n][3];
			for (int i = 0; i < n; i++)
			{
				final Player t = attackers.get(Rnd.get(attackers.size()));
				centers[i][0] = t.getX() + Rnd.get(-90, 90);
				centers[i][1] = t.getY() + Rnd.get(-90, 90);
				centers[i][2] = TELEGRAPH_RADIUS / 2; // menores
			}
		}
		else
		{
			final Player t = attackers.get(Rnd.get(attackers.size()));
			centers = new int[][] { { t.getX(), t.getY(), TELEGRAPH_RADIUS } };
		}

		for (Player p : viewers)
		{
			for (int i = 0; i < centers.length; i++)
			{
				drawDangerCircle(p, centers[i][0], centers[i][1], z, centers[i][2], TELEGRAPH_COLOR_WARN, "dz_tele" + i);
			}
			p.sendPacket(new net.sf.l2jdev.gameserver.network.serverpackets.ExShowScreenMessage("PERIGO! Saia das areas marcadas!", 2000));
		}

		ThreadPool.schedule(() ->
		{
			try
			{
				for (Player p : viewers)
				{
					clearTelegraph(p);
				}
				if (boss.isDead())
				{
					return;
				}
				for (Player p : getBossAttackers(boss))
				{
					if (p.isDead())
					{
						continue;
					}
					for (int[] c : centers)
					{
						final double dist = Math.sqrt(Math.pow(p.getX() - c[0], 2) + Math.pow(p.getY() - c[1], 2));
						if (dist <= c[2])
						{
							p.reduceCurrentHp(p.getMaxHp() * TELEGRAPH_HP_PERCENT, boss, null);
							p.sendPacket(new net.sf.l2jdev.gameserver.network.serverpackets.ExShowScreenMessage("Voce foi atingido!", 1500));
							break;
						}
					}
				}
			}
			catch (Exception ignored)
			{
			}
		}, TELEGRAPH_DELAY_MS);
	}

	/** Players que estao na aggro list do boss (quem realmente luta). */
	private static List<Player> getBossAttackers(Npc boss)
	{
		final List<Player> result = new ArrayList<>();
		if (!boss.isAttackable())
		{
			return result;
		}
		for (Creature c : boss.asAttackable().getAggroList().keySet())
		{
			if ((c != null) && c.isPlayer() && !c.isDead() && (c.calculateDistance3D(boss) <= 2000))
			{
				result.add(c.asPlayer());
			}
		}
		return result;
	}

	/** Desenha uma faixa retangular (linha grossa) no chao. */
	private static void drawDangerLine(Player p, int ax, int ay, int bx, int by, int z, int halfW, int color, String name)
	{
		try
		{
			final double dx = bx - ax;
			final double dy = by - ay;
			final double len = Math.sqrt((dx * dx) + (dy * dy));
			if (len < 1)
			{
				return;
			}
			final net.sf.l2jdev.gameserver.network.serverpackets.ExServerPrimitive prim =
				new net.sf.l2jdev.gameserver.network.serverpackets.ExServerPrimitive(name, p.getX(), p.getY(), p.getZ());
			// faixas paralelas preenchendo a largura (de -halfW a +halfW)
			final double ux = -dy / len; // perpendicular unitario
			final double uy = dx / len;
			for (int off = -halfW; off <= halfW; off += 35)
			{
				final int ox = (int) Math.round(ux * off);
				final int oy = (int) Math.round(uy * off);
				prim.addLine(color, ax + ox, ay + oy, z, bx + ox, by + oy, z);
			}
			p.sendPacket(prim);
		}
		catch (Exception ignored)
		{
		}
	}

	/** Ponto dentro da faixa retangular (linha)? */
	private static boolean pointInBand(int px, int py, int ax, int ay, int bx, int by, int halfW)
	{
		final double dx = bx - ax;
		final double dy = by - ay;
		final double len2 = (dx * dx) + (dy * dy);
		if (len2 < 1)
		{
			return false;
		}
		final double t = (((px - ax) * dx) + ((py - ay) * dy)) / len2;
		if ((t < 0) || (t > 1))
		{
			return false;
		}
		final double projX = ax + (t * dx);
		final double projY = ay + (t * dy);
		final double perp = Math.sqrt(Math.pow(px - projX, 2) + Math.pow(py - projY, 2));
		return perp <= halfW;
	}

	/** Limpa todas as marcas de telegraph de um player (ate 4 circulos + linha). */
	private static void clearTelegraph(Player p)
	{
		for (int i = 0; i < 4; i++)
		{
			clearDangerCircle(p, "dz_tele" + i);
		}
	}

	private static void stopTelegraph(int objectId)
	{
		final ScheduledFuture<?> task = TELEGRAPH_TASKS.remove(objectId);
		if (task != null && !task.isCancelled())
		{
			task.cancel(false);
		}
	}

	/** Desenha um circulo PREENCHIDO no chao (borda + raios + aneis concentricos = aparencia solida). */
	private static void drawDangerCircle(Player p, int cx, int cy, int cz, int radius, int color, String name)
	{
		try
		{
			final int segments = 36;
			final net.sf.l2jdev.gameserver.network.serverpackets.ExServerPrimitive prim =
				new net.sf.l2jdev.gameserver.network.serverpackets.ExServerPrimitive(name, p.getX(), p.getY(), p.getZ());
			// (sem addPoint: o ponto nomeado gera um icone feio que alguns clientes nao removem na limpeza)
			final double step = (Math.PI * 2.0) / segments;
			// pre-calcula os pontos da borda
			final int[] bx = new int[segments + 1];
			final int[] by = new int[segments + 1];
			for (int i = 0; i <= segments; i++)
			{
				final double a = step * i;
				bx[i] = cx + (int) Math.round(Math.cos(a) * radius);
				by[i] = cy + (int) Math.round(Math.sin(a) * radius);
			}
			// borda + RAIOS do centro ate a borda (preenchimento "raios de sol")
			for (int i = 0; i < segments; i++)
			{
				prim.addLine(color, bx[i], by[i], cz, bx[i + 1], by[i + 1], cz); // borda
				prim.addLine(color, cx, cy, cz, bx[i], by[i], cz);               // raio
			}
			// aneis concentricos internos (75%, 50%, 25%) pra dar volume
			for (double f = 0.75; f >= 0.24; f -= 0.25)
			{
				final int rr = (int) (radius * f);
				int prevX = cx + rr;
				int prevY = cy;
				for (int i = 1; i <= segments; i++)
				{
					final double a = step * i;
					final int x = cx + (int) Math.round(Math.cos(a) * rr);
					final int y = cy + (int) Math.round(Math.sin(a) * rr);
					prim.addLine(color, prevX, prevY, cz, x, y, cz);
					prevX = x;
					prevY = y;
				}
			}
			p.sendPacket(prim);
		}
		catch (Exception ignored)
		{
		}
	}

	private static void clearDangerCircle(Player p, String name)
	{
		try
		{
			final net.sf.l2jdev.gameserver.network.serverpackets.ExServerPrimitive prim =
				new net.sf.l2jdev.gameserver.network.serverpackets.ExServerPrimitive(name, p.getX(), p.getY(), p.getZ());
			p.sendPacket(prim);
		}
		catch (Exception ignored)
		{
		}
	}

	/** Boss morto: dropa adena + L-Coin + chance da Sapphire Box (exclusiva da zona D). */
	@Override
	public void onKill(Npc npc, Player killer, boolean isSummon)
	{
		// para o telegraph deste boss
		stopTelegraph(npc.getObjectId());

		if (killer == null)
		{
			return;
		}

		// Adena: aleatorio entre min e max
		final long adena = BOSS_ADENA_MIN + Rnd.get(Math.max(1, BOSS_ADENA_MAX - BOSS_ADENA_MIN));
		if (adena > 0)
		{
			killer.addItem(ItemProcessType.REWARD, 57, adena, npc, true);
		}
		// L-Coin: aleatorio entre min e max
		final int lcoin = BOSS_LCOIN_MIN + Rnd.get(Math.max(1, BOSS_LCOIN_MAX - BOSS_LCOIN_MIN + 1));
		if (lcoin > 0)
		{
			killer.addItem(ItemProcessType.REWARD, 91663, lcoin, npc, true);
		}
		// Materiais de craft (lixo pro Random Craft) - sorteia alguns da lista
		if ((BOSS_CRAFT_MATERIALS.length > 0) && (BOSS_CRAFT_DROP_COUNT > 0))
		{
			for (int i = 0; i < BOSS_CRAFT_DROP_COUNT; i++)
			{
				final int matId = BOSS_CRAFT_MATERIALS[Rnd.get(BOSS_CRAFT_MATERIALS.length)];
				killer.addItem(ItemProcessType.REWARD, matId, 1, npc, true);
			}
		}
		// Sapphire (drop exclusivo da zona) - chance
		if (Rnd.get(100) < BOSS_JEWEL_CHANCE)
		{
			killer.addItem(ItemProcessType.REWARD, BOSS_JEWEL_BOX_ID, 1, npc, true);
			killer.sendPacket(new ExShowScreenMessage("Voce achou uma SAPPHIRE! Joia exclusiva da Zona Mortal!", ExShowScreenMessage.MIDDLE_CENTER, 6000));
		}
	}

	/** Verifica se o NPC e um boss da zona. */
	public static boolean isZoneBoss(int npcId)
	{
		for (int id : BOSS_IDS)
		{
			if (id == npcId)
			{
				return true;
			}
		}
		return false;
	}

	/** Janela de status + drops do boss (chamada pelo shift+click via NpcActionShift). */
	public static void showBossDropWindow(Npc npc, Player player)
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("<html><title>").append(npc.getName()).append("</title><body>");
		sb.append("<table width=290 height=350 background=\"L2UI_CT1.HtmlWnd_DF_TextureKnight\"><tr><td align=center valign=top>");
		sb.append("<br><br><br><br><br><br><br><br><br><br><br>");
		sb.append("<font color=\"FF4444\"><font name=\"hs12\">").append(npc.getName()).append("</font></font><br1>");
		sb.append("<font color=\"888888\">Nivel ").append(npc.getLevel()).append("  |  HP ").append((long) npc.getCurrentHp()).append(" / ").append(npc.getMaxHp()).append("</font><br1>");
		sb.append("<img src=\"L2UI.SquareGray\" width=260 height=1><br>");
		sb.append("<font color=\"LEVEL\">== DROPS ==</font><br>");
		sb.append("<font color=\"CCAA44\">Adena</font>: ").append(BOSS_ADENA_MIN).append(" - ").append(BOSS_ADENA_MAX).append("<br1>");
		sb.append("<font color=\"CCAA44\">L-Coin</font>: ").append(BOSS_LCOIN_MIN).append(" - ").append(BOSS_LCOIN_MAX).append("<br1>");
		sb.append("<font color=\"CCAA44\">Materiais de Craft</font>: ").append(BOSS_CRAFT_DROP_COUNT).append("x (100%)<br1>");
		sb.append("<br><font color=\"3399FF\">>> Sapphire Lv.1 <<</font><br1>");
		sb.append("<font color=\"3399FF\">Joia EXCLUSIVA - chance ").append(BOSS_JEWEL_CHANCE).append("%</font><br>");
		sb.append("</td></tr></table></body></html>");
		final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId(), sb.toString());
		player.sendPacket(html);
	}

	/** Morte detectada: se foi dentro da zona, o jogador perde TUDO. */
	private void onPlayerDeath(OnCreatureDeath event)
	{
		if (!event.getTarget().isPlayer())
		{
			return;
		}

		final Player player = event.getTarget().asPlayer();
		if (!PLAYERS_INSIDE.contains(player.getObjectId()))
		{
			return;
		}

		handleDeathLoot(player);
	}

	/** Coleta equipado + inventario + adena, remove do jogador e guarda o loot. */
	private void handleDeathLoot(Player player)
	{
		final List<LootEntry> loot = new ArrayList<>();
		int count = 0;

		// Separa em EQUIPAVEIS (candidatos a quebra - cobre Set A equipado E Set B no inventario)
		// e OUTROS (consumiveis/materiais - vao inteiros). Adena tratada a parte.
		final List<Item> equippables = new ArrayList<>();
		final List<Item> others = new ArrayList<>();
		for (Item item : new ArrayList<>(player.getInventory().getItems()))
		{
			if (isProtected(item.getId()) || (item.getId() == 57))
			{
				continue;
			}
			if (item.isEquipable())
			{
				equippables.add(item);
			}
			else
			{
				others.add(item);
			}
		}

		// QUEBRA sobre TODOS os equipaveis (os dois sets - Set B nao e refugio)
		int broken = 0;
		if (BREAK_ENABLED && !equippables.isEmpty())
		{
			final int pct = (BREAK_MIN_PCT >= BREAK_MAX_PCT) ? BREAK_MIN_PCT : Rnd.get(BREAK_MIN_PCT, BREAK_MAX_PCT);
			broken = (int) Math.ceil((equippables.size() * pct) / 100.0);
			broken = Math.max(1, Math.min(broken, equippables.size()));
			Collections.shuffle(equippables);
			LOGGER.info("DeadZone: quebra " + pct + "% de " + equippables.size() + " equipaveis (2 sets) = " + broken + " derretidos.");
		}

		for (int i = 0; i < equippables.size(); i++)
		{
			final Item item = equippables.get(i);
			if (i < broken)
			{
				loot.add(new LootEntry(DEBRIS_ITEM_ID, 1, 0));
			}
			else
			{
				loot.add(new LootEntry(item.getId(), item.getCount(), item.getEnchantLevel()));
			}
			player.getInventory().destroyItem(ItemProcessType.DESTROY, item, player, null);
			count++;
		}

		// Itens nao-equipaveis (consumiveis, materiais) vao inteiros
		for (Item item : others)
		{
			loot.add(new LootEntry(item.getId(), item.getCount(), item.getEnchantLevel()));
			player.getInventory().destroyItem(ItemProcessType.DESTROY, item, player, null);
			count++;
		}

		// 3) adena
		if (DROP_ADENA)
		{
			final long adena = player.getAdena();
			if (adena > 0)
			{
				loot.add(new LootEntry(57, adena, 0));
				player.reduceAdena(ItemProcessType.DESTROY, adena, player, false);
			}
		}

		// spawna a lapide (corpo saqueavel) na posicao da morte
		final Npc grave = addSpawn(GRAVE_NPC_ID, player.getX(), player.getY(), player.getZ() + 10, 0, false, CORPSE_DURATION * 1000L);
		grave.setTitle("Tumulo de " + player.getName());
		grave.broadcastInfo();

		final int graveObjId = grave.getObjectId();
		CORPSE_LOOT.put(graveObjId, loot);
		// expira o loot junto com a lapide (DESTROY o que sobrar)
		ThreadPool.schedule(() -> expireCorpse(graveObjId), CORPSE_DURATION * 1000L);

		// Atualiza paperdoll com delay (cliente precisa processar a morte antes do update)
		ThreadPool.schedule(() ->
		{
			player.sendPacket(new ExUserInfoEquipSlot(player, true));
			player.sendItemList();
			player.broadcastUserInfo();
		}, 500L);

		player.sendPacket(new ExShowScreenMessage("Voce MORREU na Zona Mortal e perdeu TUDO!", ExShowScreenMessage.MIDDLE_CENTER, 8000));
		player.sendPacket(new CreatureSay(null, ChatType.CRITICAL_ANNOUNCE, "", "Voce perdeu " + count + " itens e sua adena na Zona Mortal!"));
		LOGGER.info("DeadZone: " + player.getName() + " perdeu " + count + " itens + adena. Tumulo objId=" + graveObjId);
	}

	/** Clicar na lapide: mostra os itens do tumulo com links para saquear. */
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		// Gatekeeper (Portador da Morte)
		if (npc.getId() == GATEKEEPER_NPC_ID)
		{
			showGatekeeperWindow(npc, player);
			return null;
		}

		// Lapide (corpo saqueavel)
		final List<LootEntry> loot = CORPSE_LOOT.get(npc.getObjectId());
		if (loot == null)
		{
			return null;
		}

		showCorpseWindow(npc, player, loot);
		return null;
	}

	/** Janela do Gatekeeper - padrao premium com fundo da textura. */
	private void showGatekeeperWindow(Npc npc, Player player)
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("<html><title>PORTADOR DA MORTE</title><body>");
		sb.append("<table width=290 height=350 background=\"L2UI_CT1.HtmlWnd_DF_TextureKnight\"><tr><td align=center valign=top>");
		sb.append("<br><br><br><br><br><br><br><br><br><br><br><br><br>");
		sb.append("<font color=\"FF4444\"><font name=\"hs12\">ZONA MORTAL</font></font><br1>");
		sb.append("<img src=\"L2UI.SquareGray\" width=260 height=1><br>");
		sb.append("<font color=\"AAAAAA\">Area de risco extremo. Morreu aqui,<br1>perde TUDO. So entram com a grade da zona.</font><br1>");
		sb.append("<font color=\"CCAA44\">Cada zona tem DROPS UNICOS e uteis!</font><br>");
		sb.append("<br>");
		sb.append("<button value=\"Entrar Zona D\" action=\"bypass Quest DeadZone enter D\" width=150 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"><br1>");
		sb.append("<font color=\"888888\"><font name=\"hs9\">Recomendado: 25 GS e Lvl 45+</font></font><br>");
		sb.append("<button value=\"Entrar Zona C\" action=\"bypass Quest DeadZone enter C\" width=150 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"><br1>");
		sb.append("<button value=\"Entrar Zona B\" action=\"bypass Quest DeadZone enter B\" width=150 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
		sb.append("</td></tr></table></body></html>");
		final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId(), sb.toString());
		player.sendPacket(html);
	}

	private void showCorpseWindow(Npc npc, Player player, List<LootEntry> loot)
	{
		final StringBuilder sb = new StringBuilder();
		// Extrai o nome do jogador do titulo do NPC ("Tumulo de NOME" -> "NOME")
		final String playerName = npc.getTitle().replace("Tumulo de ", "");
		sb.append("<html><title>TUMULO DE ").append(playerName).append("</title><body>");
		sb.append("<table width=290 height=350 background=\"L2UI_CT1.HtmlWnd_DF_TextureKnight\"><tr><td align=center valign=top>");
		sb.append("<br><br><br><br><br><br><br><br><br><br>");
		sb.append("<img src=\"L2UI.SquareGray\" width=260 height=1><br>");

		for (int i = 0; i < loot.size(); i++)
		{
			final LootEntry entry = loot.get(i);
			final ItemTemplate tmpl = ItemData.getInstance().getTemplate(entry.id);
			final String realName = (tmpl != null) ? tmpl.getName() : ("Item " + entry.id);
			final String display;
			if (HIDE_ITEM_NAMES)
			{
				final StringBuilder mask = new StringBuilder();
				for (int c = 0; c < realName.length(); c++)
				{
					mask.append(realName.charAt(c) == ' ' ? ' ' : '?');
				}
				display = (entry.count > 1) ? mask.toString() + " x" + entry.count : mask.toString();
			}
			else
			{
				display = (entry.count > 1) ? realName + " x" + entry.count : realName;
			}
			sb.append("<a action=\"bypass Quest DeadZone loot ")
				.append(npc.getObjectId()).append(" ").append(i).append("\">")
				.append(display).append("</a><br1>");
		}

		sb.append("</td></tr></table></body></html>");
		final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId(), sb.toString());
		player.sendPacket(html);
	}

	/** Bypass: jogador clicou em um item para saquear. Inicia o delay de 2s. */
	@Override
	public String onEvent(String event, Npc npc, Player player)
	{
		// Gatekeeper: teleporta pra zona validando a grade ANTES
		if (event.startsWith("enter"))
		{
			// Zonas C e B ainda nao implementadas
			if (event.equals("enter C") || event.equals("enter B"))
			{
				player.sendMessage("Esta zona ainda esta em construcao. Em breve!");
				return null;
			}

			// Zona D
			final String denial = player.isGM() ? null : validateAccess(player);
			if (denial != null)
			{
				player.sendPacket(new CreatureSay(null, ChatType.CRITICAL_ANNOUNCE, "", denial));
				player.sendMessage(denial + " O Portador da Morte recusa sua entrada.");
				return null;
			}
			player.teleToLocation(ENTRANCE_X, ENTRANCE_Y, ENTRANCE_Z);
			return null;
		}

		if (!event.startsWith("loot "))
		{
			return null;
		}

		final StringTokenizer st = new StringTokenizer(event);
		st.nextToken(); // "loot"
		final int graveObjId = Integer.parseInt(st.nextToken());
		final int itemIndex = Integer.parseInt(st.nextToken());

		final List<LootEntry> loot = CORPSE_LOOT.get(graveObjId);
		if (loot == null)
		{
			player.sendMessage("Este tumulo ja nao existe.");
			return null;
		}

		if (itemIndex >= loot.size())
		{
			player.sendMessage("Este item ja foi saqueado.");
			return null;
		}

		// Cancela saque anterior se ainda estava tentando
		final ScheduledFuture<?> existing = ACTIVE_LOOTS.remove(player.getObjectId());
		if (existing != null)
		{
			existing.cancel(false);
		}

		// Registra posicao inicial
		LOOT_POSITIONS.put(player.getObjectId(), new int[]{ player.getX(), player.getY(), player.getZ() });

		final LootEntry target = loot.get(itemIndex);
		final ItemTemplate tmpl = ItemData.getInstance().getTemplate(target.id);
		final String itemName = (tmpl != null) ? tmpl.getName() : ("Item " + target.id);

		final String lootingMsg = HIDE_ITEM_NAMES ? "Saqueando ???... nao se mova por " + LOOT_DELAY + " segundos!" : "Saqueando " + itemName + "... nao se mova por " + LOOT_DELAY + " segundos!";

		// Fecha o HTML pra barra ficar visivel
		player.sendPacket(ActionFailed.STATIC_PACKET);

		player.sendMessage(lootingMsg);

		// Barra de cast como indicador de progresso do saque (skill 2 = Wind Attack, s/ efeito real)
		final int delayMs = LOOT_DELAY * 1000;
		player.sendPacket(new MagicSkillUse(player, player, 2, 1, delayMs, 0));

		final ScheduledFuture<?> task = ThreadPool.schedule(() ->
			completeLoot(player, graveObjId, itemIndex, npc), LOOT_DELAY * 1000L);

		ACTIVE_LOOTS.put(player.getObjectId(), task);
		return null;
	}

	/** Chamado apos o delay: verifica se o jogador moveu e entrega o item. */
	private void completeLoot(Player player, int graveObjId, int itemIndex, Npc npc)
	{
		ACTIVE_LOOTS.remove(player.getObjectId());
		final int[] startPos = LOOT_POSITIONS.remove(player.getObjectId());

		if (startPos == null || !player.isOnline())
		{
			return;
		}

		// Verifica se moveu (tolerancia de 50 unidades)
		final int dx = player.getX() - startPos[0];
		final int dy = player.getY() - startPos[1];
		if ((dx * dx + dy * dy) > (50 * 50))
		{
			player.sendPacket(new MagicSkillCanceled(player.getObjectId()));
			player.sendMessage("Saque cancelado! Voce se moveu.");
			return;
		}

		final List<LootEntry> loot = CORPSE_LOOT.get(graveObjId);
		if (loot == null || itemIndex >= loot.size())
		{
			player.sendMessage("Este item nao esta mais disponivel.");
			return;
		}

		final LootEntry entry = loot.remove(itemIndex);
		// inventory.addItem (baixo nivel) NAO envia packet ao cliente - evita o item "+0 fantasma"
		final Item received = player.getInventory().addItem(ItemProcessType.PICKUP, entry.id, entry.count, player, npc);
		if (received != null && entry.enchant > 0 && !received.isStackable())
		{
			received.setEnchantLevel(entry.enchant);
			received.updateDatabase(true);
		}
		// envia o inventario completo de uma vez, ja com o enchant correto
		player.sendItemList();

		final ItemTemplate tmpl = ItemData.getInstance().getTemplate(entry.id);
		final String itemName = (tmpl != null) ? tmpl.getName() : ("Item " + entry.id);
		final String enchantStr = (entry.enchant > 0) ? " +" + entry.enchant : "";
		player.sendMessage("Voce saqueou: " + enchantStr + itemName + (entry.count > 1 ? " x" + entry.count : "") + "!");

		// Atualiza a janela se ainda ha itens; senao fecha o dialogo
		if (!loot.isEmpty() && (npc != null) && !npc.isDead())
		{
			showCorpseWindow(npc, player, loot);
		}
		else
		{
			// tumulo vazio: fecha a janela
			player.sendPacket(ActionFailed.STATIC_PACKET);
			final NpcHtmlMessage empty = new NpcHtmlMessage(npc != null ? npc.getObjectId() : 0, "<html><body></body></html>");
			player.sendPacket(empty);
		}
	}

	/** Lapide expirou: DESTROY o que sobrou (so remove do mapa, ja que nao esta no inventario de ninguem). */
	private void expireCorpse(int graveObjId)
	{
		final List<LootEntry> loot = CORPSE_LOOT.remove(graveObjId);
		if (loot != null)
		{
			LOGGER.info("DeadZone: tumulo objId=" + graveObjId + " expirou, " + loot.size() + " itens destruidos.");
		}
	}

	public static void main(String[] args)
	{
		new DeadZone();
	}
}
