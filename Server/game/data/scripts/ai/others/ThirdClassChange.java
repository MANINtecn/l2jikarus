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

import net.sf.l2jdev.gameserver.data.enums.CategoryType;
import net.sf.l2jdev.gameserver.data.xml.CategoryData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.ListenerRegisterType;
import net.sf.l2jdev.gameserver.model.events.annotations.RegisterEvent;
import net.sf.l2jdev.gameserver.model.events.annotations.RegisterType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerLevelChanged;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerLogin;
import net.sf.l2jdev.gameserver.model.script.Script;
import net.sf.l2jdev.gameserver.network.serverpackets.classchange.ExClassChangeSetAlarm;

/**
 * Third class change has only level requirement.
 * @author BAN-JDEV
 */
public class ThirdClassChange extends Script
{
	private static final int LEVEL_REQUIREMENT = 76;

	@RegisterEvent(EventType.ON_PLAYER_LEVEL_CHANGED)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerLevelChanged(OnPlayerLevelChanged event)
	{
		final Player player = event.getPlayer();
		if ((player == null) || (player.getLevel() < LEVEL_REQUIREMENT) || !CategoryData.getInstance().isInCategory(CategoryType.THIRD_CLASS_GROUP, player.getPlayerClass().getId()))
		{
			return;
		}

		player.sendPacket(ExClassChangeSetAlarm.STATIC_PACKET);
	}

	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerLogin(OnPlayerLogin event)
	{
		final Player player = event.getPlayer();
		if ((player == null) || (player.getLevel() < LEVEL_REQUIREMENT) || !CategoryData.getInstance().isInCategory(CategoryType.THIRD_CLASS_GROUP, player.getPlayerClass().getId()))
		{
			return;
		}

		player.sendPacket(ExClassChangeSetAlarm.STATIC_PACKET);
	}

	public static void main(String[] args)
	{
		new ThirdClassChange();
	}
}
