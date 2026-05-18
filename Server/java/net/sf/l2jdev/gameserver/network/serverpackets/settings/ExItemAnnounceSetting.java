package net.sf.l2jdev.gameserver.network.serverpackets.settings;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExItemAnnounceSetting extends ServerPacket
{
	private final boolean _announceDisabled;

	public ExItemAnnounceSetting(boolean announceDisabled)
	{
		this._announceDisabled = announceDisabled;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ITEM_ANNOUNCE_SETTING.writeId(this, buffer);
		buffer.writeByte(this._announceDisabled);
	}
}
