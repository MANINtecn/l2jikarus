package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExDuelEnd extends ServerPacket
{
	public static final ExDuelEnd PLAYER_DUEL = new ExDuelEnd(false);
	public static final ExDuelEnd PARTY_DUEL = new ExDuelEnd(true);
	private final boolean _partyDuel;

	public ExDuelEnd(boolean isPartyDuel)
	{
		this._partyDuel = isPartyDuel;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_DUEL_END.writeId(this, buffer);
		buffer.writeInt(this._partyDuel);
	}
}
