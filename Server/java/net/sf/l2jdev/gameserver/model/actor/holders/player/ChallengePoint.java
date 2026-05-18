package net.sf.l2jdev.gameserver.model.actor.holders.player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.gameserver.data.xml.EnchantChallengePointData;
import net.sf.l2jdev.gameserver.model.actor.Player;

public class ChallengePoint
{
	private static final Logger LOGGER = Logger.getLogger(ChallengePoint.class.getName());
	public static final String INSERT_CHALLENGE_POINTS = "REPLACE INTO enchant_challenge_points (`charId`, `groupId`, `points`) VALUES (?, ?, ?)";
	public static final String RESTORE_CHALLENGE_POINTS = "SELECT * FROM enchant_challenge_points WHERE charId=?";
	public static final String INSERT_CHALLENGE_POINTS_RECHARGES = "REPLACE INTO enchant_challenge_points_recharges (`charId`, `groupId`, `optionIndex`, `count`) VALUES (?, ?, ?, ?)";
	public static final String RESTORE_CHALLENGE_POINTS_RECHARGES = "SELECT * FROM enchant_challenge_points_recharges WHERE charId=?";
	private final Player _owner;
	private int _nowGroup;
	private int _nowPoint;
	private final Map<Integer, Integer> _challengePoints = new HashMap<>();
	private final Map<Integer, Map<Integer, Integer>> _challengePointsRecharges = new HashMap<>();
	private final int[] _challengePointsPendingRecharge = new int[]
	{
		-1,
		-1
	};

	public ChallengePoint(Player owner)
	{
		this._owner = owner;
		this._nowGroup = 0;
		this._nowPoint = 0;
	}

	public void storeChallengePoints()
	{
		if (!this._challengePoints.isEmpty())
		{
			try (Connection conn = DatabaseFactory.getConnection())
			{
				try (PreparedStatement ps1 = conn.prepareStatement("REPLACE INTO enchant_challenge_points (`charId`, `groupId`, `points`) VALUES (?, ?, ?)"))
				{
					for (Entry<Integer, Integer> entry : this._challengePoints.entrySet())
					{
						ps1.setInt(1, this._owner.getObjectId());
						ps1.setInt(2, entry.getKey());
						ps1.setInt(3, entry.getValue());
						ps1.addBatch();
					}

					ps1.executeBatch();
				}

				try (PreparedStatement ps2 = conn.prepareStatement("REPLACE INTO enchant_challenge_points_recharges (`charId`, `groupId`, `optionIndex`, `count`) VALUES (?, ?, ?, ?)"))
				{
					for (Entry<Integer, Map<Integer, Integer>> entry : this._challengePointsRecharges.entrySet())
					{
						for (Entry<Integer, Integer> entry2 : entry.getValue().entrySet())
						{
							ps2.setInt(1, this._owner.getObjectId());
							ps2.setInt(2, entry.getKey());
							ps2.setInt(3, entry2.getKey());
							ps2.setInt(4, entry2.getValue());
							ps2.addBatch();
						}
					}

					ps2.executeBatch();
				}
			}
			catch (Exception var13)
			{
				LOGGER.warning("Could not store Challenge Points for " + this._owner + " " + var13);
			}
		}
	}

	public void restoreChallengePoints()
	{
		this._challengePoints.clear();

		try (Connection con = DatabaseFactory.getConnection())
		{
			try (PreparedStatement ps = con.prepareStatement("SELECT * FROM enchant_challenge_points WHERE charId=?"))
			{
				ps.setInt(1, this._owner.getObjectId());

				try (ResultSet rs = ps.executeQuery())
				{
					while (rs.next())
					{
						int groupId = rs.getInt("groupId");
						int points = rs.getInt("points");
						this._challengePoints.put(groupId, points);
					}
				}
			}

			this._challengePointsRecharges.clear();

			try (PreparedStatement ps = con.prepareStatement("SELECT * FROM enchant_challenge_points_recharges WHERE charId=?"))
			{
				ps.setInt(1, this._owner.getObjectId());

				try (ResultSet rs = ps.executeQuery())
				{
					while (rs.next())
					{
						int groupId = rs.getInt("groupId");
						int optionIndex = rs.getInt("optionIndex");
						int count = rs.getInt("count");
						Map<Integer, Integer> options = this._challengePointsRecharges.get(groupId);
						if (options == null)
						{
							options = new HashMap<>();
							this._challengePointsRecharges.put(groupId, options);
						}

						options.put(optionIndex, count);
					}
				}
			}
		}
		catch (Exception var18)
		{
			LOGGER.warning("Could not restore Challenge Points for " + this._owner + " " + var18);
		}
	}

	public int getNowPoint()
	{
		int nowPoint = this._nowPoint;
		this._nowPoint = 0;
		return nowPoint;
	}

	public int getNowGroup()
	{
		int nowGroup = this._nowGroup;
		this._nowGroup = 0;
		return nowGroup;
	}

	public void setNowGroup(int val)
	{
		this._nowGroup = val;
	}

	public void setNowPoint(int val)
	{
		this._nowPoint = val;
	}

	public Map<Integer, Integer> getChallengePoints()
	{
		return this._challengePoints;
	}

	public int getChallengePointsRecharges(int groupId, int optionIndex)
	{
		Map<Integer, Integer> options = this._challengePointsRecharges.get(groupId);
		return options != null ? options.getOrDefault(optionIndex, 0) : 0;
	}

	public void addChallengePointsRecharge(int groupId, int optionIndex, int amount)
	{
		Map<Integer, Integer> options = this._challengePointsRecharges.get(groupId);
		if (options == null)
		{
			options = new HashMap<>();
			this._challengePointsRecharges.put(groupId, options);
		}

		options.compute(optionIndex, (_, v) -> v == null ? amount : v + amount);
	}

	public void setChallengePointsPendingRecharge(int groupId, int optionIndex)
	{
		this._challengePointsPendingRecharge[0] = groupId;
		this._challengePointsPendingRecharge[1] = optionIndex;
	}

	public int[] getChallengePointsPendingRecharge()
	{
		return this._challengePointsPendingRecharge;
	}

	public ChallengePointInfoHolder[] initializeChallengePoints()
	{
		Map<Integer, Integer> challengePoints = this.getChallengePoints();
		ChallengePointInfoHolder[] info = new ChallengePointInfoHolder[challengePoints.size()];
		int i = 0;

		for (Entry<Integer, Integer> entry : challengePoints.entrySet())
		{
			int groupId = entry.getKey();
			info[i] = new ChallengePointInfoHolder(groupId, entry.getValue(), this.getChallengePointsRecharges(groupId, 0), this.getChallengePointsRecharges(groupId, 1), this.getChallengePointsRecharges(groupId, 2), this.getChallengePointsRecharges(groupId, 3), this.getChallengePointsRecharges(groupId, 4), this.getChallengePointsRecharges(groupId, 5));
			i++;
		}

		return info;
	}

	public boolean canAddPoints(int categoryId, int points)
	{
		int totalPoints = this._challengePoints.getOrDefault(categoryId, 0) + points;
		int maxPoints = EnchantChallengePointData.getInstance().getMaxPoints();
		return maxPoints > totalPoints;
	}
}
