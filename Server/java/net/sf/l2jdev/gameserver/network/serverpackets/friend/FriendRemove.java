package net.sf.l2jdev.gameserver.network.serverpackets.friend;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class FriendRemove extends ServerPacket
{
	private final int _responce;
	private final String _charName;

	public FriendRemove(String charName, int responce)
	{
		this._responce = responce;
		this._charName = charName;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.FRIEND_REMOVE.writeId(this, buffer);
		buffer.writeInt(this._responce);
		buffer.writeString(this._charName);
	}
}
