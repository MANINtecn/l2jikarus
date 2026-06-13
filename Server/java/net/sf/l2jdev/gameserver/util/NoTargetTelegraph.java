package net.sf.l2jdev.gameserver.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.variables.PlayerVariables;
import net.sf.l2jdev.gameserver.network.serverpackets.NoTargetConePacket;

/**
 * Mod No-Target (skillshot): desenha no chão, em tempo real, o cone de mira na direção em que o
 * personagem está virado — linha central (mira), bordas do cone (120°) e um arco fechando a ponta.
 * Serve como "telegraph" de onde a skill/ataque direcional vai cair, dando a sensação hack'n'slash.
 *
 * O desenho é atualizado por uma task leve enquanto o mod está ativo, e só re-envia o pacote quando
 * o personagem realmente muda de posição ou direção (evita spam de rede quando parado).
 */
public final class NoTargetTelegraph
{
	private static final Map<Integer, ScheduledFuture<?>> TASKS = new ConcurrentHashMap<>();
	private static final Map<Integer, Long> LAST_STATE = new ConcurrentHashMap<>();
	private static final Map<Integer, Integer> RANGE_BY_PLAYER = new ConcurrentHashMap<>();

	private static final boolean ENABLED = true; // telegraph ligado
	private static final String PRIMITIVE_NAME = "ntm_cone"; // nome fixo: o cliente substitui o desenho anterior (sem rastro)
	private static final int CONE_HALF_ANGLE = 45; // cone total de 90° (bate com o dano)
	private static final int CONE_RANGE = 700; // alcance representativo da mira (default ate usar uma skill)
	private static final int ARC_SEGMENTS = 14; // suavidade do arco da ponta
	private static final int ORIGIN_Z_OFFSET = -600; // origem enterrada (esconde o icone). Testar se com redesenho sempre nao desloca mais
	private static final long PERIOD_MS = 150L; // redesenho rapido (acompanha o personagem na hora)

	// Cores (0xRRGGBB)
	private static final int COLOR_CENTER = 0x00FF00; // verde puro (mira)
	private static final int COLOR_EDGE = 0x00AA00; // verde escuro (bordas/arco)

	private NoTargetTelegraph()
	{
	}

	/** Inicia o telegraph contínuo do cone para o player. */
	public static void start(Player player)
	{
		if (player == null)
		{
			return;
		}
		if (!ENABLED)
		{
			clear(player); // garante que nao fica desenho antigo na tela
			return;
		}
		final int oid = player.getObjectId();
		stop(oid);
		LAST_STATE.remove(oid);
		final ScheduledFuture<?> task = ThreadPool.scheduleAtFixedRate(() -> tick(oid), PERIOD_MS, PERIOD_MS);
		TASKS.put(oid, task);
	}

	/** Para o telegraph e apaga o desenho do chão. */
	public static void stop(Player player)
	{
		if (player == null)
		{
			return;
		}
		stop(player.getObjectId());
		clear(player);
	}

	private static void stop(int oid)
	{
		final ScheduledFuture<?> task = TASKS.remove(oid);
		if (task != null)
		{
			task.cancel(false);
		}
		LAST_STATE.remove(oid);
	}

	private static void tick(int oid)
	{
		try
		{
			final Player p = World.getInstance().getPlayer(oid);
			if ((p == null) || !p.isOnline() || !p.getVariables().getBoolean(PlayerVariables.NO_TARGET_MOD, false))
			{
				stop(oid);
				return;
			}

			// Redesenha SEMPRE: garante que o cone acompanha o personagem na hora e reaparece rapido
			// se algo (ex: ataque) limpar o desenho no cliente.
			draw(p);
		}
		catch (Exception ignored)
		{
		}
	}

	/**
	 * Define o alcance da última skill ofensiva usada pelo player — o cone passa a refletir
	 * esse alcance (treina a distância de cada skill). Força um redesenho imediato.
	 */
	public static void setSkillRange(Player player, int range)
	{
		if (player == null)
		{
			return;
		}
		final int r = Math.max(60, Math.min(range, 2000));
		RANGE_BY_PLAYER.put(player.getObjectId(), Integer.valueOf(r));
		LAST_STATE.remove(player.getObjectId()); // força redesenho na próxima tick
	}

	private static int rangeOf(Player p)
	{
		final Integer r = RANGE_BY_PLAYER.get(p.getObjectId());
		return r != null ? r.intValue() : CONE_RANGE;
	}

	/** Codifica posição (grade de 32) + direção (grade de 4°) + alcance para detectar mudança relevante. */
	private static long encodeState(Player p)
	{
		final long gx = p.getX() >> 5;
		final long gy = p.getY() >> 5;
		final long heading = (long) (LocationUtil.convertHeadingToDegree(p.getHeading()) / 4.0);
		final long range = rangeOf(p) >> 4;
		return ((range & 0xFFL) << 56) | ((gx & 0xFFFFFL) << 28) | ((gy & 0xFFFFFL) << 8) | (heading & 0xFF);
	}

	private static void draw(Player p)
	{
		final int cx = p.getX();
		final int cy = p.getY();
		final int cz = p.getZ() + 8; // levemente acima do chão para não sumir no terreno
		final int range = rangeOf(p);
		final double headingDeg = LocationUtil.convertHeadingToDegree(p.getHeading());
		final double headingRad = Math.toRadians(headingDeg);
		final double leftRad = Math.toRadians(headingDeg - CONE_HALF_ANGLE);
		final double rightRad = Math.toRadians(headingDeg + CONE_HALF_ANGLE);

		final NoTargetConePacket prim = new NoTargetConePacket(PRIMITIVE_NAME, cx, cy, cz + ORIGIN_Z_OFFSET);

		// Linha central de mira (brilhante)
		final int centerX = cx + (int) (range * Math.cos(headingRad));
		final int centerY = cy + (int) (range * Math.sin(headingRad));
		prim.addLine(COLOR_CENTER, cx, cy, cz, centerX, centerY, cz);

		// Bordas do cone
		final int leftX = cx + (int) (range * Math.cos(leftRad));
		final int leftY = cy + (int) (range * Math.sin(leftRad));
		final int rightX = cx + (int) (range * Math.cos(rightRad));
		final int rightY = cy + (int) (range * Math.sin(rightRad));
		prim.addLine(COLOR_EDGE, cx, cy, cz, leftX, leftY, cz);
		prim.addLine(COLOR_EDGE, cx, cy, cz, rightX, rightY, cz);

		// Arco fechando a ponta do cone
		int prevX = leftX;
		int prevY = leftY;
		for (int i = 1; i <= ARC_SEGMENTS; i++)
		{
			final double a = leftRad + (((rightRad - leftRad) * i) / ARC_SEGMENTS);
			final int ax = cx + (int) (range * Math.cos(a));
			final int ay = cy + (int) (range * Math.sin(a));
			prim.addLine(COLOR_EDGE, prevX, prevY, cz, ax, ay, cz);
			prevX = ax;
			prevY = ay;
		}

		p.sendPacket(prim);
	}

	/** Apaga o desenho (envia primitive vazio com o mesmo nome). */
	public static void clear(Player player)
	{
		try
		{
			player.sendPacket(new NoTargetConePacket(PRIMITIVE_NAME, player.getX(), player.getY(), player.getZ()));
		}
		catch (Exception ignored)
		{
		}
	}
}
