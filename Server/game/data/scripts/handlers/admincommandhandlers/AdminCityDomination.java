/*
 * L2 Ikarus Intercrow - Admin commands para o mod City Domination
 * Permite forcar guerra, status, troca de level e reset de owner.
 *
 * Comandos:
 *   //cd                       - help
 *   //cd status                - estado de todas cidades
 *   //cd forcewar              - liga war agora (guardians ficam vulneraveis)
 *   //cd endwar                - desliga war agora (guardians invul + full HP)
 *   //cd setlevel <city> <lvl> - troca level do guardian (respawn)
 *   //cd resetowner <city>     - remove dono atual da cidade
 *   //cd kill <city>           - mata o guardian (dispara onGuardianKilled)
 *   //cd tp <city>             - teleporta admin pra cidade
 */
package handlers.admincommandhandlers;

import java.lang.reflect.Field;
import java.util.Map;

import ai.citydomination.CityGuardianAI;

import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.data.xml.NpcData;
import net.sf.l2jdev.gameserver.handler.IAdminCommandHandler;
import net.sf.l2jdev.gameserver.model.Spawn;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.citydomination.CityDominationManager;
import net.sf.l2jdev.gameserver.model.citydomination.CityDominationManager.CityConfig;
import net.sf.l2jdev.gameserver.model.citydomination.CityDominationManager.LevelStats;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2jdev.gameserver.util.Broadcast;

public class AdminCityDomination implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_cd",
		"admin_cd_status",
		"admin_cd_forcewar",
		"admin_cd_endwar",
		"admin_cd_setlevel",
		"admin_cd_resetowner",
		"admin_cd_reset",
		"admin_cd_kill",
		"admin_cd_tp",
		"admin_cd_conquest",
		"admin_cd_open"
	};

	@Override
	public boolean onCommand(String command, Player activeChar)
	{
		final CityDominationManager mgr = CityDominationManager.getInstance();
		final String[] parts = command.split(" ");
		final String cmd = parts[0];

		// //cd                          -> "admin_cd"             -> help
		if (cmd.equalsIgnoreCase("admin_cd"))
		{
			showHelp(activeChar);
			return true;
		}

		if (cmd.equalsIgnoreCase("admin_cd_status"))
		{
			showStatus(activeChar, mgr);
			return true;
		}

		if (cmd.equalsIgnoreCase("admin_cd_forcewar"))
		{
			setWarActive(mgr, true);
			int count = 0;
			for (String cityId : mgr.getCities().keySet())
			{
				Npc g = mgr.getGuardian(cityId);
				if (g != null && !g.isDead())
				{
					// CRITICO: disableCoreAI(true) bloqueia o cliente de iniciar auto-attack.
					// Tem que liberar tudo pra player conseguir bater no boss.
					try { g.disableCoreAI(false); } catch (Exception ignored) {}
					try { g.setImmobilized(false); } catch (Exception ignored) {}
					try { g.setInvul(false); } catch (Exception ignored) {}
					try { g.setTargetable(true); } catch (Exception ignored) {}
					try { g.setAutoAttackable(true); } catch (Exception ignored) {}
					count++;
				}
			}
			Broadcast.toAllOnlinePlayers("=== ADMIN: WAR FORCADA! " + count + " guardian(s) vulneravel(eis) ===", true);
			activeChar.sendMessage("[CD] War forcada. " + count + " guardian(s) atacavel(eis).");
			return true;
		}

		if (cmd.equalsIgnoreCase("admin_cd_endwar"))
		{
			setWarActive(mgr, false);
			int count = 0;
			for (String cityId : mgr.getCities().keySet())
			{
				Npc g = mgr.getGuardian(cityId);
				if (g != null && !g.isDead())
				{
					try { g.disableCoreAI(true); } catch (Exception ignored) {}
					try { g.setImmobilized(true); } catch (Exception ignored) {}
					try { g.setInvul(true); } catch (Exception ignored) {}
					try { g.setCurrentHp(g.getMaxHp()); } catch (Exception ignored) {}
					try { g.setAutoAttackable(false); } catch (Exception ignored) {}
					count++;
				}
			}
			Broadcast.toAllOnlinePlayers("=== ADMIN: WAR ENCERRADA! " + count + " guardian(s) protegido(s) ===", true);
			activeChar.sendMessage("[CD] War encerrada. " + count + " guardian(s) full HP + invul.");
			return true;
		}

		if (cmd.equalsIgnoreCase("admin_cd_setlevel"))
		{
			if (parts.length < 3)
			{
				activeChar.sendMessage("Uso: //cd_setlevel <city> <level>");
				return false;
			}
			String cityId = parts[1].toLowerCase();
			int newLevel;
			try { newLevel = Integer.parseInt(parts[2]); }
			catch (Exception e) { activeChar.sendMessage("Level invalido."); return false; }

			CityConfig city = mgr.getCity(cityId);
			if (city == null) { activeChar.sendMessage("Cidade desconhecida: " + cityId); return false; }

			LevelStats ls = mgr.getLevelStats(cityId, newLevel);
			if (ls == null) { activeChar.sendMessage("Level " + newLevel + " nao definido pra " + cityId); return false; }

			mgr.setGuardianLevel(cityId, newLevel);
			Npc newGuardian = CityGuardianAI.forceRespawnGuardian(cityId, mgr.getGuardianLevel(cityId));
			if (newGuardian != null)
				activeChar.sendMessage("[CD] " + city.name + " setada pra Lv" + newLevel + " e guardian respawnado.");
			else
				activeChar.sendMessage("[CD] Nivel setado mas respawn falhou - cheque o log.");
			return true;
		}

		if (cmd.equalsIgnoreCase("admin_cd_reset"))
		{
			if (parts.length < 2)
			{
				activeChar.sendMessage("Uso: //cd_reset <city>");
				return false;
			}
			String cityId = parts[1].toLowerCase();
			CityConfig city = mgr.getCity(cityId);
			if (city == null) { activeChar.sendMessage("Cidade desconhecida: " + cityId); return false; }

			mgr.setOwnerClanId(cityId, 0);
			mgr.setGuardianLevel(cityId, 1);
			setWarActive(mgr, false);
			Npc newGuardian = CityGuardianAI.forceRespawnGuardian(cityId, mgr.getGuardianLevel(cityId));

			activeChar.sendMessage("[CD] " + city.name + " RESETADA: sem dono, Lv1, guardian novo, war OFF.");
			if (newGuardian != null)
				activeChar.sendMessage("    Guardian id=" + newGuardian.getObjectId() + " HP=" + (long) newGuardian.getMaxHp());
			return true;
		}

		if (cmd.equalsIgnoreCase("admin_cd_resetowner"))
		{
			if (parts.length < 2)
			{
				activeChar.sendMessage("Uso: //cd resetowner <city>");
				return false;
			}
			String cityId = parts[1].toLowerCase();
			CityConfig city = mgr.getCity(cityId);
			if (city == null) { activeChar.sendMessage("Cidade desconhecida: " + cityId); return false; }

			mgr.setOwnerClanId(cityId, 0);
			activeChar.sendMessage("[CD] Owner de " + city.name + " resetado (sem dono).");
			return true;
		}

		if (cmd.equalsIgnoreCase("admin_cd_kill"))
		{
			if (parts.length < 2)
			{
				activeChar.sendMessage("Uso: //cd kill <city>");
				return false;
			}
			String cityId = parts[1].toLowerCase();
			Npc g = mgr.getGuardian(cityId);
			if (g == null || g.isDead())
			{
				activeChar.sendMessage("Guardian nao encontrado ou ja morto.");
				return false;
			}
			g.setInvul(false);
			g.reduceCurrentHp(g.getCurrentHp() + 1, activeChar, null);
			activeChar.sendMessage("[CD] Guardian de " + cityId + " executado.");
			return true;
		}

		if (cmd.equalsIgnoreCase("admin_cd_open"))
		{
			if (parts.length < 2)
			{
				activeChar.sendMessage("Uso: //cd_open <city>");
				return false;
			}
			String cityId = parts[1].toLowerCase();
			Npc g = mgr.getGuardian(cityId);
			if (g == null)
			{
				activeChar.sendMessage("Guardian de " + cityId + " nao encontrado.");
				return false;
			}
			// Gera HTML premium e envia ao player
			String html = CityGuardianAI.buildPremiumHtml(g, activeChar, cityId);
			if (html == null)
			{
				activeChar.sendMessage("[CD] Falha ao gerar HTML.");
				return false;
			}
			activeChar.setTarget(g);
			NpcHtmlMessage msg = new NpcHtmlMessage(g.getObjectId());
			msg.setHtml(html);
			activeChar.sendPacket(msg);
			return true;
		}

		if (cmd.equalsIgnoreCase("admin_cd_conquest"))
		{
			if (parts.length < 3)
			{
				activeChar.sendMessage("Uso: //cd_conquest <city> <clanId>");
				activeChar.sendMessage("     (simula conquista sem precisar matar - dispara broadcast e troca owner)");
				return false;
			}
			String cityId = parts[1].toLowerCase();
			int clanId;
			try { clanId = Integer.parseInt(parts[2]); }
			catch (Exception e) { activeChar.sendMessage("ClanId invalido."); return false; }

			CityConfig city = mgr.getCity(cityId);
			if (city == null) { activeChar.sendMessage("Cidade desconhecida: " + cityId); return false; }

			Clan clan = ClanTable.getInstance().getClan(clanId);
			if (clan == null) { activeChar.sendMessage("Clan id " + clanId + " nao existe."); return false; }

			// Chama o flow oficial de conquista (limpa owner, dispara broadcast etc)
			mgr.onGuardianKilled(cityId, clanId);

			// Respawna via metodo do script - garante hooks completos
			Npc nw = CityGuardianAI.forceRespawnGuardian(cityId, 1);
			activeChar.sendMessage("[CD] Conquista simulada: " + city.name + " -> clan " + clan.getName());
			if (nw != null) activeChar.sendMessage("    Guardian respawnado Lv1, id=" + nw.getObjectId());
			return true;
		}

		if (cmd.equalsIgnoreCase("admin_cd_tp"))
		{
			if (parts.length < 2)
			{
				activeChar.sendMessage("Uso: //cd tp <city>");
				return false;
			}
			String cityId = parts[1].toLowerCase();
			CityConfig city = mgr.getCity(cityId);
			if (city == null) { activeChar.sendMessage("Cidade desconhecida: " + cityId); return false; }

			activeChar.teleToLocation(city.x, city.y, city.z, city.heading);
			activeChar.sendMessage("[CD] Teleportado pra " + city.name);
			return true;
		}

		return false;
	}

	private static void showHelp(Player p)
	{
		p.sendMessage("=== City Domination - Admin Commands ===");
		p.sendMessage("//cd_status                  - status de todas cidades");
		p.sendMessage("//cd_forcewar                - liga war agora");
		p.sendMessage("//cd_endwar                  - desliga war agora");
		p.sendMessage("//cd_setlevel <city> <lvl>   - troca level (respawn automatico)");
		p.sendMessage("//cd_resetowner <city>       - remove dono atual");
		p.sendMessage("//cd_reset <city>            - RESET FULL (dono=0, lv=1, respawn, war OFF)");
		p.sendMessage("//cd_kill <city>             - mata guardian (testa conquista - precisa clan)");
		p.sendMessage("//cd_conquest <city> <clan>  - SIMULA conquista (broadcast + owner sem precisar matar)");
		p.sendMessage("//cd_tp <city>               - teleporta pra cidade");
		p.sendMessage("Cidades configuradas: " + String.join(", ", CityDominationManager.getInstance().getCities().keySet()));
	}

	// Respawna o guardian de uma cidade no level atual configurado.
	// Mata o anterior se existir, cria spawn novo, registra no manager.
	private static Npc respawnGuardian(String cityId, CityDominationManager mgr)
	{
		CityConfig city = mgr.getCity(cityId);
		if (city == null) return null;

		int level = mgr.getGuardianLevel(cityId);
		LevelStats ls = mgr.getLevelStats(cityId, level);
		if (ls == null) return null;

		// Remove o guardian anterior
		Npc old = mgr.getGuardian(cityId);
		if (old != null)
			old.deleteMe();

		try
		{
			NpcTemplate template = NpcData.getInstance().getTemplate(ls.npcId);
			if (template == null) return null;

			Spawn spawn = new Spawn(template);
			spawn.setXYZ(city.x, city.y, city.z);
			spawn.setHeading(city.heading);
			spawn.setAmount(1);
			Npc npc = spawn.doSpawn(false);
			if (npc != null)
			{
				npc.disableCoreAI(true);
				npc.setImmobilized(true);
				npc.setRandomWalking(false);
				npc.setTargetable(true);
				// Aura visual baseada no level
				try
				{
					int lvl = mgr.getGuardianLevel(cityId);
					if (lvl >= 1) npc.getEffectList().startAbnormalVisualEffect(net.sf.l2jdev.gameserver.model.skill.AbnormalVisualEffect.AURA_BUFF);
					if (lvl == 2) npc.getEffectList().startAbnormalVisualEffect(net.sf.l2jdev.gameserver.model.skill.AbnormalVisualEffect.BIG_HEAD);
					if (lvl >= 4) npc.getEffectList().startAbnormalVisualEffect(net.sf.l2jdev.gameserver.model.skill.AbnormalVisualEffect.AURA_DEBUFF);
					// Lv3 = soh aura (o boss base ja e imponente, sem cabecao)
					// Lv4 = aura dupla (BUFF azul + DEBUFF vermelha) - lord supremo
					npc.broadcastInfo();
				}
				catch (Exception ignored) {}
				mgr.onGuardianRegistered(cityId, npc);
			}
			return npc;
		}
		catch (Exception ex)
		{
			return null;
		}
	}

	private static void showStatus(Player p, CityDominationManager mgr)
	{
		p.sendMessage("=== City Domination Status ===");
		p.sendMessage("War active: " + mgr.isWarActive());
		for (Map.Entry<String, CityConfig> e : mgr.getCities().entrySet())
		{
			String cityId = e.getKey();
			CityConfig city = e.getValue();
			int level = mgr.getGuardianLevel(cityId);
			int ownerClanId = mgr.getOwnerClanId(cityId);
			String ownerName = "Nenhum";
			if (ownerClanId != 0)
			{
				Clan c = ClanTable.getInstance().getClan(ownerClanId);
				if (c != null) ownerName = c.getName();
			}
			Npc g = mgr.getGuardian(cityId);
			String guardianInfo = g == null ? "OFFLINE" : (g.isDead() ? "MORTO" :
				"Lv" + level + " HP=" + ((long) g.getCurrentHp()) + "/" + ((long) g.getMaxHp()) + " invul=" + g.isInvul());
			p.sendMessage("  " + city.name + " | Lv:" + level + " | Owner:" + ownerName + " | " + guardianInfo);
		}
	}

	// Reflection helper pra setar campo privado _warActive sem recompilar Manager
	private static void setWarActive(CityDominationManager mgr, boolean value)
	{
		try
		{
			Field f = CityDominationManager.class.getDeclaredField("_warActive");
			f.setAccessible(true);
			f.setBoolean(mgr, value);
		}
		catch (Exception ex)
		{
			// silent
		}
	}

	@Override
	public String[] getCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
