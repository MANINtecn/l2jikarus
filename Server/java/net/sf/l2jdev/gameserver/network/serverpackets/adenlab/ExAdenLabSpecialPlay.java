package net.sf.l2jdev.gameserver.network.serverpackets.adenlab;

import java.util.Map;
import java.util.Map.Entry;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExAdenLabSpecialPlay extends ServerPacket
{
	private final int _bossId;
	private final int _pageIndex;
	private final byte _result;
	private final Map<Integer, Integer> _drawnOptionGrades;

	public ExAdenLabSpecialPlay(int bossID, int pageIndex, byte result, Map<Integer, Integer> drawnOptionGrades)
	{
		this._bossId = bossID;
		this._pageIndex = pageIndex;
		this._result = result;
		this._drawnOptionGrades = drawnOptionGrades;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ADENLAB_SPECIAL_PLAY.writeId(this, buffer);
		buffer.writeInt(this._bossId);
		buffer.writeInt(this._pageIndex);
		buffer.writeByte(this._result);
		buffer.writeInt(this._drawnOptionGrades.size());

		for (Entry<Integer, Integer> optionGrade : this._drawnOptionGrades.entrySet())
		{
			buffer.writeInt(optionGrade.getValue());
		}
	}
}
