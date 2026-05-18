package net.sf.l2jdev.gameserver.network.serverpackets.fishing;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.fishing.FishingEndReason;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExFishingEnd extends ServerPacket
{
	private final Player _player;
	private final FishingEndReason _reason;

	public ExFishingEnd(Player player, FishingEndReason reason)
	{
		this._player = player;
		this._reason = reason;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_FISHING_END.writeId(this, buffer);
		buffer.writeInt(this._player.getObjectId());
		buffer.writeByte(this._reason.getReason());
	}
}
