package net.sf.l2jdev.gameserver.model.events;

import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;
import net.sf.l2jdev.gameserver.model.events.holders.OnDailyReset;
import net.sf.l2jdev.gameserver.model.events.holders.OnDayNightChange;
import net.sf.l2jdev.gameserver.model.events.holders.OnServerStart;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureAttack;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureAttackAvoid;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureAttacked;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureDamageDealt;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureDamageReceived;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureDeath;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureHpChange;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureKilled;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureSee;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureSkillFinishCast;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureSkillUse;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureTeleport;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureTeleported;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureZoneEnter;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureZoneExit;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnElementalSpiritUpgrade;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnAttackableAggroRangeEnter;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnAttackableAttack;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnAttackableFactionCall;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnAttackableHate;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnAttackableKill;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcCanBeSeen;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcDespawn;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcEventReceived;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcFirstTalk;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcManorBypass;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcMenuSelect;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcMoveFinished;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcMoveNodeArrived;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcMoveRouteFinished;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcSkillFinished;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcSkillSee;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcSpawn;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcTeleport;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcTeleportRequest;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnElementalSpiritLearn;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayableExpChanged;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerAbilityPointsChanged;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerAugment;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerBypass;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerCallToChangeClass;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerChangeToAwakenedClass;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerChat;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerCheatDeath;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerClanCreate;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerClanDestroy;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerClanJoin;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerClanLeaderChange;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerClanLeft;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerClanLvlUp;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerClanWHItemAdd;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerClanWHItemDestroy;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerClanWHItemTransfer;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerCreate;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerDelete;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerDlgAnswer;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerFameChanged;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerFishing;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerHennaAdd;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerHennaEnchant;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerHennaRemove;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerItemAdd;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerItemDestroy;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerItemDrop;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerItemEquip;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerItemPickup;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerItemTransfer;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerItemUnequip;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerLevelChanged;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerLoad;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerLogin;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerLogout;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerMenteeAdd;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerMenteeLeft;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerMenteeRemove;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerMenteeStatus;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerMentorStatus;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerMoveRequest;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerPKChanged;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerPressTutorialMark;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerProfessionCancel;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerProfessionChange;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerPvPChanged;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerPvPKill;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerQuestAbort;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerQuestAccept;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerQuestComplete;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerReputationChanged;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerRestore;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerSelect;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerSkillLearn;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerSocialAction;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerSubChange;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerSummonAgathion;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerSummonSpawn;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerSummonTalk;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerTakeHero;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerTransform;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerUnsummonAgathion;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnTrapAction;
import net.sf.l2jdev.gameserver.model.events.holders.clan.OnClanWarFinish;
import net.sf.l2jdev.gameserver.model.events.holders.clan.OnClanWarStart;
import net.sf.l2jdev.gameserver.model.events.holders.instance.OnInstanceCreated;
import net.sf.l2jdev.gameserver.model.events.holders.instance.OnInstanceDestroy;
import net.sf.l2jdev.gameserver.model.events.holders.instance.OnInstanceEnter;
import net.sf.l2jdev.gameserver.model.events.holders.instance.OnInstanceLeave;
import net.sf.l2jdev.gameserver.model.events.holders.instance.OnInstanceStatusChange;
import net.sf.l2jdev.gameserver.model.events.holders.item.OnItemBypassEvent;
import net.sf.l2jdev.gameserver.model.events.holders.item.OnItemCreate;
import net.sf.l2jdev.gameserver.model.events.holders.item.OnItemPurgeReward;
import net.sf.l2jdev.gameserver.model.events.holders.item.OnItemTalk;
import net.sf.l2jdev.gameserver.model.events.holders.item.OnItemUse;
import net.sf.l2jdev.gameserver.model.events.holders.item.OnMultisellBuyItem;
import net.sf.l2jdev.gameserver.model.events.holders.olympiad.OnOlympiadMatchResult;
import net.sf.l2jdev.gameserver.model.events.holders.sieges.OnCastleSiegeFinish;
import net.sf.l2jdev.gameserver.model.events.holders.sieges.OnCastleSiegeOwnerChange;
import net.sf.l2jdev.gameserver.model.events.holders.sieges.OnCastleSiegeStart;
import net.sf.l2jdev.gameserver.model.events.holders.sieges.OnFortSiegeFinish;
import net.sf.l2jdev.gameserver.model.events.holders.sieges.OnFortSiegeStart;
import net.sf.l2jdev.gameserver.model.events.returns.ChatFilterReturn;
import net.sf.l2jdev.gameserver.model.events.returns.DamageReturn;
import net.sf.l2jdev.gameserver.model.events.returns.LocationReturn;
import net.sf.l2jdev.gameserver.model.events.returns.TerminateReturn;
import net.sf.l2jdev.gameserver.util.ArrayUtil;

public enum EventType
{
	ON_ATTACKABLE_AGGRO_RANGE_ENTER(OnAttackableAggroRangeEnter.class, void.class),
	ON_ATTACKABLE_ATTACK(OnAttackableAttack.class, void.class),
	ON_ATTACKABLE_FACTION_CALL(OnAttackableFactionCall.class, void.class),
	ON_ATTACKABLE_KILL(OnAttackableKill.class, void.class),
	ON_CASTLE_SIEGE_FINISH(OnCastleSiegeFinish.class, void.class),
	ON_CASTLE_SIEGE_OWNER_CHANGE(OnCastleSiegeOwnerChange.class, void.class),
	ON_CASTLE_SIEGE_START(OnCastleSiegeStart.class, void.class),
	ON_CLAN_WAR_FINISH(OnClanWarFinish.class, void.class),
	ON_CLAN_WAR_START(OnClanWarStart.class, void.class),
	ON_CREATURE_ATTACK(OnCreatureAttack.class, void.class, TerminateReturn.class),
	ON_CREATURE_ATTACK_AVOID(OnCreatureAttackAvoid.class, void.class, void.class),
	ON_CREATURE_ATTACKED(OnCreatureAttacked.class, void.class, TerminateReturn.class),
	ON_CREATURE_DAMAGE_RECEIVED(OnCreatureDamageReceived.class, void.class, DamageReturn.class),
	ON_CREATURE_DAMAGE_DEALT(OnCreatureDamageDealt.class, void.class),
	ON_CREATURE_HP_CHANGE(OnCreatureHpChange.class, void.class),
	ON_CREATURE_DEATH(OnCreatureDeath.class, void.class),
	ON_CREATURE_KILLED(OnCreatureKilled.class, void.class, TerminateReturn.class),
	ON_CREATURE_SEE(OnCreatureSee.class, void.class),
	ON_CREATURE_SKILL_USE(OnCreatureSkillUse.class, void.class, TerminateReturn.class),
	ON_CREATURE_SKILL_FINISH_CAST(OnCreatureSkillFinishCast.class, void.class),
	ON_CREATURE_TELEPORT(OnCreatureTeleport.class, void.class, LocationReturn.class),
	ON_CREATURE_TELEPORTED(OnCreatureTeleported.class, void.class),
	ON_CREATURE_ZONE_ENTER(OnCreatureZoneEnter.class, void.class),
	ON_CREATURE_ZONE_EXIT(OnCreatureZoneExit.class, void.class),
	ON_FORT_SIEGE_FINISH(OnFortSiegeFinish.class, void.class),
	ON_FORT_SIEGE_START(OnFortSiegeStart.class, void.class),
	ON_ITEM_BYPASS_EVENT(OnItemBypassEvent.class, void.class),
	ON_ITEM_CREATE(OnItemCreate.class, void.class),
	ON_ITEM_USE(OnItemUse.class, void.class),
	ON_ITEM_TALK(OnItemTalk.class, void.class),
	ON_ITEM_PURGE_REWARD(OnItemPurgeReward.class, void.class),
	ON_MULTISELL_BUY_ITEM(OnMultisellBuyItem.class, void.class),
	ON_NPC_CAN_BE_SEEN(OnNpcCanBeSeen.class, void.class, TerminateReturn.class),
	ON_NPC_EVENT_RECEIVED(OnNpcEventReceived.class, void.class),
	ON_NPC_FIRST_TALK(OnNpcFirstTalk.class, void.class),
	ON_NPC_HATE(OnAttackableHate.class, void.class, TerminateReturn.class),
	ON_NPC_MOVE_FINISHED(OnNpcMoveFinished.class, void.class),
	ON_NPC_MOVE_NODE_ARRIVED(OnNpcMoveNodeArrived.class, void.class),
	ON_NPC_MOVE_ROUTE_FINISHED(OnNpcMoveRouteFinished.class, void.class),
	ON_NPC_QUEST_START(null, void.class),
	ON_NPC_SKILL_FINISHED(OnNpcSkillFinished.class, void.class),
	ON_NPC_SKILL_SEE(OnNpcSkillSee.class, void.class),
	ON_NPC_SPAWN(OnNpcSpawn.class, void.class),
	ON_NPC_TALK(null, void.class),
	ON_NPC_TELEPORT(OnNpcTeleport.class, void.class),
	ON_NPC_MANOR_BYPASS(OnNpcManorBypass.class, void.class),
	ON_NPC_MENU_SELECT(OnNpcMenuSelect.class, void.class),
	ON_NPC_DESPAWN(OnNpcDespawn.class, void.class),
	ON_NPC_TELEPORT_REQUEST(OnNpcTeleportRequest.class, void.class, TerminateReturn.class),
	ON_OLYMPIAD_MATCH_RESULT(OnOlympiadMatchResult.class, void.class),
	ON_PLAYABLE_EXP_CHANGED(OnPlayableExpChanged.class, void.class, TerminateReturn.class),
	ON_PLAYER_AUGMENT(OnPlayerAugment.class, void.class),
	ON_PLAYER_BYPASS(OnPlayerBypass.class, void.class, TerminateReturn.class),
	ON_PLAYER_CALL_TO_CHANGE_CLASS(OnPlayerCallToChangeClass.class, void.class),
	ON_PLAYER_CHAT(OnPlayerChat.class, void.class, ChatFilterReturn.class),
	ON_PLAYER_ABILITY_POINTS_CHANGED(OnPlayerAbilityPointsChanged.class, void.class),
	ON_PLAYER_CLAN_CREATE(OnPlayerClanCreate.class, void.class),
	ON_PLAYER_CLAN_DESTROY(OnPlayerClanDestroy.class, void.class),
	ON_PLAYER_CLAN_JOIN(OnPlayerClanJoin.class, void.class),
	ON_PLAYER_CLAN_LEADER_CHANGE(OnPlayerClanLeaderChange.class, void.class),
	ON_PLAYER_CLAN_LEFT(OnPlayerClanLeft.class, void.class),
	ON_PLAYER_CLAN_LEVELUP(OnPlayerClanLvlUp.class, void.class),
	ON_PLAYER_CLAN_WH_ITEM_ADD(OnPlayerClanWHItemAdd.class, void.class),
	ON_PLAYER_CLAN_WH_ITEM_DESTROY(OnPlayerClanWHItemDestroy.class, void.class),
	ON_PLAYER_CLAN_WH_ITEM_TRANSFER(OnPlayerClanWHItemTransfer.class, void.class),
	ON_PLAYER_CREATE(OnPlayerCreate.class, void.class),
	ON_PLAYER_DELETE(OnPlayerDelete.class, void.class),
	ON_PLAYER_DLG_ANSWER(OnPlayerDlgAnswer.class, void.class, TerminateReturn.class),
	ON_PLAYER_FAME_CHANGED(OnPlayerFameChanged.class, void.class),
	ON_PLAYER_FISHING(OnPlayerFishing.class, void.class),
	ON_PLAYER_HENNA_ADD(OnPlayerHennaAdd.class, void.class),
	ON_PLAYER_HENNA_REMOVE(OnPlayerHennaRemove.class, void.class),
	ON_PLAYER_HENNA_ENCHANT(OnPlayerHennaEnchant.class, void.class),
	ON_PLAYER_ITEM_ADD(OnPlayerItemAdd.class, void.class),
	ON_PLAYER_ITEM_DESTROY(OnPlayerItemDestroy.class, void.class),
	ON_PLAYER_ITEM_DROP(OnPlayerItemDrop.class, void.class),
	ON_PLAYER_ITEM_PICKUP(OnPlayerItemPickup.class, void.class),
	ON_PLAYER_ITEM_TRANSFER(OnPlayerItemTransfer.class, void.class),
	ON_PLAYER_ITEM_EQUIP(OnPlayerItemEquip.class, void.class),
	ON_PLAYER_ITEM_UNEQUIP(OnPlayerItemUnequip.class, void.class),
	ON_PLAYER_MENTEE_ADD(OnPlayerMenteeAdd.class, void.class),
	ON_PLAYER_MENTEE_LEFT(OnPlayerMenteeLeft.class, void.class),
	ON_PLAYER_MENTEE_REMOVE(OnPlayerMenteeRemove.class, void.class),
	ON_PLAYER_MENTEE_STATUS(OnPlayerMenteeStatus.class, void.class),
	ON_PLAYER_MENTOR_STATUS(OnPlayerMentorStatus.class, void.class),
	ON_PLAYER_REPUTATION_CHANGED(OnPlayerReputationChanged.class, void.class),
	ON_PLAYER_LEVEL_CHANGED(OnPlayerLevelChanged.class, void.class),
	ON_PLAYER_LOGIN(OnPlayerLogin.class, void.class),
	ON_PLAYER_LOGOUT(OnPlayerLogout.class, void.class),
	ON_PLAYER_LOAD(OnPlayerLoad.class, void.class),
	ON_PLAYER_PK_CHANGED(OnPlayerPKChanged.class, void.class),
	ON_PLAYER_PRESS_TUTORIAL_MARK(OnPlayerPressTutorialMark.class, void.class),
	ON_PLAYER_MOVE_REQUEST(OnPlayerMoveRequest.class, void.class, TerminateReturn.class),
	ON_PLAYER_PROFESSION_CHANGE(OnPlayerProfessionChange.class, void.class),
	ON_PLAYER_PROFESSION_CANCEL(OnPlayerProfessionCancel.class, void.class),
	ON_PLAYER_CHANGE_TO_AWAKENED_CLASS(OnPlayerChangeToAwakenedClass.class, void.class),
	ON_PLAYER_PVP_CHANGED(OnPlayerPvPChanged.class, void.class),
	ON_PLAYER_PVP_KILL(OnPlayerPvPKill.class, void.class),
	ON_PLAYER_CHEAT_DEATH(OnPlayerCheatDeath.class, void.class),
	ON_PLAYER_RESTORE(OnPlayerRestore.class, void.class),
	ON_PLAYER_SELECT(OnPlayerSelect.class, void.class, TerminateReturn.class),
	ON_PLAYER_SOCIAL_ACTION(OnPlayerSocialAction.class, void.class),
	ON_PLAYER_SKILL_LEARN(OnPlayerSkillLearn.class, void.class),
	ON_PLAYER_SUMMON_SPAWN(OnPlayerSummonSpawn.class, void.class),
	ON_PLAYER_SUMMON_TALK(OnPlayerSummonTalk.class, void.class),
	ON_PLAYER_TAKE_HERO(OnPlayerTakeHero.class, void.class),
	ON_PLAYER_TRANSFORM(OnPlayerTransform.class, void.class),
	ON_PLAYER_SUB_CHANGE(OnPlayerSubChange.class, void.class),
	ON_PLAYER_QUEST_ACCEPT(OnPlayerQuestAccept.class, void.class),
	ON_PLAYER_QUEST_ABORT(OnPlayerQuestAbort.class, void.class),
	ON_PLAYER_QUEST_COMPLETE(OnPlayerQuestComplete.class, void.class),
	ON_PLAYER_SUMMON_AGATHION(OnPlayerSummonAgathion.class, void.class),
	ON_PLAYER_UNSUMMON_AGATHION(OnPlayerUnsummonAgathion.class, void.class),
	ON_TRAP_ACTION(OnTrapAction.class, void.class),
	ON_ELEMENTAL_SPIRIT_UPGRADE(OnElementalSpiritUpgrade.class, void.class),
	ON_ELEMENTAL_SPIRIT_LEARN(OnElementalSpiritLearn.class, void.class),
	ON_INSTANCE_CREATED(OnInstanceCreated.class, void.class),
	ON_INSTANCE_DESTROY(OnInstanceDestroy.class, void.class),
	ON_INSTANCE_ENTER(OnInstanceEnter.class, void.class),
	ON_INSTANCE_LEAVE(OnInstanceLeave.class, void.class),
	ON_INSTANCE_STATUS_CHANGE(OnInstanceStatusChange.class, void.class),
	ON_SERVER_START(OnServerStart.class, void.class),
	ON_DAY_NIGHT_CHANGE(OnDayNightChange.class, void.class),
	ON_DAILY_RESET(OnDailyReset.class, void.class);

	private final Class<? extends IBaseEvent> _eventClass;
	private final Class<?>[] _returnClass;

	private EventType(Class<? extends IBaseEvent> eventClass, Class<?>... returnClasss)
	{
		this._eventClass = eventClass;
		this._returnClass = returnClasss;
	}

	public Class<? extends IBaseEvent> getEventClass()
	{
		return this._eventClass;
	}

	public Class<?>[] getReturnClasses()
	{
		return this._returnClass;
	}

	public boolean isEventClass(Class<?> clazz)
	{
		return this._eventClass == clazz;
	}

	public boolean isReturnClass(Class<?> clazz)
	{
		return ArrayUtil.contains(this._returnClass, clazz);
	}
}
