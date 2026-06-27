/*
 * #83 - Combate livre (PvP) em volta de qualquer Raid Boss vivo - L2 Ikarus.
 * A cada PERIOD, qualquer jogador dentro de RANGE de um raid boss vivo entra
 * em ZoneId.PVP (combate livre, sem karma). Sai do range -> volta ao normal.
 * Cobre TODOS os raid bosses do mundo automaticamente (sem precisar zona estatica
 * por boss). Os epics/grand bosses tambem tem zonas estaticas em zones/pvp.xml
 * (o contador de zona soma sem conflito).
 */
package custom.BossPvpZone;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.script.Script;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;

public class BossPvpZone extends Script
{
	private static final Logger LOGGER = Logger.getLogger(BossPvpZone.class.getName());

	private static final int RANGE = 2000;    // raio (em unidades) do flag PvP em volta do raid boss
	private static final long PERIOD = 5000L;  // intervalo da checagem (ms)

	// objectIds dos players que NOS flagamos (pra balancear o contador de zona ao sair)
	private final Set<Integer> _flagged = ConcurrentHashMap.newKeySet();

	private BossPvpZone()
	{
		ThreadPool.scheduleAtFixedRate(this::check, PERIOD, PERIOD);
		LOGGER.info("BossPvpZone: ativo - combate livre (PvP) num raio de " + RANGE + " de qualquer Raid Boss vivo.");
	}

	private void check()
	{
		try
		{
			final Set<Integer> nowInRange = new HashSet<>();
			for (Player player : World.getInstance().getPlayers())
			{
				if ((player == null) || !player.isOnline())
				{
					continue;
				}

				final boolean nearBoss = !World.getInstance().getVisibleObjectsInRange(player, Npc.class, RANGE, npc -> npc.isRaid() && !npc.isDead()).isEmpty();
				if (nearBoss)
				{
					nowInRange.add(player.getObjectId());
					// mantem a flag PvP (nome roxo) enquanto perto do boss - refresh a cada tick
					player.updatePvPFlag(1);
					if (_flagged.add(player.getObjectId()))
					{
						player.setInsideZone(ZoneId.PVP, true);
						player.sendMessage("Combate livre: voce entrou na area de um Raid Boss. Voce esta flagado (PvP).");
					}
				}
			}

			// remove o flag de quem saiu do range de todos os bosses
			_flagged.removeIf(objectId ->
			{
				if (nowInRange.contains(objectId))
				{
					return false;
				}
				final Player p = World.getInstance().getPlayer(objectId);
				if (p != null)
				{
					p.setInsideZone(ZoneId.PVP, false);
					p.sendMessage("Voce saiu da area de Raid Boss.");
				}
				return true;
			});
		}
		catch (Exception e)
		{
			LOGGER.warning("BossPvpZone: erro no check - " + e.getMessage());
		}
	}

	public static void main(String[] args)
	{
		new BossPvpZone();
	}
}
