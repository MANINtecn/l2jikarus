package net.sf.l2jdev.gameserver.data.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.StringUtil;
import net.sf.l2jdev.gameserver.communitybbs.Manager.ForumsBBSManager;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.data.xml.ClanHallData;
import net.sf.l2jdev.gameserver.managers.ClanEntryManager;
import net.sf.l2jdev.gameserver.managers.FortManager;
import net.sf.l2jdev.gameserver.managers.FortSiegeManager;
import net.sf.l2jdev.gameserver.managers.IdManager;
import net.sf.l2jdev.gameserver.managers.SiegeManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.ClanMember;
import net.sf.l2jdev.gameserver.model.clan.ClanPrivileges;
import net.sf.l2jdev.gameserver.model.clan.ClanWar;
import net.sf.l2jdev.gameserver.model.clan.enums.ClanWarState;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerClanCreate;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerClanDestroy;
import net.sf.l2jdev.gameserver.model.events.holders.clan.OnClanWarFinish;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.residences.ClanHall;
import net.sf.l2jdev.gameserver.model.siege.Fort;
import net.sf.l2jdev.gameserver.model.siege.FortSiege;
import net.sf.l2jdev.gameserver.model.siege.Siege;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.UserInfoType;
import net.sf.l2jdev.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.PledgeShowMemberListAll;
import net.sf.l2jdev.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class ClanTable
{
	private static final Logger LOGGER = Logger.getLogger(ClanTable.class.getName());
	private final Map<Integer, Clan> _clans = new ConcurrentHashMap<>();

	protected ClanTable()
	{
		if (GeneralConfig.ENABLE_COMMUNITY_BOARD)
		{
			ForumsBBSManager.getInstance().initRoot();
		}

		List<Integer> cids = new ArrayList<>();

		try (Connection con = DatabaseFactory.getConnection(); Statement s = con.createStatement(); ResultSet rs = s.executeQuery("SELECT clan_id FROM clan_data");)
		{
			while (rs.next())
			{
				cids.add(rs.getInt("clan_id"));
			}
		}
		catch (Exception var13)
		{
			LOGGER.log(Level.SEVERE, "Error restoring ClanTable.", var13);
		}

		for (int cid : cids)
		{
			Clan clan = new Clan(cid);
			this._clans.put(cid, clan);
			if (clan.getDissolvingExpiryTime() != 0L)
			{
				this.scheduleRemoveClan(clan.getId());
			}
		}

		LOGGER.info(this.getClass().getSimpleName() + ": Restored " + cids.size() + " clans from the database.");
		this.allianceCheck();
		this.restoreClanWars();
		ThreadPool.scheduleAtFixedRate(this::updateClanRanks, 1000L, 1200000L);
	}

	public Collection<Clan> getClans()
	{
		return this._clans.values();
	}

	public int getClanCount()
	{
		return this._clans.size();
	}

	public Clan getClan(int clanId)
	{
		return this._clans.get(clanId);
	}

	public Clan getClanByName(String clanName)
	{
		for (Clan clan : this._clans.values())
		{
			if (clan.getName().equalsIgnoreCase(clanName))
			{
				return clan;
			}
		}

		return null;
	}

	public Clan createClan(Player player, String clanName)
	{
		if (player == null)
		{
			return null;
		}
		else if (player.getClanId() != 0)
		{
			player.sendPacket(SystemMessageId.FAILED_TO_CREATE_A_CLAN);
			return null;
		}
		else if (System.currentTimeMillis() < player.getClanCreateExpiryTime())
		{
			player.sendPacket(SystemMessageId.YOU_MUST_WAIT_10_DAYS_BEFORE_CREATING_A_NEW_CLAN);
			return null;
		}
		else if (!StringUtil.isAlphaNumeric(clanName) || clanName.length() < 2)
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_IS_INVALID);
			return null;
		}
		else if (clanName.length() > 16)
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_S_LENGTH_IS_INCORRECT);
			return null;
		}
		else if (this.getClanByName(clanName) != null)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_ALREADY_EXISTS);
			sm.addString(clanName);
			player.sendPacket(sm);
			return null;
		}
		else
		{
			Clan clan = new Clan(IdManager.getInstance().getNextId(), clanName);
			ClanMember leader = new ClanMember(clan, player);
			clan.setLeader(leader);
			leader.setPlayer(player);
			clan.store();
			player.setClan(clan);
			player.setPledgeClass(ClanMember.calculatePledgeClass(player));
			ClanPrivileges privileges = new ClanPrivileges();
			privileges.enableAll();
			player.setClanPrivileges(privileges);
			this._clans.put(clan.getId(), clan);
			player.sendPacket(new PledgeShowInfoUpdate(clan));
			PledgeShowMemberListAll.sendAllTo(player);
			player.sendPacket(new PledgeShowMemberListUpdate(player));
			player.sendPacket(SystemMessageId.YOUR_CLAN_HAS_BEEN_CREATED);
			player.broadcastUserInfo(UserInfoType.RELATION, UserInfoType.CLAN);
			if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_CLAN_CREATE))
			{
				EventDispatcher.getInstance().notifyEventAsync(new OnPlayerClanCreate(player, clan));
			}

			return clan;
		}
	}

	public synchronized void destroyClan(int clanId)
	{
		Clan clan = this.getClan(clanId);
		if (clan != null)
		{
			clan.broadcastToOnlineMembers(new SystemMessage(SystemMessageId.THE_CLAN_IS_DISBANDED));
			ClanEntryManager.getInstance().removeFromClanList(clan.getId());
			int castleId = clan.getCastleId();
			if (castleId == 0)
			{
				for (Siege siege : SiegeManager.getInstance().getSieges())
				{
					siege.removeSiegeClan(clan);
				}
			}

			int fortId = clan.getFortId();
			if (fortId == 0)
			{
				for (FortSiege siege : FortSiegeManager.getInstance().getSieges())
				{
					siege.removeAttacker(clan);
				}
			}

			ClanHall hall = ClanHallData.getInstance().getClanHallByClan(clan);
			if (hall != null)
			{
				hall.setOwner(null);
			}

			ClanMember leaderMember = clan.getLeader();
			if (leaderMember == null)
			{
				clan.getWarehouse().destroyAllItems(ItemProcessType.DESTROY, null, null);
			}
			else
			{
				clan.getWarehouse().destroyAllItems(ItemProcessType.DESTROY, clan.getLeader().getPlayer(), null);
			}

			for (ClanMember member : clan.getMembers())
			{
				clan.removeClanMember(member.getObjectId(), 0L);
			}

			this._clans.remove(clanId);
			IdManager.getInstance().releaseId(clanId);

			try (Connection con = DatabaseFactory.getConnection())
			{
				try (PreparedStatement ps = con.prepareStatement("DELETE FROM clan_data WHERE clan_id=?"))
				{
					ps.setInt(1, clanId);
					ps.execute();
				}

				try (PreparedStatement ps = con.prepareStatement("DELETE FROM clan_privs WHERE clan_id=?"))
				{
					ps.setInt(1, clanId);
					ps.execute();
				}

				try (PreparedStatement ps = con.prepareStatement("DELETE FROM clan_skills WHERE clan_id=?"))
				{
					ps.setInt(1, clanId);
					ps.execute();
				}

				try (PreparedStatement ps = con.prepareStatement("DELETE FROM clan_subpledges WHERE clan_id=?"))
				{
					ps.setInt(1, clanId);
					ps.execute();
				}

				try (PreparedStatement ps = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? OR clan2=?"))
				{
					ps.setInt(1, clanId);
					ps.setInt(2, clanId);
					ps.execute();
				}

				try (PreparedStatement ps = con.prepareStatement("DELETE FROM clan_notices WHERE clan_id=?"))
				{
					ps.setInt(1, clanId);
					ps.execute();
				}

				if (fortId != 0)
				{
					Fort fort = FortManager.getInstance().getFortById(fortId);
					if (fort != null)
					{
						Clan owner = fort.getOwnerClan();
						if (clan == owner)
						{
							fort.removeOwner(true);
						}
					}
				}
			}
			catch (Exception var25)
			{
				LOGGER.log(Level.SEVERE, this.getClass().getSimpleName() + ": Error removing clan from DB.", var25);
			}

			if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_CLAN_DESTROY))
			{
				EventDispatcher.getInstance().notifyEventAsync(new OnPlayerClanDestroy(leaderMember, clan));
			}
		}
	}

	public void scheduleRemoveClan(int clanId)
	{
		ThreadPool.schedule(() -> {
			if (this.getClan(clanId) != null)
			{
				if (this.getClan(clanId).getDissolvingExpiryTime() != 0L)
				{
					this.destroyClan(clanId);
				}
			}
		}, Math.max(this.getClan(clanId).getDissolvingExpiryTime() - System.currentTimeMillis(), 300000L));
	}

	public boolean isAllyExists(String allyName)
	{
		for (Clan clan : this._clans.values())
		{
			if (clan.getAllyName() != null && clan.getAllyName().equalsIgnoreCase(allyName))
			{
				return true;
			}
		}

		return false;
	}

	public void storeClanWars(ClanWar war)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("REPLACE INTO clan_wars (clan1, clan2, clan1Kill, clan2Kill, winnerClan, startTime, endTime, state) VALUES(?,?,?,?,?,?,?,?)");)
		{
			ps.setInt(1, war.getAttackerClanId());
			ps.setInt(2, war.getAttackedClanId());
			ps.setInt(3, war.getAttackerKillCount());
			ps.setInt(4, war.getAttackedKillCount());
			ps.setInt(5, war.getWinnerClanId());
			ps.setLong(6, war.getStartTime());
			ps.setLong(7, war.getEndTime());
			ps.setInt(8, war.getState().ordinal());
			ps.execute();
		}
		catch (Exception var10)
		{
			LOGGER.severe("Error storing clan wars data: " + var10);
		}
	}

	public void deleteClanWars(int clanId1, int clanId2)
	{
		Clan clan1 = getInstance().getClan(clanId1);
		Clan clan2 = getInstance().getClan(clanId2);
		if (EventDispatcher.getInstance().hasListener(EventType.ON_CLAN_WAR_FINISH))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnClanWarFinish(clan1, clan2));
		}

		clan1.deleteWar(clan2.getId());
		clan2.deleteWar(clan1.getId());
		clan1.broadcastClanStatus();
		clan2.broadcastClanStatus();

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM clan_wars WHERE (clan1=? AND clan2=?) OR (clan2=? AND clan1=?)");)
		{
			ps.setInt(1, clanId1);
			ps.setInt(2, clanId2);
			ps.setInt(3, clanId1);
			ps.setInt(4, clanId2);
			ps.execute();
		}
		catch (Exception var13)
		{
			LOGGER.log(Level.SEVERE, this.getClass().getSimpleName() + ": Error removing clan wars data.", var13);
		}
	}

	private void restoreClanWars()
	{
		try (Connection con = DatabaseFactory.getConnection(); Statement statement = con.createStatement(); ResultSet rset = statement.executeQuery("SELECT clan1, clan2, clan1Kill, clan2Kill, winnerClan, startTime, endTime, state FROM clan_wars");)
		{
			while (rset.next())
			{
				Clan attacker = this.getClan(rset.getInt("clan1"));
				Clan attacked = this.getClan(rset.getInt("clan2"));
				if (attacker != null && attacked != null)
				{
					ClanWarState state = ClanWarState.values()[rset.getInt("state")];
					ClanWar clanWar = new ClanWar(attacker, attacked, rset.getInt("clan1Kill"), rset.getInt("clan2Kill"), rset.getInt("winnerClan"), rset.getLong("startTime"), rset.getLong("endTime"), state);
					attacker.addWar(attacked.getId(), clanWar);
					attacked.addWar(attacker.getId(), clanWar);
				}
				else
				{
					LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Restorewars one of clans is null attacker:" + attacker + " attacked:" + attacked);
				}
			}
		}
		catch (Exception var14)
		{
			LOGGER.log(Level.SEVERE, this.getClass().getSimpleName() + ": Error restoring clan wars data.", var14);
		}
	}

	private void allianceCheck()
	{
		for (Clan clan : this._clans.values())
		{
			int allyId = clan.getAllyId();
			if (allyId != 0 && clan.getId() != allyId && !this._clans.containsKey(allyId))
			{
				clan.setAllyId(0);
				clan.setAllyName(null);
				clan.changeAllyCrest(0, true);
				clan.updateClanInDB();
				LOGGER.info(this.getClass().getSimpleName() + ": Removed alliance from clan: " + clan);
			}
		}
	}

	public List<Clan> getClanAllies(int allianceId)
	{
		List<Clan> clanAllies = new ArrayList<>();
		if (allianceId != 0)
		{
			for (Clan clan : this._clans.values())
			{
				if (clan != null && clan.getAllyId() == allianceId)
				{
					clanAllies.add(clan);
				}
			}
		}

		return clanAllies;
	}

	public void shutdown()
	{
		for (Clan clan : this._clans.values())
		{
			clan.updateClanInDB();
			clan.getVariables().saveNow();

			for (ClanWar war : clan.getWarList().values())
			{
				this.storeClanWars(war);
			}
		}
	}

	private void updateClanRanks()
	{
		for (Clan clan : this._clans.values())
		{
			clan.setRank(this.getClanRank(clan));
		}
	}

	public int getClanRank(Clan clan)
	{
		if (clan.getLevel() < 3)
		{
			return 0;
		}
		int rank = 1;

		for (Clan c : this._clans.values())
		{
			if (clan != c && (clan.getLevel() < c.getLevel() || clan.getLevel() == c.getLevel() && clan.getReputationScore() <= c.getReputationScore()))
			{
				rank++;
			}
		}

		return rank;
	}

	public static ClanTable getInstance()
	{
		return ClanTable.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final ClanTable INSTANCE = new ClanTable();
	}
}
