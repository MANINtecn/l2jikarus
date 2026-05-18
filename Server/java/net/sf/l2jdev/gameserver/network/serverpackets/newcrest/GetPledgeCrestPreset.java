package net.sf.l2jdev.gameserver.network.serverpackets.newcrest;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class GetPledgeCrestPreset extends ServerPacket
{
	private final int _clanId;
	private final int _emblemId;

	public GetPledgeCrestPreset(int pledgeId, int crestId)
	{
		this._clanId = pledgeId;
		this._emblemId = crestId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_GET_PLEDGE_CREST_PRESET.writeId(this, buffer);
		buffer.writeInt(1);
		buffer.writeInt(this._clanId);
		buffer.writeInt(this._emblemId);
	}
}
