package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.enums.ChatType;

public class Snoop extends ServerPacket
{
	private final int _convoId;
	private final String _name;
	private final ChatType _type;
	private final String _speaker;
	private final String _msg;

	public Snoop(int id, String name, ChatType type, String speaker, String msg)
	{
		this._convoId = id;
		this._name = name;
		this._type = type;
		this._speaker = speaker;
		this._msg = msg;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.SNOOP.writeId(this, buffer);
		buffer.writeInt(this._convoId);
		buffer.writeString(this._name);
		buffer.writeInt(0);
		buffer.writeInt(this._type.getClientId());
		buffer.writeString(this._speaker);
		buffer.writeString(this._msg);
	}
}
