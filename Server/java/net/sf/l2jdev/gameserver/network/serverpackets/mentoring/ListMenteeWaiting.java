package net.sf.l2jdev.gameserver.network.serverpackets.mentoring;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ListMenteeWaiting extends ServerPacket
{
	 
	private final List<Player> _possibleCandiates = new ArrayList<>();
	private final int _page;

	public ListMenteeWaiting(int page, int minLevel, int maxLevel)
	{
		this._page = page;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_MENTEE_WAITING_LIST.writeId(this, buffer);
		buffer.writeInt(1);
		if (this._possibleCandiates.isEmpty())
		{
			buffer.writeInt(0);
			buffer.writeInt(0);
		}
		else
		{
			buffer.writeInt(this._possibleCandiates.size());
			buffer.writeInt(this._possibleCandiates.size() % 64);

			for (Player player : this._possibleCandiates)
			{
				if (1 <= 64 * this._page && 1 > 64 * (this._page - 1))
				{
					buffer.writeString(player.getName());
					buffer.writeInt(player.getActiveClass());
					buffer.writeInt(player.getLevel());
				}
			}
		}
	}
}
