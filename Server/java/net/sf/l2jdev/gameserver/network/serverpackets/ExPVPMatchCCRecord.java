package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Map;
import java.util.Map.Entry;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExPVPMatchCCRecord extends ServerPacket
{
	public static final int INITIALIZE = 0;
	public static final int UPDATE = 1;
	public static final int FINISH = 2;
	private final int _state;
	private final Map<Player, Integer> _players;

	public ExPVPMatchCCRecord(int state, Map<Player, Integer> players)
	{
		this._state = state;
		this._players = players;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PVPMATCH_CC_RECORD.writeId(this, buffer);
		buffer.writeInt(this._state);
		buffer.writeInt(Math.min(this._players.size(), 25));
		int counter = 0;

		for (Entry<Player, Integer> entry : this._players.entrySet())
		{
			if (++counter > 25)
			{
				break;
			}

			buffer.writeString(entry.getKey().getName());
			buffer.writeInt(entry.getValue());
		}
	}
}
