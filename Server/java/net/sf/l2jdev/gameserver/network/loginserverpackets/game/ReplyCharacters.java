package net.sf.l2jdev.gameserver.network.loginserverpackets.game;

import java.util.Collections;
import java.util.List;

import net.sf.l2jdev.commons.network.base.BaseWritablePacket;

public class ReplyCharacters extends BaseWritablePacket
{
	 
	private final String _account;
	private final int _chars;
	private final List<Long> _timeToDelete;

	public ReplyCharacters(String account, int chars, List<Long> timeToDel)
	{
		this._account = account != null ? account : "";
		this._chars = chars;
		this._timeToDelete = timeToDel != null ? timeToDel : Collections.emptyList();
	}

	@Override
	public void write()
	{
		this.writeByte(8);
		this.writeString(this._account);
		this.writeByte(this._chars);
		this.writeByte(this._timeToDelete.size());

		for (Long t : this._timeToDelete)
		{
			this.writeLong(t != null ? t : 0L);
		}
	}
}
