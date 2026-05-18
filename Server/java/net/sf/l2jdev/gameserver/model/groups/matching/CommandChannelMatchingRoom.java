package net.sf.l2jdev.gameserver.model.groups.matching;

import net.sf.l2jdev.gameserver.managers.MatchingRoomManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.groups.CommandChannel;
import net.sf.l2jdev.gameserver.model.groups.Party;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.ExManagePartyRoomMemberType;
import net.sf.l2jdev.gameserver.network.enums.UserInfoType;
import net.sf.l2jdev.gameserver.network.serverpackets.ExDissmissMPCCRoom;
import net.sf.l2jdev.gameserver.network.serverpackets.ExMPCCRoomInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.ExMPCCRoomMember;
import net.sf.l2jdev.gameserver.network.serverpackets.ExManageMpccRoomMember;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class CommandChannelMatchingRoom extends MatchingRoom
{
	public CommandChannelMatchingRoom(String title, int loot, int minLevel, int maxLevel, int maxmem, Player leader)
	{
		super(title, loot, minLevel, maxLevel, maxmem, leader);
	}

	@Override
	protected void onRoomCreation(Player player)
	{
		player.sendPacket(SystemMessageId.THE_COMMAND_CHANNEL_MATCHING_ROOM_WAS_CREATED);
	}

	@Override
	protected void notifyInvalidCondition(Player player)
	{
		player.sendPacket(SystemMessageId.YOU_CANNOT_ENTER_THE_COMMAND_CHANNEL_MATCHING_ROOM_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
	}

	@Override
	protected void notifyNewMember(Player player)
	{
		for (Player member : this.getMembers())
		{
			if (member != player)
			{
				member.sendPacket(new ExManageMpccRoomMember(member, this, ExManagePartyRoomMemberType.ADD_MEMBER));
			}
		}

		SystemMessage sm = new SystemMessage(SystemMessageId.C1_ENTERED_THE_COMMAND_CHANNEL_MATCHING_ROOM);
		sm.addPcName(player);

		for (Player memberx : this.getMembers())
		{
			if (memberx != player)
			{
				memberx.sendPacket(sm);
			}
		}

		player.sendPacket(new ExMPCCRoomInfo(this));
		player.sendPacket(new ExMPCCRoomMember(player, this));
	}

	@Override
	protected void notifyRemovedMember(Player player, boolean kicked, boolean leaderChanged)
	{
		this.getMembers().forEach(p -> {
			p.sendPacket(new ExMPCCRoomInfo(this));
			p.sendPacket(new ExMPCCRoomMember(player, this));
		});
		player.sendPacket(new SystemMessage(kicked ? SystemMessageId.YOU_WERE_EXPELLED_FROM_THE_COMMAND_CHANNEL_MATCHING_ROOM : SystemMessageId.YOU_EXITED_FROM_THE_COMMAND_CHANNEL_MATCHING_ROOM));
	}

	@Override
	public void disbandRoom()
	{
		this.getMembers().forEach(p -> {
			p.sendPacket(SystemMessageId.THE_COMMAND_CHANNEL_MATCHING_ROOM_WAS_CANCELLED);
			p.sendPacket(ExDissmissMPCCRoom.STATIC_PACKET);
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
		return MatchingRoomType.COMMAND_CHANNEL;
	}

	@Override
	public MatchingMemberType getMemberType(Player player)
	{
		if (this.isLeader(player))
		{
			return MatchingMemberType.COMMAND_CHANNEL_LEADER;
		}
		Party playerParty = player.getParty();
		if (playerParty == null)
		{
			return MatchingMemberType.WAITING_PLAYER_NO_PARTY;
		}
		Party leaderParty = this.getLeader().getParty();
		if (leaderParty != null)
		{
			CommandChannel cc = leaderParty.getCommandChannel();
			if (leaderParty == playerParty || cc != null && cc.getParties().contains(playerParty))
			{
				return MatchingMemberType.COMMAND_CHANNEL_PARTY_MEMBER;
			}
		}

		return MatchingMemberType.WAITING_PARTY;
	}
}
