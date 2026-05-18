package net.sf.l2jdev.gameserver.network.serverpackets.pet;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class PetStatusShow extends ServerPacket
{
	private final int _summonType;
	private final int _summonObjectId;

	public PetStatusShow(Summon summon)
	{
		this._summonType = summon.getSummonType();
		this._summonObjectId = summon.getObjectId();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.PET_STATUS_SHOW.writeId(this, buffer);
		buffer.writeInt(this._summonType);
		buffer.writeInt(this._summonObjectId);
	}
}
