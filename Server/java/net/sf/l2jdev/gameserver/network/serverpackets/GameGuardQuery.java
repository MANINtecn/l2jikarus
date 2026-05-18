package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class GameGuardQuery extends ServerPacket
{
	public static final GameGuardQuery STATIC_PACKET = new GameGuardQuery();

	private GameGuardQuery()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.GAME_GUARD_QUERY.writeId(this, buffer);
		buffer.writeInt(659766745);
		buffer.writeInt(779265309);
		buffer.writeInt(538379147);
		buffer.writeInt(-1017438557);
	}
}
