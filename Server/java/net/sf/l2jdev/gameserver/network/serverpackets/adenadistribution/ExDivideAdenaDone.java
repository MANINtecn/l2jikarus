package net.sf.l2jdev.gameserver.network.serverpackets.adenadistribution;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExDivideAdenaDone extends ServerPacket
{
	private final boolean _isPartyLeader;
	private final boolean _isCCLeader;
	private final long _adenaCount;
	private final long _distributedAdenaCount;
	private final int _memberCount;
	private final String _distributorName;

	public ExDivideAdenaDone(boolean isPartyLeader, boolean isCCLeader, long adenaCount, long distributedAdenaCount, int memberCount, String distributorName)
	{
		this._isPartyLeader = isPartyLeader;
		this._isCCLeader = isCCLeader;
		this._adenaCount = adenaCount;
		this._distributedAdenaCount = distributedAdenaCount;
		this._memberCount = memberCount;
		this._distributorName = distributorName;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_DIVIDE_ADENA_DONE.writeId(this, buffer);
		buffer.writeByte(this._isPartyLeader);
		buffer.writeByte(this._isCCLeader);
		buffer.writeInt(this._memberCount);
		buffer.writeLong(this._distributedAdenaCount);
		buffer.writeLong(this._adenaCount);
		buffer.writeString(this._distributorName);
	}
}
