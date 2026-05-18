package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.ClanMember;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class PledgeShowMemberListUpdate extends ServerPacket
{
	private final int _pledgeType;
	private boolean _hasSponsor;
	private final String _name;
	private final int _level;
	private final int _classId;
	private final int _objectId;
	private final int _onlineStatus;
	private final int _race;
	private final boolean _sex;

	public PledgeShowMemberListUpdate(Player player)
	{
		this(player.getClan().getClanMember(player.getObjectId()));
	}

	public PledgeShowMemberListUpdate(ClanMember member)
	{
		this._name = member.getName();
		this._level = member.getLevel();
		this._classId = member.getClassId();
		this._objectId = member.getObjectId();
		this._pledgeType = member.getPledgeType();
		this._race = member.getRaceOrdinal();
		this._sex = member.getSex();
		this._onlineStatus = member.getOnlineStatus();
		if (this._pledgeType == -1)
		{
			this._hasSponsor = member.getSponsor() != 0;
		}
		else
		{
			this._hasSponsor = false;
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.PLEDGE_SHOW_MEMBER_LIST_UPDATE.writeId(this, buffer);
		buffer.writeString(this._name);
		buffer.writeInt(this._level);
		buffer.writeInt(this._classId);
		buffer.writeInt(this._sex);
		buffer.writeInt(this._race);
		if (this._onlineStatus > 0)
		{
			buffer.writeInt(this._objectId);
			buffer.writeInt(this._pledgeType);
		}
		else
		{
			buffer.writeInt(0);
			buffer.writeInt(0);
		}

		buffer.writeInt(this._hasSponsor);
		buffer.writeByte(this._onlineStatus);
	}
}
