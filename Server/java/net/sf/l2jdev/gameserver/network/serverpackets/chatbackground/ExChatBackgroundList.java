package net.sf.l2jdev.gameserver.network.serverpackets.chatbackground;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.variables.PlayerVariables;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExChatBackgroundList extends ServerPacket
{
	private final int _length;
	private final boolean _yellow;
	private final boolean _blue;

	public ExChatBackgroundList(PlayerVariables variables)
	{
		this._yellow = variables.getBoolean("CHAT_BACKGROUND_YELLOW", false);
		this._blue = variables.getBoolean("CHAT_BACKGROUND_BLUE", false);
		this._length = (this._yellow ? 1 : 0) + (this._blue ? 1 : 0);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CHARACTER_STYLE_LIST.writeId(this, buffer);
		buffer.writeInt(this._length);
		if (this._yellow)
		{
			buffer.writeInt(2);
		}

		if (this._blue)
		{
			buffer.writeInt(1);
		}
	}
}
