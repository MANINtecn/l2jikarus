package net.sf.l2jdev.gameserver.network.serverpackets.pk;

import java.util.Set;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExPkPenaltyListOnlyLoc extends ServerPacket
{
	private final int _lastPkTime = World.getInstance().getLastPkTime();
	private final Set<Player> _players = World.getInstance().getPkPlayers();

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PK_PENALTY_LIST_ONLY_LOC.writeId(this, buffer);
		buffer.writeInt(this._lastPkTime);
		buffer.writeInt(this._players.size());

		for (Player player : this._players)
		{
			buffer.writeInt(player.getObjectId());
			buffer.writeInt(player.getX());
			buffer.writeInt(player.getY());
			buffer.writeInt(player.getZ());
		}
	}
}
