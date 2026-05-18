package net.sf.l2jdev.gameserver.network.serverpackets.revenge;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExPvpBookShareRevengeKillerLocation extends ServerPacket
{
	private final Player _player;

	public ExPvpBookShareRevengeKillerLocation(Player player)
	{
		this._player = player;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PVPBOOK_SHARE_REVENGE_KILLER_LOCATION.writeId(this, buffer);
		buffer.writeSizedString(this._player.getName());
		buffer.writeInt(this._player.getX());
		buffer.writeInt(this._player.getY());
		buffer.writeInt(this._player.getZ());
	}
}
