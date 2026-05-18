package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class PartySmallWindowDelete extends ServerPacket
{
	private final Player _member;

	public PartySmallWindowDelete(Player member)
	{
		this._member = member;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.PARTY_SMALL_WINDOW_DELETE.writeId(this, buffer);
		buffer.writeInt(this._member.getObjectId());
		buffer.writeString(this._member.getName());
	}
}
