package net.sf.l2jdev.gameserver.network.serverpackets.olympiad;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.olympiad.Olympiad;
import net.sf.l2jdev.gameserver.model.olympiad.OlympiadManager;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExOlympiadRecord extends ServerPacket
{
	protected static final String GET_PREVIOUS_CYCLE_DATA = "SELECT charId, class_id, olympiad_points, competitions_won, competitions_lost, (SELECT COUNT(*) FROM olympiad_nobles_eom WHERE olympiad_points > t.olympiad_points) AS previousPlaceTotal FROM olympiad_nobles_eom t WHERE class_id = ? ORDER BY olympiad_points DESC LIMIT 500";
	private final int _gameRuleType;
	private final int _type;
	private final int _noblePoints;
	private final int _competitionWon;
	private final int _competitionLost;
	private final int _remainingWeeklyMatches;
	private final int _previousPlace;
	private final int _previousWins;
	private final int _previousLoses;
	private final int _previousPoints;
	private final int _previousClass;
	private final int _previousPlaceTotal;
	private final boolean _inCompPeriod;
	private final int _currentCycle;

	public ExOlympiadRecord(Player player, int gameRuleType)
	{
		this._gameRuleType = gameRuleType;
		this._type = OlympiadManager.getInstance().isRegistered(player) ? 1 : 0;
		Olympiad olympiad = Olympiad.getInstance();
		this._noblePoints = olympiad.getNoblePoints(player);
		this._competitionWon = olympiad.getCompetitionWon(player.getObjectId());
		this._competitionLost = olympiad.getCompetitionLost(player.getObjectId());
		this._remainingWeeklyMatches = olympiad.getRemainingWeeklyMatches(player.getObjectId());
		this._inCompPeriod = olympiad.inCompPeriod();
		this._currentCycle = olympiad.getCurrentCycle();
		int previousPlace = 0;
		int previousWins = 0;
		int previousLoses = 0;
		int previousPoints = 0;
		int previousClass = 0;
		int previousPlaceTotal = 0;

		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT charId, class_id, olympiad_points, competitions_won, competitions_lost, (SELECT COUNT(*) FROM olympiad_nobles_eom WHERE olympiad_points > t.olympiad_points) AS previousPlaceTotal FROM olympiad_nobles_eom t WHERE class_id = ? ORDER BY olympiad_points DESC LIMIT 500");)
		{
			statement.setInt(1, player.getBaseClass());

			try (ResultSet rset = statement.executeQuery())
			{
				if (rset.next())
				{
					previousPlace = rset.getRow();
					previousWins = rset.getInt("competitions_won");
					previousLoses = rset.getInt("competitions_lost");
					previousPoints = rset.getInt("olympiad_points");
					previousClass = rset.getInt("class_id");
					previousPlaceTotal = rset.getInt("previousPlaceTotal") + 1;
				}
			}
		}
		catch (Exception var21)
		{
			PacketLogger.warning("ExOlympiadRecord: Could not load data: " + var21.getMessage());
		}

		this._previousPlace = previousPlace;
		this._previousWins = previousWins;
		this._previousLoses = previousLoses;
		this._previousPoints = previousPoints;
		this._previousClass = previousClass;
		this._previousPlaceTotal = previousPlaceTotal;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_OLYMPIAD_RECORD.writeId(this, buffer);
		buffer.writeInt(this._noblePoints);
		buffer.writeInt(this._competitionWon);
		buffer.writeInt(this._competitionLost);
		buffer.writeInt(this._remainingWeeklyMatches);
		buffer.writeInt(this._previousClass);
		buffer.writeInt(this._previousPlaceTotal);
		buffer.writeInt(2);
		buffer.writeInt(this._previousPlace);
		buffer.writeInt(4);
		buffer.writeInt(5);
		buffer.writeInt(6);
		buffer.writeInt(this._previousPoints);
		buffer.writeInt(this._previousWins);
		buffer.writeInt(this._previousLoses);
		buffer.writeInt(this._previousPlace);
		buffer.writeInt(Calendar.getInstance().get(1));
		buffer.writeInt(Calendar.getInstance().get(2) + 1);
		buffer.writeByte(this._inCompPeriod);
		buffer.writeInt(this._currentCycle);
		buffer.writeByte(this._type);
		buffer.writeInt(this._gameRuleType);
	}
}
