package net.sf.l2jdev.gameserver.network.serverpackets.commission;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExResponseCommissionRegister extends ServerPacket
{
	public static final ExResponseCommissionRegister SUCCEED = new ExResponseCommissionRegister(1);
	public static final ExResponseCommissionRegister FAILED = new ExResponseCommissionRegister(0);
	private final int _result;

	private ExResponseCommissionRegister(int result)
	{
		this._result = result;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_RESPONSE_COMMISSION_REGISTER.writeId(this, buffer);
		buffer.writeInt(this._result);
	}
}
