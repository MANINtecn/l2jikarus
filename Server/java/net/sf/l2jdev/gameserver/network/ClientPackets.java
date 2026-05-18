package net.sf.l2jdev.gameserver.network;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import net.sf.l2jdev.gameserver.config.DevelopmentConfig;
import net.sf.l2jdev.gameserver.network.clientpackets.*;
import net.sf.l2jdev.gameserver.network.clientpackets.enchant.RequestEnchantItem;
import net.sf.l2jdev.gameserver.network.clientpackets.friend.RequestAnswerFriendInvite;
import net.sf.l2jdev.gameserver.network.clientpackets.friend.RequestFriendDel;
import net.sf.l2jdev.gameserver.network.clientpackets.friend.RequestFriendInvite;
import net.sf.l2jdev.gameserver.network.clientpackets.friend.RequestFriendList;
import net.sf.l2jdev.gameserver.network.clientpackets.friend.RequestSendFriendMsg;
import net.sf.l2jdev.gameserver.network.clientpackets.pet.RequestChangePetName;
import net.sf.l2jdev.gameserver.network.clientpackets.pet.RequestGetItemFromPet;
import net.sf.l2jdev.gameserver.network.clientpackets.pet.RequestGiveItemToPet;
import net.sf.l2jdev.gameserver.network.clientpackets.pet.RequestPetGetItem;
import net.sf.l2jdev.gameserver.network.clientpackets.pet.RequestPetUseItem;

public enum ClientPackets
{
	LOGOUT(0, Logout::new, ConnectionState.AUTHENTICATED, ConnectionState.IN_GAME),
	ATTACK(1, AttackRequest::new, ConnectionState.IN_GAME),
	MOVE_BACKWARD_TO_LOCATION(2, null, ConnectionState.IN_GAME),
	START_PLEDGE_WAR(3, RequestStartPledgeWar::new, ConnectionState.IN_GAME),
	REPLY_START_PLEDGE(4, RequestReplyStartPledgeWar::new, ConnectionState.IN_GAME),
	STOP_PLEDGE_WAR(5, RequestStopPledgeWar::new, ConnectionState.IN_GAME),
	REPLY_STOP_PLEDGE_WAR(6, RequestReplyStopPledgeWar::new, ConnectionState.IN_GAME),
	SURRENDER_PLEDGE_WAR(7, RequestSurrenderPledgeWar::new, ConnectionState.IN_GAME),
	REPLY_SURRENDER_PLEDGE_WAR(8, RequestReplySurrenderPledgeWar::new, ConnectionState.IN_GAME),
	SET_PLEDGE_CREST(9, RequestSetPledgeCrest::new, ConnectionState.IN_GAME),
	NOT_USE_14(10, null, ConnectionState.IN_GAME),
	GIVE_NICKNAME(11, RequestGiveNickName::new, ConnectionState.IN_GAME),
	CHARACTER_CREATE(12, CharacterCreate::new, ConnectionState.AUTHENTICATED),
	CHARACTER_DELETE(13, CharacterDelete::new, ConnectionState.AUTHENTICATED),
	VERSION(14, ProtocolVersion::new, ConnectionState.CONNECTED),
	MOVE_TO_LOCATION(15, MoveToLocation::new, ConnectionState.IN_GAME),
	NOT_USE_34(16, null, ConnectionState.IN_GAME),
	ENTER_WORLD(17, EnterWorld::new, ConnectionState.ENTERING),
	CHARACTER_SELECT(18, CharacterSelect::new, ConnectionState.AUTHENTICATED),
	NEW_CHARACTER(19, NewCharacter::new, ConnectionState.AUTHENTICATED),
	ITEMLIST(20, RequestItemList::new, ConnectionState.IN_GAME),
	NOT_USE_1(21, null, ConnectionState.IN_GAME),
	UNEQUIP_ITEM(22, RequestUnEquipItem::new, ConnectionState.IN_GAME),
	DROP_ITEM(23, RequestDropItem::new, ConnectionState.IN_GAME),
	GET_ITEM(24, null, ConnectionState.IN_GAME),
	USE_ITEM(25, UseItem::new, ConnectionState.IN_GAME),
	TRADE_REQUEST(26, TradeRequest::new, ConnectionState.IN_GAME),
	TRADE_ADD(27, AddTradeItem::new, ConnectionState.IN_GAME),
	TRADE_DONE(28, TradeDone::new, ConnectionState.IN_GAME),
	NOT_USE_35(29, null, ConnectionState.IN_GAME),
	NOT_USE_36(30, null, ConnectionState.IN_GAME),
	ACTION(31, Action::new, ConnectionState.IN_GAME),
	NOT_USE_37(32, null, ConnectionState.IN_GAME),
	NOT_USE_38(33, null, ConnectionState.IN_GAME),
	LINK_HTML(34, RequestLinkHtml::new, ConnectionState.IN_GAME),
	PASS_CMD_TO_SERVER(35, RequestBypassToServer::new, ConnectionState.IN_GAME),
	WRITE_BBS(36, RequestBBSwrite::new, ConnectionState.IN_GAME),
	JOIN_PLEDGE(38, RequestJoinPledge::new, ConnectionState.IN_GAME),
	ANSWER_JOIN_PLEDGE(39, RequestAnswerJoinPledge::new, ConnectionState.IN_GAME),
	WITHDRAWAL_PLEDGE(40, RequestWithdrawalPledge::new, ConnectionState.IN_GAME),
	OUST_PLEDGE_MEMBER(41, RequestOustPledgeMember::new, ConnectionState.IN_GAME),
	NOT_USE_40(42, null, ConnectionState.IN_GAME),
	LOGIN(43, AuthLogin::new, ConnectionState.CONNECTED),
	GET_ITEM_FROM_PET(44, RequestGetItemFromPet::new, ConnectionState.IN_GAME),
	NOT_USE_22(45, null, ConnectionState.IN_GAME),
	ALLIANCE_INFO(46, RequestAllyInfo::new, ConnectionState.IN_GAME),
	CRYSTALLIZE_ITEM(47, RequestCrystallizeItem::new, ConnectionState.IN_GAME),
	PRIVATE_STORE_MANAGE_SELL(48, RequestPrivateStoreManageSell::new, ConnectionState.IN_GAME),
	PRIVATE_STORE_LIST_SET(49, SetPrivateStoreListSell::new, ConnectionState.IN_GAME),
	PRIVATE_STORE_MANAGE_CANCEL(50, null, ConnectionState.IN_GAME),
	STOP_MOVE_TOWARD(51, StopMoveToward::new, ConnectionState.IN_GAME),
	SOCIAL_ACTION(52, null, ConnectionState.IN_GAME),
	CHANGE_MOVE_TYPE(53, null, ConnectionState.IN_GAME),
	CHANGE_WAIT_TYPE(54, null, ConnectionState.IN_GAME),
	SELL_LIST(55, RequestSellItem::new, ConnectionState.IN_GAME),
	MAGIC_SKILL_LIST(56, RequestMagicSkillList::new, ConnectionState.IN_GAME),
	MAGIC_SKILL_USE(57, RequestMagicSkillUse::new, ConnectionState.IN_GAME),
	APPEARING(58, Appearing::new, ConnectionState.IN_GAME),
	WAREHOUSE_DEPOSIT_LIST(59, SendWareHouseDepositList::new, ConnectionState.IN_GAME),
	WAREHOUSE_WITHDRAW_LIST(60, SendWareHouseWithDrawList::new, ConnectionState.IN_GAME),
	SHORTCUT_REG(61, RequestShortcutReg::new, ConnectionState.IN_GAME),
	NOT_USE_3(62, null, ConnectionState.IN_GAME),
	DEL_SHORTCUT(63, RequestShortcutDel::new, ConnectionState.IN_GAME),
	BUY_LIST(64, RequestBuyItem::new, ConnectionState.IN_GAME),
	NOT_USE_2(65, null, ConnectionState.IN_GAME),
	JOIN_PARTY(66, RequestJoinParty::new, ConnectionState.IN_GAME),
	ANSWER_JOIN_PARTY(67, RequestAnswerJoinParty::new, ConnectionState.IN_GAME),
	WITHDRAWAL_PARTY(68, RequestWithDrawalParty::new, ConnectionState.IN_GAME),
	OUST_PARTY_MEMBER(69, RequestOustPartyMember::new, ConnectionState.IN_GAME),
	DISMISS_PARTY(70, null, ConnectionState.IN_GAME),
	CAN_NOT_MOVE_ANYMORE(71, CannotMoveAnymore::new, ConnectionState.IN_GAME),
	TARGET_UNSELECTED(72, RequestTargetCanceld::new, ConnectionState.IN_GAME),
	SAY2(73, Say2::new, ConnectionState.IN_GAME),
	MOVE_TOWARD(74, MoveToward::new, ConnectionState.IN_GAME),
	NOT_USE_4(75, null, ConnectionState.IN_GAME),
	NOT_USE_5(76, null, ConnectionState.IN_GAME),
	PLEDGE_REQ_SHOW_MEMBER_LIST_OPEN(77, RequestPledgeMemberList::new, ConnectionState.IN_GAME),
	NOT_USE_6(78, null, ConnectionState.IN_GAME),
	MAGIC_LIST(79, null, ConnectionState.IN_GAME),
	SKILL_LIST(80, RequestSkillList::new, ConnectionState.IN_GAME),
	MOVE_WITH_DELTA(82, MoveWithDelta::new, ConnectionState.IN_GAME),
	GETON_VEHICLE(83, RequestGetOnVehicle::new, ConnectionState.IN_GAME),
	GETOFF_VEHICLE(84, RequestGetOffVehicle::new, ConnectionState.IN_GAME),
	TRADE_START(85, AnswerTradeRequest::new, ConnectionState.IN_GAME),
	ICON_ACTION(86, RequestActionUse::new, ConnectionState.IN_GAME),
	RESTART(87, RequestRestart::new, ConnectionState.IN_GAME),
	NOT_USE_9(88, null, ConnectionState.IN_GAME),
	VALIDATE_POSITION(89, ValidatePosition::new, ConnectionState.IN_GAME),
	START_ROTATING(91, StartRotating::new, ConnectionState.IN_GAME),
	FINISH_ROTATING(92, FinishRotating::new, ConnectionState.IN_GAME),
	NOT_USE_15(93, null, ConnectionState.IN_GAME),
	SHOW_BOARD(94, RequestShowBoard::new, ConnectionState.IN_GAME),
	REQUEST_ENCHANT_ITEM(95, RequestEnchantItem::new, ConnectionState.IN_GAME),
	DESTROY_ITEM(96, RequestDestroyItem::new, ConnectionState.IN_GAME),
	TARGET_USER_FROM_MENU(97, null, ConnectionState.IN_GAME),
	QUESTLIST(98, RequestQuestList::new, ConnectionState.IN_GAME),
	DESTROY_QUEST(99, RequestQuestAbort::new, ConnectionState.IN_GAME),
	NOT_USE_16(100, null, ConnectionState.IN_GAME),
	PLEDGE_INFO(101, RequestPledgeInfo::new, ConnectionState.IN_GAME),
	PLEDGE_EXTENDED_INFO(102, RequestPledgeExtendedInfo::new, ConnectionState.IN_GAME),
	PLEDGE_CREST(103, RequestPledgeCrest::new, ConnectionState.IN_GAME),
	NOT_USE_17(104, null, ConnectionState.IN_GAME),
	NOT_USE_18(105, null, ConnectionState.IN_GAME),
	L2_FRIEND_LIST(106, null, ConnectionState.IN_GAME),
	L2_FRIEND_SAY(107, RequestSendFriendMsg::new, ConnectionState.IN_GAME),
	OPEN_MINIMAP(108, RequestShowMiniMap::new, ConnectionState.IN_GAME),
	MSN_CHAT_LOG(109, null, ConnectionState.IN_GAME),
	RELOAD(110, null, ConnectionState.IN_GAME),
	HENNA_EQUIP(111, null, ConnectionState.IN_GAME),
	HENNA_UNEQUIP_LIST(112, RequestHennaRemoveList::new, ConnectionState.IN_GAME),
	HENNA_UNEQUIP_INFO(113, RequestHennaItemRemoveInfo::new, ConnectionState.IN_GAME),
	HENNA_UNEQUIP(114, null, ConnectionState.IN_GAME),
	ACQUIRE_SKILL_INFO(115, RequestAcquireSkillInfo::new, ConnectionState.IN_GAME),
	SYS_CMD_2(116, SendBypassBuildCmd::new, ConnectionState.IN_GAME),
	MOVE_TO_LOCATION_IN_VEHICLE(117, RequestMoveToLocationInVehicle::new, ConnectionState.IN_GAME),
	CAN_NOT_MOVE_ANYMORE_IN_VEHICLE(118, CannotMoveAnymoreInVehicle::new, ConnectionState.IN_GAME),
	FRIEND_ADD_REQUEST(119, RequestFriendInvite::new, ConnectionState.IN_GAME),
	FRIEND_ADD_REPLY(120, RequestAnswerFriendInvite::new, ConnectionState.IN_GAME),
	FRIEND_LIST(121, RequestFriendList::new, ConnectionState.IN_GAME),
	FRIEND_REMOVE(122, RequestFriendDel::new, ConnectionState.IN_GAME),
	RESTORE_CHARACTER(123, CharacterRestore::new, ConnectionState.AUTHENTICATED),
	REQ_ACQUIRE_SKILL(124, RequestAcquireSkill::new, ConnectionState.IN_GAME),
	RESTART_POINT(125, RequestRestartPoint::new, ConnectionState.IN_GAME),
	GM_COMMAND_TYPE(126, RequestGMCommand::new, ConnectionState.IN_GAME),
	LIST_PARTY_WAITING(127, RequestPartyMatchConfig::new, ConnectionState.IN_GAME),
	MANAGE_PARTY_ROOM(128, RequestPartyMatchList::new, ConnectionState.IN_GAME),
	JOIN_PARTY_ROOM(129, RequestPartyMatchDetail::new, ConnectionState.IN_GAME),
	NOT_USE_20(130, null, ConnectionState.IN_GAME),
	PRIVATE_STORE_BUY_LIST_SEND(131, RequestPrivateStoreBuy::new, ConnectionState.IN_GAME),
	NOT_USE_21(132, null, ConnectionState.IN_GAME),
	TUTORIAL_LINK_HTML(133, RequestTutorialLinkHtml::new, ConnectionState.IN_GAME),
	TUTORIAL_PASS_CMD_TO_SERVER(134, RequestTutorialPassCmdToServer::new, ConnectionState.IN_GAME),
	TUTORIAL_MARK_PRESSED(135, RequestTutorialQuestionMark::new, ConnectionState.IN_GAME),
	TUTORIAL_CLIENT_EVENT(136, RequestTutorialClientEvent::new, ConnectionState.IN_GAME),
	PETITION(137, RequestPetition::new, ConnectionState.IN_GAME),
	PETITION_CANCEL(138, RequestPetitionCancel::new, ConnectionState.IN_GAME),
	GMLIST(139, RequestGmList::new, ConnectionState.IN_GAME),
	JOIN_ALLIANCE(140, RequestJoinAlly::new, ConnectionState.IN_GAME),
	ANSWER_JOIN_ALLIANCE(141, RequestAnswerJoinAlly::new, ConnectionState.IN_GAME),
	WITHDRAW_ALLIANCE(142, AllyLeave::new, ConnectionState.IN_GAME),
	OUST_ALLIANCE_MEMBER_PLEDGE(143, AllyDismiss::new, ConnectionState.IN_GAME),
	DISMISS_ALLIANCE(144, RequestDismissAlly::new, ConnectionState.IN_GAME),
	SET_ALLIANCE_CREST(145, RequestSetAllyCrest::new, ConnectionState.IN_GAME),
	ALLIANCE_CREST(146, RequestAllyCrest::new, ConnectionState.IN_GAME),
	CHANGE_PET_NAME(147, RequestChangePetName::new, ConnectionState.IN_GAME),
	PET_USE_ITEM(148, RequestPetUseItem::new, ConnectionState.IN_GAME),
	GIVE_ITEM_TO_PET(149, RequestGiveItemToPet::new, ConnectionState.IN_GAME),
	PRIVATE_STORE_QUIT(150, RequestPrivateStoreQuitSell::new, ConnectionState.IN_GAME),
	PRIVATE_STORE_SET_MSG(151, SetPrivateStoreMsgSell::new, ConnectionState.IN_GAME),
	PET_GET_ITEM(152, RequestPetGetItem::new, ConnectionState.IN_GAME),
	PRIVATE_STORE_MANAGE_BUY(153, RequestPrivateStoreManageBuy::new, ConnectionState.IN_GAME),
	PRIVATE_STORE_BUY_LIST_SET(154, SetPrivateStoreListBuy::new, ConnectionState.IN_GAME),
	PRIVATE_STORE_BUY_MANAGE_CANCEL(155, null, ConnectionState.IN_GAME),
	PRIVATE_STORE_BUY_QUIT(156, RequestPrivateStoreQuitBuy::new, ConnectionState.IN_GAME),
	PRIVATE_STORE_BUY_SET_MSG(157, SetPrivateStoreMsgBuy::new, ConnectionState.IN_GAME),
	NOT_USE_24(174, null, ConnectionState.IN_GAME),
	PRIVATE_STORE_BUY_BUY_LIST_SEND(159, RequestPrivateStoreSell::new, ConnectionState.IN_GAME),
	SEND_TIME_CHECK_PACKET(160, null, ConnectionState.IN_GAME),
	NOT_USE_26(161, null, ConnectionState.IN_GAME),
	NOT_USE_27(162, null, ConnectionState.IN_GAME),
	NOT_USE_28(163, null, ConnectionState.IN_GAME),
	NOT_USE_29(164, null, ConnectionState.IN_GAME),
	NOT_USE_30(165, null, ConnectionState.IN_GAME),
	REQUEST_SKILL_COOL_TIME(166, RequestSkillCoolTime::new, ConnectionState.IN_GAME),
	REQUEST_PACKAGE_SENDABLE_ITEM_LIST(167, RequestPackageSendableItemList::new, ConnectionState.IN_GAME),
	REQUEST_PACKAGE_SEND(168, RequestPackageSend::new, ConnectionState.IN_GAME),
	BLOCK_PACKET(169, RequestBlock::new, ConnectionState.IN_GAME),
	CASTLE_SIEGE_INFO(170, RequestSiegeInfo::new, ConnectionState.IN_GAME),
	CASTLE_SIEGE_ATTACKER_LIST(171, RequestSiegeAttackerList::new, ConnectionState.IN_GAME),
	CASTLE_SIEGE_DEFENDER_LIST(172, RequestSiegeDefenderList::new, ConnectionState.IN_GAME),
	JOIN_CASTLE_SIEGE(173, RequestJoinSiege::new, ConnectionState.IN_GAME),
	CONFIRM_CASTLE_SIEGE_WAITING_LIST(174, RequestConfirmSiegeWaitingList::new, ConnectionState.IN_GAME),
	SET_CASTLE_SIEGE_TIME(175, RequestSetCastleSiegeTime::new, ConnectionState.IN_GAME),
	MULTI_SELL_CHOOSE(176, MultiSellChoose::new, ConnectionState.IN_GAME),
	NET_PING(177, RequestNetPing::new, ConnectionState.IN_GAME),
	REMAIN_TIME(178, null, ConnectionState.IN_GAME),
	USER_CMD_BYPASS(179, BypassUserCmd::new, ConnectionState.IN_GAME),
	SNOOP_QUIT(180, SnoopQuit::new, ConnectionState.IN_GAME),
	RECIPE_BOOK_OPEN(181, RequestRecipeBookOpen::new, ConnectionState.IN_GAME),
	RECIPE_ITEM_DELETE(182, RequestRecipeBookDestroy::new, ConnectionState.IN_GAME),
	RECIPE_ITEM_MAKE_INFO(183, RequestRecipeItemMakeInfo::new, ConnectionState.IN_GAME),
	RECIPE_ITEM_MAKE_SELF(184, RequestRecipeItemMakeSelf::new, ConnectionState.IN_GAME),
	RECIPE_SHOP_MANAGE_LIST(185, null, ConnectionState.IN_GAME),
	RECIPE_SHOP_MESSAGE_SET(186, RequestRecipeShopMessageSet::new, ConnectionState.IN_GAME),
	RECIPE_SHOP_LIST_SET(187, RequestRecipeShopListSet::new, ConnectionState.IN_GAME),
	RECIPE_SHOP_MANAGE_QUIT(188, RequestRecipeShopManageQuit::new, ConnectionState.IN_GAME),
	RECIPE_SHOP_MANAGE_CANCEL(189, null, ConnectionState.IN_GAME),
	RECIPE_SHOP_MAKE_INFO(190, RequestRecipeShopMakeInfo::new, ConnectionState.IN_GAME),
	RECIPE_SHOP_MAKE_DO(191, RequestRecipeShopMakeItem::new, ConnectionState.IN_GAME),
	RECIPE_SHOP_SELL_LIST(192, RequestRecipeShopManagePrev::new, ConnectionState.IN_GAME),
	OBSERVER_END(193, ObserverReturn::new, ConnectionState.IN_GAME),
	VOTE_SOCIALITY(194, null, ConnectionState.IN_GAME),
	HENNA_ITEM_LIST(195, RequestHennaItemList::new, ConnectionState.IN_GAME),
	HENNA_ITEM_INFO(196, RequestHennaItemInfo::new, ConnectionState.IN_GAME),
	BUY_SEED(197, RequestBuySeed::new, ConnectionState.IN_GAME),
	CONFIRM_DLG(198, DlgAnswer::new, ConnectionState.IN_GAME),
	BUY_PREVIEW_LIST(199, RequestPreviewItem::new, ConnectionState.IN_GAME),
	SSQ_STATUS(200, null, ConnectionState.IN_GAME),
	PETITION_VOTE(201, RequestPetitionFeedback::new, ConnectionState.IN_GAME),
	NOT_USE_33(202, null, ConnectionState.IN_GAME),
	GAMEGUARD_REPLY(203, GameGuardReply::new, ConnectionState.IN_GAME),
	MANAGE_PLEDGE_POWER(204, RequestPledgePower::new, ConnectionState.IN_GAME),
	MAKE_MACRO(205, RequestMakeMacro::new, ConnectionState.IN_GAME),
	DELETE_MACRO(206, RequestDeleteMacro::new, ConnectionState.IN_GAME),
	BUY_PROCURE(207, null, ConnectionState.IN_GAME),
	EX_PACKET(208, null, ConnectionState.values());

	public static final ClientPackets[] PACKET_ARRAY;
	private final int _packetId;
	private final Supplier<ClientPacket> _packetSupplier;
	private final Set<ConnectionState> _connectionStates;

	private ClientPackets(int packetId, Supplier<ClientPacket> packetSupplier, ConnectionState... connectionStates)
	{
		if (packetId > 255)
		{
			throw new IllegalArgumentException("Packet id must not be bigger than 0xFF");
		}
		this._packetId = packetId;
		this._packetSupplier = packetSupplier != null ? packetSupplier : () -> null;
		this._connectionStates = new HashSet<>(Arrays.asList(connectionStates));
	}

	public int getPacketId()
	{
		return this._packetId;
	}

	public ClientPacket newPacket()
	{
		ClientPacket packet = this._packetSupplier.get();
		if (DevelopmentConfig.DEBUG_CLIENT_PACKETS)
		{
			if (packet != null)
			{
				String name = packet.getClass().getSimpleName();
				if (!DevelopmentConfig.EXCLUDED_DEBUG_PACKETS.contains(name))
				{
					PacketLogger.info("[C] " + name);
				}
			}
			else if (DevelopmentConfig.DEBUG_UNKNOWN_PACKETS)
			{
				PacketLogger.info("[C] 0x" + Integer.toHexString(this._packetId).toUpperCase());
			}
		}

		return packet;
	}

	public Set<ConnectionState> getConnectionStates()
	{
		return this._connectionStates;
	}

	static
	{
		int maxPacketId = Arrays.stream(values()).mapToInt(ClientPackets::getPacketId).max().orElse(0);
		PACKET_ARRAY = new ClientPackets[maxPacketId + 1];

		for (ClientPackets packet : values())
		{
			PACKET_ARRAY[packet.getPacketId()] = packet;
		}
	}
}
