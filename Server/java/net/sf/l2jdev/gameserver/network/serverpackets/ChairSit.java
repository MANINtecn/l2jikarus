package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ChairSit extends ServerPacket
{
	private final Player _player;
	private final int _staticObjectId;

	public ChairSit(Player player, int staticObjectId)
	{
		this._player = player;
		this._staticObjectId = staticObjectId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.CHAIR_SIT.writeId(this, buffer);
		buffer.writeInt(this._player.getObjectId());
		buffer.writeInt(this._staticObjectId);
	}
}
