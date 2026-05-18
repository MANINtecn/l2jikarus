package net.sf.l2jdev.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.gameserver.model.Mentee;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.skill.BuffInfo;
import net.sf.l2jdev.gameserver.model.variables.PlayerVariables;

public class MentorManager
{
	private static final Logger LOGGER = Logger.getLogger(MentorManager.class.getName());
	private final Map<Integer, Map<Integer, Mentee>> _menteeData = new ConcurrentHashMap<>();
	private final Map<Integer, Mentee> _mentors = new ConcurrentHashMap<>();

	protected MentorManager()
	{
		this.load();
	}

	private void load()
	{
		try (Connection con = DatabaseFactory.getConnection(); Statement statement = con.createStatement(); ResultSet rset = statement.executeQuery("SELECT * FROM character_mentees");)
		{
			while (rset.next())
			{
				this.addMentor(rset.getInt("mentorId"), rset.getInt("charId"));
			}
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.WARNING, var12.getMessage(), var12);
		}
	}

	public void deleteMentee(int mentorId, int menteeId)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("DELETE FROM character_mentees WHERE mentorId = ? AND charId = ?");)
		{
			statement.setInt(1, mentorId);
			statement.setInt(2, menteeId);
			statement.execute();
		}
		catch (Exception var11)
		{
			LOGGER.log(Level.WARNING, var11.getMessage(), var11);
		}
	}

	public void deleteMentor(int mentorId, int menteeId)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("DELETE FROM character_mentees WHERE mentorId = ? AND charId = ?");)
		{
			statement.setInt(1, mentorId);
			statement.setInt(2, menteeId);
			statement.execute();
		}
		catch (Exception var18)
		{
			LOGGER.log(Level.WARNING, var18.getMessage(), var18);
		}
		finally
		{
			this.removeMentor(mentorId, menteeId);
		}
	}

	public boolean isMentor(int objectId)
	{
		return this._menteeData.containsKey(objectId);
	}

	public boolean isMentee(int objectId)
	{
		for (Map<Integer, Mentee> map : this._menteeData.values())
		{
			if (map.containsKey(objectId))
			{
				return true;
			}
		}

		return false;
	}

	public Map<Integer, Map<Integer, Mentee>> getMentorData()
	{
		return this._menteeData;
	}

	public void cancelAllMentoringBuffs(Player player)
	{
		if (player != null)
		{
			for (BuffInfo info : player.getEffectList().getEffects())
			{
				if (info.getSkill().isMentoring())
				{
					player.stopSkillEffects(info.getSkill());
				}
			}
		}
	}

	public void setPenalty(int mentorId, long penalty)
	{
		Player player = World.getInstance().getPlayer(mentorId);
		PlayerVariables vars = player != null ? player.getVariables() : new PlayerVariables(mentorId);
		vars.set("Mentor-Penalty-" + mentorId, String.valueOf(System.currentTimeMillis() + penalty));
	}

	public long getMentorPenalty(int mentorId)
	{
		Player player = World.getInstance().getPlayer(mentorId);
		PlayerVariables vars = player != null ? player.getVariables() : new PlayerVariables(mentorId);
		return vars.getLong("Mentor-Penalty-" + mentorId, 0L);
	}

	public void addMentor(int mentorId, int menteeId)
	{
		Map<Integer, Mentee> mentees = this._menteeData.computeIfAbsent(mentorId, _ -> new ConcurrentHashMap<>());
		if (mentees.containsKey(menteeId))
		{
			mentees.get(menteeId).load();
		}
		else
		{
			mentees.put(menteeId, new Mentee(menteeId));
		}
	}

	public void removeMentor(int mentorId, int menteeId)
	{
		if (this._menteeData.containsKey(mentorId))
		{
			this._menteeData.get(mentorId).remove(menteeId);
			if (this._menteeData.get(mentorId).isEmpty())
			{
				this._menteeData.remove(mentorId);
				this._mentors.remove(mentorId);
			}
		}
	}

	public Mentee getMentor(int menteeId)
	{
		for (Entry<Integer, Map<Integer, Mentee>> map : this._menteeData.entrySet())
		{
			if (map.getValue().containsKey(menteeId))
			{
				if (!this._mentors.containsKey(map.getKey()))
				{
					this._mentors.put(map.getKey(), new Mentee(map.getKey()));
				}

				return this._mentors.get(map.getKey());
			}
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public Collection<Mentee> getMentees(int mentorId)
	{
		return (Collection<Mentee>) (this._menteeData.containsKey(mentorId) ? this._menteeData.get(mentorId).values() : Collections.emptyList());
	}

	public Mentee getMentee(int mentorId, int menteeId)
	{
		return this._menteeData.containsKey(mentorId) ? this._menteeData.get(mentorId).get(menteeId) : null;
	}

	public boolean isAllMenteesOffline(int menteorId, int menteeId)
	{
		boolean isAllMenteesOffline = true;

		for (Mentee men : this.getMentees(menteorId))
		{
			if (men.isOnline() && men.getObjectId() != menteeId)
			{
				isAllMenteesOffline = false;
				break;
			}
		}

		return isAllMenteesOffline;
	}

	public boolean hasOnlineMentees(int menteorId)
	{
		for (Mentee mentee : this.getMentees(menteorId))
		{
			if (mentee != null && mentee.isOnline())
			{
				return true;
			}
		}

		return false;
	}

	public static MentorManager getInstance()
	{
		return MentorManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final MentorManager INSTANCE = new MentorManager();
	}
}
