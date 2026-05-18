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
package handlers.dailymissionhandlers;

import java.util.Calendar;

import net.sf.l2jdev.gameserver.handler.AbstractDailyMissionHandler;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.DailyMissionStatus;
import net.sf.l2jdev.gameserver.model.actor.holders.player.DailyMissionDataHolder;
import net.sf.l2jdev.gameserver.model.actor.holders.player.DailyMissionPlayerEntry;
import net.sf.l2jdev.gameserver.model.events.Containers;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerLogin;
import net.sf.l2jdev.gameserver.model.events.listeners.ConsumerEventListener;

/**
 * @author Iris, BAN-JDEV
 */
public class LoginWeekendDailyMissionHandler extends AbstractDailyMissionHandler
{
	public LoginWeekendDailyMissionHandler(DailyMissionDataHolder holder)
	{
		super(holder);
	}

	@Override
	public boolean isAvailable(Player player)
	{
		final DailyMissionPlayerEntry entry = getPlayerEntry(player.getObjectId(), false);
		return (entry != null) && (entry.getStatus() == DailyMissionStatus.AVAILABLE);
	}

	@Override
	public void init()
	{
		Containers.Global().addListener(new ConsumerEventListener(this, EventType.ON_PLAYER_LOGIN, (OnPlayerLogin event) -> onPlayerLogin(event), this));
	}

	private void onPlayerLogin(OnPlayerLogin event)
	{
		final DailyMissionPlayerEntry entry = getPlayerEntry(event.getPlayer().getObjectId(), true);
		if (entry.getStatus() != DailyMissionStatus.COMPLETED)
		{
			final int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
			if (((currentDay == Calendar.SATURDAY) || (currentDay == Calendar.SUNDAY) || (currentDay == Calendar.MONDAY)) //
				&& (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 6) && (Calendar.getInstance().get(Calendar.MINUTE) < 30))
			{
				entry.setProgress(1);
				entry.setStatus(DailyMissionStatus.AVAILABLE);
			}
		}

		storePlayerEntry(entry);
	}
}
