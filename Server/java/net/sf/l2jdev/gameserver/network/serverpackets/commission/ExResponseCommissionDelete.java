package net.sf.l2jdev.gameserver.network.serverpackets.commission;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExResponseCommissionDelete extends ServerPacket
{
	public static final ExResponseCommissionDelete SUCCEED = new ExResponseCommissionDelete(1);
	public static final ExResponseCommissionDelete FAILED = new ExResponseCommissionDelete(0);
	private final int _result;

	private ExResponseCommissionDelete(int result)
	{
		this._result = result;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_RESPONSE_COMMISSION_DELETE.writeId(this, buffer);
		buffer.writeInt(this._result);
	}
}
