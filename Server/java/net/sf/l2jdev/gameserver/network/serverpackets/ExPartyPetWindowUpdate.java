package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExPartyPetWindowUpdate extends ServerPacket
{
	private final Summon _summon;

	public ExPartyPetWindowUpdate(Summon summon)
	{
		this._summon = summon;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PARTY_PET_WINDOW_UPDATE.writeId(this, buffer);
		buffer.writeInt(this._summon.getObjectId());
		buffer.writeInt(this._summon.getTemplate().getDisplayId() + 1000000);
		buffer.writeByte(this._summon.getSummonType());
		buffer.writeInt(this._summon.getOwner().getObjectId());
		buffer.writeInt((int) this._summon.getCurrentHp());
		buffer.writeInt((int) this._summon.getMaxHp());
		buffer.writeInt((int) this._summon.getCurrentMp());
		buffer.writeInt(this._summon.getMaxMp());
	}
}
