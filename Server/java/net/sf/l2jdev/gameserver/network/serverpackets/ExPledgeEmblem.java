package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.ServerConfig;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExPledgeEmblem extends ServerPacket
{
	 
	private final int _crestId;
	private final int _clanId;
	private final byte[] _data;
	private final int _chunkId;

	public ExPledgeEmblem(int crestId, byte[] chunkedData, int clanId, int chunkId)
	{
		this._crestId = crestId;
		this._data = chunkedData;
		this._clanId = clanId;
		this._chunkId = chunkId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PLEDGE_EMBLEM.writeId(this, buffer);
		buffer.writeInt(ServerConfig.SERVER_ID);
		buffer.writeInt(this._clanId);
		buffer.writeInt(this._crestId);
		buffer.writeInt(this._chunkId);
		buffer.writeInt(65664);
		if (this._data != null)
		{
			buffer.writeInt(this._data.length);
			buffer.writeBytes(this._data);
		}
		else
		{
			buffer.writeInt(0);
		}
	}
}
