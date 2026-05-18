package net.sf.l2jdev.gameserver.model.siege;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.cache.RelationCache;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.data.xml.SiegeScheduleData;
import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.managers.MailManager;
import net.sf.l2jdev.gameserver.managers.SiegeGuardManager;
import net.sf.l2jdev.gameserver.managers.SiegeManager;
import net.sf.l2jdev.gameserver.model.Message;
import net.sf.l2jdev.gameserver.model.SiegeClan;
import net.sf.l2jdev.gameserver.model.SiegeScheduleDate;
import net.sf.l2jdev.gameserver.model.Spawn;
import net.sf.l2jdev.gameserver.model.TowerSpawn;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.actor.enums.player.TeleportWhereType;
import net.sf.l2jdev.gameserver.model.actor.instance.ControlTower;
import net.sf.l2jdev.gameserver.model.actor.instance.FlameTower;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.ClanMember;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.sieges.OnCastleSiegeFinish;
import net.sf.l2jdev.gameserver.model.events.holders.sieges.OnCastleSiegeOwnerChange;
import net.sf.l2jdev.gameserver.model.events.holders.sieges.OnCastleSiegeStart;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.itemcontainer.Mail;
import net.sf.l2jdev.gameserver.model.olympiad.Hero;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.MailType;
import net.sf.l2jdev.gameserver.network.serverpackets.PlaySound;
import net.sf.l2jdev.gameserver.network.serverpackets.RelationChanged;
import net.sf.l2jdev.gameserver.network.serverpackets.SiegeInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.util.Broadcast;

public class Siege implements Siegable
{
	protected static final Logger LOGGER = Logger.getLogger(Siege.class.getName());
	public static final byte OWNER = -1;
	public static final byte DEFENDER = 0;
	public static final byte ATTACKER = 1;
	public static final byte DEFENDER_NOT_APPROVED = 2;
	private int _controlTowerCount;
	private final Collection<SiegeClan> _attackerClans = ConcurrentHashMap.newKeySet();
	private final Collection<SiegeClan> _defenderClans = ConcurrentHashMap.newKeySet();
	private final Collection<SiegeClan> _defenderWaitingClans = ConcurrentHashMap.newKeySet();
	private final List<ControlTower> _controlTowers = new ArrayList<>();
	private final List<Npc> _relic = new ArrayList<>();
	private final List<FlameTower> _flameTowers = new ArrayList<>();
	final Castle _castle;
	boolean _isInProgress = false;
	private boolean _isNormalSide = true;
	protected boolean _isRegistrationOver = false;
	protected Calendar _siegeEndDate;
	protected ScheduledFuture<?> _scheduledStartSiegeTask = null;
	protected ScheduledFuture<?> _scheduledSiegeInfoTask = null;
	protected int _firstOwnerClanId = -1;

	public Siege(Castle castle)
	{
		this._castle = castle;
		SiegeScheduleDate schedule = SiegeScheduleData.getInstance().getScheduleDateForCastleId(this._castle.getResidenceId());
		if (schedule != null && schedule.siegeEnabled())
		{
			this.startAutoTask();
		}
	}

	@Override
	public void endSiege()
	{
		if (this._isInProgress)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.THE_S1_SIEGE_HAS_FINISHED);
			sm.addCastleId(this._castle.getResidenceId());
			Broadcast.toAllOnlinePlayers(sm);
			if (this._castle.getOwnerId() <= 0)
			{
				sm = new SystemMessage(SystemMessageId.THE_SIEGE_OF_S1_HAS_ENDED_IN_A_DRAW);
				sm.addCastleId(this._castle.getResidenceId());
				Broadcast.toAllOnlinePlayers(sm);
			}
			else
			{
				Clan clan = ClanTable.getInstance().getClan(this.getCastle().getOwnerId());
				sm = new SystemMessage(SystemMessageId.CLAN_S1_IS_VICTORIOUS_OVER_S2_S_CASTLE_SIEGE);
				sm.addString(clan.getName());
				sm.addString(this._castle.getName());
				Broadcast.toAllOnlinePlayers(sm);
				if (clan.getId() == this._firstOwnerClanId)
				{
					clan.increaseBloodAllianceCount();
				}
				else
				{
					this._castle.setTicketBuyCount(0);

					for (ClanMember member : clan.getMembers())
					{
						if (member != null)
						{
							Player player = member.getPlayer();
							if (player != null && player.isNoble())
							{
								Hero.getInstance().setCastleTaken(player.getObjectId(), this.getCastle().getResidenceId());
							}
						}
					}
				}

				long reward = this.getCastle().getTempTreasury() * clan.getRewardMercenary() / 100L;
				this.getCastle().updateTempTreasure(this.getCastle().getTempTreasury() - reward);
				int winnersCount = clan.getMapMercenary().size();
				if (winnersCount != 0)
				{
					reward /= winnersCount;

					for (Integer elem : clan.getMapMercenary().keySet())
					{
						Message msg = new Message(elem, "Reward from Siege!", "Your reward mercenary.", MailType.REGULAR);
						Mail attachments = msg.createAttachments();
						attachments.addItem(ItemProcessType.REWARD, 57, reward, null, null);
						MailManager.getInstance().sendMessage(msg);
					}
				}
			}

			for (SiegeClan attackerClan : this.getAttackerClans())
			{
				Clan clanx = ClanTable.getInstance().getClan(attackerClan.getClanId());
				if (clanx != null)
				{
					for (Player memberx : clanx.getOnlineMembers(0))
					{
						memberx.checkItemRestriction();
					}

					clanx.clearSiegeKills();
					clanx.clearSiegeDeaths();
					clanx.setRecruitMercenary(false);
					clanx.removeMercenaryByClanId(attackerClan.getClanId());
				}
			}

			for (SiegeClan defenderClan : this.getDefenderClans())
			{
				Clan clanx = ClanTable.getInstance().getClan(defenderClan.getClanId());
				if (clanx != null)
				{
					for (Player memberx : clanx.getOnlineMembers(0))
					{
						memberx.checkItemRestriction();
					}

					clanx.clearSiegeKills();
					clanx.clearSiegeDeaths();
					clanx.setRecruitMercenary(false);
					clanx.removeMercenaryByClanId(defenderClan.getClanId());
				}
			}

			this._castle.updateClansReputation();
			this.removeFlags();
			this.teleportPlayer(SiegeTeleportWhoType.NotOwner, TeleportWhereType.TOWN);
			this._isInProgress = false;
			this.updatePlayerSiegeStateFlags(true);
			this.saveCastleSiege();
			this.clearSiegeClan();
			this.removeTowers();
			SiegeGuardManager.getInstance().unspawnSiegeGuard(this.getCastle());
			if (this._castle.getOwnerId() > 0)
			{
				SiegeGuardManager.getInstance().removeSiegeGuards(this.getCastle());
			}

			this._castle.spawnDoor();
			this._castle.setFirstMidVictory(false);
			this._castle.getZone().setActive(false);
			this._castle.getZone().updateZoneStatusForCharactersInside();
			this._castle.getZone().setSiegeInstance(null);
			if (EventDispatcher.getInstance().hasListener(EventType.ON_CASTLE_SIEGE_FINISH, this.getCastle()))
			{
				EventDispatcher.getInstance().notifyEventAsync(new OnCastleSiegeFinish(this), this.getCastle());
			}
		}
	}

	private void removeDefender(SiegeClan sc)
	{
		if (sc != null)
		{
			this.getDefenderClans().remove(sc);
		}
	}

	private void removeAttacker(SiegeClan sc)
	{
		if (sc != null)
		{
			this.getAttackerClans().remove(sc);
		}
	}

	private void addDefender(SiegeClan sc, SiegeClanType type)
	{
		if (sc != null)
		{
			sc.setType(type);
			this.getDefenderClans().add(sc);
		}
	}

	private void addAttacker(SiegeClan sc)
	{
		if (sc != null)
		{
			sc.setType(SiegeClanType.ATTACKER);
			this.getAttackerClans().add(sc);
		}
	}

	public void midVictory()
	{
		if (this._isInProgress)
		{
			if (this._castle.getOwnerId() > 0)
			{
				SiegeGuardManager.getInstance().removeSiegeGuards(this.getCastle());
			}

			if (this.getDefenderClans().isEmpty() && this.getAttackerClans().size() == 1)
			{
				SiegeClan scNewOwner = this.getAttackerClan(this._castle.getOwnerId());
				this.removeAttacker(scNewOwner);
				this.addDefender(scNewOwner, SiegeClanType.OWNER);
				this.endSiege();
				return;
			}

			if (this._castle.getOwnerId() > 0)
			{
				int allyId = ClanTable.getInstance().getClan(this.getCastle().getOwnerId()).getAllyId();
				if (this.getDefenderClans().isEmpty() && allyId != 0)
				{
					boolean allinsamealliance = true;

					for (SiegeClan sc : this.getAttackerClans())
					{
						if (sc != null && ClanTable.getInstance().getClan(sc.getClanId()).getAllyId() != allyId)
						{
							allinsamealliance = false;
						}
					}

					if (allinsamealliance)
					{
						SiegeClan scNewOwner = this.getAttackerClan(this._castle.getOwnerId());
						this.removeAttacker(scNewOwner);
						this.addDefender(scNewOwner, SiegeClanType.OWNER);
						this.endSiege();
						return;
					}
				}

				for (SiegeClan scx : this.getDefenderClans())
				{
					if (scx != null)
					{
						this.removeDefender(scx);
						this.addAttacker(scx);
					}
				}

				SiegeClan scNewOwner = this.getAttackerClan(this._castle.getOwnerId());
				this.removeAttacker(scNewOwner);
				this.addDefender(scNewOwner, SiegeClanType.OWNER);

				for (Clan clan : ClanTable.getInstance().getClanAllies(allyId))
				{
					SiegeClan scxx = this.getAttackerClan(clan.getId());
					if (scxx != null)
					{
						this.removeAttacker(scxx);
						this.addDefender(scxx, SiegeClanType.DEFENDER);
					}
				}

				this._castle.setFirstMidVictory(true);
				this.teleportPlayer(SiegeTeleportWhoType.Attacker, TeleportWhereType.SIEGEFLAG);
				this.teleportPlayer(SiegeTeleportWhoType.Spectator, TeleportWhereType.TOWN);
				this.removeDefenderFlags();
				this._castle.removeUpgrade();
				this._castle.spawnDoor(true);
				this.removeTowers();
				SiegeGuardManager.getInstance().unspawnSiegeGuard(this.getCastle());
				if (this._castle.getOwnerId() > 0)
				{
					SiegeGuardManager.getInstance().removeSiegeGuards(this.getCastle());
				}

				this._controlTowerCount = 0;
				this.spawnControlTower();
				this.spawnFlameTower();
				this.updatePlayerSiegeStateFlags(false);
				if (EventDispatcher.getInstance().hasListener(EventType.ON_CASTLE_SIEGE_OWNER_CHANGE, this.getCastle()))
				{
					EventDispatcher.getInstance().notifyEventAsync(new OnCastleSiegeOwnerChange(this), this.getCastle());
				}
			}
		}
	}

	@Override
	public void startSiege()
	{
		if (!this._isInProgress)
		{
			this._firstOwnerClanId = this._castle.getOwnerId();
			if (this.getAttackerClans().isEmpty())
			{
				SystemMessage sm;
				if (this._firstOwnerClanId <= 0)
				{
					sm = new SystemMessage(SystemMessageId.THE_SIEGE_OF_S1_HAS_BEEN_CANCELLED_DUE_TO_LACK_OF_INTEREST);
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.S1_S_SIEGE_WAS_CANCELLED_BECAUSE_THERE_WERE_NO_CLANS_THAT_PARTICIPATED);
					Clan ownerClan = ClanTable.getInstance().getClan(this._firstOwnerClanId);
					ownerClan.increaseBloodAllianceCount();
				}

				sm.addCastleId(this._castle.getResidenceId());
				Broadcast.toAllOnlinePlayers(sm);
				this.saveCastleSiege();
				return;
			}

			this._isNormalSide = true;
			this._isInProgress = true;
			this.loadSiegeClan();
			this.updatePlayerSiegeStateFlags(false);
			this.updatePlayerSiegeStateFlags(false);
			this.teleportPlayer(SiegeTeleportWhoType.NotOwner, TeleportWhereType.TOWN);
			this._controlTowerCount = 0;
			this.spawnRelic();
			this.spawnControlTower();
			this.spawnFlameTower();
			this._castle.spawnDoor();
			this.spawnSiegeGuard();
			SiegeGuardManager.getInstance().deleteTickets(this.getCastle().getResidenceId());
			this._castle.getZone().setSiegeInstance(this);
			this._castle.getZone().setActive(true);
			this._castle.getZone().updateZoneStatusForCharactersInside();
			this._siegeEndDate = Calendar.getInstance();
			this._siegeEndDate.add(12, SiegeManager.getInstance().getSiegeLength());
			ThreadPool.schedule(new Siege.ScheduleEndSiegeTask(this._castle), 1000L);
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_THE_SIEGE_HAS_BEGUN);
			sm.addCastleId(this._castle.getResidenceId());
			Broadcast.toAllOnlinePlayers(sm);
			Broadcast.toAllOnlinePlayers(new PlaySound("systemmsg_eu.17"));

			for (Player player : World.getInstance().getPlayers())
			{
				SiegeManager.getInstance().sendSiegeInfo(player);
			}

			if (EventDispatcher.getInstance().hasListener(EventType.ON_CASTLE_SIEGE_START, this.getCastle()))
			{
				EventDispatcher.getInstance().notifyEventAsync(new OnCastleSiegeStart(this), this.getCastle());
			}
		}
	}

	public void announceToPlayer(SystemMessage message, boolean bothSides)
	{
		for (SiegeClan siegeClans : this.getDefenderClans())
		{
			Clan clan = ClanTable.getInstance().getClan(siegeClans.getClanId());
			if (clan != null)
			{
				for (Player member : clan.getOnlineMembers(0))
				{
					member.sendPacket(message);
				}
			}
		}

		if (bothSides)
		{
			for (SiegeClan siegeClansx : this.getAttackerClans())
			{
				Clan clan = ClanTable.getInstance().getClan(siegeClansx.getClanId());
				if (clan != null)
				{
					for (Player member : clan.getOnlineMembers(0))
					{
						member.sendPacket(message);
					}
				}
			}
		}
	}

	public void updatePlayerSiegeStateFlags(boolean clear)
	{
		for (SiegeClan siegeclan : this.getAttackerClans())
		{
			if (siegeclan != null)
			{
				Clan clan = ClanTable.getInstance().getClan(siegeclan.getClanId());

				for (Player member : clan.getOnlineMembers(0))
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
						member.setSiegeSide(this._castle.getResidenceId());
						if (this.checkIfInZone(member))
						{
							member.setInSiege(true);
							member.startFameTask(PlayerConfig.CASTLE_ZONE_FAME_TASK_FREQUENCY * 1000, PlayerConfig.CASTLE_ZONE_FAME_AQUIRE_POINTS);
						}
					}

					member.updateUserInfo();
					World.getInstance().forEachVisibleObject(member, Player.class, player -> {
						if (member.isVisibleFor(player))
						{
							long relation = member.getRelation(player);
							boolean isAutoAttackable = member.isAutoAttackable(player);
							RelationCache oldrelation = member.getKnownRelations().get(player.getObjectId());
							if (oldrelation == null || oldrelation.getRelation() != relation || oldrelation.isAutoAttackable() != isAutoAttackable)
							{
								RelationChanged rc = new RelationChanged();
								rc.addRelation(member, relation, isAutoAttackable);
								if (member.hasSummon())
								{
									Summon pet = member.getPet();
									if (pet != null)
									{
										rc.addRelation(pet, relation, isAutoAttackable);
									}

									if (member.hasServitors())
									{
										member.getServitors().values().forEach(s -> rc.addRelation(s, relation, isAutoAttackable));
									}
								}

								player.sendPacket(rc);
								member.getKnownRelations().put(player.getObjectId(), new RelationCache(relation, isAutoAttackable));
							}
						}
					});
				}
			}
		}

		for (SiegeClan siegeclanx : this.getDefenderClans())
		{
			if (siegeclanx != null)
			{
				Clan clan = ClanTable.getInstance().getClan(siegeclanx.getClanId());

				for (Player member : clan.getOnlineMembers(0))
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
						member.setSiegeState((byte) 2);
						member.setSiegeSide(this._castle.getResidenceId());
						if (this.checkIfInZone(member))
						{
							member.setInSiege(true);
							member.startFameTask(PlayerConfig.CASTLE_ZONE_FAME_TASK_FREQUENCY * 1000, PlayerConfig.CASTLE_ZONE_FAME_AQUIRE_POINTS);
						}
					}

					member.updateUserInfo();
					World.getInstance().forEachVisibleObject(member, Player.class, player -> {
						if (member.isVisibleFor(player))
						{
							long relation = member.getRelation(player);
							boolean isAutoAttackable = member.isAutoAttackable(player);
							RelationCache oldrelation = member.getKnownRelations().get(player.getObjectId());
							if (oldrelation == null || oldrelation.getRelation() != relation || oldrelation.isAutoAttackable() != isAutoAttackable)
							{
								RelationChanged rc = new RelationChanged();
								rc.addRelation(member, relation, isAutoAttackable);
								if (member.hasSummon())
								{
									Summon pet = member.getPet();
									if (pet != null)
									{
										rc.addRelation(pet, relation, isAutoAttackable);
									}

									if (member.hasServitors())
									{
										member.getServitors().values().forEach(s -> rc.addRelation(s, relation, isAutoAttackable));
									}
								}

								player.sendPacket(rc);
								member.getKnownRelations().put(player.getObjectId(), new RelationCache(relation, isAutoAttackable));
							}
						}
					});
				}
			}
		}
	}

	public void approveSiegeDefenderClan(int clanId)
	{
		if (clanId > 0)
		{
			this.saveSiegeClan(ClanTable.getInstance().getClan(clanId), (byte) 0, true);
			this.loadSiegeClan();
		}
	}

	public boolean checkIfInZone(WorldObject object)
	{
		return this.checkIfInZone(object.getX(), object.getY(), object.getZ());
	}

	public boolean checkIfInZone(int x, int y, int z)
	{
		return this._isInProgress && this._castle.checkIfInZone(x, y, z);
	}

	@Override
	public boolean checkIsAttacker(Clan clan)
	{
		return this.getAttackerClan(clan) != null;
	}

	@Override
	public boolean checkIsDefender(Clan clan)
	{
		return this.getDefenderClan(clan) != null;
	}

	public boolean checkIsDefenderWaiting(Clan clan)
	{
		return this.getDefenderWaitingClan(clan) != null;
	}

	public void clearSiegeClan()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("DELETE FROM siege_clans WHERE castle_id=?");)
		{
			statement.setInt(1, this._castle.getResidenceId());
			statement.execute();
			if (this._castle.getOwnerId() > 0)
			{
				try (PreparedStatement delete = con.prepareStatement("DELETE FROM siege_clans WHERE clan_id=?"))
				{
					delete.setInt(1, this._castle.getOwnerId());
					delete.execute();
				}
			}

			this.getAttackerClans().clear();
			this.getDefenderClans().clear();
			this._defenderWaitingClans.clear();
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception: clearSiegeClan(): " + var12.getMessage(), var12);
		}
	}

	public void clearSiegeWaitingClan()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("DELETE FROM siege_clans WHERE castle_id=? and type = 2");)
		{
			statement.setInt(1, this._castle.getResidenceId());
			statement.execute();
			this._defenderWaitingClans.clear();
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception: clearSiegeWaitingClan(): " + var9.getMessage(), var9);
		}
	}

	@Override
	public List<Player> getAttackersInZone()
	{
		List<Player> result = new ArrayList<>();

		for (SiegeClan siegeclan : this.getAttackerClans())
		{
			Clan clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			if (clan != null)
			{
				for (Player member : clan.getOnlineMembers(0))
				{
					if (member.isInSiege())
					{
						result.add(member);
					}
				}
			}
		}

		return result;
	}

	public List<Player> getPlayersInZone()
	{
		return this._castle.getZone().getPlayersInside();
	}

	public List<Player> getOwnersInZone()
	{
		List<Player> result = new ArrayList<>();

		for (SiegeClan siegeclan : this.getDefenderClans())
		{
			if (siegeclan.getClanId() == this._castle.getOwnerId())
			{
				Clan clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
				if (clan != null)
				{
					for (Player member : clan.getOnlineMembers(0))
					{
						if (member.isInSiege())
						{
							result.add(member);
						}
					}
				}
			}
		}

		return result;
	}

	public List<Player> getSpectatorsInZone()
	{
		List<Player> result = new ArrayList<>();

		for (Player player : this._castle.getZone().getPlayersInside())
		{
			if (!player.isInSiege())
			{
				result.add(player);
			}
		}

		return result;
	}

	public void killedCT(Npc ct)
	{
		this._controlTowerCount = Math.max(this._controlTowerCount - 1, 0);
	}

	public void killedFlag(Npc flag)
	{
		this.getAttackerClans().forEach(siegeClan -> siegeClan.removeFlag(flag));
	}

	public void listRegisterClan(Player player)
	{
		player.sendPacket(new SiegeInfo(this._castle, player));
	}

	public void registerAttacker(Player player)
	{
		this.registerAttacker(player, false);
	}

	public void registerAttacker(Player player, boolean force)
	{
		Clan clan = player.getClan();
		if (clan != null)
		{
			int allyId = 0;
			if (this._castle.getOwnerId() != 0)
			{
				allyId = ClanTable.getInstance().getClan(this.getCastle().getOwnerId()).getAllyId();
			}

			if (allyId != 0 && clan.getAllyId() == allyId && !force)
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_REGISTER_AS_AN_ATTACKER_BECAUSE_YOU_ARE_IN_AN_ALLIANCE_WITH_THE_CASTLE_OWNING_CLAN);
			}
			else if (force)
			{
				if (SiegeManager.getInstance().checkIsRegistered(clan, this.getCastle().getResidenceId()))
				{
					player.sendPacket(SystemMessageId.YOU_HAVE_ALREADY_REQUESTED_A_CASTLE_SIEGE);
				}
				else
				{
					this.saveSiegeClan(clan, (byte) 1, false);
				}
			}
			else
			{
				if (this.checkIfCanRegister(player, (byte) 1))
				{
					this.saveSiegeClan(clan, (byte) 1, false);
				}
			}
		}
	}

	public void registerDefender(Player player)
	{
		this.registerDefender(player, false);
	}

	public void registerDefender(Player player, boolean force)
	{
		if (this._castle.getOwnerId() <= 0)
		{
			player.sendMessage("You cannot register as a defender because " + this._castle.getName() + " is owned by NPC.");
		}
		else
		{
			Clan clan = player.getClan();
			if (force)
			{
				if (SiegeManager.getInstance().checkIsRegistered(clan, this.getCastle().getResidenceId()))
				{
					player.sendPacket(SystemMessageId.YOU_HAVE_ALREADY_REQUESTED_A_CASTLE_SIEGE);
				}
				else
				{
					this.saveSiegeClan(clan, (byte) 2, false);
				}
			}
			else
			{
				if (this.checkIfCanRegister(player, (byte) 2))
				{
					this.saveSiegeClan(clan, (byte) 2, false);
				}
			}
		}
	}

	public void removeSiegeClan(int clanId)
	{
		if (clanId > 0)
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("DELETE FROM siege_clans WHERE castle_id=? and clan_id=?");)
			{
				statement.setInt(1, this._castle.getResidenceId());
				statement.setInt(2, clanId);
				statement.execute();
				this.loadSiegeClan();
			}
			catch (Exception var10)
			{
				LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception: removeSiegeClan(): " + var10.getMessage(), var10);
			}
		}
	}

	public void removeSiegeClan(Clan clan)
	{
		if (clan != null && clan.getCastleId() != this.getCastle().getResidenceId() && SiegeManager.getInstance().checkIsRegistered(clan, this.getCastle().getResidenceId()))
		{
			this.removeSiegeClan(clan.getId());
		}
	}

	public void removeSiegeClan(Player player)
	{
		this.removeSiegeClan(player.getClan());
	}

	public void startAutoTask()
	{
		this.correctSiegeDateTime();
		LOGGER.info("Siege of " + this._castle.getName() + ": " + this._castle.getSiegeDate().getTime());
		this.loadSiegeClan();
		if (this._scheduledStartSiegeTask != null)
		{
			this._scheduledStartSiegeTask.cancel(false);
		}

		this._scheduledStartSiegeTask = ThreadPool.schedule(new Siege.ScheduleStartSiegeTask(this._castle), 1000L);
		this.startInfoTask();
	}

	private void startInfoTask()
	{
		if (this._scheduledSiegeInfoTask != null)
		{
			this._scheduledSiegeInfoTask.cancel(false);
		}

		this._scheduledSiegeInfoTask = ThreadPool.schedule(() -> {
			for (Player player : World.getInstance().getPlayers())
			{
				SiegeManager.getInstance().sendSiegeInfo(player, this._castle.getResidenceId());
			}
		}, Math.max(0L, this.getSiegeDate().getTimeInMillis() - System.currentTimeMillis() - 3600000L));
	}

	public void teleportPlayer(SiegeTeleportWhoType teleportWho, TeleportWhereType teleportWhere)
	{
		List<Player> players = switch (teleportWho)
		{
			case Owner -> this.getOwnersInZone();
			case NotOwner -> {
				List<Player> list = new LinkedList<>(this._castle.getZone().getPlayersInside());
				list.removeIf(player -> player == null || player.inObserverMode() || (player.getClanId() > 0 && (player.getClanId() == this._castle.getOwnerId() || player.getClanIdMercenary() == this._castle.getOwnerId())));
				yield list;
			}
			case Attacker -> this.getAttackersInZone();
			case Spectator -> this.getSpectatorsInZone();
			default -> Collections.emptyList();
		};
		for (Player player : players)
		{
			if (!player.isGM() && !player.isJailed())
			{
				player.teleToLocation(teleportWhere);
			}
		}
	}

	private void addAttacker(int clanId)
	{
		this.getAttackerClans().add(new SiegeClan(clanId, SiegeClanType.ATTACKER));
	}

	private void addDefender(int clanId)
	{
		this.getDefenderClans().add(new SiegeClan(clanId, SiegeClanType.DEFENDER));
	}

	private void addDefender(int clanId, SiegeClanType type)
	{
		this.getDefenderClans().add(new SiegeClan(clanId, type));
	}

	private void addDefenderWaiting(int clanId)
	{
		this._defenderWaitingClans.add(new SiegeClan(clanId, SiegeClanType.DEFENDER_PENDING));
	}

	private boolean checkIfCanRegister(Player player, byte typeId)
	{
		Clan clan = player.getClan();
		if (this._isInProgress)
		{
			player.sendPacket(SystemMessageId.THIS_IS_NOT_THE_TIME_FOR_SIEGE_REGISTRATION_AND_SO_REGISTRATION_AND_CANCELLATION_CANNOT_BE_DONE);
		}
		else if (clan == null || clan.getLevel() < SiegeManager.getInstance().getSiegeClanMinLevel())
		{
			player.sendPacket(SystemMessageId.ONLY_CLANS_OF_LEVEL_3_OR_ABOVE_MAY_REGISTER_FOR_A_CASTLE_SIEGE);
		}
		else if (clan.getId() == this._castle.getOwnerId())
		{
			player.sendPacket(SystemMessageId.CASTLE_OWNING_CLANS_ARE_AUTOMATICALLY_REGISTERED_ON_THE_DEFENDING_SIDE);
		}
		else if (clan.getCastleId() > 0)
		{
			player.sendPacket(SystemMessageId.A_CLAN_THAT_OWNS_A_CASTLE_CANNOT_PARTICIPATE_IN_ANOTHER_SIEGE);
		}
		else if (SiegeManager.getInstance().checkIsRegistered(clan, this.getCastle().getResidenceId()))
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_ALREADY_REQUESTED_A_CASTLE_SIEGE);
		}
		else if (this.checkIfAlreadyRegisteredForSameDay(clan))
		{
			player.sendPacket(SystemMessageId.YOUR_APPLICATION_HAS_BEEN_DENIED_BECAUSE_YOU_HAVE_ALREADY_SUBMITTED_A_REQUEST_FOR_ANOTHER_CASTLE_SIEGE);
		}
		else if (typeId == 1 && this.getAttackerClans().size() >= SiegeManager.getInstance().getAttackerMaxClans())
		{
			player.sendPacket(SystemMessageId.NO_MORE_REGISTRATIONS_MAY_BE_ACCEPTED_FOR_THE_ATTACKER_SIDE);
		}
		else if ((typeId == 0 || typeId == 2 || typeId == -1) && this.getDefenderClans().size() + this.getDefenderWaitingClans().size() >= SiegeManager.getInstance().getDefenderMaxClans())
		{
			player.sendPacket(SystemMessageId.NO_MORE_REGISTRATIONS_MAY_BE_ACCEPTED_FOR_THE_DEFENDER_SIDE);
		}
		else
		{
			if (this._castle.getResidenceId() != 1 || clan.getLevel() < 5)
			{
				return true;
			}

			player.sendPacket(SystemMessageId.ONLY_LEVEL_3_4_CLANS_CAN_PARTICIPATE_IN_CASTLE_SIEGE);
		}

		return false;
	}

	public boolean checkIfAlreadyRegisteredForSameDay(Clan clan)
	{
		for (Siege siege : SiegeManager.getInstance().getSieges())
		{
			if (siege != this && siege.getSiegeDate().get(7) == this.getSiegeDate().get(7))
			{
				if (siege.checkIsAttacker(clan) || siege.checkIsDefender(clan) || siege.checkIsDefenderWaiting(clan))
				{
					return true;
				}
			}
		}

		return false;
	}

	public void correctSiegeDateTime()
	{
		boolean corrected = false;
		if (this.getCastle().getSiegeDate().getTimeInMillis() < System.currentTimeMillis())
		{
			corrected = true;
			this.setNextSiegeDate();
		}

		if (corrected)
		{
			this.saveSiegeDate();
		}
	}

	private void loadSiegeClan()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT clan_id,type FROM siege_clans where castle_id=?");)
		{
			this.getAttackerClans().clear();
			this.getDefenderClans().clear();
			this._defenderWaitingClans.clear();
			if (this._castle.getOwnerId() > 0)
			{
				this.addDefender(this._castle.getOwnerId(), SiegeClanType.OWNER);
			}

			statement.setInt(1, this._castle.getResidenceId());

			try (ResultSet rs = statement.executeQuery())
			{
				while (rs.next())
				{
					int typeId = rs.getInt("type");
					if (typeId == 0)
					{
						this.addDefender(rs.getInt("clan_id"));
					}
					else if (typeId == 1)
					{
						this.addAttacker(rs.getInt("clan_id"));
					}
					else if (typeId == 2)
					{
						this.addDefenderWaiting(rs.getInt("clan_id"));
					}
				}
			}
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception: loadSiegeClan(): " + var12.getMessage(), var12);
		}
	}

	private void removeTowers()
	{
		for (FlameTower ct : this._flameTowers)
		{
			ct.deleteMe();
		}

		for (ControlTower ct : this._controlTowers)
		{
			ct.deleteMe();
		}

		for (Npc ct : this._relic)
		{
			ct.deleteMe();
		}

		this._flameTowers.clear();
		this._controlTowers.clear();
	}

	private void removeFlags()
	{
		for (SiegeClan sc : this.getAttackerClans())
		{
			if (sc != null)
			{
				sc.removeFlags();
			}
		}

		for (SiegeClan scx : this.getDefenderClans())
		{
			if (scx != null)
			{
				scx.removeFlags();
			}
		}
	}

	private void removeDefenderFlags()
	{
		for (SiegeClan sc : this.getDefenderClans())
		{
			if (sc != null)
			{
				sc.removeFlags();
			}
		}
	}

	private void saveCastleSiege()
	{
		this.setNextSiegeDate();
		this.getTimeRegistrationOverDate().setTimeInMillis(System.currentTimeMillis());
		this._castle.getTimeRegistrationOverDate().add(5, 1);
		this._castle.setTimeRegistrationOver(false);
		this.saveSiegeDate();
		this.startAutoTask();
	}

	public void saveSiegeDate()
	{
		if (this._scheduledStartSiegeTask != null)
		{
			this._scheduledStartSiegeTask.cancel(true);
			this._scheduledStartSiegeTask = ThreadPool.schedule(new Siege.ScheduleStartSiegeTask(this._castle), 1000L);
		}

		this.startInfoTask();

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("UPDATE castle SET siegeDate = ?, regTimeEnd = ?, regTimeOver = ?  WHERE id = ?");)
		{
			statement.setLong(1, this._castle.getSiegeDate().getTimeInMillis());
			statement.setLong(2, 0L);
			statement.setString(3, "false");
			statement.setInt(4, this._castle.getResidenceId());
			statement.execute();
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception: saveSiegeDate(): " + var9.getMessage(), var9);
		}
	}

	private void saveSiegeClan(Clan clan, byte typeId, boolean isUpdateRegistration)
	{
		if (clan.getCastleId() <= 0)
		{
			for (ClanMember clanMember : clan.getMembers())
			{
				if (clanMember.getPlayer() != null)
				{
					if (clanMember.getPlayer().isMercenary())
					{
						clanMember.getPlayer().setMercenary(false, clanMember.getPlayer().getClanIdMercenary());
					}
				}
				else
				{
					clan.removeMercenaryByPlayerId(clanMember.getObjectId());
				}
			}

			try (Connection con = DatabaseFactory.getConnection())
			{
				if (typeId != 0 && typeId != 2 && typeId != -1)
				{
					if (this.getAttackerClans().size() >= SiegeManager.getInstance().getAttackerMaxClans())
					{
						return;
					}
				}
				else if (this.getDefenderClans().size() + this.getDefenderWaitingClans().size() >= SiegeManager.getInstance().getDefenderMaxClans())
				{
					return;
				}

				if (!isUpdateRegistration)
				{
					try (PreparedStatement statement = con.prepareStatement("INSERT INTO siege_clans (clan_id,castle_id,type,castle_owner) values (?,?,?,0)"))
					{
						statement.setInt(1, clan.getId());
						statement.setInt(2, this._castle.getResidenceId());
						statement.setInt(3, typeId);
						statement.execute();
					}
				}
				else
				{
					try (PreparedStatement statement = con.prepareStatement("UPDATE siege_clans SET type = ? WHERE castle_id = ? AND clan_id = ?"))
					{
						statement.setInt(1, typeId);
						statement.setInt(2, this._castle.getResidenceId());
						statement.setInt(3, clan.getId());
						statement.execute();
					}
				}

				if (typeId == 0 || typeId == -1)
				{
					this.addDefender(clan.getId());
					return;
				}
				else if (typeId == 1)
				{
					this.addAttacker(clan.getId());
				}
				else if (typeId == 2)
				{
					this.addDefenderWaiting(clan.getId());
				}
			}
			catch (Exception var14)
			{
				LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception: saveSiegeClan(Pledge clan, int typeId, boolean isUpdateRegistration): " + var14.getMessage(), var14);
			}
		}
	}

	private void setNextSiegeDate()
	{
		SiegeScheduleDate holder = SiegeScheduleData.getInstance().getScheduleDateForCastleId(this._castle.getResidenceId());
		if (holder != null && holder.siegeEnabled())
		{
			Calendar calendar = this._castle.getSiegeDate();
			if (calendar.getTimeInMillis() < System.currentTimeMillis())
			{
				calendar.setTimeInMillis(System.currentTimeMillis());
			}

			calendar.set(7, holder.getDay());
			calendar.set(11, holder.getHour());
			calendar.set(12, 0);
			calendar.set(13, 0);
			if (calendar.before(Calendar.getInstance()))
			{
				calendar.add(3, SiegeManager.getInstance().getSiegeCycle());
			}

			if (CastleManager.getInstance().getSiegeDates(calendar.getTimeInMillis()) < holder.getMaxConcurrent())
			{
				CastleManager.getInstance().registerSiegeDate(this.getCastle().getResidenceId(), calendar.getTimeInMillis());
				Broadcast.toAllOnlinePlayers(new SystemMessage(SystemMessageId.S1_HAS_ANNOUNCED_THE_NEXT_CASTLE_SIEGE_TIME).addCastleId(this._castle.getResidenceId()));
				this._isRegistrationOver = false;
			}
			else
			{
				this._isRegistrationOver = true;
			}
		}
	}

	private void spawnRelic()
	{
		try
		{
			TowerSpawn ts = SiegeManager.getInstance().getRelicTowers(this.getCastle().getResidenceId());
			Spawn spawn = new Spawn(ts.getId());
			spawn.setLocation(ts.getLocation());
			Npc npc = spawn.doSpawn(false);
			this._relic.add(npc);
		}
		catch (Exception var4)
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": Cannot spawn Relic! " + var4);
		}
	}

	private void spawnControlTower()
	{
		try
		{
			for (TowerSpawn ts : SiegeManager.getInstance().getControlTowers(this.getCastle().getResidenceId()))
			{
				Spawn spawn = new Spawn(ts.getId());
				spawn.setLocation(ts.getLocation());
				this._controlTowers.add((ControlTower) spawn.doSpawn(false));
			}
		}
		catch (Exception var4)
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": Cannot spawn control tower! " + var4);
		}

		this._controlTowerCount = this._controlTowers.size();
	}

	private void spawnFlameTower()
	{
		try
		{
			for (TowerSpawn ts : SiegeManager.getInstance().getFlameTowers(this.getCastle().getResidenceId()))
			{
				Spawn spawn = new Spawn(ts.getId());
				spawn.setLocation(ts.getLocation());
				FlameTower tower = (FlameTower) spawn.doSpawn(false);
				tower.setUpgradeLevel(ts.getUpgradeLevel());
				tower.setZoneList(ts.getZoneList());
				this._flameTowers.add(tower);
			}
		}
		catch (Exception var5)
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": Cannot spawn flame tower! " + var5);
		}
	}

	private void spawnSiegeGuard()
	{
		SiegeGuardManager.getInstance().spawnSiegeGuard(this.getCastle());
		Set<Spawn> spawned = SiegeGuardManager.getInstance().getSpawnedGuards(this.getCastle().getResidenceId());
		if (!spawned.isEmpty())
		{
			double distanceClosest = 0.0;

			for (Spawn spawn : spawned)
			{
				if (spawn != null)
				{
					ControlTower closestCt = null;
					distanceClosest = 2.147483647E9;

					for (ControlTower ct : this._controlTowers)
					{
						if (ct != null)
						{
							double distance = ct.calculateDistance3D(spawn);
							if (distance < distanceClosest)
							{
								closestCt = ct;
								distanceClosest = distance;
							}
						}
					}

					if (closestCt != null)
					{
						closestCt.registerGuard(spawn);
					}
				}
			}
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
		for (SiegeClan sc : this.getAttackerClans())
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
		return this._isNormalSide ? this._attackerClans : this._defenderClans;
	}

	public int getAttackerRespawnDelay()
	{
		return SiegeManager.getInstance().getAttackerRespawnDelay();
	}

	public Castle getCastle()
	{
		return this._castle;
	}

	@Override
	public SiegeClan getDefenderClan(Clan clan)
	{
		return clan == null ? null : this.getDefenderClan(clan.getId());
	}

	@Override
	public SiegeClan getDefenderClan(int clanId)
	{
		for (SiegeClan sc : this.getDefenderClans())
		{
			if (sc != null && sc.getClanId() == clanId)
			{
				return sc;
			}
		}

		return null;
	}

	@Override
	public Collection<SiegeClan> getDefenderClans()
	{
		return this._isNormalSide ? this._defenderClans : this._attackerClans;
	}

	public SiegeClan getDefenderWaitingClan(Clan clan)
	{
		return clan == null ? null : this.getDefenderWaitingClan(clan.getId());
	}

	public SiegeClan getDefenderWaitingClan(int clanId)
	{
		for (SiegeClan sc : this._defenderWaitingClans)
		{
			if (sc != null && sc.getClanId() == clanId)
			{
				return sc;
			}
		}

		return null;
	}

	public Collection<SiegeClan> getDefenderWaitingClans()
	{
		return this._defenderWaitingClans;
	}

	public boolean isInProgress()
	{
		return this._isInProgress;
	}

	public boolean isRegistrationOver()
	{
		return this._isRegistrationOver;
	}

	public boolean isTimeRegistrationOver()
	{
		return this._castle.isTimeRegistrationOver();
	}

	@Override
	public Calendar getSiegeDate()
	{
		return this._castle.getSiegeDate();
	}

	public Calendar getTimeRegistrationOverDate()
	{
		return this._castle.getTimeRegistrationOverDate();
	}

	public void endTimeRegistration(boolean automatic)
	{
		this._castle.setTimeRegistrationOver(true);
		if (!automatic)
		{
			this.saveSiegeDate();
		}
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

	public int getControlTowerCount()
	{
		return this._controlTowerCount;
	}

	@Override
	public boolean giveFame()
	{
		return true;
	}

	@Override
	public int getFameFrequency()
	{
		return PlayerConfig.CASTLE_ZONE_FAME_TASK_FREQUENCY;
	}

	@Override
	public int getFameAmount()
	{
		return PlayerConfig.CASTLE_ZONE_FAME_AQUIRE_POINTS;
	}

	@Override
	public void updateSiege()
	{
	}

	public class ScheduleEndSiegeTask implements Runnable
	{
		private final Castle _castleInst;

		public ScheduleEndSiegeTask(Castle pCastle)
		{
			Objects.requireNonNull(Siege.this);
			super();
			this._castleInst = pCastle;
		}

		@Override
		public void run()
		{
			if (Siege.this._isInProgress)
			{
				try
				{
					long timeRemaining = Siege.this._siegeEndDate.getTimeInMillis() - System.currentTimeMillis();
					if (timeRemaining > 3600000L)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.THE_CASTLE_SIEGE_ENDS_IN_S1_H);
						sm.addInt(2);
						Siege.this.announceToPlayer(sm, true);
						ThreadPool.schedule(Siege.this.new ScheduleEndSiegeTask(this._castleInst), timeRemaining - 3600000L);
					}
					else if (timeRemaining <= 3600000L && timeRemaining > 600000L)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.THE_CASTLE_SIEGE_ENDS_IN_S1_MIN);
						sm.addInt((int) timeRemaining / 60000);
						Siege.this.announceToPlayer(sm, true);
						ThreadPool.schedule(Siege.this.new ScheduleEndSiegeTask(this._castleInst), timeRemaining - 600000L);
					}
					else if (timeRemaining <= 600000L && timeRemaining > 300000L)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.THE_CASTLE_SIEGE_ENDS_IN_S1_MIN);
						sm.addInt((int) timeRemaining / 60000);
						Siege.this.announceToPlayer(sm, true);
						ThreadPool.schedule(Siege.this.new ScheduleEndSiegeTask(this._castleInst), timeRemaining - 300000L);
					}
					else if (timeRemaining <= 300000L && timeRemaining > 10000L)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.THE_CASTLE_SIEGE_ENDS_IN_S1_MIN);
						sm.addInt((int) timeRemaining / 60000);
						Siege.this.announceToPlayer(sm, true);
						ThreadPool.schedule(Siege.this.new ScheduleEndSiegeTask(this._castleInst), timeRemaining - 10000L);
					}
					else if (timeRemaining <= 10000L && timeRemaining > 0L)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.THE_CASTLE_SIEGE_ENDS_IN_S1_SEC);
						sm.addInt((int) timeRemaining / 1000);
						Siege.this.announceToPlayer(sm, true);
						ThreadPool.schedule(Siege.this.new ScheduleEndSiegeTask(this._castleInst), timeRemaining);
					}
					else
					{
						this._castleInst.getSiege().endSiege();
					}
				}
				catch (Exception var4)
				{
					Siege.LOGGER.log(Level.SEVERE, this.getClass().getSimpleName() + ": ", var4);
				}
			}
		}
	}

	public class ScheduleStartSiegeTask implements Runnable
	{
		private final Castle _castleInst;

		public ScheduleStartSiegeTask(Castle pCastle)
		{
			Objects.requireNonNull(Siege.this);
			super();
			this._castleInst = pCastle;
		}

		@Override
		public void run()
		{
			Siege.this._scheduledStartSiegeTask.cancel(false);
			if (!Siege.this._isInProgress)
			{
				try
				{
					long currentTime = System.currentTimeMillis();
					if (!Siege.this._castle.isTimeRegistrationOver())
					{
						long regTimeRemaining = Siege.this.getTimeRegistrationOverDate().getTimeInMillis() - currentTime;
						if (regTimeRemaining > 0L)
						{
							Siege.this._scheduledStartSiegeTask = ThreadPool.schedule(Siege.this.new ScheduleStartSiegeTask(this._castleInst), regTimeRemaining);
							return;
						}

						Siege.this.endTimeRegistration(true);
					}

					long timeRemaining = Siege.this.getSiegeDate().getTimeInMillis() - currentTime;
					if (timeRemaining > 86400000L)
					{
						Siege.this._scheduledStartSiegeTask = ThreadPool.schedule(Siege.this.new ScheduleStartSiegeTask(this._castleInst), timeRemaining - 86400000L);
					}
					else if (timeRemaining <= 86400000L && timeRemaining > 13600000L)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.THE_REGISTRATION_TERM_FOR_S1_HAS_ENDED);
						sm.addCastleId(Siege.this._castle.getResidenceId());
						Broadcast.toAllOnlinePlayers(sm);
						Siege.this._isRegistrationOver = true;
						Siege.this.clearSiegeWaitingClan();
						Siege.this._scheduledStartSiegeTask = ThreadPool.schedule(Siege.this.new ScheduleStartSiegeTask(this._castleInst), timeRemaining - 13600000L);
					}
					else if (timeRemaining <= 13600000L && timeRemaining > 600000L)
					{
						Siege.this._scheduledStartSiegeTask = ThreadPool.schedule(Siege.this.new ScheduleStartSiegeTask(this._castleInst), timeRemaining - 600000L);
					}
					else if (timeRemaining <= 600000L && timeRemaining > 300000L)
					{
						Siege.this._scheduledStartSiegeTask = ThreadPool.schedule(Siege.this.new ScheduleStartSiegeTask(this._castleInst), timeRemaining - 300000L);
					}
					else if (timeRemaining <= 300000L && timeRemaining > 10000L)
					{
						Siege.this._scheduledStartSiegeTask = ThreadPool.schedule(Siege.this.new ScheduleStartSiegeTask(this._castleInst), timeRemaining - 10000L);
					}
					else if (timeRemaining <= 10000L && timeRemaining > 0L)
					{
						Siege.this._scheduledStartSiegeTask = ThreadPool.schedule(Siege.this.new ScheduleStartSiegeTask(this._castleInst), timeRemaining);
					}
					else
					{
						this._castleInst.getSiege().startSiege();
					}
				}
				catch (Exception var6)
				{
					Siege.LOGGER.log(Level.SEVERE, this.getClass().getSimpleName() + ": ", var6);
				}
			}
		}
	}
}
