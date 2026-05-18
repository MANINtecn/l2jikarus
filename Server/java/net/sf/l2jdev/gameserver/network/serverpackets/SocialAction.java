package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class SocialAction extends ServerPacket
{
	public static final int LEVEL_UP = 2122;
	private final int _objectId;
	private final int _actionId;

	public SocialAction(int objectId, int actionId)
	{
		this._objectId = objectId;
		this._actionId = actionId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.SOCIAL_ACTION.writeId(this, buffer);
		buffer.writeInt(this._objectId);
		buffer.writeInt(this._actionId);
		buffer.writeInt(0);
	}

	@Override
	public boolean canBeDropped(GameClient client)
	{
		return true;
	}
}
