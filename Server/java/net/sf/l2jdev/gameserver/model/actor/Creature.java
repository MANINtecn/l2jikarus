package net.sf.l2jdev.gameserver.model.actor;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Predicate;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.ai.Action;
import net.sf.l2jdev.gameserver.ai.AttackableAI;
import net.sf.l2jdev.gameserver.ai.CreatureAI;
import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.cache.RelationCache;
import net.sf.l2jdev.gameserver.config.FeatureConfig;
import net.sf.l2jdev.gameserver.config.GeoEngineConfig;
import net.sf.l2jdev.gameserver.config.NpcConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.config.PvpConfig;
import net.sf.l2jdev.gameserver.config.custom.BossAnnouncementsConfig;
import net.sf.l2jdev.gameserver.config.custom.ChampionMonstersConfig;
import net.sf.l2jdev.gameserver.config.custom.FakePlayersConfig;
import net.sf.l2jdev.gameserver.data.enums.CategoryType;
import net.sf.l2jdev.gameserver.data.enums.CharacterStyleCategoryType;
import net.sf.l2jdev.gameserver.data.xml.CategoryData;
import net.sf.l2jdev.gameserver.data.xml.CharacterStylesData;
import net.sf.l2jdev.gameserver.data.xml.DoorData;
import net.sf.l2jdev.gameserver.data.xml.FenceData;
import net.sf.l2jdev.gameserver.data.xml.NpcData;
import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.data.xml.TransformData;
import net.sf.l2jdev.gameserver.geoengine.GeoEngine;
import net.sf.l2jdev.gameserver.geoengine.pathfinding.GeoLocation;
import net.sf.l2jdev.gameserver.geoengine.pathfinding.PathFinding;
import net.sf.l2jdev.gameserver.managers.IdManager;
import net.sf.l2jdev.gameserver.managers.MapRegionManager;
import net.sf.l2jdev.gameserver.managers.ScriptManager;
import net.sf.l2jdev.gameserver.managers.ZoneManager;
import net.sf.l2jdev.gameserver.model.AccessLevel;
import net.sf.l2jdev.gameserver.model.EffectList;
import net.sf.l2jdev.gameserver.model.ElementalSpirit;
import net.sf.l2jdev.gameserver.model.Hit;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.Spawn;
import net.sf.l2jdev.gameserver.model.TimeStamp;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.WorldRegion;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.AttributeType;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Race;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Team;
import net.sf.l2jdev.gameserver.model.actor.enums.player.ElementalSpiritType;
import net.sf.l2jdev.gameserver.model.actor.enums.player.TeleportWhereType;
import net.sf.l2jdev.gameserver.model.actor.holders.creature.IgnoreSkillHolder;
import net.sf.l2jdev.gameserver.model.actor.instance.FriendlyNpc;
import net.sf.l2jdev.gameserver.model.actor.instance.GrandBoss;
import net.sf.l2jdev.gameserver.model.actor.instance.Guardian;
import net.sf.l2jdev.gameserver.model.actor.instance.Monster;
import net.sf.l2jdev.gameserver.model.actor.instance.Trap;
import net.sf.l2jdev.gameserver.model.actor.stat.CreatureStat;
import net.sf.l2jdev.gameserver.model.actor.status.CreatureStatus;
import net.sf.l2jdev.gameserver.model.actor.tasks.creature.NotifyAITask;
import net.sf.l2jdev.gameserver.model.actor.templates.CreatureTemplate;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.actor.transform.Transform;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.effects.EffectFlag;
import net.sf.l2jdev.gameserver.model.events.Containers;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureAttack;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureAttackAvoid;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureAttacked;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureDamageDealt;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureDamageReceived;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureDeath;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureKilled;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureSee;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureSkillFinishCast;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureSkillUse;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureTeleport;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureTeleported;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnAttackableFactionCall;
import net.sf.l2jdev.gameserver.model.events.listeners.AbstractEventListener;
import net.sf.l2jdev.gameserver.model.events.returns.DamageReturn;
import net.sf.l2jdev.gameserver.model.events.returns.LocationReturn;
import net.sf.l2jdev.gameserver.model.groups.Party;
import net.sf.l2jdev.gameserver.model.instancezone.Instance;
import net.sf.l2jdev.gameserver.model.interfaces.ILocational;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.Weapon;
import net.sf.l2jdev.gameserver.model.item.enums.BodyPart;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.enums.ItemSkillType;
import net.sf.l2jdev.gameserver.model.item.enums.ShotType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.item.type.EtcItemType;
import net.sf.l2jdev.gameserver.model.item.type.WeaponType;
import net.sf.l2jdev.gameserver.model.itemcontainer.Inventory;
import net.sf.l2jdev.gameserver.model.options.OptionSkillHolder;
import net.sf.l2jdev.gameserver.model.options.OptionSkillType;
import net.sf.l2jdev.gameserver.model.skill.AbnormalType;
import net.sf.l2jdev.gameserver.model.skill.BuffFinishTask;
import net.sf.l2jdev.gameserver.model.skill.BuffInfo;
import net.sf.l2jdev.gameserver.model.skill.CommonSkill;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.SkillCaster;
import net.sf.l2jdev.gameserver.model.skill.SkillCastingType;
import net.sf.l2jdev.gameserver.model.skill.SkillChannelized;
import net.sf.l2jdev.gameserver.model.skill.SkillChannelizer;
import net.sf.l2jdev.gameserver.model.skill.enums.BasicProperty;
import net.sf.l2jdev.gameserver.model.skill.enums.SkillFinishType;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;
import net.sf.l2jdev.gameserver.model.stats.BaseStat;
import net.sf.l2jdev.gameserver.model.stats.BasicPropertyResist;
import net.sf.l2jdev.gameserver.model.stats.Formulas;
import net.sf.l2jdev.gameserver.model.stats.MoveType;
import net.sf.l2jdev.gameserver.model.stats.Stat;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.model.zone.ZoneRegion;
import net.sf.l2jdev.gameserver.network.Disconnection;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.StatusUpdateType;
import net.sf.l2jdev.gameserver.network.enums.UserInfoType;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.Attack;
import net.sf.l2jdev.gameserver.network.serverpackets.ChangeMoveType;
import net.sf.l2jdev.gameserver.network.serverpackets.ChangeWaitType;
import net.sf.l2jdev.gameserver.network.serverpackets.ExMax;
import net.sf.l2jdev.gameserver.network.serverpackets.ExTeleportToLocationActivate;
import net.sf.l2jdev.gameserver.network.serverpackets.FakePlayerInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2jdev.gameserver.network.serverpackets.MoveToLocation;
import net.sf.l2jdev.gameserver.network.serverpackets.MoveToPawn;
import net.sf.l2jdev.gameserver.network.serverpackets.NpcInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.Revive;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerObjectInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.SetupGauge;
import net.sf.l2jdev.gameserver.network.serverpackets.SocialAction;
import net.sf.l2jdev.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.StopMove;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.TeleportToLocation;
import net.sf.l2jdev.gameserver.network.serverpackets.UserInfo;
import net.sf.l2jdev.gameserver.taskmanagers.AttackStanceTaskManager;
import net.sf.l2jdev.gameserver.taskmanagers.CreatureAttackTaskManager;
import net.sf.l2jdev.gameserver.taskmanagers.CreatureSeeTaskManager;
import net.sf.l2jdev.gameserver.taskmanagers.GameTimeTaskManager;
import net.sf.l2jdev.gameserver.taskmanagers.MovementTaskManager;
import net.sf.l2jdev.gameserver.util.Broadcast;
import net.sf.l2jdev.gameserver.util.LocationUtil;

public abstract class Creature extends WorldObject
{
	public static final Logger LOGGER = Logger.getLogger(Creature.class.getName());
	private final Set<WeakReference<Creature>> _attackByList = ConcurrentHashMap.newKeySet(1);
	private boolean _isDead = false;
	private boolean _isImmobilized = false;
	private boolean _isOverloaded = false;
	private boolean _isPendingRevive = false;
	private boolean _isRunning = this.isPlayer();
	protected boolean _showSummonAnimation = false;
	protected boolean _isTeleporting = false;
	private boolean _isInvul = false;
	private boolean _isUndying = false;
	private boolean _isFlying = false;
	private boolean _blockActions = false;
	private final Map<Integer, AtomicInteger> _blockActionsAllowedSkills = new ConcurrentHashMap<>();
	private CreatureStat _stat;
	private CreatureStatus _status;
	private CreatureTemplate _template;
	private String _title;
	public static final double MAX_HP_BAR_PX = 352.0;
	private double _hpUpdateIncCheck = 0.0;
	private double _hpUpdateDecCheck = 0.0;
	private double _hpUpdateInterval = 0.0;
	private int _reputation = 0;
	private final ConcurrentSkipListMap<Integer, Skill> _skills = new ConcurrentSkipListMap<>();
	private final Map<Long, TimeStamp> _reuseTimeStampsSkills = new ConcurrentHashMap<>();
	private final Map<Integer, TimeStamp> _reuseTimeStampsItems = new ConcurrentHashMap<>();
	private final Map<Long, Long> _disabledSkills = new ConcurrentHashMap<>();
	private boolean _allSkillsDisabled;
	private final byte[] _zones = new byte[ZoneId.getZoneCount()];
	protected final Location _lastZoneValidateLocation = new Location(super.getX(), super.getY(), super.getZ());
	private final StampedLock _attackLock = new StampedLock();
	private Team _team = Team.NONE;
	private boolean _lethalable = true;
	private final Map<Integer, OptionSkillHolder> _triggerSkills = new ConcurrentHashMap<>(1);
	private final Map<Integer, IgnoreSkillHolder> _ignoreSkillEffects = new ConcurrentHashMap<>(1);
	private final EffectList _effectList = new EffectList(this);
	private Creature _summoner = null;
	private Map<Integer, Npc> _summonedNpcs = null;
	private Creature _recallCreature = null;
	private SkillChannelizer _channelizer = null;
	private SkillChannelized _channelized = null;
	private final BuffFinishTask _buffFinishTask = new BuffFinishTask();
	private Transform _transform = null;
	protected Creature.MoveData _move;
	private boolean _cursorKeyMovement = false;
	private WorldObject _target;
	private volatile long _attackEndTime;
	private volatile long _disableRangedAttackEndTime;
	private CreatureAI _ai = null;
	protected Map<SkillCastingType, SkillCaster> _skillCasters = new ConcurrentHashMap<>();
	private final AtomicInteger _abnormalShieldBlocks = new AtomicInteger();
	private final Map<Integer, Double> _multipliedAbnormalTimes = new ConcurrentHashMap<>();
	private final Map<Integer, Integer> _addedAbnormalTimes = new ConcurrentHashMap<>();
	private final Map<Integer, RelationCache> _knownRelations = new ConcurrentHashMap<>();
	private Set<Creature> _seenCreatures = null;
	private int _seenCreatureRange = PlayerConfig.ALT_PARTY_RANGE;
	private final Map<StatusUpdateType, Long> _statusUpdates = new ConcurrentHashMap<>();
	private final Map<BasicProperty, BasicPropertyResist> _basicPropertyResists = new ConcurrentHashMap<>(1);
	private Set<ShotType> _chargedShots = EnumSet.noneOf(ShotType.class);
	private final List<Item> _fakePlayerDrops = new CopyOnWriteArrayList<>();
	private OnCreatureAttack _onCreatureAttack = null;
	private OnCreatureAttacked _onCreatureAttacked = null;
	private OnCreatureDamageDealt _onCreatureDamageDealt = null;
	private OnCreatureDamageReceived _onCreatureDamageReceived = null;
	private OnCreatureAttackAvoid _onCreatureAttackAvoid = null;
	public OnCreatureSkillFinishCast onCreatureSkillFinishCast = null;
	public OnCreatureSkillUse onCreatureSkillUse = null;
	private boolean _disabledAI = false;

	public Creature(CreatureTemplate template)
	{
		this(IdManager.getInstance().getNextId(), template);
	}

	public Creature(int objectId, CreatureTemplate template)
	{
		super(objectId);
		if (template == null)
		{
			throw new NullPointerException("Template is null!");
		}
		this.setInstanceType(InstanceType.Creature);
		this._template = template;
		this.initCharStat();
		this.initCharStatus();
		if (this.isNpc())
		{
			for (Skill skill : template.getSkills().values())
			{
				this.addSkill(skill);
			}
		}
		else if (this.isSummon())
		{
			for (Skill skill : template.getSkills().values())
			{
				this.addSkill(skill);
			}
		}

		this.setInvul(true);
	}

	public EffectList getEffectList()
	{
		return this._effectList;
	}

	public Inventory getInventory()
	{
		return null;
	}

	public boolean destroyItemByItemId(ItemProcessType process, int itemId, long count, WorldObject reference, boolean sendMessage)
	{
		return true;
	}

	public boolean destroyItem(ItemProcessType process, int objectId, long count, WorldObject reference, boolean sendMessage)
	{
		return true;
	}

	@Override
	public boolean isInsideZone(ZoneId zone)
	{
		Instance instance = this.getInstanceWorld();
		switch (zone)
		{
			case PVP:
				if (instance != null && instance.isPvP())
				{
					return true;
				}

				return this._zones[ZoneId.PVP.ordinal()] > 0 && this._zones[ZoneId.PEACE.ordinal()] == 0 && this._zones[ZoneId.NO_PVP.ordinal()] == 0;
			case PEACE:
				if (instance != null && instance.isPvP())
				{
					return false;
				}
			default:
				return this._zones[zone.ordinal()] > 0;
		}
	}

	public void setInsideZone(ZoneId zone, boolean state)
	{
		synchronized (this._zones)
		{
			if (state)
			{
				this._zones[zone.ordinal()]++;
			}
			else if (this._zones[zone.ordinal()] > 0)
			{
				this._zones[zone.ordinal()]--;
			}
		}
	}

	public boolean isTransformed()
	{
		return this._transform != null;
	}

	public boolean transform(int id, boolean addSkills)
	{
		Transform transform = TransformData.getInstance().getTransform(id);
		if (transform != null)
		{
			if (transform.isFlying() && this.getX() > -166168)
			{
				return false;
			}
			else if (!FeatureConfig.ALLOW_MOUNTS_DURING_SIEGE && transform.isRiding() && this.isInsideZone(ZoneId.SIEGE))
			{
				return false;
			}
			else
			{
				this._transform = transform;
				transform.onTransform(this, addSkills);
				return true;
			}
		}
		return false;
	}

	public void untransform()
	{
		if (this._transform != null)
		{
			this._transform.onUntransform(this);
			this._transform = null;
		}

		if (this.isPlayer())
		{
			this.getStat().recalculateStats(true);
			this.asPlayer().updateUserInfo();
		}
	}

	public Transform getTransformation()
	{
		return this._transform;
	}

	public int getTransformationId()
	{
		return this._transform == null ? 0 : this._transform.getId();
	}

	public int getTransformationDisplayId()
	{
		return this._transform != null && this._transform.isVisual() ? this._transform.getDisplayId() : 0;
	}

	public float getCollisionRadius()
	{
		return this._transform == null ? this._template.getCollisionRadius() : this._transform.getCollisionRadius(this, this._template.getCollisionRadius());
	}

	public float getCollisionHeight()
	{
		return this._transform == null ? this._template.getCollisionHeight() : this._transform.getCollisionHeight(this, this._template.getCollisionHeight());
	}

	public boolean isGM()
	{
		return false;
	}

	public AccessLevel getAccessLevel()
	{
		return null;
	}

	protected void initCharStatusUpdateValues()
	{
		this._hpUpdateIncCheck = this._stat.getMaxHp();
		this._hpUpdateInterval = this._hpUpdateIncCheck / 352.0;
		this._hpUpdateDecCheck = this._hpUpdateIncCheck - this._hpUpdateInterval;
	}

	public void onDecay()
	{
		if (this.isPlayer())
		{
			Player player = this.asPlayer();
			if (player.isInTimedHuntingZone())
			{
				player.stopTimedHuntingZoneTask();
				this.abortCast();
				this.stopMove(null);
				this.teleToLocation(MapRegionManager.getInstance().getTeleToLocation(this, TeleportWhereType.TOWN));
				this.setInstance(null);
			}
			else if (PlayerConfig.DISCONNECT_AFTER_DEATH && player.isOnline())
			{
				Disconnection.of(player).storeAndDeleteWith(new SystemMessage(SystemMessageId.SIXTY_MIN_HAVE_PASSED_AFTER_THE_DEATH_OF_YOUR_CHARACTER_SO_YOU_WERE_DISCONNECTED_FROM_THE_GAME));
			}
		}
		else
		{
			this.decayMe();
			ZoneRegion region = ZoneManager.getInstance().getRegion(this);
			if (region != null)
			{
				region.removeFromZones(this);
			}

			if (this._summoner != null)
			{
				this._summoner.removeSummonedNpc(this.getObjectId());
			}

			this._disabledAI = false;
			this._onCreatureAttack = null;
			this._onCreatureAttacked = null;
			this._onCreatureDamageDealt = null;
			this._onCreatureDamageReceived = null;
			this._onCreatureAttackAvoid = null;
			this.onCreatureSkillFinishCast = null;
			this.onCreatureSkillUse = null;
		}
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		this._buffFinishTask.start();
		this.revalidateZone(true);
		if (this instanceof GrandBoss)
		{
			if (BossAnnouncementsConfig.GRANDBOSS_SPAWN_ANNOUNCEMENTS && (!this.isInInstance() || BossAnnouncementsConfig.GRANDBOSS_INSTANCE_ANNOUNCEMENTS) && !this.isMinion() && !this.isRaidMinion())
			{
				String name = NpcData.getInstance().getTemplate(this.getId()).getName();
				if (name != null && !BossAnnouncementsConfig.RAIDBOSSES_EXCLUDED_FROM_SPAWN_ANNOUNCEMENTS.contains(this.getId()))
				{
					Broadcast.toAllOnlinePlayers(name + " has spawned!");
					Broadcast.toAllOnlinePlayersOnScreen(name + " has spawned!");
				}
			}
		}
		else if (this.isRaid() && BossAnnouncementsConfig.RAIDBOSS_SPAWN_ANNOUNCEMENTS && (!this.isInInstance() || BossAnnouncementsConfig.RAIDBOSS_INSTANCE_ANNOUNCEMENTS) && !this.isMinion() && !this.isRaidMinion())
		{
			String name = NpcData.getInstance().getTemplate(this.getId()).getName();
			if (name != null && !BossAnnouncementsConfig.RAIDBOSSES_EXCLUDED_FROM_SPAWN_ANNOUNCEMENTS.contains(this.getId()))
			{
				Broadcast.toAllOnlinePlayers(name + " has spawned!");
				Broadcast.toAllOnlinePlayersOnScreen(name + " has spawned!");
			}
		}
	}

	public synchronized void onTeleported()
	{
		if (this._isTeleporting)
		{
			this.spawnMe(this.getX(), this.getY(), this.getZ());
			this.setTeleporting(false);
			if (EventDispatcher.getInstance().hasListener(EventType.ON_CREATURE_TELEPORTED, this))
			{
				EventDispatcher.getInstance().notifyEventAsync(new OnCreatureTeleported(this), this);
			}
		}
	}

	public void addAttackerToAttackByList(Creature creature)
	{
	}

	public void broadcastPacket(ServerPacket packet)
	{
		this.broadcastPacket(packet, true);
	}

	public void broadcastPacket(ServerPacket packet, boolean includeSelf)
	{
		packet.sendInBroadcast();
		World.getInstance().forEachVisibleObject(this, Player.class, player -> {
			if (this.isVisibleFor(player))
			{
				player.sendPacket(packet);
			}
		});
	}

	public void broadcastSkillPacket(ServerPacket packet, WorldObject target)
	{
		this.broadcastPacket(packet);
	}

	public void broadcastSkillPacket(ServerPacket packet, Collection<WorldObject> targets)
	{
		this.broadcastPacket(packet);
	}

	public void broadcastMoveToLocation()
	{
		this.broadcastMoveToLocation(false);
	}

	public void broadcastMoveToLocation(boolean force)
	{
		Creature.MoveData move = this._move;
		if (move != null)
		{
			int gameTicks = GameTimeTaskManager.getInstance().getGameTicks();
			if (force || move.moveTimestamp <= 0 || gameTicks - move.lastBroadcastTime >= 10)
			{
				move.lastBroadcastTime = gameTicks;
				if (this.isPlayable())
				{
					this.broadcastPacket(new MoveToLocation(this));
				}
				else
				{
					CreatureAI ai = this.hasAI() ? this.getAI() : null;
					Intention intention = ai != null ? ai.getIntention() : null;
					WorldObject target = intention != Intention.ATTACK && intention != Intention.FOLLOW ? null : this._target;
					if (target != null)
					{
						if (target != this)
						{
							this.broadcastPacket(new MoveToPawn(this, target, this.getAI().getClientMovingToPawnOffset()));
						}
						else
						{
							this.broadcastPacket(new MoveToLocation(this));
						}
					}
					else
					{
						WorldRegion region = this.getWorldRegion();
						if (region != null && region.areNeighborsActive())
						{
							this.broadcastPacket(new MoveToLocation(this));
						}
					}
				}
			}
		}
	}

	public void broadcastSocialAction(int id)
	{
		if (this.isPlayable())
		{
			this.broadcastPacket(new SocialAction(this.getObjectId(), id));
		}
		else
		{
			WorldRegion region = this.getWorldRegion();
			if (region != null && region.areNeighborsActive())
			{
				this.broadcastPacket(new SocialAction(this.getObjectId(), id));
			}
		}
	}

	protected boolean needHpUpdate()
	{
		double currentHp = this._status.getCurrentHp();
		double maxHp = this._stat.getMaxHp();
		if (currentHp <= 1.0 || maxHp < 352.0)
		{
			return true;
		}
		else if (!(currentHp <= this._hpUpdateDecCheck) && !(currentHp >= this._hpUpdateIncCheck))
		{
			return false;
		}
		else
		{
			if (currentHp == maxHp)
			{
				this._hpUpdateIncCheck = currentHp + 1.0;
				this._hpUpdateDecCheck = currentHp - this._hpUpdateInterval;
			}
			else
			{
				double doubleMulti = currentHp / this._hpUpdateInterval;
				int intMulti = (int) doubleMulti;
				this._hpUpdateDecCheck = this._hpUpdateInterval * (doubleMulti < intMulti ? intMulti - 1 : intMulti);
				this._hpUpdateIncCheck = this._hpUpdateDecCheck + this._hpUpdateInterval;
			}

			return true;
		}
	}

	public void broadcastStatusUpdate()
	{
		this.broadcastStatusUpdate(null);
	}

	public void broadcastStatusUpdate(Creature caster)
	{
		StatusUpdate su = new StatusUpdate(this);
		if (caster != null)
		{
			su.addCaster(caster);
		}

		su.addUpdate(StatusUpdateType.MAX_HP, this._stat.getMaxHp());
		su.addUpdate(StatusUpdateType.CUR_HP, (long) this._status.getCurrentHp());
		this.computeStatusUpdate(su, StatusUpdateType.MAX_MP);
		this.computeStatusUpdate(su, StatusUpdateType.CUR_MP);
		this.broadcastPacket(su);
	}

	public void sendMessage(String text)
	{
	}

	public void teleToLocation(int xValue, int yValue, int zValue, int headingValue, Instance instanceValue)
	{
		if (!this.isPlayer() || this.asPlayer().isOnline())
		{
			int x = xValue;
			int y = yValue;
			int z = this._isFlying ? zValue : GeoEngine.getInstance().getHeight(xValue, yValue, zValue);
			int heading = headingValue;
			Instance instance = instanceValue;
			if (EventDispatcher.getInstance().hasListener(EventType.ON_CREATURE_TELEPORT, this))
			{
				LocationReturn term = EventDispatcher.getInstance().notifyEvent(new OnCreatureTeleport(this, xValue, yValue, z, headingValue, instanceValue), this, LocationReturn.class);
				if (term != null)
				{
					if (term.terminate())
					{
						return;
					}

					if (term.overrideLocation())
					{
						x = term.getX();
						y = term.getY();
						z = term.getZ();
						heading = term.getHeading();
						instance = term.getInstance();
					}
				}
			}

			if (this._isPendingRevive)
			{
				this.doRevive();
			}

			this.sendPacket(ActionFailed.get(SkillCastingType.NORMAL));
			this.sendPacket(ActionFailed.get(SkillCastingType.NORMAL_SECOND));
			if (this.isMoving())
			{
				this.stopMove(null);
			}

			this.abortCast();
			this.setTarget(null);
			this.setTeleporting(true);
			this.getAI().setIntention(Intention.ACTIVE);
			this.decayMe();
			z += 5;
			this.broadcastPacket(new TeleportToLocation(this, x, y, z, heading));
			if (this.getInstanceWorld() != instance)
			{
				this.setInstance(instance);
			}

			this.setXYZ(x, y, z);
			if (heading != 0)
			{
				this.setHeading(heading);
			}

			this.sendPacket(new ExTeleportToLocationActivate(this));
			if (this.isPlayer())
			{
				Player player = this.asPlayer();
				GameClient client = player.getClient();
				if (client != null && client.isDetached())
				{
					this.onTeleported();
				}
			}
			else
			{
				this.onTeleported();
			}

			this.revalidateZone(true);
		}
	}

	public void teleToLocation(int x, int y, int z)
	{
		this.teleToLocation(x, y, z, 0, this.getInstanceWorld());
	}

	public void teleToLocation(int x, int y, int z, Instance instance)
	{
		this.teleToLocation(x, y, z, 0, instance);
	}

	public void teleToLocation(int x, int y, int z, int heading)
	{
		this.teleToLocation(x, y, z, heading, this.getInstanceWorld());
	}

	public void teleToLocation(int x, int y, int z, int heading, boolean randomOffset)
	{
		this.teleToLocation(x, y, z, heading, randomOffset ? PlayerConfig.MAX_OFFSET_ON_TELEPORT : 0, this.getInstanceWorld());
	}

	public void teleToLocation(int x, int y, int z, int heading, boolean randomOffset, Instance instance)
	{
		this.teleToLocation(x, y, z, heading, randomOffset ? PlayerConfig.MAX_OFFSET_ON_TELEPORT : 0, instance);
	}

	public void teleToLocation(int x, int y, int z, int heading, int randomOffset)
	{
		this.teleToLocation(x, y, z, heading, randomOffset, this.getInstanceWorld());
	}

	public void teleToLocation(int xValue, int yValue, int zValue, int heading, int randomOffset, Instance instance)
	{
		int x = xValue;
		int y = yValue;
		int z = zValue;
		if (PlayerConfig.OFFSET_ON_TELEPORT_ENABLED || randomOffset > 0)
		{
			x = xValue + Rnd.get(-randomOffset, randomOffset);
			y = yValue + Rnd.get(-randomOffset, randomOffset);
			int count = 0;

			for (float collision = Math.min(this.getCollisionRadius() * 2.0F, randomOffset); count++ < 100 && (LocationUtil.calculateDistance(xValue, yValue, zValue, x, y, z, true, false) < collision || !GeoEngine.getInstance().canSeeTarget(xValue, yValue, zValue, x, y, z, instance) || !GeoEngine.getInstance().canMoveToTarget(xValue, yValue, zValue, x, y, z, instance)); y = yValue + Rnd.get(-randomOffset, randomOffset))
			{
				x = xValue + Rnd.get(-randomOffset, randomOffset);
			}

			if (!this._isFlying)
			{
				Location validLocation = GeoEngine.getInstance().getValidLocation(xValue, yValue, z, x, y, z, instance);
				x = validLocation.getX();
				y = validLocation.getY();
				z = validLocation.getZ();
			}
		}

		this.teleToLocation(x, y, z, heading, instance);
	}

	public void teleToLocation(ILocational loc)
	{
		this.teleToLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getHeading());
	}

	public void teleToLocation(ILocational loc, Instance instance)
	{
		this.teleToLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), instance);
	}

	public void teleToLocation(ILocational loc, int randomOffset)
	{
		this.teleToLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), randomOffset);
	}

	public void teleToLocation(ILocational loc, int randomOffset, Instance instance)
	{
		this.teleToLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), randomOffset, instance);
	}

	public void teleToLocation(ILocational loc, boolean randomOffset)
	{
		this.teleToLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), randomOffset ? PlayerConfig.MAX_OFFSET_ON_TELEPORT : 0);
	}

	public void teleToLocation(ILocational loc, boolean randomOffset, Instance instance)
	{
		this.teleToLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), randomOffset, instance);
	}

	public void teleToLocation(TeleportWhereType teleportWhere)
	{
		this.teleToLocation(teleportWhere, this.getInstanceWorld());
	}

	public void teleToLocation(TeleportWhereType teleportWhere, Instance instance)
	{
		this.teleToLocation(MapRegionManager.getInstance().getTeleToLocation(this, teleportWhere), true, instance);
	}

	public void doAutoAttack(Creature target)
	{
		long stamp = this._attackLock.tryWriteLock();
		if (stamp != 0L)
		{
			try
			{
				if (target != null && (this.isPlayable() || !this.isAttackDisabled()) && target.isTargetable())
				{
					if (!this.isAlikeDead())
					{
						if (this.isNpc() && target.isAlikeDead() || !this.isInSurroundingRegion(target) || this.isPlayer() && target.isDead())
						{
							this.getAI().setIntention(Intention.ACTIVE);
							this.sendPacket(ActionFailed.STATIC_PACKET);
							return;
						}

						if (this._transform != null && !this._transform.canAttack())
						{
							this.sendPacket(ActionFailed.STATIC_PACKET);
							return;
						}
					}

					Player player = this.asPlayer();
					if (player != null)
					{
						if (player.inObserverMode())
						{
							this.sendPacket(SystemMessageId.YOU_CANNOT_USE_THIS_FUNCTION_IN_THE_SPECTATOR_MODE);
							this.sendPacket(ActionFailed.STATIC_PACKET);
							return;
						}

						if (target.isInsidePeaceZone(this))
						{
							this.getAI().setIntention(Intention.ACTIVE);
							this.sendPacket(ActionFailed.STATIC_PACKET);
							return;
						}

						if (player.isOnEvent() && !player.isOnSoloEvent() && target.isPlayable() && player.getTeam() == target.asPlayer().getTeam())
						{
							this.sendPacket(ActionFailed.STATIC_PACKET);
							return;
						}
					}
					else if (this.isInsidePeaceZone(this, target))
					{
						this.getAI().setIntention(Intention.ACTIVE);
						this.sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}

					this.stopEffectsOnAction();
					if (!GeoEngine.getInstance().canSeeTarget(this, target))
					{
						this.sendPacket(SystemMessageId.CANNOT_SEE_TARGET);
						this.getAI().setIntention(Intention.ACTIVE);
						this.sendPacket(ActionFailed.STATIC_PACKET);
					}
					else
					{
						Weapon weaponItem = this.getActiveWeaponItem();
						WeaponType weaponType = this.getAttackType();
						if (weaponItem != null)
						{
							if (!weaponItem.isAttackWeapon() && !this.isGM())
							{
								if (weaponItem.getItemType() == WeaponType.FISHINGROD)
								{
									this.sendPacket(SystemMessageId.YOU_CANNOT_ATTACK_WHILE_FISHING);
								}
								else
								{
									this.sendPacket(SystemMessageId.YOU_CANNOT_ATTACK_WITH_THIS_WEAPON);
								}

								this.sendPacket(ActionFailed.STATIC_PACKET);
								return;
							}

							if (weaponItem.getItemType().isRanged())
							{
								if (this._disableRangedAttackEndTime > System.nanoTime())
								{
									if (this.isPlayer())
									{
										ThreadPool.schedule(new NotifyAITask(this, Action.READY_TO_ACT), 300L);
										this.sendPacket(ActionFailed.STATIC_PACKET);
									}

									return;
								}

								if (this.isPlayer())
								{
									if (!this.checkAndEquipAmmunition(weaponItem.getItemType().isPistols() ? EtcItemType.ELEMENTAL_ORB : (weaponItem.getItemType().isCrossbow() ? EtcItemType.BOLT : EtcItemType.ARROW)))
									{
										this.getAI().setIntention(Intention.ACTIVE);
										this.sendPacket(ActionFailed.STATIC_PACKET);
										if (weaponItem.getItemType().isPistols())
										{
											this.sendPacket(SystemMessageId.YOU_CANNOT_ATTACK_BECAUSE_YOU_DON_T_HAVE_AN_ELEMENTAL_ORB);
										}
										else
										{
											this.sendPacket(SystemMessageId.YOU_HAVE_RUN_OUT_OF_ARROWS);
										}

										return;
									}

									if (target.isInsidePeaceZone(this))
									{
										this.getAI().setIntention(Intention.ACTIVE);
										this.sendPacket(SystemMessageId.YOU_CANNOT_ATTACK_IN_A_PEACEFUL_ZONE);
										this.sendPacket(ActionFailed.STATIC_PACKET);
										return;
									}

									int mpConsume = weaponItem.getMpConsume();
									if (weaponItem.getReducedMpConsume() > 0 && Rnd.get(100) < weaponItem.getReducedMpConsumeChance())
									{
										mpConsume = weaponItem.getReducedMpConsume();
									}

									mpConsume = this.isAffected(EffectFlag.CHEAPSHOT) ? 0 : mpConsume;
									if (this._status.getCurrentMp() < mpConsume)
									{
										ThreadPool.schedule(new NotifyAITask(this, Action.READY_TO_ACT), 1000L);
										this.sendPacket(SystemMessageId.NOT_ENOUGH_MP);
										this.sendPacket(ActionFailed.STATIC_PACKET);
										return;
									}

									if (mpConsume > 0)
									{
										this._status.reduceMp(mpConsume);
									}
								}
							}
						}

						if (this.isMoving())
						{
							this.stopMove(this.getLocation());
						}

						WeaponType attackType = this.getAttackType();
						boolean isTwoHanded = weaponItem != null && weaponItem.getBodyPart() == BodyPart.LR_HAND;
						int timeAtk = Formulas.calculateTimeBetweenAttacks(this._stat.getPAtkSpd());
						int timeToHit = Formulas.calculateTimeToHit(timeAtk, weaponType, isTwoHanded, false);
						long currentTime = System.nanoTime();
						this._attackEndTime = currentTime + TimeUnit.MILLISECONDS.toNanos(timeAtk);
						if (this._attackEndTime < currentTime)
						{
							this._attackEndTime = currentTime + TimeUnit.MILLISECONDS.toNanos(2147483647L);
						}

						this.setHeading(LocationUtil.calculateHeadingFrom(this, target));
						if (!this.isChargedShot(ShotType.SOULSHOTS) && !this.isChargedShot(ShotType.BLESSED_SOULSHOTS))
						{
							this.rechargeShots(true, false, false);
						}

						Attack attack = this.generateAttackTargetData(target, weaponItem, attackType);
						boolean crossbow = false;
						switch (attackType)
						{
							case CROSSBOW:
							case TWOHANDCROSSBOW:
								crossbow = true;
							case BOW:
								int reuse = Formulas.calculateReuseTime(this, weaponItem);
								Inventory inventory = this.getInventory();
								if (inventory != null)
								{
									inventory.reduceAmmunitionCount(crossbow ? EtcItemType.BOLT : EtcItemType.ARROW);
								}

								if (this.isPlayer())
								{
									if (crossbow)
									{
										this.sendPacket(SystemMessageId.YOUR_CROSSBOW_IS_PREPARING_TO_FIRE);
									}

									this.sendPacket(new SetupGauge(this.getObjectId(), 1, reuse));
								}

								this._disableRangedAttackEndTime = currentTime + TimeUnit.MILLISECONDS.toNanos(reuse);
								if (this._disableRangedAttackEndTime < currentTime)
								{
									this._disableRangedAttackEndTime = currentTime + TimeUnit.MILLISECONDS.toNanos(2147483647L);
								}

								CreatureAttackTaskManager.getInstance().onHitTimeNotDual(this, weaponItem, attack, timeToHit, timeAtk);
								break;
							case PISTOLS:
								int pistolReuse = Formulas.calculateReuseTime(this, weaponItem);
								this._disableRangedAttackEndTime = currentTime + TimeUnit.MILLISECONDS.toNanos(pistolReuse);
								if (this._disableRangedAttackEndTime < currentTime)
								{
									this._disableRangedAttackEndTime = currentTime + TimeUnit.MILLISECONDS.toNanos(2147483647L);
								}

								CreatureAttackTaskManager.getInstance().onHitTimeNotDual(this, weaponItem, attack, timeToHit, timeAtk);
								break;
							case FIST:
								if (!this.isPlayer())
								{
									CreatureAttackTaskManager.getInstance().onHitTimeNotDual(this, weaponItem, attack, timeToHit, timeAtk);
									break;
								}
							case DUAL:
							case DUALFIST:
							case DUALBLUNT:
							case DUALDAGGER:
								int delayForSecondAttack = Formulas.calculateTimeToHit(timeAtk, weaponType, isTwoHanded, true) - timeToHit;
								CreatureAttackTaskManager.getInstance().onFirstHitTimeForDual(this, weaponItem, attack, timeToHit, timeAtk, delayForSecondAttack);
								break;
							default:
								CreatureAttackTaskManager.getInstance().onHitTimeNotDual(this, weaponItem, attack, timeToHit, timeAtk);
						}

						if (attack.hasHits())
						{
							this.broadcastPacket(attack);
						}

						if (player != null && !player.isInsideZone(ZoneId.PVP) && player != target)
						{
							AttackStanceTaskManager.getInstance().addAttackStanceTask(player);
							player.updatePvPStatus(target);
						}

						if (this.isFakePlayer() && !FakePlayersConfig.FAKE_PLAYER_AUTO_ATTACKABLE && (target.isPlayable() || target.isFakePlayer()))
						{
							Npc npc = this.asNpc();
							if (!npc.isScriptValue(1))
							{
								npc.setScriptValue(1);
								this.broadcastInfo();
								ScriptManager.getInstance().getScript("PvpFlaggingStopTask").notifyEvent("FLAG_CHECK", npc, null);
								return;
							}
						}
					}
				}
			}
			finally
			{
				this._attackLock.unlockWrite(stamp);
			}
		}
	}

	private Attack generateAttackTargetData(Creature target, Weapon weapon, WeaponType weaponType)
	{
		boolean isDual = WeaponType.DUAL == weaponType || WeaponType.DUALBLUNT == weaponType || WeaponType.DUALDAGGER == weaponType || WeaponType.DUALFIST == weaponType;
		Attack attack = new Attack(this, target);
		boolean shotConsumed = false;
		Hit hit = this.generateHit(target, weapon, shotConsumed, isDual);
		attack.addHit(hit);
		shotConsumed = hit.isShotUsed();
		if (isDual)
		{
			hit = this.generateHit(target, weapon, shotConsumed, isDual);
			attack.addHit(hit);
			shotConsumed = hit.isShotUsed();
		}

		int attackCountMax = (int) this._stat.getValue(Stat.ATTACK_COUNT_MAX, 1.0);
		if (attackCountMax > 1 && this._stat.getValue(Stat.PHYSICAL_POLEARM_TARGET_SINGLE, 0.0) <= 0.0)
		{
			double headingAngle = LocationUtil.convertHeadingToDegree(this.getHeading());
			int maxRadius = this._stat.getPhysicalAttackRadius();
			int physicalAttackAngle = this._stat.getPhysicalAttackAngle();

			for (Creature obj : World.getInstance().getVisibleObjectsInRange(this, Creature.class, maxRadius))
			{
				if (obj != target && !obj.isAlikeDead() && obj.isAutoAttackable(this) && !(Math.abs(this.calculateDirectionTo(obj) - headingAngle) > physicalAttackAngle))
				{
					hit = this.generateHit(obj, weapon, shotConsumed, false);
					attack.addHit(hit);
					shotConsumed = hit.isShotUsed();
					if (--attackCountMax <= 0)
					{
						break;
					}
				}
			}
		}

		return attack;
	}

	private Hit generateHit(Creature target, Weapon weapon, boolean shotConsumedValue, boolean halfDamage)
	{
		int damage = 0;
		byte shld = 0;
		boolean crit = false;
		boolean shotConsumed = shotConsumedValue;
		boolean shotBlessed = false;
		boolean miss = Formulas.calcHitMiss(this, target);
		if (!shotConsumedValue)
		{
			if (this.isChargedShot(ShotType.BLESSED_SOULSHOTS))
			{
				shotBlessed = true;
				shotConsumed = !miss && this.unchargeShot(ShotType.BLESSED_SOULSHOTS);
			}
			else
			{
				shotConsumed = !miss && this.unchargeShot(ShotType.SOULSHOTS);
			}
		}

		int ssGrade = shotConsumed && weapon != null ? weapon.getItemGrade().ordinal() : 0;
		if (!miss)
		{
			shld = Formulas.calcShldUse(this, target);
			crit = Formulas.calcCrit(this._stat.getCriticalHit(), this, target, null);
			damage = (int) Formulas.calcAutoAttackDamage(this, target, shld, crit, shotConsumed, shotBlessed);
			if (halfDamage)
			{
				damage /= 2;
			}
		}

		return new Hit(target, damage, miss, crit, shld, shotConsumed, ssGrade);
	}

	public void doCast(Skill skill)
	{
		this.doCast(skill, null, false, false);
	}

	public synchronized void doCast(Skill skill, Item item, boolean ctrlPressed, boolean shiftPressed)
	{
		if (!this.isAttackable() || !this.isMoving() || this instanceof Guardian)
		{
			SkillCastingType castingType = SkillCastingType.NORMAL;
			if (skill.canDoubleCast() && this.isAffected(EffectFlag.DOUBLE_CAST) && this.isCastingNow(castingType))
			{
				castingType = SkillCastingType.NORMAL_SECOND;
			}

			SkillCaster skillCaster = SkillCaster.castSkill(this, this._target, skill, item, castingType, ctrlPressed, shiftPressed);
			if (skillCaster == null && this.isPlayer())
			{
				this.sendPacket(ActionFailed.get(castingType));
				this.getAI().setIntention(Intention.ACTIVE);
			}

			if (!NpcConfig.RAID_DISABLE_CURSE && this.isPlayer())
			{
				World.getInstance().forEachVisibleObjectInRange(this, Attackable.class, PlayerConfig.ALT_PARTY_RANGE, attackable -> {
					if (attackable.giveRaidCurse() && attackable.isInCombat() && this.getLevel() - attackable.getLevel() > 8)
					{
						CommonSkill curse = skill.hasNegativeEffect() ? CommonSkill.RAID_CURSE2 : CommonSkill.RAID_CURSE;
						curse.getSkill().applyEffects(attackable, this);
					}
				});
			}
		}
	}

	public Map<Integer, TimeStamp> getItemReuseTimeStamps()
	{
		return this._reuseTimeStampsItems;
	}

	public void addTimeStampItem(Item item, long reuse)
	{
		this.addTimeStampItem(item, reuse, -1L);
	}

	public void addTimeStampItem(Item item, long reuse, long systime)
	{
		this._reuseTimeStampsItems.put(item.getObjectId(), new TimeStamp(item, reuse, systime));
	}

	public long getItemRemainingReuseTime(int itemObjId)
	{
		TimeStamp reuseStamp = this._reuseTimeStampsItems.get(itemObjId);
		return reuseStamp != null ? reuseStamp.getRemaining() : -1L;
	}

	public long getReuseDelayOnGroup(int group)
	{
		if (group > 0 && !this._reuseTimeStampsItems.isEmpty())
		{
			long currentTime = System.currentTimeMillis();

			for (TimeStamp ts : this._reuseTimeStampsItems.values())
			{
				if (ts.getSharedReuseGroup() == group)
				{
					long stamp = ts.getStamp();
					if (currentTime < stamp)
					{
						return Math.max(stamp - currentTime, 0L);
					}
				}
			}
		}

		return -1L;
	}

	public Map<Long, TimeStamp> getSkillReuseTimeStamps()
	{
		return this._reuseTimeStampsSkills;
	}

	public void addTimeStamp(Skill skill, long reuse)
	{
		this.addTimeStamp(skill, reuse, -1L);
	}

	public void addTimeStamp(Skill skill, long reuse, long systime)
	{
		this._reuseTimeStampsSkills.put(skill.getReuseHashCode(), new TimeStamp(skill, reuse, systime));
	}

	public void removeTimeStamp(Skill skill)
	{
		this._reuseTimeStampsSkills.remove(skill.getReuseHashCode());
	}

	public void resetTimeStamps()
	{
		this._reuseTimeStampsSkills.clear();
	}

	public long getSkillRemainingReuseTime(long hashCode)
	{
		TimeStamp reuseStamp = this._reuseTimeStampsSkills.get(hashCode);
		return reuseStamp != null ? reuseStamp.getRemaining() : -1L;
	}

	public boolean hasSkillReuse(long hashCode)
	{
		TimeStamp reuseStamp = this._reuseTimeStampsSkills.get(hashCode);
		return reuseStamp != null && reuseStamp.hasNotPassed();
	}

	public synchronized TimeStamp getSkillReuseTimeStamp(long hashCode)
	{
		return this._reuseTimeStampsSkills.get(hashCode);
	}

	public Map<Long, Long> getDisabledSkills()
	{
		return this._disabledSkills;
	}

	public void enableSkill(Skill skill)
	{
		if (skill != null)
		{
			this._disabledSkills.remove(skill.getReuseHashCode());
		}
	}

	public void disableSkill(Skill skill, long delay)
	{
		if (skill != null)
		{
			this._disabledSkills.put(skill.getReuseHashCode(), delay > 0L ? System.currentTimeMillis() + delay : Long.MAX_VALUE);
		}
	}

	public void resetDisabledSkills()
	{
		this._disabledSkills.clear();
	}

	public boolean isSkillDisabled(Skill skill)
	{
		if (skill == null)
		{
			return false;
		}
		else if (!this._allSkillsDisabled && (skill.canCastWhileDisabled() || !this.isAllSkillsDisabled()))
		{
			if (this.isAffected(EffectFlag.CONDITIONAL_BLOCK_ACTIONS) && !this.isBlockedActionsAllowedSkill(skill))
			{
				return true;
			}
			long hashCode = skill.getReuseHashCode();
			if (this.hasSkillReuse(hashCode))
			{
				return true;
			}
			else if (this._disabledSkills.isEmpty())
			{
				return false;
			}
			else
			{
				Long stamp = this._disabledSkills.get(hashCode);
				if (stamp == null)
				{
					return false;
				}
				else if (stamp < System.currentTimeMillis())
				{
					this._disabledSkills.remove(hashCode);
					return false;
				}
				else
				{
					return true;
				}
			}
		}
		else
		{
			return true;
		}
	}

	public void disableAllSkills()
	{
		this._allSkillsDisabled = true;
	}

	public void enableAllSkills()
	{
		this._allSkillsDisabled = false;
	}

	public boolean doDie(Creature killer)
	{
		synchronized (this)
		{
			if (this._isDead)
			{
				return false;
			}

			this.setCurrentHp(0.0);
			this.setDead(true);
		}

		if (EventDispatcher.getInstance().hasListener(EventType.ON_CREATURE_DEATH, this))
		{
			EventDispatcher.getInstance().notifyEvent(new OnCreatureDeath(killer, this), this);
		}

		if (EventDispatcher.getInstance().hasListener(EventType.ON_CREATURE_KILLED, killer))
		{
			EventDispatcher.getInstance().notifyEvent(new OnCreatureKilled(killer, this), killer);
		}

		if (killer != null && killer.isPlayer())
		{
			Player player = killer.asPlayer();
			int skillStyleId = player.getActiveCharacterStyleId(CharacterStyleCategoryType.KILL_EFFECT);
			if (skillStyleId > 0)
			{
				SkillHolder holder = CharacterStylesData.getInstance().getKillEffectStyleByStyleId(skillStyleId);
				if (holder != null && holder.getSkillId() > 0)
				{
					this.broadcastPacket(new MagicSkillUse(this, this, holder.getSkillId(), holder.getSkillLevel(), 0, 0));
				}
			}

			if (player.isAssassin() && player.isAffectedBySkill(CommonSkill.BRUTALITY.getId()))
			{
				player.setAssassinationPoints(player.getAssassinationPoints() + 10000);
			}
		}

		this.abortAttack();
		this.abortCast();
		Creature mainDamageDealer = this.isMonster() ? this.asMonster().getMainDamageDealer() : null;
		this.calculateRewards(mainDamageDealer != null ? mainDamageDealer : killer);
		this.setTarget(null);
		this.stopMove(null);
		this._status.stopHpMpRegeneration();
		if (this.isAttackable())
		{
			Spawn spawn = this.asNpc().getSpawn();
			if (spawn != null && spawn.isRespawnEnabled())
			{
				this.stopAllEffects();
			}
			else
			{
				this._effectList.stopAllEffectsWithoutExclusions(true, true);
			}

			if (killer != null && killer.isPlayable() && !killer.asPlayer().isGM())
			{
				NpcTemplate template = this.asAttackable().getTemplate();
				Set<Integer> clans = template.getClans();
				if (clans != null && !clans.isEmpty())
				{
					World.getInstance().forEachVisibleObjectInRange(this, Attackable.class, template.getClanHelpRange(), called -> {
						if (!called.isDead() && called.hasAI() && Math.abs(killer.getZ() - called.getZ()) <= 600)
						{
							if (called.getAI().getIntention() == Intention.IDLE || called.getAI().getIntention() == Intention.ACTIVE)
							{
								if (template.isClan(called.getTemplate().getClans()))
								{
									called.getAI().notifyAction(Action.AGGRESSION, killer, 1);
									if (EventDispatcher.getInstance().hasListener(EventType.ON_ATTACKABLE_FACTION_CALL, called))
									{
										EventDispatcher.getInstance().notifyEventAsync(new OnAttackableFactionCall(called, this.asAttackable(), killer.asPlayer(), killer.isSummon()), called);
									}
								}
							}
						}
					});
				}
			}
		}
		else
		{
			this.stopAllEffectsExceptThoseThatLastThroughDeath();
		}

		this.broadcastStatusUpdate();
		if (this.hasAI())
		{
			this.getAI().notifyAction(Action.DEATH);
		}

		ZoneManager.getInstance().getRegion(this).onDeath(this);
		this.getAttackByList().clear();
		if (this.isChannelized())
		{
			this.getSkillChannelized().abortChannelization();
		}

		if (this instanceof GrandBoss)
		{
			if (BossAnnouncementsConfig.GRANDBOSS_DEFEAT_ANNOUNCEMENTS && (!this.isInInstance() || BossAnnouncementsConfig.GRANDBOSS_INSTANCE_ANNOUNCEMENTS) && !this.isMinion() && !this.isRaidMinion())
			{
				String name = NpcData.getInstance().getTemplate(this.getId()).getName();
				if (name != null && !BossAnnouncementsConfig.RAIDBOSSES_EXCLUDED_FROM_DEFEAT_ANNOUNCEMENTS.contains(this.getId()))
				{
					Broadcast.toAllOnlinePlayers(name + " has been defeated!");
					Broadcast.toAllOnlinePlayersOnScreen(name + " has been defeated!");
				}
			}
		}
		else if (this.isRaid() && BossAnnouncementsConfig.RAIDBOSS_DEFEAT_ANNOUNCEMENTS && (!this.isInInstance() || BossAnnouncementsConfig.RAIDBOSS_INSTANCE_ANNOUNCEMENTS) && !this.isMinion() && !this.isRaidMinion())
		{
			String name = NpcData.getInstance().getTemplate(this.getId()).getName();
			if (name != null && !BossAnnouncementsConfig.RAIDBOSSES_EXCLUDED_FROM_DEFEAT_ANNOUNCEMENTS.contains(this.getId()))
			{
				Broadcast.toAllOnlinePlayers(name + " has been defeated!");
				Broadcast.toAllOnlinePlayersOnScreen(name + " has been defeated!");
			}
		}

		return true;
	}

	@Override
	public boolean decayMe()
	{
		if (this.hasAI())
		{
			if (this.isAttackable())
			{
				this.getAttackByList().clear();
				this.asAttackable().clearAggroList();
				this.getAI().setIntention(Intention.IDLE);
			}

			this.getAI().stopAITask();
		}

		return super.decayMe();
	}

	public boolean deleteMe()
	{
		if (this.hasAI())
		{
			this.getAI().stopAITask();
		}

		if (this._summoner != null)
		{
			this._summoner.removeSummonedNpc(this.getObjectId());
		}

		this._effectList.stopAllEffectsWithoutExclusions(false, false);
		if (this._seenCreatures != null)
		{
			CreatureSeeTaskManager.getInstance().remove(this);
			this._seenCreatures.clear();
		}

		this._buffFinishTask.stop();
		this.setWorldRegion(null);
		return true;
	}

	protected void calculateRewards(Creature killer)
	{
	}

	public void doRevive()
	{
		if (this._isDead)
		{
			if (!this._isTeleporting)
			{
				this.setIsPendingRevive(false);
				this.setDead(false);
				if (PlayerConfig.RESPAWN_RESTORE_CP > 0.0 && this._status.getCurrentCp() < this._stat.getMaxCp() * PlayerConfig.RESPAWN_RESTORE_CP)
				{
					this._status.setCurrentCp(this._stat.getMaxCp() * PlayerConfig.RESPAWN_RESTORE_CP);
				}

				if (PlayerConfig.RESPAWN_RESTORE_HP > 0.0 && this._status.getCurrentHp() < this._stat.getMaxHp() * PlayerConfig.RESPAWN_RESTORE_HP)
				{
					this._status.setCurrentHp(this._stat.getMaxHp() * PlayerConfig.RESPAWN_RESTORE_HP);
				}

				if (PlayerConfig.RESPAWN_RESTORE_MP > 0.0 && this._status.getCurrentMp() < this._stat.getMaxMp() * PlayerConfig.RESPAWN_RESTORE_MP)
				{
					this._status.setCurrentMp(this._stat.getMaxMp() * PlayerConfig.RESPAWN_RESTORE_MP);
				}

				this.broadcastPacket(new Revive(this));
				ZoneManager.getInstance().getRegion(this).onRevive(this);
			}
			else
			{
				this.setIsPendingRevive(true);
			}
		}
	}

	public void doRevive(double revivePower)
	{
		this.doRevive();
	}

	public CreatureAI getAI()
	{
		CreatureAI ai = this._ai;
		if (ai == null)
		{
			synchronized (this)
			{
				ai = this._ai;
				if (ai == null)
				{
					this._ai = ai = this.initAI();
				}
			}
		}

		return ai;
	}

	protected CreatureAI initAI()
	{
		return new CreatureAI(this);
	}

	public void detachAI()
	{
		if (!this.isWalker())
		{
			this.setAI(null);
		}
	}

	public void setAI(CreatureAI newAI)
	{
		CreatureAI oldAI = this._ai;
		if (oldAI != null && oldAI != newAI && oldAI instanceof AttackableAI)
		{
			oldAI.stopAITask();
		}

		this._ai = newAI;
	}

	public boolean hasAI()
	{
		return this._ai != null;
	}

	public boolean isRaid()
	{
		return false;
	}

	public boolean isMinion()
	{
		return false;
	}

	public boolean isRaidMinion()
	{
		return false;
	}

	public Set<WeakReference<Creature>> getAttackByList()
	{
		return this._attackByList;
	}

	public boolean isControlBlocked()
	{
		return this.isAffected(EffectFlag.BLOCK_CONTROL);
	}

	public boolean isAllSkillsDisabled()
	{
		return this._allSkillsDisabled || this.hasBlockActions();
	}

	public boolean isAttackDisabled()
	{
		return this.isAttackingNow() || this.isDisabled();
	}

	public boolean isDisabled()
	{
		return this._disabledAI || this.isAlikeDead() || this.isPhysicalAttackMuted() || this.hasBlockActions();
	}

	public boolean isConfused()
	{
		return this.isAffected(EffectFlag.CONFUSED);
	}

	public boolean isAlikeDead()
	{
		return this._isDead;
	}

	public boolean isDead()
	{
		return this._isDead;
	}

	public void setDead(boolean value)
	{
		this._isDead = value;
	}

	public boolean isImmobilized()
	{
		return this._isImmobilized;
	}

	public void setImmobilized(boolean value)
	{
		this._isImmobilized = value;
	}

	public boolean isMuted()
	{
		return this.isAffected(EffectFlag.MUTED);
	}

	public boolean isPhysicalMuted()
	{
		return this.isAffected(EffectFlag.PSYCHICAL_MUTED);
	}

	public boolean isPhysicalAttackMuted()
	{
		return this.isAffected(EffectFlag.PSYCHICAL_ATTACK_MUTED);
	}

	public boolean isMovementDisabled()
	{
		return this.hasBlockActions() || this.isRooted() || this._isOverloaded || this._isImmobilized || this.isAlikeDead() || this._isTeleporting;
	}

	public boolean isOverloaded()
	{
		return this._isOverloaded;
	}

	public void setOverloaded(boolean value)
	{
		this._isOverloaded = value;
	}

	public boolean isPendingRevive()
	{
		return this._isDead && this._isPendingRevive;
	}

	public void setIsPendingRevive(boolean value)
	{
		this._isPendingRevive = value;
	}

	public boolean isDisarmed()
	{
		return this.isAffected(EffectFlag.DISARMED);
	}

	public Summon getPet()
	{
		return null;
	}

	public Map<Integer, Summon> getServitors()
	{
		return Collections.emptyMap();
	}

	public Summon getServitor(int objectId)
	{
		return null;
	}

	public boolean hasSummon()
	{
		return this.getPet() != null || !this.getServitors().isEmpty();
	}

	public boolean hasPet()
	{
		return this.getPet() != null;
	}

	public boolean hasServitor(int objectId)
	{
		return this.getServitors().containsKey(objectId);
	}

	public boolean hasServitors()
	{
		return !this.getServitors().isEmpty();
	}

	public void removeServitor(int objectId)
	{
		this.getServitors().remove(objectId);
	}

	public boolean isRooted()
	{
		return this.isAffected(EffectFlag.ROOTED);
	}

	public boolean isRunning()
	{
		return this._isRunning;
	}

	private void setRunning(boolean value)
	{
		if (this._isRunning != value)
		{
			this._isRunning = value;
			if (this._stat.getRunSpeed() != 0.0)
			{
				this.broadcastPacket(new ChangeMoveType(this));
			}

			if (this.isPlayer())
			{
				this.asPlayer().broadcastUserInfo();
			}
			else if (this.isSummon())
			{
				this.broadcastStatusUpdate();
			}
			else if (this.isNpc())
			{
				World.getInstance().forEachVisibleObject(this, Player.class, player -> {
					if (this.isVisibleFor(player))
					{
						if (this.isFakePlayer())
						{
							player.sendPacket(new FakePlayerInfo(this.asNpc()));
						}
						else if (this._stat.getRunSpeed() == 0.0)
						{
							player.sendPacket(new ServerObjectInfo(this.asNpc(), player));
						}
						else
						{
							player.sendPacket(new NpcInfo(this.asNpc()));
						}
					}
				});
			}
		}
	}

	public void setRunning()
	{
		this.setRunning(true);
	}

	public boolean hasBlockActions()
	{
		return this._blockActions || this.isAffected(EffectFlag.BLOCK_ACTIONS) || this.isAffected(EffectFlag.CONDITIONAL_BLOCK_ACTIONS);
	}

	public void setBlockActions(boolean blockActions)
	{
		this._blockActions = blockActions;
	}

	public boolean isBetrayed()
	{
		return this.isAffected(EffectFlag.BETRAYED);
	}

	public boolean isTeleporting()
	{
		return this._isTeleporting;
	}

	public void setTeleporting(boolean value)
	{
		this._isTeleporting = value;
	}

	public void setInvul(boolean value)
	{
		this._isInvul = value;
	}

	@Override
	public boolean isInvul()
	{
		return this._isInvul || this._isTeleporting;
	}

	public void setUndying(boolean undying)
	{
		this._isUndying = undying;
	}

	public boolean isUndying()
	{
		return this._isUndying || this.isInvul() || this.isAffected(EffectFlag.IGNORE_DEATH) || this.isInsideZone(ZoneId.UNDYING);
	}

	public boolean isHpBlocked()
	{
		return this.isInvul() || this.isAffected(EffectFlag.HP_BLOCK);
	}

	public boolean isMpBlocked()
	{
		return this.isInvul() || this.isAffected(EffectFlag.MP_BLOCK);
	}

	public boolean isBuffBlocked()
	{
		return this.isAffected(EffectFlag.BUFF_BLOCK);
	}

	public boolean isDebuffBlocked()
	{
		return this.isInvul() || this.isAffected(EffectFlag.DEBUFF_BLOCK);
	}

	public boolean isUndead()
	{
		return false;
	}

	public boolean isResurrectionBlocked()
	{
		return this.isAffected(EffectFlag.BLOCK_RESURRECTION);
	}

	public boolean isFlying()
	{
		return this._isFlying;
	}

	public void setFlying(boolean mode)
	{
		this._isFlying = mode;
	}

	public CreatureStat getStat()
	{
		return this._stat;
	}

	public void initCharStat()
	{
		this._stat = new CreatureStat(this);
	}

	public void setStat(CreatureStat value)
	{
		this._stat = value;
	}

	public CreatureStatus getStatus()
	{
		return this._status;
	}

	public void initCharStatus()
	{
		this._status = new CreatureStatus(this);
	}

	public void setStatus(CreatureStatus value)
	{
		this._status = value;
	}

	public CreatureTemplate getTemplate()
	{
		return this._template;
	}

	protected void setTemplate(CreatureTemplate template)
	{
		this._template = template;
	}

	public String getTitle()
	{
		if (!this.isMonster() || !NpcConfig.SHOW_NPC_LEVEL && !NpcConfig.SHOW_NPC_AGGRESSION)
		{
			if (this.isChampion())
			{
				return ChampionMonstersConfig.CHAMP_TITLE;
			}
			if (this.isTrap())
			{
				Player owner = ((Trap) this).getOwner();
				if (owner != null)
				{
					this._title = owner.getName();
				}
			}

			return this._title != null ? this._title : "";
		}
		String t1 = "";
		if (NpcConfig.SHOW_NPC_LEVEL)
		{
			t1 = t1 + "Lv " + this.getLevel();
		}

		String t2 = "";
		if (NpcConfig.SHOW_NPC_AGGRESSION)
		{
			if (!t1.isEmpty())
			{
				t2 = t2 + " ";
			}

			Monster monster = this.asMonster();
			if (monster.isAggressive())
			{
				t2 = t2 + "[A]";
			}

			if (monster.getTemplate().getClans() != null && monster.getTemplate().getClanHelpRange() > 0)
			{
				t2 = t2 + "[G]";
			}
		}

		t1 = t1 + t2;
		if (this._title != null && !this._title.isEmpty())
		{
			t1 = t1 + " " + this._title;
		}

		return this.isChampion() ? ChampionMonstersConfig.CHAMP_TITLE + " " + t1 : t1;
	}

	public void setTitle(String value)
	{
		if (value == null)
		{
			this._title = "";
		}
		else if (this.isPlayer())
		{
			String title = value.replaceAll("\\{i-(\\d+)|\\{i(3[5-9]|[4-9]\\d*)\\}", "");
			if (title.length() > 21)
			{
				this._title = title.substring(0, 20);
			}
			else
			{
				this._title = title;
			}
		}
		else
		{
			this._title = value;
		}
	}

	public void setWalking()
	{
		this.setRunning(false);
	}

	public void startFakeDeath()
	{
		if (this.isPlayer())
		{
			this.abortAttack();
			this.abortCast();
			this.stopMove(null);
			this.getAI().notifyAction(Action.FAKE_DEATH);
			this.broadcastPacket(new ChangeWaitType(this, 2));
			if (PlayerConfig.FAKE_DEATH_UNTARGET)
			{
				World.getInstance().forEachVisibleObject(this, Creature.class, c -> {
					if (c.getTarget() == this)
					{
						c.setTarget(null);
					}
				});
			}
		}
	}

	public void startParalyze()
	{
		this.abortAttack();
		this.abortCast();
		this.stopMove(null);
		this.getAI().notifyAction(Action.BLOCKED);
	}

	public void stopAllEffects()
	{
		this._effectList.stopAllEffects(true);
	}

	public void stopAllEffectsExceptThoseThatLastThroughDeath()
	{
		this._effectList.stopAllEffectsExceptThoseThatLastThroughDeath();
	}

	public void stopSkillEffects(SkillFinishType type, int skillId)
	{
		this._effectList.stopSkillEffects(type, skillId);
	}

	public void stopSkillEffects(Skill skill)
	{
		this._effectList.stopSkillEffects(SkillFinishType.REMOVED, skill.getId());
	}

	public void stopEffects(EffectFlag effectFlag)
	{
		this._effectList.stopEffects(effectFlag);
	}

	public void stopEffectsOnAction()
	{
		this._effectList.stopEffectsOnAction();
	}

	public void stopEffectsOnDamage()
	{
		this._effectList.stopEffectsOnDamage();
	}

	public void stopFakeDeath(boolean removeEffects)
	{
		if (removeEffects)
		{
			this.stopEffects(EffectFlag.FAKE_DEATH);
		}

		if (this.isPlayer())
		{
			this.asPlayer().setRecentFakeDeath(true);
		}

		this.broadcastPacket(new ChangeWaitType(this, 3));
		this.broadcastPacket(new Revive(this));
	}

	public void stopStunning(boolean removeEffects)
	{
		if (removeEffects)
		{
			this._effectList.stopEffects(AbnormalType.STUN);
		}

		if (!this.isPlayer())
		{
			this.getAI().notifyAction(Action.THINK);
		}
	}

	public void stopTransformation(boolean removeEffects)
	{
		if (removeEffects && !this._effectList.stopEffects(AbnormalType.TRANSFORM))
		{
			this._effectList.stopEffects(AbnormalType.CHANGEBODY);
		}

		if (this._transform != null)
		{
			this.untransform();
		}

		if (!this.isPlayer())
		{
			this.getAI().notifyAction(Action.THINK);
		}

		this.updateAbnormalVisualEffects();
	}

	public void updateAbnormalVisualEffects()
	{
	}

	public void updateEffectIcons()
	{
		this.updateEffectIcons(false);
	}

	public void updateEffectIcons(boolean partyOnly)
	{
	}

	public boolean isAffectedBySkill(SkillHolder skill)
	{
		return this.isAffectedBySkill(skill.getSkillId());
	}

	public boolean isAffectedBySkill(int skillId)
	{
		return this._effectList.isAffectedBySkill(skillId);
	}

	public int getAffectedSkillLevel(int skillId)
	{
		BuffInfo info = this._effectList.getBuffInfoBySkillId(skillId);
		return info == null ? 0 : info.getSkill().getLevel();
	}

	public void broadcastModifiedStats(Set<Stat> changed)
	{
		if (this.isSpawned())
		{
			if (changed != null && !changed.isEmpty())
			{
				if (!this.isPlayer() || this.asPlayer().isOnline())
				{
					if (this.isMoving() && this.getMoveSpeed() <= 0.0)
					{
						this.stopMove(null);
					}

					if (this.isSummon())
					{
						Summon summon = this.asSummon();
						if (summon.getOwner() != null)
						{
							summon.updateAndBroadcastStatus(1);
						}
					}
					else if (this.isPlayer())
					{
						Player player = this.asPlayer();
						UserInfo info = new UserInfo(player, false);
						info.addComponentType(UserInfoType.SLOTS, UserInfoType.ENCHANTLEVEL);
						boolean updateWeight = false;

						for (Stat stat : changed)
						{
							switch (stat)
							{
								case MOVE_SPEED:
								case RUN_SPEED:
								case WALK_SPEED:
								case SWIM_RUN_SPEED:
								case SWIM_WALK_SPEED:
								case FLY_RUN_SPEED:
								case FLY_WALK_SPEED:
									info.addComponentType(UserInfoType.MULTIPLIER);
									break;
								case PHYSICAL_ATTACK_SPEED:
									info.addComponentType(UserInfoType.MULTIPLIER, UserInfoType.STATS);
									break;
								case PHYSICAL_ATTACK:
								case PHYSICAL_DEFENCE:
								case EVASION_RATE:
								case ACCURACY_COMBAT:
								case CRITICAL_RATE:
								case CRITICAL_RATE_SKILL:
								case MAGIC_CRITICAL_RATE:
								case MAGIC_EVASION_RATE:
								case ACCURACY_MAGIC:
								case MAGIC_ATTACK:
								case MAGIC_ATTACK_SPEED:
								case MAGICAL_DEFENCE:
									info.addComponentType(UserInfoType.STATS);
									break;
								case MAX_CP:
									info.addComponentType(UserInfoType.MAX_HPCPMP);
									break;
								case MAX_HP:
									info.addComponentType(UserInfoType.MAX_HPCPMP);
									break;
								case MAX_MP:
									info.addComponentType(UserInfoType.MAX_HPCPMP);
									break;
								case STAT_STR:
								case STAT_CON:
								case STAT_DEX:
								case STAT_INT:
								case STAT_WIT:
								case STAT_MEN:
									player.calculateStatIncreaseSkills();
									player.calculateMaxBeastPoints();
									info.addComponentType(UserInfoType.BASE_STATS);
									updateWeight = true;
									break;
								case FIRE_RES:
								case WATER_RES:
								case WIND_RES:
								case EARTH_RES:
								case HOLY_RES:
								case DARK_RES:
									info.addComponentType(UserInfoType.ELEMENTALS);
									break;
								case FIRE_POWER:
								case WATER_POWER:
								case WIND_POWER:
								case EARTH_POWER:
								case HOLY_POWER:
								case DARK_POWER:
									info.addComponentType(UserInfoType.ATK_ELEMENTAL);
									break;
								case WEIGHT_LIMIT:
								case WEIGHT_PENALTY:
									updateWeight = true;
									break;
								case ELEMENTAL_SPIRIT_EARTH_ATTACK:
								case ELEMENTAL_SPIRIT_EARTH_DEFENSE:
								case ELEMENTAL_SPIRIT_FIRE_ATTACK:
								case ELEMENTAL_SPIRIT_FIRE_DEFENSE:
								case ELEMENTAL_SPIRIT_WATER_ATTACK:
								case ELEMENTAL_SPIRIT_WATER_DEFENSE:
								case ELEMENTAL_SPIRIT_WIND_ATTACK:
								case ELEMENTAL_SPIRIT_WIND_DEFENSE:
									info.addComponentType(UserInfoType.ATT_SPIRITS);
							}
						}

						if (updateWeight)
						{
							player.refreshOverloaded(true);
						}

						this.sendPacket(info);
						player.broadcastCharInfo();
						if (this.hasServitors() && this.hasAbnormalType(AbnormalType.ABILITY_CHANGE))
						{
							this.getServitors().values().forEach(Creature::broadcastStatusUpdate);
						}
					}
					else if (this.isNpc())
					{
						World.getInstance().forEachVisibleObject(this, Player.class, playerx -> {
							if (this.isVisibleFor(playerx))
							{
								if (this.isFakePlayer())
								{
									playerx.sendPacket(new FakePlayerInfo(this.asNpc()));
								}
								else if (this.getRunSpeed() == 0.0)
								{
									playerx.sendPacket(new ServerObjectInfo(this.asNpc(), playerx));
								}
								else
								{
									playerx.sendPacket(new NpcInfo(this.asNpc()));
								}
							}
						});
					}
				}
			}
		}
	}

	public int getXdestination()
	{
		Creature.MoveData move = this._move;
		return move != null ? move.xDestination : this.getX();
	}

	public int getYdestination()
	{
		Creature.MoveData move = this._move;
		return move != null ? move.yDestination : this.getY();
	}

	public int getZdestination()
	{
		Creature.MoveData move = this._move;
		return move != null ? move.zDestination : this.getZ();
	}

	public boolean isInCombat()
	{
		return this.hasAI() && this.getAI().isAutoAttacking();
	}

	public boolean isMoving()
	{
		return this._move != null;
	}

	public boolean isOnGeodataPath()
	{
		Creature.MoveData move = this._move;
		return move == null ? false : this.isOnGeodataPath(move);
	}

	public boolean isOnGeodataPath(Creature.MoveData move)
	{
		return move.onGeodataPathIndex == -1 ? false : move.onGeodataPathIndex != move.geoPath.size() - 1;
	}

	public List<GeoLocation> getGeoPath()
	{
		Creature.MoveData move = this._move;
		return move != null ? move.geoPath : null;
	}

	public boolean isCastingNow()
	{
		return !this._skillCasters.isEmpty();
	}

	public boolean isCastingNow(SkillCastingType skillCastingType)
	{
		return this._skillCasters.containsKey(skillCastingType);
	}

	public boolean isCastingNow(Predicate<SkillCaster> filter)
	{
		for (SkillCaster skillCaster : this._skillCasters.values())
		{
			if (filter.test(skillCaster))
			{
				return true;
			}
		}

		return false;
	}

	public boolean isAttackingOrCastingNow()
	{
		return this.isAttackingNow() || this.isRangeAttackingNow() || this.isCastingNow();
	}

	public boolean isAttackingNow()
	{
		return this._attackEndTime > System.nanoTime();
	}

	public final boolean isRangeAttackingNow()
	{
		return this._disableRangedAttackEndTime > System.nanoTime();
	}

	public void abortAttack()
	{
		if (this.isAttackingNow())
		{
			CreatureAttackTaskManager.getInstance().abortAttack(this);
			this.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	public void abortAllSkillCasters()
	{
		for (SkillCaster skillCaster : this.getSkillCasters())
		{
			skillCaster.stopCasting(true);
			if (this.isPlayer())
			{
				this.asPlayer().setQueuedSkill(null, null, false, false);
			}
		}
	}

	public boolean abortCast()
	{
		return this.abortCast(SkillCaster::isAnyNormalType);
	}

	public boolean abortCast(Predicate<SkillCaster> filter)
	{
		SkillCaster skillCaster = this.getSkillCaster(SkillCaster::canAbortCast, filter);
		if (skillCaster != null)
		{
			skillCaster.stopCasting(true);
			if (this.isPlayer())
			{
				this.asPlayer().setQueuedSkill(null, null, false, false);
			}

			return true;
		}
		return false;
	}

	public boolean updatePosition()
	{
		if (!this.isSpawned())
		{
			this._move = null;
			return true;
		}
		Creature.MoveData move = this._move;
		if (move == null)
		{
			return true;
		}
		if (move.moveTimestamp == 0)
		{
			move.moveTimestamp = move.moveStartTime;
			move.xAccurate = this.getX();
			move.yAccurate = this.getY();
		}

		int gameTicks = GameTimeTaskManager.getInstance().getGameTicks();
		if (move.moveTimestamp == gameTicks)
		{
			return false;
		}
		int xPrev = this.getX();
		int yPrev = this.getY();
		int zPrev = this.getZ();
		double dx = move.xDestination - move.xAccurate;
		double dy = move.yDestination - move.yAccurate;
		double dz = move.zDestination - zPrev;
		if (this.isPlayer() && !this._isFlying)
		{
			if (this._cursorKeyMovement)
			{
				double angle = LocationUtil.convertHeadingToDegree(this.getHeading());
				double radian = Math.toRadians(angle);
				double course = Math.toRadians(180.0);
				double frontDistance = 10.0 * (this._stat.getMoveSpeed() / 100.0);
				int x1 = (int) (Math.cos(Math.PI + radian + course) * frontDistance);
				int y1 = (int) (Math.sin(Math.PI + radian + course) * frontDistance);
				int x = xPrev + x1;
				int y = yPrev + y1;
				if (!GeoEngine.getInstance().canMoveToTarget(xPrev, yPrev, zPrev, x, y, zPrev, this.getInstanceWorld()))
				{
					this._move.onGeodataPathIndex = -1;
					this.stopMove(this.asPlayer().getLastServerPosition());
					return true;
				}
			}
			else
			{
				double distance = Math.hypot(dx, dy);
				if (distance > 3000.0)
				{
					double angle = LocationUtil.convertHeadingToDegree(this.getHeading());
					double radian = Math.toRadians(angle);
					double course = Math.toRadians(180.0);
					double frontDistance = 10.0 * (this._stat.getMoveSpeed() / 100.0);
					int x1 = (int) (Math.cos(Math.PI + radian + course) * frontDistance);
					int y1 = (int) (Math.sin(Math.PI + radian + course) * frontDistance);
					int x = xPrev + x1;
					int y = yPrev + y1;
					if (!GeoEngine.getInstance().canMoveToTarget(xPrev, yPrev, zPrev, x, y, zPrev, this.getInstanceWorld()))
					{
						this._move.onGeodataPathIndex = -1;
						if (this.hasAI())
						{
							if (this.getAI().isFollowing())
							{
								this.getAI().stopFollow();
							}

							this.getAI().setIntention(Intention.IDLE);
						}

						return true;
					}
				}
				else if (this.hasAI() && this.getAI().getIntention() == Intention.ATTACK)
				{
					double angle = LocationUtil.convertHeadingToDegree(this.getHeading());
					double radian = Math.toRadians(angle);
					double course = Math.toRadians(180.0);
					double frontDistance = 10.0 * (this._stat.getMoveSpeed() / 100.0);
					int x1 = (int) (Math.cos(Math.PI + radian + course) * frontDistance);
					int y1 = (int) (Math.sin(Math.PI + radian + course) * frontDistance);
					int x = xPrev + x1;
					int y = yPrev + y1;
					if (!GeoEngine.getInstance().canMoveToTarget(xPrev, yPrev, zPrev, x, y, zPrev, this.getInstanceWorld()))
					{
						this._move.onGeodataPathIndex = -1;
						this.broadcastPacket(new StopMove(this));
						return true;
					}
				}
				else
				{
					WorldRegion region = this.getWorldRegion();
					if (region != null)
					{
						boolean hasDoors = !region.getDoors().isEmpty();
						boolean hasFences = !region.getFences().isEmpty();
						if (hasDoors || hasFences)
						{
							double angle = LocationUtil.convertHeadingToDegree(this.getHeading());
							double radian = Math.toRadians(angle);
							double course = Math.toRadians(180.0);
							double frontDistance = 10.0 * (this._stat.getMoveSpeed() / 100.0);
							int x1 = (int) (Math.cos(Math.PI + radian + course) * frontDistance);
							int y1 = (int) (Math.sin(Math.PI + radian + course) * frontDistance);
							int x = xPrev + x1;
							int y = yPrev + y1;
							if (hasDoors && DoorData.getInstance().checkIfDoorsBetween(xPrev, yPrev, zPrev, x, y, zPrev, this.getInstanceWorld(), false) || hasFences && FenceData.getInstance().checkIfFenceBetween(xPrev, yPrev, zPrev, x, y, zPrev, this.getInstanceWorld()))
							{
								this._move.onGeodataPathIndex = -1;
								if (this.hasAI())
								{
									if (this.getAI().isFollowing())
									{
										this.getAI().stopFollow();
									}

									this.getAI().setIntention(Intention.IDLE);
								}

								this.stopMove(null);
								return true;
							}
						}
					}
				}
			}
		}

		double delta = dx * dx + dy * dy;
		boolean isFloating = this._isFlying || this.isInsideZone(ZoneId.WATER) && !this.isInsideZone(ZoneId.CASTLE);
		if (!isFloating && delta < 10000.0 && dz * dz > 2500.0)
		{
			delta = Math.sqrt(delta);
		}
		else
		{
			delta = Math.sqrt(delta + dz * dz);
		}

		WorldObject target = this._target;
		double collision;
		if (target != null && target.isCreature() && this.hasAI() && this.getAI().getIntention() == Intention.ATTACK)
		{
			collision = target.asCreature().getCollisionRadius();
		}
		else
		{
			collision = this.getCollisionRadius();
		}

		delta = Math.max(1.0E-5, delta - collision);
		double distFraction = Double.MAX_VALUE;
		if (delta > 1.0)
		{
			double distPassed = this._stat.getMoveSpeed() * (gameTicks - move.moveTimestamp) / 10.0;
			distFraction = distPassed / delta;
		}

		boolean arrived = distFraction > 1.79;
		if (arrived)
		{
			super.setXYZ(move.xDestination, move.yDestination, move.zDestination);
		}
		else
		{
			int newZ = zPrev + (int) (dz * distFraction + 0.895);
			move.xAccurate += dx * distFraction;
			move.yAccurate += dy * distFraction;
			if (this.isAttackable() && !isFloating && Math.abs(newZ - zPrev) > 300)
			{
				Spawn spawn = this.asAttackable().getSpawn();
				if (spawn != null)
				{
					this.teleToLocation(spawn, this.getInstanceWorld());
					this.getAttackByList().clear();
					this.asAttackable().clearAggroList();
					this.getAI().setIntention(Intention.IDLE);
				}

				return true;
			}

			super.setXYZ((int) move.xAccurate, (int) move.yAccurate, newZ);
		}

		this.revalidateZone(false);
		move.moveTimestamp = gameTicks;
		if (arrived && !this.isOnGeodataPath())
		{
			this.broadcastMoveToLocation(true);
		}
		else if (this.isAttackable() && target != null)
		{
			this.broadcastMoveToLocation();
		}

		return arrived;
	}

	public void revalidateZone(boolean force)
	{
		if (force || !(this.calculateDistance3D(this._lastZoneValidateLocation) < (this.isNpc() && !this.isInCombat() ? NpcConfig.MAX_DRIFT_RANGE : 100)))
		{
			this._lastZoneValidateLocation.setXYZ(this);
			ZoneRegion region = ZoneManager.getInstance().getRegion(this);
			if (region != null)
			{
				region.revalidateZones(this);
			}
			else
			{
				World.getInstance().disposeOutOfBoundsObject(this);
			}
		}
	}

	public void stopMove(Location loc)
	{
		this._move = null;
		this._cursorKeyMovement = false;
		if (loc != null)
		{
			this.setXYZ(loc.getX(), loc.getY(), loc.getZ());
			this.setHeading(loc.getHeading());
			this.revalidateZone(true);
		}

		this.broadcastPacket(new StopMove(this));
	}

	public boolean isShowSummonAnimation()
	{
		return this._showSummonAnimation;
	}

	public void setShowSummonAnimation(boolean showSummonAnimation)
	{
		this._showSummonAnimation = showSummonAnimation;
	}

	public void setTarget(WorldObject object)
	{
		if (object != null && !object.isSpawned())
		{
			this._target = null;
		}
		else
		{
			this._target = object;
		}
	}

	public int getTargetId()
	{
		return this._target != null ? this._target.getObjectId() : 0;
	}

	public WorldObject getTarget()
	{
		return this._target;
	}

	public void moveToLocation(int xValue, int yValue, int zValue, int offsetValue)
	{
		double speed = this._stat.getMoveSpeed();
		if (!(speed <= 0.0) && !this.isMovementDisabled())
		{
			int x = xValue;
			int y = yValue;
			int z = zValue;
			int curX = this.getX();
			int curY = this.getY();
			int curZ = this.getZ();
			double dx = xValue - curX;
			double dy = yValue - curY;
			double dz = zValue - curZ;
			double distance = Math.hypot(dx, dy);
			boolean verticalMovementOnly = this._isFlying && distance == 0.0 && dz != 0.0;
			if (verticalMovementOnly)
			{
				distance = Math.abs(dz);
			}

			boolean isInWater = this.isInsideZone(ZoneId.WATER) && !this.isInsideZone(ZoneId.CASTLE);
			if (isInWater && distance > 700.0)
			{
				double divider = 700.0 / distance;
				x = curX + (int) (divider * dx);
				y = curY + (int) (divider * dy);
				z = curZ + (int) (divider * dz);
				dx = x - curX;
				dy = y - curY;
				dz = z - curZ;
				distance = Math.hypot(dx, dy);
			}

			double sin;
			double cos;
			if (offsetValue <= 0 && !(distance < 1.79))
			{
				sin = dy / distance;
				cos = dx / distance;
			}
			else
			{
				int offset = (int) (offsetValue - Math.abs(dz));
				if (offset < 5)
				{
					offset = 5;
				}

				if (distance < 1.79 || distance - offset <= 0.0)
				{
					this.getAI().notifyAction(Action.ARRIVED);
					this.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}

				sin = dy / distance;
				cos = dx / distance;
				distance -= offset - 5;
				x = curX + (int) (distance * cos);
				y = curY + (int) (distance * sin);
			}

			Creature.MoveData move = new Creature.MoveData();
			WorldRegion region = this.getWorldRegion();
			move.disregardingGeodata = region == null || !region.areNeighborsActive();
			move.onGeodataPathIndex = -1;
			if (!move.disregardingGeodata && !this._isFlying && !isInWater && !this.isVehicle() && !this._cursorKeyMovement)
			{
				boolean isInVehicle = this.isPlayer() && this.asPlayer().getVehicle() != null;
				if (isInVehicle)
				{
					move.disregardingGeodata = true;
				}

				if (GeoEngineConfig.PATHFINDING > 0 && !(this instanceof FriendlyNpc))
				{
					int originalX = x;
					int originalY = y;
					int originalZ = z;
					double originalDistance = distance;
					int gtx = x - -294912 >> 4;
					int gty = y - -262144 >> 4;
					if (this.isOnGeodataPath())
					{
						try
						{
							if (gtx == this._move.geoPathGtx && gty == this._move.geoPathGty)
							{
								this.sendPacket(ActionFailed.STATIC_PACKET);
								return;
							}

							this._move.onGeodataPathIndex = -1;
						}
						catch (NullPointerException var54)
						{
						}
					}

					boolean directMove = this.isPlayer() && this.hasAI() && this.getAI().getIntention() == Intention.ATTACK;
					if (directMove || !isInVehicle && (!this.isPlayer() || !(distance > 3000.0)) && (!this.isMonster() || !(Math.abs(dz) > 100.0)) && (curZ - z <= 300 || !(distance < 300.0)))
					{
						Location destination = GeoEngine.getInstance().getValidLocation(curX, curY, curZ, x, y, z, this.getInstanceWorld());
						x = destination.getX();
						y = destination.getY();
						if (!this.isPlayer())
						{
							z = destination.getZ();
						}

						dx = x - curX;
						dy = y - curY;
						dz = z - curZ;
						distance = verticalMovementOnly ? Math.pow(dz, 2.0) : Math.hypot(dx, dy);
					}

					if (!directMove && originalDistance - distance > 30.0 && !this.isControlBlocked() && !isInVehicle)
					{
						move.geoPath = PathFinding.getInstance().findPath(curX, curY, curZ, originalX, originalY, originalZ, this.getInstanceWorld(), this.isPlayer());
						boolean found = move.geoPath != null && move.geoPath.size() > 1;
						if (!found && this.isAttackable())
						{
							int xMin = Math.min(curX, originalX);
							int xMax = Math.max(curX, originalX);
							int yMin = Math.min(curY, originalY);
							int yMax = Math.max(curY, originalY);
							int maxDiff = Math.min(Math.max(xMax - xMin, yMax - yMin), 500);
							xMin -= maxDiff;
							xMax += maxDiff;
							yMin -= maxDiff;
							yMax += maxDiff;
							int destinationX = 0;
							int destinationY = 0;
							double shortDistance = Double.MAX_VALUE;

							for (int sX = xMin; sX < xMax; sX += 500)
							{
								for (int sY = yMin; sY < yMax; sY += 500)
								{
									double tempDistance = Math.hypot(sX - originalX, sY - originalY);
									if (tempDistance < shortDistance)
									{
										List<GeoLocation> tempPath = PathFinding.getInstance().findPath(curX, curY, curZ, sX, sY, originalZ, this.getInstanceWorld(), false);
										found = tempPath != null && tempPath.size() > 1;
										if (found)
										{
											shortDistance = tempDistance;
											move.geoPath = tempPath;
											destinationX = sX;
											destinationY = sY;
										}
									}
								}
							}

							found = move.geoPath != null && move.geoPath.size() > 1;
							if (found)
							{
								originalX = destinationX;
								originalY = destinationY;
							}
						}

						if (found)
						{
							move.onGeodataPathIndex = 0;
							move.geoPathGtx = gtx;
							move.geoPathGty = gty;
							move.geoPathAccurateTx = originalX;
							move.geoPathAccurateTy = originalY;
							x = move.geoPath.get(move.onGeodataPathIndex).getX();
							y = move.geoPath.get(move.onGeodataPathIndex).getY();
							z = move.geoPath.get(move.onGeodataPathIndex).getZ();
							dx = x - curX;
							dy = y - curY;
							dz = z - curZ;
							distance = verticalMovementOnly ? Math.pow(dz, 2.0) : Math.hypot(dx, dy);
							sin = dy / distance;
							cos = dx / distance;
						}
						else
						{
							move.disregardingGeodata = true;
							x = originalX;
							y = originalY;
							z = originalZ;
							distance = originalDistance;
						}
					}

					if (this.isPlayable() && !this._cursorKeyMovement && move.geoPath == null && !isInVehicle && distance < 3000.0 && (curZ - z <= 300 || !(distance < 300.0)))
					{
						Location destination = GeoEngine.getInstance().getValidLocation(curX, curY, curZ, x, y, z, this.getInstanceWorld());
						x = destination.getX();
						y = destination.getY();
						z = destination.getZ();
						dx = x - curX;
						dy = y - curY;
						dz = z - curZ;
						distance = verticalMovementOnly ? Math.pow(dz, 2.0) : Math.hypot(dx, dy);
					}
				}

				if (distance < 1.79 && (GeoEngineConfig.PATHFINDING > 0 || this.isPlayable()))
				{
					if (this.isSummon())
					{
						if (this.getAI().getTarget() != this.asPlayer())
						{
							this.asSummon().setFollowStatus(false);
							this.getAI().setIntention(Intention.IDLE);
						}
					}
					else
					{
						this.getAI().setIntention(Intention.IDLE);
					}

					this.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}

			if ((this._isFlying || isInWater) && !verticalMovementOnly)
			{
				distance = Math.hypot(distance, dz);
			}

			int ticksToMove = (int) (10.0 * distance / speed);
			move.xDestination = x;
			move.yDestination = y;
			move.zDestination = z;
			move.heading = 0;
			if (!verticalMovementOnly)
			{
				this.setHeading(LocationUtil.calculateHeadingFrom(cos, sin));
			}

			move.moveStartTime = GameTimeTaskManager.getInstance().getGameTicks();
			this._move = move;
			MovementTaskManager.getInstance().registerMovingObject(this);
			if (ticksToMove * 100 > 3000)
			{
				ThreadPool.schedule(new NotifyAITask(this, Action.ARRIVED_REVALIDATE), 2000L);
			}
		}
		else
		{
			this.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	public boolean moveToNextRoutePoint()
	{
		Creature.MoveData move = this._move;
		if (move == null)
		{
			return false;
		}
		else if (!this.isOnGeodataPath(move))
		{
			this._move = null;
			return false;
		}
		else
		{
			double speed = this._stat.getMoveSpeed();
			if (!(speed <= 0.0) && !this.isMovementDisabled())
			{
				int curX = this.getX();
				int curY = this.getY();
				Creature.MoveData newMove = new Creature.MoveData();
				newMove.onGeodataPathIndex = move.onGeodataPathIndex + 1;
				newMove.geoPath = move.geoPath;
				newMove.geoPathGtx = move.geoPathGtx;
				newMove.geoPathGty = move.geoPathGty;
				newMove.geoPathAccurateTx = move.geoPathAccurateTx;
				newMove.geoPathAccurateTy = move.geoPathAccurateTy;
				if (move.onGeodataPathIndex == move.geoPath.size() - 2)
				{
					newMove.xDestination = move.geoPathAccurateTx;
					newMove.yDestination = move.geoPathAccurateTy;
					newMove.zDestination = move.geoPath.get(newMove.onGeodataPathIndex).getZ();
				}
				else
				{
					newMove.xDestination = move.geoPath.get(newMove.onGeodataPathIndex).getX();
					newMove.yDestination = move.geoPath.get(newMove.onGeodataPathIndex).getY();
					newMove.zDestination = move.geoPath.get(newMove.onGeodataPathIndex).getZ();
				}

				double distance = Math.hypot(newMove.xDestination - curX, newMove.yDestination - curY);
				if (distance != 0.0)
				{
					this.setHeading(LocationUtil.calculateHeadingFrom(curX, curY, newMove.xDestination, newMove.yDestination));
				}

				int ticksToMove = (int) (10.0 * distance / speed);
				newMove.heading = 0;
				newMove.moveStartTime = GameTimeTaskManager.getInstance().getGameTicks();
				this._move = newMove;
				MovementTaskManager.getInstance().registerMovingObject(this);
				if (ticksToMove * 100 > 3000)
				{
					ThreadPool.schedule(new NotifyAITask(this, Action.ARRIVED_REVALIDATE), 2000L);
				}

				this.broadcastMoveToLocation(true);
				return true;
			}
			this._move = null;
			return false;
		}
	}

	public boolean validateMovementHeading(int heading)
	{
		Creature.MoveData move = this._move;
		if (move == null)
		{
			return true;
		}
		boolean result = true;
		if (move.heading != heading)
		{
			result = move.heading == 0;
			move.heading = heading;
		}

		return result;
	}

	public boolean isInsideRadius2D(ILocational loc, int radius)
	{
		return this.isInsideRadius2D(loc.getX(), loc.getY(), loc.getZ(), radius);
	}

	public boolean isInsideRadius2D(int x, int y, int z, int radius)
	{
		return this.calculateDistance2D(x, y, z) < radius;
	}

	public boolean isInsideRadius3D(ILocational loc, int radius)
	{
		return this.isInsideRadius3D(loc.getX(), loc.getY(), loc.getZ(), radius);
	}

	public boolean isInsideRadius3D(int x, int y, int z, int radius)
	{
		return this.calculateDistance3D(x, y, z) < radius;
	}

	protected boolean checkAndEquipAmmunition(EtcItemType type)
	{
		return true;
	}

	public synchronized void addExpAndSp(double addToExp, double addToSp)
	{
	}

	public abstract Item getActiveWeaponInstance();

	public abstract Weapon getActiveWeaponItem();

	public abstract Item getSecondaryWeaponInstance();

	public abstract ItemTemplate getSecondaryWeaponItem();

	public void onHitTimeNotDual(Weapon weapon, Attack attack, int hitTime, int attackTime)
	{
		if (this._isDead)
		{
			this.getAI().notifyAction(Action.CANCEL);
		}
		else
		{
			for (Hit hit : attack.getHits())
			{
				Creature target = hit.getTarget().asCreature();
				if (target != null && !target.isDead() && this.isInSurroundingRegion(target))
				{
					if (hit.isMiss())
					{
						this.notifyAttackAvoid(target, false);
					}
					else if (weapon != null && weapon.getItemType().isRanged() && !GeoEngine.getInstance().canSeeTarget(this, target))
					{
						if (target.isPlayer())
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_AVOIDED_C1_S_ATTACK);
							sm.addString(this.getName());
							target.sendPacket(sm);
						}

						if (this.isPlayer())
						{
							this.sendPacket(SystemMessageId.YOU_HAVE_MISSED);
						}
					}
					else
					{
						this.onHitTarget(target, weapon, hit);
					}
				}
			}

			CreatureAttackTaskManager.getInstance().onAttackFinish(this, attack, attackTime - hitTime);
		}
	}

	public void onFirstHitTimeForDual(Weapon weapon, Attack attack, int hitTime, int attackTime, int delayForSecondAttack)
	{
		if (this._isDead)
		{
			this.getAI().notifyAction(Action.CANCEL);
		}
		else
		{
			CreatureAttackTaskManager.getInstance().onSecondHitTimeForDual(this, weapon, attack, hitTime, attackTime, delayForSecondAttack);
			Hit hit = attack.getHits().get(0);
			Creature target = hit.getTarget().asCreature();
			if (target != null && !target.isDead() && this.isInSurroundingRegion(target))
			{
				if (hit.isMiss())
				{
					this.notifyAttackAvoid(target, false);
				}
				else
				{
					this.onHitTarget(target, weapon, hit);
				}
			}
			else
			{
				this.getAI().notifyAction(Action.CANCEL);
			}
		}
	}

	public void onSecondHitTimeForDual(Weapon weapon, Attack attack, int hitTime1, int hitTime2, int attackTime)
	{
		if (this._isDead)
		{
			this.getAI().notifyAction(Action.CANCEL);
		}
		else
		{
			for (int i = 1; i < attack.getHits().size(); i++)
			{
				Hit hit = attack.getHits().get(i);
				Creature target = hit.getTarget().asCreature();
				if (target != null && !target.isDead() && this.isInSurroundingRegion(target))
				{
					if (hit.isMiss())
					{
						this.notifyAttackAvoid(target, false);
					}
					else
					{
						this.onHitTarget(target, weapon, hit);
					}
				}
			}

			CreatureAttackTaskManager.getInstance().onAttackFinish(this, attack, attackTime - (hitTime1 + hitTime2));
		}
	}

	public void onHitTarget(Creature target, Weapon weapon, Hit hit)
	{
		this.doAttack(hit.getDamage(), target, null, false, false, hit.isCritical(), false);
		if (EventDispatcher.getInstance().hasListener(EventType.ON_CREATURE_ATTACK, this))
		{
			if (this._onCreatureAttack == null)
			{
				this._onCreatureAttack = new OnCreatureAttack();
			}

			this._onCreatureAttack.setAttacker(this);
			this._onCreatureAttack.setTarget(target);
			this._onCreatureAttack.setSkill(null);
			EventDispatcher.getInstance().notifyEvent(this._onCreatureAttack, this);
		}

		if (EventDispatcher.getInstance().hasListener(EventType.ON_CREATURE_ATTACKED, target))
		{
			if (this._onCreatureAttacked == null)
			{
				this._onCreatureAttacked = new OnCreatureAttacked();
			}

			this._onCreatureAttacked.setAttacker(this);
			this._onCreatureAttacked.setTarget(target);
			this._onCreatureAttacked.setSkill(null);
			EventDispatcher.getInstance().notifyEvent(this._onCreatureAttacked, target);
		}

		if (!this._triggerSkills.isEmpty())
		{
			for (OptionSkillHolder holder : this._triggerSkills.values())
			{
				if ((!hit.isCritical() && holder.getSkillType() == OptionSkillType.ATTACK || holder.getSkillType() == OptionSkillType.CRITICAL && hit.isCritical()) && Rnd.get(100) < holder.getChance())
				{
					SkillCaster.triggerCast(this, target, holder.getSkill(), null, false);
				}
			}
		}

		if (hit.isCritical() && weapon != null)
		{
			weapon.applyConditionalSkills(this, target, null, ItemSkillType.ON_CRITICAL_SKILL);
		}

		if (this.isPlayer() && !target.isHpBlocked())
		{
			Player player = this.asPlayer();
			if (player.isCursedWeaponEquipped() || player.isHero() && target.isPlayer() && target.asPlayer().isCursedWeaponEquipped())
			{
				target.setCurrentCp(0.0);
			}

			if (player.isDeathKnight())
			{
				if (target.isAttackable() || target.isPlayable())
				{
					player.setDeathPoints(player.getDeathPoints() + 2);
				}
			}
			else if (player.isVanguard())
			{
				if (target.isAttackable() || target.isPlayable())
				{
					player.setBeastPoints(player.getBeastPoints() + 5);
				}
			}
			else if (player.isAssassin() && player.getPlayerClass().level() > 2 && target.isDead())
			{
				if (target.isPlayable())
				{
					player.setAssassinationPoints(player.getAssassinationPoints() + 1000);
					player.sendPacket(new UserInfo(player));
				}
				else if (target.isAttackable())
				{
					player.setAssassinationPoints(player.getAssassinationPoints() + 5);
					player.sendPacket(new UserInfo(player));
				}
			}
			else if (player.isWarg() && !player.isTransformed() && (target.isAttackable() || target.isPlayable()))
			{
				player.setWolfPoints(player.getWolfPoints() + 2);
			}
		}
	}

	public void onAttackFinish(Attack attack)
	{
		for (Hit hit : attack.getHits())
		{
			if (!hit.isMiss())
			{
				this.rechargeShots(true, false, false);
				break;
			}
		}

		this.getAI().notifyAction(Action.READY_TO_ACT);
	}

	public void breakAttack()
	{
		if (this.isAttackingNow())
		{
			this.abortAttack();
			if (this.isPlayer())
			{
				this.sendPacket(SystemMessageId.YOUR_ATTACK_HAS_FAILED);
			}
		}
	}

	public void breakCast()
	{
		SkillCaster skillCaster = this.getSkillCaster(SkillCaster::isAnyNormalType);
		if (skillCaster != null && skillCaster.getSkill().isMagic())
		{
			skillCaster.stopCasting(true);
			if (this.isPlayer())
			{
				this.sendPacket(SystemMessageId.YOUR_CASTING_HAS_BEEN_INTERRUPTED);
			}
		}
	}

	@Override
	public void onForcedAttack(Player player)
	{
		if (this.isInsidePeaceZone(player))
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			if (player.isInOlympiadMode() && player.getTarget() != null && player.getTarget().isPlayable())
			{
				Player target = null;
				WorldObject object = player.getTarget();
				if (object != null && object.isPlayable())
				{
					target = object.asPlayer();
				}

				if (target == null || target.isInOlympiadMode() && (!player.isOlympiadStart() || player.getOlympiadGameId() != target.getOlympiadGameId()))
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}

			if (player.getTarget() != null && !player.getTarget().canBeAttacked() && !player.getAccessLevel().allowPeaceAttack())
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else if (player.isConfused())
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else
			{
				player.getAI().setIntention(Intention.ATTACK, this);
			}
		}
	}

	public boolean isInsidePeaceZone(WorldObject attacker)
	{
		return this.isInsidePeaceZone(attacker, this);
	}

	public boolean isInsidePeaceZone(WorldObject attacker, WorldObject target)
	{
		Instance instanceWorld = this.getInstanceWorld();
		if (target != null && (target.isPlayable() || target.isFakePlayer()) && attacker.isPlayable() && (instanceWorld == null || !instanceWorld.isPvP()))
		{
			Player attackerPlayer = attacker.asPlayer();
			if (PlayerConfig.ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE)
			{
				Player targetPlayer = target.asPlayer();
				if (targetPlayer != null && targetPlayer.getReputation() < 0)
				{
					return false;
				}

				if (attackerPlayer != null && attackerPlayer.getReputation() < 0 && targetPlayer != null && targetPlayer.getPvpFlag() > 0)
				{
					return false;
				}
			}

			return attackerPlayer != null && attackerPlayer.getAccessLevel().allowPeaceAttack() ? false : target.isInsideZone(ZoneId.PEACE) || attacker.isInsideZone(ZoneId.PEACE) || target.isInsideZone(ZoneId.NO_PVP) || attacker.isInsideZone(ZoneId.NO_PVP);
		}
		return false;
	}

	public boolean isInActiveRegion()
	{
		WorldRegion region = this.getWorldRegion();
		return region != null && region.isActive();
	}

	public boolean isInParty()
	{
		return false;
	}

	public Party getParty()
	{
		return null;
	}

	public Skill addSkill(Skill skill)
	{
		Skill oldSkill = null;
		Skill newSkill = skill;
		if (skill != null)
		{
			Skill existingSkill = this._skills.get(skill.getId());
			if (existingSkill != null && existingSkill.getSubLevel() > 0 && skill.getSubLevel() == 0 && existingSkill.getLevel() < skill.getLevel())
			{
				newSkill = SkillData.getInstance().getSkill(skill.getId(), skill.getLevel(), existingSkill.getSubLevel());
			}

			oldSkill = this._skills.put(newSkill.getId(), newSkill);
			if (oldSkill != null)
			{
				if (oldSkill.isPassive())
				{
					this._effectList.stopSkillEffects(SkillFinishType.REMOVED, oldSkill);
				}

				this._stat.recalculateStats(true);
			}

			if (newSkill.isPassive())
			{
				newSkill.applyEffects(this, this, false, true, false, 0, null);
			}
		}

		return oldSkill;
	}

	public Skill removeSkill(Skill skill, boolean cancelEffect)
	{
		return skill != null ? this.removeSkill(skill.getId(), cancelEffect) : null;
	}

	public Skill removeSkill(int skillId)
	{
		return this.removeSkill(skillId, true);
	}

	public Skill removeSkill(int skillId, boolean cancelEffect)
	{
		Skill oldSkill = this._skills.remove(skillId);
		if (oldSkill != null)
		{
			this.abortCast(s -> s.getSkill().getId() == skillId);
			if (cancelEffect || oldSkill.isToggle() || oldSkill.isPassive())
			{
				this.stopSkillEffects(SkillFinishType.REMOVED, oldSkill.getId());
				this._stat.recalculateStats(true);
			}
		}

		return oldSkill;
	}

	public Collection<Skill> getAllSkills()
	{
		return this._skills.values();
	}

	public void removeAllSkills()
	{
		if (this.isPlayer())
		{
			Player player = this.asPlayer();

			while (!this._skills.isEmpty())
			{
				player.removeSkill(this._skills.firstEntry().getValue());
			}
		}
		else
		{
			while (!this._skills.isEmpty())
			{
				this.removeSkill(this._skills.firstEntry().getValue(), true);
			}
		}
	}

	public Map<Integer, Skill> getSkills()
	{
		return this._skills;
	}

	public int getSkillLevel(int skillId)
	{
		Skill skill = this.getKnownSkill(skillId);
		return skill == null ? 0 : skill.getLevel();
	}

	public Skill getKnownSkill(int skillId)
	{
		return this._skills.get(skillId);
	}

	public int getBuffCount()
	{
		return this._effectList.getBuffCount();
	}

	public int getDanceCount()
	{
		return this._effectList.getDanceCount();
	}

	public void notifyQuestEventSkillFinished(Skill skill, WorldObject target)
	{
	}

	public double getLevelMod()
	{
		return this._transform != null && !this._transform.isStance() ? this._transform.getLevelMod(this) : (this.getLevel() + 89) / 100.0;
	}

	public byte getPvpFlag()
	{
		return 0;
	}

	public void updatePvPFlag(int value)
	{
	}

	public double getRandomDamageMultiplier()
	{
		int random = (int) this._stat.getValue(Stat.RANDOM_DAMAGE);
		return 1.0 + Rnd.get(-random, random) / 100.0;
	}

	public long getAttackEndTime()
	{
		return this._attackEndTime;
	}

	public long getRangedAttackEndTime()
	{
		return this._disableRangedAttackEndTime;
	}

	public abstract int getLevel();

	public int getAccuracy()
	{
		return this._stat.getAccuracy();
	}

	public int getMagicAccuracy()
	{
		return this._stat.getMagicAccuracy();
	}

	public int getMagicEvasionRate()
	{
		return this._stat.getMagicEvasionRate();
	}

	public double getAttackSpeedMultiplier()
	{
		return this._stat.getAttackSpeedMultiplier();
	}

	public double getCriticalDmg(int init)
	{
		return this._stat.getCriticalDmg(init);
	}

	public int getCriticalHit()
	{
		return this._stat.getCriticalHit();
	}

	public int getPSkillCriticalRate()
	{
		return this._stat.getPSkillCriticalRate();
	}

	public int getEvasionRate()
	{
		return this._stat.getEvasionRate();
	}

	public int getMagicalAttackRange(Skill skill)
	{
		return this._stat.getMagicalAttackRange(skill);
	}

	public int getMaxCp()
	{
		return this._stat.getMaxCp();
	}

	public int getMaxRecoverableCp()
	{
		return this._stat.getMaxRecoverableCp();
	}

	public int getMAtk()
	{
		return this._stat.getMAtk();
	}

	public int getWeaponBonusMAtk()
	{
		return this._stat.getWeaponBonusMAtk();
	}

	public int getMAtkSpd()
	{
		return this._stat.getMAtkSpd();
	}

	public int getMaxMp()
	{
		return this._stat.getMaxMp();
	}

	public int getMaxRecoverableMp()
	{
		return this._stat.getMaxRecoverableMp();
	}

	public long getMaxHp()
	{
		return this._stat.getMaxHp();
	}

	public long getMaxRecoverableHp()
	{
		return this._stat.getMaxRecoverableHp();
	}

	public int getMCriticalHit()
	{
		return this._stat.getMCriticalHit();
	}

	public int getMDef()
	{
		return this._stat.getMDef();
	}

	public int getPAtk()
	{
		return this._stat.getPAtk();
	}

	public int getWeaponBonusPAtk()
	{
		return this._stat.getWeaponBonusPAtk();
	}

	public int getPAtkSpd()
	{
		return this._stat.getPAtkSpd();
	}

	public int getPDef()
	{
		return this._stat.getPDef();
	}

	public int getPhysicalAttackRange()
	{
		return this._stat.getPhysicalAttackRange();
	}

	public double getMovementSpeedMultiplier()
	{
		return this._stat.getMovementSpeedMultiplier();
	}

	public double getRunSpeed()
	{
		return this._stat.getRunSpeed();
	}

	public double getWalkSpeed()
	{
		return this._stat.getWalkSpeed();
	}

	public double getSwimRunSpeed()
	{
		return this._stat.getSwimRunSpeed();
	}

	public double getSwimWalkSpeed()
	{
		return this._stat.getSwimWalkSpeed();
	}

	public double getMoveSpeed()
	{
		return this._stat.getMoveSpeed();
	}

	public int getShldDef()
	{
		return this._stat.getShldDef();
	}

	public int getSTR()
	{
		return this._stat.getSTR();
	}

	public int getDEX()
	{
		return this._stat.getDEX();
	}

	public int getCON()
	{
		return this._stat.getCON();
	}

	public int getINT()
	{
		return this._stat.getINT();
	}

	public int getWIT()
	{
		return this._stat.getWIT();
	}

	public int getMEN()
	{
		return this._stat.getMEN();
	}

	public void addStatusListener(Creature object)
	{
		this._status.addStatusListener(object);
	}

	public void doAttack(double damageValue, Creature target, Skill skill, boolean isDOT, boolean directlyToHp, boolean critical, boolean reflect)
	{
		if (!this.isFakePlayer() || FakePlayersConfig.FAKE_PLAYER_AGGRO_FPC || !target.isFakePlayer())
		{
			if (target.hasAI())
			{
				target.getAI().clientStartAutoAttack();
				target.getAI().notifyAction(Action.ATTACKED, this);
			}

			this.getAI().clientStartAutoAttack();
			double damage = damageValue;
			if (target.isImmobilized())
			{
				damage = damageValue * this._stat.getMul(Stat.IMMOBILE_DAMAGE_BONUS, 1.0);
				damage *= Math.max(0.22, target.getStat().getMul(Stat.IMMOBILE_DAMAGE_RESIST, 1.0));
			}

			if (!reflect && !isDOT)
			{
				if (this.isBehind(target))
				{
					damage *= this._stat.getMul(Stat.REAR_DAMAGE_RATE, 1.0);
				}

				if (!target.isDead() && skill != null)
				{
					Formulas.calcCounterAttack(this, target, skill, true);
					if (skill.isMagic() && target.getStat().getValue(Stat.VENGEANCE_SKILL_MAGIC_DAMAGE, 0.0) > Rnd.get(100))
					{
						this.reduceCurrentHp(damage, target, skill, isDOT, directlyToHp, critical, true);
						return;
					}
				}
			}

			boolean isPvP = this.isPlayable() && (target.isPlayable() || target.isFakePlayer());
			if ((!isPvP || PvpConfig.VAMPIRIC_ATTACK_AFFECTS_PVP) && (skill == null || PlayerConfig.VAMPIRIC_ATTACK_WORKS_WITH_SKILLS))
			{
				double absorbHpPercent = this.getStat().getValue(Stat.ABSORB_DAMAGE_PERCENT, 0.0);
				if (absorbHpPercent > 0.0 && Rnd.nextDouble() < this._stat.getValue(Stat.ABSORB_DAMAGE_CHANCE))
				{
					int absorbDamage = (int) Math.min(absorbHpPercent * damage, this._stat.getMaxRecoverableHp() - this._status.getCurrentHp());
					absorbDamage = Math.min(absorbDamage, (int) target.getCurrentHp());
					absorbDamage = (int) (absorbDamage * target.getStat().getValue(Stat.ABSORB_DAMAGE_DEFENCE, 1.0));
					if (absorbDamage > 0)
					{
						this.setCurrentHp(this._status.getCurrentHp() + absorbDamage);
					}
				}
			}

			if ((!isPvP || PvpConfig.MP_VAMPIRIC_ATTACK_AFFECTS_PVP) && (skill != null || PlayerConfig.MP_VAMPIRIC_ATTACK_WORKS_WITH_MELEE))
			{
				double absorbMpPercent = this._stat.getValue(Stat.ABSORB_MANA_DAMAGE_PERCENT, 0.0);
				if (absorbMpPercent > 0.0 && Rnd.nextDouble() < this._stat.getValue(Stat.ABSORB_MANA_DAMAGE_CHANCE))
				{
					int absorbDamage = (int) Math.min(absorbMpPercent * damage, this._stat.getMaxRecoverableMp() - this._status.getCurrentMp());
					absorbDamage = Math.min(absorbDamage, (int) target.getCurrentMp());
					if (absorbDamage > 0)
					{
						this.setCurrentMp(this._status.getCurrentMp() + absorbDamage);
					}
				}
			}

			target.reduceCurrentHp(damage, this, skill, isDOT, directlyToHp, critical, reflect);
			if (!reflect && !isDOT && !target.isDead() && !target.isHpBlocked())
			{
				int reflectedDamage = 0;
				double reflectPercent = Math.min(target.getStat().getValue(Stat.REFLECT_DAMAGE_PERCENT, 0.0) - this.getStat().getValue(Stat.REFLECT_DAMAGE_PERCENT_DEFENSE, 0.0), target.isPlayer() ? PlayerConfig.PLAYER_REFLECT_PERCENT_LIMIT : PlayerConfig.NON_PLAYER_REFLECT_PERCENT_LIMIT);
				if (reflectPercent > 0.0)
				{
					reflectedDamage = (int) (reflectPercent / 100.0 * damage);
					reflectedDamage = (int) Math.min(reflectedDamage, target.getMaxHp());
					if (skill != null && skill.isMagic())
					{
						reflectedDamage = (int) Math.min(reflectedDamage, target.getStat().getMDef() * 1.5);
					}
					else
					{
						reflectedDamage = Math.min(reflectedDamage, target.getStat().getPDef());
					}
				}

				if (reflectedDamage > 0)
				{
					target.doAttack(reflectedDamage, this, skill, isDOT, directlyToHp, critical, true);
				}
			}

			if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
			{
				target.breakAttack();
				target.breakCast();
			}
		}
	}

	public void reduceCurrentHp(double amount, Creature attacker, Skill skill)
	{
		this.reduceCurrentHp(amount, attacker, skill, false, false, false, false);
	}

	public void reduceCurrentHp(double amountValue, Creature attacker, Skill skill, boolean isDOT, boolean directlyToHp, boolean critical, boolean reflect)
	{
		double amount = amountValue;
		if (this.isPlayer() && this.asPlayer().isFakeDeath() && PlayerConfig.FAKE_DEATH_DAMAGE_STAND && amountValue > 0.0)
		{
			this.stopFakeDeath(true);
		}

		if (attacker != null && EventDispatcher.getInstance().hasListener(EventType.ON_CREATURE_DAMAGE_DEALT, attacker))
		{
			if (this._onCreatureDamageDealt == null)
			{
				this._onCreatureDamageDealt = new OnCreatureDamageDealt();
			}

			this._onCreatureDamageDealt.setAttacker(attacker);
			this._onCreatureDamageDealt.setTarget(this);
			this._onCreatureDamageDealt.setDamage(amountValue);
			this._onCreatureDamageDealt.setSkill(skill);
			this._onCreatureDamageDealt.setCritical(critical);
			this._onCreatureDamageDealt.setDamageOverTime(isDOT);
			this._onCreatureDamageDealt.setReflect(reflect);
			EventDispatcher.getInstance().notifyEvent(this._onCreatureDamageDealt, attacker);
		}

		if (EventDispatcher.getInstance().hasListener(EventType.ON_CREATURE_DAMAGE_RECEIVED, this))
		{
			if (this._onCreatureDamageReceived == null)
			{
				this._onCreatureDamageReceived = new OnCreatureDamageReceived();
			}

			this._onCreatureDamageReceived.setAttacker(attacker);
			this._onCreatureDamageReceived.setTarget(this);
			this._onCreatureDamageReceived.setDamage(amountValue);
			this._onCreatureDamageReceived.setSkill(skill);
			this._onCreatureDamageReceived.setCritical(critical);
			this._onCreatureDamageReceived.setDamageOverTime(isDOT);
			this._onCreatureDamageReceived.setReflect(reflect);
			DamageReturn term = EventDispatcher.getInstance().notifyEvent(this._onCreatureDamageReceived, this, DamageReturn.class);
			if (term != null)
			{
				if (term.terminate())
				{
					return;
				}

				if (term.override())
				{
					amount = term.getDamage();
				}
			}
		}

		double elementalDamage = 0.0;
		boolean elementalCrit = false;
		if (attacker != null)
		{
			if (attacker.isPlayable())
			{
				amount *= (100.0 + Math.max(this._stat.getValue(Stat.PVP_DAMAGE_TAKEN), -80.0)) / 100.0;
			}
			else
			{
				amount *= (100.0 + Math.max(this._stat.getValue(Stat.PVE_DAMAGE_TAKEN), -80.0)) / 100.0;
			}

			if (attacker.isRaid() || attacker.isRaidMinion())
			{
				amount *= (100.0 + Math.max(this._stat.getValue(Stat.PVE_DAMAGE_TAKEN_RAID), -80.0)) / 100.0;
			}
			else if (attacker.isMonster())
			{
				amount *= (100.0 + Math.max(this._stat.getValue(Stat.PVE_DAMAGE_TAKEN_MONSTER), -80.0)) / 100.0;
			}

			if (!reflect)
			{
				elementalCrit = Formulas.calcSpiritElementalCrit(attacker, this);
				elementalDamage = Formulas.calcSpiritElementalDamage(attacker, this, amount, elementalCrit);
				amount += elementalDamage;
			}
		}

		if (attacker != null && !attacker.isGM())
		{
			double damageCap = this._stat.getValue(Stat.DAMAGE_LIMIT);
			if (damageCap > 0.0)
			{
				amount = Math.min(amount, damageCap);
			}
		}

		if (ChampionMonstersConfig.CHAMPION_ENABLE && this.isChampion() && ChampionMonstersConfig.CHAMPION_HP != 0)
		{
			this._status.reduceHp(amount / ChampionMonstersConfig.CHAMPION_HP, attacker, skill == null || !skill.isToggle(), isDOT, false);
		}
		else if (this.isPlayer())
		{
			Player player = this.asPlayer();
			player.addDamageTaken(attacker, skill != null ? skill.getDisplayId() : 0, amount, isDOT, reflect);
			if (!isDOT && skill != null && skill.getCastRange() > 0 && attacker != null && !GeoEngine.getInstance().canSeeTarget(attacker, this))
			{
				amount = 0.0;
			}

			player.getStatus().reduceHp(amount, attacker, skill, skill == null || !skill.isToggle(), isDOT, false, directlyToHp);
		}
		else
		{
			this._status.reduceHp(amount, attacker, skill == null || !skill.isToggle(), isDOT, false);
		}

		if (attacker != null)
		{
			attacker.sendDamageMessage(this, skill, (int) amount, elementalDamage, critical, false, elementalCrit);
		}

		if (this.isMonster() && attacker instanceof Playable)
		{
			Player attackerPlayer = attacker.asPlayer();
			ElementalSpirit[] playerSpirits = attackerPlayer.getSpirits();
			if (playerSpirits != null)
			{
				ElementalSpiritType monsterElementalSpiritType = this.getElementalSpiritType();
				if (monsterElementalSpiritType != ElementalSpiritType.NONE && attackerPlayer.getActiveElementalSpiritType() != monsterElementalSpiritType.getId())
				{
					attackerPlayer.changeElementalSpirit(ElementalSpiritType.superior(monsterElementalSpiritType).getId());
				}
			}
		}
	}

	public void reduceCurrentMp(double amount)
	{
		this._status.reduceMp(amount);
	}

	@Override
	public void removeStatusListener(Creature object)
	{
		this._status.removeStatusListener(object);
	}

	protected void stopHpMpRegeneration()
	{
		this._status.stopHpMpRegeneration();
	}

	public double getCurrentCp()
	{
		return this._status.getCurrentCp();
	}

	public int getCurrentCpPercent()
	{
		return (int) (this._status.getCurrentCp() * 100.0 / this._stat.getMaxCp());
	}

	public void setCurrentCp(double newCp)
	{
		this._status.setCurrentCp(newCp);
	}

	public void setCurrentCp(double newCp, boolean broadcast)
	{
		this._status.setCurrentCp(newCp, broadcast);
	}

	public double getCurrentHp()
	{
		return this._status.getCurrentHp();
	}

	public int getCurrentHpPercent()
	{
		return (int) (this._status.getCurrentHp() * 100.0 / this._stat.getMaxHp());
	}

	public void setCurrentHp(double newHp)
	{
		this._status.setCurrentHp(newHp);
	}

	public void setCurrentHp(double newHp, boolean broadcast)
	{
		this._status.setCurrentHp(newHp, broadcast);
	}

	public void setCurrentHpMp(double newHp, double newMp)
	{
		this._status.setCurrentHpMp(newHp, newMp);
	}

	public double getCurrentMp()
	{
		return this._status.getCurrentMp();
	}

	public int getCurrentMpPercent()
	{
		return (int) (this._status.getCurrentMp() * 100.0 / this._stat.getMaxMp());
	}

	public void setCurrentMp(double newMp)
	{
		this._status.setCurrentMp(newMp);
	}

	public void setCurrentMp(double newMp, boolean broadcast)
	{
		this._status.setCurrentMp(newMp, false);
	}

	public void fullRestore()
	{
		this.setCurrentHp(this.getMaxHp(), false);
		ThreadPool.schedule(() -> {
			this.setCurrentHp(this.getMaxHp());
			this.setCurrentMp(this.getMaxMp(), this.isPlayable());
		}, 100L);
	}

	public int getMaxLoad()
	{
		if (!this.isPlayer() && !this.isPet())
		{
			return 0;
		}
		double baseLoad = Math.floor(BaseStat.CON.calcBonus(this) * 69000.0 * PlayerConfig.ALT_WEIGHT_LIMIT);
		return (int) this._stat.getValue(Stat.WEIGHT_LIMIT, baseLoad);
	}

	public int getBonusWeightPenalty()
	{
		return !this.isPlayer() && !this.isPet() ? 0 : (int) this._stat.getValue(Stat.WEIGHT_PENALTY, 1.0);
	}

	public int getCurrentLoad()
	{
		return !this.isPlayer() && !this.isPet() ? 0 : this.getInventory().getTotalWeight();
	}

	public boolean isChampion()
	{
		return false;
	}

	public void sendDamageMessage(Creature target, Skill skill, int damage, double elementalDamage, boolean crit, boolean miss, boolean elementalCrit)
	{
	}

	public AttributeType getAttackElement()
	{
		return this._stat.getAttackElement();
	}

	public int getAttackElementValue(AttributeType attackAttribute)
	{
		return this._stat.getAttackElementValue(attackAttribute);
	}

	public int getDefenseElementValue(AttributeType defenseAttribute)
	{
		return this._stat.getDefenseElementValue(defenseAttribute);
	}

	public void disableCoreAI(boolean value)
	{
		this._disabledAI = value;
	}

	public boolean isCoreAIDisabled()
	{
		return this._disabledAI;
	}

	public boolean giveRaidCurse()
	{
		return false;
	}

	public boolean isAffected(EffectFlag flag)
	{
		return this._effectList.isAffected(flag);
	}

	public Team getTeam()
	{
		return this._team;
	}

	public void setTeam(Team team)
	{
		this._team = team;
	}

	public void setLethalable(boolean value)
	{
		this._lethalable = value;
	}

	public boolean isLethalable()
	{
		return this._lethalable;
	}

	public boolean hasTriggerSkills()
	{
		return !this._triggerSkills.isEmpty();
	}

	public Map<Integer, OptionSkillHolder> getTriggerSkills()
	{
		return this._triggerSkills;
	}

	public void addTriggerSkill(OptionSkillHolder holder)
	{
		this.getTriggerSkills().put(holder.getSkill().getId(), holder);
	}

	public void removeTriggerSkill(OptionSkillHolder holder)
	{
		this.getTriggerSkills().remove(holder.getSkill().getId());
	}

	public boolean canRevive()
	{
		return true;
	}

	public void setCanRevive(boolean value)
	{
	}

	public boolean isSweepActive()
	{
		return false;
	}

	public int getClanId()
	{
		return 0;
	}

	public Clan getClan()
	{
		return null;
	}

	public boolean isAcademyMember()
	{
		return false;
	}

	public int getPledgeType()
	{
		return 0;
	}

	public int getAllyId()
	{
		return 0;
	}

	public void notifyAttackAvoid(Creature target, boolean isDot)
	{
		if (EventDispatcher.getInstance().hasListener(EventType.ON_CREATURE_ATTACK_AVOID, target))
		{
			if (this._onCreatureAttackAvoid == null)
			{
				this._onCreatureAttackAvoid = new OnCreatureAttackAvoid();
			}

			this._onCreatureAttackAvoid.setAttacker(this);
			this._onCreatureAttackAvoid.setTarget(target);
			this._onCreatureAttackAvoid.setDamageOverTime(isDot);
			EventDispatcher.getInstance().notifyEvent(this._onCreatureAttackAvoid, target);
		}
	}

	public WeaponType getAttackType()
	{
		Weapon weapon = this.getActiveWeaponItem();
		if (weapon != null)
		{
			return weapon.getItemType();
		}
		WeaponType defaultWeaponType = this._template.getBaseAttackType();
		return this._transform == null ? defaultWeaponType : this._transform.getBaseAttackType(this, defaultWeaponType);
	}

	public boolean isInCategory(CategoryType type)
	{
		return CategoryData.getInstance().isInCategory(type, this.getId());
	}

	public boolean isInOneOfCategory(CategoryType... types)
	{
		for (CategoryType type : types)
		{
			if (CategoryData.getInstance().isInCategory(type, this.getId()))
			{
				return true;
			}
		}

		return false;
	}

	public Creature getSummoner()
	{
		return this._summoner;
	}

	public void setSummoner(Creature summoner)
	{
		this._summoner = summoner;
	}

	public void addSummonedNpc(Npc npc)
	{
		if (this._summonedNpcs == null)
		{
			synchronized (this)
			{
				if (this._summonedNpcs == null)
				{
					this._summonedNpcs = new ConcurrentHashMap<>();
				}
			}
		}

		this._summonedNpcs.put(npc.getObjectId(), npc);
		npc.setSummoner(this);
	}

	public void removeSummonedNpc(int objectId)
	{
		if (this._summonedNpcs != null)
		{
			this._summonedNpcs.remove(objectId);
		}
	}

	@SuppressWarnings("unchecked")
	public Collection<Npc> getSummonedNpcs()
	{
		return (Collection<Npc>) (this._summonedNpcs != null ? this._summonedNpcs.values() : Collections.emptyList());
	}

	public Npc getSummonedNpc(int objectId)
	{
		return this._summonedNpcs != null ? this._summonedNpcs.get(objectId) : null;
	}

	public int getSummonedNpcCount()
	{
		return this._summonedNpcs != null ? this._summonedNpcs.size() : 0;
	}

	public void resetSummonedNpcs()
	{
		if (this._summonedNpcs != null)
		{
			this._summonedNpcs.clear();
		}
	}

	public Creature getRecallCreature()
	{
		return this._recallCreature;
	}

	public void setRecallCreature(Creature recallCreature)
	{
		this._recallCreature = recallCreature;
	}

	@Override
	public boolean isCreature()
	{
		return true;
	}

	@Override
	public Creature asCreature()
	{
		return this;
	}

	public int getMinShopDistance()
	{
		return 0;
	}

	public Collection<SkillCaster> getSkillCasters()
	{
		return this._skillCasters.values();
	}

	public SkillCaster addSkillCaster(SkillCastingType castingType, SkillCaster skillCaster)
	{
		return this._skillCasters.put(castingType, skillCaster);
	}

	public SkillCaster removeSkillCaster(SkillCastingType castingType)
	{
		return this._skillCasters.remove(castingType);
	}

	@SafeVarargs
	public final List<SkillCaster> getSkillCasters(Predicate<SkillCaster> filterValue, Predicate<SkillCaster>... filters)
	{
		Predicate<SkillCaster> filter = filterValue;

		for (Predicate<SkillCaster> additionalFilter : filters)
		{
			filter = filter.and(additionalFilter);
		}

		List<SkillCaster> result = new ArrayList<>();

		for (SkillCaster skillCaster : this._skillCasters.values())
		{
			if (filter.test(skillCaster))
			{
				result.add(skillCaster);
			}
		}

		return result;
	}

	@SafeVarargs
	public final SkillCaster getSkillCaster(Predicate<SkillCaster> filterValue, Predicate<SkillCaster>... filters)
	{
		Predicate<SkillCaster> filter = filterValue;

		for (Predicate<SkillCaster> additionalFilter : filters)
		{
			filter = filter.and(additionalFilter);
		}

		for (SkillCaster skillCaster : this._skillCasters.values())
		{
			if (filter.test(skillCaster))
			{
				return skillCaster;
			}
		}

		return null;
	}

	public boolean isChanneling()
	{
		return this._channelizer != null && this._channelizer.isChanneling();
	}

	public SkillChannelizer getSkillChannelizer()
	{
		if (this._channelizer == null)
		{
			this._channelizer = new SkillChannelizer(this);
		}

		return this._channelizer;
	}

	public boolean isChannelized()
	{
		return this._channelized != null && !this._channelized.isChannelized();
	}

	public SkillChannelized getSkillChannelized()
	{
		if (this._channelized == null)
		{
			this._channelized = new SkillChannelized();
		}

		return this._channelized;
	}

	public void addIgnoreSkillEffects(SkillHolder holder)
	{
		IgnoreSkillHolder ignoreSkillHolder = this.getIgnoreSkillEffects().get(holder.getSkillId());
		if (ignoreSkillHolder != null)
		{
			ignoreSkillHolder.increaseInstances();
		}
		else
		{
			this.getIgnoreSkillEffects().put(holder.getSkillId(), new IgnoreSkillHolder(holder));
		}
	}

	public void removeIgnoreSkillEffects(SkillHolder holder)
	{
		IgnoreSkillHolder ignoreSkillHolder = this.getIgnoreSkillEffects().get(holder.getSkillId());
		if (ignoreSkillHolder != null && ignoreSkillHolder.decreaseInstances() < 1)
		{
			this.getIgnoreSkillEffects().remove(holder.getSkillId());
		}
	}

	public boolean isIgnoringSkillEffects(int skillId, int skillLevel)
	{
		if (this._ignoreSkillEffects.isEmpty())
		{
			return false;
		}
		SkillHolder holder = this.getIgnoreSkillEffects().get(skillId);
		return holder != null && (holder.getSkillLevel() < 1 || holder.getSkillLevel() == skillLevel);
	}

	private Map<Integer, IgnoreSkillHolder> getIgnoreSkillEffects()
	{
		return this._ignoreSkillEffects;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<AbstractEventListener> getListeners(EventType type)
	{
		Collection<AbstractEventListener> objectListeners = super.getListeners(type);
		Collection<AbstractEventListener> templateListeners = this._template.getListeners(type);
		Collection<AbstractEventListener> globalListeners = (Collection<AbstractEventListener>) (this.isMonster() ? Containers.Monsters().getListeners(type) : (this.isNpc() ? Containers.Npcs().getListeners(type) : (this.isPlayer() ? Containers.Players().getListeners(type) : Collections.emptyList())));
		if (objectListeners.isEmpty() && templateListeners.isEmpty() && globalListeners.isEmpty())
		{
			return Collections.emptyList();
		}
		else if (!objectListeners.isEmpty() && templateListeners.isEmpty() && globalListeners.isEmpty())
		{
			return objectListeners;
		}
		else if (!templateListeners.isEmpty() && objectListeners.isEmpty() && globalListeners.isEmpty())
		{
			return templateListeners;
		}
		else if (!globalListeners.isEmpty() && objectListeners.isEmpty() && templateListeners.isEmpty())
		{
			return globalListeners;
		}
		else
		{
			Collection<AbstractEventListener> allListeners = new ArrayList<>(objectListeners.size() + templateListeners.size() + globalListeners.size());
			allListeners.addAll(objectListeners);
			allListeners.addAll(templateListeners);
			allListeners.addAll(globalListeners);
			return allListeners;
		}
	}

	public Race getRace()
	{
		return this._template.getRace();
	}

	@Override
	public void setXYZ(int newX, int newY, int newZ)
	{
		if (newX != 0 || newY != 0)
		{
			ZoneRegion oldZoneRegion = ZoneManager.getInstance().getRegion(this);
			ZoneRegion newZoneRegion = ZoneManager.getInstance().getRegion(newX, newY);
			if (newZoneRegion != null)
			{
				if (oldZoneRegion != newZoneRegion)
				{
					oldZoneRegion.removeFromZones(this);
					newZoneRegion.revalidateZones(this);
				}

				super.setXYZ(newX, newY, newZ);
			}
		}
	}

	public Map<Integer, RelationCache> getKnownRelations()
	{
		return this._knownRelations;
	}

	@Override
	public boolean isTargetable()
	{
		return super.isTargetable() && !this.isAffected(EffectFlag.UNTARGETABLE);
	}

	public boolean isTargetingDisabled()
	{
		return this.isAffected(EffectFlag.TARGETING_DISABLED);
	}

	public boolean cannotEscape()
	{
		return this.isAffected(EffectFlag.CANNOT_ESCAPE);
	}

	public void setAbnormalShieldBlocks(int times)
	{
		this._abnormalShieldBlocks.set(times);
	}

	public int getAbnormalShieldBlocks()
	{
		return this._abnormalShieldBlocks.get();
	}

	public int decrementAbnormalShieldBlocks()
	{
		return this._abnormalShieldBlocks.decrementAndGet();
	}

	public void addMultipliedAbnormalTime(int skillId, double time)
	{
		double oldTime = this._multipliedAbnormalTimes.getOrDefault(skillId, 0.0);
		if (oldTime != 0.0)
		{
			this._multipliedAbnormalTimes.put(skillId, oldTime * time);
		}
		else
		{
			this._multipliedAbnormalTimes.put(skillId, time);
		}
	}

	public void addAddedAbnormalTime(int skillId, int time)
	{
		int oldTime = this._addedAbnormalTimes.getOrDefault(skillId, 0);
		if (oldTime != 0)
		{
			this._addedAbnormalTimes.put(skillId, oldTime + time);
		}
		else
		{
			this._addedAbnormalTimes.put(skillId, time);
		}
	}

	public void removeMultipliedAbnormalTime(int skillId, double time)
	{
		double oldTime = this._multipliedAbnormalTimes.getOrDefault(skillId, 0.0);
		if (oldTime == time)
		{
			this._multipliedAbnormalTimes.remove(skillId);
		}
		else
		{
			this._multipliedAbnormalTimes.put(skillId, oldTime / time);
		}
	}

	public void removeAddedAbnormalTime(int skillId, int time)
	{
		int oldTime = this._addedAbnormalTimes.getOrDefault(skillId, 0);
		if (oldTime == time)
		{
			this._addedAbnormalTimes.remove(skillId);
		}
		else
		{
			this._addedAbnormalTimes.put(skillId, oldTime - time);
		}
	}

	public double getMultipliedAbnormalTime(int skillId)
	{
		return this._multipliedAbnormalTimes.getOrDefault(skillId, 0.0);
	}

	public int getAddedAbnormalTime(int skillId)
	{
		return this._addedAbnormalTimes.getOrDefault(skillId, 0);
	}

	public boolean hasAbnormalType(AbnormalType abnormalType)
	{
		return this._effectList.hasAbnormalType(abnormalType);
	}

	public void addBlockActionsAllowedSkill(Integer skillId)
	{
		this._blockActionsAllowedSkills.computeIfAbsent(skillId, _ -> new AtomicInteger()).incrementAndGet();
	}

	public void removeBlockActionsAllowedSkill(Integer skillId)
	{
		this._blockActionsAllowedSkills.computeIfPresent(skillId, (_, v) -> (v.decrementAndGet() != 0 ? v : null));
	}

	public boolean isBlockedActionsAllowedSkill(Skill skill)
	{
		return this._blockActionsAllowedSkills.containsKey(skill.getId());
	}

	protected void initSeenCreatures()
	{
		if (this._seenCreatures == null)
		{
			synchronized (this)
			{
				if (this._seenCreatures == null)
				{
					if (this.isNpc())
					{
						NpcTemplate template = this.asNpc().getTemplate();
						if (template != null && template.getAggroRange() > 0)
						{
							this._seenCreatureRange = template.getAggroRange();
						}
					}

					this._seenCreatures = ConcurrentHashMap.newKeySet(1);
				}
			}
		}

		CreatureSeeTaskManager.getInstance().add(this);
	}

	public void updateSeenCreatures()
	{
		if (this._seenCreatures != null && !this._isDead && this.isSpawned())
		{
			WorldRegion region = this.getWorldRegion();
			if (region != null && region.areNeighborsActive())
			{
				World.getInstance().forEachVisibleObjectInRange(this, Creature.class, this._seenCreatureRange, creature -> {
					if (!creature.isInvisible() && this._seenCreatures.add(creature) && EventDispatcher.getInstance().hasListener(EventType.ON_CREATURE_SEE, this))
					{
						EventDispatcher.getInstance().notifyEventAsync(new OnCreatureSee(this, creature), this);
					}
				});
			}
		}
	}

	public void removeSeenCreature(WorldObject worldObject)
	{
		if (this._seenCreatures != null)
		{
			this._seenCreatures.remove(worldObject);
		}
	}

	public MoveType getMoveType()
	{
		if (this.isMoving() && this._isRunning)
		{
			return MoveType.RUNNING;
		}
		return this.isMoving() && !this._isRunning ? MoveType.WALKING : MoveType.STANDING;
	}

	protected void computeStatusUpdate(StatusUpdate su, StatusUpdateType type)
	{
		long newValue = type.getValue(this);
		this._statusUpdates.compute(type, (_, oldValue) -> {
			if (oldValue != null && oldValue == newValue)
			{
				return oldValue;
			}
			su.addUpdate(type, newValue);
			if (this.isPlayer())
			{
				if (type == StatusUpdateType.MAX_DP)
				{
					su.addUpdate(StatusUpdateType.CUR_DP, this.asPlayer().getDeathPoints());
				}
				else if (type == StatusUpdateType.MAX_BP)
				{
					su.addUpdate(StatusUpdateType.CUR_BP, this.asPlayer().getBeastPoints());
				}
				else if (type == StatusUpdateType.MAX_AP)
				{
					Player player = this.asPlayer();
					player.sendPacket(new ExMax());
					su.addUpdate(StatusUpdateType.CUR_AP, player.getAssassinationPoints());
				}
			}

			return newValue;
		});
	}

	protected void addStatusUpdateValue(StatusUpdateType type)
	{
		this._statusUpdates.put(type, type.getValue(this));
	}

	protected void initStatusUpdateCache()
	{
		this.addStatusUpdateValue(StatusUpdateType.MAX_HP);
		this.addStatusUpdateValue(StatusUpdateType.MAX_MP);
		this.addStatusUpdateValue(StatusUpdateType.CUR_HP);
		this.addStatusUpdateValue(StatusUpdateType.CUR_MP);
	}

	public boolean hasBasicPropertyResist()
	{
		return true;
	}

	public BasicPropertyResist getBasicPropertyResist(BasicProperty basicProperty)
	{
		return this._basicPropertyResists.computeIfAbsent(basicProperty, _ -> new BasicPropertyResist());
	}

	public int getReputation()
	{
		return this._reputation;
	}

	public void setReputation(int reputation)
	{
		this._reputation = reputation;
	}

	public boolean isChargedShot(ShotType type)
	{
		return this._chargedShots.contains(type);
	}

	public boolean chargeShot(ShotType type)
	{
		return this._chargedShots.add(type);
	}

	public boolean unchargeShot(ShotType type)
	{
		return this._chargedShots.remove(type);
	}

	public void unchargeAllShots()
	{
		this._chargedShots = EnumSet.noneOf(ShotType.class);
	}

	public void rechargeShots(boolean physical, boolean magic, boolean fish)
	{
	}

	public void setCursorKeyMovement(boolean value)
	{
		this._cursorKeyMovement = value;
	}

	public List<Item> getFakePlayerDrops()
	{
		return this._fakePlayerDrops;
	}

	public void addBuffInfoTime(BuffInfo info)
	{
		this._buffFinishTask.addBuffInfo(info);
	}

	public void removeBuffInfoTime(BuffInfo info)
	{
		this._buffFinishTask.removeBuffInfo(info);
	}

	public double getElementalSpiritDefenseOf(ElementalSpiritType type)
	{
		return this.getElementalSpiritType() == type ? 100.0 : 0.0;
	}

	public double getElementalSpiritAttackOf(ElementalSpiritType type)
	{
		return this.getElementalSpiritType() == type ? 100.0 : 0.0;
	}

	public ElementalSpiritType getElementalSpiritType()
	{
		return ElementalSpiritType.NONE;
	}

	public static class MoveData
	{
		public int moveStartTime;
		public int moveTimestamp;
		public int xDestination;
		public int yDestination;
		public int zDestination;
		public double xAccurate;
		public double yAccurate;
		public double zAccurate;
		public int heading;
		public boolean disregardingGeodata;
		public int onGeodataPathIndex;
		public List<GeoLocation> geoPath;
		public int geoPathAccurateTx;
		public int geoPathAccurateTy;
		public int geoPathGtx;
		public int geoPathGty;
		public int lastBroadcastTime;
	}
}
