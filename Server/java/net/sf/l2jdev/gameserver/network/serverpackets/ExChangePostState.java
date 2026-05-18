package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExChangePostState extends ServerPacket
{
	private final boolean _receivedBoard;
	private final int[] _changedMsgIds;
	private final int _changeId;

	public ExChangePostState(boolean receivedBoard, int[] changedMsgIds, int changeId)
	{
		this._receivedBoard = receivedBoard;
		this._changedMsgIds = changedMsgIds;
		this._changeId = changeId;
	}

	public ExChangePostState(boolean receivedBoard, int changedMsgId, int changeId)
	{
		this._receivedBoard = receivedBoard;
		this._changedMsgIds = new int[]
		{
			changedMsgId
		};
		this._changeId = changeId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CHANGE_POST_STATE.writeId(this, buffer);
		buffer.writeInt(this._receivedBoard);
		buffer.writeInt(this._changedMsgIds.length);

		for (int postId : this._changedMsgIds)
		{
			buffer.writeInt(postId);
			buffer.writeInt(this._changeId);
		}
	}
}
