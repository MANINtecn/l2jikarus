package net.sf.l2jdev.gameserver.network.serverpackets.ranking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.OlympiadConfig;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.olympiad.Hero;
import net.sf.l2jdev.gameserver.model.olympiad.Olympiad;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExOlympiadMyRankingInfo extends ServerPacket
{
	 private final Player _player;

	public ExOlympiadMyRankingInfo(Player player)
	{
		this._player = player;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_OLYMPIAD_MY_RANKING_INFO.writeId(this, buffer);
		Date date = new Date();
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		int year = calendar.get(1);
		int month = calendar.get(2) + 1;
		int currentPlace = 0;
		int currentWins = 0;
		int currentLoses = 0;
		int currentPoints = 0;

		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT charId, olympiad_points, competitions_won, competitions_lost, competitions_drawn FROM olympiad_nobles WHERE class_id = ? ORDER BY olympiad_points DESC, competitions_won DESC LIMIT 500");)
		{
			statement.setInt(1, this._player.getBaseClass());

			try (ResultSet rset = statement.executeQuery())
			{
				for (int i = 1; rset.next(); i++)
				{
					if (rset.getInt("charId") == this._player.getObjectId())
					{
						currentPlace = i;
						currentWins = rset.getInt("competitions_won");
						currentLoses = rset.getInt("competitions_lost");
						currentPoints = rset.getInt("olympiad_points");
					}
				}
			}
		}
		catch (SQLException var34)
		{
			PacketLogger.warning("Olympiad my ranking: Could not load data: " + var34.getMessage());
		}

		int previousPlace = 0;
		int previousWins = 0;
		int previousLoses = 0;
		int previousDrawn = 0;
		int previousPoints = 0;

		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT charId, olympiad_points, competitions_won, competitions_lost, competitions_drawn FROM olympiad_nobles_eom WHERE class_id = ? ORDER BY olympiad_points DESC, competitions_won DESC LIMIT 500");)
		{
			statement.setInt(1, this._player.getBaseClass());

			try (ResultSet rset = statement.executeQuery())
			{
				for (int ix = 1; rset.next(); ix++)
				{
					if (rset.getInt("charId") == this._player.getObjectId())
					{
						previousPlace = ix;
						previousWins = rset.getInt("competitions_won");
						previousLoses = rset.getInt("competitions_lost");
						previousDrawn = rset.getInt("competitions_drawn");
						previousPoints = rset.getInt("olympiad_points");
					}
				}
			}
		}
		catch (SQLException var30)
		{
			PacketLogger.warning("Olympiad my ranking: Could not load data: " + var30.getMessage());
		}

		int heroCount = 0;
		int legendCount = 0;
		if (Hero.getInstance().getCompleteHeroes().containsKey(this._player.getObjectId()))
		{
			StatSet hero = Hero.getInstance().getCompleteHeroes().get(this._player.getObjectId());
			heroCount = hero.getInt("count", 0);
			legendCount = hero.getInt("legend_count", 0);
		}

		buffer.writeInt(year);
		buffer.writeInt(month);
		buffer.writeInt(Math.min(Olympiad.getInstance().getCurrentCycle() - 1, 0));
		buffer.writeInt(currentPlace);
		buffer.writeInt(currentWins);
		buffer.writeInt(currentLoses);
		buffer.writeInt(currentPoints);
		buffer.writeInt(previousPlace);
		buffer.writeInt(previousWins);
		buffer.writeInt(previousLoses);
		buffer.writeInt(previousDrawn);
		buffer.writeInt(OlympiadConfig.OLYMPIAD_MIN_MATCHES);
		buffer.writeInt(previousPoints);
		buffer.writeInt(heroCount);
		buffer.writeInt(legendCount);
		buffer.writeInt(0);
	}
}
