package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.xml.FakePlayerData;
import net.sf.l2jdev.gameserver.model.BlockList;
import net.sf.l2jdev.gameserver.model.ClientSettings;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.PartyRequest;
import net.sf.l2jdev.gameserver.model.groups.Party;
import net.sf.l2jdev.gameserver.model.groups.PartyDistributionType;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.AskJoinParty;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class RequestJoinParty extends ClientPacket
{
	private String _name;
	private int _partyDistributionTypeId;

	@Override
	protected void readImpl()
	{
		this._name = this.readString();
		this._partyDistributionTypeId = this.readInt();
	}

	protected void scheduleDeny(Player player)
	{
		if (player != null)
		{
			if (player.getParty() == null)
			{
				player.sendPacket(SystemMessageId.THE_PARTY_IS_DISBANDED);
			}
			else
			{
				player.sendPacket(SystemMessageId.THE_PLAYER_HAS_DECLINED_TO_JOIN_YOUR_PARTY);
			}

			player.onTransactionResponse();
		}
	}

	@Override
	protected void runImpl()
	{
		Player requestor = this.getPlayer();
		if (requestor != null)
		{
			ClientSettings clientSettings = requestor.getClientSettings();
			if (clientSettings.getPartyContributionType() != this._partyDistributionTypeId)
			{
				requestor.getClientSettings().setPartyContributionType(this._partyDistributionTypeId);
			}

			if (FakePlayerData.getInstance().isTalkable(this._name))
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.C1_HAS_BEEN_INVITED_TO_THE_PARTY);
				sm.addString(FakePlayerData.getInstance().getProperName(this._name));
				requestor.sendPacket(sm);
				if (!requestor.isProcessingRequest())
				{
					ThreadPool.schedule(() -> this.scheduleDeny(requestor), 10000L);
					requestor.blockRequest();
				}
				else
				{
					requestor.sendPacket(SystemMessageId.WAITING_FOR_ANOTHER_REPLY);
				}
			}
			else
			{
				Player target = World.getInstance().getPlayer(this._name);
				if (target == null)
				{
					requestor.sendPacket(SystemMessageId.SELECT_A_PLAYER_YOU_WANT_TO_INVITE_TO_YOUR_PARTY);
				}
				else if (target.getClient() == null || target.getClient().isDetached())
				{
					requestor.sendMessage("Player is in offline mode.");
				}
				else if (requestor.isPartyBanned())
				{
					requestor.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_SO_PARTICIPATING_IN_A_PARTY_IS_NOT_ALLOWED);
					requestor.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else if (target.isPartyBanned())
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.C1_HAS_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_AND_CANNOT_JOIN_A_PARTY);
					sm.addString(target.getAppearance().getVisibleName());
					requestor.sendPacket(sm);
				}
				else
				{
					if (requestor.isRegisteredOnEvent() || target.isRegisteredOnEvent())
					{
						if (!GeneralConfig.ALLOW_PARTY_IN_SAME_EVENT || requestor.getInstanceId() != target.getInstanceId() || !requestor.isRegisteredOnEvent() || !target.isRegisteredOnEvent())
						{
							requestor.sendMessage("Event paticipants cannot be invited to parties.");
							return;
						}

						if (!requestor.getTeam().equals(target.getTeam()))
						{
							requestor.sendMessage("You cannot be invited to a party of another team.");
							return;
						}
					}

					if (target.isInParty())
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.C1_IS_A_MEMBER_OF_ANOTHER_PARTY_AND_CANNOT_BE_INVITED);
						sm.addString(target.getAppearance().getVisibleName());
						requestor.sendPacket(sm);
					}
					else if (BlockList.isBlocked(target, requestor))
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.C1_HAS_ADDED_YOU_TO_THEIR_IGNORE_LIST);
						sm.addString(target.getAppearance().getVisibleName());
						requestor.sendPacket(sm);
					}
					else if (target == requestor)
					{
						requestor.sendPacket(SystemMessageId.THE_TARGET_CANNOT_BE_INVITED);
					}
					else if (this.checkInviteByIgnoredSettings(target, requestor))
					{
						requestor.sendPacket(new SystemMessage(SystemMessageId.C1_IS_SET_TO_REFUSE_PARTY_REQUESTS_AND_CANNOT_RECEIVE_A_PARTY_REQUEST).addPcName(target));
						target.sendPacket(new SystemMessage(SystemMessageId.PARTY_INVITATION_IS_SET_UP_TO_BE_REJECTED_AT_PREFERENCES_THE_PARTY_INVITATION_OF_C1_IS_AUTOMATICALLY_REJECTED).addPcName(requestor));
					}
					else if (target.isCursedWeaponEquipped() || requestor.isCursedWeaponEquipped())
					{
						requestor.sendPacket(SystemMessageId.INVALID_TARGET);
					}
					else if (target.isJailed() || requestor.isJailed())
					{
						requestor.sendMessage("You cannot invite a player while is in Jail.");
					}
					else if ((target.isInOlympiadMode() || requestor.isInOlympiadMode()) && (target.isInOlympiadMode() != requestor.isInOlympiadMode() || target.getOlympiadGameId() != requestor.getOlympiadGameId() || target.getOlympiadSide() != requestor.getOlympiadSide()))
					{
						requestor.sendPacket(SystemMessageId.A_USER_CURRENTLY_PARTICIPATING_IN_THE_OLYMPIAD_CANNOT_SEND_PARTY_AND_FRIEND_INVITATIONS);
					}
					else if (requestor.isProcessingRequest())
					{
						requestor.sendPacket(SystemMessageId.WAITING_FOR_ANOTHER_REPLY);
					}
					else if (target.isProcessingRequest())
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.C1_IS_ON_ANOTHER_TASK_PLEASE_TRY_AGAIN_LATER);
						sm.addString(target.getName());
						requestor.sendPacket(sm);
					}
					else
					{
						Party party = requestor.getParty();
						if (party != null && !party.isLeader(requestor))
						{
							requestor.sendPacket(SystemMessageId.ONLY_THE_LEADER_CAN_GIVE_OUT_INVITATIONS);
						}
						else
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.C1_HAS_BEEN_INVITED_TO_THE_PARTY);
							sm.addString(target.getAppearance().getVisibleName());
							requestor.sendPacket(sm);
							if (!requestor.isInParty())
							{
								this.createNewParty(target, requestor);
							}
							else
							{
								this.addTargetToParty(target, requestor);
							}
						}
					}
				}
			}
		}
	}

	protected void addTargetToParty(Player target, Player requestor)
	{
		Party party = requestor.getParty();
		if (!party.isLeader(requestor))
		{
			requestor.sendPacket(SystemMessageId.ONLY_THE_LEADER_CAN_GIVE_OUT_INVITATIONS);
		}
		else if (party.getMemberCount() >= PlayerConfig.ALT_PARTY_MAX_MEMBERS)
		{
			requestor.sendPacket(SystemMessageId.THE_PARTY_IS_FULL);
		}
		else if (party.getPendingInvitation() && !party.isInvitationRequestExpired())
		{
			requestor.sendPacket(SystemMessageId.WAITING_FOR_ANOTHER_REPLY);
		}
		else if (!target.hasRequest(PartyRequest.class))
		{
			PartyRequest request = new PartyRequest(requestor, target, party);
			request.scheduleTimeout(30000L);
			requestor.addRequest(request);
			target.addRequest(request);
			target.sendPacket(new AskJoinParty(requestor.getAppearance().getVisibleName(), party.getDistributionType()));
			party.setPendingInvitation(true);
		}
		else
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.C1_IS_ON_ANOTHER_TASK_PLEASE_TRY_AGAIN_LATER);
			sm.addString(target.getAppearance().getVisibleName());
			requestor.sendPacket(sm);
		}
	}

	private void createNewParty(Player target, Player requestor)
	{
		PartyDistributionType partyDistributionType = PartyDistributionType.findById(this._partyDistributionTypeId);
		if (partyDistributionType != null)
		{
			if (!target.hasRequest(PartyRequest.class))
			{
				Party party = new Party(requestor, partyDistributionType);
				party.setPendingInvitation(true);
				PartyRequest request = new PartyRequest(requestor, target, party);
				request.scheduleTimeout(30000L);
				requestor.addRequest(request);
				target.addRequest(request);
				target.sendPacket(new AskJoinParty(requestor.getAppearance().getVisibleName(), partyDistributionType));
			}
			else
			{
				requestor.sendPacket(SystemMessageId.WAITING_FOR_ANOTHER_REPLY);
			}
		}
	}

	protected boolean checkInviteByIgnoredSettings(Player target, Player requestor)
	{
		ClientSettings targetClientSettings = target.getClientSettings();
		boolean condition = targetClientSettings.isPartyRequestRestrictedFromOthers();
		boolean clanCheck = target.getClan() != null && requestor.getClan() != null && target.getClan() == requestor.getClan();
		if (condition && (!targetClientSettings.isPartyRequestRestrictedFromFriends() && target.getFriendList().contains(requestor.getObjectId()) || !targetClientSettings.isPartyRequestRestrictedFromClan() && clanCheck))
		{
			condition = false;
		}

		return condition;
	}
}
