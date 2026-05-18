package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.LinkedList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.script.QuestState;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class QuestList extends ServerPacket
{
	private final List<QuestState> _activeQuests = new LinkedList<>();
	private final byte[] _oneTimeQuestMask = new byte[128];

	public QuestList(Player player)
	{
		for (QuestState qs : player.getAllQuestStates())
		{
			int questId = qs.getQuest().getId();
			if (questId > 0)
			{
				if (qs.isStarted())
				{
					this._activeQuests.add(qs);
				}
				else if (qs.isCompleted() && (questId <= 255 || questId >= 10256) && questId <= 11023)
				{
					this._oneTimeQuestMask[questId % 10000 / 8] = (byte) (this._oneTimeQuestMask[questId % 10000 / 8] | 1 << questId % 8);
				}
			}
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.QUESTLIST.writeId(this, buffer);
		buffer.writeShort(this._activeQuests.size());

		for (QuestState qs : this._activeQuests)
		{
			buffer.writeInt(qs.getQuest().getId());
			buffer.writeInt(qs.getCondBitSet());
		}

		buffer.writeBytes(this._oneTimeQuestMask);
	}
}
