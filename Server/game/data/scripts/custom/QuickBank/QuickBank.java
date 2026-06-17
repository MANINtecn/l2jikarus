/*
 * This file is part of the L2J BAN-JDEV project.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package custom.QuickBank;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.script.Script;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.WareHouseDepositList;

/**
 * Banco Rapido (Quick Bank) - L2 Ikarus.
 * NPC cosmetico 18423: clicou, abre o bau direto (sem menu Private/Clan, sem
 * escolher depositar/retirar). A janela nativa do warehouse ja faz o 2-cliques
 * pra mover item e pergunta a quantidade se for stackavel.
 * Compensa o "perde tudo" da Zona Mortal: guardar/buildar com praticidade.
 */
public class QuickBank extends Script
{
	private static final int NPC = 18423; // Folk cosmetico (Giran)

	private QuickBank()
	{
		addStartNpc(NPC);
		addTalkId(NPC);
		addFirstTalkId(NPC);
	}

	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		// So funciona em cidades/vilas (peace zone). Nas zonas de farm (Dragon
		// Valley, Towers, Kelbim) o 18423 continua so cosmetico - preserva o
		// risco da Zona Mortal (sem acesso a bau no meio do farm).
		if (!npc.isInsideZone(ZoneId.PEACE))
		{
			return null; // cosmetico: nao abre nada
		}
		openBank(player);
		return null; // sem html: a janela do bau ja abre
	}

	private static void openBank(Player player)
	{
		if (!GeneralConfig.ALLOW_WAREHOUSE || player.hasItemRequest())
		{
			return;
		}

		player.sendPacket(ActionFailed.STATIC_PACKET);
		player.setActiveWarehouse(player.getWarehouse());
		player.setInventoryBlockingStatus(true);
		// Janela de deposito (mostra inventario; no cliente Essence costuma exibir
		// o bau junto, permitindo depositar E retirar na mesma janela).
		player.sendPacket(new WareHouseDepositList(1, player, WareHouseDepositList.PRIVATE));
		player.sendPacket(new WareHouseDepositList(2, player, WareHouseDepositList.PRIVATE));
	}

	public static void main(String[] args)
	{
		new QuickBank();
	}
}
