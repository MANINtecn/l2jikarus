package net.sf.l2jdev.gameserver.model.clan;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.config.FeatureConfig;
import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.enums.ClanWarState;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.clan.OnClanWarStart;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SurrenderPledgeWar;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class ClanWar
{
	public static final long TIME_TO_CANCEL_NON_MUTUAL_CLAN_WAR = TimeUnit.DAYS.toMillis(7L);
	public static final long TIME_TO_DELETION_AFTER_CANCELLATION = TimeUnit.DAYS.toMillis(5L);
	public static final long TIME_TO_DELETION_AFTER_DEFEAT = TimeUnit.DAYS.toMillis(21L);
	private final int _attackerClanId;
	private final int _attackedClanId;
	private int _winnerClanId = 0;
	private ClanWarState _state;
	private Future<?> _cancelTask;
	private final long _startTime;
	private long _endTime = 0L;
	private final AtomicInteger _attackerKillCount = new AtomicInteger();
	private final AtomicInteger _attackedKillCount = new AtomicInteger();
	private boolean _attackerDeclared;
	private boolean _attackedDeclared;

	public ClanWar(Clan attacker, Clan attacked)
	{
		this._attackerClanId = attacker.getId();
		this._attackedClanId = attacked.getId();
		this._startTime = System.currentTimeMillis();
		this._state = ClanWarState.BLOOD_DECLARATION;
		this._cancelTask = ThreadPool.schedule(this::clanWarTimeout, this._startTime + TIME_TO_CANCEL_NON_MUTUAL_CLAN_WAR - System.currentTimeMillis());
		attacker.addWar(attacked.getId(), this);
		attacked.addWar(attacker.getId(), this);
		if (EventDispatcher.getInstance().hasListener(EventType.ON_CLAN_WAR_START))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnClanWarStart(attacker, attacked));
		}

		SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_DECLARED_A_CLAN_WAR_WITH_S1);
		sm.addString(attacked.getName());
		attacker.broadcastToOnlineMembers(sm);
		sm = new SystemMessage(SystemMessageId.S1_HAS_DECLARED_A_CLAN_WAR_THE_WAR_WILL_AUTOMATICALLY_START_IF_YOU_KILL_MORE_THAN_5_S1_CLAN_MEMBERS_IN_A_WEEK);
		sm.addString(attacker.getName());
		attacked.broadcastToOnlineMembers(sm);
	}

	public ClanWar(Clan attacker, Clan attacked, int attackerKillCount, int attackedKillCount, int winnerClan, long startTime, long endTime, ClanWarState state)
	{
		this._attackerClanId = attacker.getId();
		this._attackedClanId = attacked.getId();
		this._startTime = startTime;
		this._endTime = endTime;
		this._state = state;
		this._attackerKillCount.set(attackerKillCount);
		this._attackedKillCount.set(attackedKillCount);
		this._winnerClanId = winnerClan;
		if (this._startTime + TIME_TO_CANCEL_NON_MUTUAL_CLAN_WAR > System.currentTimeMillis())
		{
			this._cancelTask = ThreadPool.schedule(this::clanWarTimeout, this._startTime + TIME_TO_CANCEL_NON_MUTUAL_CLAN_WAR - System.currentTimeMillis());
		}

		if (this._endTime > 0L)
		{
			long endTimePeriod = this._endTime + (this._state == ClanWarState.TIE ? TIME_TO_DELETION_AFTER_CANCELLATION : TIME_TO_DELETION_AFTER_DEFEAT);
			if (endTimePeriod > System.currentTimeMillis())
			{
				ThreadPool.schedule(() -> ClanTable.getInstance().deleteClanWars(this._attackerClanId, this._attackedClanId), 10000L);
			}
			else
			{
				ThreadPool.schedule(() -> ClanTable.getInstance().deleteClanWars(this._attackerClanId, this._attackedClanId), endTimePeriod);
			}
		}
	}

	public void onKill(Player killer, Player victim)
	{
		Clan victimClan = victim.getClan();
		Clan killerClan = killer.getClan();
		if (victim.getLevel() > 4 && this._state == ClanWarState.MUTUAL)
		{
			if (victimClan.getReputationScore() > 0)
			{
				victimClan.takeReputationScore(FeatureConfig.REPUTATION_SCORE_PER_KILL);
				killerClan.addReputationScore(FeatureConfig.REPUTATION_SCORE_PER_KILL);
			}

			SystemMessage sm = new SystemMessage(SystemMessageId.C1_IS_KILLED_BY_A_MEMBER_OF_THE_S2_CLAN_CLAN_REPUTATION_POINTS_1);
			sm.addPcName(victim);
			sm.addString(killerClan.getName());
			victimClan.broadcastToOnlineMembers(sm);
			sm = new SystemMessage(SystemMessageId.A_MEMBER_OF_THE_S1_CLAN_IS_KILLED_BY_C2_CLAN_REPUTATION_POINTS_1);
			sm.addString(victimClan.getName());
			sm.addPcName(killer);
			killerClan.broadcastToOnlineMembers(sm);
			if (killerClan.getId() == this._attackerClanId)
			{
				this._attackerKillCount.incrementAndGet();
			}
			else
			{
				this._attackedKillCount.incrementAndGet();
			}
		}
		else if (this._state == ClanWarState.BLOOD_DECLARATION && victimClan.getId() == this._attackerClanId && victim.getReputation() >= 0)
		{
			int killCount = this._attackedKillCount.incrementAndGet();
			if (killCount >= 5)
			{
				this._state = ClanWarState.MUTUAL;
				SystemMessage sm = new SystemMessage(SystemMessageId.A_CLAN_WAR_WITH_CLAN_S1_HAS_STARTED_THE_CLAN_THAT_CANCELS_THE_WAR_FIRST_WILL_LOSE_500_CLAN_REPUTATION_POINTS_IF_YOUR_CLAN_MEMBER_GETS_KILLED_BY_THE_OTHER_CLAN_XP_DECREASES_BY_1_4_OF_THE_AMOUNT_THAT_DECREASES_IN_HUNTING_ZONES);
				sm.addString(victimClan.getName());
				killerClan.broadcastToOnlineMembers(sm);
				sm = new SystemMessage(SystemMessageId.A_CLAN_WAR_WITH_CLAN_S1_HAS_STARTED_THE_CLAN_THAT_CANCELS_THE_WAR_FIRST_WILL_LOSE_500_CLAN_REPUTATION_POINTS_IF_YOUR_CLAN_MEMBER_GETS_KILLED_BY_THE_OTHER_CLAN_XP_DECREASES_BY_1_4_OF_THE_AMOUNT_THAT_DECREASES_IN_HUNTING_ZONES);
				sm.addString(killerClan.getName());
				victimClan.broadcastToOnlineMembers(sm);
				if (this._cancelTask != null)
				{
					this._cancelTask.cancel(true);
					this._cancelTask = null;
				}
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.A_CLAN_MEMBER_OF_S1_WAS_KILLED_BY_YOUR_CLAN_MEMBER_IF_YOUR_CLAN_KILLS_S2_MEMBERS_OF_CLAN_S1_A_CLAN_WAR_WITH_CLAN_S1_WILL_START);
				sm.addString(victimClan.getName());
				sm.addInt(5 - killCount);
				killerClan.broadcastToOnlineMembers(sm);
			}
		}
	}

	public void cancel(Player player, Clan cancelor)
	{
		Clan winnerClan = cancelor.getId() == this._attackerClanId ? ClanTable.getInstance().getClan(this._attackedClanId) : ClanTable.getInstance().getClan(this._attackerClanId);
		cancelor.takeReputationScore(500);
		player.sendPacket(new SurrenderPledgeWar(cancelor.getName(), player.getName()));
		SystemMessage sm = new SystemMessage(SystemMessageId.THE_WAR_IS_OVER_AS_YOU_VE_ADMITTED_DEFEAT_FROM_THE_CLAN_S1_YOU_VE_LOST);
		sm.addString(winnerClan.getName());
		cancelor.broadcastToOnlineMembers(sm);
		sm = new SystemMessage(SystemMessageId.THE_WAR_ENDED_BY_THE_S1_CLAN_S_DEFEAT_DECLARATION_YOU_HAVE_WON_THE_CLAN_WAR_OVER_THE_S1_CLAN);
		sm.addString(cancelor.getName());
		winnerClan.broadcastToOnlineMembers(sm);
		this._winnerClanId = winnerClan.getId();
		this._endTime = System.currentTimeMillis();
		ThreadPool.schedule(() -> ClanTable.getInstance().deleteClanWars(cancelor.getId(), winnerClan.getId()), 5000L);
	}

	public void clanWarTimeout()
	{
		Clan attackerClan = ClanTable.getInstance().getClan(this._attackerClanId);
		Clan attackedClan = ClanTable.getInstance().getClan(this._attackedClanId);
		if (attackerClan != null && attackedClan != null)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.THE_WAR_DECLARED_BY_THE_S1_CLAN_HAS_ENDED);
			sm.addString(attackerClan.getName());
			attackedClan.broadcastToOnlineMembers(sm);
			sm = new SystemMessage(SystemMessageId.S1_THE_CLAN_HAS_NOT_FOUGHT_BACK_FOR_1_WEEK_THE_WAR_IS_OVER);
			sm.addString(attackedClan.getName());
			attackerClan.broadcastToOnlineMembers(sm);
			this._state = ClanWarState.TIE;
			this._endTime = System.currentTimeMillis();
			ThreadPool.schedule(() -> ClanTable.getInstance().deleteClanWars(attackerClan.getId(), attackedClan.getId()), 5000L);
		}
	}

	public void mutualClanWarAccepted(Clan attacker, Clan attacked)
	{
		if (attacker.getId() == this._attackerClanId)
		{
			this._attackerDeclared = true;
		}
		else if (attacker.getId() == this._attackedClanId)
		{
			this._attackedDeclared = true;
		}

		if (this._attackerDeclared && this._attackedDeclared)
		{
			this._state = ClanWarState.MUTUAL;
			SystemMessage sm = new SystemMessage(SystemMessageId.A_CLAN_WAR_WITH_CLAN_S1_HAS_STARTED_THE_CLAN_THAT_CANCELS_THE_WAR_FIRST_WILL_LOSE_500_CLAN_REPUTATION_POINTS_IF_YOUR_CLAN_MEMBER_GETS_KILLED_BY_THE_OTHER_CLAN_XP_DECREASES_BY_1_4_OF_THE_AMOUNT_THAT_DECREASES_IN_HUNTING_ZONES);
			sm.addString(attacker.getName());
			attacked.broadcastToOnlineMembers(sm);
			sm = new SystemMessage(SystemMessageId.A_CLAN_WAR_WITH_CLAN_S1_HAS_STARTED_THE_CLAN_THAT_CANCELS_THE_WAR_FIRST_WILL_LOSE_500_CLAN_REPUTATION_POINTS_IF_YOUR_CLAN_MEMBER_GETS_KILLED_BY_THE_OTHER_CLAN_XP_DECREASES_BY_1_4_OF_THE_AMOUNT_THAT_DECREASES_IN_HUNTING_ZONES);
			sm.addString(attacked.getName());
			attacker.broadcastToOnlineMembers(sm);
			if (this._cancelTask != null)
			{
				this._cancelTask.cancel(true);
				this._cancelTask = null;
			}
		}
	}

	public int getKillDifference(Clan clan)
	{
		return this._attackerClanId == clan.getId() ? this._attackerKillCount.get() - this._attackedKillCount.get() : this._attackedKillCount.get() - this._attackerKillCount.get();
	}

	public ClanWarState getClanWarState(Clan clan)
	{
		if (this._winnerClanId > 0)
		{
			return this._winnerClanId == clan.getId() ? ClanWarState.WIN : ClanWarState.LOSS;
		}
		return this._state;
	}

	public ClanWar.WarProgress calculateWarProgress(Clan clan)
	{
		int pointDiff = this.getKillDifference(clan);
		if (pointDiff <= -50)
		{
			return ClanWar.WarProgress.VERY_LOW;
		}
		else if (pointDiff > -50 && pointDiff <= -20)
		{
			return ClanWar.WarProgress.LOW;
		}
		else if (pointDiff > -20 && pointDiff <= 19)
		{
			return ClanWar.WarProgress.NORMAL;
		}
		else
		{
			return pointDiff > 19 && pointDiff <= 49 ? ClanWar.WarProgress.HIGH : ClanWar.WarProgress.VERY_HIGH;
		}
	}

	public int getAttackerClanId()
	{
		return this._attackerClanId;
	}

	public int getAttackedClanId()
	{
		return this._attackedClanId;
	}

	public int getAttackerKillCount()
	{
		return this._attackerKillCount.get();
	}

	public int getAttackedKillCount()
	{
		return this._attackedKillCount.get();
	}

	public int getWinnerClanId()
	{
		return this._winnerClanId;
	}

	public long getStartTime()
	{
		return this._startTime;
	}

	public long getEndTime()
	{
		return this._endTime;
	}

	public ClanWarState getState()
	{
		return this._state;
	}

	public int getKillToStart()
	{
		return this._state == ClanWarState.BLOOD_DECLARATION ? 5 - this._attackedKillCount.get() : 0;
	}

	public int getRemainingTime()
	{
		return (int) ((this._startTime + TIME_TO_CANCEL_NON_MUTUAL_CLAN_WAR - System.currentTimeMillis()) / 1000L);
	}

	public Clan getOpposingClan(Clan clan)
	{
		return this._attackerClanId == clan.getId() ? ClanTable.getInstance().getClan(this._attackedClanId) : ClanTable.getInstance().getClan(this._attackerClanId);
	}

	public static enum WarProgress
	{
		VERY_LOW,
		LOW,
		NORMAL,
		HIGH,
		VERY_HIGH;
	}
}
