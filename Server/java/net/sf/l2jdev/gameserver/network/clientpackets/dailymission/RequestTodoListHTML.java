package net.sf.l2jdev.gameserver.network.clientpackets.dailymission;

import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class RequestTodoListHTML extends ClientPacket
{
	protected int _tab;
	protected String _linkName;

	@Override
	protected void readImpl()
	{
		this._tab = this.readByte();
		this._linkName = this.readString();
	}

	@Override
	protected void runImpl()
	{
	}
}
