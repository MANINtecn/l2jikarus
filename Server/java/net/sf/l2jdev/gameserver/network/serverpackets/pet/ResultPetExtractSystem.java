package net.sf.l2jdev.gameserver.network.serverpackets.pet;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ResultPetExtractSystem extends ServerPacket
{
	private final boolean _success;

	public ResultPetExtractSystem(boolean success)
	{
		this._success = success;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_RESULT_PET_EXTRACT_SYSTEM.writeId(this, buffer);
		buffer.writeInt(this._success);
	}
}
