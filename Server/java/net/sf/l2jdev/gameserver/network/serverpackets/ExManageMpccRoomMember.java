package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.MapRegionManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.groups.matching.CommandChannelMatchingRoom;
import net.sf.l2jdev.gameserver.model.groups.matching.MatchingMemberType;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.enums.ExManagePartyRoomMemberType;

public class ExManageMpccRoomMember extends ServerPacket
{
	private final Player _player;
	private final MatchingMemberType _memberType;
	private final ExManagePartyRoomMemberType _type;

	public ExManageMpccRoomMember(Player player, CommandChannelMatchingRoom room, ExManagePartyRoomMemberType mode)
	{
		this._player = player;
		this._memberType = room.getMemberType(player);
		this._type = mode;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_MANAGE_PARTY_ROOM_MEMBER.writeId(this, buffer);
		buffer.writeInt(this._type.ordinal());
		buffer.writeInt(this._player.getObjectId());
		buffer.writeString(this._player.getName());
		buffer.writeInt(this._player.getPlayerClass().getId());
		buffer.writeInt(this._player.getLevel());
		buffer.writeInt(MapRegionManager.getInstance().getBBs(this._player.getLocation()));
		buffer.writeInt(this._memberType.ordinal());
	}
}
