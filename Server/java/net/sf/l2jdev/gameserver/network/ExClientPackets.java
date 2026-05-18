package net.sf.l2jdev.gameserver.network;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import net.sf.l2jdev.gameserver.config.DevelopmentConfig;
import net.sf.l2jdev.gameserver.network.clientpackets.*;
import net.sf.l2jdev.gameserver.network.clientpackets.adenadistribution.RequestDivideAdena;
import net.sf.l2jdev.gameserver.network.clientpackets.adenadistribution.RequestDivideAdenaCancel;
import net.sf.l2jdev.gameserver.network.clientpackets.adenadistribution.RequestDivideAdenaStart;
import net.sf.l2jdev.gameserver.network.clientpackets.adenlab.RequestAdenLabBossInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.adenlab.RequestAdenLabBossList;
import net.sf.l2jdev.gameserver.network.clientpackets.adenlab.RequestAdenLabBossUnlock;
import net.sf.l2jdev.gameserver.network.clientpackets.adenlab.RequestAdenLabNormalPlay;
import net.sf.l2jdev.gameserver.network.clientpackets.adenlab.RequestAdenLabNormalSlot;
import net.sf.l2jdev.gameserver.network.clientpackets.adenlab.RequestAdenLabSpecialFix;
import net.sf.l2jdev.gameserver.network.clientpackets.adenlab.RequestAdenLabSpecialPlay;
import net.sf.l2jdev.gameserver.network.clientpackets.adenlab.RequestAdenLabSpecialProbability;
import net.sf.l2jdev.gameserver.network.clientpackets.adenlab.RequestAdenLabSpecialSlot;
import net.sf.l2jdev.gameserver.network.clientpackets.adenlab.RequestAdenLabTranscendentEnchant;
import net.sf.l2jdev.gameserver.network.clientpackets.adenlab.RequestAdenLabTranscendentProbability;
import net.sf.l2jdev.gameserver.network.clientpackets.appearance.RequestExCancelShape_Shifting_Item;
import net.sf.l2jdev.gameserver.network.clientpackets.appearance.RequestExTryToPutShapeShiftingEnchantSupportItem;
import net.sf.l2jdev.gameserver.network.clientpackets.appearance.RequestExTryToPutShapeShiftingTargetItem;
import net.sf.l2jdev.gameserver.network.clientpackets.appearance.RequestShapeShiftingItem;
import net.sf.l2jdev.gameserver.network.clientpackets.attendance.RequestVipAttendanceCheck;
import net.sf.l2jdev.gameserver.network.clientpackets.attendance.RequestVipAttendanceItemList;
import net.sf.l2jdev.gameserver.network.clientpackets.attendance.RequestVipAttendanceItemReward;
import net.sf.l2jdev.gameserver.network.clientpackets.attributechange.RequestChangeAttributeCancel;
import net.sf.l2jdev.gameserver.network.clientpackets.attributechange.RequestChangeAttributeItem;
import net.sf.l2jdev.gameserver.network.clientpackets.attributechange.SendChangeAttributeTargetItem;
import net.sf.l2jdev.gameserver.network.clientpackets.autopeel.ExRequestItemAutoPeel;
import net.sf.l2jdev.gameserver.network.clientpackets.autopeel.ExRequestReadyItemAutoPeel;
import net.sf.l2jdev.gameserver.network.clientpackets.autopeel.ExRequestStopItemAutoPeel;
import net.sf.l2jdev.gameserver.network.clientpackets.autoplay.ExAutoPlaySetting;
import net.sf.l2jdev.gameserver.network.clientpackets.autoplay.ExRequestActivateAutoShortcut;
import net.sf.l2jdev.gameserver.network.clientpackets.balok.ExBalrogWarGetReward;
import net.sf.l2jdev.gameserver.network.clientpackets.balok.ExBalrogWarShowRanking;
import net.sf.l2jdev.gameserver.network.clientpackets.balok.ExBalrogWarShowUI;
import net.sf.l2jdev.gameserver.network.clientpackets.balok.ExBalrogWarTeleport;
import net.sf.l2jdev.gameserver.network.clientpackets.balthusevent.RequestEventBalthusToken;
import net.sf.l2jdev.gameserver.network.clientpackets.blackcoupon.RequestItemRestore;
import net.sf.l2jdev.gameserver.network.clientpackets.blackcoupon.RequestItemRestoreList;
import net.sf.l2jdev.gameserver.network.clientpackets.blessing.RequestBlessOptionCancel;
import net.sf.l2jdev.gameserver.network.clientpackets.blessing.RequestBlessOptionEnchant;
import net.sf.l2jdev.gameserver.network.clientpackets.blessing.RequestBlessOptionProbList;
import net.sf.l2jdev.gameserver.network.clientpackets.blessing.RequestBlessOptionPutItem;
import net.sf.l2jdev.gameserver.network.clientpackets.captcha.RequestCaptchaAnswer;
import net.sf.l2jdev.gameserver.network.clientpackets.captcha.RequestRefreshCaptcha;
import net.sf.l2jdev.gameserver.network.clientpackets.castlewar.ExCastleWarObserverStart;
import net.sf.l2jdev.gameserver.network.clientpackets.castlewar.ExMercenaryCastleWarCastleSiegeAttackerList;
import net.sf.l2jdev.gameserver.network.clientpackets.castlewar.ExMercenaryCastleWarCastleSiegeDefenderList;
import net.sf.l2jdev.gameserver.network.clientpackets.castlewar.ExMercenaryCastleWarCastleSiegeHudInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.castlewar.ExMercenaryCastleWarCastleSiegeInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.castlewar.ExPledgeMercenaryMemberJoin;
import net.sf.l2jdev.gameserver.network.clientpackets.castlewar.ExPledgeMercenaryMemberList;
import net.sf.l2jdev.gameserver.network.clientpackets.castlewar.ExPledgeMercenaryRecruitInfoSet;
import net.sf.l2jdev.gameserver.network.clientpackets.characterstyle.ExRequestCharacterStyleList;
import net.sf.l2jdev.gameserver.network.clientpackets.characterstyle.ExRequestCharacterStyleRegister;
import net.sf.l2jdev.gameserver.network.clientpackets.characterstyle.ExRequestCharacterStyleSelect;
import net.sf.l2jdev.gameserver.network.clientpackets.characterstyle.ExRequestCharacterStyleUpdateFavorite;
import net.sf.l2jdev.gameserver.network.clientpackets.classchange.ExRequestClassChange;
import net.sf.l2jdev.gameserver.network.clientpackets.classchange.ExRequestClassChangeVerifying;
import net.sf.l2jdev.gameserver.network.clientpackets.collection.RequestCollectionCloseUI;
import net.sf.l2jdev.gameserver.network.clientpackets.collection.RequestCollectionFavoriteList;
import net.sf.l2jdev.gameserver.network.clientpackets.collection.RequestCollectionReceiveReward;
import net.sf.l2jdev.gameserver.network.clientpackets.collection.RequestCollectionRegister;
import net.sf.l2jdev.gameserver.network.clientpackets.collection.RequestCollectionUpdateFavorite;
import net.sf.l2jdev.gameserver.network.clientpackets.collection.RequestExCollectionList;
import net.sf.l2jdev.gameserver.network.clientpackets.collection.RequestExCollectionOpenUI;
import net.sf.l2jdev.gameserver.network.clientpackets.collection.RequestExCollectionSummary;
import net.sf.l2jdev.gameserver.network.clientpackets.commission.RequestCommissionBuyInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.commission.RequestCommissionBuyItem;
import net.sf.l2jdev.gameserver.network.clientpackets.commission.RequestCommissionCancel;
import net.sf.l2jdev.gameserver.network.clientpackets.commission.RequestCommissionDelete;
import net.sf.l2jdev.gameserver.network.clientpackets.commission.RequestCommissionInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.commission.RequestCommissionList;
import net.sf.l2jdev.gameserver.network.clientpackets.commission.RequestCommissionRegister;
import net.sf.l2jdev.gameserver.network.clientpackets.commission.RequestCommissionRegisteredItem;
import net.sf.l2jdev.gameserver.network.clientpackets.commission.RequestCommissionRegistrableItemList;
import net.sf.l2jdev.gameserver.network.clientpackets.compound.RequestNewEnchantClose;
import net.sf.l2jdev.gameserver.network.clientpackets.compound.RequestNewEnchantPushOne;
import net.sf.l2jdev.gameserver.network.clientpackets.compound.RequestNewEnchantPushTwo;
import net.sf.l2jdev.gameserver.network.clientpackets.compound.RequestNewEnchantRemoveOne;
import net.sf.l2jdev.gameserver.network.clientpackets.compound.RequestNewEnchantRemoveTwo;
import net.sf.l2jdev.gameserver.network.clientpackets.compound.RequestNewEnchantRetryToPutItems;
import net.sf.l2jdev.gameserver.network.clientpackets.compound.RequestNewEnchantTry;
import net.sf.l2jdev.gameserver.network.clientpackets.crossevent.RequestCrossEventData;
import net.sf.l2jdev.gameserver.network.clientpackets.crossevent.RequestCrossEventInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.crossevent.RequestCrossEventNormalReward;
import net.sf.l2jdev.gameserver.network.clientpackets.crossevent.RequestCrossEventRareReward;
import net.sf.l2jdev.gameserver.network.clientpackets.crossevent.RequestCrossEventReset;
import net.sf.l2jdev.gameserver.network.clientpackets.crystalization.RequestCrystallizeEstimate;
import net.sf.l2jdev.gameserver.network.clientpackets.crystalization.RequestCrystallizeItemCancel;
import net.sf.l2jdev.gameserver.network.clientpackets.dailymission.RequestMissionLevelJumpLevel;
import net.sf.l2jdev.gameserver.network.clientpackets.dailymission.RequestMissionLevelReceiveReward;
import net.sf.l2jdev.gameserver.network.clientpackets.dailymission.RequestMissionRewardList;
import net.sf.l2jdev.gameserver.network.clientpackets.dailymission.RequestOneDayRewardReceive;
import net.sf.l2jdev.gameserver.network.clientpackets.dailymission.RequestTodoList;
import net.sf.l2jdev.gameserver.network.clientpackets.dualinventory.RequestExDualInventorySwap;
import net.sf.l2jdev.gameserver.network.clientpackets.elementalspirits.ExElementalInitTalent;
import net.sf.l2jdev.gameserver.network.clientpackets.elementalspirits.ExElementalSpiritAbsorb;
import net.sf.l2jdev.gameserver.network.clientpackets.elementalspirits.ExElementalSpiritAbsorbInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.elementalspirits.ExElementalSpiritEvolution;
import net.sf.l2jdev.gameserver.network.clientpackets.elementalspirits.ExElementalSpiritEvolutionInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.elementalspirits.ExElementalSpiritExtract;
import net.sf.l2jdev.gameserver.network.clientpackets.elementalspirits.ExElementalSpiritExtractInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.elementalspirits.ExElementalSpiritInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.elementalspirits.ExElementalSpiritSetTalent;
import net.sf.l2jdev.gameserver.network.clientpackets.enchant.RequestExAddEnchantScrollItem;
import net.sf.l2jdev.gameserver.network.clientpackets.enchant.RequestExCancelEnchantItem;
import net.sf.l2jdev.gameserver.network.clientpackets.enchant.RequestExRemoveEnchantSupportItem;
import net.sf.l2jdev.gameserver.network.clientpackets.enchant.RequestExTryToPutEnchantSupportItem;
import net.sf.l2jdev.gameserver.network.clientpackets.enchant.RequestExTryToPutEnchantTargetItem;
import net.sf.l2jdev.gameserver.network.clientpackets.enchant.challengepoint.ExRequestResetEnchantChallengePoint;
import net.sf.l2jdev.gameserver.network.clientpackets.enchant.challengepoint.ExRequestSetEnchantChallengePoint;
import net.sf.l2jdev.gameserver.network.clientpackets.enchant.multi.ExRequestFinishMultiEnchantScroll;
import net.sf.l2jdev.gameserver.network.clientpackets.enchant.multi.ExRequestMultiEnchantItemList;
import net.sf.l2jdev.gameserver.network.clientpackets.enchant.multi.ExRequestSetMultiEnchantItemList;
import net.sf.l2jdev.gameserver.network.clientpackets.enchant.multi.ExRequestStartMultiEnchantScroll;
import net.sf.l2jdev.gameserver.network.clientpackets.enchant.multi.ExRequestViewMultiEnchantResult;
import net.sf.l2jdev.gameserver.network.clientpackets.enchant.single.ExRequestEnchantFailRewardInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.enchant.single.ExRequestViewEnchantResult;
import net.sf.l2jdev.gameserver.network.clientpackets.ensoul.RequestItemEnsoul;
import net.sf.l2jdev.gameserver.network.clientpackets.ensoul.RequestTryEnSoulExtraction;
import net.sf.l2jdev.gameserver.network.clientpackets.equipmentupgrade.RequestUpgradeProb;
import net.sf.l2jdev.gameserver.network.clientpackets.equipmentupgrade.RequestUpgradeSystemProbList;
import net.sf.l2jdev.gameserver.network.clientpackets.equipmentupgrade.RequestUpgradeSystemResult;
import net.sf.l2jdev.gameserver.network.clientpackets.equipmentupgradenormal.ExUpgradeSystemNormalRequest;
import net.sf.l2jdev.gameserver.network.clientpackets.friend.RequestBlockDetailInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.friend.RequestBlockMemo;
import net.sf.l2jdev.gameserver.network.clientpackets.friend.RequestFriendDetailInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.friend.RequestUpdateFriendMemo;
import net.sf.l2jdev.gameserver.network.clientpackets.gacha.ExUniqueGachaGame;
import net.sf.l2jdev.gameserver.network.clientpackets.gacha.ExUniqueGachaHistory;
import net.sf.l2jdev.gameserver.network.clientpackets.gacha.ExUniqueGachaInvenGetItem;
import net.sf.l2jdev.gameserver.network.clientpackets.gacha.ExUniqueGachaInvenItemList;
import net.sf.l2jdev.gameserver.network.clientpackets.gacha.ExUniqueGachaOpen;
import net.sf.l2jdev.gameserver.network.clientpackets.huntingzones.ExTimedHuntingZoneEnter;
import net.sf.l2jdev.gameserver.network.clientpackets.huntingzones.ExTimedHuntingZoneLeave;
import net.sf.l2jdev.gameserver.network.clientpackets.huntingzones.ExTimedHuntingZoneList;
import net.sf.l2jdev.gameserver.network.clientpackets.huntpass.HuntpassSayhasToggle;
import net.sf.l2jdev.gameserver.network.clientpackets.huntpass.RequestHuntPassBuyPremium;
import net.sf.l2jdev.gameserver.network.clientpackets.huntpass.RequestHuntPassInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.huntpass.RequestHuntPassReward;
import net.sf.l2jdev.gameserver.network.clientpackets.huntpass.RequestHuntPassRewardAll;
import net.sf.l2jdev.gameserver.network.clientpackets.limitshop.RequestPurchaseLimitCraftItem;
import net.sf.l2jdev.gameserver.network.clientpackets.limitshop.RequestPurchaseLimitShopItemBuy;
import net.sf.l2jdev.gameserver.network.clientpackets.limitshop.RequestPurchaseLimitShopItemList;
import net.sf.l2jdev.gameserver.network.clientpackets.luckygame.RequestLuckyGamePlay;
import net.sf.l2jdev.gameserver.network.clientpackets.luckygame.RequestLuckyGameStartInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.mablegame.ExRequestMableGameClose;
import net.sf.l2jdev.gameserver.network.clientpackets.mablegame.ExRequestMableGameOpen;
import net.sf.l2jdev.gameserver.network.clientpackets.mablegame.ExRequestMableGamePopupOk;
import net.sf.l2jdev.gameserver.network.clientpackets.mablegame.ExRequestMableGameReset;
import net.sf.l2jdev.gameserver.network.clientpackets.mablegame.ExRequestMableGameRollDice;
import net.sf.l2jdev.gameserver.network.clientpackets.magiclamp.ExMagicLampGameInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.magiclamp.ExMagicLampGameStart;
import net.sf.l2jdev.gameserver.network.clientpackets.mentoring.ConfirmMenteeAdd;
import net.sf.l2jdev.gameserver.network.clientpackets.mentoring.RequestMenteeAdd;
import net.sf.l2jdev.gameserver.network.clientpackets.mentoring.RequestMenteeWaitingList;
import net.sf.l2jdev.gameserver.network.clientpackets.mentoring.RequestMentorCancel;
import net.sf.l2jdev.gameserver.network.clientpackets.mentoring.RequestMentorList;
import net.sf.l2jdev.gameserver.network.clientpackets.newcrest.RequestGetPledgeCrestPreset;
import net.sf.l2jdev.gameserver.network.clientpackets.newcrest.RequestSetPledgeCrestPreset;
import net.sf.l2jdev.gameserver.network.clientpackets.newhenna.ExRequestNewHennaEnchantReset;
import net.sf.l2jdev.gameserver.network.clientpackets.newhenna.RequestNewHennaCompose;
import net.sf.l2jdev.gameserver.network.clientpackets.newhenna.RequestNewHennaEquip;
import net.sf.l2jdev.gameserver.network.clientpackets.newhenna.RequestNewHennaList;
import net.sf.l2jdev.gameserver.network.clientpackets.newhenna.RequestNewHennaPotenEnchant;
import net.sf.l2jdev.gameserver.network.clientpackets.newhenna.RequestNewHennaPotenOpenslot;
import net.sf.l2jdev.gameserver.network.clientpackets.newhenna.RequestNewHennaPotenOpenslotProbInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.newhenna.RequestNewHennaPotenSelect;
import net.sf.l2jdev.gameserver.network.clientpackets.newhenna.RequestNewHennaUnequip;
import net.sf.l2jdev.gameserver.network.clientpackets.newhenna.RequestNewHennaUnequipInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.newskillenchant.RequestExSkillEnchantCharge;
import net.sf.l2jdev.gameserver.network.clientpackets.newskillenchant.RequestExSkillEnchantInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.newskillenchant.RequestExSpExtractInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.newskillenchant.RequestExSpExtractItem;
import net.sf.l2jdev.gameserver.network.clientpackets.olympiad.OlympiadMatchMaking;
import net.sf.l2jdev.gameserver.network.clientpackets.olympiad.OlympiadMatchMakingCancel;
import net.sf.l2jdev.gameserver.network.clientpackets.olympiad.OlympiadUI;
import net.sf.l2jdev.gameserver.network.clientpackets.olympiad.RequestExOlympiadMatchListRefresh;
import net.sf.l2jdev.gameserver.network.clientpackets.olympiad.RequestOlympiadMatchList;
import net.sf.l2jdev.gameserver.network.clientpackets.olympiad.RequestOlympiadObserverEnd;
import net.sf.l2jdev.gameserver.network.clientpackets.payback.ExPaybackGiveReward;
import net.sf.l2jdev.gameserver.network.clientpackets.payback.ExPaybackList;
import net.sf.l2jdev.gameserver.network.clientpackets.penaltyitemdrop.ExRequestPenaltyItemList;
import net.sf.l2jdev.gameserver.network.clientpackets.penaltyitemdrop.ExRequestPenaltyItemRestore;
import net.sf.l2jdev.gameserver.network.clientpackets.pet.ExEvolvePet;
import net.sf.l2jdev.gameserver.network.clientpackets.pet.ExPetEquipItem;
import net.sf.l2jdev.gameserver.network.clientpackets.pet.ExPetUnequipItem;
import net.sf.l2jdev.gameserver.network.clientpackets.pet.ExTryPetExtractSystem;
import net.sf.l2jdev.gameserver.network.clientpackets.pet.RequestExAcquirePetSkill;
import net.sf.l2jdev.gameserver.network.clientpackets.pk.RequestExPkPenaltyList;
import net.sf.l2jdev.gameserver.network.clientpackets.pk.RequestExPkPenaltyListOnlyLoc;
import net.sf.l2jdev.gameserver.network.clientpackets.pledgeV3.RequestExPledgeEnemyDelete;
import net.sf.l2jdev.gameserver.network.clientpackets.pledgeV3.RequestExPledgeEnemyInfoList;
import net.sf.l2jdev.gameserver.network.clientpackets.pledgeV3.RequestExPledgeEnemyRegister;
import net.sf.l2jdev.gameserver.network.clientpackets.pledgeV3.RequestExPledgeV3Info;
import net.sf.l2jdev.gameserver.network.clientpackets.pledgeV3.RequestExPledgeV3SetAnnounce;
import net.sf.l2jdev.gameserver.network.clientpackets.pledgebonus.RequestPledgeBonusOpen;
import net.sf.l2jdev.gameserver.network.clientpackets.pledgebonus.RequestPledgeBonusReward;
import net.sf.l2jdev.gameserver.network.clientpackets.pledgebonus.RequestPledgeBonusRewardList;
import net.sf.l2jdev.gameserver.network.clientpackets.pledgedonation.RequestExPledgeContributionList;
import net.sf.l2jdev.gameserver.network.clientpackets.pledgedonation.RequestExPledgeDonationInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.pledgedonation.RequestExPledgeDonationRequest;
import net.sf.l2jdev.gameserver.network.clientpackets.primeshop.RequestBRBuyProduct;
import net.sf.l2jdev.gameserver.network.clientpackets.primeshop.RequestBRGamePoint;
import net.sf.l2jdev.gameserver.network.clientpackets.primeshop.RequestBRPresentBuyProduct;
import net.sf.l2jdev.gameserver.network.clientpackets.primeshop.RequestBRProductInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.primeshop.RequestBRProductList;
import net.sf.l2jdev.gameserver.network.clientpackets.primeshop.RequestBRRecentProductList;
import net.sf.l2jdev.gameserver.network.clientpackets.prison.RequestPrisonUserDonation;
import net.sf.l2jdev.gameserver.network.clientpackets.prison.RequestPrisonUserInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.quest.RequestExQuestAccept;
import net.sf.l2jdev.gameserver.network.clientpackets.quest.RequestExQuestAcceptableList;
import net.sf.l2jdev.gameserver.network.clientpackets.quest.RequestExQuestCancel;
import net.sf.l2jdev.gameserver.network.clientpackets.quest.RequestExQuestComplete;
import net.sf.l2jdev.gameserver.network.clientpackets.quest.RequestExQuestNotificationAll;
import net.sf.l2jdev.gameserver.network.clientpackets.quest.RequestExQuestTeleport;
import net.sf.l2jdev.gameserver.network.clientpackets.quest.RequestExQuestUI;
import net.sf.l2jdev.gameserver.network.clientpackets.quest.RequestExTeleportUI;
import net.sf.l2jdev.gameserver.network.clientpackets.raidbossinfo.RequestRaidBossSpawnInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.raidbossinfo.RequestRaidServerInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.randomcraft.ExRequestRandomCraftExtract;
import net.sf.l2jdev.gameserver.network.clientpackets.randomcraft.ExRequestRandomCraftInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.randomcraft.ExRequestRandomCraftLockSlot;
import net.sf.l2jdev.gameserver.network.clientpackets.randomcraft.ExRequestRandomCraftMake;
import net.sf.l2jdev.gameserver.network.clientpackets.randomcraft.ExRequestRandomCraftRefresh;
import net.sf.l2jdev.gameserver.network.clientpackets.ranking.RequestExRankingCharBuffzoneNpcPosition;
import net.sf.l2jdev.gameserver.network.clientpackets.ranking.RequestExRankingCharSpawnBuffzoneNpc;
import net.sf.l2jdev.gameserver.network.clientpackets.ranking.RequestOlympiadHeroAndLegendInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.ranking.RequestOlympiadMyRankingInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.ranking.RequestOlympiadRankingInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.ranking.RequestPetRankingList;
import net.sf.l2jdev.gameserver.network.clientpackets.ranking.RequestPetRankingMyInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.ranking.RequestPledgeRankingList;
import net.sf.l2jdev.gameserver.network.clientpackets.ranking.RequestPledgeRankingMyInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.ranking.RequestPvpRankingList;
import net.sf.l2jdev.gameserver.network.clientpackets.ranking.RequestPvpRankingMyInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.ranking.RequestRankingCharHistory;
import net.sf.l2jdev.gameserver.network.clientpackets.ranking.RequestRankingCharInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.ranking.RequestRankingCharRankers;
import net.sf.l2jdev.gameserver.network.clientpackets.relics.RequestRelicsActive;
import net.sf.l2jdev.gameserver.network.clientpackets.relics.RequestRelicsCloseUI;
import net.sf.l2jdev.gameserver.network.clientpackets.relics.RequestRelicsCombination;
import net.sf.l2jdev.gameserver.network.clientpackets.relics.RequestRelicsCombinationComplete;
import net.sf.l2jdev.gameserver.network.clientpackets.relics.RequestRelicsIdSummon;
import net.sf.l2jdev.gameserver.network.clientpackets.relics.RequestRelicsOpenUI;
import net.sf.l2jdev.gameserver.network.clientpackets.relics.RequestRelicsProbList;
import net.sf.l2jdev.gameserver.network.clientpackets.relics.RequestRelicsSummon;
import net.sf.l2jdev.gameserver.network.clientpackets.relics.RequestRelicsSummonCloseUI;
import net.sf.l2jdev.gameserver.network.clientpackets.relics.RequestRelicsSummonList;
import net.sf.l2jdev.gameserver.network.clientpackets.relics.RequestRelicsUpgrade;
import net.sf.l2jdev.gameserver.network.clientpackets.revenge.RequestExPvpBookShareRevengeKillerLocation;
import net.sf.l2jdev.gameserver.network.clientpackets.revenge.RequestExPvpBookShareRevengeList;
import net.sf.l2jdev.gameserver.network.clientpackets.revenge.RequestExPvpBookShareRevengeReqShareRevengeInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.revenge.RequestExPvpBookShareRevengeSharedTeleportToKiller;
import net.sf.l2jdev.gameserver.network.clientpackets.revenge.RequestExPvpBookShareRevengeTeleportToKiller;
import net.sf.l2jdev.gameserver.network.clientpackets.sayune.RequestFlyMove;
import net.sf.l2jdev.gameserver.network.clientpackets.sayune.RequestFlyMoveStart;
import net.sf.l2jdev.gameserver.network.clientpackets.secretshop.ExRequestFestivalBmGame;
import net.sf.l2jdev.gameserver.network.clientpackets.secretshop.ExRequestFestivalBmInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.settings.ExInteractModify;
import net.sf.l2jdev.gameserver.network.clientpackets.settings.ExSaveItemAnnounceSetting;
import net.sf.l2jdev.gameserver.network.clientpackets.settings.RequestKeyMapping;
import net.sf.l2jdev.gameserver.network.clientpackets.settings.RequestSaveKeyMapping;
import net.sf.l2jdev.gameserver.network.clientpackets.shuttle.CannotMoveAnymoreInShuttle;
import net.sf.l2jdev.gameserver.network.clientpackets.shuttle.MoveToLocationInShuttle;
import net.sf.l2jdev.gameserver.network.clientpackets.shuttle.RequestShuttleGetOff;
import net.sf.l2jdev.gameserver.network.clientpackets.shuttle.RequestShuttleGetOn;
import net.sf.l2jdev.gameserver.network.clientpackets.skillenchantextract.RequestExtractSkillEnchant;
import net.sf.l2jdev.gameserver.network.clientpackets.skillenchantguarantee.RequestSkillEnchantConfirm;
import net.sf.l2jdev.gameserver.network.clientpackets.stats.ExResetStatusBonus;
import net.sf.l2jdev.gameserver.network.clientpackets.stats.ExSetStatusBonus;
import net.sf.l2jdev.gameserver.network.clientpackets.steadybox.RequestSteadyBoxLoad;
import net.sf.l2jdev.gameserver.network.clientpackets.steadybox.RequestSteadyGetReward;
import net.sf.l2jdev.gameserver.network.clientpackets.steadybox.RequestSteadyOpenBox;
import net.sf.l2jdev.gameserver.network.clientpackets.steadybox.RequestSteadyOpenSlot;
import net.sf.l2jdev.gameserver.network.clientpackets.storereview.ExRequestPrivateStoreSearchList;
import net.sf.l2jdev.gameserver.network.clientpackets.storereview.ExRequestPrivateStoreSearchStatistics;
import net.sf.l2jdev.gameserver.network.clientpackets.subjugation.RequestSubjugationGacha;
import net.sf.l2jdev.gameserver.network.clientpackets.subjugation.RequestSubjugationGachaUI;
import net.sf.l2jdev.gameserver.network.clientpackets.subjugation.RequestSubjugationList;
import net.sf.l2jdev.gameserver.network.clientpackets.subjugation.RequestSubjugationRanking;
import net.sf.l2jdev.gameserver.network.clientpackets.surveillance.ExRequestUserWatcherAdd;
import net.sf.l2jdev.gameserver.network.clientpackets.surveillance.ExRequestUserWatcherDelete;
import net.sf.l2jdev.gameserver.network.clientpackets.surveillance.ExRequestUserWatcherTargetList;
import net.sf.l2jdev.gameserver.network.clientpackets.teleports.ExRequestSharedLocationTeleport;
import net.sf.l2jdev.gameserver.network.clientpackets.teleports.ExRequestSharedLocationTeleportUi;
import net.sf.l2jdev.gameserver.network.clientpackets.teleports.ExRequestSharingLocationUi;
import net.sf.l2jdev.gameserver.network.clientpackets.teleports.ExRequestTeleport;
import net.sf.l2jdev.gameserver.network.clientpackets.teleports.ExRequestTeleportFavoriteList;
import net.sf.l2jdev.gameserver.network.clientpackets.teleports.ExRequestTeleportFavoritesAddDel;
import net.sf.l2jdev.gameserver.network.clientpackets.teleports.ExRequestTeleportFavoritesUIToggle;
import net.sf.l2jdev.gameserver.network.clientpackets.teleports.ExTeleportToRaidPosition;
import net.sf.l2jdev.gameserver.network.clientpackets.teleports.RequestRaidTeleportInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.training.NotifyTrainingRoomEnd;
import net.sf.l2jdev.gameserver.network.clientpackets.variation.ExApplyVariationOption;
import net.sf.l2jdev.gameserver.network.clientpackets.variation.ExVariationCloseUi;
import net.sf.l2jdev.gameserver.network.clientpackets.variation.ExVariationOpenUi;
import net.sf.l2jdev.gameserver.network.clientpackets.variation.RequestConfirmGemStone;
import net.sf.l2jdev.gameserver.network.clientpackets.variation.RequestRefine;
import net.sf.l2jdev.gameserver.network.clientpackets.vip.ExRequestVipInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.vip.RequestVipLuckGameInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.worldexchange.ExWorldExchangeAveragePrice;
import net.sf.l2jdev.gameserver.network.clientpackets.worldexchange.ExWorldExchangeBuyItem;
import net.sf.l2jdev.gameserver.network.clientpackets.worldexchange.ExWorldExchangeItemList;
import net.sf.l2jdev.gameserver.network.clientpackets.worldexchange.ExWorldExchangeRegisterItem;
import net.sf.l2jdev.gameserver.network.clientpackets.worldexchange.ExWorldExchangeSettleList;
import net.sf.l2jdev.gameserver.network.clientpackets.worldexchange.ExWorldExchangeSettleRecvResult;
import net.sf.l2jdev.gameserver.network.clientpackets.worldexchange.ExWorldExchangeTotalList;

public enum ExClientPackets
{
	EX_REQ_MANOR_LIST(1, RequestManorList::new, ConnectionState.IN_GAME),
	EX_PROCURE_CROP_LIST(2, RequestProcureCropList::new, ConnectionState.IN_GAME),
	EX_SET_SEED(3, RequestSetSeed::new, ConnectionState.IN_GAME),
	EX_SET_CROP(4, RequestSetCrop::new, ConnectionState.IN_GAME),
	EX_WRITE_HERO_WORDS(5, RequestWriteHeroWords::new, ConnectionState.IN_GAME),
	EX_ASK_JOIN_MPCC(6, RequestExAskJoinMPCC::new, ConnectionState.IN_GAME),
	EX_ACCEPT_JOIN_MPCC(7, RequestExAcceptJoinMPCC::new, ConnectionState.IN_GAME),
	EX_OUST_FROM_MPCC(8, RequestExOustFromMPCC::new, ConnectionState.IN_GAME),
	EX_OUST_FROM_PARTY_ROOM(9, RequestOustFromPartyRoom::new, ConnectionState.IN_GAME),
	EX_DISMISS_PARTY_ROOM(10, RequestDismissPartyRoom::new, ConnectionState.IN_GAME),
	EX_WITHDRAW_PARTY_ROOM(11, RequestWithdrawPartyRoom::new, ConnectionState.IN_GAME),
	EX_HAND_OVER_PARTY_MASTER(12, RequestChangePartyLeader::new, ConnectionState.IN_GAME),
	EX_AUTO_SOULSHOT(13, RequestAutoSoulShot::new, ConnectionState.IN_GAME),
	EX_ENCHANT_SKILL_INFO(14, RequestExEnchantSkillInfo::new, ConnectionState.IN_GAME),
	EX_REQ_ENCHANT_SKILL(15, RequestExEnchantSkill::new, ConnectionState.IN_GAME),
	EX_PLEDGE_EMBLEM(16, RequestExPledgeCrestLarge::new, ConnectionState.IN_GAME),
	EX_SET_PLEDGE_EMBLEM(17, RequestExSetPledgeCrestLarge::new, ConnectionState.IN_GAME),
	EX_SET_ACADEMY_MASTER(18, RequestPledgeSetAcademyMaster::new, ConnectionState.IN_GAME),
	EX_PLEDGE_POWER_GRADE_LIST(19, RequestPledgePowerGradeList::new, ConnectionState.IN_GAME),
	EX_VIEW_PLEDGE_POWER(20, RequestPledgeMemberPowerInfo::new, ConnectionState.IN_GAME),
	EX_SET_PLEDGE_POWER_GRADE(21, RequestPledgeSetMemberPowerGrade::new, ConnectionState.IN_GAME),
	EX_VIEW_PLEDGE_MEMBER_INFO(22, RequestPledgeMemberInfo::new, ConnectionState.IN_GAME),
	EX_VIEW_PLEDGE_WARLIST(23, RequestPledgeWarList::new, ConnectionState.IN_GAME),
	EX_FISH_RANKING(24, RequestExFishRanking::new, ConnectionState.IN_GAME),
	EX_PCCAFE_COUPON_USE(25, RequestPCCafeCouponUse::new, ConnectionState.IN_GAME),
	EX_ORC_MOVE(26, null, ConnectionState.IN_GAME),
	EX_DUEL_ASK_START(27, RequestDuelStart::new, ConnectionState.IN_GAME),
	EX_DUEL_ACCEPT_START(28, RequestDuelAnswerStart::new, ConnectionState.IN_GAME),
	EX_SET_TUTORIAL(29, null, ConnectionState.IN_GAME),
	EX_RQ_ITEMLINK(30, RequestExRqItemLink::new, ConnectionState.IN_GAME),
	EX_CAN_NOT_MOVE_ANYMORE_IN_AIRSHIP(31, null, ConnectionState.IN_GAME),
	EX_MOVE_TO_LOCATION_IN_AIRSHIP(32, MoveToLocationInAirShip::new, ConnectionState.IN_GAME),
	EX_LOAD_UI_SETTING(33, RequestKeyMapping::new, ConnectionState.ENTERING, ConnectionState.IN_GAME),
	EX_SAVE_UI_SETTING(34, RequestSaveKeyMapping::new, ConnectionState.IN_GAME),
	EX_REQUEST_BASE_ATTRIBUTE_CANCEL(35, RequestExRemoveItemAttribute::new, ConnectionState.IN_GAME),
	EX_CHANGE_INVENTORY_SLOT(36, RequestSaveInventoryOrder::new, ConnectionState.IN_GAME),
	EX_EXIT_PARTY_MATCHING_WAITING_ROOM(37, RequestExitPartyMatchingWaitingRoom::new, ConnectionState.IN_GAME),
	EX_TRY_TO_PUT_ITEM_FOR_VARIATION_MAKE(38, RequestConfirmTargetItem::new, ConnectionState.IN_GAME),
	EX_TRY_TO_PUT_INTENSIVE_FOR_VARIATION_MAKE(39, RequestConfirmRefinerItem::new, ConnectionState.IN_GAME),
	EX_TRY_TO_PUT_COMMISSION_FOR_VARIATION_MAKE(40, RequestConfirmGemStone::new, ConnectionState.IN_GAME),
	EX_OLYMPIAD_OBSERVER_END(41, RequestOlympiadObserverEnd::new, ConnectionState.IN_GAME),
	EX_CURSED_WEAPON_LIST(42, RequestCursedWeaponList::new, ConnectionState.IN_GAME),
	EX_EXISTING_CURSED_WEAPON_LOCATION(43, RequestCursedWeaponLocation::new, ConnectionState.IN_GAME),
	EX_REORGANIZE_PLEDGE_MEMBER(44, RequestPledgeReorganizeMember::new, ConnectionState.IN_GAME),
	EX_MPCC_SHOW_PARTY_MEMBERS_INFO(45, RequestExMPCCShowPartyMembersInfo::new, ConnectionState.IN_GAME),
	EX_OLYMPIAD_MATCH_LIST(46, RequestOlympiadMatchList::new, ConnectionState.IN_GAME),
	EX_ASK_JOIN_PARTY_ROOM(47, RequestAskJoinPartyRoom::new, ConnectionState.IN_GAME),
	EX_ANSWER_JOIN_PARTY_ROOM(48, AnswerJoinPartyRoom::new, ConnectionState.IN_GAME),
	EX_LIST_PARTY_MATCHING_WAITING_ROOM(49, RequestListPartyMatchingWaitingRoom::new, ConnectionState.IN_GAME),
	EX_CHOOSE_INVENTORY_ATTRIBUTE_ITEM(50, RequestExEnchantItemAttribute::new, ConnectionState.IN_GAME),
	EX_CHARACTER_BACK(51, RequestGotoLobby::new, ConnectionState.AUTHENTICATED),
	EX_CANNOT_AIRSHIP_MOVE_ANYMORE(52, null, ConnectionState.IN_GAME),
	EX_MOVE_TO_LOCATION_AIRSHIP(53, MoveToLocationAirShip::new, ConnectionState.IN_GAME),
	EX_ITEM_AUCTION_BID(54, RequestBidItemAuction::new, ConnectionState.IN_GAME),
	EX_ITEM_AUCTION_INFO(55, RequestInfoItemAuction::new, ConnectionState.IN_GAME),
	EX_CHANGE_NAME(56, RequestExChangeName::new, ConnectionState.IN_GAME),
	EX_SHOW_CASTLE_INFO(57, RequestAllCastleInfo::new, ConnectionState.IN_GAME),
	EX_SHOW_FORTRESS_INFO(58, RequestAllFortressInfo::new, ConnectionState.IN_GAME),
	EX_SHOW_AGIT_INFO(59, RequestAllAgitInfo::new, ConnectionState.IN_GAME),
	EX_SHOW_FORTRESS_SIEGE_INFO(60, RequestFortressSiegeInfo::new, ConnectionState.IN_GAME),
	EX_GET_BOSS_RECORD(61, RequestGetBossRecord::new, ConnectionState.IN_GAME),
	EX_TRY_TO_MAKE_VARIATION(62, RequestRefine::new, ConnectionState.IN_GAME),
	EX_TRY_TO_PUT_ITEM_FOR_VARIATION_CANCEL(63, RequestConfirmCancelItem::new, ConnectionState.IN_GAME),
	EX_CLICK_VARIATION_CANCEL_BUTTON(64, RequestRefineCancel::new, ConnectionState.IN_GAME),
	EX_MAGIC_SKILL_USE_GROUND(65, RequestExMagicSkillUseGround::new, ConnectionState.IN_GAME),
	EX_DUEL_SURRENDER(66, RequestDuelSurrender::new, ConnectionState.IN_GAME),
	EX_ENCHANT_SKILL_INFO_DETAIL(67, RequestExEnchantSkillInfoDetail::new, ConnectionState.IN_GAME),
	EX_REQUEST_ANTI_FREE_SERVER(68, null, ConnectionState.IN_GAME),
	EX_SHOW_FORTRESS_MAP_INFO(69, RequestFortressMapInfo::new, ConnectionState.IN_GAME),
	EX_REQUEST_PVPMATCH_RECORD(70, RequestPVPMatchRecord::new, ConnectionState.IN_GAME),
	EX_PRIVATE_STORE_WHOLE_SET_MSG(71, SetPrivateStoreWholeMsg::new, ConnectionState.IN_GAME),
	EX_DISPEL(72, RequestDispel::new, ConnectionState.IN_GAME),
	EX_TRY_TO_PUT_ENCHANT_TARGET_ITEM(73, RequestExTryToPutEnchantTargetItem::new, ConnectionState.IN_GAME),
	EX_TRY_TO_PUT_ENCHANT_SUPPORT_ITEM(74, RequestExTryToPutEnchantSupportItem::new, ConnectionState.IN_GAME),
	EX_CANCEL_ENCHANT_ITEM(75, RequestExCancelEnchantItem::new, ConnectionState.IN_GAME),
	EX_CHANGE_NICKNAME_COLOR(76, RequestChangeNicknameColor::new, ConnectionState.IN_GAME),
	EX_REQUEST_RESET_NICKNAME(77, RequestResetNickname::new, ConnectionState.IN_GAME),
	EX_USER_BOOKMARK(78, null, ConnectionState.IN_GAME),
	EX_WITHDRAW_PREMIUM_ITEM(79, RequestWithDrawPremiumItem::new, ConnectionState.IN_GAME),
	EX_JUMP(80, null, ConnectionState.IN_GAME),
	EX_START_REQUEST_PVPMATCH_CC_RANK(81, null, ConnectionState.IN_GAME),
	EX_STOP_REQUEST_PVPMATCH_CC_RANK(82, null, ConnectionState.IN_GAME),
	EX_NOTIFY_START_MINIGAME(83, null, ConnectionState.IN_GAME),
	EX_REQUEST_REGISTER_DOMINION(84, null, ConnectionState.IN_GAME),
	EX_REQUEST_DOMINION_INFO(85, null, ConnectionState.IN_GAME),
	EX_CLEFT_ENTER(86, null, ConnectionState.IN_GAME),
	EX_BLOCK_UPSET_ENTER(87, null, ConnectionState.IN_GAME),
	EX_END_SCENE_PLAYER(88, EndScenePlayer::new, ConnectionState.IN_GAME),
	EX_BLOCK_UPSET_VOTE(89, null, ConnectionState.IN_GAME),
	EX_LIST_MPCC_WAITING(90, RequestExListMpccWaiting::new, ConnectionState.IN_GAME),
	EX_MANAGE_MPCC_ROOM(91, RequestExManageMpccRoom::new, ConnectionState.IN_GAME),
	EX_JOIN_MPCC_ROOM(92, RequestExJoinMpccRoom::new, ConnectionState.IN_GAME),
	EX_OUST_FROM_MPCC_ROOM(93, RequestExOustFromMpccRoom::new, ConnectionState.IN_GAME),
	EX_DISMISS_MPCC_ROOM(94, RequestExDismissMpccRoom::new, ConnectionState.IN_GAME),
	EX_WITHDRAW_MPCC_ROOM(95, RequestExWithdrawMpccRoom::new, ConnectionState.IN_GAME),
	EX_SEED_PHASE(96, RequestSeedPhase::new, ConnectionState.IN_GAME),
	EX_MPCC_PARTYMASTER_LIST(97, RequestExMpccPartymasterList::new, ConnectionState.IN_GAME),
	EX_REQUEST_POST_ITEM_LIST(98, RequestPostItemList::new, ConnectionState.IN_GAME),
	EX_SEND_POST(99, RequestSendPost::new, ConnectionState.IN_GAME),
	EX_REQUEST_RECEIVED_POST_LIST(100, RequestReceivedPostList::new, ConnectionState.IN_GAME),
	EX_DELETE_RECEIVED_POST(101, RequestDeleteReceivedPost::new, ConnectionState.IN_GAME),
	EX_REQUEST_RECEIVED_POST(102, RequestReceivedPost::new, ConnectionState.IN_GAME),
	EX_RECEIVE_POST(103, RequestPostAttachment::new, ConnectionState.IN_GAME),
	EX_REJECT_POST(104, RequestRejectPostAttachment::new, ConnectionState.IN_GAME),
	EX_REQUEST_SENT_POST_LIST(105, RequestSentPostList::new, ConnectionState.IN_GAME),
	EX_DELETE_SENT_POST(106, RequestDeleteSentPost::new, ConnectionState.IN_GAME),
	EX_REQUEST_SENT_POST(107, RequestSentPost::new, ConnectionState.IN_GAME),
	EX_CANCEL_SEND_POST(108, RequestCancelPostAttachment::new, ConnectionState.IN_GAME),
	EX_POST_ITEM_FEE(109, RequestPostItemFee::new, ConnectionState.IN_GAME),
	EX_REQUEST_SHOW_PETITION(110, null, ConnectionState.IN_GAME),
	EX_POST_TRADE_FEE(111, null, ConnectionState.IN_GAME),
	EX_POST_TRADE_COUNT(112, null, ConnectionState.IN_GAME),
	EX_REQUEST_SHOWSTEP_TWO(113, null, ConnectionState.IN_GAME),
	EX_REQUEST_SHOWSTEP_THREE(114, null, ConnectionState.IN_GAME),
	EX_CONNECT_TO_RAID_SERVER(115, null, ConnectionState.IN_GAME),
	EX_RETURN_FROM_RAID(116, null, ConnectionState.IN_GAME),
	EX_REFUND_REQ(117, RequestRefundItem::new, ConnectionState.IN_GAME),
	EX_BUY_SELL_UI_CLOSE_REQ(118, RequestBuySellUIClose::new, ConnectionState.IN_GAME),
	EX_EVENT_MATCH(119, null, ConnectionState.IN_GAME),
	EX_PARTY_LOOTING_MODIFY(120, RequestPartyLootModification::new, ConnectionState.IN_GAME),
	EX_PARTY_LOOTING_MODIFY_AGREEMENT(121, AnswerPartyLootModification::new, ConnectionState.IN_GAME),
	EX_ANSWER_COUPLE_ACTION(122, AnswerCoupleAction::new, ConnectionState.IN_GAME),
	EX_BR_LOAD_EVENT_TOP_RANKERS_REQ(123, BrEventRankerList::new, ConnectionState.IN_GAME),
	EX_ASK_MY_MEMBERSHIP(124, null, ConnectionState.IN_GAME),
	EX_QUEST_NPC_LOG_LIST(125, RequestAddExpandQuestAlarm::new, ConnectionState.IN_GAME),
	EX_VOTE_SYSTEM(126, RequestVoteNew::new, ConnectionState.IN_GAME),
	EX_GETON_SHUTTLE(127, RequestShuttleGetOn::new, ConnectionState.IN_GAME),
	EX_GETOFF_SHUTTLE(128, RequestShuttleGetOff::new, ConnectionState.IN_GAME),
	EX_MOVE_TO_LOCATION_IN_SHUTTLE(129, MoveToLocationInShuttle::new, ConnectionState.IN_GAME),
	EX_CAN_NOT_MOVE_ANYMORE_IN_SHUTTLE(130, CannotMoveAnymoreInShuttle::new, ConnectionState.IN_GAME),
	EX_AGITAUCTION_CMD(131, null, ConnectionState.IN_GAME),
	EX_ADD_POST_FRIEND(132, RequestExAddContactToContactList::new, ConnectionState.IN_GAME),
	EX_DELETE_POST_FRIEND(133, RequestExDeleteContactFromContactList::new, ConnectionState.IN_GAME),
	EX_SHOW_POST_FRIEND(134, RequestExShowContactList::new, ConnectionState.IN_GAME),
	EX_FRIEND_LIST_FOR_POSTBOX(135, RequestExFriendListExtended::new, ConnectionState.IN_GAME),
	EX_GFX_OLYMPIAD(136, RequestExOlympiadMatchListRefresh::new, ConnectionState.IN_GAME),
	EX_BR_GAME_POINT_REQ(137, RequestBRGamePoint::new, ConnectionState.IN_GAME),
	EX_BR_PRODUCT_LIST_REQ(138, RequestBRProductList::new, ConnectionState.IN_GAME),
	EX_BR_PRODUCT_INFO_REQ(139, RequestBRProductInfo::new, ConnectionState.IN_GAME),
	EX_BR_BUY_PRODUCT_REQ(140, RequestBRBuyProduct::new, ConnectionState.IN_GAME),
	EX_BR_RECENT_PRODUCT_REQ(141, RequestBRRecentProductList::new, ConnectionState.IN_GAME),
	EX_BR_MINIGAME_LOAD_SCORES_REQ(142, null, ConnectionState.IN_GAME),
	EX_BR_MINIGAME_INSERT_SCORE_REQ(143, null, ConnectionState.IN_GAME),
	EX_BR_SET_LECTURE_MARK_REQ(144, null, ConnectionState.IN_GAME),
	EX_REQUEST_CRYSTALITEM_INFO(145, RequestCrystallizeEstimate::new, ConnectionState.IN_GAME),
	EX_REQUEST_CRYSTALITEM_CANCEL(146, RequestCrystallizeItemCancel::new, ConnectionState.IN_GAME),
	EX_STOP_SCENE_PLAYER(147, RequestExEscapeScene::new, ConnectionState.IN_GAME),
	EX_FLY_MOVE(148, RequestFlyMove::new, ConnectionState.IN_GAME),
	EX_SURRENDER_PLEDGE_WAR(149, null, ConnectionState.IN_GAME),
	EX_DYNAMIC_QUEST(150, null, ConnectionState.IN_GAME),
	EX_FRIEND_DETAIL_INFO(151, RequestFriendDetailInfo::new, ConnectionState.IN_GAME),
	EX_UPDATE_FRIEND_MEMO(152, RequestUpdateFriendMemo::new, ConnectionState.IN_GAME),
	EX_UPDATE_BLOCK_MEMO(153, RequestBlockMemo::new, ConnectionState.IN_GAME),
	EX_LOAD_INZONE_PARTY_HISTORY(154, null, ConnectionState.IN_GAME),
	EX_REQUEST_COMMISSION_ITEM_LIST(155, RequestCommissionRegistrableItemList::new, ConnectionState.IN_GAME),
	EX_REQUEST_COMMISSION_INFO(156, RequestCommissionInfo::new, ConnectionState.IN_GAME),
	EX_REQUEST_COMMISSION_REGISTER(157, RequestCommissionRegister::new, ConnectionState.IN_GAME),
	EX_REQUEST_COMMISSION_CANCEL(158, RequestCommissionCancel::new, ConnectionState.IN_GAME),
	EX_REQUEST_COMMISSION_DELETE(159, RequestCommissionDelete::new, ConnectionState.IN_GAME),
	EX_REQUEST_COMMISSION_SEARCH(160, RequestCommissionList::new, ConnectionState.IN_GAME),
	EX_REQUEST_COMMISSION_BUY_INFO(161, RequestCommissionBuyInfo::new, ConnectionState.IN_GAME),
	EX_REQUEST_COMMISSION_BUY_ITEM(162, RequestCommissionBuyItem::new, ConnectionState.IN_GAME),
	EX_REQUEST_COMMISSION_REGISTERED_ITEM(163, RequestCommissionRegisteredItem::new, ConnectionState.IN_GAME),
	EX_CALL_TO_CHANGE_CLASS(164, null, ConnectionState.IN_GAME),
	EX_CHANGE_TO_AWAKENED_CLASS(165, RequestChangeToAwakenedClass::new, ConnectionState.IN_GAME),
	EX_REQUEST_WORLD_STATISTICS(166, null, ConnectionState.IN_GAME),
	EX_REQUEST_USER_STATISTICS(167, null, ConnectionState.IN_GAME),
	EX_REQUEST_WEB_SESSION_ID(168, null, ConnectionState.IN_GAME),
	EX_2ND_PASSWORD_CHECK(169, RequestEx2ndPasswordCheck::new, ConnectionState.AUTHENTICATED),
	EX_2ND_PASSWORD_VERIFY(170, RequestEx2ndPasswordVerify::new, ConnectionState.AUTHENTICATED),
	EX_2ND_PASSWORD_REQ(171, RequestEx2ndPasswordReq::new, ConnectionState.AUTHENTICATED),
	EX_CHECK_CHAR_NAME(172, RequestCharacterNameCreatable::new, ConnectionState.AUTHENTICATED),
	EX_REQUEST_GOODS_INVENTORY_INFO(173, null, ConnectionState.IN_GAME),
	EX_REQUEST_USE_GOODS_IVENTORY_ITEM(174, null, ConnectionState.IN_GAME),
	EX_NOTIFY_PLAY_START(175, null, ConnectionState.IN_GAME),
	EX_FLY_MOVE_START(176, RequestFlyMoveStart::new, ConnectionState.IN_GAME),
	EX_USER_HARDWARE_INFO(177, RequestHardWareInfo::new, ConnectionState.values()),
	EX_USER_INTERFACE_INFO(178, null, ConnectionState.IN_GAME),
	EX_CHANGE_ATTRIBUTE_ITEM(179, SendChangeAttributeTargetItem::new, ConnectionState.IN_GAME),
	EX_REQUEST_CHANGE_ATTRIBUTE(180, RequestChangeAttributeItem::new, ConnectionState.IN_GAME),
	EX_CHANGE_ATTRIBUTE_CANCEL(181, RequestChangeAttributeCancel::new, ConnectionState.IN_GAME),
	EX_BR_BUY_PRODUCT_GIFT_REQ(182, RequestBRPresentBuyProduct::new, ConnectionState.IN_GAME),
	EX_MENTOR_ADD(183, ConfirmMenteeAdd::new, ConnectionState.IN_GAME),
	EX_MENTOR_CANCEL(184, RequestMentorCancel::new, ConnectionState.IN_GAME),
	EX_MENTOR_LIST(185, RequestMentorList::new, ConnectionState.IN_GAME),
	EX_REQUEST_MENTOR_ADD(186, RequestMenteeAdd::new, ConnectionState.IN_GAME),
	EX_MENTEE_WAITING_LIST(187, RequestMenteeWaitingList::new, ConnectionState.IN_GAME),
	EX_JOIN_PLEDGE_BY_NAME(188, RequestClanAskJoinByName::new, ConnectionState.IN_GAME),
	EX_INZONE_WAITING_TIME(189, RequestInzoneWaitingTime::new, ConnectionState.IN_GAME),
	EX_JOIN_CURIOUS_HOUSE(190, null, ConnectionState.IN_GAME),
	EX_CANCEL_CURIOUS_HOUSE(191, null, ConnectionState.IN_GAME),
	EX_LEAVE_CURIOUS_HOUSE(192, null, ConnectionState.IN_GAME),
	EX_OBSERVE_LIST_CURIOUS_HOUSE(193, null, ConnectionState.IN_GAME),
	EX_OBSERVE_CURIOUS_HOUSE(194, null, ConnectionState.IN_GAME),
	EX_EXIT_OBSERVE_CURIOUS_HOUSE(195, null, ConnectionState.IN_GAME),
	EX_REQ_CURIOUS_HOUSE_HTML(196, null, ConnectionState.IN_GAME),
	EX_REQ_CURIOUS_HOUSE_RECORD(197, null, ConnectionState.IN_GAME),
	EX_SYS_STRING(198, null, ConnectionState.IN_GAME),
	EX_TRY_TO_PUT_SHAPE_SHIFTING_TARGET_ITEM(199, RequestExTryToPutShapeShiftingTargetItem::new, ConnectionState.IN_GAME),
	EX_TRY_TO_PUT_SHAPE_SHIFTING_EXTRACTION_ITEM(200, RequestExTryToPutShapeShiftingEnchantSupportItem::new, ConnectionState.IN_GAME),
	EX_CANCEL_SHAPE_SHIFTING(201, RequestExCancelShape_Shifting_Item::new, ConnectionState.IN_GAME),
	EX_REQUEST_SHAPE_SHIFTING(202, RequestShapeShiftingItem::new, ConnectionState.IN_GAME),
	EX_NCGUARD(203, null, ConnectionState.IN_GAME),
	EX_REQUEST_KALIE_TOKEN(204, null, ConnectionState.IN_GAME),
	EX_REQUEST_SHOW_REGIST_BEAUTY(205, RequestShowBeautyList::new, ConnectionState.IN_GAME),
	EX_REQUEST_REGIST_BEAUTY(206, RequestRegistBeauty::new, ConnectionState.IN_GAME),
	EX_REQUEST_SHOW_RESET_BEAUTY(207, null, ConnectionState.IN_GAME),
	EX_REQUEST_RESET_BEAUTY(208, RequestShowResetShopList::new, ConnectionState.IN_GAME),
	EX_CHECK_SPEEDHACK(209, null, ConnectionState.IN_GAME),
	EX_BR_ADD_INTERESTED_PRODUCT(210, null, ConnectionState.IN_GAME),
	EX_BR_DELETE_INTERESTED_PRODUCT(211, null, ConnectionState.IN_GAME),
	EX_BR_EXIST_NEW_PRODUCT_REQ(212, null, ConnectionState.IN_GAME),
	EX_EVENT_CAMPAIGN_INFO(213, null, ConnectionState.IN_GAME),
	EX_PLEDGE_RECRUIT_INFO(214, RequestPledgeRecruitInfo::new, ConnectionState.IN_GAME),
	EX_PLEDGE_RECRUIT_BOARD_SEARCH(215, RequestPledgeRecruitBoardSearch::new, ConnectionState.IN_GAME),
	EX_PLEDGE_RECRUIT_BOARD_APPLY(216, RequestPledgeRecruitBoardAccess::new, ConnectionState.IN_GAME),
	EX_PLEDGE_RECRUIT_BOARD_DETAIL(217, RequestPledgeRecruitBoardDetail::new, ConnectionState.IN_GAME),
	EX_PLEDGE_WAITING_LIST_APPLY(218, RequestPledgeWaitingApply::new, ConnectionState.IN_GAME),
	EX_PLEDGE_WAITING_LIST_APPLIED(219, RequestPledgeWaitingApplied::new, ConnectionState.IN_GAME),
	EX_PLEDGE_WAITING_LIST(220, RequestPledgeWaitingList::new, ConnectionState.IN_GAME),
	EX_PLEDGE_WAITING_USER(221, RequestPledgeWaitingUser::new, ConnectionState.IN_GAME),
	EX_PLEDGE_WAITING_USER_ACCEPT(222, RequestPledgeWaitingUserAccept::new, ConnectionState.IN_GAME),
	EX_PLEDGE_DRAFT_LIST_SEARCH(223, RequestPledgeDraftListSearch::new, ConnectionState.IN_GAME),
	EX_PLEDGE_DRAFT_LIST_APPLY(224, RequestPledgeDraftListApply::new, ConnectionState.IN_GAME),
	EX_PLEDGE_RECRUIT_APPLY_INFO(225, RequestPledgeRecruitApplyInfo::new, ConnectionState.IN_GAME),
	EX_PLEDGE_JOIN_SYS(226, null, ConnectionState.IN_GAME),
	EX_RESPONSE_WEB_PETITION_ALARM(227, null, ConnectionState.IN_GAME),
	EX_NOTIFY_EXIT_BEAUTYSHOP(228, NotifyExitBeautyShop::new, ConnectionState.IN_GAME),
	EX_EVENT_REGISTER_XMAS_WISHCARD(229, null, ConnectionState.IN_GAME),
	EX_ENCHANT_SCROLL_ITEM_ADD(230, RequestExAddEnchantScrollItem::new, ConnectionState.IN_GAME),
	EX_ENCHANT_SUPPORT_ITEM_REMOVE(231, RequestExRemoveEnchantSupportItem::new, ConnectionState.IN_GAME),
	EX_SELECT_CARD_REWARD(232, null, ConnectionState.IN_GAME),
	EX_DIVIDE_ADENA_START(233, RequestDivideAdenaStart::new, ConnectionState.IN_GAME),
	EX_DIVIDE_ADENA_CANCEL(234, RequestDivideAdenaCancel::new, ConnectionState.IN_GAME),
	EX_DIVIDE_ADENA(235, RequestDivideAdena::new, ConnectionState.IN_GAME),
	EX_ACQUIRE_POTENTIAL_SKILL(236, null, ConnectionState.IN_GAME),
	EX_REQUEST_POTENTIAL_SKILL_LIST(237, null, ConnectionState.IN_GAME),
	EX_RESET_POTENTIAL_SKILL(238, null, ConnectionState.IN_GAME),
	EX_CHANGE_POTENTIAL_POINT(239, null, ConnectionState.IN_GAME),
	EX_STOP_MOVE(240, RequestStopMove::new, ConnectionState.IN_GAME),
	EX_ABILITY_WND_OPEN(241, null, ConnectionState.IN_GAME),
	EX_ABILITY_WND_CLOSE(242, null, ConnectionState.IN_GAME),
	EX_START_LUCKY_GAME(243, RequestLuckyGameStartInfo::new, ConnectionState.IN_GAME),
	EX_BETTING_LUCKY_GAME(244, RequestLuckyGamePlay::new, ConnectionState.IN_GAME),
	EX_TRAININGZONE_LEAVING(245, NotifyTrainingRoomEnd::new, ConnectionState.IN_GAME),
	EX_ENCHANT_ONE(246, RequestNewEnchantPushOne::new, ConnectionState.IN_GAME),
	EX_ENCHANT_ONE_REMOVE(247, RequestNewEnchantRemoveOne::new, ConnectionState.IN_GAME),
	EX_ENCHANT_TWO(248, RequestNewEnchantPushTwo::new, ConnectionState.IN_GAME),
	EX_ENCHANT_TWO_REMOVE(249, RequestNewEnchantRemoveTwo::new, ConnectionState.IN_GAME),
	EX_ENCHANT_CLOSE(250, RequestNewEnchantClose::new, ConnectionState.IN_GAME),
	EX_ENCHANT_TRY(251, RequestNewEnchantTry::new, ConnectionState.IN_GAME),
	EX_ENCHANT_RETRY_TO_PUT_ITEMS(252, RequestNewEnchantRetryToPutItems::new, ConnectionState.IN_GAME),
	EX_REQUEST_CARD_REWARD_LIST(253, null, ConnectionState.IN_GAME),
	EX_REQUEST_ACCOUNT_ATTENDANCE_INFO(254, null, ConnectionState.IN_GAME),
	EX_REQUEST_ACCOUNT_ATTENDANCE_REWARD(255, null, ConnectionState.IN_GAME),
	EX_TARGET(256, RequestTargetActionMenu::new, ConnectionState.IN_GAME),
	EX_SELECTED_QUEST_ZONEID(257, ExSendSelectedQuestZoneID::new, ConnectionState.IN_GAME),
	EX_ALCHEMY_SKILL_LIST(258, RequestAlchemySkillList::new, ConnectionState.IN_GAME),
	EX_TRY_MIX_CUBE(259, null, ConnectionState.IN_GAME),
	C_REQUEST_ALCHEMY_CONVERSION(260, null, ConnectionState.IN_GAME),
	EX_EXECUTED_UIEVENTS_COUNT(261, null, ConnectionState.IN_GAME),
	EX_CLIENT_INI(262, null, ConnectionState.AUTHENTICATED),
	EX_REQUEST_AUTOFISH(263, ExRequestAutoFish::new, ConnectionState.IN_GAME),
	EX_REQUEST_VIP_ATTENDANCE_ITEMLIST(264, RequestVipAttendanceItemList::new, ConnectionState.IN_GAME),
	EX_REQUEST_VIP_ATTENDANCE_CHECK(265, RequestVipAttendanceCheck::new, ConnectionState.IN_GAME),
	EX_TRY_ENSOUL(266, RequestItemEnsoul::new, ConnectionState.IN_GAME),
	EX_CASTLEWAR_SEASON_REWARD(267, null, ConnectionState.IN_GAME),
	EX_BR_VIP_PRODUCT_LIST_REQ(268, null, ConnectionState.IN_GAME),
	EX_REQUEST_LUCKY_GAME_INFO(269, RequestVipLuckGameInfo::new, ConnectionState.IN_GAME),
	EX_REQUEST_LUCKY_GAME_ITEMLIST(270, null, ConnectionState.IN_GAME),
	EX_REQUEST_LUCKY_GAME_BONUS(271, null, ConnectionState.IN_GAME),
	EX_VIP_INFO(272, ExRequestVipInfo::new, ConnectionState.IN_GAME),
	EX_CAPTCHA_ANSWER(273, RequestCaptchaAnswer::new, ConnectionState.IN_GAME),
	EX_REFRESH_CAPTCHA_IMAGE(274, RequestRefreshCaptcha::new, ConnectionState.IN_GAME),
	EX_PLEDGE_SIGNIN(275, RequestPledgeSignInForOpenJoiningMethod::new, ConnectionState.IN_GAME),
	EX_REQUEST_MATCH_ARENA(276, null, ConnectionState.IN_GAME),
	EX_CONFIRM_MATCH_ARENA(277, null, ConnectionState.IN_GAME),
	EX_CANCEL_MATCH_ARENA(278, null, ConnectionState.IN_GAME),
	EX_CHANGE_CLASS_ARENA(279, null, ConnectionState.IN_GAME),
	EX_CONFIRM_CLASS_ARENA(280, null, ConnectionState.IN_GAME),
	EX_DECO_NPC_INFO(281, null, ConnectionState.IN_GAME),
	EX_DECO_NPC_SET(282, null, ConnectionState.IN_GAME),
	EX_FACTION_INFO(283, null, ConnectionState.IN_GAME),
	EX_EXIT_ARENA(284, null, ConnectionState.IN_GAME),
	EX_REQUEST_BALTHUS_TOKEN(285, RequestEventBalthusToken::new, ConnectionState.IN_GAME),
	EX_PARTY_MATCHING_ROOM_HISTORY(286, RequestPartyMatchingHistory::new, ConnectionState.IN_GAME),
	EX_ARENA_CUSTOM_NOTIFICATION(287, null, ConnectionState.IN_GAME),
	EX_TODOLIST(288, RequestTodoList::new, ConnectionState.IN_GAME),
	EX_TODOLIST_HTML(289, null, ConnectionState.IN_GAME),
	EX_ONE_DAY_RECEIVE_REWARD(290, RequestOneDayRewardReceive::new, ConnectionState.IN_GAME),
	EX_QUEUETICKET(291, null, ConnectionState.IN_GAME),
	EX_PLEDGE_BONUS_UI_OPEN(292, RequestPledgeBonusOpen::new, ConnectionState.IN_GAME),
	EX_PLEDGE_BONUS_REWARD_LIST(293, RequestPledgeBonusRewardList::new, ConnectionState.IN_GAME),
	EX_PLEDGE_BONUS_REWARD(294, RequestPledgeBonusReward::new, ConnectionState.IN_GAME),
	EX_SSO_AUTHNTOKEN_REQ(295, null, ConnectionState.IN_GAME),
	EX_QUEUETICKET_LOGIN(296, null, ConnectionState.IN_GAME),
	EX_BLOCK_DETAIL_INFO(297, RequestBlockDetailInfo::new, ConnectionState.IN_GAME),
	EX_TRY_ENSOUL_EXTRACTION(298, RequestTryEnSoulExtraction::new, ConnectionState.IN_GAME),
	EX_RAID_BOSS_SPAWN_INFO(299, RequestRaidBossSpawnInfo::new, ConnectionState.IN_GAME),
	EX_RAID_SERVER_INFO(300, RequestRaidServerInfo::new, ConnectionState.IN_GAME),
	EX_SHOW_AGIT_SIEGE_INFO(301, null, ConnectionState.IN_GAME),
	EX_ITEM_AUCTION_STATUS(302, null, ConnectionState.IN_GAME),
	EX_MONSTER_BOOK_OPEN(303, null, ConnectionState.IN_GAME),
	EX_MONSTER_BOOK_CLOSE(304, null, ConnectionState.IN_GAME),
	EX_REQ_MONSTER_BOOK_REWARD(305, null, ConnectionState.IN_GAME),
	EX_MATCHGROUP(306, null, ConnectionState.IN_GAME),
	EX_MATCHGROUP_ASK(307, null, ConnectionState.IN_GAME),
	EX_MATCHGROUP_ANSWER(308, null, ConnectionState.IN_GAME),
	EX_MATCHGROUP_WITHDRAW(309, null, ConnectionState.IN_GAME),
	EX_MATCHGROUP_OUST(310, null, ConnectionState.IN_GAME),
	EX_MATCHGROUP_CHANGE_MASTER(311, null, ConnectionState.IN_GAME),
	EX_UPGRADE_SYSTEM_REQUEST(312, RequestUpgradeSystemResult::new, ConnectionState.IN_GAME),
	EX_CARD_UPDOWN_PICK_NUMB(313, null, ConnectionState.IN_GAME),
	EX_CARD_UPDOWN_GAME_REWARD_REQUEST(314, null, ConnectionState.IN_GAME),
	EX_CARD_UPDOWN_GAME_RETRY(315, null, ConnectionState.IN_GAME),
	EX_CARD_UPDOWN_GAME_QUIT(316, null, ConnectionState.IN_GAME),
	EX_ARENA_RANK_ALL(317, null, ConnectionState.IN_GAME),
	EX_ARENA_MYRANK(318, null, ConnectionState.IN_GAME),
	EX_SWAP_AGATHION_SLOT_ITEMS(319, null, ConnectionState.IN_GAME),
	EX_PLEDGE_CONTRIBUTION_RANK(320, null, ConnectionState.IN_GAME),
	EX_PLEDGE_CONTRIBUTION_INFO(321, null, ConnectionState.IN_GAME),
	EX_PLEDGE_CONTRIBUTION_REWARD(322, null, ConnectionState.IN_GAME),
	EX_PLEDGE_LEVEL_UP(323, null, ConnectionState.IN_GAME),
	EX_PLEDGE_MISSION_INFO(324, null, ConnectionState.IN_GAME),
	EX_PLEDGE_MISSION_REWARD(325, null, ConnectionState.IN_GAME),
	EX_PLEDGE_MASTERY_INFO(326, null, ConnectionState.IN_GAME),
	EX_PLEDGE_MASTERY_SET(327, null, ConnectionState.IN_GAME),
	EX_PLEDGE_MASTERY_RESET(328, null, ConnectionState.IN_GAME),
	EX_PLEDGE_SKILL_INFO(329, null, ConnectionState.IN_GAME),
	EX_PLEDGE_SKILL_ACTIVATE(330, null, ConnectionState.IN_GAME),
	EX_PLEDGE_ITEM_LIST(331, null, ConnectionState.IN_GAME),
	EX_PLEDGE_ITEM_ACTIVATE(332, null, ConnectionState.IN_GAME),
	EX_PLEDGE_ANNOUNCE(333, null, ConnectionState.IN_GAME),
	EX_PLEDGE_ANNOUNCE_SET(334, null, ConnectionState.IN_GAME),
	EX_CREATE_PLEDGE(335, null, ConnectionState.IN_GAME),
	EX_PLEDGE_ITEM_INFO(336, null, ConnectionState.IN_GAME),
	EX_PLEDGE_ITEM_BUY(337, null, ConnectionState.IN_GAME),
	EX_ELEMENTAL_SPIRIT_INFO(338, ExElementalSpiritInfo::new, ConnectionState.IN_GAME),
	EX_ELEMENTAL_SPIRIT_EXTRACT_INFO(339, ExElementalSpiritExtractInfo::new, ConnectionState.IN_GAME),
	EX_ELEMENTAL_SPIRIT_EXTRACT(340, ExElementalSpiritExtract::new, ConnectionState.IN_GAME),
	EX_ELEMENTAL_SPIRIT_EVOLUTION_INFO(341, ExElementalSpiritEvolutionInfo::new, ConnectionState.IN_GAME),
	EX_ELEMENTAL_SPIRIT_EVOLUTION(342, ExElementalSpiritEvolution::new, ConnectionState.IN_GAME),
	EX_ELEMENTAL_SPIRIT_SET_TALENT(343, ExElementalSpiritSetTalent::new, ConnectionState.IN_GAME),
	EX_ELEMENTAL_SPIRIT_INIT_TALENT(344, ExElementalInitTalent::new, ConnectionState.IN_GAME),
	EX_ELEMENTAL_SPIRIT_ABSORB_INFO(345, ExElementalSpiritAbsorbInfo::new, ConnectionState.IN_GAME),
	EX_ELEMENTAL_SPIRIT_ABSORB(346, ExElementalSpiritAbsorb::new, ConnectionState.IN_GAME),
	EX_REQUEST_LOCKED_ITEM(347, null, ConnectionState.IN_GAME),
	EX_REQUEST_UNLOCKED_ITEM(348, null, ConnectionState.IN_GAME),
	EX_LOCKED_ITEM_CANCEL(349, null, ConnectionState.IN_GAME),
	EX_UNLOCKED_ITEM_CANCEL(350, null, ConnectionState.IN_GAME),
	EX_BLOCK_PACKET_FOR_AD(351, null, ConnectionState.IN_GAME),
	EX_USER_BAN_INFO(352, null, ConnectionState.IN_GAME),
	EX_INTERACT_MODIFY(353, ExInteractModify::new, ConnectionState.IN_GAME),
	EX_TRY_ENCHANT_ARTIFACT(354, null, ConnectionState.IN_GAME),
	EX_UPGRADE_SYSTEM_NORMAL_REQUEST(355, ExUpgradeSystemNormalRequest::new, ConnectionState.IN_GAME),
	EX_PURCHASE_LIMIT_SHOP_ITEM_LIST(356, RequestPurchaseLimitShopItemList::new, ConnectionState.IN_GAME),
	EX_PURCHASE_LIMIT_SHOP_ITEM_BUY(357, RequestPurchaseLimitShopItemBuy::new, ConnectionState.IN_GAME),
	EX_OPEN_HTML(358, ExOpenHtml::new, ConnectionState.IN_GAME),
	EX_REQUEST_CLASS_CHANGE(359, ExRequestClassChange::new, ConnectionState.IN_GAME),
	EX_REQUEST_CLASS_CHANGE_VERIFYING(360, ExRequestClassChangeVerifying::new, ConnectionState.IN_GAME),
	EX_REQUEST_TELEPORT(361, ExRequestTeleport::new, ConnectionState.IN_GAME),
	EX_COSTUME_USE_ITEM(362, null, ConnectionState.IN_GAME),
	EX_COSTUME_LIST(363, null, ConnectionState.IN_GAME),
	EX_COSTUME_COLLECTION_SKILL_ACTIVE(364, null, ConnectionState.IN_GAME),
	EX_COSTUME_EVOLUTION(365, null, ConnectionState.IN_GAME),
	EX_COSTUME_EXTRACT(366, null, ConnectionState.IN_GAME),
	EX_COSTUME_LOCK(367, null, ConnectionState.IN_GAME),
	EX_COSTUME_CHANGE_SHORTCUT(368, null, ConnectionState.IN_GAME),
	EX_MAGICLAMP_GAME_INFO(369, ExMagicLampGameInfo::new, ConnectionState.IN_GAME),
	EX_MAGICLAMP_GAME_START(370, ExMagicLampGameStart::new, ConnectionState.IN_GAME),
	EX_ACTIVATE_AUTO_SHORTCUT(371, ExRequestActivateAutoShortcut::new, ConnectionState.IN_GAME),
	EX_PREMIUM_MANAGER_LINK_HTML(372, null, ConnectionState.IN_GAME),
	EX_PREMIUM_MANAGER_PASS_CMD_TO_SERVER(373, null, ConnectionState.IN_GAME),
	EX_ACTIVATED_CURSED_TREASURE_BOX_LOCATION(374, null, ConnectionState.IN_GAME),
	EX_PAYBACK_LIST(375, ExPaybackList::new, ConnectionState.IN_GAME),
	EX_PAYBACK_GIVE_REWARD(376, ExPaybackGiveReward::new, ConnectionState.IN_GAME),
	EX_AUTOPLAY_SETTING(377, ExAutoPlaySetting::new, ConnectionState.IN_GAME),
	EX_OLYMPIAD_MATCH_MAKING(378, OlympiadMatchMaking::new, ConnectionState.IN_GAME),
	EX_OLYMPIAD_MATCH_MAKING_CANCEL(379, OlympiadMatchMakingCancel::new, ConnectionState.IN_GAME),
	EX_FESTIVAL_BM_INFO(380, ExRequestFestivalBmInfo::new, ConnectionState.IN_GAME),
	EX_FESTIVAL_BM_GAME(381, ExRequestFestivalBmGame::new, ConnectionState.IN_GAME),
	EX_GACHA_SHOP_INFO(382, null, ConnectionState.IN_GAME),
	EX_GACHA_SHOP_GACHA_GROUP(383, null, ConnectionState.IN_GAME),
	EX_GACHA_SHOP_GACHA_ITEM(384, null, ConnectionState.IN_GAME),
	EX_TIME_RESTRICT_FIELD_LIST(385, ExTimedHuntingZoneList::new, ConnectionState.IN_GAME),
	EX_TIME_RESTRICT_FIELD_USER_ENTER(386, ExTimedHuntingZoneEnter::new, ConnectionState.IN_GAME),
	EX_TIME_RESTRICT_FIELD_USER_LEAVE(387, ExTimedHuntingZoneLeave::new, ConnectionState.IN_GAME),
	EX_RANKING_CHAR_INFO(388, RequestRankingCharInfo::new, ConnectionState.IN_GAME),
	EX_RANKING_CHAR_HISTORY(389, RequestRankingCharHistory::new, ConnectionState.IN_GAME),
	EX_RANKING_CHAR_RANKERS(390, RequestRankingCharRankers::new, ConnectionState.IN_GAME),
	EX_RANKING_CHAR_SPAWN_BUFFZONE_NPC(391, RequestExRankingCharSpawnBuffzoneNpc::new, ConnectionState.IN_GAME),
	EX_RANKING_CHAR_BUFFZONE_NPC_POSITION(392, RequestExRankingCharBuffzoneNpcPosition::new, ConnectionState.IN_GAME),
	EX_PLEDGE_MERCENARY_RECRUIT_INFO_SET(393, ExPledgeMercenaryRecruitInfoSet::new, ConnectionState.IN_GAME),
	EX_MERCENARY_CASTLEWAR_CASTLE_INFO(394, ExMercenaryCastleWarCastleSiegeInfo::new, ConnectionState.IN_GAME),
	EX_MERCENARY_CASTLEWAR_CASTLE_SIEGE_INFO(395, ExMercenaryCastleWarCastleSiegeHudInfo::new, ConnectionState.IN_GAME),
	EX_MERCENARY_CASTLEWAR_CASTLE_SIEGE_ATTACKER_LIST(396, ExMercenaryCastleWarCastleSiegeAttackerList::new, ConnectionState.IN_GAME),
	EX_MERCENARY_CASTLEWAR_CASTLE_SIEGE_DEFENDER_LIST(397, ExMercenaryCastleWarCastleSiegeDefenderList::new, ConnectionState.IN_GAME),
	EX_PLEDGE_MERCENARY_MEMBER_LIST(398, ExPledgeMercenaryMemberList::new, ConnectionState.IN_GAME),
	EX_PLEDGE_MERCENARY_MEMBER_JOIN(399, ExPledgeMercenaryMemberJoin::new, ConnectionState.IN_GAME),
	EX_PVPBOOK_LIST(400, ExPvpBookList::new, ConnectionState.IN_GAME),
	EX_PVPBOOK_KILLER_LOCATION(401, null, ConnectionState.IN_GAME),
	EX_PVPBOOK_TELEPORT_TO_KILLER(402, null, ConnectionState.IN_GAME),
	EX_LETTER_COLLECTOR_TAKE_REWARD(403, ExLetterCollectorTakeReward::new, ConnectionState.IN_GAME),
	EX_SET_STATUS_BONUS(404, ExSetStatusBonus::new, ConnectionState.IN_GAME),
	EX_RESET_STATUS_BONUS(405, ExResetStatusBonus::new, ConnectionState.IN_GAME),
	EX_OLYMPIAD_MY_RANKING_INFO(406, RequestOlympiadMyRankingInfo::new, ConnectionState.IN_GAME),
	EX_OLYMPIAD_RANKING_INFO(407, RequestOlympiadRankingInfo::new, ConnectionState.IN_GAME),
	EX_OLYMPIAD_HERO_AND_LEGEND_INFO(408, RequestOlympiadHeroAndLegendInfo::new, ConnectionState.IN_GAME),
	EX_CASTLEWAR_OBSERVER_START(409, ExCastleWarObserverStart::new, ConnectionState.IN_GAME),
	EX_RAID_TELEPORT_INFO(410, RequestRaidTeleportInfo::new, ConnectionState.IN_GAME),
	EX_TELEPORT_TO_RAID_POSITION(411, ExTeleportToRaidPosition::new, ConnectionState.IN_GAME),
	EX_CRAFT_EXTRACT(412, ExRequestRandomCraftExtract::new, ConnectionState.IN_GAME),
	EX_CRAFT_RANDOM_INFO(413, ExRequestRandomCraftInfo::new, ConnectionState.IN_GAME),
	EX_CRAFT_RANDOM_LOCK_SLOT(414, ExRequestRandomCraftLockSlot::new, ConnectionState.IN_GAME),
	EX_CRAFT_RANDOM_REFRESH(415, ExRequestRandomCraftRefresh::new, ConnectionState.IN_GAME),
	EX_CRAFT_RANDOM_MAKE(416, ExRequestRandomCraftMake::new, ConnectionState.IN_GAME),
	EX_MULTI_SELL_LIST(417, RequestMultisellList::new, ConnectionState.IN_GAME),
	EX_SAVE_ITEM_ANNOUNCE_SETTING(418, ExSaveItemAnnounceSetting::new, ConnectionState.IN_GAME),
	EX_OLYMPIAD_UI(419, OlympiadUI::new, ConnectionState.IN_GAME),
	EX_SHARED_POSITION_SHARING_UI(420, ExRequestSharingLocationUi::new, ConnectionState.IN_GAME),
	EX_SHARED_POSITION_TELEPORT_UI(421, ExRequestSharedLocationTeleportUi::new, ConnectionState.IN_GAME),
	EX_SHARED_POSITION_TELEPORT(422, ExRequestSharedLocationTeleport::new, ConnectionState.IN_GAME),
	EX_AUTH_RECONNECT(423, null, ConnectionState.IN_GAME),
	EX_PET_EQUIP_ITEM(424, ExPetEquipItem::new, ConnectionState.IN_GAME),
	EX_PET_UNEQUIP_ITEM(425, ExPetUnequipItem::new, ConnectionState.IN_GAME),
	EX_SHOW_HOMUNCULUS_INFO(426, null, ConnectionState.IN_GAME),
	EX_HOMUNCULUS_CREATE_START(427, null, ConnectionState.IN_GAME),
	EX_HOMUNCULUS_INSERT(428, null, ConnectionState.IN_GAME),
	EX_HOMUNCULUS_SUMMON(429, null, ConnectionState.IN_GAME),
	EX_DELETE_HOMUNCULUS_DATA(430, null, ConnectionState.IN_GAME),
	EX_REQUEST_ACTIVATE_HOMUNCULUS(431, null, ConnectionState.IN_GAME),
	EX_HOMUNCULUS_GET_ENCHANT_POINT(432, null, ConnectionState.IN_GAME),
	EX_HOMUNCULUS_INIT_POINT(433, null, ConnectionState.IN_GAME),
	EX_EVOLVE_PET(434, ExEvolvePet::new, ConnectionState.IN_GAME),
	EX_ENCHANT_HOMUNCULUS_SKILL(435, null, ConnectionState.IN_GAME),
	EX_HOMUNCULUS_ENCHANT_EXP(436, null, ConnectionState.IN_GAME),
	EX_TELEPORT_FAVORITES_LIST(437, ExRequestTeleportFavoriteList::new, ConnectionState.IN_GAME),
	EX_TELEPORT_FAVORITES_UI_TOGGLE(438, ExRequestTeleportFavoritesUIToggle::new, ConnectionState.IN_GAME),
	EX_TELEPORT_FAVORITES_ADD_DEL(439, ExRequestTeleportFavoritesAddDel::new, ConnectionState.IN_GAME),
	EX_ANTIBOT(440, null, ConnectionState.IN_GAME),
	EX_DPSVR(441, null, ConnectionState.IN_GAME),
	EX_TENPROTECT_DECRYPT_ERROR(442, null, ConnectionState.IN_GAME),
	EX_NET_LATENCY(443, null, ConnectionState.IN_GAME),
	EX_MABLE_GAME_OPEN(444, ExRequestMableGameOpen::new, ConnectionState.IN_GAME),
	EX_MABLE_GAME_ROLL_DICE(445, ExRequestMableGameRollDice::new, ConnectionState.IN_GAME),
	EX_MABLE_GAME_POPUP_OK(446, ExRequestMableGamePopupOk::new, ConnectionState.IN_GAME),
	EX_MABLE_GAME_RESET(447, ExRequestMableGameReset::new, ConnectionState.IN_GAME),
	EX_MABLE_GAME_CLOSE(448, ExRequestMableGameClose::new, ConnectionState.IN_GAME),
	EX_RETURN_TO_ORIGIN(449, null, ConnectionState.IN_GAME),
	EX_PK_PENALTY_LIST(450, RequestExPkPenaltyList::new, ConnectionState.IN_GAME),
	EX_PK_PENALTY_LIST_ONLY_LOC(451, RequestExPkPenaltyListOnlyLoc::new, ConnectionState.IN_GAME),
	EX_BLESS_OPTION_PUT_ITEM(452, RequestBlessOptionPutItem::new, ConnectionState.IN_GAME),
	EX_BLESS_OPTION_ENCHANT(453, RequestBlessOptionEnchant::new, ConnectionState.IN_GAME),
	EX_BLESS_OPTION_CANCEL(454, RequestBlessOptionCancel::new, ConnectionState.IN_GAME),
	EX_PVP_RANKING_MY_INFO(455, RequestPvpRankingMyInfo::new, ConnectionState.IN_GAME),
	EX_PVP_RANKING_LIST(456, RequestPvpRankingList::new, ConnectionState.IN_GAME),
	EX_ACQUIRE_PET_SKILL(457, RequestExAcquirePetSkill::new, ConnectionState.IN_GAME),
	EX_PLEDGE_V3_INFO(458, RequestExPledgeV3Info::new, ConnectionState.IN_GAME),
	EX_PLEDGE_ENEMY_INFO_LIST(459, RequestExPledgeEnemyInfoList::new, ConnectionState.IN_GAME),
	EX_PLEDGE_ENEMY_REGISTER(460, RequestExPledgeEnemyRegister::new, ConnectionState.IN_GAME),
	EX_PLEDGE_ENEMY_DELETE(461, RequestExPledgeEnemyDelete::new, ConnectionState.IN_GAME),
	EX_TRY_PET_EXTRACT_SYSTEM(462, ExTryPetExtractSystem::new, ConnectionState.IN_GAME),
	EX_PLEDGE_V3_SET_ANNOUNCE(463, RequestExPledgeV3SetAnnounce::new, ConnectionState.IN_GAME),
	EX_RANKING_FESTIVAL_OPEN(464, null, ConnectionState.IN_GAME),
	EX_RANKING_FESTIVAL_BUY(465, null, ConnectionState.IN_GAME),
	EX_RANKING_FESTIVAL_BONUS(466, null, ConnectionState.IN_GAME),
	EX_RANKING_FESTIVAL_RANKING(467, null, ConnectionState.IN_GAME),
	EX_RANKING_FESTIVAL_MY_RECEIVED_BONUS(468, null, ConnectionState.IN_GAME),
	EX_RANKING_FESTIVAL_REWARD(469, null, ConnectionState.IN_GAME),
	EX_TIMER_CHECK(470, null, ConnectionState.IN_GAME),
	EX_STEADY_BOX_LOAD(471, RequestSteadyBoxLoad::new, ConnectionState.IN_GAME),
	EX_STEADY_OPEN_SLOT(472, RequestSteadyOpenSlot::new, ConnectionState.IN_GAME),
	EX_STEADY_OPEN_BOX(473, RequestSteadyOpenBox::new, ConnectionState.IN_GAME),
	EX_STEADY_GET_REWARD(474, RequestSteadyGetReward::new, ConnectionState.IN_GAME),
	EX_PET_RANKING_MY_INFO(475, RequestPetRankingMyInfo::new, ConnectionState.IN_GAME),
	EX_PET_RANKING_LIST(476, RequestPetRankingList::new, ConnectionState.IN_GAME),
	EX_COLLECTION_OPEN_UI(477, RequestExCollectionOpenUI::new, ConnectionState.IN_GAME),
	EX_COLLECTION_CLOSE_UI(478, RequestCollectionCloseUI::new, ConnectionState.IN_GAME),
	EX_COLLECTION_LIST(479, RequestExCollectionList::new, ConnectionState.IN_GAME),
	EX_COLLECTION_UPDATE_FAVORITE(480, RequestCollectionUpdateFavorite::new, ConnectionState.IN_GAME),
	EX_COLLECTION_FAVORITE_LIST(481, RequestCollectionFavoriteList::new, ConnectionState.IN_GAME),
	EX_COLLECTION_SUMMARY(482, RequestExCollectionSummary::new, ConnectionState.IN_GAME),
	EX_COLLECTION_REGISTER(483, RequestCollectionRegister::new, ConnectionState.IN_GAME),
	EX_COLLECTION_RECEIVE_REWARD(484, RequestCollectionReceiveReward::new, ConnectionState.IN_GAME),
	EX_PVPBOOK_SHARE_REVENGE_LIST(485, RequestExPvpBookShareRevengeList::new, ConnectionState.IN_GAME),
	EX_PVPBOOK_SHARE_REVENGE_REQ_SHARE_REVENGEINFO(486, RequestExPvpBookShareRevengeReqShareRevengeInfo::new, ConnectionState.IN_GAME),
	EX_PVPBOOK_SHARE_REVENGE_KILLER_LOCATION(487, RequestExPvpBookShareRevengeKillerLocation::new, ConnectionState.IN_GAME),
	EX_PVPBOOK_SHARE_REVENGE_TELEPORT_TO_KILLER(488, RequestExPvpBookShareRevengeTeleportToKiller::new, ConnectionState.IN_GAME),
	EX_PVPBOOK_SHARE_REVENGE_SHARED_TELEPORT_TO_KILLER(489, RequestExPvpBookShareRevengeSharedTeleportToKiller::new, ConnectionState.IN_GAME),
	EX_PENALTY_ITEM_LIST(490, ExRequestPenaltyItemList::new, ConnectionState.IN_GAME),
	EX_PENALTY_ITEM_RESTORE(491, ExRequestPenaltyItemRestore::new, ConnectionState.IN_GAME),
	EX_USER_WATCHER_TARGET_LIST(492, ExRequestUserWatcherTargetList::new, ConnectionState.IN_GAME),
	EX_USER_WATCHER_ADD(493, ExRequestUserWatcherAdd::new, ConnectionState.IN_GAME),
	EX_USER_WATCHER_DELETE(494, ExRequestUserWatcherDelete::new, ConnectionState.IN_GAME),
	EX_HOMUNCULUS_ACTIVATE_SLOT(495, null, ConnectionState.IN_GAME),
	EX_SUMMON_HOMUNCULUS_COUPON(496, null, ConnectionState.IN_GAME),
	EX_SUBJUGATION_LIST(497, RequestSubjugationList::new, ConnectionState.IN_GAME),
	EX_SUBJUGATION_RANKING(498, RequestSubjugationRanking::new, ConnectionState.IN_GAME),
	EX_SUBJUGATION_GACHA_UI(499, RequestSubjugationGachaUI::new, ConnectionState.IN_GAME),
	EX_SUBJUGATION_GACHA(500, RequestSubjugationGacha::new, ConnectionState.IN_GAME),
	EX_PLEDGE_DONATION_INFO(501, RequestExPledgeDonationInfo::new, ConnectionState.IN_GAME),
	EX_PLEDGE_DONATION_REQUEST(502, RequestExPledgeDonationRequest::new, ConnectionState.IN_GAME),
	EX_PLEDGE_CONTRIBUTION_LIST(503, RequestExPledgeContributionList::new, ConnectionState.IN_GAME),
	EX_PLEDGE_RANKING_MY_INFO(504, RequestPledgeRankingMyInfo::new, ConnectionState.IN_GAME),
	EX_PLEDGE_RANKING_LIST(505, RequestPledgeRankingList::new, ConnectionState.IN_GAME),
	EX_ITEM_RESTORE_LIST(506, RequestItemRestoreList::new, ConnectionState.IN_GAME),
	EX_ITEM_RESTORE(507, RequestItemRestore::new, ConnectionState.IN_GAME),
	EX_DETHRONE_INFO(508, null, ConnectionState.IN_GAME),
	EX_DETHRONE_RANKING_INFO(509, null, ConnectionState.IN_GAME),
	EX_DETHRONE_SERVER_INFO(510, null, ConnectionState.IN_GAME),
	EX_DETHRONE_DISTRICT_OCCUPATION_INFO(511, null, ConnectionState.IN_GAME),
	EX_DETHRONE_DAILY_MISSION_INFO(512, null, ConnectionState.IN_GAME),
	EX_DETHRONE_DAILY_MISSION_GET_REWARD(513, null, ConnectionState.IN_GAME),
	EX_DETHRONE_PREV_SEASON_INFO(514, null, ConnectionState.IN_GAME),
	EX_DETHRONE_GET_REWARD(515, null, ConnectionState.IN_GAME),
	EX_DETHRONE_ENTER(516, null, ConnectionState.IN_GAME),
	EX_DETHRONE_LEAVE(517, null, ConnectionState.IN_GAME),
	EX_DETHRONE_CHECK_NAME(518, null, ConnectionState.IN_GAME),
	EX_DETHRONE_CHANGE_NAME(519, null, ConnectionState.IN_GAME),
	EX_DETHRONE_CONNECT_CASTLE(520, null, ConnectionState.IN_GAME),
	EX_DETHRONE_DISCONNECT_CASTLE(521, null, ConnectionState.IN_GAME),
	EX_CHANGE_NICKNAME_COLOR_ICON(522, RequestChangeNicknameEmote::new, ConnectionState.IN_GAME),
	EX_WORLDCASTLEWAR_MOVE_TO_HOST(523, null, ConnectionState.IN_GAME),
	EX_WORLDCASTLEWAR_RETURN_TO_ORIGIN_PEER(524, null, ConnectionState.IN_GAME),
	EX_WORLDCASTLEWAR_CASTLE_INFO(525, null, ConnectionState.IN_GAME),
	EX_WORLDCASTLEWAR_CASTLE_SIEGE_INFO(526, null, ConnectionState.IN_GAME),
	EX_WORLDCASTLEWAR_CASTLE_SIEGE_JOIN(527, null, ConnectionState.IN_GAME),
	EX_WORLDCASTLEWAR_CASTLE_SIEGE_ATTACKER_LIST(528, null, ConnectionState.IN_GAME),
	EX_WORLDCASTLEWAR_PLEDGE_MERCENARY_RECRUIT_INFO_SET(529, null, ConnectionState.IN_GAME),
	EX_WORLDCASTLEWAR_PLEDGE_MERCENARY_MEMBER_LIST(530, null, ConnectionState.IN_GAME),
	EX_WORLDCASTLEWAR_PLEDGE_MERCENARY_MEMBER_JOIN(531, null, ConnectionState.IN_GAME),
	EX_WORLDCASTLEWAR_TELEPORT(532, null, ConnectionState.IN_GAME),
	EX_WORLDCASTLEWAR_OBSERVER_START(533, null, ConnectionState.IN_GAME),
	EX_PRIVATE_STORE_SEARCH_LIST(534, ExRequestPrivateStoreSearchList::new, ConnectionState.IN_GAME),
	EX_PRIVATE_STORE_SEARCH_STATISTICS(535, ExRequestPrivateStoreSearchStatistics::new, ConnectionState.IN_GAME),
	EX_WORLDCASTLEWAR_HOST_CASTLE_SIEGE_RANKING_INFO(536, null, ConnectionState.IN_GAME),
	EX_WORLDCASTLEWAR_CASTLE_SIEGE_RANKING_INFO(537, null, ConnectionState.IN_GAME),
	EX_WORLDCASTLEWAR_SIEGE_MAINBATTLE_HUD_INFO(538, null, ConnectionState.IN_GAME),
	EX_NEW_HENNA_LIST(539, RequestNewHennaList::new, ConnectionState.IN_GAME),
	EX_NEW_HENNA_EQUIP(540, RequestNewHennaEquip::new, ConnectionState.IN_GAME),
	EX_NEW_HENNA_UNEQUIP(541, RequestNewHennaUnequip::new, ConnectionState.IN_GAME),
	EX_NEW_HENNA_POTEN_SELECT(542, RequestNewHennaPotenSelect::new, ConnectionState.IN_GAME),
	EX_NEW_HENNA_POTEN_ENCHANT(543, RequestNewHennaPotenEnchant::new, ConnectionState.IN_GAME),
	EX_NEW_HENNA_COMPOSE(544, RequestNewHennaCompose::new, ConnectionState.IN_GAME),
	EX_REQUEST_INVITE_PARTY(545, RequestNewInvitePartyInquiry::new, ConnectionState.IN_GAME),
	EX_ITEM_USABLE_LIST(546, null, ConnectionState.IN_GAME),
	EX_PACKETREADCOUNTPERSECOND(547, null, ConnectionState.IN_GAME),
	EX_SELECT_GLOBAL_EVENT_UI(548, null, ConnectionState.IN_GAME),
	EX_L2PASS_INFO(549, RequestHuntPassInfo::new, ConnectionState.IN_GAME),
	EX_L2PASS_REQUEST_REWARD(550, RequestHuntPassReward::new, ConnectionState.IN_GAME),
	EX_L2PASS_REQUEST_REWARD_ALL(551, RequestHuntPassRewardAll::new, ConnectionState.IN_GAME),
	EX_L2PASS_BUY_PREMIUM(552, RequestHuntPassBuyPremium::new, ConnectionState.IN_GAME),
	EX_SAYHAS_SUPPORT_TOGGLE(553, HuntpassSayhasToggle::new, ConnectionState.IN_GAME),
	EX_REQ_ENCHANT_FAIL_REWARD_INFO(554, ExRequestEnchantFailRewardInfo::new, ConnectionState.IN_GAME),
	EX_SET_ENCHANT_CHALLENGE_POINT(555, ExRequestSetEnchantChallengePoint::new, ConnectionState.IN_GAME),
	EX_RESET_ENCHANT_CHALLENGE_POINT(556, ExRequestResetEnchantChallengePoint::new, ConnectionState.IN_GAME),
	EX_REQ_VIEW_ENCHANT_RESULT(557, ExRequestViewEnchantResult::new, ConnectionState.IN_GAME),
	EX_REQ_START_MULTI_ENCHANT_SCROLL(558, ExRequestStartMultiEnchantScroll::new, ConnectionState.IN_GAME),
	EX_REQ_VIEW_MULTI_ENCHANT_RESULT(559, ExRequestViewMultiEnchantResult::new, ConnectionState.IN_GAME),
	EX_REQ_FINISH_MULTI_ENCHANT_SCROLL(560, ExRequestFinishMultiEnchantScroll::new, ConnectionState.IN_GAME),
	EX_REQ_CHANGE_MULTI_ENCHANT_SCROLL(561, null, ConnectionState.IN_GAME),
	EX_REQ_SET_MULTI_ENCHANT_ITEM_LIST(562, ExRequestSetMultiEnchantItemList::new, ConnectionState.IN_GAME),
	EX_REQ_MULTI_ENCHANT_ITEM_LIST(563, ExRequestMultiEnchantItemList::new, ConnectionState.IN_GAME),
	EX_WORLDCASTLEWAR_SUPPORT_PLEDGE_FLAG_SET(564, null, ConnectionState.IN_GAME),
	EX_WORLDCASTLEWAR_SUPPORT_PLEDGE_INFO_SET(565, null, ConnectionState.IN_GAME),
	EX_REQ_HOMUNCULUS_PROB_LIST(566, null, ConnectionState.IN_GAME),
	EX_WORLDCASTLEWAR_HOST_CASTLE_SIEGE_ALL_RANKING_INFO(567, null, ConnectionState.IN_GAME),
	EX_WORLDCASTLEWAR_CASTLE_SIEGE_ALL_RANKING_INFO(568, null, ConnectionState.IN_GAME),
	EX_MISSION_LEVEL_REWARD_LIST(569, RequestMissionRewardList::new, ConnectionState.IN_GAME),
	EX_MISSION_LEVEL_RECEIVE_REWARD(570, RequestMissionLevelReceiveReward::new, ConnectionState.IN_GAME),
	EX_MISSION_LEVEL_JUMP_LEVEL(571, RequestMissionLevelJumpLevel::new, ConnectionState.IN_GAME),
	EX_BALROGWAR_TELEPORT(572, ExBalrogWarTeleport::new, ConnectionState.IN_GAME),
	EX_BALROGWAR_SHOW_UI(573, ExBalrogWarShowUI::new, ConnectionState.IN_GAME),
	EX_BALROGWAR_SHOW_RANKING(574, ExBalrogWarShowRanking::new, ConnectionState.IN_GAME),
	EX_BALROGWAR_GET_REWARD(575, ExBalrogWarGetReward::new, ConnectionState.IN_GAME),
	EX_USER_RESTART_LOCKER_UPDATE(576, null, ConnectionState.IN_GAME),
	EX_WORLD_EXCHANGE_ITEM_LIST(577, ExWorldExchangeItemList::new, ConnectionState.IN_GAME),
	EX_WORLD_EXCHANGE_REGI_ITEM(578, ExWorldExchangeRegisterItem::new, ConnectionState.IN_GAME),
	EX_WORLD_EXCHANGE_BUY_ITEM(579, ExWorldExchangeBuyItem::new, ConnectionState.IN_GAME),
	EX_WORLD_EXCHANGE_SETTLE_LIST(580, ExWorldExchangeSettleList::new, ConnectionState.IN_GAME),
	EX_WORLD_EXCHANGE_SETTLE_RECV_RESULT(581, ExWorldExchangeSettleRecvResult::new, ConnectionState.IN_GAME),
	EX_READY_ITEM_AUTO_PEEL(582, ExRequestReadyItemAutoPeel::new, ConnectionState.IN_GAME),
	EX_REQUEST_ITEM_AUTO_PEEL(583, ExRequestItemAutoPeel::new, ConnectionState.IN_GAME),
	EX_STOP_ITEM_AUTO_PEEL(584, ExRequestStopItemAutoPeel::new, ConnectionState.IN_GAME),
	EX_VARIATION_OPEN_UI(585, ExVariationOpenUi::new, ConnectionState.IN_GAME),
	EX_VARIATION_CLOSE_UI(586, ExVariationCloseUi::new, ConnectionState.IN_GAME),
	EX_APPLY_VARIATION_OPTION(587, ExApplyVariationOption::new, ConnectionState.IN_GAME),
	EX_REQUEST_AUDIO_LOG_SAVE(588, null, ConnectionState.IN_GAME),
	EX_BR_VERSION(589, RequestBRVersion::new, ConnectionState.AUTHENTICATED, ConnectionState.CONNECTED),
	EX_WRANKING_FESTIVAL_INFO(590, null, ConnectionState.IN_GAME),
	EX_WRANKING_FESTIVAL_OPEN(591, null, ConnectionState.IN_GAME),
	EX_WRANKING_FESTIVAL_BUY(592, null, ConnectionState.IN_GAME),
	EX_WRANKING_FESTIVAL_BONUS(593, null, ConnectionState.IN_GAME),
	EX_WRANKING_FESTIVAL_RANKING(594, null, ConnectionState.IN_GAME),
	EX_WRANKING_FESTIVAL_MY_RECEIVED_BONUS(595, null, ConnectionState.IN_GAME),
	EX_WRANKING_FESTIVAL_REWARD(596, null, ConnectionState.IN_GAME),
	EX_HENNA_UNEQUIP_INFO(597, RequestNewHennaUnequipInfo::new, ConnectionState.IN_GAME),
	EX_HERO_BOOK_CHARGE(598, null, ConnectionState.IN_GAME),
	EX_HERO_BOOK_ENCHANT(599, null, ConnectionState.IN_GAME),
	EX_HERO_BOOK_CHARGE_PROB(600, null, ConnectionState.IN_GAME),
	EX_TELEPORT_UI(601, RequestExTeleportUI::new, ConnectionState.IN_GAME),
	EX_GOODS_GIFT_LIST_INFO(602, null, ConnectionState.IN_GAME),
	EX_GOODS_GIFT_ACCEPT(603, null, ConnectionState.IN_GAME),
	EX_GOODS_GIFT_REFUSE(604, null, ConnectionState.IN_GAME),
	EX_WORLD_EXCHANGE_AVERAGE_PRICE(605, ExWorldExchangeAveragePrice::new, ConnectionState.IN_GAME),
	EX_WORLD_EXCHANGE_TOTAL_LIST(606, ExWorldExchangeTotalList::new, ConnectionState.IN_GAME),
	EX_PRISON_USER_INFO(607, RequestPrisonUserInfo::new, ConnectionState.IN_GAME),
	EX_PRISON_USER_DONATION(608, RequestPrisonUserDonation::new, ConnectionState.IN_GAME),
	EX_TRADE_LIMIT_INFO(609, null, ConnectionState.IN_GAME),
	EX_UNIQUE_GACHA_OPEN(610, ExUniqueGachaOpen::new, ConnectionState.IN_GAME),
	EX_UNIQUE_GACHA_GAME(611, ExUniqueGachaGame::new, ConnectionState.IN_GAME),
	EX_UNIQUE_GACHA_INVEN_ITEM_LIST(612, ExUniqueGachaInvenItemList::new, ConnectionState.IN_GAME),
	EX_UNIQUE_GACHA_INVEN_GET_ITEM(613, ExUniqueGachaInvenGetItem::new, ConnectionState.IN_GAME),
	EX_UNIQUE_GACHA_HISTORY(614, ExUniqueGachaHistory::new, ConnectionState.IN_GAME),
	EX_SET_PLEDGE_CREST_PRESET(615, RequestSetPledgeCrestPreset::new, ConnectionState.IN_GAME),
	EX_GET_PLEDGE_CREST_PRESET(616, RequestGetPledgeCrestPreset::new, ConnectionState.IN_GAME),
	EX_DUAL_INVENTORY_SWAP(617, RequestExDualInventorySwap::new, ConnectionState.IN_GAME),
	EX_SP_EXTRACT_INFO(618, RequestExSpExtractInfo::new, ConnectionState.IN_GAME),
	EX_SP_EXTRACT_ITEM(619, RequestExSpExtractItem::new, ConnectionState.IN_GAME),
	EX_QUEST_TELEPORT(620, RequestExQuestTeleport::new, ConnectionState.IN_GAME),
	EX_QUEST_ACCEPT(621, RequestExQuestAccept::new, ConnectionState.IN_GAME),
	EX_QUEST_CANCEL(622, RequestExQuestCancel::new, ConnectionState.IN_GAME),
	EX_QUEST_COMPLETE(623, RequestExQuestComplete::new, ConnectionState.IN_GAME),
	EX_QUEST_NOTIFICATION_ALL(624, RequestExQuestNotificationAll::new, ConnectionState.IN_GAME),
	EX_QUEST_UI(625, RequestExQuestUI::new, ConnectionState.IN_GAME),
	EX_QUEST_ACCEPTABLE_LIST(626, RequestExQuestAcceptableList::new, ConnectionState.IN_GAME),
	EX_SKILL_ENCHANT_INFO(627, RequestExSkillEnchantInfo::new, ConnectionState.IN_GAME),
	EX_SKILL_ENCHANT_CHARGE(628, RequestExSkillEnchantCharge::new, ConnectionState.IN_GAME),
	EX_TIME_RESTRICT_FIELD_HOST_USER_ENTER(629, null, ConnectionState.IN_GAME),
	EX_TIME_RESTRICT_FIELD_HOST_USER_LEAVE(630, null, ConnectionState.IN_GAME),
	EX_DETHRONE_SHOP_OPEN_UI(631, null, ConnectionState.IN_GAME),
	EX_DETHRONE_SHOP_BUY(632, null, ConnectionState.IN_GAME),
	EX_ENHANCED_ABILITY_OF_FIRE_OPEN_UI(633, null, ConnectionState.IN_GAME),
	EX_ENHANCED_ABILITY_OF_FIRE_INIT(634, null, ConnectionState.IN_GAME),
	EX_ENHANCED_ABILITY_OF_FIRE_EXP_UP(635, null, ConnectionState.IN_GAME),
	EX_ENHANCED_ABILITY_OF_FIRE_LEVEL_UP(636, null, ConnectionState.IN_GAME),
	EX_HOLY_FIRE_OPEN_UI(637, null, ConnectionState.IN_GAME),
	EX_PRIVATE_STORE_BUY_SELL(638, null, ConnectionState.IN_GAME),
	EX_VIP_ATTENDANCE_LIST(639, RequestVipAttendanceItemList::new, ConnectionState.IN_GAME),
	EX_VIP_ATTENDANCE_CHECK(640, RequestVipAttendanceCheck::new, ConnectionState.IN_GAME),
	EX_VIP_ATTENDANCE_REWARD(641, RequestVipAttendanceItemReward::new, ConnectionState.IN_GAME),
	EX_CHANGE_ABILITY_PRESET(642, null, ConnectionState.IN_GAME),
	EX_NEW_HENNA_POTEN_ENCHANT_RESET(643, ExRequestNewHennaEnchantReset::new, ConnectionState.IN_GAME),
	EX_INZONE_RANKING_MY_INFO(644, null, ConnectionState.IN_GAME),
	EX_INZONE_RANKING_LIST(645, null, ConnectionState.IN_GAME),
	EX_TIME_RESTRICT_FIELD_HOST_USER_ENTER_BY_NPC(646, null, ConnectionState.IN_GAME),
	EX_PREPARE_LOGIN(647, null, ConnectionState.IN_GAME),
	EX_RELICS_OPEN_UI(648, RequestRelicsOpenUI::new, ConnectionState.IN_GAME),
	EX_RELICS_CLOSE_UI(649, RequestRelicsCloseUI::new, ConnectionState.IN_GAME),
	EX_RELICS_SUMMON_CLOSE_UI(650, RequestRelicsSummonCloseUI::new, ConnectionState.IN_GAME),
	EX_RELICS_ACTIVE(651, RequestRelicsActive::new, ConnectionState.IN_GAME),
	EX_RELICS_SUMMON(652, RequestRelicsSummon::new, ConnectionState.IN_GAME),
	EX_RELICS_EXCHANGE(653, null, ConnectionState.IN_GAME),
	EX_RELICS_EXCHANGE_CONFIRM(654, null, ConnectionState.IN_GAME),
	EX_RELICS_UPGRADE(655, RequestRelicsUpgrade::new, ConnectionState.IN_GAME),
	EX_RELICS_COMBINATION(656, RequestRelicsCombination::new, ConnectionState.IN_GAME),
	EX_SERVERWAR_FIELD_ENTER_USER_INFO(657, null, ConnectionState.IN_GAME),
	EX_SERVERWAR_MOVE_TO_HOST(658, null, ConnectionState.IN_GAME),
	EX_SERVERWAR_BATTLE_HUD_INFO(659, null, ConnectionState.IN_GAME),
	EX_SERVERWAR_LEADER_LIST(660, null, ConnectionState.IN_GAME),
	EX_SERVERWAR_SELECT_LEADER(661, null, ConnectionState.IN_GAME),
	EX_SERVERWAR_SELECT_LEADER_INFO(662, null, ConnectionState.IN_GAME),
	EX_SERVERWAR_MOVE_TO_LEADER_CAMP(663, null, ConnectionState.IN_GAME),
	EX_SERVERWAR_REWARD_ITEM_INFO(664, null, ConnectionState.IN_GAME),
	EX_SERVERWAR_REWARD_INFO(665, null, ConnectionState.IN_GAME),
	EX_SERVERWAR_GET_REWARD(666, null, ConnectionState.IN_GAME),
	EX_RELICS_COMBINATION_COMPLETE(667, RequestRelicsCombinationComplete::new, ConnectionState.IN_GAME),
	EX_VIRTUALITEM_SYSTEM(668, null, ConnectionState.IN_GAME),
	EX_CROSS_EVENT_DATA(669, RequestCrossEventData::new, ConnectionState.IN_GAME),
	EX_CROSS_EVENT_INFO(670, RequestCrossEventInfo::new, ConnectionState.IN_GAME),
	EX_CROSS_EVENT_NORMAL_REWARD(671, RequestCrossEventNormalReward::new, ConnectionState.IN_GAME),
	EX_CROSS_EVENT_RARE_REWARD(672, RequestCrossEventRareReward::new, ConnectionState.IN_GAME),
	EX_CROSS_EVENT_RESET(673, RequestCrossEventReset::new, ConnectionState.IN_GAME),
	EX_ADENLAB_BOSS_LIST(674, RequestAdenLabBossList::new, ConnectionState.IN_GAME),
	EX_ADENLAB_UNLOCK_BOSS(675, RequestAdenLabBossUnlock::new, ConnectionState.IN_GAME),
	EX_ADENLAB_BOSS_INFO(676, RequestAdenLabBossInfo::new, ConnectionState.IN_GAME),
	EX_ADENLAB_NORMAL_SLOT(677, RequestAdenLabNormalSlot::new, ConnectionState.IN_GAME),
	EX_ADENLAB_NORMAL_PLAY(678, RequestAdenLabNormalPlay::new, ConnectionState.IN_GAME),
	EX_ADENLAB_SPECIAL_SLOT(679, RequestAdenLabSpecialSlot::new, ConnectionState.IN_GAME),
	EX_ADENLAB_SPECIAL_PROB(680, RequestAdenLabSpecialProbability::new, ConnectionState.IN_GAME),
	EX_ADENLAB_SPECIAL_PLAY(681, RequestAdenLabSpecialPlay::new, ConnectionState.IN_GAME),
	EX_ADENLAB_SPECIAL_FIX(682, RequestAdenLabSpecialFix::new, ConnectionState.IN_GAME),
	EX_ADENLAB_TRANSCEND_ENCHANT(683, RequestAdenLabTranscendentEnchant::new, ConnectionState.IN_GAME),
	EX_ADENLAB_TRANSCEND_PROB(684, RequestAdenLabTranscendentProbability::new, ConnectionState.IN_GAME),
	EX_HOMUNCULUS_EVOLVE(685, null, ConnectionState.IN_GAME),
	EX_EXTRACT_SKILL_ENCHANT(686, RequestExtractSkillEnchant::new, ConnectionState.IN_GAME),
	EX_REQUEST_SKILL_ENCHANT_CONFIRM(687, RequestSkillEnchantConfirm::new, ConnectionState.IN_GAME),
	EX_CREATE_ITEM_PROB_LIST(688, RequestCreateItemProbList::new, ConnectionState.IN_GAME),
	EX_CRAFT_SLOT_PROB_LIST(689, RequestCreateSlotProbList::new, ConnectionState.IN_GAME),
	EX_NEW_HENNA_COMPOSE_PROB_LIST(690, RequestNewHennaComposeProbList::new, ConnectionState.IN_GAME),
	EX_VARIATION_PROB_LIST(691, RequestVariationProbList::new, ConnectionState.IN_GAME),
	EX_RELICS_PROB_LIST(692, RequestRelicsProbList::new, ConnectionState.IN_GAME),
	EX_UPGRADE_SYSTEM_PROB_LIST(693, RequestUpgradeSystemProbList::new, ConnectionState.IN_GAME),
	EX_COMBINATION_PROB_LIST(694, RequestCombinationProbList::new, ConnectionState.IN_GAME),
	EX_RELICS_ID_SUMMON(695, RequestRelicsIdSummon::new, ConnectionState.IN_GAME),
	EX_RELICS_SUMMON_LIST(696, RequestRelicsSummonList::new, ConnectionState.IN_GAME),
	EX_RELICS_CONFIRM_COMBINATION(697, RequestRelicsCombination::new, ConnectionState.IN_GAME),
	EX_NEW_HENNA_POTEN_OPENSLOT_PROB_INFO(698, RequestNewHennaPotenOpenslotProbInfo::new, ConnectionState.IN_GAME),
	EX_NEW_HENNA_POTEN_OPENSLOT(699, RequestNewHennaPotenOpenslot::new, ConnectionState.IN_GAME),
	EX_DYEEFFECT_LIST(700, null, ConnectionState.IN_GAME),
	EX_DYEEFFECT_ENCHANT_PROB_INFO(701, null, ConnectionState.IN_GAME),
	EX_DYEEFFECT_ENCHANT_NORMALSKILL(702, null, ConnectionState.IN_GAME),
	EX_DYEEFFECT_ACQUIRE_HIDDENSKILL(703, null, ConnectionState.IN_GAME),
	EX_DYEEFFECT_ENCHANT_RESET(704, null, ConnectionState.IN_GAME),
	EX_LOAD_PET_PREVIEW_BY_SID(705, null, ConnectionState.IN_GAME),
	EX_LOAD_PET_PREVIEW_BY_DBID(706, null, ConnectionState.IN_GAME),
	EX_CHECK_CLIENT_INFO(707, null, ConnectionState.IN_GAME),
	EX_MATCHINGINZONE_FIELD_ENTER_USER_INFO(708, null, ConnectionState.IN_GAME),
	EX_RAID_AUCTION_BID(709, null, ConnectionState.IN_GAME),
	EX_RAID_AUCTION_CANCEL_BID(710, null, ConnectionState.IN_GAME),
	EX_RAID_AUCTION_POST_LIST(711, null, ConnectionState.IN_GAME),
	EX_RAID_AUCTION_POST_RECEIVE(712, null, ConnectionState.IN_GAME),
	EX_RAID_AUCTION_POST_RECEIVE_ALL(713, null, ConnectionState.IN_GAME),
	EX_REPAIR_ALL_EQUIPMENT(714, null, ConnectionState.IN_GAME),
	EX_CLASS_CHANGE(715, null, ConnectionState.IN_GAME),
	EX_CHAT_BAN_START(716, null, ConnectionState.IN_GAME),
	EX_CHAT_BAN_END(717, null, ConnectionState.IN_GAME),
	EX_BLESS_OPTION_PROB_LIST(718, RequestBlessOptionProbList::new, ConnectionState.IN_GAME),
	EX_CHARACTER_STYLE_LIST(719, ExRequestCharacterStyleList::new, ConnectionState.IN_GAME),
	EX_CHARACTER_STYLE_REGIST(720, ExRequestCharacterStyleRegister::new, ConnectionState.IN_GAME),
	EX_CHARACTER_STYLE_SELECT(721, ExRequestCharacterStyleSelect::new, ConnectionState.IN_GAME),
	EX_CHARACTER_STYLE_UPDATE_FAVORITE(722, ExRequestCharacterStyleUpdateFavorite::new, ConnectionState.IN_GAME),
	EX_PURCHASE_LIMIT_CRAFT_ITEM(723, RequestPurchaseLimitCraftItem::new, ConnectionState.IN_GAME),
	EX_PARTY_ROOM_ANNOUNCE(724, RequestPartyMatchingAnnounce::new, ConnectionState.IN_GAME),
	EX_CHARACTER_STYLE_UNREGIST(725, null, ConnectionState.IN_GAME),
	EX_UPGRADE_PROB(726, RequestUpgradeProb::new, ConnectionState.IN_GAME),
	EX_ALLIANCE_CREATE(727, AllyCreate::new, ConnectionState.IN_GAME),
	EX_MAX(728, null, ConnectionState.IN_GAME);

	public static final ExClientPackets[] PACKET_ARRAY;
	private final int _packetId;
	private final Supplier<ClientPacket> _packetSupplier;
	private final Set<ConnectionState> _connectionStates;

	private ExClientPackets(int packetId, Supplier<ClientPacket> packetSupplier, ConnectionState... connectionStates)
	{
		if (packetId > 65535)
		{
			throw new IllegalArgumentException("Packet id must not be bigger than 0xFFFF");
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
		if (DevelopmentConfig.DEBUG_EX_CLIENT_PACKETS)
		{
			if (packet != null)
			{
				String name = packet.getClass().getSimpleName();
				if (!DevelopmentConfig.EXCLUDED_DEBUG_PACKETS.contains(name))
				{
					PacketLogger.info("[C EX] " + name);
				}
			}
			else if (DevelopmentConfig.DEBUG_UNKNOWN_PACKETS)
			{
				PacketLogger.info("[C EX] 0x" + Integer.toHexString(this._packetId).toUpperCase());
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
		int maxPacketId = Arrays.stream(values()).mapToInt(ExClientPackets::getPacketId).max().orElse(0);
		PACKET_ARRAY = new ExClientPackets[maxPacketId + 1];

		for (ExClientPackets packet : values())
		{
			PACKET_ARRAY[packet.getPacketId()] = packet;
		}
	}
}
