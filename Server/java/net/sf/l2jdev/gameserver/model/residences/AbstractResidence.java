package net.sf.l2jdev.gameserver.model.residences;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.data.xml.SkillTreeData;
import net.sf.l2jdev.gameserver.model.SkillLearn;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.SocialClass;
import net.sf.l2jdev.gameserver.model.clan.enums.ClanHallGrade;
import net.sf.l2jdev.gameserver.model.events.ListenersContainer;
import net.sf.l2jdev.gameserver.model.zone.type.ResidenceZone;

public abstract class AbstractResidence extends ListenersContainer
{
	private static final Logger LOGGER = Logger.getLogger(AbstractResidence.class.getName());
	protected ClanHallGrade _grade = ClanHallGrade.GRADE_NONE;
	private final int _residenceId;
	private String _name;
	private ResidenceZone _zone = null;
	private final Map<Integer, ResidenceFunction> _functions = new ConcurrentHashMap<>();
	private List<SkillLearn> _residentialSkills = new ArrayList<>();

	public AbstractResidence(int residenceId)
	{
		this._residenceId = residenceId;
		this.initResidentialSkills();
	}

	protected abstract void load();

	protected abstract void initResidenceZone();

	public abstract int getOwnerId();

	protected void initResidentialSkills()
	{
		this._residentialSkills = SkillTreeData.getInstance().getAvailableResidentialSkills(this.getResidenceId());
	}

	public ClanHallGrade getGrade()
	{
		return this._grade;
	}

	public int getResidenceId()
	{
		return this._residenceId;
	}

	public String getName()
	{
		return this._name;
	}

	public void setName(String name)
	{
		this._name = name;
	}

	public ResidenceZone getResidenceZone()
	{
		return this._zone;
	}

	protected void setResidenceZone(ResidenceZone zone)
	{
		this._zone = zone;
	}

	public void giveResidentialSkills(Player player)
	{
		if (this._residentialSkills != null && !this._residentialSkills.isEmpty())
		{
			int playerSocialClass = player.getPledgeClass() + 1;

			for (SkillLearn skill : this._residentialSkills)
			{
				SocialClass skillSocialClass = skill.getSocialClass();
				if (skillSocialClass == null || playerSocialClass >= skillSocialClass.ordinal())
				{
					player.addSkill(SkillData.getInstance().getSkill(skill.getSkillId(), skill.getSkillLevel()), false);
				}
			}
		}
	}

	public void removeResidentialSkills(Player player)
	{
		if (this._residentialSkills != null && !this._residentialSkills.isEmpty())
		{
			for (SkillLearn skill : this._residentialSkills)
			{
				player.removeSkill(skill.getSkillId(), false);
			}
		}
	}

	protected void initFunctions()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM residence_functions WHERE residenceId = ?");)
		{
			ps.setInt(1, this._residenceId);

			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					int id = rs.getInt("id");
					int level = rs.getInt("level");
					long expiration = rs.getLong("expiration");
					ResidenceFunction func = new ResidenceFunction(id, level, expiration, this);
					if (expiration <= System.currentTimeMillis() && !func.reactivate())
					{
						this.removeFunction(func);
					}
					else
					{
						this._functions.put(id, func);
					}
				}
			}
		}
		catch (Exception var15)
		{
			LOGGER.log(Level.WARNING, "Failed to initialize functions for residence: " + this._residenceId, var15);
		}
	}

	public void addFunction(int id, int level)
	{
		this.addFunction(new ResidenceFunction(id, level, this));
	}

	public void addFunction(ResidenceFunction func)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("INSERT INTO residence_functions (id, level, expiration, residenceId) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE level = ?, expiration = ?");)
		{
			ps.setInt(1, func.getId());
			ps.setInt(2, func.getLevel());
			ps.setLong(3, func.getExpiration());
			ps.setInt(4, this._residenceId);
			ps.setInt(5, func.getLevel());
			ps.setLong(6, func.getExpiration());
			ps.execute();
		}
		catch (Exception var17)
		{
			LOGGER.log(Level.WARNING, "Failed to add function: " + func.getId() + " for residence: " + this._residenceId, var17);
		}
		finally
		{
			if (this._functions.containsKey(func.getId()))
			{
				this.removeFunction(this._functions.get(func.getId()));
			}

			this._functions.put(func.getId(), func);
		}
	}

	public void removeFunction(ResidenceFunction func)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM residence_functions WHERE residenceId = ? and id = ?");)
		{
			ps.setInt(1, this._residenceId);
			ps.setInt(2, func.getId());
			ps.execute();
		}
		catch (Exception var17)
		{
			LOGGER.log(Level.WARNING, "Failed to remove function: " + func.getId() + " residence: " + this._residenceId, var17);
		}
		finally
		{
			func.cancelExpiration();
			this._functions.remove(func.getId());
		}
	}

	public void removeFunctions()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM residence_functions WHERE residenceId = ?");)
		{
			ps.setInt(1, this._residenceId);
			ps.execute();
		}
		catch (Exception var16)
		{
			LOGGER.log(Level.WARNING, "Failed to remove functions for residence: " + this._residenceId, var16);
		}
		finally
		{
			this._functions.values().forEach(ResidenceFunction::cancelExpiration);
			this._functions.clear();
		}
	}

	public boolean hasFunction(ResidenceFunctionType type)
	{
		for (ResidenceFunction function : this._functions.values())
		{
			ResidenceFunctionTemplate template = function.getTemplate();
			if (template != null && template.getType() == type)
			{
				return true;
			}
		}

		return false;
	}

	public ResidenceFunction getFunction(ResidenceFunctionType type)
	{
		for (ResidenceFunction function : this._functions.values())
		{
			if (function.getType() == type)
			{
				return function;
			}
		}

		return null;
	}

	public ResidenceFunction getFunction(int id, int level)
	{
		for (ResidenceFunction func : this._functions.values())
		{
			if (func.getId() == id && func.getLevel() == level)
			{
				return func;
			}
		}

		return null;
	}

	public ResidenceFunction getFunction(int id)
	{
		for (ResidenceFunction func : this._functions.values())
		{
			if (func.getId() == id)
			{
				return func;
			}
		}

		return null;
	}

	public int getFunctionLevel(ResidenceFunctionType type)
	{
		ResidenceFunction func = this.getFunction(type);
		return func != null ? func.getLevel() : 0;
	}

	public long getFunctionExpiration(ResidenceFunctionType type)
	{
		ResidenceFunction function = null;

		for (ResidenceFunction func : this._functions.values())
		{
			if (func.getTemplate().getType() == type)
			{
				function = func;
				break;
			}
		}

		return function != null ? function.getExpiration() : -1L;
	}

	public Collection<ResidenceFunction> getFunctions()
	{
		return this._functions.values();
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof AbstractResidence && ((AbstractResidence) obj).getResidenceId() == this.getResidenceId();
	}

	@Override
	public String toString()
	{
		return this._name + " (" + this._residenceId + ")";
	}
}
