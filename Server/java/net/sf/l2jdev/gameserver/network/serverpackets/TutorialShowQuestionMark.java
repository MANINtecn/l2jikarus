package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class TutorialShowQuestionMark extends ServerPacket
{
	private final int _markId;
	private final int _markType;

	public TutorialShowQuestionMark(int markId, int markType)
	{
		this._markId = markId;
		this._markType = markType;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.SHOW_TUTORIAL_MARK.writeId(this, buffer);
		buffer.writeByte(this._markType);
		buffer.writeInt(this._markId);
	}
}
