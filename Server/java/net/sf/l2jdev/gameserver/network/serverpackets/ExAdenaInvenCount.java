package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExAdenaInvenCount extends ServerPacket
{
	private final Player _player;

	public ExAdenaInvenCount(Player player)
	{
		this._player = player;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ADENA_INVEN_COUNT.writeId(this, buffer);
		buffer.writeLong(this._player.getAdena());
		buffer.writeShort(this._player.getInventory().getSize());
	}
}
