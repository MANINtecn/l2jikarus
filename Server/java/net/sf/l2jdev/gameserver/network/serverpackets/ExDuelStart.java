package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExDuelStart extends ServerPacket
{
	public static final ExDuelStart PLAYER_DUEL = new ExDuelStart(false);
	public static final ExDuelStart PARTY_DUEL = new ExDuelStart(true);
	private final boolean _partyDuel;

	public ExDuelStart(boolean isPartyDuel)
	{
		this._partyDuel = isPartyDuel;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_DUEL_START.writeId(this, buffer);
		buffer.writeInt(this._partyDuel);
	}
}
