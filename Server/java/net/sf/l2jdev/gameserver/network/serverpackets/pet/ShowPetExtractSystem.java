package net.sf.l2jdev.gameserver.network.serverpackets.pet;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ShowPetExtractSystem extends ServerPacket
{
	public static final ShowPetExtractSystem STATIC_PACKET = new ShowPetExtractSystem();

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHOW_PET_EXTRACT_SYSTEM.writeId(this, buffer);
		buffer.writeInt(0);
	}
}
