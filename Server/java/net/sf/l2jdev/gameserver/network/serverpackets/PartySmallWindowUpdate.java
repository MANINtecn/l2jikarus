package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.enums.PartySmallWindowUpdateType;

public class PartySmallWindowUpdate extends AbstractMaskPacket<PartySmallWindowUpdateType>
{
	private final Player _member;
	private int _flags = 0;

	public PartySmallWindowUpdate(Player member, boolean addAllFlags)
	{
		this._member = member;
		if (addAllFlags)
		{
			for (PartySmallWindowUpdateType type : PartySmallWindowUpdateType.values())
			{
				this.addComponentType(type);
			}
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.PARTY_SMALL_WINDOW_UPDATE.writeId(this, buffer);
		buffer.writeInt(this._member.getObjectId());
		buffer.writeShort(this._flags);
		if (this.containsMask(PartySmallWindowUpdateType.CURRENT_CP))
		{
			buffer.writeInt((int) this._member.getCurrentCp());
		}

		if (this.containsMask(PartySmallWindowUpdateType.MAX_CP))
		{
			buffer.writeInt(this._member.getMaxCp());
		}

		if (this.containsMask(PartySmallWindowUpdateType.CURRENT_HP))
		{
			buffer.writeInt((int) this._member.getCurrentHp());
		}

		if (this.containsMask(PartySmallWindowUpdateType.MAX_HP))
		{
			buffer.writeInt((int) this._member.getMaxHp());
		}

		if (this.containsMask(PartySmallWindowUpdateType.CURRENT_MP))
		{
			buffer.writeInt((int) this._member.getCurrentMp());
		}

		if (this.containsMask(PartySmallWindowUpdateType.MAX_MP))
		{
			buffer.writeInt(this._member.getMaxMp());
		}

		if (this.containsMask(PartySmallWindowUpdateType.LEVEL))
		{
			buffer.writeByte(this._member.getLevel());
		}

		if (this.containsMask(PartySmallWindowUpdateType.CLASS_ID))
		{
			buffer.writeShort(this._member.getPlayerClass().getId());
		}

		if (this.containsMask(PartySmallWindowUpdateType.PARTY_SUBSTITUTE))
		{
			buffer.writeByte(0);
		}

		if (this.containsMask(PartySmallWindowUpdateType.VITALITY_POINTS))
		{
			buffer.writeInt(this._member.getVitalityPoints());
		}
	}

	@Override
	protected void addMask(int mask)
	{
		this._flags |= mask;
	}

	@Override
	public boolean containsMask(PartySmallWindowUpdateType component)
	{
		return this.containsMask(this._flags, component);
	}

	@Override
	protected byte[] getMasks()
	{
		return new byte[0];
	}
}
