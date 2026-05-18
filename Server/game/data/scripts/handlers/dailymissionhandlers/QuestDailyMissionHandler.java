/*
 * This file is part of the L2J BAN-JDEV project.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.dailymissionhandlers;

import net.sf.l2jdev.gameserver.handler.AbstractDailyMissionHandler;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.DailyMissionStatus;
import net.sf.l2jdev.gameserver.model.actor.holders.player.DailyMissionDataHolder;
import net.sf.l2jdev.gameserver.model.actor.holders.player.DailyMissionPlayerEntry;
import net.sf.l2jdev.gameserver.model.events.Containers;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerQuestComplete;
import net.sf.l2jdev.gameserver.model.events.listeners.ConsumerEventListener;
import net.sf.l2jdev.gameserver.model.script.QuestType;

/**
 * @author UnAfraid
 */
public class QuestDailyMissionHandler extends AbstractDailyMissionHandler
{
	private final int _amount;

	public QuestDailyMissionHandler(DailyMissionDataHolder holder)
	{
		super(holder);
		_amount = holder.getRequiredCompletions();
	}

	@Override
	public void init()
	{
		Containers.Players().addListener(new ConsumerEventListener(this, EventType.ON_PLAYER_QUEST_COMPLETE, (OnPlayerQuestComplete event) -> onQuestComplete(event), this));
	}

	@Override
	public boolean isAvailable(Player player)
	{
		final DailyMissionPlayerEntry entry = getPlayerEntry(player.getObjectId(), false);
		if (entry != null)
		{
			switch (entry.getStatus())
			{
				case NOT_AVAILABLE: // Initial state
				{
					if (entry.getProgress() >= _amount)
					{
						entry.setStatus(DailyMissionStatus.AVAILABLE);
						storePlayerEntry(entry);
					}
					break;
				}
				case AVAILABLE:
				{
					return true;
				}
			}
		}

		return false;
	}

	private void onQuestComplete(OnPlayerQuestComplete event)
	{
		final Player player = event.getPlayer();
		if (event.getQuestType() == QuestType.DAILY)
		{
			final DailyMissionPlayerEntry entry = getPlayerEntry(player.getObjectId(), true);
			if (entry.getStatus() == DailyMissionStatus.NOT_AVAILABLE)
			{
				if (entry.increaseProgress() >= _amount)
				{
					entry.setStatus(DailyMissionStatus.AVAILABLE);
				}

				storePlayerEntry(entry);
			}
		}
	}
}
