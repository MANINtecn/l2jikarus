/*
 * Copyright (c) 2013 L2jBAN-JDEV
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ai.others;

import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.script.Script;
import net.sf.l2jdev.gameserver.network.serverpackets.teleports.ExShowTeleportUi;

/**
 * @author BAN-JDEV
 */
public class TownGatekeepers extends Script
{
	// NPCs
	private static final int[] GATEKEEPERS =
	{
		30080, // Giran
		30848, // Aden
		30059, // Dion
		30177, // Oren
		30256, // Gludio
		30320, // Gludin
		30233, // Hunters
		31275, // Goddard
		30006, // Talking Island
		30146, // Elven Village
		30134, // Dark Elven Village
		30540, // Dwarven Village
		34112, // Kamael Village
		34213, // Sylph Village
		34404, // Assassin Hideout
		34459, // High Elf Village
		34559, // Warg Settlement
		34637, // Crow Village
	};

	private TownGatekeepers()
	{
		addStartNpc(GATEKEEPERS);
		addFirstTalkId(GATEKEEPERS);
		addTalkId(GATEKEEPERS);
	}

	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		player.sendPacket(ExShowTeleportUi.STATIC_PACKET);
		return null;
	}

	public static void main(String[] args)
	{
		new TownGatekeepers();
	}
}
