package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class PledgeShowMemberListDelete extends ServerPacket
{
	private final String _player;

	public PledgeShowMemberListDelete(String playerName)
	{
		this._player = playerName;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.PLEDGE_SHOW_MEMBER_LIST_DELETE.writeId(this, buffer);
		buffer.writeString(this._player);
	}
}
