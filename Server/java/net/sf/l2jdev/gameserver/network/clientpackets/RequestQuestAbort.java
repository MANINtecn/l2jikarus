package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.managers.ScriptManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.Containers;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerQuestAbort;
import net.sf.l2jdev.gameserver.model.script.Quest;
import net.sf.l2jdev.gameserver.model.script.QuestState;
import net.sf.l2jdev.gameserver.model.script.QuestType;
import net.sf.l2jdev.gameserver.network.serverpackets.QuestList;

public class RequestQuestAbort extends ClientPacket
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
			Quest qe = ScriptManager.getInstance().getQuest(this._questId);
			if (qe != null)
			{
				QuestState qs = player.getQuestState(qe.getName());
				if (qs != null)
				{
					qs.setSimulated(false);
					qs.exitQuest(QuestType.REPEATABLE);
					player.sendPacket(new QuestList(player));
					if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_QUEST_ABORT, player, Containers.Players()))
					{
						EventDispatcher.getInstance().notifyEventAsync(new OnPlayerQuestAbort(player, this._questId), player, Containers.Players());
					}

					qe.onQuestAborted(player);
				}
			}
		}
	}
}
