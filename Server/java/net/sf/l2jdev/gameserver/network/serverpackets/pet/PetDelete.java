package net.sf.l2jdev.gameserver.network.serverpackets.pet;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class PetDelete extends ServerPacket
{
	private final int _petType;
	private final int _petObjId;

	public PetDelete(int petType, int petObjId)
	{
		this._petType = petType;
		this._petObjId = petObjId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.PET_DELETE.writeId(this, buffer);
		buffer.writeInt(this._petType);
		buffer.writeInt(this._petObjId);
	}
}
