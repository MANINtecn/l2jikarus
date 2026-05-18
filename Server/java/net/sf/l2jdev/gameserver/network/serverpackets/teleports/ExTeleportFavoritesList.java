package net.sf.l2jdev.gameserver.network.serverpackets.teleports;

import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExTeleportFavoritesList extends ServerPacket
{
	private final List<Integer> _teleports;
	private final boolean _enable;

	public ExTeleportFavoritesList(Player player, boolean enable)
	{
		this._teleports = player.getVariables().getIntegerList("FAVORITE_TELEPORTS");
		this._enable = enable;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_TELEPORT_FAVORITES_LIST.writeId(this, buffer);
		buffer.writeByte(this._enable);
		buffer.writeInt(this._teleports.size());

		for (int id : this._teleports)
		{
			buffer.writeInt(id);
		}
	}
}
