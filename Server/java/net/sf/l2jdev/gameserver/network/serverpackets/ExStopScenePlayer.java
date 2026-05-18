package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.enums.Movie;

public class ExStopScenePlayer extends ServerPacket
{
	private final Movie _movie;

	public ExStopScenePlayer(Movie movie)
	{
		this._movie = movie;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_STOP_SCENE_PLAYER.writeId(this, buffer);
		buffer.writeInt(this._movie.getClientId());
	}
}
