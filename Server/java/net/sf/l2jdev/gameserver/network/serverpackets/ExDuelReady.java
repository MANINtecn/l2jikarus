package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExDuelReady extends ServerPacket
{
	public static final ExDuelReady PLAYER_DUEL = new ExDuelReady(false);
	public static final ExDuelReady PARTY_DUEL = new ExDuelReady(true);
	private final boolean _partyDuel;

	public ExDuelReady(boolean isPartyDuel)
	{
		this._partyDuel = isPartyDuel;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_DUEL_READY.writeId(this, buffer);
		buffer.writeInt(this._partyDuel);
	}
}
