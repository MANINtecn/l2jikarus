package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExBrPremiumState extends ServerPacket
{
	private final Player _player;

	public ExBrPremiumState(Player player)
	{
		this._player = player;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_BR_NOTIFY_PREMIUM_STATE.writeId(this, buffer);
		buffer.writeInt(this._player.getObjectId());
		buffer.writeByte(this._player.hasPremiumStatus() || this._player.getVipTier() > 0);
	}
}
