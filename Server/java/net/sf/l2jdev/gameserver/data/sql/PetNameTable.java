package net.sf.l2jdev.gameserver.data.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.util.StringUtil;
import net.sf.l2jdev.gameserver.config.ServerConfig;
import net.sf.l2jdev.gameserver.data.xml.PetDataTable;

public class PetNameTable
{
	private static final Logger LOGGER = Logger.getLogger(PetNameTable.class.getName());
	public static final String CHECK_PET_NAME = "SELECT name FROM pets p, items i WHERE p.item_obj_id = i.object_id AND name=? AND i.item_id IN (?)";

	public boolean doesPetNameExist(String name, int petNpcId)
	{
		boolean result = false;

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT name FROM pets p, items i WHERE p.item_obj_id = i.object_id AND name=? AND i.item_id IN (?)");)
		{
			ps.setString(1, name);
			StringBuilder cond = new StringBuilder();
			if (!cond.toString().isEmpty())
			{
				cond.append(", ");
			}

			cond.append(PetDataTable.getInstance().getPetItemsByNpc(petNpcId));
			ps.setString(2, cond.toString());

			try (ResultSet rs = ps.executeQuery())
			{
				result = rs.next();
			}
		}
		catch (SQLException var16)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Could not check existing petname:" + var16.getMessage(), var16);
		}

		return result;
	}

	public boolean isValidPetName(String name)
	{
		boolean result = false;
		if (!StringUtil.isAlphaNumeric(name))
		{
			return result;
		}
		Pattern pattern;
		try
		{
			pattern = Pattern.compile(ServerConfig.PET_NAME_TEMPLATE);
		}
		catch (PatternSyntaxException var5)
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": Pet name pattern of config is wrong!");
			pattern = Pattern.compile(".*");
		}

		Matcher regexp = pattern.matcher(name);
		if (regexp.matches())
		{
			result = true;
		}

		return result;
	}

	public static PetNameTable getInstance()
	{
		return PetNameTable.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final PetNameTable INSTANCE = new PetNameTable();
	}
}
