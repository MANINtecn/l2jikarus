package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class L2FriendSay extends ServerPacket
{
	private final String _sender;
	private final String _receiver;
	private final String _message;

	public L2FriendSay(String sender, String reciever, String message)
	{
		this._sender = sender;
		this._receiver = reciever;
		this._message = message;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.L2_FRIEND_SAY.writeId(this, buffer);
		buffer.writeInt(0);
		buffer.writeString(this._receiver);
		buffer.writeString(this._sender);
		buffer.writeString(this._message);
	}
}
