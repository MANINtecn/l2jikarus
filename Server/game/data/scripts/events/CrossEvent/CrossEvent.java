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
package events.CrossEvent;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.managers.events.CrossEventManager;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.holders.player.CrossEventAdvancedRewardHolder;
import net.sf.l2jdev.gameserver.model.actor.holders.player.CrossEventRegularRewardHolder;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.ListenerRegisterType;
import net.sf.l2jdev.gameserver.model.events.annotations.RegisterEvent;
import net.sf.l2jdev.gameserver.model.events.annotations.RegisterType;
import net.sf.l2jdev.gameserver.model.events.holders.OnDailyReset;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerLogin;
import net.sf.l2jdev.gameserver.model.script.LongTimeEvent;
import net.sf.l2jdev.gameserver.model.variables.PlayerVariables;
import net.sf.l2jdev.gameserver.network.serverpackets.crossevent.ExCrossEventData;
import net.sf.l2jdev.gameserver.network.serverpackets.crossevent.ExCrossEventInfo;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

/**
 * @author Smoto
 * @URL https://l2central.info/essence/events_and_promos/2438.html
 */
public class CrossEvent extends LongTimeEvent implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(CrossEvent.class.getName());

	public CrossEvent()
	{
		if (isEventPeriod())
		{
			load();
			final int endTime = (int) ((getEndDate().getTime() - System.currentTimeMillis()) / 1000L);
			CrossEventManager.getInstance().setEndTime(endTime);
		}
	}

	@Override
	public void load()
	{
		CrossEventManager.getInstance().getRegularRewardsList().clear();
		CrossEventManager.getInstance().getAdvancedRewardList().clear();
		parseDatapackFile("data/scripts/events/CrossEvent/CrossEvent.xml");
		LOGGER.info(getClass().getSimpleName() + ": Loaded: " + CrossEventManager.getInstance().getRegularRewardsList().size() + " normal rewards.");
		LOGGER.info(getClass().getSimpleName() + ": Loaded: " + CrossEventManager.getInstance().getAdvancedRewardList().size() + " advanced rewards.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		forEach(document, "list", listNode -> {
			forEach(listNode, "displayId", displayId -> {
				CrossEventManager.getInstance().setDisplayId(Integer.parseInt(displayId.getTextContent()));
			});

			forEach(listNode, "ticketId", ticketId -> {
				CrossEventManager.getInstance().setTicketId(Integer.parseInt(ticketId.getTextContent()));
			});

			forEach(listNode, "dailyResets", dailyResets -> {
				CrossEventManager.getInstance().setDailyResets(Integer.parseInt(dailyResets.getTextContent()));
			});

			forEach(listNode, "dailyResetsCostAmount", dailyResetsCostAmount -> {
				CrossEventManager.getInstance().setResetCostAmount(Integer.parseInt(dailyResetsCostAmount.getTextContent()));
			});

			forEach(listNode, "cells", cells -> {
				forEach(cells, "cell", cell -> {
					final NamedNodeMap attrs = cell.getAttributes();
					final int id = parseInteger(attrs, "id");
					final int rewardId = parseInteger(attrs, "rewardId");
					final int rewardAmount = parseInteger(attrs, "rewardAmount");
					CrossEventManager.getInstance().getRegularRewardsList().add(new CrossEventRegularRewardHolder(id, rewardId, rewardAmount));
				});
			});

			forEach(listNode, "advancedRewards", advancedRewards -> {
				forEach(advancedRewards, "item", item -> {
					final NamedNodeMap itemAttrs = item.getAttributes();
					final int itemId = parseInteger(itemAttrs, "id");
					final int itemCount = parseInteger(itemAttrs, "count", 1);
					final double itemChance = parseDouble(itemAttrs, "chance");
					CrossEventManager.getInstance().getAdvancedRewardList().add(new CrossEventAdvancedRewardHolder(itemId, itemCount, itemChance));
				});
			});
		});
	}

	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerLogin(OnPlayerLogin event)
	{
		if (!isEventPeriod())
		{
			return;
		}

		final Player player = event.getPlayer();
		if (player == null)
		{
			return;
		}

		player.sendPacket(new ExCrossEventData());
		player.sendPacket(new ExCrossEventInfo(player));
	}

	@RegisterEvent(EventType.ON_DAILY_RESET)
	@RegisterType(ListenerRegisterType.GLOBAL)
	public void onDailyReset(OnDailyReset event)
	{
		if (!isEventPeriod())
		{
			return;
		}

		// Update data for offline players.
		try (Connection con = DatabaseFactory.getConnection())
		{
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_variables WHERE var = ? AND charId IN (SELECT charId FROM characters WHERE online = 0)"))
			{
				ps.setString(1, PlayerVariables.CROSS_EVENT_DAILY_RESET_COUNT);
				ps.execute();
			}
		}
		catch (Exception e)
		{
			LOGGER.warning(getClass().getSimpleName() + ": Could not reset variables: " + e.getMessage());
		}

		// Update data for online players.
		for (Player player : World.getInstance().getPlayers())
		{
			player.getVariables().remove(PlayerVariables.CROSS_EVENT_DAILY_RESET_COUNT);
		}

		LOGGER.info(getClass().getSimpleName() + " has been reset.");
	}

	public static void main(String[] args)
	{
		new CrossEvent();
	}
}
