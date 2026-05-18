package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExCubeGameTeamList extends ServerPacket
{
	private final List<Player> _bluePlayers;
	private final List<Player> _redPlayers;
	private final int _roomNumber;

	public ExCubeGameTeamList(List<Player> redPlayers, List<Player> bluePlayers, int roomNumber)
	{
		this._redPlayers = redPlayers;
		this._bluePlayers = bluePlayers;
		this._roomNumber = roomNumber - 1;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_BLOCK_UPSET_LIST.writeId(this, buffer);
		buffer.writeInt(0);
		buffer.writeInt(this._roomNumber);
		buffer.writeInt(-1);
		buffer.writeInt(this._bluePlayers.size());

		for (Player player : this._bluePlayers)
		{
			buffer.writeInt(player.getObjectId());
			buffer.writeString(player.getName());
		}

		buffer.writeInt(this._redPlayers.size());

		for (Player player : this._redPlayers)
		{
			buffer.writeInt(player.getObjectId());
			buffer.writeString(player.getName());
		}
	}
}
