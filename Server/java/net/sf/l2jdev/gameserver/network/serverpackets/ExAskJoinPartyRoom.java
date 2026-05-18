package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExAskJoinPartyRoom extends ServerPacket
{
	private final String _charName;
	private final String _roomName;

	public ExAskJoinPartyRoom(Player player)
	{
		this._charName = player.getName();
		this._roomName = player.getMatchingRoom().getTitle();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ASK_JOIN_PARTY_ROOM.writeId(this, buffer);
		buffer.writeString(this._charName);
		buffer.writeString(this._roomName);
	}
}
