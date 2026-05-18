package net.sf.l2jdev.gameserver.network.clientpackets;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.LoginServerThread;
import net.sf.l2jdev.gameserver.cache.HtmCache;
import net.sf.l2jdev.gameserver.config.AchievementBoxConfig;
import net.sf.l2jdev.gameserver.config.AdenLaboratoryConfig;
import net.sf.l2jdev.gameserver.config.AttendanceRewardsConfig;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.HuntPassConfig;
import net.sf.l2jdev.gameserver.config.MagicLampConfig;
import net.sf.l2jdev.gameserver.config.OlympiadConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.config.RandomCraftConfig;
import net.sf.l2jdev.gameserver.config.RelicSystemConfig;
import net.sf.l2jdev.gameserver.config.ServerConfig;
import net.sf.l2jdev.gameserver.config.custom.FactionSystemConfig;
import net.sf.l2jdev.gameserver.config.custom.OfflineTradeConfig;
import net.sf.l2jdev.gameserver.config.custom.PremiumSystemConfig;
import net.sf.l2jdev.gameserver.config.custom.ScreenWelcomeMessageConfig;
import net.sf.l2jdev.gameserver.config.custom.WeddingConfig;
import net.sf.l2jdev.gameserver.data.enums.CategoryType;
import net.sf.l2jdev.gameserver.data.sql.AnnouncementsTable;
import net.sf.l2jdev.gameserver.data.sql.OfflineTraderTable;
import net.sf.l2jdev.gameserver.data.xml.AdminData;
import net.sf.l2jdev.gameserver.data.xml.BeautyShopData;
import net.sf.l2jdev.gameserver.data.xml.ClanHallData;
import net.sf.l2jdev.gameserver.data.xml.EnchantItemGroupsData;
import net.sf.l2jdev.gameserver.data.xml.MableGameData;
import net.sf.l2jdev.gameserver.data.xml.SkillTreeData;
import net.sf.l2jdev.gameserver.managers.AdenLaboratoryManager;
import net.sf.l2jdev.gameserver.managers.AntiFeedManager;
import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.managers.CoupleManager;
import net.sf.l2jdev.gameserver.managers.CursedWeaponsManager;
import net.sf.l2jdev.gameserver.managers.FortManager;
import net.sf.l2jdev.gameserver.managers.FortSiegeManager;
import net.sf.l2jdev.gameserver.managers.InstanceManager;
import net.sf.l2jdev.gameserver.managers.MailManager;
import net.sf.l2jdev.gameserver.managers.PcCafePointsManager;
import net.sf.l2jdev.gameserver.managers.PetitionManager;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.managers.ServerRestartManager;
import net.sf.l2jdev.gameserver.managers.SiegeManager;
import net.sf.l2jdev.gameserver.managers.WorldExchangeManager;
import net.sf.l2jdev.gameserver.model.Couple;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.appearance.PlayerAppearance;
import net.sf.l2jdev.gameserver.model.actor.enums.player.IllegalActionPunishmentType;
import net.sf.l2jdev.gameserver.model.actor.enums.player.SubclassInfoType;
import net.sf.l2jdev.gameserver.model.actor.enums.player.TeleportWhereType;
import net.sf.l2jdev.gameserver.model.actor.holders.player.AttendanceInfoHolder;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.instancezone.Instance;
import net.sf.l2jdev.gameserver.model.item.enums.BodyPart;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.item.type.EtcItemType;
import net.sf.l2jdev.gameserver.model.olympiad.Olympiad;
import net.sf.l2jdev.gameserver.model.punishment.PunishmentAffect;
import net.sf.l2jdev.gameserver.model.punishment.PunishmentType;
import net.sf.l2jdev.gameserver.model.residences.ClanHall;
import net.sf.l2jdev.gameserver.model.script.Quest;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.model.siege.Fort;
import net.sf.l2jdev.gameserver.model.siege.FortSiege;
import net.sf.l2jdev.gameserver.model.siege.Siege;
import net.sf.l2jdev.gameserver.model.skill.AbnormalVisualEffect;
import net.sf.l2jdev.gameserver.model.variables.PlayerVariables;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.ConnectionState;
import net.sf.l2jdev.gameserver.network.Disconnection;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.ChatType;
import net.sf.l2jdev.gameserver.network.holders.ClientHardwareInfoHolder;
import net.sf.l2jdev.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2jdev.gameserver.network.serverpackets.Die;
import net.sf.l2jdev.gameserver.network.serverpackets.EtcStatusUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.ExAdenaInvenCount;
import net.sf.l2jdev.gameserver.network.serverpackets.ExAutoSoulShot;
import net.sf.l2jdev.gameserver.network.serverpackets.ExBasicActionList;
import net.sf.l2jdev.gameserver.network.serverpackets.ExBeautyItemList;
import net.sf.l2jdev.gameserver.network.serverpackets.ExBrPremiumState;
import net.sf.l2jdev.gameserver.network.serverpackets.ExEnterWorld;
import net.sf.l2jdev.gameserver.network.serverpackets.ExGetBookMarkInfoPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.ExNoticePostArrived;
import net.sf.l2jdev.gameserver.network.serverpackets.ExNotifyPremiumItem;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPCCafePointInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPledgeCoinInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPledgeCount;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPledgeWaitingListAlarm;
import net.sf.l2jdev.gameserver.network.serverpackets.ExQuestItemList;
import net.sf.l2jdev.gameserver.network.serverpackets.ExRotation;
import net.sf.l2jdev.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.ExSubjobInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.ExUnReadMailCount;
import net.sf.l2jdev.gameserver.network.serverpackets.ExUserInfoEquipSlot;
import net.sf.l2jdev.gameserver.network.serverpackets.ExUserInfoInvenWeight;
import net.sf.l2jdev.gameserver.network.serverpackets.ExVitalityEffectInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.ExVoteSystemInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.ExWorldChatCnt;
import net.sf.l2jdev.gameserver.network.serverpackets.HennaInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.ItemDeletionInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.ItemList;
import net.sf.l2jdev.gameserver.network.serverpackets.LeaveWorld;
import net.sf.l2jdev.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.PledgeShowMemberListAll;
import net.sf.l2jdev.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.PledgeSkillList;
import net.sf.l2jdev.gameserver.network.serverpackets.ShortcutInit;
import net.sf.l2jdev.gameserver.network.serverpackets.SkillCoolTime;
import net.sf.l2jdev.gameserver.network.serverpackets.SkillList;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.UserInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.achievementbox.ExSteadyBoxUiInit;
import net.sf.l2jdev.gameserver.network.serverpackets.attendance.ExVipAttendanceItemList;
import net.sf.l2jdev.gameserver.network.serverpackets.attendance.ExVipAttendanceList;
import net.sf.l2jdev.gameserver.network.serverpackets.attendance.ExVipAttendanceNotify;
import net.sf.l2jdev.gameserver.network.serverpackets.collection.ExCollectionActiveEvent;
import net.sf.l2jdev.gameserver.network.serverpackets.collection.ExCollectionInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.dailymission.ExConnectedTimeAndGettableReward;
import net.sf.l2jdev.gameserver.network.serverpackets.dailymission.ExOneDayReceiveRewardList;
import net.sf.l2jdev.gameserver.network.serverpackets.enchant.challengepoint.ExEnchantChallengePointInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.friend.L2FriendList;
import net.sf.l2jdev.gameserver.network.serverpackets.huntpass.HuntPassSimpleInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.limitshop.ExBloodyCoinCount;
import net.sf.l2jdev.gameserver.network.serverpackets.mablegame.ExMableGameUILauncher;
import net.sf.l2jdev.gameserver.network.serverpackets.magiclamp.ExMagicLampInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.olympiad.ExOlympiadInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.pledgedonation.ExPledgeContributionList;
import net.sf.l2jdev.gameserver.network.serverpackets.randomcraft.ExCraftInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.relics.ExRelicsCollectionInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.relics.ExRelicsList;
import net.sf.l2jdev.gameserver.network.serverpackets.settings.ExItemAnnounceSetting;
import net.sf.l2jdev.gameserver.network.serverpackets.subjugation.ExSubjugationSidebar;

public class EnterWorld extends ClientPacket
{
	private static final Map<String, ClientHardwareInfoHolder> TRACE_HWINFO = new ConcurrentHashMap<>();
	private final int[][] _tracert = new int[5][4];

	@Override
	protected void readImpl()
	{
		for (int i = 0; i < 5; i++)
		{
			for (int o = 0; o < 4; o++)
			{
				this._tracert[i][o] = this.readUnsignedByte();
			}
		}

		this.readInt();
		this.readInt();
		this.readInt();
		this.readInt();
		this.readBytes(64);
		this.readInt();
	}

	@Override
	protected void runImpl()
	{
		GameClient client = this.getClient();
		Player player = client.getPlayer();
		if (player == null)
		{
			PacketLogger.warning("EnterWorld failed! player returned 'null'.");
			Disconnection.of(client).storeAndDeleteWith(LeaveWorld.STATIC_PACKET);
		}
		else
		{
			client.setConnectionState(ConnectionState.IN_GAME);
			String[] adress = new String[5];

			for (int i = 0; i < 5; i++)
			{
				adress[i] = this._tracert[i][0] + "." + this._tracert[i][1] + "." + this._tracert[i][2] + "." + this._tracert[i][3];
			}

			LoginServerThread.getInstance().sendClientTracert(player.getAccountName(), adress);
			client.setClientTracert(this._tracert);
			player.sendPacket(new UserInfo(player));
			PlayerVariables vars = player.getVariables();
			if (GeneralConfig.RESTORE_PLAYER_INSTANCE)
			{
				Instance instance = InstanceManager.getInstance().getPlayerInstance(player, false);
				if (instance != null && instance.getId() == vars.getInt("INSTANCE_RESTORE", 0))
				{
					player.setInstance(instance);
				}

				vars.remove("INSTANCE_RESTORE");
			}

			if (!player.isGM())
			{
				player.updatePvpTitleAndColor(false);
			}
			else
			{
				if (GeneralConfig.GM_STARTUP_BUILDER_HIDE && AdminData.getInstance().hasAccess("admin_hide", player.getAccessLevel()))
				{
					player.setHiding(true);
					player.sendSysMessage("hide is default for builder.");
					player.sendSysMessage("FriendAddOff is default for builder.");
					player.sendSysMessage("whisperoff is default for builder.");
				}
				else
				{
					if (GeneralConfig.GM_STARTUP_INVULNERABLE && AdminData.getInstance().hasAccess("admin_invul", player.getAccessLevel()))
					{
						player.setInvul(true);
					}

					if (GeneralConfig.GM_STARTUP_INVISIBLE && AdminData.getInstance().hasAccess("admin_invisible", player.getAccessLevel()))
					{
						player.setInvisible(true);
						player.getEffectList().startAbnormalVisualEffect(AbnormalVisualEffect.STEALTH);
					}

					if (GeneralConfig.GM_STARTUP_SILENCE && AdminData.getInstance().hasAccess("admin_silence", player.getAccessLevel()))
					{
						player.setSilenceMode(true);
					}

					if (GeneralConfig.GM_STARTUP_DIET_MODE && AdminData.getInstance().hasAccess("admin_diet", player.getAccessLevel()))
					{
						player.setDietMode(true);
						player.refreshOverloaded(true);
					}
				}

				if (GeneralConfig.GM_STARTUP_AUTO_LIST && AdminData.getInstance().hasAccess("admin_gmliston", player.getAccessLevel()))
				{
					AdminData.getInstance().addGm(player, false);
				}
				else
				{
					AdminData.getInstance().addGm(player, true);
				}

				if (GeneralConfig.GM_GIVE_SPECIAL_SKILLS)
				{
					SkillTreeData.getInstance().addSkills(player, false);
				}

				if (GeneralConfig.GM_GIVE_SPECIAL_AURA_SKILLS)
				{
					SkillTreeData.getInstance().addSkills(player, true);
				}
			}

			if (player.getCurrentHp() < 0.5)
			{
				player.setDead(true);
			}

			boolean showClanNotice = false;
			Clan clan = player.getClan();
			if (clan != null)
			{
				this.notifyClanMembers(player);
				this.notifySponsorOrApprentice(player);

				for (Siege siege : SiegeManager.getInstance().getSieges())
				{
					if (siege.isInProgress())
					{
						if (siege.checkIsAttacker(clan))
						{
							player.setSiegeState((byte) 1);
							player.setSiegeSide(siege.getCastle().getResidenceId());
						}
						else if (siege.checkIsDefender(clan))
						{
							player.setSiegeState((byte) 2);
							player.setSiegeSide(siege.getCastle().getResidenceId());
						}
					}
				}

				for (FortSiege siegex : FortSiegeManager.getInstance().getSieges())
				{
					if (siegex.isInProgress())
					{
						if (siegex.checkIsAttacker(clan))
						{
							player.setSiegeState((byte) 1);
							player.setSiegeSide(siegex.getFort().getResidenceId());
						}
						else if (siegex.checkIsDefender(clan))
						{
							player.setSiegeState((byte) 2);
							player.setSiegeSide(siegex.getFort().getResidenceId());
						}
					}
				}

				if (clan.getCastleId() > 0)
				{
					Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
					if (castle != null)
					{
						castle.giveResidentialSkills(player);
					}
				}

				if (clan.getFortId() > 0)
				{
					Fort fort = FortManager.getInstance().getFortByOwner(clan);
					if (fort != null)
					{
						fort.giveResidentialSkills(player);
					}
				}

				showClanNotice = clan.isNoticeEnabled();
			}

			if (player.isMercenary())
			{
				player.updateMercenary();
			}

			player.sendPacket(new ExEnterWorld());
			player.getMacros().sendAllMacros();
			player.sendPacket(new ExGetBookMarkInfoPacket(player));
			player.sendPacket(new ItemList(1, player));
			player.sendPacket(new ItemList(2, player));
			player.sendPacket(new ExQuestItemList(1, player));
			player.sendPacket(new ExQuestItemList(2, player));
			player.sendPacket(new ShortcutInit(player));
			player.sendPacket(ExBasicActionList.STATIC_PACKET);
			player.sendPacket(new SkillList());
			player.sendPacket(new HennaInfo(player));
			if (AdenLaboratoryConfig.ADENLAB_ENABLED)
			{
				AdenLaboratoryManager.restorePlayerData(player);
				AdenLaboratoryManager.checkPlayerSkills(player);
				AdenLaboratoryManager.calculateAdenLabCombatPower(player);
			}

			player.sendSkillList();
			player.sendPacket(new EtcStatusUpdate(player));
			player.calculateStatIncreaseSkills();
			if (clan != null)
			{
				clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(player));
				PledgeShowMemberListAll.sendAllTo(player);
				clan.broadcastToOnlineMembers(new ExPledgeCount(clan));
				player.sendPacket(new PledgeSkillList(clan));
				ClanHall ch = ClanHallData.getInstance().getClanHallByClan(clan);
				if (ch != null && ch.getCostFailDay() > 0 && ch.getResidenceId() < 186)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.THE_PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_DEPOSIT_THE_NECESSARY_AMOUNT_OF_ADENA_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW);
					sm.addInt(ch.getLease());
					player.sendPacket(sm);
				}
			}
			else
			{
				player.sendPacket(ExPledgeWaitingListAlarm.STATIC_PACKET);
			}

			player.sendPacket(new ExSubjobInfo(player, SubclassInfoType.NO_CHANGES));
			player.sendPacket(new ExUserInfoInvenWeight(player));
			player.sendPacket(new ExAdenaInvenCount(player));
			player.sendPacket(new ExBloodyCoinCount(player));
			player.sendPacket(new ExPledgeCoinInfo(player));
			player.sendPacket(new ExBrPremiumState(player));
			player.sendPacket(new ExEnchantChallengePointInfo(player));
			if (MailManager.getInstance().hasUnreadPost(player))
			{
				player.sendPacket(new ExUnReadMailCount(player));
			}

			if (FactionSystemConfig.FACTION_SYSTEM_ENABLED)
			{
				if (player.isGood())
				{
					PlayerAppearance appearance = player.getAppearance();
					appearance.setNameColor(FactionSystemConfig.FACTION_GOOD_NAME_COLOR);
					appearance.setTitleColor(FactionSystemConfig.FACTION_GOOD_NAME_COLOR);
					player.sendMessage("Welcome " + player.getName() + ", you are fighting for the " + FactionSystemConfig.FACTION_GOOD_TEAM_NAME + " faction.");
					player.sendPacket(new ExShowScreenMessage("Welcome " + player.getName() + ", you are fighting for the " + FactionSystemConfig.FACTION_GOOD_TEAM_NAME + " faction.", 10000));
				}
				else if (player.isEvil())
				{
					PlayerAppearance appearance = player.getAppearance();
					appearance.setNameColor(FactionSystemConfig.FACTION_EVIL_NAME_COLOR);
					appearance.setTitleColor(FactionSystemConfig.FACTION_EVIL_NAME_COLOR);
					player.sendMessage("Welcome " + player.getName() + ", you are fighting for the " + FactionSystemConfig.FACTION_EVIL_TEAM_NAME + " faction.");
					player.sendPacket(new ExShowScreenMessage("Welcome " + player.getName() + ", you are fighting for the " + FactionSystemConfig.FACTION_EVIL_TEAM_NAME + " faction.", 10000));
				}
			}

			Quest.playerEnter(player);
			if (!PlayerConfig.DISABLE_TUTORIAL)
			{
				player.sendQuestList();
			}

			if (PlayerConfig.PLAYER_SPAWN_PROTECTION > 0)
			{
				player.setSpawnProtection(true);
			}

			player.spawnMe(player.getX(), player.getY(), player.getZ());
			player.sendPacket(new ExRotation(player.getObjectId(), player.getHeading()));
			if (player.isCursedWeaponEquipped())
			{
				CursedWeaponsManager.getInstance().getCursedWeapon(player.getCursedWeaponEquippedId()).cursedOnLogin();
			}

			if (PremiumSystemConfig.PC_CAFE_ENABLED)
			{
				if (player.getPcCafePoints() > 0)
				{
					player.sendPacket(new ExPCCafePointInfo(player.getPcCafePoints(), 0, 1));
				}
				else
				{
					player.sendPacket(new ExPCCafePointInfo());
				}
			}

			player.sendStorageMaxCount();
			player.sendPacket(new ExUserInfoEquipSlot(player));
			player.sendPacket(new L2FriendList(player));
			SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_FRIEND_S1_JUST_LOGGED_IN);
			sm.addString(player.getName());

			for (int id : player.getFriendList())
			{
				WorldObject obj = World.getInstance().findObject(id);
				if (obj != null)
				{
					obj.sendPacket(sm);
				}
			}

			player.sendPacket(SystemMessageId.WELCOME_TO_THE_WORLD_OF_LINEAGE_II);
			AnnouncementsTable.getInstance().showAnnouncements(player);
			if (ServerConfig.SERVER_RESTART_SCHEDULE_ENABLED && ServerConfig.SERVER_RESTART_SCHEDULE_MESSAGE)
			{
				player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, "[SERVER]", "Next restart is scheduled at " + ServerRestartManager.getInstance().getNextRestartTime() + "."));
			}

			if (showClanNotice)
			{
				NpcHtmlMessage notice = new NpcHtmlMessage();
				notice.setFile(player, "data/html/clanNotice.htm");
				notice.replace("%clan_name%", player.getClan().getName());
				notice.replace("%notice_text%", player.getClan().getNotice().replaceAll("(\r\n|\n)", "<br>"));
				notice.disableValidation();
				player.sendPacket(notice);
			}
			else if (GeneralConfig.SERVER_NEWS)
			{
				String serverNews = HtmCache.getInstance().getHtm(player, "data/html/servnews.htm");
				if (serverNews != null)
				{
					player.sendPacket(new NpcHtmlMessage(serverNews));
				}
			}

			if (PlayerConfig.PETITIONING_ALLOWED)
			{
				PetitionManager.getInstance().checkPetitionMessages(player);
			}

			player.onPlayerEnter();
			player.sendPacket(new SkillCoolTime(player));
			player.sendPacket(new ExVoteSystemInfo(player));
			if (player.isAlikeDead())
			{
				player.sendPacket(new Die(player));
			}

			for (Item item : player.getInventory().getItems())
			{
				if (item.isTimeLimitedItem())
				{
					item.scheduleLifeTimeTask();
				}

				if (item.isShadowItem() && item.isEquipped())
				{
					item.decreaseMana(false);
				}
			}

			for (Item whItem : player.getWarehouse().getItems())
			{
				if (whItem.isTimeLimitedItem())
				{
					whItem.scheduleLifeTimeTask();
				}
			}

			if (player.getClanJoinExpiryTime() > System.currentTimeMillis())
			{
				player.sendPacket(SystemMessageId.YOU_ARE_DISMISSED_FROM_A_CLAN_YOU_CANNOT_JOIN_ANOTHER_FOR_24_H);
			}

			if (player.getInventory().getItemByItemId(93331) != null)
			{
				Fort fort = FortManager.getInstance().getFort(player);
				if (fort != null)
				{
					FortSiegeManager.getInstance().dropCombatFlag(player, fort.getResidenceId());
				}
				else
				{
					BodyPart bodyPart = BodyPart.fromItem(player.getInventory().getItemByItemId(93331));
					player.getInventory().unEquipItemInBodySlot(bodyPart);
					player.destroyItem(ItemProcessType.DESTROY, player.getInventory().getItemByItemId(93331), null, true);
				}
			}

			if (!player.isGM() && player.isInsideZone(ZoneId.SIEGE) && (!player.isInSiege() || player.getSiegeState() < 2))
			{
				player.teleToLocation(TeleportWhereType.TOWN);
			}

			if (PlayerConfig.OVER_ENCHANT_PROTECTION && !player.isGM())
			{
				boolean punish = false;

				for (Item item : player.getInventory().getItems())
				{
					if (item.isEquipable() && (item.isWeapon() && item.getEnchantLevel() > EnchantItemGroupsData.getInstance().getMaxWeaponEnchant() || item.getTemplate().getType2() == 2 && item.getEnchantLevel() > EnchantItemGroupsData.getInstance().getMaxAccessoryEnchant() || item.isArmor() && item.getTemplate().getType2() != 2 && item.getEnchantLevel() > EnchantItemGroupsData.getInstance().getMaxArmorEnchant()))
					{
						PacketLogger.info("Over-enchanted (+" + item.getEnchantLevel() + ") " + item + " has been removed from " + player);
						player.getInventory().destroyItem(ItemProcessType.DESTROY, item, player, null);
						punish = true;
					}
				}

				if (punish && PlayerConfig.OVER_ENCHANT_PUNISHMENT != IllegalActionPunishmentType.NONE)
				{
					player.sendMessage("[Server]: You have over-enchanted items!");
					player.sendMessage("[Server]: Respect our server rules.");
					player.sendPacket(new ExShowScreenMessage("You have over-enchanted items!", 6000));
					PunishmentManager.handleIllegalPlayerAction(player, player.getName() + " has over-enchanted items.", PlayerConfig.OVER_ENCHANT_PUNISHMENT);
				}
			}

			if (player.getInventory().getItemByItemId(8190) != null && !player.isCursedWeaponEquipped())
			{
				player.destroyItem(ItemProcessType.DESTROY, player.getInventory().getItemByItemId(8190), null, true);
			}

			if (player.getInventory().getItemByItemId(8689) != null && !player.isCursedWeaponEquipped())
			{
				player.destroyItem(ItemProcessType.DESTROY, player.getInventory().getItemByItemId(8689), null, true);
			}

			if (GeneralConfig.ALLOW_MAIL && MailManager.getInstance().hasUnreadPost(player))
			{
				player.sendPacket(ExNoticePostArrived.valueOf(false));
			}

			if (ScreenWelcomeMessageConfig.WELCOME_MESSAGE_ENABLED)
			{
				player.sendPacket(new ExShowScreenMessage(ScreenWelcomeMessageConfig.WELCOME_MESSAGE_TEXT, ScreenWelcomeMessageConfig.WELCOME_MESSAGE_TIME));
			}

			if (!player.getPremiumItemList().isEmpty())
			{
				player.sendPacket(ExNotifyPremiumItem.STATIC_PACKET);
			}

			if ((OfflineTradeConfig.OFFLINE_TRADE_ENABLE || OfflineTradeConfig.OFFLINE_CRAFT_ENABLE) && OfflineTradeConfig.STORE_OFFLINE_TRADE_IN_REALTIME)
			{
				OfflineTraderTable.getInstance().onTransaction(player, true, false);
			}

			if (vars.getBoolean("EXPOFF", false))
			{
				player.disableExpGain();
				player.sendMessage("Experience gain is disabled.");
			}

			if (OlympiadConfig.OLYMPIAD_ENABLED && Olympiad.getInstance().inCompPeriod())
			{
				player.sendPacket(new ExOlympiadInfo(1, Olympiad.getInstance().getRemainingTime()));
			}
			else
			{
				player.sendPacket(new ExOlympiadInfo(0, 0));
			}

			player.broadcastUserInfo();
			if (BeautyShopData.getInstance().hasBeautyData(player.getRace(), player.getAppearance().getSexType()))
			{
				player.sendPacket(new ExBeautyItemList(player));
			}

			if (GeneralConfig.ENABLE_WORLD_CHAT)
			{
				player.sendPacket(new ExWorldChatCnt(player));
			}

			player.getMissionLevelProgress();
			player.sendPacket(new ExConnectedTimeAndGettableReward(player));
			player.sendPacket(new ExOneDayReceiveRewardList(player, true));
			player.sendPacket(new ExAutoSoulShot(0, true, 0));
			player.sendPacket(new ExAutoSoulShot(0, true, 1));
			player.sendPacket(new ExAutoSoulShot(0, true, 2));
			player.sendPacket(new ExAutoSoulShot(0, true, 3));
			player.restoreAutoShortcuts();
			player.restoreAutoSettings();
			player.getClientSettings();
			player.sendPacket(new ExItemAnnounceSetting(player.getClientSettings().isAnnounceDisabled()));
			player.restoreChatBackground();
			if (!player.getEffectList().getCurrentAbnormalVisualEffects().isEmpty())
			{
				player.updateAbnormalVisualEffects();
			}

			if (player.isDeathKnight())
			{
				player.setDeathPoints(500);
				player.setDeathPoints(vars.getInt("DEATH_POINT_COUNT", 0));
			}
			else if (player.isVanguard())
			{
				player.setBeastPoints(1000);
				player.setBeastPoints(vars.getInt("BEAST_POINT_COUNT", 0));
			}
			else if (player.isAssassin() && player.isInCategory(CategoryType.FOURTH_CLASS_GROUP))
			{
				player.setAssassinationPoints(vars.getInt("ASSASSINATION_POINT_COUNT", 0));
			}
			else if (player.isInCategory(CategoryType.HIGH_ELF_TEMPLAR))
			{
				player.setLightPoints(1);
				player.setLightPoints(vars.getInt("LIGHT_POINT_COUNT", 0));
			}
			else if (player.isWarg())
			{
				player.setWolfPoints(1000);
				player.setWolfPoints(vars.getInt("WOLF_POINT_COUNT", 0));
			}

			if (PlayerConfig.ENABLE_VITALITY)
			{
				player.sendPacket(new ExVitalityEffectInfo(player));
			}

			if (MagicLampConfig.ENABLE_MAGIC_LAMP)
			{
				player.sendPacket(new ExMagicLampInfo(player));
			}

			if (RandomCraftConfig.ENABLE_RANDOM_CRAFT)
			{
				player.sendPacket(new ExCraftInfo(player));
			}

			if (HuntPassConfig.ENABLE_HUNT_PASS)
			{
				player.sendPacket(new HuntPassSimpleInfo(player));
			}

			if (AchievementBoxConfig.ENABLE_ACHIEVEMENT_BOX)
			{
				player.sendPacket(new ExSteadyBoxUiInit(player));
			}

			if (player.getLevel() >= 40 && player.getPlayerClass().level() > 1)
			{
				player.initElementalSpirits();
			}

			for (int category = 1; category <= 7; category++)
			{
				player.sendPacket(new ExCollectionInfo(player, category));
			}

			player.sendPacket(new ExCollectionActiveEvent());
			player.sendPacket(new ExSubjugationSidebar(player, player.getPurgePoints().get(player.getPurgeLastCategory())));
			if (RelicSystemConfig.RELIC_SYSTEM_ENABLED)
			{
				player.sendPacket(new ExRelicsList(player));
				player.sendPacket(new ExRelicsCollectionInfo(player));
			}

			player.sendPacket(new ItemDeletionInfo());
			player.applyKarmaPenalty();
			SiegeManager.getInstance().sendSiegeInfo(player);
			Item agathion = player.getInventory().unEquipItemInBodySlot(BodyPart.AGATHION);
			if (agathion != null)
			{
				player.getInventory().equipItemAndRecord(agathion);
			}

			Item leftHandItem = player.getInventory().getPaperdollItem(7);
			if (leftHandItem != null && (leftHandItem.getItemType() == EtcItemType.ARROW || leftHandItem.getItemType() == EtcItemType.BOLT || leftHandItem.getItemType() == EtcItemType.ELEMENTAL_ORB))
			{
				player.getInventory().unEquipItemInBodySlot(BodyPart.L_HAND);
			}

			if (MableGameData.getInstance().isEnabled())
			{
				player.sendPacket(ExMableGameUILauncher.STATIC_PACKET);
			}

			WorldExchangeManager.getInstance().checkPlayerSellAlarm(player);
			player.restoreDualInventory();
			if (AttendanceRewardsConfig.ENABLE_ATTENDANCE_REWARDS)
			{
				AttendanceInfoHolder attendanceInfo = player.getAttendanceInfo();
				if (attendanceInfo.isRewardAvailable())
				{
					player.setAttendanceDelay(AttendanceRewardsConfig.ATTENDANCE_REWARD_DELAY);
				}

				ThreadPool.schedule(() -> {
					if (attendanceInfo.isRewardAvailable())
					{
						int lastRewardIndex = attendanceInfo.getRewardIndex() + 1;
						player.sendPacket(new ExShowScreenMessage("Your attendance day " + lastRewardIndex + " reward is ready.", 2, 7000, 0, true, true));
						player.sendMessage("Your attendance day " + lastRewardIndex + " reward is ready.");
						player.sendMessage("Click on General Menu -> Attendance Check.");
						if (AttendanceRewardsConfig.ATTENDANCE_POPUP_WINDOW)
						{
							player.sendPacket(new ExVipAttendanceList(player));
						}

						player.sendPacket(new ExVipAttendanceNotify());
					}
				}, AttendanceRewardsConfig.ATTENDANCE_REWARD_DELAY * 60 * 1000);
				if (AttendanceRewardsConfig.ATTENDANCE_POPUP_START)
				{
					player.sendPacket(new ExVipAttendanceList(player));
				}

				player.sendPacket(new ExVipAttendanceItemList());
			}

			if (ServerConfig.HARDWARE_INFO_ENABLED)
			{
				ThreadPool.schedule(() -> {
					StringBuilder sb = new StringBuilder();

					for (int[] i : this._tracert)
					{
						for (int j : i)
						{
							sb.append(j);
							sb.append(".");
						}
					}

					String trace = sb.toString();
					ClientHardwareInfoHolder hwInfo = client.getHardwareInfo();
					if (hwInfo != null)
					{
						hwInfo.store(player);
						TRACE_HWINFO.put(trace, hwInfo);
					}
					else
					{
						hwInfo = TRACE_HWINFO.get(trace);
						if (hwInfo != null)
						{
							hwInfo.store(player);
							client.setHardwareInfo(hwInfo);
						}
						else
						{
							String storedInfo = player.getAccountVariables().getString("HWID", "");
							if (!storedInfo.isEmpty())
							{
								hwInfo = new ClientHardwareInfoHolder(storedInfo);
								TRACE_HWINFO.put(trace, hwInfo);
								client.setHardwareInfo(hwInfo);
							}
						}
					}

					if (hwInfo != null && PunishmentManager.getInstance().hasPunishment(hwInfo.getMacAddress(), PunishmentAffect.HWID, PunishmentType.BAN))
					{
						Disconnection.of(client).storeAndDeleteWith(LeaveWorld.STATIC_PACKET);
					}
					else
					{
						if (ServerConfig.KICK_MISSING_HWID && hwInfo == null)
						{
							Disconnection.of(client).storeAndDeleteWith(LeaveWorld.STATIC_PACKET);
						}
						else if (ServerConfig.MAX_PLAYERS_PER_HWID > 0)
						{
							int count = 0;

							for (Player plr : World.getInstance().getPlayers())
							{
								if (plr.isOnlineInt() == 1)
								{
									ClientHardwareInfoHolder hwi = plr.getClient().getHardwareInfo();
									if (hwi != null && hwi.equals(hwInfo))
									{
										count++;
									}
								}
							}

							if (count > ServerConfig.MAX_PLAYERS_PER_HWID)
							{
								Disconnection.of(client).storeAndDeleteWith(LeaveWorld.STATIC_PACKET);
							}
						}
					}
				}, 5000L);
			}

			ThreadPool.schedule(() -> {
				if (player.isChatBanned())
				{
					player.getEffectList().startAbnormalVisualEffect(AbnormalVisualEffect.NO_CHAT);
				}
			}, 5500L);
			AntiFeedManager.getInstance().removePlayer(4, player);
			player.setEnteredWorld();
			if (WeddingConfig.ALLOW_WEDDING)
			{
				int playerObjectId = player.getObjectId();

				for (Couple couple : CoupleManager.getInstance().getCouples())
				{
					if (couple.getPlayer1Id() == playerObjectId || couple.getPlayer2Id() == playerObjectId)
					{
						if (couple.getMaried())
						{
							player.setMarried(true);
						}

						player.setCoupleId(couple.getId());
						if (couple.getPlayer1Id() == playerObjectId)
						{
							player.setPartnerId(couple.getPlayer2Id());
						}
						else
						{
							player.setPartnerId(couple.getPlayer1Id());
						}
					}
				}

				int partnerId = player.getPartnerId();
				if (partnerId != 0)
				{
					Player partner = World.getInstance().getPlayer(partnerId);
					if (partner != null)
					{
						partner.sendMessage("Your partner has logged in.");
					}
				}
			}

			if ((player.hasPremiumStatus() || !PremiumSystemConfig.PC_CAFE_ONLY_PREMIUM) && PremiumSystemConfig.PC_CAFE_RETAIL_LIKE)
			{
				PcCafePointsManager.getInstance().run(player);
			}

			player.getVariables().remove("LAST_HUNTING_ZONE_ID");
		}
	}

	protected void notifyClanMembers(Player player)
	{
		Clan clan = player.getClan();
		if (clan != null)
		{
			clan.getClanMember(player.getObjectId()).setPlayer(player);
			SystemMessage msg = new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_HAS_LOGGED_IN);
			msg.addString(player.getName());
			clan.broadcastToOtherOnlineMembers(msg, player);
			clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(player), player);
			player.sendPacket(new ExPledgeContributionList(clan.getMembers()));
		}
	}

	protected void notifySponsorOrApprentice(Player player)
	{
		if (player.getSponsor() != 0)
		{
			Player sponsor = World.getInstance().getPlayer(player.getSponsor());
			if (sponsor != null)
			{
				SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_MENTEE_S1_HAS_LOGGED_IN);
				msg.addString(player.getName());
				sponsor.sendPacket(msg);
			}
		}
		else if (player.getApprentice() != 0)
		{
			Player apprentice = World.getInstance().getPlayer(player.getApprentice());
			if (apprentice != null)
			{
				SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_SPONSOR_C1_HAS_LOGGED_IN);
				msg.addString(player.getName());
				apprentice.sendPacket(msg);
			}
		}
	}
}
