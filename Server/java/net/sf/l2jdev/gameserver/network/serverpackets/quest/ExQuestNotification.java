package net.sf.l2jdev.gameserver.network.serverpackets.quest;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.script.QuestState;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExQuestNotification extends ServerPacket
{
	private final QuestState _questState;

	public ExQuestNotification(QuestState questState)
	{
		this._questState = questState;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_QUEST_NOTIFICATION.writeId(this, buffer);
		buffer.writeInt(this._questState.getQuest().getId());
		buffer.writeInt(this._questState.getCount());
		buffer.writeByte(this._questState.getState());
	}
}
