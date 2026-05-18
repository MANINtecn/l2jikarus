package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExNoticePostSent extends ServerPacket
{
	private static final ExNoticePostSent STATIC_PACKET_TRUE = new ExNoticePostSent(true);
	private static final ExNoticePostSent STATIC_PACKET_FALSE = new ExNoticePostSent(false);
	private final boolean _showAnim;

	public static ExNoticePostSent valueOf(boolean result)
	{
		return result ? STATIC_PACKET_TRUE : STATIC_PACKET_FALSE;
	}

	public ExNoticePostSent(boolean showAnimation)
	{
		this._showAnim = showAnimation;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_REPLY_WRITE_POST.writeId(this, buffer);
		buffer.writeInt(this._showAnim);
	}
}
