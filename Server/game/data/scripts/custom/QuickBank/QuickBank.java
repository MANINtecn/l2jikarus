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

/**
 * Banco Rapido (Quick Bank) - L2 Ikarus.
 * NPC cosmetico 18423: abre menu Depositar/Retirar usando os bypass handlers
 * nativos (PrivateWarehouse IBypassHandler). Funciona so em peace zone.
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

		// Retornar HTML com os bypass nativos do warehouse.
		// O handler PrivateWarehouse (IBypassHandler) processa DepositP e WithdrawP.
		final String id = String.valueOf(npc.getObjectId());
		return "<html><body><center><br>" +
			"<font color=\"LEVEL\">Banco Rápido</font><br>" +
			"<font color=\"aaaaaa\">Guarde seus itens antes de entrar<br>na Zona Mortal!</font><br><br>" +
			"<Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h npc_" + id + "_DepositP\">Depositar Itens</Button>" +
			"<Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h npc_" + id + "_WithdrawP\">Retirar Itens</Button>" +
			"</center></body></html>";
	}

	public static void main(String[] args)
	{
		new QuickBank();
	}
}
