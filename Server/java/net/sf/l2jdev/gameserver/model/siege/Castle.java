package net.sf.l2jdev.gameserver.model.siege;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.config.FeatureConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.data.xml.CastleData;
import net.sf.l2jdev.gameserver.data.xml.DoorData;
import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.managers.CastleManorManager;
import net.sf.l2jdev.gameserver.managers.FortManager;
import net.sf.l2jdev.gameserver.managers.GlobalVariablesManager;
import net.sf.l2jdev.gameserver.managers.SiegeManager;
import net.sf.l2jdev.gameserver.managers.ZoneManager;
import net.sf.l2jdev.gameserver.model.Spawn;
import net.sf.l2jdev.gameserver.model.TowerSpawn;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.MountType;
import net.sf.l2jdev.gameserver.model.actor.instance.Artefact;
import net.sf.l2jdev.gameserver.model.actor.instance.Door;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.itemcontainer.Inventory;
import net.sf.l2jdev.gameserver.model.residences.AbstractResidence;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;
import net.sf.l2jdev.gameserver.model.zone.type.CastleZone;
import net.sf.l2jdev.gameserver.model.zone.type.ResidenceTeleportZone;
import net.sf.l2jdev.gameserver.model.zone.type.SiegeZone;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ExCastleState;
import net.sf.l2jdev.gameserver.network.serverpackets.PlaySound;
import net.sf.l2jdev.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.util.Broadcast;

public class Castle extends AbstractResidence
{
	protected static final Logger LOGGER = Logger.getLogger(Castle.class.getName());
	private final List<Door> _doors = new ArrayList<>();
	private final List<Npc> _sideNpcs = new ArrayList<>();
	int _ownerId = 0;
	private Siege _siege = null;
	private Calendar _siegeDate;
	private boolean _isTimeRegistrationOver = true;
	private Calendar _siegeTimeRegistrationEndDate;
	private CastleSide _castleSide = null;
	private long _treasury = 0L;
	private long _tempTreasury = 0L;
	private boolean _showNpcCrest = false;
	private SiegeZone _zone = null;
	private ResidenceTeleportZone _teleZone;
	private Clan _formerOwner = null;
	private final Set<Artefact> _artefacts = new HashSet<>(1);
	private final Map<Integer, Castle.CastleFunction> _function = new ConcurrentHashMap<>();
	private int _ticketBuyCount = 0;
	private boolean _isFirstMidVictory = false;
	public static final int FUNC_TELEPORT = 1;
	public static final int FUNC_RESTORE_HP = 2;
	public static final int FUNC_RESTORE_MP = 3;
	public static final int FUNC_RESTORE_EXP = 4;
	public static final int FUNC_SUPPORT = 5;

	public Castle(int castleId)
	{
		super(castleId);
		this.load();
		this.initResidenceZone();
		this.spawnSideNpcs();
		if (this._ownerId != 0)
		{
			this.loadFunctions();
			this.loadDoorUpgrade();
		}
	}

	public Castle.CastleFunction getCastleFunction(int type)
	{
		return this._function.containsKey(type) ? this._function.get(type) : null;
	}

	public synchronized void engrave(Clan clan, WorldObject target, CastleSide side)
	{
		if (this._artefacts.contains(target))
		{
			this.setSide(side);
			this.setOwner(clan);
			SystemMessage msg = new SystemMessage(SystemMessageId.CLAN_S1_HAS_SUCCEEDED_IN_S2);
			msg.addString(clan.getName());
			msg.addString(this.getName());
			this.getSiege().announceToPlayer(msg, true);
		}
	}

	public void addToTreasury(long amountValue)
	{
		if (this._ownerId > 0)
		{
			long amount = amountValue;
			String var5 = this.getName().toLowerCase();
			switch (var5)
			{
				case "schuttgart":
				case "goddard":
					Castle rune = CastleManager.getInstance().getCastle("rune");
					if (rune != null)
					{
						long runeTax = (long) (amountValue * rune.getTaxRate(TaxType.BUY));
						if (rune.getOwnerId() > 0)
						{
							rune.addToTreasury(runeTax);
						}

						amount = amountValue - runeTax;
					}
					break;
				case "dion":
				case "giran":
				case "gludio":
				case "innadril":
				case "oren":
					Castle aden = CastleManager.getInstance().getCastle("aden");
					if (aden != null)
					{
						long adenTax = (long) (amountValue * aden.getTaxRate(TaxType.BUY));
						if (aden.getOwnerId() > 0)
						{
							aden.addToTreasury(adenTax);
						}

						amount = amountValue - adenTax;
					}
			}

			this.addToTreasuryNoTax(amount);
		}
	}

	public void updateTempTreasure(long amountValue)
	{
		LOGGER.info("Update tempTreasure for mercenary castle - " + this.getName());

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE castle SET dynamicTreasury = ? WHERE id = ?");)
		{
			ps.setLong(1, amountValue);
			ps.setInt(2, this.getResidenceId());
			ps.execute();
		}
		catch (Exception var11)
		{
			LOGGER.log(Level.WARNING, var11.getMessage(), var11);
		}
	}

	public boolean addToTreasuryTemp(long amountValue)
	{
		if (this._ownerId <= 0)
		{
			return false;
		}
		if (amountValue < 0L)
		{
			long amount = amountValue * -1L;
			if (this._tempTreasury < amount)
			{
				return false;
			}

			this._tempTreasury -= amount;
		}
		else if (this._tempTreasury + amountValue > Inventory.MAX_ADENA)
		{
			this._tempTreasury = Inventory.MAX_ADENA;
		}
		else
		{
			this._tempTreasury += amountValue;
		}

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE castle SET dynamicTreasury = ? WHERE id = ?");)
		{
			ps.setLong(1, this._tempTreasury);
			ps.setInt(2, this.getResidenceId());
			ps.execute();
		}
		catch (Exception var13)
		{
			LOGGER.log(Level.WARNING, var13.getMessage(), var13);
		}

		return true;
	}

	public boolean addToTreasuryNoTax(long amountValue)
	{
		if (this._ownerId <= 0)
		{
			return false;
		}
		if (amountValue < 0L)
		{
			long amount = amountValue * -1L;
			if (this._treasury < amount)
			{
				return false;
			}

			this._treasury -= amount;
		}
		else if (this._treasury + amountValue > Inventory.MAX_ADENA)
		{
			this._treasury = Inventory.MAX_ADENA;
		}
		else
		{
			this._treasury += amountValue;
		}

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE castle SET treasury = ? WHERE id = ?");)
		{
			ps.setLong(1, this._treasury);
			ps.setInt(2, this.getResidenceId());
			ps.execute();
		}
		catch (Exception var13)
		{
			LOGGER.log(Level.WARNING, var13.getMessage(), var13);
		}

		return true;
	}

	public void banishForeigners()
	{
		this.getResidenceZone().banishForeigners(this._ownerId);
	}

	public boolean checkIfInZone(int x, int y, int z)
	{
		SiegeZone zone = this.getZone();
		return zone != null && zone.isInsideZone(x, y, z);
	}

	public SiegeZone getZone()
	{
		if (this._zone == null)
		{
			for (SiegeZone zone : ZoneManager.getInstance().getAllZones(SiegeZone.class))
			{
				if (zone.getSiegeObjectId() == this.getResidenceId())
				{
					this._zone = zone;
					break;
				}
			}
		}

		return this._zone;
	}

	@Override
	public CastleZone getResidenceZone()
	{
		return (CastleZone) super.getResidenceZone();
	}

	public ResidenceTeleportZone getTeleZone()
	{
		if (this._teleZone == null)
		{
			for (ResidenceTeleportZone zone : ZoneManager.getInstance().getAllZones(ResidenceTeleportZone.class))
			{
				if (zone.getResidenceId() == this.getResidenceId())
				{
					this._teleZone = zone;
					break;
				}
			}
		}

		return this._teleZone;
	}

	public void oustAllPlayers()
	{
		this.getTeleZone().oustAllPlayers();
	}

	public double getDistance(WorldObject obj)
	{
		return this.getZone().getDistanceToZone(obj);
	}

	public void closeDoor(Player player, int doorId)
	{
		this.openCloseDoor(player, doorId, false);
	}

	public void openDoor(Player player, int doorId)
	{
		this.openCloseDoor(player, doorId, true);
	}

	public void openCloseDoor(Player player, int doorId, boolean open)
	{
		if (player.getClanId() == this._ownerId || player.isGM())
		{
			Door door = this.getDoor(doorId);
			if (door != null)
			{
				if (open)
				{
					door.openMe();
				}
				else
				{
					door.closeMe();
				}
			}
		}
	}

	public void openCloseDoor(Player player, String doorName, boolean open)
	{
		if (player.getClanId() == this._ownerId || player.isGM())
		{
			Door door = this.getDoor(doorName);
			if (door != null)
			{
				if (open)
				{
					door.openMe();
				}
				else
				{
					door.closeMe();
				}
			}
		}
	}

	public void removeUpgrade()
	{
		this.removeDoorUpgrade();
		this.removeTrapUpgrade();

		for (Integer fc : this._function.keySet())
		{
			this.removeFunction(fc);
		}

		this._function.clear();
	}

	public void setOwner(Clan clan)
	{
		if (this._ownerId > 0 && (clan == null || clan.getId() != this._ownerId))
		{
			Clan oldOwner = ClanTable.getInstance().getClan(this.getOwnerId());
			if (oldOwner != null)
			{
				if (this._formerOwner == null)
				{
					this._formerOwner = oldOwner;
					if (PlayerConfig.REMOVE_CASTLE_CIRCLETS)
					{
						CastleManager.getInstance().removeCirclet(this._formerOwner, this.getResidenceId());
					}
				}

				try
				{
					Player oldleader = oldOwner.getLeader().getPlayer();
					if (oldleader != null && oldleader.getMountType() == MountType.WYVERN)
					{
						oldleader.dismount();
					}
				}
				catch (Exception var5)
				{
					LOGGER.log(Level.WARNING, "Exception in setOwner: " + var5.getMessage(), var5);
				}

				oldOwner.setCastleId(0);

				for (Player member : oldOwner.getOnlineMembers(0))
				{
					this.removeResidentialSkills(member);
					member.sendSkillList();
					member.broadcastUserInfo();
				}
			}
		}

		this.updateOwnerInDB(clan);
		this.setShowNpcCrest(false);
		if (clan != null && clan.getFortId() > 0)
		{
			FortManager.getInstance().getFortByOwner(clan).removeOwner(true);
		}

		if (this.getSiege().isInProgress())
		{
			this.getSiege().midVictory();
		}

		if (clan != null)
		{
			for (Player member : clan.getOnlineMembers(0))
			{
				this.giveResidentialSkills(member);
				member.sendSkillList();
			}
		}
	}

	public void removeOwner(Clan clan)
	{
		if (clan != null)
		{
			this._formerOwner = clan;
			if (PlayerConfig.REMOVE_CASTLE_CIRCLETS)
			{
				CastleManager.getInstance().removeCirclet(this._formerOwner, this.getResidenceId());
			}

			for (Player member : clan.getOnlineMembers(0))
			{
				this.removeResidentialSkills(member);
				member.sendSkillList();
			}

			clan.setCastleId(0);
			clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
		}

		this.setSide(CastleSide.NEUTRAL);
		this.updateOwnerInDB(null);
		if (this.getSiege().isInProgress())
		{
			this.getSiege().midVictory();
		}

		for (Integer fc : this._function.keySet())
		{
			this.removeFunction(fc);
		}

		this._function.clear();
	}

	public void spawnDoor()
	{
		this.spawnDoor(false);
	}

	public void spawnDoor(boolean isDoorWeak)
	{
		for (Door door : this._doors)
		{
			if (door.isDead())
			{
				door.doRevive();
				door.setCurrentHp(isDoorWeak ? door.getMaxHp() / 2L : door.getMaxHp());
			}

			if (door.isOpen())
			{
				door.closeMe();
			}
		}
	}

	@Override
	protected void load()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps1 = con.prepareStatement("SELECT * FROM castle WHERE id = ?"); PreparedStatement ps2 = con.prepareStatement("SELECT clan_id FROM clan_data WHERE hasCastle = ?");)
		{
			ps1.setInt(1, this.getResidenceId());

			try (ResultSet rs = ps1.executeQuery())
			{
				while (rs.next())
				{
					this.setName(rs.getString("name"));
					this._siegeDate = Calendar.getInstance();
					this._siegeDate.setTimeInMillis(rs.getLong("siegeDate"));
					this._siegeTimeRegistrationEndDate = Calendar.getInstance();
					this._siegeTimeRegistrationEndDate.setTimeInMillis(rs.getLong("regTimeEnd"));
					this._isTimeRegistrationOver = Boolean.parseBoolean(rs.getString("regTimeOver"));
					this._castleSide = Enum.valueOf(CastleSide.class, rs.getString("side"));
					this._treasury = rs.getLong("treasury");
					this._tempTreasury = rs.getLong("dynamicTreasury");
					this._showNpcCrest = Boolean.parseBoolean(rs.getString("showNpcCrest"));
					this._ticketBuyCount = rs.getInt("ticketBuyCount");
				}
			}

			ps2.setInt(1, this.getResidenceId());

			try (ResultSet rs = ps2.executeQuery())
			{
				while (rs.next())
				{
					this._ownerId = rs.getInt("clan_id");
				}
			}
		}
		catch (Exception var17)
		{
			LOGGER.log(Level.WARNING, "Exception: loadCastleData(): " + var17.getMessage(), var17);
		}
	}

	private void loadFunctions()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM castle_functions WHERE castle_id = ?");)
		{
			ps.setInt(1, this.getResidenceId());

			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					this._function.put(rs.getInt("type"), new Castle.CastleFunction(rs.getInt("type"), rs.getInt("lvl"), rs.getInt("lease"), 0, rs.getLong("rate"), rs.getLong("endTime"), true));
				}
			}
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.SEVERE, "Exception: Castle.loadFunctions(): " + var12.getMessage(), var12);
		}
	}

	public void removeFunction(int functionType)
	{
		this._function.remove(functionType);

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM castle_functions WHERE castle_id=? AND type=?");)
		{
			ps.setInt(1, this.getResidenceId());
			ps.setInt(2, functionType);
			ps.execute();
		}
		catch (Exception var10)
		{
			LOGGER.log(Level.SEVERE, "Exception: Castle.removeFunctions(int functionType): " + var10.getMessage(), var10);
		}
	}

	public boolean updateFunctions(Player player, int type, int lvl, int lease, long rate, boolean addNew)
	{
		if (player == null)
		{
			return false;
		}
		else if (lease > 0 && !player.destroyItemByItemId(null, 57, lease, null, true))
		{
			return false;
		}
		else
		{
			if (addNew)
			{
				this._function.put(type, new Castle.CastleFunction(type, lvl, lease, 0, rate, 0L, false));
			}
			else if (lvl == 0 && lease == 0)
			{
				this.removeFunction(type);
			}
			else
			{
				int diffLease = lease - this._function.get(type).getLease();
				if (diffLease > 0)
				{
					this._function.remove(type);
					this._function.put(type, new Castle.CastleFunction(type, lvl, lease, 0, rate, -1L, false));
				}
				else
				{
					this._function.get(type).setLease(lease);
					this._function.get(type).setLvl(lvl);
					this._function.get(type).dbSave();
				}
			}

			return true;
		}
	}

	public void activateInstance()
	{
		this.loadDoor();
	}

	private void loadDoor()
	{
		for (Door door : DoorData.getInstance().getDoors())
		{
			if (door.getCastle() != null && door.getCastle().getResidenceId() == this.getResidenceId())
			{
				this._doors.add(door);
			}
		}
	}

	private void loadDoorUpgrade()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM castle_doorupgrade WHERE castleId=?");)
		{
			ps.setInt(1, this.getResidenceId());

			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					this.setDoorUpgrade(rs.getInt("doorId"), rs.getInt("ratio"), false);
				}
			}
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.WARNING, "Exception: loadCastleDoorUpgrade(): " + var12.getMessage(), var12);
		}
	}

	private void removeDoorUpgrade()
	{
		for (Door door : this._doors)
		{
			door.getStat().setUpgradeHpRatio(1);
			door.setCurrentHp(door.getCurrentHp());
		}

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM castle_doorupgrade WHERE castleId=?");)
		{
			ps.setInt(1, this.getResidenceId());
			ps.execute();
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.WARNING, "Exception: removeDoorUpgrade(): " + var9.getMessage(), var9);
		}
	}

	public void setDoorUpgrade(int doorId, int ratio, boolean save)
	{
		Door door = this.getDoors().isEmpty() ? DoorData.getInstance().getDoor(doorId) : this.getDoor(doorId);
		if (door != null)
		{
			door.getStat().setUpgradeHpRatio(ratio);
			door.setCurrentHp(door.getMaxHp());
			if (save)
			{
				try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("REPLACE INTO castle_doorupgrade (doorId, ratio, castleId) values (?,?,?)");)
				{
					ps.setInt(1, doorId);
					ps.setInt(2, ratio);
					ps.setInt(3, this.getResidenceId());
					ps.execute();
				}
				catch (Exception var13)
				{
					LOGGER.log(Level.WARNING, "Exception: setDoorUpgrade(int doorId, int ratio, int castleId): " + var13.getMessage(), var13);
				}
			}
		}
	}

	private void updateOwnerInDB(Clan clan)
	{
		if (clan != null)
		{
			this._ownerId = clan.getId();
		}
		else
		{
			this._ownerId = 0;
			CastleManorManager.getInstance().resetManorData(this.getResidenceId());
		}

		try (Connection con = DatabaseFactory.getConnection())
		{
			try (PreparedStatement ps = con.prepareStatement("UPDATE clan_data SET hasCastle = 0 WHERE hasCastle = ?"))
			{
				ps.setInt(1, this.getResidenceId());
				ps.execute();
			}

			try (PreparedStatement ps = con.prepareStatement("UPDATE clan_data SET hasCastle = ? WHERE clan_id = ?"))
			{
				ps.setInt(1, this.getResidenceId());
				ps.setInt(2, this._ownerId);
				ps.execute();
			}

			if (clan != null)
			{
				clan.setCastleId(this.getResidenceId());
				clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
				clan.broadcastToOnlineMembers(new PlaySound(1, "Siege_Victory", 0, 0, 0, 0, 0));
			}
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.WARNING, "Exception: updateOwnerInDB(Pledge clan): " + var12.getMessage(), var12);
		}
	}

	public Door getDoor(int doorId)
	{
		for (Door door : this._doors)
		{
			if (door.getId() == doorId)
			{
				return door;
			}
		}

		return null;
	}

	public Door getDoor(String doorName)
	{
		for (Door door : this._doors)
		{
			if (door.getTemplate().getName().equals(doorName))
			{
				return door;
			}
		}

		return null;
	}

	public List<Door> getDoors()
	{
		return this._doors;
	}

	public boolean isFirstMidVictory()
	{
		return this._isFirstMidVictory;
	}

	public void setFirstMidVictory(boolean value)
	{
		this._isFirstMidVictory = value;
	}

	@Override
	public int getOwnerId()
	{
		return this._ownerId;
	}

	public Clan getOwner()
	{
		return this._ownerId != 0 ? ClanTable.getInstance().getClan(this._ownerId) : null;
	}

	public Siege getSiege()
	{
		if (this._siege == null)
		{
			this._siege = new Siege(this);
		}

		return this._siege;
	}

	public Calendar getSiegeDate()
	{
		return this._siegeDate;
	}

	public boolean isTimeRegistrationOver()
	{
		return this._isTimeRegistrationOver;
	}

	public void setTimeRegistrationOver(boolean value)
	{
		this._isTimeRegistrationOver = value;
	}

	public Calendar getTimeRegistrationOverDate()
	{
		if (this._siegeTimeRegistrationEndDate == null)
		{
			this._siegeTimeRegistrationEndDate = Calendar.getInstance();
		}

		return this._siegeTimeRegistrationEndDate;
	}

	public int getTaxPercent(TaxType type)
	{
		return switch (this._castleSide)
		{
			case LIGHT -> type == TaxType.BUY ? FeatureConfig.CASTLE_BUY_TAX_LIGHT : FeatureConfig.CASTLE_SELL_TAX_LIGHT;
			case DARK -> type == TaxType.BUY ? FeatureConfig.CASTLE_BUY_TAX_DARK : FeatureConfig.CASTLE_SELL_TAX_DARK;
			default -> GlobalVariablesManager.getInstance().getInt("TAX_RATE_" + this.getResidenceId(), 0);
		};
	}

	public double getTaxRate(TaxType taxType)
	{
		return this.getTaxPercent(taxType) / 100.0;
	}

	public long getTempTreasury()
	{
		return this._tempTreasury;
	}

	public long getTreasury()
	{
		return this._treasury;
	}

	public boolean getShowNpcCrest()
	{
		return this._showNpcCrest;
	}

	public void setShowNpcCrest(boolean showNpcCrest)
	{
		if (this._showNpcCrest != showNpcCrest)
		{
			this._showNpcCrest = showNpcCrest;
			this.updateShowNpcCrest();
		}
	}

	public void updateClansReputation()
	{
		Clan owner = ClanTable.getInstance().getClan(this.getOwnerId());
		if (this._formerOwner != null)
		{
			if (this._formerOwner != owner)
			{
				int maxreward = Math.max(0, this._formerOwner.getReputationScore());
				this._formerOwner.takeReputationScore(FeatureConfig.LOOSE_CASTLE_POINTS);
				if (owner != null)
				{
					owner.addReputationScore(Math.min(FeatureConfig.TAKE_CASTLE_POINTS, maxreward));
					owner.broadcastToOnlineMembers(new SystemMessage(SystemMessageId.YOUR_CLAN_HAS_WON_THE_SIEGE_CLAN_REPUTATION_POINTS_S1).addInt(Math.min(FeatureConfig.TAKE_CASTLE_POINTS, maxreward)));
				}

				this._formerOwner.broadcastToOnlineMembers(new SystemMessage(SystemMessageId.YOUR_CLAN_HAS_LOST_THE_SIEGE_CLAN_REPUTATION_POINTS_S1).addInt(FeatureConfig.LOOSE_CASTLE_POINTS));
			}
			else
			{
				this._formerOwner.addReputationScore(FeatureConfig.CASTLE_DEFENDED_POINTS);
				this._formerOwner.broadcastToOnlineMembers(new SystemMessage(SystemMessageId.YOUR_CLAN_HAS_WON_THE_SIEGE_CLAN_REPUTATION_POINTS_S1).addInt(FeatureConfig.CASTLE_DEFENDED_POINTS));
			}
		}
		else if (owner != null)
		{
			owner.addReputationScore(FeatureConfig.TAKE_CASTLE_POINTS);
			owner.broadcastToOnlineMembers(new SystemMessage(SystemMessageId.YOUR_CLAN_HAS_WON_THE_SIEGE_CLAN_REPUTATION_POINTS_S1).addInt(FeatureConfig.TAKE_CASTLE_POINTS));
		}
	}

	public void updateShowNpcCrest()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE castle SET showNpcCrest = ? WHERE id = ?");)
		{
			ps.setString(1, String.valueOf(this._showNpcCrest));
			ps.setInt(2, this.getResidenceId());
			ps.execute();
		}
		catch (Exception var9)
		{
			LOGGER.info("Error saving showNpcCrest for castle " + this.getName() + ": " + var9.getMessage());
		}
	}

	public void registerArtefact(Artefact artefact)
	{
		this._artefacts.add(artefact);
	}

	public Set<Artefact> getArtefacts()
	{
		return this._artefacts;
	}

	public int getTicketBuyCount()
	{
		return this._ticketBuyCount;
	}

	public void setTicketBuyCount(int count)
	{
		this._ticketBuyCount = count;

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE castle SET ticketBuyCount = ? WHERE id = ?");)
		{
			ps.setInt(1, this._ticketBuyCount);
			ps.setInt(2, this.getResidenceId());
			ps.execute();
		}
		catch (Exception var10)
		{
			LOGGER.log(Level.WARNING, var10.getMessage(), var10);
		}
	}

	public int getTrapUpgradeLevel(int towerIndex)
	{
		TowerSpawn spawn = SiegeManager.getInstance().getFlameTowers(this.getResidenceId()).get(towerIndex);
		return spawn != null ? spawn.getUpgradeLevel() : 0;
	}

	public void setTrapUpgrade(int towerIndex, int level, boolean save)
	{
		if (save)
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("REPLACE INTO castle_trapupgrade (castleId, towerIndex, level) values (?,?,?)");)
			{
				ps.setInt(1, this.getResidenceId());
				ps.setInt(2, towerIndex);
				ps.setInt(3, level);
				ps.execute();
			}
			catch (Exception var12)
			{
				LOGGER.log(Level.WARNING, "Exception: setTrapUpgradeLevel(int towerIndex, int level, int castleId): " + var12.getMessage(), var12);
			}
		}

		TowerSpawn spawn = SiegeManager.getInstance().getFlameTowers(this.getResidenceId()).get(towerIndex);
		if (spawn != null)
		{
			spawn.setUpgradeLevel(level);
		}
	}

	private void removeTrapUpgrade()
	{
		for (TowerSpawn ts : SiegeManager.getInstance().getFlameTowers(this.getResidenceId()))
		{
			ts.setUpgradeLevel(0);
		}

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM castle_trapupgrade WHERE castleId=?");)
		{
			ps.setInt(1, this.getResidenceId());
			ps.execute();
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.WARNING, "Exception: removeDoorUpgrade(): " + var9.getMessage(), var9);
		}
	}

	@Override
	protected void initResidenceZone()
	{
		for (CastleZone zone : ZoneManager.getInstance().getAllZones(CastleZone.class))
		{
			if (zone.getResidenceId() == this.getResidenceId())
			{
				this.setResidenceZone(zone);
				break;
			}
		}
	}

	@Override
	public void giveResidentialSkills(Player player)
	{
		super.giveResidentialSkills(player);

		for (SkillHolder sh : CastleData.getSkills().getOrDefault(this.getResidenceId(), Collections.emptyList()))
		{
			player.addSkill(sh.getSkill());
		}
	}

	@Override
	public void removeResidentialSkills(Player player)
	{
		super.removeResidentialSkills(player);

		for (SkillHolder sh : CastleData.getSkills().getOrDefault(this.getResidenceId(), Collections.emptyList()))
		{
			player.removeSkill(sh.getSkill());
		}
	}

	public void spawnSideNpcs()
	{
		for (Npc npc : this._sideNpcs)
		{
			if (npc != null)
			{
				npc.deleteMe();
			}
		}

		this._sideNpcs.clear();

		for (CastleSpawnHolder holder : this.getSideSpawns())
		{
			if (holder != null)
			{
				Spawn spawn;
				try
				{
					spawn = new Spawn(holder.getNpcId());
				}
				catch (Exception var5)
				{
					LOGGER.warning(Castle.class.getSimpleName() + ": " + var5.getMessage());
					return;
				}

				spawn.setXYZ(holder);
				spawn.setHeading(holder.getHeading());
				Npc npcx = spawn.doSpawn(false);
				spawn.stopRespawn();
				npcx.broadcastInfo();
				this._sideNpcs.add(npcx);
			}
		}
	}

	public List<CastleSpawnHolder> getSideSpawns()
	{
		return CastleData.getInstance().getSpawnsForSide(this.getResidenceId(), this.getSide());
	}

	public void setSide(CastleSide side)
	{
		if (this._castleSide != side)
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE castle SET side = ? WHERE id = ?");)
			{
				ps.setString(1, side.toString());
				ps.setInt(2, this.getResidenceId());
				ps.execute();
			}
			catch (Exception var10)
			{
				LOGGER.log(Level.WARNING, var10.getMessage(), var10);
			}

			this._castleSide = side;
			Broadcast.toAllOnlinePlayers(new ExCastleState(this));
			this.spawnSideNpcs();
		}
	}

	public CastleSide getSide()
	{
		return this._castleSide;
	}

	public class CastleFunction
	{
		final int _type;
		private int _lvl;
		protected int _fee;
		protected int _tempFee;
		final long _rate;
		long _endDate;
		protected boolean _inDebt;
		public boolean _cwh;

		public CastleFunction(int type, int lvl, int lease, int tempLease, long rate, long time, boolean cwh)
		{
			Objects.requireNonNull(Castle.this);
			super();
			this._type = type;
			this._lvl = lvl;
			this._fee = lease;
			this._tempFee = tempLease;
			this._rate = rate;
			this._endDate = time;
			this.initializeTask(cwh);
		}

		public int getType()
		{
			return this._type;
		}

		public int getLvl()
		{
			return this._lvl;
		}

		public int getLease()
		{
			return this._fee;
		}

		public long getRate()
		{
			return this._rate;
		}

		public long getEndTime()
		{
			return this._endDate;
		}

		public void setLvl(int lvl)
		{
			this._lvl = lvl;
		}

		public void setLease(int lease)
		{
			this._fee = lease;
		}

		public void setEndTime(long time)
		{
			this._endDate = time;
		}

		private void initializeTask(boolean cwh)
		{
			if (Castle.this._ownerId > 0)
			{
				long currentTime = System.currentTimeMillis();
				if (this._endDate > currentTime)
				{
					ThreadPool.schedule(new Castle.CastleFunction.FunctionTask(cwh), this._endDate - currentTime);
				}
				else
				{
					ThreadPool.schedule(new Castle.CastleFunction.FunctionTask(cwh), 0L);
				}
			}
		}

		public void dbSave()
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("REPLACE INTO castle_functions (castle_id, type, lvl, lease, rate, endTime) VALUES (?,?,?,?,?,?)");)
			{
				ps.setInt(1, Castle.this.getResidenceId());
				ps.setInt(2, this._type);
				ps.setInt(3, this._lvl);
				ps.setInt(4, this._fee);
				ps.setLong(5, this._rate);
				ps.setLong(6, this._endDate);
				ps.execute();
			}
			catch (Exception var9)
			{
				Castle.LOGGER.log(Level.SEVERE, "Exception: Castle.updateFunctions(int type, int lvl, int lease, long rate, long time, boolean addNew): " + var9.getMessage(), var9);
			}
		}

		private class FunctionTask implements Runnable
		{
			public FunctionTask(boolean cwh)
			{
				Objects.requireNonNull(CastleFunction.this);
				super();
				CastleFunction.this._cwh = cwh;
			}

			@Override
			public void run()
			{
				try
				{
					if (Castle.this._ownerId <= 0)
					{
						return;
					}

					if (ClanTable.getInstance().getClan(Castle.this.getOwnerId()).getWarehouse().getAdena() < CastleFunction.this._fee && CastleFunction.this._cwh)
					{
						Castle.this.removeFunction(CastleFunction.this._type);
					}
					else
					{
						int fee = CastleFunction.this._fee;
						if (CastleFunction.this._endDate == -1L)
						{
							fee = CastleFunction.this._tempFee;
						}

						CastleFunction.this.setEndTime(System.currentTimeMillis() + CastleFunction.this._rate);
						CastleFunction.this.dbSave();
						if (CastleFunction.this._cwh)
						{
							ClanTable.getInstance().getClan(Castle.this.getOwnerId()).getWarehouse().destroyItemByItemId(ItemProcessType.FEE, 57, fee, null, null);
						}

						ThreadPool.schedule(CastleFunction.this.new FunctionTask(true), CastleFunction.this._rate);
					}
				}
				catch (Exception var2)
				{
					Castle.LOGGER.log(Level.SEVERE, "", var2);
				}
			}
		}
	}
}
