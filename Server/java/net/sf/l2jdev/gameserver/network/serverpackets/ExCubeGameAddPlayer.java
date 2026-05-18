package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExCubeGameAddPlayer extends ServerPacket
{
	private final Player _player;
	private final boolean _isRedTeam;

	public ExCubeGameAddPlayer(Player player, boolean isRedTeam)
	{
		this._player = player;
		this._isRedTeam = isRedTeam;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_BLOCK_UPSET_LIST.writeId(this, buffer);
		buffer.writeInt(1);
		buffer.writeInt(-1);
		buffer.writeInt(this._isRedTeam);
		buffer.writeInt(this._player.getObjectId());
		buffer.writeString(this._player.getName());
	}
}
