package net.sf.l2jdev.gameserver.model.actor;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.LoginServerThread;
import net.sf.l2jdev.gameserver.ai.CreatureAI;
import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.ai.PlayerAI;
import net.sf.l2jdev.gameserver.ai.SummonAI;
import net.sf.l2jdev.gameserver.cache.RelationCache;
import net.sf.l2jdev.gameserver.communitybbs.BB.Forum;
import net.sf.l2jdev.gameserver.communitybbs.Manager.ForumsBBSManager;
import net.sf.l2jdev.gameserver.config.AchievementBoxConfig;
import net.sf.l2jdev.gameserver.config.AdenLaboratoryConfig;
import net.sf.l2jdev.gameserver.config.AttendanceRewardsConfig;
import net.sf.l2jdev.gameserver.config.FeatureConfig;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.HuntPassConfig;
import net.sf.l2jdev.gameserver.config.OlympiadConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.config.PrisonConfig;
import net.sf.l2jdev.gameserver.config.PvpConfig;
import net.sf.l2jdev.gameserver.config.RatesConfig;
import net.sf.l2jdev.gameserver.config.RelicSystemConfig;
import net.sf.l2jdev.gameserver.config.custom.DualboxCheckConfig;
import net.sf.l2jdev.gameserver.config.custom.FactionSystemConfig;
import net.sf.l2jdev.gameserver.config.custom.FakePlayersConfig;
import net.sf.l2jdev.gameserver.config.custom.FreeMountsConfig;
import net.sf.l2jdev.gameserver.config.custom.MultilingualSupportConfig;
import net.sf.l2jdev.gameserver.config.custom.OfflinePlayConfig;
import net.sf.l2jdev.gameserver.config.custom.OfflineTradeConfig;
import net.sf.l2jdev.gameserver.config.custom.PremiumSystemConfig;
import net.sf.l2jdev.gameserver.config.custom.PrivateStoreRangeConfig;
import net.sf.l2jdev.gameserver.config.custom.PvpAnnounceConfig;
import net.sf.l2jdev.gameserver.config.custom.PvpRewardItemConfig;
import net.sf.l2jdev.gameserver.config.custom.PvpTitleColorConfig;
import net.sf.l2jdev.gameserver.data.enums.CategoryType;
import net.sf.l2jdev.gameserver.data.enums.CharacterStyleCategoryType;
import net.sf.l2jdev.gameserver.data.enums.EvolveLevel;
import net.sf.l2jdev.gameserver.data.holders.CharacterStyleDataHolder;
import net.sf.l2jdev.gameserver.data.holders.CollectionDataHolder;
import net.sf.l2jdev.gameserver.data.holders.PreparedMultisellListHolder;
import net.sf.l2jdev.gameserver.data.holders.RelicCollectionDataHolder;
import net.sf.l2jdev.gameserver.data.holders.RelicDataHolder;
import net.sf.l2jdev.gameserver.data.holders.SellBuffHolder;
import net.sf.l2jdev.gameserver.data.holders.TimedHuntingZoneHolder;
import net.sf.l2jdev.gameserver.data.holders.TrainingHolder;
import net.sf.l2jdev.gameserver.data.sql.CharInfoTable;
import net.sf.l2jdev.gameserver.data.sql.CharSummonTable;
import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.data.sql.OfflinePlayTable;
import net.sf.l2jdev.gameserver.data.sql.OfflineTraderTable;
import net.sf.l2jdev.gameserver.data.xml.AdminData;
import net.sf.l2jdev.gameserver.data.xml.AttendanceRewardData;
import net.sf.l2jdev.gameserver.data.xml.CategoryData;
import net.sf.l2jdev.gameserver.data.xml.CharacterStylesData;
import net.sf.l2jdev.gameserver.data.xml.ClassListData;
import net.sf.l2jdev.gameserver.data.xml.CollectionData;
import net.sf.l2jdev.gameserver.data.xml.ElementalSpiritData;
import net.sf.l2jdev.gameserver.data.xml.ExperienceData;
import net.sf.l2jdev.gameserver.data.xml.ExperienceLossData;
import net.sf.l2jdev.gameserver.data.xml.HennaData;
import net.sf.l2jdev.gameserver.data.xml.HennaPatternPotentialData;
import net.sf.l2jdev.gameserver.data.xml.ItemData;
import net.sf.l2jdev.gameserver.data.xml.MissionLevel;
import net.sf.l2jdev.gameserver.data.xml.NewQuestData;
import net.sf.l2jdev.gameserver.data.xml.NpcData;
import net.sf.l2jdev.gameserver.data.xml.NpcNameLocalisationData;
import net.sf.l2jdev.gameserver.data.xml.OptionData;
import net.sf.l2jdev.gameserver.data.xml.PetDataTable;
import net.sf.l2jdev.gameserver.data.xml.PlayerTemplateData;
import net.sf.l2jdev.gameserver.data.xml.RecipeData;
import net.sf.l2jdev.gameserver.data.xml.RelicCollectionData;
import net.sf.l2jdev.gameserver.data.xml.RelicData;
import net.sf.l2jdev.gameserver.data.xml.SendMessageLocalisationData;
import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.data.xml.SkillTreeData;
import net.sf.l2jdev.gameserver.data.xml.TimedHuntingZoneData;
import net.sf.l2jdev.gameserver.geoengine.GeoEngine;
import net.sf.l2jdev.gameserver.handler.IItemHandler;
import net.sf.l2jdev.gameserver.handler.ItemHandler;
import net.sf.l2jdev.gameserver.managers.AdenLaboratoryManager;
import net.sf.l2jdev.gameserver.managers.AntiFeedManager;
import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.managers.CoupleManager;
import net.sf.l2jdev.gameserver.managers.CursedWeaponsManager;
import net.sf.l2jdev.gameserver.managers.DuelManager;
import net.sf.l2jdev.gameserver.managers.FortManager;
import net.sf.l2jdev.gameserver.managers.FortSiegeManager;
import net.sf.l2jdev.gameserver.managers.IdManager;
import net.sf.l2jdev.gameserver.managers.ItemManager;
import net.sf.l2jdev.gameserver.managers.ItemsOnGroundManager;
import net.sf.l2jdev.gameserver.managers.MapRegionManager;
import net.sf.l2jdev.gameserver.managers.MatchingRoomManager;
import net.sf.l2jdev.gameserver.managers.MentorManager;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.managers.RecipeManager;
import net.sf.l2jdev.gameserver.managers.RevengeHistoryManager;
import net.sf.l2jdev.gameserver.managers.ScriptManager;
import net.sf.l2jdev.gameserver.managers.SellBuffsManager;
import net.sf.l2jdev.gameserver.managers.SiegeManager;
import net.sf.l2jdev.gameserver.managers.ZoneManager;
import net.sf.l2jdev.gameserver.managers.events.CrossEventManager;
import net.sf.l2jdev.gameserver.model.AccessLevel;
import net.sf.l2jdev.gameserver.model.BlockList;
import net.sf.l2jdev.gameserver.model.ClientSettings;
import net.sf.l2jdev.gameserver.model.ContactList;
import net.sf.l2jdev.gameserver.model.ElementalSpirit;
import net.sf.l2jdev.gameserver.model.HuntPass;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.ManufactureItem;
import net.sf.l2jdev.gameserver.model.PetData;
import net.sf.l2jdev.gameserver.model.PetLevelData;
import net.sf.l2jdev.gameserver.model.PremiumItem;
import net.sf.l2jdev.gameserver.model.Radar;
import net.sf.l2jdev.gameserver.model.RankingHistory;
import net.sf.l2jdev.gameserver.model.RecipeList;
import net.sf.l2jdev.gameserver.model.Request;
import net.sf.l2jdev.gameserver.model.SkillLearn;
import net.sf.l2jdev.gameserver.model.TeleportBookmark;
import net.sf.l2jdev.gameserver.model.TimeStamp;
import net.sf.l2jdev.gameserver.model.TradeList;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.appearance.PlayerAppearance;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Race;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Team;
import net.sf.l2jdev.gameserver.model.actor.enums.player.AdminTeleportType;
import net.sf.l2jdev.gameserver.model.actor.enums.player.BonusExpType;
import net.sf.l2jdev.gameserver.model.actor.enums.player.ElementalSpiritType;
import net.sf.l2jdev.gameserver.model.actor.enums.player.GroupType;
import net.sf.l2jdev.gameserver.model.actor.enums.player.IllegalActionPunishmentType;
import net.sf.l2jdev.gameserver.model.actor.enums.player.MountType;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PlayerAction;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PlayerClass;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PrivateStoreType;
import net.sf.l2jdev.gameserver.model.actor.enums.player.Sex;
import net.sf.l2jdev.gameserver.model.actor.enums.player.ShortcutType;
import net.sf.l2jdev.gameserver.model.actor.enums.player.SubclassInfoType;
import net.sf.l2jdev.gameserver.model.actor.enums.player.TeleportWhereType;
import net.sf.l2jdev.gameserver.model.actor.holders.creature.PetEvolveHolder;
import net.sf.l2jdev.gameserver.model.actor.holders.player.AchievementBoxHolder;
import net.sf.l2jdev.gameserver.model.actor.holders.player.AttendanceInfoHolder;
import net.sf.l2jdev.gameserver.model.actor.holders.player.AutoPlaySettingsHolder;
import net.sf.l2jdev.gameserver.model.actor.holders.player.AutoUseSettingsHolder;
import net.sf.l2jdev.gameserver.model.actor.holders.player.ChallengePoint;
import net.sf.l2jdev.gameserver.model.actor.holders.player.CombatPowerHolder;
import net.sf.l2jdev.gameserver.model.actor.holders.player.CrossEventHolder;
import net.sf.l2jdev.gameserver.model.actor.holders.player.DamageTakenHolder;
import net.sf.l2jdev.gameserver.model.actor.holders.player.Macro;
import net.sf.l2jdev.gameserver.model.actor.holders.player.MacroList;
import net.sf.l2jdev.gameserver.model.actor.holders.player.MissionLevelPlayerDataHolder;
import net.sf.l2jdev.gameserver.model.actor.holders.player.MovieHolder;
import net.sf.l2jdev.gameserver.model.actor.holders.player.PlayerCollectionData;
import net.sf.l2jdev.gameserver.model.actor.holders.player.PlayerPurgeHolder;
import net.sf.l2jdev.gameserver.model.actor.holders.player.PlayerRelicCollectionData;
import net.sf.l2jdev.gameserver.model.actor.holders.player.PlayerRelicData;
import net.sf.l2jdev.gameserver.model.actor.holders.player.RankingHistoryDataHolder;
import net.sf.l2jdev.gameserver.model.actor.holders.player.Shortcut;
import net.sf.l2jdev.gameserver.model.actor.holders.player.Shortcuts;
import net.sf.l2jdev.gameserver.model.actor.holders.player.SubClassHolder;
import net.sf.l2jdev.gameserver.model.actor.instance.AirShip;
import net.sf.l2jdev.gameserver.model.actor.instance.Boat;
import net.sf.l2jdev.gameserver.model.actor.instance.ControlTower;
import net.sf.l2jdev.gameserver.model.actor.instance.Defender;
import net.sf.l2jdev.gameserver.model.actor.instance.Doppelganger;
import net.sf.l2jdev.gameserver.model.actor.instance.FriendlyMob;
import net.sf.l2jdev.gameserver.model.actor.instance.Guard;
import net.sf.l2jdev.gameserver.model.actor.instance.Guardian;
import net.sf.l2jdev.gameserver.model.actor.instance.Pet;
import net.sf.l2jdev.gameserver.model.actor.instance.Shadow;
import net.sf.l2jdev.gameserver.model.actor.instance.Shuttle;
import net.sf.l2jdev.gameserver.model.actor.instance.TamedBeast;
import net.sf.l2jdev.gameserver.model.actor.instance.Trap;
import net.sf.l2jdev.gameserver.model.actor.request.AbstractRequest;
import net.sf.l2jdev.gameserver.model.actor.request.AutoPeelRequest;
import net.sf.l2jdev.gameserver.model.actor.request.SayuneRequest;
import net.sf.l2jdev.gameserver.model.actor.stat.PlayerStat;
import net.sf.l2jdev.gameserver.model.actor.status.PlayerStatus;
import net.sf.l2jdev.gameserver.model.actor.tasks.player.DismountTask;
import net.sf.l2jdev.gameserver.model.actor.tasks.player.FameTask;
import net.sf.l2jdev.gameserver.model.actor.tasks.player.HennaDurationTask;
import net.sf.l2jdev.gameserver.model.actor.tasks.player.InventoryEnableTask;
import net.sf.l2jdev.gameserver.model.actor.tasks.player.PetFeedTask;
import net.sf.l2jdev.gameserver.model.actor.tasks.player.RecoGiveTask;
import net.sf.l2jdev.gameserver.model.actor.tasks.player.RentPetTask;
import net.sf.l2jdev.gameserver.model.actor.tasks.player.ResetChargesTask;
import net.sf.l2jdev.gameserver.model.actor.tasks.player.ResetSoulsTask;
import net.sf.l2jdev.gameserver.model.actor.tasks.player.SitDownTask;
import net.sf.l2jdev.gameserver.model.actor.tasks.player.StandUpTask;
import net.sf.l2jdev.gameserver.model.actor.tasks.player.TeleportWatchdogTask;
import net.sf.l2jdev.gameserver.model.actor.tasks.player.WarnUserTakeBreakTask;
import net.sf.l2jdev.gameserver.model.actor.tasks.player.WaterTask;
import net.sf.l2jdev.gameserver.model.actor.templates.PlayerTemplate;
import net.sf.l2jdev.gameserver.model.actor.transform.Transform;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.ClanAccess;
import net.sf.l2jdev.gameserver.model.clan.ClanMember;
import net.sf.l2jdev.gameserver.model.clan.ClanPrivileges;
import net.sf.l2jdev.gameserver.model.clan.ClanWar;
import net.sf.l2jdev.gameserver.model.clan.enums.ClanWarState;
import net.sf.l2jdev.gameserver.model.cubic.Cubic;
import net.sf.l2jdev.gameserver.model.effects.EffectFlag;
import net.sf.l2jdev.gameserver.model.effects.EffectType;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerAbilityPointsChanged;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerFameChanged;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerHennaAdd;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerHennaRemove;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerItemEquip;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerLoad;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerLogin;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerLogout;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerMenteeStatus;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerMentorStatus;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerPKChanged;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerProfessionCancel;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerProfessionChange;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerPvPChanged;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerPvPKill;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerReputationChanged;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerSubChange;
import net.sf.l2jdev.gameserver.model.events.listeners.FunctionEventListener;
import net.sf.l2jdev.gameserver.model.events.returns.TerminateReturn;
import net.sf.l2jdev.gameserver.model.fishing.Fishing;
import net.sf.l2jdev.gameserver.model.groups.CommandChannel;
import net.sf.l2jdev.gameserver.model.groups.Party;
import net.sf.l2jdev.gameserver.model.groups.PartyDistributionType;
import net.sf.l2jdev.gameserver.model.groups.PartyMessageType;
import net.sf.l2jdev.gameserver.model.groups.matching.MatchingRoom;
import net.sf.l2jdev.gameserver.model.instancezone.Instance;
import net.sf.l2jdev.gameserver.model.interfaces.ILocational;
import net.sf.l2jdev.gameserver.model.item.Armor;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.Weapon;
import net.sf.l2jdev.gameserver.model.item.enums.BodyPart;
import net.sf.l2jdev.gameserver.model.item.enums.BroochJewel;
import net.sf.l2jdev.gameserver.model.item.enums.ItemLocation;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.henna.Henna;
import net.sf.l2jdev.gameserver.model.item.henna.HennaPoten;
import net.sf.l2jdev.gameserver.model.item.holders.ElementalSpiritDataHolder;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.item.holders.ItemPenaltyHolder;
import net.sf.l2jdev.gameserver.model.item.holders.ItemSkillHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.item.type.ActionType;
import net.sf.l2jdev.gameserver.model.item.type.ArmorType;
import net.sf.l2jdev.gameserver.model.item.type.EtcItemType;
import net.sf.l2jdev.gameserver.model.item.type.ItemType;
import net.sf.l2jdev.gameserver.model.item.type.WeaponType;
import net.sf.l2jdev.gameserver.model.itemcontainer.Inventory;
import net.sf.l2jdev.gameserver.model.itemcontainer.ItemContainer;
import net.sf.l2jdev.gameserver.model.itemcontainer.ItemPenalty;
import net.sf.l2jdev.gameserver.model.itemcontainer.PlayerFreight;
import net.sf.l2jdev.gameserver.model.itemcontainer.PlayerInventory;
import net.sf.l2jdev.gameserver.model.itemcontainer.PlayerRandomCraft;
import net.sf.l2jdev.gameserver.model.itemcontainer.PlayerRefund;
import net.sf.l2jdev.gameserver.model.itemcontainer.PlayerWarehouse;
import net.sf.l2jdev.gameserver.model.olympiad.Hero;
import net.sf.l2jdev.gameserver.model.olympiad.OlympiadGameManager;
import net.sf.l2jdev.gameserver.model.olympiad.OlympiadGameTask;
import net.sf.l2jdev.gameserver.model.olympiad.OlympiadManager;
import net.sf.l2jdev.gameserver.model.olympiad.OlympiadMode;
import net.sf.l2jdev.gameserver.model.options.Options;
import net.sf.l2jdev.gameserver.model.prison.PrisonManager;
import net.sf.l2jdev.gameserver.model.prison.Prisoner;
import net.sf.l2jdev.gameserver.model.punishment.PunishmentAffect;
import net.sf.l2jdev.gameserver.model.punishment.PunishmentType;
import net.sf.l2jdev.gameserver.model.script.Quest;
import net.sf.l2jdev.gameserver.model.script.QuestDialogType;
import net.sf.l2jdev.gameserver.model.script.QuestState;
import net.sf.l2jdev.gameserver.model.script.QuestTimer;
import net.sf.l2jdev.gameserver.model.script.newquestdata.NewQuest;
import net.sf.l2jdev.gameserver.model.script.timers.TimerHolder;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.model.siege.CastleSide;
import net.sf.l2jdev.gameserver.model.siege.Fort;
import net.sf.l2jdev.gameserver.model.siege.MercenaryPledgeHolder;
import net.sf.l2jdev.gameserver.model.siege.Siege;
import net.sf.l2jdev.gameserver.model.skill.AbnormalType;
import net.sf.l2jdev.gameserver.model.skill.AmmunitionSkillList;
import net.sf.l2jdev.gameserver.model.skill.BuffInfo;
import net.sf.l2jdev.gameserver.model.skill.CommonSkill;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.SkillCaster;
import net.sf.l2jdev.gameserver.model.skill.SkillCastingType;
import net.sf.l2jdev.gameserver.model.skill.enums.NextActionType;
import net.sf.l2jdev.gameserver.model.skill.enums.SkillFinishType;
import net.sf.l2jdev.gameserver.model.skill.enums.SoulType;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillUseHolder;
import net.sf.l2jdev.gameserver.model.skill.targets.AffectScope;
import net.sf.l2jdev.gameserver.model.skill.targets.TargetType;
import net.sf.l2jdev.gameserver.model.stats.BaseStat;
import net.sf.l2jdev.gameserver.model.stats.Formulas;
import net.sf.l2jdev.gameserver.model.stats.MoveType;
import net.sf.l2jdev.gameserver.model.stats.Stat;
import net.sf.l2jdev.gameserver.model.variables.AccountVariables;
import net.sf.l2jdev.gameserver.model.variables.PlayerVariables;
import net.sf.l2jdev.gameserver.model.vip.VipManager;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.model.zone.ZoneRegion;
import net.sf.l2jdev.gameserver.model.zone.ZoneType;
import net.sf.l2jdev.gameserver.model.zone.type.WaterZone;
import net.sf.l2jdev.gameserver.network.Disconnection;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.ChatType;
import net.sf.l2jdev.gameserver.network.enums.HtmlActionScope;
import net.sf.l2jdev.gameserver.network.enums.InventorySlot;
import net.sf.l2jdev.gameserver.network.enums.PartySmallWindowUpdateType;
import net.sf.l2jdev.gameserver.network.enums.StatusUpdateType;
import net.sf.l2jdev.gameserver.network.enums.UserInfoType;
import net.sf.l2jdev.gameserver.network.serverpackets.AcquireSkillList;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.ChangeWaitType;
import net.sf.l2jdev.gameserver.network.serverpackets.CharInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.ConfirmDlg;
import net.sf.l2jdev.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2jdev.gameserver.network.serverpackets.EtcStatusUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.ExAbnormalStatusUpdateFromTarget;
import net.sf.l2jdev.gameserver.network.serverpackets.ExAdenaInvenCount;
import net.sf.l2jdev.gameserver.network.serverpackets.ExAutoSoulShot;
import net.sf.l2jdev.gameserver.network.serverpackets.ExBrPremiumState;
import net.sf.l2jdev.gameserver.network.serverpackets.ExDamagePopUp;
import net.sf.l2jdev.gameserver.network.serverpackets.ExDieInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.ExDuelUpdateUserInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.ExGetBookMarkInfoPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.ExGetOnAirShip;
import net.sf.l2jdev.gameserver.network.serverpackets.ExItemScore;
import net.sf.l2jdev.gameserver.network.serverpackets.ExMagicAttackInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPledgeCoinInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPledgeCount;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPrivateStoreSetWholeMsg;
import net.sf.l2jdev.gameserver.network.serverpackets.ExQuestItemList;
import net.sf.l2jdev.gameserver.network.serverpackets.ExSetCompassZoneCode;
import net.sf.l2jdev.gameserver.network.serverpackets.ExStartScenePlayer;
import net.sf.l2jdev.gameserver.network.serverpackets.ExStopScenePlayer;
import net.sf.l2jdev.gameserver.network.serverpackets.ExStorageMaxCount;
import net.sf.l2jdev.gameserver.network.serverpackets.ExSubjobInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.ExUseSharedGroupItem;
import net.sf.l2jdev.gameserver.network.serverpackets.ExUserBoostStat;
import net.sf.l2jdev.gameserver.network.serverpackets.ExUserInfoAbnormalVisualEffect;
import net.sf.l2jdev.gameserver.network.serverpackets.ExUserInfoCubic;
import net.sf.l2jdev.gameserver.network.serverpackets.ExUserInfoEquipSlot;
import net.sf.l2jdev.gameserver.network.serverpackets.ExUserInfoInvenWeight;
import net.sf.l2jdev.gameserver.network.serverpackets.ExUserViewInfoParameter;
import net.sf.l2jdev.gameserver.network.serverpackets.ExVitalityEffectInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.GetOnVehicle;
import net.sf.l2jdev.gameserver.network.serverpackets.HennaInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.ItemList;
import net.sf.l2jdev.gameserver.network.serverpackets.LeaveWorld;
import net.sf.l2jdev.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2jdev.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2jdev.gameserver.network.serverpackets.NicknameChanged;
import net.sf.l2jdev.gameserver.network.serverpackets.ObservationMode;
import net.sf.l2jdev.gameserver.network.serverpackets.ObservationReturn;
import net.sf.l2jdev.gameserver.network.serverpackets.PartySmallWindowUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import net.sf.l2jdev.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.PrivateStoreListBuy;
import net.sf.l2jdev.gameserver.network.serverpackets.PrivateStoreListSell;
import net.sf.l2jdev.gameserver.network.serverpackets.PrivateStoreManageListBuy;
import net.sf.l2jdev.gameserver.network.serverpackets.PrivateStoreMsgBuy;
import net.sf.l2jdev.gameserver.network.serverpackets.PrivateStoreMsgSell;
import net.sf.l2jdev.gameserver.network.serverpackets.RecipeShopMsg;
import net.sf.l2jdev.gameserver.network.serverpackets.RecipeShopSellList;
import net.sf.l2jdev.gameserver.network.serverpackets.RelationChanged;
import net.sf.l2jdev.gameserver.network.serverpackets.Ride;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.SetupGauge;
import net.sf.l2jdev.gameserver.network.serverpackets.ShortcutInit;
import net.sf.l2jdev.gameserver.network.serverpackets.SkillCoolTime;
import net.sf.l2jdev.gameserver.network.serverpackets.SkillList;
import net.sf.l2jdev.gameserver.network.serverpackets.Snoop;
import net.sf.l2jdev.gameserver.network.serverpackets.SocialAction;
import net.sf.l2jdev.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.StopMove;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.TargetSelected;
import net.sf.l2jdev.gameserver.network.serverpackets.TargetUnselected;
import net.sf.l2jdev.gameserver.network.serverpackets.TradeDone;
import net.sf.l2jdev.gameserver.network.serverpackets.TradeOtherDone;
import net.sf.l2jdev.gameserver.network.serverpackets.TradeStart;
import net.sf.l2jdev.gameserver.network.serverpackets.UserInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2jdev.gameserver.network.serverpackets.autopeel.ExReadyItemAutoPeel;
import net.sf.l2jdev.gameserver.network.serverpackets.autopeel.ExStopItemAutoPeel;
import net.sf.l2jdev.gameserver.network.serverpackets.autoplay.ExActivateAutoShortcut;
import net.sf.l2jdev.gameserver.network.serverpackets.autoplay.ExAutoPlaySettingSend;
import net.sf.l2jdev.gameserver.network.serverpackets.chatbackground.ExChatBackGroundSettingNotification;
import net.sf.l2jdev.gameserver.network.serverpackets.chatbackground.ExChatBackgroundList;
import net.sf.l2jdev.gameserver.network.serverpackets.commission.ExResponseCommissionInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.dualinventory.ExDualInventorySwap;
import net.sf.l2jdev.gameserver.network.serverpackets.elementalspirits.ElementalSpiritInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.elementalspirits.ExElementalSpiritAttackType;
import net.sf.l2jdev.gameserver.network.serverpackets.friend.FriendStatus;
import net.sf.l2jdev.gameserver.network.serverpackets.huntingzones.TimeRestrictFieldDieLimitTime;
import net.sf.l2jdev.gameserver.network.serverpackets.huntingzones.TimedHuntingZoneExit;
import net.sf.l2jdev.gameserver.network.serverpackets.limitshop.ExBloodyCoinCount;
import net.sf.l2jdev.gameserver.network.serverpackets.olympiad.ExOlympiadMode;
import net.sf.l2jdev.gameserver.network.serverpackets.penaltyitemdrop.ExPenaltyItemDrop;
import net.sf.l2jdev.gameserver.network.serverpackets.penaltyitemdrop.ExPenaltyItemInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.pet.PetSummonInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.quest.ExQuestDialog;
import net.sf.l2jdev.gameserver.network.serverpackets.quest.ExQuestNotificationAll;
import net.sf.l2jdev.gameserver.network.serverpackets.relics.ExRelicsAnnounce;
import net.sf.l2jdev.gameserver.network.serverpackets.relics.ExRelicsCollectionInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.relics.ExRelicsCollectionUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.relics.ExRelicsList;
import net.sf.l2jdev.gameserver.network.serverpackets.relics.ExRelicsUpdateList;
import net.sf.l2jdev.gameserver.network.serverpackets.surveillance.ExUserWatcherTargetStatus;
import net.sf.l2jdev.gameserver.network.serverpackets.vip.ReceiveVipInfo;
import net.sf.l2jdev.gameserver.taskmanagers.AttackStanceTaskManager;
import net.sf.l2jdev.gameserver.taskmanagers.AutoPlayTaskManager;
import net.sf.l2jdev.gameserver.taskmanagers.AutoUseTaskManager;
import net.sf.l2jdev.gameserver.taskmanagers.DecayTaskManager;
import net.sf.l2jdev.gameserver.taskmanagers.GameTimeTaskManager;
import net.sf.l2jdev.gameserver.taskmanagers.ItemsAutoDestroyTaskManager;
import net.sf.l2jdev.gameserver.taskmanagers.PlayerAutoSaveTaskManager;
import net.sf.l2jdev.gameserver.taskmanagers.PvpFlagTaskManager;
import net.sf.l2jdev.gameserver.util.Broadcast;
import net.sf.l2jdev.gameserver.util.LocationUtil;

public class Player extends Playable
{
	public static final String RESTORE_SKILLS_FOR_CHAR = "SELECT skill_id,skill_level,skill_sub_level FROM character_skills WHERE charId=? AND class_index=?";
	public static final String UPDATE_CHARACTER_SKILL_LEVEL = "UPDATE character_skills SET skill_level=?, skill_sub_level=?  WHERE skill_id=? AND charId=? AND class_index=?";
	public static final String ADD_NEW_SKILLS = "REPLACE INTO character_skills (charId,skill_id,skill_level,skill_sub_level,class_index) VALUES (?,?,?,?,?)";
	public static final String DELETE_SKILL_FROM_CHAR = "DELETE FROM character_skills WHERE skill_id=? AND charId=? AND class_index=?";
	public static final String DELETE_CHAR_SKILLS = "DELETE FROM character_skills WHERE charId=? AND class_index=?";
	public static final String ADD_SKILL_SAVE = "REPLACE INTO character_skills_save (charId,skill_id,skill_level,skill_sub_level,remaining_time,reuse_delay,systime,restore_type,class_index,buff_index) VALUES (?,?,?,?,?,?,?,?,?,?)";
	public static final String RESTORE_SKILL_SAVE = "SELECT skill_id,skill_level,skill_sub_level,remaining_time, reuse_delay, systime, restore_type FROM character_skills_save WHERE charId=? AND class_index=? ORDER BY buff_index ASC";
	public static final String DELETE_SKILL_SAVE = "DELETE FROM character_skills_save WHERE charId=? AND class_index=?";
	public static final String ADD_ITEM_REUSE_SAVE = "INSERT INTO character_item_reuse_save (charId,itemId,itemObjId,reuseDelay,systime) VALUES (?,?,?,?,?)";
	public static final String RESTORE_ITEM_REUSE_SAVE = "SELECT charId,itemId,itemObjId,reuseDelay,systime FROM character_item_reuse_save WHERE charId=?";
	public static final String DELETE_ITEM_REUSE_SAVE = "DELETE FROM character_item_reuse_save WHERE charId=?";
	public static final String INSERT_CHARACTER = "INSERT INTO characters (account_name,charId,char_name,level,maxHp,curHp,maxCp,curCp,maxMp,curMp,face,hairStyle,hairColor,sex,exp,sp,reputation,fame,raidbossPoints,pvpkills,pkkills,clanid,race,classid,deletetime,cancraft,title,title_color,online,clan_privs,wantspeace,base_class,nobless,power_grade,vitality_points,createDate,lastAccess,kills,deaths) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	public static final String UPDATE_CHARACTER = "UPDATE characters SET level=?,maxHp=?,curHp=?,maxCp=?,curCp=?,maxMp=?,curMp=?,face=?,hairStyle=?,hairColor=?,sex=?,heading=?,x=?,y=?,z=?,exp=?,expBeforeDeath=?,sp=?,reputation=?,fame=?,raidbossPoints=?,pvpkills=?,pkkills=?,clanid=?,race=?,classid=?,deletetime=?,title=?,title_color=?,online=?,clan_privs=?,wantspeace=?,base_class=?,onlinetime=?,nobless=?,power_grade=?,subpledge=?,lvl_joined_academy=?,apprentice=?,sponsor=?,clan_join_expiry_time=?,clan_create_expiry_time=?,char_name=?,bookmarkslot=?,vitality_points=?,language=?,faction=?,pccafe_points=?,kills=?,deaths=? WHERE charId=?";
	public static final String UPDATE_CHARACTER_ACCESS = "UPDATE characters SET accesslevel = ? WHERE charId = ?";
	public static final String RESTORE_CHARACTER = "SELECT * FROM characters WHERE charId=?";
	public static final String INSERT_TP_BOOKMARK = "INSERT INTO character_tpbookmark (charId,Id,x,y,z,icon,tag,name) values (?,?,?,?,?,?,?,?)";
	public static final String UPDATE_TP_BOOKMARK = "UPDATE character_tpbookmark SET icon=?,tag=?,name=? where charId=? AND Id=?";
	public static final String RESTORE_TP_BOOKMARK = "SELECT Id,x,y,z,icon,tag,name FROM character_tpbookmark WHERE charId=?";
	public static final String DELETE_TP_BOOKMARK = "DELETE FROM character_tpbookmark WHERE charId=? AND Id=?";
	public static final String RESTORE_CHAR_SUBCLASSES = "SELECT class_id,exp,sp,level,vitality_points,class_index,dual_class FROM character_subclasses WHERE charId=? ORDER BY class_index ASC";
	public static final String ADD_CHAR_SUBCLASS = "INSERT INTO character_subclasses (charId,class_id,exp,sp,level,vitality_points,class_index,dual_class) VALUES (?,?,?,?,?,?,?,?)";
	public static final String UPDATE_CHAR_SUBCLASS = "UPDATE character_subclasses SET exp=?,sp=?,level=?,vitality_points=?,class_id=?,dual_class=? WHERE charId=? AND class_index =?";
	public static final String DELETE_CHAR_SUBCLASS = "DELETE FROM character_subclasses WHERE charId=? AND class_index=?";
	public static final String RESTORE_CHAR_HENNAS = "SELECT slot,symbol_id FROM character_hennas WHERE charId=? AND class_index=?";
	public static final String ADD_CHAR_HENNA = "INSERT INTO character_hennas (charId,symbol_id,slot,class_index) VALUES (?,?,?,?)";
	public static final String DELETE_CHAR_HENNA = "DELETE FROM character_hennas WHERE charId=? AND slot=? AND class_index=?";
	public static final String ADD_CHAR_HENNA_POTENS = "REPLACE INTO character_potens (charId,slot_position,poten_id,enchant_level,enchant_exp,unlock_slot) VALUES (?,?,?,?,?,?)";
	public static final String RESTORE_CHAR_HENNA_POTENS = "SELECT slot_position,poten_id,enchant_level,enchant_exp,unlock_slot FROM character_potens WHERE charId=? ORDER BY slot_position ASC";
	public static final String DELETE_CHAR_SHORTCUTS = "DELETE FROM character_shortcuts WHERE charId=? AND class_index=?";
	public static final String INSERT_COLLECTION = "REPLACE INTO collections (`accountName`, `itemId`, `collectionId`, `index`) VALUES (?, ?, ?, ?)";
	public static final String RESTORE_COLLECTION = "SELECT * FROM collections WHERE accountName=? ORDER BY `index`";
	public static final String DELETE_COLLECTION_FAVORITE = "DELETE FROM collection_favorites WHERE accountName=?";
	public static final String INSERT_COLLECTION_FAVORITE = "REPLACE INTO collection_favorites (`accountName`, `collectionId`) VALUES (?, ?)";
	public static final String RESTORE_COLLECTION_FAVORITE = "SELECT * FROM collection_favorites WHERE accountName=?";
	public static final String INSERT_RELICS = "REPLACE INTO relics (`accountName`, `relicId`, `relicLevel`, `relicCount`, `relicIndex`, `relicSummonTime`) VALUES (?, ?, ?, ?, ?, ?)";
	public static final String RESTORE_RELICS = "SELECT * FROM relics WHERE accountName=? ORDER BY `relicId`";
	public static final String DELETE_RELICS = "DELETE FROM relics WHERE accountName=? AND relicId=? AND relicLevel=? AND relicCount=? AND relicIndex=? AND relicSummonTime=?";
	public static final String INSERT_RELICS_COLLECTION = "REPLACE INTO relic_collections (`accountName`, `relicId`, `relicLevel`, `relicCollectionId`, `index`) VALUES (?, ?, ?, ?, ?)";
	public static final String RESTORE_RELICS_COLLECTION = "SELECT * FROM relic_collections WHERE accountName=? ORDER BY `relicCollectionId`";
	public static final String DELETE_CHAR_RECIPE_SHOP = "DELETE FROM character_recipeshoplist WHERE charId=?";
	public static final String INSERT_CHAR_RECIPE_SHOP = "REPLACE INTO character_recipeshoplist (`charId`, `recipeId`, `price`, `index`) VALUES (?, ?, ?, ?)";
	public static final String RESTORE_CHAR_RECIPE_SHOP = "SELECT * FROM character_recipeshoplist WHERE charId=? ORDER BY `index`";
	public static final String DELETE_SUBJUGATION = "DELETE FROM character_purge WHERE charId=?";
	public static final String INSERT_SUBJUGATION = "REPLACE INTO character_purge (`charId`, `category`, `points`, `keys`, `remainingKeys`) VALUES (?, ?, ?, ?, ?)";
	public static final String RESTORE_SUBJUGATION = "SELECT * FROM character_purge WHERE charId=?";
	public static final String RESTORE_ELEMENTAL_SPIRITS = "SELECT * FROM character_spirits WHERE charId=?";
	public static final String NEWBIE_KEY = "NEWBIE";
	public static final int ID_NONE = -1;
	public static final int REQUEST_TIMEOUT = 15;
	private int _pcCafePoints = 0;
	private GameClient _client;
	private String _ip = "N/A";
	private final String _accountName;
	private long _deleteTimer;
	private Calendar _createDate = Calendar.getInstance();
	private String _lang = null;
	private String _htmlPrefix = "";
	private volatile boolean _isOnline = false;
	private boolean _offlinePlay = false;
	private boolean _enteredWorld = false;
	private long _onlineTime;
	private long _onlineBeginTime;
	private long _lastAccess;
	private long _uptime;
	private final InventoryUpdate _inventoryUpdate = new InventoryUpdate();
	private ScheduledFuture<?> _inventoryUpdateTask;
	private ScheduledFuture<?> _itemListTask;
	private ScheduledFuture<?> _adenaAndWeightTask;
	private ScheduledFuture<?> _skillListTask;
	private ScheduledFuture<?> _storageCountTask;
	private ScheduledFuture<?> _userBoostStatTask;
	private ScheduledFuture<?> _abnormalVisualEffectTask;
	private ScheduledFuture<?> _updateAndBroadcastStatusTask;
	private ScheduledFuture<?> _broadcastCharInfoTask;
	private ScheduledFuture<?> _broadcastStatusUpdateTask;
	private boolean _subclassLock = false;
	protected int _baseClass;
	protected int _activeClass;
	protected int _classIndex = 0;
	private boolean _isDeathKnight = false;
	private boolean _isVanguard = false;
	private boolean _isAssassin = false;
	private boolean _isWarg = false;
	private boolean _isBloodRose = false;
	private boolean _isSamurai = false;
	private int _controlItemId;
	private PetData _data;
	private PetLevelData _leveldata;
	private int _curFeed;
	protected Future<?> _mountFeedTask;
	private ScheduledFuture<?> _dismountTask;
	private boolean _petItems = false;
	private final Map<Integer, SubClassHolder> _subClasses = new ConcurrentHashMap<>();
	private final PlayerAppearance _appearance;
	private long _expBeforeDeath;
	private int _pvpKills;
	private int _pkKills;
	private int _totalKills = 0;
	private int _totalDeaths = 0;
	private byte _pvpFlag;
	private int _einhasadOverseeingLevel = 0;
	private final LinkedList<DamageTakenHolder> _lastDamageTaken = new LinkedList<>();
	private final ItemPenalty _itemPenalty = new ItemPenalty(this);
	private final List<ItemPenaltyHolder> _itemPenaltyList = new LinkedList<>();
	private int _fame;
	private ScheduledFuture<?> _fameTask;
	private int _raidbossPoints;
	private ScheduledFuture<?> _teleportWatchdog;
	private byte _siegeState = 0;
	private int _siegeSide = 0;
	private int _curWeightPenalty = 0;
	private int _lastCompassZone;
	private final ContactList _contactList = new ContactList(this);
	private int _bookmarkslot = 0;
	private final Map<Integer, TeleportBookmark> _tpbookmarks = new ConcurrentSkipListMap<>();
	private boolean _canFeed;
	private boolean _isInSiege;
	private boolean _isInHideoutSiege = false;
	private boolean _inOlympiadMode = false;
	private boolean _olympiadStart = false;
	private int _olympiadGameId = -1;
	private int _olympiadSide = -1;
	private boolean _isInDuel = false;
	private boolean _startingDuel = false;
	private int _duelState = 0;
	private int _duelId = 0;
	private SystemMessageId _noDuelReason = SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL;
	private Vehicle _vehicle = null;
	private Location _inVehiclePosition;
	private MountType _mountType = MountType.NONE;
	private int _mountNpcId;
	private int _mountLevel;
	private int _mountObjectID = 0;
	private AdminTeleportType _teleportType = AdminTeleportType.NORMAL;
	private boolean _inCrystallize;
	private boolean _isCrafting;
	private long _offlineShopStart = 0L;
	private final Map<Integer, RecipeList> _dwarvenRecipeBook = new ConcurrentSkipListMap<>();
	private final Map<Integer, RecipeList> _commonRecipeBook = new ConcurrentSkipListMap<>();
	private final Map<Integer, PremiumItem> _premiumItems = new ConcurrentSkipListMap<>();
	private boolean _waitTypeSitting;
	private boolean _sittingInProgress;
	private Location _lastLoc;
	private boolean _observerMode = false;
	private Location _teleportLocation;
	private boolean _castingTeleportSkill = false;
	private final Location _lastServerPosition = new Location(0, 0, 0);
	private final AtomicBoolean _blinkActive = new AtomicBoolean();
	private int _recomHave;
	private int _recomLeft;
	private ScheduledFuture<?> _recoGiveTask;
	protected boolean _recoTwoHoursGiven = false;
	private ScheduledFuture<?> _onlineTimeUpdateTask;
	private final PlayerInventory _inventory = new PlayerInventory(this);
	private final PlayerFreight _freight = new PlayerFreight(this);
	private final PlayerWarehouse _warehouse = new PlayerWarehouse(this);
	private PlayerRefund _refund;
	private PrivateStoreType _privateStoreType = PrivateStoreType.NONE;
	private TradeList _activeTradeList;
	private ItemContainer _activeWarehouse;
	private Map<Integer, ManufactureItem> _manufactureItems;
	private String _storeName = "";
	private TradeList _sellList;
	private TradeList _buyList;
	private PreparedMultisellListHolder _currentMultiSell = null;
	private boolean _noble = false;
	private boolean _hero = false;
	private boolean _trueHero = false;
	private boolean _premiumStatus = false;
	private boolean _isGood = false;
	private boolean _isEvil = false;
	private Npc _lastFolkNpc = null;
	private int _questNpcObject = 0;
	private boolean _simulatedTalking = false;
	private final Map<String, QuestState> _quests = new ConcurrentSkipListMap<>(String.CASE_INSENSITIVE_ORDER);
	private final Shortcuts _shortcuts = new Shortcuts(this);
	private final MacroList _macros = new MacroList(this);
	private final Set<Player> _snoopListener = ConcurrentHashMap.newKeySet();
	private final Set<Player> _snoopedPlayer = ConcurrentHashMap.newKeySet();
	private final HennaPoten[] _hennaPoten = new HennaPoten[4];
	private final Map<BaseStat, Integer> _hennaBaseStats = new ConcurrentHashMap<>();
	private final Map<Integer, ScheduledFuture<?>> _hennaRemoveSchedules = new ConcurrentHashMap<>(3);
	private Pet _pet = null;
	private final Map<Integer, Summon> _servitors = new ConcurrentHashMap<>(1);
	private int _agathionId = 0;
	private final Set<TamedBeast> _tamedBeast = ConcurrentHashMap.newKeySet();
	private boolean _minimapAllowed = false;
	private final Radar _radar;
	private MatchingRoom _matchingRoom;
	private ScheduledFuture<?> _taskWarnUserTakeBreak;
	private int _clanId;
	private Clan _clan;
	private int _apprentice = 0;
	private int _sponsor = 0;
	private long _clanJoinExpiryTime;
	private long _clanCreateExpiryTime;
	private int _powerGrade = 0;
	private ClanPrivileges _clanPrivileges = new ClanPrivileges();
	private int _pledgeClass = 0;
	private int _pledgeType = 0;
	private int _lvlJoinedAcademy = 0;
	private int _wantsPeace = 0;
	private final AtomicInteger _charges = new AtomicInteger();
	private ScheduledFuture<?> _chargeTask = null;
	private final Map<SoulType, Integer> _souls = new ConcurrentHashMap<>(2);
	private ScheduledFuture<?> _soulTask = null;
	private int _deathPoints = 0;
	private int _maxDeathPoints = 0;
	private int _beastPoints = 0;
	private int _maxBeastPoints = 1000;
	private int _assassinationPoints = 0;
	private int _maxAssassinationPoints = 100000;
	private int _lightPoints = 0;
	private int _maxLightPoints = 100000;
	private int _wolfPoints = 0;
	private int _maxWolfPoints = 1000;
	private Location _currentSkillWorldPosition;
	private AccessLevel _accessLevel;
	private boolean _messageRefusal = false;
	private boolean _silenceMode = false;
	private List<Integer> _silenceModeExcluded;
	private boolean _dietMode = false;
	private boolean _tradeRefusal = false;
	private boolean _exchangeRefusal = false;
	private Party _party;
	private Player _activeRequester;
	private long _requestExpireTime = 0L;
	private final Request _request = new Request(this);
	private long _spawnProtectEndTime = 0L;
	private long _teleportProtectEndTime = 0L;
	private final Map<Integer, ExResponseCommissionInfo> _lastCommissionInfos = new ConcurrentHashMap<>();
	private long _recentFakeDeathEndTime = 0L;
	private Weapon _fistsWeaponItem;
	private final Map<Integer, String> _chars = new ConcurrentSkipListMap<>();
	private final Map<Class<? extends AbstractRequest>, AbstractRequest> _requests = new ConcurrentHashMap<>();
	protected boolean _inventoryDisable = false;
	private final Map<Integer, Cubic> _cubics = new ConcurrentSkipListMap<>();
	protected Set<Integer> _activeSoulShots = ConcurrentHashMap.newKeySet();
	private BroochJewel _activeRubyJewel = null;
	private BroochJewel _activeShappireJewel = null;
	private int _lastAmmunitionId = 0;
	private boolean _isRegisteredOnEvent = false;
	private boolean _isOnSoloEvent = false;
	private boolean _isOnEvent = false;
	private final int[] _raceTickets = new int[2];
	private final BlockList _blockList = new BlockList(this);
	private final Map<Integer, Skill> _transformSkills = new ConcurrentHashMap<>();
	private ScheduledFuture<?> _taskRentPet;
	private ScheduledFuture<?> _taskWater;
	private final int[] _htmlActionOriginObjectIds = new int[HtmlActionScope.values().length];
	private int _lastHtmlActionOriginObjId;
	@SuppressWarnings("unchecked")
	private final LinkedList<String>[] _htmlActionCaches = new LinkedList[HtmlActionScope.values().length];
	private Forum _forumMail;
	private Forum _forumMemo;
	private Skill _lastSkillUsed;
	private SkillUseHolder _queuedSkill;
	private int _cursedWeaponEquippedId = 0;
	private boolean _combatFlagEquippedId = false;
	private boolean _canRevive = true;
	private int _reviveRequested = 0;
	private double _revivePower = 0.0;
	private int _reviveHpPercent = 0;
	private int _reviveMpPercent = 0;
	private int _reviveCpPercent = 0;
	private boolean _revivePet = false;
	private double _cpUpdateIncCheck = 0.0;
	private double _cpUpdateDecCheck = 0.0;
	private double _cpUpdateInterval = 0.0;
	private double _mpUpdateIncCheck = 0.0;
	private double _mpUpdateDecCheck = 0.0;
	private double _mpUpdateInterval = 0.0;
	private double _originalCp = 0.0;
	private double _originalHp = 0.0;
	private double _originalMp = 0.0;
	private int _clientX;
	private int _clientY;
	private int _clientZ;
	private int _clientHeading;
	private volatile long _fallingTimestamp = 0L;
	private volatile int _fallingDamage = 0;
	private Future<?> _fallingDamageTask = null;
	private int _multiSocialTarget = 0;
	private int _multiSociaAction = 0;
	private MovieHolder _movieHolder = null;
	private String _adminConfirmCmd = null;
	private volatile long _lastItemAuctionInfoRequest = 0L;
	private long _pvpFlagLasts;
	private long _notMoveUntil = 0L;
	private Map<Integer, Skill> _customSkills = null;
	private volatile int _actionMask;
	private int _questZoneId = -1;
	private final Fishing _fishing = new Fishing(this);
	private Prisoner _prisonerInfo = new Prisoner();
	private boolean _married = false;
	private int _partnerId = 0;
	private int _coupleId = 0;
	private boolean _engagerequest = false;
	private int _engageid = 0;
	private boolean _marryrequest = false;
	private boolean _marryaccepted = false;
	private String _lastPetitionGmName = null;
	private boolean _hasCharmOfCourage = false;
	private final Set<Integer> _whisperers = ConcurrentHashMap.newKeySet();
	private ElementalSpirit[] _spirits;
	private ElementalSpiritType _activeElementalSpiritType;
	private byte _vipTier = 0;
	private long _attendanceDelay;
	private final AutoPlaySettingsHolder _autoPlaySettings = new AutoPlaySettingsHolder();
	private final AutoUseSettingsHolder _autoUseSettings = new AutoUseSettingsHolder();
	private final AtomicBoolean _autoPlaying = new AtomicBoolean();
	private boolean _resumedAutoPlay = false;
	private ScheduledFuture<?> _timedHuntingZoneTask = null;
	private PlayerRandomCraft _randomCraft = null;
	private ScheduledFuture<?> _statIncreaseSkillTask;
	private final List<PlayerCollectionData> _collections = new ArrayList<>();
	private final List<Integer> _collectionFavorites = new ArrayList<>();
	private int _crossAdvancedReward;
	private final List<CrossEventHolder> _crossCell = new ArrayList<>();
	private final Map<Integer, PlayerPurgeHolder> _purgePoints = new HashMap<>();
	private final HuntPass _huntPass;
	private final AchievementBoxHolder _achivemenetBox;
	private final ChallengePoint _challengePoints;
	private final RankingHistory _rankingHistory;
	private String _mercenaryName;
	private int _clanIdMercenary;
	private Clan _clanOg;
	private int _clanIdOg;
	private final Map<Integer, PetEvolveHolder> _petEvolves = new HashMap<>();
	private MissionLevelPlayerDataHolder _missionLevelProgress = null;
	private int _dualInventorySlot = 0;
	private List<Integer> _dualInventorySetA;
	private List<Integer> _dualInventorySetB;
	private ScheduledFuture<?> _sendItemScoreTask;
	private final CombatPowerHolder _combatPowerHolder = new CombatPowerHolder(this);
	private final List<PlayerRelicCollectionData> _relicCollections = new ArrayList<>();
	private final List<PlayerRelicData> _relics = Collections.synchronizedList(new ArrayList<>());
	private final List<QuestTimer> _questTimers = new ArrayList<>();
	private final List<TimerHolder<?>> _timerHolders = new ArrayList<>();
	private boolean _isSellingBuffs = false;
	private List<SellBuffHolder> _sellingBuffs = null;
	private float _adenLabBonusChance = 1.0F;
	private final Map<Byte, AtomicInteger> _adenLabCurrentlyUnlockedPage = new ConcurrentHashMap<>();
	private final Map<Byte, AtomicInteger> _adenLabCurrentTranscendLevel = new ConcurrentHashMap<>();
	private final Map<Byte, AtomicInteger> _adenLabNormalGameOpenedCardsCount = new ConcurrentHashMap<>();
	private final Map<Byte, Map<Byte, Map<Byte, Integer>>> _adenLabSpecialStagesDrawnOptions = new HashMap<>();
	private final Map<Byte, Map<Byte, Map<Byte, Integer>>> _adenLabSpecialStagesConfirmedOptions = new HashMap<>();
	private ClientSettings _clientSettings;
	private Set<QuestState> _notifyQuestOfDeathList;
	private final Set<Integer> _friendList = ConcurrentHashMap.newKeySet();
	private final Set<Integer> _surveillanceList = ConcurrentHashMap.newKeySet();
	
	public void setPvpFlagLasts(long time)
	{
		this._pvpFlagLasts = time;
	}
	
	public long getPvpFlagLasts()
	{
		return this._pvpFlagLasts;
	}
	
	public void startPvPFlag()
	{
		this.updatePvPFlag(1);
		PvpFlagTaskManager.getInstance().add(this);
	}
	
	public void stopPvpRegTask()
	{
		PvpFlagTaskManager.getInstance().remove(this);
	}
	
	public void stopPvPFlag()
	{
		this.stopPvpRegTask();
		this.updatePvPFlag(0);
	}
	
	public boolean isSellingBuffs()
	{
		return this._isSellingBuffs;
	}
	
	public void setSellingBuffs(boolean value)
	{
		this._isSellingBuffs = value;
	}
	
	public List<SellBuffHolder> getSellingBuffs()
	{
		if (this._sellingBuffs == null)
		{
			this._sellingBuffs = new ArrayList<>();
		}
		
		return this._sellingBuffs;
	}
	
	public ClientSettings getClientSettings()
	{
		if (this._clientSettings == null)
		{
			this._clientSettings = new ClientSettings(this);
		}
		
		return this._clientSettings;
	}
	
	public static Player create(PlayerTemplate template, String accountName, String name, PlayerAppearance app)
	{
		Player player = new Player(template, accountName, app);
		player.setName(name);
		player.setAccessLevel(0, false, false);
		player.setCreateDate(Calendar.getInstance());
		player.setBaseClass(player.getPlayerClass());
		player.setRecomLeft(20);
		if (player.createDb())
		{
			CharInfoTable.getInstance().addName(player);
			return player;
		}
		return null;
	}
	
	@Override
	public String getName()
	{
		return OlympiadConfig.OLYMPIAD_HIDE_NAMES && this.isInOlympiadMode() ? ClassListData.getInstance().getClass(this.getPlayerClass()).getClassName() : this._name;
	}
	
	public String getAccountName()
	{
		return this._client == null ? this._accountName : this._client.getAccountName();
	}
	
	public String getAccountNamePlayer()
	{
		return this._accountName;
	}
	
	public Map<Integer, String> getAccountChars()
	{
		return this._chars;
	}
	
	public long getRelation(Player target)
	{
		Clan clan = this.getClan();
		Party party = this.getParty();
		Clan targetClan = target.getClan();
		long result = 0L;
		if (clan != null)
		{
			result |= 64L;
			if (clan == target.getClan())
			{
				result |= 256L;
			}
			
			if (this.getAllyId() != 0)
			{
				result |= 65536L;
			}
		}
		
		if (this.isClanLeader())
		{
			result |= 128L;
		}
		
		if (party != null && party == target.getParty())
		{
			result |= 32L;
			
			for (int i = 0; i < party.getMembers().size(); i++)
			{
				if (party.getMembers().get(i) == this)
				{
					switch (i)
					{
						case 0:
							result |= 16L;
							break;
						case 1:
							result |= 8L;
							break;
						case 2:
							result |= 7L;
							break;
						case 3:
							result |= 6L;
							break;
						case 4:
							result |= 5L;
							break;
						case 5:
							result |= 4L;
							break;
						case 6:
							result |= 3L;
							break;
						case 7:
							result |= 2L;
							break;
						case 8:
							result |= 1L;
					}
				}
			}
		}
		
		if (this._siegeState != 0)
		{
			result |= 512L;
			if (this.getSiegeState() != target.getSiegeState())
			{
				result |= 4096L;
			}
			else
			{
				result |= 2048L;
			}
			
			if (this._siegeState == 1)
			{
				result |= 1024L;
			}
		}
		
		if (clan != null && targetClan != null && target.getPledgeType() != -1 && this.getPledgeType() != -1)
		{
			ClanWar war = clan.getWarWith(target.getClan().getId());
			if (war != null)
			{
				switch (war.getState())
				{
					case DECLARATION:
					case BLOOD_DECLARATION:
						if (war.getAttackerClanId() != target.getClanId())
						{
							result |= 8192L;
						}
						break;
					case MUTUAL:
						result |= 24576L;
				}
			}
		}
		
		if (target.getSurveillanceList().contains(this.getObjectId()))
		{
			result |= 2147483648L;
		}
		
		return result;
	}
	
	public static Player load(int objectId)
	{
		return restore(objectId);
	}
	
	private void initPcStatusUpdateValues()
	{
		this._cpUpdateInterval = this.getMaxCp() / 352.0;
		this._cpUpdateIncCheck = this.getMaxCp();
		this._cpUpdateDecCheck = this.getMaxCp() - this._cpUpdateInterval;
		this._mpUpdateInterval = this.getMaxMp() / 352.0;
		this._mpUpdateIncCheck = this.getMaxMp();
		this._mpUpdateDecCheck = this.getMaxMp() - this._mpUpdateInterval;
	}
	
	private Player(int objectId, PlayerTemplate template, String accountName, PlayerAppearance app)
	{
		super(objectId, template);
		this.setInstanceType(InstanceType.Player);
		this.initCharStatusUpdateValues();
		this.initPcStatusUpdateValues();
		
		for (int i = 0; i < this._htmlActionCaches.length; i++)
		{
			this._htmlActionCaches[i] = new LinkedList<>();
		}
		
		this._accountName = accountName;
		app.setOwner(this);
		this._appearance = app;
		this._huntPass = HuntPassConfig.ENABLE_HUNT_PASS ? new HuntPass(this) : null;
		this._achivemenetBox = AchievementBoxConfig.ENABLE_ACHIEVEMENT_BOX ? new AchievementBoxHolder(this) : null;
		this.getAI();
		this._radar = new Radar(this);
		this._challengePoints = new ChallengePoint(this);
		this._rankingHistory = new RankingHistory(this);
	}
	
	private Player(PlayerTemplate template, String accountName, PlayerAppearance app)
	{
		this(IdManager.getInstance().getNextId(), template, accountName, app);
	}
	
	@Override
	public PlayerStat getStat()
	{
		return (PlayerStat) super.getStat();
	}
	
	@Override
	public void initCharStat()
	{
		this.setStat(new PlayerStat(this));
	}
	
	@Override
	public PlayerStatus getStatus()
	{
		return (PlayerStatus) super.getStatus();
	}
	
	@Override
	public void initCharStatus()
	{
		this.setStatus(new PlayerStatus(this));
	}
	
	public PlayerAppearance getAppearance()
	{
		return this._appearance;
	}
	
	public boolean isHairAccessoryEnabled()
	{
		return this.getVariables().getBoolean("HAIR_ACCESSORY_ENABLED", true);
	}
	
	public void setHairAccessoryEnabled(boolean enabled)
	{
		this.getVariables().set("HAIR_ACCESSORY_ENABLED", enabled);
	}
	
	public int getLampExp()
	{
		return this.getVariables().getInt("MAGIC_LAMP_EXP", 0);
	}
	
	public void setLampExp(int exp)
	{
		this.getVariables().set("MAGIC_LAMP_EXP", exp);
	}
	
	public PlayerTemplate getBaseTemplate()
	{
		return PlayerTemplateData.getInstance().getTemplate(this._baseClass);
	}
	
	public HuntPass getHuntPass()
	{
		return this._huntPass;
	}
	
	public AchievementBoxHolder getAchievementBox()
	{
		return this._achivemenetBox;
	}
	
	public Collection<RankingHistoryDataHolder> getRankingHistoryData()
	{
		return this._rankingHistory.getData();
	}
	
	public ChallengePoint getChallengeInfo()
	{
		return this._challengePoints;
	}
	
	@Override
	public PlayerTemplate getTemplate()
	{
		return (PlayerTemplate) super.getTemplate();
	}
	
	public void setTemplate(PlayerClass newclass)
	{
		super.setTemplate(PlayerTemplateData.getInstance().getTemplate(newclass));
	}
	
	@Override
	protected CreatureAI initAI()
	{
		return new PlayerAI(this);
	}
	
	@Override
	public int getLevel()
	{
		return this.getStat().getLevel();
	}
	
	public void setBaseClass(int baseClass)
	{
		this._baseClass = baseClass;
	}
	
	public void setBaseClass(PlayerClass playerClass)
	{
		this._baseClass = playerClass.getId();
	}
	
	public boolean isInStoreMode()
	{
		return this._privateStoreType != PrivateStoreType.NONE;
	}
	
	public boolean isInStoreSellOrBuyMode()
	{
		return this._privateStoreType == PrivateStoreType.BUY || this._privateStoreType == PrivateStoreType.SELL || this._privateStoreType == PrivateStoreType.PACKAGE_SELL;
	}
	
	public boolean isCrafting()
	{
		return this._isCrafting;
	}
	
	public void setCrafting(boolean isCrafting)
	{
		this._isCrafting = isCrafting;
	}
	
	public Collection<RecipeList> getCommonRecipeBook()
	{
		return this._commonRecipeBook.values();
	}
	
	public Collection<RecipeList> getDwarvenRecipeBook()
	{
		return this._dwarvenRecipeBook.values();
	}
	
	public void registerCommonRecipeList(RecipeList recipe, boolean saveToDb)
	{
		this._commonRecipeBook.put(recipe.getId(), recipe);
		if (saveToDb)
		{
			this.insertNewRecipeData(recipe.getId(), false);
		}
	}
	
	public void registerDwarvenRecipeList(RecipeList recipe, boolean saveToDb)
	{
		this._dwarvenRecipeBook.put(recipe.getId(), recipe);
		if (saveToDb)
		{
			this.insertNewRecipeData(recipe.getId(), true);
		}
	}
	
	public boolean hasRecipeList(int recipeId)
	{
		return this._dwarvenRecipeBook.containsKey(recipeId) || this._commonRecipeBook.containsKey(recipeId);
	}
	
	public void unregisterRecipeList(int recipeId)
	{
		if (this._dwarvenRecipeBook.remove(recipeId) != null)
		{
			this.deleteRecipeData(recipeId, true);
		}
		else if (this._commonRecipeBook.remove(recipeId) != null)
		{
			this.deleteRecipeData(recipeId, false);
		}
		else
		{
			LOGGER.warning("Attempted to remove unknown RecipeList: " + recipeId);
		}
		
		for (Shortcut sc : this._shortcuts.getAllShortcuts())
		{
			if (sc != null && sc.getId() == recipeId && sc.getType() == ShortcutType.RECIPE)
			{
				this.deleteShortcut(sc.getSlot(), sc.getPage());
			}
		}
	}
	
	private void insertNewRecipeData(int recipeId, boolean isDwarf)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("INSERT INTO character_recipebook (charId, id, classIndex, type) values(?,?,?,?)");)
		{
			statement.setInt(1, this.getObjectId());
			statement.setInt(2, recipeId);
			statement.setInt(3, isDwarf ? this._classIndex : 0);
			statement.setInt(4, isDwarf ? 1 : 0);
			statement.execute();
		}
		catch (SQLException var11)
		{
			LOGGER.log(Level.WARNING, "SQL exception while inserting recipe: " + recipeId + " from character " + this.getObjectId(), var11);
		}
	}
	
	private void deleteRecipeData(int recipeId, boolean isDwarf)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("DELETE FROM character_recipebook WHERE charId=? AND id=? AND classIndex=?");)
		{
			statement.setInt(1, this.getObjectId());
			statement.setInt(2, recipeId);
			statement.setInt(3, isDwarf ? this._classIndex : 0);
			statement.execute();
		}
		catch (SQLException var11)
		{
			LOGGER.log(Level.WARNING, "SQL exception while deleting recipe: " + recipeId + " from character " + this.getObjectId(), var11);
		}
	}
	
	public int getLastQuestNpcObject()
	{
		return this._questNpcObject;
	}
	
	public void setLastQuestNpcObject(int npcId)
	{
		this._questNpcObject = npcId;
	}
	
	public boolean isSimulatingTalking()
	{
		return this._simulatedTalking;
	}
	
	public void setSimulatedTalking(boolean value)
	{
		this._simulatedTalking = value;
	}
	
	public QuestState getQuestState(String quest)
	{
		return this._quests.get(quest);
	}
	
	public void setQuestState(QuestState qs)
	{
		this._quests.put(qs.getQuestName(), qs);
	}
	
	public boolean hasQuestState(String quest)
	{
		return this._quests.containsKey(quest);
	}
	
	public boolean hasAnyCompletedQuestStates(List<Integer> questIds)
	{
		for (QuestState questState : this._quests.values())
		{
			if (questIds.contains(questState.getQuest().getId()) && questState.isCompleted())
			{
				return true;
			}
		}
		
		return false;
	}
	
	public void delQuestState(String quest)
	{
		this._quests.remove(quest);
	}
	
	public Collection<QuestState> getAllQuestStates()
	{
		return this._quests.values();
	}
	
	public Collection<Quest> getAllActiveQuests()
	{
		List<Quest> activeQuests = new LinkedList<>();
		
		for (QuestState questState : this._quests.values())
		{
			if (questState.isStarted())
			{
				Quest quest = questState.getQuest();
				if (quest != null && quest.getId() > 1)
				{
					activeQuests.add(quest);
				}
			}
		}
		
		return activeQuests;
	}
	
	public void processQuestEvent(String questName, String event)
	{
		Quest quest = ScriptManager.getInstance().getScript(questName);
		if (quest != null && event != null && !event.isEmpty())
		{
			Npc target = this._lastFolkNpc;
			if (target != null && this.isInsideRadius2D(target, 250))
			{
				quest.notifyEvent(event, target, this);
			}
			else if (this._questNpcObject > 0)
			{
				WorldObject object = World.getInstance().findObject(this.getLastQuestNpcObject());
				if (object != null && object.isNpc() && this.isInsideRadius2D(object, 250))
				{
					Npc npc = object.asNpc();
					quest.notifyEvent(event, npc, this);
				}
			}
		}
	}
	
	public void addNotifyQuestOfDeath(QuestState qs)
	{
		if (qs != null)
		{
			if (!this.getNotifyQuestOfDeath().contains(qs))
			{
				this.getNotifyQuestOfDeath().add(qs);
			}
		}
	}
	
	public void removeNotifyQuestOfDeath(QuestState qs)
	{
		if (qs != null && this._notifyQuestOfDeathList != null)
		{
			this._notifyQuestOfDeathList.remove(qs);
		}
	}
	
	public Set<QuestState> getNotifyQuestOfDeath()
	{
		if (this._notifyQuestOfDeathList == null)
		{
			synchronized (this)
			{
				if (this._notifyQuestOfDeathList == null)
				{
					this._notifyQuestOfDeathList = ConcurrentHashMap.newKeySet();
				}
			}
		}
		
		return this._notifyQuestOfDeathList;
	}
	
	public boolean isNotifyQuestOfDeathEmpty()
	{
		return this._notifyQuestOfDeathList == null || this._notifyQuestOfDeathList.isEmpty();
	}
	
	public void sendQuestList()
	{
		this.sendPacket(new ExQuestNotificationAll(this));
		
		for (NewQuest newQuest : NewQuestData.getInstance().getQuests())
		{
			if (newQuest.getQuestType() != 2)
			{
				Quest quest = ScriptManager.getInstance().getQuest(newQuest.getId());
				if (quest != null)
				{
					QuestState questState = this.getQuestState(quest.getName());
					if (questState == null && quest.canStartQuest(this) && !newQuest.getConditions().getSpecificStart())
					{
						this.sendPacket(new ExQuestDialog(quest.getId(), QuestDialogType.ACCEPT));
						break;
					}
				}
			}
		}
	}
	
	public Collection<Shortcut> getAllShortcuts()
	{
		return this._shortcuts.getAllShortcuts();
	}
	
	public Shortcut getShortcut(int slot, int page)
	{
		return this._shortcuts.getShortcut(slot, page);
	}
	
	public void registerShortcut(Shortcut shortcut)
	{
		this._shortcuts.registerShortcut(shortcut);
	}
	
	public void updateShortcuts(int skillId, int skillLevel, int skillSubLevel)
	{
		this._shortcuts.updateShortcuts(skillId, skillLevel, skillSubLevel);
	}
	
	public void deleteShortcut(int slot, int page)
	{
		this._shortcuts.deleteShortcut(slot, page);
	}
	
	public void registerMacro(Macro macro)
	{
		this._macros.registerMacro(macro);
	}
	
	public void deleteMacro(int id)
	{
		this._macros.deleteMacro(id);
	}
	
	public MacroList getMacros()
	{
		return this._macros;
	}
	
	public void setSiegeState(byte siegeState)
	{
		this._siegeState = siegeState;
	}
	
	public byte getSiegeState()
	{
		return this._siegeState;
	}
	
	public void setSiegeSide(int value)
	{
		this._siegeSide = value;
	}
	
	public boolean isRegisteredOnThisSiegeField(int value)
	{
		return this._siegeSide == value || this._siegeSide >= 81 && this._siegeSide <= 89;
	}
	
	public int getSiegeSide()
	{
		return this._siegeSide;
	}
	
	public boolean isSiegeFriend(WorldObject target)
	{
		if (this._siegeState != 0 && this.isInsideZone(ZoneId.SIEGE))
		{
			Castle castle = CastleManager.getInstance().getCastleById(this._siegeSide);
			if (castle == null)
			{
				return false;
			}
			Player targetPlayer = target.asPlayer();
			if (targetPlayer != null && targetPlayer != this)
			{
				if (targetPlayer.getSiegeSide() != this._siegeSide || this._siegeState != targetPlayer.getSiegeState())
				{
					return false;
				}
				else if (this._siegeState != 1)
				{
					return true;
				}
				else
				{
					return !castle.isFirstMidVictory() && this._siegeState == targetPlayer.getSiegeState() ? true : castle.getOwner() == null;
				}
			}
			return false;
		}
		return false;
	}
	
	public void setPvpFlag(int pvpFlag)
	{
		this._pvpFlag = (byte) pvpFlag;
	}
	
	@Override
	public byte getPvpFlag()
	{
		return this._pvpFlag;
	}
	
	@Override
	public void updatePvPFlag(int value)
	{
		if (this._pvpFlag != value)
		{
			this.setPvpFlag(value);
			StatusUpdate su = new StatusUpdate(this);
			this.computeStatusUpdate(su, StatusUpdateType.PVP_FLAG);
			if (su.hasUpdates())
			{
				this.broadcastPacket(su);
				this.sendPacket(su);
			}
			
			if (this.hasSummon())
			{
				RelationChanged rc = new RelationChanged();
				Summon pet = this._pet;
				if (pet != null)
				{
					rc.addRelation(pet, this.getRelation(this), false);
				}
				
				if (this.hasServitors())
				{
					this.getServitors().values().forEach(s -> rc.addRelation(s, this.getRelation(this), false));
				}
				
				this.sendPacket(rc);
			}
			
			World.getInstance().forEachVisibleObject(this, Player.class, player -> {
				if (this.isVisibleFor(player))
				{
					long relation = this.getRelation(player);
					boolean isAutoAttackable = this.isAutoAttackable(player);
					RelationCache oldrelation = this.getKnownRelations().get(player.getObjectId());
					if (oldrelation == null || oldrelation.getRelation() != relation || oldrelation.isAutoAttackable() != isAutoAttackable)
					{
						RelationChanged rcx = new RelationChanged();
						rcx.addRelation(this, relation, isAutoAttackable);
						if (this.hasSummon())
						{
							Summon petx = this._pet;
							if (petx != null)
							{
								rcx.addRelation(petx, relation, isAutoAttackable);
							}
							
							if (this.hasServitors())
							{
								this.getServitors().values().forEach(s -> rcx.addRelation(s, relation, isAutoAttackable));
							}
						}
						
						player.sendPacket(rcx);
						this.getKnownRelations().put(player.getObjectId(), new RelationCache(relation, isAutoAttackable));
					}
				}
			});
		}
	}
	
	@Override
	public void revalidateZone(boolean force)
	{
		if (this.getWorldRegion() != null)
		{
			if (force || !(this.calculateDistance3D(this._lastZoneValidateLocation) < 100.0))
			{
				this._lastZoneValidateLocation.setXYZ(this);
				ZoneManager.getInstance().getRegion(this).revalidateZones(this);
				if (GeneralConfig.ALLOW_WATER)
				{
					this.checkWaterState();
				}
				
				if (!this.isInsideZone(ZoneId.PEACE) && !this._autoUseSettings.isEmpty())
				{
					AutoUseTaskManager.getInstance().startAutoUseTask(this);
				}
				
				if (this.isInsideZone(ZoneId.ALTERED))
				{
					if (this._lastCompassZone == 7)
					{
						return;
					}
					
					this._lastCompassZone = 7;
					this.sendPacket(new ExSetCompassZoneCode(7));
				}
				else if (this.isInsideZone(ZoneId.SIEGE))
				{
					if (this._lastCompassZone == 10)
					{
						return;
					}
					
					this._lastCompassZone = 10;
					this.sendPacket(new ExSetCompassZoneCode(10));
				}
				else if (this.isInsideZone(ZoneId.PVP))
				{
					if (this._lastCompassZone == 14)
					{
						return;
					}
					
					this._lastCompassZone = 14;
					this.sendPacket(new ExSetCompassZoneCode(14));
				}
				else if (this.isInsideZone(ZoneId.PEACE))
				{
					if (this._lastCompassZone == 11)
					{
						return;
					}
					
					this._lastCompassZone = 11;
					this.sendPacket(new ExSetCompassZoneCode(11));
				}
				else if (this.isInsideZone(ZoneId.NO_PVP))
				{
					if (this._lastCompassZone == 13)
					{
						return;
					}
					
					this._lastCompassZone = 13;
					this.sendPacket(new ExSetCompassZoneCode(13));
				}
				else
				{
					if (this._lastCompassZone == 15)
					{
						return;
					}
					
					if (this._lastCompassZone == 10)
					{
						this.updatePvPStatus();
					}
					
					this._lastCompassZone = 15;
					this.sendPacket(new ExSetCompassZoneCode(15));
				}
			}
		}
	}
	
	public boolean hasDwarvenCraft()
	{
		return this.getSkillLevel(CommonSkill.CREATE_DWARVEN.getId()) >= 1;
	}
	
	public int getDwarvenCraft()
	{
		return this.getSkillLevel(CommonSkill.CREATE_DWARVEN.getId());
	}
	
	public boolean hasCommonCraft()
	{
		return this.getSkillLevel(CommonSkill.CREATE_COMMON.getId()) >= 1;
	}
	
	public int getCommonCraft()
	{
		return this.getSkillLevel(CommonSkill.CREATE_COMMON.getId());
	}
	
	public int getPkKills()
	{
		return this._pkKills;
	}
	
	public void setPkKills(int pkKills)
	{
		if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_PK_CHANGED, this))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerPKChanged(this, this._pkKills, pkKills), this);
		}
		
		this._pkKills = pkKills;
		if (this._pkKills > FeatureConfig.PK_PENALTY_LIST_MINIMUM_COUNT)
		{
			World.getInstance().addPkPlayer(this);
		}
	}
	
	public int getTotalKills()
	{
		return this._totalKills;
	}
	
	public int getTotalDeaths()
	{
		return this._totalDeaths;
	}
	
	public void setTotalKills(int value)
	{
		this._totalKills = value;
	}
	
	public void setTotalDeaths(int value)
	{
		this._totalDeaths = value;
	}
	
	public long getDeleteTimer()
	{
		return this._deleteTimer;
	}
	
	public void setDeleteTimer(long deleteTimer)
	{
		this._deleteTimer = deleteTimer;
	}
	
	public int getRecomHave()
	{
		return this._recomHave;
	}
	
	protected void incRecomHave()
	{
		if (this._recomHave < 255)
		{
			this._recomHave++;
		}
	}
	
	public void setRecomHave(int value)
	{
		this._recomHave = Math.min(Math.max(value, 0), 255);
	}
	
	public void setRecomLeft(int value)
	{
		this._recomLeft = Math.min(Math.max(value, 0), 255);
	}
	
	public int getRecomLeft()
	{
		return this._recomLeft;
	}
	
	protected void decRecomLeft()
	{
		if (this._recomLeft > 0)
		{
			this._recomLeft--;
		}
	}
	
	public void giveRecom(Player target)
	{
		target.incRecomHave();
		this.decRecomLeft();
	}
	
	public void setExpBeforeDeath(long exp)
	{
		this._expBeforeDeath = exp;
	}
	
	public long getExpBeforeDeath()
	{
		return this._expBeforeDeath;
	}
	
	public void setInitialReputation(int reputation)
	{
		super.setReputation(reputation);
	}
	
	@Override
	public void setReputation(int value)
	{
		if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_REPUTATION_CHANGED, this))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerReputationChanged(this, this.getReputation(), value), this);
		}
		
		int reputation = value;
		if (value > PvpConfig.MAX_REPUTATION)
		{
			reputation = PvpConfig.MAX_REPUTATION;
		}
		
		if (this.getReputation() != reputation)
		{
			if (this.getReputation() >= 0 && reputation < 0)
			{
				World.getInstance().forEachVisibleObject(this, Guard.class, object -> {
					if (object.getAI().getIntention() == Intention.IDLE)
					{
						object.getAI().setIntention(Intention.ACTIVE);
					}
				});
			}
			
			super.setReputation(reputation);
			this.sendPacket(new SystemMessage(SystemMessageId.YOUR_REPUTATION_HAS_BEEN_CHANGED_TO_S1).addInt(this.getReputation()));
			this.broadcastReputation();
			if (this.getReputation() >= 0)
			{
				World.getInstance().removePkPlayer(this);
			}
			
			this.applyKarmaPenalty();
		}
	}
	
	public void applyKarmaPenalty()
	{
		int expectedLevel;
		if (this.getReputation() < -288000)
		{
			expectedLevel = 10;
		}
		else if (this.getReputation() < -216000)
		{
			expectedLevel = 9;
		}
		else if (this.getReputation() < -144000)
		{
			expectedLevel = 8;
		}
		else if (this.getReputation() < -72000)
		{
			expectedLevel = 7;
		}
		else if (this.getReputation() < -36000)
		{
			expectedLevel = 6;
		}
		else if (this.getReputation() < -33840)
		{
			expectedLevel = 5;
		}
		else if (this.getReputation() < -30240)
		{
			expectedLevel = 4;
		}
		else if (this.getReputation() < -27000)
		{
			expectedLevel = 3;
		}
		else if (this.getReputation() < -18000)
		{
			expectedLevel = 2;
		}
		else if (this.getReputation() < 0)
		{
			expectedLevel = 1;
		}
		else
		{
			expectedLevel = 0;
		}
		
		if (expectedLevel > 0)
		{
			if (this._einhasadOverseeingLevel != expectedLevel)
			{
				this.getEffectList().stopSkillEffects(SkillFinishType.REMOVED, CommonSkill.EINHASAD_OVERSEEING.getId());
				SkillData.getInstance().getSkill(CommonSkill.EINHASAD_OVERSEEING.getId(), expectedLevel).applyEffects(this, this);
			}
		}
		else
		{
			this.getEffectList().stopSkillEffects(SkillFinishType.REMOVED, CommonSkill.EINHASAD_OVERSEEING.getId());
			this.getServitors().values().forEach(s -> s.getEffectList().stopSkillEffects(SkillFinishType.REMOVED, CommonSkill.EINHASAD_OVERSEEING.getId()));
			if (this.getPet() != null)
			{
				this.getPet().getEffectList().stopSkillEffects(SkillFinishType.REMOVED, CommonSkill.EINHASAD_OVERSEEING.getId());
			}
		}
		
		this._einhasadOverseeingLevel = expectedLevel;
	}
	
	public int getEinhasadOverseeingLevel()
	{
		return this._einhasadOverseeingLevel;
	}
	
	public void setEinhasadOverseeingLevel(int level)
	{
		this._einhasadOverseeingLevel = level;
	}
	
	public int getWeightPenalty()
	{
		return this._dietMode ? 0 : this._curWeightPenalty;
	}
	
	public void refreshOverloaded(boolean broadcast)
	{
		int maxLoad = this.getMaxLoad();
		if (maxLoad > 0)
		{
			long weightproc = (this.getCurrentLoad() - this.getBonusWeightPenalty()) * 1000 / this.getMaxLoad();
			int newWeightPenalty;
			if (weightproc < 500L || this._dietMode)
			{
				newWeightPenalty = 0;
			}
			else if (weightproc < 666L)
			{
				newWeightPenalty = 1;
			}
			else if (weightproc < 800L)
			{
				newWeightPenalty = 2;
			}
			else if (weightproc < 1000L)
			{
				newWeightPenalty = 3;
			}
			else
			{
				newWeightPenalty = 4;
			}
			
			if (this._curWeightPenalty != newWeightPenalty)
			{
				this._curWeightPenalty = newWeightPenalty;
				if (newWeightPenalty > 0 && !this._dietMode)
				{
					this.addSkill(SkillData.getInstance().getSkill(CommonSkill.WEIGHT_PENALTY.getId(), newWeightPenalty));
					this.setOverloaded(this.getCurrentLoad() > maxLoad);
				}
				else
				{
					this.removeSkill(this.getKnownSkill(4270), false, true);
					this.setOverloaded(false);
				}
				
				if (broadcast)
				{
					this.sendPacket(new EtcStatusUpdate(this));
					this.broadcastUserInfo();
				}
			}
		}
	}
	
	public void useEquippableItem(Item item, boolean abortAttack)
	{
		if (item != null)
		{
			if (item.getOwnerId() == this.getObjectId())
			{
				ItemLocation itemLocation = item.getItemLocation();
				if (itemLocation == ItemLocation.INVENTORY || itemLocation == ItemLocation.PAPERDOLL)
				{
					List<Item> items = null;
					boolean isEquiped = item.isEquipped();
					int oldInvLimit = this.getInventoryLimit();
					Item isFirstSlotAgathion = this.getInventory().getPaperdollItem(17);
					SystemMessage sm = null;
					if (isEquiped)
					{
						List<Integer> dualInvenotry = this.getDualInventorySet();
						if (dualInvenotry != null)
						{
							dualInvenotry.set(item.getLocationSlot(), 0);
						}
						
						BodyPart bodyPart = BodyPart.fromItem(item);
						if (item.getEnchantLevel() > 0)
						{
							if (bodyPart == BodyPart.AGATHION)
							{
								sm = new SystemMessage(SystemMessageId.S1_S2_S_POWER_WAS_SEALED);
								sm.addInt(item.getEnchantLevel());
								sm.addItemName(item);
							}
							else
							{
								sm = new SystemMessage(SystemMessageId.S1_S2_UNEQUIPPED);
								sm.addInt(item.getEnchantLevel());
								sm.addItemName(item);
							}
						}
						else if (bodyPart == BodyPart.AGATHION)
						{
							sm = new SystemMessage(SystemMessageId.S1_S_POWER_WAS_SEALED);
							sm.addItemName(item);
						}
						else
						{
							sm = new SystemMessage(SystemMessageId.S1_UNEQUIPPED);
							sm.addItemName(item);
						}
						
						this.sendPacket(sm);
						if (bodyPart != BodyPart.DECO && bodyPart != BodyPart.BROOCH_JEWEL && bodyPart != BodyPart.AGATHION && bodyPart != BodyPart.ARTIFACT)
						{
							items = this._inventory.unEquipItemInBodySlotAndRecord(bodyPart);
						}
						else
						{
							items = this._inventory.unEquipItemInSlotAndRecord(item.getLocationSlot());
						}
					}
					else
					{
						items = this._inventory.equipItemAndRecord(item);
						if (item.isEquipped())
						{
							BodyPart bodyPartx = item.getTemplate().getBodyPart();
							if (item.getEnchantLevel() > 0)
							{
								if (!item.isArmor() || bodyPartx != BodyPart.AGATHION)
								{
									sm = new SystemMessage(SystemMessageId.S1_S2_EQUIPPED);
									sm.addInt(item.getEnchantLevel());
									sm.addItemName(item);
								}
								else if (isFirstSlotAgathion != null)
								{
									sm = new SystemMessage(SystemMessageId.S1_S2_IS_SUMMONED_AS_A_SECONDARY_AGATHION);
									sm.addInt(item.getEnchantLevel());
									sm.addItemName(item);
									SystemMessage sm2 = new SystemMessage(SystemMessageId.ONLY_S1_S2_S_UNIQUE_ABILITY_BECOMES_ACTIVE);
									sm2.addInt(item.getEnchantLevel());
									sm2.addItemName(item);
									this.sendPacket(sm2);
								}
								else
								{
									sm = new SystemMessage(SystemMessageId.S1_S2_WAS_SUMMONED_AS_A_PRIMARY_AGATHION);
									sm.addInt(item.getEnchantLevel());
									sm.addItemName(item);
									SystemMessage sm2 = new SystemMessage(SystemMessageId.S1_S2_S_POWER_WAS_UNLOCKED_THEREBY_ACTIVATING_ALL_ITS_ABILITIES);
									sm2.addInt(item.getEnchantLevel());
									sm2.addItemName(item);
									this.sendPacket(sm2);
								}
							}
							else if (!item.isArmor() || bodyPartx != BodyPart.AGATHION)
							{
								sm = new SystemMessage(SystemMessageId.S1_EQUIPPED);
								sm.addItemName(item);
							}
							else if (isFirstSlotAgathion != null)
							{
								sm = new SystemMessage(SystemMessageId.S1_IS_SUMMONED_AS_A_SECONDARY_AGATHION);
								sm.addItemName(item);
								SystemMessage sm2 = new SystemMessage(SystemMessageId.ONLY_S1_S_UNIQUE_ABILITY_BECOMES_ACTIVE);
								sm2.addItemName(item);
								this.sendPacket(sm2);
							}
							else
							{
								sm = new SystemMessage(SystemMessageId.S1_WAS_SUMMONED_AS_A_PRIMARY_AGATHION);
								sm.addItemName(item);
								SystemMessage sm2 = new SystemMessage(SystemMessageId.S1_S_POWER_WAS_UNLOCKED_THEREBY_ACTIVATING_ALL_ITS_ABILITIES);
								sm2.addItemName(item);
								this.sendPacket(sm2);
							}
							
							this.sendPacket(sm);
							item.decreaseMana(false);
							if (bodyPartx == BodyPart.R_HAND || bodyPartx == BodyPart.LR_HAND)
							{
								this.rechargeShots(true, true, false);
							}
							
							if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_ITEM_EQUIP, item.getTemplate()))
							{
								EventDispatcher.getInstance().notifyEventAsync(new OnPlayerItemEquip(this, item), item.getTemplate());
							}
							
							List<Integer> dualInvenotryx = this.getDualInventorySet();
							if (dualInvenotryx != null)
							{
								dualInvenotryx.set(item.getLocationSlot(), item.getObjectId());
							}
						}
						else
						{
							this.sendPacket(SystemMessageId.NO_EQUIPMENT_SLOT_AVAILABLE);
						}
					}
					
					this.broadcastUserInfo();
					ThreadPool.schedule(() -> this.sendPacket(new ExUserInfoEquipSlot(this)), 100L);
					InventoryUpdate iu = new InventoryUpdate();
					iu.addItems(items);
					this.sendInventoryUpdate(iu);
					if (abortAttack)
					{
						this.abortAttack();
					}
					
					if (this.getInventoryLimit() != oldInvLimit)
					{
						this.sendStorageMaxCount();
					}
				}
			}
		}
	}
	
	public int getPvpKills()
	{
		return this._pvpKills;
	}
	
	public void setPvpKills(int pvpKills)
	{
		if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_PVP_CHANGED, this))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerPvPChanged(this, this._pvpKills, pvpKills), this);
		}
		
		this._pvpKills = pvpKills;
	}
	
	public int getFame()
	{
		return this._fame;
	}
	
	public void setFame(int fame)
	{
		int newFame = fame;
		if (fame > PlayerConfig.MAX_PERSONAL_FAME_POINTS)
		{
			newFame = PlayerConfig.MAX_PERSONAL_FAME_POINTS;
		}
		else if (fame < 0)
		{
			newFame = 0;
		}
		
		if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_FAME_CHANGED, this))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerFameChanged(this, this._fame, newFame), this);
		}
		
		this._fame = newFame;
	}
	
	public int getRaidbossPoints()
	{
		return this._raidbossPoints;
	}
	
	public void setRaidbossPoints(int points)
	{
		int value = points;
		if (points > 2000000000)
		{
			value = 2000000000;
		}
		else if (points < 0)
		{
			value = 0;
		}
		
		this._raidbossPoints = value;
	}
	
	public void increaseRaidbossPoints(int increasePoints)
	{
		this.setRaidbossPoints(this._raidbossPoints + increasePoints);
	}
	
	public PlayerClass getPlayerClass()
	{
		return this.getTemplate().getPlayerClass();
	}
	
	public void setPlayerClass(int id)
	{
		if (!this._subclassLock)
		{
			this._subclassLock = true;
			
			try
			{
				if (this._lvlJoinedAcademy != 0 && this._clan != null && CategoryData.getInstance().isInCategory(CategoryType.THIRD_CLASS_GROUP, id))
				{
					if (this._lvlJoinedAcademy <= 16)
					{
						this._clan.addReputationScore(FeatureConfig.JOIN_ACADEMY_MAX_REP_SCORE);
					}
					else if (this._lvlJoinedAcademy >= 39)
					{
						this._clan.addReputationScore(FeatureConfig.JOIN_ACADEMY_MIN_REP_SCORE);
					}
					else
					{
						this._clan.addReputationScore(FeatureConfig.JOIN_ACADEMY_MAX_REP_SCORE - (this._lvlJoinedAcademy - 16) * 20);
					}
					
					this.setLvlJoinedAcademy(0);
					SystemMessage msg = new SystemMessage(SystemMessageId.S1_IS_DISMISSED_FROM_THE_CLAN);
					msg.addPcName(this);
					this._clan.broadcastToOnlineMembers(msg);
					this._clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(this._name));
					this._clan.removeClanMember(this.getObjectId(), 0L);
					this.sendPacket(SystemMessageId.CONGRATULATIONS_YOU_WILL_NOW_GRADUATE_FROM_THE_CLAN_ACADEMY_AND_LEAVE_YOUR_CURRENT_CLAN_YOU_CAN_NOW_JOIN_A_CLAN_WITHOUT_BEING_SUBJECT_TO_ANY_PENALTIES);
					this._inventory.addItem(ItemProcessType.REWARD, 8181, 1L, this, null);
				}
				
				if (this.isSubClassActive())
				{
					this.getSubClasses().get(this._classIndex).setPlayerClass(id);
				}
				
				this.setTarget(this);
				this.broadcastSkillPacket(new MagicSkillUse(this, 5103, 1, 0, 0), this);
				this.setClassTemplate(id);
				if (this.getPlayerClass().level() == 3)
				{
					this.sendPacket(SystemMessageId.CONGRATULATIONS_YOU_VE_COMPLETED_YOUR_THIRD_CLASS_TRANSFER_QUEST);
					this.initElementalSpirits();
				}
				else
				{
					this.sendPacket(SystemMessageId.CONGRATULATIONS_YOU_VE_COMPLETED_THE_CLASS_CHANGE);
				}
				
				for (int slot = 1; slot < 4; slot++)
				{
					Henna henna = this.getHenna(slot);
					if (henna != null && !henna.isAllowedClass(this.asPlayer()))
					{
						this.removeHenna(slot);
					}
				}
				
				if (this.isInParty())
				{
					this._party.broadcastPacket(new PartySmallWindowUpdate(this, true));
				}
				
				if (this._clan != null)
				{
					this._clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));
				}
				
				this.sendPacket(new ExSubjobInfo(this, SubclassInfoType.CLASS_CHANGED));
				this.rewardSkills();
				if (!this.isGM() && PlayerConfig.DECREASE_SKILL_LEVEL)
				{
					this.checkPlayerSkills();
				}
				
				this.notifyFriends(3);
			}
			finally
			{
				BuffInfo info = this.getEffectList().getFirstBuffInfoByAbnormalType(AbnormalType.STAT_SKILL);
				this.getEffectList().remove(info, SkillFinishType.REMOVED, true, true);
				info = this.getEffectList().getFirstBuffInfoByAbnormalType(AbnormalType.STAT_SKILL_COMBAT);
				this.getEffectList().remove(info, SkillFinishType.REMOVED, true, true);
				info = this.getEffectList().getFirstBuffInfoByAbnormalType(AbnormalType.STAT_SKILL_PDEF);
				this.getEffectList().remove(info, SkillFinishType.REMOVED, true, true);
				info = this.getEffectList().getFirstBuffInfoByAbnormalType(AbnormalType.STAT_SKILL_MDEF);
				this.getEffectList().remove(info, SkillFinishType.REMOVED, true, true);
				info = this.getEffectList().getFirstBuffInfoByAbnormalType(AbnormalType.STAT_SKILL_PATK);
				this.getEffectList().remove(info, SkillFinishType.REMOVED, true, true);
				info = this.getEffectList().getFirstBuffInfoByAbnormalType(AbnormalType.STAT_SKILL_MATK);
				this.getEffectList().remove(info, SkillFinishType.REMOVED, true, true);
				info = this.getEffectList().getFirstBuffInfoByAbnormalType(AbnormalType.STAT_SKILL_PATK);
				this.getEffectList().remove(info, SkillFinishType.REMOVED, true, true);
				info = this.getEffectList().getFirstBuffInfoByAbnormalType(AbnormalType.STAT_SKILL_STR);
				this.getEffectList().remove(info, SkillFinishType.REMOVED, true, true);
				info = this.getEffectList().getFirstBuffInfoByAbnormalType(AbnormalType.STAT_SKILL_DEX);
				this.getEffectList().remove(info, SkillFinishType.REMOVED, true, true);
				info = this.getEffectList().getFirstBuffInfoByAbnormalType(AbnormalType.STAT_SKILL_INT);
				this.getEffectList().remove(info, SkillFinishType.REMOVED, true, true);
				info = this.getEffectList().getFirstBuffInfoByAbnormalType(AbnormalType.STAT_SKILL_WIT);
				this.getEffectList().remove(info, SkillFinishType.REMOVED, true, true);
				info = this.getEffectList().getFirstBuffInfoByAbnormalType(AbnormalType.STAT_SKILL_DEX);
				this.getEffectList().remove(info, SkillFinishType.REMOVED, true, true);
				info = this.getEffectList().getFirstBuffInfoByAbnormalType(AbnormalType.STAT_SKILL_CON);
				this.getEffectList().remove(info, SkillFinishType.REMOVED, true, true);
				this._subclassLock = false;
				CharInfoTable.getInstance().setClassId(this.getObjectId(), id);
				ThreadPool.schedule(() -> {
					this.getInventory().applyItemSkills();
					this.getStat().recalculateStats(false);
					this.updateAbnormalVisualEffects();
					this.sendSkillList();
				}, 100L);
			}
		}
	}
	
	public boolean isChangingClass()
	{
		return this._subclassLock;
	}
	
	public long getExp()
	{
		return this.getStat().getExp();
	}
	
	public void setFistsWeaponItem(Weapon weaponItem)
	{
		this._fistsWeaponItem = weaponItem;
	}
	
	public Weapon getFistsWeaponItem()
	{
		return this._fistsWeaponItem;
	}
	
	public Weapon findFistsWeaponItem(int classId)
	{
		Weapon weaponItem = null;
		if (classId >= 0 && classId <= 9)
		{
			ItemTemplate temp = ItemData.getInstance().getTemplate(246);
			weaponItem = (Weapon) temp;
		}
		else if (classId >= 10 && classId <= 17)
		{
			ItemTemplate temp = ItemData.getInstance().getTemplate(251);
			weaponItem = (Weapon) temp;
		}
		else if (classId >= 18 && classId <= 24)
		{
			ItemTemplate temp = ItemData.getInstance().getTemplate(244);
			weaponItem = (Weapon) temp;
		}
		else if (classId >= 25 && classId <= 30)
		{
			ItemTemplate temp = ItemData.getInstance().getTemplate(249);
			weaponItem = (Weapon) temp;
		}
		else if (classId >= 31 && classId <= 37)
		{
			ItemTemplate temp = ItemData.getInstance().getTemplate(245);
			weaponItem = (Weapon) temp;
		}
		else if (classId >= 38 && classId <= 43)
		{
			ItemTemplate temp = ItemData.getInstance().getTemplate(250);
			weaponItem = (Weapon) temp;
		}
		else if (classId >= 44 && classId <= 48)
		{
			ItemTemplate temp = ItemData.getInstance().getTemplate(248);
			weaponItem = (Weapon) temp;
		}
		else if (classId >= 49 && classId <= 52)
		{
			ItemTemplate temp = ItemData.getInstance().getTemplate(252);
			weaponItem = (Weapon) temp;
		}
		else if (classId >= 53 && classId <= 57)
		{
			ItemTemplate temp = ItemData.getInstance().getTemplate(247);
			weaponItem = (Weapon) temp;
		}
		
		return weaponItem;
	}
	
	public void rewardSkills()
	{
		if (PlayerConfig.AUTO_LEARN_SKILLS)
		{
			this.giveAvailableSkills(PlayerConfig.AUTO_LEARN_FS_SKILLS, true, PlayerConfig.AUTO_LEARN_SKILLS_WITHOUT_ITEMS);
		}
		else
		{
			this.giveAvailableAutoGetSkills();
		}
		
		if (PlayerConfig.DECREASE_SKILL_LEVEL && !this.isGM())
		{
			this.checkPlayerSkills();
		}
		
		for (SkillLearn skill : SkillTreeData.getInstance().getRaceSkillTree(this.getRace()))
		{
			if (this.getLevel() >= skill.getGetLevel())
			{
				this.addSkill(SkillData.getInstance().getSkill(skill.getSkillId(), skill.getSkillLevel()), true);
			}
		}
		
		if (FreeMountsConfig.ENABLE_FREE_STRIDER)
		{
			Skill skillx = SkillData.getInstance().getSkill(49991, 1);
			if (skillx != null)
			{
				this.addSkill(skillx, false);
			}
		}
		
		if (FreeMountsConfig.ENABLE_FREE_WYVERN)
		{
			Skill skillx = SkillData.getInstance().getSkill(49992, 1);
			if (skillx != null)
			{
				this.addSkill(skillx, false);
			}
		}
		
		this.checkItemRestriction();
		this.sendSkillList();
		this.restoreAutoShortcutVisual();
	}
	
	public void regiveTemporarySkills()
	{
		if (this.isNoble())
		{
			this.setNoble(true);
		}
		
		if (this._hero)
		{
			this.setHero(true);
		}
		
		if (this._clan != null)
		{
			this._clan.addSkillEffects(this);
			if (this._clan.getLevel() >= SiegeManager.getInstance().getSiegeClanMinLevel() && this.isClanLeader())
			{
				SiegeManager.getInstance().addSiegeSkills(this);
			}
			
			if (this._clan.getCastleId() > 0)
			{
				Castle castle = CastleManager.getInstance().getCastleByOwner(this._clan);
				if (castle != null)
				{
					castle.giveResidentialSkills(this);
				}
			}
			
			if (this._clan.getFortId() > 0)
			{
				Fort fort = FortManager.getInstance().getFortByOwner(this._clan);
				if (fort != null)
				{
					fort.giveResidentialSkills(this);
				}
			}
		}
		
		this._inventory.reloadEquippedItems();
	}
	
	public int giveAvailableSkills(boolean includeByFs, boolean includeAutoGet, boolean includeRequiredItems)
	{
		int skillCounter = 0;
		Collection<Skill> skills = SkillTreeData.getInstance().getAllAvailableSkills(this, this.getTemplate().getPlayerClass(), includeByFs, includeAutoGet, includeRequiredItems);
		List<Skill> skillsForStore = new ArrayList<>();
		
		for (Skill skill : skills)
		{
			int skillId = skill.getId();
			Skill oldSkill = this.getKnownSkill(skillId);
			if (oldSkill != skill && this.getReplacementSkill(skillId) == skillId)
			{
				if (this.getSkillLevel(skillId) == 0)
				{
					skillCounter++;
				}
				
				if (skill.isToggle() && !skill.isNecessaryToggle() && this.isAffectedBySkill(skillId))
				{
					this.stopSkillEffects(SkillFinishType.REMOVED, skillId);
				}
				
				int skillLevel = skill.getLevel();
				if (oldSkill != null && oldSkill.getSubLevel() > 0 && skill.getSubLevel() == 0 && oldSkill.getLevel() < skillLevel)
				{
					skill = SkillData.getInstance().getSkill(skillId, skillLevel, oldSkill.getSubLevel());
				}
				
				this.addSkill(skill, false);
				skillsForStore.add(skill);
				if (PlayerConfig.AUTO_LEARN_SKILLS)
				{
					this.updateShortcuts(skillId, skillLevel, skill.getSubLevel());
				}
			}
		}
		
		this.storeSkills(skillsForStore, -1);
		if (PlayerConfig.AUTO_LEARN_SKILLS && skillCounter > 0)
		{
			this.sendMessage("You have learned " + skillCounter + " new skills.");
		}
		
		this.restoreAutoShortcutVisual();
		return skillCounter;
	}
	
	public void giveAvailableAutoGetSkills()
	{
		List<SkillLearn> autoGetSkills = SkillTreeData.getInstance().getAvailableAutoGetSkills(this);
		SkillData st = SkillData.getInstance();
		
		for (SkillLearn s : autoGetSkills)
		{
			Skill skill = st.getSkill(s.getSkillId(), s.getSkillLevel());
			if (skill != null)
			{
				this.addSkill(skill, true);
			}
			else
			{
				LOGGER.warning("Skipping null auto-get skill for " + this);
			}
		}
	}
	
	public void setExp(long exp)
	{
		this.getStat().setExp(Math.max(0L, exp));
	}
	
	@Override
	public Race getRace()
	{
		return !this.isSubClassActive() ? this.getTemplate().getRace() : PlayerTemplateData.getInstance().getTemplate(this._baseClass).getRace();
	}
	
	public Radar getRadar()
	{
		return this._radar;
	}
	
	public boolean isMinimapAllowed()
	{
		return this._minimapAllowed;
	}
	
	public void setMinimapAllowed(boolean value)
	{
		this._minimapAllowed = value;
	}
	
	public long getSp()
	{
		return this.getStat().getSp();
	}
	
	public void setSp(long sp)
	{
		super.getStat().setSp(Math.max(0L, sp));
	}
	
	public boolean isCastleLord(int castleId)
	{
		if (this._clan != null && this._clan.getLeader().getPlayer() == this)
		{
			Castle castle = CastleManager.getInstance().getCastleByOwner(this._clan);
			if (castle != null && castle == CastleManager.getInstance().getCastleById(castleId))
			{
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public int getClanId()
	{
		return this._clanId;
	}
	
	public int getClanCrestId()
	{
		return this._clan != null ? this._clan.getCrestId() : 0;
	}
	
	public int getClanCrestLargeId()
	{
		return this._clan == null || this._clan.getCastleId() == 0 && this._clan.getHideoutId() == 0 ? 0 : this._clan.getCrestLargeId();
	}
	
	public long getClanJoinExpiryTime()
	{
		return this._clanJoinExpiryTime;
	}
	
	public void setClanJoinExpiryTime(long time)
	{
		this._clanJoinExpiryTime = time;
	}
	
	public long getClanJoinTime()
	{
		return this.getVariables().getLong("CLAN_JOIN_TIME", 0L);
	}
	
	public void setClanJoinTime(long time)
	{
		this.getVariables().set("CLAN_JOIN_TIME", time);
	}
	
	public long getClanCreateExpiryTime()
	{
		return this._clanCreateExpiryTime;
	}
	
	public void setClanCreateExpiryTime(long time)
	{
		this._clanCreateExpiryTime = time;
	}
	
	public void setOnlineTime(long time)
	{
		this._onlineTime = time;
		this._onlineBeginTime = System.currentTimeMillis();
	}
	
	public int getOnlineTimeMillis()
	{
		return (int) (System.currentTimeMillis() - this._onlineBeginTime);
	}
	
	@Override
	public PlayerInventory getInventory()
	{
		return this._inventory;
	}
	
	public void removeItemFromShortcut(int objectId)
	{
		this._shortcuts.deleteShortcutByObjectId(objectId);
	}
	
	public boolean isSitting()
	{
		return this._waitTypeSitting;
	}
	
	public void setSitting(boolean value)
	{
		this._waitTypeSitting = value;
	}
	
	public void setSittingProgress(boolean value)
	{
		this._sittingInProgress = value;
	}
	
	public void sitDown()
	{
		this.sitDown(true);
	}
	
	public void sitDown(boolean checkCast)
	{
		if (!this._sittingInProgress)
		{
			if (checkCast && this.isCastingNow())
			{
				this.sendMessage("Cannot sit while casting.");
			}
			else
			{
				if (!this._waitTypeSitting && !this.isAttackDisabled() && !this.isControlBlocked() && !this.isImmobilized() && !this.isFishing())
				{
					this.breakAttack();
					this.setSitting(true);
					this.setSittingProgress(true);
					this.getAI().setIntention(Intention.REST);
					this.broadcastPacket(new ChangeWaitType(this, 0));
					ThreadPool.schedule(new SitDownTask(this), 2500L);
				}
			}
		}
	}
	
	public void standUp()
	{
		if (!this._sittingInProgress)
		{
			if (this._waitTypeSitting && !this.isInStoreMode() && !this.isAlikeDead())
			{
				this.setSittingProgress(true);
				if (this.getEffectList().isAffected(EffectFlag.RELAXING))
				{
					this.stopEffects(EffectFlag.RELAXING);
				}
				
				this.broadcastPacket(new ChangeWaitType(this, 1));
				ThreadPool.schedule(new StandUpTask(this), 2500L);
			}
		}
	}
	
	public PlayerWarehouse getWarehouse()
	{
		return this._warehouse;
	}
	
	public PlayerFreight getFreight()
	{
		return this._freight;
	}
	
	public boolean hasRefund()
	{
		return this._refund != null && this._refund.getSize() > 0 && GeneralConfig.ALLOW_REFUND;
	}
	
	public PlayerRefund getRefund()
	{
		if (this._refund == null)
		{
			this._refund = new PlayerRefund(this);
		}
		
		return this._refund;
	}
	
	public void clearRefund()
	{
		if (this._refund != null)
		{
			this._refund.deleteMe();
		}
		
		this._refund = null;
	}
	
	public long getAdena()
	{
		return this._inventory.getAdena();
	}
	
	public long getAncientAdena()
	{
		return this._inventory.getAncientAdena();
	}
	
	public long getBeautyTickets()
	{
		return this._inventory.getBeautyTickets();
	}
	
	public void addAdena(ItemProcessType process, long count, WorldObject reference, boolean sendMessage)
	{
		long currentAdena = this._inventory.getAdena();
		long limitRemaining = PlayerConfig.MAX_ADENA - currentAdena;
		long amountToAdd = count;
		if (count > limitRemaining)
		{
			count = limitRemaining;
		}
		
		if (sendMessage)
		{
			if (count == 0L && amountToAdd > 0L)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_FAILED_TO_ACQUIRE_S1_ADENA);
				sm.addLong(amountToAdd);
				this.sendPacket(sm);
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1_ADENA_2);
				sm.addLong(count);
				this.sendPacket(sm);
			}
		}
		
		if (count > 0L)
		{
			this._inventory.addAdena(process, count, this, reference);
			if (count == this.getAdena())
			{
				this.sendItemList();
			}
			else
			{
				InventoryUpdate iu = new InventoryUpdate();
				iu.addModifiedItem(this._inventory.getAdenaInstance());
				this.sendInventoryUpdate(iu);
			}
		}
		
		if (this._inventory.getAdena() == PlayerConfig.MAX_ADENA && sendMessage)
		{
			this.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_YOUR_OUT_OF_POCKET_ADENA_LIMIT);
		}
	}
	
	public boolean reduceAdena(ItemProcessType process, long count, WorldObject reference, boolean sendMessage)
	{
		if (count > this._inventory.getAdena())
		{
			if (sendMessage)
			{
				this.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA);
			}
			
			return false;
		}
		if (count > 0L)
		{
			Item adenaItem = this._inventory.getAdenaInstance();
			if (!this._inventory.reduceAdena(process, count, this, reference))
			{
				return false;
			}
			
			InventoryUpdate iu = new InventoryUpdate();
			iu.addItem(adenaItem);
			this.sendInventoryUpdate(iu);
			if (sendMessage)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_SPENT_S1_ADENA);
				sm.addLong(count);
				this.sendPacket(sm);
			}
		}
		
		return true;
	}
	
	public boolean reduceBeautyTickets(ItemProcessType process, long count, WorldObject reference, boolean sendMessage)
	{
		if (count > this._inventory.getBeautyTickets())
		{
			if (sendMessage)
			{
				this.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
			}
			
			return false;
		}
		if (count > 0L)
		{
			Item beautyTickets = this._inventory.getBeautyTicketsInstance();
			if (!this._inventory.reduceBeautyTickets(process, count, this, reference))
			{
				return false;
			}
			
			InventoryUpdate iu = new InventoryUpdate();
			iu.addItem(beautyTickets);
			this.sendInventoryUpdate(iu);
			if (sendMessage)
			{
				if (count > 1L)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_X_S2_DISAPPEARED);
					sm.addItemName(36308);
					sm.addLong(count);
					this.sendPacket(sm);
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISAPPEARED);
					sm.addItemName(36308);
					this.sendPacket(sm);
				}
			}
		}
		
		return true;
	}
	
	public void addAncientAdena(ItemProcessType process, long count, WorldObject reference, boolean sendMessage)
	{
		if (sendMessage)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1_X_S2);
			sm.addItemName(5575);
			sm.addLong(count);
			this.sendPacket(sm);
		}
		
		if (count > 0L)
		{
			this._inventory.addAncientAdena(process, count, this, reference);
			InventoryUpdate iu = new InventoryUpdate();
			iu.addItem(this._inventory.getAncientAdenaInstance());
			this.sendInventoryUpdate(iu);
		}
	}
	
	public boolean reduceAncientAdena(ItemProcessType process, long count, WorldObject reference, boolean sendMessage)
	{
		if (count > this._inventory.getAncientAdena())
		{
			if (sendMessage)
			{
				this.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA);
			}
			
			return false;
		}
		if (count > 0L)
		{
			Item ancientAdenaItem = this._inventory.getAncientAdenaInstance();
			if (!this._inventory.reduceAncientAdena(process, count, this, reference))
			{
				return false;
			}
			
			InventoryUpdate iu = new InventoryUpdate();
			iu.addItem(ancientAdenaItem);
			this.sendInventoryUpdate(iu);
			if (sendMessage)
			{
				if (count > 1L)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_X_S2_DISAPPEARED);
					sm.addItemName(5575);
					sm.addLong(count);
					this.sendPacket(sm);
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISAPPEARED);
					sm.addItemName(5575);
					this.sendPacket(sm);
				}
			}
		}
		
		return true;
	}
	
	public void addItem(ItemProcessType process, Item item, WorldObject reference, boolean sendMessage)
	{
		if (item.getCount() > 0L)
		{
			if (sendMessage)
			{
				if (item.getCount() > 1L)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1_X_S2);
					sm.addItemName(item);
					sm.addLong(item.getCount());
					this.sendPacket(sm);
				}
				else if (item.getEnchantLevel() > 0)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1_S2);
					sm.addInt(item.getEnchantLevel());
					sm.addItemName(item);
					this.sendPacket(sm);
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1);
					sm.addItemName(item);
					this.sendPacket(sm);
				}
			}
			
			Item newitem = this._inventory.addItem(process, item, this, reference);
			if (this.isGM() || this._inventory.validateCapacity(0L, item.isQuestItem()) || !newitem.isDropable() || newitem.isStackable() && newitem.getLastChange() == 2)
			{
				if (CursedWeaponsManager.getInstance().isCursed(newitem.getId()))
				{
					CursedWeaponsManager.getInstance().activate(this, newitem);
				}
				else if (FortSiegeManager.getInstance().isCombat(item.getId()) && FortSiegeManager.getInstance().activateCombatFlag(this, item))
				{
					Fort fort = FortManager.getInstance().getFort(this);
					fort.getSiege().announceToPlayer(new SystemMessage(SystemMessageId.C1_HAS_ACQUIRED_THE_FLAG), this._name);
				}
			}
			else
			{
				this.dropItem(ItemProcessType.DROP, newitem, null, true, true);
			}
		}
	}
	
	public Item addItem(ItemProcessType process, int itemId, long count, WorldObject reference, boolean sendMessage)
	{
		return this.addItem(process, itemId, count, -1, reference, sendMessage);
	}
	
	public Item addItem(ItemProcessType process, int itemId, long count, int enchantLevel, WorldObject reference, boolean sendMessage)
	{
		if (count > 0L)
		{
			ItemTemplate item = ItemData.getInstance().getTemplate(itemId);
			if (item == null)
			{
				LOGGER.severe("Item doesn't exist so cannot be added. Item ID: " + itemId);
				return null;
			}
			
			if (sendMessage && (!this.isCastingNow() && item.hasExImmediateEffect() || !item.hasExImmediateEffect()))
			{
				if (count > 1L)
				{
					if (process != ItemProcessType.SWEEP && process != ItemProcessType.QUEST)
					{
						SystemMessage sm;
						if (enchantLevel > 0)
						{
							sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1_S2_X_S3);
							sm.addInt(enchantLevel);
						}
						else
						{
							sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1_X_S2);
						}
						
						sm.addItemName(itemId);
						sm.addLong(count);
						this.sendPacket(sm);
					}
					else
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1_X_S2);
						sm.addItemName(itemId);
						sm.addLong(count);
						this.sendPacket(sm);
					}
				}
				else if (process != ItemProcessType.SWEEP && process != ItemProcessType.QUEST)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1);
					sm.addItemName(itemId);
					this.sendPacket(sm);
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1_2);
					sm.addItemName(itemId);
					this.sendPacket(sm);
				}
			}
			
			if (!item.hasExImmediateEffect() || !item.isEtcItem())
			{
				Item createdItem = this._inventory.addItem(process, itemId, count, this, reference);
				if (enchantLevel > -1)
				{
					createdItem.setEnchantLevel(enchantLevel);
				}
				
				if (this.isGM() || this._inventory.validateCapacity(0L, item.isQuestItem()) || !createdItem.isDropable() || createdItem.isStackable() && createdItem.getLastChange() == 2)
				{
					if (CursedWeaponsManager.getInstance().isCursed(createdItem.getId()))
					{
						CursedWeaponsManager.getInstance().activate(this, createdItem);
					}
				}
				else
				{
					this.dropItem(ItemProcessType.DROP, createdItem, null, true);
				}
				
				return createdItem;
			}
			
			for (SkillHolder skillHolder : item.getAllSkills())
			{
				SkillCaster.triggerCast(this, null, skillHolder.getSkill(), null, false);
			}
			
			this.broadcastInfo();
		}
		
		return null;
	}
	
	public void addItem(ItemProcessType process, ItemHolder item, WorldObject reference, boolean sendMessage)
	{
		this.addItem(process, item.getId(), item.getCount(), reference, sendMessage);
	}
	
	public boolean destroyItem(ItemProcessType process, Item item, WorldObject reference, boolean sendMessage)
	{
		return this.destroyItem(process, item, item.getCount(), reference, sendMessage);
	}
	
	public boolean destroyItem(ItemProcessType process, Item item, long count, WorldObject reference, boolean sendMessage)
	{
		Item destoyedItem = this._inventory.destroyItem(process, item, count, this, reference);
		if (destoyedItem == null)
		{
			if (sendMessage)
			{
				this.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
			}
			
			return false;
		}
		InventoryUpdate playerIU = new InventoryUpdate();
		if (destoyedItem.isStackable() && destoyedItem.getCount() > 0L)
		{
			playerIU.addModifiedItem(destoyedItem);
		}
		else
		{
			playerIU.addRemovedItem(destoyedItem);
		}
		
		this.sendInventoryUpdate(playerIU);
		if (item.getId() == 91663)
		{
			this.sendPacket(new ExBloodyCoinCount(this));
		}
		
		if (sendMessage)
		{
			if (count > 1L)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_X_S2_DISAPPEARED);
				sm.addItemName(destoyedItem);
				sm.addLong(count);
				this.sendPacket(sm);
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISAPPEARED);
				sm.addItemName(destoyedItem);
				this.sendPacket(sm);
			}
		}
		
		return true;
	}
	
	@Override
	public boolean destroyItem(ItemProcessType process, int objectId, long count, WorldObject reference, boolean sendMessage)
	{
		Item item = this._inventory.getItemByObjectId(objectId);
		if (item != null && item.getCount() >= count)
		{
			return this.destroyItem(process, item, count, reference, sendMessage);
		}
		if (sendMessage)
		{
			this.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
		}
		
		return false;
	}
	
	@Override
	public boolean destroyItemByItemId(ItemProcessType process, int itemId, long count, WorldObject reference, boolean sendMessage)
	{
		if (itemId == 57)
		{
			return this.reduceAdena(process, count, reference, sendMessage);
		}
		Item item = this._inventory.getItemByItemId(itemId);
		if (item == null)
		{
			if (sendMessage)
			{
				this.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
			}
			
			return false;
		}
		long itemCount = item.isStackable() ? item.getCount() : this._inventory.getInventoryItemCount(itemId, -1);
		long removeCount = count < 0L ? itemCount : count;
		if (removeCount > 0L && itemCount >= removeCount && this._inventory.destroyItemByItemId(process, itemId, removeCount, this, reference) != null)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			if (item.isStackable() && itemCount > 0L && itemCount != removeCount)
			{
				playerIU.addModifiedItem(item);
			}
			else
			{
				playerIU.addRemovedItem(item);
			}
			
			this.sendInventoryUpdate(playerIU);
			if (sendMessage)
			{
				if (removeCount > 1L)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_X_S2_DISAPPEARED);
					sm.addItemName(itemId);
					sm.addLong(removeCount);
					this.sendPacket(sm);
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISAPPEARED);
					sm.addItemName(itemId);
					this.sendPacket(sm);
				}
			}
			
			return true;
		}
		if (sendMessage)
		{
			this.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
		}
		
		return false;
	}
	
	public Item transferItem(ItemProcessType process, int objectId, long count, Inventory target, WorldObject reference)
	{
		Item oldItem = this.checkItemManipulation(objectId, count, "transfer");
		if (oldItem == null)
		{
			return null;
		}
		Item newItem = this._inventory.transferItem(process, objectId, count, target, this, reference);
		if (newItem == null)
		{
			return null;
		}
		InventoryUpdate playerIU = new InventoryUpdate();
		if (oldItem.getCount() > 0L && oldItem != newItem)
		{
			playerIU.addModifiedItem(oldItem);
		}
		else
		{
			playerIU.addRemovedItem(oldItem);
		}
		
		this.sendInventoryUpdate(playerIU);
		if (target instanceof PlayerInventory)
		{
			Player targetPlayer = ((PlayerInventory) target).getOwner();
			InventoryUpdate targetIU = new InventoryUpdate();
			if (newItem.getCount() > count)
			{
				targetIU.addModifiedItem(newItem);
			}
			else
			{
				targetIU.addNewItem(newItem);
			}
			
			targetPlayer.sendPacket(targetIU);
		}
		
		if (newItem.getId() == 91663)
		{
			this.sendPacket(new ExBloodyCoinCount(this));
		}
		
		return newItem;
	}
	
	public boolean exchangeItemsById(ItemProcessType process, WorldObject reference, int coinId, long cost, int rewardId, long count, boolean sendMessage)
	{
		if (!this._inventory.validateCapacityByItemId(rewardId, count))
		{
			if (sendMessage)
			{
				this.sendPacket(SystemMessageId.YOUR_INVENTORY_IS_FULL);
			}
			
			return false;
		}
		else if (!this._inventory.validateWeightByItemId(rewardId, count))
		{
			if (sendMessage)
			{
				this.sendPacket(SystemMessageId.WEIGHT_LIMIT_IS_EXCEEDED);
			}
			
			return false;
		}
		else if (this.destroyItemByItemId(process, coinId, cost, reference, sendMessage))
		{
			this.addItem(process, rewardId, count, reference, sendMessage);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean dropItem(ItemProcessType process, Item item, WorldObject reference, boolean sendMessage, boolean protectItem)
	{
		Item droppedItem = this._inventory.dropItem(process, item, this, reference);
		if (droppedItem == null)
		{
			if (sendMessage)
			{
				this.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
			}
			
			return false;
		}
		droppedItem.dropMe(this, this.getX() + Rnd.get(50) - 25, this.getY() + Rnd.get(50) - 25, this.getZ() + 20);
		if (GeneralConfig.AUTODESTROY_ITEM_AFTER > 0 && GeneralConfig.DESTROY_DROPPED_PLAYER_ITEM && !GeneralConfig.LIST_PROTECTED_ITEMS.contains(droppedItem.getId()) && (droppedItem.isEquipable() && GeneralConfig.DESTROY_EQUIPABLE_PLAYER_ITEM || !droppedItem.isEquipable()))
		{
			ItemsAutoDestroyTaskManager.getInstance().addItem(droppedItem);
		}
		
		if (GeneralConfig.DESTROY_DROPPED_PLAYER_ITEM)
		{
			droppedItem.setProtected(droppedItem.isEquipable() && (!droppedItem.isEquipable() || !GeneralConfig.DESTROY_EQUIPABLE_PLAYER_ITEM));
		}
		else
		{
			droppedItem.setProtected(true);
		}
		
		if (protectItem)
		{
			droppedItem.getDropProtection().protect(this);
		}
		
		InventoryUpdate playerIU = new InventoryUpdate();
		playerIU.addItem(droppedItem);
		this.sendInventoryUpdate(playerIU);
		if (sendMessage)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_DROPPED_S1);
			sm.addItemName(droppedItem);
			this.sendPacket(sm);
		}
		
		if (item.getId() == 91663)
		{
			this.sendPacket(new ExBloodyCoinCount(this));
		}
		
		return true;
	}
	
	public boolean dropItem(ItemProcessType process, Item item, WorldObject reference, boolean sendMessage)
	{
		return this.dropItem(process, item, reference, sendMessage, false);
	}
	
	public Item dropItem(ItemProcessType process, int objectId, long count, int x, int y, int z, WorldObject reference, boolean sendMessage, boolean protectItem)
	{
		Item invitem = this._inventory.getItemByObjectId(objectId);
		Item item = this._inventory.dropItem(process, objectId, count, this, reference);
		if (item == null)
		{
			if (sendMessage)
			{
				this.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
			}
			
			return null;
		}
		item.dropMe(this, x, y, z);
		if (GeneralConfig.AUTODESTROY_ITEM_AFTER > 0 && GeneralConfig.DESTROY_DROPPED_PLAYER_ITEM && !GeneralConfig.LIST_PROTECTED_ITEMS.contains(item.getId()) && (item.isEquipable() && GeneralConfig.DESTROY_EQUIPABLE_PLAYER_ITEM || !item.isEquipable()))
		{
			ItemsAutoDestroyTaskManager.getInstance().addItem(item);
		}
		
		if (GeneralConfig.DESTROY_DROPPED_PLAYER_ITEM)
		{
			item.setProtected(item.isEquipable() && (!item.isEquipable() || !GeneralConfig.DESTROY_EQUIPABLE_PLAYER_ITEM));
		}
		else
		{
			item.setProtected(true);
		}
		
		if (protectItem)
		{
			item.getDropProtection().protect(this);
		}
		
		InventoryUpdate playerIU = new InventoryUpdate();
		playerIU.addItem(invitem);
		this.sendInventoryUpdate(playerIU);
		if (sendMessage)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_DROPPED_S1);
			sm.addItemName(item);
			this.sendPacket(sm);
		}
		
		if (item.getId() == 91663)
		{
			this.sendPacket(new ExBloodyCoinCount(this));
		}
		
		return item;
	}
	
	public Item checkItemManipulation(int objectId, long count, String action)
	{
		if (World.getInstance().findObject(objectId) == null)
		{
			LOGGER.finest(this.getObjectId() + ": player tried to " + action + " item not available in World");
			return null;
		}
		Item item = this._inventory.getItemByObjectId(objectId);
		if (item == null || item.getOwnerId() != this.getObjectId())
		{
			LOGGER.finest(this.getObjectId() + ": player tried to " + action + " item he is not owner of");
			return null;
		}
		else if (count >= 0L && (count <= 1L || item.isStackable()))
		{
			if (count > item.getCount())
			{
				LOGGER.finest(this.getObjectId() + ": player tried to " + action + " more items than he owns");
				return null;
			}
			else if ((this._pet == null || this._pet.getControlObjectId() != objectId) && this._mountObjectID != objectId)
			{
				if (this.isProcessingItem(objectId))
				{
					return null;
				}
				return item.isAugmented() && this.isCastingNow() ? null : item;
			}
			else
			{
				return null;
			}
		}
		else
		{
			LOGGER.finest(this.getObjectId() + ": player tried to " + action + " item with invalid count: " + count);
			return null;
		}
	}
	
	public boolean isSpawnProtected()
	{
		return this._spawnProtectEndTime != 0L && this._spawnProtectEndTime > System.currentTimeMillis();
	}
	
	public boolean isTeleportProtected()
	{
		return this._teleportProtectEndTime != 0L && this._teleportProtectEndTime > System.currentTimeMillis();
	}
	
	public void setSpawnProtection(boolean protect)
	{
		this._spawnProtectEndTime = protect ? System.currentTimeMillis() + PlayerConfig.PLAYER_SPAWN_PROTECTION * 1000 : 0L;
	}
	
	public void setTeleportProtection(boolean protect)
	{
		this._teleportProtectEndTime = protect ? System.currentTimeMillis() + PlayerConfig.PLAYER_TELEPORT_PROTECTION * 1000 : 0L;
	}
	
	public void setRecentFakeDeath(boolean protect)
	{
		this._recentFakeDeathEndTime = protect ? GameTimeTaskManager.getInstance().getGameTicks() + PlayerConfig.PLAYER_FAKEDEATH_UP_PROTECTION * 10 : 0L;
	}
	
	public boolean isRecentFakeDeath()
	{
		return this._recentFakeDeathEndTime > GameTimeTaskManager.getInstance().getGameTicks();
	}
	
	public boolean isFakeDeath()
	{
		return this.isAffected(EffectFlag.FAKE_DEATH);
	}
	
	@Override
	public boolean isAlikeDead()
	{
		return super.isAlikeDead() || this.isFakeDeath();
	}
	
	public GameClient getClient()
	{
		return this._client;
	}
	
	public void setClient(GameClient client)
	{
		this._client = client;
		if (this._client != null && this._client.getIp() != null)
		{
			this._ip = this._client.getIp();
		}
	}
	
	public String getIPAddress()
	{
		return this._ip;
	}
	
	public Location getCurrentSkillWorldPosition()
	{
		return this._currentSkillWorldPosition;
	}
	
	public void setCurrentSkillWorldPosition(Location worldPosition)
	{
		this._currentSkillWorldPosition = worldPosition;
	}
	
	public void enableSkill(Skill skill, boolean removeTimeStamp)
	{
		super.enableSkill(skill);
		if (removeTimeStamp)
		{
			this.removeTimeStamp(skill);
		}
	}
	
	@Override
	public void enableSkill(Skill skill)
	{
		this.enableSkill(skill, true);
	}
	
	private boolean needCpUpdate()
	{
		double currentCp = this.getCurrentCp();
		if (currentCp <= 1.0 || this.getMaxCp() < 352.0)
		{
			return true;
		}
		else if (!(currentCp <= this._cpUpdateDecCheck) && !(currentCp >= this._cpUpdateIncCheck))
		{
			return false;
		}
		else
		{
			if (currentCp == this.getMaxCp())
			{
				this._cpUpdateIncCheck = currentCp + 1.0;
				this._cpUpdateDecCheck = currentCp - this._cpUpdateInterval;
			}
			else
			{
				double doubleMulti = currentCp / this._cpUpdateInterval;
				int intMulti = (int) doubleMulti;
				this._cpUpdateDecCheck = this._cpUpdateInterval * (doubleMulti < intMulti ? intMulti - 1 : intMulti);
				this._cpUpdateIncCheck = this._cpUpdateDecCheck + this._cpUpdateInterval;
			}
			
			return true;
		}
	}
	
	private boolean needMpUpdate()
	{
		double currentMp = this.getCurrentMp();
		if (currentMp <= 1.0 || this.getMaxMp() < 352.0)
		{
			return true;
		}
		else if (!(currentMp <= this._mpUpdateDecCheck) && !(currentMp >= this._mpUpdateIncCheck))
		{
			return false;
		}
		else
		{
			if (currentMp == this.getMaxMp())
			{
				this._mpUpdateIncCheck = currentMp + 1.0;
				this._mpUpdateDecCheck = currentMp - this._mpUpdateInterval;
			}
			else
			{
				double doubleMulti = currentMp / this._mpUpdateInterval;
				int intMulti = (int) doubleMulti;
				this._mpUpdateDecCheck = this._mpUpdateInterval * (doubleMulti < intMulti ? intMulti - 1 : intMulti);
				this._mpUpdateIncCheck = this._mpUpdateDecCheck + this._mpUpdateInterval;
			}
			
			return true;
		}
	}
	
	@Override
	public void broadcastStatusUpdate(Creature caster)
	{
		if (this._broadcastStatusUpdateTask == null)
		{
			this._broadcastStatusUpdateTask = ThreadPool.schedule(() -> {
				StatusUpdate su = new StatusUpdate(this);
				if (caster != null)
				{
					su.addCaster(caster);
				}
				
				this.computeStatusUpdate(su, StatusUpdateType.LEVEL);
				this.computeStatusUpdate(su, StatusUpdateType.MAX_HP);
				this.computeStatusUpdate(su, StatusUpdateType.CUR_HP);
				this.computeStatusUpdate(su, StatusUpdateType.MAX_MP);
				this.computeStatusUpdate(su, StatusUpdateType.CUR_MP);
				this.computeStatusUpdate(su, StatusUpdateType.MAX_CP);
				this.computeStatusUpdate(su, StatusUpdateType.CUR_CP);
				if (su.hasUpdates())
				{
					this.broadcastPacket(su);
				}
				
				boolean needCpUpdate = this.needCpUpdate();
				boolean needHpUpdate = this.needHpUpdate();
				boolean needMpUpdate = this.needMpUpdate();
				Party party = this.getParty();
				if (this._party != null && (needCpUpdate || needHpUpdate || needMpUpdate))
				{
					PartySmallWindowUpdate partyWindow = new PartySmallWindowUpdate(this, false);
					if (needCpUpdate)
					{
						partyWindow.addComponentType(PartySmallWindowUpdateType.CURRENT_CP);
						partyWindow.addComponentType(PartySmallWindowUpdateType.MAX_CP);
					}
					
					if (needHpUpdate)
					{
						partyWindow.addComponentType(PartySmallWindowUpdateType.CURRENT_HP);
						partyWindow.addComponentType(PartySmallWindowUpdateType.MAX_HP);
					}
					
					if (needMpUpdate)
					{
						partyWindow.addComponentType(PartySmallWindowUpdateType.CURRENT_MP);
						partyWindow.addComponentType(PartySmallWindowUpdateType.MAX_MP);
					}
					
					party.broadcastToPartyMembers(this, partyWindow);
				}
				
				if (this._inOlympiadMode && this._olympiadStart && (needCpUpdate || needHpUpdate))
				{
					OlympiadGameTask game = OlympiadGameManager.getInstance().getOlympiadTask(this.getOlympiadGameId());
					if (game != null && game.isBattleStarted())
					{
						game.getStadium().broadcastStatusUpdate(this);
					}
				}
				
				if (this._isInDuel && (needCpUpdate || needHpUpdate))
				{
					DuelManager.getInstance().broadcastToOppositTeam(this, new ExDuelUpdateUserInfo(this));
				}
				
				this._broadcastStatusUpdateTask = null;
			}, 50L);
		}
	}
	
	public void broadcastUserInfo()
	{
		this.updateUserInfo();
		this.broadcastCharInfo();
	}
	
	public void updateUserInfo()
	{
		this.sendPacket(new UserInfo(this));
		this.sendPacket(new ExUserViewInfoParameter(this));
	}
	
	public void broadcastUserInfo(UserInfoType... types)
	{
		UserInfo ui = new UserInfo(this, false);
		ui.addComponentType(types);
		this.sendPacket(ui);
		this.broadcastCharInfo();
	}
	
	public void broadcastCharInfo()
	{
		if (this.isOnlineInt() != 0)
		{
			if (this._broadcastCharInfoTask == null)
			{
				this._broadcastCharInfoTask = ThreadPool.schedule(() -> {
					CharInfo charInfo = new CharInfo(this, false);
					charInfo.sendInBroadcast();
					World.getInstance().forEachVisibleObject(this, Player.class, player -> {
						if (this.isVisibleFor(player))
						{
							if (this.isInvisible() && this.isGM())
							{
								player.sendPacket(new CharInfo(this, true));
							}
							else
							{
								player.sendPacket(charInfo);
							}
							
							long relation = this.getRelation(player);
							boolean isAutoAttackable = this.isAutoAttackable(player);
							RelationCache oldrelation = this.getKnownRelations().get(player.getObjectId());
							if (oldrelation == null || oldrelation.getRelation() != relation || oldrelation.isAutoAttackable() != isAutoAttackable)
							{
								RelationChanged rc = new RelationChanged();
								rc.addRelation(this, relation, isAutoAttackable);
								if (this.hasSummon())
								{
									Summon pet = this.getPet();
									if (pet != null)
									{
										rc.addRelation(pet, relation, isAutoAttackable);
									}
									
									if (this.hasServitors())
									{
										this.getServitors().values().forEach(s -> rc.addRelation(s, relation, isAutoAttackable));
									}
								}
								
								player.sendPacket(rc);
								this.getKnownRelations().put(player.getObjectId(), new RelationCache(relation, isAutoAttackable));
							}
						}
					});
					this._broadcastCharInfoTask = null;
				}, 100L);
			}
		}
	}
	
	public void broadcastTitleInfo()
	{
		this.broadcastUserInfo(UserInfoType.CLAN);
		this.broadcastPacket(new NicknameChanged(this));
	}
	
	@Override
	public void broadcastPacket(ServerPacket packet, boolean includeSelf)
	{
		if (packet instanceof CharInfo)
		{
			new IllegalArgumentException("CharInfo is being send via broadcastPacket. Do NOT do that! Use broadcastCharInfo() instead.");
		}
		
		packet.sendInBroadcast();
		if (includeSelf)
		{
			this.sendPacket(packet);
		}
		
		World.getInstance().forEachVisibleObject(this, Player.class, player -> {
			if (this.isVisibleFor(player))
			{
				player.sendPacket(packet);
			}
		});
	}
	
	@Override
	public int getAllyId()
	{
		return this._clan == null ? 0 : this._clan.getAllyId();
	}
	
	public int getAllyCrestId()
	{
		return this.getAllyId() == 0 ? 0 : this._clan.getAllyCrestId();
	}
	
	@Override
	public void sendPacket(ServerPacket packet)
	{
		if (this._client != null)
		{
			this._client.sendPacket(packet);
		}
	}
	
	@Override
	public void sendPacket(SystemMessageId id)
	{
		this.sendPacket(new SystemMessage(id));
	}
	
	public void doInteract(Creature target)
	{
		if (target != null)
		{
			if (target.isPlayer())
			{
				Player targetPlayer = target.asPlayer();
				this.sendPacket(ActionFailed.STATIC_PACKET);
				if (targetPlayer.getPrivateStoreType() != PrivateStoreType.SELL && targetPlayer.getPrivateStoreType() != PrivateStoreType.PACKAGE_SELL)
				{
					if (targetPlayer.getPrivateStoreType() == PrivateStoreType.BUY)
					{
						this.sendPacket(new PrivateStoreListBuy(this, targetPlayer));
					}
					else if (targetPlayer.getPrivateStoreType() == PrivateStoreType.MANUFACTURE)
					{
						this.sendPacket(new RecipeShopSellList(this, targetPlayer));
					}
				}
				else if (this._isSellingBuffs)
				{
					SellBuffsManager.getInstance().sendBuffMenu(this, targetPlayer, 0);
				}
				else
				{
					this.sendPacket(new PrivateStoreListSell(this, targetPlayer));
				}
			}
			else
			{
				target.onAction(this);
			}
		}
	}
	
	public void doAutoLoot(Attackable target, int itemId, long itemCount)
	{
		if (this.isInParty() && !ItemData.getInstance().getTemplate(itemId).hasExImmediateEffect())
		{
			this._party.distributeItem(this, itemId, itemCount, false, target);
		}
		else if (itemId == 57)
		{
			this.addAdena(ItemProcessType.LOOT, itemCount, target, true);
		}
		else
		{
			this.addItem(ItemProcessType.LOOT, itemId, itemCount, target, true);
		}
	}
	
	public void doAutoLoot(Attackable target, ItemHolder item)
	{
		this.doAutoLoot(target, item.getId(), item.getCount());
	}
	
	@Override
	public void doPickupItem(WorldObject object)
	{
		if (!this.isAlikeDead() && !this.isFakeDeath())
		{
			if (this.getActiveTradeList() != null)
			{
				this.sendPacket(SystemMessageId.YOU_CANNOT_PICK_UP_OR_USE_ITEMS_WHILE_TRADING);
				this.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else
			{
				this.getAI().setIntention(Intention.IDLE);
				if (!object.isItem())
				{
					LOGGER.warning(this + " trying to pickup wrong target." + this.getTarget());
				}
				else
				{
					Item target = (Item) object;
					this.sendPacket(ActionFailed.STATIC_PACKET);
					this.sendPacket(new StopMove(this));
					SystemMessage smsg = null;
					synchronized (target)
					{
						if (!target.isSpawned())
						{
							this.sendPacket(ActionFailed.STATIC_PACKET);
							return;
						}
						
						if (!target.getDropProtection().tryPickUp(this))
						{
							this.sendPacket(ActionFailed.STATIC_PACKET);
							smsg = new SystemMessage(SystemMessageId.YOU_HAVE_FAILED_TO_PICK_UP_S1);
							smsg.addItemName(target);
							this.sendPacket(smsg);
							return;
						}
						
						if ((this.isInParty() && this._party.getDistributionType() == PartyDistributionType.FINDERS_KEEPERS || !this.isInParty()) && !this._inventory.validateCapacity(target))
						{
							this.sendPacket(ActionFailed.STATIC_PACKET);
							this.sendPacket(SystemMessageId.YOUR_INVENTORY_IS_FULL);
							return;
						}
						
						if (this.isInvul() && !this.isGM())
						{
							this.sendPacket(ActionFailed.STATIC_PACKET);
							smsg = new SystemMessage(SystemMessageId.YOU_HAVE_FAILED_TO_PICK_UP_S1);
							smsg.addItemName(target);
							this.sendPacket(smsg);
							return;
						}
						
						if (target.getOwnerId() != 0 && target.getOwnerId() != this.getObjectId() && !this.isInLooterParty(target.getOwnerId()))
						{
							if (target.getId() == 57)
							{
								smsg = new SystemMessage(SystemMessageId.YOU_HAVE_FAILED_TO_PICK_UP_S1_ADENA);
								smsg.addLong(target.getCount());
							}
							else if (target.getCount() > 1L)
							{
								smsg = new SystemMessage(SystemMessageId.YOU_HAVE_FAILED_TO_PICK_UP_S2_S1_S);
								smsg.addItemName(target);
								smsg.addLong(target.getCount());
							}
							else
							{
								smsg = new SystemMessage(SystemMessageId.YOU_HAVE_FAILED_TO_PICK_UP_S1);
								smsg.addItemName(target);
							}
							
							this.sendPacket(ActionFailed.STATIC_PACKET);
							this.sendPacket(smsg);
							return;
						}
						
						if (FortSiegeManager.getInstance().isCombat(target.getId()) && !FortSiegeManager.getInstance().checkIfCanPickup(this))
						{
							return;
						}
						
						if (target.getItemLootShedule() != null && (target.getOwnerId() == this.getObjectId() || this.isInLooterParty(target.getOwnerId())))
						{
							target.resetOwnerTimer();
						}
						
						target.pickupMe(this);
						if (GeneralConfig.SAVE_DROPPED_ITEM)
						{
							ItemsOnGroundManager.getInstance().removeObject(target);
						}
					}
					
					if (target.getTemplate().hasExImmediateEffect())
					{
						IItemHandler handler = ItemHandler.getInstance().getHandler(target.getEtcItem());
						if (handler == null)
						{
							LOGGER.warning("No item handler registered for item ID: " + target.getId() + ".");
						}
						else
						{
							handler.onItemUse(this, target, false);
						}
						
						ItemManager.destroyItem(ItemProcessType.NONE, target, this, null);
					}
					else if (CursedWeaponsManager.getInstance().isCursed(target.getId()))
					{
						this.addItem(ItemProcessType.PICKUP, target, null, true);
					}
					else if (FortSiegeManager.getInstance().isCombat(target.getId()))
					{
						this.addItem(ItemProcessType.PICKUP, target, null, true);
					}
					else
					{
						if (target.getItemType() instanceof ArmorType || target.getItemType() instanceof WeaponType)
						{
							if (target.getEnchantLevel() > 0)
							{
								smsg = new SystemMessage(SystemMessageId.ATTENTION_C1_HAS_PICKED_UP_S2_S3);
								smsg.addPcName(this);
								smsg.addInt(target.getEnchantLevel());
								smsg.addItemName(target.getId());
								this.broadcastPacket(smsg);
							}
							else
							{
								smsg = new SystemMessage(SystemMessageId.ATTENTION_C1_HAS_PICKED_UP_S2);
								smsg.addPcName(this);
								smsg.addItemName(target.getId());
								this.broadcastPacket(smsg);
							}
						}
						
						if (this.isInParty())
						{
							this._party.distributeItem(this, target);
						}
						else if (target.getId() == 57 && this._inventory.getAdenaInstance() != null)
						{
							this.addAdena(ItemProcessType.PICKUP, target.getCount(), null, true);
							ItemManager.destroyItem(ItemProcessType.PICKUP, target, this, null);
						}
						else
						{
							this.addItem(ItemProcessType.PICKUP, target, null, true);
						}
					}
				}
			}
		}
	}
	
	@Override
	public void doAutoAttack(Creature target)
	{
		super.doAutoAttack(target);
		this.setRecentFakeDeath(false);
		if (target.isFakePlayer() && !FakePlayersConfig.FAKE_PLAYER_AUTO_ATTACKABLE)
		{
			this.updatePvPStatus();
		}
	}
	
	@Override
	public void doCast(Skill skill)
	{
		super.doCast(skill);
		this.setRecentFakeDeath(false);
	}
	
	public boolean canOpenPrivateStore()
	{
		if (PrivateStoreRangeConfig.SHOP_MIN_RANGE_FROM_NPC > 0 || PrivateStoreRangeConfig.SHOP_MIN_RANGE_FROM_PLAYER > 0)
		{
			for (Creature creature : World.getInstance().getVisibleObjectsInRange(this, Creature.class, 1000))
			{
				if (creature.getMinShopDistance() > 0 && LocationUtil.checkIfInRange(creature.getMinShopDistance(), this, creature, true))
				{
					this.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_OPEN_A_PRIVATE_STORE_HERE));
					return false;
				}
			}
		}
		
		return !this._isSellingBuffs && !this.isAlikeDead() && !this._inOlympiadMode && !this.isMounted() && !this.isInsideZone(ZoneId.NO_STORE) && !this.isCastingNow();
	}
	
	@Override
	public int getMinShopDistance()
	{
		return this._waitTypeSitting ? PrivateStoreRangeConfig.SHOP_MIN_RANGE_FROM_PLAYER : 0;
	}
	
	public void tryOpenPrivateBuyStore()
	{
		if (this.canOpenPrivateStore())
		{
			if (this._privateStoreType == PrivateStoreType.BUY || this._privateStoreType == PrivateStoreType.BUY_MANAGE)
			{
				this.setPrivateStoreType(PrivateStoreType.NONE);
			}
			
			if (this._privateStoreType == PrivateStoreType.NONE)
			{
				if (this._waitTypeSitting)
				{
					this.standUp();
				}
				
				this.setPrivateStoreType(PrivateStoreType.BUY_MANAGE);
				this.sendPacket(new PrivateStoreManageListBuy(1, this));
				this.sendPacket(new PrivateStoreManageListBuy(2, this));
			}
		}
		else
		{
			if (this.isInsideZone(ZoneId.NO_STORE))
			{
				this.sendPacket(SystemMessageId.YOU_CANNOT_OPEN_A_PRIVATE_STORE_HERE);
			}
			
			this.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	public PreparedMultisellListHolder getMultiSell()
	{
		return this._currentMultiSell;
	}
	
	public void setMultiSell(PreparedMultisellListHolder list)
	{
		this._currentMultiSell = list;
	}
	
	@Override
	public void setTarget(WorldObject worldObject)
	{
		WorldObject newTarget = worldObject;
		if (worldObject != null)
		{
			boolean isInParty = worldObject.isPlayer() && this.isInParty() && this._party.containsPlayer(worldObject.asPlayer());
			if (!isInParty && Math.abs(worldObject.getZ() - this.getZ()) > 3000)
			{
				newTarget = null;
			}
			
			if (newTarget != null && !isInParty && !newTarget.isSpawned())
			{
				newTarget = null;
			}
			
			if (!this.isGM() && newTarget instanceof Vehicle)
			{
				newTarget = null;
			}
		}
		
		WorldObject oldTarget = this.getTarget();
		if (oldTarget != null)
		{
			if (oldTarget.equals(newTarget))
			{
				if (newTarget != null && newTarget.getObjectId() != this.getObjectId())
				{
					this.sendPacket(new ValidateLocation(newTarget));
				}
				
				return;
			}
			
			oldTarget.removeStatusListener(this);
		}
		
		if (newTarget != null && newTarget.isCreature())
		{
			Creature target = newTarget.asCreature();
			if (newTarget.getObjectId() != this.getObjectId())
			{
				this.sendPacket(new ValidateLocation(target));
			}
			
			this.sendPacket(new MyTargetSelected(this, target));
			target.addStatusListener(this);
			StatusUpdate su = new StatusUpdate(target);
			su.addUpdate(StatusUpdateType.MAX_HP, target.getMaxHp());
			su.addUpdate(StatusUpdateType.CUR_HP, (long) target.getCurrentHp());
			this.sendPacket(su);
			Broadcast.toKnownPlayers(this, new TargetSelected(this.getObjectId(), newTarget.getObjectId(), this.getX(), this.getY(), this.getZ()));
			this.sendPacket(new ExAbnormalStatusUpdateFromTarget(target));
		}
		
		if (newTarget == null && this.getTarget() != null)
		{
			this.broadcastPacket(new TargetUnselected(this));
		}
		
		super.setTarget(newTarget);
	}
	
	@Override
	public Item getActiveWeaponInstance()
	{
		return this._inventory.getPaperdollItem(5);
	}
	
	@Override
	public Weapon getActiveWeaponItem()
	{
		Item weapon = this.getActiveWeaponInstance();
		return weapon == null ? this._fistsWeaponItem : (Weapon) weapon.getTemplate();
	}
	
	public Item getChestArmorInstance()
	{
		return this._inventory.getPaperdollItem(6);
	}
	
	public Item getLegsArmorInstance()
	{
		return this._inventory.getPaperdollItem(11);
	}
	
	public Armor getActiveChestArmorItem()
	{
		Item armor = this.getChestArmorInstance();
		return armor == null ? null : (Armor) armor.getTemplate();
	}
	
	public Armor getActiveLegsArmorItem()
	{
		Item legs = this.getLegsArmorInstance();
		return legs == null ? null : (Armor) legs.getTemplate();
	}
	
	public boolean isWearingHeavyArmor()
	{
		Item legs = this.getLegsArmorInstance();
		Item armor = this.getChestArmorInstance();
		return armor != null && legs != null && legs.getItemType() == ArmorType.HEAVY && armor.getItemType() == ArmorType.HEAVY ? true : armor != null && this._inventory.getPaperdollItem(6).getTemplate().getBodyPart() == BodyPart.FULL_ARMOR && armor.getItemType() == ArmorType.HEAVY;
	}
	
	public boolean isWearingLightArmor()
	{
		Item legs = this.getLegsArmorInstance();
		Item armor = this.getChestArmorInstance();
		return armor != null && legs != null && legs.getItemType() == ArmorType.LIGHT && armor.getItemType() == ArmorType.LIGHT ? true : armor != null && this._inventory.getPaperdollItem(6).getTemplate().getBodyPart() == BodyPart.FULL_ARMOR && armor.getItemType() == ArmorType.LIGHT;
	}
	
	public boolean isWearingMagicArmor()
	{
		Item legs = this.getLegsArmorInstance();
		Item armor = this.getChestArmorInstance();
		return armor != null && legs != null && legs.getItemType() == ArmorType.MAGIC && armor.getItemType() == ArmorType.MAGIC ? true : armor != null && this._inventory.getPaperdollItem(6).getTemplate().getBodyPart() == BodyPart.FULL_ARMOR && armor.getItemType() == ArmorType.MAGIC;
	}
	
	public boolean isMarried()
	{
		return this._married;
	}
	
	public void setMarried(boolean value)
	{
		this._married = value;
	}
	
	public boolean isEngageRequest()
	{
		return this._engagerequest;
	}
	
	public void setEngageRequest(boolean state, int playerid)
	{
		this._engagerequest = state;
		this._engageid = playerid;
	}
	
	public void setMarryRequest(boolean value)
	{
		this._marryrequest = value;
	}
	
	public boolean isMarryRequest()
	{
		return this._marryrequest;
	}
	
	public void setMarryAccepted(boolean value)
	{
		this._marryaccepted = value;
	}
	
	public boolean isMarryAccepted()
	{
		return this._marryaccepted;
	}
	
	public int getEngageId()
	{
		return this._engageid;
	}
	
	public int getPartnerId()
	{
		return this._partnerId;
	}
	
	public void setPartnerId(int partnerid)
	{
		this._partnerId = partnerid;
	}
	
	public int getCoupleId()
	{
		return this._coupleId;
	}
	
	public void setCoupleId(int coupleId)
	{
		this._coupleId = coupleId;
	}
	
	public void engageAnswer(int answer)
	{
		if (this._engagerequest && this._engageid != 0)
		{
			Player ptarget = World.getInstance().getPlayer(this._engageid);
			this.setEngageRequest(false, 0);
			if (ptarget != null)
			{
				if (answer == 1)
				{
					CoupleManager.getInstance().createCouple(ptarget, this);
					ptarget.sendMessage("Request to Engage has been >ACCEPTED<");
				}
				else
				{
					ptarget.sendMessage("Request to Engage has been >DENIED<!");
				}
			}
		}
	}
	
	@Override
	public Item getSecondaryWeaponInstance()
	{
		return this._inventory.getPaperdollItem(7);
	}
	
	@Override
	public ItemTemplate getSecondaryWeaponItem()
	{
		Item item = this._inventory.getPaperdollItem(7);
		return item != null ? item.getTemplate() : null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean doDie(Creature killer)
	{
		if (this.hasRequest(AutoPeelRequest.class))
		{
			this.sendPacket(new ExStopItemAutoPeel(true));
			this.sendPacket(new ExReadyItemAutoPeel(false, 0));
			this.removeRequest(AutoPeelRequest.class);
		}
		
		Collection<Item> droppedItems = null;
		if (killer != null)
		{
			Player pk = killer.asPlayer();
			boolean fpcKill = killer.isFakePlayer();
			if (pk != null || fpcKill)
			{
				if (pk != null)
				{
					if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_PVP_KILL, this))
					{
						EventDispatcher.getInstance().notifyEventAsync(new OnPlayerPvPKill(pk, this), this);
					}
					
					this.setTotalDeaths(this.getTotalDeaths() + 1);
					if (pk != this)
					{
						RevengeHistoryManager.getInstance().addNewKill(this, pk);
					}
					
					if ((!PvpRewardItemConfig.DISABLE_REWARDS_IN_INSTANCES || this.getInstanceId() == 0) && (!PvpRewardItemConfig.DISABLE_REWARDS_IN_PVP_ZONES || !this.isInsideZone(ZoneId.PVP)))
					{
						if (PvpRewardItemConfig.REWARD_PVP_ITEM && this._pvpFlag != 0)
						{
							pk.addItem(ItemProcessType.REWARD, PvpRewardItemConfig.REWARD_PVP_ITEM_ID, PvpRewardItemConfig.REWARD_PVP_ITEM_AMOUNT, this, PvpRewardItemConfig.REWARD_PVP_ITEM_MESSAGE);
						}
						
						if (PvpRewardItemConfig.REWARD_PK_ITEM && this._pvpFlag == 0)
						{
							pk.addItem(ItemProcessType.REWARD, PvpRewardItemConfig.REWARD_PK_ITEM_ID, PvpRewardItemConfig.REWARD_PK_ITEM_AMOUNT, this, PvpRewardItemConfig.REWARD_PK_ITEM_MESSAGE);
						}
					}
				}
				
				if (PvpAnnounceConfig.ANNOUNCE_PK_PVP && (pk != null && !pk.isGM() || fpcKill))
				{
					String msg = "";
					if (this._pvpFlag == 0)
					{
						msg = PvpAnnounceConfig.ANNOUNCE_PK_MSG.replace("$killer", pk != null ? pk.getName() : killer.getName()).replace("$target", this._name);
						if (PvpAnnounceConfig.ANNOUNCE_PK_PVP_NORMAL_MESSAGE)
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.S1_3);
							sm.addString(msg);
							Broadcast.toAllOnlinePlayers(sm);
						}
						else
						{
							Broadcast.toAllOnlinePlayers(msg, false);
						}
					}
					else if (this._pvpFlag != 0)
					{
						msg = PvpAnnounceConfig.ANNOUNCE_PVP_MSG.replace("$killer", killer.getName()).replace("$target", this._name);
						if (PvpAnnounceConfig.ANNOUNCE_PK_PVP_NORMAL_MESSAGE)
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.S1_3);
							sm.addString(msg);
							Broadcast.toAllOnlinePlayers(sm);
						}
						else
						{
							Broadcast.toAllOnlinePlayers(msg, false);
						}
					}
				}
				
				if (fpcKill && FakePlayersConfig.FAKE_PLAYER_KILL_KARMA && this._pvpFlag == 0 && this.getReputation() >= 0)
				{
					killer.setReputation(killer.getReputation() - 150);
				}
			}
			
			this.broadcastStatusUpdate();
			this.setExpBeforeDeath(0L);
			if (!super.doDie(killer))
			{
				return false;
			}
			
			if (this.isCursedWeaponEquipped())
			{
				CursedWeaponsManager.getInstance().drop(this._cursedWeaponEquippedId, killer);
			}
			else if (this._combatFlagEquippedId)
			{
				Fort fort = FortManager.getInstance().getFort(this);
				if (fort != null)
				{
					FortSiegeManager.getInstance().dropCombatFlag(this, fort.getResidenceId());
				}
				else
				{
					BodyPart bodyPart = BodyPart.fromItem(this._inventory.getItemByItemId(93331));
					this._inventory.unEquipItemInBodySlot(bodyPart);
					this.destroyItem(ItemProcessType.DESTROY, this._inventory.getItemByItemId(93331), null, true);
				}
			}
			else
			{
				boolean insidePvpZone = this.isInsideZone(ZoneId.PVP) || this.isInsideZone(ZoneId.SIEGE);
				if (pk == null || !pk.isCursedWeaponEquipped())
				{
					droppedItems = this.onDieDropItem(killer);
					if (!insidePvpZone && pk != null)
					{
						Clan pkClan = pk.getClan();
						if (pkClan != null && this._clan != null && !this.isAcademyMember() && !pk.isAcademyMember())
						{
							ClanWar clanWar = this._clan.getWarWith(pkClan.getId());
							if (clanWar != null && AntiFeedManager.getInstance().check(killer, this))
							{
								clanWar.onKill(pk, this);
							}
						}
					}
					
					if (!this.isLucky() && !insidePvpZone && !this.isOnEvent())
					{
						this.calculateDeathExpPenalty(killer);
					}
				}
			}
		}
		
		this.sendPacket(new ExDieInfo((Collection<Item>) (droppedItems == null ? Collections.emptyList() : droppedItems), this._lastDamageTaken));
		if (this.isMounted())
		{
			this.stopFeed();
		}
		
		if (this.isFakeDeath())
		{
			this.stopFakeDeath(true);
		}
		
		if (!this._cubics.isEmpty())
		{
			this._cubics.values().forEach(Cubic::deactivate);
			this._cubics.clear();
		}
		
		for (Npc npc : this.getSummonedNpcs())
		{
			if (npc instanceof Doppelganger)
			{
				npc.deleteMe();
			}
		}
		
		if (this.isChannelized())
		{
			this.getSkillChannelized().abortChannelization();
		}
		
		if (this._agathionId != 0)
		{
			this.setAgathionId(0);
		}
		
		if (this.hasServitors())
		{
			this.getServitors().values().forEach(servitor -> {
				if (servitor.isBetrayed())
				{
					this.sendPacket(SystemMessageId.YOUR_SERVITOR_IS_UNRESPONSIVE_AND_WILL_NOT_OBEY_ANY_ORDERS);
				}
				else
				{
					servitor.cancelAction();
				}
			});
		}
		
		this.stopRentPet();
		this.stopWaterTask();
		AntiFeedManager.getInstance().setLastDeathTime(this.getObjectId());
		if (this.getReputation() < 0)
		{
			int newRep = this.getReputation() - this.getReputation() / 4;
			this.setReputation(newRep < -20 ? newRep : 0);
		}
		
		if (this.isInTimedHuntingZone())
		{
			DecayTaskManager.getInstance().add(this);
			this.sendPacket(new TimeRestrictFieldDieLimitTime());
		}
		else if (PlayerConfig.DISCONNECT_AFTER_DEATH)
		{
			DecayTaskManager.getInstance().add(this);
		}
		
		return true;
	}
	
	public void addDamageTaken(Creature attacker, int skillId, double damage, boolean isDOT, boolean reflect)
	{
		if (attacker != this)
		{
			synchronized (this._lastDamageTaken)
			{
				this._lastDamageTaken.add(new DamageTakenHolder(attacker, skillId, damage, isDOT, reflect));
				if (this._lastDamageTaken.size() > 20)
				{
					this._lastDamageTaken.removeFirst();
				}
			}
		}
	}
	
	public void clearDamageTaken()
	{
		synchronized (this._lastDamageTaken)
		{
			this._lastDamageTaken.clear();
		}
	}
	
	public ItemPenalty getItemPenalty()
	{
		return this._itemPenalty;
	}
	
	public List<ItemPenaltyHolder> getItemPenaltyList()
	{
		return this._itemPenaltyList;
	}
	
	private void restoreItemPenaltyList()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM item_restore WHERE ownerId=?");)
		{
			ps.setInt(1, this.getObjectId());
			
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					int objectId = rs.getInt("objectId");
					Date date = rs.getDate("dateLost");
					int killerObj = rs.getInt("killerObj");
					this._itemPenaltyList.add(new ItemPenaltyHolder(objectId, killerObj, date));
				}
			}
		}
		catch (SQLException var13)
		{
			LOGGER.warning("Could not restore item_restore table. " + var13.getMessage());
		}
	}
	
	private void addPenaltyItem(Item item, int killerObjectId)
	{
		Date date = new Date(System.currentTimeMillis());
		Item itemTransfer = this.getInventory().transferItem(ItemProcessType.TRANSFER, item.getObjectId(), item.getCount(), this._itemPenalty, this, null);
		if (this._itemPenaltyList.size() < 30)
		{
			this._itemPenaltyList.add(new ItemPenaltyHolder(itemTransfer.getObjectId(), killerObjectId, date));
		}
		else
		{
			ItemPenaltyHolder itemToDelete = this._itemPenaltyList.getFirst();
			Item newItem = this._itemPenalty.getItemByObjectId(itemToDelete.getItemObjectId());
			newItem.setOwnerId(0);
			newItem.updateDatabase(true);
			this._itemPenaltyList.removeFirst();
			this._itemPenaltyList.add(new ItemPenaltyHolder(itemTransfer.getObjectId(), killerObjectId, date));
		}
		
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("INSERT INTO item_restore (ownerId, objectId, dateLost, killerObj) VALUES (?,?,?,?)");)
		{
			statement.setInt(1, this.getObjectId());
			statement.setInt(2, itemTransfer.getObjectId());
			statement.setDate(3, date);
			statement.setInt(4, killerObjectId);
			statement.execute();
		}
		catch (SQLException var13)
		{
			LOGGER.warning("Could not insert item to item_restore table. " + var13.getMessage());
		}
	}
	
	public void removePenaltyItem(ItemPenaltyHolder holder)
	{
		this._itemPenaltyList.remove(holder);
		
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM item_restore WHERE objectId=?");)
		{
			ps.setInt(1, holder.getItemObjectId());
			ps.execute();
		}
		catch (SQLException var10)
		{
			LOGGER.warning("Could not remove item from item_restore table. " + var10.getMessage());
		}
	}
	
	private Collection<Item> onDieDropItem(Creature killer)
	{
		List<Item> droppedItems = new ArrayList<>();
		if (!this.isOnEvent() && killer != null)
		{
			Player pk = killer.asPlayer();
			if (this.getReputation() >= 0 && pk != null && pk.getClan() != null && this.getClan() != null && pk.getClan().isAtWarWith(this._clanId))
			{
				return droppedItems;
			}
			if ((!this.isInsideZone(ZoneId.PVP) || pk == null) && (!this.isGM() || PvpConfig.KARMA_DROP_GM))
			{
				int dropEquip = 0;
				int dropEquipWeapon = 0;
				int dropItem = 0;
				int dropLimit = 0;
				int dropPercent = 0;
				if (killer.isPlayable() && this.getReputation() < 0 && this._pkKills >= PvpConfig.KARMA_PK_LIMIT)
				{
					dropPercent = RatesConfig.KARMA_RATE_DROP;
					dropEquip = RatesConfig.KARMA_RATE_DROP_EQUIP;
					dropEquipWeapon = RatesConfig.KARMA_RATE_DROP_EQUIP_WEAPON;
					dropItem = RatesConfig.KARMA_RATE_DROP_ITEM;
					dropLimit = RatesConfig.KARMA_DROP_LIMIT;
				}
				else if (killer.isNpc())
				{
					dropPercent = RatesConfig.PLAYER_RATE_DROP;
					dropEquip = RatesConfig.PLAYER_RATE_DROP_EQUIP;
					dropEquipWeapon = RatesConfig.PLAYER_RATE_DROP_EQUIP_WEAPON;
					dropItem = RatesConfig.PLAYER_RATE_DROP_ITEM;
					dropLimit = RatesConfig.PLAYER_DROP_LIMIT;
				}
				
				if (dropPercent > 0 && Rnd.get(100) < dropPercent)
				{
					int itemDropPercent = 0;
					
					for (Item itemDrop : this._inventory.getItems())
					{
						if (!itemDrop.isShadowItem() && !itemDrop.isTimeLimitedItem() && itemDrop.isDropable() && itemDrop.getId() != 57 && itemDrop.getTemplate().getType2() != 3 && (this._pet == null || this._pet.getControlObjectId() != itemDrop.getId()) && Arrays.binarySearch(PvpConfig.KARMA_LIST_NONDROPPABLE_ITEMS, itemDrop.getId()) < 0 && Arrays.binarySearch(PvpConfig.KARMA_LIST_NONDROPPABLE_PET_ITEMS, itemDrop.getId()) < 0)
						{
							if (itemDrop.isEquipped())
							{
								itemDropPercent = itemDrop.getTemplate().getType2() == 0 ? dropEquipWeapon : dropEquip;
							}
							else
							{
								itemDropPercent = dropItem;
							}
							
							if (Rnd.get(100) < itemDropPercent)
							{
								if (itemDrop.isEquipped())
								{
									this._inventory.unEquipItemInSlot(itemDrop.getLocationSlot());
								}
								
								this.broadcastPacket(new ExPenaltyItemDrop(new Location(this.getX() + Rnd.get(50) - 25, this.getY() + Rnd.get(50) - 25, this.getZ() + 20), itemDrop.getId()));
								droppedItems.add(itemDrop);
								int killerObjectId = killer.isNpc() ? -1 : killer.getObjectId();
								this.addPenaltyItem(itemDrop, killerObjectId);
								if (droppedItems.size() >= dropLimit)
								{
									break;
								}
							}
						}
					}
					
					if (!droppedItems.isEmpty())
					{
						this.sendPacket(new ExPenaltyItemInfo(this));
						this.sendItemList();
					}
				}
			}
			
			return droppedItems;
		}
		return droppedItems;
	}
	
	public void onPlayerKill(Playable target)
	{
		if (target != null && target.isPlayable())
		{
			Player killedPlayer = target.asPlayer();
			if (killedPlayer != null && this != killedPlayer)
			{
				if (this.isCursedWeaponEquipped() && target.isPlayer())
				{
					CursedWeaponsManager.getInstance().increaseKills(this._cursedWeaponEquippedId);
				}
				else if (!this.isInOlympiadMode() && !killedPlayer.isInOlympiadMode())
				{
					if (!this.isInDuel() || !killedPlayer.isInDuel())
					{
						if (target.isPlayer() && this.isInsideZone(ZoneId.SIEGE) && killedPlayer.isInsideZone(ZoneId.SIEGE))
						{
							if (!this.isSiegeFriend(killedPlayer))
							{
								Clan targetClan = killedPlayer.getClan();
								if (this._clan != null && targetClan != null)
								{
									this._clan.addSiegeKill();
									targetClan.addSiegeDeath();
								}
							}
						}
						else if (!this.isInsideZone(ZoneId.PVP) && !target.isInsideZone(ZoneId.PVP))
						{
							if (this.checkIfPvP(killedPlayer))
							{
								if (killedPlayer.getReputation() < 0)
								{
									int levelDiff = killedPlayer.getLevel() - this.getLevel();
									if (this.getReputation() >= 0 && levelDiff < 11 && levelDiff > -11)
									{
										this.setReputation(this.getReputation() + PvpConfig.REPUTATION_INCREASE);
									}
								}
								
								if (target.isPlayer())
								{
									this.setPvpKills(this._pvpKills + 1);
									this.setTotalKills(this.getTotalKills() + 1);
									this.updatePvpTitleAndColor(true);
									if (AchievementBoxConfig.ENABLE_ACHIEVEMENT_PVP)
									{
										this.getAchievementBox().addPvpPoints(1);
									}
								}
							}
							else if (this.getReputation() > 0 && this._pkKills == 0)
							{
								this.setReputation(0);
								if (target.isPlayer())
								{
									this.setTotalKills(this.getTotalKills() + 1);
									this.setPkKills(this.getPkKills() + 1);
								}
							}
							else if (FactionSystemConfig.FACTION_SYSTEM_ENABLED)
							{
								if (this._isGood && killedPlayer.isGood() || this._isEvil && killedPlayer.isEvil())
								{
									this.setReputation(this.getReputation() - Formulas.calculateKarmaGain(this.getPkKills(), target.isSummon()));
									if (target.isPlayer())
									{
										this.setPkKills(this.getPkKills() + 1);
										this.setTotalKills(this.getTotalKills() + 1);
									}
								}
							}
							else
							{
								this.setReputation(this.getReputation() - Formulas.calculateKarmaGain(this.getPkKills(), target.isSummon()));
								if (target.isPlayer())
								{
									this.setPkKills(this.getPkKills() + 1);
									this.setTotalKills(this.getTotalKills() + 1);
								}
								
								if (PrisonConfig.ENABLE_PRISON)
								{
									if (this.getReputation() > PrisonConfig.REPUTATION_FOR_ZONE_1 && this.getPkKills() <= PrisonConfig.PK_FOR_ZONE_1)
									{
										SystemMessage msg = new SystemMessage(SystemMessageId.IF_YOUR_REPUTATION_REACHES_S1_OR_YOUR_PK_COUNTER_IS_S2_OR_LESS_YOU_WILL_BE_TELEPORTED_TO_THE_UNDERGROUND_LABYRINTH);
										msg.addInt((int) PrisonConfig.REPUTATION_FOR_ZONE_1);
										msg.addByte(PrisonConfig.PK_FOR_ZONE_1);
										this.sendPacket(msg);
									}
									else
									{
										PrisonManager.processPK(this, true);
									}
								}
							}
							
							this.broadcastUserInfo(UserInfoType.SOCIAL);
							this.checkItemRestriction();
						}
					}
				}
			}
		}
	}
	
	public void updatePvpTitleAndColor(boolean broadcastInfo)
	{
		if (PvpTitleColorConfig.PVP_COLOR_SYSTEM_ENABLED && !FactionSystemConfig.FACTION_SYSTEM_ENABLED)
		{
			if (this._pvpKills >= PvpTitleColorConfig.PVP_AMOUNT1 && this._pvpKills < PvpTitleColorConfig.PVP_AMOUNT2)
			{
				this.setTitle("® " + PvpTitleColorConfig.TITLE_FOR_PVP_AMOUNT1 + " ®");
				this._appearance.setTitleColor(PvpTitleColorConfig.NAME_COLOR_FOR_PVP_AMOUNT1);
			}
			else if (this._pvpKills >= PvpTitleColorConfig.PVP_AMOUNT2 && this._pvpKills < PvpTitleColorConfig.PVP_AMOUNT3)
			{
				this.setTitle("® " + PvpTitleColorConfig.TITLE_FOR_PVP_AMOUNT2 + " ®");
				this._appearance.setTitleColor(PvpTitleColorConfig.NAME_COLOR_FOR_PVP_AMOUNT2);
			}
			else if (this._pvpKills >= PvpTitleColorConfig.PVP_AMOUNT3 && this._pvpKills < PvpTitleColorConfig.PVP_AMOUNT4)
			{
				this.setTitle("® " + PvpTitleColorConfig.TITLE_FOR_PVP_AMOUNT3 + " ®");
				this._appearance.setTitleColor(PvpTitleColorConfig.NAME_COLOR_FOR_PVP_AMOUNT3);
			}
			else if (this._pvpKills >= PvpTitleColorConfig.PVP_AMOUNT4 && this._pvpKills < PvpTitleColorConfig.PVP_AMOUNT5)
			{
				this.setTitle("® " + PvpTitleColorConfig.TITLE_FOR_PVP_AMOUNT4 + " ®");
				this._appearance.setTitleColor(PvpTitleColorConfig.NAME_COLOR_FOR_PVP_AMOUNT4);
			}
			else if (this._pvpKills >= PvpTitleColorConfig.PVP_AMOUNT5)
			{
				this.setTitle("® " + PvpTitleColorConfig.TITLE_FOR_PVP_AMOUNT5 + " ®");
				this._appearance.setTitleColor(PvpTitleColorConfig.NAME_COLOR_FOR_PVP_AMOUNT5);
			}
			
			if (broadcastInfo)
			{
				this.broadcastTitleInfo();
			}
		}
	}
	
	public void updatePvPStatus()
	{
		if (!this.isInsideZone(ZoneId.PVP))
		{
			this.setPvpFlagLasts(System.currentTimeMillis() + PvpConfig.PVP_NORMAL_TIME);
			if (this._pvpFlag == 0)
			{
				this.startPvPFlag();
			}
		}
	}
	
	public void updatePvPStatus(Creature target)
	{
		Player targetPlayer = target.asPlayer();
		if (targetPlayer != null)
		{
			if (this != targetPlayer)
			{
				if (!FactionSystemConfig.FACTION_SYSTEM_ENABLED || !target.isPlayer() || (!this.isGood() || !targetPlayer.isEvil()) && (!this.isEvil() || !targetPlayer.isGood()))
				{
					if (!this._isInDuel || targetPlayer.getDuelId() != this.getDuelId())
					{
						if ((!this.isInsideZone(ZoneId.PVP) || !target.isInsideZone(ZoneId.PVP)) && targetPlayer.getReputation() >= 0)
						{
							if (this.checkIfPvP(targetPlayer))
							{
								this.setPvpFlagLasts(System.currentTimeMillis() + PvpConfig.PVP_PVP_TIME);
							}
							else
							{
								this.setPvpFlagLasts(System.currentTimeMillis() + PvpConfig.PVP_NORMAL_TIME);
							}
							
							if (this._pvpFlag == 0)
							{
								this.startPvPFlag();
							}
						}
					}
				}
			}
		}
	}
	
	public boolean isLucky()
	{
		return this.getLevel() <= 9 && this.isAffectedBySkill(CommonSkill.LUCKY.getId());
	}
	
	public void restoreExp(double restorePercent)
	{
		if (this._expBeforeDeath > 0L)
		{
			this.getStat().addExp(Math.round((this._expBeforeDeath - this.getExp()) * restorePercent / 100.0));
			this.setExpBeforeDeath(0L);
		}
	}
	
	public void calculateDeathExpPenalty(Creature killer)
	{
		int level = this.getLevel();
		double percentLost = ExperienceLossData.getInstance().getPercentLost(level);
		if (killer != null)
		{
			if (killer.isRaid())
			{
				percentLost *= this.getStat().getValue(Stat.REDUCE_EXP_LOST_BY_RAID, 1.0);
			}
			else if (killer.isMonster())
			{
				percentLost *= this.getStat().getValue(Stat.REDUCE_EXP_LOST_BY_MOB, 1.0);
			}
			else if (killer.isPlayable())
			{
				percentLost *= this.getStat().getValue(Stat.REDUCE_EXP_LOST_BY_PVP, 1.0);
			}
		}
		
		if (this.getReputation() < 0)
		{
			percentLost *= RatesConfig.RATE_KARMA_EXP_LOST;
		}
		
		long currentLevelExp = 0L;
		if (!this.isOnEvent())
		{
			int maxLevel = ExperienceData.getInstance().getMaxLevel();
			if (level < maxLevel)
			{
				currentLevelExp = this.getStat().getExpForLevel(level + 1) - this.getStat().getExpForLevel(level);
			}
			else
			{
				currentLevelExp = this.getStat().getExpForLevel(maxLevel) - this.getStat().getExpForLevel(maxLevel - 1);
			}
		}
		
		long lostExp = 0L;
		if (currentLevelExp > 0L)
		{
			lostExp = Math.round(currentLevelExp * percentLost / 100.0);
			lostExp = Math.min(lostExp, currentLevelExp / 10L);
			if (killer != null && killer.isPlayable() && this.atWarWith(killer.asPlayer()))
			{
				lostExp /= 4L;
			}
		}
		
		this.setExpBeforeDeath(this.getExp());
		this.getStat().removeExp(lostExp);
	}
	
	public void stopAllTimers()
	{
		this.stopHpMpRegeneration();
		this.stopWarnUserTakeBreak();
		this.stopWaterTask();
		this.stopFeed();
		this.clearPetData();
		this.storePetFood(this._mountNpcId);
		this.stopRentPet();
		this.stopPvpRegTask();
		this.stopSoulTask();
		this.stopChargeTask();
		this.stopFameTask();
		this.stopRecoGiveTask();
		this.stopOnlineTimeUpdateTask();
	}
	
	@Override
	public Pet getPet()
	{
		return this._pet;
	}
	
	@Override
	public Map<Integer, Summon> getServitors()
	{
		return this._servitors;
	}
	
	public Summon getAnyServitor()
	{
		return this.getServitors().values().stream().findAny().orElse(null);
	}
	
	public Summon getFirstServitor()
	{
		return this.getServitors().isEmpty() ? null : this.getServitors().values().iterator().next();
	}
	
	@Override
	public Summon getServitor(int objectId)
	{
		return this.getServitors().get(objectId);
	}
	
	public List<Summon> getServitorsAndPets()
	{
		List<Summon> summons = new ArrayList<>();
		summons.addAll(this.getServitors().values());
		if (this._pet != null)
		{
			summons.add(this._pet);
		}
		
		return summons;
	}
	
	public Trap getTrap()
	{
		for (Npc npc : this.getSummonedNpcs())
		{
			if (npc.isTrap())
			{
				return (Trap) npc;
			}
		}
		
		return null;
	}
	
	public void setPet(Pet pet)
	{
		this._pet = pet;
	}
	
	public void addServitor(Summon servitor)
	{
		this._servitors.put(servitor.getObjectId(), servitor);
	}
	
	public Set<TamedBeast> getTrainedBeasts()
	{
		return this._tamedBeast;
	}
	
	public void addTrainedBeast(TamedBeast tamedBeast)
	{
		this._tamedBeast.add(tamedBeast);
	}
	
	public Request getRequest()
	{
		return this._request;
	}
	
	public void setActiveRequester(Player requester)
	{
		this._activeRequester = requester;
	}
	
	public Player getActiveRequester()
	{
		Player requester = this._activeRequester;
		if (requester != null && requester.isRequestExpired() && this._activeTradeList == null)
		{
			this._activeRequester = null;
		}
		
		return this._activeRequester;
	}
	
	public boolean isProcessingRequest()
	{
		return this.getActiveRequester() != null || this._requestExpireTime > GameTimeTaskManager.getInstance().getGameTicks();
	}
	
	public boolean isProcessingTransaction()
	{
		return this.getActiveRequester() != null || this._activeTradeList != null || this._requestExpireTime > GameTimeTaskManager.getInstance().getGameTicks();
	}
	
	public void blockRequest()
	{
		this._requestExpireTime = GameTimeTaskManager.getInstance().getGameTicks() + 150;
	}
	
	public void onTransactionRequest(Player partner)
	{
		this._requestExpireTime = GameTimeTaskManager.getInstance().getGameTicks() + 150;
		partner.setActiveRequester(this);
	}
	
	public boolean isRequestExpired()
	{
		return this._requestExpireTime <= GameTimeTaskManager.getInstance().getGameTicks();
	}
	
	public void onTransactionResponse()
	{
		this._requestExpireTime = 0L;
	}
	
	public void setActiveWarehouse(ItemContainer warehouse)
	{
		this._activeWarehouse = warehouse;
	}
	
	public ItemContainer getActiveWarehouse()
	{
		return this._activeWarehouse;
	}
	
	public void setActiveTradeList(TradeList tradeList)
	{
		this._activeTradeList = tradeList;
	}
	
	public TradeList getActiveTradeList()
	{
		return this._activeTradeList;
	}
	
	public void onTradeStart(Player partner)
	{
		this._activeTradeList = new TradeList(this);
		this._activeTradeList.setPartner(partner);
		SystemMessage msg = new SystemMessage(SystemMessageId.YOU_BEGIN_TRADING_WITH_C1);
		msg.addPcName(partner);
		this.sendPacket(msg);
		this.sendPacket(new TradeStart(1, this));
		this.sendPacket(new TradeStart(2, this));
	}
	
	public void onTradeConfirm(Player partner)
	{
		SystemMessage msg = new SystemMessage(SystemMessageId.C1_HAS_CONFIRMED_THE_TRADE);
		msg.addPcName(partner);
		this.sendPacket(msg);
		this.sendPacket(TradeOtherDone.STATIC_PACKET);
	}
	
	public void onTradeCancel(Player partner)
	{
		if (this._activeTradeList != null)
		{
			this._activeTradeList.lock();
			this._activeTradeList = null;
			this.sendPacket(new TradeDone(0));
			SystemMessage msg = new SystemMessage(SystemMessageId.C1_HAS_CANCELLED_THE_TRADE);
			msg.addPcName(partner);
			this.sendPacket(msg);
		}
	}
	
	public void onTradeFinish(boolean successfull)
	{
		this._activeTradeList = null;
		this.sendPacket(new TradeDone(1));
		if (successfull)
		{
			this.sendPacket(SystemMessageId.YOUR_TRADE_WAS_SUCCESSFUL);
		}
	}
	
	public void startTrade(Player partner)
	{
		this.onTradeStart(partner);
		partner.onTradeStart(this);
	}
	
	public void cancelActiveTrade()
	{
		if (this._activeTradeList != null)
		{
			Player partner = this._activeTradeList.getPartner();
			if (partner != null)
			{
				partner.onTradeCancel(this);
			}
			
			this.onTradeCancel(this);
		}
	}
	
	public boolean hasManufactureShop()
	{
		return this._manufactureItems != null && !this._manufactureItems.isEmpty();
	}
	
	public Map<Integer, ManufactureItem> getManufactureItems()
	{
		if (this._manufactureItems == null)
		{
			synchronized (this)
			{
				if (this._manufactureItems == null)
				{
					this._manufactureItems = Collections.synchronizedMap(new LinkedHashMap<>());
				}
			}
		}
		
		return this._manufactureItems;
	}
	
	public String getStoreName()
	{
		return this._storeName;
	}
	
	public void setStoreName(String name)
	{
		this._storeName = name == null ? "" : name;
	}
	
	public TradeList getSellList()
	{
		if (this._sellList == null)
		{
			this._sellList = new TradeList(this);
		}
		
		return this._sellList;
	}
	
	public TradeList getBuyList()
	{
		if (this._buyList == null)
		{
			this._buyList = new TradeList(this);
		}
		
		return this._buyList;
	}
	
	public void setPrivateStoreType(PrivateStoreType privateStoreType)
	{
		this._privateStoreType = privateStoreType;
		if (OfflineTradeConfig.OFFLINE_DISCONNECT_FINISHED && privateStoreType == PrivateStoreType.NONE && (this._client == null || this._client.isDetached()))
		{
			OfflineTraderTable.getInstance().removeTrader(this.getObjectId());
			Disconnection.of(this).storeAndDelete();
		}
	}
	
	public PrivateStoreType getPrivateStoreType()
	{
		return this._privateStoreType;
	}
	
	public void setClan(Clan clan)
	{
		this._clan = clan;
		if (clan == null)
		{
			this.setTitle("");
			this._clanId = 0;
			this._clanPrivileges = new ClanPrivileges();
			this._pledgeType = 0;
			this._powerGrade = 0;
			this._lvlJoinedAcademy = 0;
			this._apprentice = 0;
			this._sponsor = 0;
			this._activeWarehouse = null;
			CharInfoTable.getInstance().removeClanId(this.getObjectId());
		}
		else if (!clan.isMember(this.getObjectId()))
		{
			this.setClan(null);
		}
		else
		{
			this._clanId = clan.getId();
			CharInfoTable.getInstance().setClanId(this.getObjectId(), this._clanId);
		}
	}
	
	@Override
	public Clan getClan()
	{
		return this._clan;
	}
	
	public boolean isClanLeader()
	{
		return this._clan == null ? false : this.getObjectId() == this._clan.getLeaderId();
	}
	
	@Override
	protected boolean checkAndEquipAmmunition(EtcItemType type)
	{
		Item ammunition = null;
		Weapon weapon = this.getActiveWeaponItem();
		if (type == EtcItemType.ARROW)
		{
			ammunition = this._inventory.findArrowForBow(weapon);
		}
		else if (type == EtcItemType.BOLT)
		{
			ammunition = this._inventory.findBoltForCrossBow(weapon);
		}
		else if (type == EtcItemType.ELEMENTAL_ORB)
		{
			ammunition = this._inventory.findElementalOrbForPistols(weapon);
		}
		
		if (ammunition != null)
		{
			this.addAmmunitionSkills(ammunition);
			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(ammunition);
			this.sendInventoryUpdate(iu);
			return true;
		}
		this.removeAmmunitionSkills();
		return false;
	}
	
	private void addAmmunitionSkills(Item ammunition)
	{
		int currentAmmunitionId = ammunition.getId();
		if (this._lastAmmunitionId != currentAmmunitionId)
		{
			this.removeAmmunitionSkills();
			this._lastAmmunitionId = currentAmmunitionId;
			List<ItemSkillHolder> skills = ammunition.getTemplate().getAllSkills();
			if (skills != null)
			{
				boolean sendSkillList = false;
				
				for (ItemSkillHolder holder : skills)
				{
					if (!this.isAffectedBySkill(holder))
					{
						Skill skill = holder.getSkill();
						if (skill.isPassive())
						{
							this.addSkill(skill);
							sendSkillList = true;
						}
					}
				}
				
				if (sendSkillList)
				{
					this.sendSkillList();
				}
			}
		}
	}
	
	public void removeAmmunitionSkills()
	{
		if (this._lastAmmunitionId != 0)
		{
			this._lastAmmunitionId = 0;
			boolean sendSkillList = false;
			
			for (Integer skillId : AmmunitionSkillList.values())
			{
				if (this.removeSkill(skillId, true) != null)
				{
					sendSkillList = true;
				}
			}
			
			if (sendSkillList)
			{
				this.sendSkillList();
			}
		}
	}
	
	public boolean canUseEquipment(Item item)
	{
		if (item.isArmor())
		{
			ItemType itemType = item.getItemType();
			if (itemType == ArmorType.SHIELD)
			{
				if (this.isDeathKnight() || this.getRace() == Race.SYLPH || this.isVanguard() || this.isAssassin() || CategoryData.getInstance().isInCategory(CategoryType.HIGH_ELF_WEAVER, this.getPlayerClass().getId()) || this.isSamurai())
				{
					return false;
				}
			}
			else if (itemType == ArmorType.SIGIL && CategoryData.getInstance().isInCategory(CategoryType.HIGH_ELF_TEMPLAR, this.getPlayerClass().getId()))
			{
				return false;
			}
		}
		else if (item.isWeapon())
		{
			ItemType itemType = item.getItemType();
			if (itemType == WeaponType.FISHINGROD)
			{
				return item.isEquipable();
			}
			
			Race race = this.getRace();
			if (itemType == WeaponType.PISTOLS)
			{
				if (race != Race.SYLPH)
				{
					return false;
				}
			}
			else if (race == Race.DWARF)
			{
				if (itemType == WeaponType.RAPIER)
				{
					return false;
				}
			}
			else if (race == Race.ORC)
			{
				if ((itemType == WeaponType.RAPIER) || (this.isVanguard() && itemType != WeaponType.POLE))
				{
					return false;
				}
			}
			else if (race == Race.SYLPH)
			{
				if (itemType != WeaponType.PISTOLS)
				{
					return false;
				}
			}
			else if (this.isAssassin())
			{
				if (itemType != WeaponType.DAGGER)
				{
					return false;
				}
			}
			else if (this.isWarg())
			{
				if (itemType != WeaponType.FIST && itemType != WeaponType.DUALFIST)
				{
					return false;
				}
			}
			else if (CategoryData.getInstance().isInCategory(CategoryType.HIGH_ELF_WEAVER, this.getPlayerClass().getId()))
			{
				if (itemType != WeaponType.BLUNT || item.getTemplate().getBodyPart() != BodyPart.LR_HAND)
				{
					return false;
				}
			}
			else if (!CategoryData.getInstance().isInCategory(CategoryType.HIGH_ELF_TEMPLAR, this.getPlayerClass().getId()) && !this.isSamurai())
			{
				if (race != Race.KAMAEL && itemType == WeaponType.ANCIENTSWORD)
				{
					return false;
				}
			}
			else if (itemType != WeaponType.SWORD || item.getTemplate().getBodyPart() != BodyPart.R_HAND)
			{
				return false;
			}
		}
		
		return item.isEquipable();
	}
	
	public void disarmUnusableEquipment()
	{
		InventoryUpdate iu = new InventoryUpdate();
		PlayerInventory inventory = this.getInventory();
		
		for (Item equipped : inventory.getPaperdollItems())
		{
			if (equipped != null && !this.canUseEquipment(equipped))
			{
				BodyPart bodyPart = BodyPart.fromItem(equipped);
				inventory.unEquipItemInBodySlot(bodyPart);
				iu.addModifiedItem(equipped);
				SystemMessage sm;
				if (equipped.getEnchantLevel() > 0)
				{
					sm = new SystemMessage(SystemMessageId.S1_S2_UNEQUIPPED);
					sm.addInt(equipped.getEnchantLevel());
					sm.addItemName(equipped);
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.S1_UNEQUIPPED);
					sm.addItemName(equipped);
				}
				
				this.sendPacket(sm);
			}
		}
		
		if (!iu.getItems().isEmpty())
		{
			this.sendInventoryUpdate(iu);
			this.broadcastUserInfo();
		}
	}
	
	public boolean disarmWeapons()
	{
		Item wpn = this._inventory.getPaperdollItem(5);
		if (wpn == null)
		{
			return true;
		}
		else if (this.isCursedWeaponEquipped())
		{
			return false;
		}
		else if (this._combatFlagEquippedId)
		{
			return false;
		}
		else if (wpn.getWeaponItem().isForceEquip())
		{
			return false;
		}
		else
		{
			List<Item> unequipped = this._inventory.unEquipItemInBodySlotAndRecord(wpn.getTemplate().getBodyPart());
			InventoryUpdate iu = new InventoryUpdate();
			
			for (Item itm : unequipped)
			{
				iu.addModifiedItem(itm);
			}
			
			this.sendInventoryUpdate(iu);
			this.abortAttack();
			this.broadcastUserInfo();
			if (!unequipped.isEmpty())
			{
				Item unequippedItem = unequipped.get(0);
				SystemMessage sm;
				if (unequippedItem.getEnchantLevel() > 0)
				{
					sm = new SystemMessage(SystemMessageId.S1_S2_UNEQUIPPED);
					sm.addInt(unequippedItem.getEnchantLevel());
					sm.addItemName(unequippedItem);
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.S1_UNEQUIPPED);
					sm.addItemName(unequippedItem);
				}
				
				this.sendPacket(sm);
			}
			
			return true;
		}
	}
	
	public boolean disarmShield()
	{
		Item sld = this._inventory.getPaperdollItem(7);
		if (sld != null)
		{
			List<Item> unequipped = this._inventory.unEquipItemInBodySlotAndRecord(sld.getTemplate().getBodyPart());
			InventoryUpdate iu = new InventoryUpdate();
			
			for (Item itm : unequipped)
			{
				iu.addModifiedItem(itm);
			}
			
			this.sendInventoryUpdate(iu);
			this.abortAttack();
			this.broadcastUserInfo();
			if (!unequipped.isEmpty())
			{
				SystemMessage sm = null;
				Item unequippedItem = unequipped.get(0);
				if (unequippedItem.getEnchantLevel() > 0)
				{
					sm = new SystemMessage(SystemMessageId.S1_S2_UNEQUIPPED);
					sm.addInt(unequippedItem.getEnchantLevel());
					sm.addItemName(unequippedItem);
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.S1_UNEQUIPPED);
					sm.addItemName(unequippedItem);
				}
				
				this.sendPacket(sm);
			}
		}
		
		return true;
	}
	
	public boolean mount(Summon pet)
	{
		if (!FeatureConfig.ALLOW_MOUNTS_DURING_SIEGE && this.isInsideZone(ZoneId.SIEGE))
		{
			MountType type = MountType.findByNpcId(pet.getId());
			boolean isAllowed = type == MountType.STRIDER && CastleManager.getInstance().getCastle(this) != null && this.isClanLeader() && this.getClanId() == CastleManager.getInstance().getCastle(this).getOwnerId();
			if (!isAllowed)
			{
				this.sendMessage("You cannot mount during a siege.");
				return false;
			}
		}
		
		if (this.disarmWeapons() && this.disarmShield() && !this.isTransformed())
		{
			this.getEffectList().stopAllToggles();
			this.setMount(pet.getId(), pet.getLevel());
			this.setMountObjectID(pet.getControlObjectId());
			this.clearPetData();
			this.startFeed(pet.getId());
			this.broadcastPacket(new Ride(this));
			this.broadcastUserInfo();
			pet.unSummon(this);
			return true;
		}
		return false;
	}
	
	public boolean mount(int npcId, int controlItemObjId, boolean useFood)
	{
		if (this.disarmWeapons() && this.disarmShield() && !this.isTransformed())
		{
			this.getEffectList().stopAllToggles();
			this.setMount(npcId, this.getLevel());
			this.clearPetData();
			this.setMountObjectID(controlItemObjId);
			this.broadcastPacket(new Ride(this));
			this.broadcastUserInfo();
			if (useFood)
			{
				this.startFeed(npcId);
			}
			
			return true;
		}
		return false;
	}
	
	public boolean mountPlayer(Summon pet)
	{
		if (pet != null && pet.isMountable() && !this.isMounted() && !this.isBetrayed())
		{
			if (this.isDead())
			{
				this.sendPacket(ActionFailed.STATIC_PACKET);
				this.sendPacket(SystemMessageId.YOU_ARE_DEAD_AND_CANNOT_RIDE_A_DRAGON);
				return false;
			}
			
			if (pet.isDead())
			{
				this.sendPacket(ActionFailed.STATIC_PACKET);
				this.sendPacket(SystemMessageId.A_DEAD_MOUNT_CANNOT_BE_RIDDEN);
				return false;
			}
			
			if (pet.isInCombat() || pet.isRooted())
			{
				this.sendPacket(ActionFailed.STATIC_PACKET);
				this.sendPacket(SystemMessageId.A_MOUNT_IN_BATTLE_CANNOT_BE_RIDDEN);
				return false;
			}
			
			if (this.isInCombat())
			{
				this.sendPacket(ActionFailed.STATIC_PACKET);
				this.sendPacket(SystemMessageId.A_MOUNT_CANNOT_BE_RIDDEN_WHILE_IN_BATTLE);
				return false;
			}
			
			if (this._waitTypeSitting)
			{
				this.sendPacket(ActionFailed.STATIC_PACKET);
				this.sendPacket(SystemMessageId.A_MOUNT_CAN_BE_RIDDEN_ONLY_WHEN_STANDING);
				return false;
			}
			
			if (this.isFishing())
			{
				this.sendPacket(ActionFailed.STATIC_PACKET);
				this.sendPacket(SystemMessageId.YOU_CANNOT_DO_THAT_WHILE_FISHING_2);
				return false;
			}
			
			if (this.isTransformed() || this.isCursedWeaponEquipped())
			{
				this.sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			
			if (this._inventory.getItemByItemId(93331) != null)
			{
				this.sendPacket(ActionFailed.STATIC_PACKET);
				this.sendMessage("You cannot mount a steed while holding a flag.");
				return false;
			}
			
			if (pet.isHungry())
			{
				this.sendPacket(ActionFailed.STATIC_PACKET);
				this.sendPacket(SystemMessageId.A_HUNGRY_MOUNT_CANNOT_BE_MOUNTED_OR_DISMOUNTED);
				return false;
			}
			
			if (!LocationUtil.checkIfInRange(200, this, pet, true))
			{
				this.sendPacket(ActionFailed.STATIC_PACKET);
				this.sendPacket(SystemMessageId.YOU_ARE_TOO_FAR_AWAY_FROM_YOUR_MOUNT_TO_RIDE);
				return false;
			}
			
			if (!pet.isDead() && !this.isMounted())
			{
				this.mount(pet);
			}
		}
		else if (this.isRentedPet())
		{
			this.stopRentPet();
		}
		else if (this.isMounted())
		{
			if (this._mountType == MountType.WYVERN && this.isInsideZone(ZoneId.NO_LANDING) && !this.isGM())
			{
				this.sendPacket(ActionFailed.STATIC_PACKET);
				this.sendPacket(SystemMessageId.YOU_ARE_NOT_ALLOWED_TO_DISMOUNT_IN_THIS_LOCATION);
				return false;
			}
			
			if (this.isHungry())
			{
				this.sendPacket(ActionFailed.STATIC_PACKET);
				this.sendPacket(SystemMessageId.A_HUNGRY_MOUNT_CANNOT_BE_MOUNTED_OR_DISMOUNTED);
				return false;
			}
			
			this.dismount();
		}
		
		return true;
	}
	
	public boolean dismount()
	{
		if (ZoneManager.getInstance().getZone(this.getX(), this.getY(), this.getZ() - 300, WaterZone.class) == null)
		{
			if (!this.isGM() && !this.isInWater() && this.getZ() > 10000)
			{
				this.sendPacket(SystemMessageId.YOU_ARE_NOT_ALLOWED_TO_DISMOUNT_IN_THIS_LOCATION);
				this.sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			
			if (!this.isInsideZone(ZoneId.NO_LANDING) && GeoEngine.getInstance().getHeight(this.getX(), this.getY(), this.getZ()) + 300 < this.getZ())
			{
				this.sendPacket(SystemMessageId.YOU_CANNOT_DISMOUNT_FROM_THIS_ELEVATION);
				this.sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}
		else
		{
			ThreadPool.schedule(() -> {
				if (this.isInWater())
				{
					this.broadcastUserInfo();
				}
			}, 1500L);
		}
		
		boolean wasFlying = this.isFlying();
		this.sendPacket(new SetupGauge(3, 0, 0));
		int petId = this._mountNpcId;
		this.setMount(0, 0);
		this.stopFeed();
		this.clearPetData();
		if (wasFlying)
		{
			this.removeSkill(CommonSkill.WYVERN_BREATH.getSkill());
		}
		
		this.broadcastPacket(new Ride(this));
		this.setMountObjectID(0);
		this.storePetFood(petId);
		this.broadcastUserInfo();
		return true;
	}
	
	public void setUptime(long time)
	{
		this._uptime = time;
	}
	
	public long getUptime()
	{
		return System.currentTimeMillis() - this._uptime;
	}
	
	@Override
	public boolean isInvul()
	{
		return super.isInvul() || this.isTeleportProtected();
	}
	
	@Override
	public boolean isInParty()
	{
		return this._party != null;
	}
	
	public void setParty(Party party)
	{
		this._party = party;
	}
	
	public void joinParty(Party party)
	{
		if (party != null)
		{
			this._party = party;
			party.addPartyMember(this);
		}
	}
	
	public void leaveParty()
	{
		if (this.isInParty())
		{
			this._party.removePartyMember(this, PartyMessageType.DISCONNECTED);
			this._party = null;
		}
	}
	
	@Override
	public Party getParty()
	{
		return this._party;
	}
	
	public boolean isInCommandChannel()
	{
		return this.isInParty() && this._party.isInCommandChannel();
	}
	
	public CommandChannel getCommandChannel()
	{
		return this.isInCommandChannel() ? this._party.getCommandChannel() : null;
	}
	
	@Override
	public boolean isGM()
	{
		return this._accessLevel.isGm();
	}
	
	public void setAccessLevel(int level, boolean broadcast, boolean updateInDb)
	{
		AccessLevel accessLevel = AdminData.getInstance().getAccessLevel(level);
		if (accessLevel == null)
		{
			LOGGER.warning("Can't find access level " + level + " for " + this);
			accessLevel = AdminData.getInstance().getAccessLevel(0);
		}
		
		if (accessLevel.getLevel() == 0 && GeneralConfig.DEFAULT_ACCESS_LEVEL > 0)
		{
			accessLevel = AdminData.getInstance().getAccessLevel(GeneralConfig.DEFAULT_ACCESS_LEVEL);
			if (accessLevel == null)
			{
				LOGGER.warning("Config's default access level (" + GeneralConfig.DEFAULT_ACCESS_LEVEL + ") is not defined, defaulting to 0!");
				accessLevel = AdminData.getInstance().getAccessLevel(0);
				GeneralConfig.DEFAULT_ACCESS_LEVEL = 0;
			}
		}
		
		this._accessLevel = accessLevel;
		this._appearance.setNameColor(this._accessLevel.getNameColor());
		this._appearance.setTitleColor(this._accessLevel.getTitleColor());
		if (broadcast)
		{
			this.broadcastUserInfo();
		}
		
		if (updateInDb)
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE characters SET accesslevel = ? WHERE charId = ?");)
			{
				ps.setInt(1, accessLevel.getLevel());
				ps.setInt(2, this.getObjectId());
				ps.executeUpdate();
			}
			catch (SQLException var13)
			{
				LOGGER.log(Level.WARNING, "Failed to update character's accesslevel in db: " + this, var13);
			}
		}
		
		CharInfoTable.getInstance().addName(this);
		if (accessLevel == null)
		{
			LOGGER.warning("Tryed to set unregistered access level " + level + " for " + this + ". Setting access level without privileges!");
		}
		else if (level > 0)
		{
			LOGGER.warning(this._accessLevel.getName() + " access level set for character " + this._name + "! Just a warning to be careful ;)");
		}
	}
	
	public void setAccountAccesslevel(int level)
	{
		LoginServerThread.getInstance().sendAccessLevel(this.getAccountName(), level);
	}
	
	@Override
	public AccessLevel getAccessLevel()
	{
		return this._accessLevel;
	}
	
	public void updateAndBroadcastStatus()
	{
		if (this._updateAndBroadcastStatusTask == null)
		{
			this._updateAndBroadcastStatusTask = ThreadPool.schedule(() -> {
				this.refreshOverloaded(true);
				this.broadcastUserInfo();
				this._updateAndBroadcastStatusTask = null;
			}, 50L);
		}
	}
	
	public void broadcastReputation()
	{
		this.broadcastUserInfo(UserInfoType.SOCIAL);
		World.getInstance().forEachVisibleObject(this, Player.class, player -> {
			if (this.isVisibleFor(player))
			{
				long relation = this.getRelation(player);
				boolean isAutoAttackable = this.isAutoAttackable(player);
				RelationCache oldrelation = this.getKnownRelations().get(player.getObjectId());
				if (oldrelation == null || oldrelation.getRelation() != relation || oldrelation.isAutoAttackable() != isAutoAttackable)
				{
					RelationChanged rc = new RelationChanged();
					rc.addRelation(this, relation, isAutoAttackable);
					if (this.hasSummon())
					{
						if (this._pet != null)
						{
							rc.addRelation(this._pet, relation, isAutoAttackable);
						}
						
						if (this.hasServitors())
						{
							this.getServitors().values().forEach(s -> rc.addRelation(s, relation, isAutoAttackable));
						}
					}
					
					player.sendPacket(rc);
					this.getKnownRelations().put(player.getObjectId(), new RelationCache(relation, isAutoAttackable));
				}
			}
		});
	}
	
	public void setOnlineStatus(boolean isOnline, boolean updateInDb)
	{
		if (this._isOnline != isOnline)
		{
			this._isOnline = isOnline;
		}
		
		if (updateInDb)
		{
			this.updateOnlineStatus();
		}
	}
	
	public void updateOnlineStatus()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("UPDATE characters SET online=?, lastAccess=? WHERE charId=?");)
		{
			statement.setInt(1, this.isOnlineInt());
			statement.setLong(2, System.currentTimeMillis());
			statement.setInt(3, this.getObjectId());
			statement.execute();
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.SEVERE, "Failed updating character online status.", var9);
		}
	}
	
	private boolean createDb()
	{
		try
		{
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement statement = con.prepareStatement("INSERT INTO characters (account_name,charId,char_name,level,maxHp,curHp,maxCp,curCp,maxMp,curMp,face,hairStyle,hairColor,sex,exp,sp,reputation,fame,raidbossPoints,pvpkills,pkkills,clanid,race,classid,deletetime,cancraft,title,title_color,online,clan_privs,wantspeace,base_class,nobless,power_grade,vitality_points,createDate,lastAccess,kills,deaths) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");)
			{
				statement.setString(1, this._accountName);
				statement.setInt(2, this.getObjectId());
				statement.setString(3, this._name);
				statement.setInt(4, this.getLevel());
				statement.setInt(5, (int) this.getMaxHp());
				statement.setDouble(6, this.getCurrentHp());
				statement.setInt(7, this.getMaxCp());
				statement.setDouble(8, this.getCurrentCp());
				statement.setInt(9, this.getMaxMp());
				statement.setDouble(10, this.getCurrentMp());
				statement.setInt(11, this._appearance.getFace());
				statement.setInt(12, this._appearance.getHairStyle());
				statement.setInt(13, this._appearance.getHairColor());
				statement.setInt(14, this._appearance.isFemale() ? 1 : 0);
				statement.setLong(15, this.getExp());
				statement.setLong(16, this.getSp());
				statement.setInt(17, this.getReputation());
				statement.setInt(18, this._fame);
				statement.setInt(19, this._raidbossPoints);
				statement.setInt(20, this._pvpKills);
				statement.setInt(21, this._pkKills);
				statement.setInt(22, this.getOgClanId());
				statement.setInt(23, this.getRace().ordinal());
				statement.setInt(24, this.getPlayerClass().getId());
				statement.setLong(25, this._deleteTimer);
				statement.setInt(26, this.hasDwarvenCraft() ? 1 : 0);
				statement.setString(27, this.getTitle());
				statement.setInt(28, this._appearance.getTitleColor());
				statement.setInt(29, this.isOnlineInt());
				statement.setInt(30, this._clanPrivileges.getMask());
				statement.setInt(31, this._wantsPeace);
				statement.setInt(32, this._baseClass);
				statement.setInt(33, this.isNoble() ? 1 : 0);
				statement.setLong(34, 0L);
				statement.setInt(35, 0);
				statement.setDate(36, new Date(this._createDate.getTimeInMillis()));
				statement.setLong(37, System.currentTimeMillis());
				statement.setInt(38, this.getTotalKills());
				statement.setInt(39, this.getTotalDeaths());
				statement.executeUpdate();
			}
			
			return true;
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.SEVERE, "Could not insert char data: " + var9.getMessage(), var9);
			return false;
		}
	}
	
	private static Player restore(int objectId)
	{
		Player player = null;
		double currentCp = 0.0;
		double currentHp = 0.0;
		double currentMp = 0.0;
		
		try
		{
			Summon pet;
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM characters WHERE charId=?");)
			{
				statement.setInt(1, objectId);
				
				try (ResultSet rset = statement.executeQuery())
				{
					if (rset.next())
					{
						int activeClassId = rset.getInt("classid");
						boolean female = rset.getInt("sex") != Sex.MALE.ordinal();
						PlayerTemplate template = PlayerTemplateData.getInstance().getTemplate(activeClassId);
						PlayerAppearance app = new PlayerAppearance(rset.getByte("face"), rset.getByte("hairColor"), rset.getByte("hairStyle"), female);
						player = new Player(objectId, template, rset.getString("account_name"), app);
						player.setName(rset.getString("char_name"));
						player.setLastAccess(rset.getLong("lastAccess"));
						PlayerStat stat = player.getStat();
						stat.setExp(rset.getLong("exp"));
						player.setExpBeforeDeath(rset.getLong("expBeforeDeath"));
						stat.setLevel(rset.getInt("level"));
						stat.setSp(rset.getLong("sp"));
						player.setWantsPeace(rset.getInt("wantspeace"));
						player.setHeading(rset.getInt("heading"));
						player.setInitialReputation(rset.getInt("reputation"));
						player.setFame(rset.getInt("fame"));
						player.setRaidbossPoints(rset.getInt("raidbossPoints"));
						player.setPvpKills(rset.getInt("pvpkills"));
						player.setPkKills(rset.getInt("pkkills"));
						player.setOnlineTime(rset.getLong("onlinetime"));
						player.setNoble(rset.getInt("nobless") == 1);
						int factionId = rset.getInt("faction");
						if (factionId == 1)
						{
							player.setGood();
						}
						
						if (factionId == 2)
						{
							player.setEvil();
						}
						
						player.setClanJoinExpiryTime(rset.getLong("clan_join_expiry_time"));
						if (player.getClanJoinExpiryTime() < System.currentTimeMillis())
						{
							player.setClanJoinExpiryTime(0L);
						}
						
						player.setClanCreateExpiryTime(rset.getLong("clan_create_expiry_time"));
						if (player.getClanCreateExpiryTime() < System.currentTimeMillis())
						{
							player.setClanCreateExpiryTime(0L);
						}
						
						player.setPcCafePoints(rset.getInt("pccafe_points"));
						int clanId = rset.getInt("clanid");
						player.setPowerGrade(rset.getInt("power_grade"));
						stat.setVitalityPoints(rset.getInt("vitality_points"));
						player.setPledgeType(rset.getInt("subpledge"));
						Clan clan = null;
						if (clanId > 0)
						{
							clan = ClanTable.getInstance().getClan(clanId);
							player.setClan(clan);
							if (clan != null && clan.isMember(objectId))
							{
								if (clan.getLeaderId() != player.getObjectId())
								{
									if (player.getPowerGrade() == 0)
									{
										player.setPowerGrade(5);
									}
									
									player.setClanPrivileges(clan.getRankPrivs(player.getPowerGrade()));
								}
								else
								{
									player.getClanPrivileges().enableAll();
									player.setPowerGrade(1);
								}
								
								player.setPledgeClass(ClanMember.calculatePledgeClass(player));
							}
						}
						
						if (clan == null)
						{
							if (player.isNoble())
							{
								player.setPledgeClass(5);
							}
							
							if (player.isHero())
							{
								player.setPledgeClass(8);
							}
							
							player.getClanPrivileges().disableAll();
						}
						
						player.setTotalDeaths(rset.getInt("deaths"));
						player.setTotalKills(rset.getInt("kills"));
						player.setDeleteTimer(rset.getLong("deletetime"));
						player.setTitle(rset.getString("title"));
						player.setAccessLevel(rset.getInt("accesslevel"), false, false);
						int titleColor = rset.getInt("title_color");
						if (titleColor != 15530402)
						{
							player.getAppearance().setTitleColor(titleColor);
						}
						
						player.setFistsWeaponItem(player.findFistsWeaponItem(activeClassId));
						player.setUptime(System.currentTimeMillis());
						currentHp = rset.getDouble("curHp");
						currentCp = rset.getDouble("curCp");
						currentMp = rset.getDouble("curMp");
						player.setClassIndex(0);
						
						try
						{
							player.setBaseClass(rset.getInt("base_class"));
						}
						catch (Exception var32)
						{
							player.setBaseClass(activeClassId);
							LOGGER.log(Level.WARNING, "Exception during player.setBaseClass for player: " + player + " base class: " + rset.getInt("base_class"), var32);
						}
						
						if (restoreSubClassData(player) && activeClassId != player.getBaseClass())
						{
							for (SubClassHolder subClass : player.getSubClasses().values())
							{
								if (subClass.getId() == activeClassId)
								{
									player.setClassIndex(subClass.getClassIndex());
								}
							}
						}
						
						if (player.getClassIndex() == 0 && activeClassId != player.getBaseClass())
						{
							player.setPlayerClass(player.getBaseClass());
							LOGGER.warning(player + " reverted to base class. Possibly has tried a relogin exploit while subclassing.");
						}
						else
						{
							player._activeClass = activeClassId;
						}
						
						if (CategoryData.getInstance().isInCategory(CategoryType.DEATH_KNIGHT_ALL_CLASS, player.getBaseTemplate().getPlayerClass().getId()))
						{
							player._isDeathKnight = true;
						}
						else if (CategoryData.getInstance().isInCategory(CategoryType.VANGUARD_ALL_CLASS, player.getBaseTemplate().getPlayerClass().getId()))
						{
							player._isVanguard = true;
						}
						else if (CategoryData.getInstance().isInCategory(CategoryType.ASSASSIN_ALL_CLASS, player.getBaseTemplate().getPlayerClass().getId()))
						{
							player._isAssassin = true;
						}
						else if (CategoryData.getInstance().isInCategory(CategoryType.WARG_ALL_CLASS, player.getBaseTemplate().getPlayerClass().getId()))
						{
							player._isWarg = true;
						}
						else if (CategoryData.getInstance().isInCategory(CategoryType.BLOOD_ROSE_ALL_CLASS, player.getBaseTemplate().getPlayerClass().getId()))
						{
							player._isBloodRose = true;
						}
						else if (CategoryData.getInstance().isInCategory(CategoryType.SAMURAI_ALL_CLASS, player.getBaseTemplate().getPlayerClass().getId()))
						{
							player._isSamurai = true;
						}
						
						player.setApprentice(rset.getInt("apprentice"));
						player.setSponsor(rset.getInt("sponsor"));
						player.setLvlJoinedAcademy(rset.getInt("lvl_joined_academy"));
						player.setHero(Hero.getInstance().isHero(objectId));
						CursedWeaponsManager.getInstance().checkPlayer(player);
						int x = rset.getInt("x");
						int y = rset.getInt("y");
						int z = GeoEngine.getInstance().getHeight(x, y, rset.getInt("z"));
						player.setXYZInvisible(x, y, z);
						player.setLastServerPosition(x, y, z);
						player.setBookMarkSlot(rset.getInt("BookmarkSlot"));
						player.getCreateDate().setTime(rset.getDate("createDate"));
						player.setLang(rset.getString("language"));
						
						try (PreparedStatement stmt = con.prepareStatement("SELECT charId, char_name FROM characters WHERE account_name=? AND charId<>?"))
						{
							stmt.setString(1, player._accountName);
							stmt.setInt(2, objectId);
							
							try (ResultSet chars = stmt.executeQuery())
							{
								while (chars.next())
								{
									player._chars.put(chars.getInt("charId"), chars.getString("char_name"));
								}
							}
						}
					}
				}
				
				if (player != null)
				{
					if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_LOAD, player))
					{
						EventDispatcher.getInstance().notifyEventAsync(new OnPlayerLoad(player), player);
					}
					
					if (PrisonConfig.ENABLE_PRISON)
					{
						PrisonManager.loadPrisoner(player);
					}
					
					player.getInventory().restore();
					player.getWarehouse().restore();
					player.getFreight().restore();
					player.restoreItemReuse();
					player.restoreCharData();
					player.rewardSkills();
					player.restoreShortcuts();
					player.restorePetEvolvesByItem();
					player.initStatusUpdateCache();
					player.setCurrentCp(currentCp);
					player.setCurrentHp(currentHp);
					player.setCurrentMp(currentMp);
					player.setOriginalCpHpMp(currentCp, currentHp, currentMp);
					if (currentHp < 0.5)
					{
						player.setDead(true);
						player.stopHpMpRegeneration();
					}
					
					player.setPet(World.getInstance().getPet(player.getObjectId()));
					pet = player.getPet();
					if (pet != null)
					{
						pet.setOwner(player);
					}
					
					if (player.hasServitors())
					{
						for (Summon summon : player.getServitors().values())
						{
							summon.setOwner(player);
						}
					}
					
					player.getStat().recalculateStats(false);
					player.refreshOverloaded(false);
					player.restoreFriendList();
					player.restoreRandomCraft();
					player.restoreSurveillanceList();
					player.loadRecommendations();
					player.startRecoGiveTask();
					player.startOnlineTimeUpdateTask();
					player.setOnlineStatus(true, false);
					PlayerAutoSaveTaskManager.getInstance().add(player);
					if (AchievementBoxConfig.ENABLE_ACHIEVEMENT_BOX)
					{
						player.getAchievementBox().restore();
					}
					
					return player;
				}
				
				pet = null;
			}
			
			return player;
		}
		catch (Exception var38)
		{
			LOGGER.log(Level.SEVERE, "Failed loading character.", var38);
			return player;
		}
	}
	
	public Forum getMail()
	{
		if (this._forumMail == null)
		{
			this.setMail(ForumsBBSManager.getInstance().getForumByName("MailRoot").getChildByName(this._name));
			if (this._forumMail == null)
			{
				ForumsBBSManager.getInstance().createNewForum(this._name, ForumsBBSManager.getInstance().getForumByName("MailRoot"), 4, 3, this.getObjectId());
				this.setMail(ForumsBBSManager.getInstance().getForumByName("MailRoot").getChildByName(this._name));
			}
		}
		
		return this._forumMail;
	}
	
	public void setMail(Forum forum)
	{
		this._forumMail = forum;
	}
	
	public Forum getMemo()
	{
		if (this._forumMemo == null)
		{
			this.setMemo(ForumsBBSManager.getInstance().getForumByName("MemoRoot").getChildByName(this._accountName));
			if (this._forumMemo == null)
			{
				ForumsBBSManager.getInstance().createNewForum(this._accountName, ForumsBBSManager.getInstance().getForumByName("MemoRoot"), 3, 3, this.getObjectId());
				this.setMemo(ForumsBBSManager.getInstance().getForumByName("MemoRoot").getChildByName(this._accountName));
			}
		}
		
		return this._forumMemo;
	}
	
	public void setMemo(Forum forum)
	{
		this._forumMemo = forum;
	}
	
	private static boolean restoreSubClassData(Player player)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT class_id,exp,sp,level,vitality_points,class_index,dual_class FROM character_subclasses WHERE charId=? ORDER BY class_index ASC");)
		{
			statement.setInt(1, player.getObjectId());
			
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					SubClassHolder subClass = new SubClassHolder();
					subClass.setPlayerClass(rset.getInt("class_id"));
					subClass.setDualClassActive(rset.getBoolean("dual_class"));
					subClass.setVitalityPoints(rset.getInt("vitality_points"));
					subClass.setLevel(rset.getInt("level"));
					subClass.setExp(rset.getLong("exp"));
					subClass.setSp(rset.getLong("sp"));
					subClass.setClassIndex(rset.getInt("class_index"));
					player.getSubClasses().put(subClass.getClassIndex(), subClass);
				}
			}
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.WARNING, "Could not restore classes for " + player.getName() + ": " + var12.getMessage(), var12);
		}
		
		return true;
	}
	
	private void restoreCharData()
	{
		this.restoreSkills();
		this._macros.restoreMe();
		this.restoreHenna();
		this.restoreTeleportBookmark();
		this.restoreRecipeBook(true);
		if (PlayerConfig.STORE_RECIPE_SHOPLIST)
		{
			this.restoreRecipeShopList();
		}
		
		this.restoreItemPenaltyList();
		this.restoreCollections();
		this.restoreCollectionBonuses();
		this.restoreCollectionFavorites();
		this._challengePoints.restoreChallengePoints();
		this.restoreCrossEvent();
		this.restoreSubjugation();
		this.restoreRelics();
		this.sendPacket(new ExRelicsList(this));
		this.restoreRelicCollections();
		this.restoreRelicCollectionBonuses();
		this.sendPacket(new ExRelicsCollectionInfo(this));
		this.loadPremiumItemList();
		this.restorePetInventoryItems();
	}
	
	private void restoreShortcuts()
	{
		this._shortcuts.restoreMe();
	}
	
	private void restoreRecipeBook(boolean loadCommon)
	{
		String sql = loadCommon ? "SELECT id, type, classIndex FROM character_recipebook WHERE charId=?" : "SELECT id FROM character_recipebook WHERE charId=? AND classIndex=? AND type = 1";
		
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement(sql);)
		{
			statement.setInt(1, this.getObjectId());
			if (!loadCommon)
			{
				statement.setInt(2, this._classIndex);
			}
			
			try (ResultSet rset = statement.executeQuery())
			{
				this._dwarvenRecipeBook.clear();
				RecipeData rd = RecipeData.getInstance();
				
				while (rset.next())
				{
					RecipeList recipe = rd.getRecipeList(rset.getInt("id"));
					if (loadCommon)
					{
						if (rset.getInt(2) == 1)
						{
							if (rset.getInt(3) == this._classIndex)
							{
								this.registerDwarvenRecipeList(recipe, false);
							}
						}
						else
						{
							this.registerCommonRecipeList(recipe, false);
						}
					}
					else
					{
						this.registerDwarvenRecipeList(recipe, false);
					}
				}
			}
		}
		catch (Exception var14)
		{
			LOGGER.log(Level.SEVERE, "Could not restore recipe book data:" + var14.getMessage(), var14);
		}
	}
	
	public Map<Integer, PremiumItem> getPremiumItemList()
	{
		return this._premiumItems;
	}
	
	private void loadPremiumItemList()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT itemNum, itemId, itemCount, itemSender FROM character_premium_items WHERE charId=?");)
		{
			statement.setInt(1, this.getObjectId());
			
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					int itemNum = rset.getInt("itemNum");
					int itemId = rset.getInt("itemId");
					long itemCount = rset.getLong("itemCount");
					String itemSender = rset.getString("itemSender");
					this._premiumItems.put(itemNum, new PremiumItem(itemId, itemCount, itemSender));
				}
			}
		}
		catch (Exception var16)
		{
			LOGGER.log(Level.SEVERE, "Could not restore premium items: " + var16.getMessage(), var16);
		}
	}
	
	public void updatePremiumItem(int itemNum, long newcount)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("UPDATE character_premium_items SET itemCount=? WHERE charId=? AND itemNum=? ");)
		{
			statement.setLong(1, newcount);
			statement.setInt(2, this.getObjectId());
			statement.setInt(3, itemNum);
			statement.execute();
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.SEVERE, "Could not update premium items: " + var12.getMessage(), var12);
		}
	}
	
	public void deletePremiumItem(int itemNum)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("DELETE FROM character_premium_items WHERE charId=? AND itemNum=? ");)
		{
			statement.setInt(1, this.getObjectId());
			statement.setInt(2, itemNum);
			statement.execute();
		}
		catch (Exception var10)
		{
			LOGGER.severe("Could not delete premium item: " + var10);
		}
	}
	
	public synchronized void store(boolean storeActiveEffects)
	{
		if (PrisonConfig.ENABLE_PRISON && this.isPrisoner())
		{
			PrisonManager.savePrisonerOnVar(this.getObjectId(), this._prisonerInfo);
		}
		
		this.storeCharBase();
		this.storeCharSub();
		this.storeEffect(storeActiveEffects);
		this.storeItemReuseDelay();
		this.storeDyePoten();
		if (PlayerConfig.STORE_RECIPE_SHOPLIST)
		{
			this.storeRecipeShopList();
		}
		
		this._rankingHistory.store();
		this.storeCollections();
		this.storeCollectionFavorites();
		this.storeSubjugation();
		this._challengePoints.storeChallengePoints();
		this.storeDualInventory();
		this.storeCrossEvent();
		PlayerVariables vars = this.getScript(PlayerVariables.class);
		if (vars != null)
		{
			vars.storeMe();
		}
		
		AccountVariables aVars = this.getScript(AccountVariables.class);
		if (aVars != null)
		{
			aVars.storeMe();
		}
		
		this.getInventory().updateDatabase();
		this.getWarehouse().updateDatabase();
		this.getFreight().updateDatabase();
		if (this._spirits != null)
		{
			for (ElementalSpirit spirit : this._spirits)
			{
				if (spirit != null)
				{
					spirit.save();
				}
			}
		}
		
		if (this._randomCraft != null)
		{
			this._randomCraft.store();
		}
		
		if (this._huntPass != null)
		{
			this._huntPass.store();
		}
		
		if (this._achivemenetBox != null)
		{
			this._achivemenetBox.store();
		}
	}
	
	@Override
	public void storeMe()
	{
		this.store(true);
	}
	
	private void storeCharBase()
	{
		long exp = this.getStat().getBaseExp();
		int level = this.getStat().getBaseLevel();
		long sp = this.getStat().getBaseSp();
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET level=?,maxHp=?,curHp=?,maxCp=?,curCp=?,maxMp=?,curMp=?,face=?,hairStyle=?,hairColor=?,sex=?,heading=?,x=?,y=?,z=?,exp=?,expBeforeDeath=?,sp=?,reputation=?,fame=?,raidbossPoints=?,pvpkills=?,pkkills=?,clanid=?,race=?,classid=?,deletetime=?,title=?,title_color=?,online=?,clan_privs=?,wantspeace=?,base_class=?,onlinetime=?,nobless=?,power_grade=?,subpledge=?,lvl_joined_academy=?,apprentice=?,sponsor=?,clan_join_expiry_time=?,clan_create_expiry_time=?,char_name=?,bookmarkslot=?,vitality_points=?,language=?,faction=?,pccafe_points=?,kills=?,deaths=? WHERE charId=?");)
		{
			statement.setInt(1, level);
			statement.setInt(2, (int) this.getMaxHp());
			statement.setDouble(3, this.getCurrentHp());
			statement.setInt(4, this.getMaxCp());
			statement.setDouble(5, this.getCurrentCp());
			statement.setInt(6, this.getMaxMp());
			statement.setDouble(7, this.getCurrentMp());
			statement.setInt(8, this._appearance.getFace());
			statement.setInt(9, this._appearance.getHairStyle());
			statement.setInt(10, this._appearance.getHairColor());
			statement.setInt(11, this._appearance.isFemale() ? 1 : 0);
			statement.setInt(12, this.getHeading());
			statement.setInt(13, this._lastLoc != null ? this._lastLoc.getX() : this.getX());
			statement.setInt(14, this._lastLoc != null ? this._lastLoc.getY() : this.getY());
			statement.setInt(15, this._lastLoc != null ? this._lastLoc.getZ() : this.getZ());
			statement.setLong(16, exp);
			statement.setLong(17, this._expBeforeDeath);
			statement.setLong(18, sp);
			statement.setInt(19, this.getReputation());
			statement.setInt(20, this._fame);
			statement.setInt(21, this._raidbossPoints);
			statement.setInt(22, this._pvpKills);
			statement.setInt(23, this._pkKills);
			statement.setInt(24, this.getOgClanId());
			statement.setInt(25, this.getRace().ordinal());
			statement.setInt(26, this.getPlayerClass().getId());
			statement.setLong(27, this._deleteTimer);
			statement.setString(28, this.getTitle());
			statement.setInt(29, this._appearance.getTitleColor());
			statement.setInt(30, this.isOnlineInt());
			statement.setInt(31, this._clanPrivileges.getMask());
			statement.setInt(32, this._wantsPeace);
			statement.setInt(33, this._baseClass);
			long totalOnlineTime = this._onlineTime;
			if (this._onlineBeginTime > 0L)
			{
				totalOnlineTime += (System.currentTimeMillis() - this._onlineBeginTime) / 1000L;
			}
			
			statement.setLong(34, this._offlineShopStart > 0L ? this._onlineTime : totalOnlineTime);
			statement.setInt(35, this.isNoble() ? 1 : 0);
			statement.setInt(36, this._powerGrade);
			statement.setInt(37, this._pledgeType);
			statement.setInt(38, this._lvlJoinedAcademy);
			statement.setLong(39, this._apprentice);
			statement.setLong(40, this._sponsor);
			statement.setLong(41, this._clanJoinExpiryTime);
			statement.setLong(42, this._clanCreateExpiryTime);
			statement.setString(43, this._name);
			statement.setInt(44, this._bookmarkslot);
			statement.setInt(45, this.getStat().getBaseVitalityPoints());
			statement.setString(46, this._lang);
			int factionId = 0;
			if (this._isGood)
			{
				factionId = 1;
			}
			
			if (this._isEvil)
			{
				factionId = 2;
			}
			
			statement.setInt(47, factionId);
			statement.setInt(48, this._pcCafePoints);
			statement.setInt(49, this.getTotalKills());
			statement.setInt(50, this.getTotalDeaths());
			statement.setInt(51, this.getObjectId());
			statement.execute();
		}
		catch (Exception var15)
		{
			LOGGER.log(Level.WARNING, "Could not store char base data: " + this + " - " + var15.getMessage(), var15);
		}
	}
	
	private void storeCharSub()
	{
		if (this.getTotalSubClasses() > 0)
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("UPDATE character_subclasses SET exp=?,sp=?,level=?,vitality_points=?,class_id=?,dual_class=? WHERE charId=? AND class_index =?");)
			{
				for (SubClassHolder subClass : this.getSubClasses().values())
				{
					statement.setLong(1, subClass.getExp());
					statement.setLong(2, subClass.getSp());
					statement.setInt(3, subClass.getLevel());
					statement.setInt(4, subClass.getVitalityPoints());
					statement.setInt(5, subClass.getId());
					statement.setBoolean(6, subClass.isDualClass());
					statement.setInt(7, this.getObjectId());
					statement.setInt(8, subClass.getClassIndex());
					statement.addBatch();
				}
				
				statement.executeBatch();
			}
			catch (Exception var9)
			{
				LOGGER.log(Level.WARNING, "Could not store sub class data for " + this._name + ": " + var9.getMessage(), var9);
			}
		}
	}
	
	@Override
	public void storeEffect(boolean storeEffects)
	{
		if (PlayerConfig.STORE_SKILL_COOLTIME)
		{
			try (Connection con = DatabaseFactory.getConnection())
			{
				try (PreparedStatement delete = con.prepareStatement("DELETE FROM character_skills_save WHERE charId=? AND class_index=?"))
				{
					delete.setInt(1, this.getObjectId());
					delete.setInt(2, this._classIndex);
					delete.execute();
				}
				
				int buffIndex = 0;
				List<Long> storedSkills = new ArrayList<>();
				long currentTime = System.currentTimeMillis();
				
				try (PreparedStatement statement = con.prepareStatement("REPLACE INTO character_skills_save (charId,skill_id,skill_level,skill_sub_level,remaining_time,reuse_delay,systime,restore_type,class_index,buff_index) VALUES (?,?,?,?,?,?,?,?,?,?)"))
				{
					if (storeEffects)
					{
						for (BuffInfo info : this.getEffectList().getEffects())
						{
							if (info != null)
							{
								Skill skill = info.getSkill();
								if (!skill.isDeleteAbnormalOnLeave() && skill.getAbnormalType() != AbnormalType.LIFE_FORCE_OTHERS && (PlayerConfig.ALT_STORE_TOGGLES || !skill.isToggle() || skill.isNecessaryToggle()) && !skill.isMentoring() && (!skill.isDance() || PlayerConfig.ALT_STORE_DANCES) && !storedSkills.contains(skill.getReuseHashCode()))
								{
									storedSkills.add(skill.getReuseHashCode());
									statement.setInt(1, this.getObjectId());
									statement.setInt(2, skill.getId());
									statement.setInt(3, skill.getLevel());
									statement.setInt(4, skill.getSubLevel());
									statement.setInt(5, info.getTime());
									TimeStamp t = this.getSkillReuseTimeStamp(skill.getReuseHashCode());
									statement.setLong(6, t != null && currentTime < t.getStamp() ? t.getReuse() : 0L);
									statement.setDouble(7, t != null && currentTime < t.getStamp() ? t.getStamp() : 0.0);
									statement.setInt(8, 0);
									statement.setInt(9, this._classIndex);
									statement.setInt(10, ++buffIndex);
									statement.addBatch();
								}
							}
						}
					}
					
					for (Entry<Long, TimeStamp> ts : this.getSkillReuseTimeStamps().entrySet())
					{
						long hash = ts.getKey();
						if (!storedSkills.contains(hash))
						{
							TimeStamp t = ts.getValue();
							if (t != null && currentTime < t.getStamp())
							{
								storedSkills.add(hash);
								statement.setInt(1, this.getObjectId());
								statement.setInt(2, t.getSkillId());
								statement.setInt(3, t.getSkillLevel());
								statement.setInt(4, t.getSkillSubLevel());
								statement.setInt(5, -1);
								statement.setLong(6, t.getReuse());
								statement.setDouble(7, t.getStamp());
								statement.setInt(8, 1);
								statement.setInt(9, this._classIndex);
								statement.setInt(10, ++buffIndex);
								statement.addBatch();
							}
						}
					}
					
					statement.executeBatch();
				}
			}
			catch (Exception var19)
			{
				LOGGER.log(Level.WARNING, "Could not store char effect data: ", var19);
			}
		}
	}
	
	private void storeItemReuseDelay()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps1 = con.prepareStatement("DELETE FROM character_item_reuse_save WHERE charId=?");
			PreparedStatement ps2 = con.prepareStatement("INSERT INTO character_item_reuse_save (charId,itemId,itemObjId,reuseDelay,systime) VALUES (?,?,?,?,?)");)
		{
			ps1.setInt(1, this.getObjectId());
			ps1.execute();
			long currentTime = System.currentTimeMillis();
			
			for (TimeStamp ts : this.getItemReuseTimeStamps().values())
			{
				if (ts != null && currentTime < ts.getStamp())
				{
					ps2.setInt(1, this.getObjectId());
					ps2.setInt(2, ts.getItemId());
					ps2.setInt(3, ts.getItemObjectId());
					ps2.setLong(4, ts.getReuse());
					ps2.setDouble(5, ts.getStamp());
					ps2.addBatch();
				}
			}
			
			ps2.executeBatch();
		}
		catch (Exception var14)
		{
			LOGGER.log(Level.WARNING, "Could not store char item reuse data: ", var14);
		}
	}
	
	public boolean isOnline()
	{
		return this._isOnline;
	}
	
	public int isOnlineInt()
	{
		if (this._isOnline && this._client != null)
		{
			return this._client.isDetached() ? 2 : 1;
		}
		return 0;
	}
	
	public void startOfflinePlay()
	{
		if (this.hasPremiumStatus() && DualboxCheckConfig.DUALBOX_CHECK_MAX_OFFLINEPLAY_PREMIUM_PER_IP > 0 && !AntiFeedManager.getInstance().tryAddPlayer(4, this, DualboxCheckConfig.DUALBOX_CHECK_MAX_OFFLINEPLAY_PREMIUM_PER_IP))
		{
			String limit = String.valueOf(AntiFeedManager.getInstance().getLimit(this, DualboxCheckConfig.DUALBOX_CHECK_MAX_OFFLINEPLAY_PER_IP));
			this.sendMessage("Only " + limit + " offline players allowed per IP.");
		}
		else if (DualboxCheckConfig.DUALBOX_CHECK_MAX_OFFLINEPLAY_PER_IP > 0 && !AntiFeedManager.getInstance().tryAddPlayer(4, this, DualboxCheckConfig.DUALBOX_CHECK_MAX_OFFLINEPLAY_PER_IP))
		{
			String limit = String.valueOf(AntiFeedManager.getInstance().getLimit(this, DualboxCheckConfig.DUALBOX_CHECK_MAX_OFFLINEPLAY_PER_IP));
			this.sendMessage("Only " + limit + " offline players allowed per IP.");
		}
		else
		{
			AntiFeedManager.getInstance().removePlayer(0, this);
			this.sendPacket(LeaveWorld.STATIC_PACKET);
			if (OfflinePlayConfig.OFFLINE_PLAY_SET_NAME_COLOR)
			{
				this.getAppearance().setNameColor(OfflinePlayConfig.OFFLINE_PLAY_NAME_COLOR);
			}
			
			if (!OfflinePlayConfig.OFFLINE_PLAY_ABNORMAL_EFFECTS.isEmpty())
			{
				this.getEffectList().startAbnormalVisualEffect(OfflinePlayConfig.OFFLINE_PLAY_ABNORMAL_EFFECTS.get(Rnd.get(OfflinePlayConfig.OFFLINE_PLAY_ABNORMAL_EFFECTS.size())));
			}
			
			this.broadcastUserInfo();
			this._offlinePlay = true;
			this._client.setDetached(true);
			if (OfflinePlayConfig.RESTORE_AUTO_PLAY_OFFLINERS)
			{
				OfflinePlayTable.getInstance().storeOfflinePlay(this);
			}
		}
	}
	
	public boolean isOfflinePlay()
	{
		return this._offlinePlay;
	}
	
	public void setOfflinePlay(boolean value)
	{
		this._offlinePlay = value;
	}
	
	public void setEnteredWorld()
	{
		this._enteredWorld = true;
	}
	
	public boolean hasEnteredWorld()
	{
		return this._enteredWorld;
	}
	
	public boolean isInOfflineMode()
	{
		return this._client == null || this._client.isDetached();
	}
	
	@Override
	public Skill addSkill(Skill newSkill)
	{
		this.addCustomSkill(newSkill);
		return super.addSkill(newSkill);
	}
	
	public Skill addSkill(Skill newSkill, boolean store)
	{
		Skill oldSkill = this.addSkill(newSkill);
		if (store)
		{
			this.storeSkill(newSkill, oldSkill, -1);
		}
		
		return oldSkill;
	}
	
	@Override
	public Skill removeSkill(Skill skill, boolean store)
	{
		this.removeCustomSkill(skill);
		return store ? this.removeSkill(skill) : super.removeSkill(skill, true);
	}
	
	public Skill removeSkill(Skill skill, boolean store, boolean cancelEffect)
	{
		this.removeCustomSkill(skill);
		return store ? this.removeSkill(skill) : super.removeSkill(skill, cancelEffect);
	}
	
	public Skill removeSkill(Skill skill)
	{
		this.removeCustomSkill(skill);
		Skill oldSkill = super.removeSkill(skill, true);
		if (oldSkill != null)
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("DELETE FROM character_skills WHERE skill_id=? AND charId=? AND class_index=?");)
			{
				statement.setInt(1, oldSkill.getId());
				statement.setInt(2, this.getObjectId());
				statement.setInt(3, this._classIndex);
				statement.execute();
			}
			catch (Exception var11)
			{
				LOGGER.log(Level.WARNING, "Error could not delete skill: " + var11.getMessage(), var11);
			}
		}
		
		if (this.getTransformationId() <= 0 && !this.isCursedWeaponEquipped())
		{
			if (skill != null)
			{
				for (Shortcut sc : this._shortcuts.getAllShortcuts())
				{
					if (sc != null && sc.getId() == skill.getId() && sc.getType() == ShortcutType.SKILL && (skill.getId() < 3080 || skill.getId() > 3259))
					{
						this.deleteShortcut(sc.getSlot(), sc.getPage());
					}
				}
			}
			
			return oldSkill;
		}
		return oldSkill;
	}
	
	private void storeSkill(Skill newSkill, Skill oldSkill, int newClassIndex)
	{
		int classIndex = newClassIndex > -1 ? newClassIndex : this._classIndex;
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			if (oldSkill != null && newSkill != null)
			{
				try (PreparedStatement ps = con.prepareStatement("UPDATE character_skills SET skill_level=?, skill_sub_level=?  WHERE skill_id=? AND charId=? AND class_index=?"))
				{
					ps.setInt(1, newSkill.getLevel());
					ps.setInt(2, newSkill.getSubLevel());
					ps.setInt(3, oldSkill.getId());
					ps.setInt(4, this.getObjectId());
					ps.setInt(5, classIndex);
					ps.execute();
				}
			}
			else if (newSkill != null)
			{
				try (PreparedStatement ps = con.prepareStatement("REPLACE INTO character_skills (charId,skill_id,skill_level,skill_sub_level,class_index) VALUES (?,?,?,?,?)"))
				{
					ps.setInt(1, this.getObjectId());
					ps.setInt(2, newSkill.getId());
					ps.setInt(3, newSkill.getLevel());
					ps.setInt(4, newSkill.getSubLevel());
					ps.setInt(5, classIndex);
					ps.execute();
				}
			}
		}
		catch (Exception var15)
		{
			LOGGER.log(Level.WARNING, "Error could not store char skills: " + var15.getMessage(), var15);
		}
	}
	
	private void storeSkills(List<Skill> newSkills, int newClassIndex)
	{
		if (!newSkills.isEmpty())
		{
			int classIndex = newClassIndex > -1 ? newClassIndex : this._classIndex;
			
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("REPLACE INTO character_skills (charId,skill_id,skill_level,skill_sub_level,class_index) VALUES (?,?,?,?,?)");)
			{
				for (Skill addSkill : newSkills)
				{
					ps.setInt(1, this.getObjectId());
					ps.setInt(2, addSkill.getId());
					ps.setInt(3, addSkill.getLevel());
					ps.setInt(4, addSkill.getSubLevel());
					ps.setInt(5, classIndex);
					ps.addBatch();
				}
				
				ps.executeBatch();
			}
			catch (SQLException var12)
			{
				LOGGER.log(Level.WARNING, "Error could not store char skills: " + var12.getMessage(), var12);
			}
		}
	}
	
	private void restoreSkills()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT skill_id,skill_level,skill_sub_level FROM character_skills WHERE charId=? AND class_index=?");)
		{
			statement.setInt(1, this.getObjectId());
			statement.setInt(2, this._classIndex);
			
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					int id = rset.getInt("skill_id");
					int level = rset.getInt("skill_level");
					int subLevel = rset.getInt("skill_sub_level");
					Skill skill = SkillData.getInstance().getSkill(id, level, subLevel);
					if (skill == null)
					{
						LOGGER.warning("Skipped null skill Id: " + id + " Level: " + level + " while restoring player skills for playerObjId: " + this.getObjectId());
					}
					else
					{
						this.addSkill(skill);
						if (GeneralConfig.SKILL_CHECK_ENABLE && (!this.isGM() || GeneralConfig.SKILL_CHECK_GM) && !SkillTreeData.getInstance().isSkillAllowed(this, skill))
						{
							PunishmentManager.handleIllegalPlayerAction(this, "Player " + this._name + " has invalid skill " + skill.getName() + " (" + skill.getId() + "/" + skill.getLevel() + "), class:" + ClassListData.getInstance().getClass(this.getPlayerClass()).getClassName(), IllegalActionPunishmentType.BROADCAST);
							if (GeneralConfig.SKILL_CHECK_REMOVE)
							{
								this.removeSkill(skill);
							}
						}
					}
				}
			}
		}
		catch (Exception var14)
		{
			LOGGER.log(Level.WARNING, "Could not restore character " + this + " skills: " + var14.getMessage(), var14);
		}
	}
	
	@Override
	public void restoreEffects()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT skill_id,skill_level,skill_sub_level,remaining_time, reuse_delay, systime, restore_type FROM character_skills_save WHERE charId=? AND class_index=? ORDER BY buff_index ASC");)
		{
			statement.setInt(1, this.getObjectId());
			statement.setInt(2, this._classIndex);
			
			try (ResultSet rset = statement.executeQuery())
			{
				long currentTime = System.currentTimeMillis();
				
				while (rset.next())
				{
					int remainingTime = rset.getInt("remaining_time");
					long reuseDelay = rset.getLong("reuse_delay");
					long systime = rset.getLong("systime");
					int restoreType = rset.getInt("restore_type");
					Skill skill = SkillData.getInstance().getSkill(rset.getInt("skill_id"), rset.getInt("skill_level"), rset.getInt("skill_sub_level"));
					if (skill != null)
					{
						long time = systime - currentTime;
						if (time > 10L)
						{
							this.disableSkill(skill, time);
							this.addTimeStamp(skill, reuseDelay, systime);
						}
						
						if (restoreType <= 0)
						{
							skill.applyEffects(this, this, false, remainingTime);
						}
					}
				}
			}
			
			try (PreparedStatement delete = con.prepareStatement("DELETE FROM character_skills_save WHERE charId=? AND class_index=?"))
			{
				delete.setInt(1, this.getObjectId());
				delete.setInt(2, this._classIndex);
				delete.executeUpdate();
			}
		}
		catch (Exception var23)
		{
			LOGGER.log(Level.WARNING, "Could not restore " + this + " active effect data: " + var23.getMessage(), var23);
		}
	}
	
	private void restoreItemReuse()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT charId,itemId,itemObjId,reuseDelay,systime FROM character_item_reuse_save WHERE charId=?");
			PreparedStatement delete = con.prepareStatement("DELETE FROM character_item_reuse_save WHERE charId=?");)
		{
			statement.setInt(1, this.getObjectId());
			
			try (ResultSet rset = statement.executeQuery())
			{
				long currentTime = System.currentTimeMillis();
				
				while (rset.next())
				{
					int itemId = rset.getInt("itemId");
					long reuseDelay = rset.getLong("reuseDelay");
					long systime = rset.getLong("systime");
					boolean isInInventory = true;
					Item item = this._inventory.getItemByItemId(itemId);
					if (item == null)
					{
						item = this.getWarehouse().getItemByItemId(itemId);
						isInInventory = false;
					}
					
					if (item != null && item.getId() == itemId && item.getReuseDelay() > 0)
					{
						long remainingTime = systime - currentTime;
						if (remainingTime > 10L)
						{
							this.addTimeStampItem(item, reuseDelay, systime);
							if (isInInventory && item.isEtcItem())
							{
								int group = item.getSharedReuseGroup();
								if (group > 0)
								{
									this.sendPacket(new ExUseSharedGroupItem(itemId, group, (int) remainingTime, (int) reuseDelay));
								}
							}
						}
					}
				}
			}
			
			delete.setInt(1, this.getObjectId());
			delete.executeUpdate();
		}
		catch (Exception var25)
		{
			LOGGER.log(Level.WARNING, "Could not restore " + this + " Item Reuse data: " + var25.getMessage(), var25);
		}
	}
	
	private void restoreHenna()
	{
		this.restoreDyePoten();
		
		for (Entry<Integer, ScheduledFuture<?>> entry : this._hennaRemoveSchedules.entrySet())
		{
			ScheduledFuture<?> task = entry.getValue();
			if (task != null && !task.isCancelled() && !task.isDone())
			{
				task.cancel(true);
			}
			
			this._hennaRemoveSchedules.remove(entry.getKey());
		}
		
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT slot,symbol_id FROM character_hennas WHERE charId=? AND class_index=?");)
		{
			statement.setInt(1, this.getObjectId());
			statement.setInt(2, this._classIndex);
			
			try (ResultSet rset = statement.executeQuery())
			{
				long currentTime = System.currentTimeMillis();
				
				while (rset.next())
				{
					int slot = rset.getInt("slot");
					if (slot >= 1 && slot <= this.getAvailableHennaSlots())
					{
						int symbolId = rset.getInt("symbol_id");
						if (symbolId != 0)
						{
							Henna henna = HennaData.getInstance().getHennaByDyeId(symbolId);
							if (henna != null)
							{
								if (henna.getDuration() > 0)
								{
									long remainingTime = this.getVariables().getLong("HennaDuration" + slot, currentTime) - currentTime;
									if (remainingTime < 0L)
									{
										this.removeHenna(slot);
										continue;
									}
									
									this._hennaRemoveSchedules.put(slot, ThreadPool.schedule(new HennaDurationTask(this, slot), currentTime + remainingTime));
								}
								
								this._hennaPoten[slot - 1].setHenna(henna);
								
								for (Skill skill : henna.getSkills())
								{
									this.addSkill(skill, false);
								}
							}
						}
					}
				}
			}
		}
		catch (Exception var17)
		{
			LOGGER.log(Level.SEVERE, "Failed restoing character " + this + " hennas.", var17);
		}
		
		this.recalcHennaStats(false);
		this.applyDyePotenSkills();
	}
	
	public int getHennaEmptySlots()
	{
		int totalSlots = 0;
		if (this.getPlayerClass().level() == 1)
		{
			totalSlots = 2;
		}
		else if (this.getPlayerClass().level() > 1)
		{
			totalSlots = this.getAvailableHennaSlots();
		}
		
		for (HennaPoten element : this._hennaPoten)
		{
			if (element.getHenna() != null)
			{
				totalSlots--;
			}
		}
		
		return totalSlots <= 0 ? 0 : totalSlots;
	}
	
	public boolean removeHenna(int slot)
	{
		return this.removeHenna(slot, true);
	}
	
	public boolean removeHenna(int slot, boolean restoreDye)
	{
		if (slot >= 1 && slot <= this._hennaPoten.length)
		{
			Henna henna = this._hennaPoten[slot - 1].getHenna();
			if (henna == null)
			{
				return false;
			}
			this._hennaPoten[slot - 1].setHenna(null);
			
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("DELETE FROM character_hennas WHERE charId=? AND slot=? AND class_index=?");)
			{
				statement.setInt(1, this.getObjectId());
				statement.setInt(2, slot);
				statement.setInt(3, this._classIndex);
				statement.execute();
			}
			catch (Exception var12)
			{
				LOGGER.log(Level.SEVERE, "Failed removing character henna.", var12);
			}
			
			this.recalcHennaStats(true);
			this.broadcastUserInfo(UserInfoType.BASE_STATS, UserInfoType.STAT_POINTS, UserInfoType.STAT_ABILITIES, UserInfoType.MAX_HPCPMP, UserInfoType.STATS, UserInfoType.SPEED);
			if (henna.getCancelCount() > 0 && restoreDye)
			{
				long remainingTime = this.getVariables().getLong("HennaDuration" + slot, 0L) - System.currentTimeMillis();
				if (remainingTime > 0L || henna.getDuration() < 0)
				{
					this._inventory.addItem(ItemProcessType.RESTORE, henna.getDyeItemId(), henna.getCancelCount(), this, null);
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1_X_S2);
					sm.addItemName(henna.getDyeItemId());
					sm.addLong(henna.getCancelCount());
					this.sendPacket(sm);
				}
			}
			
			this.sendPacket(SystemMessageId.PATTERN_WAS_DELETED);
			if (henna.getDuration() > 0)
			{
				this.getVariables().remove("HennaDuration" + slot);
				if (this._hennaRemoveSchedules.get(slot) != null)
				{
					this._hennaRemoveSchedules.get(slot).cancel(false);
					this._hennaRemoveSchedules.remove(slot);
				}
			}
			
			for (Skill skill : henna.getSkills())
			{
				this.removeSkill(skill, false);
			}
			
			if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_HENNA_REMOVE, this))
			{
				EventDispatcher.getInstance().notifyEventAsync(new OnPlayerHennaRemove(this, henna), this);
			}
			
			return true;
		}
		return false;
	}
	
	public boolean addHenna(int slotId, Henna henna)
	{
		if (slotId > this.getAvailableHennaSlots())
		{
			return false;
		}
		else if (this._hennaPoten[slotId - 1].getHenna() == null)
		{
			this._hennaPoten[slotId - 1].setHenna(henna);
			this.recalcHennaStats(true);
			
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("INSERT INTO character_hennas (charId,symbol_id,slot,class_index) VALUES (?,?,?,?)");)
			{
				statement.setInt(1, this.getObjectId());
				statement.setInt(2, henna.getDyeId());
				statement.setInt(3, slotId);
				statement.setInt(4, this._classIndex);
				statement.execute();
			}
			catch (Exception var11)
			{
				LOGGER.log(Level.SEVERE, "Failed saving character henna.", var11);
			}
			
			if (henna.getDuration() > 0)
			{
				this.getVariables().set("HennaDuration" + slotId, System.currentTimeMillis() + henna.getDuration() * 60000);
				this._hennaRemoveSchedules.put(slotId, ThreadPool.schedule(new HennaDurationTask(this, slotId), System.currentTimeMillis() + henna.getDuration() * 60000));
			}
			
			for (Skill skill : henna.getSkills())
			{
				if (skill.getLevel() > this.getSkillLevel(skill.getId()))
				{
					this.addSkill(skill, false);
				}
			}
			
			this.broadcastUserInfo(UserInfoType.BASE_STATS, UserInfoType.STAT_ABILITIES, UserInfoType.STAT_POINTS, UserInfoType.MAX_HPCPMP, UserInfoType.STATS, UserInfoType.SPEED);
			if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_HENNA_ADD, this))
			{
				EventDispatcher.getInstance().notifyEventAsync(new OnPlayerHennaAdd(this, henna), this);
			}
			
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private void recalcHennaStats(boolean recalculatePlayerStat)
	{
		this._hennaBaseStats.clear();
		
		for (HennaPoten hennaPoten : this._hennaPoten)
		{
			Henna henna = hennaPoten.getHenna();
			if (henna != null)
			{
				for (Entry<BaseStat, Integer> entry : henna.getBaseStats().entrySet())
				{
					this._hennaBaseStats.merge(entry.getKey(), entry.getValue(), Integer::sum);
				}
			}
		}
		
		if (recalculatePlayerStat)
		{
			this.getStat().recalculateStats(true);
			this.getStatus().startHpMpRegeneration();
		}
	}
	
	private void restoreDyePoten()
	{
		int pos = 0;
		
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT slot_position,poten_id,enchant_level,enchant_exp,unlock_slot FROM character_potens WHERE charId=? ORDER BY slot_position ASC");)
		{
			statement.setInt(1, this.getObjectId());
			
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					this._hennaPoten[pos] = new HennaPoten();
					this._hennaPoten[pos].setSlotPosition(rset.getInt("slot_position"));
					this._hennaPoten[pos].setEnchantLevel(rset.getInt("enchant_level"));
					this._hennaPoten[pos].setEnchantExp(rset.getInt("enchant_exp"));
					this._hennaPoten[pos].setPotenId(rset.getInt("poten_id"));
					this._hennaPoten[pos].setUnlockSlot(rset.getInt("unlock_slot"));
					pos++;
				}
			}
		}
		catch (Exception var13)
		{
			LOGGER.log(Level.SEVERE, "Failed restoring character " + this + " henna potential.", var13);
		}
		
		for (int i = pos; i < 4; i++)
		{
			this._hennaPoten[i] = new HennaPoten();
			this._hennaPoten[i].setSlotPosition(i + 1);
		}
		
		this.applyDyePotenSkills();
	}
	
	private void storeDyePoten()
	{
		for (int i = 0; i < 4; i++)
		{
			if (this._hennaPoten[i] != null && this._hennaPoten[i].getSlotPosition() > 0)
			{
				try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("REPLACE INTO character_potens (charId,slot_position,poten_id,enchant_level,enchant_exp,unlock_slot) VALUES (?,?,?,?,?,?)");)
				{
					statement.setInt(1, this.getObjectId());
					statement.setInt(2, this._hennaPoten[i].getSlotPosition());
					statement.setInt(3, this._hennaPoten[i].getPotenId());
					statement.setInt(4, this._hennaPoten[i].getEnchantLevel());
					statement.setInt(5, this._hennaPoten[i].getEnchantExp());
					statement.setInt(6, this._hennaPoten[i].getUnlockSlot());
					statement.execute();
				}
				catch (Exception var10)
				{
					LOGGER.log(Level.SEVERE, "Failed saving character " + this + " henna potential.", var10);
				}
			}
		}
	}
	
	public void applyDyePotenSkills()
	{
		for (int i = 1; i <= this._hennaPoten.length; i++)
		{
			for (int skillId : HennaPatternPotentialData.getInstance().getSkillIdsBySlotId(i))
			{
				this.removeSkill(skillId);
			}
			
			HennaPoten hennaPoten = this._hennaPoten[i - 1];
			if (hennaPoten != null && hennaPoten.getPotenId() > 0 && hennaPoten.getActiveStep() > 0)
			{
				Skill hennaSkill = HennaPatternPotentialData.getInstance().getPotentialSkill(hennaPoten.getPotenId(), i, hennaPoten.getActiveStep());
				if (hennaSkill != null && hennaSkill.getLevel() > this.getSkillLevel(hennaSkill.getId()))
				{
					this.addSkill(hennaSkill, false);
				}
			}
		}
		
		this.sendSkillList();
	}
	
	public HennaPoten getHennaPoten(int slot)
	{
		return slot >= 1 && slot <= this._hennaPoten.length ? this._hennaPoten[slot - 1] : null;
	}
	
	public Henna getHenna(int slot)
	{
		int index = slot - 1;
		if (index >= 0 && index < this._hennaPoten.length && index < this.getAvailableHennaSlots())
		{
			HennaPoten poten = this._hennaPoten[index];
			return poten == null ? null : poten.getHenna();
		}
		return null;
	}
	
	public boolean hasHennas()
	{
		for (HennaPoten hennaPoten : this._hennaPoten)
		{
			Henna henna = hennaPoten.getHenna();
			if (henna != null)
			{
				return true;
			}
		}
		
		return false;
	}
	
	public HennaPoten[] getHennaPotenList()
	{
		return this._hennaPoten;
	}
	
	public int getHennaValue(BaseStat stat)
	{
		return this._hennaBaseStats.getOrDefault(stat, 0);
	}
	
	public int getAvailableHennaSlots()
	{
		return (int) this.getStat().getValue(Stat.HENNA_SLOTS_AVAILABLE, 4.0);
	}
	
	public void setDyePotentialDailyStep(int dailyStep)
	{
		this.getVariables().set("DYE_POTENTIAL_DAILY_STEP", dailyStep);
	}
	
	public void setDyePotentialDailyCount(int dailyCount)
	{
		this.getVariables().set("DYE_POTENTIAL_DAILY_COUNT", dailyCount);
	}
	
	public int getDyePotentialDailyStep()
	{
		return this.getVariables().getInt("DYE_POTENTIAL_DAILY_STEP", 1);
	}
	
	public int getDyePotentialDailyCount()
	{
		return this.getVariables().getInt("DYE_POTENTIAL_DAILY_COUNT", 20);
	}
	
	public int getDyePotentialDailyEnchantReset()
	{
		return this.getVariables().getInt("DYE_POTENTIAL_DAILY_COUNT_ENCHANT_RESET", 0);
	}
	
	public void setDyePotentialDailyEnchantReset(int val)
	{
		this.getVariables().set("DYE_POTENTIAL_DAILY_COUNT_ENCHANT_RESET", val);
	}
	
	public Map<BaseStat, Integer> getHennaBaseStats()
	{
		return this._hennaBaseStats;
	}
	
	@Override
	public boolean hasBasicPropertyResist()
	{
		return false;
	}
	
	public void autoSave()
	{
		this.storeMe();
		this.storeRecommendations();
		if (AdenLaboratoryConfig.ADENLAB_ENABLED)
		{
			AdenLaboratoryManager.storeAdenLabBossData(this);
		}
		
		if (GeneralConfig.UPDATE_ITEMS_ON_CHAR_STORE)
		{
			this.getInventory().updateDatabase();
			this.getWarehouse().updateDatabase();
			this.getFreight().updateDatabase();
		}
	}
	
	public boolean canLogout()
	{
		if (this.hasItemRequest())
		{
			return false;
		}
		else if (this.isSubclassLocked())
		{
			LOGGER.warning("Player " + this._name + " tried to restart/logout during class change.");
			return false;
		}
		else
		{
			return !AttackStanceTaskManager.getInstance().hasAttackStanceTask(this) || this.isGM() && GeneralConfig.GM_RESTART_FIGHTING ? !this.isRegisteredOnEvent() : false;
		}
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		if (attacker == null)
		{
			return false;
		}
		else if (this.isInvisible() || this.isAffected(EffectFlag.UNTARGETABLE))
		{
			return false;
		}
		else if (attacker == this || attacker == this._pet || attacker.hasServitor(attacker.getObjectId()))
		{
			return false;
		}
		else if (attacker instanceof FriendlyMob)
		{
			return false;
		}
		else if (attacker.isMonster())
		{
			return true;
		}
		else
		{
			boolean isPlayableAttacker = attacker.isPlayable() || attacker instanceof Shadow || attacker instanceof Guardian;
			if (isPlayableAttacker && this._duelState == 1 && this.getDuelId() == attacker.asPlayer().getDuelId())
			{
				return true;
			}
			else if (this.isInParty() && this._party.getMembers().contains(attacker))
			{
				return false;
			}
			else if (attacker.isPlayer() && attacker.asPlayer().isInOlympiadMode())
			{
				return this._inOlympiadMode && this._olympiadStart && attacker.asPlayer().getOlympiadGameId() == this.getOlympiadGameId();
			}
			else if (this.isOnEvent())
			{
				return this.isOnSoloEvent() || this.getTeam() != attacker.getTeam();
			}
			else
			{
				if (isPlayableAttacker)
				{
					if (this.isInsideZone(ZoneId.PEACE) || this.isInsideZone(ZoneId.NO_PVP))
					{
						return false;
					}
					
					if (PlayerConfig.ALT_COMMAND_CHANNEL_FRIENDS && this.isInParty() && this.getParty().getCommandChannel() != null && attacker.isInParty() && attacker.getParty().getCommandChannel() != null && this.getParty().getCommandChannel() == attacker.getParty().getCommandChannel())
					{
						return false;
					}
					
					Player attackerPlayer = attacker.asPlayer();
					Clan clan = this.getClan();
					Clan attackerClan = attackerPlayer.getClan();
					if (clan != null && attackerClan != null)
					{
						if (clan != attackerClan)
						{
							Siege siege = SiegeManager.getInstance().getSiege(this.getX(), this.getY(), this.getZ());
							if (siege != null)
							{
								if (siege.checkIsDefender(attackerClan) && siege.checkIsDefender(clan))
								{
									return false;
								}
								
								if (siege.checkIsAttacker(attackerClan) && siege.checkIsAttacker(clan))
								{
									Castle castle = CastleManager.getInstance().getCastleById(this._siegeSide);
									return castle != null && castle.isFirstMidVictory();
								}
							}
						}
						
						if (this.getWantsPeace() == 0 && attackerPlayer.getWantsPeace() == 0 && !this.isAcademyMember())
						{
							ClanWar war = attackerClan.getWarWith(this.getClanId());
							if (war != null)
							{
								ClanWarState warState = war.getState();
								if (warState == ClanWarState.MUTUAL || (warState == ClanWarState.BLOOD_DECLARATION || warState == ClanWarState.DECLARATION) && war.getAttackerClanId() == clan.getId())
								{
									return true;
								}
							}
						}
					}
					
					if (attackerPlayer.isMercenary())
					{
						if ((clan != null && attackerPlayer.getClanIdMercenary() == clan.getId()) || (this.isMercenary() && attackerPlayer.getClanIdMercenary() == this.getClanIdMercenary()))
						{
							return false;
						}
					}
					else if (attackerClan != null && attackerClan.getId() == this.getClanIdMercenary())
					{
						return false;
					}
					
					if (this.isInsideZone(ZoneId.PVP) && attackerPlayer.isInsideZone(ZoneId.PVP) && (!this.isInsideZone(ZoneId.SIEGE) || !attackerPlayer.isInsideZone(ZoneId.SIEGE)))
					{
						return true;
					}
					
					if (clan != null && clan.isMember(attacker.getObjectId()))
					{
						return false;
					}
					
					if (attacker.isPlayer() && this.getAllyId() != 0 && this.getAllyId() == attackerPlayer.getAllyId())
					{
						return false;
					}
					
					if (this.isInsideZone(ZoneId.PVP) && attackerPlayer.isInsideZone(ZoneId.PVP) && this.isInsideZone(ZoneId.SIEGE) && attackerPlayer.isInsideZone(ZoneId.SIEGE))
					{
						return true;
					}
					
					if (FactionSystemConfig.FACTION_SYSTEM_ENABLED && (this.isGood() && attackerPlayer.isEvil() || this.isEvil() && attackerPlayer.isGood()))
					{
						return true;
					}
				}
				
				if (attacker instanceof Defender && this._clan != null)
				{
					Siege siege = SiegeManager.getInstance().getSiege(this);
					return siege != null && siege.checkIsAttacker(this._clan);
				}
				else if (!(attacker instanceof Guard))
				{
					return this.getReputation() < 0 || this._pvpFlag > 0;
				}
				else
				{
					return !FactionSystemConfig.FACTION_SYSTEM_ENABLED || !FactionSystemConfig.FACTION_GUARDS_ENABLED || (!this._isGood || !attacker.asNpc().getTemplate().isClan(FactionSystemConfig.FACTION_EVIL_TEAM_NAME)) && (!this._isEvil || !attacker.asNpc().getTemplate().isClan(FactionSystemConfig.FACTION_GOOD_TEAM_NAME)) ? this.getReputation() < 0 : true;
				}
			}
		}
	}
	
	@Override
	public boolean useMagic(Skill skill, Item item, boolean forceUse, boolean dontMove)
	{
		Skill usedSkill = skill;
		if (skill.isPassive())
		{
			this.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		else if (!PlayerConfig.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT && this.getReputation() < 0 && skill.hasEffectType(EffectType.TELEPORT))
		{
			this.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		else if (skill.isToggle() && this.isMounted())
		{
			this.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		else
		{
			Skill attachedSkill = skill.getAttachedSkill(this);
			if (attachedSkill != null)
			{
				usedSkill = attachedSkill;
			}
			
			int alternateEnemySkill = usedSkill.getAlternateEnemySkillId();
			if (alternateEnemySkill > 0)
			{
				int alternateAllySkillId = usedSkill.getAlternateAllySkillId();
				usedSkill = SkillData.getInstance().getSkill(alternateEnemySkill, usedSkill.getLevel(), usedSkill.getSubLevel());
				if (alternateAllySkillId > 0)
				{
					WorldObject target = this.getTarget();
					if (target != null && target.isPlayer())
					{
						Player targetPlayer = target.asPlayer();
						if (targetPlayer == this || this.getParty() != null && this.getParty() == targetPlayer.getParty() || this.getClan() != null && this.getClan() == targetPlayer.getClan() || this.getAllyId() != 0 && this.getAllyId() == targetPlayer.getAllyId())
						{
							usedSkill = SkillData.getInstance().getSkill(alternateAllySkillId, usedSkill.getLevel(), usedSkill.getSubLevel());
						}
					}
				}
			}
			
			if (usedSkill.canCastWhileDisabled() || !this.isControlBlocked() && !this.hasBlockActions())
			{
				if (this.isDead())
				{
					this.sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
				else if (this.isFishing() && !usedSkill.hasEffectType(EffectType.FISHING, EffectType.FISHING_START))
				{
					this.sendPacket(SystemMessageId.ONLY_FISHING_SKILLS_MAY_BE_USED_AT_THIS_TIME);
					return false;
				}
				else if (this._observerMode)
				{
					this.sendPacket(SystemMessageId.YOU_CANNOT_USE_THIS_FUNCTION_IN_THE_SPECTATOR_MODE);
					this.sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
				else if (this.isSkillDisabled(usedSkill))
				{
					SystemMessage sm;
					if (this.hasSkillReuse(usedSkill.getReuseHashCode()))
					{
						int remainingTime = (int) (this.getSkillRemainingReuseTime(usedSkill.getReuseHashCode()) / 1000L);
						int hours = remainingTime / 3600;
						int minutes = remainingTime % 3600 / 60;
						int seconds = remainingTime % 60;
						if (hours > 0)
						{
							sm = new SystemMessage(SystemMessageId.S1_WILL_BE_AVAILABLE_AGAIN_IN_S2_H_S3_MIN_S4_SEC);
							sm.addSkillName(usedSkill);
							sm.addInt(hours);
							sm.addInt(minutes);
						}
						else if (minutes > 0)
						{
							sm = new SystemMessage(SystemMessageId.S1_WILL_BE_AVAILABLE_AGAIN_IN_S2_MIN_S3_SEC);
							sm.addSkillName(usedSkill);
							sm.addInt(minutes);
						}
						else
						{
							sm = new SystemMessage(SystemMessageId.S1_WILL_BE_AVAILABLE_AGAIN_IN_S2_SEC);
							sm.addSkillName(usedSkill);
						}
						
						sm.addInt(seconds);
					}
					else
					{
						sm = new SystemMessage(SystemMessageId.S1_IS_NOT_AVAILABLE_AT_THIS_TIME_BEING_PREPARED_FOR_REUSE);
						sm.addSkillName(usedSkill);
					}
					
					this.sendPacket(sm);
					this.sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
				else if (this._waitTypeSitting)
				{
					this.sendPacket(SystemMessageId.YOU_CANNOT_USE_ACTIONS_AND_SKILLS_WHILE_THE_CHARACTER_IS_SITTING);
					this.sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
				else
				{
					if (usedSkill.isToggle())
					{
						if (this.isAffectedBySkill(usedSkill.getId()))
						{
							if (!usedSkill.isNecessaryToggle())
							{
								this.stopSkillEffects(SkillFinishType.REMOVED, usedSkill.getId());
							}
							
							this.sendPacket(ActionFailed.STATIC_PACKET);
							return false;
						}
						
						if (usedSkill.getToggleGroupId() > 0)
						{
							this.getEffectList().stopAllTogglesOfGroup(usedSkill.getToggleGroupId());
						}
					}
					
					if (this.isFakeDeath())
					{
						this.sendPacket(ActionFailed.STATIC_PACKET);
						return false;
					}
					WorldObject target = usedSkill.getTarget(this, forceUse, dontMove, true);
					Location worldPosition = this._currentSkillWorldPosition;
					if (usedSkill.getTargetType() == TargetType.GROUND && worldPosition == null)
					{
						if (usedSkill.getAffectScope() == AffectScope.FAN_PB)
						{
							if (this.isInsideZone(ZoneId.PEACE))
							{
								this.sendPacket(SystemMessageId.YOU_CANNOT_ATTACK_IN_A_PEACEFUL_ZONE);
							}
							else if (this.getCurrentMp() < usedSkill.getMpConsume())
							{
								this.sendPacket(SystemMessageId.NOT_ENOUGH_MP);
							}
							else if (usedSkill.checkCondition(this, target, true))
							{
								this.sendPacket(new MagicSkillUse(this, this, usedSkill.getDisplayId(), usedSkill.getDisplayLevel(), 0, 0, usedSkill.getReuseDelayGroup(), -1, SkillCastingType.NORMAL, true));
							}
						}
						
						this.sendPacket(ActionFailed.STATIC_PACKET);
						return false;
					}
					else if (target == null)
					{
						this.sendPacket(ActionFailed.STATIC_PACKET);
						return false;
					}
					else if (!usedSkill.checkCondition(this, target, true))
					{
						this.sendPacket(ActionFailed.STATIC_PACKET);
						if (usedSkill.getNextAction() != NextActionType.NONE && target != this && target.isAutoAttackable(this))
						{
							CreatureAI.IntentionCommand nextIntention = this.getAI().getNextIntention();
							if (nextIntention == null || nextIntention.getIntention() != Intention.MOVE_TO)
							{
								if (usedSkill.getNextAction() == NextActionType.ATTACK)
								{
									this.getAI().setIntention(Intention.ATTACK, target);
								}
								else if (usedSkill.getNextAction() == NextActionType.CAST)
								{
									this.getAI().setIntention(Intention.CAST, usedSkill, target, item, false, false);
								}
							}
						}
						
						return false;
					}
					else
					{
						boolean doubleCast = this.isAffected(EffectFlag.DOUBLE_CAST) && usedSkill.canDoubleCast();
						if ((doubleCast || !this.isAttackingNow() && !this.isCastingNow(SkillCaster::isAnyNormalType)) && (!this.isCastingNow(s -> s.getCastingType() == SkillCastingType.NORMAL) || !this.isCastingNow(s -> s.getCastingType() == SkillCastingType.NORMAL_SECOND)))
						{
							if (this._queuedSkill != null)
							{
								this.setQueuedSkill(null, null, false, false);
							}
							
							this.getAI().setIntention(Intention.CAST, usedSkill, target, item, forceUse, dontMove);
							return true;
						}
						if (item == null)
						{
							this.setQueuedSkill(usedSkill, item, forceUse, dontMove);
						}
						
						this.sendPacket(ActionFailed.STATIC_PACKET);
						return false;
					}
				}
			}
			this.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
	}
	
	public boolean isInLooterParty(int looterId)
	{
		Player looter = World.getInstance().getPlayer(looterId);
		if (this.isInParty() && this._party.isInCommandChannel() && looter != null)
		{
			return this._party.getCommandChannel().getMembers().contains(looter);
		}
		return this.isInParty() && looter != null ? this._party.getMembers().contains(looter) : false;
	}
	
	public boolean isMageClass()
	{
		return this.getPlayerClass().isMage();
	}
	
	public boolean isDeathKnight()
	{
		return this._isDeathKnight;
	}
	
	public void setDeathKnight(boolean value)
	{
		this._isDeathKnight = value;
	}
	
	public boolean isVanguard()
	{
		return this._isVanguard;
	}
	
	public void setVanguard(boolean value)
	{
		this._isVanguard = value;
	}
	
	public boolean isAssassin()
	{
		return this._isAssassin;
	}
	
	public void setAssassin(boolean value)
	{
		this._isAssassin = value;
	}
	
	public boolean isWarg()
	{
		return this._isWarg;
	}
	
	public void setWarg(boolean value)
	{
		this._isWarg = value;
	}
	
	public boolean isBloodRose()
	{
		return this._isBloodRose;
	}
	
	public void setBloodRose(boolean value)
	{
		this._isBloodRose = value;
	}
	
	public boolean isSamurai()
	{
		return this._isSamurai;
	}
	
	public void setSamurai(boolean value)
	{
		this._isSamurai = value;
	}
	
	public boolean isMounted()
	{
		return this._mountType != MountType.NONE;
	}
	
	public boolean checkLandingState()
	{
		return this.isInsideZone(ZoneId.NO_LANDING) ? true : this.isInsideZone(ZoneId.SIEGE) && (this.getClan() == null || CastleManager.getInstance().getCastle(this) != CastleManager.getInstance().getCastleByOwner(this.getClan()) || this != this.getClan().getLeader().getPlayer());
	}
	
	public void setMount(int npcId, int npcLevel)
	{
		MountType type = MountType.findByNpcId(npcId);
		switch (type)
		{
			case NONE:
				this.setFlying(false);
				break;
			case STRIDER:
				if (this.isNoble())
				{
					this.addSkill(CommonSkill.STRIDER_SIEGE_ASSAULT.getSkill(), false);
				}
				break;
			case WYVERN:
				this.setFlying(true);
		}
		
		this._mountType = type;
		this._mountNpcId = npcId;
		this._mountLevel = npcLevel;
	}
	
	public MountType getMountType()
	{
		return this._mountType;
	}
	
	@Override
	public void stopAllEffects()
	{
		super.stopAllEffects();
		this.updateAndBroadcastStatus();
	}
	
	@Override
	public void stopAllEffectsExceptThoseThatLastThroughDeath()
	{
		super.stopAllEffectsExceptThoseThatLastThroughDeath();
		this.updateAndBroadcastStatus();
	}
	
	public void stopCubics()
	{
		if (!this._cubics.isEmpty())
		{
			this._cubics.values().forEach(Cubic::deactivate);
			this._cubics.clear();
		}
	}
	
	public void stopCubicsByOthers()
	{
		if (!this._cubics.isEmpty())
		{
			boolean broadcast = false;
			
			for (Cubic cubic : this._cubics.values())
			{
				if (cubic.isGivenByOther())
				{
					cubic.deactivate();
					this._cubics.remove(cubic.getTemplate().getId());
					broadcast = true;
				}
			}
			
			if (broadcast)
			{
				this.sendPacket(new ExUserInfoCubic(this));
				this.broadcastUserInfo();
			}
		}
	}
	
	@Override
	public void updateAbnormalVisualEffects()
	{
		if (this._abnormalVisualEffectTask == null)
		{
			this._abnormalVisualEffectTask = ThreadPool.schedule(() -> {
				this.sendPacket(new ExUserInfoAbnormalVisualEffect(this));
				this.broadcastCharInfo();
				this._abnormalVisualEffectTask = null;
			}, 50L);
		}
	}
	
	public void setInventoryBlockingStatus(boolean value)
	{
		this._inventoryDisable = value;
		if (value)
		{
			ThreadPool.schedule(new InventoryEnableTask(this), 1500L);
		}
	}
	
	public boolean isInventoryDisabled()
	{
		return this._inventoryDisable;
	}
	
	public Cubic addCubic(Cubic cubic)
	{
		return this._cubics.put(cubic.getTemplate().getId(), cubic);
	}
	
	public Map<Integer, Cubic> getCubics()
	{
		return this._cubics;
	}
	
	public Cubic getCubicById(int cubicId)
	{
		return this._cubics.get(cubicId);
	}
	
	public int getEnchantEffect()
	{
		Item wpn = this.getActiveWeaponInstance();
		return wpn == null ? 0 : Math.min(127, wpn.getEnchantLevel());
	}
	
	public void setLastFolkNPC(Npc folkNpc)
	{
		this._lastFolkNpc = folkNpc;
	}
	
	public Npc getLastFolkNPC()
	{
		return this._lastFolkNpc;
	}
	
	public void addAutoSoulShot(int itemId)
	{
		this._activeSoulShots.add(itemId);
	}
	
	public boolean removeAutoSoulShot(int itemId)
	{
		return this._activeSoulShots.remove(itemId);
	}
	
	public Set<Integer> getAutoSoulShot()
	{
		return this._activeSoulShots;
	}
	
	@Override
	public void rechargeShots(boolean physical, boolean magic, boolean fish)
	{
		for (int itemId : this._activeSoulShots)
		{
			Item item = this._inventory.getItemByItemId(itemId);
			if (item == null)
			{
				this.removeAutoSoulShot(itemId);
			}
			else
			{
				IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
				if (handler != null)
				{
					ActionType defaultAction = item.getTemplate().getDefaultAction();
					if (magic && defaultAction == ActionType.SPIRITSHOT || physical && defaultAction == ActionType.SOULSHOT || fish && defaultAction == ActionType.FISHINGSHOT)
					{
						handler.onItemUse(this, item, false);
					}
				}
			}
		}
	}
	
	public void disableAutoShotByCrystalType(int crystalType)
	{
		for (int itemId : this._activeSoulShots)
		{
			if (ItemData.getInstance().getTemplate(itemId).getCrystalType().getLevel() == crystalType)
			{
				this.disableAutoShot(itemId);
			}
		}
	}
	
	public boolean disableAutoShot(int itemId)
	{
		if (this._activeSoulShots.contains(itemId))
		{
			this.removeAutoSoulShot(itemId);
			this.sendPacket(new ExAutoSoulShot(itemId, false, 0));
			SystemMessage sm = new SystemMessage(SystemMessageId.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_DEACTIVATED);
			sm.addItemName(itemId);
			this.sendPacket(sm);
			return true;
		}
		return false;
	}
	
	public void disableAutoShotsAll()
	{
		for (int itemId : this._activeSoulShots)
		{
			this.sendPacket(new ExAutoSoulShot(itemId, false, 0));
			SystemMessage sm = new SystemMessage(SystemMessageId.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_DEACTIVATED);
			sm.addItemName(itemId);
			this.sendPacket(sm);
		}
		
		this._activeSoulShots.clear();
	}
	
	public BroochJewel getActiveRubyJewel()
	{
		return this._activeRubyJewel;
	}
	
	public void setActiveRubyJewel(BroochJewel jewel)
	{
		this._activeRubyJewel = jewel;
	}
	
	public BroochJewel getActiveShappireJewel()
	{
		return this._activeShappireJewel;
	}
	
	public void setActiveShappireJewel(BroochJewel jewel)
	{
		this._activeShappireJewel = jewel;
	}
	
	public void updateActiveBroochJewel()
	{
		BroochJewel[] broochJewels = BroochJewel.values();
		this.setActiveRubyJewel(null);
		
		for (int i = broochJewels.length - 1; i > 0; i--)
		{
			BroochJewel jewel = broochJewels[i];
			if (jewel.isRuby() && this._inventory.isItemEquipped(jewel.getItemId()))
			{
				this.setActiveRubyJewel(jewel);
				break;
			}
		}
		
		this.setActiveShappireJewel(null);
		
		for (int ix = broochJewels.length - 1; ix > 0; ix--)
		{
			BroochJewel jewel = broochJewels[ix];
			if (jewel.isSapphire() && this._inventory.isItemEquipped(jewel.getItemId()))
			{
				this.setActiveShappireJewel(jewel);
				break;
			}
		}
	}
	
	public ClanPrivileges getClanPrivileges()
	{
		return this._clanPrivileges;
	}
	
	public void setClanPrivileges(ClanPrivileges clanPrivileges)
	{
		this._clanPrivileges = clanPrivileges.clone();
	}
	
	public boolean hasAccess(ClanAccess access)
	{
		return this._clanPrivileges.hasMinimumPrivileges(access);
	}
	
	public void setPledgeClass(int id)
	{
		this._pledgeClass = id;
		this.checkItemRestriction();
	}
	
	public int getPledgeClass()
	{
		return this._pledgeClass;
	}
	
	public void setPledgeType(int typeId)
	{
		this._pledgeType = typeId;
	}
	
	@Override
	public int getPledgeType()
	{
		return this._pledgeType;
	}
	
	public int getApprentice()
	{
		return this._apprentice;
	}
	
	public void setApprentice(int apprenticeId)
	{
		this._apprentice = apprenticeId;
	}
	
	public int getSponsor()
	{
		return this._sponsor;
	}
	
	public void setSponsor(int sponsorId)
	{
		this._sponsor = sponsorId;
	}
	
	public int getBookMarkSlot()
	{
		return this._bookmarkslot;
	}
	
	public void setBookMarkSlot(int slot)
	{
		this._bookmarkslot = slot;
		this.sendPacket(new ExGetBookMarkInfoPacket(this));
	}
	
	@Override
	public void sendMessage(String message)
	{
		this.sendPacket(new SystemMessage(SendMessageLocalisationData.getLocalisation(this, message)));
	}
	
	public void sendSysMessage(String message)
	{
		if (GeneralConfig.GM_STARTUP_BUILDER_HIDE)
		{
			this.sendPacket(new CreatureSay(null, ChatType.GENERAL, "SYS", SendMessageLocalisationData.getLocalisation(this, message)));
		}
		else
		{
			this.sendMessage(message);
		}
	}
	
	public boolean setHiding(boolean hide)
	{
		if (!this.isGM())
		{
			return false;
		}
		if (this.hasEnteredWorld())
		{
			if ((this.isInvisible() && hide) || (!this.isInvisible() && !hide))
			{
				return false;
			}
		}
		
		this.setSilenceMode(hide);
		this.setInvul(hide);
		this.setInvisible(hide);
		if (hide && this.hasEnteredWorld())
		{
			World.getInstance().forEachVisibleObject(this, Player.class, player -> {
				if (player.getTarget() == this)
				{
					player.setTarget(null);
				}
			});
		}
		
		this.broadcastUserInfo();
		this.sendPacket(new ExUserInfoAbnormalVisualEffect(this));
		return true;
	}
	
	public void setObserving(boolean value)
	{
		this._observerMode = value;
		this.setTarget(null);
		this.setBlockActions(value);
		this.setInvul(value);
		this.setInvisible(value);
		if (this.hasAI() && !value)
		{
			this.getAI().setIntention(Intention.IDLE);
		}
	}
	
	public void enterObserverMode(Location loc)
	{
		this.setLastLocation();
		this.getEffectList().stopEffects(AbnormalType.HIDE);
		this.setObserving(true);
		this.sendPacket(new ObservationMode(loc));
		this.teleToLocation(loc, false);
		this.broadcastUserInfo();
	}
	
	public void setLastLocation()
	{
		this._lastLoc = new Location(this.getX(), this.getY(), this.getZ());
	}
	
	public void unsetLastLocation()
	{
		this._lastLoc = null;
	}
	
	public void enterOlympiadObserverMode(Location loc, int id)
	{
		if (this._pet != null)
		{
			this._pet.unSummon(this);
		}
		
		if (this.hasServitors())
		{
			this.getServitors().values().forEach(s -> s.unSummon(this));
		}
		
		this.getEffectList().stopEffects(AbnormalType.HIDE);
		if (!this._cubics.isEmpty())
		{
			this._cubics.values().forEach(Cubic::deactivate);
			this._cubics.clear();
			this.sendPacket(new ExUserInfoCubic(this));
		}
		
		if (this._party != null)
		{
			this._party.removePartyMember(this, PartyMessageType.EXPELLED);
		}
		
		this._olympiadGameId = id;
		if (this._waitTypeSitting)
		{
			this.standUp();
		}
		
		if (!this._observerMode)
		{
			this.setLastLocation();
		}
		
		this._observerMode = true;
		this.setTarget(null);
		this.setInvul(true);
		this.setInvisible(true);
		this.setInstance(OlympiadGameManager.getInstance().getOlympiadTask(id).getStadium().getInstance());
		this.teleToLocation(loc, false);
		this.sendPacket(new ExOlympiadMode(OlympiadMode.SPECTATOR));
		this.broadcastUserInfo();
	}
	
	public void leaveObserverMode()
	{
		this.setTarget(null);
		this.setInstance(null);
		this.teleToLocation(this._lastLoc, false);
		this.unsetLastLocation();
		this.sendPacket(new ObservationReturn(this.getLocation()));
		this.setBlockActions(false);
		if (!this.isGM())
		{
			this.setInvisible(false);
			this.setInvul(false);
		}
		
		if (this.hasAI())
		{
			this.getAI().setIntention(Intention.IDLE);
		}
		
		this.setFalling();
		this._observerMode = false;
		this.broadcastUserInfo();
	}
	
	public void leaveOlympiadObserverMode()
	{
		if (this._olympiadGameId != -1)
		{
			this._olympiadGameId = -1;
			this._observerMode = false;
			this.setTarget(null);
			this.sendPacket(new ExOlympiadMode(OlympiadMode.NONE));
			this.setInstance(null);
			this.teleToLocation(this._lastLoc, true);
			if (!this.isGM())
			{
				this.setInvisible(false);
				this.setInvul(false);
			}
			
			if (this.hasAI())
			{
				this.getAI().setIntention(Intention.IDLE);
			}
			
			this.unsetLastLocation();
			this.broadcastUserInfo();
		}
	}
	
	public void setOlympiadSide(int i)
	{
		this._olympiadSide = i;
	}
	
	public int getOlympiadSide()
	{
		return this._olympiadSide;
	}
	
	public void setOlympiadGameId(int id)
	{
		this._olympiadGameId = id;
	}
	
	public int getOlympiadGameId()
	{
		return this._olympiadGameId;
	}
	
	public Location getLastLocation()
	{
		return this._lastLoc;
	}
	
	public boolean inObserverMode()
	{
		return this._observerMode;
	}
	
	public AdminTeleportType getTeleMode()
	{
		return this._teleportType;
	}
	
	public void setTeleMode(AdminTeleportType type)
	{
		this._teleportType = type;
	}
	
	public void setRaceTicket(int i, int value)
	{
		this._raceTickets[i] = value;
	}
	
	public int getRaceTicket(int i)
	{
		return this._raceTickets[i];
	}
	
	public boolean getMessageRefusal()
	{
		return this._messageRefusal;
	}
	
	public void setMessageRefusal(boolean mode)
	{
		this._messageRefusal = mode;
		this.sendPacket(new EtcStatusUpdate(this));
	}
	
	public void setDietMode(boolean mode)
	{
		this._dietMode = mode;
	}
	
	public boolean getDietMode()
	{
		return this._dietMode;
	}
	
	public void setTradeRefusal(boolean mode)
	{
		this._tradeRefusal = mode;
	}
	
	public boolean getTradeRefusal()
	{
		return this._tradeRefusal;
	}
	
	public void setExchangeRefusal(boolean mode)
	{
		this._exchangeRefusal = mode;
	}
	
	public boolean getExchangeRefusal()
	{
		return this._exchangeRefusal;
	}
	
	public BlockList getBlockList()
	{
		return this._blockList;
	}
	
	public boolean isBlocking(Player player)
	{
		return this._blockList.isBlockAll() || this._blockList.isInBlockList(player);
	}
	
	public boolean isNotBlocking(Player player)
	{
		return !this._blockList.isBlockAll() && !this._blockList.isInBlockList(player);
	}
	
	public boolean isBlocked(Player player)
	{
		return player.getBlockList().isBlockAll() || player.getBlockList().isInBlockList(this);
	}
	
	public boolean isNotBlocked(Player player)
	{
		return !player.getBlockList().isBlockAll() && !player.getBlockList().isInBlockList(this);
	}
	
	public void setHero(boolean hero)
	{
		if (hero && this._baseClass == this._activeClass)
		{
			for (Skill skill : SkillTreeData.getInstance().getHeroSkillTree())
			{
				this.addSkill(skill, false);
			}
		}
		else
		{
			for (Skill skill : SkillTreeData.getInstance().getHeroSkillTree())
			{
				this.removeSkill(skill, false, true);
			}
		}
		
		this._hero = hero;
		this.sendSkillList();
	}
	
	public void setInOlympiadMode(boolean value)
	{
		this._inOlympiadMode = value;
	}
	
	public void setOlympiadStart(boolean value)
	{
		this._olympiadStart = value;
	}
	
	public boolean isOlympiadStart()
	{
		return this._olympiadStart;
	}
	
	public boolean isHero()
	{
		return this._hero;
	}
	
	public boolean isInOlympiadMode()
	{
		return this._inOlympiadMode;
	}
	
	public boolean isInDuel()
	{
		return this._isInDuel;
	}
	
	public void setStartingDuel()
	{
		this._startingDuel = true;
	}
	
	public int getDuelId()
	{
		return this._duelId;
	}
	
	public void setDuelState(int mode)
	{
		this._duelState = mode;
	}
	
	public int getDuelState()
	{
		return this._duelState;
	}
	
	public void setInDuel(int duelId)
	{
		if (duelId > 0)
		{
			this._isInDuel = true;
			this._duelState = 1;
			this._duelId = duelId;
		}
		else
		{
			if (this._duelState == 2)
			{
				this.enableAllSkills();
				this.getStatus().startHpMpRegeneration();
			}
			
			this._isInDuel = false;
			this._duelState = 0;
			this._duelId = 0;
		}
		
		this._startingDuel = false;
	}
	
	public SystemMessage getNoDuelReason()
	{
		SystemMessage sm = new SystemMessage(this._noDuelReason);
		sm.addPcName(this);
		this._noDuelReason = SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL;
		return sm;
	}
	
	public boolean canDuel()
	{
		if (!this.isInCombat() && !this.isJailed())
		{
			if (this.isDead() || this.isAlikeDead() || this.getCurrentHp() < this.getMaxHp() / 2L || this.getCurrentMp() < this.getMaxMp() / 2)
			{
				this._noDuelReason = SystemMessageId.C1_CANNOT_DUEL_AS_THEIR_HP_MP_50;
				return false;
			}
			else if (this._isInDuel || this._startingDuel)
			{
				this._noDuelReason = SystemMessageId.C1_IS_ALREADY_IN_A_DUEL;
				return false;
			}
			else if (this._inOlympiadMode)
			{
				this._noDuelReason = SystemMessageId.C1_IS_PARTICIPATING_IN_THE_OLYMPIAD_OR_THE_CEREMONY_OF_CHAOS_AND_THEREFORE_CANNOT_DUEL;
				return false;
			}
			else if (this.isOnEvent())
			{
				this._noDuelReason = SystemMessageId.C1_CANNOT_DUEL_AS_THEY_ARE_CURRENTLY_IN_BATTLE;
				return false;
			}
			else if (this.isCursedWeaponEquipped())
			{
				this._noDuelReason = SystemMessageId.C1_IS_IN_A_CHAOTIC_OR_PURPLE_STATE_AND_CANNOT_PARTICIPATE_IN_A_DUEL;
				return false;
			}
			else if (this._privateStoreType != PrivateStoreType.NONE)
			{
				this._noDuelReason = SystemMessageId.C1_CANNOT_DUEL_AS_THEY_ARE_CURRENTLY_IN_A_PRIVATE_STORE_OR_MANUFACTURE;
				return false;
			}
			else if (this.isMounted() || this.isInBoat())
			{
				this._noDuelReason = SystemMessageId.C1_IS_RIDING_A_BOAT_FENRIR_OR_STRIDER_AND_THEREFORE_CANNOT_DUEL;
				return false;
			}
			else if (this.isFishing())
			{
				this._noDuelReason = SystemMessageId.C1_CANNOT_DUEL_AS_THEY_ARE_CURRENTLY_FISHING;
				return false;
			}
			else if (!this.isInsideZone(ZoneId.PVP) && !this.isInsideZone(ZoneId.PEACE) && !this.isInsideZone(ZoneId.SIEGE) && !this.isInsideZone(ZoneId.NO_PVP))
			{
				return true;
			}
			else
			{
				this._noDuelReason = SystemMessageId.C1_IS_IN_AN_AREA_WHERE_DUEL_IS_NOT_ALLOWED_AND_YOU_CANNOT_APPLY_FOR_A_DUEL;
				return false;
			}
		}
		this._noDuelReason = SystemMessageId.C1_CANNOT_DUEL_AS_THEY_ARE_CURRENTLY_IN_BATTLE;
		return false;
	}
	
	public boolean isNoble()
	{
		return this._noble;
	}
	
	public void setNoble(boolean value)
	{
		if (value)
		{
			SkillTreeData.getInstance().getNobleSkillAutoGetTree().forEach(skill -> this.addSkill(skill, false));
		}
		else
		{
			SkillTreeData.getInstance().getNobleSkillTree().forEach(skill -> this.removeSkill(skill, false, true));
		}
		
		this._noble = value;
		this.sendSkillList();
	}
	
	public void setLvlJoinedAcademy(int lvl)
	{
		this._lvlJoinedAcademy = lvl;
	}
	
	public int getLvlJoinedAcademy()
	{
		return this._lvlJoinedAcademy;
	}
	
	@Override
	public boolean isAcademyMember()
	{
		return this._lvlJoinedAcademy > 0;
	}
	
	@Override
	public void setTeam(Team team)
	{
		super.setTeam(team);
		this.broadcastUserInfo();
		if (GeneralConfig.BLUE_TEAM_ABNORMAL_EFFECT != null || GeneralConfig.RED_TEAM_ABNORMAL_EFFECT != null)
		{
			this.sendPacket(new ExUserInfoAbnormalVisualEffect(this));
		}
		
		if (this._pet != null)
		{
			this._pet.broadcastStatusUpdate();
		}
		
		if (this.hasServitors())
		{
			this.getServitors().values().forEach(Creature::broadcastStatusUpdate);
		}
	}
	
	public void setWantsPeace(int wantsPeace)
	{
		this._wantsPeace = wantsPeace;
	}
	
	public int getWantsPeace()
	{
		return this._wantsPeace;
	}
	
	public void sendSkillList()
	{
		this.sendSkillList(0);
	}
	
	public void sendSkillList(int lastLearnedSkillId)
	{
		if (this._skillListTask == null)
		{
			this._skillListTask = ThreadPool.schedule(() -> {
				boolean isDisabled = false;
				SkillList skillList = new SkillList();
				
				for (Skill skill : this.getSkillList())
				{
					if (this._clan != null)
					{
						isDisabled = skill.isClanSkill() && this._clan.getReputationScore() < 0;
					}
					
					int originalSkillId = this.getOriginalSkill(skill.getId());
					if (originalSkillId != skill.getDisplayId())
					{
						Skill originalSkill = SkillData.getInstance().getSkill(originalSkillId, skill.getLevel(), skill.getSubLevel());
						skillList.addSkill(originalSkill.getDisplayId(), originalSkill.getReuseDelayGroup(), originalSkill.getDisplayLevel(), originalSkill.getSubLevel(), originalSkill.isPassive(), isDisabled, originalSkill.isEnchantable());
					}
					else
					{
						skillList.addSkill(skill.getDisplayId(), skill.getReuseDelayGroup(), skill.getDisplayLevel(), skill.getSubLevel(), skill.isPassive(), isDisabled, skill.isEnchantable());
					}
				}
				
				if (lastLearnedSkillId > 0)
				{
					skillList.setLastLearnedSkillId(lastLearnedSkillId);
				}
				
				this.sendPacket(skillList);
				this.sendPacket(new AcquireSkillList(this));
				this.restoreAutoShortcutVisual();
				this._skillListTask = null;
			}, 300L);
		}
	}
	
	public void sendStorageMaxCount()
	{
		if (this._storageCountTask == null)
		{
			this._storageCountTask = ThreadPool.schedule(() -> {
				this.sendPacket(new ExStorageMaxCount(this));
				this._storageCountTask = null;
			}, 300L);
		}
	}
	
	public void sendUserBoostStat()
	{
		if (this._userBoostStatTask == null)
		{
			this._userBoostStatTask = ThreadPool.schedule(() -> {
				this.sendPacket(new ExUserBoostStat(this, BonusExpType.VITALITY));
				this.sendPacket(new ExUserBoostStat(this, BonusExpType.BUFFS));
				this.sendPacket(new ExUserBoostStat(this, BonusExpType.PASSIVE));
				if (PlayerConfig.ENABLE_VITALITY)
				{
					this.sendPacket(new ExVitalityEffectInfo(this));
				}
				
				this._userBoostStatTask = null;
			}, 300L);
		}
	}
	
	public boolean addSubClass(int classId, int classIndex, boolean isDualClass)
	{
		if (this._subclassLock)
		{
			return false;
		}
		this._subclassLock = true;
		
		try
		{
			if (this.getTotalSubClasses() == PlayerConfig.MAX_SUBCLASS || classIndex == 0)
			{
				return false;
			}
			else if (this.getSubClasses().containsKey(classIndex))
			{
				return false;
			}
			else
			{
				SubClassHolder newClass = new SubClassHolder();
				newClass.setPlayerClass(classId);
				newClass.setClassIndex(classIndex);
				newClass.setVitalityPoints(3500000);
				if (isDualClass)
				{
					newClass.setDualClassActive(true);
					newClass.setExp(ExperienceData.getInstance().getExpForLevel(PlayerConfig.BASE_DUALCLASS_LEVEL));
					newClass.setLevel(PlayerConfig.BASE_DUALCLASS_LEVEL);
				}
				
				try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("INSERT INTO character_subclasses (charId,class_id,exp,sp,level,vitality_points,class_index,dual_class) VALUES (?,?,?,?,?,?,?,?)");)
				{
					statement.setInt(1, this.getObjectId());
					statement.setInt(2, newClass.getId());
					statement.setLong(3, newClass.getExp());
					statement.setLong(4, newClass.getSp());
					statement.setInt(5, newClass.getLevel());
					statement.setInt(6, newClass.getVitalityPoints());
					statement.setInt(7, newClass.getClassIndex());
					statement.setBoolean(8, newClass.isDualClass());
					statement.execute();
				}
				catch (Exception var23)
				{
					LOGGER.log(Level.WARNING, "WARNING: Could not add character sub class for " + this._name + ": " + var23.getMessage(), var23);
					return false;
				}
				
				this.getSubClasses().put(newClass.getClassIndex(), newClass);
				PlayerClass subTemplate = PlayerClass.getPlayerClass(classId);
				Map<Long, SkillLearn> skillTree = SkillTreeData.getInstance().getCompleteClassSkillTree(subTemplate);
				Map<Integer, Skill> prevSkillList = new HashMap<>();
				
				for (SkillLearn skillInfo : skillTree.values())
				{
					if ((skillInfo.getSkillId() != CommonSkill.DIVINE_INSPIRATION.getId() || PlayerConfig.AUTO_LEARN_DIVINE_INSPIRATION) && skillInfo.getGetLevel() <= newClass.getLevel())
					{
						Skill prevSkill = prevSkillList.get(skillInfo.getSkillId());
						Skill newSkill = SkillData.getInstance().getSkill(skillInfo.getSkillId(), skillInfo.getSkillLevel());
						if ((prevSkill == null || prevSkill.getLevel() <= newSkill.getLevel()) && !SkillTreeData.getInstance().isRemoveSkill(subTemplate, skillInfo.getSkillId()))
						{
							prevSkillList.put(newSkill.getId(), newSkill);
							this.storeSkill(newSkill, prevSkill, classIndex);
						}
					}
				}
				
				return true;
			}
		}
		finally
		{
			this._subclassLock = false;
			this.getStat().recalculateStats(false);
			this.updateAbnormalVisualEffects();
			this.sendSkillList();
		}
	}
	
	public boolean modifySubClass(int classIndex, int newClassId, boolean isDualClass)
	{
		SubClassHolder subClass = this.getSubClasses().get(classIndex);
		if (subClass == null)
		{
			return false;
		}
		if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_PROFESSION_CANCEL, this))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerProfessionCancel(this, subClass.getId()), this);
		}
		
		if (subClass.isDualClass())
		{
			this.getVariables().remove("ABILITY_POINTS_DUAL_CLASS");
			this.getVariables().remove("ABILITY_POINTS_DUAL_CLASS_USED");
			int revelationSkill = this.getVariables().getInt("DualclassRevelationSkill1", 0);
			if (revelationSkill != 0)
			{
				this.removeSkill(revelationSkill);
			}
			
			revelationSkill = this.getVariables().getInt("DualclassRevelationSkill2", 0);
			if (revelationSkill != 0)
			{
				this.removeSkill(revelationSkill);
			}
		}
		
		this.getSubClasses().remove(classIndex);
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement deleteHennas = con.prepareStatement("DELETE FROM character_hennas WHERE charId=? AND slot=? AND class_index=?");
			PreparedStatement deleteShortcuts = con.prepareStatement("DELETE FROM character_shortcuts WHERE charId=? AND class_index=?");
			PreparedStatement deleteSkillReuse = con.prepareStatement("DELETE FROM character_skills_save WHERE charId=? AND class_index=?");
			PreparedStatement deleteSkills = con.prepareStatement("DELETE FROM character_skills WHERE charId=? AND class_index=?");
			PreparedStatement deleteSubclass = con.prepareStatement("DELETE FROM character_subclasses WHERE charId=? AND class_index=?");)
		{
			deleteHennas.setInt(1, this.getObjectId());
			deleteHennas.setInt(2, classIndex);
			deleteHennas.execute();
			deleteShortcuts.setInt(1, this.getObjectId());
			deleteShortcuts.setInt(2, classIndex);
			deleteShortcuts.execute();
			deleteSkillReuse.setInt(1, this.getObjectId());
			deleteSkillReuse.setInt(2, classIndex);
			deleteSkillReuse.execute();
			deleteSkills.setInt(1, this.getObjectId());
			deleteSkills.setInt(2, classIndex);
			deleteSkills.execute();
			deleteSubclass.setInt(1, this.getObjectId());
			deleteSubclass.setInt(2, classIndex);
			deleteSubclass.execute();
		}
		catch (Exception var25)
		{
			LOGGER.log(Level.WARNING, "Could not modify sub class for " + this._name + " to class index " + classIndex + ": " + var25.getMessage(), var25);
			return false;
		}
		
		return this.addSubClass(newClassId, classIndex, isDualClass);
	}
	
	public boolean isSubClassActive()
	{
		return this._classIndex > 0;
	}
	
	public void setDualClassActive(int classIndex)
	{
		if (this.isSubClassActive())
		{
			this.getSubClasses().get(this._classIndex).setDualClassActive(true);
		}
	}
	
	public boolean isDualClassActive()
	{
		if (!this.isSubClassActive())
		{
			return false;
		}
		else if (this._subClasses.isEmpty())
		{
			return false;
		}
		else
		{
			SubClassHolder subClass = this._subClasses.get(this._classIndex);
			return subClass == null ? false : subClass.isDualClass();
		}
	}
	
	public boolean hasDualClass()
	{
		for (SubClassHolder subClass : this._subClasses.values())
		{
			if (subClass.isDualClass())
			{
				return true;
			}
		}
		
		return false;
	}
	
	public SubClassHolder getDualClass()
	{
		for (SubClassHolder subClass : this._subClasses.values())
		{
			if (subClass.isDualClass())
			{
				return subClass;
			}
		}
		
		return null;
	}
	
	public Map<Integer, SubClassHolder> getSubClasses()
	{
		return this._subClasses;
	}
	
	public int getTotalSubClasses()
	{
		return this.getSubClasses().size();
	}
	
	public int getBaseClass()
	{
		return this._baseClass;
	}
	
	public int getActiveClass()
	{
		return this._activeClass;
	}
	
	public int getClassIndex()
	{
		return this._classIndex;
	}
	
	protected void setClassIndex(int classIndex)
	{
		this._classIndex = classIndex;
	}
	
	private void setClassTemplate(int classId)
	{
		this._activeClass = classId;
		PlayerTemplate pcTemplate = PlayerTemplateData.getInstance().getTemplate(classId);
		if (pcTemplate == null)
		{
			LOGGER.severe("Missing template for classId: " + classId);
			throw new Error();
		}
		this.setTemplate(pcTemplate);
		if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_PROFESSION_CHANGE, this))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerProfessionChange(this, pcTemplate, this.isSubClassActive()), this);
		}
	}
	
	public void setActiveClass(int classIndex)
	{
		if (!this._subclassLock)
		{
			this._subclassLock = true;
			
			try
			{
				if (!this.isTransformed())
				{
					for (Item item : this._inventory.getPaperdollItems(Item::isAugmented))
					{
						if (item != null && item.isEquipped())
						{
							item.getAugmentation().removeBonus(this);
						}
					}
					
					this.abortCast();
					if (this.isChannelized())
					{
						this.getSkillChannelized().abortChannelization();
					}
					
					this.store(PlayerConfig.SUBCLASS_STORE_SKILL_COOLTIME);
					if (this._sellingBuffs != null)
					{
						this._sellingBuffs.clear();
					}
					
					this.resetTimeStamps();
					this._charges.set(0);
					this.stopChargeTask();
					if (this.hasServitors())
					{
						this.getServitors().values().forEach(s -> s.unSummon(this));
					}
					
					if (classIndex == 0)
					{
						this.setClassTemplate(this._baseClass);
					}
					else
					{
						try
						{
							this.setClassTemplate(this.getSubClasses().get(classIndex).getId());
						}
						catch (Exception var7)
						{
							LOGGER.log(Level.WARNING, "Could not switch " + this._name + "'s sub class to class index " + classIndex + ": " + var7.getMessage(), var7);
							return;
						}
					}
					
					this._classIndex = classIndex;
					if (this.isInParty())
					{
						this._party.recalculatePartyLevel();
					}
					
					this._autoUseSettings.getAutoSkills().clear();
					this._autoUseSettings.getAutoBuffs().clear();
					
					for (Skill oldSkill : this.getAllSkills())
					{
						this.removeSkill(oldSkill, false, true);
					}
					
					this.getEffectList().stopEffects(info -> !info.getSkill().isStayAfterDeath(), true, false);
					this.getEffectList().stopEffects(info -> !info.getSkill().isNecessaryToggle() && !info.getSkill().isIrreplaceableBuff(), true, false);
					this.getEffectList().stopAllToggles();
					this.sendPacket(new ExUserInfoAbnormalVisualEffect(this));
					this.stopCubics();
					this.restoreRecipeBook(false);
					this.restoreSkills();
					this.rewardSkills();
					this.regiveTemporarySkills();
					this.getInventory().applyItemSkills();
					this.restoreRelicCollectionBonuses();
					this.restoreCollectionBonuses();
					this.resetDisabledSkills();
					this.restoreEffects();
					this.sendPacket(new EtcStatusUpdate(this));
					this.restoreHenna();
					this.sendPacket(new HennaInfo(this));
					if (this.getCurrentHp() > this.getMaxHp())
					{
						this.setCurrentHp(this.getMaxHp());
					}
					
					if (this.getCurrentMp() > this.getMaxMp())
					{
						this.setCurrentMp(this.getMaxMp());
					}
					
					if (this.getCurrentCp() > this.getMaxCp())
					{
						this.setCurrentCp(this.getMaxCp());
					}
					
					this.refreshOverloaded(true);
					this.broadcastUserInfo();
					this.setExpBeforeDeath(0L);
					this._shortcuts.restoreMe();
					this.sendPacket(new ShortcutInit(this));
					this.broadcastPacket(new SocialAction(this.getObjectId(), 2122));
					this.sendPacket(new SkillCoolTime(this));
					this.sendStorageMaxCount();
					if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_SUB_CHANGE, this))
					{
						EventDispatcher.getInstance().notifyEventAsync(new OnPlayerSubChange(this), this);
					}
				}
			}
			finally
			{
				this._subclassLock = false;
				this.getStat().recalculateStats(false);
				this.updateAbnormalVisualEffects();
				this.sendSkillList();
			}
		}
	}
	
	public boolean isSubclassLocked()
	{
		return this._subclassLock;
	}
	
	public void stopWarnUserTakeBreak()
	{
		if (this._taskWarnUserTakeBreak != null)
		{
			this._taskWarnUserTakeBreak.cancel(true);
			this._taskWarnUserTakeBreak = null;
		}
	}
	
	public void startWarnUserTakeBreak()
	{
		if (this._taskWarnUserTakeBreak == null)
		{
			this._taskWarnUserTakeBreak = ThreadPool.scheduleAtFixedRate(new WarnUserTakeBreakTask(this), 3600000L, 3600000L);
		}
	}
	
	public void stopRentPet()
	{
		if (this._taskRentPet != null)
		{
			if (this.checkLandingState() && this._mountType == MountType.WYVERN)
			{
				this.teleToLocation(TeleportWhereType.TOWN);
			}
			
			if (this.dismount())
			{
				this._taskRentPet.cancel(true);
				this._taskRentPet = null;
			}
		}
	}
	
	public void startRentPet(int seconds)
	{
		if (this._taskRentPet == null)
		{
			this._taskRentPet = ThreadPool.scheduleAtFixedRate(new RentPetTask(this), seconds * 1000, seconds * 1000);
		}
	}
	
	public boolean isRentedPet()
	{
		return this._taskRentPet != null;
	}
	
	public void stopWaterTask()
	{
		if (this._taskWater != null)
		{
			this._taskWater.cancel(false);
			this._taskWater = null;
			this.sendPacket(new SetupGauge(this.getObjectId(), 2, 0));
		}
	}
	
	public void startWaterTask()
	{
		if (!this.isDead() && this._taskWater == null)
		{
			double breathPercentage = this.getStat().getValue(Stat.BREATH, this.getBaseTemplate().getBaseBreath()) / 100.0;
			int timeInWater = (int) (60000.0 * breathPercentage);
			this.sendPacket(new SetupGauge(this.getObjectId(), 2, timeInWater));
			this._taskWater = ThreadPool.scheduleAtFixedRate(new WaterTask(this), timeInWater, 1000L);
		}
	}
	
	public boolean isInWater()
	{
		return this._taskWater != null;
	}
	
	public void checkWaterState()
	{
		if (this.isInsideZone(ZoneId.WATER))
		{
			this.startWaterTask();
		}
		else
		{
			this.stopWaterTask();
		}
	}
	
	public void onPlayerEnter()
	{
		this.startWarnUserTakeBreak();
		if (this.isGM() && !GeneralConfig.GM_STARTUP_BUILDER_HIDE)
		{
			if (this.isInvul())
			{
				this.sendMessage("Entering world in Invulnerable mode.");
			}
			
			if (this.isInvisible())
			{
				this.sendMessage("Entering world in Invisible mode.");
			}
			
			if (this._silenceMode)
			{
				this.sendMessage("Entering world in Silence mode.");
			}
		}
		
		this._inventory.applyItemSkills();
		if (PlayerConfig.STORE_SKILL_COOLTIME)
		{
			this.restoreEffects();
		}
		
		if (this._pkKills > FeatureConfig.PK_PENALTY_LIST_MINIMUM_COUNT && this.getReputation() < 0)
		{
			World.getInstance().addPkPlayer(this);
		}
		
		this.revalidateZone(true);
		this.notifyFriends(1);
		if (!this.isGM() && PlayerConfig.DECREASE_SKILL_LEVEL)
		{
			this.checkPlayerSkills();
		}
		
		try
		{
			SayuneRequest sayune = this.getRequest(SayuneRequest.class);
			if (sayune != null)
			{
				sayune.onLogout();
			}
		}
		catch (Exception var3)
		{
			LOGGER.log(Level.SEVERE, "deleteMe()", var3);
		}
		
		try
		{
			for (ZoneType zone : ZoneManager.getInstance().getZones(this))
			{
				zone.onPlayerLoginInside(this);
			}
		}
		catch (Exception var4)
		{
			LOGGER.log(Level.SEVERE, "", var4);
		}
		
		if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_LOGIN, this))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerLogin(this), this);
		}
		
		if (this.isMentee())
		{
			if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_MENTEE_STATUS, this))
			{
				EventDispatcher.getInstance().notifyEventAsync(new OnPlayerMenteeStatus(this, true), this);
			}
		}
		else if (this.isMentor() && EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_MENTOR_STATUS, this))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerMentorStatus(this, true), this);
		}
		
		if (!this.isDead())
		{
			ThreadPool.schedule(() -> {
				this.setCurrentHp(this._originalHp);
				this.setCurrentMp(this._originalMp);
				this.setCurrentCp(this._originalCp);
			}, 300L);
		}
	}
	
	@Override
	public void fullRestore()
	{
		ThreadPool.schedule(() -> {
			this.setCurrentHp(this.getMaxHp());
			this.setCurrentMp(this.getMaxMp());
			this.setCurrentCp(this.getMaxCp());
		}, 100L);
	}
	
	public long getLastAccess()
	{
		return this._lastAccess;
	}
	
	protected void setLastAccess(long lastAccess)
	{
		this._lastAccess = lastAccess;
	}
	
	@Override
	public void doRevive()
	{
		super.doRevive();
		DecayTaskManager.getInstance().cancel(this);
		this.applyKarmaPenalty();
		this.sendPacket(new EtcStatusUpdate(this));
		this._revivePet = false;
		this._reviveRequested = 0;
		this._revivePower = 0.0;
		if (this.isInsideZone(ZoneId.PEACE) && this.hasSummon())
		{
			Pet pet = this.getPet();
			if (pet != null)
			{
				pet.teleToLocation(this, true);
			}
			
			for (Summon summon : this.getServitors().values())
			{
				if (!summon.isInsideZone(ZoneId.SIEGE))
				{
					summon.teleToLocation(this, true);
				}
			}
		}
		
		if (this.isMounted())
		{
			this.startFeed(this._mountNpcId);
		}
		
		Instance instance = this.getInstanceWorld();
		if (instance != null)
		{
			instance.doRevive(this);
		}
		
		this.getEffectList().updateEffectList();
		this.clearDamageTaken();
	}
	
	@Override
	public void doRevive(double revivePower)
	{
		this.doRevive();
		this.restoreExp(revivePower);
	}
	
	public void reviveRequest(Player reviver, boolean isPet, int power, int reviveHp, int reviveMp, int reviveCp)
	{
		if (!this.isResurrectionBlocked())
		{
			if (this._reviveRequested == 1)
			{
				if (this._revivePet == isPet)
				{
					reviver.sendPacket(SystemMessageId.RESURRECTION_HAS_ALREADY_BEEN_PROPOSED);
				}
				else if (isPet)
				{
					reviver.sendPacket(SystemMessageId.YOU_CANNOT_RESURRECT_A_GUARDIAN_WHILE_ITS_MASTER_IS_BEING_RESURRECTED);
				}
				else
				{
					reviver.sendPacket(SystemMessageId.THE_GUARDIAN_CANNOT_HELP_ITS_MASTER_WHILE_BEING_RESURRECTED);
				}
			}
			else
			{
				if (isPet && this._pet != null && this._pet.isDead() || !isPet && this.isDead())
				{
					this._reviveRequested = 1;
					this._revivePower = Formulas.calculateSkillResurrectRestorePercent(power, reviver);
					this._reviveHpPercent = reviveHp;
					this._reviveMpPercent = reviveMp;
					this._reviveCpPercent = reviveCp;
					this._revivePet = isPet;
					if (this.hasCharmOfCourage())
					{
						ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.YOUR_CHARM_OF_COURAGE_IS_TRYING_TO_RESURRECT_YOU_WOULD_YOU_LIKE_TO_RESURRECT_NOW.getId());
						dlg.addTime(60000);
						this.sendPacket(dlg);
						return;
					}
					
					long restoreExp = Math.round((this._expBeforeDeath - this.getExp()) * this._revivePower / 100.0);
					ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.C1_IS_ATTEMPTING_TO_DO_A_RESURRECTION_THAT_RESTORES_S2_S3_XP_ACCEPT.getId());
					dlg.getSystemMessage().addPcName(reviver);
					dlg.getSystemMessage().addLong(restoreExp);
					dlg.getSystemMessage().addInt(power);
					this.sendPacket(dlg);
				}
			}
		}
	}
	
	public void reviveAnswer(int answer)
	{
		if (this._reviveRequested == 1 && (this.isDead() || this._revivePet) && (!this._revivePet || this._pet == null || this._pet.isDead()))
		{
			if (answer == 1)
			{
				if (!this._revivePet)
				{
					if (this._revivePower != 0.0)
					{
						this.doRevive(this._revivePower);
					}
					else
					{
						this.doRevive();
					}
				}
				else if (this._pet != null)
				{
					if (this._revivePower != 0.0)
					{
						this._pet.doRevive(this._revivePower);
					}
					else
					{
						this._pet.doRevive();
					}
				}
			}
			
			this._reviveRequested = 0;
			this._revivePower = 0.0;
			Creature effected = this._revivePet ? this._pet : this;
			if (effected == null)
			{
				this._reviveHpPercent = 0;
				this._reviveMpPercent = 0;
				this._reviveCpPercent = 0;
			}
			else
			{
				if (this._reviveHpPercent > 0)
				{
					double amount = effected.getMaxHp() * this._reviveHpPercent / 100L;
					if (amount > 0.0)
					{
						effected.setCurrentHp(amount, true);
					}
					
					this._reviveHpPercent = 0;
				}
				
				if (this._reviveMpPercent > 0)
				{
					double amount = effected.getMaxMp() * this._reviveMpPercent / 100;
					if (amount > 0.0)
					{
						effected.setCurrentMp(amount, true);
					}
					
					this._reviveMpPercent = 0;
				}
				
				if (this._reviveCpPercent > 0)
				{
					double amount = effected.getMaxCp() * this._reviveCpPercent / 100;
					if (amount > 0.0)
					{
						effected.setCurrentCp(amount, true);
					}
					
					this._reviveCpPercent = 0;
				}
			}
		}
	}
	
	public boolean isReviveRequested()
	{
		return this._reviveRequested == 1;
	}
	
	public boolean isRevivingPet()
	{
		return this._revivePet;
	}
	
	public void removeReviving()
	{
		this._reviveRequested = 0;
		this._revivePower = 0.0;
	}
	
	public void onActionRequest()
	{
		if (this.isSpawnProtected())
		{
			this.setSpawnProtection(false);
			if (!this.isInsideZone(ZoneId.PEACE))
			{
				this.sendPacket(SystemMessageId.YOU_ARE_NO_LONGER_PROTECTED_FROM_AGGRESSIVE_MONSTERS);
			}
			
			if (PlayerConfig.RESTORE_SERVITOR_ON_RECONNECT && !this.hasSummon() && CharSummonTable.getInstance().getServitors().containsKey(this.getObjectId()))
			{
				CharSummonTable.getInstance().restoreServitor(this);
			}
			
			if (PlayerConfig.RESTORE_PET_ON_RECONNECT && !this.hasSummon() && CharSummonTable.getInstance().getPets().containsKey(this.getObjectId()))
			{
				CharSummonTable.getInstance().restorePet(this);
			}
			
			CharSummonTable.getInstance().restoreGuardians(this);
		}
		
		if (this.isTeleportProtected())
		{
			this.setTeleportProtection(false);
			if (!this.isInsideZone(ZoneId.PEACE))
			{
				this.sendMessage("Teleport spawn protection ended.");
			}
		}
	}
	
	@Override
	public void teleToLocation(ILocational loc, boolean allowRandomOffset)
	{
		if (this._vehicle != null && !this._vehicle.isTeleporting())
		{
			this.setVehicle(null);
		}
		
		if (this.isFlyingMounted() && loc.getZ() < -1005)
		{
			super.teleToLocation(loc.getX(), loc.getY(), -1005, loc.getHeading());
		}
		
		super.teleToLocation(loc, allowRandomOffset);
	}
	
	@Override
	public synchronized void onTeleported()
	{
		if (this.hasRequest(AutoPeelRequest.class))
		{
			this.sendPacket(new ExStopItemAutoPeel(true));
			this.sendPacket(new ExReadyItemAutoPeel(false, 0));
			this.removeRequest(AutoPeelRequest.class);
		}
		
		super.onTeleported();
		if (this.isInAirShip())
		{
			this.getAirShip().sendInfo(this);
		}
		else
		{
			this.setLastServerPosition(this.getX(), this.getY(), this.getZ());
		}
		
		this.revalidateZone(true);
		this.checkItemRestriction();
		if (PlayerConfig.PLAYER_TELEPORT_PROTECTION > 0 && !this._inOlympiadMode)
		{
			this.setTeleportProtection(true);
		}
		
		for (TamedBeast tamedBeast : this._tamedBeast)
		{
			tamedBeast.deleteMe();
		}
		
		this._tamedBeast.clear();
		if (this._pet != null)
		{
			this._pet.setFollowStatus(false);
			this._pet.teleToLocation(this.getLocation(), false);
			((SummonAI) this._pet.getAI()).setStartFollowController(true);
			this._pet.setFollowStatus(true);
			this._pet.setInstance(this.getInstanceWorld());
			this._pet.updateAndBroadcastStatus(0);
			this.sendPacket(new PetSummonInfo(this._pet, 0));
		}
		
		this.getServitors().values().forEach(s -> {
			s.setFollowStatus(false);
			s.teleToLocation(this.getLocation(), false);
			((SummonAI) s.getAI()).setStartFollowController(true);
			s.setFollowStatus(true);
			s.setInstance(this.getInstanceWorld());
			s.updateAndBroadcastStatus(0);
			this.sendPacket(new PetSummonInfo(s, 0));
		});
		this.getSummonedNpcs().forEach(s -> {
			if (s instanceof Guardian)
			{
				if (s.getCloneObjId() == 0)
				{
					s.teleToLocation(this.getLocation(), false);
					s.setInstance(this.getInstanceWorld());
					s.onTeleported();
				}
				else
				{
					s.deleteMe();
				}
			}
		});
		if (this._movieHolder != null)
		{
			this.sendPacket(new ExStartScenePlayer(this._movieHolder.getMovie()));
		}
		
		TimedHuntingZoneHolder holder = this.getTimedHuntingZone();
		if (holder != null && !this.isInsideZone(ZoneId.TIMED_HUNTING))
		{
			this.sendPacket(new TimedHuntingZoneExit(holder.getZoneId()));
			this.stopTimedHuntingZoneTask();
		}
		
		AutoPlayTaskManager.getInstance().stopAutoPlay(this);
		AutoUseTaskManager.getInstance().stopAutoUseTask(this);
		this.sendPacket(new ExAutoPlaySettingSend(this._autoPlaySettings.getOptions(), false, this._autoPlaySettings.doPickup(), this._autoPlaySettings.getNextTargetMode(), this._autoPlaySettings.isShortRange(), this._autoPlaySettings.getAutoPotionPercent(), this._autoPlaySettings.isRespectfulHunting(), this._autoPlaySettings.getAutoPetPotionPercent()));
		this.restoreAutoShortcutVisual();
		this.broadcastInfo();
	}
	
	@Override
	public void setTeleporting(boolean teleport)
	{
		this.setTeleporting(teleport, true);
	}
	
	public void setTeleporting(boolean teleport, boolean useWatchDog)
	{
		super.setTeleporting(teleport);
		if (useWatchDog)
		{
			if (teleport)
			{
				if (this._teleportWatchdog == null && PlayerConfig.TELEPORT_WATCHDOG_TIMEOUT > 0)
				{
					synchronized (this)
					{
						if (this._teleportWatchdog == null)
						{
							this._teleportWatchdog = ThreadPool.schedule(new TeleportWatchdogTask(this), PlayerConfig.TELEPORT_WATCHDOG_TIMEOUT * 1000);
						}
					}
				}
			}
			else if (this._teleportWatchdog != null)
			{
				this._teleportWatchdog.cancel(false);
				this._teleportWatchdog = null;
			}
		}
	}
	
	public void setTeleportLocation(Location location)
	{
		this._teleportLocation = location;
	}
	
	public Location getTeleportLocation()
	{
		return this._teleportLocation;
	}
	
	public void castTeleportSkill()
	{
		this._castingTeleportSkill = true;
		AutoPlayTaskManager.getInstance().stopAutoPlay(this);
		AutoUseTaskManager.getInstance().stopAutoUseTask(this);
		this.sendPacket(new ExAutoPlaySettingSend(this._autoPlaySettings.getOptions(), false, this._autoPlaySettings.doPickup(), this._autoPlaySettings.getNextTargetMode(), this._autoPlaySettings.isShortRange(), this._autoPlaySettings.getAutoPotionPercent(), this._autoPlaySettings.isRespectfulHunting(), this._autoPlaySettings.getAutoPetPotionPercent()));
		this.restoreAutoShortcutVisual();
		Skill skill = this.isAffectedBySkill(CommonSkill.GLORIOUS_QUICK_ESCAPE.getId()) ? CommonSkill.GLORIOUS_TELEPORT.getSkill() : CommonSkill.TELEPORT.getSkill();
		this.doCast(skill);
		
		try
		{
			Thread.sleep(skill.getHitTime() + 1000);
		}
		catch (Exception var3)
		{
		}
		
		this._castingTeleportSkill = false;
	}
	
	public boolean isCastingTeleportSkill()
	{
		return this._castingTeleportSkill;
	}
	
	public void setLastServerPosition(int x, int y, int z)
	{
		this._lastServerPosition.setXYZ(x, y, z);
	}
	
	public Location getLastServerPosition()
	{
		return this._lastServerPosition;
	}
	
	public void setBlinkActive(boolean value)
	{
		this._blinkActive.set(value);
	}
	
	public boolean isBlinkActive()
	{
		return this._blinkActive.get();
	}
	
	@Override
	public synchronized void addExpAndSp(double addToExp, double addToSp)
	{
		this.getStat().addExpAndSp(addToExp, addToSp, false);
	}
	
	public synchronized void addExpAndSp(double addToExp, double addToSp, boolean useVitality)
	{
		this.getStat().addExpAndSp(addToExp, addToSp, useVitality);
	}
	
	public void removeExpAndSp(long removeExp, long removeSp)
	{
		this.getStat().removeExpAndSp(removeExp, removeSp, true);
	}
	
	public void removeExpAndSp(long removeExp, long removeSp, boolean sendMessage)
	{
		this.getStat().removeExpAndSp(removeExp, removeSp, sendMessage);
	}
	
	@Override
	public void reduceCurrentHp(double value, Creature attacker, Skill skill, boolean isDOT, boolean directlyToHp, boolean critical, boolean reflect)
	{
		super.reduceCurrentHp(value, attacker, skill, isDOT, directlyToHp, critical, reflect);
		
		for (TamedBeast tamedBeast : this._tamedBeast)
		{
			tamedBeast.onOwnerGotAttacked(attacker);
		}
	}
	
	public void broadcastSnoop(ChatType type, String name, String text, CreatureSay cs)
	{
		if (!this._snoopListener.isEmpty())
		{
			Snoop sn = new Snoop(this.getObjectId(), this._name, type, name, text);
			
			for (Player pci : this._snoopListener)
			{
				if (pci != null)
				{
					pci.sendPacket(cs);
					pci.sendPacket(sn);
				}
			}
		}
	}
	
	public void addSnooper(Player pci)
	{
		if (!this._snoopListener.contains(pci))
		{
			this._snoopListener.add(pci);
		}
	}
	
	public void removeSnooper(Player pci)
	{
		this._snoopListener.remove(pci);
	}
	
	public void addSnooped(Player pci)
	{
		if (!this._snoopedPlayer.contains(pci))
		{
			this._snoopedPlayer.add(pci);
		}
	}
	
	public void removeSnooped(Player pci)
	{
		this._snoopedPlayer.remove(pci);
	}
	
	public void addHtmlAction(HtmlActionScope scope, String action)
	{
		this._htmlActionCaches[scope.ordinal()].add(action);
	}
	
	public void clearHtmlActions(HtmlActionScope scope)
	{
		this._htmlActionCaches[scope.ordinal()].clear();
	}
	
	public void setHtmlActionOriginObjectId(HtmlActionScope scope, int npcObjId)
	{
		if (npcObjId < 0)
		{
			throw new IllegalArgumentException();
		}
		this._htmlActionOriginObjectIds[scope.ordinal()] = npcObjId;
	}
	
	public int getLastHtmlActionOriginId()
	{
		return this._lastHtmlActionOriginObjId;
	}
	
	private static boolean validateHtmlAction(Iterable<String> actionIter, String action)
	{
		for (String cachedAction : actionIter)
		{
			if (cachedAction.charAt(cachedAction.length() - 1) == '$')
			{
				if (action.startsWith(cachedAction.substring(0, cachedAction.length() - 1).trim()))
				{
					return true;
				}
			}
			else if (cachedAction.equals(action))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public int validateHtmlAction(String action)
	{
		for (int i = 0; i < this._htmlActionCaches.length; i++)
		{
			if (validateHtmlAction(this._htmlActionCaches[i], action))
			{
				this._lastHtmlActionOriginObjId = this._htmlActionOriginObjectIds[i];
				return this._lastHtmlActionOriginObjId;
			}
		}
		
		return -1;
	}
	
	public boolean validateItemManipulation(int objectId, ItemProcessType itemProcessType)
	{
		Item item = this._inventory.getItemByObjectId(objectId);
		if (item != null && item.getOwnerId() == this.getObjectId())
		{
			if ((this._pet == null || this._pet.getControlObjectId() != objectId) && this._mountObjectID != objectId)
			{
				return this.isProcessingItem(objectId) ? false : !CursedWeaponsManager.getInstance().isCursed(item.getId());
			}
			return false;
		}
		LOGGER.finest(this.getObjectId() + ": player tried to " + itemProcessType + " item he is not owner of.");
		return false;
	}
	
	public boolean isInBoat()
	{
		return this._vehicle != null && this._vehicle.isBoat();
	}
	
	public Boat getBoat()
	{
		return (Boat) this._vehicle;
	}
	
	public boolean isInAirShip()
	{
		return this._vehicle != null && this._vehicle.isAirShip();
	}
	
	public AirShip getAirShip()
	{
		return (AirShip) this._vehicle;
	}
	
	public boolean isInShuttle()
	{
		return this._vehicle instanceof Shuttle;
	}
	
	public Shuttle getShuttle()
	{
		return (Shuttle) this._vehicle;
	}
	
	public Vehicle getVehicle()
	{
		return this._vehicle;
	}
	
	public void setVehicle(Vehicle v)
	{
		if (v == null && this._vehicle != null)
		{
			this._vehicle.removePassenger(this);
		}
		
		this._vehicle = v;
	}
	
	public boolean isInVehicle()
	{
		return this._vehicle != null;
	}
	
	public void setInCrystallize(boolean inCrystallize)
	{
		this._inCrystallize = inCrystallize;
	}
	
	public boolean isInCrystallize()
	{
		return this._inCrystallize;
	}
	
	public Location getInVehiclePosition()
	{
		return this._inVehiclePosition;
	}
	
	public void setInVehiclePosition(Location pt)
	{
		this._inVehiclePosition = pt;
	}
	
	@Override
	public boolean deleteMe()
	{
		if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_LOGOUT, this))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerLogout(this), this);
		}
		
		if (this.getReputation() < 0)
		{
			World.getInstance().removePkPlayer(this);
		}
		
		try
		{
			for (ZoneType zone : ZoneManager.getInstance().getZones(this))
			{
				zone.onPlayerLogoutInside(this);
			}
		}
		catch (Exception var31)
		{
			LOGGER.log(Level.SEVERE, "deleteMe()", var31);
		}
		
		try
		{
			if (!this._isOnline)
			{
				LOGGER.log(Level.SEVERE, "deleteMe() called on offline character " + this, (new RuntimeException()));
			}
			
			this.setOnlineStatus(false, true);
			CharInfoTable.getInstance().setLastAccess(this.getObjectId(), System.currentTimeMillis());
		}
		catch (Exception var29)
		{
			LOGGER.log(Level.SEVERE, "deleteMe()", var29);
		}
		
		try
		{
			this._isOnline = false;
			this._offlinePlay = false;
			this.abortAttack();
			this.abortCast();
			this.stopMove(null);
		}
		catch (Exception var28)
		{
			LOGGER.log(Level.SEVERE, "deleteMe()", var28);
		}
		
		try
		{
			if (this._inventory.getItemByItemId(93331) != null)
			{
				Fort fort = FortManager.getInstance().getFort(this);
				if (fort != null)
				{
					FortSiegeManager.getInstance().dropCombatFlag(this, fort.getResidenceId());
				}
				else
				{
					BodyPart bodyPart = BodyPart.fromItem(this._inventory.getItemByItemId(93331));
					this._inventory.unEquipItemInBodySlot(bodyPart);
					this.destroyItem(ItemProcessType.DESTROY, this._inventory.getItemByItemId(93331), null, true);
				}
			}
		}
		catch (Exception var27)
		{
			LOGGER.log(Level.SEVERE, "deleteMe()", var27);
		}
		
		try
		{
			if (this._matchingRoom != null)
			{
				this._matchingRoom.deleteMember(this, false);
			}
			
			MatchingRoomManager.getInstance().removeFromWaitingList(this);
		}
		catch (Exception var26)
		{
			LOGGER.log(Level.SEVERE, "deleteMe()", var26);
		}
		
		try
		{
			if (this.isFlying())
			{
				this.removeSkill(SkillData.getInstance().getSkill(CommonSkill.WYVERN_BREATH.getId(), 1));
			}
		}
		catch (Exception var25)
		{
			LOGGER.log(Level.SEVERE, "deleteMe()", var25);
		}
		
		if (this.isInTimedHuntingZone())
		{
			this.teleToLocation(TeleportWhereType.TOWN);
			this.storeCharBase();
		}
		
		CharSummonTable.getInstance().storeGuardians(this);
		if (this._isDeathKnight)
		{
			this.getVariables().set("DEATH_POINT_COUNT", this._deathPoints);
		}
		
		if (this._isVanguard)
		{
			this.getVariables().set("BEAST_POINT_COUNT", this._beastPoints);
		}
		
		if (this._isAssassin)
		{
			this.getVariables().set("ASSASSINATION_POINT_COUNT", this._assassinationPoints);
		}
		
		if (this.isInCategory(CategoryType.HIGH_ELF_TEMPLAR))
		{
			this.getVariables().set("LIGHT_POINT_COUNT", this._lightPoints);
		}
		
		if (this._isWarg)
		{
			this.getVariables().set("WOLF_POINT_COUNT", this._wolfPoints);
		}
		
		if (AdenLaboratoryConfig.ADENLAB_ENABLED)
		{
			AdenLaboratoryManager.storeAdenLabBossData(this);
		}
		
		this.getVariables().saveNow();
		this.getAccountVariables().saveNow();
		
		try
		{
			this.storeRecommendations();
		}
		catch (Exception var24)
		{
			LOGGER.log(Level.SEVERE, "deleteMe()", var24);
		}
		
		try
		{
			this.stopAllTimers();
		}
		catch (Exception var23)
		{
			LOGGER.log(Level.SEVERE, "deleteMe()", var23);
		}
		
		try
		{
			this.setTeleporting(false);
		}
		catch (Exception var22)
		{
			LOGGER.log(Level.SEVERE, "deleteMe()", var22);
		}
		
		try
		{
			RecipeManager.getInstance().requestMakeItemAbort(this);
		}
		catch (Exception var21)
		{
			LOGGER.log(Level.SEVERE, "deleteMe()", var21);
		}
		
		try
		{
			this.setTarget(null);
		}
		catch (Exception var20)
		{
			LOGGER.log(Level.SEVERE, "deleteMe()", var20);
		}
		
		if (this.isChannelized())
		{
			this.getSkillChannelized().abortChannelization();
		}
		
		this.getEffectList().stopAllToggles();
		ZoneRegion region = ZoneManager.getInstance().getRegion(this);
		if (region != null)
		{
			region.removeFromZones(this);
		}
		
		if (this.isInParty())
		{
			try
			{
				this.leaveParty();
			}
			catch (Exception var19)
			{
				LOGGER.log(Level.SEVERE, "deleteMe()", var19);
			}
		}
		
		this.stopCubics();
		
		try
		{
			this.decayMe();
		}
		catch (Exception var18)
		{
			LOGGER.log(Level.SEVERE, "deleteMe()", var18);
		}
		
		if (OlympiadManager.getInstance().isRegistered(this) || this.getOlympiadGameId() != -1)
		{
			OlympiadManager.getInstance().removeDisconnectedCompetitor(this);
		}
		
		if (this.hasSummon())
		{
			try
			{
				Summon pet = this._pet;
				if (pet != null)
				{
					pet.setRestoreSummon(true);
					pet.unSummon(this);
					pet = this._pet;
					if (pet != null)
					{
						pet.broadcastNpcInfo(0);
					}
				}
				
				this.getServitors().values().forEach(s -> {
					s.setRestoreSummon(true);
					s.unSummon(this);
				});
			}
			catch (Exception var17)
			{
				LOGGER.log(Level.SEVERE, "deleteMe()", var17);
			}
		}
		
		if (this._clan != null)
		{
			try
			{
				ClanMember clanMember = this._clan.getClanMember(this.getObjectId());
				if (clanMember != null)
				{
					clanMember.setPlayer(null);
				}
			}
			catch (Exception var16)
			{
				LOGGER.log(Level.SEVERE, "deleteMe()", var16);
			}
		}
		
		if (this.getActiveRequester() != null)
		{
			this.setActiveRequester(null);
			this.cancelActiveTrade();
		}
		
		if (this.isGM())
		{
			try
			{
				AdminData.getInstance().deleteGm(this);
			}
			catch (Exception var15)
			{
				LOGGER.log(Level.SEVERE, "deleteMe()", var15);
			}
		}
		
		try
		{
			if (this._observerMode)
			{
				this.setLocationInvisible(this._lastLoc);
			}
			
			if (this._vehicle != null)
			{
				this._vehicle.oustPlayer(this);
			}
		}
		catch (Exception var14)
		{
			LOGGER.log(Level.SEVERE, "deleteMe()", var14);
		}
		
		Instance inst = this.getInstanceWorld();
		if (inst != null)
		{
			try
			{
				inst.onPlayerLogout(this);
			}
			catch (Exception var13)
			{
				LOGGER.log(Level.SEVERE, "deleteMe()", var13);
			}
		}
		
		try
		{
			this.stopCubics();
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.SEVERE, "deleteMe()", var12);
		}
		
		try
		{
			this._inventory.deleteMe();
		}
		catch (Exception var11)
		{
			LOGGER.log(Level.SEVERE, "deleteMe()", var11);
		}
		
		try
		{
			this.getWarehouse().deleteMe();
		}
		catch (Exception var10)
		{
			LOGGER.log(Level.SEVERE, "deleteMe()", var10);
		}
		
		try
		{
			this._freight.deleteMe();
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.SEVERE, "deleteMe()", var9);
		}
		
		try
		{
			this.clearRefund();
		}
		catch (Exception var8)
		{
			LOGGER.log(Level.SEVERE, "deleteMe()", var8);
		}
		
		if (this.isCursedWeaponEquipped())
		{
			try
			{
				CursedWeaponsManager.getInstance().getCursedWeapon(this._cursedWeaponEquippedId).setPlayer(null);
			}
			catch (Exception var7)
			{
				LOGGER.log(Level.SEVERE, "deleteMe()", var7);
			}
		}
		
		if (this._clanId > 0)
		{
			this._clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(this), this);
			this._clan.broadcastToOnlineMembers(new ExPledgeCount(this._clan));
		}
		
		for (Player player : this._snoopedPlayer)
		{
			player.removeSnooper(this);
		}
		
		for (Player player : this._snoopListener)
		{
			player.removeSnooped(this);
		}
		
		if (this.isMentee())
		{
			if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_MENTEE_STATUS, this))
			{
				EventDispatcher.getInstance().notifyEventAsync(new OnPlayerMenteeStatus(this, false), this);
			}
		}
		else if (this.isMentor() && EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_MENTOR_STATUS, this))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerMentorStatus(this, false), this);
		}
		
		try
		{
			this.notifyFriends(0);
			SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_FRIEND_S1_HAS_LOGGED_OUT);
			sm.addString(this.getName());
			
			for (int id : this.getFriendList())
			{
				WorldObject obj = World.getInstance().findObject(id);
				if (obj != null)
				{
					obj.sendPacket(sm);
				}
			}
			
			ExUserWatcherTargetStatus surveillanceUpdate = new ExUserWatcherTargetStatus(this.getName(), false);
			sm = new SystemMessage(SystemMessageId.C1_FROM_YOUR_SURVEILLANCE_LIST_IS_OFFLINE);
			sm.addString(this.getName());
			
			for (Player p : World.getInstance().getPlayers())
			{
				if (p.getSurveillanceList().contains(this.getObjectId()))
				{
					p.sendPacket(sm);
					p.sendPacket(surveillanceUpdate);
				}
			}
			
			this._blockList.playerLogout();
		}
		catch (Exception var30)
		{
			LOGGER.log(Level.WARNING, "Exception on deleteMe() notifyFriends: " + var30.getMessage(), var30);
		}
		
		this.getEffectList().stopAllPassives(false, false);
		this.getEffectList().stopAllOptions(false, false);
		PlayerAutoSaveTaskManager.getInstance().remove(this);
		return super.deleteMe();
	}
	
	public int getInventoryLimit()
	{
		int ivlim;
		if (this.isGM())
		{
			ivlim = PlayerConfig.INVENTORY_MAXIMUM_GM;
		}
		else if (this.getRace() == Race.DWARF)
		{
			ivlim = PlayerConfig.INVENTORY_MAXIMUM_DWARF;
		}
		else
		{
			ivlim = PlayerConfig.INVENTORY_MAXIMUM_NO_DWARF;
		}
		
		return ivlim + (int) this.getStat().getValue(Stat.INVENTORY_NORMAL, 0.0);
	}
	
	public int getWareHouseLimit()
	{
		int whlim;
		if (this.getRace() == Race.DWARF)
		{
			whlim = PlayerConfig.WAREHOUSE_SLOTS_DWARF;
		}
		else
		{
			whlim = PlayerConfig.WAREHOUSE_SLOTS_NO_DWARF;
		}
		
		return whlim + (int) this.getStat().getValue(Stat.STORAGE_PRIVATE, 0.0);
	}
	
	public int getPrivateSellStoreLimit()
	{
		int pslim;
		if (this.getRace() == Race.DWARF)
		{
			pslim = PlayerConfig.MAX_PVTSTORESELL_SLOTS_DWARF;
		}
		else
		{
			pslim = PlayerConfig.MAX_PVTSTORESELL_SLOTS_OTHER;
		}
		
		return pslim + (int) this.getStat().getValue(Stat.TRADE_SELL, 0.0);
	}
	
	public int getPrivateBuyStoreLimit()
	{
		int pblim;
		if (this.getRace() == Race.DWARF)
		{
			pblim = PlayerConfig.MAX_PVTSTOREBUY_SLOTS_DWARF;
		}
		else
		{
			pblim = PlayerConfig.MAX_PVTSTOREBUY_SLOTS_OTHER;
		}
		
		return pblim + (int) this.getStat().getValue(Stat.TRADE_BUY, 0.0);
	}
	
	public int getDwarfRecipeLimit()
	{
		int recdlim = PlayerConfig.DWARF_RECIPE_LIMIT;
		return recdlim + (int) this.getStat().getValue(Stat.RECIPE_DWARVEN, 0.0);
	}
	
	public int getCommonRecipeLimit()
	{
		int recclim = PlayerConfig.COMMON_RECIPE_LIMIT;
		return recclim + (int) this.getStat().getValue(Stat.RECIPE_COMMON, 0.0);
	}
	
	public int getMountNpcId()
	{
		return this._mountNpcId;
	}
	
	public int getMountLevel()
	{
		return this._mountLevel;
	}
	
	public void setMountObjectID(int newID)
	{
		this._mountObjectID = newID;
	}
	
	public int getMountObjectID()
	{
		return this._mountObjectID;
	}
	
	public void setLastSkillUsed(Skill skill)
	{
		this._lastSkillUsed = skill;
	}
	
	public Skill getLastSkillUsed()
	{
		return this._lastSkillUsed;
	}
	
	public SkillUseHolder getQueuedSkill()
	{
		return this._queuedSkill;
	}
	
	public void setQueuedSkill(Skill queuedSkill, Item item, boolean ctrlPressed, boolean shiftPressed)
	{
		if (queuedSkill == null)
		{
			this._queuedSkill = null;
		}
		else
		{
			this._queuedSkill = new SkillUseHolder(queuedSkill, item, ctrlPressed, shiftPressed);
		}
	}
	
	public boolean isJailed()
	{
		return PunishmentManager.getInstance().hasPunishment(this.getObjectId(), PunishmentAffect.CHARACTER, PunishmentType.JAIL) || PunishmentManager.getInstance().hasPunishment(this.getAccountName(), PunishmentAffect.ACCOUNT, PunishmentType.JAIL) || PunishmentManager.getInstance().hasPunishment(this.getIPAddress(), PunishmentAffect.IP, PunishmentType.JAIL) || this._client != null && this._client.getHardwareInfo() != null && PunishmentManager.getInstance().hasPunishment(this._client.getHardwareInfo().getMacAddress(), PunishmentAffect.HWID, PunishmentType.JAIL);
	}
	
	public boolean isChatBanned()
	{
		return PunishmentManager.getInstance().hasPunishment(this.getObjectId(), PunishmentAffect.CHARACTER, PunishmentType.CHAT_BAN) || PunishmentManager.getInstance().hasPunishment(this.getAccountName(), PunishmentAffect.ACCOUNT, PunishmentType.CHAT_BAN) || PunishmentManager.getInstance().hasPunishment(this.getIPAddress(), PunishmentAffect.IP, PunishmentType.CHAT_BAN) || this._client != null && this._client.getHardwareInfo() != null && PunishmentManager.getInstance().hasPunishment(this._client.getHardwareInfo().getMacAddress(), PunishmentAffect.HWID, PunishmentType.CHAT_BAN);
	}
	
	public void startFameTask(long delay, int fameFixRate)
	{
		if (PlayerConfig.FAME_SYSTEM_ENABLED)
		{
			if (this.getLevel() >= 40 && this.getPlayerClass().level() >= 2)
			{
				if (this._fameTask == null)
				{
					this._fameTask = ThreadPool.scheduleAtFixedRate(new FameTask(this, fameFixRate), delay, delay);
				}
			}
		}
	}
	
	public void stopFameTask()
	{
		if (this._fameTask != null)
		{
			this._fameTask.cancel(false);
			this._fameTask = null;
		}
	}
	
	public int getPowerGrade()
	{
		return this._powerGrade;
	}
	
	public void setPowerGrade(int power)
	{
		this._powerGrade = power;
	}
	
	public boolean isCursedWeaponEquipped()
	{
		return this._cursedWeaponEquippedId != 0;
	}
	
	public void setCursedWeaponEquippedId(int value)
	{
		this._cursedWeaponEquippedId = value;
	}
	
	public int getCursedWeaponEquippedId()
	{
		return this._cursedWeaponEquippedId;
	}
	
	public boolean isCombatFlagEquipped()
	{
		return this._combatFlagEquippedId;
	}
	
	public void setCombatFlagEquipped(boolean value)
	{
		this._combatFlagEquippedId = value;
	}
	
	public int getChargedSouls(SoulType type)
	{
		return this._souls.getOrDefault(type, 0);
	}
	
	public void increaseSouls(int count, SoulType type)
	{
		if (!this.isTransformed() && !this.hasAbnormalType(AbnormalType.KAMAEL_TRANSFORM))
		{
			int newCount = this.getChargedSouls(type) + count;
			this._souls.put(type, newCount);
			SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_SOUL_COUNT_HAS_INCREASED_BY_S1_IT_IS_NOW_AT_S2);
			sm.addInt(count);
			sm.addInt(newCount);
			this.sendPacket(sm);
			this.restartSoulTask();
			this.sendPacket(new EtcStatusUpdate(this));
			if (this.getRace() == Race.KAMAEL && newCount >= 100)
			{
				if (type == SoulType.LIGHT)
				{
					int skillLevel = this.getLightMasterLevel();
					if (skillLevel > 0)
					{
						this.abortCast();
						this.decreaseSouls(100, type);
						SkillData.getInstance().getSkill(397, skillLevel).applyEffects(this, this);
					}
				}
				else
				{
					int skillLevel = this.getShadowMasterLevel();
					if (skillLevel > 0)
					{
						this.abortCast();
						this.decreaseSouls(100, type);
						SkillData.getInstance().getSkill(398, skillLevel).applyEffects(this, this);
					}
				}
			}
		}
	}
	
	public int getLightMasterLevel()
	{
		return this.getSkillLevel(45178);
	}
	
	public int getShadowMasterLevel()
	{
		return this.getSkillLevel(45179);
	}
	
	public boolean decreaseSouls(int count, SoulType type)
	{
		int newCount = this.getChargedSouls(type) - count;
		if (newCount < 0)
		{
			newCount = 0;
		}
		
		this._souls.put(type, newCount);
		if (newCount == 0)
		{
			this.stopSoulTask();
		}
		else
		{
			this.restartSoulTask();
		}
		
		this.sendPacket(new EtcStatusUpdate(this));
		return true;
	}
	
	public void clearSouls()
	{
		this._souls.clear();
		this.stopSoulTask();
		this.sendPacket(new EtcStatusUpdate(this));
	}
	
	private void restartSoulTask()
	{
		if (this._soulTask != null)
		{
			this._soulTask.cancel(false);
			this._soulTask = null;
		}
		
		this._soulTask = ThreadPool.schedule(new ResetSoulsTask(this), 600000L);
	}
	
	public void stopSoulTask()
	{
		if (this._soulTask != null)
		{
			this._soulTask.cancel(false);
			this._soulTask = null;
		}
	}
	
	public int getDeathPoints()
	{
		return this._deathPoints;
	}
	
	public int getMaxDeathPoints()
	{
		return this._maxDeathPoints;
	}
	
	public void setDeathPoints(int value)
	{
		switch (this.getAffectedSkillLevel(45352))
		{
			case 1:
				this._maxDeathPoints = 500;
				break;
			case 2:
				this._maxDeathPoints = 700;
				break;
			case 3:
				this._maxDeathPoints = 1000;
		}
		
		this._deathPoints = Math.min(this._maxDeathPoints, Math.max(0, value));
		int expectedLevel = this._deathPoints / 100;
		if (expectedLevel > 0)
		{
			if (this.getAffectedSkillLevel(45300) != expectedLevel)
			{
				this.getEffectList().stopSkillEffects(SkillFinishType.REMOVED, 45300);
				SkillData.getInstance().getSkill(45300, expectedLevel).applyEffects(this, this);
			}
		}
		else
		{
			this.getEffectList().stopSkillEffects(SkillFinishType.REMOVED, 45300);
		}
		
		StatusUpdate su = new StatusUpdate(this);
		this.computeStatusUpdate(su, StatusUpdateType.MAX_DP);
		this.computeStatusUpdate(su, StatusUpdateType.CUR_DP);
		this.sendPacket(su);
	}
	
	public void calculateMaxBeastPoints()
	{
		double baseValue = 1000.0;
		int classLevel = this.getPlayerClass().level();
		if (classLevel > 1)
		{
			int con = this.getCON();
			double conBonus = Math.max(BaseStat.CON.calcBonus(this), 1.0);
			baseValue += 377.24 * conBonus;
			if (con >= 45)
			{
				baseValue = 975.0 * conBonus;
			}
			
			if (classLevel > 2)
			{
				baseValue *= 4.0;
			}
		}
		
		this._maxBeastPoints = (int) (baseValue + this.getStat().getValue(Stat.BEAST_POINTS_ADD, 0.0));
	}
	
	public int getMaxBeastPoints()
	{
		return this._maxBeastPoints;
	}
	
	public void setMaxBeastPoints(int value)
	{
		this._maxBeastPoints = value;
	}
	
	public int getBeastPoints()
	{
		return this._beastPoints;
	}
	
	public void setBeastPoints(int value)
	{
		this._beastPoints = Math.min(this._maxBeastPoints, Math.max(0, value));
		StatusUpdate su = new StatusUpdate(this);
		this.computeStatusUpdate(su, StatusUpdateType.MAX_BP);
		this.computeStatusUpdate(su, StatusUpdateType.CUR_BP);
		this.sendPacket(su);
	}
	
	public int getMaxAssassinationPoints()
	{
		return this._maxAssassinationPoints;
	}
	
	public void setMaxAssassinationPoints(int value)
	{
		this._maxAssassinationPoints = value;
	}
	
	public int getAssassinationPoints()
	{
		return this._assassinationPoints;
	}
	
	public void setAssassinationPoints(int value)
	{
		this._assassinationPoints = Math.min(this._maxAssassinationPoints, Math.max(0, value));
		StatusUpdate su = new StatusUpdate(this);
		this.computeStatusUpdate(su, StatusUpdateType.MAX_AP);
		this.computeStatusUpdate(su, StatusUpdateType.CUR_AP);
		this.sendPacket(su);
	}
	
	public int getMaxLightPoints()
	{
		return this._maxLightPoints;
	}
	
	public void setMaxLightPoints(int value)
	{
		this._maxLightPoints = value;
	}
	
	public int getLightPoints()
	{
		return this._lightPoints;
	}
	
	public void setLightPoints(int value)
	{
		this._lightPoints = Math.min(this._maxLightPoints, Math.max(0, value));
		StatusUpdate su = new StatusUpdate(this);
		this.computeStatusUpdate(su, StatusUpdateType.MAX_LP);
		this.computeStatusUpdate(su, StatusUpdateType.CUR_LP);
		this.sendPacket(su);
	}
	
	public int getMaxWolfPoints()
	{
		return this._maxWolfPoints;
	}
	
	public void setMaxWolfPoints(int value)
	{
		this._maxWolfPoints = value;
	}
	
	public int getWolfPoints()
	{
		return this._wolfPoints;
	}
	
	public void setWolfPoints(int value)
	{
		this._wolfPoints = Math.min(this._maxWolfPoints, Math.max(0, value));
		StatusUpdate su = new StatusUpdate(this);
		this.computeStatusUpdate(su, StatusUpdateType.MAX_WP);
		this.computeStatusUpdate(su, StatusUpdateType.CUR_WP);
		this.sendPacket(su);
	}
	
	@Override
	public void sendDamageMessage(Creature target, Skill skill, int damage, double elementalDamage, boolean crit, boolean miss, boolean elementalCrit)
	{
		if (miss)
		{
			this.sendPacket(new ExDamagePopUp(this.getObjectId(), target.getObjectId(), 0, (byte) 11));
			if (skill == null)
			{
				if (target.isPlayer())
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.C1_HAS_EVADED_C2_S_ATTACK);
					sm.addPcName(target.asPlayer());
					sm.addString(this.getName());
					target.sendPacket(sm);
				}
				
				SystemMessage sm = new SystemMessage(SystemMessageId.C1_S_ATTACK_WENT_ASTRAY);
				sm.addPcName(this);
				this.sendPacket(sm);
			}
			else
			{
				this.sendPacket(new ExMagicAttackInfo(this.getObjectId(), target.getObjectId(), 2));
			}
		}
		else
		{
			if (crit)
			{
				if (skill != null && skill.isMagic())
				{
					this.sendPacket(SystemMessageId.M_CRITICAL);
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.C1_LANDED_A_CRITICAL_HIT);
					sm.addPcName(this);
					this.sendPacket(sm);
				}
				
				if (skill != null)
				{
					if (skill.isMagic())
					{
						this.sendPacket(new ExMagicAttackInfo(this.getObjectId(), target.getObjectId(), 11));
					}
					else if (skill.isPhysical())
					{
						this.sendPacket(new ExMagicAttackInfo(this.getObjectId(), target.getObjectId(), 10));
					}
					else
					{
						this.sendPacket(new ExMagicAttackInfo(this.getObjectId(), target.getObjectId(), 7));
					}
				}
			}
			
			if (elementalCrit)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_ATTACK_CRITICAL_IS_ACTIVATED);
				sm.addElementalSpiritName(this.getActiveElementalSpiritType());
				this.sendPacket(sm);
			}
			
			if (this.isInOlympiadMode() && target.isPlayer() && target.asPlayer().isInOlympiadMode() && target.asPlayer().getOlympiadGameId() == this.getOlympiadGameId())
			{
				OlympiadGameManager.getInstance().notifyCompetitorDamage(this, damage);
			}
			
			if ((!target.isHpBlocked() || target.isNpc()) && (!target.isPlayer() || !target.isAffected(EffectFlag.DUELIST_FURY) || this.isAffected(EffectFlag.FACEOFF)) && !target.isInvul())
			{
				if (target.isDoor() || target instanceof ControlTower)
				{
					this.sendPacket(new ExDamagePopUp(this.getObjectId(), target.getObjectId(), damage, (byte) 1));
					target.sendPacket(new ExDamagePopUp(this.getObjectId(), target.getObjectId(), damage, (byte) 1));
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_VE_HIT_FOR_S1_DAMAGE);
					sm.addInt(damage);
					this.sendPacket(sm);
				}
				else if (this != target)
				{
					if (crit)
					{
						this.sendPacket(new ExDamagePopUp(this.getObjectId(), target.getObjectId(), damage, (byte) 3));
						target.sendPacket(new ExDamagePopUp(this.getObjectId(), target.getObjectId(), damage, (byte) 3));
					}
					else if (skill != null)
					{
						this.sendPacket(new ExDamagePopUp(this.getObjectId(), target.getObjectId(), damage, (byte) 13));
						target.sendPacket(new ExDamagePopUp(this.getObjectId(), target.getObjectId(), damage, (byte) 13));
					}
					else
					{
						this.sendPacket(new ExDamagePopUp(this.getObjectId(), target.getObjectId(), damage, (byte) 1));
						target.sendPacket(new ExDamagePopUp(this.getObjectId(), target.getObjectId(), damage, (byte) 1));
					}
					
					SystemMessage sm;
					if (elementalDamage != 0.0)
					{
						sm = new SystemMessage(SystemMessageId.S1_HAS_DEALT_S3_DAMAGE_TO_S2_S4_ATTRIBUTE_DAMAGE);
					}
					else
					{
						sm = new SystemMessage(SystemMessageId.C1_HAS_DEALT_S3_DAMAGE_TO_C2);
					}
					
					sm.addPcName(this);
					String targetName = target.getName();
					if (MultilingualSupportConfig.MULTILANG_ENABLE && target.isNpc())
					{
						String[] localisation = NpcNameLocalisationData.getInstance().getLocalisation(this._lang, target.getId());
						if (localisation != null)
						{
							targetName = localisation[0];
						}
					}
					
					sm.addString(targetName);
					sm.addInt(damage);
					if (elementalDamage != 0.0)
					{
						sm.addInt((int) elementalDamage);
					}
					
					sm.addPopup(target.getObjectId(), this.getObjectId(), -damage);
					this.sendPacket(sm);
				}
			}
			else
			{
				if (skill == null)
				{
					this.sendPacket(new ExDamagePopUp(this.getObjectId(), target.getObjectId(), 0, (byte) 12));
				}
				else
				{
					this.sendPacket(new ExMagicAttackInfo(this.getObjectId(), target.getObjectId(), 4));
				}
				
				this.sendPacket(SystemMessageId.THE_ATTACK_HAS_BEEN_BLOCKED);
			}
		}
	}
	
	public void setAgathionId(int npcId)
	{
		this._agathionId = npcId;
	}
	
	public int getAgathionId()
	{
		return this._agathionId;
	}
	
	public int getVitalityPoints()
	{
		return this.getStat().getVitalityPoints();
	}
	
	public void setVitalityPoints(int points, boolean quiet)
	{
		this.getStat().setVitalityPoints(points, quiet);
	}
	
	public void updateVitalityPoints(int points, boolean useRates, boolean quiet)
	{
		if (this._huntPass == null || !this._huntPass.toggleSayha())
		{
			this.getStat().updateVitalityPoints(points, useRates, quiet);
		}
	}
	
	public void setSayhaGraceSupportEndTime(long endTime)
	{
		if (this.getVariables().getLong("SAYHA_GRACE_SUPPORT_ENDTIME", 0L) < System.currentTimeMillis())
		{
			this.getVariables().set("SAYHA_GRACE_SUPPORT_ENDTIME", endTime);
			this.sendPacket(new ExUserBoostStat(this, BonusExpType.VITALITY));
			this.sendPacket(new ExVitalityEffectInfo(this));
		}
	}
	
	public long getSayhaGraceSupportEndTime()
	{
		return this.getVariables().getLong("SAYHA_GRACE_SUPPORT_ENDTIME", 0L);
	}
	
	public boolean setLimitedSayhaGraceEndTime(long endTime)
	{
		if (endTime > this.getVariables().getLong("LIMITED_SAYHA_GRACE_ENDTIME", 0L))
		{
			this.getVariables().set("LIMITED_SAYHA_GRACE_ENDTIME", endTime);
			this.sendPacket(new ExUserBoostStat(this, BonusExpType.VITALITY));
			this.sendPacket(new ExVitalityEffectInfo(this));
			return true;
		}
		return false;
	}
	
	public long getLimitedSayhaGraceEndTime()
	{
		return this.getVariables().getLong("LIMITED_SAYHA_GRACE_ENDTIME", 0L);
	}
	
	public void checkItemRestriction()
	{
		for (int i = 0; i < 59; i++)
		{
			Item equippedItem = this._inventory.getPaperdollItem(i);
			if (equippedItem != null && !equippedItem.getTemplate().checkCondition(this, this, false))
			{
				this._inventory.unEquipItemInSlot(i);
				InventoryUpdate iu = new InventoryUpdate();
				iu.addModifiedItem(equippedItem);
				this.sendInventoryUpdate(iu);
				SystemMessage sm = null;
				if (equippedItem.getTemplate().getBodyPart() == BodyPart.BACK)
				{
					this.sendPacket(SystemMessageId.YOUR_CLOAK_HAS_BEEN_UNEQUIPPED_BECAUSE_YOUR_ARMOR_SET_IS_NO_LONGER_COMPLETE);
					return;
				}
				
				if (equippedItem.getEnchantLevel() > 0)
				{
					sm = new SystemMessage(SystemMessageId.S1_S2_UNEQUIPPED);
					sm.addInt(equippedItem.getEnchantLevel());
					sm.addItemName(equippedItem);
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.S1_UNEQUIPPED);
					sm.addItemName(equippedItem);
				}
				
				this.sendPacket(sm);
			}
		}
	}
	
	public void addTransformSkill(Skill skill)
	{
		this._transformSkills.put(skill.getId(), skill);
	}
	
	public boolean hasTransformSkill(Skill skill)
	{
		return this._transformSkills.get(skill.getId()) == skill;
	}
	
	public boolean hasTransformSkills()
	{
		return !this._transformSkills.isEmpty();
	}
	
	public Collection<Skill> getAllTransformSkills()
	{
		return this._transformSkills.values();
	}
	
	public void removeAllTransformSkills()
	{
		this._transformSkills.clear();
	}
	
	@Override
	public Skill getKnownSkill(int skillId)
	{
		return !this._transformSkills.isEmpty() ? this._transformSkills.getOrDefault(skillId, super.getKnownSkill(skillId)) : super.getKnownSkill(skillId);
	}
	
	public Collection<Skill> getSkillList()
	{
		Collection<Skill> currentSkills = this.getAllSkills();
		if (this.isTransformed() && !this._transformSkills.isEmpty())
		{
			List<Skill> filteredSkills = new LinkedList<>();
			
			for (Skill skill : currentSkills)
			{
				if (skill.allowOnTransform())
				{
					filteredSkills.add(skill);
				}
			}
			
			currentSkills = filteredSkills;
			if (this.isDualClassActive())
			{
				int revelationSkill = this.getVariables().getInt("DualclassRevelationSkill1", 0);
				if (revelationSkill != 0)
				{
					this.addSkill(SkillData.getInstance().getSkill(revelationSkill, 1), false);
				}
				
				revelationSkill = this.getVariables().getInt("DualclassRevelationSkill2", 0);
				if (revelationSkill != 0)
				{
					this.addSkill(SkillData.getInstance().getSkill(revelationSkill, 1), false);
				}
			}
			else if (!this.isSubClassActive())
			{
				int revelationSkillx = this.getVariables().getInt("RevelationSkill1", 0);
				if (revelationSkillx != 0)
				{
					this.addSkill(SkillData.getInstance().getSkill(revelationSkillx, 1), false);
				}
				
				revelationSkillx = this.getVariables().getInt("RevelationSkill2", 0);
				if (revelationSkillx != 0)
				{
					this.addSkill(SkillData.getInstance().getSkill(revelationSkillx, 1), false);
				}
			}
			
			filteredSkills.addAll(this._transformSkills.values());
		}
		
		List<Skill> finalSkills = new LinkedList<>();
		
		for (Skill skillx : currentSkills)
		{
			if (skillx != null && !skillx.isBlockActionUseSkill() && !SkillTreeData.getInstance().isAlchemySkill(skillx.getId(), skillx.getLevel()) && skillx.isDisplayInList())
			{
				finalSkills.add(skillx);
			}
		}
		
		return finalSkills;
	}
	
	protected void startFeed(int npcId)
	{
		this._canFeed = npcId > 0;
		if (this.isMounted())
		{
			if (this.hasPet())
			{
				this.setCurrentFeed(this._pet.getCurrentFed());
				this._controlItemId = this._pet.getControlObjectId();
				this.sendPacket(new SetupGauge(3, this._curFeed * 10000 / this.getFeedConsume(), this.getMaxFeed() * 10000 / this.getFeedConsume()));
				if (!this.isDead())
				{
					this._mountFeedTask = ThreadPool.scheduleAtFixedRate(new PetFeedTask(this), 10000L, 10000L);
				}
			}
			else if (this._canFeed)
			{
				this.setCurrentFeed(this.getMaxFeed());
				this.sendPacket(new SetupGauge(3, this._curFeed * 10000 / this.getFeedConsume(), this.getMaxFeed() * 10000 / this.getFeedConsume()));
				if (!this.isDead())
				{
					this._mountFeedTask = ThreadPool.scheduleAtFixedRate(new PetFeedTask(this), 10000L, 10000L);
				}
			}
		}
	}
	
	public void stopFeed()
	{
		if (this._mountFeedTask != null)
		{
			this._mountFeedTask.cancel(false);
			this._mountFeedTask = null;
		}
	}
	
	private void clearPetData()
	{
		this._data = null;
	}
	
	public PetData getPetData(int npcId)
	{
		if (this._data == null)
		{
			this._data = PetDataTable.getInstance().getPetData(npcId);
		}
		
		return this._data;
	}
	
	private PetLevelData getPetLevelData(int npcId)
	{
		if (this._leveldata == null)
		{
			this._leveldata = PetDataTable.getInstance().getPetData(npcId).getPetLevelData(this.getMountLevel());
		}
		
		return this._leveldata;
	}
	
	public int getCurrentFeed()
	{
		return this._curFeed;
	}
	
	public int getFeedConsume()
	{
		return this.isAttackingNow() ? this.getPetLevelData(this._mountNpcId).getPetFeedBattle() : this.getPetLevelData(this._mountNpcId).getPetFeedNormal();
	}
	
	public void setCurrentFeed(int num)
	{
		boolean lastHungryState = this.isHungry();
		this._curFeed = num > this.getMaxFeed() ? this.getMaxFeed() : num;
		this.sendPacket(new SetupGauge(3, this._curFeed * 10000 / this.getFeedConsume(), this.getMaxFeed() * 10000 / this.getFeedConsume()));
		if (lastHungryState != this.isHungry())
		{
			this.broadcastUserInfo();
		}
	}
	
	private int getMaxFeed()
	{
		return this.getPetLevelData(this._mountNpcId).getPetMaxFeed();
	}
	
	public boolean isHungry()
	{
		return this.hasPet() && this._canFeed && this._curFeed < this.getPetData(this._mountNpcId).getHungryLimit() / 100.0F * this.getPetLevelData(this._mountNpcId).getPetMaxFeed();
	}
	
	public void enteredNoLanding(int delay)
	{
		this._dismountTask = ThreadPool.schedule(new DismountTask(this), delay * 1000);
	}
	
	public void exitedNoLanding()
	{
		if (this._dismountTask != null)
		{
			this._dismountTask.cancel(true);
			this._dismountTask = null;
		}
	}
	
	public void storePetFood(int petId)
	{
		if (this._controlItemId != 0 && petId != 0)
		{
			String req = "UPDATE pets SET fed=? WHERE item_obj_id = ?";
			
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement(req);)
			{
				statement.setInt(1, this._curFeed);
				statement.setInt(2, this._controlItemId);
				statement.executeUpdate();
				this._controlItemId = 0;
			}
			catch (Exception var11)
			{
				LOGGER.log(Level.SEVERE, "Failed to store Pet [NpcId: " + petId + "] data", var11);
			}
		}
	}
	
	public void setInSiege(boolean value)
	{
		this._isInSiege = value;
	}
	
	public boolean isInSiege()
	{
		return this._isInSiege;
	}
	
	public void setInHideoutSiege(boolean isInHideoutSiege)
	{
		this._isInHideoutSiege = isInHideoutSiege;
	}
	
	public boolean isInHideoutSiege()
	{
		return this._isInHideoutSiege;
	}
	
	public boolean isFlyingMounted()
	{
		Transform transform = this.getTransformation();
		return transform != null && transform.isFlying();
	}
	
	public int getCharges()
	{
		return this._charges.get();
	}
	
	public void setCharges(int count)
	{
		this.restartChargeTask();
		this._charges.set(count);
	}
	
	public boolean decreaseCharges(int count)
	{
		if (this._charges.get() < count)
		{
			return false;
		}
		if (this._charges.addAndGet(-count) == 0)
		{
			this.stopChargeTask();
		}
		else
		{
			this.restartChargeTask();
		}
		
		this.sendPacket(new EtcStatusUpdate(this));
		return true;
	}
	
	public void clearCharges()
	{
		this._charges.set(0);
		this.sendPacket(new EtcStatusUpdate(this));
	}
	
	private void restartChargeTask()
	{
		if (this._chargeTask != null)
		{
			this._chargeTask.cancel(false);
			this._chargeTask = null;
		}
		
		this._chargeTask = ThreadPool.schedule(new ResetChargesTask(this), 600000L);
	}
	
	public void stopChargeTask()
	{
		if (this._chargeTask != null)
		{
			this._chargeTask.cancel(false);
			this._chargeTask = null;
		}
	}
	
	public void teleportBookmarkModify(int id, int icon, String tag, String name)
	{
		if (!this.isInTimedHuntingZone())
		{
			TeleportBookmark bookmark = this._tpbookmarks.get(id);
			if (bookmark != null)
			{
				bookmark.setIcon(icon);
				bookmark.setTag(tag);
				bookmark.setName(name);
				
				try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("UPDATE character_tpbookmark SET icon=?,tag=?,name=? where charId=? AND Id=?");)
				{
					statement.setInt(1, icon);
					statement.setString(2, tag);
					statement.setString(3, name);
					statement.setInt(4, this.getObjectId());
					statement.setInt(5, id);
					statement.execute();
				}
				catch (Exception var14)
				{
					LOGGER.log(Level.WARNING, "Could not update character teleport bookmark data: " + var14.getMessage(), var14);
				}
			}
			
			this.sendPacket(new ExGetBookMarkInfoPacket(this));
		}
	}
	
	public void teleportBookmarkDelete(int id)
	{
		if (this._tpbookmarks.remove(id) != null)
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("DELETE FROM character_tpbookmark WHERE charId=? AND Id=?");)
			{
				statement.setInt(1, this.getObjectId());
				statement.setInt(2, id);
				statement.execute();
			}
			catch (Exception var10)
			{
				LOGGER.log(Level.WARNING, "Could not delete character teleport bookmark data: " + var10.getMessage(), var10);
			}
			
			this.sendPacket(new ExGetBookMarkInfoPacket(this));
		}
	}
	
	public void teleportBookmarkGo(int id)
	{
		if (this.teleportBookmarkCondition(0))
		{
			if (this._inventory.getInventoryItemCount(13016, 0) == 0L)
			{
				this.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_BECAUSE_YOU_DO_NOT_HAVE_A_TELEPORT_ITEM);
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISAPPEARED);
				sm.addItemName(13016);
				this.sendPacket(sm);
				TeleportBookmark bookmark = this._tpbookmarks.get(id);
				if (bookmark != null)
				{
					if (this.isInTimedHuntingZone())
					{
						this.sendMessage("You cannot teleport at this location.");
						return;
					}
					
					this.destroyItem(ItemProcessType.NONE, this._inventory.getItemByItemId(13016).getObjectId(), 1L, null, false);
					this.setTeleportLocation(bookmark);
					this.doCast(CommonSkill.MY_TELEPORT.getSkill());
				}
				
				this.sendPacket(new ExGetBookMarkInfoPacket(this));
			}
		}
	}
	
	public boolean teleportBookmarkCondition(int type)
	{
		if (this.isInCombat())
		{
			this.sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_DURING_A_BATTLE);
			return false;
		}
		else if (this._isInSiege || this._siegeState != 0)
		{
			this.sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_PARTICIPATING_A_LARGE_SCALE_BATTLE_SUCH_AS_A_CASTLE_SIEGE_FORTRESS_SIEGE_OR_CLAN_HALL_SIEGE);
			return false;
		}
		else if (this._isInDuel || this._startingDuel)
		{
			this.sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_DURING_A_DUEL);
			return false;
		}
		else if (this.isFlying())
		{
			this.sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_FLYING);
			return false;
		}
		else if (this._inOlympiadMode)
		{
			this.sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_PARTICIPATING_IN_AN_OLYMPIAD_MATCH);
			return false;
		}
		else if (this.hasBlockActions() && this.hasAbnormalType(AbnormalType.PARALYZE))
		{
			this.sendPacket(SystemMessageId.CANNOT_TELEPORT_WHILE_PETRIFIED_OR_PARALYZED);
			return false;
		}
		else if (this.isDead())
		{
			this.sendPacket(SystemMessageId.YOU_CANNOT_USE_TELEPORT_WHILE_YOU_ARE_DEAD);
			return false;
		}
		else if (this.isInWater())
		{
			this.sendPacket(SystemMessageId.YOU_CANNOT_USE_TELEPORT_UNDERWATER);
			return false;
		}
		else if (type != 1 || !this.isInsideZone(ZoneId.SIEGE) && !this.isInsideZone(ZoneId.CLAN_HALL) && !this.isInsideZone(ZoneId.JAIL) && !this.isInsideZone(ZoneId.CASTLE) && !this.isInsideZone(ZoneId.NO_SUMMON_FRIEND) && !this.isInsideZone(ZoneId.FORT))
		{
			if (!this.isInsideZone(ZoneId.NO_BOOKMARK) && !this.isInBoat() && !this.isInAirShip() && !this.isInTimedHuntingZone())
			{
				return true;
			}
			if (type == 0)
			{
				this.sendPacket(SystemMessageId.YOU_CANNOT_USE_TELEPORT_IN_THIS_AREA);
			}
			else if (type == 1)
			{
				this.sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_TO_REACH_THIS_AREA);
			}
			
			return false;
		}
		else
		{
			this.sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_TO_REACH_THIS_AREA);
			return false;
		}
	}
	
	public void teleportBookmarkAdd(int x, int y, int z, int icon, String tag, String name)
	{
		if (this.teleportBookmarkCondition(1))
		{
			if (!this.isInTimedHuntingZone())
			{
				if (this._tpbookmarks.size() >= this._bookmarkslot)
				{
					this.sendPacket(SystemMessageId.YOU_HAVE_NO_SPACE_TO_SAVE_THE_TELEPORT_LOCATION);
				}
				else
				{
					if (GeneralConfig.BOOKMARK_CONSUME_ITEM_ID > 0)
					{
						if (this._inventory.getInventoryItemCount(GeneralConfig.BOOKMARK_CONSUME_ITEM_ID, -1) == 0L)
						{
							if (GeneralConfig.BOOKMARK_CONSUME_ITEM_ID == 20033)
							{
								this.sendPacket(SystemMessageId.YOU_CANNOT_BOOKMARK_THIS_LOCATION_BECAUSE_YOU_DO_NOT_HAVE_A_MY_TELEPORT_FLAG);
							}
							else
							{
								this.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
							}
							
							return;
						}
						
						this.destroyItem(ItemProcessType.NONE, this._inventory.getItemByItemId(GeneralConfig.BOOKMARK_CONSUME_ITEM_ID).getObjectId(), 1L, null, true);
					}
					
					int id = 1;
					
					while (id <= this._bookmarkslot && this._tpbookmarks.containsKey(id))
					{
						id++;
					}
					
					this._tpbookmarks.put(id, new TeleportBookmark(id, x, y, z, icon, tag, name));
					
					try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("INSERT INTO character_tpbookmark (charId,Id,x,y,z,icon,tag,name) values (?,?,?,?,?,?,?,?)");)
					{
						statement.setInt(1, this.getObjectId());
						statement.setInt(2, id);
						statement.setInt(3, x);
						statement.setInt(4, y);
						statement.setInt(5, z);
						statement.setInt(6, icon);
						statement.setString(7, tag);
						statement.setString(8, name);
						statement.execute();
					}
					catch (Exception var16)
					{
						LOGGER.log(Level.WARNING, "Could not insert character teleport bookmark data: " + var16.getMessage(), var16);
					}
					
					this.sendPacket(new ExGetBookMarkInfoPacket(this));
				}
			}
		}
	}
	
	public void restoreTeleportBookmark()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT Id,x,y,z,icon,tag,name FROM character_tpbookmark WHERE charId=?");)
		{
			statement.setInt(1, this.getObjectId());
			
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					this._tpbookmarks.put(rset.getInt("Id"), new TeleportBookmark(rset.getInt("Id"), rset.getInt("x"), rset.getInt("y"), rset.getInt("z"), rset.getInt("icon"), rset.getString("tag"), rset.getString("name")));
				}
			}
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.SEVERE, "Failed restoing character teleport bookmark.", var12);
		}
	}
	
	public Collection<TeleportBookmark> getTeleportBookmarks()
	{
		return this._tpbookmarks.values();
	}
	
	@Override
	public void sendInfo(Player player)
	{
		if (this.isInBoat())
		{
			this.setXYZ(this.getBoat().getLocation());
			player.sendPacket(new CharInfo(this, this.isInvisible() && player.isGM()));
			player.sendPacket(new GetOnVehicle(this.getObjectId(), this.getBoat().getObjectId(), this._inVehiclePosition));
		}
		else if (this.isInAirShip())
		{
			this.setXYZ(this.getAirShip().getLocation());
			player.sendPacket(new CharInfo(this, this.isInvisible() && player.isGM()));
			player.sendPacket(new ExGetOnAirShip(this, this.getAirShip()));
		}
		else
		{
			player.sendPacket(new CharInfo(this, this.isInvisible() && player.isGM()));
		}
		
		long relation1 = this.getRelation(player);
		RelationChanged rc1 = new RelationChanged();
		rc1.addRelation(this, relation1, !this.isInsideZone(ZoneId.PEACE) || !this.isInsideZone(ZoneId.NO_PVP));
		if (this.hasSummon())
		{
			if (this._pet != null)
			{
				rc1.addRelation(this._pet, relation1, !this.isInsideZone(ZoneId.PEACE) || !this.isInsideZone(ZoneId.NO_PVP));
			}
			
			if (this.hasServitors())
			{
				this.getServitors().values().forEach(s -> rc1.addRelation(s, relation1, !this.isInsideZone(ZoneId.PEACE) || !this.isInsideZone(ZoneId.NO_PVP)));
			}
		}
		
		player.sendPacket(rc1);
		long relation2 = player.getRelation(this);
		RelationChanged rc2 = new RelationChanged();
		rc2.addRelation(player, relation2, !player.isInsideZone(ZoneId.PEACE));
		if (player.hasSummon())
		{
			if (this._pet != null)
			{
				rc2.addRelation(this._pet, relation2, !player.isInsideZone(ZoneId.PEACE));
			}
			
			if (this.hasServitors())
			{
				this.getServitors().values().forEach(s -> rc2.addRelation(s, relation2, !player.isInsideZone(ZoneId.PEACE)));
			}
		}
		
		this.sendPacket(rc2);
		switch (this._privateStoreType)
		{
			case SELL:
				player.sendPacket(new PrivateStoreMsgSell(this));
				break;
			case PACKAGE_SELL:
				player.sendPacket(new ExPrivateStoreSetWholeMsg(this));
				break;
			case BUY:
				player.sendPacket(new PrivateStoreMsgBuy(this));
				break;
			case MANUFACTURE:
				player.sendPacket(new RecipeShopMsg(this));
		}
		
		if (this.isTransformed())
		{
			player.sendPacket(new CharInfo(this, this.isInvisible() && player.isGM()));
		}
	}
	
	public void playMovie(MovieHolder holder)
	{
		if (this._movieHolder == null)
		{
			this.abortAttack();
			this.stopMove(null);
			this.setMovieHolder(holder);
			if (!this.isTeleporting())
			{
				this.sendPacket(new ExStartScenePlayer(holder.getMovie()));
			}
		}
	}
	
	public void stopMovie()
	{
		this.sendPacket(new ExStopScenePlayer(this._movieHolder.getMovie()));
		this.setMovieHolder(null);
	}
	
	public boolean isAllowedToEnchantSkills()
	{
		if (this.isSubclassLocked())
		{
			return false;
		}
		else if (this.isTransformed())
		{
			return false;
		}
		else if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(this))
		{
			return false;
		}
		else
		{
			return this.isCastingNow() ? false : !this.isInBoat() && !this.isInAirShip();
		}
	}
	
	public void setCreateDate(Calendar createDate)
	{
		this._createDate = createDate;
	}
	
	public Calendar getCreateDate()
	{
		return this._createDate;
	}
	
	public int checkBirthDay()
	{
		Calendar now = Calendar.getInstance();
		if (this._createDate.get(5) == 29 && this._createDate.get(2) == 1)
		{
			this._createDate.add(11, -24);
		}
		
		if (now.get(2) == this._createDate.get(2) && now.get(5) == this._createDate.get(5) && now.get(1) != this._createDate.get(1))
		{
			return 0;
		}
		for (int i = 1; i < 6; i++)
		{
			now.add(11, 24);
			if (now.get(2) == this._createDate.get(2) && now.get(5) == this._createDate.get(5) && now.get(1) != this._createDate.get(1))
			{
				return i;
			}
		}
		
		return -1;
	}
	
	public int getBirthdays()
	{
		long time = (System.currentTimeMillis() - this._createDate.getTimeInMillis()) / 1000L;
		time /= TimeUnit.DAYS.toMillis(365L);
		return (int) time;
	}
	
	public Set<Integer> getFriendList()
	{
		return this._friendList;
	}
	
	public void restoreFriendList()
	{
		this._friendList.clear();
		
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT friendId FROM character_friends WHERE charId=? AND relation=0");)
		{
			statement.setInt(1, this.getObjectId());
			
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					int friendId = rset.getInt("friendId");
					if (friendId != this.getObjectId())
					{
						this._friendList.add(friendId);
					}
				}
			}
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.WARNING, "Error found in " + this._name + "'s FriendList: " + var12.getMessage(), var12);
		}
	}
	
	public void notifyFriends(int type)
	{
		FriendStatus pkt = new FriendStatus(this, type);
		
		for (int id : this._friendList)
		{
			Player friend = World.getInstance().getPlayer(id);
			if (friend != null)
			{
				friend.sendPacket(pkt);
			}
		}
	}
	
	public Set<Integer> getSurveillanceList()
	{
		return this._surveillanceList;
	}
	
	public void restoreSurveillanceList()
	{
		this._surveillanceList.clear();
		
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT targetId FROM character_surveillances WHERE charId=?");)
		{
			statement.setInt(1, this.getObjectId());
			
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					int friendId = rset.getInt("targetId");
					if (friendId != this.getObjectId())
					{
						this._surveillanceList.add(friendId);
					}
				}
			}
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.WARNING, "Error found in " + this.getName() + "'s SurveillanceList: " + var12.getMessage(), var12);
		}
	}
	
	public void updateFriendMemo(String name, String memo)
	{
		if (memo.length() <= 50)
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("UPDATE character_friends SET memo=? WHERE charId=? AND friendId=?");)
			{
				int friendId = CharInfoTable.getInstance().getIdByName(name);
				statement.setString(1, memo);
				statement.setInt(2, this.getObjectId());
				statement.setInt(3, friendId);
				statement.execute();
				CharInfoTable.getInstance().setFriendMemo(this.getObjectId(), friendId, memo);
			}
			catch (Exception var11)
			{
				LOGGER.log(Level.WARNING, "Error occurred while updating friend memo: " + var11.getMessage(), var11);
			}
		}
	}
	
	public boolean isSilenceMode()
	{
		return this._silenceMode;
	}
	
	public boolean isSilenceMode(int playerObjId)
	{
		return PlayerConfig.SILENCE_MODE_EXCLUDE && this._silenceMode && this._silenceModeExcluded != null ? !this._silenceModeExcluded.contains(playerObjId) : this._silenceMode;
	}
	
	public void setSilenceMode(boolean mode)
	{
		this._silenceMode = mode;
		if (this._silenceModeExcluded != null)
		{
			this._silenceModeExcluded.clear();
		}
		
		this.sendPacket(new EtcStatusUpdate(this));
	}
	
	public void addSilenceModeExcluded(int playerObjId)
	{
		if (this._silenceModeExcluded == null)
		{
			this._silenceModeExcluded = new ArrayList<>(1);
		}
		
		this._silenceModeExcluded.add(playerObjId);
	}
	
	private void storeRecipeShopList()
	{
		if (this.hasManufactureShop())
		{
			try (Connection con = DatabaseFactory.getConnection())
			{
				try (PreparedStatement st = con.prepareStatement("DELETE FROM character_recipeshoplist WHERE charId=?"))
				{
					st.setInt(1, this.getObjectId());
					st.execute();
				}
				
				try (PreparedStatement st = con.prepareStatement("REPLACE INTO character_recipeshoplist (`charId`, `recipeId`, `price`, `index`) VALUES (?, ?, ?, ?)"))
				{
					AtomicInteger slot = new AtomicInteger(1);
					
					for (ManufactureItem item : this._manufactureItems.values())
					{
						st.setInt(1, this.getObjectId());
						st.setInt(2, item.getRecipeId());
						st.setLong(3, item.getCost());
						st.setInt(4, slot.getAndIncrement());
						st.addBatch();
					}
					
					st.executeBatch();
				}
			}
			catch (Exception var12)
			{
				LOGGER.log(Level.SEVERE, "Could not store recipe shop for playerId " + this.getObjectId() + ": ", var12);
			}
		}
	}
	
	private void restoreRecipeShopList()
	{
		if (this._manufactureItems != null)
		{
			this._manufactureItems.clear();
		}
		
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM character_recipeshoplist WHERE charId=? ORDER BY `index`");)
		{
			statement.setInt(1, this.getObjectId());
			
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					this.getManufactureItems().put(rset.getInt("recipeId"), new ManufactureItem(rset.getInt("recipeId"), rset.getLong("price")));
				}
			}
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.SEVERE, "Could not restore recipe shop list data for playerId: " + this.getObjectId(), var12);
		}
	}
	
	@Override
	public float getCollisionRadius()
	{
		if (this.isMounted() && this._mountNpcId > 0)
		{
			return NpcData.getInstance().getTemplate(this.getMountNpcId()).getFCollisionRadius();
		}
		float defaultCollisionRadius = this._appearance.isFemale() ? this.getBaseTemplate().getFCollisionRadiusFemale() : this.getBaseTemplate().getFCollisionRadius();
		Transform transform = this.getTransformation();
		return transform == null ? defaultCollisionRadius : transform.getCollisionRadius(this, defaultCollisionRadius);
	}
	
	@Override
	public float getCollisionHeight()
	{
		if (this.isMounted() && this._mountNpcId > 0)
		{
			return NpcData.getInstance().getTemplate(this.getMountNpcId()).getFCollisionHeight();
		}
		float defaultCollisionHeight = this._appearance.isFemale() ? this.getBaseTemplate().getFCollisionHeightFemale() : this.getBaseTemplate().getFCollisionHeight();
		Transform transform = this.getTransformation();
		return transform == null ? defaultCollisionHeight : transform.getCollisionHeight(this, defaultCollisionHeight);
	}
	
	public int getClientX()
	{
		return this._clientX;
	}
	
	public int getClientY()
	{
		return this._clientY;
	}
	
	public int getClientZ()
	{
		return this._clientZ;
	}
	
	public int getClientHeading()
	{
		return this._clientHeading;
	}
	
	public void setClientX(int value)
	{
		this._clientX = value;
	}
	
	public void setClientY(int value)
	{
		this._clientY = value;
	}
	
	public void setClientZ(int value)
	{
		this._clientZ = value;
	}
	
	public void setClientHeading(int value)
	{
		this._clientHeading = value;
	}
	
	public boolean isFalling(int z)
	{
		if (!this.isDead() && !this.isFlying() && !this.isFlyingMounted() && !this.isInsideZone(ZoneId.WATER))
		{
			if (this._fallingTimestamp != 0L && System.currentTimeMillis() < this._fallingTimestamp)
			{
				return true;
			}
			int deltaZ = this.getZ() - z;
			if (deltaZ <= this.getBaseTemplate().getSafeFallHeight())
			{
				this._fallingTimestamp = 0L;
				return false;
			}
			else if (!GeoEngine.getInstance().hasGeo(this.getX(), this.getY()))
			{
				this._fallingTimestamp = 0L;
				return false;
			}
			else
			{
				if (this._fallingDamage == 0)
				{
					this._fallingDamage = (int) Formulas.calcFallDam(this, deltaZ);
				}
				
				if (this._fallingDamageTask != null)
				{
					this._fallingDamageTask.cancel(true);
				}
				
				this._fallingDamageTask = ThreadPool.schedule(() -> {
					if (this._fallingDamage > 0 && !this.isInvul())
					{
						this.reduceCurrentHp(Math.min(this._fallingDamage, this.getCurrentHp() - 1.0), this, null, false, true, false, false);
						SystemMessage sm = new SystemMessage(SystemMessageId.YOU_VE_RECEIVED_S1_DAMAGE_FROM_FALLING);
						sm.addInt(this._fallingDamage);
						this.sendPacket(sm);
					}
					
					this._fallingDamage = 0;
					this._fallingDamageTask = null;
				}, 1500L);
				this.sendPacket(new ValidateLocation(this));
				this.setFalling();
				return false;
			}
		}
		return false;
	}
	
	public void setFalling()
	{
		this._fallingTimestamp = System.currentTimeMillis() + 1000L;
	}
	
	public MovieHolder getMovieHolder()
	{
		return this._movieHolder;
	}
	
	public void setMovieHolder(MovieHolder movie)
	{
		this._movieHolder = movie;
	}
	
	public void updateLastItemAuctionRequest()
	{
		this._lastItemAuctionInfoRequest = System.currentTimeMillis();
	}
	
	public boolean isItemAuctionPolling()
	{
		return System.currentTimeMillis() - this._lastItemAuctionInfoRequest < 2000L;
	}
	
	@Override
	public boolean isMovementDisabled()
	{
		return super.isMovementDisabled() || this._movieHolder != null || this._fishing.isFishing();
	}
	
	public String getHtmlPrefix()
	{
		return !MultilingualSupportConfig.MULTILANG_ENABLE ? "" : this._htmlPrefix;
	}
	
	public String getLang()
	{
		return this._lang;
	}
	
	public boolean setLang(String lang)
	{
		boolean result = false;
		if (MultilingualSupportConfig.MULTILANG_ENABLE)
		{
			if (MultilingualSupportConfig.MULTILANG_ALLOWED.contains(lang))
			{
				this._lang = lang;
				result = true;
			}
			else
			{
				this._lang = MultilingualSupportConfig.MULTILANG_DEFAULT;
			}
			
			this._htmlPrefix = this._lang.equals("en") ? "" : "data/lang/" + this._lang + "/";
		}
		else
		{
			this._lang = null;
			this._htmlPrefix = "";
		}
		
		return result;
	}
	
	public long getOfflineStartTime()
	{
		return this._offlineShopStart;
	}
	
	public void setOfflineStartTime(long time)
	{
		this._offlineShopStart = time;
	}
	
	public int getPcCafePoints()
	{
		return this._pcCafePoints;
	}
	
	public void setPcCafePoints(int count)
	{
		this._pcCafePoints = count < PremiumSystemConfig.PC_CAFE_MAX_POINTS ? count : PremiumSystemConfig.PC_CAFE_MAX_POINTS;
	}
	
	public long getHonorCoins()
	{
		return this.getVariables().getLong("HONOR_COINS", 0L);
	}
	
	public void setHonorCoins(long value)
	{
		this.getVariables().set("HONOR_COINS", value);
		this.sendPacket(new ExPledgeCoinInfo(this));
	}
	
	public void checkPlayerSkills()
	{
		for (Entry<Integer, Skill> e : this.getSkills().entrySet())
		{
			SkillLearn learn = SkillTreeData.getInstance().getClassSkill(e.getKey(), e.getValue().getLevel() % 100, this.getPlayerClass());
			if (learn != null)
			{
				int levelDiff = e.getKey() == CommonSkill.EXPERTISE.getId() ? 0 : 9;
				if (this.getLevel() < learn.getGetLevel() - levelDiff)
				{
					this.deacreaseSkillLevel(e.getValue(), levelDiff);
				}
			}
		}
	}
	
	private void deacreaseSkillLevel(Skill skill, int levelDiff)
	{
		int nextLevel = -1;
		Map<Long, SkillLearn> skillTree = SkillTreeData.getInstance().getCompleteClassSkillTree(this.getPlayerClass());
		
		for (SkillLearn sl : skillTree.values())
		{
			if (sl.getSkillId() == skill.getId() && nextLevel < sl.getSkillLevel() && this.getLevel() >= sl.getGetLevel() - levelDiff)
			{
				nextLevel = sl.getSkillLevel();
			}
		}
		
		if (nextLevel == -1)
		{
			LOGGER.info("Removing skill " + skill + " from " + this);
			this.removeSkill(skill, true);
		}
		else
		{
			LOGGER.info("Decreasing skill " + skill + " to " + nextLevel + " for " + this);
			this.addSkill(SkillData.getInstance().getSkill(skill.getId(), nextLevel), true);
		}
	}
	
	public boolean canMakeSocialAction()
	{
		return this._privateStoreType == PrivateStoreType.NONE && this.getActiveRequester() == null && !this.isAlikeDead() && !this.isAllSkillsDisabled() && !this.isCastingNow() && this.getAI().getIntention() == Intention.IDLE;
	}
	
	public void setMultiSocialAction(int id, int targetId)
	{
		this._multiSociaAction = id;
		this._multiSocialTarget = targetId;
	}
	
	public int getMultiSociaAction()
	{
		return this._multiSociaAction;
	}
	
	public int getMultiSocialTarget()
	{
		return this._multiSocialTarget;
	}
	
	public int getQuestInventoryLimit()
	{
		return PlayerConfig.INVENTORY_MAXIMUM_QUEST_ITEMS;
	}
	
	public boolean canAttackCreature(Creature creature)
	{
		if (creature.isAttackable())
		{
			return true;
		}
		if (creature.isPlayable())
		{
			if (creature.isInsideZone(ZoneId.PVP) && !creature.isInsideZone(ZoneId.SIEGE))
			{
				return true;
			}
			
			Player target = creature.isSummon() ? creature.asSummon().getOwner() : creature.asPlayer();
			if (this.isInDuel() && target.isInDuel() && target.getDuelId() == this.getDuelId())
			{
				return true;
			}
			
			if (this.isInParty() && target.isInParty())
			{
				if ((this.getParty() == target.getParty()) || ((this.getParty().getCommandChannel() != null || target.getParty().getCommandChannel() != null) && this.getParty().getCommandChannel() == target.getParty().getCommandChannel()))
				{
					return false;
				}
			}
			else if (this.getClan() != null && target.getClan() != null)
			{
				if ((this.getClanId() == target.getClanId()) || ((this.getAllyId() > 0 || target.getAllyId() > 0) && this.getAllyId() == target.getAllyId()))
				{
					return false;
				}
				
				if (this.getClan().isAtWarWith(target.getClan().getId()) && target.getClan().isAtWarWith(this.getClan().getId()))
				{
					return true;
				}
			}
			else if ((this.getClan() == null || target.getClan() == null) && target.getPvpFlag() == 0 && target.getReputation() >= 0)
			{
				return false;
			}
		}
		
		return true;
	}
	
	public boolean isInventoryUnder90(boolean includeQuestInv)
	{
		return (includeQuestInv ? this._inventory.getSize() : this._inventory.getNonQuestSize()) <= this.getInventoryLimit() * 0.9;
	}
	
	public boolean isInventoryUnder80(boolean includeQuestInv)
	{
		return (includeQuestInv ? this._inventory.getSize() : this._inventory.getNonQuestSize()) <= this.getInventoryLimit() * 0.8;
	}
	
	public boolean havePetInvItems()
	{
		return this._petItems;
	}
	
	public void setPetInvItems(boolean haveit)
	{
		this._petItems = haveit;
	}
	
	private void restorePetInventoryItems()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT object_id FROM `items` WHERE `owner_id`=? AND (`loc`='PET' OR `loc`='PET_EQUIP') LIMIT 1;");)
		{
			statement.setInt(1, this.getObjectId());
			
			try (ResultSet rset = statement.executeQuery())
			{
				this.setPetInvItems(rset.next() && rset.getInt("object_id") > 0);
			}
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.SEVERE, "Could not check Items in Pet Inventory for playerId: " + this.getObjectId(), var12);
		}
	}
	
	public String getAdminConfirmCmd()
	{
		return this._adminConfirmCmd;
	}
	
	public void setAdminConfirmCmd(String adminConfirmCmd)
	{
		this._adminConfirmCmd = adminConfirmCmd;
	}
	
	private void loadRecommendations()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT rec_have, rec_left FROM character_reco_bonus WHERE charId = ?");)
		{
			statement.setInt(1, this.getObjectId());
			
			try (ResultSet rset = statement.executeQuery())
			{
				if (rset.next())
				{
					this.setRecomHave(rset.getInt("rec_have"));
					this.setRecomLeft(rset.getInt("rec_left"));
				}
			}
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.SEVERE, "Could not restore Recommendations for player: " + this.getObjectId(), var12);
		}
	}
	
	public void storeRecommendations()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("REPLACE INTO character_reco_bonus (charId,rec_have,rec_left,time_left) VALUES (?,?,?,?)");)
		{
			ps.setInt(1, this.getObjectId());
			ps.setInt(2, this._recomHave);
			ps.setInt(3, this._recomLeft);
			ps.setLong(4, 0L);
			ps.execute();
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.SEVERE, "Could not update Recommendations for player: " + this.getObjectId(), var9);
		}
	}
	
	public void startRecoGiveTask()
	{
		this._recoGiveTask = ThreadPool.scheduleAtFixedRate(new RecoGiveTask(this), 7200000L, 3600000L);
		this.storeRecommendations();
	}
	
	public void stopRecoGiveTask()
	{
		if (this._recoGiveTask != null)
		{
			this._recoGiveTask.cancel(false);
			this._recoGiveTask = null;
		}
	}
	
	public boolean isRecoTwoHoursGiven()
	{
		return this._recoTwoHoursGiven;
	}
	
	public void setRecoTwoHoursGiven(boolean value)
	{
		this._recoTwoHoursGiven = value;
	}
	
	public void setPremiumStatus(boolean premiumStatus)
	{
		this._premiumStatus = premiumStatus;
		this.sendPacket(new ExBrPremiumState(this));
	}
	
	public boolean hasPremiumStatus()
	{
		return PremiumSystemConfig.PREMIUM_SYSTEM_ENABLED && this._premiumStatus;
	}
	
	public void setLastPetitionGmName(String gmName)
	{
		this._lastPetitionGmName = gmName;
	}
	
	public String getLastPetitionGmName()
	{
		return this._lastPetitionGmName;
	}
	
	public ContactList getContactList()
	{
		return this._contactList;
	}
	
	public long getNotMoveUntil()
	{
		return this._notMoveUntil;
	}
	
	public void updateNotMoveUntil()
	{
		this._notMoveUntil = System.currentTimeMillis() + PlayerConfig.PLAYER_MOVEMENT_BLOCK_TIME;
	}
	
	@Override
	public boolean isPlayer()
	{
		return true;
	}
	
	@Override
	public Player asPlayer()
	{
		return this;
	}
	
	public Skill getCustomSkill(int skillId)
	{
		return this._customSkills != null ? this._customSkills.get(skillId) : null;
	}
	
	private void addCustomSkill(Skill skill)
	{
		if (skill != null && skill.getDisplayId() != skill.getId())
		{
			if (this._customSkills == null)
			{
				this._customSkills = new ConcurrentSkipListMap<>();
			}
			
			this._customSkills.put(skill.getDisplayId(), skill);
		}
	}
	
	private void removeCustomSkill(Skill skill)
	{
		if (skill != null && this._customSkills != null && skill.getDisplayId() != skill.getId())
		{
			this._customSkills.remove(skill.getDisplayId());
		}
	}
	
	@Override
	public boolean canRevive()
	{
		return this._canRevive;
	}
	
	@Override
	public void setCanRevive(boolean value)
	{
		this._canRevive = value;
	}
	
	public boolean isRegisteredOnEvent()
	{
		return this._isRegisteredOnEvent || this._isOnEvent;
	}
	
	public void setRegisteredOnEvent(boolean value)
	{
		this._isRegisteredOnEvent = value;
	}
	
	@Override
	public boolean isOnEvent()
	{
		return this._isOnEvent;
	}
	
	public void setOnEvent(boolean value)
	{
		this._isOnEvent = value;
	}
	
	public boolean isOnSoloEvent()
	{
		return this._isOnSoloEvent;
	}
	
	public void setOnSoloEvent(boolean value)
	{
		this._isOnSoloEvent = value;
	}
	
	public boolean isBlockedFromDeathPenalty()
	{
		return this._isOnEvent || this.isAffected(EffectFlag.PROTECT_DEATH_PENALTY);
	}
	
	public void setOriginalCpHpMp(double cp, double hp, double mp)
	{
		this._originalCp = cp;
		this._originalHp = hp;
		this._originalMp = mp;
	}
	
	public boolean hasVariables()
	{
		return this.getScript(PlayerVariables.class) != null;
	}
	
	public PlayerVariables getVariables()
	{
		PlayerVariables vars = this.getScript(PlayerVariables.class);
		return vars != null ? vars : this.addScript(new PlayerVariables(this.getObjectId()));
	}
	
	public boolean hasAccountVariables()
	{
		return this.getScript(AccountVariables.class) != null;
	}
	
	public AccountVariables getAccountVariables()
	{
		AccountVariables vars = this.getScript(AccountVariables.class);
		return vars != null ? vars : this.addScript(new AccountVariables(this.getAccountName()));
	}
	
	@Override
	public int getId()
	{
		return this.getPlayerClass().getId();
	}
	
	public boolean isPartyBanned()
	{
		return PunishmentManager.getInstance().hasPunishment(this.getObjectId(), PunishmentAffect.CHARACTER, PunishmentType.PARTY_BAN);
	}
	
	public boolean addAction(PlayerAction act)
	{
		if (!this.hasAction(act))
		{
			this._actionMask = this._actionMask | act.getMask();
			return true;
		}
		return false;
	}
	
	public boolean removeAction(PlayerAction act)
	{
		if (this.hasAction(act))
		{
			this._actionMask = this._actionMask & ~act.getMask();
			return true;
		}
		return false;
	}
	
	public boolean hasAction(PlayerAction act)
	{
		return (this._actionMask & act.getMask()) == act.getMask();
	}
	
	public void setCharmOfCourage(boolean value)
	{
		this._hasCharmOfCourage = value;
	}
	
	public boolean hasCharmOfCourage()
	{
		return this._hasCharmOfCourage;
	}
	
	public boolean isGood()
	{
		return this._isGood;
	}
	
	public boolean isEvil()
	{
		return this._isEvil;
	}
	
	public void setGood()
	{
		this._isGood = true;
		this._isEvil = false;
	}
	
	public void setEvil()
	{
		this._isGood = false;
		this._isEvil = true;
	}
	
	public boolean atWarWith(Playable target)
	{
		if (target == null)
		{
			return false;
		}
		return this._clan != null && !this.isAcademyMember() && target.getClan() != null && !target.isAcademyMember() ? this._clan.isAtWarWith(target.getClan()) : false;
	}
	
	public void setVisualHair(int hairId)
	{
		this.getVariables().set("visualHairId", hairId);
	}
	
	public void setVisualHairColor(int colorId)
	{
		this.getVariables().set("visualHairColorId", colorId);
	}
	
	public void setVisualFace(int faceId)
	{
		this.getVariables().set("visualFaceId", faceId);
	}
	
	public int getVisualHair()
	{
		return this.getVariables().getInt("visualHairId", this._appearance.getHairStyle());
	}
	
	public int getVisualHairColor()
	{
		return this.getVariables().getInt("visualHairColorId", this._appearance.getHairColor());
	}
	
	public int getVisualFace()
	{
		return this.getVariables().getInt("visualFaceId", this._appearance.getFace());
	}
	
	public boolean isMentor()
	{
		return MentorManager.getInstance().isMentor(this.getObjectId());
	}
	
	public boolean isMentee()
	{
		return MentorManager.getInstance().isMentee(this.getObjectId());
	}
	
	public int getAbilityPoints()
	{
		return this.getVariables().getInt(this.isDualClassActive() ? "ABILITY_POINTS_DUAL_CLASS" : "ABILITY_POINTS", 0);
	}
	
	public void setAbilityPoints(int points)
	{
		this.getVariables().set(this.isDualClassActive() ? "ABILITY_POINTS_DUAL_CLASS" : "ABILITY_POINTS", points);
	}
	
	public int getAbilityPointsUsed()
	{
		return this.getVariables().getInt(this.isDualClassActive() ? "ABILITY_POINTS_DUAL_CLASS_USED" : "ABILITY_POINTS_USED", 0);
	}
	
	public void setAbilityPointsUsed(int points)
	{
		if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_ABILITY_POINTS_CHANGED, this))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerAbilityPointsChanged(this, this.getAbilityPointsUsed(), points), this);
		}
		
		this.getVariables().set(this.isDualClassActive() ? "ABILITY_POINTS_DUAL_CLASS_USED" : "ABILITY_POINTS_USED", points);
	}
	
	public int getWorldChatPoints()
	{
		return (int) ((GeneralConfig.WORLD_CHAT_POINTS_PER_DAY + this.getStat().getAdd(Stat.WORLD_CHAT_POINTS, 0.0)) * this.getStat().getMul(Stat.WORLD_CHAT_POINTS, 1.0));
	}
	
	public int getWorldChatUsed()
	{
		return this.getVariables().getInt("WORLD_CHAT_USED", 0);
	}
	
	public void setWorldChatUsed(int timesUsed)
	{
		this.getVariables().set("WORLD_CHAT_USED", timesUsed);
	}
	
	public CastleSide getPlayerSide()
	{
		if (this._clan == null)
		{
			return CastleSide.NEUTRAL;
		}
		int castleId = this._clan.getCastleId();
		if (castleId == 0)
		{
			return CastleSide.NEUTRAL;
		}
		Castle castle = CastleManager.getInstance().getCastleById(castleId);
		return castle == null ? CastleSide.NEUTRAL : castle.getSide();
	}
	
	public boolean isOnDarkSide()
	{
		return this.getPlayerSide() == CastleSide.DARK;
	}
	
	public boolean isOnLightSide()
	{
		return this.getPlayerSide() == CastleSide.LIGHT;
	}
	
	public int getMaxSummonPoints()
	{
		return (int) this.getStat().getValue(Stat.MAX_SUMMON_POINTS, 0.0);
	}
	
	public int getSummonPoints()
	{
		int totalPoints = 0;
		
		for (Summon summon : this.getServitors().values())
		{
			totalPoints += summon.getSummonPoints();
		}
		
		return totalPoints;
	}
	
	public boolean addRequest(AbstractRequest request)
	{
		return this.canRequest(request) && this._requests.putIfAbsent(request.getClass(), request) == null;
	}
	
	public boolean canRequest(AbstractRequest request)
	{
		for (AbstractRequest r : this._requests.values())
		{
			if (!request.canWorkWith(r))
			{
				return false;
			}
		}
		
		return true;
	}
	
	public boolean removeRequest(Class<? extends AbstractRequest> clazz)
	{
		return this._requests.remove(clazz) != null;
	}
	
	public <T extends AbstractRequest> T getRequest(Class<T> requestClass)
	{
		return requestClass.cast(this._requests.get(requestClass));
	}
	
	public boolean hasRequests()
	{
		return !this._requests.isEmpty();
	}
	
	public boolean hasItemRequest()
	{
		for (AbstractRequest request : this._requests.values())
		{
			if (request.isItemRequest())
			{
				return true;
			}
		}
		
		return false;
	}
	
	@SafeVarargs
	public final boolean hasRequest(Class<? extends AbstractRequest> requestClass, Class<? extends AbstractRequest>... classes)
	{
		for (Class<? extends AbstractRequest> clazz : classes)
		{
			if (this._requests.containsKey(clazz))
			{
				return true;
			}
		}
		
		return this._requests.containsKey(requestClass);
	}
	
	public boolean isProcessingItem(int objectId)
	{
		for (AbstractRequest request : this._requests.values())
		{
			if (request.isUsing(objectId))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public void removeRequestsThatProcessesItem(int objectId)
	{
		this._requests.values().removeIf(req -> req.isUsing(objectId));
	}
	
	public int getPrimePoints()
	{
		return this.getAccountVariables().getInt("PRIME_POINTS", 0);
	}
	
	public void setPrimePoints(int points)
	{
		AccountVariables vars = this.getAccountVariables();
		vars.set("PRIME_POINTS", Math.max(points, 0));
		vars.storeMe();
	}
	
	private TerminateReturn onExperienceReceived()
	{
		return this.isDead() ? new TerminateReturn(false, false, false) : new TerminateReturn(true, true, true);
	}
	
	public void disableExpGain()
	{
		this.addListener(new FunctionEventListener(this, EventType.ON_PLAYABLE_EXP_CHANGED, _ -> this.onExperienceReceived(), this));
	}
	
	public void enableExpGain()
	{
		this.removeListenerIf(EventType.ON_PLAYABLE_EXP_CHANGED, listener -> listener.getOwner() == this);
	}
	
	public Map<Integer, ExResponseCommissionInfo> getLastCommissionInfos()
	{
		return this._lastCommissionInfos;
	}
	
	public Set<Integer> getWhisperers()
	{
		return this._whisperers;
	}
	
	public MatchingRoom getMatchingRoom()
	{
		return this._matchingRoom;
	}
	
	public void setMatchingRoom(MatchingRoom matchingRoom)
	{
		this._matchingRoom = matchingRoom;
	}
	
	public boolean isInMatchingRoom()
	{
		return this._matchingRoom != null;
	}
	
	public int getVitalityItemsUsed()
	{
		return this.getVariables().getInt("VITALITY_ITEMS_USED", 0);
	}
	
	public void setVitalityItemsUsed(int used)
	{
		PlayerVariables vars = this.getVariables();
		vars.set("VITALITY_ITEMS_USED", used);
		vars.storeMe();
	}
	
	@Override
	public boolean isVisibleFor(Player player)
	{
		return super.isVisibleFor(player) || player.getParty() != null && player.getParty() == this.getParty();
	}
	
	public void setQuestZoneId(int id)
	{
		this._questZoneId = id;
	}
	
	public int getQuestZoneId()
	{
		return this._questZoneId;
	}
	
	public void sendInventoryUpdate(InventoryUpdate iu)
	{
		if (this._inventoryUpdateTask != null)
		{
			this._inventoryUpdateTask.cancel(false);
		}
		
		this._inventoryUpdate.putAll(iu.getItemEntries());
		this._inventoryUpdateTask = ThreadPool.schedule(() -> {
			this.sendPacket(this._inventoryUpdate);
			this.updateAdenaAndWeight();
		}, 100L);
	}
	
	public void sendItemList()
	{
		if (this._itemListTask != null)
		{
			this._itemListTask.cancel(false);
		}
		
		this._itemListTask = ThreadPool.schedule(() -> {
			this.sendPacket(new ItemList(1, this));
			this.sendPacket(new ItemList(2, this));
			this.sendPacket(new ExQuestItemList(1, this));
			this.sendPacket(new ExQuestItemList(2, this));
			this.updateAdenaAndWeight();
		}, 250L);
	}
	
	public void updateAdenaAndWeight()
	{
		if (this._adenaAndWeightTask != null)
		{
			this._adenaAndWeightTask.cancel(false);
		}
		
		this._adenaAndWeightTask = ThreadPool.schedule(() -> {
			this.sendPacket(new ExAdenaInvenCount(this));
			this.sendPacket(new ExUserInfoInvenWeight(this));
		}, 800L);
	}
	
	public Fishing getFishing()
	{
		return this._fishing;
	}
	
	public boolean isFishing()
	{
		return this._fishing.isFishing();
	}
	
	@Override
	public MoveType getMoveType()
	{
		return this._waitTypeSitting ? MoveType.SITTING : super.getMoveType();
	}
	
	public void stopAllTasks()
	{
		if (this._mountFeedTask != null && !this._mountFeedTask.isDone() && !this._mountFeedTask.isCancelled())
		{
			this._mountFeedTask.cancel(false);
			this._mountFeedTask = null;
		}
		
		if (this._dismountTask != null && !this._dismountTask.isDone() && !this._dismountTask.isCancelled())
		{
			this._dismountTask.cancel(false);
			this._dismountTask = null;
		}
		
		if (this._fameTask != null && !this._fameTask.isDone() && !this._fameTask.isCancelled())
		{
			this._fameTask.cancel(false);
			this._fameTask = null;
		}
		
		if (this._teleportWatchdog != null && !this._teleportWatchdog.isDone() && !this._teleportWatchdog.isCancelled())
		{
			this._teleportWatchdog.cancel(false);
			this._teleportWatchdog = null;
		}
		
		if (this._recoGiveTask != null && !this._recoGiveTask.isDone() && !this._recoGiveTask.isCancelled())
		{
			this._recoGiveTask.cancel(false);
			this._recoGiveTask = null;
		}
		
		if (this._chargeTask != null && !this._chargeTask.isDone() && !this._chargeTask.isCancelled())
		{
			this._chargeTask.cancel(false);
			this._chargeTask = null;
		}
		
		if (this._soulTask != null && !this._soulTask.isDone() && !this._soulTask.isCancelled())
		{
			this._soulTask.cancel(false);
			this._soulTask = null;
		}
		
		if (this._taskRentPet != null && !this._taskRentPet.isDone() && !this._taskRentPet.isCancelled())
		{
			this._taskRentPet.cancel(false);
			this._taskRentPet = null;
		}
		
		if (this._taskWater != null && !this._taskWater.isDone() && !this._taskWater.isCancelled())
		{
			this._taskWater.cancel(false);
			this._taskWater = null;
		}
		
		if (this._fallingDamageTask != null && !this._fallingDamageTask.isDone() && !this._fallingDamageTask.isCancelled())
		{
			this._fallingDamageTask.cancel(false);
			this._fallingDamageTask = null;
		}
		
		if (this._timedHuntingZoneTask != null && !this._timedHuntingZoneTask.isDone() && !this._timedHuntingZoneTask.isCancelled())
		{
			this._timedHuntingZoneTask.cancel(false);
			this._timedHuntingZoneTask = null;
		}
		
		if (this._taskWarnUserTakeBreak != null && !this._taskWarnUserTakeBreak.isDone() && !this._taskWarnUserTakeBreak.isCancelled())
		{
			this._taskWarnUserTakeBreak.cancel(false);
			this._taskWarnUserTakeBreak = null;
		}
		
		if (this._onlineTimeUpdateTask != null && !this._onlineTimeUpdateTask.isDone() && !this._onlineTimeUpdateTask.isCancelled())
		{
			this._onlineTimeUpdateTask.cancel(false);
			this._onlineTimeUpdateTask = null;
		}
		
		for (Entry<Integer, ScheduledFuture<?>> entry : this._hennaRemoveSchedules.entrySet())
		{
			ScheduledFuture<?> task = entry.getValue();
			if (task != null && !task.isCancelled() && !task.isDone())
			{
				task.cancel(false);
			}
			
			this._hennaRemoveSchedules.remove(entry.getKey());
		}
		
		synchronized (this._questTimers)
		{
			for (QuestTimer timer : this._questTimers)
			{
				timer.cancelTask();
			}
			
			this._questTimers.clear();
		}
		
		synchronized (this._timerHolders)
		{
			for (TimerHolder<?> timer : this._timerHolders)
			{
				timer.cancelTask();
			}
			
			this._timerHolders.clear();
		}
	}
	
	public void addQuestTimer(QuestTimer questTimer)
	{
		synchronized (this._questTimers)
		{
			this._questTimers.add(questTimer);
		}
	}
	
	public void removeQuestTimer(QuestTimer questTimer)
	{
		synchronized (this._questTimers)
		{
			this._questTimers.remove(questTimer);
		}
	}
	
	public void addTimerHolder(TimerHolder<?> timer)
	{
		synchronized (this._timerHolders)
		{
			this._timerHolders.add(timer);
		}
	}
	
	public void removeTimerHolder(TimerHolder<?> timer)
	{
		synchronized (this._timerHolders)
		{
			this._timerHolders.remove(timer);
		}
	}
	
	private void startOnlineTimeUpdateTask()
	{
		if (this._onlineTimeUpdateTask != null)
		{
			this.stopOnlineTimeUpdateTask();
		}
		
		this._onlineTimeUpdateTask = ThreadPool.scheduleAtFixedRate(this::updateOnlineTime, 60000L, 60000L);
	}
	
	private void updateOnlineTime()
	{
		if (this._clan != null)
		{
			this._clan.addMemberOnlineTime(this);
		}
	}
	
	private void stopOnlineTimeUpdateTask()
	{
		if (this._onlineTimeUpdateTask != null)
		{
			this._onlineTimeUpdateTask.cancel(true);
			this._onlineTimeUpdateTask = null;
		}
	}
	
	public GroupType getGroupType()
	{
		return this.isInParty() ? (this._party.isInCommandChannel() ? GroupType.COMMAND_CHANNEL : GroupType.PARTY) : GroupType.NONE;
	}
	
	public boolean isTrueHero()
	{
		return this._trueHero;
	}
	
	public void setTrueHero(boolean value)
	{
		this._trueHero = value;
	}
	
	@Override
	protected void initStatusUpdateCache()
	{
		super.initStatusUpdateCache();
		this.addStatusUpdateValue(StatusUpdateType.LEVEL);
		this.addStatusUpdateValue(StatusUpdateType.MAX_CP);
		this.addStatusUpdateValue(StatusUpdateType.CUR_CP);
		if (this._isDeathKnight)
		{
			this.addStatusUpdateValue(StatusUpdateType.CUR_DP);
		}
		else if (this._isVanguard)
		{
			this.addStatusUpdateValue(StatusUpdateType.CUR_BP);
		}
		else if (this._isAssassin)
		{
			this.addStatusUpdateValue(StatusUpdateType.CUR_AP);
		}
	}
	
	public TrainingHolder getTraingCampInfo()
	{
		String info = this.getAccountVariables().getString("TRAINING_CAMP", null);
		return info == null ? null : new TrainingHolder(Integer.parseInt(info.split(";")[0]), Integer.parseInt(info.split(";")[1]), Integer.parseInt(info.split(";")[2]), Long.parseLong(info.split(";")[3]), Long.parseLong(info.split(";")[4]));
	}
	
	public void setTraingCampInfo(TrainingHolder holder)
	{
		this.getAccountVariables().set("TRAINING_CAMP", holder.getObjectId() + ";" + holder.getClassIndex() + ";" + holder.getLevel() + ";" + holder.getStartTime() + ";" + holder.getEndTime());
	}
	
	public void removeTraingCampInfo()
	{
		this.getAccountVariables().remove("TRAINING_CAMP");
	}
	
	public long getTraingCampDuration()
	{
		return this.getAccountVariables().getLong("TRAINING_CAMP_DURATION", 0L);
	}
	
	public void setTraingCampDuration(long duration)
	{
		this.getAccountVariables().set("TRAINING_CAMP_DURATION", duration);
	}
	
	public void resetTraingCampDuration()
	{
		this.getAccountVariables().remove("TRAINING_CAMP_DURATION");
	}
	
	public boolean isInTraingCamp()
	{
		TrainingHolder trainingHolder = this.getTraingCampInfo();
		return trainingHolder != null && trainingHolder.getEndTime() > System.currentTimeMillis();
	}
	
	public AttendanceInfoHolder getAttendanceInfo()
	{
		Calendar calendar = Calendar.getInstance();
		if (calendar.get(11) < 6 && calendar.get(12) < 30)
		{
			calendar.add(5, -1);
		}
		
		calendar.set(11, 6);
		calendar.set(12, 30);
		calendar.set(13, 0);
		calendar.set(14, 0);
		long receiveDate;
		int rewardIndex;
		if (AttendanceRewardsConfig.ATTENDANCE_REWARDS_SHARE_ACCOUNT)
		{
			receiveDate = this.getAccountVariables().getLong("ATTENDANCE_DATE", 0L);
			rewardIndex = this.getAccountVariables().getInt("ATTENDANCE_INDEX", 0);
		}
		else
		{
			receiveDate = this.getVariables().getLong("ATTENDANCE_DATE", 0L);
			rewardIndex = this.getVariables().getInt("ATTENDANCE_INDEX", 0);
		}
		
		boolean canBeRewarded = false;
		if (calendar.getTimeInMillis() > receiveDate)
		{
			canBeRewarded = true;
			if (rewardIndex >= AttendanceRewardData.getInstance().getRewardsCount())
			{
				rewardIndex = 0;
			}
		}
		
		return new AttendanceInfoHolder(rewardIndex, canBeRewarded);
	}
	
	public void setAttendanceInfo(int rewardIndex)
	{
		Calendar nextReward = Calendar.getInstance();
		nextReward.set(12, 30);
		if (nextReward.get(11) >= 6)
		{
			nextReward.add(5, 1);
		}
		
		nextReward.set(11, 6);
		if (AttendanceRewardsConfig.ATTENDANCE_REWARDS_SHARE_ACCOUNT)
		{
			this.getAccountVariables().set("ATTENDANCE_DATE", nextReward.getTimeInMillis());
			this.getAccountVariables().set("ATTENDANCE_INDEX", rewardIndex);
		}
		else
		{
			this.getVariables().set("ATTENDANCE_DATE", nextReward.getTimeInMillis());
			this.getVariables().set("ATTENDANCE_INDEX", rewardIndex);
		}
	}
	
	public int getAttendanceDelay()
	{
		long currentTime = System.currentTimeMillis();
		long remainingTime = this._attendanceDelay - currentTime;
		int remainingSeconds = (int) (remainingTime / 1000L);
		return Math.max(remainingSeconds, 0);
	}
	
	public void setAttendanceDelay(int timeInMinutes)
	{
		long currentTime = System.currentTimeMillis();
		this._attendanceDelay = currentTime + timeInMinutes * 60 * 1000;
	}
	
	public byte getVipTier()
	{
		return this._vipTier;
	}
	
	public void setVipTier(byte vipTier)
	{
		this._vipTier = vipTier;
	}
	
	public long getVipPoints()
	{
		return this.getAccountVariables().getLong("VipPoints", 0L);
	}
	
	public long getVipTierExpiration()
	{
		return this.getAccountVariables().getLong("VipExpiration", 0L);
	}
	
	public void setVipTierExpiration(long expiration)
	{
		this.getAccountVariables().set("VipExpiration", expiration);
	}
	
	public void updateVipPoints(long points)
	{
		if (points != 0L)
		{
			int currentVipTier = VipManager.getInstance().getVipTier(this.getVipPoints());
			this.getAccountVariables().set("VipPoints", this.getVipPoints() + points);
			byte newTier = VipManager.getInstance().getVipTier(this.getVipPoints());
			if (newTier != currentVipTier)
			{
				this._vipTier = newTier;
				if (newTier > 0)
				{
					this.getAccountVariables().set("VipExpiration", Instant.now().plus(30L, ChronoUnit.DAYS).toEpochMilli());
					VipManager.getInstance().manageTier(this);
				}
				else
				{
					this.getAccountVariables().set("VipExpiration", 0L);
				}
			}
			
			this.getAccountVariables().storeMe();
			this.sendPacket(new ReceiveVipInfo(this));
		}
	}
	
	public void initElementalSpirits()
	{
		this.tryLoadSpirits();
		if (this._spirits == null)
		{
			ElementalSpiritType[] types = ElementalSpiritType.values();
			this._spirits = new ElementalSpirit[types.length - 1];
			
			for (ElementalSpiritType type : types)
			{
				if (ElementalSpiritType.NONE != type)
				{
					ElementalSpirit spirit = new ElementalSpirit(type, this);
					this._spirits[type.getId() - 1] = spirit;
					spirit.save();
				}
			}
		}
		
		if (this._activeElementalSpiritType == null)
		{
			this.changeElementalSpirit(ElementalSpiritType.FIRE.getId());
		}
	}
	
	private void tryLoadSpirits()
	{
		List<ElementalSpiritDataHolder> restoredSpirits = new ArrayList<>();
		
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement("SELECT * FROM character_spirits WHERE charId=?");)
		{
			stmt.setInt(1, this.getObjectId());
			
			try (ResultSet rset = stmt.executeQuery())
			{
				while (rset.next())
				{
					ElementalSpiritDataHolder newHolder = new ElementalSpiritDataHolder();
					newHolder.setCharId(rset.getInt("charId"));
					byte type = rset.getByte("type");
					newHolder.setType(type);
					byte level = rset.getByte("level");
					newHolder.setLevel(level);
					byte stage = rset.getByte("stage");
					newHolder.setStage(stage);
					long experience = Math.min(rset.getLong("experience"), ElementalSpiritData.getInstance().getSpirit(type, stage).getMaxExperienceAtLevel(level));
					newHolder.setExperience(experience);
					newHolder.setAttackPoints(rset.getByte("attack_points"));
					newHolder.setDefensePoints(rset.getByte("defense_points"));
					newHolder.setCritRatePoints(rset.getByte("crit_rate_points"));
					newHolder.setCritDamagePoints(rset.getByte("crit_damage_points"));
					newHolder.setInUse(rset.getByte("in_use") == 1);
					restoredSpirits.add(newHolder);
				}
			}
		}
		catch (SQLException var17)
		{
			var17.printStackTrace();
		}
		
		if (!restoredSpirits.isEmpty())
		{
			this._spirits = new ElementalSpirit[ElementalSpiritType.values().length - 1];
			
			for (ElementalSpiritDataHolder spiritData : restoredSpirits)
			{
				this._spirits[spiritData.getType() - 1] = new ElementalSpirit(spiritData, this);
				if (spiritData.isInUse())
				{
					this._activeElementalSpiritType = ElementalSpiritType.of(spiritData.getType());
				}
			}
			
			ThreadPool.schedule(() -> {
				this.sendPacket(new ElementalSpiritInfo(this, (byte) 0));
				this.sendPacket(new ExElementalSpiritAttackType(this));
			}, 4000L);
		}
	}
	
	public double getFireSpiritAttack()
	{
		return this.getElementalSpiritAttackOf(ElementalSpiritType.FIRE);
	}
	
	public double getWaterSpiritAttack()
	{
		return this.getElementalSpiritAttackOf(ElementalSpiritType.WATER);
	}
	
	public double getWindSpiritAttack()
	{
		return this.getElementalSpiritAttackOf(ElementalSpiritType.WIND);
	}
	
	public double getEarthSpiritAttack()
	{
		return this.getElementalSpiritAttackOf(ElementalSpiritType.EARTH);
	}
	
	public double getFireSpiritDefense()
	{
		return this.getElementalSpiritDefenseOf(ElementalSpiritType.FIRE);
	}
	
	public double getWaterSpiritDefense()
	{
		return this.getElementalSpiritDefenseOf(ElementalSpiritType.WATER);
	}
	
	public double getWindSpiritDefense()
	{
		return this.getElementalSpiritDefenseOf(ElementalSpiritType.WIND);
	}
	
	public double getEarthSpiritDefense()
	{
		return this.getElementalSpiritDefenseOf(ElementalSpiritType.EARTH);
	}
	
	public double getActiveElementalSpiritAttack()
	{
		ElementalSpirit spirit = this.getElementalSpirit(this._activeElementalSpiritType);
		int attackValue = spirit != null ? spirit.getAttack() : 0;
		return this.getStat().getElementalSpiritPower(this._activeElementalSpiritType, attackValue);
	}
	
	@Override
	public double getElementalSpiritDefenseOf(ElementalSpiritType type)
	{
		ElementalSpirit spirit = this.getElementalSpirit(type);
		int defenseValue = spirit != null ? spirit.getDefense() : 0;
		return this.getStat().getElementalSpiritDefense(type, defenseValue);
	}
	
	@Override
	public double getElementalSpiritAttackOf(ElementalSpiritType type)
	{
		ElementalSpirit spirit = this.getElementalSpirit(type);
		int attackValue = spirit != null ? spirit.getAttack() : 0;
		return this.getStat().getElementSpiritAttack(type, attackValue);
	}
	
	public double getElementalSpiritCritRate()
	{
		ElementalSpirit spirit = this.getElementalSpirit(this._activeElementalSpiritType);
		int critRateValue = spirit != null ? spirit.getCriticalRate() : 0;
		return this.getStat().getElementalSpiritCriticalRate(critRateValue);
	}
	
	public double getElementalSpiritCritDamage()
	{
		ElementalSpirit spirit = this.getElementalSpirit(this._activeElementalSpiritType);
		double critDamageValue = spirit != null ? spirit.getCriticalDamage() : 0.0;
		return this.getStat().getElementalSpiritCriticalDamage(critDamageValue);
	}
	
	public double getElementalSpiritXpBonus()
	{
		return this.getStat().getElementalSpiritXpBonus();
	}
	
	public ElementalSpirit getElementalSpirit(ElementalSpiritType type)
	{
		return this._spirits != null && type != null && type != ElementalSpiritType.NONE ? this._spirits[type.getId() - 1] : null;
	}
	
	public byte getActiveElementalSpiritType()
	{
		return this._activeElementalSpiritType != null ? this._activeElementalSpiritType.getId() : 0;
	}
	
	public void changeElementalSpirit(byte element)
	{
		this._activeElementalSpiritType = ElementalSpiritType.of(element);
		if (this._spirits != null)
		{
			for (ElementalSpirit spirit : this._spirits)
			{
				if (spirit != null)
				{
					spirit.setInUse(spirit.getType() == element);
					this.sendPacket(new ExElementalSpiritAttackType(this));
				}
			}
		}
		
		UserInfo userInfo = new UserInfo(this, false);
		userInfo.addComponentType(UserInfoType.ATT_SPIRITS);
		this.sendPacket(userInfo);
	}
	
	public ElementalSpirit[] getSpirits()
	{
		return this._spirits;
	}
	
	public boolean isInBattle()
	{
		return AttackStanceTaskManager.getInstance().hasAttackStanceTask(this);
	}
	
	public AutoPlaySettingsHolder getAutoPlaySettings()
	{
		return this._autoPlaySettings;
	}
	
	public AutoUseSettingsHolder getAutoUseSettings()
	{
		return this._autoUseSettings;
	}
	
	public void setAutoPlaying(boolean value)
	{
		this._autoPlaying.set(value);
		if (!value && this._offlinePlay && OfflinePlayConfig.RESTORE_AUTO_PLAY_OFFLINERS)
		{
			OfflinePlayTable.getInstance().removeOfflinePlay(this);
		}
	}
	
	public boolean isAutoPlaying()
	{
		return this._autoPlaying.get();
	}
	
	public void setResumedAutoPlay(boolean value)
	{
		this._resumedAutoPlay = value;
	}
	
	public boolean hasResumedAutoPlay()
	{
		return this._resumedAutoPlay;
	}
	
	public void restoreAutoSettings()
	{
		if (GeneralConfig.ENABLE_AUTO_PLAY && this.getVariables().contains("AUTO_USE_SETTINGS"))
		{
			List<Integer> settings = this.getVariables().getIntegerList("AUTO_USE_SETTINGS");
			if (!settings.isEmpty())
			{
				int options = settings.get(0);
				boolean active = GeneralConfig.RESUME_AUTO_PLAY && settings.get(1) == 1;
				boolean pickUp = settings.get(2) == 1;
				int nextTargetMode = settings.get(3);
				boolean shortRange = settings.get(4) == 1;
				int potionPercent = settings.get(5);
				boolean respectfulHunting = settings.get(6) == 1;
				int petPotionPercent = settings.size() < 8 ? 0 : settings.get(7);
				this.getAutoPlaySettings().setAutoPotionPercent(potionPercent);
				this.getAutoPlaySettings().setOptions(options);
				this.getAutoPlaySettings().setPickup(pickUp);
				this.getAutoPlaySettings().setNextTargetMode(nextTargetMode);
				this.getAutoPlaySettings().setShortRange(shortRange);
				this.getAutoPlaySettings().setRespectfulHunting(respectfulHunting);
				this.getAutoPlaySettings().setAutoPetPotionPercent(petPotionPercent);
				this.sendPacket(new ExAutoPlaySettingSend(options, active, pickUp, nextTargetMode, shortRange, potionPercent, respectfulHunting, petPotionPercent));
				if (active)
				{
					AutoPlayTaskManager.getInstance().startAutoPlay(this);
				}
				
				this._resumedAutoPlay = true;
			}
		}
	}
	
	public void restoreAutoShortcutVisual()
	{
		if (this.getVariables().contains("AUTO_USE_SHORTCUTS"))
		{
			List<Integer> positions = this.getVariables().getIntegerList("AUTO_USE_SHORTCUTS");
			
			for (Shortcut shortcut : this.getAllShortcuts())
			{
				Integer position = shortcut.getSlot() + shortcut.getPage() * 12;
				if (positions.contains(position))
				{
					if (shortcut.getType() == ShortcutType.SKILL)
					{
						Skill knownSkill = this.getKnownSkill(shortcut.getId());
						if (knownSkill != null)
						{
							shortcut.setAutoUse(true);
						}
					}
					else if (shortcut.getType() == ShortcutType.ACTION)
					{
						shortcut.setAutoUse(true);
					}
					else
					{
						Item item = this.getInventory().getItemByObjectId(shortcut.getId());
						if (item != null)
						{
							shortcut.setAutoUse(true);
						}
					}
				}
			}
		}
	}
	
	public void restoreAutoShortcuts()
	{
		if (this.getVariables().contains("AUTO_USE_SHORTCUTS"))
		{
			List<Integer> positions = this.getVariables().getIntegerList("AUTO_USE_SHORTCUTS");
			
			for (Shortcut shortcut : this.getAllShortcuts())
			{
				Integer position = shortcut.getSlot() + shortcut.getPage() * 12;
				if (positions.contains(position))
				{
					if (shortcut.getType() == ShortcutType.ACTION)
					{
						shortcut.setAutoUse(true);
						AutoUseTaskManager.getInstance().addAutoAction(this, shortcut.getId());
					}
					else
					{
						Skill knownSkill = this.getKnownSkill(shortcut.getId());
						if (knownSkill != null)
						{
							shortcut.setAutoUse(true);
							if (knownSkill.hasNegativeEffect())
							{
								AutoUseTaskManager.getInstance().addAutoSkill(this, shortcut.getId());
							}
							else
							{
								AutoUseTaskManager.getInstance().addAutoBuff(this, shortcut.getId());
							}
						}
						else
						{
							Item item = this.getInventory().getItemByObjectId(shortcut.getId());
							if (item != null)
							{
								shortcut.setAutoUse(true);
								if (item.isPotion())
								{
									AutoUseTaskManager.getInstance().setAutoPotionItem(this, item.getId());
								}
								else
								{
									AutoUseTaskManager.getInstance().addAutoSupplyItem(this, item.getId());
								}
							}
						}
					}
				}
			}
		}
	}
	
	public synchronized void addAutoShortcut(int slot, int page)
	{
		List<Integer> positions = this.getVariables().getIntegerList("AUTO_USE_SHORTCUTS");
		Shortcut usedShortcut = this.getShortcut(slot, page);
		if (usedShortcut == null)
		{
			Integer position = slot + page * 12;
			positions.remove(position);
		}
		else
		{
			for (Shortcut shortcut : this.getAllShortcuts())
			{
				if (usedShortcut.getId() == shortcut.getId() && usedShortcut.getType() == shortcut.getType())
				{
					shortcut.setAutoUse(true);
					this.sendPacket(new ExActivateAutoShortcut(shortcut, true));
					Integer position = shortcut.getSlot() + shortcut.getPage() * 12;
					if (!positions.contains(position))
					{
						positions.add(position);
					}
				}
			}
		}
		
		this.getVariables().setIntegerList("AUTO_USE_SHORTCUTS", positions);
	}
	
	public synchronized void removeAutoShortcut(int slot, int page)
	{
		if (this.getVariables().contains("AUTO_USE_SHORTCUTS"))
		{
			List<Integer> positions = this.getVariables().getIntegerList("AUTO_USE_SHORTCUTS");
			Shortcut usedShortcut = this.getShortcut(slot, page);
			if (usedShortcut == null)
			{
				Integer position = slot + page * 12;
				positions.remove(position);
			}
			else
			{
				for (Shortcut shortcut : this.getAllShortcuts())
				{
					if (usedShortcut.getId() == shortcut.getId() && usedShortcut.getType() == shortcut.getType())
					{
						shortcut.setAutoUse(false);
						this.sendPacket(new ExActivateAutoShortcut(shortcut, false));
						Integer position = shortcut.getSlot() + shortcut.getPage() * 12;
						positions.remove(position);
					}
				}
			}
			
			this.getVariables().setIntegerList("AUTO_USE_SHORTCUTS", positions);
		}
	}
	
	public boolean isInTimedHuntingZone()
	{
		return this.getVariables().getInt("LAST_HUNTING_ZONE_ID", 0) > 0;
	}
	
	public boolean isInTimedHuntingZone(int zoneId)
	{
		return this.getVariables().getInt("LAST_HUNTING_ZONE_ID", 0) == zoneId;
	}
	
	public TimedHuntingZoneHolder getTimedHuntingZone()
	{
		return TimedHuntingZoneData.getInstance().getHuntingZone(this.getVariables().getInt("LAST_HUNTING_ZONE_ID", 0));
	}
	
	public void startTimedHuntingZone(int zoneId)
	{
		this.stopTimedHuntingZoneTask();
		this._timedHuntingZoneTask = ThreadPool.scheduleAtFixedRate(() -> {
			if (this.isInTimedHuntingZone(zoneId))
			{
				long time = this.getTimedHuntingZoneRemainingTime(zoneId);
				if (time > 0L)
				{
					if (time < 300000L)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.THE_TIME_FOR_HUNTING_IN_THIS_ZONE_EXPIRES_IN_S1_MIN_PLEASE_ADD_MORE_TIME);
						sm.addLong(time / 60000L);
						this.sendPacket(sm);
					}
					
					this.getVariables().set("HUNTING_ZONE_TIME_" + zoneId, time - 60000L);
				}
				else
				{
					if (this._timedHuntingZoneTask != null)
					{
						this._timedHuntingZoneTask.cancel(false);
						this._timedHuntingZoneTask = null;
					}
					
					this.abortCast();
					this.stopMove(null);
					this.teleToLocation(MapRegionManager.getInstance().getTeleToLocation(this, TeleportWhereType.TOWN));
					this.sendPacket(SystemMessageId.THE_HUNTING_ZONE_S_USE_TIME_HAS_EXPIRED_SO_YOU_WERE_MOVED_OUTSIDE);
					this.setInstance(null);
				}
			}
		}, 60000L, 60000L);
	}
	
	public void stopTimedHuntingZoneTask()
	{
		if (this._timedHuntingZoneTask != null && !this._timedHuntingZoneTask.isCancelled() && !this._timedHuntingZoneTask.isDone())
		{
			this._timedHuntingZoneTask.cancel(true);
			this._timedHuntingZoneTask = null;
		}
	}
	
	public int getTimedHuntingZoneRemainingTime(int zoneId)
	{
		return Math.max(this.getVariables().getInt("HUNTING_ZONE_TIME_" + zoneId, 0), 0);
	}
	
	public long getTimedHuntingZoneInitialEntry(int zoneId)
	{
		return Math.max(this.getVariables().getLong("HUNTING_ZONE_ENTRY_" + zoneId, 0L), 0L);
	}
	
	private void restoreRandomCraft()
	{
		this._randomCraft = new PlayerRandomCraft(this);
		this._randomCraft.restore();
	}
	
	public PlayerRandomCraft getRandomCraft()
	{
		return this._randomCraft;
	}
	
	public PetEvolveHolder getPetEvolve(int controlItemId)
	{
		PetEvolveHolder evolve = this._petEvolves.get(controlItemId);
		if (evolve != null)
		{
			return evolve;
		}
		Item item = this.getInventory().getItemByObjectId(controlItemId);
		PetData petData = item == null ? null : PetDataTable.getInstance().getPetDataByItemId(item.getId());
		return new PetEvolveHolder(petData == null ? 0 : petData.getIndex(), EvolveLevel.None.ordinal(), "", 1, 0L);
	}
	
	public void setPetEvolve(int itemObjectId, PetEvolveHolder entry)
	{
		this._petEvolves.put(itemObjectId, entry);
	}
	
	public void restorePetEvolvesByItem()
	{
		this.getInventory().getItems().forEach(it -> {
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement ps2 = con.prepareStatement("SELECT pet_evolves.index, pet_evolves.level as evolve, pets.name, pets.level, pets.exp FROM pet_evolves, pets WHERE pet_evolves.itemObjId=? AND pet_evolves.itemObjId = pets.item_obj_id");)
			{
				ps2.setInt(1, it.getObjectId());
				
				try (ResultSet rset = ps2.executeQuery())
				{
					while (rset.next())
					{
						EvolveLevel evolve = EvolveLevel.values()[rset.getInt("evolve")];
						if (evolve != null)
						{
							this._petEvolves.put(it.getObjectId(), new PetEvolveHolder(rset.getInt("index"), rset.getInt("evolve"), rset.getString("name"), rset.getInt("level"), rset.getLong("exp")));
						}
					}
				}
			}
			catch (Exception var13)
			{
				LOGGER.log(Level.SEVERE, "Could not restore pet evolve for playerId: " + this.getObjectId(), var13);
			}
		});
	}
	
	public void calculateStatIncreaseSkills()
	{
		if (this._statIncreaseSkillTask == null)
		{
			this._statIncreaseSkillTask = ThreadPool.schedule(() -> {
				boolean update = false;
				double statValue = this.getStat().getValue(Stat.STAT_STR);
				if (statValue >= 60.0 && statValue < 70.0)
				{
					if (this.getSkillLevel(CommonSkill.STR_INCREASE_BONUS_1.getId()) != 1)
					{
						this.removeSkill(CommonSkill.STR_INCREASE_BONUS_1.getSkill());
						this.addSkill(CommonSkill.STR_INCREASE_BONUS_1.getSkill(), false);
						update = true;
					}
				}
				else if (statValue >= 70.0 && statValue < 90.0)
				{
					if (this.getSkillLevel(CommonSkill.STR_INCREASE_BONUS_2.getId()) != 2)
					{
						this.removeSkill(CommonSkill.STR_INCREASE_BONUS_2.getSkill());
						this.addSkill(CommonSkill.STR_INCREASE_BONUS_2.getSkill(), false);
						update = true;
					}
				}
				else if (statValue >= 90.0)
				{
					if (this.getSkillLevel(CommonSkill.STR_INCREASE_BONUS_3.getId()) != 3)
					{
						this.removeSkill(CommonSkill.STR_INCREASE_BONUS_3.getSkill());
						this.addSkill(CommonSkill.STR_INCREASE_BONUS_3.getSkill(), false);
						update = true;
					}
				}
				else if (this.getSkillLevel(CommonSkill.STR_INCREASE_BONUS_3.getId()) > 0)
				{
					this.removeSkill(CommonSkill.STR_INCREASE_BONUS_3.getSkill());
					update = true;
				}
				
				statValue = this.getStat().getValue(Stat.STAT_INT);
				if (statValue >= 60.0 && statValue < 70.0)
				{
					if (this.getSkillLevel(CommonSkill.INT_INCREASE_BONUS_1.getId()) != 1)
					{
						this.removeSkill(CommonSkill.INT_INCREASE_BONUS_1.getSkill());
						this.addSkill(CommonSkill.INT_INCREASE_BONUS_1.getSkill(), false);
						update = true;
					}
				}
				else if (statValue >= 70.0 && statValue < 90.0)
				{
					if (this.getSkillLevel(CommonSkill.INT_INCREASE_BONUS_2.getId()) != 2)
					{
						this.removeSkill(CommonSkill.INT_INCREASE_BONUS_2.getSkill());
						this.addSkill(CommonSkill.INT_INCREASE_BONUS_2.getSkill(), false);
						update = true;
					}
				}
				else if (statValue >= 90.0)
				{
					if (this.getSkillLevel(CommonSkill.INT_INCREASE_BONUS_3.getId()) != 3)
					{
						this.removeSkill(CommonSkill.INT_INCREASE_BONUS_3.getSkill());
						this.addSkill(CommonSkill.INT_INCREASE_BONUS_3.getSkill(), false);
						update = true;
					}
				}
				else if (this.getSkillLevel(CommonSkill.INT_INCREASE_BONUS_3.getId()) > 0)
				{
					this.removeSkill(CommonSkill.INT_INCREASE_BONUS_3.getSkill());
					update = true;
				}
				
				statValue = this.getStat().getValue(Stat.STAT_DEX);
				if (statValue >= 50.0 && statValue < 60.0)
				{
					if (this.getSkillLevel(CommonSkill.DEX_INCREASE_BONUS_1.getId()) != 1)
					{
						this.removeSkill(CommonSkill.DEX_INCREASE_BONUS_1.getSkill());
						this.addSkill(CommonSkill.DEX_INCREASE_BONUS_1.getSkill(), false);
						update = true;
					}
				}
				else if (statValue >= 60.0 && statValue < 80.0)
				{
					if (this.getSkillLevel(CommonSkill.DEX_INCREASE_BONUS_2.getId()) != 2)
					{
						this.removeSkill(CommonSkill.DEX_INCREASE_BONUS_2.getSkill());
						this.addSkill(CommonSkill.DEX_INCREASE_BONUS_2.getSkill(), false);
						update = true;
					}
				}
				else if (statValue >= 80.0)
				{
					if (this.getSkillLevel(CommonSkill.DEX_INCREASE_BONUS_3.getId()) != 3)
					{
						this.removeSkill(CommonSkill.DEX_INCREASE_BONUS_3.getSkill());
						this.addSkill(CommonSkill.DEX_INCREASE_BONUS_3.getSkill(), false);
						update = true;
					}
				}
				else if (this.getSkillLevel(CommonSkill.DEX_INCREASE_BONUS_3.getId()) > 0)
				{
					this.removeSkill(CommonSkill.DEX_INCREASE_BONUS_3.getSkill());
					update = true;
				}
				
				statValue = this.getStat().getValue(Stat.STAT_WIT);
				if (statValue >= 40.0 && statValue < 50.0)
				{
					if (this.getSkillLevel(CommonSkill.WIT_INCREASE_BONUS_1.getId()) != 1)
					{
						this.removeSkill(CommonSkill.WIT_INCREASE_BONUS_1.getSkill());
						this.addSkill(CommonSkill.WIT_INCREASE_BONUS_1.getSkill(), false);
						update = true;
					}
				}
				else if (statValue >= 50.0 && statValue < 70.0)
				{
					if (this.getSkillLevel(CommonSkill.WIT_INCREASE_BONUS_2.getId()) != 2)
					{
						this.removeSkill(CommonSkill.WIT_INCREASE_BONUS_2.getSkill());
						this.addSkill(CommonSkill.WIT_INCREASE_BONUS_2.getSkill(), false);
						update = true;
					}
				}
				else if (statValue >= 70.0)
				{
					if (this.getSkillLevel(CommonSkill.WIT_INCREASE_BONUS_3.getId()) != 3)
					{
						this.removeSkill(CommonSkill.WIT_INCREASE_BONUS_3.getSkill());
						this.addSkill(CommonSkill.WIT_INCREASE_BONUS_3.getSkill(), false);
						update = true;
					}
				}
				else if (this.getSkillLevel(CommonSkill.WIT_INCREASE_BONUS_3.getId()) > 0)
				{
					this.removeSkill(CommonSkill.WIT_INCREASE_BONUS_3.getSkill());
					update = true;
				}
				
				statValue = this.getStat().getValue(Stat.STAT_CON);
				if (statValue >= 50.0 && statValue < 65.0)
				{
					if (this.getSkillLevel(CommonSkill.CON_INCREASE_BONUS_1.getId()) != 1)
					{
						this.removeSkill(CommonSkill.CON_INCREASE_BONUS_1.getSkill());
						this.addSkill(CommonSkill.CON_INCREASE_BONUS_1.getSkill(), false);
						update = true;
					}
				}
				else if (statValue >= 65.0 && statValue < 90.0)
				{
					if (this.getSkillLevel(CommonSkill.CON_INCREASE_BONUS_2.getId()) != 2)
					{
						this.removeSkill(CommonSkill.CON_INCREASE_BONUS_2.getSkill());
						this.addSkill(CommonSkill.CON_INCREASE_BONUS_2.getSkill(), false);
						update = true;
					}
				}
				else if (statValue >= 90.0)
				{
					if (this.getSkillLevel(CommonSkill.CON_INCREASE_BONUS_3.getId()) != 3)
					{
						this.removeSkill(CommonSkill.CON_INCREASE_BONUS_3.getSkill());
						this.addSkill(CommonSkill.CON_INCREASE_BONUS_3.getSkill(), false);
						update = true;
					}
				}
				else if (this.getSkillLevel(CommonSkill.CON_INCREASE_BONUS_3.getId()) > 0)
				{
					this.removeSkill(CommonSkill.CON_INCREASE_BONUS_3.getSkill());
					update = true;
				}
				
				statValue = this.getStat().getValue(Stat.STAT_MEN);
				if (statValue >= 45.0 && statValue < 60.0)
				{
					if (this.getSkillLevel(CommonSkill.MEN_INCREASE_BONUS_1.getId()) != 1)
					{
						this.removeSkill(CommonSkill.MEN_INCREASE_BONUS_1.getSkill());
						this.addSkill(CommonSkill.MEN_INCREASE_BONUS_1.getSkill(), false);
						update = true;
					}
				}
				else if (statValue >= 60.0 && statValue < 85.0)
				{
					if (this.getSkillLevel(CommonSkill.MEN_INCREASE_BONUS_2.getId()) != 2)
					{
						this.removeSkill(CommonSkill.MEN_INCREASE_BONUS_2.getSkill());
						this.addSkill(CommonSkill.MEN_INCREASE_BONUS_2.getSkill(), false);
						update = true;
					}
				}
				else if (statValue >= 85.0)
				{
					if (this.getSkillLevel(CommonSkill.MEN_INCREASE_BONUS_3.getId()) != 3)
					{
						this.removeSkill(CommonSkill.MEN_INCREASE_BONUS_3.getSkill());
						this.addSkill(CommonSkill.MEN_INCREASE_BONUS_3.getSkill(), false);
						update = true;
					}
				}
				else if (this.getSkillLevel(CommonSkill.MEN_INCREASE_BONUS_3.getId()) > 0)
				{
					this.removeSkill(CommonSkill.MEN_INCREASE_BONUS_3.getSkill());
					update = true;
				}
				
				if (update)
				{
					this.sendSkillList();
				}
				
				this._statIncreaseSkillTask = null;
			}, 1000L);
		}
	}
	
	private int getOgClanId()
	{
		return this.isMercenary() ? this._clanIdOg : this._clanId;
	}
	
	public void setMercenary(boolean update, int clanId)
	{
		Clan clan = ClanTable.getInstance().getClan(clanId);
		if (update)
		{
			String name = clan.createMercenary(this.getObjectId(), this._baseClass);
			this.setMercenary(update);
			this.getVariables().set("MercenaryClan", clanId);
			this._mercenaryName = name;
			this._clanIdMercenary = clanId;
			this.setClanMercenary(clan);
		}
		else
		{
			clan.removeMercenaryByPlayerId(this.getObjectId());
			this.setMercenary(update);
			this.getVariables().remove("MercenaryClan");
			this._mercenaryName = "";
			this._clanIdMercenary = 0;
			this.setClan(this._clanOg);
		}
		
		this.broadcastUserInfo();
	}
	
	public void updateMercenary()
	{
		int clanId = this.getVariables().getInt("MercenaryClan");
		Clan clan = ClanTable.getInstance().getClan(clanId);
		this._clanIdMercenary = clanId;
		this._mercenaryName = clan.getMapMercenary().get(this.getObjectId()).getName();
		this.setClanMercenary(clan);
	}
	
	public void setClanMercenary(Clan clan)
	{
		if (this._clan != null)
		{
			this._clanOg = this._clan;
			this._clanIdOg = this._clan.getId();
		}
		
		this._clan = clan;
		if (clan == null)
		{
			this.setTitle("");
			this._clanId = 0;
			this._clanPrivileges = new ClanPrivileges();
			this._pledgeType = 0;
			this._powerGrade = 0;
			this._lvlJoinedAcademy = 0;
			this._apprentice = 0;
			this._sponsor = 0;
			this._activeWarehouse = null;
		}
		else
		{
			this._clanId = clan.getId();
		}
	}
	
	public String getMercenaryName()
	{
		if (this._mercenaryName == null)
		{
			int clanId = this.getVariables().getInt("MercenaryClan", -1);
			Clan clan = ClanTable.getInstance().getClan(clanId);
			if (clan != null)
			{
				MercenaryPledgeHolder mercenary = clan.getMapMercenary().get(this.getObjectId());
				if (mercenary != null)
				{
					this._mercenaryName = mercenary.getName();
				}
			}
		}
		
		return this._mercenaryName == null ? this.getName() : this._mercenaryName;
	}
	
	public int getClanIdMercenary()
	{
		return this._clanIdMercenary;
	}
	
	public void setMercenary(boolean update)
	{
		this.getVariables().set("isMercenary", update);
	}
	
	public boolean isMercenary()
	{
		return this.getVariables().getBoolean("isMercenary", false);
	}
	
	public List<PlayerCollectionData> getCollections()
	{
		return this._collections;
	}
	
	public List<Integer> getCollectionFavorites()
	{
		return this._collectionFavorites;
	}
	
	public void addCollectionFavorite(Integer id)
	{
		this._collectionFavorites.add(id);
	}
	
	public void removeCollectionFavorite(Integer id)
	{
		this._collectionFavorites.remove(id);
	}
	
	public void storeCollections()
	{
		if (!this._collections.isEmpty())
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement st = con.prepareStatement("REPLACE INTO collections (`accountName`, `itemId`, `collectionId`, `index`) VALUES (?, ?, ?, ?)");)
			{
				this._collections.forEach(data -> {
					try
					{
						st.setString(1, this.getAccountName());
						st.setInt(2, data.getItemId());
						st.setInt(3, data.getCollectionId());
						st.setInt(4, data.getIndex());
						st.addBatch();
					}
					catch (Exception var4)
					{
						LOGGER.log(Level.SEVERE, "Could not store collection for playerId " + this.getObjectId() + ": ", var4);
					}
				});
				st.executeBatch();
			}
			catch (Exception var9)
			{
				LOGGER.log(Level.SEVERE, "Could not store collection for playerId " + this.getObjectId() + ": ", var9);
			}
		}
	}
	
	public void storeCollectionFavorites()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			try (PreparedStatement st = con.prepareStatement("DELETE FROM collection_favorites WHERE accountName=?"))
			{
				st.setString(1, this.getAccountName());
				st.execute();
			}
			
			try (PreparedStatement st = con.prepareStatement("REPLACE INTO collection_favorites (`accountName`, `collectionId`) VALUES (?, ?)"))
			{
				this._collectionFavorites.forEach(data -> {
					try
					{
						st.setString(1, this.getAccountName());
						st.setInt(2, data);
						st.addBatch();
					}
					catch (Exception var4)
					{
						LOGGER.log(Level.SEVERE, "Could not store collection favorite for playerId " + this.getObjectId() + ": ", var4);
					}
				});
				st.executeBatch();
			}
		}
		catch (Exception var11)
		{
			LOGGER.log(Level.SEVERE, "Could not store collection favorite for playerId " + this.getObjectId() + ": ", var11);
		}
	}
	
	private void restoreCollections()
	{
		this._collections.clear();
		
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM collections WHERE accountName=? ORDER BY `index`");)
		{
			statement.setString(1, this.getAccountName());
			
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					int collectionId = rset.getInt("collectionId");
					if (CollectionData.getInstance().getCollection(collectionId) != null)
					{
						this._collections.add(new PlayerCollectionData(collectionId, rset.getInt("itemId"), rset.getInt("index")));
					}
				}
			}
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.SEVERE, "Could not restore collection list data for playerId: " + this.getObjectId(), var12);
		}
	}
	
	private void restoreCollectionBonuses()
	{
		Set<Integer> collectionIds = new HashSet<>();
		
		for (PlayerCollectionData collection : this._collections)
		{
			collectionIds.add(collection.getCollectionId());
		}
		
		for (int collectionId : collectionIds)
		{
			CollectionDataHolder collection = CollectionData.getInstance().getCollection(collectionId);
			int count = 0;
			
			for (PlayerCollectionData data : this._collections)
			{
				if (data.getCollectionId() == collectionId)
				{
					count++;
				}
			}
			
			if (count >= collection.getCompleteCount())
			{
				Options options = OptionData.getInstance().getOptions(collection.getOptionId());
				if (options != null)
				{
					options.apply(this);
				}
			}
		}
	}
	
	private void restoreCollectionFavorites()
	{
		this._collectionFavorites.clear();
		
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM collection_favorites WHERE accountName=?");)
		{
			statement.setString(1, this.getAccountName());
			
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					this._collectionFavorites.add(rset.getInt("collectionId"));
				}
			}
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.SEVERE, "Could not restore collection favorite list data for playerId: " + this.getObjectId(), var12);
		}
	}
	
	public int getPlayerResetCount()
	{
		return this.getVariables().getInt("CROSS_EVENT_DAILY_RESET_COUNT", CrossEventManager.getInstance().getDailyResets());
	}
	
	public void setPlayerResetCount(int dailyReset)
	{
		this.getVariables().set("CROSS_EVENT_DAILY_RESET_COUNT", dailyReset);
	}
	
	public int getCrossRewardsCount()
	{
		return this._crossAdvancedReward;
	}
	
	public void setCrossAdvancedRewardCount(int value)
	{
		this._crossAdvancedReward += value;
	}
	
	public List<CrossEventHolder> getCrossEventCells()
	{
		return this._crossCell;
	}
	
	private void restoreCrossEvent()
	{
		this._crossCell.clear();
		CrossEventManager.getInstance().resetAdvancedRewards(this);
		List<Integer> cells = this.getVariables().getIntegerList("CROSS_EVENT_CELLS");
		if (!cells.isEmpty())
		{
			for (int id : cells)
			{
				this._crossCell.add(new CrossEventHolder(id));
			}
		}
		
		List<Integer> rewards = this.getVariables().getIntegerList("CROSS_EVENT_REWARDS");
		if (!rewards.isEmpty())
		{
			for (int id : rewards)
			{
				CrossEventManager.getInstance().addRewardsAvailable(this, id);
			}
		}
		
		this.setCrossAdvancedRewardCount(this.getVariables().getInt("CROSS_EVENT_ADVANCED_COUNT", 0));
	}
	
	private void storeCrossEvent()
	{
		List<Integer> cells = new ArrayList<>();
		this._crossCell.forEach(data -> cells.add(data.cellId()));
		this.getVariables().setIntegerList("CROSS_EVENT_CELLS", cells);
		this.getVariables().setIntegerList("CROSS_EVENT_REWARDS", CrossEventManager.getInstance().getPlayerRewardsAvailable(this));
		this.getVariables().set("CROSS_EVENT_ADVANCED_COUNT", this._crossAdvancedReward);
	}
	
	public Map<Integer, PlayerPurgeHolder> getPurgePoints()
	{
		return this._purgePoints;
	}
	
	public void storeSubjugation()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			try (PreparedStatement st = con.prepareStatement("DELETE FROM character_purge WHERE charId=?"))
			{
				st.setInt(1, this.getObjectId());
				st.execute();
			}
			
			try (PreparedStatement st = con.prepareStatement("REPLACE INTO character_purge (`charId`, `category`, `points`, `keys`, `remainingKeys`) VALUES (?, ?, ?, ?, ?)"))
			{
				this.getPurgePoints().forEach((category, data) -> {
					try
					{
						st.setInt(1, this.getObjectId());
						st.setInt(2, category);
						st.setInt(3, data.getPoints());
						st.setInt(4, data.getKeys());
						st.setInt(5, data.getRemainingKeys());
						st.addBatch();
					}
					catch (Exception var5)
					{
						LOGGER.log(Level.SEVERE, "Could not store subjugation data for playerId " + this.getObjectId() + ": ", var5);
					}
				});
				st.executeBatch();
			}
		}
		catch (Exception var11)
		{
			LOGGER.log(Level.SEVERE, "Could not store subjugation data for playerId " + this.getObjectId() + ": ", var11);
		}
	}
	
	private void restoreSubjugation()
	{
		this._purgePoints.clear();
		
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM character_purge WHERE charId=?");)
		{
			statement.setInt(1, this.getObjectId());
			
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					this._purgePoints.put(rset.getInt("category"), new PlayerPurgeHolder(rset.getInt("points"), rset.getInt("keys"), rset.getInt("remainingKeys")));
				}
			}
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.SEVERE, "Could not restore subjugation data for playerId: " + this.getObjectId(), var12);
		}
	}
	
	public int getPurgeLastCategory()
	{
		return this.getVariables().getInt("PURGE_LAST_CATEGORY", 1);
	}
	
	public void setPurgeLastCategory(int category)
	{
		this.getVariables().set("PURGE_LAST_CATEGORY", category);
	}
	
	public int getClanDonationPoints()
	{
		return this.getVariables().getInt("CLAN_DONATION_POINTS", 3);
	}
	
	public MissionLevelPlayerDataHolder getMissionLevelProgress()
	{
		if (this._missionLevelProgress == null)
		{
			String variable = "MISSION_LEVEL_PROGRESS_" + MissionLevel.getInstance().getCurrentSeason();
			if (this.getVariables().hasVariable(variable))
			{
				this._missionLevelProgress = new MissionLevelPlayerDataHolder(this.getVariables().getString(variable));
			}
			else
			{
				this._missionLevelProgress = new MissionLevelPlayerDataHolder();
				this._missionLevelProgress.storeInfoInVariable(this);
			}
		}
		
		return this._missionLevelProgress;
	}
	
	public Prisoner getPrisonerInfo()
	{
		return this._prisonerInfo;
	}
	
	public void setPrisonerInfo(Prisoner prisoner)
	{
		this._prisonerInfo = prisoner;
	}
	
	public boolean isPrisoner()
	{
		return this._prisonerInfo.getSentenceTime() != 0L;
	}
	
	private void storeDualInventory()
	{
		this.getVariables().set("DUAL_INVENTORY_SLOT", this._dualInventorySlot);
		this.getVariables().setIntegerList("DUAL_INVENTORY_SET_A", this._dualInventorySetA);
		this.getVariables().setIntegerList("DUAL_INVENTORY_SET_B", this._dualInventorySetB);
	}
	
	public void restoreDualInventory()
	{
		this._dualInventorySlot = this.getVariables().getInt("DUAL_INVENTORY_SLOT", 0);
		if (this.getVariables().contains("DUAL_INVENTORY_SET_A"))
		{
			this._dualInventorySetA = this.getVariables().getIntegerList("DUAL_INVENTORY_SET_A");
		}
		else
		{
			List<Integer> list = new ArrayList<>(59);
			
			for (int i = 0; i < 59; i++)
			{
				list.add(this.getInventory().getPaperdollObjectId(i));
			}
			
			this.getVariables().setIntegerList("DUAL_INVENTORY_SET_A", list);
			this._dualInventorySetA = list;
		}
		
		if (this.getVariables().contains("DUAL_INVENTORY_SET_B"))
		{
			this._dualInventorySetB = this.getVariables().getIntegerList("DUAL_INVENTORY_SET_B");
		}
		else
		{
			List<Integer> list = new ArrayList<>(59);
			
			for (int i = 0; i < 59; i++)
			{
				list.add(0);
			}
			
			this.getVariables().setIntegerList("DUAL_INVENTORY_SET_B", list);
			this._dualInventorySetB = list;
		}
		
		this.sendPacket(new ExDualInventorySwap(this._dualInventorySlot));
	}
	
	public void setDualInventorySlot(int slot)
	{
		List<Integer> itemObjectIds = this.getDualInventorySet();
		if (itemObjectIds != null)
		{
			this._dualInventorySlot = slot;
			boolean changed = false;
			
			for (int i = 0; i < 59; i++)
			{
				int existingObjectId = this.getInventory().getPaperdollObjectId(i);
				int itemObjectId = itemObjectIds.get(i);
				if (existingObjectId != itemObjectId)
				{
					changed = true;
					if (existingObjectId > 0)
					{
						this.getInventory().unEquipItemInSlot(i);
					}
					
					if (itemObjectId > 0)
					{
						Item item = this.getInventory().getItemByObjectId(itemObjectId);
						if (item != null)
						{
							this.useEquippableItem(item, false);
						}
					}
				}
			}
			
			this.sendPacket(new ExDualInventorySwap(slot));
			if (changed)
			{
				this.sendItemList();
				this.broadcastUserInfo();
			}
		}
	}
	
	private List<Integer> getDualInventorySet()
	{
		return this._dualInventorySlot == 0 ? this._dualInventorySetA : this._dualInventorySetB;
	}
	
	public int getSkillEnchantExp(int level)
	{
		return this.getVariables().getInt("SKILL_ENCHANT_STAR_" + level, 0);
	}
	
	public void setSkillEnchantExp(int level, int exp)
	{
		this.getVariables().set("SKILL_ENCHANT_STAR_" + level, exp);
	}
	
	public void increaseTrySkillEnchant(int level)
	{
		int currentTry = this.getSkillTryEnchant(level) + 1;
		this.getVariables().set("SKILL_TRY_ENCHANT_" + level, currentTry);
	}
	
	public int getSkillTryEnchant(int level)
	{
		return this.getVariables().getInt("SKILL_TRY_ENCHANT_" + level, 1);
	}
	
	public void setSkillTryEnchant(int level)
	{
		this.getVariables().set("SKILL_TRY_ENCHANT_" + level, 1);
	}
	
	public void sendCombatPower()
	{
		if (this._sendItemScoreTask == null)
		{
			this._sendItemScoreTask = ThreadPool.schedule(() -> {
				this.sendPacket(new ExItemScore(this._combatPowerHolder));
				this._sendItemScoreTask = null;
			}, 1000L);
		}
	}
	
	public CombatPowerHolder getCombatPower()
	{
		return this._combatPowerHolder;
	}
	
	public Collection<PlayerRelicData> getRelics()
	{
		return this._relics;
	}
	
	public Collection<PlayerRelicCollectionData> getRelicCollections()
	{
		return this._relicCollections;
	}
	
	public boolean isRelicRegisteredInCollection(int relicId, int relicCollectionId)
	{
		for (PlayerRelicCollectionData collectionData : this._relicCollections)
		{
			if (collectionData.getRelicId() == relicId && collectionData.getRelicCollectionId() == relicCollectionId)
			{
				return true;
			}
		}
		
		return false;
	}
	
	public boolean isRelicRegistered(int relicId, int relicLevel)
	{
		for (PlayerRelicCollectionData collectionData : _relicCollections)
		{
			if ((collectionData.getRelicId() == relicId) && (collectionData.getRelicLevel() == relicLevel))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isCompleteCollection(int relicCollectionId)
	{
		RelicCollectionDataHolder collection = RelicCollectionData.getInstance().getRelicCollection(relicCollectionId);
		if (collection == null)
		{
			return false;
		}
		int completeRelicsInCollection = collection.getCompleteCount();
		Set<Integer> result = new HashSet<>();
		
		for (PlayerRelicCollectionData collectionData : this._relicCollections)
		{
			if (collectionData.getRelicCollectionId() == relicCollectionId)
			{
				result.add(collectionData.getRelicId());
			}
		}
		
		if (completeRelicsInCollection == result.size())
		{
			if (RelicSystemConfig.RELIC_SYSTEM_DEBUG_ENABLED)
			{
				this.sendMessage("Collection id: " + relicCollectionId + " is complete.");
				this.sendMessage("Relics in collection id: " + relicCollectionId + " : " + result);
			}
			
			return true;
		}
		return false;
	}
	
	public void storeRelics()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement st = con.prepareStatement("REPLACE INTO relics (`accountName`, `relicId`, `relicLevel`, `relicCount`, `relicIndex`, `relicSummonTime`) VALUES (?, ?, ?, ?, ?, ?)");)
		{
			this._relics.forEach(data -> {
				try
				{
					st.setString(1, this.getAccountName());
					st.setInt(2, data.getRelicId());
					st.setInt(3, data.getRelicLevel());
					st.setInt(4, data.getRelicCount());
					st.setInt(5, data.getRelicIndex());
					st.setLong(6, data.getRelicSummonTime());
					st.addBatch();
				}
				catch (Exception var4)
				{
					LOGGER.log(Level.SEVERE, "Could not store relics for playerId " + this.getObjectId() + ": ", var4);
				}
			});
			st.executeBatch();
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.SEVERE, "Could not store relics for playerId " + this.getObjectId() + ": ", var9);
		}
	}
	
	private void restoreRelics()
	{
		this._relics.clear();
		
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM relics WHERE accountName=? ORDER BY `relicId`");)
		{
			statement.setString(1, this.getAccountName());
			
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					int relicId = rset.getInt("relicId");
					int relicLevel = rset.getInt("relicLevel");
					int relicCount = rset.getInt("relicCount");
					int relicIndex = rset.getInt("relicIndex");
					Long relicSummonTime = rset.getLong("relicSummonTime");
					RelicDataHolder holder = RelicData.getInstance().getRelic(relicId);
					if (holder != null)
					{
						this._relics.add(new PlayerRelicData(relicId, relicLevel, relicCount, relicIndex, relicSummonTime));
						this.giveRelicSkill(holder, relicLevel);
					}
				}
			}
		}
		catch (Exception var16)
		{
			LOGGER.log(Level.SEVERE, "Could not restore relic data for playerId: " + this.getObjectId(), var16);
		}
	}
	
	public void deleteRelics(int relicId, int relicLevel, int relicCount, int relicIndex, long relicSummonTime)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("DELETE FROM relics WHERE accountName=? AND relicId=? AND relicLevel=? AND relicCount=? AND relicIndex=? AND relicSummonTime=?");)
		{
			statement.setString(1, this.getAccountName());
			statement.setInt(2, relicId);
			statement.setInt(3, relicLevel);
			statement.setInt(4, relicCount);
			statement.setInt(5, relicIndex);
			statement.setLong(6, relicSummonTime);
			statement.executeUpdate();
		}
		catch (Exception var15)
		{
			LOGGER.log(Level.SEVERE, "Could not delete relics data for playerId: " + this.getObjectId(), var15);
		}
	}
	
	public void storeRelicCollections()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement st = con.prepareStatement("REPLACE INTO relic_collections (`accountName`, `relicId`, `relicLevel`, `relicCollectionId`, `index`) VALUES (?, ?, ?, ?, ?)");)
		{
			this._relicCollections.forEach(data -> {
				try
				{
					st.setString(1, this.getAccountName());
					st.setInt(2, data.getRelicId());
					st.setInt(3, data.getRelicLevel());
					st.setInt(4, data.getRelicCollectionId());
					st.setInt(5, data.getIndex());
					st.addBatch();
				}
				catch (Exception var4)
				{
					LOGGER.log(Level.SEVERE, "Could not store relics collection for playerId " + this.getObjectId() + ": ", var4);
				}
			});
			st.executeBatch();
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.SEVERE, "Could not store relics collection for playerId " + this.getObjectId() + ": ", var9);
		}
	}
	
	private void restoreRelicCollections()
	{
		this._relicCollections.clear();
		
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM relic_collections WHERE accountName=? ORDER BY `relicCollectionId`");)
		{
			statement.setString(1, this.getAccountName());
			
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					int relicCollectionId = rset.getInt("relicCollectionId");
					if (RelicCollectionData.getInstance().getRelicCollection(relicCollectionId) != null)
					{
						this._relicCollections.add(new PlayerRelicCollectionData(relicCollectionId, rset.getInt("relicId"), rset.getInt("relicLevel"), rset.getInt("index")));
					}
				}
			}
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.SEVERE, "Could not restore relics collection list data for playerId: " + this.getObjectId(), var12);
		}
	}
	
	private void restoreRelicCollectionBonuses()
	{
		Set<Integer> relicCollectionIds = new HashSet<>();
		
		for (PlayerRelicCollectionData relicCollection : this._relicCollections)
		{
			relicCollectionIds.add(relicCollection.getRelicCollectionId());
		}
		
		for (int relicCollectionId : relicCollectionIds)
		{
			RelicCollectionDataHolder relicCollection = RelicCollectionData.getInstance().getRelicCollection(relicCollectionId);
			int count = 0;
			
			for (PlayerRelicCollectionData data : this._relicCollections)
			{
				if (data.getRelicCollectionId() == relicCollectionId)
				{
					count++;
				}
			}
			
			if (count >= relicCollection.getCompleteCount())
			{
				Options options = OptionData.getInstance().getOptions(relicCollection.getOptionId());
				if (options != null)
				{
					options.apply(this);
				}
			}
		}
		
		this._combatPowerHolder.updateRelicCollectionCombatPower();
		this.sendCombatPower();
	}
	
	public void giveRelicSkill(RelicDataHolder holder, int enchant)
	{
		List<PlayerRelicData> ownedFamilyRelics = new ArrayList<>();
		
		for (PlayerRelicData relic : this.getRelics())
		{
			RelicDataHolder relicHolder = RelicData.getInstance().getRelic(relic.getRelicId());
			if (relicHolder != null && relicHolder.getParentRelicId() == holder.getParentRelicId())
			{
				ownedFamilyRelics.add(relic);
			}
		}
		
		RelicDataHolder bestHolder = ownedFamilyRelics.stream().map(relicx -> RelicData.getInstance().getRelic(relicx.getRelicId())).filter(Objects::nonNull).max(Comparator.<RelicDataHolder, Integer> comparing(r -> r.getGradeOrdinal()).thenComparing(r -> r.getEnchantHolderByEnchant(enchant).getSkillId())).orElse(null);
		if (bestHolder != null)
		{
			for (PlayerRelicData ownedRelic : this.getRelics())
			{
				RelicDataHolder ownedHolder = RelicData.getInstance().getRelic(ownedRelic.getRelicId());
				if (ownedHolder != null && ownedHolder.getParentRelicId() == holder.getParentRelicId())
				{
					this.removeSkill(ownedHolder.getEnchantHolderByEnchant(ownedRelic.getRelicLevel()).getSkillId());
				}
			}
			
			int skillId = bestHolder.getEnchantHolderByEnchant(enchant).getSkillId();
			if (skillId > 0)
			{
				Skill relicSkill = SkillData.getInstance().getSkill(skillId, bestHolder.getEnchantHolderByEnchant(enchant).getSkillLevel());
				if (relicSkill != null)
				{
					this.addSkill(relicSkill, false);
					this._combatPowerHolder.updateRelicCombatPower(this);
					this.sendCombatPower();
				}
			}
		}
	}
	
	public void handleRelicAcquisition(int obtainedRelicId)
	{
		Collection<PlayerRelicData> storedRelics = this.getRelics();
		PlayerRelicData existingRelic = storedRelics.stream().filter(relic -> relic.getRelicId() == obtainedRelicId).findFirst().orElse(null);
		RelicDataHolder relicTemplate = RelicData.getInstance().getRelic(obtainedRelicId);
		if (relicTemplate != null)
		{
			if (existingRelic != null)
			{
				existingRelic.setRelicCount(existingRelic.getRelicCount() + 1);
				this.storeRelics();
				this.sendPacket(new ExRelicsUpdateList(1, existingRelic.getRelicId(), 0, 1));
				this.handleRelic(existingRelic);
			}
			else
			{
				PlayerRelicData newRelic = new PlayerRelicData(obtainedRelicId, 0, 0, 0, 0L);
				storedRelics.add(newRelic);
				this.storeRelics();
				this.sendPacket(new ExRelicsUpdateList(1, newRelic.getRelicId(), 0, 0));
				this.handleRelic(newRelic);
			}
			
			this.giveRelicSkill(relicTemplate, 0);
		}
		else
		{
			PacketLogger.warning("ExRelicsSummonResult: Relic ID " + obtainedRelicId + " is not registered in RelicData.");
		}
	}
	
	public void handleRelic(PlayerRelicData relic)
	{
		if (RelicSystemConfig.RELIC_SUMMON_ANNOUNCE && !RelicSystemConfig.RELIC_ANNOUNCE_ONLY_A_B_GRADE)
		{
			Broadcast.toAllOnlinePlayers(new ExRelicsAnnounce(this, relic.getRelicId()));
		}
		
		if (!this.isRelicRegistered(relic.getRelicId(), relic.getRelicLevel()))
		{
			this.sendPacket(new ExRelicsCollectionUpdate(this, relic.getRelicId(), relic.getRelicLevel()));
		}
	}
	
	public void restoreChatBackground()
	{
		PlayerVariables variables = this.getVariables();
		this.sendPacket(new ExChatBackgroundList(variables));
		this.sendPacket(new ExChatBackGroundSettingNotification(variables));
	}
	
	public void setChatBackground(boolean enable, int chatBackground)
	{
		PlayerVariables variables = this.getVariables();
		variables.set("ENABLE_CHAT_BACKGROUND", enable);
		variables.set("ACTIVE_CHAT_BACKGROUND", chatBackground);
	}
	
	public float getAdenLabBonusChance()
	{
		return this._adenLabBonusChance;
	}
	
	public int getAdenLabCurrentlyUnlockedPage(byte bossId)
	{
		return this._adenLabCurrentlyUnlockedPage.computeIfAbsent(bossId, _ -> new AtomicInteger(1)).get();
	}
	
	public Map<Byte, Map<Byte, Map<Byte, Integer>>> getAdenLabSpecialGameStagesDrawnOptions()
	{
		return this._adenLabSpecialStagesDrawnOptions;
	}
	
	public Map<Byte, Map<Byte, Map<Byte, Integer>>> getAdenLabSpecialGameStagesConfirmedOptions()
	{
		return this._adenLabSpecialStagesConfirmedOptions;
	}
	
	public int getAdenLabCurrentTranscendLevel(byte bossId)
	{
		return this._adenLabCurrentTranscendLevel.computeIfAbsent(bossId, _ -> new AtomicInteger()).get();
	}
	
	public int getAdenLabNormalGameOpenedCardsCount(byte bossId)
	{
		return this._adenLabNormalGameOpenedCardsCount.computeIfAbsent(bossId, _ -> new AtomicInteger()).get();
	}
	
	public void setAdenLabBonusChance(float chance)
	{
		this._adenLabBonusChance = chance;
	}
	
	public void setAdenLabCurrentlyUnlockedPage(byte bossId, int pageIndex)
	{
		this._adenLabCurrentlyUnlockedPage.computeIfAbsent(bossId, _ -> new AtomicInteger(1)).set(pageIndex);
	}
	
	public void incrementAdenLabCurrentPage(byte bossId)
	{
		this._adenLabCurrentlyUnlockedPage.computeIfAbsent(bossId, _ -> new AtomicInteger(1)).getAndIncrement();
	}
	
	public void setAdenLabSpecialGameDrawnOptionsIndividual(byte bossId, byte pageIndex, byte optionIndex, int level)
	{
		if (level <= 0)
		{
			Map<Byte, Map<Byte, Integer>> middleMap = this._adenLabSpecialStagesDrawnOptions.get(bossId);
			if (middleMap != null)
			{
				Map<Byte, Integer> innerMap = middleMap.get(pageIndex);
				if (innerMap != null)
				{
					innerMap.remove(optionIndex);
				}
			}
		}
		else
		{
			Map<Byte, Map<Byte, Integer>> pageMap = this._adenLabSpecialStagesDrawnOptions.computeIfAbsent(bossId, _ -> new HashMap<>());
			Map<Byte, Integer> stageMap = pageMap.computeIfAbsent(pageIndex, _ -> new HashMap<>());
			stageMap.put(optionIndex, level);
		}
	}
	
	public void setAdenLabSpecialGameConfirmedOptionsIndividual(byte bossId, byte pageIndex, byte optionIndex, int level)
	{
		Map<Byte, Map<Byte, Integer>> pageMap = this._adenLabSpecialStagesConfirmedOptions.computeIfAbsent(bossId, _ -> new HashMap<>());
		Map<Byte, Integer> stageMap = pageMap.computeIfAbsent(pageIndex, _ -> new HashMap<>());
		stageMap.put(optionIndex, level);
	}
	
	public void setAdenLabSpecialGameConfirmedOptionsBulk(Map<Byte, Map<Byte, Map<Byte, Integer>>> options)
	{
		options.forEach((bossId, pages) -> this._adenLabSpecialStagesConfirmedOptions.computeIfAbsent(bossId, _ -> new HashMap<>()).putAll(pages));
	}
	
	public void setAdenLabSpecialGameDrawnOptionsBulk(Map<Byte, Map<Byte, Map<Byte, Integer>>> options)
	{
		options.forEach((bossId, pages) -> this._adenLabSpecialStagesDrawnOptions.computeIfAbsent(bossId, _ -> new HashMap<>()).putAll(pages));
	}
	
	public void setAdenLabNormalGameOpenedCardsCount(byte bossId, int count)
	{
		this._adenLabNormalGameOpenedCardsCount.computeIfAbsent(bossId, _ -> new AtomicInteger()).set(count);
	}
	
	public void incrementAdenLabNormalGameOpenedCardsCount(byte bossId)
	{
		this._adenLabNormalGameOpenedCardsCount.computeIfAbsent(bossId, _ -> new AtomicInteger()).getAndIncrement();
	}
	
	public void setAdenLabCurrentTranscendLevel(byte bossId, int level)
	{
		this._adenLabCurrentTranscendLevel.computeIfAbsent(bossId, _ -> new AtomicInteger()).set(level);
	}
	
	public void incrementAdenLabTranscendLevel(byte bossId)
	{
		this._adenLabCurrentTranscendLevel.computeIfAbsent(bossId, _ -> new AtomicInteger()).getAndIncrement();
	}
	
	public void modifyCharacterStyle(CharacterStyleCategoryType type, int styleId, boolean favorite, boolean add)
	{
		List<Integer> styles = this.getVariables().getIntegerList((favorite ? "FAVORITE_CHARACTER_STYLES_" : "AVAILABLE_CHARACTER_STYLES_") + type);
		if (add)
		{
			if (styles.add(styleId))
			{
				this.getVariables().setIntegerList((favorite ? "FAVORITE_CHARACTER_STYLES_" : "AVAILABLE_CHARACTER_STYLES_") + type, styles);
			}
		}
		else
		{
			for (int i = 0; i < styles.size(); i++)
			{
				if (styles.get(i) == styleId)
				{
					styles.remove(i);
					break;
				}
			}
			
			this.getVariables().setIntegerList((favorite ? "FAVORITE_CHARACTER_STYLES_" : "AVAILABLE_CHARACTER_STYLES_") + type, styles);
		}
	}
	
	public List<Integer> getAvailableCharacterStyles(CharacterStyleCategoryType type)
	{
		return this.getVariables().getIntegerList("AVAILABLE_CHARACTER_STYLES_" + type);
	}
	
	public void setActiveCharacterStyle(CharacterStyleCategoryType type, int styleId)
	{
		if (type == CharacterStyleCategoryType.APPEARANCE_WEAPON)
		{
			this.getVariables().set("ACTIVE_CHARACTER_STYLE_" + type + "_" + this._dualInventorySlot, styleId);
		}
		else
		{
			this.getVariables().set("ACTIVE_CHARACTER_STYLE_" + type, styleId);
		}
	}
	
	public int getActiveCharacterStyleId(CharacterStyleCategoryType type, int inventorySlot)
	{
		return this.getVariables().getInt("ACTIVE_CHARACTER_STYLE_" + type + "_" + inventorySlot, 0);
	}
	
	public int getActiveCharacterStyleId(CharacterStyleCategoryType type)
	{
		return type == CharacterStyleCategoryType.APPEARANCE_WEAPON ? this.getVariables().getInt("ACTIVE_CHARACTER_STYLE_" + type + "_" + this._dualInventorySlot) : this.getVariables().getInt("ACTIVE_CHARACTER_STYLE_" + type, 0);
	}
	
	public int getWeaponShiftedDisplayId()
	{
		Item activeWeapon = this._inventory.getPaperdollItem(InventorySlot.RHAND.getSlot());
		if (activeWeapon != null)
		{
			int weaponStyleId = this.getActiveCharacterStyleId(CharacterStyleCategoryType.APPEARANCE_WEAPON, this.getDualInventorySlotPresetId());
			CharacterStyleDataHolder dataHolder = CharacterStylesData.getInstance().getSpecificStyleByCategoryAndId(CharacterStyleCategoryType.APPEARANCE_WEAPON, weaponStyleId);
			int shiftWeaponId = CharacterStylesData.getInstance().getWeaponStyleByStyleId(weaponStyleId);
			return dataHolder != null && weaponStyleId > 0 && dataHolder.getWeaponType() == activeWeapon.getItemType() ? shiftWeaponId : activeWeapon.getVisualId();
		}
		return 0;
	}
	
	public int getDualInventorySlotPresetId()
	{
		return this._dualInventorySlot;
	}
}
