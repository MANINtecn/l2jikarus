package net.sf.l2jdev.gameserver.model.prison;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.gameserver.config.PrisonConfig;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.olympiad.OlympiadManager;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class PrisonManager
{
	private static final Logger LOGGER = Logger.getLogger(PrisonManager.class.getName());
	public static final Map<Integer, Prisoner> PRISONERS = new ConcurrentHashMap<>();

	public static void processPK(Player player, boolean whileOnline)
	{
		if (player.getReputation() <= PrisonConfig.REPUTATION_FOR_ZONE_1)
		{
			processPrisoner(player, 1, PrisonConfig.SENTENCE_TIME_ZONE_1, PrisonConfig.ENTRANCE_LOC_ZONE_1, whileOnline);
			SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_REPUTATION_HAS_REACHED_S1_YOU_LL_BE_TELEPORTED_TO_THE_UNDERGROUND_LABYRINTH);
			msg.addInt((int) PrisonConfig.REPUTATION_FOR_ZONE_1);
			player.sendPacket(msg);
		}

		if (player.getPkKills() >= PrisonConfig.PK_FOR_ZONE_2 && player.getReputation() < 0)
		{
			processPrisoner(player, 2, PrisonConfig.SENTENCE_TIME_ZONE_2, PrisonConfig.ENTRANCE_LOC_ZONE_2, whileOnline);
			SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_REPUTATION_HAS_REACHED_S1_OR_YOUR_PK_COUNTER_IS_S2_OR_LESS_SO_YOU_ARE_TELEPORTED_TO_THE_UNDERGROUND_LABYRINTH);
			msg.addInt(player.getReputation());
			msg.addByte(PrisonConfig.PK_FOR_ZONE_1);
			player.sendPacket(msg);
		}
		else if (player.getPkKills() >= PrisonConfig.PK_FOR_ZONE_1 && player.getReputation() < 0)
		{
			processPrisoner(player, 1, PrisonConfig.SENTENCE_TIME_ZONE_1, PrisonConfig.ENTRANCE_LOC_ZONE_1, whileOnline);
			SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_REPUTATION_HAS_REACHED_S1_OR_YOUR_PK_COUNTER_IS_S2_OR_LESS_SO_YOU_ARE_TELEPORTED_TO_THE_UNDERGROUND_LABYRINTH);
			msg.addInt(player.getReputation());
			msg.addByte(PrisonConfig.PK_FOR_ZONE_1);
			player.sendPacket(msg);
		}
	}

	private static void processPrisoner(Player player, int zoneId, long sentenceTime, Location entrance, boolean whileOnline)
	{
		Prisoner prisoner = new Prisoner(player.getObjectId(), zoneId, sentenceTime);
		player.setPrisonerInfo(prisoner);
		PRISONERS.put(player.getObjectId(), prisoner);
		if (whileOnline)
		{
			if (OlympiadManager.getInstance().isRegistered(player))
			{
				OlympiadManager.getInstance().unRegisterNoble(player);
			}

			player.teleToLocation(entrance, 250);
		}
		else
		{
			player.setLocationInvisible(entrance);
		}
	}

	public static void loadPrisoner(Player player)
	{
		if (player.getReputation() < 0)
		{
			if (PRISONERS.containsKey(player.getObjectId()))
			{
				player.setPrisonerInfo(PRISONERS.get(player.getObjectId()));
				player.setLocationInvisible(getEntranceLocById(player.getPrisonerInfo().getZoneId()));
			}
			else if (!loadPrisonerFromDB(player))
			{
				processPK(player, false);
			}
		}
	}

	public static boolean loadPrisonerFromDB(Player player)
	{
		boolean output = false;

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM prisoners WHERE charId=?");)
		{
			statement.setInt(1, player.getObjectId());

			try (ResultSet rs = statement.executeQuery())
			{
				if (rs.next())
				{
					Prisoner prisoner = new Prisoner(rs.getInt("charId"), rs.getInt("zoneId"), rs.getLong("sentenceTime"), rs.getLong("timeSpent"), rs.getInt("bailAmount"));
					player.setPrisonerInfo(prisoner);
					player.setLocationInvisible(getEntranceLocById(rs.getInt("zoneId")));
					PRISONERS.put(player.getObjectId(), prisoner);
					output = true;
				}
			}
			catch (Exception var11)
			{
				LOGGER.log(Level.SEVERE, "Error selecting prisoner.", var11);
			}
		}
		catch (Exception var14)
		{
			LOGGER.log(Level.SEVERE, "Error restoring prisoner.", var14);
		}

		return output;
	}

	public static void savePrisonerOnDB(Player player)
	{
		if (PRISONERS.containsKey(player.getObjectId()))
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("INSERT INTO prisoners (charId,sentenceTime,timeSpent,zoneId,bailAmount) VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE sentenceTime=?,timeSpent=?,zoneId=?, bailAmount=?");)
			{
				statement.setInt(1, player.getObjectId());
				statement.setLong(2, player.getPrisonerInfo().getSentenceTime());
				statement.setLong(3, player.getPrisonerInfo().getTimeSpent());
				statement.setInt(4, player.getPrisonerInfo().getZoneId());
				statement.setLong(5, player.getPrisonerInfo().getSentenceTime());
				statement.setLong(6, player.getPrisonerInfo().getTimeSpent());
				statement.setInt(7, player.getPrisonerInfo().getZoneId());
				statement.executeUpdate();
			}
			catch (Exception var9)
			{
				LOGGER.log(Level.WARNING, "could not insert or update prisoner:", var9);
			}
		}
	}

	public static void savePrisonerOnVar(int playerId, Prisoner prisoner)
	{
		PRISONERS.put(playerId, prisoner);
	}

	public static void savePrisoners()
	{
		if (!PRISONERS.isEmpty())
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement dps = con.prepareStatement("DELETE FROM prisoners"); PreparedStatement ips = con.prepareStatement("INSERT INTO prisoners (charId,sentenceTime,timeSpent,zoneId) VALUES (?,?,?,?)");)
			{
				dps.executeUpdate();

				for (Entry<Integer, Prisoner> entry : PRISONERS.entrySet())
				{
					ips.setInt(1, entry.getKey());
					ips.setLong(2, entry.getValue().getSentenceTime());
					ips.setLong(3, entry.getValue().getTimeSpent());
					ips.setLong(4, entry.getValue().getZoneId());
					ips.addBatch();
				}

				ips.executeBatch();
			}
			catch (Exception var11)
			{
				LOGGER.log(Level.WARNING, "could not save prisoners:", var11);
			}
		}
	}

	public static void restorePrisoners()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM prisoners"); ResultSet rs = statement.executeQuery();)
		{
			while (rs.next())
			{
				Prisoner prisoner = new Prisoner(rs.getInt("charId"), rs.getInt("zoneId"), rs.getLong("sentenceTime"), rs.getLong("timeSpent"), rs.getInt("bailAmount"));
				PRISONERS.put(rs.getInt("charId"), prisoner);
			}
		}
		catch (Exception var11)
		{
			LOGGER.log(Level.SEVERE, "Error restoring prisoner.", var11);
		}
	}

	public static Location getEntranceLocById(int zoneId)
	{
		switch (zoneId)
		{
			case 1:
				return PrisonConfig.ENTRANCE_LOC_ZONE_1;
			case 2:
				return PrisonConfig.ENTRANCE_LOC_ZONE_2;
			default:
				return PrisonConfig.ENTRANCE_LOC_ZONE_1;
		}
	}

	public static Location getReleaseLoc(int zoneId)
	{
		switch (zoneId)
		{
			case 1:
				return PrisonConfig.RELEASE_LOC_ZONE_1;
			case 2:
				return PrisonConfig.RELEASE_LOC_ZONE_2;
			default:
				return PrisonConfig.RELEASE_LOC_ZONE_1;
		}
	}

	public static int getRepPointsReceived(int zoneId)
	{
		switch (zoneId)
		{
			case 1:
				return PrisonConfig.REP_POINTS_RECEIVED_BY_ZONE_1;
			case 2:
				return PrisonConfig.REP_POINTS_RECEIVED_BY_ZONE_2;
			default:
				return PrisonConfig.REP_POINTS_RECEIVED_BY_ZONE_1;
		}
	}
}
