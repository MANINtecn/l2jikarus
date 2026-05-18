package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.MapRegionManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.groups.matching.CommandChannelMatchingRoom;
import net.sf.l2jdev.gameserver.model.groups.matching.MatchingMemberType;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExMPCCRoomMember extends ServerPacket
{
	private final CommandChannelMatchingRoom _room;
	private final MatchingMemberType _type;

	public ExMPCCRoomMember(Player player, CommandChannelMatchingRoom room)
	{
		this._room = room;
		this._type = room.getMemberType(player);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_MPCC_ROOM_MEMBER.writeId(this, buffer);
		buffer.writeInt(this._type.ordinal());
		buffer.writeInt(this._room.getMembersCount());

		for (Player member : this._room.getMembers())
		{
			buffer.writeInt(member.getObjectId());
			buffer.writeString(member.getName());
			buffer.writeInt(member.getLevel());
			buffer.writeInt(member.getPlayerClass().getId());
			buffer.writeInt(MapRegionManager.getInstance().getBBs(member.getLocation()));
			buffer.writeInt(this._room.getMemberType(member).ordinal());
		}
	}
}
