package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExPartyPetWindowDelete extends ServerPacket
{
	private final Summon _summon;

	public ExPartyPetWindowDelete(Summon summon)
	{
		this._summon = summon;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PARTY_PET_WINDOW_DELETE.writeId(this, buffer);
		buffer.writeInt(this._summon.getObjectId());
		buffer.writeByte(this._summon.getSummonType());
		buffer.writeInt(this._summon.getOwner().getObjectId());
	}
}
