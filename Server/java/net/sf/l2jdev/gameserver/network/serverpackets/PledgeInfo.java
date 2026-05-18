package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.ServerConfig;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class PledgeInfo extends ServerPacket
{
	private final Clan _clan;

	public PledgeInfo(Clan clan)
	{
		this._clan = clan;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.PLEDGE_INFO.writeId(this, buffer);
		buffer.writeInt(ServerConfig.SERVER_ID);
		buffer.writeInt(this._clan.getId());
		buffer.writeString(this._clan.getName());
		buffer.writeString(this._clan.getAllyName());
	}
}
