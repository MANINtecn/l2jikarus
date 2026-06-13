package handlers.admincommandhandlers;

import net.sf.l2jdev.gameserver.handler.IAdminCommandHandler;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.variables.PlayerVariables;
import net.sf.l2jdev.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2jdev.gameserver.util.NoTargetTelegraph;

/**
 * Mod No-Target: permite que skills AoE sejam usadas sem alvo selecionado,
 * causando dano em todos os inimigos dentro do range.
 *
 * Uso:
 *   //notarget on   — ativa para o target (ou para si mesmo)
 *   //notarget off  — desativa
 *   //notarget      — mostra status atual
 */
public class AdminNoTargetMod implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_notarget",
	};

	@Override
	public boolean onCommand(String command, Player activeChar)
	{
		// Aplica no target selecionado se for player, caso contrário aplica em si mesmo
		Player target = null;
		if (activeChar.getTarget() != null && activeChar.getTarget().isPlayer())
		{
			target = activeChar.getTarget().asPlayer();
		}
		else
		{
			target = activeChar;
		}

		String[] parts = command.split(" ");
		String action = parts.length > 1 ? parts[1].toLowerCase() : "status";

		switch (action)
		{
			case "on":
			{
				target.getVariables().set(PlayerVariables.NO_TARGET_MOD, true);
				target.getVariables().storeMe();
				NoTargetTelegraph.start(target);
					target.sendPacket(new ExShowScreenMessage("MOD NO-TARGET: LIGADO", 3000));
					activeChar.sendMessage("[NoTargetMod] Ativado para " + target.getName() + ". Skills AoE funcionam sem target.");
				if (target != activeChar)
				{
					target.sendMessage("[NoTargetMod] Modo No-Target ATIVADO pelo GM. Skills AoE funcionam sem target.");
				}
				break;
			}
			case "off":
			{
				target.getVariables().set(PlayerVariables.NO_TARGET_MOD, false);
				target.getVariables().storeMe();
				NoTargetTelegraph.stop(target);
					target.sendPacket(new ExShowScreenMessage("MOD NO-TARGET: DESLIGADO (skills oficiais)", 3000));
					activeChar.sendMessage("[NoTargetMod] Desativado para " + target.getName() + ".");
				if (target != activeChar)
				{
					target.sendMessage("[NoTargetMod] Modo No-Target DESATIVADO pelo GM.");
				}
				break;
			}
			default:
			{
				boolean status = target.getVariables().getBoolean(PlayerVariables.NO_TARGET_MOD, false);
				activeChar.sendMessage("[NoTargetMod] Status de " + target.getName() + ": " + (status ? "ON" : "OFF"));
				activeChar.sendMessage("Uso: //notarget on | //notarget off");
				break;
			}
		}

		return true;
	}

	@Override
	public String[] getCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
