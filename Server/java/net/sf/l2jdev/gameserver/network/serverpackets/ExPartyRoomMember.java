package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.InstanceManager;
import net.sf.l2jdev.gameserver.managers.MapRegionManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.groups.matching.MatchingMemberType;
import net.sf.l2jdev.gameserver.model.groups.matching.PartyMatchingRoom;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExPartyRoomMember extends ServerPacket
{
	private final PartyMatchingRoom _room;
	private final MatchingMemberType _type;

	public ExPartyRoomMember(Player player, PartyMatchingRoom room)
	{
		this._room = room;
		this._type = room.getMemberType(player);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PARTY_ROOM_MEMBER.writeId(this, buffer);
		buffer.writeInt(this._type.ordinal());
		buffer.writeInt(this._room.getMembersCount());

		for (Player member : this._room.getMembers())
		{
			buffer.writeInt(member.getObjectId());
			buffer.writeString(member.getName());
			buffer.writeInt(member.getActiveClass());
			buffer.writeInt(member.getLevel());
			buffer.writeInt(MapRegionManager.getInstance().getBBs(member.getLocation()));
			buffer.writeInt(this._room.getMemberType(member).ordinal());
			Map<Integer, Long> instanceTimes = InstanceManager.getInstance().getAllInstanceTimes(member);
			buffer.writeInt(instanceTimes.size());

			for (Entry<Integer, Long> entry : instanceTimes.entrySet())
			{
				long instanceTime = TimeUnit.MILLISECONDS.toSeconds(entry.getValue() - System.currentTimeMillis());
				buffer.writeInt(entry.getKey());
				buffer.writeInt((int) instanceTime);
			}
		}
	}
}
