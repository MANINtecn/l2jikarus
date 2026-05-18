package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class PetitionVotePacket extends ServerPacket
{
	public static final PetitionVotePacket STATIC_PACKET = new PetitionVotePacket();

	private PetitionVotePacket()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.PETITION_VOTE.writeId(this, buffer);
	}
}
