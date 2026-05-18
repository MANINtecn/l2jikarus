package net.sf.l2jdev.gameserver.network.serverpackets.newhenna;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class NewHennaPotenCompose extends ServerPacket
{
	private final int _resultHennaId;
	private final int _resultItemId;
	private final boolean _success;

	public NewHennaPotenCompose(int resultHennaId, int resultItemId, boolean success)
	{
		this._resultHennaId = resultHennaId;
		this._resultItemId = resultItemId;
		this._success = success;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_NEW_HENNA_COMPOSE.writeId(this, buffer);
		buffer.writeInt(this._resultHennaId);
		buffer.writeInt(this._resultItemId);
		buffer.writeByte(this._success);
	}
}
