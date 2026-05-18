package net.sf.l2jdev.gameserver.network.serverpackets.penaltyitemdrop;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExPenaltyItemInfo extends ServerPacket
{
	private final Player _player;

	public ExPenaltyItemInfo(Player player)
	{
		this._player = player;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PENALTY_ITEM_INFO.writeId(this, buffer);
		buffer.writeInt(this._player.getItemPenaltyList().size());
		buffer.writeInt(1);
	}
}
