package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExSpawnEmitter extends ServerPacket
{
	private final int _playerObjectId;
	private final int _npcObjectId;

	public ExSpawnEmitter(int playerObjectId, int npcObjectId)
	{
		this._playerObjectId = playerObjectId;
		this._npcObjectId = npcObjectId;
	}

	public ExSpawnEmitter(Player player, Npc npc)
	{
		this(player.getObjectId(), npc.getObjectId());
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SPAWN_EMITTER.writeId(this, buffer);
		buffer.writeInt(this._npcObjectId);
		buffer.writeInt(this._playerObjectId);
		buffer.writeInt(0);
	}
}
