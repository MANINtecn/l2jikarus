package net.sf.l2jdev.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class Mentee
{
	private static final Logger LOGGER = Logger.getLogger(Mentee.class.getName());
	private final int _objectId;
	private String _name;
	private int _classId;
	private int _currentLevel;

	public Mentee(int objectId)
	{
		this._objectId = objectId;
		this.load();
	}

	public void load()
	{
		Player player = this.getPlayer();
		if (player == null)
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT char_name, level, base_class FROM characters WHERE charId = ?");)
			{
				statement.setInt(1, this._objectId);

				try (ResultSet rset = statement.executeQuery())
				{
					if (rset.next())
					{
						this._name = rset.getString("char_name");
						this._classId = rset.getInt("base_class");
						this._currentLevel = rset.getInt("level");
					}
				}
			}
			catch (Exception var13)
			{
				LOGGER.log(Level.WARNING, var13.getMessage(), var13);
			}
		}
		else
		{
			this._name = player.getName();
			this._classId = player.getBaseClass();
			this._currentLevel = player.getLevel();
		}
	}

	public int getObjectId()
	{
		return this._objectId;
	}

	public String getName()
	{
		return this._name;
	}

	public int getClassId()
	{
		if (this.isOnline() && this.getPlayer().getPlayerClass().getId() != this._classId)
		{
			this._classId = this.getPlayer().getPlayerClass().getId();
		}

		return this._classId;
	}

	public int getLevel()
	{
		if (this.isOnline() && this.getPlayer().getLevel() != this._currentLevel)
		{
			this._currentLevel = this.getPlayer().getLevel();
		}

		return this._currentLevel;
	}

	public Player getPlayer()
	{
		return World.getInstance().getPlayer(this._objectId);
	}

	public boolean isOnline()
	{
		return this.getPlayer() != null && this.getPlayer().isOnlineInt() > 0;
	}

	public int isOnlineInt()
	{
		return this.isOnline() ? this.getPlayer().isOnlineInt() : 0;
	}

	public void sendPacket(ServerPacket packet)
	{
		if (this.isOnline())
		{
			this.getPlayer().sendPacket(packet);
		}
	}
}
