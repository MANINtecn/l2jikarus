package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.sql.CrestTable;
import net.sf.l2jdev.gameserver.model.Crest;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class PledgeCrest extends ServerPacket
{
	private final int _crestId;
	private final byte[] _data;
	private final int _clanId;

	public PledgeCrest(int crestId, int clanId)
	{
		this._crestId = crestId;
		Crest crest = CrestTable.getInstance().getCrest(crestId);
		this._data = crest != null ? crest.getData() : null;
		this._clanId = clanId;
	}

	public PledgeCrest(int crestId, byte[] data, int clanId)
	{
		this._crestId = crestId;
		this._data = data;
		this._clanId = clanId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.PLEDGE_CREST.writeId(this, buffer);
		buffer.writeInt(this._clanId);
		buffer.writeInt(this._crestId);
		if (this._data != null)
		{
			buffer.writeInt(this._data.length);
			buffer.writeInt(this._data.length);
			buffer.writeBytes(this._data);
		}
		else
		{
			buffer.writeInt(0);
		}
	}
}
