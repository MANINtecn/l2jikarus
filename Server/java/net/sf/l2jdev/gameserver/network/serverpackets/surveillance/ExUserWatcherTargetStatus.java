package net.sf.l2jdev.gameserver.network.serverpackets.surveillance;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExUserWatcherTargetStatus extends ServerPacket
{
	private final String _name;
	private final boolean _online;

	public ExUserWatcherTargetStatus(String name, boolean online)
	{
		this._name = name;
		this._online = online;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_USER_WATCHER_TARGET_STATUS.writeId(this, buffer);
		buffer.writeSizedString(this._name);
		buffer.writeInt(0);
		buffer.writeByte(this._online ? 1 : 0);
	}
}
