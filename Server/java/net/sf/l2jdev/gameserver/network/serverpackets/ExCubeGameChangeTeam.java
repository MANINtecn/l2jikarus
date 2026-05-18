package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExCubeGameChangeTeam extends ServerPacket
{
	private final Player _player;
	private final boolean _fromRedTeam;

	public ExCubeGameChangeTeam(Player player, boolean fromRedTeam)
	{
		this._player = player;
		this._fromRedTeam = fromRedTeam;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_BLOCK_UPSET_LIST.writeId(this, buffer);
		buffer.writeInt(5);
		buffer.writeInt(this._player.getObjectId());
		buffer.writeInt(this._fromRedTeam);
		buffer.writeInt(!this._fromRedTeam);
	}
}
