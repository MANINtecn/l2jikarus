package net.sf.l2jdev.gameserver.model.siege;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.config.OrcFortressConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.data.xml.SpawnData;
import net.sf.l2jdev.gameserver.managers.FortManager;
import net.sf.l2jdev.gameserver.managers.FortSiegeManager;
import net.sf.l2jdev.gameserver.managers.ZoneManager;
import net.sf.l2jdev.gameserver.model.CombatFlag;
import net.sf.l2jdev.gameserver.model.FortSiegeSpawn;
import net.sf.l2jdev.gameserver.model.SiegeClan;
import net.sf.l2jdev.gameserver.model.Spawn;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.TeleportWhereType;
import net.sf.l2jdev.gameserver.model.actor.instance.Door;
import net.sf.l2jdev.gameserver.model.actor.instance.FortCommander;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.events.Containers;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.ListenersContainer;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerLogin;
import net.sf.l2jdev.gameserver.model.events.holders.sieges.OnFortSiegeFinish;
import net.sf.l2jdev.gameserver.model.events.holders.sieges.OnFortSiegeStart;
import net.sf.l2jdev.gameserver.model.events.listeners.ConsumerEventListener;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;
import net.sf.l2jdev.gameserver.model.spawns.NpcSpawnTemplate;
import net.sf.l2jdev.gameserver.model.spawns.SpawnGroup;
import net.sf.l2jdev.gameserver.model.spawns.SpawnTemplate;
import net.sf.l2jdev.gameserver.model.zone.ZoneType;
import net.sf.l2jdev.gameserver.network.NpcStringId;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.ChatType;
import net.sf.l2jdev.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.orcfortress.OrcFortressSiegeInfoHUD;
import net.sf.l2jdev.gameserver.util.Broadcast;

public class FortSiege extends ListenersContainer implements Siegable
{
	protected static final Logger LOGGER = Logger.getLogger(FortSiege.class.getName());
	public static final String ORC_FORTRESS_GREG_UPPER_LEFT_SPAWN = "orc_fortress_greg_upper_left";
	public static final String ORC_FORTRESS_GREG_UPPER_RIGHT_SPAWN = "orc_fortress_greg_upper_right";
	public static final String ORC_FORTRESS_GREG_BOTTOM_RIGHT_SPAWN = "orc_fortress_greg_bottom_right";
	public static final String GREG_SPAWN_VAR = "GREG_SPAWN";
	private static final AtomicReference<SpawnTemplate> SPAWN_PREPARATION_NPCS = new AtomicReference<>();
	private static final ZoneType FORTRESS_ZONE = ZoneManager.getInstance().getZoneByName("orc_fortress_general_area");
	private static final SkillHolder BUFF = new SkillHolder(52016, 1);
	private ScheduledFuture<?> _siegeGregSentryTask = null;
	private boolean _hasSpawnedPreparationNpcs = false;
	private int _flagCount = 0;
	protected static final String DELETE_FORT_SIEGECLANS_BY_CLAN_ID = "DELETE FROM fortsiege_clans WHERE fort_id = ? AND clan_id = ?";
	protected static final String DELETE_FORT_SIEGECLANS = "DELETE FROM fortsiege_clans WHERE fort_id = ?";
	private final Set<SiegeClan> _attackerClans = ConcurrentHashMap.newKeySet();
	protected Set<Spawn> _commanders = ConcurrentHashMap.newKeySet();
	protected final Fort _fort;
	boolean _isInProgress = false;
	private final Collection<Spawn> _siegeGuards = new LinkedList<>();
	ScheduledFuture<?> _siegeEnd = null;
	ScheduledFuture<?> _siegeRestore = null;
	ScheduledFuture<?> _siegeStartTask = null;
	boolean _isInPreparation = false;

	public FortSiege(Fort fort)
	{
		this._fort = fort;
		this.checkAutoTask();
		FortSiegeManager.getInstance().addSiege(this);
		if (this._fort.getResidenceId() == 122)
		{
			Containers.Global().addListener(new ConsumerEventListener(this, EventType.ON_FORT_SIEGE_START, event -> this.announceStartToPlayers((OnFortSiegeStart) event), this));
			Containers.Global().addListener(new ConsumerEventListener(this, EventType.ON_FORT_SIEGE_FINISH, event -> this.announceEndToPlayers((OnFortSiegeFinish) event), this));
			Containers.Global().addListener(new ConsumerEventListener(this, EventType.ON_PLAYER_LOGIN, event -> this.showHUDToPlayer((OnPlayerLogin) event), this));
		}
	}

	protected void announceStartToPlayers(OnFortSiegeStart event)
	{
		Broadcast.toAllOnlinePlayers(new OrcFortressSiegeInfoHUD(event.getSiege().getFort().getResidenceId(), 1, 0, 1800));
		Broadcast.toAllOnlinePlayers(new SystemMessage(SystemMessageId.SEAL_THE_SEAL_TOWER_AND_CONQUER_ORC_FORTRESS));
	}

	protected void announceEndToPlayers(OnFortSiegeFinish event)
	{
		Broadcast.toAllOnlinePlayers(new OrcFortressSiegeInfoHUD(event.getSiege().getFort().getResidenceId(), 0, 0, 0));
	}

	private void showHUDToPlayer(OnPlayerLogin event)
	{
		if (this._isInPreparation)
		{
			int remainingTimeInSeconds = (int) (this._fort.getSiegeDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis()) / 1000;
			event.getPlayer().sendPacket(new OrcFortressSiegeInfoHUD(this._fort.getResidenceId(), 0, (int) Calendar.getInstance().getTimeInMillis() / 1000, remainingTimeInSeconds));
		}
		else if (this._isInProgress)
		{
			int remainingTimeInSeconds = (int) this._siegeEnd.getDelay(TimeUnit.SECONDS);
			event.getPlayer().sendPacket(new OrcFortressSiegeInfoHUD(this._fort.getResidenceId(), 1, (int) Calendar.getInstance().getTimeInMillis() / 1000, remainingTimeInSeconds));
		}
	}

	@Override
	public void endSiege()
	{
		if (this._isInProgress)
		{
			if (this._siegeGregSentryTask != null)
			{
				this._siegeGregSentryTask.cancel(true);
				this._siegeGregSentryTask = null;
			}

			if (this._fort.getResidenceId() == 122)
			{
				for (Player player : World.getInstance().getPlayers())
				{
					Item weap = player.getActiveWeaponInstance();
					if (weap != null && weap.getId() == 93331)
					{
						FortSiegeManager.getInstance().dropCombatFlag(player, this.getFort().getResidenceId());

						for (Player member : player.getClan().getOnlineMembers(0))
						{
							BUFF.getSkill().applyEffects(member.asPlayer(), member.asPlayer());
						}
					}
				}

				for (WorldObject obj : World.getInstance().getVisibleObjects())
				{
					if (obj instanceof Item && obj.getId() == 93331)
					{
						obj.decayMe();
					}
				}

				SpawnData.getInstance().getSpawns().forEach(spawnTemplate -> spawnTemplate.getGroupsByName("orc_fortress").forEach(holder -> holder.despawnAll()));
				SpawnData.getInstance().getSpawns().forEach(spawnTemplate -> spawnTemplate.getGroupsByName("orc_runners").forEach(holder -> holder.despawnAll()));
				SpawnData.getInstance().getSpawns().forEach(spawnTemplate -> spawnTemplate.getGroupsByName("orc_fortress_inside").forEach(holder -> holder.despawnAll()));
				SpawnData.getInstance().getSpawns().forEach(spawnTemplate -> spawnTemplate.getGroupsByName("orc_fortress_jeras_guards").forEach(holder -> holder.despawnAll()));
				SpawnData.getInstance().getSpawns().forEach(spawnTemplate -> spawnTemplate.getGroupsByName("orc_fortress_greg_upper_left").forEach(holder -> holder.despawnAll()));
				SpawnData.getInstance().getSpawns().forEach(spawnTemplate -> spawnTemplate.getGroupsByName("orc_fortress_greg_upper_right").forEach(holder -> holder.despawnAll()));
				SpawnData.getInstance().getSpawns().forEach(spawnTemplate -> spawnTemplate.getGroupsByName("orc_fortress_greg_bottom_right").forEach(holder -> holder.despawnAll()));
				SPAWN_PREPARATION_NPCS.get().getGroups().forEach(SpawnGroup::despawnAll);
			}

			this._isInProgress = false;
			this._hasSpawnedPreparationNpcs = false;
			this.removeFlags();
			this.unSpawnFlags();
			this.teleportPlayer(FortTeleportWhoType.Attacker, TeleportWhereType.TOWN);
			this.updatePlayerSiegeStateFlags(true);
			int ownerId = -1;
			if (this._fort.getOwnerClan() != null)
			{
				ownerId = this._fort.getOwnerClan().getId();
			}

			this._fort.getZone().banishForeigners(ownerId);
			this._fort.getZone().setActive(false);
			this._fort.getZone().updateZoneStatusForCharactersInside();
			this._fort.getZone().setSiegeInstance(null);
			this.saveFortSiege();
			this.clearSiegeClan();
			this.removeCommanders();
			this._fort.spawnNpcCommanders();
			this.unspawnSiegeGuard();
			this._fort.closeOrcFortressDoors();
			if (this._fort.getResidenceId() != 122)
			{
				this._fort.resetDoors();
			}
			else
			{
				this._fort.closeOrcFortressDoors();
				LOGGER.info("FortSiege: Closed Orc Fortress doors.");
			}

			this._fort.setOrcFortressOwnerNpcs(true);
			ThreadPool.schedule(new FortSiege.ScheduleSuspiciousMerchantSpawn(), FortSiegeManager.getInstance().getSuspiciousMerchantRespawnDelay() * 60 * 1000);
			this.setSiegeDateTime(true);
			if (this._siegeEnd != null)
			{
				this._siegeEnd.cancel(true);
				this._siegeEnd = null;
			}

			if (this._siegeRestore != null)
			{
				this._siegeRestore.cancel(true);
				this._siegeRestore = null;
			}

			if (this._fort.getOwnerClan() != null && this._fort.getFlagPole().getMeshIndex() == 0)
			{
				this._fort.setVisibleFlag(true);
			}

			LOGGER.info(this.getClass().getSimpleName() + ": Siege of " + this._fort.getName() + " fort finished.");
			if (EventDispatcher.getInstance().hasListener(EventType.ON_FORT_SIEGE_FINISH, this.getFort()))
			{
				EventDispatcher.getInstance().notifyEventAsync(new OnFortSiegeFinish(this), this.getFort());
			}
		}
	}

	@Override
	public void startSiege()
	{
		if (!this._isInProgress)
		{
			if (this._siegeStartTask != null)
			{
				this._siegeStartTask.cancel(true);
				this._fort.despawnSuspiciousMerchant();
			}

			this._siegeStartTask = null;
			if (this._attackerClans.isEmpty() && this._fort.getResidenceId() != 122)
			{
				return;
			}

			this._isInProgress = true;
			this.loadSiegeClan();
			this.updatePlayerSiegeStateFlags(false);
			this.setFlagCount(0);
			this._fort.despawnNpcCommanders();
			this.spawnCommanders();
			if (this._fort.getResidenceId() != 122)
			{
				this._fort.resetDoors();
			}
			else
			{
				this._fort.openOrcFortressDoors();
				LOGGER.info("FortSiege: Opened Orc Fortress doors.");
			}

			this._fort.setOrcFortressOwnerNpcs(false);
			this.spawnSiegeGuard();
			this._fort.setVisibleFlag(false);
			this._fort.getZone().setSiegeInstance(this);
			this._fort.getZone().setActive(true);
			this._fort.getZone().updateZoneStatusForCharactersInside();
			if (this._fort.getResidenceId() == 122)
			{
				this._siegeGregSentryTask = ThreadPool.schedule(new FortSiege.ScheduleGregSentrySpawnTask(), 1200000L);
				this._siegeEnd = ThreadPool.schedule(new FortSiege.ScheduleEndSiegeTask(), 1800000L);
			}
			else
			{
				this._siegeEnd = ThreadPool.schedule(new FortSiege.ScheduleEndSiegeTask(), FortSiegeManager.getInstance().getSiegeLength() * 60 * 1000);
			}

			this.announceToPlayer(new SystemMessage(SystemMessageId.THE_FORTRESS_BATTLE_HAS_BEGUN));
			this.saveFortSiege();
			if (this._fort.getResidenceId() == 122)
			{
				SpawnData.getInstance().getSpawns().forEach(spawnTemplate -> spawnTemplate.getGroupsByName("orc_fortress").forEach(holder -> {
					holder.spawnAll();

					for (NpcSpawnTemplate nst : holder.getSpawns())
					{
						for (Npc npc : nst.getSpawnedNpcs())
						{
							Spawn spawn = npc.getSpawn();
							if (spawn != null)
							{
								spawn.setRespawnDelay(5);
							}
						}
					}
				}));
				SpawnData.getInstance().getSpawns().forEach(spawnTemplate -> spawnTemplate.getGroupsByName("orc_runners").forEach(holder -> {
					holder.spawnAll();

					for (NpcSpawnTemplate nst : holder.getSpawns())
					{
						for (Npc npc : nst.getSpawnedNpcs())
						{
							Spawn spawn = npc.getSpawn();
							if (spawn != null)
							{
								spawn.setRespawnDelay(5, 10);
							}
						}
					}
				}));
				SpawnData.getInstance().getSpawns().forEach(spawnTemplate -> spawnTemplate.getGroupsByName("orc_fortress_inside").forEach(holder -> {
					holder.spawnAll();

					for (NpcSpawnTemplate nst : holder.getSpawns())
					{
						for (Npc npc : nst.getSpawnedNpcs())
						{
							Spawn spawn = npc.getSpawn();
							if (spawn != null)
							{
								spawn.setRespawnDelay(5);
							}
						}
					}
				}));
			}

			SpawnData.getInstance().getSpawns().forEach(spawnTemplate -> spawnTemplate.getGroupsByName("orc_fortress_jeras_guards").forEach(holder -> {
				holder.spawnAll();

				for (NpcSpawnTemplate nst : holder.getSpawns())
				{
					for (Npc npc : nst.getSpawnedNpcs())
					{
						Spawn spawn = npc.getSpawn();
						if (spawn != null)
						{
							spawn.setRespawnDelay(160);
						}
					}
				}
			}));
		}

		LOGGER.info(this.getClass().getSimpleName() + ": Siege of " + this._fort.getName() + " fort started.");
		if (EventDispatcher.getInstance().hasListener(EventType.ON_FORT_SIEGE_START, this.getFort()))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnFortSiegeStart(this), this.getFort());
		}
	}

	public void announceToPlayer(SystemMessage sm)
	{
		for (SiegeClan siegeclan : this._attackerClans)
		{
			Clan clan = ClanTable.getInstance().getClan(siegeclan.getClanId());

			for (Player member : clan.getOnlineMembers(0))
			{
				if (member != null)
				{
					member.sendPacket(sm);
				}
			}
		}

		if (this._fort.getOwnerClan() != null)
		{
			Clan clan = ClanTable.getInstance().getClan(this.getFort().getOwnerClan().getId());

			for (Player memberx : clan.getOnlineMembers(0))
			{
				if (memberx != null)
				{
					memberx.sendPacket(sm);
				}
			}
		}
	}

	public void announceToPlayer(SystemMessage sm, String s)
	{
		sm.addString(s);
		this.announceToPlayer(sm);
	}

	public void updatePlayerSiegeStateFlags(boolean clear)
	{
		for (SiegeClan siegeclan : this._attackerClans)
		{
			Clan clan = ClanTable.getInstance().getClan(siegeclan.getClanId());

			for (Player member : clan.getOnlineMembers(0))
			{
				if (member != null)
				{
					if (clear)
					{
						member.setSiegeState((byte) 0);
						member.setSiegeSide(0);
						member.setInSiege(false);
						member.stopFameTask();
					}
					else
					{
						member.setSiegeState((byte) 1);
						member.setSiegeSide(this._fort.getResidenceId());
						if (this.checkIfInZone(member))
						{
							member.setInSiege(true);
							member.startFameTask(PlayerConfig.FORTRESS_ZONE_FAME_TASK_FREQUENCY * 1000, PlayerConfig.FORTRESS_ZONE_FAME_AQUIRE_POINTS);
						}
					}

					member.broadcastUserInfo();
				}
			}
		}

		if (this._fort.getOwnerClan() != null)
		{
			Clan clan = ClanTable.getInstance().getClan(this.getFort().getOwnerClan().getId());

			for (Player memberx : clan.getOnlineMembers(0))
			{
				if (memberx != null)
				{
					if (clear)
					{
						memberx.setSiegeState((byte) 0);
						memberx.setSiegeSide(0);
						memberx.setInSiege(false);
						memberx.stopFameTask();
					}
					else
					{
						memberx.setSiegeState((byte) 2);
						memberx.setSiegeSide(this._fort.getResidenceId());
						if (this.checkIfInZone(memberx))
						{
							memberx.setInSiege(true);
							memberx.startFameTask(PlayerConfig.FORTRESS_ZONE_FAME_TASK_FREQUENCY * 1000, PlayerConfig.FORTRESS_ZONE_FAME_AQUIRE_POINTS);
						}
					}

					memberx.broadcastUserInfo();
				}
			}
		}
	}

	public boolean checkIfInZone(WorldObject object)
	{
		return this.checkIfInZone(object.getX(), object.getY(), object.getZ());
	}

	public boolean checkIfInZone(int x, int y, int z)
	{
		return this._isInProgress && this._fort.checkIfInZone(x, y, z);
	}

	@Override
	public boolean checkIsAttacker(Clan clan)
	{
		return this.getAttackerClan(clan) != null;
	}

	@Override
	public boolean checkIsDefender(Clan clan)
	{
		return clan != null && this._fort.getOwnerClan() == clan;
	}

	public void clearSiegeClan()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM fortsiege_clans WHERE fort_id=?");)
		{
			ps.setInt(1, this._fort.getResidenceId());
			ps.execute();
			if (this._fort.getOwnerClan() != null)
			{
				try (PreparedStatement delete = con.prepareStatement("DELETE FROM fortsiege_clans WHERE clan_id=?"))
				{
					delete.setInt(1, this._fort.getOwnerClan().getId());
					delete.execute();
				}
			}

			this._attackerClans.clear();
			if (this._isInProgress)
			{
				this.endSiege();
			}

			if (this._siegeStartTask != null)
			{
				this._siegeStartTask.cancel(true);
				this._siegeStartTask = null;
			}
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception: clearSiegeClan(): " + var12.getMessage(), var12);
		}
	}

	private void clearSiegeDate()
	{
		this._fort.getSiegeDate().setTimeInMillis(0L);
	}

	@Override
	public List<Player> getAttackersInZone()
	{
		List<Player> players = new LinkedList<>();

		for (SiegeClan siegeclan : this._attackerClans)
		{
			Clan clan = ClanTable.getInstance().getClan(siegeclan.getClanId());

			for (Player player : clan.getOnlineMembers(0))
			{
				if (player != null && player.isInSiege())
				{
					players.add(player);
				}
			}
		}

		return players;
	}

	public List<Player> getPlayersInZone()
	{
		return this._fort.getZone().getPlayersInside();
	}

	public List<Player> getOwnersInZone()
	{
		List<Player> players = new LinkedList<>();
		if (this._fort.getOwnerClan() != null)
		{
			Clan clan = ClanTable.getInstance().getClan(this.getFort().getOwnerClan().getId());
			if (clan != this._fort.getOwnerClan())
			{
				return null;
			}

			for (Player player : clan.getOnlineMembers(0))
			{
				if (player != null && player.isInSiege())
				{
					players.add(player);
				}
			}
		}

		return players;
	}

	public void killedCommander(FortCommander instance)
	{
		int RisidenceId = this._fort.getResidenceId();
		if (this._fort != null && !this._commanders.isEmpty() && RisidenceId != 122)
		{
			Spawn spawn = instance.getSpawn();
			if (spawn != null)
			{
				for (FortSiegeSpawn spawn2 : FortSiegeManager.getInstance().getCommanderSpawnList(this.getFort().getResidenceId()))
				{
					if (spawn2.getId() == spawn.getId())
					{
						NpcStringId npcString = null;
						switch (spawn2.getMessageId())
						{
							case 1:
								npcString = NpcStringId.YOU_MAY_HAVE_BROKEN_OUR_ARROWS_BUT_YOU_WILL_NEVER_BREAK_OUR_WILL_ARCHERS_RETREAT;
								break;
							case 2:
								npcString = NpcStringId.AIIEEEE_COMMAND_CENTER_THIS_IS_GUARD_UNIT_WE_NEED_BACKUP_RIGHT_AWAY;
								break;
							case 3:
								npcString = NpcStringId.AT_LAST_THE_MAGIC_CIRCLE_THAT_PROTECTS_THE_FORTRESS_HAS_WEAKENED_VOLUNTEERS_STAND_BACK;
								break;
							case 4:
								npcString = NpcStringId.I_FEEL_SO_MUCH_GRIEF_THAT_I_CAN_T_EVEN_TAKE_CARE_OF_MYSELF_THERE_ISN_T_ANY_REASON_FOR_ME_TO_STAY_HERE_ANY_LONGER;
						}

						if (npcString != null)
						{
							instance.broadcastSay(ChatType.NPC_SHOUT, npcString);
						}
					}
				}

				this._commanders.remove(spawn);
				if (this._commanders.isEmpty())
				{
					this.spawnFlag(this._fort.getResidenceId());
					if (this._siegeRestore != null)
					{
						this._siegeRestore.cancel(true);
					}

					for (Door door : this._fort.getDoors())
					{
						if (!door.isShowHp())
						{
							door.openMe();
						}
					}

					this._fort.getSiege().announceToPlayer(new SystemMessage(SystemMessageId.ALL_BARRACKS_ARE_OCCUPIED));
				}
				else if (this._siegeRestore == null)
				{
					this._fort.getSiege().announceToPlayer(new SystemMessage(SystemMessageId.THE_BARRACKS_HAVE_BEEN_SEIZED));
					this._siegeRestore = ThreadPool.schedule(new FortSiege.ScheduleSiegeRestore(), FortSiegeManager.getInstance().getCountDownLength() * 60 * 1000);
				}
				else
				{
					this._fort.getSiege().announceToPlayer(new SystemMessage(SystemMessageId.THE_BARRACKS_HAVE_BEEN_SEIZED));
				}
			}
			else
			{
				LOGGER.warning(this.getClass().getSimpleName() + ": FortSiege.killedCommander(): killed commander, but commander not registered for fortress. NpcId: " + instance.getId() + " FortId: " + this._fort.getResidenceId());
			}
		}
	}

	public void killedFlag(Npc flag)
	{
		if (flag != null)
		{
			for (SiegeClan clan : this._attackerClans)
			{
				if (clan.removeFlag(flag))
				{
					return;
				}
			}
		}
	}

	public int addAttacker(Player player, boolean checkConditions)
	{
		Clan clan = player.getClan();
		if (clan == null)
		{
			return 0;
		}
		if (checkConditions)
		{
			if (this._fort.getSiege().getAttackerClans().isEmpty() && player.getInventory().getAdena() < 250000L)
			{
				return 1;
			}

			for (Fort fort : FortManager.getInstance().getForts())
			{
				if ((fort.getSiege().getAttackerClan(player.getClanId()) != null) || (fort.getOwnerClan() == clan && (fort.getSiege().isInProgress() || fort.getSiege()._siegeStartTask != null)))
				{
					return 3;
				}
			}
		}

		this.saveSiegeClan(clan);
		if (this._attackerClans.size() == 1)
		{
			if (checkConditions)
			{
				player.reduceAdena(ItemProcessType.FEE, 250000L, null, true);
			}

			this.startAutoTask(true);
		}

		return 4;
	}

	public void removeAttacker(Clan clan)
	{
		if (clan != null && clan.getFortId() != this.getFort().getResidenceId() && FortSiegeManager.getInstance().checkIsRegistered(clan, this.getFort().getResidenceId()))
		{
			this.removeSiegeClan(clan.getId());
		}
	}

	private void removeSiegeClan(int clanId)
	{
		String query = clanId != 0 ? "DELETE FROM fortsiege_clans WHERE fort_id = ? AND clan_id = ?" : "DELETE FROM fortsiege_clans WHERE fort_id = ?";

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement(query);)
		{
			statement.setInt(1, this._fort.getResidenceId());
			if (clanId != 0)
			{
				statement.setInt(2, clanId);
			}

			statement.execute();
			this.loadSiegeClan();
			if (this._attackerClans.isEmpty())
			{
				if (this._isInProgress)
				{
					this.endSiege();
				}
				else
				{
					this.saveFortSiege();
				}

				if (this._siegeStartTask != null)
				{
					this._siegeStartTask.cancel(true);
					this._siegeStartTask = null;
				}
			}
		}
		catch (Exception var11)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception on removeSiegeClan: " + var11.getMessage(), var11);
		}
	}

	public void checkAutoTask()
	{
		if (this._siegeStartTask == null)
		{
			if (this._fort.getResidenceId() == 122 && OrcFortressConfig.ORC_FORTRESS_ENABLE)
			{
				if (this._siegeStartTask != null)
				{
					return;
				}

				ThreadPool.execute(new FortSiege.ScheduleSuspiciousMerchantSpawn());
				Calendar cal = Calendar.getInstance();
				cal.set(11, OrcFortressConfig.ORC_FORTRESS_HOUR);
				cal.set(12, OrcFortressConfig.ORC_FORTRESS_MINUTE);
				cal.set(13, 0);
				cal.set(14, 0);
				if (cal.getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
				{
					cal.add(5, 1);
				}

				this._fort.setSiegeDate(cal);
				this.saveSiegeDate();
				long initialDelayInMilliseconds = this._fort.getSiegeDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
				this._siegeStartTask = ThreadPool.schedule(new FortSiege.ScheduleStartSiegeTask(0, initialDelayInMilliseconds), 0L);
			}
			else
			{
				long delay = this.getFort().getSiegeDate().getTimeInMillis() - System.currentTimeMillis();
				if (delay < 0L)
				{
					this.saveFortSiege();
					this.clearSiegeClan();
					ThreadPool.execute(new FortSiege.ScheduleSuspiciousMerchantSpawn());
				}
				else
				{
					this.loadSiegeClan();
					if (this._attackerClans.isEmpty())
					{
						ThreadPool.schedule(new FortSiege.ScheduleSuspiciousMerchantSpawn(), delay);
					}
					else
					{
						if (delay > 3600000L)
						{
							ThreadPool.execute(new FortSiege.ScheduleSuspiciousMerchantSpawn());
							this._siegeStartTask = ThreadPool.schedule(new FortSiege.ScheduleStartSiegeTask(3600), delay - 3600000L);
						}

						if (delay > 600000L)
						{
							ThreadPool.execute(new FortSiege.ScheduleSuspiciousMerchantSpawn());
							this._siegeStartTask = ThreadPool.schedule(new FortSiege.ScheduleStartSiegeTask(600), delay - 600000L);
						}
						else if (delay > 300000L)
						{
							this._siegeStartTask = ThreadPool.schedule(new FortSiege.ScheduleStartSiegeTask(300), delay - 300000L);
						}
						else if (delay > 60000L)
						{
							this._siegeStartTask = ThreadPool.schedule(new FortSiege.ScheduleStartSiegeTask(60), delay - 60000L);
						}
						else
						{
							this._siegeStartTask = ThreadPool.schedule(new FortSiege.ScheduleStartSiegeTask(60), 0L);
						}

						LOGGER.info(this.getClass().getSimpleName() + ": Siege of " + this._fort.getName() + " fort: " + this._fort.getSiegeDate().getTime());
					}
				}
			}
		}
	}

	public void startAutoTask(boolean setTime)
	{
		if (this._siegeStartTask == null)
		{
			if (setTime)
			{
				this.setSiegeDateTime(false);
			}

			if (this._fort.getOwnerClan() != null)
			{
				this._fort.getOwnerClan().broadcastToOnlineMembers(new SystemMessage(SystemMessageId.A_FORTRESS_IS_UNDER_ATTACK));
			}

			this._siegeStartTask = ThreadPool.schedule(new FortSiege.ScheduleStartSiegeTask(3600), 0L);
		}
	}

	public void teleportPlayer(FortTeleportWhoType teleportWho, TeleportWhereType teleportWhere)
	{
		List<Player> players = switch (teleportWho)
		{
			case All -> this.getPlayersInZone();
			case Attacker -> this.getAttackersInZone();
			case Owner -> this.getOwnersInZone();
		};
		if (players != null)
		{
			for (Player player : players)
			{
				if (!player.isGM() && !player.isJailed())
				{
					player.teleToLocation(teleportWhere);
				}
			}
		}
	}

	private void addAttacker(int clanId)
	{
		this._attackerClans.add(new SiegeClan(clanId, SiegeClanType.ATTACKER));
	}

	public boolean checkIfAlreadyRegisteredForSameDay(Clan clan)
	{
		for (FortSiege siege : FortSiegeManager.getInstance().getSieges())
		{
			if (siege != this && siege.getSiegeDate().get(7) == this.getSiegeDate().get(7))
			{
				if (siege.checkIsAttacker(clan) || siege.checkIsDefender(clan))
				{
					return true;
				}
			}
		}

		return false;
	}

	private void setSiegeDateTime(boolean merchant)
	{
		Calendar newDate = Calendar.getInstance();
		if (merchant)
		{
			newDate.add(12, FortSiegeManager.getInstance().getSuspiciousMerchantRespawnDelay());
		}
		else
		{
			newDate.add(12, 60);
		}

		this._fort.setSiegeDate(newDate);
		this.saveSiegeDate();
	}

	private void loadSiegeClan()
	{
		this._attackerClans.clear();

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT clan_id FROM fortsiege_clans WHERE fort_id=?");)
		{
			ps.setInt(1, this._fort.getResidenceId());

			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					this.addAttacker(rs.getInt("clan_id"));
				}
			}
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception: loadSiegeClan(): " + var12.getMessage(), var12);
		}
	}

	private void removeCommanders()
	{
		for (Spawn spawn : this._commanders)
		{
			if (spawn != null)
			{
				spawn.stopRespawn();
				if (spawn.getLastSpawn() != null)
				{
					spawn.getLastSpawn().deleteMe();
				}
			}
		}

		this._commanders.clear();
	}

	private void removeFlags()
	{
		for (SiegeClan sc : this._attackerClans)
		{
			if (sc != null)
			{
				sc.removeFlags();
			}
		}
	}

	private void saveFortSiege()
	{
		this.clearSiegeDate();
		this.saveSiegeDate();
	}

	private void saveSiegeDate()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE fort SET siegeDate = ? WHERE id = ?");)
		{
			ps.setLong(1, this._fort.getSiegeDate().getTimeInMillis());
			ps.setInt(2, this._fort.getResidenceId());
			ps.execute();
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception: saveSiegeDate(): " + var9.getMessage(), var9);
		}
	}

	private void saveSiegeClan(Clan clan)
	{
		if (this.getAttackerClans().size() < FortSiegeManager.getInstance().getAttackerMaxClans())
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("INSERT INTO fortsiege_clans (clan_id,fort_id) values (?,?)");)
			{
				statement.setInt(1, clan.getId());
				statement.setInt(2, this._fort.getResidenceId());
				statement.execute();
				this.addAttacker(clan.getId());
			}
			catch (Exception var10)
			{
				LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception: saveSiegeClan(Pledge clan): " + var10.getMessage(), var10);
			}
		}
	}

	private void spawnCommanders()
	{
		try
		{
			this._commanders.clear();

			for (FortSiegeSpawn _sp : FortSiegeManager.getInstance().getCommanderSpawnList(this.getFort().getResidenceId()))
			{
				Spawn spawnDat = new Spawn(_sp.getId());
				spawnDat.setAmount(1);
				spawnDat.setXYZ(_sp.getLocation());
				spawnDat.setHeading(_sp.getLocation().getHeading());
				spawnDat.setRespawnDelay(60);
				spawnDat.doSpawn(false);
				spawnDat.stopRespawn();
				this._commanders.add(spawnDat);
			}
		}
		catch (Exception var4)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": FortSiege.spawnCommander: Spawn could not be initialized: " + var4.getMessage(), var4);
		}
	}

	protected void spawnFlag(int id)
	{
		for (CombatFlag cf : FortSiegeManager.getInstance().getFlagList(id))
		{
			cf.spawnMe();
		}
	}

	private void unSpawnFlags()
	{
		if (FortSiegeManager.getInstance().getFlagList(this.getFort().getResidenceId()) != null)
		{
			for (CombatFlag cf : FortSiegeManager.getInstance().getFlagList(this.getFort().getResidenceId()))
			{
				cf.unSpawnMe();
			}
		}
	}

	public void addFlagCount(int count)
	{
		this._flagCount += count;
		this._flagCount = this._flagCount < 0 ? 0 : this._flagCount;
	}

	public int getFlagCount()
	{
		return this._flagCount;
	}

	public void setFlagCount(int count)
	{
		this._flagCount = 0;
	}

	public void loadSiegeGuard()
	{
		this._siegeGuards.clear();

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT npcId, x, y, z, heading, respawnDelay FROM fort_siege_guards WHERE fortId = ?");)
		{
			int fortId = this._fort.getResidenceId();
			ps.setInt(1, fortId);

			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					Spawn spawn = new Spawn(rs.getInt("npcId"));
					spawn.setAmount(1);
					spawn.setXYZ(rs.getInt("x"), rs.getInt("y"), rs.getInt("z"));
					spawn.setHeading(rs.getInt("heading"));
					spawn.setRespawnDelay(rs.getInt("respawnDelay"));
					spawn.setLocationId(0);
					this._siegeGuards.add(spawn);
				}
			}
		}
		catch (Exception var13)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Error loading siege guard for fort " + this._fort.getName() + ": " + var13.getMessage(), var13);
		}
	}

	private void spawnSiegeGuard()
	{
		try
		{
			for (Spawn spawnDat : this._siegeGuards)
			{
				spawnDat.doSpawn(false);
				if (spawnDat.getRespawnDelay() == 0)
				{
					spawnDat.stopRespawn();
				}
				else
				{
					spawnDat.startRespawn();
				}
			}
		}
		catch (Exception var3)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Error spawning siege guards for fort " + this._fort.getName() + ":" + var3.getMessage(), var3);
		}
	}

	private void unspawnSiegeGuard()
	{
		try
		{
			for (Spawn spawnDat : this._siegeGuards)
			{
				spawnDat.stopRespawn();
				if (spawnDat.getLastSpawn() != null)
				{
					spawnDat.getLastSpawn().doDie(spawnDat.getLastSpawn());
				}
			}
		}
		catch (Exception var3)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Error unspawning siege guards for fort " + this._fort.getName() + ":" + var3.getMessage(), var3);
		}
	}

	@Override
	public SiegeClan getAttackerClan(Clan clan)
	{
		return clan == null ? null : this.getAttackerClan(clan.getId());
	}

	@Override
	public SiegeClan getAttackerClan(int clanId)
	{
		for (SiegeClan sc : this._attackerClans)
		{
			if (sc != null && sc.getClanId() == clanId)
			{
				return sc;
			}
		}

		return null;
	}

	@Override
	public Collection<SiegeClan> getAttackerClans()
	{
		return this._attackerClans;
	}

	public Fort getFort()
	{
		return this._fort;
	}

	public boolean isInProgress()
	{
		return this._isInProgress;
	}

	@Override
	public Calendar getSiegeDate()
	{
		return this._fort.getSiegeDate();
	}

	@Override
	public Set<Npc> getFlag(Clan clan)
	{
		if (clan != null)
		{
			SiegeClan sc = this.getAttackerClan(clan);
			if (sc != null)
			{
				return sc.getFlag();
			}
		}

		return null;
	}

	public void resetSiege()
	{
		this.removeCommanders();
		this.spawnCommanders();
		this._fort.resetDoors();
	}

	public Set<Spawn> getCommanders()
	{
		return this._commanders;
	}

	@Override
	public SiegeClan getDefenderClan(int clanId)
	{
		return null;
	}

	@Override
	public SiegeClan getDefenderClan(Clan clan)
	{
		return null;
	}

	@Override
	public List<SiegeClan> getDefenderClans()
	{
		return null;
	}

	@Override
	public boolean giveFame()
	{
		return true;
	}

	@Override
	public int getFameFrequency()
	{
		return PlayerConfig.FORTRESS_ZONE_FAME_TASK_FREQUENCY;
	}

	@Override
	public int getFameAmount()
	{
		return PlayerConfig.FORTRESS_ZONE_FAME_AQUIRE_POINTS;
	}

	@Override
	public void updateSiege()
	{
	}

	public class ScheduleEndSiegeTask implements Runnable
	{
		public ScheduleEndSiegeTask()
		{
			Objects.requireNonNull(FortSiege.this);
			super();
		}

		@Override
		public void run()
		{
			if (FortSiege.this._isInProgress)
			{
				try
				{
					FortSiege.this._siegeEnd = null;
					FortSiege.this.endSiege();
				}
				catch (Exception var2)
				{
					FortSiege.LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception: ScheduleEndSiegeTask() for Fort: " + FortSiege.this._fort.getName() + " " + var2.getMessage(), var2);
				}
			}
		}
	}

	public class ScheduleGregSentrySpawnTask implements Runnable
	{
		public ScheduleGregSentrySpawnTask()
		{
			Objects.requireNonNull(FortSiege.this);
			super();
		}

		@Override
		public void run()
		{
			FortSiege.FORTRESS_ZONE.broadcastPacket(new ExShowScreenMessage(2, -1, 2, 0, 0, 0, 0, true, 8000, false, null, NpcStringId.FLAG_SENTRY_GREG_HAS_APPEARED, null));
			if (FortSiege.this._isInProgress)
			{
				try
				{
					SpawnData.getInstance().getSpawns().forEach(spawnTemplate -> spawnTemplate.getGroupsByName("orc_fortress_greg_upper_left").forEach(holder -> {
						holder.spawnAll();

						for (NpcSpawnTemplate nst : holder.getSpawns())
						{
							for (Npc npc : nst.getSpawnedNpcs())
							{
								Spawn spawn = npc.getSpawn();
								if (spawn != null)
								{
									spawn.stopRespawn();
								}
							}
						}
					}));
					SpawnData.getInstance().getSpawns().forEach(spawnTemplate -> spawnTemplate.getGroupsByName("orc_fortress_greg_upper_right").forEach(holder -> {
						holder.spawnAll();

						for (NpcSpawnTemplate nst : holder.getSpawns())
						{
							for (Npc npc : nst.getSpawnedNpcs())
							{
								Spawn spawn = npc.getSpawn();
								if (spawn != null)
								{
									spawn.stopRespawn();
								}
							}
						}
					}));
					SpawnData.getInstance().getSpawns().forEach(spawnTemplate -> spawnTemplate.getGroupsByName("orc_fortress_greg_bottom_right").forEach(holder -> {
						holder.spawnAll();

						for (NpcSpawnTemplate nst : holder.getSpawns())
						{
							for (Npc npc : nst.getSpawnedNpcs())
							{
								Spawn spawn = npc.getSpawn();
								if (spawn != null)
								{
									spawn.stopRespawn();
								}
							}
						}
					}));
				}
				catch (Exception var2)
				{
					FortSiege.LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception: ScheduleGregSentrySpawn() for Fort: " + FortSiege.this._fort.getName() + " " + var2.getMessage(), var2);
				}
			}
		}
	}

	public class ScheduleSiegeRestore implements Runnable
	{
		public ScheduleSiegeRestore()
		{
			Objects.requireNonNull(FortSiege.this);
			super();
		}

		@Override
		public void run()
		{
			if (FortSiege.this._isInProgress)
			{
				try
				{
					FortSiege.this._siegeRestore = null;
					FortSiege.this.resetSiege();
					FortSiege.this.announceToPlayer(new SystemMessage(SystemMessageId.THE_BARRACKS_FUNCTION_HAS_BEEN_RESTORED));
				}
				catch (Exception var2)
				{
					FortSiege.LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception: ScheduleSiegeRestore() for Fort: " + FortSiege.this._fort.getName() + " " + var2.getMessage(), var2);
				}
			}
		}
	}

	public class ScheduleStartSiegeTask implements Runnable
	{
		private final Fort _fortInst;
		private final int _time;
		private final long _initialDelayInMilliseconds;

		public ScheduleStartSiegeTask(int time)
		{
			Objects.requireNonNull(FortSiege.this);
			super();
			this._fortInst = FortSiege.this._fort;
			this._time = time;
			this._initialDelayInMilliseconds = 0L;
		}

		public ScheduleStartSiegeTask(int time, long initialDelayInMilliseconds)
		{
			Objects.requireNonNull(FortSiege.this);
			super();
			this._fortInst = FortSiege.this._fort;
			this._time = time;
			this._initialDelayInMilliseconds = initialDelayInMilliseconds;
		}

		@Override
		public void run()
		{
			if (!FortSiege.this._isInProgress)
			{
				if (!FortSiege.this._hasSpawnedPreparationNpcs)
				{
					FortSiege.this._hasSpawnedPreparationNpcs = true;
					FortSiege.SPAWN_PREPARATION_NPCS.set(SpawnData.getInstance().getSpawns().stream().filter(t -> t.getName() != null).filter(t -> t.getName().contains("orc_fortress_preparation_npcs")).findAny().orElse(null));
					FortSiege.SPAWN_PREPARATION_NPCS.get().getGroups().forEach(SpawnGroup::spawnAll);
				}

				try
				{
					if (this._initialDelayInMilliseconds != 0L && this._fortInst.getResidenceId() == 122)
					{
						short var9;
						if (this._initialDelayInMilliseconds >= 1200000L)
						{
							var9 = 1200;
						}
						else
						{
							FortSiege.this._isInPreparation = true;
							if (this._initialDelayInMilliseconds >= 600000L)
							{
								var9 = 600;
							}
							else if (this._initialDelayInMilliseconds >= 300000L)
							{
								var9 = 300;
							}
							else if (this._initialDelayInMilliseconds >= 60000L)
							{
								var9 = 60;
							}
							else if (this._initialDelayInMilliseconds >= 30000L)
							{
								var9 = 30;
							}
							else if (this._initialDelayInMilliseconds >= 10000L)
							{
								var9 = 10;
							}
							else if (this._initialDelayInMilliseconds >= 5000L)
							{
								var9 = 5;
							}
							else
							{
								var9 = 0;
							}

							Broadcast.toAllOnlinePlayers(new OrcFortressSiegeInfoHUD(this._fortInst.getResidenceId(), 0, (int) Calendar.getInstance().getTimeInMillis() / 1000, (int) this._initialDelayInMilliseconds / 1000));
						}

						ThreadPool.schedule(FortSiege.this.new ScheduleStartSiegeTask(var9), this._initialDelayInMilliseconds - var9 * 1000);
					}
					else if (this._time == 3600 && this._fortInst.getResidenceId() != 122)
					{
						ThreadPool.schedule(FortSiege.this.new ScheduleStartSiegeTask(600), 3000000L);
					}
					else if (this._time == 1200 && this._fortInst.getResidenceId() == 122)
					{
						FortSiege.this._isInPreparation = true;
						SystemMessage sm = new SystemMessage(SystemMessageId.THE_FORTRESS_BATTLE_STARTS_IN_S1_MIN);
						sm.addInt(20);
						FortSiege.this.announceToPlayer(sm);
						Broadcast.toAllOnlinePlayers(new OrcFortressSiegeInfoHUD(this._fortInst.getResidenceId(), 0, (int) Calendar.getInstance().getTimeInMillis() / 1000, 1200));
						ThreadPool.schedule(FortSiege.this.new ScheduleStartSiegeTask(600), 600000L);
					}
					else if (this._time == 600)
					{
						FortSiege.this._fort.despawnSuspiciousMerchant();
						SystemMessage sm = new SystemMessage(SystemMessageId.THE_FORTRESS_BATTLE_STARTS_IN_S1_MIN);
						sm.addInt(10);
						FortSiege.this.announceToPlayer(sm);
						ThreadPool.schedule(FortSiege.this.new ScheduleStartSiegeTask(300), 300000L);
					}
					else if (this._time == 300)
					{
						if (this._fortInst.getResidenceId() == 122)
						{
							FortSiege.this._fort.despawnSuspiciousMerchant();
						}

						SystemMessage sm = new SystemMessage(SystemMessageId.THE_FORTRESS_BATTLE_STARTS_IN_S1_MIN);
						sm.addInt(5);
						FortSiege.this.announceToPlayer(sm);
						ThreadPool.schedule(FortSiege.this.new ScheduleStartSiegeTask(60), 240000L);
					}
					else if (this._time == 60)
					{
						if (this._fortInst.getResidenceId() == 122)
						{
							FortSiege.this._fort.despawnSuspiciousMerchant();
						}

						SystemMessage sm = new SystemMessage(SystemMessageId.THE_FORTRESS_BATTLE_STARTS_IN_S1_MIN);
						sm.addInt(1);
						FortSiege.this.announceToPlayer(sm);
						ThreadPool.schedule(FortSiege.this.new ScheduleStartSiegeTask(30), 30000L);
					}
					else if (this._time == 30)
					{
						if (this._fortInst.getResidenceId() == 122)
						{
							FortSiege.this._fort.despawnSuspiciousMerchant();
						}

						SystemMessage sm = new SystemMessage(SystemMessageId.THE_FORTRESS_BATTLE_STARTS_IN_S1_SEC);
						sm.addInt(30);
						FortSiege.this.announceToPlayer(sm);
						ThreadPool.schedule(FortSiege.this.new ScheduleStartSiegeTask(10), 20000L);
					}
					else if (this._time == 10)
					{
						if (this._fortInst.getResidenceId() == 122)
						{
							FortSiege.this._fort.despawnSuspiciousMerchant();
						}

						SystemMessage sm = new SystemMessage(SystemMessageId.THE_FORTRESS_BATTLE_STARTS_IN_S1_SEC);
						sm.addInt(10);
						FortSiege.this.announceToPlayer(sm);
						ThreadPool.schedule(FortSiege.this.new ScheduleStartSiegeTask(5), 5000L);
					}
					else if (this._time == 5)
					{
						if (this._fortInst.getResidenceId() == 122)
						{
							FortSiege.this._fort.despawnSuspiciousMerchant();
						}

						ThreadPool.schedule(FortSiege.this.new ScheduleStartSiegeTask(1), 4000L);
					}
					else if (this._time == 1)
					{
						if (this._fortInst.getResidenceId() == 122)
						{
							FortSiege.this._fort.despawnSuspiciousMerchant();
						}

						ThreadPool.schedule(FortSiege.this.new ScheduleStartSiegeTask(0), 1000L);
					}
					else if (this._time == 0)
					{
						if (this._fortInst.getResidenceId() == 122)
						{
							FortSiege.this._isInPreparation = false;
						}

						this._fortInst.getSiege().startSiege();
					}
					else
					{
						FortSiege.LOGGER.warning(this.getClass().getSimpleName() + ": Exception: ScheduleStartSiegeTask(): unknown siege time: " + this._time);
					}
				}
				catch (Exception var3)
				{
					FortSiege.LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception: ScheduleStartSiegeTask() for Fort: " + this._fortInst.getName() + " " + var3.getMessage(), var3);
				}
			}
		}
	}

	public class ScheduleSuspiciousMerchantSpawn implements Runnable
	{
		public ScheduleSuspiciousMerchantSpawn()
		{
			Objects.requireNonNull(FortSiege.this);
			super();
		}

		@Override
		public void run()
		{
			if (!FortSiege.this._isInProgress)
			{
				try
				{
					FortSiege.this._fort.spawnSuspiciousMerchant();
				}
				catch (Exception var2)
				{
					FortSiege.LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception: ScheduleSuspicoiusMerchantSpawn() for Fort: " + FortSiege.this._fort.getName() + " " + var2.getMessage(), var2);
				}
			}
		}
	}
}
