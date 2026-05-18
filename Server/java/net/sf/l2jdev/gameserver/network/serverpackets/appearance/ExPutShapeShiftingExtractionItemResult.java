package net.sf.l2jdev.gameserver.network.serverpackets.appearance;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExPutShapeShiftingExtractionItemResult extends ServerPacket
{
	public static final ExPutShapeShiftingExtractionItemResult FAILED = new ExPutShapeShiftingExtractionItemResult(0);
	public static final ExPutShapeShiftingExtractionItemResult SUCCESS = new ExPutShapeShiftingExtractionItemResult(1);
	private final int _result;

	public ExPutShapeShiftingExtractionItemResult(int result)
	{
		this._result = result;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PUT_SHAPE_SHIFTING_EXTRACTION_ITEM_RESULT.writeId(this, buffer);
		buffer.writeInt(this._result);
	}
}
