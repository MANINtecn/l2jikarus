package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class PrivateStoreMsgBuy extends ServerPacket
{
	private final int _objectId;
	private String _message;

	public PrivateStoreMsgBuy(Player player)
	{
		this._objectId = player.getObjectId();
		if (player.getBuyList() != null)
		{
			this._message = player.getBuyList().getTitle();
		}
	}

	public PrivateStoreMsgBuy(int objectId, String message)
	{
		this._objectId = objectId;
		this._message = message;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.PRIVATE_STORE_BUY_MSG.writeId(this, buffer);
		buffer.writeInt(this._objectId);
		buffer.writeString(this._message);
	}
}
