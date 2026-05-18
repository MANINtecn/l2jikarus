package net.sf.l2jdev.gameserver.network.serverpackets.prison;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExPrisonUserDonation extends ServerPacket
{
	private final boolean _success;

	public ExPrisonUserDonation(boolean success)
	{
		this._success = success;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PRISON_USER_DONATION.writeId(this, buffer);
		buffer.writeByte(this._success);
	}
}
