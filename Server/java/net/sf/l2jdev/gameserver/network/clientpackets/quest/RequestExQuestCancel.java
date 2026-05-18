package net.sf.l2jdev.gameserver.network.clientpackets.quest;

import net.sf.l2jdev.gameserver.managers.ScriptManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.Containers;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerQuestAbort;
import net.sf.l2jdev.gameserver.model.script.Quest;
import net.sf.l2jdev.gameserver.model.script.QuestState;
import net.sf.l2jdev.gameserver.model.script.QuestType;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.quest.ExQuestNotificationAll;
import net.sf.l2jdev.gameserver.network.serverpackets.quest.ExQuestUI;

public class RequestExQuestCancel extends ClientPacket
{
	private int _questId;

	@Override
	protected void readImpl()
	{
		this._questId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Quest quest = ScriptManager.getInstance().getQuest(this._questId);
			QuestState qs = quest.getQuestState(player, false);
			if (qs != null && !qs.isCompleted())
			{
				qs.setSimulated(false);
				qs.exitQuest(QuestType.REPEATABLE);
				player.sendPacket(new ExQuestUI(player));
				player.sendPacket(new ExQuestNotificationAll(player));
				if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_QUEST_ABORT, player, Containers.Players()))
				{
					EventDispatcher.getInstance().notifyEventAsync(new OnPlayerQuestAbort(player, this._questId), player, Containers.Players());
				}

				quest.onQuestAborted(player);
			}
		}
	}
}
