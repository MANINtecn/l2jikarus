package net.sf.l2jdev.gameserver.model.actor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.cache.HtmCache;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.NpcConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.config.RatesConfig;
import net.sf.l2jdev.gameserver.config.ServerConfig;
import net.sf.l2jdev.gameserver.config.custom.FakePlayersConfig;
import net.sf.l2jdev.gameserver.config.custom.PrivateStoreRangeConfig;
import net.sf.l2jdev.gameserver.config.custom.PvpAnnounceConfig;
import net.sf.l2jdev.gameserver.config.custom.PvpRewardItemConfig;
import net.sf.l2jdev.gameserver.data.xml.ClanHallData;
import net.sf.l2jdev.gameserver.data.xml.DynamicExpRateData;
import net.sf.l2jdev.gameserver.data.xml.ItemData;
import net.sf.l2jdev.gameserver.handler.BypassHandler;
import net.sf.l2jdev.gameserver.handler.IBypassHandler;
import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.managers.DatabaseSpawnManager;
import net.sf.l2jdev.gameserver.managers.FortManager;
import net.sf.l2jdev.gameserver.managers.ItemManager;
import net.sf.l2jdev.gameserver.managers.WalkingManager;
import net.sf.l2jdev.gameserver.managers.ZoneManager;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.Spawn;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Race;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Team;
import net.sf.l2jdev.gameserver.model.actor.enums.npc.AISkillScope;
import net.sf.l2jdev.gameserver.model.actor.enums.npc.AIType;
import net.sf.l2jdev.gameserver.model.actor.enums.npc.MpRewardAffectType;
import net.sf.l2jdev.gameserver.model.actor.enums.npc.RaidBossStatus;
import net.sf.l2jdev.gameserver.model.actor.enums.player.ElementalSpiritType;
import net.sf.l2jdev.gameserver.model.actor.holders.npc.FakePlayerHolder;
import net.sf.l2jdev.gameserver.model.actor.instance.Fisherman;
import net.sf.l2jdev.gameserver.model.actor.instance.Guardian;
import net.sf.l2jdev.gameserver.model.actor.instance.Merchant;
import net.sf.l2jdev.gameserver.model.actor.instance.Shadow;
import net.sf.l2jdev.gameserver.model.actor.instance.Teleporter;
import net.sf.l2jdev.gameserver.model.actor.instance.Warehouse;
import net.sf.l2jdev.gameserver.model.actor.stat.NpcStat;
import net.sf.l2jdev.gameserver.model.actor.status.NpcStatus;
import net.sf.l2jdev.gameserver.model.actor.tasks.npc.MpRewardTask;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcCanBeSeen;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcDespawn;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcEventReceived;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcSkillFinished;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcSpawn;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcTeleport;
import net.sf.l2jdev.gameserver.model.events.returns.TerminateReturn;
import net.sf.l2jdev.gameserver.model.groups.Party;
import net.sf.l2jdev.gameserver.model.instancezone.Instance;
import net.sf.l2jdev.gameserver.model.item.Weapon;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.enums.ShotType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.residences.ClanHall;
import net.sf.l2jdev.gameserver.model.script.QuestTimer;
import net.sf.l2jdev.gameserver.model.script.timers.TimerHolder;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.model.siege.Fort;
import net.sf.l2jdev.gameserver.model.siege.TaxType;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.spawns.NpcSpawnTemplate;
import net.sf.l2jdev.gameserver.model.stats.Formulas;
import net.sf.l2jdev.gameserver.model.variables.NpcVariables;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.model.zone.ZoneType;
import net.sf.l2jdev.gameserver.model.zone.type.ClanHallZone;
import net.sf.l2jdev.gameserver.model.zone.type.TaxZone;
import net.sf.l2jdev.gameserver.network.NpcStringId;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.ChatType;
import net.sf.l2jdev.gameserver.network.enums.UserInfoType;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.ExChangeNpcState;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPrivateStoreSetWholeMsg;
import net.sf.l2jdev.gameserver.network.serverpackets.ExShowChannelingEffect;
import net.sf.l2jdev.gameserver.network.serverpackets.FakePlayerInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2jdev.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.NpcInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.NpcInfoAbnormalVisualEffect;
import net.sf.l2jdev.gameserver.network.serverpackets.NpcSay;
import net.sf.l2jdev.gameserver.network.serverpackets.PrivateStoreMsgBuy;
import net.sf.l2jdev.gameserver.network.serverpackets.PrivateStoreMsgSell;
import net.sf.l2jdev.gameserver.network.serverpackets.RecipeShopMsg;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerObjectInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.taskmanagers.DecayTaskManager;
import net.sf.l2jdev.gameserver.taskmanagers.ItemsAutoDestroyTaskManager;
import net.sf.l2jdev.gameserver.util.Broadcast;

public class Npc extends Creature
{
	public static final int INTERACTION_DISTANCE = 250;
	public static final int RANDOM_ITEM_DROP_LIMIT = 70;
	private static final Set<Integer> CREATURE_SEE_IDS = ConcurrentHashMap.newKeySet();
	private Spawn _spawn;
	private boolean _isBusy = false;
	private volatile boolean _isDecayed = false;
	private boolean _isAutoAttackable = false;
	private long _lastSocialBroadcast = 0L;
	public static final int MINIMUM_SOCIAL_INTERVAL = 6000;
	private boolean _isRandomAnimationEnabled = true;
	private boolean _isRandomWalkingEnabled = true;
	private boolean _isWalker = false;
	private boolean _isTalkable = this.getTemplate().isTalkable();
	private final boolean _isQuestMonster = this.getTemplate().isQuestMonster();
	private final boolean _isFakePlayer = this.getTemplate().isFakePlayer();
	private int _currentLHandId;
	private int _currentRHandId;
	private int _currentEnchant;
	private float _currentCollisionHeight;
	private float _currentCollisionRadius;
	private int _soulshotamount = 0;
	private int _spiritshotamount = 0;
	private int _displayEffect = 0;
	private int _killingBlowWeaponId;
	private int _cloneObjId;
	private int _clanId;
	private NpcStringId _titleString;
	private NpcStringId _nameString;
	private StatSet _params;
	private volatile int _scriptValue = 0;
	private RaidBossStatus _raidStatus;
	private TaxZone _taxZone = null;
	private final List<QuestTimer> _questTimers = new ArrayList<>();
	private final List<TimerHolder<?>> _timerHolders = new ArrayList<>();
	private final Set<Player> _talkedPlayers = ConcurrentHashMap.newKeySet(0);

	public Npc(NpcTemplate template)
	{
		super(template);
		this.setInstanceType(InstanceType.Npc);
		this.initCharStatusUpdateValues();
		this.setTargetable(this.getTemplate().isTargetable());
		this._currentLHandId = this.getTemplate().getLHandId();
		this._currentRHandId = this.getTemplate().getRHandId();
		this._currentEnchant = NpcConfig.ENABLE_RANDOM_ENCHANT_EFFECT ? Rnd.get(4, 21) : this.getTemplate().getWeaponEnchant();
		this._currentCollisionHeight = this.getTemplate().getFCollisionHeight();
		this._currentCollisionRadius = this.getTemplate().getFCollisionRadius();
		this.setFlying(template.isFlying());
		this.initStatusUpdateCache();
	}

	public void onRandomAnimation(int animationId)
	{
		long now = System.currentTimeMillis();
		if (now - this._lastSocialBroadcast > 6000L)
		{
			this._lastSocialBroadcast = now;
			this.broadcastSocialAction(animationId);
		}
	}

	public boolean hasRandomAnimation()
	{
		return GeneralConfig.MAX_NPC_ANIMATION > 0 && this._isRandomAnimationEnabled && this.getAiType() != AIType.CORPSE;
	}

	public void setRandomAnimation(boolean value)
	{
		this._isRandomAnimationEnabled = value;
	}

	public boolean isRandomAnimationEnabled()
	{
		return this._isRandomAnimationEnabled;
	}

	public void setRandomWalking(boolean enabled)
	{
		this._isRandomWalkingEnabled = enabled;
	}

	public boolean isRandomWalkingEnabled()
	{
		return this._isRandomWalkingEnabled;
	}

	@Override
	public NpcStat getStat()
	{
		return (NpcStat) super.getStat();
	}

	@Override
	public void initCharStat()
	{
		this.setStat(new NpcStat(this));
	}

	@Override
	public NpcStatus getStatus()
	{
		return (NpcStatus) super.getStatus();
	}

	@Override
	public void initCharStatus()
	{
		this.setStatus(new NpcStatus(this));
	}

	@Override
	public NpcTemplate getTemplate()
	{
		return (NpcTemplate) super.getTemplate();
	}

	@Override
	public int getId()
	{
		return this.getTemplate().getId();
	}

	@Override
	public boolean canBeAttacked()
	{
		return NpcConfig.ALT_ATTACKABLE_NPCS;
	}

	@Override
	public int getLevel()
	{
		return this.getTemplate().getLevel();
	}

	public boolean isAggressive()
	{
		return false;
	}

	public int getAggroRange()
	{
		return this.getTemplate().getAggroRange();
	}

	public boolean isInMyClan(Npc npc)
	{
		return this.getTemplate().isClan(npc.getTemplate().getClans());
	}

	@Override
	public boolean isUndead()
	{
		return this.getTemplate().getRace() == Race.UNDEAD;
	}

	@Override
	public void updateAbnormalVisualEffects()
	{
		World.getInstance().forEachVisibleObject(this, Player.class, player -> {
			if (this.isVisibleFor(player))
			{
				if (this._isFakePlayer)
				{
					player.sendPacket(new FakePlayerInfo(this));
					FakePlayerHolder fakePlayerInfo = this.getTemplate().getFakePlayerInfo();
					int storeType = fakePlayerInfo.getPrivateStoreType();
					if (storeType > 0)
					{
						String message = fakePlayerInfo.getPrivateStoreMessage();
						if (!message.isEmpty())
						{
							switch (storeType)
							{
								case 1:
									player.sendPacket(new PrivateStoreMsgSell(this.getObjectId(), message));
								case 2:
								case 4:
								case 6:
								case 7:
								default:
									break;
								case 3:
									player.sendPacket(new PrivateStoreMsgBuy(this.getObjectId(), message));
									break;
								case 5:
									player.sendPacket(new RecipeShopMsg(this.getObjectId(), message));
									break;
								case 8:
									player.sendPacket(new ExPrivateStoreSetWholeMsg(this.getObjectId(), message));
							}
						}
					}
				}
				else if (this.getRunSpeed() == 0.0)
				{
					player.sendPacket(new ServerObjectInfo(this, player));
				}
				else
				{
					player.sendPacket(new NpcInfoAbnormalVisualEffect(this));
				}
			}
		});
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		if (attacker == null)
		{
			return false;
		}
		else if (attacker.isSummon() || attacker instanceof Shadow || attacker instanceof Guardian)
		{
			return true;
		}
		else if (!this.isTargetable())
		{
			return false;
		}
		else if (!attacker.isAttackable())
		{
			return this._isAutoAttackable;
		}
		else if (this.isInMyClan(attacker.asNpc()))
		{
			return false;
		}
		else
		{
			return ((NpcTemplate) attacker.getTemplate()).isChaos() ? true : attacker.asAttackable().getHating(this) > 0L;
		}
	}

	public void setAutoAttackable(boolean flag)
	{
		this._isAutoAttackable = flag;
	}

	public int getLeftHandItem()
	{
		return this._currentLHandId;
	}

	public int getRightHandItem()
	{
		return this._currentRHandId;
	}

	public int getEnchantEffect()
	{
		return this._currentEnchant;
	}

	public boolean isBusy()
	{
		return this._isBusy;
	}

	public void setBusy(boolean isBusy)
	{
		this._isBusy = isBusy;
	}

	public boolean isWarehouse()
	{
		return false;
	}

	public boolean canTarget(Player player)
	{
		if (player.isControlBlocked())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		else if (player.isLockedTarget() && player.getLockedTarget() != this)
		{
			player.sendPacket(SystemMessageId.FAILED_TO_CHANGE_ENMITY);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		else
		{
			return true;
		}
	}

	public boolean canInteract(Player player)
	{
		if (player.isCastingNow())
		{
			return false;
		}
		else if (player.isDead() || player.isFakeDeath())
		{
			return false;
		}
		else if (player.isSitting())
		{
			return false;
		}
		else if (player.isInStoreMode())
		{
			return false;
		}
		else if (!this.isInsideRadius3D(player, 250))
		{
			return false;
		}
		else
		{
			return player.getInstanceWorld() != this.getInstanceWorld() ? false : !this._isBusy;
		}
	}

	public void setTaxZone(TaxZone zone)
	{
		this._taxZone = zone != null && !this.isInInstance() ? zone : null;
	}

	public Castle getTaxCastle()
	{
		return this._taxZone != null ? this._taxZone.getCastle() : null;
	}

	public double getCastleTaxRate(TaxType type)
	{
		Castle giran = CastleManager.getInstance().getCastleById(3);
		Castle goddart = CastleManager.getInstance().getCastleById(7);
		double giranTaxPercent = giran == null ? 0.0 : giran.getTaxPercent(type);
		double goddartTaxPercent = goddart == null ? 0.0 : goddart.getTaxPercent(type);
		double taxPercent = giranTaxPercent + goddartTaxPercent;
		return taxPercent / 100.0;
	}

	public void handleTaxPayment(long amount)
	{
		Castle giran = CastleManager.getInstance().getCastleById(3);
		double giranTax = giran.getTaxPercent(TaxType.BUY) / 100.0;
		Castle goddart = CastleManager.getInstance().getCastleById(7);
		double goddartTax = goddart.getTaxPercent(TaxType.BUY) / 100.0;
		if (giranTax == 0.0)
		{
			goddart.addToTreasuryTemp((long) (amount * goddartTax));
		}
		else if (goddartTax == 0.0)
		{
			giran.addToTreasuryTemp((long) (amount * giranTax));
		}
		else
		{
			goddart.addToTreasuryTemp((long) (amount * goddartTax));
			giran.addToTreasuryTemp((long) (amount * giranTax));
		}
	}

	public Castle getCastle()
	{
		return CastleManager.getInstance().findNearestCastle(this);
	}

	public ClanHall getClanHall()
	{
		if (this.getId() == 33360)
		{
			for (ZoneType zone : ZoneManager.getInstance().getZones(this))
			{
				if (zone instanceof ClanHallZone)
				{
					ClanHall clanHall = ClanHallData.getInstance().getClanHallById(((ClanHallZone) zone).getResidenceId());
					if (clanHall != null)
					{
						return clanHall;
					}
				}
			}
		}

		return ClanHallData.getInstance().getClanHallByNpcId(this.getId());
	}

	public Castle getCastle(long maxDistance)
	{
		return CastleManager.getInstance().findNearestCastle(this, maxDistance);
	}

	public Fort getFort()
	{
		return FortManager.getInstance().findNearestFort(this);
	}

	public Fort getFort(long maxDistance)
	{
		return FortManager.getInstance().findNearestFort(this, maxDistance);
	}

	public void onBypassFeedback(Player player, String command)
	{
		if (this.canInteract(player))
		{
			IBypassHandler handler = BypassHandler.getInstance().getHandler(command);
			if (handler != null)
			{
				handler.onCommand(command, player, this);
			}
			else
			{
				LOGGER.info(this.getClass().getSimpleName() + ": Unknown NPC bypass: \"" + command + "\" NpcId: " + this.getId());
			}
		}
	}

	@Override
	public Item getActiveWeaponInstance()
	{
		return null;
	}

	@Override
	public Weapon getActiveWeaponItem()
	{
		return null;
	}

	@Override
	public Item getSecondaryWeaponInstance()
	{
		return null;
	}

	@Override
	public Weapon getSecondaryWeaponItem()
	{
		return null;
	}

	public String getHtmlPath(int npcId, int value, Player player)
	{
		String pom = "";
		if (value == 0)
		{
			pom = Integer.toString(npcId);
		}
		else
		{
			pom = npcId + "-" + value;
		}

		String temp = "data/html/default/" + pom + ".htm";
		if (GeneralConfig.HTM_CACHE)
		{
			if (HtmCache.getInstance().contains(temp))
			{
				return temp;
			}
		}
		else
		{
			File file = new File(ServerConfig.DATAPACK_ROOT, temp);
			if (file.isFile())
			{
				String lowerCaseName = file.getName().toLowerCase();
				if (lowerCaseName.endsWith(".htm") || lowerCaseName.endsWith(".html"))
				{
					return temp;
				}
			}
		}

		return "data/html/npcdefault.htm";
	}

	public void showChatWindow(Player player)
	{
		if (!this._talkedPlayers.contains(player))
		{
			this._talkedPlayers.add(player);
			ThreadPool.schedule(() -> this._talkedPlayers.remove(player), 1000L);
			this.showChatWindow(player, 0);
		}
	}

	private boolean showPkDenyChatWindow(Player player, String type)
	{
		String html = HtmCache.getInstance().getHtm(player, "data/html/" + type + "/" + this.getId() + "-pk.htm");
		if (html != null)
		{
			html = html.replace("%objectId%", String.valueOf(this.getObjectId()));
			player.sendPacket(new NpcHtmlMessage(this.getObjectId(), html));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return true;
		}
		return false;
	}

	public void showChatWindow(Player player, int value)
	{
		if (!this._isTalkable)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			if (player.getReputation() < 0)
			{
				if (!PlayerConfig.ALT_GAME_KARMA_PLAYER_CAN_SHOP && this instanceof Merchant)
				{
					if (this.showPkDenyChatWindow(player, "merchant"))
					{
						return;
					}
				}
				else if (!PlayerConfig.ALT_GAME_KARMA_PLAYER_CAN_USE_GK && this instanceof Teleporter)
				{
					if (this.showPkDenyChatWindow(player, "teleporter"))
					{
						return;
					}
				}
				else if (!PlayerConfig.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && this instanceof Warehouse)
				{
					if (this.showPkDenyChatWindow(player, "warehouse"))
					{
						return;
					}
				}
				else if (!PlayerConfig.ALT_GAME_KARMA_PLAYER_CAN_SHOP && this instanceof Fisherman && this.showPkDenyChatWindow(player, "fisherman"))
				{
					return;
				}
			}

			if (!this.getTemplate().isType("Auctioneer") || value != 0)
			{
				int npcId = this.getTemplate().getId();
				String filename;
				switch (npcId)
				{
					case 30298:
						if (player.isAcademyMember())
						{
							filename = this.getHtmlPath(npcId, 1, player);
						}
						else
						{
							filename = this.getHtmlPath(npcId, value, player);
						}
						break;
					case 31690:
					case 31769:
					case 31770:
					case 31771:
					case 31772:
						if (!player.isHero() && !player.isNoble())
						{
							filename = this.getHtmlPath(npcId, value, player);
						}
						else
						{
							filename = "data/html/olympiad/hero_main.htm";
						}
						break;
					default:
						if (npcId >= 31093 && npcId <= 31094 || npcId >= 31172 && npcId <= 31201 || npcId >= 31239 && npcId <= 31254)
						{
							return;
						}

						filename = this.getHtmlPath(npcId, value, player);
				}

				NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
				html.setFile(player, filename);
				html.replace("%npcname%", this.getName());
				html.replace("%objectId%", String.valueOf(this.getObjectId()));
				player.sendPacket(html);
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}

	public void showChatWindow(Player player, String filename)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
		html.setFile(player, filename);
		html.replace("%objectId%", String.valueOf(this.getObjectId()));
		player.sendPacket(html);
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	public double getExpReward(int level)
	{
		if (DynamicExpRateData.getInstance().isEnabled())
		{
			return this.getTemplate().getExp() * DynamicExpRateData.getInstance().getDynamicExpRate(level);
		}
		Instance instance = this.getInstanceWorld();
		float rateMul = instance != null ? instance.getExpRate() : RatesConfig.RATE_XP;
		return this.getTemplate().getExp() * rateMul;
	}

	public double getSpReward(int level)
	{
		if (DynamicExpRateData.getInstance().isEnabled())
		{
			return this.getTemplate().getSP() * DynamicExpRateData.getInstance().getDynamicSpRate(level);
		}
		Instance instance = this.getInstanceWorld();
		float rateMul = instance != null ? instance.getSPRate() : RatesConfig.RATE_SP;
		return this.getTemplate().getSP() * rateMul;
	}

	public long getAttributeExp()
	{
		return this.getTemplate().getAttributeExp();
	}

	@Override
	public ElementalSpiritType getElementalSpiritType()
	{
		return this.getTemplate().getElementalSpiritType();
	}

	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		this._currentLHandId = this.getTemplate().getLHandId();
		this._currentRHandId = this.getTemplate().getRHandId();
		this._currentCollisionHeight = this.getTemplate().getFCollisionHeight();
		this._currentCollisionRadius = this.getTemplate().getFCollisionRadius();
		Weapon weapon = killer != null ? killer.getActiveWeaponItem() : null;
		this._killingBlowWeaponId = weapon != null ? weapon.getId() : 0;
		if (this._isFakePlayer && killer != null && killer.isPlayable())
		{
			Player player = killer.asPlayer();
			if (this.isScriptValue(0) && this.getReputation() >= 0)
			{
				if (FakePlayersConfig.FAKE_PLAYER_KILL_KARMA)
				{
					player.setReputation(player.getReputation() - Formulas.calculateKarmaGain(player.getPkKills(), killer.isSummon()));
					player.setPkKills(player.getPkKills() + 1);
					player.broadcastUserInfo(UserInfoType.SOCIAL);
					player.checkItemRestriction();
					if (PvpRewardItemConfig.REWARD_PK_ITEM && (!PvpRewardItemConfig.DISABLE_REWARDS_IN_INSTANCES || this.getInstanceId() == 0) && (!PvpRewardItemConfig.DISABLE_REWARDS_IN_PVP_ZONES || !this.isInsideZone(ZoneId.PVP)))
					{
						player.addItem(ItemProcessType.REWARD, PvpRewardItemConfig.REWARD_PK_ITEM_ID, PvpRewardItemConfig.REWARD_PK_ITEM_AMOUNT, this, PvpRewardItemConfig.REWARD_PK_ITEM_MESSAGE);
					}

					if (PvpAnnounceConfig.ANNOUNCE_PK_PVP && !player.isGM())
					{
						String msg = PvpAnnounceConfig.ANNOUNCE_PK_MSG.replace("$killer", player.getName()).replace("$target", this.getName());
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
			}
			else if (FakePlayersConfig.FAKE_PLAYER_KILL_PVP)
			{
				player.setPvpKills(player.getPvpKills() + 1);
				player.setTotalKills(player.getTotalKills() + 1);
				player.broadcastUserInfo(UserInfoType.SOCIAL);
				if (PvpRewardItemConfig.REWARD_PVP_ITEM && (!PvpRewardItemConfig.DISABLE_REWARDS_IN_INSTANCES || this.getInstanceId() == 0) && (!PvpRewardItemConfig.DISABLE_REWARDS_IN_PVP_ZONES || !this.isInsideZone(ZoneId.PVP)))
				{
					player.addItem(ItemProcessType.REWARD, PvpRewardItemConfig.REWARD_PVP_ITEM_ID, PvpRewardItemConfig.REWARD_PVP_ITEM_AMOUNT, this, PvpRewardItemConfig.REWARD_PVP_ITEM_MESSAGE);
				}

				if (PvpAnnounceConfig.ANNOUNCE_PK_PVP && !player.isGM())
				{
					String msg = PvpAnnounceConfig.ANNOUNCE_PVP_MSG.replace("$killer", player.getName()).replace("$target", this.getName());
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
		}

		DecayTaskManager.getInstance().add(this);
		if (this._spawn != null)
		{
			NpcSpawnTemplate npcTemplate = this._spawn.getNpcSpawnTemplate();
			if (npcTemplate != null)
			{
				npcTemplate.notifyNpcDeath(this, killer);
			}
		}

		if (this.getTemplate().getMpRewardValue() > 0 && killer != null && killer.isPlayable())
		{
			Player killerPlayer = killer.asPlayer();
			new MpRewardTask(killerPlayer, this);

			for (Summon summon : killerPlayer.getServitors().values())
			{
				new MpRewardTask(summon, this);
			}

			if (this.getTemplate().getMpRewardAffectType() == MpRewardAffectType.PARTY)
			{
				Party party = killerPlayer.getParty();
				if (party != null)
				{
					for (Player member : party.getMembers())
					{
						if (member != killerPlayer && member.calculateDistance3D(this.getX(), this.getY(), this.getZ()) <= PlayerConfig.ALT_PARTY_RANGE)
						{
							new MpRewardTask(member, this);

							for (Summon summon : member.getServitors().values())
							{
								new MpRewardTask(summon, this);
							}
						}
					}
				}
			}
		}

		DatabaseSpawnManager.getInstance().updateStatus(this, true);
		return true;
	}

	public void setSpawn(Spawn spawn)
	{
		this._spawn = spawn;
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		this._soulshotamount = this.getTemplate().getSoulShot();
		this._spiritshotamount = this.getTemplate().getSpiritShot();
		this._killingBlowWeaponId = 0;
		this._isRandomAnimationEnabled = this.getTemplate().isRandomAnimationEnabled();
		this._isRandomWalkingEnabled = !WalkingManager.getInstance().isTargeted(this) && this.getTemplate().isRandomWalkEnabled();
		if (this.isTeleporting())
		{
			if (EventDispatcher.getInstance().hasListener(EventType.ON_NPC_TELEPORT, this))
			{
				EventDispatcher.getInstance().notifyEventAsync(new OnNpcTeleport(this), this);
			}
		}
		else if (EventDispatcher.getInstance().hasListener(EventType.ON_NPC_SPAWN, this))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnNpcSpawn(this), this);
		}

		if (!this.isTeleporting())
		{
			WalkingManager.getInstance().onSpawn(this);
		}

		if (this.isInsideZone(ZoneId.TAX) && this.getCastle() != null && (NpcConfig.SHOW_CREST_WITHOUT_QUEST || this.getCastle().getShowNpcCrest()) && this.getCastle().getOwnerId() != 0)
		{
			this.setClanId(this.getCastle().getOwnerId());
		}

		if (CREATURE_SEE_IDS.contains(this.getId()))
		{
			this.initSeenCreatures();
		}
	}

	public static void addCreatureSeeId(int id)
	{
		CREATURE_SEE_IDS.add(id);
	}

	public void onRespawn()
	{
		this.setDead(false);
		this.getEffectList().stopAllEffects(false);
		this.setDecayed(false);
		this.fullRestore();
		if (this.hasVariables())
		{
			this.getVariables().getSet().clear();
		}

		this.setTargetable(this.getTemplate().isTargetable());
		this.setSummoner(null);
		this.resetSummonedNpcs();
		this._nameString = null;
		this._titleString = null;
		this._params = null;
	}

	@Override
	public void onDecay()
	{
		if (!this._isDecayed)
		{
			this.setDecayed(true);
			super.onDecay();
			if (this._spawn != null && !DatabaseSpawnManager.getInstance().isDefined(this.getId()))
			{
				this._spawn.decreaseCount(this);
			}

			WalkingManager.getInstance().onDeath(this);
			if (EventDispatcher.getInstance().hasListener(EventType.ON_NPC_DESPAWN, this))
			{
				EventDispatcher.getInstance().notifyEventAsync(new OnNpcDespawn(this), this);
			}

			Instance instance = this.getInstanceWorld();
			if (instance != null)
			{
				instance.removeNpc(this);
			}

			this.stopQuestTimers();
			this.stopTimerHolders();
			this._scriptValue = 0;
		}
	}

	@Override
	public boolean deleteMe()
	{
		try
		{
			this.onDecay();
		}
		catch (Exception var2)
		{
			LOGGER.log(Level.SEVERE, "Failed decayMe().", var2);
		}

		if (this.isChannelized())
		{
			this.getSkillChannelized().abortChannelization();
		}

		ZoneManager.getInstance().getRegion(this).removeFromZones(this);
		return super.deleteMe();
	}

	public Spawn getSpawn()
	{
		return this._spawn;
	}

	public boolean isDecayed()
	{
		return this._isDecayed;
	}

	public void setDecayed(boolean decayed)
	{
		this._isDecayed = decayed;
	}

	public void endDecayTask()
	{
		if (!this._isDecayed)
		{
			DecayTaskManager.getInstance().cancel(this);
			this.onDecay();
		}
	}

	public void setLHandId(int newWeaponId)
	{
		this._currentLHandId = newWeaponId;
		this.broadcastInfo();
	}

	public void setRHandId(int newWeaponId)
	{
		this._currentRHandId = newWeaponId;
		this.broadcastInfo();
	}

	public void setLRHandId(int newLWeaponId, int newRWeaponId)
	{
		this._currentRHandId = newRWeaponId;
		this._currentLHandId = newLWeaponId;
		this.broadcastInfo();
	}

	public void setEnchant(int newEnchantValue)
	{
		this._currentEnchant = newEnchantValue;
		this.broadcastInfo();
	}

	public boolean isShowName()
	{
		return this.getTemplate().isShowName();
	}

	public void setCollisionHeight(float height)
	{
		this._currentCollisionHeight = height;
	}

	public void setCollisionRadius(float radius)
	{
		this._currentCollisionRadius = radius;
	}

	@Override
	public float getCollisionHeight()
	{
		return this._currentCollisionHeight;
	}

	@Override
	public float getCollisionRadius()
	{
		return this._currentCollisionRadius;
	}

	@Override
	public void sendInfo(Player player)
	{
		if (this.isVisibleFor(player))
		{
			if (this._isFakePlayer)
			{
				player.sendPacket(new FakePlayerInfo(this));
			}
			else if (this.getRunSpeed() == 0.0)
			{
				player.sendPacket(new ServerObjectInfo(this, player));
			}
			else
			{
				player.sendPacket(new NpcInfo(this));
			}
		}
	}

	public void scheduleDespawn(long delay)
	{
		ThreadPool.schedule(() -> {
			if (!this._isDecayed)
			{
				this.deleteMe();
			}
		}, delay);
	}

	@Override
	public void notifyQuestEventSkillFinished(Skill skill, WorldObject target)
	{
		if (target != null && EventDispatcher.getInstance().hasListener(EventType.ON_NPC_SKILL_FINISHED, this))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnNpcSkillFinished(this, target.asPlayer(), skill), this);
		}
	}

	@Override
	public boolean isMovementDisabled()
	{
		return super.isMovementDisabled() || !this.getTemplate().canMove() || this.getAiType() == AIType.CORPSE;
	}

	public AIType getAiType()
	{
		return this.getTemplate().getAIType();
	}

	public void setDisplayEffect(int value)
	{
		if (value != this._displayEffect)
		{
			this._displayEffect = value;
			this.broadcastPacket(new ExChangeNpcState(this.getObjectId(), value));
		}
	}

	public boolean hasDisplayEffect(int value)
	{
		return this._displayEffect == value;
	}

	public int getDisplayEffect()
	{
		return this._displayEffect;
	}

	public int getColorEffect()
	{
		return 0;
	}

	@Override
	public boolean isNpc()
	{
		return true;
	}

	@Override
	public Npc asNpc()
	{
		return this;
	}

	public void setTeam(Team team, boolean broadcast)
	{
		super.setTeam(team);
		if (broadcast)
		{
			this.broadcastInfo();
		}
	}

	@Override
	public void setTeam(Team team)
	{
		super.setTeam(team);
		this.broadcastInfo();
	}

	@Override
	public boolean isWalker()
	{
		return this._isWalker;
	}

	public void setWalker()
	{
		this._isWalker = true;
	}

	@Override
	public void rechargeShots(boolean physical, boolean magic, boolean fish)
	{
		if (this._isFakePlayer && FakePlayersConfig.FAKE_PLAYER_USE_SHOTS)
		{
			if (physical)
			{
				this.broadcastPacket(new MagicSkillUse(this, this, 2154, 1, 0, 0));
				this.chargeShot(ShotType.SOULSHOTS);
			}

			if (magic)
			{
				this.broadcastPacket(new MagicSkillUse(this, this, 2159, 1, 0, 0));
				this.chargeShot(ShotType.SPIRITSHOTS);
			}
		}
		else
		{
			if (physical && this._soulshotamount > 0)
			{
				if (Rnd.get(100) > this.getTemplate().getSoulShotChance())
				{
					return;
				}

				this._soulshotamount--;
				this.broadcastPacket(new MagicSkillUse(this, this, 2154, 1, 0, 0));
				this.chargeShot(ShotType.SOULSHOTS);
			}

			if (magic && this._spiritshotamount > 0)
			{
				if (Rnd.get(100) > this.getTemplate().getSpiritShotChance())
				{
					return;
				}

				this._spiritshotamount--;
				this.broadcastPacket(new MagicSkillUse(this, this, 2159, 1, 0, 0));
				this.chargeShot(ShotType.SPIRITSHOTS);
			}
		}
	}

	public int getScriptValue()
	{
		return this._scriptValue;
	}

	public void setScriptValue(int value)
	{
		this._scriptValue = value;
	}

	public boolean isScriptValue(int value)
	{
		return this._scriptValue == value;
	}

	public boolean isInMySpawnGroup(Npc npc)
	{
		return this.getSpawn().getNpcSpawnTemplate().getSpawnTemplate().getName().equals(npc.getSpawn().getNpcSpawnTemplate().getSpawnTemplate().getName());
	}

	public boolean staysInSpawnLoc()
	{
		return this._spawn != null && this._spawn.getX() == this.getX() && this._spawn.getY() == this.getY();
	}

	public boolean hasVariables()
	{
		return this.getScript(NpcVariables.class) != null;
	}

	public NpcVariables getVariables()
	{
		NpcVariables vars = this.getScript(NpcVariables.class);
		return vars != null ? vars : this.addScript(new NpcVariables());
	}

	public void broadcastEvent(String eventName, int radius, WorldObject reference)
	{
		World.getInstance().forEachVisibleObjectInRange(this, Npc.class, radius, obj -> {
			if (obj.hasListener(EventType.ON_NPC_EVENT_RECEIVED))
			{
				EventDispatcher.getInstance().notifyEventAsync(new OnNpcEventReceived(eventName, this, obj, reference), obj);
			}
		});
	}

	public void sendScriptEvent(String eventName, WorldObject receiver, WorldObject reference)
	{
		if (EventDispatcher.getInstance().hasListener(EventType.ON_NPC_EVENT_RECEIVED, receiver))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnNpcEventReceived(eventName, this, receiver.asNpc(), reference), receiver);
		}
	}

	public Location getPointInRange(int radiusMin, int radiusMax)
	{
		if (radiusMax != 0 && radiusMax >= radiusMin)
		{
			int radius = Rnd.get(radiusMin, radiusMax);
			double angle = Rnd.nextDouble() * 2.0 * Math.PI;
			return new Location((int) (this.getX() + radius * Math.cos(angle)), (int) (this.getY() + radius * Math.sin(angle)), this.getZ());
		}
		return new Location(this.getX(), this.getY(), this.getZ());
	}

	public Item dropItem(Creature creature, int itemId, long itemCount)
	{
		Item item = null;

		for (int i = 0; i < itemCount; i++)
		{
			if (ItemData.getInstance().getTemplate(itemId) == null)
			{
				LOGGER.severe("Item doesn't exist so cannot be dropped. Item ID: " + itemId + " Quest: " + this.getName());
				return null;
			}

			item = ItemManager.createItem(ItemProcessType.LOOT, itemId, itemCount, creature, this);
			if (item == null)
			{
				return null;
			}

			if (creature != null)
			{
				item.getDropProtection().protect(creature);
			}

			int newX = this.getX() + Rnd.get(141) - 70;
			int newY = this.getY() + Rnd.get(141) - 70;
			int newZ = this.getZ() + 20;
			item.dropMe(this, newX, newY, newZ);
			if (!GeneralConfig.LIST_PROTECTED_ITEMS.contains(itemId) && (GeneralConfig.AUTODESTROY_ITEM_AFTER > 0 && !item.getTemplate().hasExImmediateEffect() || GeneralConfig.HERB_AUTO_DESTROY_TIME > 0 && item.getTemplate().hasExImmediateEffect()))
			{
				ItemsAutoDestroyTaskManager.getInstance().addItem(item);
			}

			item.setProtected(false);
			if (item.isStackable() || !GeneralConfig.MULTIPLE_ITEM_DROP)
			{
				break;
			}
		}

		return item;
	}

	public Item dropItem(Creature creature, ItemHolder item)
	{
		return this.dropItem(creature, item.getId(), item.getCount());
	}

	@Override
	public String getName()
	{
		return this.getTemplate().getName();
	}

	@Override
	public boolean isVisibleFor(Player player)
	{
		if (this.hasListener(EventType.ON_NPC_CAN_BE_SEEN))
		{
			TerminateReturn term = EventDispatcher.getInstance().notifyEvent(new OnNpcCanBeSeen(this, player), this, TerminateReturn.class);
			if (term != null)
			{
				return term.terminate();
			}
		}

		return super.isVisibleFor(player);
	}

	public void setTalkable(boolean value)
	{
		this._isTalkable = value;
	}

	public boolean isTalkable()
	{
		return this._isTalkable;
	}

	public boolean isQuestMonster()
	{
		return this._isQuestMonster;
	}

	public void setKillingBlowWeapon(int weaponId)
	{
		this._killingBlowWeaponId = weaponId;
	}

	public int getKillingBlowWeapon()
	{
		return this._killingBlowWeaponId;
	}

	@Override
	public int getMinShopDistance()
	{
		return PrivateStoreRangeConfig.SHOP_MIN_RANGE_FROM_NPC;
	}

	@Override
	public boolean isFakePlayer()
	{
		return this._isFakePlayer;
	}

	public int getCloneObjId()
	{
		return this._cloneObjId;
	}

	public void setCloneObjId(int cloneObjId)
	{
		this._cloneObjId = cloneObjId;
	}

	@Override
	public int getClanId()
	{
		return this._clanId;
	}

	public void setClanId(int clanObjId)
	{
		this._clanId = clanObjId;
	}

	public void broadcastSay(ChatType chatType, String text)
	{
		Broadcast.toKnownPlayers(this, new NpcSay(this, chatType, text));
	}

	public void broadcastSay(ChatType chatType, NpcStringId npcStringId, String... parameters)
	{
		NpcSay npcSay = new NpcSay(this, chatType, npcStringId);
		if (parameters != null)
		{
			for (String parameter : parameters)
			{
				if (parameter != null)
				{
					npcSay.addStringParameter(parameter);
				}
			}
		}

		switch (chatType)
		{
			case NPC_GENERAL:
				Broadcast.toKnownPlayersInRadius(this, npcSay, 1250);
				break;
			default:
				Broadcast.toKnownPlayers(this, npcSay);
		}
	}

	public void broadcastSay(ChatType chatType, String text, int radius)
	{
		Broadcast.toKnownPlayersInRadius(this, new NpcSay(this, chatType, text), radius);
	}

	public void broadcastSay(ChatType chatType, NpcStringId npcStringId, int radius)
	{
		Broadcast.toKnownPlayersInRadius(this, new NpcSay(this, chatType, npcStringId), radius);
	}

	public StatSet getParameters()
	{
		if (this._params != null)
		{
			return this._params;
		}
		if (this._spawn != null)
		{
			NpcSpawnTemplate npcSpawnTemplate = this._spawn.getNpcSpawnTemplate();
			if (npcSpawnTemplate != null && npcSpawnTemplate.getParameters() != null && !npcSpawnTemplate.getParameters().isEmpty())
			{
				StatSet params = this.getTemplate().getParameters();
				if (params != null && !params.getSet().isEmpty())
				{
					StatSet set = new StatSet();
					set.merge(params);
					set.merge(npcSpawnTemplate.getParameters());
					this._params = set;
					return set;
				}

				this._params = npcSpawnTemplate.getParameters();
				return this._params;
			}
		}

		this._params = this.getTemplate().getParameters();
		return this._params;
	}

	public List<Skill> getLongRangeSkills()
	{
		return this.getTemplate().getAISkills(AISkillScope.LONG_RANGE);
	}

	public List<Skill> getShortRangeSkills()
	{
		return this.getTemplate().getAISkills(AISkillScope.SHORT_RANGE);
	}

	public boolean hasSkillChance()
	{
		return Rnd.get(100) < Rnd.get(this.getTemplate().getMinSkillChance(), this.getTemplate().getMaxSkillChance());
	}

	public NpcStringId getNameString()
	{
		return this._nameString;
	}

	public NpcStringId getTitleString()
	{
		return this._titleString;
	}

	public void setNameString(NpcStringId nameString)
	{
		this._nameString = nameString;
	}

	public void setTitleString(NpcStringId titleString)
	{
		this._titleString = titleString;
	}

	public void sendChannelingEffect(Creature target, int state)
	{
		this.broadcastPacket(new ExShowChannelingEffect(this, target, state));
	}

	public void setDBStatus(RaidBossStatus status)
	{
		this._raidStatus = status;
	}

	public RaidBossStatus getDBStatus()
	{
		return this._raidStatus;
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

	public void stopQuestTimers()
	{
		synchronized (this._questTimers)
		{
			for (QuestTimer timer : this._questTimers)
			{
				timer.cancelTask();
			}

			this._questTimers.clear();
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

	public void stopTimerHolders()
	{
		synchronized (this._timerHolders)
		{
			for (TimerHolder<?> timer : this._timerHolders)
			{
				timer.cancelTask();
			}

			this._timerHolders.clear();
		}
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName());
		sb.append(":");
		sb.append(this.getName());
		sb.append("(");
		sb.append(this.getId());
		sb.append(")[");
		sb.append(this.getObjectId());
		sb.append("]");
		return sb.toString();
	}
}
