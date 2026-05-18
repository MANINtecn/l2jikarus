package net.sf.l2jdev.gameserver.model.announce;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;

public class Announcement implements IAnnouncement
{
	protected static final Logger LOGGER = Logger.getLogger(Announcement.class.getName());
	public static final String INSERT_QUERY = "INSERT INTO announcements (type, content, author) VALUES (?, ?, ?)";
	public static final String UPDATE_QUERY = "UPDATE announcements SET type = ?, content = ?, author = ? WHERE id = ?";
	public static final String DELETE_QUERY = "DELETE FROM announcements WHERE id = ?";
	protected int _id;
	private AnnouncementType _type;
	private String _content;
	private String _author;

	public Announcement(AnnouncementType type, String content, String author)
	{
		this._type = type;
		this._content = content;
		this._author = author;
	}

	public Announcement(ResultSet rset) throws SQLException
	{
		this._id = rset.getInt("id");
		this._type = AnnouncementType.findById(rset.getInt("type"));
		this._content = rset.getString("content");
		this._author = rset.getString("author");
	}

	@Override
	public int getId()
	{
		return this._id;
	}

	@Override
	public AnnouncementType getType()
	{
		return this._type;
	}

	@Override
	public void setType(AnnouncementType type)
	{
		this._type = type;
	}

	@Override
	public String getContent()
	{
		return this._content;
	}

	@Override
	public void setContent(String content)
	{
		this._content = content;
	}

	@Override
	public String getAuthor()
	{
		return this._author;
	}

	@Override
	public void setAuthor(String author)
	{
		this._author = author;
	}

	@Override
	public boolean isValid()
	{
		return true;
	}

	@Override
	public boolean storeMe()
	{
		try
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("INSERT INTO announcements (type, content, author) VALUES (?, ?, ?)", 1);)
			{
				ps.setInt(1, this._type.ordinal());
				ps.setString(2, this._content);
				ps.setString(3, this._author);
				ps.execute();

				try (ResultSet rset = ps.getGeneratedKeys())
				{
					if (rset.next())
					{
						this._id = rset.getInt(1);
					}
				}
			}

			return true;
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Couldn't store announcement: ", var12);
			return false;
		}
	}

	@Override
	public boolean updateMe()
	{
		try
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE announcements SET type = ?, content = ?, author = ? WHERE id = ?");)
			{
				ps.setInt(1, this._type.ordinal());
				ps.setString(2, this._content);
				ps.setString(3, this._author);
				ps.setInt(4, this._id);
				ps.execute();
			}

			return true;
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Couldn't store announcement: ", var9);
			return false;
		}
	}

	@Override
	public boolean deleteMe()
	{
		try
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM announcements WHERE id = ?");)
			{
				ps.setInt(1, this._id);
				ps.execute();
			}

			return true;
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Couldn't remove announcement: ", var9);
			return false;
		}
	}
}
