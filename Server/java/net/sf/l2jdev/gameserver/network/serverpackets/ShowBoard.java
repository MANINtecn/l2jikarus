package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ShowBoard extends ServerPacket
{
	private final String _content;
	private int _showBoard = 1;

	public ShowBoard(String htmlCode, String id)
	{
		this._content = id + "\b" + htmlCode;
	}

	public ShowBoard()
	{
		this._showBoard = 0;
		this._content = "";
	}

	public ShowBoard(List<String> arg)
	{
		StringBuilder builder = new StringBuilder(256).append("1002\b");

		for (String str : arg)
		{
			builder.append(str).append("\b");
		}

		this._content = builder.toString();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.SHOW_BOARD.writeId(this, buffer);
		buffer.writeByte(this._showBoard);
		buffer.writeString("bypass _bbshome");
		buffer.writeString("bypass _bbsgetfav");
		buffer.writeString("bypass _bbsloc");
		buffer.writeString("bypass _bbsclan");
		buffer.writeString("bypass _bbsmemo");
		buffer.writeString("bypass _bbsmail");
		buffer.writeString("bypass _bbsfriends");
		buffer.writeString("bypass bbs_add_fav");
		buffer.writeString(this._content);
	}
}
