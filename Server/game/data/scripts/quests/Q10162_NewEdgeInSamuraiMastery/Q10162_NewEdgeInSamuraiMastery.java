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
package quests.Q10162_NewEdgeInSamuraiMastery;

import net.sf.l2jdev.gameserver.data.enums.CategoryType;
import net.sf.l2jdev.gameserver.data.xml.CategoryData;
import net.sf.l2jdev.gameserver.data.xml.TeleportListData;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.ListenerRegisterType;
import net.sf.l2jdev.gameserver.model.events.annotations.Id;
import net.sf.l2jdev.gameserver.model.events.annotations.RegisterEvent;
import net.sf.l2jdev.gameserver.model.events.annotations.RegisterType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerItemAdd;
import net.sf.l2jdev.gameserver.model.script.Quest;
import net.sf.l2jdev.gameserver.model.script.QuestDialogType;
import net.sf.l2jdev.gameserver.model.script.QuestSound;
import net.sf.l2jdev.gameserver.model.script.QuestState;
import net.sf.l2jdev.gameserver.model.script.newquestdata.NewQuest;
import net.sf.l2jdev.gameserver.model.script.newquestdata.NewQuestLocation;
import net.sf.l2jdev.gameserver.model.script.newquestdata.QuestCondType;
import net.sf.l2jdev.gameserver.network.serverpackets.PlaySound;
import net.sf.l2jdev.gameserver.network.serverpackets.classchange.ExClassChangeSetAlarm;
import net.sf.l2jdev.gameserver.network.serverpackets.quest.ExQuestDialog;
import net.sf.l2jdev.gameserver.network.serverpackets.quest.ExQuestNotification;

import quests.Q10163_AnotherStepToTruth.Q10163_AnotherStepToTruth;

/**
 * @author Galagard
 */
public class Q10162_NewEdgeInSamuraiMastery extends Quest
{
	private static final int QUEST_ID = 10162;
	private static final int QUEST_ITEM = 105436;

	private static final int[] MONSTERS =
	{
		23239, // Moon Maestro
		23240, // Moon Fighter
		23241, // Moon Guard
	};

	public Q10162_NewEdgeInSamuraiMastery()
	{
		super(QUEST_ID);
		addKillId(MONSTERS);
	}

	@Override
	public String onEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "ACCEPT":
			{
				if (!canStartQuest(player))
				{
					break;
				}

				final QuestState questState = getQuestState(player, true);
				if (!questState.isStarted() && !questState.isCompleted())
				{
					questState.startQuest();
				}
				break;
			}
			case "TELEPORT":
			{
				QuestState questState = getQuestState(player, false);
				if (questState == null)
				{
					if (!canStartQuest(player))
					{
						break;
					}

					questState = getQuestState(player, true);

					final NewQuestLocation questLocation = getQuestData().getLocation();
					if (questLocation.getStartLocationId() > 0)
					{
						final Location location = TeleportListData.getInstance().getTeleport(questLocation.getStartLocationId()).getLocation();
						if (teleportToQuestLocation(player, location))
						{
							questState.setCond(QuestCondType.ACT);
							sendAcceptDialog(player);
						}
					}
					break;
				}

				final NewQuestLocation questLocation = getQuestData().getLocation();
				if (questState.isCond(QuestCondType.STARTED))
				{
					if (questLocation.getQuestLocationId() > 0)
					{
						final Location location = TeleportListData.getInstance().getTeleport(questLocation.getQuestLocationId()).getLocation();
						if (teleportToQuestLocation(player, location) && (questLocation.getQuestLocationId() == questLocation.getEndLocationId()))
						{
							questState.setCond(QuestCondType.DONE);
							sendEndDialog(player);
						}
					}
				}
				else if (questState.isCond(QuestCondType.DONE) && !questState.isCompleted())
				{
					if (questLocation.getEndLocationId() > 0)
					{
						final Location location = TeleportListData.getInstance().getTeleport(questLocation.getEndLocationId()).getLocation();
						if (teleportToQuestLocation(player, location))
						{
							sendEndDialog(player);
						}
					}
				}
				break;
			}
			case "COMPLETE":
			{
				final QuestState questState = getQuestState(player, false);
				if (questState == null)
				{
					break;
				}

				if (questState.isCond(QuestCondType.DONE) && !questState.isCompleted())
				{
					questState.exitQuest(false, true);
					rewardPlayer(player);

					if (CategoryData.getInstance().isInCategory(CategoryType.FIRST_CLASS_GROUP, player.getPlayerClass().getId()))
					{
						player.sendPacket(ExClassChangeSetAlarm.STATIC_PACKET);
						player.sendPacket(new PlaySound(2, "tutorial_voice_051", 0, 0, player.getX(), player.getY(), player.getZ()));
					}
				}

				final QuestState nextQuestState = player.getQuestState(Q10163_AnotherStepToTruth.class.getSimpleName());
				if (nextQuestState == null)
				{
					player.sendPacket(new ExQuestDialog(10163, QuestDialogType.ACCEPT));
				}
				break;
			}
		}

		return null;
	}

	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		final QuestState questState = getQuestState(player, false);
		if ((questState != null) && !questState.isCompleted())
		{
			if ((questState.getCount() == 0) && questState.isCond(QuestCondType.NONE))
			{
				player.sendPacket(new ExQuestDialog(QUEST_ID, QuestDialogType.START));
			}
			else if (questState.isCond(QuestCondType.DONE))
			{
				player.sendPacket(new ExQuestDialog(QUEST_ID, QuestDialogType.END));
			}
		}

		npc.showChatWindow(player);
		return null;
	}

	@Override
	public void onKill(Npc npc, Player killer, boolean isSummon)
	{
		final QuestState questState = getQuestState(killer, false);
		if ((questState != null) && questState.isCond(QuestCondType.STARTED))
		{
			final NewQuest data = getQuestData();
			if (data.getGoal().getItemId() > 0)
			{
				final int itemCount = (int) getQuestItemsCount(killer, data.getGoal().getItemId());
				if (itemCount < data.getGoal().getCount())
				{
					giveItems(killer, data.getGoal().getItemId(), 1);
					final int newItemCount = (int) getQuestItemsCount(killer, data.getGoal().getItemId());
					questState.setCount(newItemCount);
					playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
			else
			{
				final int currentCount = questState.getCount();
				if (currentCount < data.getGoal().getCount())
				{
					questState.setCount(currentCount + 1);
				}
			}

			if (questState.getCount() >= data.getGoal().getCount())
			{
				questState.setCond(QuestCondType.DONE);
				killer.sendPacket(new ExQuestNotification(questState));
			}
		}
	}

	@RegisterEvent(EventType.ON_PLAYER_ITEM_ADD)
	@RegisterType(ListenerRegisterType.ITEM)
	@Id(QUEST_ITEM)
	public void onItemAdd(OnPlayerItemAdd event)
	{
		final Player player = event.getPlayer();
		if (!canStartQuest(player))
		{
			return;
		}

		final QuestState questState = getQuestState(player, false);
		if ((questState == null) || !questState.isStarted())
		{
			player.sendPacket(new ExQuestDialog(QUEST_ID, QuestDialogType.START));
			onEvent("ACCEPT", null, player);
		}
	}
}
