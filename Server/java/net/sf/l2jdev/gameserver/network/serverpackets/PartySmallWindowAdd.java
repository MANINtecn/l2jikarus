package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.groups.Party;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class PartySmallWindowAdd extends ServerPacket
{
	private final Player _member;
	private final Party _party;

	public PartySmallWindowAdd(Player member, Party party)
	{
		this._member = member;
		this._party = party;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.PARTY_SMALL_WINDOW_ADD.writeId(this, buffer);
		buffer.writeInt(this._party.getLeaderObjectId());
		buffer.writeInt(this._party.getDistributionType().getId());
		buffer.writeInt(this._member.getObjectId());
		buffer.writeString(this._member.getName());
		buffer.writeInt((int) this._member.getCurrentCp());
		buffer.writeInt(this._member.getMaxCp());
		buffer.writeInt((int) this._member.getCurrentHp());
		buffer.writeInt((int) this._member.getMaxHp());
		buffer.writeInt((int) this._member.getCurrentMp());
		buffer.writeInt(this._member.getMaxMp());
		buffer.writeInt(this._member.getVitalityPoints());
		buffer.writeByte(this._member.getLevel());
		buffer.writeShort(this._member.getPlayerClass().getId());
		buffer.writeByte(0);
		buffer.writeShort(this._member.getRace().ordinal());
		buffer.writeInt(0);
	}
}
