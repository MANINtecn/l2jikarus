/*
 * This file is part of the L2J BAN-JDEV project.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package custom.QuickBank;

import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.script.Script;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.WareHouseDepositList;
import net.sf.l2jdev.gameserver.network.serverpackets.WareHouseWithdrawalList;

/**
 * Banco Rapido (Quick Bank) - L2 Ikarus.
 * NPC 18423: abre deposito e retirada da warehouse privada diretamente (sem dialog).
 * Funciona so em peace zone.
 */
public class QuickBank extends Script
{
	private static final int NPC = 18423;

	private QuickBank()
	{
		addStartNpc(NPC);
		addTalkId(NPC);
		addFirstTalkId(NPC);
	}

	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		if (!npc.isInsideZone(ZoneId.PEACE))
		{
			player.sendMessage("O Banco Rápido só funciona em cidades e vilas.");
			return null;
		}

		player.sendPacket(ActionFailed.STATIC_PACKET);
		player.setActiveWarehouse(player.getWarehouse());
		player.setInventoryBlockingStatus(true);

		// Abre a janela de deposito
		player.sendPacket(new WareHouseDepositList(1, player, WareHouseDepositList.PRIVATE));
		player.sendPacket(new WareHouseDepositList(2, player, WareHouseDepositList.PRIVATE));

		// Abre tambem a janela de retirada (se tiver itens guardados)
		if (player.getWarehouse().getSize() > 0)
		{
			player.sendPacket(new WareHouseWithdrawalList(1, player, WareHouseWithdrawalList.PRIVATE));
			player.sendPacket(new WareHouseWithdrawalList(2, player, WareHouseWithdrawalList.PRIVATE));
		}

		return null;
	}

	public static void main(String[] args)
	{
		new QuickBank();
	}
}
