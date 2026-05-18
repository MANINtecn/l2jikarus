package net.sf.l2jdev.gameserver.network.serverpackets.friend;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class FriendAddRequestResult extends ServerPacket
{
	private final int _result;
	private final int _charId;
	private final String _charName;
	private final int _isOnline;
	private final int _charObjectId;
	private final int _charLevel;
	private final int _charClassId;

	public FriendAddRequestResult(Player player, int result)
	{
		this._result = result;
		this._charId = player.getObjectId();
		this._charName = player.getName();
		this._isOnline = player.isOnlineInt();
		this._charObjectId = player.getObjectId();
		this._charLevel = player.getLevel();
		this._charClassId = player.getActiveClass();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.FRIEND_ADD_REQUEST_RESULT.writeId(this, buffer);
		buffer.writeInt(this._result);
		buffer.writeInt(this._charId);
		buffer.writeString(this._charName);
		buffer.writeInt(this._isOnline);
		buffer.writeInt(this._charObjectId);
		buffer.writeInt(this._charLevel);
		buffer.writeInt(this._charClassId);
		buffer.writeShort(0);
	}
}
