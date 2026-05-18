package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExShowQuestMark extends ServerPacket
{
	private final int _questId;
	private final int _questState;

	public ExShowQuestMark(int questId, int questState)
	{
		this._questId = questId;
		this._questState = questState;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHOW_QUEST_MARK.writeId(this, buffer);
		buffer.writeInt(this._questId);
		buffer.writeInt(this._questState);
	}
}
