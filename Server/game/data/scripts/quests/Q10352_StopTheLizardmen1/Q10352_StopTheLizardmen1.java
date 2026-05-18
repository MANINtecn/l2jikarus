package quests.Q10352_StopTheLizardmen1;

import net.sf.l2jdev.gameserver.data.xml.TeleportListData;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.script.Quest;
import net.sf.l2jdev.gameserver.model.script.QuestDialogType;
import net.sf.l2jdev.gameserver.model.script.QuestState;
import net.sf.l2jdev.gameserver.model.script.newquestdata.NewQuest;
import net.sf.l2jdev.gameserver.model.script.newquestdata.NewQuestLocation;
import net.sf.l2jdev.gameserver.model.script.newquestdata.QuestCondType;
import net.sf.l2jdev.gameserver.network.serverpackets.quest.ExQuestDialog;
import net.sf.l2jdev.gameserver.network.serverpackets.quest.ExQuestNotification;

import quests.Q10353_StopTheLizardmen2.Q10353_StopTheLizardmen2;

public class Q10352_StopTheLizardmen1 extends Quest
{
	private static final int QUEST_ID = 10352;
	private static final int[] MONSTERS =
	{
		22151, // Tanta Lizardman
		22152, // Tanta Lizardman Warrior
		22153, // Tanta Lizardman Berserker
		22154, // Tanta Lizardman Archer
		22155, // Tanta Lizardman Summoner
	};

	public Q10352_StopTheLizardmen1()
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
				}

				final QuestState nextQuestState = player.getQuestState(Q10353_StopTheLizardmen2.class.getSimpleName());
				if (nextQuestState == null)
				{
					player.sendPacket(new ExQuestDialog(10353, QuestDialogType.ACCEPT));
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
}
