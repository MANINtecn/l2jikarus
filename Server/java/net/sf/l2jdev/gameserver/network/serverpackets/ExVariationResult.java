package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExVariationResult extends ServerPacket
{
	public static final ExVariationResult FAIL = new ExVariationResult(0, 0, 0, false);
	private final int _option1;
	private final int _option2;
	private final int _option3;
	private final boolean _success;

	public ExVariationResult(int option1, int option2, int option3, boolean success)
	{
		this._option1 = option1;
		this._option2 = option2;
		this._option3 = option3;
		this._success = success;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_VARIATION_RESULT.writeId(this, buffer);
		buffer.writeInt(this._option1);
		buffer.writeInt(this._option2);
		buffer.writeInt(this._option3);
		buffer.writeLong(0L);
		buffer.writeLong(0L);
		buffer.writeInt(this._success);
	}
}
