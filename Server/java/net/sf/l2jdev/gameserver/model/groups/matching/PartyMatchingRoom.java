package net.sf.l2jdev.gameserver.model.groups.matching;

import net.sf.l2jdev.gameserver.managers.MatchingRoomManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.groups.Party;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.UserInfoType;
import net.sf.l2jdev.gameserver.network.serverpackets.ExClosePartyRoom;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPartyRoomMember;
import net.sf.l2jdev.gameserver.network.serverpackets.ListPartyWaiting;
import net.sf.l2jdev.gameserver.network.serverpackets.PartyRoomInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class PartyMatchingRoom extends MatchingRoom
{
	public PartyMatchingRoom(String title, int loot, int minLevel, int maxLevel, int maxmem, Player leader)
	{
		super(title, loot, minLevel, maxLevel, maxmem, leader);
	}

	@Override
	protected void onRoomCreation(Player player)
	{
		player.broadcastUserInfo(UserInfoType.CLAN);
		player.sendPacket(new ListPartyWaiting(PartyMatchingRoomLevelType.ALL, -1, 1, player.getLevel()));
		player.sendPacket(SystemMessageId.YOU_HAVE_CREATED_A_PARTY_ROOM);
	}

	@Override
	protected void notifyInvalidCondition(Player player)
	{
		player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_THE_REQUIREMENTS_TO_ENTER_A_PARTY_ROOM);
	}

	@Override
	protected void notifyNewMember(Player player)
	{
		for (Player member : this.getMembers())
		{
			if (member != player)
			{
				member.sendPacket(new ExPartyRoomMember(member, this));
			}
		}

		SystemMessage sm = new SystemMessage(SystemMessageId.C1_HAS_ENTERED_THE_PARTY_ROOM);
		sm.addPcName(player);

		for (Player memberx : this.getMembers())
		{
			if (memberx != player)
			{
				memberx.sendPacket(sm);
			}
		}

		player.sendPacket(new PartyRoomInfo(this));
		player.sendPacket(new ExPartyRoomMember(player, this));
	}

	@Override
	protected void notifyRemovedMember(Player player, boolean kicked, boolean leaderChanged)
	{
		SystemMessage sm = new SystemMessage(kicked ? SystemMessageId.C1_HAS_BEEN_KICKED_FROM_THE_PARTY_ROOM : SystemMessageId.C1_HAS_LEFT_THE_PARTY_ROOM);
		sm.addPcName(player);
		this.getMembers().forEach(p -> {
			p.sendPacket(new PartyRoomInfo(this));
			p.sendPacket(new ExPartyRoomMember(player, this));
			p.sendPacket(sm);
			p.sendPacket(SystemMessageId.THE_LEADER_OF_THE_PARTY_ROOM_HAS_CHANGED);
		});
		player.sendPacket(new SystemMessage(kicked ? SystemMessageId.YOU_HAVE_BEEN_OUSTED_FROM_THE_PARTY_ROOM : SystemMessageId.YOU_HAVE_EXITED_THE_PARTY_ROOM));
		player.sendPacket(ExClosePartyRoom.STATIC_PACKET);
	}

	@Override
	public void disbandRoom()
	{
		this.getMembers().forEach(p -> {
			p.sendPacket(SystemMessageId.THE_PARTY_ROOM_HAS_BEEN_DISBANDED);
			p.sendPacket(ExClosePartyRoom.STATIC_PACKET);
			p.setMatchingRoom(null);
			p.broadcastUserInfo(UserInfoType.CLAN);
			MatchingRoomManager.getInstance().addToWaitingList(p);
		});
		this.getMembers().clear();
		MatchingRoomManager.getInstance().removeMatchingRoom(this);
	}

	@Override
	public MatchingRoomType getRoomType()
	{
		return MatchingRoomType.PARTY;
	}

	@Override
	public MatchingMemberType getMemberType(Player player)
	{
		if (this.isLeader(player))
		{
			return MatchingMemberType.PARTY_LEADER;
		}
		Party leaderParty = this.getLeader().getParty();
		Party playerParty = player.getParty();
		return leaderParty != null && playerParty != null && playerParty == leaderParty ? MatchingMemberType.PARTY_MEMBER : MatchingMemberType.WAITING_PLAYER;
	}
}
