package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.MailManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExUnReadMailCount extends ServerPacket
{
	private final int _mailUnreadCount;

	public ExUnReadMailCount(Player player)
	{
		this._mailUnreadCount = (int) MailManager.getInstance().getUnreadCount(player);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_UNREADMAIL_COUNT.writeId(this, buffer);
		buffer.writeInt(this._mailUnreadCount);
	}
}
