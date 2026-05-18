package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class PledgeStatusChanged extends ServerPacket
{
	private final Clan _clan;

	public PledgeStatusChanged(Clan clan)
	{
		this._clan = clan;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.PLEDGE_STATUS_CHANGED.writeId(this, buffer);
		buffer.writeInt(0);
		buffer.writeInt(this._clan.getLeaderId());
		buffer.writeInt(this._clan.getId());
		buffer.writeInt(this._clan.getCrestId());
		buffer.writeInt(this._clan.getAllyId());
		buffer.writeInt(this._clan.getAllyCrestId());
		buffer.writeInt(this._clan.getCrestLargeId());
		buffer.writeInt(0);
	}
}
