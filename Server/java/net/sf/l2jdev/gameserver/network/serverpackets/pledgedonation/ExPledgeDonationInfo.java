package net.sf.l2jdev.gameserver.network.serverpackets.pledgedonation;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExPledgeDonationInfo extends ServerPacket
{
	private final int _curPoints;
	private final boolean _accepted;

	public ExPledgeDonationInfo(int curPoints, boolean accepted)
	{
		this._curPoints = curPoints;
		this._accepted = accepted;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PLEDGE_DONATION_INFO.writeId(this, buffer);
		buffer.writeInt(this._curPoints);
		buffer.writeByte(!this._accepted);
	}
}
