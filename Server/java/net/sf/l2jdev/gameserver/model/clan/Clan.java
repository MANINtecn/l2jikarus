package net.sf.l2jdev.gameserver.model.clan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.StringUtil;
import net.sf.l2jdev.gameserver.communitybbs.BB.Forum;
import net.sf.l2jdev.gameserver.communitybbs.Manager.ForumsBBSManager;
import net.sf.l2jdev.gameserver.config.FeatureConfig;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.sql.CharInfoTable;
import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.data.sql.CrestTable;
import net.sf.l2jdev.gameserver.data.xml.ClanLevelData;
import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.data.xml.SkillTreeData;
import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.managers.FortManager;
import net.sf.l2jdev.gameserver.managers.SiegeManager;
import net.sf.l2jdev.gameserver.model.BlockList;
import net.sf.l2jdev.gameserver.model.SkillLearn;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.enums.ClanRewardType;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerClanJoin;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerClanLeaderChange;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerClanLeft;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerClanLvlUp;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.itemcontainer.ClanWarehouse;
import net.sf.l2jdev.gameserver.model.itemcontainer.ItemContainer;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.model.siege.Fort;
import net.sf.l2jdev.gameserver.model.siege.MercenaryPledgeHolder;
import net.sf.l2jdev.gameserver.model.skill.CommonSkill;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.enums.SkillFinishType;
import net.sf.l2jdev.gameserver.model.variables.ClanVariables;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.UserInfoType;
import net.sf.l2jdev.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2jdev.gameserver.network.serverpackets.ExSubPledgeSkillAdd;
import net.sf.l2jdev.gameserver.network.serverpackets.PledgeReceiveSubPledgeCreated;
import net.sf.l2jdev.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.PledgeShowMemberListAll;
import net.sf.l2jdev.gameserver.network.serverpackets.PledgeShowMemberListDeleteAll;
import net.sf.l2jdev.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.PledgeSkillList;
import net.sf.l2jdev.gameserver.network.serverpackets.PledgeSkillListAdd;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.UserInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.pledgeV3.ExAllianceCreateResult;
import net.sf.l2jdev.gameserver.network.serverpackets.pledgeV3.ExPledgeLevelUp;
import net.sf.l2jdev.gameserver.network.serverpackets.pledgeV3.ExPledgeV3Info;
import net.sf.l2jdev.gameserver.network.serverpackets.pledgebonus.ExPledgeBonusMarkReset;

public class Clan
{
	private static final Logger LOGGER = Logger.getLogger(Clan.class.getName());
	public static final String INSERT_CLAN_DATA = "INSERT INTO clan_data (clan_id,clan_name,clan_level,hasCastle,blood_alliance_count,blood_oath_count,ally_id,ally_name,leader_id,crest_id,crest_large_id,ally_crest_id,new_leader_id,exp) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	public static final String SELECT_CLAN_DATA = "SELECT * FROM clan_data where clan_id=?";
	public static final String TAX_RATE_VAR = "TAX_RATE_";
	public static final int MAX_TAX_RATE = 10;
	public static final int PENALTY_TYPE_CLAN_LEAVED = 1;
	public static final int PENALTY_TYPE_CLAN_DISMISSED = 2;
	public static final int PENALTY_TYPE_DISMISS_CLAN = 3;
	public static final int PENALTY_TYPE_DISSOLVE_ALLY = 4;
	public static final int SUBUNIT_ACADEMY = -1;
	public static final int SUBUNIT_ROYAL1 = 100;
	public static final int SUBUNIT_ROYAL2 = 200;
	public static final int SUBUNIT_KNIGHT1 = 1001;
	public static final int SUBUNIT_KNIGHT2 = 1002;
	public static final int SUBUNIT_KNIGHT3 = 2001;
	public static final int SUBUNIT_KNIGHT4 = 2002;
	private String _name;
	private int _clanId;
	private ClanMember _leader;
	private final Map<Integer, ClanMember> _members = new ConcurrentHashMap<>();
	private String _allyName;
	private int _allyId = 0;
	private int _level;
	private int _castleId;
	private int _fortId;
	private int _hideoutId;
	private int _hiredGuards;
	private int _crestId;
	private int _crestLargeId;
	private int _allyCrestId;
	private int _auctionBiddedAt = 0;
	private long _allyPenaltyExpiryTime;
	private int _allyPenaltyType;
	private long _charPenaltyExpiryTime;
	private long _dissolvingExpiryTime;
	private int _bloodAllianceCount;
	private int _bloodOathCount;
	private static int _mercenaryId = 1;
	private final ItemContainer _warehouse = new ClanWarehouse(this);
	private final ConcurrentHashMap<Integer, ClanWar> _atWarWith = new ConcurrentHashMap<>();
	private Forum _forum;
	private final Map<Integer, Skill> _skills = new ConcurrentSkipListMap<>();
	private final Map<Integer, Clan.RankPrivs> _privs = new ConcurrentSkipListMap<>();
	private final Map<Integer, Clan.SubPledge> _subPledges = new ConcurrentSkipListMap<>();
	private final Map<Integer, Skill> _subPledgeSkills = new ConcurrentSkipListMap<>();
	private int _reputationScore = 0;
	private int _rank = 0;
	private int _exp = 0;
	private String _notice;
	private boolean _noticeEnabled = false;
	private int _newLeaderId;
	private final AtomicInteger _siegeKills = new AtomicInteger();
	private final AtomicInteger _siegeDeaths = new AtomicInteger();
	private ClanRewardBonus _lastMembersOnlineBonus = null;
	private ClanRewardBonus _lastHuntingBonus = null;
	private volatile ClanVariables _vars;
	private final Map<Integer, MercenaryPledgeHolder> _mercenaries = new HashMap<>();

	public Clan(int clanId)
	{
		this._clanId = clanId;
		this.initializePrivs();
		this.restore();
		this._warehouse.restore();
		ClanRewardBonus availableOnlineBonus = ClanRewardType.MEMBERS_ONLINE.getAvailableBonus(this);
		if (this._lastMembersOnlineBonus == null && availableOnlineBonus != null)
		{
			this._lastMembersOnlineBonus = availableOnlineBonus;
		}

		ClanRewardBonus availableHuntingBonus = ClanRewardType.HUNTING_MONSTERS.getAvailableBonus(this);
		if (this._lastHuntingBonus == null && availableHuntingBonus != null)
		{
			this._lastHuntingBonus = availableHuntingBonus;
		}

		this.restoreMercenary();
	}

	public Clan(int clanId, String clanName)
	{
		this._clanId = clanId;
		this._name = clanName;
		this.initializePrivs();
	}

	public int getId()
	{
		return this._clanId;
	}

	public void setClanId(int clanId)
	{
		this._clanId = clanId;
	}

	public int getLeaderId()
	{
		return this._leader != null ? this._leader.getObjectId() : 0;
	}

	public ClanMember getLeader()
	{
		return this._leader;
	}

	public void setLeader(ClanMember leader)
	{
		this._leader = leader;
		this._members.put(leader.getObjectId(), leader);
	}

	public void setNewLeader(ClanMember member)
	{
		Player newLeader = member.getPlayer();
		ClanMember exMember = this._leader;
		Player exLeader = exMember.getPlayer();
		if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_CLAN_LEADER_CHANGE))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerClanLeaderChange(exMember, member, this));
		}

		if (exLeader != null)
		{
			if (exLeader.isFlying())
			{
				exLeader.dismount();
			}

			if (this.getLevel() >= SiegeManager.getInstance().getSiegeClanMinLevel())
			{
				SiegeManager.getInstance().removeSiegeSkills(exLeader);
			}

			exLeader.getClanPrivileges().disableAll();
			exLeader.broadcastUserInfo();
		}
		else
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE characters SET clan_privs = ? WHERE charId = ?");)
			{
				ps.setInt(1, 0);
				ps.setInt(2, this.getLeaderId());
				ps.execute();
			}
			catch (Exception var18)
			{
				LOGGER.log(Level.WARNING, "Couldn't update clan privs for old clan leader", var18);
			}
		}

		this.setLeader(member);
		if (this._newLeaderId != 0)
		{
			this.setNewLeaderId(0, true);
		}

		this.updateClanInDB();
		if (exLeader != null)
		{
			exLeader.setPledgeClass(ClanMember.calculatePledgeClass(exLeader));
			exLeader.broadcastUserInfo();
			exLeader.checkItemRestriction();
		}

		if (newLeader != null)
		{
			newLeader.setPledgeClass(ClanMember.calculatePledgeClass(newLeader));
			newLeader.getClanPrivileges().enableAll();
			if (this.getLevel() >= SiegeManager.getInstance().getSiegeClanMinLevel())
			{
				SiegeManager.getInstance().addSiegeSkills(newLeader);
			}

			newLeader.broadcastUserInfo();
		}
		else
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE characters SET clan_privs = ? WHERE charId = ?");)
			{
				ps.setInt(1, ClanPrivileges.getCompleteMask());
				ps.setInt(2, this.getLeaderId());
				ps.execute();
			}
			catch (Exception var15)
			{
				LOGGER.log(Level.WARNING, "Couldn't update clan privs for new clan leader", var15);
			}
		}

		this.broadcastClanStatus();
		this.broadcastToOnlineMembers(new SystemMessage(SystemMessageId.CLAN_LEADER_PRIVILEGES_HAVE_BEEN_TRANSFERRED_TO_C1).addString(member.getName()));
		LOGGER.info("Leader of Clan: " + this.getName() + " changed to: " + member.getName() + " ex leader: " + exMember.getName());
	}

	public String getLeaderName()
	{
		if (this._leader == null)
		{
			LOGGER.warning("Clan " + this._name + " without clan leader!");
			return "";
		}
		return this._leader.getName();
	}

	public String getName()
	{
		return this._name;
	}

	public void setName(String name)
	{
		this._name = name;
	}

	private void addClanMember(ClanMember member)
	{
		this._members.put(member.getObjectId(), member);
	}

	public void addClanMember(Player player)
	{
		ClanMember member = new ClanMember(this, player);
		this.addClanMember(member);
		member.setPlayer(player);
		player.setClan(this);
		player.setPledgeClass(ClanMember.calculatePledgeClass(player));
		player.sendPacket(new PledgeShowMemberListUpdate(player));
		player.sendPacket(new PledgeSkillList(this));
		this.addSkillEffects(player);
		if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_CLAN_JOIN))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerClanJoin(member, this));
		}
	}

	public void updateClanMember(Player player)
	{
		ClanMember member = new ClanMember(player.getClan(), player);
		if (player.isClanLeader())
		{
			this.setLeader(member);
		}

		this.addClanMember(member);
	}

	public ClanMember getClanMember(String name)
	{
		for (ClanMember temp : this._members.values())
		{
			if (temp.getName().equals(name))
			{
				return temp;
			}
		}

		return null;
	}

	public ClanMember getClanMember(int objectId)
	{
		return this._members.get(objectId);
	}

	public void removeClanMember(int objectId, long clanJoinExpiryTime)
	{
		ClanMember exMember = this._members.remove(objectId);
		if (exMember == null)
		{
			LOGGER.warning("Member Object ID: " + objectId + " not found in clan while trying to remove");
		}
		else
		{
			int leadssubpledge = this.getLeaderSubPledge(objectId);
			if (leadssubpledge != 0)
			{
				this.getSubPledge(leadssubpledge).setLeaderId(0);
				this.updateSubPledgeInDB(leadssubpledge);
			}

			if (exMember.getApprentice() != 0)
			{
				ClanMember apprentice = this.getClanMember(exMember.getApprentice());
				if (apprentice != null)
				{
					if (apprentice.getPlayer() != null)
					{
						apprentice.getPlayer().setSponsor(0);
					}
					else
					{
						apprentice.setApprenticeAndSponsor(0, 0);
					}

					apprentice.saveApprenticeAndSponsor(0, 0);
				}
			}

			if (exMember.getSponsor() != 0)
			{
				ClanMember sponsor = this.getClanMember(exMember.getSponsor());
				if (sponsor != null)
				{
					if (sponsor.getPlayer() != null)
					{
						sponsor.getPlayer().setApprentice(0);
					}
					else
					{
						sponsor.setApprenticeAndSponsor(0, 0);
					}

					sponsor.saveApprenticeAndSponsor(0, 0);
				}
			}

			exMember.saveApprenticeAndSponsor(0, 0);
			if (PlayerConfig.REMOVE_CASTLE_CIRCLETS)
			{
				CastleManager.getInstance().removeCirclet(exMember, this.getCastleId());
			}

			if (exMember.isOnline())
			{
				Player player = exMember.getPlayer();
				if (!player.isNoble())
				{
					player.setTitle("");
				}

				player.setApprentice(0);
				player.setSponsor(0);
				if (player.isClanLeader())
				{
					SiegeManager.getInstance().removeSiegeSkills(player);
					player.setClanCreateExpiryTime(System.currentTimeMillis() + PlayerConfig.ALT_CLAN_CREATE_DAYS * 86400000);
				}

				this.removeSkillEffects(player);
				player.getEffectList().stopSkillEffects(SkillFinishType.REMOVED, CommonSkill.CLAN_ADVENT.getId());
				if (this.getCastleId() > 0)
				{
					Castle castle = CastleManager.getInstance().getCastleByOwner(this);
					if (castle != null)
					{
						castle.removeResidentialSkills(player);
					}
				}

				if (this.getFortId() > 0)
				{
					Fort fort = FortManager.getInstance().getFortByOwner(this);
					if (fort != null)
					{
						fort.removeResidentialSkills(player);
					}
				}

				player.sendSkillList();
				player.setClan(null);
				if (exMember.getPledgeType() != -1)
				{
					player.setClanJoinExpiryTime(clanJoinExpiryTime);
				}

				player.setPledgeClass(ClanMember.calculatePledgeClass(player));
				player.broadcastUserInfo();
				player.sendPacket(PledgeShowMemberListDeleteAll.STATIC_PACKET);
			}
			else
			{
				this.removeMemberInDatabase(exMember, clanJoinExpiryTime, this.getLeaderId() == objectId ? System.currentTimeMillis() + PlayerConfig.ALT_CLAN_CREATE_DAYS * 86400000 : 0L);
			}

			if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_CLAN_LEFT))
			{
				EventDispatcher.getInstance().notifyEventAsync(new OnPlayerClanLeft(exMember, this));
			}
		}
	}

	public Collection<ClanMember> getMembers()
	{
		return this._members.values();
	}

	public int getMembersCount()
	{
		return this._members.size();
	}

	public int getSubPledgeMembersCount(int subpl)
	{
		int result = 0;

		for (ClanMember temp : this._members.values())
		{
			if (temp.getPledgeType() == subpl)
			{
				result++;
			}
		}

		return result;
	}

	public int getMaxNrOfMembers(int pledgeType)
	{
		int limit = 0;
		switch (pledgeType)
		{
			case -1:
				limit = 20;
				break;
			case 0:
				switch (this._level)
				{
					case 0:
						int limitxxxx = 10;
						return limitxxxx;
					case 1:
						int limitxxx = 15;
						return limitxxx;
					case 2:
						int limitxx = 20;
						return limitxx;
					case 3:
						int limitx = 30;
						return limitx;
					default:
						int limitxxxxx = 40;
						return limitxxxxx;
				}
			case 100:
			case 200:
				switch (this._level)
				{
					case 11:
						int limitx = 30;
						return limitx;
					default:
						int limitxx = 20;
						return limitxx;
				}
			case 1001:
			case 1002:
			case 2001:
			case 2002:
				switch (this._level)
				{
					case 9:
					case 10:
					case 11:
						limit = 25;
						break;
					default:
						limit = 10;
				}
		}

		return limit;
	}

	public List<Player> getOnlineMembers(int exclude)
	{
		List<Player> result = new ArrayList<>();

		for (ClanMember member : this._members.values())
		{
			if (member.getObjectId() != exclude && member.isOnline() && member.getPlayer() != null)
			{
				result.add(member.getPlayer());
			}
		}

		return result;
	}

	public int getOnlineMembersCount()
	{
		int count = 0;

		for (ClanMember member : this._members.values())
		{
			if (member.isOnline())
			{
				count++;
			}
		}

		return count;
	}

	public int getAllyId()
	{
		return this._allyId;
	}

	public String getAllyName()
	{
		return this._allyName;
	}

	public void setAllyCrestId(int allyCrestId)
	{
		this._allyCrestId = allyCrestId;
	}

	public int getAllyCrestId()
	{
		return this._allyCrestId;
	}

	public int getLevel()
	{
		return this._level;
	}

	private void setLevel(int level)
	{
		this._level = level;
		if (this._level >= 2 && this._forum == null && GeneralConfig.ENABLE_COMMUNITY_BOARD)
		{
			Forum forum = ForumsBBSManager.getInstance().getForumByName("ClanRoot");
			if (forum != null)
			{
				this._forum = forum.getChildByName(this._name);
				if (this._forum == null)
				{
					this._forum = ForumsBBSManager.getInstance().createNewForum(this._name, ForumsBBSManager.getInstance().getForumByName("ClanRoot"), 2, 2, this.getId());
				}
			}
		}
	}

	public int getCastleId()
	{
		return this._castleId;
	}

	public int getFortId()
	{
		return this._fortId;
	}

	public int getHideoutId()
	{
		return this._hideoutId;
	}

	public void setCrestId(int crestId)
	{
		this._crestId = crestId;
	}

	public int getCrestId()
	{
		return this._crestId;
	}

	public void setCrestLargeId(int crestLargeId)
	{
		this._crestLargeId = crestLargeId;
	}

	public int getCrestLargeId()
	{
		return this._crestLargeId;
	}

	public void setAllyId(int allyId)
	{
		this._allyId = allyId;
	}

	public void setAllyName(String allyName)
	{
		this._allyName = allyName;
	}

	public void setCastleId(int castleId)
	{
		this._castleId = castleId;
	}

	public void setFortId(int fortId)
	{
		this._fortId = fortId;
	}

	public void setHideoutId(int hideoutId)
	{
		this._hideoutId = hideoutId;
	}

	public boolean isMember(int id)
	{
		return id != 0 && this._members.containsKey(id);
	}

	public int getBloodAllianceCount()
	{
		return this._bloodAllianceCount;
	}

	public void increaseBloodAllianceCount()
	{
		this._bloodAllianceCount = this._bloodAllianceCount + SiegeManager.getInstance().getBloodAllianceReward();
		this.updateBloodAllianceCountInDB();
	}

	public void resetBloodAllianceCount()
	{
		this._bloodAllianceCount = 0;
		this.updateBloodAllianceCountInDB();
	}

	public void updateBloodAllianceCountInDB()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE clan_data SET blood_alliance_count=? WHERE clan_id=?");)
		{
			ps.setInt(1, this._bloodAllianceCount);
			ps.setInt(2, this._clanId);
			ps.execute();
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.WARNING, "Exception on updateBloodAllianceCountInDB(): " + var9.getMessage(), var9);
		}
	}

	public int getBloodOathCount()
	{
		return this._bloodOathCount;
	}

	public void increaseBloodOathCount()
	{
		this._bloodOathCount = this._bloodOathCount + FeatureConfig.FS_BLOOD_OATH_COUNT;
		this.updateBloodOathCountInDB();
	}

	public void resetBloodOathCount()
	{
		this._bloodOathCount = 0;
		this.updateBloodOathCountInDB();
	}

	public void updateBloodOathCountInDB()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE clan_data SET blood_oath_count=? WHERE clan_id=?");)
		{
			ps.setInt(1, this._bloodOathCount);
			ps.setInt(2, this._clanId);
			ps.execute();
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.WARNING, "Exception on updateBloodAllianceCountInDB(): " + var9.getMessage(), var9);
		}
	}

	public void updateClanInDB()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE clan_data SET leader_id=?,ally_id=?,ally_name=?,reputation_score=?,ally_penalty_expiry_time=?,ally_penalty_type=?,char_penalty_expiry_time=?,dissolving_expiry_time=?,new_leader_id=?,exp=? WHERE clan_id=?");)
		{
			ps.setInt(1, this.getLeaderId());
			ps.setInt(2, this._allyId);
			ps.setString(3, this._allyName);
			ps.setInt(4, this._reputationScore);
			ps.setLong(5, this._allyPenaltyExpiryTime);
			ps.setInt(6, this._allyPenaltyType);
			ps.setLong(7, this._charPenaltyExpiryTime);
			ps.setLong(8, this._dissolvingExpiryTime);
			ps.setInt(9, this._newLeaderId);
			ps.setInt(10, this._exp);
			ps.setInt(11, this._clanId);
			ps.execute();
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.SEVERE, "Error saving clan: " + var9.getMessage(), var9);
		}
	}

	public void store()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO clan_data (clan_id,clan_name,clan_level,hasCastle,blood_alliance_count,blood_oath_count,ally_id,ally_name,leader_id,crest_id,crest_large_id,ally_crest_id,new_leader_id,exp) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");)
		{
			ps.setInt(1, this._clanId);
			ps.setString(2, this._name);
			ps.setInt(3, this._level);
			ps.setInt(4, this._castleId);
			ps.setInt(5, this._bloodAllianceCount);
			ps.setInt(6, this._bloodOathCount);
			ps.setInt(7, this._allyId);
			ps.setString(8, this._allyName);
			ps.setInt(9, this.getLeaderId());
			ps.setInt(10, this._crestId);
			ps.setInt(11, this._crestLargeId);
			ps.setInt(12, this._allyCrestId);
			ps.setInt(13, this._newLeaderId);
			ps.setInt(14, this._exp);
			ps.execute();
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.SEVERE, "Error saving new clan: " + var9.getMessage(), var9);
		}
	}

	public void removeMemberInDatabase(ClanMember member, long clanJoinExpiryTime, long clanCreateExpiryTime)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps1 = con.prepareStatement("UPDATE characters SET clanid=0, title=?, clan_join_expiry_time=?, clan_create_expiry_time=?, clan_privs=0, wantspeace=0, subpledge=0, lvl_joined_academy=0, apprentice=0, sponsor=0 WHERE charId=?");
			PreparedStatement ps2 = con.prepareStatement("UPDATE characters SET apprentice=0 WHERE apprentice=?");
			PreparedStatement ps3 = con.prepareStatement("UPDATE characters SET sponsor=0 WHERE sponsor=?");)
		{
			ps1.setString(1, "");
			ps1.setLong(2, clanJoinExpiryTime);
			ps1.setLong(3, clanCreateExpiryTime);
			ps1.setInt(4, member.getObjectId());
			ps1.execute();
			ps2.setInt(1, member.getObjectId());
			ps2.execute();
			ps3.setInt(1, member.getObjectId());
			ps3.execute();
		}
		catch (Exception var20)
		{
			LOGGER.log(Level.SEVERE, "Error removing clan member: " + var20.getMessage(), var20);
		}
	}

	private void restore()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM clan_data where clan_id=?");)
		{
			ps.setInt(1, this._clanId);

			try (ResultSet clanData = ps.executeQuery())
			{
				if (clanData.next())
				{
					this.setName(clanData.getString("clan_name"));
					this.setLevel(clanData.getInt("clan_level"));
					this.setCastleId(clanData.getInt("hasCastle"));
					this._bloodAllianceCount = clanData.getInt("blood_alliance_count");
					this._bloodOathCount = clanData.getInt("blood_oath_count");
					this.setAllyId(clanData.getInt("ally_id"));
					this.setAllyName(clanData.getString("ally_name"));
					this.setAllyPenaltyExpiryTime(clanData.getLong("ally_penalty_expiry_time"), clanData.getInt("ally_penalty_type"));
					if (this._allyPenaltyExpiryTime < System.currentTimeMillis())
					{
						this.setAllyPenaltyExpiryTime(0L, 0);
					}

					this.setCharPenaltyExpiryTime(clanData.getLong("char_penalty_expiry_time"));
					if (this._charPenaltyExpiryTime + PlayerConfig.ALT_CLAN_JOIN_MINS * 60000L < System.currentTimeMillis())
					{
						this.setCharPenaltyExpiryTime(0L);
					}

					this.setDissolvingExpiryTime(clanData.getLong("dissolving_expiry_time"));
					this.setCrestId(clanData.getInt("crest_id"));
					this.setCrestLargeId(clanData.getInt("crest_large_id"));
					this.setAllyCrestId(clanData.getInt("ally_crest_id"));
					this._exp = clanData.getInt("exp");
					this.setReputationScore(clanData.getInt("reputation_score"));
					this.setAuctionBiddedAt(clanData.getInt("auction_bid_at"), false);
					this.setNewLeaderId(clanData.getInt("new_leader_id"), false);
					int leaderId = clanData.getInt("leader_id");
					ps.clearParameters();

					try (PreparedStatement select = con.prepareStatement("SELECT char_name,level,classid,charId,title,power_grade,subpledge,apprentice,sponsor,sex,race FROM characters WHERE clanid=?"))
					{
						select.setInt(1, this._clanId);

						try (ResultSet clanMember = select.executeQuery())
						{
							ClanMember member = null;

							while (clanMember.next())
							{
								member = new ClanMember(this, clanMember);
								if (member.getObjectId() == leaderId)
								{
									this.setLeader(member);
								}
								else
								{
									this.addClanMember(member);
								}
							}
						}
					}
				}
			}

			this.restoreSubPledges();
			this.restoreRankPrivs();
			this.restoreSkills();
			this.restoreNotice();
		}
		catch (Exception var19)
		{
			LOGGER.log(Level.SEVERE, "Error restoring clan data: " + var19.getMessage(), var19);
		}
	}

	private void restoreNotice()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT enabled,notice FROM clan_notices WHERE clan_id=?");)
		{
			ps.setInt(1, this._clanId);

			try (ResultSet noticeData = ps.executeQuery())
			{
				while (noticeData.next())
				{
					this._noticeEnabled = Boolean.parseBoolean(noticeData.getString("enabled"));
					this._notice = noticeData.getString("notice");
				}
			}
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.SEVERE, "Error restoring clan notice: " + var12.getMessage(), var12);
		}
	}

	private void storeNotice(String noticeValue, boolean enabled)
	{
		String notice = noticeValue;
		if (noticeValue == null)
		{
			notice = "";
		}

		if (notice.length() > 8192)
		{
			notice = notice.substring(0, 8191);
		}

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("INSERT INTO clan_notices (clan_id,notice,enabled) values (?,?,?) ON DUPLICATE KEY UPDATE notice=?,enabled=?");)
		{
			ps.setInt(1, this._clanId);
			ps.setString(2, notice);
			if (enabled)
			{
				ps.setString(3, "true");
			}
			else
			{
				ps.setString(3, "false");
			}

			ps.setString(4, notice);
			if (enabled)
			{
				ps.setString(5, "true");
			}
			else
			{
				ps.setString(5, "false");
			}

			ps.execute();
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.WARNING, "Error could not store clan notice: " + var12.getMessage(), var12);
		}

		this._notice = notice;
		this._noticeEnabled = enabled;
	}

	public void setNoticeEnabled(boolean enabled)
	{
		this.storeNotice(this.getNotice(), enabled);
	}

	public void setNotice(String notice)
	{
		this.storeNotice(notice, this._noticeEnabled);
	}

	public boolean isNoticeEnabled()
	{
		return this._noticeEnabled;
	}

	public String getNotice()
	{
		if (this._notice == null)
		{
			return "";
		}
		String text = this._notice.toLowerCase();
		return text.contains("action") && text.contains("bypass") ? "" : this._notice.replaceAll("<.*?>", "");
	}

	private void restoreSkills()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT skill_id,skill_level,sub_pledge_id FROM clan_skills WHERE clan_id=?");)
		{
			ps.setInt(1, this._clanId);

			try (ResultSet rset = ps.executeQuery())
			{
				while (rset.next())
				{
					int id = rset.getInt("skill_id");
					int level = rset.getInt("skill_level");
					Skill skill = SkillData.getInstance().getSkill(id, level);
					int subType = rset.getInt("sub_pledge_id");
					if (subType == -2)
					{
						this._skills.put(skill.getId(), skill);
					}
					else if (subType == 0)
					{
						this._subPledgeSkills.put(skill.getId(), skill);
					}
					else
					{
						Clan.SubPledge subunit = this._subPledges.get(subType);
						if (subunit != null)
						{
							subunit.addNewSkill(skill);
						}
						else
						{
							LOGGER.info("Missing subpledge " + subType + " for clan " + this + ", skill skipped.");
						}
					}
				}
			}
		}
		catch (Exception var15)
		{
			LOGGER.log(Level.SEVERE, "Error restoring clan skills: " + var15.getMessage(), var15);
		}
	}

	public Collection<Skill> getAllSkills()
	{
		return this._skills.values();
	}

	public Map<Integer, Skill> getSkills()
	{
		return this._skills;
	}

	public Skill addSkill(Skill newSkill)
	{
		Skill oldSkill = null;
		if (newSkill != null)
		{
			oldSkill = this._skills.put(newSkill.getId(), newSkill);
		}

		return oldSkill;
	}

	public Skill addNewSkill(Skill newSkill)
	{
		return this.addNewSkill(newSkill, -2);
	}

	public Skill addNewSkill(Skill newSkill, int subType)
	{
		Skill oldSkill = null;
		if (newSkill != null)
		{
			if (subType == -2)
			{
				oldSkill = this._skills.put(newSkill.getId(), newSkill);
			}
			else if (subType == 0)
			{
				oldSkill = this._subPledgeSkills.put(newSkill.getId(), newSkill);
			}
			else
			{
				Clan.SubPledge subunit = this.getSubPledge(subType);
				if (subunit == null)
				{
					LOGGER.warning("Subpledge " + subType + " does not exist for clan " + this);
					return oldSkill;
				}

				oldSkill = subunit.addNewSkill(newSkill);
			}

			try (Connection con = DatabaseFactory.getConnection())
			{
				if (oldSkill != null)
				{
					try (PreparedStatement ps = con.prepareStatement("UPDATE clan_skills SET skill_level=? WHERE skill_id=? AND clan_id=?"))
					{
						ps.setInt(1, newSkill.getLevel());
						ps.setInt(2, oldSkill.getId());
						ps.setInt(3, this._clanId);
						ps.execute();
					}
				}
				else
				{
					try (PreparedStatement ps = con.prepareStatement("INSERT INTO clan_skills (clan_id,skill_id,skill_level,skill_name,sub_pledge_id) VALUES (?,?,?,?,?)"))
					{
						ps.setInt(1, this._clanId);
						ps.setInt(2, newSkill.getId());
						ps.setInt(3, newSkill.getLevel());
						ps.setString(4, newSkill.getName());
						ps.setInt(5, subType);
						ps.execute();
					}
				}
			}
			catch (Exception var14)
			{
				LOGGER.log(Level.WARNING, "Error could not store clan skills: " + var14.getMessage(), var14);
			}

			SystemMessage sm = new SystemMessage(SystemMessageId.THE_CLAN_SKILL_S1_HAS_BEEN_ADDED);
			sm.addSkillName(newSkill.getId());

			for (ClanMember temp : this._members.values())
			{
				if (temp != null && temp.getPlayer() != null && temp.isOnline())
				{
					if (subType == -2)
					{
						if (newSkill.getMinPledgeClass() <= temp.getPlayer().getPledgeClass())
						{
							temp.getPlayer().addSkill(newSkill, false);
							temp.getPlayer().sendPacket(new PledgeSkillListAdd(newSkill.getId(), newSkill.getLevel()));
							temp.getPlayer().sendPacket(sm);
							temp.getPlayer().sendSkillList();
						}
					}
					else if (temp.getPledgeType() == subType)
					{
						temp.getPlayer().addSkill(newSkill, false);
						temp.getPlayer().sendPacket(new ExSubPledgeSkillAdd(subType, newSkill.getId(), newSkill.getLevel()));
						temp.getPlayer().sendPacket(sm);
						temp.getPlayer().sendSkillList();
					}
				}
			}
		}

		return oldSkill;
	}

	public void addSkillEffects()
	{
		for (Skill skill : this._skills.values())
		{
			for (ClanMember temp : this._members.values())
			{
				try
				{
					if (temp != null && temp.isOnline() && skill.getMinPledgeClass() <= temp.getPlayer().getPledgeClass())
					{
						temp.getPlayer().addSkill(skill, false);
					}
				}
				catch (NullPointerException var6)
				{
					LOGGER.log(Level.WARNING, var6.getMessage(), var6);
				}
			}
		}
	}

	public void addSkillEffects(Player player)
	{
		if (player != null)
		{
			int playerSocialClass = player.getPledgeClass() + 1;

			for (Skill skill : this._skills.values())
			{
				SkillLearn skillLearn = SkillTreeData.getInstance().getPledgeSkill(skill.getId(), skill.getLevel());
				if (skillLearn == null || skillLearn.getSocialClass() == null || playerSocialClass >= skillLearn.getSocialClass().ordinal())
				{
					player.addSkill(skill, false);
				}
			}

			if (player.getPledgeType() == 0)
			{
				for (Skill skillx : this._subPledgeSkills.values())
				{
					SkillLearn skillLearn = SkillTreeData.getInstance().getSubPledgeSkill(skillx.getId(), skillx.getLevel());
					if (skillLearn == null || skillLearn.getSocialClass() == null || playerSocialClass >= skillLearn.getSocialClass().ordinal())
					{
						player.addSkill(skillx, false);
					}
				}
			}
			else
			{
				Clan.SubPledge subunit = this.getSubPledge(player.getPledgeType());
				if (subunit == null)
				{
					return;
				}

				for (Skill skillxx : subunit.getSkills())
				{
					player.addSkill(skillxx, false);
				}
			}

			if (this._reputationScore < 0)
			{
				this.skillsStatus(player, true);
			}
		}
	}

	public void removeSkillEffects(Player player)
	{
		if (player != null)
		{
			for (Skill skill : this._skills.values())
			{
				player.removeSkill(skill, false);
			}

			if (player.getPledgeType() == 0)
			{
				for (Skill skill : this._subPledgeSkills.values())
				{
					player.removeSkill(skill, false);
				}
			}
			else
			{
				Clan.SubPledge subunit = this.getSubPledge(player.getPledgeType());
				if (subunit == null)
				{
					return;
				}

				for (Skill skill : subunit.getSkills())
				{
					player.removeSkill(skill, false);
				}
			}
		}
	}

	public void skillsStatus(Player player, boolean disable)
	{
		if (player != null)
		{
			for (Skill skill : this._skills.values())
			{
				if (disable)
				{
					player.disableSkill(skill, -1L);
				}
				else
				{
					player.enableSkill(skill, false);
				}
			}

			if (player.getPledgeType() == 0)
			{
				for (Skill skillx : this._subPledgeSkills.values())
				{
					if (disable)
					{
						player.disableSkill(skillx, -1L);
					}
					else
					{
						player.enableSkill(skillx, false);
					}
				}
			}
			else
			{
				Clan.SubPledge subunit = this.getSubPledge(player.getPledgeType());
				if (subunit != null)
				{
					for (Skill skillxx : subunit.getSkills())
					{
						if (disable)
						{
							player.disableSkill(skillxx, -1L);
						}
						else
						{
							player.enableSkill(skillxx, false);
						}
					}
				}
			}
		}
	}

	public void broadcastToOnlineAllyMembers(ServerPacket packet)
	{
		for (Clan clan : ClanTable.getInstance().getClanAllies(this.getAllyId()))
		{
			clan.broadcastToOnlineMembers(packet);
		}
	}

	public void broadcastToOnlineMembers(ServerPacket packet)
	{
		for (ClanMember member : this._members.values())
		{
			if (member != null && member.isOnline())
			{
				member.getPlayer().sendPacket(packet);
			}
		}
	}

	public void broadcastCSToOnlineMembers(CreatureSay packet, Player broadcaster)
	{
		for (ClanMember member : this._members.values())
		{
			if (member != null && member.isOnline() && !BlockList.isBlocked(member.getPlayer(), broadcaster))
			{
				member.getPlayer().sendPacket(packet);
			}
		}
	}

	public void broadcastToOtherOnlineMembers(ServerPacket packet, Player player)
	{
		for (ClanMember member : this._members.values())
		{
			if (member != null && member.isOnline() && member.getPlayer() != player)
			{
				member.getPlayer().sendPacket(packet);
			}
		}
	}

	@Override
	public String toString()
	{
		return this._name + "[" + this._clanId + "]";
	}

	public ItemContainer getWarehouse()
	{
		return this._warehouse;
	}

	public boolean isAtWarWith(int clanId)
	{
		return this._atWarWith.containsKey(clanId);
	}

	public boolean isAtWarWith(Clan clan)
	{
		return clan == null ? false : this._atWarWith.containsKey(clan.getId());
	}

	public int getHiredGuards()
	{
		return this._hiredGuards;
	}

	public void incrementHiredGuards()
	{
		this._hiredGuards++;
	}

	public boolean isAtWar()
	{
		return !this._atWarWith.isEmpty();
	}

	public Map<Integer, ClanWar> getWarList()
	{
		return this._atWarWith;
	}

	public void broadcastClanStatus()
	{
		for (Player member : this.getOnlineMembers(0))
		{
			member.sendPacket(PledgeShowMemberListDeleteAll.STATIC_PACKET);
			PledgeShowMemberListAll.sendAllTo(member);
		}
	}

	private void restoreSubPledges()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT sub_pledge_id,name,leader_id FROM clan_subpledges WHERE clan_id=?");)
		{
			ps.setInt(1, this._clanId);

			try (ResultSet rset = ps.executeQuery())
			{
				while (rset.next())
				{
					int id = rset.getInt("sub_pledge_id");
					String name = rset.getString("name");
					int leaderId = rset.getInt("leader_id");
					Clan.SubPledge pledge = new Clan.SubPledge(id, name, leaderId);
					this._subPledges.put(id, pledge);
				}
			}
		}
		catch (Exception var14)
		{
			LOGGER.log(Level.WARNING, "Could not restore clan sub-units: " + var14.getMessage(), var14);
		}
	}

	public Clan.SubPledge getSubPledge(int pledgeType)
	{
		return this._subPledges == null ? null : this._subPledges.get(pledgeType);
	}

	public Clan.SubPledge getSubPledge(String pledgeName)
	{
		if (this._subPledges == null)
		{
			return null;
		}
		for (Clan.SubPledge sp : this._subPledges.values())
		{
			if (sp.getName().equalsIgnoreCase(pledgeName))
			{
				return sp;
			}
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public Collection<Clan.SubPledge> getAllSubPledges()
	{
		return (Collection<Clan.SubPledge>) (this._subPledges == null ? Collections.emptyList() : this._subPledges.values());
	}

	public Clan.SubPledge createSubPledge(Player player, int pledgeTypeValue, int leaderId, String subPledgeName)
	{
		Clan.SubPledge subPledge = null;
		int pledgeType = this.getAvailablePledgeTypes(pledgeTypeValue);
		if (pledgeType == 0)
		{
			if (pledgeType == -1)
			{
				player.sendPacket(SystemMessageId.YOUR_CLAN_HAS_ALREADY_ESTABLISHED_A_CLAN_ACADEMY);
			}
			else
			{
				player.sendMessage("You can't create any more sub-units of this type");
			}

			return null;
		}
		else if (this._leader.getObjectId() == leaderId)
		{
			player.sendMessage("Leader is not correct");
			return null;
		}
		else if (pledgeType == -1 || (this._reputationScore >= FeatureConfig.ROYAL_GUARD_COST || pledgeType >= 1001) && (this._reputationScore >= FeatureConfig.KNIGHT_UNIT_COST || pledgeType <= 200))
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("INSERT INTO clan_subpledges (clan_id,sub_pledge_id,name,leader_id) values (?,?,?,?)");)
			{
				ps.setInt(1, this._clanId);
				ps.setInt(2, pledgeType);
				ps.setString(3, subPledgeName);
				ps.setInt(4, pledgeType != -1 ? leaderId : 0);
				ps.execute();
				subPledge = new Clan.SubPledge(pledgeType, subPledgeName, leaderId);
				this._subPledges.put(pledgeType, subPledge);
				if (pledgeType != -1)
				{
					if (pledgeType < 1001)
					{
						this.setReputationScore(this._reputationScore - FeatureConfig.ROYAL_GUARD_COST);
					}
					else
					{
						this.setReputationScore(this._reputationScore - FeatureConfig.KNIGHT_UNIT_COST);
					}
				}
			}
			catch (Exception var15)
			{
				LOGGER.log(Level.SEVERE, "Error saving sub clan data: " + var15.getMessage(), var15);
			}

			this.broadcastToOnlineMembers(new PledgeShowInfoUpdate(this._leader.getClan()));
			this.broadcastToOnlineMembers(new PledgeReceiveSubPledgeCreated(subPledge, this._leader.getClan()));
			return subPledge;
		}
		else
		{
			player.sendPacket(SystemMessageId.THE_CLAN_REPUTATION_IS_TOO_LOW);
			return null;
		}
	}

	public int getAvailablePledgeTypes(int pledgeType)
	{
		if (this._subPledges.get(pledgeType) != null)
		{
			switch (pledgeType)
			{
				case -1:
					return 0;
				case 100:
					return this.getAvailablePledgeTypes(200);
				case 200:
					return 0;
				case 1001:
					return this.getAvailablePledgeTypes(1002);
				case 1002:
					return this.getAvailablePledgeTypes(2001);
				case 2001:
					return this.getAvailablePledgeTypes(2002);
				case 2002:
					return 0;
			}
		}

		return pledgeType;
	}

	public void updateSubPledgeInDB(int pledgeType)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE clan_subpledges SET leader_id=?, name=? WHERE clan_id=? AND sub_pledge_id=?");)
		{
			ps.setInt(1, this.getSubPledge(pledgeType).getLeaderId());
			ps.setString(2, this.getSubPledge(pledgeType).getName());
			ps.setInt(3, this._clanId);
			ps.setInt(4, pledgeType);
			ps.execute();
		}
		catch (Exception var10)
		{
			LOGGER.log(Level.SEVERE, "Error updating subpledge: " + var10.getMessage(), var10);
		}
	}

	private void restoreRankPrivs()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT privs,`rank`,party FROM clan_privs WHERE clan_id=?");)
		{
			ps.setInt(1, this._clanId);

			try (ResultSet rset = ps.executeQuery())
			{
				while (rset.next())
				{
					int rank = rset.getInt("rank");
					int privileges = rset.getInt("privs");
					if (rank != -1)
					{
						this._privs.get(rank).setPrivs(privileges);
					}
				}
			}
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.SEVERE, "Error restoring clan privs by rank: " + var12.getMessage(), var12);
		}
	}

	public void initializePrivs()
	{
		for (int i = 1; i < 10; i++)
		{
			this._privs.put(i, new Clan.RankPrivs(i, 0, new ClanPrivileges()));
		}
	}

	public ClanPrivileges getRankPrivs(int rank)
	{
		return this._privs.get(rank) != null ? this._privs.get(rank).getPrivs() : new ClanPrivileges();
	}

	public void setRankPrivs(int rank, int privs)
	{
		if (this._privs.get(rank) != null)
		{
			this._privs.get(rank).setPrivs(privs);

			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("REPLACE INTO clan_privs (clan_id,`rank`,party,privs) VALUES (?,?,?,?)");)
			{
				ps.setInt(1, this._clanId);
				ps.setInt(2, rank);
				ps.setInt(3, 0);
				ps.setInt(4, privs);
				ps.execute();
			}
			catch (Exception var16)
			{
				LOGGER.log(Level.WARNING, "Could not store clan privs for rank: " + var16.getMessage(), var16);
			}

			for (ClanMember cm : this._members.values())
			{
				if (cm.isOnline() && cm.getPowerGrade() == rank && cm.getPlayer() != null)
				{
					cm.getPlayer().getClanPrivileges().setMask(privs);
					cm.getPlayer().updateUserInfo();
				}
			}

			this.broadcastClanStatus();
		}
		else
		{
			this._privs.put(rank, new Clan.RankPrivs(rank, 0, privs));

			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("REPLACE INTO clan_privs (clan_id,`rank`,party,privs) VALUES (?,?,?,?)");)
			{
				ps.setInt(1, this._clanId);
				ps.setInt(2, rank);
				ps.setInt(3, 0);
				ps.setInt(4, privs);
				ps.execute();
			}
			catch (Exception var13)
			{
				LOGGER.log(Level.WARNING, "Could not create new rank and store clan privs for rank: " + var13.getMessage(), var13);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public Collection<Clan.RankPrivs> getAllRankPrivs()
	{
		return (Collection<Clan.RankPrivs>) (this._privs == null ? Collections.emptyList() : this._privs.values());
	}

	public int getLeaderSubPledge(int leaderId)
	{
		int id = 0;

		for (Clan.SubPledge sp : this._subPledges.values())
		{
			if (sp.getLeaderId() != 0 && sp.getLeaderId() == leaderId)
			{
				id = sp.getId();
			}
		}

		return id;
	}

	public synchronized void addReputationScore(int value)
	{
		this.setReputationScore(this._reputationScore + value);
	}

	public synchronized void takeReputationScore(int value)
	{
		this.setReputationScore(this._reputationScore - value);
	}

	private void setReputationScore(int value)
	{
		if (this._reputationScore >= 0 && value < 0)
		{
			this.broadcastToOnlineMembers(new SystemMessage(SystemMessageId.SINCE_THE_CLAN_REPUTATION_HAS_DROPPED_BELOW_0_YOUR_CLAN_SKILL_S_WILL_BE_DE_ACTIVATED));

			for (ClanMember member : this._members.values())
			{
				if (member.isOnline() && member.getPlayer() != null)
				{
					this.skillsStatus(member.getPlayer(), true);
				}
			}
		}
		else if (this._reputationScore < 0 && value >= 0)
		{
			this.broadcastToOnlineMembers(new SystemMessage(SystemMessageId.CLAN_SKILLS_WILL_NOW_BE_ACTIVATED_SINCE_THE_CLAN_REPUTATION_IS_1_OR_HIGHER));

			for (ClanMember memberx : this._members.values())
			{
				if (memberx.isOnline() && memberx.getPlayer() != null)
				{
					this.skillsStatus(memberx.getPlayer(), false);
				}
			}
		}

		this._reputationScore = value;
		if (this._reputationScore > 100000000)
		{
			this._reputationScore = 100000000;
		}

		if (this._reputationScore < -100000000)
		{
			this._reputationScore = -100000000;
		}

		this.broadcastToOnlineMembers(new PledgeShowInfoUpdate(this));
	}

	public int getReputationScore()
	{
		return this._reputationScore;
	}

	public synchronized void setRank(int rank)
	{
		this._rank = rank;
	}

	public int getRank()
	{
		return this._rank;
	}

	public int getAuctionBiddedAt()
	{
		return this._auctionBiddedAt;
	}

	public void setAuctionBiddedAt(int id, boolean storeInDb)
	{
		this._auctionBiddedAt = id;
		if (storeInDb)
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE clan_data SET auction_bid_at=? WHERE clan_id=?");)
			{
				ps.setInt(1, id);
				ps.setInt(2, this._clanId);
				ps.execute();
			}
			catch (Exception var11)
			{
				LOGGER.log(Level.WARNING, "Could not store auction for clan: " + var11.getMessage(), var11);
			}
		}
	}

	public boolean checkClanJoinCondition(Player player, Player target, int pledgeType)
	{
		if (player == null)
		{
			return false;
		}
		else if (!player.hasAccess(ClanAccess.INVITE_MEMBER))
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return false;
		}
		else if (target == null)
		{
			player.sendPacket(SystemMessageId.THE_TARGET_CANNOT_BE_INVITED);
			return false;
		}
		else if (player.getObjectId() == target.getObjectId())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_ASK_YOURSELF_TO_APPLY_TO_A_CLAN);
			return false;
		}
		else if (this._charPenaltyExpiryTime > System.currentTimeMillis())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_ACCEPT_A_NEW_CLAN_MEMBER_FOR_24_H_AFTER_DISMISSING_SOMEONE);
			return false;
		}
		else if (target.getClanId() != 0)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_ALREADY_A_MEMBER_OF_ANOTHER_CLAN);
			sm.addString(target.getName());
			player.sendPacket(sm);
			return false;
		}
		else if (target.getClanJoinExpiryTime() > System.currentTimeMillis())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.C1_WILL_BE_ABLE_TO_JOIN_YOUR_CLAN_IN_S2_MIN_AFTER_LEAVING_THE_PREVIOUS_ONE);
			sm.addString(target.getName());
			sm.addInt(PlayerConfig.ALT_CLAN_JOIN_MINS);
			player.sendPacket(sm);
			return false;
		}
		else if ((target.getLevel() > 40 || target.getPlayerClass().level() >= 2) && pledgeType == -1)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_DOES_NOT_MEET_THE_REQUIREMENTS_TO_JOIN_A_CLAN_ACADEMY);
			sm.addString(target.getName());
			player.sendPacket(sm);
			player.sendPacket(SystemMessageId.IN_ORDER_TO_JOIN_THE_CLAN_ACADEMY_YOU_MUST_BE_UNAFFILIATED_WITH_A_CLAN_AND_BE_AN_UNAWAKENED_CHARACTER_LV_84_OR_BELOW_FOR_BOTH_MAIN_AND_SUBCLASS);
			return false;
		}
		else if (this.getSubPledgeMembersCount(pledgeType) >= this.getMaxNrOfMembers(pledgeType))
		{
			if (pledgeType == 0)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_FULL_AND_CANNOT_ACCEPT_ADDITIONAL_CLAN_MEMBERS_AT_THIS_TIME);
				sm.addString(this._name);
				player.sendPacket(sm);
			}
			else
			{
				player.sendPacket(SystemMessageId.THE_CLAN_IS_FULL);
			}

			return false;
		}
		else
		{
			return true;
		}
	}

	public boolean checkAllyJoinCondition(Player player, Player target)
	{
		if (player == null)
		{
			return false;
		}
		else if (player.getAllyId() != 0 && player.isClanLeader() && player.getClanId() == player.getAllyId())
		{
			Clan leaderClan = player.getClan();
			if (leaderClan.getAllyPenaltyExpiryTime() > System.currentTimeMillis() && leaderClan.getAllyPenaltyType() == 3)
			{
				player.sendPacket(SystemMessageId.YOU_CAN_ACCEPT_A_NEW_CLAN_IN_THE_ALLIANCE_IN_24_H_AFTER_DISMISSING_ANOTHER_ONE);
				return false;
			}
			else if (target == null)
			{
				player.sendPacket(SystemMessageId.THE_TARGET_CANNOT_BE_INVITED);
				return false;
			}
			else if (player.getObjectId() == target.getObjectId())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_ASK_YOURSELF_TO_APPLY_TO_A_CLAN);
				return false;
			}
			else if (target.getClan() == null)
			{
				player.sendPacket(SystemMessageId.THE_TARGET_MUST_BE_A_CLAN_MEMBER);
				return false;
			}
			else if (!target.isClanLeader())
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER);
				sm.addString(target.getName());
				player.sendPacket(sm);
				return false;
			}
			else
			{
				Clan targetClan = target.getClan();
				if (target.getAllyId() != 0)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_CLAN_IS_ALREADY_A_MEMBER_OF_S2_ALLIANCE);
					sm.addString(targetClan.getName());
					sm.addString(targetClan.getAllyName());
					player.sendPacket(sm);
					return false;
				}
				if (targetClan.getAllyPenaltyExpiryTime() > System.currentTimeMillis())
				{
					if (targetClan.getAllyPenaltyType() == 1)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_CLAN_CANNOT_JOIN_THE_ALLIANCE_BECAUSE_ONE_DAY_HAS_NOT_YET_PASSED_SINCE_THEY_LEFT_ANOTHER_ALLIANCE);
						sm.addString(target.getClan().getName());
						sm.addString(target.getClan().getAllyName());
						player.sendPacket(sm);
						return false;
					}

					if (targetClan.getAllyPenaltyType() == 2)
					{
						player.sendPacket(SystemMessageId.A_CLAN_CAN_JOIN_ANOTHER_ALLIANCE_IN_24_H_AFTER_LEAVING_THE_PREVIOUS_ONE);
						return false;
					}
				}

				if (player.isInsideZone(ZoneId.SIEGE) && target.isInsideZone(ZoneId.SIEGE))
				{
					player.sendPacket(SystemMessageId.THE_OPPOSING_CLAN_IS_PARTICIPATING_IN_A_SIEGE_BATTLE);
					return false;
				}
				else if (leaderClan.isAtWarWith(targetClan.getId()))
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_MAKE_AN_ALLIANCE_WITH_A_CLAN_YOU_ARE_IN_WAR_WITH);
					return false;
				}
				else if (ClanTable.getInstance().getClanAllies(player.getAllyId()).size() >= PlayerConfig.ALT_MAX_NUM_OF_CLANS_IN_ALLY)
				{
					player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_LIMIT);
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
			player.sendPacket(SystemMessageId.ACCESS_ONLY_FOR_THE_CHANNEL_FOUNDER);
			return false;
		}
	}

	public long getAllyPenaltyExpiryTime()
	{
		return this._allyPenaltyExpiryTime;
	}

	public int getAllyPenaltyType()
	{
		return this._allyPenaltyType;
	}

	public void setAllyPenaltyExpiryTime(long expiryTime, int penaltyType)
	{
		this._allyPenaltyExpiryTime = expiryTime;
		this._allyPenaltyType = penaltyType;
	}

	public long getCharPenaltyExpiryTime()
	{
		return this._charPenaltyExpiryTime;
	}

	public void setCharPenaltyExpiryTime(long time)
	{
		this._charPenaltyExpiryTime = time;
	}

	public long getDissolvingExpiryTime()
	{
		return this._dissolvingExpiryTime;
	}

	public void setDissolvingExpiryTime(long time)
	{
		this._dissolvingExpiryTime = time;
	}

	public void createAlly(Player player, String allyName)
	{
		if (null != player)
		{
			if (!player.isClanLeader())
			{
				player.sendPacket(SystemMessageId.ONLY_CLAN_LEADERS_MAY_CREATE_ALLIANCES);
			}
			else if (this._allyId != 0)
			{
				player.sendPacket(SystemMessageId.YOU_ALREADY_BELONG_TO_ANOTHER_ALLIANCE);
			}
			else if (this._level < 5)
			{
				player.sendPacket(SystemMessageId.TO_CREATE_AN_ALLIANCE_YOUR_CLAN_MUST_BE_LV_5_OR_HIGHER);
			}
			else if (this._allyPenaltyExpiryTime > System.currentTimeMillis() && this._allyPenaltyType == 4)
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_CREATE_A_NEW_ALLIANCE_WITHIN_1_DAY_OF_DISSOLUTION);
			}
			else if (this._dissolvingExpiryTime > System.currentTimeMillis())
			{
				player.sendPacket(SystemMessageId.AS_YOU_ARE_CURRENTLY_SCHEDULE_FOR_CLAN_DISSOLUTION_NO_ALLIANCE_CAN_BE_CREATED);
			}
			else if (!StringUtil.isAlphaNumeric(allyName))
			{
				player.sendPacket(SystemMessageId.INCORRECT_ALLIANCE_NAME_PLEASE_TRY_AGAIN);
			}
			else if (allyName.length() > 16 || allyName.length() < 2)
			{
				player.sendPacket(SystemMessageId.INCORRECT_LENGTH_FOR_AN_ALLIANCE_NAME);
			}
			else if (ClanTable.getInstance().isAllyExists(allyName))
			{
				player.sendPacket(SystemMessageId.THAT_ALLIANCE_NAME_ALREADY_EXISTS);
			}
			else
			{
				this.setAllyId(this._clanId);
				this.setAllyName(allyName.trim());
				this.setAllyPenaltyExpiryTime(0L, 0);
				this.updateClanInDB();
				player.updateUserInfo();
				player.sendMessage("Alliance " + allyName + " has been created.");
				player.sendPacket(new ExAllianceCreateResult(1));
				this.broadcastClanStatus();
			}
		}
	}

	public void dissolveAlly(Player player)
	{
		if (this._allyId == 0)
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_IN_AN_ALLIANCE);
		}
		else if (player.isClanLeader() && this._clanId == this._allyId)
		{
			if (player.isInsideZone(ZoneId.SIEGE))
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_DISSOLVE_AN_ALLIANCE_WHILE_AN_AFFILIATED_CLAN_IS_PARTICIPATING_IN_A_SIEGE_BATTLE);
			}
			else
			{
				this.broadcastToOnlineAllyMembers(new SystemMessage(SystemMessageId.THE_ALLIANCE_IS_DISBANDED));
				long currentTime = System.currentTimeMillis();

				for (Clan clan : ClanTable.getInstance().getClanAllies(this.getAllyId()))
				{
					if (clan.getId() != this.getId())
					{
						clan.setAllyId(0);
						clan.setAllyName(null);
						clan.setAllyPenaltyExpiryTime(0L, 0);
						clan.updateClanInDB();
						clan.broadcastClanStatus();
					}
				}

				this.setAllyId(0);
				this.setAllyName(null);
				this.changeAllyCrest(0, false);
				this.setAllyPenaltyExpiryTime(currentTime + PlayerConfig.ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED * 86400000, 4);
				this.updateClanInDB();
				this.broadcastClanStatus();
			}
		}
		else
		{
			player.sendPacket(SystemMessageId.ACCESS_ONLY_FOR_THE_CHANNEL_FOUNDER);
		}
	}

	public boolean levelUpClan(Player player)
	{
		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return false;
		}
		else if (System.currentTimeMillis() < this._dissolvingExpiryTime)
		{
			player.sendPacket(SystemMessageId.AS_YOU_ARE_CURRENTLY_SCHEDULE_FOR_CLAN_DISSOLUTION_YOUR_CLAN_LEVEL_CANNOT_BE_INCREASED);
			return false;
		}
		else
		{
			boolean increaseClanLevel = false;
			switch (this._level)
			{
				case 0:
					if (player.getSp() >= 1000L && player.getAdena() >= 150000L && this._members.size() >= 1 && player.reduceAdena(ItemProcessType.FEE, 150000L, player.getTarget(), true))
					{
						player.setSp(player.getSp() - 1000L);
						SystemMessage sp = new SystemMessage(SystemMessageId.YOUR_SP_HAS_DECREASED_BY_S1);
						sp.addInt(1000);
						player.sendPacket(sp);
						increaseClanLevel = true;
					}
					break;
				case 1:
					if (player.getSp() >= 15000L && player.getAdena() >= 300000L && this._members.size() >= 1 && player.reduceAdena(ItemProcessType.FEE, 300000L, player.getTarget(), true))
					{
						player.setSp(player.getSp() - 15000L);
						SystemMessage sp = new SystemMessage(SystemMessageId.YOUR_SP_HAS_DECREASED_BY_S1);
						sp.addInt(15000);
						player.sendPacket(sp);
						increaseClanLevel = true;
					}
					break;
				case 2:
					if (player.getSp() >= 100000L && player.getInventory().getItemByItemId(1419) != null && this._members.size() >= 1 && player.destroyItemByItemId(ItemProcessType.FEE, 1419, 100L, player.getTarget(), true))
					{
						player.setSp(player.getSp() - 100000L);
						SystemMessage sp = new SystemMessage(SystemMessageId.YOUR_SP_HAS_DECREASED_BY_S1);
						sp.addInt(100000);
						player.sendPacket(sp);
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISAPPEARED);
						sm.addItemName(1419);
						player.sendPacket(sm);
						increaseClanLevel = true;
					}
					break;
				case 3:
					if (player.getSp() >= 1000000L && player.getInventory().getItemByItemId(1419) != null && this._members.size() >= 1 && player.destroyItemByItemId(ItemProcessType.FEE, 1419, 5000L, player.getTarget(), true))
					{
						player.setSp(player.getSp() - 1000000L);
						SystemMessage sp = new SystemMessage(SystemMessageId.YOUR_SP_HAS_DECREASED_BY_S1);
						sp.addInt(1000000);
						player.sendPacket(sp);
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISAPPEARED);
						sm.addItemName(1419);
						player.sendPacket(sm);
						increaseClanLevel = true;
					}
					break;
				case 4:
					if (player.getSp() >= 5000000L && player.getInventory().getItemByItemId(1419) != null && this._members.size() >= 1 && player.destroyItemByItemId(ItemProcessType.FEE, 1419, 10000L, player.getTarget(), true))
					{
						player.setSp(player.getSp() - 5000000L);
						SystemMessage sp = new SystemMessage(SystemMessageId.YOUR_SP_HAS_DECREASED_BY_S1);
						sp.addInt(5000000);
						player.sendPacket(sp);
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISAPPEARED);
						sm.addItemName(1419);
						player.sendPacket(sm);
						increaseClanLevel = true;
					}
					break;
				default:
					return false;
			}

			if (!increaseClanLevel)
			{
				player.sendPacket(SystemMessageId.THE_CONDITIONS_NECESSARY_TO_INCREASE_THE_CLAN_S_LEVEL_HAVE_NOT_BEEN_MET);
				return false;
			}
			UserInfo ui = new UserInfo(player, false);
			ui.addComponentType(UserInfoType.CURRENT_HPMPCP_EXP_SP);
			player.sendPacket(ui);
			player.sendItemList();
			this.changeLevel(this._level + 1);
			if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_CLAN_LEVELUP))
			{
				EventDispatcher.getInstance().notifyEventAsync(new OnPlayerClanLvlUp(player, this));
			}

			return true;
		}
	}

	public void changeLevel(int level)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE clan_data SET clan_level = ? WHERE clan_id = ?");)
		{
			ps.setInt(1, level);
			ps.setInt(2, this._clanId);
			ps.execute();
		}
		catch (Exception var10)
		{
			LOGGER.log(Level.WARNING, "could not increase clan level:" + var10.getMessage(), var10);
		}

		this.setLevel(level);
		this.setRank(ClanTable.getInstance().getClanRank(this));
		if (this._leader.isOnline())
		{
			Player leader = this._leader.getPlayer();
			if (level > 4)
			{
				SiegeManager.getInstance().addSiegeSkills(leader);
				leader.sendPacket(SystemMessageId.NOW_THAT_YOUR_CLAN_LEVEL_IS_ABOVE_LEVEL_5_IT_CAN_ACCUMULATE_CLAN_REPUTATION);
			}
			else if (level < 5)
			{
				SiegeManager.getInstance().removeSiegeSkills(leader);
			}
		}

		this.broadcastToOnlineMembers(new ExPledgeLevelUp(level));
		this.broadcastToOnlineMembers(new SystemMessage(SystemMessageId.YOUR_CLAN_S_LEVEL_HAS_INCREASED));
		this.broadcastToOnlineMembers(new PledgeShowInfoUpdate(this));
	}

	public void changeClanCrest(int crestId)
	{
		if (this._crestId != 0)
		{
			CrestTable.getInstance().removeCrest(this.getCrestId());
		}

		this.setCrestId(crestId);

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE clan_data SET crest_id = ? WHERE clan_id = ?");)
		{
			ps.setInt(1, crestId);
			ps.setInt(2, this._clanId);
			ps.executeUpdate();
		}
		catch (SQLException var10)
		{
			LOGGER.log(Level.WARNING, "Could not update crest for clan " + this._name + " [" + this._clanId + "] : " + var10.getMessage(), var10);
		}

		for (Player member : this.getOnlineMembers(0))
		{
			member.broadcastUserInfo();
		}
	}

	public void changeAllyCrest(int crestId, boolean onlyThisClan)
	{
		String sqlStatement = "UPDATE clan_data SET ally_crest_id = ? WHERE clan_id = ?";
		int allyId = this._clanId;
		if (!onlyThisClan)
		{
			if (this._allyCrestId != 0)
			{
				CrestTable.getInstance().removeCrest(this.getAllyCrestId());
			}

			sqlStatement = "UPDATE clan_data SET ally_crest_id = ? WHERE ally_id = ?";
			allyId = this._allyId;
		}

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement(sqlStatement);)
		{
			ps.setInt(1, crestId);
			ps.setInt(2, allyId);
			ps.executeUpdate();
		}
		catch (SQLException var13)
		{
			LOGGER.log(Level.WARNING, "Could not update ally crest for ally/clan id " + allyId + " : " + var13.getMessage(), var13);
		}

		if (onlyThisClan)
		{
			this.setAllyCrestId(crestId);

			for (Player member : this.getOnlineMembers(0))
			{
				member.broadcastUserInfo();
			}
		}
		else
		{
			for (Clan clan : ClanTable.getInstance().getClanAllies(this.getAllyId()))
			{
				clan.setAllyCrestId(crestId);

				for (Player member : clan.getOnlineMembers(0))
				{
					member.broadcastUserInfo();
				}
			}
		}
	}

	public void changeLargeCrest(int crestId)
	{
		if (this._crestLargeId != 0)
		{
			CrestTable.getInstance().removeCrest(this.getCrestLargeId());
		}

		this.setCrestLargeId(crestId);

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE clan_data SET crest_large_id = ? WHERE clan_id = ?");)
		{
			ps.setInt(1, crestId);
			ps.setInt(2, this._clanId);
			ps.executeUpdate();
		}
		catch (SQLException var10)
		{
			LOGGER.log(Level.WARNING, "Could not update large crest for clan " + this._name + " [" + this._clanId + "] : " + var10.getMessage(), var10);
		}

		for (Player member : this.getOnlineMembers(0))
		{
			member.broadcastUserInfo();
		}
	}

	public boolean isLearnableSubSkill(int skillId, int skillLevel)
	{
		Skill current = this._subPledgeSkills.get(skillId);
		if (current != null && current.getLevel() + 1 == skillLevel)
		{
			return true;
		}
		else if (current == null && skillLevel == 1)
		{
			return true;
		}
		else
		{
			for (Clan.SubPledge subunit : this._subPledges.values())
			{
				if (subunit.getId() != -1)
				{
					current = subunit.getSkill(skillId);
					if ((current != null && current.getLevel() + 1 == skillLevel) || (current == null && skillLevel == 1))
					{
						return true;
					}
				}
			}

			return false;
		}
	}

	public boolean isLearnableSubPledgeSkill(Skill skill, int subType)
	{
		if (subType == -1)
		{
			return false;
		}
		int id = skill.getId();
		Skill current;
		if (subType == 0)
		{
			current = this._subPledgeSkills.get(id);
		}
		else
		{
			current = this._subPledges.get(subType).getSkill(id);
		}

		return current != null && current.getLevel() + 1 == skill.getLevel() ? true : current == null && skill.getLevel() == 1;
	}

	public Collection<PledgeSkillList.SubPledgeSkill> getAllSubSkills()
	{
		List<PledgeSkillList.SubPledgeSkill> list = new LinkedList<>();

		for (Skill skill : this._subPledgeSkills.values())
		{
			list.add(new PledgeSkillList.SubPledgeSkill(0, skill.getId(), skill.getLevel()));
		}

		for (Clan.SubPledge subunit : this._subPledges.values())
		{
			for (Skill skill : subunit.getSkills())
			{
				list.add(new PledgeSkillList.SubPledgeSkill(subunit.getId(), skill.getId(), skill.getLevel()));
			}
		}

		return list;
	}

	public void setNewLeaderId(int objectId, boolean storeInDb)
	{
		this._newLeaderId = objectId;
		if (storeInDb)
		{
			this.updateClanInDB();
		}
	}

	public int getNewLeaderId()
	{
		return this._newLeaderId;
	}

	public Player getNewLeader()
	{
		return World.getInstance().getPlayer(this._newLeaderId);
	}

	public String getNewLeaderName()
	{
		return CharInfoTable.getInstance().getNameById(this._newLeaderId);
	}

	public int getSiegeKills()
	{
		return this._siegeKills.get();
	}

	public int getSiegeDeaths()
	{
		return this._siegeDeaths.get();
	}

	public int addSiegeKill()
	{
		return this._siegeKills.incrementAndGet();
	}

	public int addSiegeDeath()
	{
		return this._siegeDeaths.incrementAndGet();
	}

	public void clearSiegeKills()
	{
		this._siegeKills.set(0);
	}

	public void clearSiegeDeaths()
	{
		this._siegeDeaths.set(0);
	}

	public int getWarCount()
	{
		return this._atWarWith.size();
	}

	public void addWar(int clanId, ClanWar war)
	{
		this._atWarWith.put(clanId, war);
	}

	public void deleteWar(int clanId)
	{
		this._atWarWith.remove(clanId);
	}

	public ClanWar getWarWith(int clanId)
	{
		return this._atWarWith.get(clanId);
	}

	public synchronized void addMemberOnlineTime(Player player)
	{
		ClanMember clanMember = this.getClanMember(player.getObjectId());
		if (clanMember != null)
		{
			clanMember.setOnlineTime(clanMember.getOnlineTime() + 60000L);
			if (clanMember.getOnlineTime() == 1800000L)
			{
				this.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(clanMember));
			}
		}

		ClanRewardBonus availableBonus = ClanRewardType.MEMBERS_ONLINE.getAvailableBonus(this);
		if (availableBonus != null)
		{
			if (this._lastMembersOnlineBonus == null)
			{
				this._lastMembersOnlineBonus = availableBonus;
				this.broadcastToOnlineMembers(new SystemMessage(SystemMessageId.YOUR_CLAN_HAS_ACHIEVED_LOGIN_BONUS_LV_S1).addByte(availableBonus.getLevel()));
			}
			else if (this._lastMembersOnlineBonus.getLevel() < availableBonus.getLevel())
			{
				this._lastMembersOnlineBonus = availableBonus;
				this.broadcastToOnlineMembers(new SystemMessage(SystemMessageId.YOUR_CLAN_HAS_ACHIEVED_LOGIN_BONUS_LV_S1).addByte(availableBonus.getLevel()));
			}
		}

		int currentMaxOnline = 0;

		for (ClanMember member : this._members.values())
		{
			if (member.getOnlineTime() > PlayerConfig.ALT_CLAN_MEMBERS_TIME_FOR_BONUS)
			{
				currentMaxOnline++;
			}
		}

		if (this.getMaxOnlineMembers() < currentMaxOnline)
		{
			this.getVariables().set("MAX_ONLINE_MEMBERS", currentMaxOnline);
		}
	}

	public synchronized void addHuntingPoints(Player player, Npc target, double value)
	{
		int points = (int) value / 2960;
		if (points > 0)
		{
			this.getVariables().set("HUNTING_POINTS", this.getHuntingPoints() + points);
			ClanRewardBonus availableBonus = ClanRewardType.HUNTING_MONSTERS.getAvailableBonus(this);
			if (availableBonus != null)
			{
				if (this._lastHuntingBonus == null)
				{
					this._lastHuntingBonus = availableBonus;
					this.broadcastToOnlineMembers(new SystemMessage(SystemMessageId.YOUR_CLAN_HAS_ACHIEVED_HUNTING_BONUS_LV_S1).addByte(availableBonus.getLevel()));
				}
				else if (this._lastHuntingBonus.getLevel() < availableBonus.getLevel())
				{
					this._lastHuntingBonus = availableBonus;
					this.broadcastToOnlineMembers(new SystemMessage(SystemMessageId.YOUR_CLAN_HAS_ACHIEVED_HUNTING_BONUS_LV_S1).addByte(availableBonus.getLevel()));
				}
			}
		}
	}

	public int getMaxOnlineMembers()
	{
		return this.getVariables().getInt("MAX_ONLINE_MEMBERS", 0);
	}

	public int getHuntingPoints()
	{
		return this.getVariables().getInt("HUNTING_POINTS", 0);
	}

	public int getPreviousMaxOnlinePlayers()
	{
		return this.getVariables().getInt("PREVIOUS_MAX_ONLINE_PLAYERS", 0);
	}

	public int getPreviousHuntingPoints()
	{
		return this.getVariables().getInt("PREVIOUS_HUNTING_POINTS", 0);
	}

	public boolean canClaimBonusReward(Player player, ClanRewardType type)
	{
		ClanMember clanMember = this.getClanMember(player.getObjectId());
		return clanMember != null && type.getAvailableBonus(this) != null && !clanMember.isRewardClaimed(type);
	}

	public void resetClanBonus()
	{
		this.getVariables().set("PREVIOUS_MAX_ONLINE_PLAYERS", this.getMaxOnlineMembers());
		this.getVariables().set("PREVIOUS_HUNTING_POINTS", this.getHuntingPoints());
		this._members.values().forEach(ClanMember::resetBonus);
		this.getVariables().remove("HUNTING_POINTS");
		this.getVariables().storeMe();
		this.broadcastToOnlineMembers(ExPledgeBonusMarkReset.STATIC_PACKET);
	}

	public ClanVariables getVariables()
	{
		if (this._vars == null)
		{
			synchronized (this)
			{
				if (this._vars == null)
				{
					this._vars = new ClanVariables(this._clanId);
					if (GeneralConfig.CLAN_VARIABLES_STORE_INTERVAL > 0)
					{
						ThreadPool.scheduleAtFixedRate(this::storeVariables, GeneralConfig.CLAN_VARIABLES_STORE_INTERVAL, GeneralConfig.CLAN_VARIABLES_STORE_INTERVAL);
					}
				}
			}
		}

		return this._vars;
	}

	public boolean hasVariables()
	{
		return this._vars != null;
	}

	private void storeVariables()
	{
		ClanVariables vars = this._vars;
		if (vars != null)
		{
			vars.storeMe();
		}
	}

	public int getClanContribution(int objId)
	{
		return this.getVariables().getInt("CONTRIBUTION_" + objId, 0);
	}

	public void setClanContribution(int objId, int exp)
	{
		this.getVariables().set("CONTRIBUTION_" + objId, exp);
	}

	public int getClanContributionWeekly(int objId)
	{
		return this.getVariables().getInt("CONTRIBUTION_WEEKLY_" + objId, 0);
	}

	public Collection<ClanMember> getContributionList()
	{
		return this.getMembers().stream().filter(it -> it.getClan().getClanContribution(it.getObjectId()) != 0).collect(Collectors.toList());
	}

	public void setClanContributionWeekly(int objId, int exp)
	{
		this.getVariables().set("CONTRIBUTION_WEEKLY_" + objId, exp);
	}

	public int getExp()
	{
		return this._exp;
	}

	public void addExp(int objId, int value)
	{
		if (this._exp + value <= ClanLevelData.getInstance().getMaxExp())
		{
			this._exp += value;
			this.broadcastToOnlineMembers(new ExPledgeV3Info(this._exp, this.getRank(), this.getNotice(), this.isNoticeEnabled()));
		}

		int nextLevel = this._level + 1;
		if (nextLevel <= ClanLevelData.getInstance().getMaxLevel() && ClanLevelData.getInstance().getLevelExp(nextLevel) <= this._exp)
		{
			this.changeLevel(nextLevel);
		}

		int contribution = this.getClanContribution(objId);
		this.setClanContribution(objId, contribution + value);
		this.setClanContributionWeekly(objId, contribution + value);
	}

	public void setExp(int objId, int value)
	{
		this._exp = value;
		this.broadcastToOnlineMembers(new ExPledgeV3Info(this._exp, this.getRank(), this.getNotice(), this.isNoticeEnabled()));
		int contribution = this.getClanContribution(objId);
		this.setClanContribution(objId, contribution + value);
		this.setClanContributionWeekly(objId, contribution + value);
		this.updateClanInDB();
	}

	private void restoreMercenary()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement("SELECT * FROM clan_mercenary WHERE clan_id=?");)
		{
			stmt.setInt(1, this._clanId);

			try (ResultSet rset = stmt.executeQuery())
			{
				while (rset.next())
				{
					int playerId = rset.getInt("player_id");
					String name = rset.getString("player_name");
					int classId = rset.getInt("classid");
					this._mercenaries.put(playerId, new MercenaryPledgeHolder(playerId, name, classId, this._clanId));
					int cur = Integer.parseInt(name.replace("***-", ""));
					if (cur > _mercenaryId)
					{
						_mercenaryId = cur;
					}
				}
			}
		}
		catch (SQLException var14)
		{
			LOGGER.warning("Problem with Clan restoreMercenary : " + var14.getMessage());
		}
	}

	public String createMercenary(int playerId, int classId)
	{
		String name = "***-" + String.format("%03d", _mercenaryId);
		this._mercenaries.put(playerId, new MercenaryPledgeHolder(playerId, name, classId, this._clanId));

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement("INSERT INTO clan_mercenary (player_id, clan_id, player_name, classid) VALUES (?,?,?,?)");)
		{
			stmt.setInt(1, playerId);
			stmt.setInt(2, this._clanId);
			stmt.setString(3, name);
			stmt.setInt(4, classId);
			stmt.execute();
		}
		catch (SQLException var12)
		{
			LOGGER.warning("Problem with Clan createMercenary : " + var12.getMessage());
		}

		_mercenaryId++;
		return name;
	}

	public void removeMercenaryByPlayerId(int playerId)
	{
		this._mercenaries.remove(playerId);

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement("DELETE FROM clan_mercenary WHERE player_id=?");)
		{
			stmt.setInt(1, playerId);
			stmt.execute();
		}
		catch (SQLException var10)
		{
			LOGGER.warning("Problem with Clan removeMercenaryByPlayerId : " + var10.getMessage());
		}
	}

	public void removeMercenaryByClanId(int clanId)
	{
		for (Entry<Integer, MercenaryPledgeHolder> entry : this._mercenaries.entrySet())
		{
			Player player = World.getInstance().getPlayer(entry.getKey());
			if (player != null)
			{
				player.setMercenary(false, clanId);
			}
			else
			{
				try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM character_variables WHERE var=?");)
				{
					ps.setString(1, "isMercenary");
					ps.execute();
				}
				catch (Exception var18)
				{
					LOGGER.log(Level.SEVERE, this.getClass().getSimpleName() + ": Could not reset DailyMatchOlympiad: " + var18);
				}
			}
		}

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement("DELETE FROM clan_mercenary WHERE clan_Id=?");)
		{
			stmt.setInt(1, clanId);
			stmt.execute();
		}
		catch (SQLException var15)
		{
			LOGGER.warning("Problem with Clan removeMercenaryByClanId : " + var15.getMessage());
		}
	}

	public Map<Integer, MercenaryPledgeHolder> getMapMercenary()
	{
		return this._mercenaries;
	}

	public boolean isRecruitMercenary()
	{
		return this._vars.getBoolean("recruitMercenary", false);
	}

	public void setRecruitMercenary(boolean recruitMercenary)
	{
		this._vars.set("recruitMercenary", recruitMercenary);
	}

	public int getRewardMercenary()
	{
		return this._vars.getInt("rewardMercenary", 0);
	}

	public void setRewardMercenary(int rewardMercenary)
	{
		this._vars.set("rewardMercenary", rewardMercenary);
	}

	public static class RankPrivs
	{
		private final int _rankId;
		private final int _party;
		private final ClanPrivileges _rankPrivs;

		public RankPrivs(int rank, int party, int privs)
		{
			this._rankId = rank;
			this._party = party;
			this._rankPrivs = new ClanPrivileges(privs);
		}

		public RankPrivs(int rank, int party, ClanPrivileges rankPrivs)
		{
			this._rankId = rank;
			this._party = party;
			this._rankPrivs = rankPrivs;
		}

		public int getRank()
		{
			return this._rankId;
		}

		public int getParty()
		{
			return this._party;
		}

		public ClanPrivileges getPrivs()
		{
			return this._rankPrivs;
		}

		public void setPrivs(int privs)
		{
			this._rankPrivs.setMask(privs);
		}
	}

	public static class SubPledge
	{
		private final int _id;
		private String _subPledgeName;
		private int _leaderId;
		private final Map<Integer, Skill> _subPledgeSkills = new ConcurrentSkipListMap<>();

		public SubPledge(int id, String name, int leaderId)
		{
			this._id = id;
			this._subPledgeName = name;
			this._leaderId = leaderId;
		}

		public int getId()
		{
			return this._id;
		}

		public String getName()
		{
			return this._subPledgeName;
		}

		public void setName(String name)
		{
			this._subPledgeName = name;
		}

		public int getLeaderId()
		{
			return this._leaderId;
		}

		public void setLeaderId(int leaderId)
		{
			this._leaderId = leaderId;
		}

		public Skill addNewSkill(Skill skill)
		{
			return this._subPledgeSkills.put(skill.getId(), skill);
		}

		public Collection<Skill> getSkills()
		{
			return this._subPledgeSkills.values();
		}

		public Skill getSkill(int id)
		{
			return this._subPledgeSkills.get(id);
		}
	}
}
