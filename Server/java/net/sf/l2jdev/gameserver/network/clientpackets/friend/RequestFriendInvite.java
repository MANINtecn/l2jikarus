package net.sf.l2jdev.gameserver.network.clientpackets.friend;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.data.xml.FakePlayerData;
import net.sf.l2jdev.gameserver.model.BlockList;
import net.sf.l2jdev.gameserver.model.ClientSettings;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.friend.FriendAddRequest;

public class RequestFriendInvite extends ClientPacket
{
	private String _name;

	@Override
	protected void readImpl()
	{
		this._name = this.readString();
	}

	protected void scheduleDeny(Player player)
	{
		if (player != null)
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_FAILED_TO_ADD_A_FRIEND_TO_YOUR_FRIENDS_LIST);
			player.onTransactionResponse();
		}
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (FakePlayerData.getInstance().isTalkable(this._name))
			{
				if (!player.isProcessingRequest())
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_VE_REQUESTED_C1_TO_BE_ON_YOUR_FRIENDS_LIST);
					sm.addString(this._name);
					player.sendPacket(sm);
					ThreadPool.schedule(() -> this.scheduleDeny(player), 10000L);
					player.blockRequest();
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.C1_IS_ON_ANOTHER_TASK_PLEASE_TRY_AGAIN_LATER);
					sm.addString(this._name);
					player.sendPacket(sm);
				}
			}
			else
			{
				Player friend = World.getInstance().getPlayer(this._name);
				if (friend == null || !friend.isOnline() || friend.isInvisible())
				{
					player.sendPacket(SystemMessageId.THE_USER_WHO_REQUESTED_TO_BECOME_FRIENDS_IS_NOT_FOUND_IN_THE_GAME);
				}
				else if (friend == player)
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_ADD_YOURSELF_TO_YOUR_OWN_FRIEND_LIST);
				}
				else if (player.isInOlympiadMode() || friend.isInOlympiadMode())
				{
					player.sendPacket(SystemMessageId.A_USER_CURRENTLY_PARTICIPATING_IN_THE_OLYMPIAD_CANNOT_SEND_PARTY_AND_FRIEND_INVITATIONS);
				}
				else if (player.isOnEvent())
				{
					player.sendMessage("You cannot request friendship while participating in an event.");
				}
				else if (BlockList.isBlocked(friend, player))
				{
					player.sendMessage("You are in target's block list.");
				}
				else if (BlockList.isBlocked(player, friend))
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_BLOCKED_C1);
					sm.addString(friend.getName());
					player.sendPacket(sm);
				}
				else if (player.getFriendList().contains(friend.getObjectId()))
				{
					player.sendPacket(SystemMessageId.THIS_PLAYER_IS_ALREADY_REGISTERED_ON_YOUR_FRIENDS_LIST);
				}
				else if (friend.isProcessingRequest())
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.C1_IS_ON_ANOTHER_TASK_PLEASE_TRY_AGAIN_LATER);
					sm.addString(this._name);
					player.sendPacket(sm);
				}
				else if (this.checkInviteByIgnoredSettings(friend, player))
				{
					player.sendPacket(new SystemMessage(SystemMessageId.PREFERENCES_IS_CONFIGURED_TO_REFUSE_FRIEND_REQUESTS_AND_THE_FRIEND_INVITATION_OF_C1_IS_AUTOMATICALLY_REJECTED).addPcName(friend));
				}
				else
				{
					player.onTransactionRequest(friend);
					friend.sendPacket(new FriendAddRequest(player.getName()));
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_VE_REQUESTED_C1_TO_BE_ON_YOUR_FRIENDS_LIST);
					sm.addString(this._name);
					player.sendPacket(sm);
				}
			}
		}
	}

	protected boolean checkInviteByIgnoredSettings(Player target, Player requestor)
	{
		ClientSettings targetClientSettings = target.getClientSettings();
		boolean condition = targetClientSettings.isFriendRequestRestrictedFromOthers();
		return condition && !targetClientSettings.isFriendRequestRestrictedFromClan() && target.getClan() != null && requestor.getClan() != null && target.getClan() == requestor.getClan() ? false : condition;
	}
}
