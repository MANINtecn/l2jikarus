package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class TutorialEnableClientEvent extends ServerPacket
{
	private int _eventId = 0;

	public TutorialEnableClientEvent(int event)
	{
		this._eventId = event;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.TUTORIAL_ENABLE_CLIENT_EVENT.writeId(this, buffer);
		buffer.writeInt(this._eventId);
	}
}
