package net.sf.l2jdev.gameserver.model.script;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.commons.util.TraceUtil;
import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.cache.HtmCache;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.config.RatesConfig;
import net.sf.l2jdev.gameserver.config.custom.ChampionMonstersConfig;
import net.sf.l2jdev.gameserver.config.custom.PremiumSystemConfig;
import net.sf.l2jdev.gameserver.config.custom.RandomSpawnsConfig;
import net.sf.l2jdev.gameserver.data.enums.CategoryType;
import net.sf.l2jdev.gameserver.data.xml.DoorData;
import net.sf.l2jdev.gameserver.data.xml.ExperienceData;
import net.sf.l2jdev.gameserver.data.xml.ItemData;
import net.sf.l2jdev.gameserver.data.xml.NewQuestData;
import net.sf.l2jdev.gameserver.data.xml.NpcData;
import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.managers.FortManager;
import net.sf.l2jdev.gameserver.managers.InstanceManager;
import net.sf.l2jdev.gameserver.managers.ItemCommissionManager;
import net.sf.l2jdev.gameserver.managers.MailManager;
import net.sf.l2jdev.gameserver.managers.PcCafePointsManager;
import net.sf.l2jdev.gameserver.managers.ScriptManager;
import net.sf.l2jdev.gameserver.managers.ZoneManager;
import net.sf.l2jdev.gameserver.model.KeyValuePair;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.Message;
import net.sf.l2jdev.gameserver.model.Spawn;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Attackable;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Playable;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.AttributeType;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Race;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.TrapAction;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PlayerClass;
import net.sf.l2jdev.gameserver.model.actor.holders.npc.MinionList;
import net.sf.l2jdev.gameserver.model.actor.holders.player.MovieHolder;
import net.sf.l2jdev.gameserver.model.actor.instance.Door;
import net.sf.l2jdev.gameserver.model.actor.instance.Monster;
import net.sf.l2jdev.gameserver.model.actor.instance.Trap;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.effects.EffectFlag;
import net.sf.l2jdev.gameserver.model.events.Containers;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.ListenerRegisterType;
import net.sf.l2jdev.gameserver.model.events.ListenersContainer;
import net.sf.l2jdev.gameserver.model.events.annotations.Id;
import net.sf.l2jdev.gameserver.model.events.annotations.Ids;
import net.sf.l2jdev.gameserver.model.events.annotations.NpcLevelRange;
import net.sf.l2jdev.gameserver.model.events.annotations.NpcLevelRanges;
import net.sf.l2jdev.gameserver.model.events.annotations.Priority;
import net.sf.l2jdev.gameserver.model.events.annotations.Range;
import net.sf.l2jdev.gameserver.model.events.annotations.Ranges;
import net.sf.l2jdev.gameserver.model.events.annotations.RegisterEvent;
import net.sf.l2jdev.gameserver.model.events.annotations.RegisterType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureAttacked;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureDeath;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureSee;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureZoneEnter;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureZoneExit;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnAttackableAggroRangeEnter;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnAttackableAttack;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnAttackableFactionCall;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnAttackableHate;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnAttackableKill;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcCanBeSeen;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcDespawn;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcEventReceived;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcFirstTalk;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcMoveFinished;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcMoveRouteFinished;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcSkillFinished;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcSkillSee;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcSpawn;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcTeleport;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerLogin;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerLogout;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerProfessionCancel;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerProfessionChange;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerSkillLearn;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerSummonSpawn;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerSummonTalk;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnTrapAction;
import net.sf.l2jdev.gameserver.model.events.holders.instance.OnInstanceCreated;
import net.sf.l2jdev.gameserver.model.events.holders.instance.OnInstanceDestroy;
import net.sf.l2jdev.gameserver.model.events.holders.instance.OnInstanceEnter;
import net.sf.l2jdev.gameserver.model.events.holders.instance.OnInstanceLeave;
import net.sf.l2jdev.gameserver.model.events.holders.instance.OnInstanceStatusChange;
import net.sf.l2jdev.gameserver.model.events.holders.item.OnItemBypassEvent;
import net.sf.l2jdev.gameserver.model.events.holders.item.OnItemTalk;
import net.sf.l2jdev.gameserver.model.events.holders.olympiad.OnOlympiadMatchResult;
import net.sf.l2jdev.gameserver.model.events.holders.sieges.OnCastleSiegeFinish;
import net.sf.l2jdev.gameserver.model.events.holders.sieges.OnCastleSiegeOwnerChange;
import net.sf.l2jdev.gameserver.model.events.holders.sieges.OnCastleSiegeStart;
import net.sf.l2jdev.gameserver.model.events.listeners.AbstractEventListener;
import net.sf.l2jdev.gameserver.model.events.listeners.AnnotationEventListener;
import net.sf.l2jdev.gameserver.model.events.listeners.ConsumerEventListener;
import net.sf.l2jdev.gameserver.model.events.listeners.DummyEventListener;
import net.sf.l2jdev.gameserver.model.events.listeners.FunctionEventListener;
import net.sf.l2jdev.gameserver.model.events.listeners.RunnableEventListener;
import net.sf.l2jdev.gameserver.model.events.returns.AbstractEventReturn;
import net.sf.l2jdev.gameserver.model.events.returns.TerminateReturn;
import net.sf.l2jdev.gameserver.model.groups.Party;
import net.sf.l2jdev.gameserver.model.instancezone.Instance;
import net.sf.l2jdev.gameserver.model.instancezone.InstanceTemplate;
import net.sf.l2jdev.gameserver.model.interfaces.IPositionable;
import net.sf.l2jdev.gameserver.model.item.EtcItem;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.enchant.attribute.AttributeHolder;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.Mail;
import net.sf.l2jdev.gameserver.model.itemcontainer.PetInventory;
import net.sf.l2jdev.gameserver.model.itemcontainer.PlayerInventory;
import net.sf.l2jdev.gameserver.model.itemcontainer.PlayerWarehouse;
import net.sf.l2jdev.gameserver.model.olympiad.Olympiad;
import net.sf.l2jdev.gameserver.model.olympiad.Participant;
import net.sf.l2jdev.gameserver.model.script.newquestdata.NewQuest;
import net.sf.l2jdev.gameserver.model.script.newquestdata.NewQuestCondition;
import net.sf.l2jdev.gameserver.model.script.newquestdata.NewQuestReward;
import net.sf.l2jdev.gameserver.model.script.timers.IEventTimerCancel;
import net.sf.l2jdev.gameserver.model.script.timers.IEventTimerEvent;
import net.sf.l2jdev.gameserver.model.script.timers.TimerExecutor;
import net.sf.l2jdev.gameserver.model.script.timers.TimerHolder;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.model.siege.Fort;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.SkillCaster;
import net.sf.l2jdev.gameserver.model.skill.enums.AcquireSkillType;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;
import net.sf.l2jdev.gameserver.model.spawns.SpawnGroup;
import net.sf.l2jdev.gameserver.model.spawns.SpawnTemplate;
import net.sf.l2jdev.gameserver.model.stats.Stat;
import net.sf.l2jdev.gameserver.model.zone.ZoneType;
import net.sf.l2jdev.gameserver.network.NpcStringId;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.Movie;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.ExAdenaInvenCount;
import net.sf.l2jdev.gameserver.network.serverpackets.ExQuestNpcLogList;
import net.sf.l2jdev.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.ExUserInfoInvenWeight;
import net.sf.l2jdev.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.NpcQuestHtmlMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.PlaySound;
import net.sf.l2jdev.gameserver.network.serverpackets.SpecialCamera;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.quest.ExQuestDialog;
import net.sf.l2jdev.gameserver.scripting.ScriptEngine;
import net.sf.l2jdev.gameserver.taskmanagers.GameTimeTaskManager;
import net.sf.l2jdev.gameserver.util.ArrayUtil;
import net.sf.l2jdev.gameserver.util.LocationUtil;

public class Quest implements IEventTimerEvent<String>, IEventTimerCancel<String>
{
	public static final Logger LOGGER = Logger.getLogger(Quest.class.getName());
	public static final String DEFAULT_NO_QUEST_MSG = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>";
	public static final String QUEST_DELETE_FROM_CHAR_QUERY = "DELETE FROM character_quests WHERE charId=? AND name=?";
	public static final String QUEST_DELETE_FROM_CHAR_QUERY_NON_REPEATABLE_QUERY = "DELETE FROM character_quests WHERE charId=? AND name=? AND var!=?";
	private static final SkillHolder[] STORY_QUEST_BUFFS = new SkillHolder[]
	{
		new SkillHolder(1068, 1),
		new SkillHolder(1040, 1),
		new SkillHolder(1204, 1),
		new SkillHolder(1086, 1),
		new SkillHolder(1085, 1)
	};
	private final Map<ListenerRegisterType, Set<Integer>> _registeredIds = new ConcurrentHashMap<>();
	private final Queue<AbstractEventListener> _listeners = new PriorityBlockingQueue<>();
	private final Set<QuestCondition> _startCondition = ConcurrentHashMap.newKeySet(1);
	private final Map<String, List<QuestTimer>> _questTimers = new HashMap<>();
	private TimerExecutor<String> _timerExecutor;
	private final int _questId;
	private final Path _scriptFile;
	private boolean _isCustom = false;
	private NpcStringId _questNameNpcStringId;
	private int[] _questItemIds = null;
	private final NewQuest _questData;

	public Quest(int questId)
	{
		this._scriptFile = Path.of(ScriptEngine.getInstance().getCurrentLoadingScript().toUri());
		this.initializeAnnotationListeners();
		this._questId = questId;
		if (questId > 0)
		{
			ScriptManager.getInstance().addQuest(this);
		}
		else
		{
			ScriptManager.getInstance().addScript(this);
		}

		this._questData = NewQuestData.getInstance().getQuestById(questId);
		if (this._questData != null)
		{
			this.addNewQuestConditions(this._questData.getConditions(), null);
			if (this._questData.getQuestType() == 1)
			{
				if (this._questData.getStartNpcId() > 0)
				{
					this.addFirstTalkId(this._questData.getStartNpcId());
				}

				if (this._questData.getEndNpcId() > 0 && this._questData.getEndNpcId() != this._questData.getStartNpcId())
				{
					this.addFirstTalkId(this._questData.getEndNpcId());
				}
			}
			else if (this._questData.getQuestType() == 4 && this._questData.getStartItemId() > 0)
			{
				this.addItemTalkId(this._questData.getStartItemId());
			}

			if (this._questData.getGoal().getItemId() > 0)
			{
				this.registerQuestItems(this._questData.getGoal().getItemId());
			}
		}

		this.onLoad();
	}

	@Override
	public void onTimerEvent(TimerHolder<String> holder)
	{
		this.onTimerEvent(holder.getEvent(), holder.getParams(), holder.getNpc(), holder.getPlayer());
	}

	@Override
	public void onTimerCancel(TimerHolder<String> holder)
	{
		this.onTimerCancel(holder.getEvent(), holder.getParams(), holder.getNpc(), holder.getPlayer());
	}

	public void onTimerEvent(String event, StatSet params, Npc npc, Player player)
	{
		LOGGER.warning("[" + this.getClass().getSimpleName() + "]: Timer event arrived at non overriden onTimerEvent method event: " + event + " npc: " + npc + " player: " + player);
	}

	public void onTimerCancel(String event, StatSet params, Npc npc, Player player)
	{
	}

	public TimerExecutor<String> getTimers()
	{
		if (this._timerExecutor == null)
		{
			synchronized (this)
			{
				if (this._timerExecutor == null)
				{
					this._timerExecutor = new TimerExecutor<>(this, this);
				}
			}
		}

		return this._timerExecutor;
	}

	public boolean hasTimers()
	{
		return this._timerExecutor != null;
	}

	public int getId()
	{
		return this._questId;
	}

	public String getName()
	{
		return this.getClass().getSimpleName();
	}

	public Path getScriptFile()
	{
		return this._scriptFile;
	}

	public String getPath()
	{
		String path = this.getClass().getName().replace('.', '/');
		return path.substring(0, path.lastIndexOf("/" + this.getClass().getSimpleName()));
	}

	public boolean isCustomQuest()
	{
		return this._isCustom;
	}

	public void setCustom(boolean value)
	{
		this._isCustom = value;
	}

	public int getNpcStringId()
	{
		return this._questNameNpcStringId != null ? this._questNameNpcStringId.getId() / 100 : (this._questId > 10000 ? this._questId - 5000 : this._questId);
	}

	public NpcStringId getQuestNameNpcStringId()
	{
		return this._questNameNpcStringId;
	}

	public void setQuestNameNpcStringId(NpcStringId npcStringId)
	{
		this._questNameNpcStringId = npcStringId;
	}

	public void reload()
	{
		this.unload();

		try
		{
			ScriptEngine.getInstance().executeScript(this._scriptFile);
		}
		catch (Exception var2)
		{
			LOGGER.log(Level.WARNING, "Failed to reload script!", var2);
		}
	}

	public void unload()
	{
		this.unload(true);
	}

	public void unload(boolean removeFromList)
	{
		this.onSave();

		for (List<QuestTimer> timers : this._questTimers.values())
		{
			for (QuestTimer timer : timers)
			{
				timer.cancel();
			}

			timers.clear();
		}

		this._questTimers.clear();
		if (removeFromList)
		{
			ScriptManager.getInstance().removeScript(this);
		}

		this._listeners.forEach(AbstractEventListener::unregisterMe);
		this._listeners.clear();
		if (this._timerExecutor != null)
		{
			this._timerExecutor.cancelAllTimers();
		}
	}

	protected void onLoad()
	{
	}

	public void onSave()
	{
	}

	public QuestState newQuestState(Player player)
	{
		return new QuestState(this, player, (byte) 0);
	}

	public QuestState getQuestState(Player player, boolean initIfNone)
	{
		QuestState qs = player.getQuestState(this.getName());
		return qs == null && initIfNone ? this.newQuestState(player) : qs;
	}

	public void startQuestTimer(String name, long time, Npc npc, Player player)
	{
		this.startQuestTimer(name, time, npc, player, false);
	}

	public Map<String, List<QuestTimer>> getQuestTimers()
	{
		return this._questTimers;
	}

	public void startQuestTimer(String name, long time, Npc npc, Player player, boolean repeating)
	{
		if (name != null)
		{
			synchronized (this._questTimers)
			{
				if (!this._questTimers.containsKey(name))
				{
					this._questTimers.put(name, new CopyOnWriteArrayList<>());
				}

				if (this.getQuestTimer(name, npc, player) == null)
				{
					this._questTimers.get(name).add(new QuestTimer(this, name, time, npc, player, repeating));
				}
			}
		}
	}

	public QuestTimer getQuestTimer(String name, Npc npc, Player player)
	{
		if (name == null)
		{
			return null;
		}
		List<QuestTimer> timers = this._questTimers.get(name);
		if (timers != null && !timers.isEmpty())
		{
			for (QuestTimer timer : timers)
			{
				if (timer != null && timer.equals(this, name, npc, player))
				{
					return timer;
				}
			}

			return null;
		}
		return null;
	}

	public void cancelQuestTimers(String name)
	{
		if (name != null)
		{
			List<QuestTimer> timers = this._questTimers.get(name);
			if (timers != null && !timers.isEmpty())
			{
				for (QuestTimer timer : timers)
				{
					if (timer != null)
					{
						timer.cancel();
					}
				}

				timers.clear();
			}
		}
	}

	public void cancelQuestTimer(String name, Npc npc, Player player)
	{
		if (name != null)
		{
			List<QuestTimer> timers = this._questTimers.get(name);
			if (timers != null && !timers.isEmpty())
			{
				for (QuestTimer timer : timers)
				{
					if (timer != null && timer.equals(this, name, npc, player))
					{
						timer.cancel();
					}
				}
			}
		}
	}

	public void removeQuestTimer(QuestTimer timer)
	{
		if (timer != null)
		{
			List<QuestTimer> timers = this._questTimers.get(timer.toString());
			if (timers != null)
			{
				timers.remove(timer);
			}
		}
	}

	public void notifyTeleport(Npc npc)
	{
		try
		{
			this.onTeleport(npc);
		}
		catch (Exception var3)
		{
			LOGGER.log(Level.WARNING, "Exception on onTeleport() in notifyTeleport(): " + var3.getMessage(), var3);
		}
	}

	public void notifyEvent(String event, Npc npc, Player player)
	{
		String res = null;

		try
		{
			if (player != null)
			{
				player.setSimulatedTalking(false);
			}

			res = this.onEvent(event, npc, player);
		}
		catch (Exception var6)
		{
			this.showError(player, var6);
			return;
		}

		this.showResult(player, res, npc);
	}

	public void notifyTalk(Npc npc, Player player)
	{
		String res = null;

		try
		{
			Set<Quest> startingQuests = new HashSet<>();

			for (AbstractEventListener listener : npc.getListeners(EventType.ON_NPC_QUEST_START))
			{
				Object owner = listener.getOwner();
				if (owner instanceof Quest)
				{
					startingQuests.add((Quest) owner);
				}
			}

			String startConditionHtml = this.getStartConditionHtml(player, npc);
			if (startingQuests.contains(this) && startConditionHtml != null)
			{
				res = startConditionHtml;
			}
			else
			{
				res = this.onTalk(npc, player, false);
			}
		}
		catch (Exception var8)
		{
			this.showError(player, var8);
			return;
		}

		player.setLastQuestNpcObject(npc.getObjectId());
		this.showResult(player, res, npc);
	}

	public void notifyFirstTalk(Npc npc, Player player)
	{
		String res = null;

		try
		{
			res = this.onFirstTalk(npc, player);
		}
		catch (Exception var5)
		{
			this.showError(player, var5);
			return;
		}

		this.showResult(player, res, npc);
	}

	public void notifyItemTalk(Item item, Player player)
	{
		String res = null;

		try
		{
			res = this.onItemTalk(item, player);
		}
		catch (Exception var5)
		{
			this.showError(player, var5);
			return;
		}

		this.showResult(player, res);
	}

	public String onItemTalk(Item item, Player player)
	{
		return null;
	}

	public void notifyEventReceived(String eventName, Npc sender, Npc receiver, WorldObject reference)
	{
		try
		{
			this.onEventReceived(eventName, sender, receiver, reference);
		}
		catch (Exception var6)
		{
			LOGGER.log(Level.WARNING, "Exception on onEventReceived() in notifyEventReceived(): " + var6.getMessage(), var6);
		}
	}

	public void notifyOlympiadMatch(Participant winner, Participant looser)
	{
		try
		{
			this.onOlympiadMatchFinish(winner, looser);
		}
		catch (Exception var4)
		{
			LOGGER.log(Level.WARNING, "Execution on onOlympiadMatchFinish() in notifyOlympiadMatch(): " + var4.getMessage(), var4);
		}
	}

	public void notifyMoveFinished(Npc npc)
	{
		try
		{
			this.onMoveFinished(npc);
		}
		catch (Exception var3)
		{
			LOGGER.log(Level.WARNING, "Exception on onMoveFinished() in notifyMoveFinished(): " + var3.getMessage(), var3);
		}
	}

	public void notifyRouteFinished(Npc npc)
	{
		try
		{
			this.onRouteFinished(npc);
		}
		catch (Exception var3)
		{
			LOGGER.log(Level.WARNING, "Exception on onRouteFinished() in notifyRouteFinished(): " + var3.getMessage(), var3);
		}
	}

	public boolean notifyOnCanSeeMe(Npc npc, Player player)
	{
		try
		{
			return this.onCanSeeMe(npc, player);
		}
		catch (Exception var4)
		{
			LOGGER.log(Level.WARNING, "Exception on onCanSeeMe() in notifyOnCanSeeMe(): " + var4.getMessage(), var4);
			return false;
		}
	}

	public void onAttack(Npc npc, Player attacker, int damage, boolean isSummon)
	{
	}

	public void onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill)
	{
		this.onAttack(npc, attacker, damage, isSummon);
	}

	public void onDeath(Creature killer, Creature victim, QuestState qs)
	{
		this.onEvent("", killer instanceof Npc ? killer.asNpc() : null, qs.getPlayer());
	}

	public String onEvent(String event, Npc npc, Player player)
	{
		return null;
	}

	public void onKill(Npc npc, Player killer, boolean isSummon)
	{
	}

	public String onTalk(Npc npc, Player talker, boolean simulated)
	{
		QuestState qs = talker.getQuestState(this.getName());
		if (qs != null)
		{
			qs.setSimulated(simulated);
		}

		talker.setSimulatedTalking(simulated);
		return this.onTalk(npc, talker);
	}

	public String onTalk(Npc npc, Player talker)
	{
		return null;
	}

	public String onFirstTalk(Npc npc, Player player)
	{
		return null;
	}

	public void onItemEvent(Item item, Player player, String event)
	{
	}

	public void onAcquireSkillList(Npc npc, Player player)
	{
	}

	public void onAcquireSkillInfo(Npc npc, Player player, Skill skill)
	{
	}

	public void onAcquireSkill(Npc npc, Player player, Skill skill, AcquireSkillType type)
	{
	}

	public void onSkillSee(Npc npc, Player caster, Skill skill, Collection<WorldObject> targets, boolean isSummon)
	{
	}

	public void onSpellFinished(Npc npc, Player player, Skill skill)
	{
	}

	public void onTrapAction(Trap trap, Creature trigger, TrapAction action)
	{
	}

	public void onSpawn(Npc npc)
	{
	}

	protected void onTeleport(Npc npc)
	{
	}

	public void onFactionCall(Npc npc, Npc caller, Player attacker, boolean isSummon)
	{
	}

	public void onAggroRangeEnter(Npc npc, Player player, boolean isSummon)
	{
	}

	public void onCreatureSee(Npc npc, Creature creature)
	{
	}

	public void onEnterWorld(Player player)
	{
	}

	public void onEnterZone(Creature creature, ZoneType zone)
	{
	}

	public void onExitZone(Creature creature, ZoneType zone)
	{
	}

	public String onEventReceived(String eventName, Npc sender, Npc receiver, WorldObject reference)
	{
		return null;
	}

	public void onOlympiadMatchFinish(Participant winner, Participant looser)
	{
	}

	public void onOlympiadLose(Player loser)
	{
	}

	public void onMoveFinished(Npc npc)
	{
	}

	public void onRouteFinished(Npc npc)
	{
	}

	public boolean onNpcHate(Attackable mob, Player player, boolean isSummon)
	{
		return true;
	}

	public void onSummonSpawn(Summon summon)
	{
	}

	public void onSummonTalk(Summon summon)
	{
	}

	public void onInstanceCreated(Instance instance, Player player)
	{
	}

	public void onInstanceDestroy(Instance instance)
	{
	}

	public void onInstanceEnter(Player player, Instance instance)
	{
	}

	public void onInstanceLeave(Player player, Instance instance)
	{
	}

	public void onNpcDespawn(Npc npc)
	{
	}

	public boolean onCanSeeMe(Npc npc, Player player)
	{
		return false;
	}

	public boolean showError(Player player, Throwable t)
	{
		LOGGER.log(Level.WARNING, this.getScriptFile().toAbsolutePath().toString(), t);
		if (t.getMessage() == null)
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": " + t.getMessage());
		}

		if (player != null && player.getAccessLevel().isGm())
		{
			String res = "<html><body><title>Script error</title>" + TraceUtil.getStackTrace(t) + "</body></html>";
			return this.showResult(player, res);
		}
		return false;
	}

	public boolean showResult(Player player, String res)
	{
		return this.showResult(player, res, null);
	}

	public boolean showResult(Player player, String res, Npc npc)
	{
		if (res != null && !res.isEmpty() && player != null)
		{
			if (res.endsWith(".htm") || res.endsWith(".html"))
			{
				this.showHtmlFile(player, res, npc);
			}
			else if (res.startsWith("<html>"))
			{
				NpcHtmlMessage npcReply = new NpcHtmlMessage(npc != null ? npc.getObjectId() : 0, res);
				if (npc != null)
				{
					npcReply.replace("%objectId%", npc.getObjectId());
				}

				npcReply.replace("%playername%", player.getName());
				player.sendPacket(npcReply);
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else
			{
				player.sendMessage(res);
			}

			return false;
		}
		return true;
	}

	public static void playerEnter(Player player)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement invalidQuestData = con.prepareStatement("DELETE FROM character_quests WHERE charId = ? AND name = ?");
			PreparedStatement invalidQuestDataVar = con.prepareStatement("DELETE FROM character_quests WHERE charId = ? AND name = ? AND var = ?");
			PreparedStatement ps1 = con.prepareStatement("SELECT name, value FROM character_quests WHERE charId = ? AND var = ?");)
		{
			ps1.setInt(1, player.getObjectId());
			ps1.setString(2, "<state>");

			try (ResultSet rs = ps1.executeQuery())
			{
				while (rs.next())
				{
					String questId = rs.getString("name");
					String statename = rs.getString("value");
					Quest q = ScriptManager.getInstance().getScript(questId);
					if (q == null)
					{
						LOGGER.finer("Unknown quest " + questId + " for " + player);
						if (GeneralConfig.AUTODELETE_INVALID_QUEST_DATA)
						{
							invalidQuestData.setInt(1, player.getObjectId());
							invalidQuestData.setString(2, questId);
							invalidQuestData.executeUpdate();
						}
					}
					else
					{
						new QuestState(q, player, State.getStateId(statename));
					}
				}
			}

			try (PreparedStatement ps2 = con.prepareStatement("SELECT name, var, value FROM character_quests WHERE charId = ? AND var <> ?"))
			{
				ps2.setInt(1, player.getObjectId());
				ps2.setString(2, "<state>");

				try (ResultSet rs = ps2.executeQuery())
				{
					while (rs.next())
					{
						String questId = rs.getString("name");
						String var = rs.getString("var");
						String value = rs.getString("value");
						Quest quest = ScriptManager.getInstance().getScript(questId);
						if (quest != null)
						{
							QuestState qs = quest.getQuestState(player, true);
							if (qs == null)
							{
								LOGGER.finer("Lost variable " + var + " in quest " + questId + " for " + player);
								if (GeneralConfig.AUTODELETE_INVALID_QUEST_DATA)
								{
									invalidQuestDataVar.setInt(1, player.getObjectId());
									invalidQuestDataVar.setString(2, questId);
									invalidQuestDataVar.setString(3, var);
									invalidQuestDataVar.executeUpdate();
								}
							}
							else
							{
								qs.setInternal(var, value);
							}
						}
					}
				}
			}
		}
		catch (Exception var26)
		{
			LOGGER.log(Level.WARNING, "could not insert char quest:", var26);
		}
	}

	public static void createQuestVarInDb(QuestState qs, String var, String value)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("INSERT INTO character_quests (charId,name,var,value) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE value=?");)
		{
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setString(2, qs.getQuestName());
			statement.setString(3, var);
			statement.setString(4, value);
			statement.setString(5, value);
			statement.executeUpdate();
		}
		catch (Exception var11)
		{
			LOGGER.log(Level.WARNING, "could not insert char quest:", var11);
		}
	}

	public static void updateQuestVarInDb(QuestState qs, String var, String value)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("UPDATE character_quests SET value=? WHERE charId=? AND name=? AND var = ?");)
		{
			statement.setString(1, value);
			statement.setInt(2, qs.getPlayer().getObjectId());
			statement.setString(3, qs.getQuestName());
			statement.setString(4, var);
			statement.executeUpdate();
		}
		catch (Exception var11)
		{
			LOGGER.log(Level.WARNING, "could not update char quest:", var11);
		}
	}

	public static void deleteQuestVarInDb(QuestState qs, String var)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("DELETE FROM character_quests WHERE charId=? AND name=? AND var=?");)
		{
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setString(2, qs.getQuestName());
			statement.setString(3, var);
			statement.executeUpdate();
		}
		catch (Exception var10)
		{
			LOGGER.log(Level.WARNING, "Unable to delete char quest!", var10);
		}
	}

	public static void deleteQuestInDb(QuestState qs, boolean repeatable)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement(repeatable ? "DELETE FROM character_quests WHERE charId=? AND name=?" : "DELETE FROM character_quests WHERE charId=? AND name=? AND var!=?");)
		{
			ps.setInt(1, qs.getPlayer().getObjectId());
			ps.setString(2, qs.getQuestName());
			if (!repeatable)
			{
				ps.setString(3, "<state>");
			}

			ps.executeUpdate();
		}
		catch (Exception var10)
		{
			LOGGER.log(Level.WARNING, "could not delete char quest:", var10);
		}
	}

	public static void createQuestInDb(QuestState qs)
	{
		createQuestVarInDb(qs, "<state>", State.getStateName(qs.getState()));
	}

	public static void updateQuestInDb(QuestState qs)
	{
		updateQuestVarInDb(qs, "<state>", State.getStateName(qs.getState()));
	}

	public static String getNoQuestMsg(Player player)
	{
		String result = HtmCache.getInstance().getHtm(player, "data/html/noquest.htm");
		return result != null && result.length() > 0 ? result : "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>";
	}

	public static String getNoQuestLevelRewardMsg(Player player)
	{
		return HtmCache.getInstance().getHtm(player, "data/html/noquestlevelreward.html");
	}

	public static String getAlreadyCompletedMsg(Player player)
	{
		return getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
	}

	public static String getAlreadyCompletedMsg(Player player, QuestType type)
	{
		return HtmCache.getInstance().getHtm(player, type == QuestType.ONE_TIME ? "data/html/alreadyCompleted.html" : "data/html/alreadyCompletedDaily.html");
	}

	public void addStartNpc(int npcId)
	{
		this.setNpcQuestStartId(npcId);
	}

	public void addFirstTalkId(int npcId)
	{
		this.setNpcFirstTalkId(event -> this.notifyFirstTalk(event.getNpc(), event.getPlayer()), npcId);
	}

	public void addTalkId(int npcId)
	{
		this.setNpcTalkId(npcId);
	}

	public void addKillId(int npcId)
	{
		this.setAttackableKillId(kill -> {
			Player player = kill.getAttacker();
			this.onKill(kill.getTarget(), player, kill.isSummon());
			if (!this.getNpcLogList(player).isEmpty())
			{
				this.sendNpcLogList(player);
			}
		}, npcId);
	}

	public void addAttackId(int npcId)
	{
		this.setAttackableAttackId(attack -> this.onAttack(attack.getTarget(), attack.getAttacker(), attack.getDamage(), attack.isSummon(), attack.getSkill()), npcId);
	}

	public void addStartNpc(int... npcIds)
	{
		this.setNpcQuestStartId(npcIds);
	}

	public void addStartNpc(Collection<Integer> npcIds)
	{
		this.setNpcQuestStartId(npcIds);
	}

	public void addFirstTalkId(int... npcIds)
	{
		this.setNpcFirstTalkId(event -> this.notifyFirstTalk(event.getNpc(), event.getPlayer()), npcIds);
	}

	public void addFirstTalkId(Collection<Integer> npcIds)
	{
		this.setNpcFirstTalkId(event -> this.notifyFirstTalk(event.getNpc(), event.getPlayer()), npcIds);
	}

	public void addAcquireSkillId(int... npcIds)
	{
		this.setPlayerSkillLearnId(event -> this.onAcquireSkill(event.getTrainer(), event.getPlayer(), event.getSkill(), event.getAcquireType()), npcIds);
	}

	public void addAcquireSkillId(Collection<Integer> npcIds)
	{
		this.setPlayerSkillLearnId(event -> this.onAcquireSkill(event.getTrainer(), event.getPlayer(), event.getSkill(), event.getAcquireType()), npcIds);
	}

	public void addItemBypassEventId(int... itemIds)
	{
		this.setItemBypassEvenId(event -> this.onItemEvent(event.getItem(), event.getPlayer(), event.getEvent()), itemIds);
	}

	public void addItemBypassEventId(Collection<Integer> itemIds)
	{
		this.setItemBypassEvenId(event -> this.onItemEvent(event.getItem(), event.getPlayer(), event.getEvent()), itemIds);
	}

	public void addItemTalkId(int... itemIds)
	{
		this.setItemTalkId(event -> this.notifyItemTalk(event.getItem(), event.getPlayer()), itemIds);
	}

	public void addItemTalkId(Collection<Integer> itemIds)
	{
		this.setItemTalkId(event -> this.notifyItemTalk(event.getItem(), event.getPlayer()), itemIds);
	}

	public void addAttackId(int... npcIds)
	{
		this.setAttackableAttackId(attack -> this.onAttack(attack.getTarget(), attack.getAttacker(), attack.getDamage(), attack.isSummon(), attack.getSkill()), npcIds);
	}

	public void addAttackId(Collection<Integer> npcIds)
	{
		this.setAttackableAttackId(attack -> this.onAttack(attack.getTarget(), attack.getAttacker(), attack.getDamage(), attack.isSummon(), attack.getSkill()), npcIds);
	}

	public void addKillId(int... npcIds)
	{
		this.setAttackableKillId(kill -> {
			Player player = kill.getAttacker();
			this.onKill(kill.getTarget(), player, kill.isSummon());
			if (!this.getNpcLogList(player).isEmpty())
			{
				this.sendNpcLogList(player);
			}
		}, npcIds);
	}

	public void addKillId(Collection<Integer> npcIds)
	{
		this.setAttackableKillId(kill -> {
			Player player = kill.getAttacker();
			this.onKill(kill.getTarget(), player, kill.isSummon());
			if (!this.getNpcLogList(player).isEmpty())
			{
				this.sendNpcLogList(player);
			}
		}, npcIds);
	}

	public void addTalkId(int... npcIds)
	{
		this.setNpcTalkId(npcIds);
	}

	public void addTalkId(Collection<Integer> npcIds)
	{
		this.setNpcTalkId(npcIds);
	}

	public void addTeleportId(int... npcIds)
	{
		this.setNpcTeleportId(event -> this.notifyTeleport(event.getNpc()), npcIds);
	}

	public void addTeleportId(Collection<Integer> npcIds)
	{
		this.setNpcTeleportId(event -> this.notifyTeleport(event.getNpc()), npcIds);
	}

	public void addSpawnId(int... npcIds)
	{
		this.setNpcSpawnId(event -> this.onSpawn(event.getNpc()), npcIds);
	}

	public void addSpawnId(Collection<Integer> npcIds)
	{
		this.setNpcSpawnId(event -> this.onSpawn(event.getNpc()), npcIds);
	}

	public void addDespawnId(int... npcIds)
	{
		this.setNpcDespawnId(event -> this.onNpcDespawn(event.getNpc()), npcIds);
	}

	public void addDespawnId(Collection<Integer> npcIds)
	{
		this.setNpcDespawnId(event -> this.onNpcDespawn(event.getNpc()), npcIds);
	}

	public void addSkillSeeId(int... npcIds)
	{
		this.setNpcSkillSeeId(event -> this.onSkillSee(event.getTarget(), event.getCaster(), event.getSkill(), event.getTargets(), event.isSummon()), npcIds);
	}

	public void addSkillSeeId(Collection<Integer> npcIds)
	{
		this.setNpcSkillSeeId(event -> this.onSkillSee(event.getTarget(), event.getCaster(), event.getSkill(), event.getTargets(), event.isSummon()), npcIds);
	}

	public void addSpellFinishedId(int... npcIds)
	{
		this.setNpcSkillFinishedId(event -> this.onSpellFinished(event.getCaster(), event.getTarget(), event.getSkill()), npcIds);
	}

	public void addSpellFinishedId(Collection<Integer> npcIds)
	{
		this.setNpcSkillFinishedId(event -> this.onSpellFinished(event.getCaster(), event.getTarget(), event.getSkill()), npcIds);
	}

	public void addTrapActionId(int... npcIds)
	{
		this.setTrapActionId(event -> this.onTrapAction(event.getTrap(), event.getTrigger(), event.getAction()), npcIds);
	}

	public void addTrapActionId(Collection<Integer> npcIds)
	{
		this.setTrapActionId(event -> this.onTrapAction(event.getTrap(), event.getTrigger(), event.getAction()), npcIds);
	}

	public void addFactionCallId(int... npcIds)
	{
		this.setAttackableFactionIdId(event -> this.onFactionCall(event.getNpc(), event.getCaller(), event.getAttacker(), event.isSummon()), npcIds);
	}

	public void addFactionCallId(Collection<Integer> npcIds)
	{
		this.setAttackableFactionIdId(event -> this.onFactionCall(event.getNpc(), event.getCaller(), event.getAttacker(), event.isSummon()), npcIds);
	}

	public void addAggroRangeEnterId(int... npcIds)
	{
		this.setAttackableAggroRangeEnterId(event -> this.onAggroRangeEnter(event.getNpc(), event.getPlayer(), event.isSummon()), npcIds);
	}

	public void addAggroRangeEnterId(Collection<Integer> npcIds)
	{
		this.setAttackableAggroRangeEnterId(event -> this.onAggroRangeEnter(event.getNpc(), event.getPlayer(), event.isSummon()), npcIds);
	}

	public void addCreatureSeeId(int... npcIds)
	{
		this.setCreatureSeeId(event -> this.onCreatureSee(event.getCreature().asNpc(), event.getSeen()), npcIds);
	}

	public void addCreatureSeeId(Collection<Integer> npcIds)
	{
		this.setCreatureSeeId(event -> this.onCreatureSee(event.getCreature().asNpc(), event.getSeen()), npcIds);
	}

	public void addEnterZoneId(int zoneId)
	{
		this.setCreatureZoneEnterId(event -> this.onEnterZone(event.getCreature(), event.getZone()), zoneId);
	}

	public void addEnterZoneId(int... zoneIds)
	{
		this.setCreatureZoneEnterId(event -> this.onEnterZone(event.getCreature(), event.getZone()), zoneIds);
	}

	public void addEnterZoneId(Collection<Integer> zoneIds)
	{
		this.setCreatureZoneEnterId(event -> this.onEnterZone(event.getCreature(), event.getZone()), zoneIds);
	}

	public void addExitZoneId(int zoneId)
	{
		this.setCreatureZoneExitId(event -> this.onExitZone(event.getCreature(), event.getZone()), zoneId);
	}

	public void addExitZoneId(int... zoneIds)
	{
		this.setCreatureZoneExitId(event -> this.onExitZone(event.getCreature(), event.getZone()), zoneIds);
	}

	public void addExitZoneId(Collection<Integer> zoneIds)
	{
		this.setCreatureZoneExitId(event -> this.onExitZone(event.getCreature(), event.getZone()), zoneIds);
	}

	public void addEventReceivedId(int... npcIds)
	{
		this.setNpcEventReceivedId(event -> this.notifyEventReceived(event.getEventName(), event.getSender(), event.getReceiver(), event.getReference()), npcIds);
	}

	public void addEventReceivedId(Collection<Integer> npcIds)
	{
		this.setNpcEventReceivedId(event -> this.notifyEventReceived(event.getEventName(), event.getSender(), event.getReceiver(), event.getReference()), npcIds);
	}

	public void addMoveFinishedId(int... npcIds)
	{
		this.setNpcMoveFinishedId(event -> this.notifyMoveFinished(event.getNpc()), npcIds);
	}

	public void addMoveFinishedId(Collection<Integer> npcIds)
	{
		this.setNpcMoveFinishedId(event -> this.notifyMoveFinished(event.getNpc()), npcIds);
	}

	public void addRouteFinishedId(int... npcIds)
	{
		this.setNpcMoveRouteFinishedId(event -> this.notifyRouteFinished(event.getNpc()), npcIds);
	}

	public void addRouteFinishedId(Collection<Integer> npcIds)
	{
		this.setNpcMoveRouteFinishedId(event -> this.notifyRouteFinished(event.getNpc()), npcIds);
	}

	public void addNpcHateId(int... npcIds)
	{
		this.addNpcHateId(event -> new TerminateReturn(!this.onNpcHate(event.getNpc(), event.getPlayer(), event.isSummon()), false, false), npcIds);
	}

	public void addNpcHateId(Collection<Integer> npcIds)
	{
		this.addNpcHateId(event -> new TerminateReturn(!this.onNpcHate(event.getNpc(), event.getPlayer(), event.isSummon()), false, false), npcIds);
	}

	public void addSummonSpawnId(int... npcIds)
	{
		this.setPlayerSummonSpawnId(event -> this.onSummonSpawn(event.getSummon()), npcIds);
	}

	public void addSummonSpawnId(Collection<Integer> npcIds)
	{
		this.setPlayerSummonSpawnId(event -> this.onSummonSpawn(event.getSummon()), npcIds);
	}

	public void addSummonTalkId(int... npcIds)
	{
		this.setPlayerSummonTalkId(event -> this.onSummonTalk(event.getSummon()), npcIds);
	}

	public void addSummonTalkId(Collection<Integer> npcIds)
	{
		this.setPlayerSummonTalkId(event -> this.onSummonTalk(event.getSummon()), npcIds);
	}

	public void addCanSeeMeId(int... npcIds)
	{
		this.addNpcHateId(event -> new TerminateReturn(!this.notifyOnCanSeeMe(event.getNpc(), event.getPlayer()), false, false), npcIds);
	}

	public void addCanSeeMeId(Collection<Integer> npcIds)
	{
		this.addNpcHateId(event -> new TerminateReturn(!this.notifyOnCanSeeMe(event.getNpc(), event.getPlayer()), false, false), npcIds);
	}

	public void addOlympiadMatchFinishId()
	{
		this.setOlympiadMatchResult(event -> this.notifyOlympiadMatch(event.getWinner(), event.getLoser()));
	}

	public void addInstanceCreatedId(int... templateIds)
	{
		this.setInstanceCreatedId(event -> this.onInstanceCreated(event.getInstanceWorld(), event.getCreator()), templateIds);
	}

	public void addInstanceCreatedId(Collection<Integer> templateIds)
	{
		this.setInstanceCreatedId(event -> this.onInstanceCreated(event.getInstanceWorld(), event.getCreator()), templateIds);
	}

	public void addInstanceDestroyId(int... templateIds)
	{
		this.setInstanceDestroyId(event -> this.onInstanceDestroy(event.getInstanceWorld()), templateIds);
	}

	public void addInstanceDestroyId(Collection<Integer> templateIds)
	{
		this.setInstanceDestroyId(event -> this.onInstanceDestroy(event.getInstanceWorld()), templateIds);
	}

	public void addInstanceEnterId(int... templateIds)
	{
		this.setInstanceEnterId(event -> this.onInstanceEnter(event.getPlayer(), event.getInstanceWorld()), templateIds);
	}

	public void addInstanceEnterId(Collection<Integer> templateIds)
	{
		this.setInstanceEnterId(event -> this.onInstanceEnter(event.getPlayer(), event.getInstanceWorld()), templateIds);
	}

	public void addInstanceLeaveId(int... templateIds)
	{
		this.setInstanceLeaveId(event -> this.onInstanceLeave(event.getPlayer(), event.getInstanceWorld()), templateIds);
	}

	public void addInstanceLeaveId(Collection<Integer> templateIds)
	{
		this.setInstanceLeaveId(event -> this.onInstanceLeave(event.getPlayer(), event.getInstanceWorld()), templateIds);
	}

	public static Player getRandomPartyMember(Player player)
	{
		if (player == null)
		{
			return null;
		}
		Party party = player.getParty();
		if (party == null)
		{
			return player;
		}
		List<Player> members = party.getMembers();
		if (members.isEmpty())
		{
			return player;
		}
		Player member = members.get(Rnd.get(members.size()));

		while (player.getInstanceId() != member.getInstanceId())
		{
			member = members.get(Rnd.get(members.size()));
		}

		return member;
	}

	public Player getRandomPartyMember(Player player, int cond)
	{
		return this.getRandomPartyMember(player, "cond", String.valueOf(cond));
	}

	public Player getRandomPartyMember(Player player, String var, String value)
	{
		if (player == null)
		{
			return null;
		}
		else if (var == null)
		{
			return getRandomPartyMember(player);
		}
		else
		{
			QuestState temp = null;
			Party party = player.getParty();
			if (party != null && !party.getMembers().isEmpty())
			{
				List<Player> candidates = new ArrayList<>();
				WorldObject target = player.getTarget();
				if (target == null)
				{
					target = player;
				}

				for (Player partyMember : party.getMembers())
				{
					if (partyMember != null)
					{
						temp = partyMember.getQuestState(this.getName());
						if (temp != null && temp.get(var) != null && temp.get(var).equalsIgnoreCase(value) && partyMember.isInsideRadius3D(target, PlayerConfig.ALT_PARTY_RANGE) && player.getInstanceId() == partyMember.getInstanceId())
						{
							candidates.add(partyMember);
						}
					}
				}

				return candidates.isEmpty() ? null : candidates.get(Rnd.get(candidates.size()));
			}
			temp = player.getQuestState(this.getName());
			return temp != null && temp.isSet(var) && temp.get(var).equalsIgnoreCase(value) ? player : null;
		}
	}

	public Player getRandomPartyMemberState(Player player, byte state)
	{
		if (player == null)
		{
			return null;
		}
		QuestState temp = null;
		Party party = player.getParty();
		if (party != null && !party.getMembers().isEmpty())
		{
			List<Player> candidates = new ArrayList<>();
			WorldObject target = player.getTarget();
			if (target == null)
			{
				target = player;
			}

			for (Player partyMember : party.getMembers())
			{
				if (partyMember != null)
				{
					temp = partyMember.getQuestState(this.getName());
					if (temp != null && temp.getState() == state && partyMember.isInsideRadius3D(target, PlayerConfig.ALT_PARTY_RANGE) && player.getInstanceId() == partyMember.getInstanceId())
					{
						candidates.add(partyMember);
					}
				}
			}

			return candidates.isEmpty() ? null : candidates.get(Rnd.get(candidates.size()));
		}
		temp = player.getQuestState(this.getName());
		return temp != null && temp.getState() == state ? player : null;
	}

	public Player getRandomPartyMember(Player player, Npc npc)
	{
		if (player != null && checkDistanceToTarget(player, npc))
		{
			Party party = player.getParty();
			Player luckyPlayer = null;
			if (party == null)
			{
				if (this.checkPartyMember(player, npc))
				{
					luckyPlayer = player;
				}
			}
			else
			{
				int highestRoll = 0;

				for (Player member : party.getMembers())
				{
					int rnd = getRandom(1000);
					if (rnd > highestRoll && this.checkPartyMember(member, npc))
					{
						highestRoll = rnd;
						luckyPlayer = member;
					}
				}
			}

			return luckyPlayer != null && checkDistanceToTarget(luckyPlayer, npc) ? luckyPlayer : null;
		}
		return null;
	}

	public boolean checkPartyMember(Player player, Npc npc)
	{
		return true;
	}

	public QuestState getRandomPartyMemberState(Player player, int condition, int playerChance, Npc target)
	{
		if (player != null && playerChance >= 1)
		{
			QuestState qs = player.getQuestState(this.getName());
			if (!player.isInParty())
			{
				return this.checkPartyMemberConditions(qs, condition, target) && checkDistanceToTarget(player, target) ? qs : null;
			}
			List<QuestState> candidates = new ArrayList<>();
			if (this.checkPartyMemberConditions(qs, condition, target) && playerChance > 0)
			{
				for (int i = 0; i < playerChance; i++)
				{
					candidates.add(qs);
				}
			}

			for (Player member : player.getParty().getMembers())
			{
				if (member != player)
				{
					qs = member.getQuestState(this.getName());
					if (this.checkPartyMemberConditions(qs, condition, target))
					{
						candidates.add(qs);
					}
				}
			}

			if (candidates.isEmpty())
			{
				return null;
			}
			qs = candidates.get(getRandom(candidates.size()));
			return !checkDistanceToTarget(qs.getPlayer(), target) ? null : qs;
		}
		return null;
	}

	private boolean checkPartyMemberConditions(QuestState qs, int condition, Npc npc)
	{
		return qs != null && (condition == -1 ? qs.isStarted() : qs.isCond(condition)) && this.checkPartyMember(qs, npc);
	}

	private static boolean checkDistanceToTarget(Player player, Npc target)
	{
		return target == null || LocationUtil.checkIfInRange(PlayerConfig.ALT_PARTY_RANGE, player, target, true);
	}

	public boolean checkPartyMember(QuestState qs, Npc npc)
	{
		return true;
	}

	public String showHtmlFile(Player player, String filename)
	{
		return this.showHtmlFile(player, filename, null);
	}

	public String showHtmlFile(Player player, String filename, Npc npc)
	{
		boolean questwindow = !filename.endsWith(".html");
		String content = this.getHtm(player, filename);
		if (content != null)
		{
			if (npc != null)
			{
				content = content.replace("%objectId%", String.valueOf(npc.getObjectId()));
			}

			if (questwindow && this._questId > 0 && this._questId < 20000 && this._questId != 999)
			{
				NpcQuestHtmlMessage npcReply = new NpcQuestHtmlMessage(npc != null ? npc.getObjectId() : 0, this._questId);
				npcReply.setHtml(content);
				npcReply.replace("%playername%", player.getName());
				player.sendPacket(npcReply);
			}
			else
			{
				NpcHtmlMessage npcReply = new NpcHtmlMessage(npc != null ? npc.getObjectId() : 0, content);
				npcReply.replace("%playername%", player.getName());
				player.sendPacket(npcReply);
			}

			player.sendPacket(ActionFailed.STATIC_PACKET);
		}

		return content;
	}

	public String getHtm(Player player, String fileName)
	{
		HtmCache hc = HtmCache.getInstance();
		String content = hc.getHtm(player, fileName.startsWith("data/") ? fileName : "data/scripts/" + this.getPath() + "/" + fileName);
		if (content == null)
		{
			content = hc.getHtm(player, "data/scripts/" + this.getPath() + "/" + fileName);
			if (content == null)
			{
				content = hc.getHtm(player, "data/scripts/quests/" + this.getName() + "/" + fileName);
			}
		}

		return content;
	}

	public int[] getRegisteredItemIds()
	{
		return this._questItemIds;
	}

	public void registerQuestItems(int... items)
	{
		for (int id : items)
		{
			if (id != 0 && ItemData.getInstance().getTemplate(id) == null)
			{
				LOGGER.severe(super.getClass().getSimpleName() + ": Found registerQuestItems for non existing item: " + id + "!");
			}
		}

		this._questItemIds = items;
	}

	public void removeRegisteredQuestItems(Player player)
	{
		takeItems(player, -1, this._questItemIds);
	}

	public void setOnEnterWorld(boolean value)
	{
		if (value)
		{
			this.setPlayerLoginId(event -> this.onEnterWorld(event.getPlayer()));
		}
		else
		{
			for (AbstractEventListener listener : this.getListeners())
			{
				if (listener.getType() == EventType.ON_PLAYER_LOGIN)
				{
					listener.unregisterMe();
				}
			}
		}
	}

	public Set<NpcLogListHolder> getNpcLogList(Player player)
	{
		return Collections.emptySet();
	}

	public <T> boolean isTarget(int[] ids, WorldObject target, Class<T> clazz)
	{
		return target != null && clazz.isInstance(target) ? ArrayUtil.contains(ids, target.getId()) : false;
	}

	public void sendNpcLogList(Player player)
	{
		if (player.getQuestState(this.getName()) != null)
		{
			ExQuestNpcLogList packet = new ExQuestNpcLogList(this._questId);
			this.getNpcLogList(player).forEach(packet::add);
			player.sendPacket(packet);
		}
	}

	private Set<QuestCondition> getStartConditions()
	{
		return this._startCondition;
	}

	public boolean canStartQuest(Player player)
	{
		for (QuestCondition cond : this._startCondition)
		{
			if (!cond.test(player))
			{
				return false;
			}
		}

		return true;
	}

	public String getStartConditionHtml(Player player, Npc npc)
	{
		QuestState qs = this.getQuestState(player, false);
		if (qs != null && !qs.isCreated())
		{
			return null;
		}
		for (QuestCondition cond : this._startCondition)
		{
			if (!cond.test(player))
			{
				return cond.getHtml(npc);
			}
		}

		return null;
	}

	public void addCondStart(Predicate<Player> questStartRequirement, String html)
	{
		this.getStartConditions().add(new QuestCondition(questStartRequirement, html));
	}

	@SafeVarargs
	public final void addCondStart(Predicate<Player> questStartRequirement, KeyValuePair<Integer, String>... pairs)
	{
		this.getStartConditions().add(new QuestCondition(questStartRequirement, pairs));
	}

	public void addCondLevel(int minLevel, int maxLevel, String html)
	{
		this.addCondStart(p -> p.getLevel() >= minLevel && p.getLevel() <= maxLevel, html);
	}

	@SafeVarargs
	public final void addCondMinLevel(int minLevel, int maxLevel, KeyValuePair<Integer, String>... pairs)
	{
		this.addCondStart(p -> p.getLevel() >= minLevel && p.getLevel() <= maxLevel, pairs);
	}

	public void addCondMinLevel(int minLevel, String html)
	{
		this.addCondStart(p -> p.getLevel() >= minLevel, html);
	}

	@SafeVarargs
	public final void addCondMinLevel(int minLevel, KeyValuePair<Integer, String>... pairs)
	{
		this.addCondStart(p -> p.getLevel() >= minLevel, pairs);
	}

	public void addCondMaxLevel(int maxLevel, String html)
	{
		this.addCondStart(p -> p.getLevel() <= maxLevel, html);
	}

	@SafeVarargs
	public final void addCondMaxLevel(int maxLevel, KeyValuePair<Integer, String>... pairs)
	{
		this.addCondStart(p -> p.getLevel() <= maxLevel, pairs);
	}

	public void addCondRace(Race race, String html)
	{
		this.addCondStart(p -> p.getRace() == race, html);
	}

	@SafeVarargs
	public final void addCondRace(Race race, KeyValuePair<Integer, String>... pairs)
	{
		this.addCondStart(p -> p.getRace() == race, pairs);
	}

	public void addCondNotRace(Race race, String html)
	{
		this.addCondStart(p -> p.getRace() != race, html);
	}

	@SafeVarargs
	public final void addCondNotRace(Race race, KeyValuePair<Integer, String>... pairs)
	{
		this.addCondStart(p -> p.getRace() != race, pairs);
	}

	public void addCondCompletedQuest(String name, String html)
	{
		this.addCondStart(p -> p.hasQuestState(name) && p.getQuestState(name).isCompleted(), html);
	}

	@SafeVarargs
	public final void addCondCompletedQuest(String name, KeyValuePair<Integer, String>... pairs)
	{
		this.addCondStart(p -> p.hasQuestState(name) && p.getQuestState(name).isCompleted(), pairs);
	}

	public void addCondStartedQuest(String name, String html)
	{
		this.addCondStart(p -> p.hasQuestState(name) && p.getQuestState(name).isStarted(), html);
	}

	@SafeVarargs
	public final void addCondStartedQuest(String name, KeyValuePair<Integer, String>... pairs)
	{
		this.addCondStart(p -> p.hasQuestState(name) && p.getQuestState(name).isStarted(), pairs);
	}

	public void addCondClassId(PlayerClass playerClass, String html)
	{
		this.addCondStart(p -> p.getPlayerClass() == playerClass, html);
	}

	@SafeVarargs
	public final void addCondClassId(PlayerClass playerClass, KeyValuePair<Integer, String>... pairs)
	{
		this.addCondStart(p -> p.getPlayerClass() == playerClass, pairs);
	}

	public void addCondClassIds(List<PlayerClass> playerClasss, String html)
	{
		this.addCondStart(p -> playerClasss.contains(p.getPlayerClass()), html);
	}

	@SafeVarargs
	public final void addCondItemId(int itemId, KeyValuePair<Integer, String>... pairs)
	{
		this.addCondStart(p -> !p.getInventory().getAllItemsByItemId(itemId).isEmpty(), pairs);
	}

	public void addCondPlayerKarma()
	{
		this.addCondStart(p -> p.getReputation() < 0);
	}

	public void addNewQuestConditions(NewQuestCondition condition, String html)
	{
		if (!condition.getAllowedClassIds().isEmpty())
		{
			this.addCondStart(p -> condition.getAllowedClassIds().contains(p.getPlayerClass()), html);
		}

		if (!condition.getPreviousQuestIds().isEmpty())
		{
			for (Integer questId : condition.getPreviousQuestIds())
			{
				Quest quest = ScriptManager.getInstance().getQuest(questId);
				if (quest != null)
				{
					if (!condition.getOneOfPreQuests())
					{
						this.addCondStart(p -> p.hasQuestState(quest.getName()) && p.getQuestState(quest.getName()).isCompleted(), html);
					}
					else
					{
						this.addCondStart(p -> p.hasAnyCompletedQuestStates(condition.getPreviousQuestIds()), html);
					}
				}
			}

			this.addCondMinLevel(condition.getMinLevel(), html);
			this.addCondMaxLevel(condition.getMaxLevel(), html);
		}
	}

	public void addCondNotClassId(PlayerClass playerClass, String html)
	{
		this.addCondStart(p -> p.getPlayerClass() != playerClass, html);
	}

	@SafeVarargs
	public final void addCondNotClassId(PlayerClass playerClass, KeyValuePair<Integer, String>... pairs)
	{
		this.addCondStart(p -> p.getPlayerClass() != playerClass, pairs);
	}

	public void addCondIsSubClassActive(String html)
	{
		this.addCondStart(p -> p.isSubClassActive(), html);
	}

	@SafeVarargs
	public final void addCondIsSubClassActive(KeyValuePair<Integer, String>... pairs)
	{
		this.addCondStart(p -> p.isSubClassActive(), pairs);
	}

	public void addCondIsNotSubClassActive(String html)
	{
		this.addCondStart(p -> !p.isSubClassActive() && !p.isDualClassActive(), html);
	}

	@SafeVarargs
	public final void addCondIsNotSubClassActive(KeyValuePair<Integer, String>... pairs)
	{
		this.addCondStart(p -> !p.isSubClassActive() && !p.isDualClassActive(), pairs);
	}

	public void addCondInCategory(CategoryType categoryType, String html)
	{
		this.addCondStart(p -> p.isInCategory(categoryType), html);
	}

	@SafeVarargs
	public final void addCondInCategory(CategoryType categoryType, KeyValuePair<Integer, String>... pairs)
	{
		this.addCondStart(p -> p.isInCategory(categoryType), pairs);
	}

	public void addCondClanLevel(int clanLevel, String html)
	{
		this.addCondStart(p -> p.getClan() != null && p.getClan().getLevel() > clanLevel, html);
	}

	@SafeVarargs
	public final void addCondClanLevel(int clanLevel, KeyValuePair<Integer, String>... pairs)
	{
		this.addCondStart(p -> p.getClan() != null && p.getClan().getLevel() > clanLevel, pairs);
	}

	public void onQuestAborted(Player player)
	{
	}

	private void initializeAnnotationListeners()
	{
		List<Integer> ids = new ArrayList<>();

		for (Method method : this.getClass().getMethods())
		{
			if (method.isAnnotationPresent(RegisterEvent.class) && method.isAnnotationPresent(RegisterType.class))
			{
				RegisterEvent listener = method.getAnnotation(RegisterEvent.class);
				RegisterType regType = method.getAnnotation(RegisterType.class);
				ListenerRegisterType type = regType.value();
				EventType eventType = listener.value();
				if (method.getParameterCount() != 1)
				{
					LOGGER.warning(this.getClass().getSimpleName() + ": Non properly defined annotation listener on method: " + method.getName() + " expected parameter count is 1 but found: " + method.getParameterCount());
				}
				else if (!eventType.isEventClass(method.getParameterTypes()[0]))
				{
					LOGGER.warning(this.getClass().getSimpleName() + ": Non properly defined annotation listener on method: " + method.getName() + " expected parameter to be type of: " + eventType.getEventClass().getSimpleName() + " but found: " + method.getParameterTypes()[0].getSimpleName());
				}
				else if (!eventType.isReturnClass(method.getReturnType()))
				{
					LOGGER.warning(this.getClass().getSimpleName() + ": Non properly defined annotation listener on method: " + method.getName() + " expected return type to be one of: " + Arrays.toString(eventType.getReturnClasses()) + " but found: " + method.getReturnType().getSimpleName());
				}
				else
				{
					int priority = 0;
					ids.clear();

					for (Annotation annotation : method.getAnnotations())
					{
						if (annotation instanceof Id npc)
						{
							for (int id : npc.value())
							{
								ids.add(id);
							}
						}
						else if (annotation instanceof Ids npcs)
						{
							for (Id npc : npcs.value())
							{
								for (int id : npc.value())
								{
									ids.add(id);
								}
							}
						}
						else if (annotation instanceof Range range)
						{
							if (range.from() > range.to())
							{
								LOGGER.warning(this.getClass().getSimpleName() + ": Wrong " + annotation.getClass().getSimpleName() + " from is higher then to!");
							}
							else
							{
								for (int id = range.from(); id <= range.to(); id++)
								{
									ids.add(id);
								}
							}
						}
						else if (annotation instanceof Ranges ranges)
						{
							for (Range rangex : ranges.value())
							{
								if (rangex.from() > rangex.to())
								{
									LOGGER.warning(this.getClass().getSimpleName() + ": Wrong " + annotation.getClass().getSimpleName() + " from is higher then to!");
								}
								else
								{
									for (int id = rangex.from(); id <= rangex.to(); id++)
									{
										ids.add(id);
									}
								}
							}
						}
						else if (annotation instanceof NpcLevelRange rangexx)
						{
							if (rangexx.from() > rangexx.to())
							{
								LOGGER.warning(this.getClass().getSimpleName() + ": Wrong " + annotation.getClass().getSimpleName() + " from is higher then to!");
							}
							else if (type != ListenerRegisterType.NPC)
							{
								LOGGER.warning(this.getClass().getSimpleName() + ": ListenerRegisterType " + type + " for " + annotation.getClass().getSimpleName() + " NPC is expected!");
							}
							else
							{
								for (int level = rangexx.from(); level <= rangexx.to(); level++)
								{
									List<NpcTemplate> templates = NpcData.getInstance().getAllOfLevel(level);
									templates.forEach(template -> ids.add(template.getId()));
								}
							}
						}
						else if (!(annotation instanceof NpcLevelRanges ranges))
						{
							if (annotation instanceof Priority p)
							{
								priority = p.value();
							}
						}
						else
						{
							for (NpcLevelRange rangexxx : ranges.value())
							{
								if (rangexxx.from() > rangexxx.to())
								{
									LOGGER.warning(this.getClass().getSimpleName() + ": Wrong " + annotation.getClass().getSimpleName() + " from is higher then to!");
								}
								else if (type != ListenerRegisterType.NPC)
								{
									LOGGER.warning(this.getClass().getSimpleName() + ": ListenerRegisterType " + type + " for " + annotation.getClass().getSimpleName() + " NPC is expected!");
								}
								else
								{
									for (int level = rangexxx.from(); level <= rangexxx.to(); level++)
									{
										List<NpcTemplate> templates = NpcData.getInstance().getAllOfLevel(level);
										templates.forEach(template -> ids.add(template.getId()));
									}
								}
							}
						}
					}

					if (!ids.isEmpty())
					{
						this._registeredIds.computeIfAbsent(type, _ -> ConcurrentHashMap.newKeySet()).addAll(ids);
					}

					this.registerAnnotation(method, eventType, type, priority, ids);
				}
			}
		}
	}

	protected final List<AbstractEventListener> setAttackableKillId(Consumer<OnAttackableKill> callback, int... npcIds)
	{
		for (int id : npcIds)
		{
			if (NpcData.getInstance().getTemplate(id) == null)
			{
				LOGGER.severe(super.getClass().getSimpleName() + ": Found addKillId for non existing NPC: " + id + "!");
			}
		}

		return this.registerConsumer(callback, EventType.ON_ATTACKABLE_KILL, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setAttackableKillId(Consumer<OnAttackableKill> callback, Collection<Integer> npcIds)
	{
		for (int id : npcIds)
		{
			if (NpcData.getInstance().getTemplate(id) == null)
			{
				LOGGER.severe(super.getClass().getSimpleName() + ": Found addKillId for non existing NPC: " + id + "!");
			}
		}

		return this.registerConsumer(callback, EventType.ON_ATTACKABLE_KILL, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> addCreatureKillId(Function<OnCreatureDeath, ? extends AbstractEventReturn> callback, int... npcIds)
	{
		return this.registerFunction(callback, EventType.ON_CREATURE_DEATH, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setCreatureKillId(Consumer<OnCreatureDeath> callback, int... npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_CREATURE_DEATH, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setCreatureKillId(Consumer<OnCreatureDeath> callback, Collection<Integer> npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_CREATURE_DEATH, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> addCreatureAttackedId(Function<OnCreatureAttacked, ? extends AbstractEventReturn> callback, int... npcIds)
	{
		return this.registerFunction(callback, EventType.ON_CREATURE_ATTACKED, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setCreatureAttackedId(Consumer<OnCreatureAttacked> callback, int... npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_CREATURE_ATTACKED, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setCreatureAttackedId(Consumer<OnCreatureAttacked> callback, Collection<Integer> npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_CREATURE_ATTACKED, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setNpcFirstTalkId(Consumer<OnNpcFirstTalk> callback, int... npcIds)
	{
		for (int id : npcIds)
		{
			if (NpcData.getInstance().getTemplate(id) == null)
			{
				LOGGER.severe(super.getClass().getSimpleName() + ": Found addFirstTalkId for non existing NPC: " + id + "!");
			}
		}

		return this.registerConsumer(callback, EventType.ON_NPC_FIRST_TALK, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setNpcFirstTalkId(Consumer<OnNpcFirstTalk> callback, Collection<Integer> npcIds)
	{
		for (int id : npcIds)
		{
			if (NpcData.getInstance().getTemplate(id) == null)
			{
				LOGGER.severe(super.getClass().getSimpleName() + ": Found addFirstTalkId for non existing NPC: " + id + "!");
			}
		}

		return this.registerConsumer(callback, EventType.ON_NPC_FIRST_TALK, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setNpcTalkId(Collection<Integer> npcIds)
	{
		for (int id : npcIds)
		{
			if (NpcData.getInstance().getTemplate(id) == null)
			{
				LOGGER.severe(super.getClass().getSimpleName() + ": Found addTalkId for non existing NPC: " + id + "!");
			}
		}

		return this.registerDummy(EventType.ON_NPC_TALK, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setNpcTalkId(int... npcIds)
	{
		for (int id : npcIds)
		{
			if (NpcData.getInstance().getTemplate(id) == null)
			{
				LOGGER.severe(super.getClass().getSimpleName() + ": Found addTalkId for non existing NPC: " + id + "!");
			}
		}

		return this.registerDummy(EventType.ON_NPC_TALK, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setNpcTeleportId(Consumer<OnNpcTeleport> callback, Collection<Integer> npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_NPC_TELEPORT, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setNpcTeleportId(Consumer<OnNpcTeleport> callback, int... npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_NPC_TELEPORT, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setNpcQuestStartId(int... npcIds)
	{
		for (int id : npcIds)
		{
			if (NpcData.getInstance().getTemplate(id) == null)
			{
				LOGGER.severe(super.getClass().getSimpleName() + ": Found addStartNpc for non existing NPC: " + id + "!");
			}
		}

		return this.registerDummy(EventType.ON_NPC_QUEST_START, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setNpcQuestStartId(Collection<Integer> npcIds)
	{
		for (int id : npcIds)
		{
			if (NpcData.getInstance().getTemplate(id) == null)
			{
				LOGGER.severe(super.getClass().getSimpleName() + ": Found addStartNpc for non existing NPC: " + id + "!");
			}
		}

		return this.registerDummy(EventType.ON_NPC_QUEST_START, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setNpcSkillSeeId(Consumer<OnNpcSkillSee> callback, int... npcIds)
	{
		for (int id : npcIds)
		{
			if (NpcData.getInstance().getTemplate(id) == null)
			{
				LOGGER.severe(super.getClass().getSimpleName() + ": Found addSkillSeeId for non existing NPC: " + id + "!");
			}
		}

		return this.registerConsumer(callback, EventType.ON_NPC_SKILL_SEE, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setNpcSkillSeeId(Consumer<OnNpcSkillSee> callback, Collection<Integer> npcIds)
	{
		for (int id : npcIds)
		{
			if (NpcData.getInstance().getTemplate(id) == null)
			{
				LOGGER.severe(super.getClass().getSimpleName() + ": Found addSkillSeeId for non existing NPC: " + id + "!");
			}
		}

		return this.registerConsumer(callback, EventType.ON_NPC_SKILL_SEE, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setNpcSkillFinishedId(Consumer<OnNpcSkillFinished> callback, int... npcIds)
	{
		for (int id : npcIds)
		{
			if (NpcData.getInstance().getTemplate(id) == null)
			{
				LOGGER.severe(super.getClass().getSimpleName() + ": Found addSpellFinishedId for non existing NPC: " + id + "!");
			}
		}

		return this.registerConsumer(callback, EventType.ON_NPC_SKILL_FINISHED, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setNpcSkillFinishedId(Consumer<OnNpcSkillFinished> callback, Collection<Integer> npcIds)
	{
		for (int id : npcIds)
		{
			if (NpcData.getInstance().getTemplate(id) == null)
			{
				LOGGER.severe(super.getClass().getSimpleName() + ": Found addSpellFinishedId for non existing NPC: " + id + "!");
			}
		}

		return this.registerConsumer(callback, EventType.ON_NPC_SKILL_FINISHED, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setNpcSpawnId(Consumer<OnNpcSpawn> callback, int... npcIds)
	{
		for (int id : npcIds)
		{
			if (NpcData.getInstance().getTemplate(id) == null)
			{
				LOGGER.severe(super.getClass().getSimpleName() + ": Found addSpawnId for non existing NPC: " + id + "!");
			}
		}

		return this.registerConsumer(callback, EventType.ON_NPC_SPAWN, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setNpcSpawnId(Consumer<OnNpcSpawn> callback, Collection<Integer> npcIds)
	{
		for (int id : npcIds)
		{
			if (NpcData.getInstance().getTemplate(id) == null)
			{
				LOGGER.severe(super.getClass().getSimpleName() + ": Found addSpawnId for non existing NPC: " + id + "!");
			}
		}

		return this.registerConsumer(callback, EventType.ON_NPC_SPAWN, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setNpcDespawnId(Consumer<OnNpcDespawn> callback, int... npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_NPC_DESPAWN, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setNpcDespawnId(Consumer<OnNpcDespawn> callback, Collection<Integer> npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_NPC_DESPAWN, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setNpcEventReceivedId(Consumer<OnNpcEventReceived> callback, int... npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_NPC_EVENT_RECEIVED, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setNpcEventReceivedId(Consumer<OnNpcEventReceived> callback, Collection<Integer> npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_NPC_EVENT_RECEIVED, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setNpcMoveFinishedId(Consumer<OnNpcMoveFinished> callback, int... npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_NPC_MOVE_FINISHED, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setNpcMoveFinishedId(Consumer<OnNpcMoveFinished> callback, Collection<Integer> npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_NPC_MOVE_FINISHED, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setNpcMoveRouteFinishedId(Consumer<OnNpcMoveRouteFinished> callback, int... npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_NPC_MOVE_ROUTE_FINISHED, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setNpcMoveRouteFinishedId(Consumer<OnNpcMoveRouteFinished> callback, Collection<Integer> npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_NPC_MOVE_ROUTE_FINISHED, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setNpcHateId(Consumer<OnAttackableHate> callback, int... npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_NPC_HATE, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setNpcHateId(Consumer<OnAttackableHate> callback, Collection<Integer> npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_NPC_HATE, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> addNpcHateId(Function<OnAttackableHate, TerminateReturn> callback, int... npcIds)
	{
		return this.registerFunction(callback, EventType.ON_NPC_HATE, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> addNpcHateId(Function<OnAttackableHate, TerminateReturn> callback, Collection<Integer> npcIds)
	{
		return this.registerFunction(callback, EventType.ON_NPC_HATE, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setNpcCanBeSeenId(Consumer<OnNpcCanBeSeen> callback, int... npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_NPC_CAN_BE_SEEN, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setNpcCanBeSeenId(Consumer<OnNpcCanBeSeen> callback, Collection<Integer> npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_NPC_CAN_BE_SEEN, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setNpcCanBeSeenId(Function<OnNpcCanBeSeen, TerminateReturn> callback, int... npcIds)
	{
		return this.registerFunction(callback, EventType.ON_NPC_CAN_BE_SEEN, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setNpcCanBeSeenId(Function<OnNpcCanBeSeen, TerminateReturn> callback, Collection<Integer> npcIds)
	{
		return this.registerFunction(callback, EventType.ON_NPC_CAN_BE_SEEN, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setCreatureSeeId(Consumer<OnCreatureSee> callback, int... npcIds)
	{
		for (int id : npcIds)
		{
			Npc.addCreatureSeeId(id);
		}

		return this.registerConsumer(callback, EventType.ON_CREATURE_SEE, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setCreatureSeeId(Consumer<OnCreatureSee> callback, Collection<Integer> npcIds)
	{
		for (int id : npcIds)
		{
			Npc.addCreatureSeeId(id);
		}

		return this.registerConsumer(callback, EventType.ON_CREATURE_SEE, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setAttackableFactionIdId(Consumer<OnAttackableFactionCall> callback, int... npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_ATTACKABLE_FACTION_CALL, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setAttackableFactionIdId(Consumer<OnAttackableFactionCall> callback, Collection<Integer> npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_ATTACKABLE_FACTION_CALL, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setAttackableAttackId(Consumer<OnAttackableAttack> callback, int... npcIds)
	{
		for (int id : npcIds)
		{
			if (NpcData.getInstance().getTemplate(id) == null)
			{
				LOGGER.severe(super.getClass().getSimpleName() + ": Found addAttackId for non existing NPC: " + id + "!");
			}
		}

		return this.registerConsumer(callback, EventType.ON_ATTACKABLE_ATTACK, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setAttackableAttackId(Consumer<OnAttackableAttack> callback, Collection<Integer> npcIds)
	{
		for (int id : npcIds)
		{
			if (NpcData.getInstance().getTemplate(id) == null)
			{
				LOGGER.severe(super.getClass().getSimpleName() + ": Found addAttackId for non existing NPC: " + id + "!");
			}
		}

		return this.registerConsumer(callback, EventType.ON_ATTACKABLE_ATTACK, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setAttackableAggroRangeEnterId(Consumer<OnAttackableAggroRangeEnter> callback, int... npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_ATTACKABLE_AGGRO_RANGE_ENTER, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setAttackableAggroRangeEnterId(Consumer<OnAttackableAggroRangeEnter> callback, Collection<Integer> npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_ATTACKABLE_AGGRO_RANGE_ENTER, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setPlayerSkillLearnId(Consumer<OnPlayerSkillLearn> callback, int... npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_PLAYER_SKILL_LEARN, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setPlayerSkillLearnId(Consumer<OnPlayerSkillLearn> callback, Collection<Integer> npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_PLAYER_SKILL_LEARN, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setPlayerSummonSpawnId(Consumer<OnPlayerSummonSpawn> callback, int... npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_PLAYER_SUMMON_SPAWN, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setPlayerSummonSpawnId(Consumer<OnPlayerSummonSpawn> callback, Collection<Integer> npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_PLAYER_SUMMON_SPAWN, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setPlayerSummonTalkId(Consumer<OnPlayerSummonTalk> callback, int... npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_PLAYER_SUMMON_TALK, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setPlayerSummonTalkId(Consumer<OnPlayerSummonSpawn> callback, Collection<Integer> npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_PLAYER_SUMMON_TALK, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setPlayerLoginId(Consumer<OnPlayerLogin> callback)
	{
		return this.registerConsumer(callback, EventType.ON_PLAYER_LOGIN, ListenerRegisterType.GLOBAL);
	}

	protected final List<AbstractEventListener> setPlayerLogoutId(Consumer<OnPlayerLogout> callback)
	{
		return this.registerConsumer(callback, EventType.ON_PLAYER_LOGOUT, ListenerRegisterType.GLOBAL);
	}

	protected final List<AbstractEventListener> setCreatureZoneEnterId(Consumer<OnCreatureZoneEnter> callback, int... npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_CREATURE_ZONE_ENTER, ListenerRegisterType.ZONE, npcIds);
	}

	protected final List<AbstractEventListener> setCreatureZoneEnterId(Consumer<OnCreatureZoneEnter> callback, Collection<Integer> npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_CREATURE_ZONE_ENTER, ListenerRegisterType.ZONE, npcIds);
	}

	protected final List<AbstractEventListener> setCreatureZoneExitId(Consumer<OnCreatureZoneExit> callback, int... npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_CREATURE_ZONE_EXIT, ListenerRegisterType.ZONE, npcIds);
	}

	protected final List<AbstractEventListener> setCreatureZoneExitId(Consumer<OnCreatureZoneExit> callback, Collection<Integer> npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_CREATURE_ZONE_EXIT, ListenerRegisterType.ZONE, npcIds);
	}

	protected final List<AbstractEventListener> setTrapActionId(Consumer<OnTrapAction> callback, int... npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_TRAP_ACTION, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setTrapActionId(Consumer<OnTrapAction> callback, Collection<Integer> npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_TRAP_ACTION, ListenerRegisterType.NPC, npcIds);
	}

	protected final List<AbstractEventListener> setItemBypassEvenId(Consumer<OnItemBypassEvent> callback, int... npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_ITEM_BYPASS_EVENT, ListenerRegisterType.ITEM, npcIds);
	}

	protected final List<AbstractEventListener> setItemBypassEvenId(Consumer<OnItemBypassEvent> callback, Collection<Integer> npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_ITEM_BYPASS_EVENT, ListenerRegisterType.ITEM, npcIds);
	}

	protected final List<AbstractEventListener> setItemTalkId(Consumer<OnItemTalk> callback, int... npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_ITEM_TALK, ListenerRegisterType.ITEM, npcIds);
	}

	protected final List<AbstractEventListener> setItemTalkId(Consumer<OnItemTalk> callback, Collection<Integer> npcIds)
	{
		return this.registerConsumer(callback, EventType.ON_ITEM_TALK, ListenerRegisterType.ITEM, npcIds);
	}

	protected final List<AbstractEventListener> setOlympiadMatchResult(Consumer<OnOlympiadMatchResult> callback)
	{
		return this.registerConsumer(callback, EventType.ON_OLYMPIAD_MATCH_RESULT, ListenerRegisterType.OLYMPIAD);
	}

	protected final List<AbstractEventListener> setCastleSiegeStartId(Consumer<OnCastleSiegeStart> callback, int... castleIds)
	{
		return this.registerConsumer(callback, EventType.ON_CASTLE_SIEGE_START, ListenerRegisterType.CASTLE, castleIds);
	}

	protected final List<AbstractEventListener> setCastleSiegeStartId(Consumer<OnCastleSiegeStart> callback, Collection<Integer> castleIds)
	{
		return this.registerConsumer(callback, EventType.ON_CASTLE_SIEGE_START, ListenerRegisterType.CASTLE, castleIds);
	}

	protected final List<AbstractEventListener> setCastleSiegeOwnerChangeId(Consumer<OnCastleSiegeOwnerChange> callback, int... castleIds)
	{
		return this.registerConsumer(callback, EventType.ON_CASTLE_SIEGE_OWNER_CHANGE, ListenerRegisterType.CASTLE, castleIds);
	}

	protected final List<AbstractEventListener> setCastleSiegeOwnerChangeId(Consumer<OnCastleSiegeOwnerChange> callback, Collection<Integer> castleIds)
	{
		return this.registerConsumer(callback, EventType.ON_CASTLE_SIEGE_OWNER_CHANGE, ListenerRegisterType.CASTLE, castleIds);
	}

	protected final List<AbstractEventListener> setCastleSiegeFinishId(Consumer<OnCastleSiegeFinish> callback, int... castleIds)
	{
		return this.registerConsumer(callback, EventType.ON_CASTLE_SIEGE_FINISH, ListenerRegisterType.CASTLE, castleIds);
	}

	protected final List<AbstractEventListener> setCastleSiegeFinishId(Consumer<OnCastleSiegeFinish> callback, Collection<Integer> castleIds)
	{
		return this.registerConsumer(callback, EventType.ON_CASTLE_SIEGE_FINISH, ListenerRegisterType.CASTLE, castleIds);
	}

	protected final List<AbstractEventListener> setPlayerProfessionChangeId(Consumer<OnPlayerProfessionChange> callback)
	{
		return this.registerConsumer(callback, EventType.ON_PLAYER_PROFESSION_CHANGE, ListenerRegisterType.GLOBAL);
	}

	protected final List<AbstractEventListener> setPlayerProfessionCancelId(Consumer<OnPlayerProfessionCancel> callback)
	{
		return this.registerConsumer(callback, EventType.ON_PLAYER_PROFESSION_CANCEL, ListenerRegisterType.GLOBAL);
	}

	protected final List<AbstractEventListener> setInstanceCreatedId(Consumer<OnInstanceCreated> callback, int... templateIds)
	{
		return this.registerConsumer(callback, EventType.ON_INSTANCE_CREATED, ListenerRegisterType.INSTANCE, templateIds);
	}

	protected final List<AbstractEventListener> setInstanceCreatedId(Consumer<OnInstanceCreated> callback, Collection<Integer> templateIds)
	{
		return this.registerConsumer(callback, EventType.ON_INSTANCE_CREATED, ListenerRegisterType.INSTANCE, templateIds);
	}

	protected final List<AbstractEventListener> setInstanceDestroyId(Consumer<OnInstanceDestroy> callback, int... templateIds)
	{
		return this.registerConsumer(callback, EventType.ON_INSTANCE_DESTROY, ListenerRegisterType.INSTANCE, templateIds);
	}

	protected final List<AbstractEventListener> setInstanceDestroyId(Consumer<OnInstanceDestroy> callback, Collection<Integer> templateIds)
	{
		return this.registerConsumer(callback, EventType.ON_INSTANCE_DESTROY, ListenerRegisterType.INSTANCE, templateIds);
	}

	protected final List<AbstractEventListener> setInstanceEnterId(Consumer<OnInstanceEnter> callback, int... templateIds)
	{
		return this.registerConsumer(callback, EventType.ON_INSTANCE_ENTER, ListenerRegisterType.INSTANCE, templateIds);
	}

	protected final List<AbstractEventListener> setInstanceEnterId(Consumer<OnInstanceEnter> callback, Collection<Integer> templateIds)
	{
		return this.registerConsumer(callback, EventType.ON_INSTANCE_ENTER, ListenerRegisterType.INSTANCE, templateIds);
	}

	protected final List<AbstractEventListener> setInstanceLeaveId(Consumer<OnInstanceLeave> callback, int... templateIds)
	{
		return this.registerConsumer(callback, EventType.ON_INSTANCE_LEAVE, ListenerRegisterType.INSTANCE, templateIds);
	}

	protected final List<AbstractEventListener> setInstanceLeaveId(Consumer<OnInstanceLeave> callback, Collection<Integer> templateIds)
	{
		return this.registerConsumer(callback, EventType.ON_INSTANCE_LEAVE, ListenerRegisterType.INSTANCE, templateIds);
	}

	protected final List<AbstractEventListener> setInstanceStatusChangeId(Consumer<OnInstanceStatusChange> callback, int... templateIds)
	{
		return this.registerConsumer(callback, EventType.ON_INSTANCE_STATUS_CHANGE, ListenerRegisterType.INSTANCE, templateIds);
	}

	protected final List<AbstractEventListener> setInstanceStatusChangeId(Consumer<OnInstanceStatusChange> callback, Collection<Integer> templateIds)
	{
		return this.registerConsumer(callback, EventType.ON_INSTANCE_STATUS_CHANGE, ListenerRegisterType.INSTANCE, templateIds);
	}

	@SuppressWarnings({
		"rawtypes",
		"unchecked"
	})
	protected final List<AbstractEventListener> registerConsumer(Consumer<? extends IBaseEvent> callback, EventType type, ListenerRegisterType registerType, int... npcIds)
	{
		return this.registerListener(container -> new ConsumerEventListener(container, type, event -> ((Consumer) callback).accept(event), this), registerType, npcIds);
	}

	@SuppressWarnings({
		"rawtypes",
		"unchecked"
	})
	protected final List<AbstractEventListener> registerConsumer(Consumer<? extends IBaseEvent> callback, EventType type, ListenerRegisterType registerType, Collection<Integer> npcIds)
	{
		return this.registerListener(container -> new ConsumerEventListener(container, type, event -> ((Consumer) callback).accept(event), this), registerType, npcIds);
	}

	protected final List<AbstractEventListener> registerFunction(Function<? extends IBaseEvent, ? extends AbstractEventReturn> callback, EventType type, ListenerRegisterType registerType, int... npcIds)
	{
		return this.registerListener(container -> new FunctionEventListener(container, type, event -> {
			@SuppressWarnings("unchecked")
			final Function<IBaseEvent, AbstractEventReturn> fn = (Function<IBaseEvent, AbstractEventReturn>) callback;
			return fn.apply((IBaseEvent) event);
		}, this), registerType, npcIds);
	}

	protected final List<AbstractEventListener> registerFunction(Function<? extends IBaseEvent, ? extends AbstractEventReturn> callback, EventType type, ListenerRegisterType registerType, Collection<Integer> npcIds)
	{
		return this.registerListener(container -> new FunctionEventListener(container, type, event -> {
			@SuppressWarnings("unchecked")
			final Function<IBaseEvent, AbstractEventReturn> fn = (Function<IBaseEvent, AbstractEventReturn>) callback;
			return fn.apply((IBaseEvent) event);
		}, this), registerType, npcIds);
	}

	protected final List<AbstractEventListener> registerRunnable(Runnable callback, EventType type, ListenerRegisterType registerType, int... npcIds)
	{
		return this.registerListener(container -> new RunnableEventListener(container, type, callback, this), registerType, npcIds);
	}

	protected final List<AbstractEventListener> registerRunnable(Runnable callback, EventType type, ListenerRegisterType registerType, Collection<Integer> npcIds)
	{
		return this.registerListener(container -> new RunnableEventListener(container, type, callback, this), registerType, npcIds);
	}

	protected final List<AbstractEventListener> registerAnnotation(Method callback, EventType type, ListenerRegisterType registerType, int priority, int... npcIds)
	{
		return this.registerListener(container -> new AnnotationEventListener(container, type, callback, this, priority), registerType, npcIds);
	}

	protected final List<AbstractEventListener> registerAnnotation(Method callback, EventType type, ListenerRegisterType registerType, int priority, Collection<Integer> npcIds)
	{
		return this.registerListener(container -> new AnnotationEventListener(container, type, callback, this, priority), registerType, npcIds);
	}

	protected final List<AbstractEventListener> registerDummy(EventType type, ListenerRegisterType registerType, int... npcIds)
	{
		return this.registerListener(container -> new DummyEventListener(container, type, this), registerType, npcIds);
	}

	protected final List<AbstractEventListener> registerDummy(EventType type, ListenerRegisterType registerType, Collection<Integer> npcIds)
	{
		return this.registerListener(container -> new DummyEventListener(container, type, this), registerType, npcIds);
	}

	protected final List<AbstractEventListener> registerListener(Function<ListenersContainer, AbstractEventListener> action, ListenerRegisterType registerType, int... ids)
	{
		List<AbstractEventListener> listeners = new ArrayList<>(ids.length > 0 ? ids.length : 1);
		if (ids.length > 0)
		{
			for (int id : ids)
			{
				switch (registerType)
				{
					case NPC:
						NpcTemplate templatexxxxx = NpcData.getInstance().getTemplate(id);
						if (templatexxxxx != null)
						{
							listeners.add(templatexxxxx.addListener(action.apply(templatexxxxx)));
						}
						break;
					case ZONE:
						ZoneType templatexxxx = ZoneManager.getInstance().getZoneById(id);
						if (templatexxxx != null)
						{
							listeners.add(templatexxxx.addListener(action.apply(templatexxxx)));
						}
						break;
					case ITEM:
						ItemTemplate templatexxx = ItemData.getInstance().getTemplate(id);
						if (templatexxx != null)
						{
							listeners.add(templatexxx.addListener(action.apply(templatexxx)));
						}
						break;
					case CASTLE:
						Castle templatexx = CastleManager.getInstance().getCastleById(id);
						if (templatexx != null)
						{
							listeners.add(templatexx.addListener(action.apply(templatexx)));
						}
						break;
					case FORTRESS:
						Fort templatex = FortManager.getInstance().getFortById(id);
						if (templatex != null)
						{
							listeners.add(templatex.addListener(action.apply(templatex)));
						}
						break;
					case INSTANCE:
						InstanceTemplate template = InstanceManager.getInstance().getInstanceTemplate(id);
						if (template != null)
						{
							listeners.add(template.addListener(action.apply(template)));
						}
						break;
					default:
						LOGGER.warning(this.getClass().getSimpleName() + ": Unhandled register type: " + registerType);
				}

				this._registeredIds.computeIfAbsent(registerType, _ -> ConcurrentHashMap.newKeySet()).add(id);
			}
		}
		else
		{
			switch (registerType)
			{
				case OLYMPIAD:
					Olympiad templatexxxxx = Olympiad.getInstance();
					listeners.add(templatexxxxx.addListener(action.apply(templatexxxxx)));
					break;
				case GLOBAL:
					ListenersContainer templatexxxxxx = Containers.Global();
					listeners.add(templatexxxxxx.addListener(action.apply(templatexxxxxx)));
					break;
				case GLOBAL_NPCS:
					ListenersContainer templatexxxxxxx = Containers.Npcs();
					listeners.add(templatexxxxxxx.addListener(action.apply(templatexxxxxxx)));
					break;
				case GLOBAL_MONSTERS:
					ListenersContainer templatexxxxxxxx = Containers.Monsters();
					listeners.add(templatexxxxxxxx.addListener(action.apply(templatexxxxxxxx)));
					break;
				case GLOBAL_PLAYERS:
					ListenersContainer templatexxxxxxxxx = Containers.Players();
					listeners.add(templatexxxxxxxxx.addListener(action.apply(templatexxxxxxxxx)));
			}
		}

		this._listeners.addAll(listeners);
		return listeners;
	}

	protected final List<AbstractEventListener> registerListener(Function<ListenersContainer, AbstractEventListener> action, ListenerRegisterType registerType, Collection<Integer> ids)
	{
		List<AbstractEventListener> listeners = new ArrayList<>(!ids.isEmpty() ? ids.size() : 1);
		if (!ids.isEmpty())
		{
			for (int id : ids)
			{
				switch (registerType)
				{
					case NPC:
						NpcTemplate templatexxxxx = NpcData.getInstance().getTemplate(id);
						if (templatexxxxx != null)
						{
							listeners.add(templatexxxxx.addListener(action.apply(templatexxxxx)));
						}
						break;
					case ZONE:
						ZoneType templatexxxx = ZoneManager.getInstance().getZoneById(id);
						if (templatexxxx != null)
						{
							listeners.add(templatexxxx.addListener(action.apply(templatexxxx)));
						}
						break;
					case ITEM:
						ItemTemplate templatexxx = ItemData.getInstance().getTemplate(id);
						if (templatexxx != null)
						{
							listeners.add(templatexxx.addListener(action.apply(templatexxx)));
						}
						break;
					case CASTLE:
						Castle templatexx = CastleManager.getInstance().getCastleById(id);
						if (templatexx != null)
						{
							listeners.add(templatexx.addListener(action.apply(templatexx)));
						}
						break;
					case FORTRESS:
						Fort templatex = FortManager.getInstance().getFortById(id);
						if (templatex != null)
						{
							listeners.add(templatex.addListener(action.apply(templatex)));
						}
						break;
					case INSTANCE:
						InstanceTemplate template = InstanceManager.getInstance().getInstanceTemplate(id);
						if (template != null)
						{
							listeners.add(template.addListener(action.apply(template)));
						}
						break;
					default:
						LOGGER.warning(this.getClass().getSimpleName() + ": Unhandled register type: " + registerType);
				}
			}

			this._registeredIds.computeIfAbsent(registerType, _ -> ConcurrentHashMap.newKeySet()).addAll(ids);
		}
		else
		{
			switch (registerType)
			{
				case OLYMPIAD:
					Olympiad templatexxxxx = Olympiad.getInstance();
					listeners.add(templatexxxxx.addListener(action.apply(templatexxxxx)));
					break;
				case GLOBAL:
					ListenersContainer templatexxxxxx = Containers.Global();
					listeners.add(templatexxxxxx.addListener(action.apply(templatexxxxxx)));
					break;
				case GLOBAL_NPCS:
					ListenersContainer templatexxxxxxx = Containers.Npcs();
					listeners.add(templatexxxxxxx.addListener(action.apply(templatexxxxxxx)));
					break;
				case GLOBAL_MONSTERS:
					ListenersContainer templatexxxxxxxx = Containers.Monsters();
					listeners.add(templatexxxxxxxx.addListener(action.apply(templatexxxxxxxx)));
					break;
				case GLOBAL_PLAYERS:
					ListenersContainer templatexxxxxxxxx = Containers.Players();
					listeners.add(templatexxxxxxxxx.addListener(action.apply(templatexxxxxxxxx)));
			}
		}

		this._listeners.addAll(listeners);
		return listeners;
	}

	public Set<Integer> getRegisteredIds(ListenerRegisterType type)
	{
		return this._registeredIds.getOrDefault(type, Collections.emptySet());
	}

	public Queue<AbstractEventListener> getListeners()
	{
		return this._listeners;
	}

	public void onSpawnActivate(SpawnTemplate template)
	{
	}

	public void onSpawnDeactivate(SpawnTemplate template)
	{
	}

	public void onSpawnNpc(SpawnTemplate template, SpawnGroup group, Npc npc)
	{
	}

	public void onSpawnDespawnNpc(SpawnTemplate template, SpawnGroup group, Npc npc)
	{
	}

	public void onSpawnNpcDeath(SpawnTemplate template, SpawnGroup group, Npc npc, Creature killer)
	{
	}

	public static void showOnScreenMsg(Player player, String text, int time)
	{
		if (!player.isSimulatingTalking())
		{
			player.sendPacket(new ExShowScreenMessage(text, time));
		}
	}

	public static void showOnScreenMsg(Player player, NpcStringId npcString, int position, int time, String... params)
	{
		if (!player.isSimulatingTalking())
		{
			player.sendPacket(new ExShowScreenMessage(npcString, position, time, params));
		}
	}

	public static void showOnScreenMsg(Player player, NpcStringId npcString, int position, int time, boolean showEffect, String... params)
	{
		if (!player.isSimulatingTalking())
		{
			player.sendPacket(new ExShowScreenMessage(npcString, position, time, showEffect, params));
		}
	}

	public static void showOnScreenMsg(Player player, SystemMessageId systemMsg, int position, int time, String... params)
	{
		if (!player.isSimulatingTalking())
		{
			player.sendPacket(new ExShowScreenMessage(systemMsg, position, time, params));
		}
	}

	public static Npc addSpawn(int npcId, IPositionable pos)
	{
		return addSpawn(npcId, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading(), false, 0L, false, 0);
	}

	public static Npc addSpawn(Npc summoner, int npcId, IPositionable pos, boolean randomOffset, long despawnDelay)
	{
		return addSpawn(summoner, npcId, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading(), randomOffset, despawnDelay, false, 0);
	}

	public static Npc addSpawn(int npcId, IPositionable pos, boolean isSummonSpawn)
	{
		return addSpawn(npcId, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading(), false, 0L, isSummonSpawn, 0);
	}

	public static Npc addSpawn(int npcId, IPositionable pos, boolean randomOffset, long despawnDelay)
	{
		return addSpawn(npcId, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading(), randomOffset, despawnDelay, false, 0);
	}

	public static Npc addSpawn(int npcId, IPositionable pos, boolean randomOffset, long despawnDelay, boolean isSummonSpawn)
	{
		return addSpawn(npcId, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading(), randomOffset, despawnDelay, isSummonSpawn, 0);
	}

	public static Npc addSpawn(Npc summoner, int npcId, IPositionable pos, boolean randomOffset, int instanceId)
	{
		return addSpawn(summoner, npcId, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading(), randomOffset, 0L, false, instanceId);
	}

	public static Npc addSpawn(int npcId, IPositionable pos, boolean randomOffset, long despawnDelay, boolean isSummonSpawn, int instanceId)
	{
		return addSpawn(npcId, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading(), randomOffset, despawnDelay, isSummonSpawn, instanceId);
	}

	public static Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, long despawnDelay)
	{
		return addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay, false, 0);
	}

	public static Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, long despawnDelay, boolean isSummonSpawn)
	{
		return addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay, isSummonSpawn, 0);
	}

	public static Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, long despawnDelay, boolean isSummonSpawn, int instanceId)
	{
		return addSpawn(null, npcId, x, y, z, heading, randomOffset, despawnDelay, isSummonSpawn, instanceId);
	}

	public static Npc addSpawn(Npc summoner, int npcId, int xValue, int yValue, int zValue, int heading, boolean randomOffset, long despawnDelay, boolean isSummonSpawn, int instance)
	{
		try
		{
			if (xValue == 0 && yValue == 0)
			{
				LOGGER.severe("addSpawn(): invalid spawn coordinates for NPC #" + npcId + "!");
				return null;
			}
			int x = xValue;
			int y = yValue;
			if (randomOffset)
			{
				int offset = Rnd.get(50, 100);
				if (Rnd.nextBoolean())
				{
					offset *= -1;
				}

				x = xValue + offset;
				offset = Rnd.get(50, 100);
				if (Rnd.nextBoolean())
				{
					offset *= -1;
				}

				y = yValue + offset;
			}

			Spawn spawn = new Spawn(npcId);
			spawn.setInstanceId(instance);
			spawn.setHeading(heading);
			spawn.setXYZ(x, y, zValue);
			spawn.stopRespawn();
			Npc npc = spawn.doSpawn(isSummonSpawn);
			if (despawnDelay > 0L)
			{
				npc.scheduleDespawn(despawnDelay);
			}

			if (summoner != null)
			{
				summoner.addSummonedNpc(npc);
			}

			if (RandomSpawnsConfig.ENABLE_RANDOM_MONSTER_SPAWNS && !randomOffset && npc.isMonster())
			{
				spawn.setXYZ(x, y, zValue);
				npc.setXYZ(x, y, zValue);
				if (heading > -1)
				{
					npc.setHeading(heading);
				}
			}

			npc.broadcastInfo();
			return npc;
		}
		catch (Exception var15)
		{
			LOGGER.warning("Could not spawn NPC #" + npcId + "; error: " + var15.getMessage());
			return null;
		}
	}

	public Trap addTrap(int trapId, int x, int y, int z, int heading, int instanceId)
	{
		NpcTemplate npcTemplate = NpcData.getInstance().getTemplate(trapId);
		Trap trap = new Trap(npcTemplate, instanceId, -1);
		trap.setCurrentHp(trap.getMaxHp());
		trap.setCurrentMp(trap.getMaxMp());
		trap.setInvul(true);
		trap.setHeading(heading);
		trap.spawnMe(x, y, z);
		return trap;
	}

	public Npc addMinion(Monster master, int minionId)
	{
		return MinionList.spawnMinion(master, minionId);
	}

	public static long getQuestItemsCount(Player player, int itemId)
	{
		return player.getInventory().getInventoryItemCount(itemId, -1);
	}

	public long getQuestItemsCount(Player player, int... itemIds)
	{
		long count = 0L;

		for (Item item : player.getInventory().getItems())
		{
			if (item != null)
			{
				for (int itemId : itemIds)
				{
					if (item.getId() == itemId)
					{
						if (count + item.getCount() > Long.MAX_VALUE)
						{
							return Long.MAX_VALUE;
						}

						count += item.getCount();
					}
				}
			}
		}

		return count;
	}

	protected static boolean hasItem(Player player, ItemHolder item)
	{
		return hasItem(player, item, true);
	}

	protected static boolean hasItem(Player player, ItemHolder item, boolean checkCount)
	{
		if (item == null)
		{
			return false;
		}
		return checkCount ? getQuestItemsCount(player, item.getId()) >= item.getCount() : hasQuestItems(player, item.getId());
	}

	protected static boolean hasAllItems(Player player, boolean checkCount, ItemHolder... itemList)
	{
		if (itemList != null && itemList.length != 0)
		{
			for (ItemHolder item : itemList)
			{
				if (!hasItem(player, item, checkCount))
				{
					return false;
				}
			}

			return true;
		}
		return false;
	}

	public static boolean hasQuestItems(Player player, int itemId)
	{
		return player.getInventory().getItemByItemId(itemId) != null;
	}

	public static boolean hasQuestItems(Player player, int... itemIds)
	{
		if (itemIds != null && itemIds.length != 0)
		{
			PlayerInventory inv = player.getInventory();

			for (int itemId : itemIds)
			{
				if (inv.getItemByItemId(itemId) == null)
				{
					return false;
				}
			}

			return true;
		}
		return false;
	}

	public boolean hasAtLeastOneQuestItem(Player player, int... itemIds)
	{
		PlayerInventory inv = player.getInventory();

		for (int itemId : itemIds)
		{
			if (inv.getItemByItemId(itemId) != null)
			{
				return true;
			}
		}

		return false;
	}

	public boolean ownsAtLeastOneItem(Player player, int... itemIds)
	{
		PlayerInventory inventory = player.getInventory();

		for (int itemId : itemIds)
		{
			if (inventory.getItemByItemId(itemId) != null)
			{
				return true;
			}
		}

		PlayerWarehouse warehouse = player.getWarehouse();

		for (int itemIdx : itemIds)
		{
			if (warehouse.getItemByItemId(itemIdx) != null)
			{
				return true;
			}
		}

		if (player.hasPet())
		{
			PetInventory petInventory = player.getPet().getInventory();
			if (petInventory != null)
			{
				for (int itemIdxx : itemIds)
				{
					if (petInventory.getItemByItemId(itemIdxx) != null)
					{
						return true;
					}
				}
			}
		}

		if (player.hasServitors())
		{
			for (Summon summon : player.getServitors().values())
			{
				PetInventory summonInventory = summon.getInventory();
				if (summonInventory != null)
				{
					for (int itemIdxxx : itemIds)
					{
						if (summonInventory.getItemByItemId(itemIdxxx) != null)
						{
							return true;
						}
					}
				}
			}
		}

		if (GeneralConfig.ALLOW_MAIL)
		{
			List<Message> inbox = MailManager.getInstance().getInbox(player.getObjectId());

			for (int itemIdxxxx : itemIds)
			{
				for (Message message : inbox)
				{
					Mail mail = message.getAttachments();
					if (mail != null && mail.getItemByItemId(itemIdxxxx) != null)
					{
						return true;
					}
				}
			}

			List<Message> outbox = MailManager.getInstance().getOutbox(player.getObjectId());

			for (int itemIdxxxx : itemIds)
			{
				for (Message messagex : outbox)
				{
					Mail mail = messagex.getAttachments();
					if (mail != null && mail.getItemByItemId(itemIdxxxx) != null)
					{
						return true;
					}
				}
			}
		}

		for (int itemIdxxxx : itemIds)
		{
			if (ItemCommissionManager.getInstance().hasCommissionedItemId(player, itemIdxxxx))
			{
				return true;
			}
		}

		return false;
	}

	public static int getEnchantLevel(Player player, int itemId)
	{
		Item enchantedItem = player.getInventory().getItemByItemId(itemId);
		return enchantedItem == null ? 0 : enchantedItem.getEnchantLevel();
	}

	public void giveAdena(Player player, long count, boolean applyRates)
	{
		if (applyRates)
		{
			rewardItems(player, 57, count);
		}
		else
		{
			giveItems(player, 57, count);
		}
	}

	public static void rewardItems(Player player, ItemHolder holder)
	{
		rewardItems(player, holder.getId(), holder.getCount());
	}

	public static void rewardItems(Player player, int itemId, long countValue)
	{
		if (!player.isSimulatingTalking())
		{
			if (countValue > 0L)
			{
				ItemTemplate item = ItemData.getInstance().getTemplate(itemId);
				if (item != null)
				{
					long count = countValue;

					try
					{
						if (itemId == 57)
						{
							count = (long) (count * RatesConfig.RATE_QUEST_REWARD_ADENA);
						}
						else if (RatesConfig.RATE_QUEST_REWARD_USE_MULTIPLIERS)
						{
							if (item instanceof EtcItem)
							{
								switch (((EtcItem) item).getItemType())
								{
									case POTION:
										count = (long) (count * RatesConfig.RATE_QUEST_REWARD_POTION);
										break;
									case ENCHT_WP:
									case ENCHT_AM:
									case SCROLL:
										count = (long) (count * RatesConfig.RATE_QUEST_REWARD_SCROLL);
										break;
									case RECIPE:
										count = (long) (count * RatesConfig.RATE_QUEST_REWARD_RECIPE);
										break;
									case MATERIAL:
										count = (long) (count * RatesConfig.RATE_QUEST_REWARD_MATERIAL);
										break;
									default:
										count = (long) (count * RatesConfig.RATE_QUEST_REWARD);
								}
							}
						}
						else
						{
							count = (long) (count * RatesConfig.RATE_QUEST_REWARD);
						}
					}
					catch (Exception var8)
					{
						count = Long.MAX_VALUE;
					}

					Item itemInstance = player.getInventory().addItem(ItemProcessType.QUEST, itemId, count, player, player.getTarget());
					if (itemInstance != null)
					{
						sendItemGetMessage(player, itemInstance, count);
					}
				}
			}
		}
	}

	private static void sendItemGetMessage(Player player, Item item, long count)
	{
		if (item.getId() == 57)
		{
			SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1_ADENA_2);
			smsg.addLong(count);
			player.sendPacket(smsg);
		}
		else if (count > 1L)
		{
			SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1_X_S2);
			smsg.addItemName(item);
			smsg.addLong(count);
			player.sendPacket(smsg);
		}
		else
		{
			SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1_2);
			smsg.addItemName(item);
			player.sendPacket(smsg);
		}

		player.sendPacket(new ExUserInfoInvenWeight(player));
		player.sendPacket(new ExAdenaInvenCount(player));
	}

	public static void giveItems(Player player, int itemId, long count)
	{
		giveItems(player, itemId, count, 0, false);
	}

	public static void giveItems(Player player, int itemId, long count, boolean playSound)
	{
		giveItems(player, itemId, count, 0, playSound);
	}

	protected void giveItems(Player player, ItemHolder holder)
	{
		giveItems(player, holder.getId(), holder.getCount());
	}

	public static void giveItems(Player player, int itemId, long count, int enchantlevel, boolean playSound)
	{
		if (!player.isSimulatingTalking())
		{
			if (count > 0L)
			{
				ItemTemplate template = ItemData.getInstance().getTemplate(itemId);
				if (template != null)
				{
					long finalCount = count;
					if (template.isQuestItem())
					{
						finalCount = (long) (count * RatesConfig.QUEST_ITEM_DROP_AMOUNT_MULTIPLIER);
					}

					Item item = player.getInventory().addItem(ItemProcessType.QUEST, itemId, finalCount, player, player.getTarget());
					if (item != null)
					{
						if (enchantlevel > 0 && itemId != 57)
						{
							item.setEnchantLevel(enchantlevel);
						}

						if (playSound)
						{
							playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
						}

						sendItemGetMessage(player, item, finalCount);
					}
				}
			}
		}
	}

	public static void giveItems(Player player, int itemId, long count, AttributeType attributeType, int attributeValue)
	{
		if (!player.isSimulatingTalking())
		{
			if (count > 0L)
			{
				ItemTemplate template = ItemData.getInstance().getTemplate(itemId);
				if (template != null)
				{
					long finalCount = count;
					if (template.isQuestItem())
					{
						finalCount = (long) (count * RatesConfig.QUEST_ITEM_DROP_AMOUNT_MULTIPLIER);
					}

					Item item = player.getInventory().addItem(ItemProcessType.QUEST, itemId, finalCount, player, player.getTarget());
					if (item != null)
					{
						if (attributeType != null && attributeValue > 0)
						{
							item.setAttribute(new AttributeHolder(attributeType, attributeValue), true);
							if (item.isEquipped())
							{
								player.getStat().recalculateStats(true);
							}

							InventoryUpdate iu = new InventoryUpdate();
							iu.addModifiedItem(item);
							player.sendInventoryUpdate(iu);
						}

						sendItemGetMessage(player, item, finalCount);
					}
				}
			}
		}
	}

	public static boolean giveItemRandomly(Player player, int itemId, long amountToGive, long limit, double dropChance, boolean playSound)
	{
		return giveItemRandomly(player, null, itemId, amountToGive, amountToGive, limit, dropChance, playSound);
	}

	public static boolean giveItemRandomly(Player player, Npc npc, int itemId, long amountToGive, long limit, double dropChance, boolean playSound)
	{
		return giveItemRandomly(player, npc, itemId, amountToGive, amountToGive, limit, dropChance, playSound);
	}

	public static boolean giveItemRandomly(Player player, Npc npc, int itemId, long minAmount, long maxAmount, long limit, double dropChance, boolean playSound)
	{
		if (player.isSimulatingTalking())
		{
			return false;
		}
		long currentCount = getQuestItemsCount(player, itemId);
		if (limit > 0L && currentCount >= limit)
		{
			return true;
		}
		long minAmountWithBonus = (long) (minAmount * RatesConfig.QUEST_ITEM_DROP_AMOUNT_MULTIPLIER);
		long maxAmountWithBonus = (long) (maxAmount * RatesConfig.QUEST_ITEM_DROP_AMOUNT_MULTIPLIER);
		double dropChanceWithBonus = dropChance;
		if (npc != null && ChampionMonstersConfig.CHAMPION_ENABLE && npc.isChampion())
		{
			if (itemId != 57 && itemId != 5575)
			{
				dropChanceWithBonus = dropChance * ChampionMonstersConfig.CHAMPION_REWARDS_CHANCE;
				minAmountWithBonus = (long) (minAmountWithBonus * ChampionMonstersConfig.CHAMPION_REWARDS_AMOUNT);
				maxAmountWithBonus = (long) (maxAmountWithBonus * ChampionMonstersConfig.CHAMPION_REWARDS_AMOUNT);
			}
			else
			{
				dropChanceWithBonus = dropChance * ChampionMonstersConfig.CHAMPION_ADENAS_REWARDS_CHANCE;
				minAmountWithBonus = (long) (minAmountWithBonus * ChampionMonstersConfig.CHAMPION_ADENAS_REWARDS_AMOUNT);
				maxAmountWithBonus = (long) (maxAmountWithBonus * ChampionMonstersConfig.CHAMPION_ADENAS_REWARDS_AMOUNT);
			}
		}

		long amountToGive = minAmountWithBonus == maxAmountWithBonus ? minAmountWithBonus : Rnd.get(minAmountWithBonus, maxAmountWithBonus);
		double random = Rnd.nextDouble();
		if (dropChanceWithBonus >= random && amountToGive > 0L && player.getInventory().validateCapacityByItemId(itemId))
		{
			if (limit > 0L && currentCount + amountToGive > limit)
			{
				amountToGive = limit - currentCount;
			}

			if (player.addItem(ItemProcessType.QUEST, itemId, amountToGive, npc, true) != null)
			{
				if (currentCount + amountToGive == limit)
				{
					if (playSound)
					{
						playSound(player, QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}

					return true;
				}

				if (playSound)
				{
					playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}

				if (limit <= 0L)
				{
					return true;
				}
			}
		}

		return false;
	}

	public static boolean takeItems(Player player, int itemId, long amountValue)
	{
		if (player.isSimulatingTalking())
		{
			return false;
		}
		Item item = player.getInventory().getItemByItemId(itemId);
		if (item == null)
		{
			return false;
		}
		long amount = amountValue;
		if (amountValue < 0L || amountValue > item.getCount())
		{
			amount = item.getCount();
		}

		if (item.isEquipped())
		{
			InventoryUpdate iu = new InventoryUpdate();

			for (Item itm : player.getInventory().unEquipItemInBodySlotAndRecord(item.getTemplate().getBodyPart()))
			{
				iu.addModifiedItem(itm);
			}

			player.sendInventoryUpdate(iu);
			player.broadcastUserInfo();
		}

		return player.destroyItemByItemId(ItemProcessType.QUEST, itemId, amount, player, true);
	}

	protected static boolean takeItem(Player player, ItemHolder holder)
	{
		return holder == null ? false : takeItems(player, holder.getId(), holder.getCount());
	}

	protected static boolean takeAllItems(Player player, ItemHolder... itemList)
	{
		if (player.isSimulatingTalking())
		{
			return false;
		}
		else if (itemList != null && itemList.length != 0)
		{
			if (!hasAllItems(player, true, itemList))
			{
				return false;
			}
			for (ItemHolder item : itemList)
			{
				if (!takeItem(player, item))
				{
					return false;
				}
			}

			return true;
		}
		else
		{
			return false;
		}
	}

	public static boolean takeItems(Player player, int amount, int... itemIds)
	{
		if (player.isSimulatingTalking())
		{
			return false;
		}
		boolean check = true;
		if (itemIds != null)
		{
			for (int item : itemIds)
			{
				check &= takeItems(player, item, amount);
			}
		}

		return check;
	}

	public static void playSound(Instance world, String sound)
	{
		world.broadcastPacket(new PlaySound(sound));
	}

	public static void playSound(Player player, String sound)
	{
		if (!player.isSimulatingTalking())
		{
			player.sendPacket(QuestSound.getSound(sound));
		}
	}

	public static void playSound(Player player, QuestSound sound)
	{
		if (!player.isSimulatingTalking())
		{
			player.sendPacket(sound.getPacket());
		}
	}

	public static void addExpAndSp(Player player, long exp, int sp)
	{
		if (!player.isSimulatingTalking())
		{
			long addExp = exp;
			int addSp = sp;
			if (player.hasPremiumStatus())
			{
				addExp = (long) (exp * PremiumSystemConfig.PREMIUM_RATE_QUEST_XP);
				addSp = (int) (sp * PremiumSystemConfig.PREMIUM_RATE_QUEST_SP);
			}

			player.addExpAndSp((long) player.getStat().getValue(Stat.EXPSP_RATE, addExp * RatesConfig.RATE_QUEST_REWARD_XP), (int) player.getStat().getValue(Stat.EXPSP_RATE, addSp * RatesConfig.RATE_QUEST_REWARD_SP));
			PcCafePointsManager.getInstance().givePcCafePoint(player, (long) (addExp * RatesConfig.RATE_QUEST_REWARD_XP));
		}
	}

	public static int getRandom(int max)
	{
		return Rnd.get(max);
	}

	public static int getRandom(int min, int max)
	{
		return Rnd.get(min, max);
	}

	public static long getRandom(long max)
	{
		return Rnd.get(max);
	}

	public static long getRandom(long min, long max)
	{
		return Rnd.get(min, max);
	}

	public static boolean getRandomBoolean()
	{
		return Rnd.nextBoolean();
	}

	@SuppressWarnings("unchecked")
	public static <T> T getRandomEntry(T... array)
	{
		return array.length == 0 ? null : array[getRandom(array.length)];
	}

	public static <T> T getRandomEntry(List<T> list)
	{
		return list.isEmpty() ? null : list.get(getRandom(list.size()));
	}

	public static int getRandomEntry(int... array)
	{
		return array[getRandom(array.length)];
	}

	public static int getItemEquipped(Player player, int slot)
	{
		return player.getInventory().getPaperdollItemId(slot);
	}

	public static int getGameTicks()
	{
		return GameTimeTaskManager.getInstance().getGameTicks();
	}

	public void executeForEachPlayer(Player player, Npc npc, boolean isSummon, boolean includeParty, boolean includeCommandChannel)
	{
		if (!player.isSimulatingTalking())
		{
			if ((includeParty || includeCommandChannel) && player.isInParty())
			{
				Party party = player.getParty();
				if (includeCommandChannel && party.isInCommandChannel())
				{
					party.getCommandChannel().forEachMember(member -> {
						this.actionForEachPlayer(member, npc, isSummon);
						return true;
					});
				}
				else if (includeParty)
				{
					party.forEachMember(member -> {
						this.actionForEachPlayer(member, npc, isSummon);
						return true;
					});
				}
			}
			else
			{
				this.actionForEachPlayer(player, npc, isSummon);
			}
		}
	}

	public void actionForEachPlayer(Player player, Npc npc, boolean isSummon)
	{
	}

	public void openDoor(int doorId, int instanceId)
	{
		Door door = this.getDoor(doorId, instanceId);
		if (door == null)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": called openDoor(" + doorId + ", " + instanceId + "); but door was not found!", (new NullPointerException()));
		}
		else if (!door.isOpen())
		{
			door.openMe();
		}
	}

	public void closeDoor(int doorId, int instanceId)
	{
		Door door = this.getDoor(doorId, instanceId);
		if (door == null)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": called closeDoor(" + doorId + ", " + instanceId + "); but door was not found!", (new NullPointerException()));
		}
		else if (door.isOpen())
		{
			door.closeMe();
		}
	}

	public Door getDoor(int doorId, int instanceId)
	{
		Door door = null;
		Instance instance = InstanceManager.getInstance().getInstance(instanceId);
		if (instance != null)
		{
			door = instance.getDoor(doorId);
		}
		else
		{
			door = DoorData.getInstance().getDoor(doorId);
		}

		return door;
	}

	protected void addAttackPlayerDesire(Npc npc, Playable playable)
	{
		this.addAttackPlayerDesire(npc, playable, 999);
	}

	protected void addAttackPlayerDesire(Npc npc, Playable target, int desire)
	{
		if (npc.isAttackable())
		{
			npc.asAttackable().addDamageHate(target, 0L, desire);
		}

		npc.setRunning();
		npc.getAI().setIntention(Intention.ATTACK, target);
	}

	protected void addAttackDesire(Npc npc, Creature target)
	{
		npc.setRunning();
		npc.getAI().setIntention(Intention.ATTACK, target);
	}

	protected void addMoveToDesire(Npc npc, Location loc, int desire)
	{
		npc.getAI().setIntention(Intention.MOVE_TO, loc);
	}

	protected void castSkill(Npc npc, Playable target, SkillHolder skill)
	{
		npc.setTarget(target);
		npc.doCast(skill.getSkill());
	}

	protected void castSkill(Npc npc, Playable target, Skill skill)
	{
		npc.setTarget(target);
		npc.doCast(skill);
	}

	protected void addSkillCastDesire(Npc npc, WorldObject target, SkillHolder skill, int desire)
	{
		this.addSkillCastDesire(npc, target, skill.getSkill(), desire);
	}

	protected void addSkillCastDesire(Npc npc, WorldObject target, Skill skill, int desire)
	{
		if (npc.isAttackable() && target != null && target.isCreature())
		{
			npc.asAttackable().addDamageHate(target.asCreature(), 0L, desire);
		}

		npc.setTarget((WorldObject) (target != null ? target : npc));
		npc.doCast(skill);
	}

	public static void specialCamera(Player player, Creature creature, int force, int angle1, int angle2, int time, int range, int duration, int relYaw, int relPitch, int isWide, int relAngle)
	{
		if (!player.isSimulatingTalking())
		{
			player.sendPacket(new SpecialCamera(creature, force, angle1, angle2, time, range, duration, relYaw, relPitch, isWide, relAngle));
		}
	}

	public static void specialCameraEx(Player player, Creature creature, int force, int angle1, int angle2, int time, int duration, int relYaw, int relPitch, int isWide, int relAngle)
	{
		if (!player.isSimulatingTalking())
		{
			player.sendPacket(new SpecialCamera(creature, player, force, angle1, angle2, time, duration, relYaw, relPitch, isWide, relAngle));
		}
	}

	public static void specialCamera3(Player player, Creature creature, int force, int angle1, int angle2, int time, int range, int duration, int relYaw, int relPitch, int isWide, int relAngle, int unk)
	{
		if (!player.isSimulatingTalking())
		{
			player.sendPacket(new SpecialCamera(creature, force, angle1, angle2, time, range, duration, relYaw, relPitch, isWide, relAngle, unk));
		}
	}

	public static void specialCamera(Instance world, Creature creature, int force, int angle1, int angle2, int time, int range, int duration, int relYaw, int relPitch, int isWide, int relAngle, int unk)
	{
		world.broadcastPacket(new SpecialCamera(creature, force, angle1, angle2, time, range, duration, relYaw, relPitch, isWide, relAngle, unk));
	}

	public static void addRadar(Player player, int x, int y, int z)
	{
		if (!player.isSimulatingTalking())
		{
			player.getRadar().addMarker(x, y, z);
		}
	}

	public void removeRadar(Player player, int x, int y, int z)
	{
		if (!player.isSimulatingTalking())
		{
			player.getRadar().removeMarker(x, y, z);
		}
	}

	public void clearRadar(Player player)
	{
		if (!player.isSimulatingTalking())
		{
			player.getRadar().removeAllMarkers();
		}
	}

	public void playMovie(Player player, Movie movie)
	{
		if (!player.isSimulatingTalking())
		{
			new MovieHolder(Arrays.asList(player), movie);
		}
	}

	public void playMovie(List<Player> players, Movie movie)
	{
		new MovieHolder(players, movie);
	}

	public void playMovie(Set<Player> players, Movie movie)
	{
		new MovieHolder(new ArrayList<>(players), movie);
	}

	public void playMovie(Instance instance, Movie movie)
	{
		if (instance != null)
		{
			for (Player player : instance.getPlayers())
			{
				if (player != null && player.getInstanceWorld() == instance)
				{
					this.playMovie(player, movie);
				}
			}
		}
	}

	public void giveStoryBuffReward(Player player)
	{
		if (GeneralConfig.ENABLE_STORY_QUEST_BUFF_REWARD)
		{
			for (SkillHolder holder : STORY_QUEST_BUFFS)
			{
				SkillCaster.triggerCast(player, player, holder.getSkill());
			}
		}
	}

	public NewQuest getQuestData()
	{
		return this._questData;
	}

	public void rewardPlayer(Player player)
	{
		NewQuestReward reward = this._questData.getRewards();
		List<ItemHolder> rewardItems = reward.getItems();
		if (rewardItems != null && !rewardItems.isEmpty())
		{
			for (ItemHolder item : rewardItems)
			{
				rewardItems(player, item);
			}
		}

		if (reward.getLevel() > 0)
		{
			long playerExp = player.getExp();
			long targetExp = ExperienceData.getInstance().getExpForLevel(reward.getLevel());
			if (playerExp < targetExp)
			{
				player.addExpAndSp(targetExp - playerExp, 0.0);
				player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
				player.setCurrentCp(player.getMaxCp());
				player.broadcastUserInfo();
			}
		}

		if (reward.getExp() > 0L)
		{
			player.getStat().addExp(reward.getExp());
			player.broadcastUserInfo();
		}

		if (reward.getSp() > 0L)
		{
			player.getStat().addSp(reward.getSp());
			player.broadcastUserInfo();
		}
	}

	public boolean teleportToQuestLocation(Player player, Location loc)
	{
		if (loc == null)
		{
			return false;
		}
		else if (player.isDead())
		{
			player.sendPacket(SystemMessageId.DEAD_CHARACTERS_CANNOT_USE_TELEPORTATION);
			return false;
		}
		else if (player.getMovieHolder() == null && !player.isFishing() && !player.isInInstance() && !player.isOnEvent() && !player.isInOlympiadMode() && !player.inObserverMode() && !player.isInTraingCamp() && !player.isInTimedHuntingZone())
		{
			if (PlayerConfig.TELEPORT_WHILE_PLAYER_IN_COMBAT || !player.isInCombat() && !player.isCastingNow())
			{
				if ((!PlayerConfig.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT || !PlayerConfig.ALT_GAME_KARMA_PLAYER_CAN_USE_GK) && player.getReputation() < 0)
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_RIGHT_NOW);
					return false;
				}
				else if (player.isAffected(EffectFlag.CANNOT_ESCAPE))
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_RIGHT_NOW);
					return false;
				}
				else
				{
					player.abortCast();
					player.stopMove(null);
					player.setTeleportLocation(loc);
					player.castTeleportSkill();
					return true;
				}
			}
			player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_WHILE_IN_COMBAT);
			return false;
		}
		else
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_RIGHT_NOW);
			return false;
		}
	}

	public void setType(int type)
	{
		this._questData.setType(type);
	}

	public void sendAcceptDialog(Player player)
	{
		ThreadPool.schedule(() -> player.sendPacket(new ExQuestDialog(this.getId(), QuestDialogType.ACCEPT)), 2000L);
	}

	public void sendEndDialog(Player player)
	{
		ThreadPool.schedule(() -> player.sendPacket(new ExQuestDialog(this.getId(), QuestDialogType.END)), 2000L);
	}
}
