package net.sf.l2jdev.gameserver.network.serverpackets.quest;

import java.util.LinkedList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.xml.NewQuestData;
import net.sf.l2jdev.gameserver.managers.ScriptManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.script.Quest;
import net.sf.l2jdev.gameserver.model.script.QuestState;
import net.sf.l2jdev.gameserver.model.script.newquestdata.NewQuest;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExQuestAcceptableList extends ServerPacket
{
	private final List<Quest> _availableQuests = new LinkedList<>();

	public ExQuestAcceptableList(Player player)
	{
		ScriptManager scriptManager = ScriptManager.getInstance();

		for (NewQuest newQuest : NewQuestData.getInstance().getQuests())
		{
			Quest quest = scriptManager.getQuest(newQuest.getId());
			if (quest != null && quest.canStartQuest(player))
			{
				QuestState questState = player.getQuestState(quest.getName());
				if (questState == null || !questState.isStarted() && questState.isNowAvailable())
				{
					this._availableQuests.add(quest);
				}
			}
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_QUEST_ACCEPTABLE_LIST.writeId(this, buffer);
		buffer.writeInt(this._availableQuests.size());

		for (Quest quest : this._availableQuests)
		{
			buffer.writeInt(quest.getId());
		}
	}
}
