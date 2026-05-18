package net.sf.l2jdev.gameserver.model.actor.holders.player;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.managers.DuelManager;
import net.sf.l2jdev.gameserver.managers.InstanceManager;
import net.sf.l2jdev.gameserver.managers.ZoneManager;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Team;
import net.sf.l2jdev.gameserver.model.actor.enums.player.DuelResult;
import net.sf.l2jdev.gameserver.model.actor.instance.Door;
import net.sf.l2jdev.gameserver.model.instancezone.Instance;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.enums.SkillFinishType;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.model.zone.type.OlympiadStadiumZone;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.ExDuelEnd;
import net.sf.l2jdev.gameserver.network.serverpackets.ExDuelReady;
import net.sf.l2jdev.gameserver.network.serverpackets.ExDuelStart;
import net.sf.l2jdev.gameserver.network.serverpackets.ExDuelUpdateUserInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.PlaySound;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.SocialAction;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class Duel
{
	protected static final Logger LOGGER = Logger.getLogger(Duel.class.getName());
	public static final int DUELSTATE_NODUEL = 0;
	public static final int DUELSTATE_DUELLING = 1;
	public static final int DUELSTATE_DEAD = 2;
	public static final int DUELSTATE_WINNER = 3;
	public static final int DUELSTATE_INTERRUPTED = 4;
	private static final PlaySound B04_S01 = new PlaySound(1, "B04_S01", 0, 0, 0, 0, 0);
	public static final int PARTY_DUEL_DURATION = 300;
	public static final int PLAYER_DUEL_DURATION = 120;
	private final int _duelId;
	private Player _playerA;
	private Player _playerB;
	private final boolean _partyDuel;
	private final Calendar _duelEndTime;
	private int _surrenderRequest = 0;
	private int _countdown = 5;
	private boolean _finished = false;
	private final Map<Integer, Duel.PlayerCondition> _playerConditions = new ConcurrentHashMap<>();
	Instance _duelInstance;

	public Duel(Player playerA, Player playerB, int partyDuel, int duelId)
	{
		this._duelId = duelId;
		this._playerA = playerA;
		this._playerB = playerB;
		this._partyDuel = partyDuel == 1;
		if (this._partyDuel)
		{
			for (Player member : this._playerA.getParty().getMembers())
			{
				member.setStartingDuel();
			}

			for (Player member : this._playerB.getParty().getMembers())
			{
				member.setStartingDuel();
			}
		}
		else
		{
			this._playerA.setStartingDuel();
			this._playerB.setStartingDuel();
		}

		this._duelEndTime = Calendar.getInstance();
		this._duelEndTime.add(13, this._partyDuel ? 300 : 120);
		this.setFinished(false);
		if (this._partyDuel)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.IN_A_MOMENT_YOU_WILL_BE_TRANSPORTED_TO_THE_SITE_WHERE_THE_DUEL_WILL_TAKE_PLACE);
			this.broadcastToTeam1(sm);
			this.broadcastToTeam2(sm);
		}

		ThreadPool.schedule(new Duel.ScheduleStartDuelTask(this), 3000L);
	}

	public Instance getDueldInstance()
	{
		return this._duelInstance;
	}

	private void stopFighting()
	{
		ActionFailed af = ActionFailed.STATIC_PACKET;
		if (this._partyDuel)
		{
			for (Player temp : this._playerA.getParty().getMembers())
			{
				temp.abortCast();
				temp.getAI().setIntention(Intention.ACTIVE);
				temp.setTarget(null);
				temp.sendPacket(af);
				temp.getServitorsAndPets().forEach(s -> {
					s.abortCast();
					s.abortAttack();
					s.setTarget(null);
					s.getAI().setIntention(Intention.ACTIVE);
				});
			}

			for (Player temp : this._playerB.getParty().getMembers())
			{
				temp.abortCast();
				temp.getAI().setIntention(Intention.ACTIVE);
				temp.setTarget(null);
				temp.sendPacket(af);
				temp.getServitorsAndPets().forEach(s -> {
					s.abortCast();
					s.abortAttack();
					s.setTarget(null);
					s.getAI().setIntention(Intention.ACTIVE);
				});
			}
		}
		else
		{
			this._playerA.abortCast();
			this._playerB.abortCast();
			this._playerA.getAI().setIntention(Intention.ACTIVE);
			this._playerA.setTarget(null);
			this._playerB.getAI().setIntention(Intention.ACTIVE);
			this._playerB.setTarget(null);
			this._playerA.sendPacket(af);
			this._playerB.sendPacket(af);
			this._playerA.getServitorsAndPets().forEach(s -> {
				s.abortCast();
				s.abortAttack();
				s.setTarget(null);
				s.getAI().setIntention(Intention.ACTIVE);
			});
			this._playerB.getServitorsAndPets().forEach(s -> {
				s.abortCast();
				s.abortAttack();
				s.setTarget(null);
				s.getAI().setIntention(Intention.ACTIVE);
			});
		}
	}

	public boolean isDuelistInPvp(boolean sendMessage)
	{
		if (this._partyDuel)
		{
			return false;
		}
		else if (this._playerA.getPvpFlag() == 0 && this._playerB.getPvpFlag() == 0)
		{
			return false;
		}
		else
		{
			if (sendMessage)
			{
				this._playerA.sendMessage("The duel was canceled because a duelist engaged in PvP combat.");
				this._playerB.sendMessage("The duel was canceled because a duelist engaged in PvP combat.");
			}

			return true;
		}
	}

	public void startDuel()
	{
		if (this._playerA != null && this._playerB != null && !this._playerA.isInDuel() && !this._playerB.isInDuel())
		{
			if (this._partyDuel)
			{
				for (Player temp : this._playerA.getParty().getMembers())
				{
					temp.cancelActiveTrade();
					temp.setInDuel(this._duelId);
					temp.setTeam(Team.BLUE);
					temp.broadcastUserInfo();
					this.broadcastToTeam2(new ExDuelUpdateUserInfo(temp));
				}

				for (Player temp : this._playerB.getParty().getMembers())
				{
					temp.cancelActiveTrade();
					temp.setInDuel(this._duelId);
					temp.setTeam(Team.RED);
					temp.broadcastUserInfo();
					this.broadcastToTeam1(new ExDuelUpdateUserInfo(temp));
				}

				this.broadcastToTeam1(ExDuelReady.PARTY_DUEL);
				this.broadcastToTeam2(ExDuelReady.PARTY_DUEL);
				this.broadcastToTeam1(ExDuelStart.PARTY_DUEL);
				this.broadcastToTeam2(ExDuelStart.PARTY_DUEL);

				for (Door door : this._duelInstance.getDoors())
				{
					if (door != null && !door.isOpen())
					{
						door.openMe();
					}
				}
			}
			else
			{
				this._playerA.setInDuel(this._duelId);
				this._playerA.setTeam(Team.BLUE);
				this._playerB.setInDuel(this._duelId);
				this._playerB.setTeam(Team.RED);
				this.broadcastToTeam1(ExDuelReady.PLAYER_DUEL);
				this.broadcastToTeam2(ExDuelReady.PLAYER_DUEL);
				this.broadcastToTeam1(ExDuelStart.PLAYER_DUEL);
				this.broadcastToTeam2(ExDuelStart.PLAYER_DUEL);
				this.broadcastToTeam1(new ExDuelUpdateUserInfo(this._playerB));
				this.broadcastToTeam2(new ExDuelUpdateUserInfo(this._playerA));
				this._playerA.broadcastUserInfo();
				this._playerB.broadcastUserInfo();
			}

			this.broadcastToTeam1(B04_S01);
			this.broadcastToTeam2(B04_S01);
			ThreadPool.schedule(new Duel.ScheduleDuelTask(this), 1000L);
		}
		else
		{
			this._playerConditions.clear();
			DuelManager.getInstance().removeDuel(this);
		}
	}

	public void savePlayerConditions()
	{
		if (this._partyDuel)
		{
			for (Player player : this._playerA.getParty().getMembers())
			{
				this._playerConditions.put(player.getObjectId(), new Duel.PlayerCondition(player, this._partyDuel));
			}

			for (Player player : this._playerB.getParty().getMembers())
			{
				this._playerConditions.put(player.getObjectId(), new Duel.PlayerCondition(player, this._partyDuel));
			}
		}
		else
		{
			this._playerConditions.put(this._playerA.getObjectId(), new Duel.PlayerCondition(this._playerA, this._partyDuel));
			this._playerConditions.put(this._playerB.getObjectId(), new Duel.PlayerCondition(this._playerB, this._partyDuel));
		}
	}

	public void restorePlayerConditions(boolean abnormalDuelEnd)
	{
		if (this._partyDuel)
		{
			for (Player temp : this._playerA.getParty().getMembers())
			{
				temp.setInDuel(0);
				temp.setTeam(Team.NONE);
				temp.broadcastUserInfo();
			}

			for (Player temp : this._playerB.getParty().getMembers())
			{
				temp.setInDuel(0);
				temp.setTeam(Team.NONE);
				temp.broadcastUserInfo();
			}
		}
		else
		{
			this._playerA.setInDuel(0);
			this._playerA.setTeam(Team.NONE);
			this._playerA.broadcastUserInfo();
			this._playerB.setInDuel(0);
			this._playerB.setTeam(Team.NONE);
			this._playerB.broadcastUserInfo();
		}

		if (!abnormalDuelEnd)
		{
			this._playerConditions.values().forEach(Duel.PlayerCondition::restoreCondition);
		}
	}

	public int getId()
	{
		return this._duelId;
	}

	public int getRemainingTime()
	{
		return (int) (this._duelEndTime.getTimeInMillis() - System.currentTimeMillis());
	}

	public Player getPlayerA()
	{
		return this._playerA;
	}

	public Player getPlayerB()
	{
		return this._playerB;
	}

	public boolean isPartyDuel()
	{
		return this._partyDuel;
	}

	public void setFinished(boolean mode)
	{
		this._finished = mode;
	}

	public boolean getFinished()
	{
		return this._finished;
	}

	public void teleportPlayers()
	{
		if (this._partyDuel)
		{
			int instanceId = DuelManager.getInstance().getDuelArena();
			OlympiadStadiumZone zone = null;

			for (OlympiadStadiumZone z : ZoneManager.getInstance().getAllZones(OlympiadStadiumZone.class))
			{
				if (z.getInstanceTemplateId() == instanceId)
				{
					zone = z;
					break;
				}
			}

			if (zone == null)
			{
				throw new RuntimeException("Unable to find a party duel arena!");
			}
			List<Location> spawns = zone.getSpawns();
			this._duelInstance = InstanceManager.getInstance().createInstance(InstanceManager.getInstance().getInstanceTemplate(instanceId), null);
			Location spawn1 = spawns.get(Rnd.get(spawns.size() / 2));

			for (Player temp : this._playerA.getParty().getMembers())
			{
				temp.teleToLocation(spawn1.getX(), spawn1.getY(), spawn1.getZ(), 0, 0, this._duelInstance);
			}

			Location spawn2 = spawns.get(Rnd.get(spawns.size() / 2, spawns.size()));

			for (Player temp : this._playerB.getParty().getMembers())
			{
				temp.teleToLocation(spawn2.getX(), spawn2.getY(), spawn2.getZ(), 0, 0, this._duelInstance);
			}
		}
	}

	public void broadcastToTeam1(ServerPacket packet)
	{
		if (this._playerA != null)
		{
			if (this._partyDuel && this._playerA.getParty() != null)
			{
				for (Player temp : this._playerA.getParty().getMembers())
				{
					temp.sendPacket(packet);
				}
			}
			else
			{
				this._playerA.sendPacket(packet);
			}
		}
	}

	public void broadcastToTeam2(ServerPacket packet)
	{
		if (this._playerB != null)
		{
			if (this._partyDuel && this._playerB.getParty() != null)
			{
				for (Player temp : this._playerB.getParty().getMembers())
				{
					temp.sendPacket(packet);
				}
			}
			else
			{
				this._playerB.sendPacket(packet);
			}
		}
	}

	public Player getWinner()
	{
		if (this._finished && this._playerA != null && this._playerB != null)
		{
			if (this._playerA.getDuelState() == 3)
			{
				return this._playerA;
			}
			return this._playerB.getDuelState() == 3 ? this._playerB : null;
		}
		return null;
	}

	public Player getLooser()
	{
		if (this._finished && this._playerA != null && this._playerB != null)
		{
			if (this._playerA.getDuelState() == 3)
			{
				return this._playerB;
			}
			return this._playerB.getDuelState() == 3 ? this._playerA : null;
		}
		return null;
	}

	public void playKneelAnimation()
	{
		Player looser = this.getLooser();
		if (looser != null)
		{
			if (this._partyDuel && looser.getParty() != null)
			{
				for (Player temp : looser.getParty().getMembers())
				{
					temp.broadcastPacket(new SocialAction(temp.getObjectId(), 7));
				}
			}
			else
			{
				looser.broadcastPacket(new SocialAction(looser.getObjectId(), 7));
			}
		}
	}

	public int countdown()
	{
		this._countdown--;
		if (this._countdown > 3)
		{
			return this._countdown;
		}
		SystemMessage sm = null;
		if (this._countdown > 0)
		{
			sm = new SystemMessage(SystemMessageId.THE_DUEL_STARTS_IN_S1_SEC);
			sm.addInt(this._countdown);
		}
		else
		{
			sm = new SystemMessage(SystemMessageId.LET_THE_DUEL_BEGIN);
		}

		this.broadcastToTeam1(sm);
		this.broadcastToTeam2(sm);
		return this._countdown;
	}

	public void endDuel(DuelResult result)
	{
		if (this._playerA != null && this._playerB != null)
		{
			SystemMessage sm = null;
			switch (result)
			{
				case CANCELED:
					this.stopFighting();
					this.restorePlayerConditions(true);
					sm = new SystemMessage(SystemMessageId.THE_DUEL_HAS_ENDED_IN_A_TIE);
					this.broadcastToTeam1(sm);
					this.broadcastToTeam2(sm);
				case CONTINUE:
				default:
					break;
				case TEAM_1_WIN:
				case TEAM_2_SURRENDER:
					this.restorePlayerConditions(false);
					sm = this._partyDuel ? new SystemMessage(SystemMessageId.C1_S_PARTY_HAS_WON_THE_DUEL) : new SystemMessage(SystemMessageId.C1_HAS_WON_THE_DUEL);
					sm.addString(this._playerA.getName());
					this.broadcastToTeam1(sm);
					this.broadcastToTeam2(sm);
					break;
				case TEAM_1_SURRENDER:
				case TEAM_2_WIN:
					this.restorePlayerConditions(false);
					sm = this._partyDuel ? new SystemMessage(SystemMessageId.C1_S_PARTY_HAS_WON_THE_DUEL) : new SystemMessage(SystemMessageId.C1_HAS_WON_THE_DUEL);
					sm.addString(this._playerB.getName());
					this.broadcastToTeam1(sm);
					this.broadcastToTeam2(sm);
					break;
				case TIMEOUT:
					this.stopFighting();
					this.restorePlayerConditions(false);
					sm = new SystemMessage(SystemMessageId.THE_DUEL_HAS_ENDED_IN_A_TIE);
					this.broadcastToTeam1(sm);
					this.broadcastToTeam2(sm);
			}

			ExDuelEnd duelEnd = this._partyDuel ? ExDuelEnd.PARTY_DUEL : ExDuelEnd.PLAYER_DUEL;
			this.broadcastToTeam1(duelEnd);
			this.broadcastToTeam2(duelEnd);
			this._playerConditions.clear();
			DuelManager.getInstance().removeDuel(this);
		}
		else
		{
			this._playerConditions.clear();
			DuelManager.getInstance().removeDuel(this);
		}
	}

	public DuelResult checkEndDuelCondition()
	{
		if (this._playerA == null || this._playerB == null || !this._playerA.isOnline() || !this._playerB.isOnline())
		{
			return DuelResult.CANCELED;
		}
		else if (this._surrenderRequest != 0)
		{
			return this._surrenderRequest == 1 ? DuelResult.TEAM_1_SURRENDER : DuelResult.TEAM_2_SURRENDER;
		}
		else if (this.getRemainingTime() <= 0)
		{
			return DuelResult.TIMEOUT;
		}
		else if (this._playerA.getDuelState() == 3)
		{
			this.stopFighting();
			return DuelResult.TEAM_1_WIN;
		}
		else if (this._playerB.getDuelState() == 3)
		{
			this.stopFighting();
			return DuelResult.TEAM_2_WIN;
		}
		else
		{
			if (!this._partyDuel)
			{
				if (this._playerA.getDuelState() == 4 || this._playerB.getDuelState() == 4 || !this._playerA.isInsideRadius2D(this._playerB, 1600) || this.isDuelistInPvp(true))
				{
					return DuelResult.CANCELED;
				}

				if (this._playerA.isInsideZone(ZoneId.PEACE) || this._playerB.isInsideZone(ZoneId.PEACE) || this._playerA.isInsideZone(ZoneId.NO_PVP) || this._playerB.isInsideZone(ZoneId.NO_PVP) || this._playerA.isInsideZone(ZoneId.SIEGE) || this._playerB.isInsideZone(ZoneId.SIEGE) || this._playerA.isInsideZone(ZoneId.PVP) || this._playerB.isInsideZone(ZoneId.PVP))
				{
					return DuelResult.CANCELED;
				}
			}

			return DuelResult.CONTINUE;
		}
	}

	public void doSurrender(Player player)
	{
		if (this._surrenderRequest == 0)
		{
			this.stopFighting();
			if (this._partyDuel)
			{
				if (this._playerA.getParty().getMembers().contains(player))
				{
					this._surrenderRequest = 1;

					for (Player temp : this._playerA.getParty().getMembers())
					{
						temp.setDuelState(2);
					}

					for (Player temp : this._playerB.getParty().getMembers())
					{
						temp.setDuelState(3);
					}
				}
				else if (this._playerB.getParty().getMembers().contains(player))
				{
					this._surrenderRequest = 2;

					for (Player temp : this._playerB.getParty().getMembers())
					{
						temp.setDuelState(2);
					}

					for (Player temp : this._playerA.getParty().getMembers())
					{
						temp.setDuelState(3);
					}
				}
			}
			else if (player == this._playerA)
			{
				this._surrenderRequest = 1;
				this._playerA.setDuelState(2);
				this._playerB.setDuelState(3);
			}
			else if (player == this._playerB)
			{
				this._surrenderRequest = 2;
				this._playerB.setDuelState(2);
				this._playerA.setDuelState(3);
			}
		}
	}

	public void onPlayerDefeat(Player player)
	{
		player.setDuelState(2);
		if (this._partyDuel)
		{
			boolean teamdefeated = player.getParty().getMembers().stream().anyMatch(member -> member.getDuelState() == 1);
			if (teamdefeated)
			{
				Player winner = this._playerA.getParty().getMembers().contains(player) ? this._playerB : this._playerA;

				for (Player temp : winner.getParty().getMembers())
				{
					temp.setDuelState(3);
				}
			}
		}
		else
		{
			if (player != this._playerA && player != this._playerB)
			{
				LOGGER.warning("Error in onPlayerDefeat(): player is not part of this 1vs1 duel");
			}

			if (this._playerA == player)
			{
				this._playerB.setDuelState(3);
			}
			else
			{
				this._playerA.setDuelState(3);
			}
		}
	}

	public void onRemoveFromParty(Player player)
	{
		if (this._partyDuel)
		{
			if (player != this._playerA && player != this._playerB)
			{
				Duel.PlayerCondition cond = this._playerConditions.remove(player.getObjectId());
				if (cond != null)
				{
					cond.teleportBack();
				}

				player.setInDuel(0);
			}
			else
			{
				for (Duel.PlayerCondition cond : this._playerConditions.values())
				{
					cond.teleportBack();
					cond.getPlayer().setInDuel(0);
				}

				this._playerA = null;
				this._playerB = null;
			}
		}
	}

	public void onBuff(Player player, Skill debuff)
	{
		Duel.PlayerCondition cond = this._playerConditions.get(player.getObjectId());
		if (cond != null)
		{
			cond.registerDebuff(debuff);
		}
	}

	public static class PlayerCondition
	{
		private Player _player;
		private double _hp;
		private double _mp;
		private double _cp;
		private boolean _paDuel;
		private int _x;
		private int _y;
		private int _z;
		private Set<Skill> _debuffs;

		public PlayerCondition(Player player, boolean partyDuel)
		{
			if (player != null)
			{
				this._player = player;
				this._hp = this._player.getCurrentHp();
				this._mp = this._player.getCurrentMp();
				this._cp = this._player.getCurrentCp();
				this._paDuel = partyDuel;
				if (this._paDuel)
				{
					this._x = this._player.getX();
					this._y = this._player.getY();
					this._z = this._player.getZ();
				}
			}
		}

		public void restoreCondition()
		{
			if (this._player != null)
			{
				this._player.setCurrentHp(this._hp);
				this._player.setCurrentMp(this._mp);
				this._player.setCurrentCp(this._cp);
				if (this._paDuel)
				{
					this.teleportBack();
				}

				if (this._debuffs != null)
				{
					for (Skill skill : this._debuffs)
					{
						if (skill != null)
						{
							this._player.stopSkillEffects(SkillFinishType.REMOVED, skill.getId());
						}
					}
				}
			}
		}

		public void registerDebuff(Skill debuff)
		{
			if (this._debuffs == null)
			{
				this._debuffs = ConcurrentHashMap.newKeySet();
			}

			this._debuffs.add(debuff);
		}

		public void teleportBack()
		{
			if (this._paDuel)
			{
				this._player.teleToLocation(this._x, this._y, this._z);
			}
		}

		public Player getPlayer()
		{
			return this._player;
		}
	}

	public class ScheduleDuelTask implements Runnable
	{
		private final Duel _duel;

		public ScheduleDuelTask(Duel duel)
		{
			Objects.requireNonNull(Duel.this);
			super();
			this._duel = duel;
		}

		@Override
		public void run()
		{
			try
			{
				switch (this._duel.checkEndDuelCondition())
				{
					case CANCELED:
						Duel.this.setFinished(true);
						this._duel.endDuel(DuelResult.CANCELED);
						break;
					case CONTINUE:
						ThreadPool.schedule(this, 1000L);
						break;
					default:
						Duel.this.setFinished(true);
						Duel.this.playKneelAnimation();
						ThreadPool.schedule(new Duel.ScheduleEndDuelTask(this._duel, this._duel.checkEndDuelCondition()), 5000L);
						if (Duel.this._duelInstance != null)
						{
							Duel.this._duelInstance.destroy();
						}
				}
			}
			catch (Exception var2)
			{
				Duel.LOGGER.log(Level.SEVERE, "There has been a problem while runing a duel task!", var2);
			}
		}
	}

	public static class ScheduleEndDuelTask implements Runnable
	{
		private final Duel _duel;
		private final DuelResult _result;

		public ScheduleEndDuelTask(Duel duel, DuelResult result)
		{
			this._duel = duel;
			this._result = result;
		}

		@Override
		public void run()
		{
			try
			{
				this._duel.endDuel(this._result);
			}
			catch (Exception var2)
			{
				Duel.LOGGER.log(Level.SEVERE, "There has been a problem while runing a duel end task!", var2);
			}
		}
	}

	public static class ScheduleStartDuelTask implements Runnable
	{
		private final Duel _duel;

		public ScheduleStartDuelTask(Duel duel)
		{
			this._duel = duel;
		}

		@Override
		public void run()
		{
			try
			{
				int count = this._duel.countdown();
				if (count == 4)
				{
					this._duel.savePlayerConditions();
					this._duel.teleportPlayers();
					ThreadPool.schedule(this, 20000L);
				}
				else if (count > 0)
				{
					ThreadPool.schedule(this, 1000L);
				}
				else
				{
					this._duel.startDuel();
				}
			}
			catch (Exception var2)
			{
				Duel.LOGGER.log(Level.SEVERE, "There has been a problem while runing a duel start task!", var2);
			}
		}
	}
}
