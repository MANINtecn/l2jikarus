package net.sf.l2jdev.gameserver.network.serverpackets.quest;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.script.Quest;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExQuestNotificationAll extends ServerPacket
{
	private final Map<Integer, Integer> _notifications = new HashMap<>();

	public ExQuestNotificationAll(Player player)
	{
		for (Quest quest : player.getAllActiveQuests())
		{
			this._notifications.put(quest.getId(), quest.getQuestState(player, false).getCount());
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_QUEST_NOTIFICATION_ALL.writeId(this, buffer);
		buffer.writeInt(this._notifications.size());

		for (Entry<Integer, Integer> quest : this._notifications.entrySet())
		{
			buffer.writeInt(quest.getKey());
			buffer.writeInt(quest.getValue());
		}
	}
}
