package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExDuelAskStart extends ServerPacket
{
	private final String _requestorName;
	private final int _partyDuel;

	public ExDuelAskStart(String requestor, int partyDuel)
	{
		this._requestorName = requestor;
		this._partyDuel = partyDuel;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_DUEL_ASK_START.writeId(this, buffer);
		buffer.writeString(this._requestorName);
		buffer.writeInt(this._partyDuel);
	}
}
