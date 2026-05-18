package net.sf.l2jdev.gameserver.network.serverpackets.payback;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class PaybackUILauncher extends ServerPacket
{
	private final boolean _isEnabled;

	public PaybackUILauncher(boolean isEnabled)
	{
		this._isEnabled = isEnabled;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PAYBACK_UI_LAUNCHER.writeId(this, buffer);
		buffer.writeByte(this._isEnabled);
	}
}
