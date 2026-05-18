package net.sf.l2jdev.gameserver.data;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.gameserver.config.custom.SchemeBufferConfig;
import net.sf.l2jdev.gameserver.model.actor.holders.npc.BuffSkillHolder;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class SchemeBufferTable
{
	private static final Logger LOGGER = Logger.getLogger(SchemeBufferTable.class.getName());
	public static final String LOAD_SCHEMES = "SELECT * FROM buffer_schemes";
	public static final String DELETE_SCHEMES = "TRUNCATE TABLE buffer_schemes";
	public static final String INSERT_SCHEME = "INSERT INTO buffer_schemes (object_id, scheme_name, skills) VALUES (?,?,?)";
	private final Map<Integer, Map<String, List<Integer>>> _schemesTable = new ConcurrentHashMap<>();
	private final Map<Integer, BuffSkillHolder> _availableBuffs = new LinkedHashMap<>();

	public SchemeBufferTable()
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new File("./data/SchemeBufferSkills.xml"));
			Node n = document.getFirstChild();

			for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
			{
				if (d.getNodeName().equalsIgnoreCase("category"))
				{
					String category = d.getAttributes().getNamedItem("type").getNodeValue();

					for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
					{
						if (c.getNodeName().equalsIgnoreCase("buff"))
						{
							NamedNodeMap attrs = c.getAttributes();
							int skillId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
							this._availableBuffs.put(skillId, new BuffSkillHolder(skillId, Integer.parseInt(attrs.getNamedItem("level").getNodeValue()), Integer.parseInt(attrs.getNamedItem("price").getNodeValue()), category, attrs.getNamedItem("desc").getNodeValue()));
						}
					}
				}
			}
		}
		catch (Exception var21)
		{
			LOGGER.warning("SchemeBufferTable: Failed to load buff info : " + var21);
		}

		int count = 0;

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement st = con.prepareStatement("SELECT * FROM buffer_schemes"); ResultSet rs = st.executeQuery();)
		{
			while (rs.next())
			{
				int objectId = rs.getInt("object_id");
				String schemeName = rs.getString("scheme_name");
				String[] skills = rs.getString("skills").split(",");
				List<Integer> schemeList = new ArrayList<>();
				String[] var30 = skills;
				int var10 = skills.length;
				int var11 = 0;

				while (true)
				{
					if (var11 < var10)
					{
						String skill = var30[var11];
						if (!skill.isEmpty())
						{
							Integer skillId = Integer.parseInt(skill);
							if (this._availableBuffs.containsKey(skillId))
							{
								schemeList.add(skillId);
							}

							var11++;
							continue;
						}
					}

					this.setScheme(objectId, schemeName, schemeList);
					count++;
					break;
				}
			}
		}
		catch (Exception var20)
		{
			LOGGER.warning("SchemeBufferTable: Failed to load buff schemes: " + var20);
		}

		LOGGER.info("SchemeBufferTable: Loaded " + count + " players schemes and " + this._availableBuffs.size() + " available buffs.");
	}

	public void saveSchemes()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			try (PreparedStatement st = con.prepareStatement("TRUNCATE TABLE buffer_schemes"))
			{
				st.execute();
			}

			try (PreparedStatement st = con.prepareStatement("INSERT INTO buffer_schemes (object_id, scheme_name, skills) VALUES (?,?,?)"))
			{
				for (Entry<Integer, Map<String, List<Integer>>> player : this._schemesTable.entrySet())
				{
					for (Entry<String, List<Integer>> scheme : player.getValue().entrySet())
					{
						StringBuilder sb = new StringBuilder();

						for (int skillId : scheme.getValue())
						{
							sb.append(skillId + ",");
						}

						if (sb.length() > 0)
						{
							sb.setLength(sb.length() - 1);
						}

						st.setInt(1, player.getKey());
						st.setString(2, scheme.getKey());
						st.setString(3, sb.toString());
						st.addBatch();
					}
				}

				st.executeBatch();
			}
		}
		catch (Exception var16)
		{
			LOGGER.warning("BufferTableScheme: Error while saving schemes : " + var16);
		}
	}

	public void setScheme(int playerId, String schemeName, List<Integer> list)
	{
		if (!this._schemesTable.containsKey(playerId))
		{
			this._schemesTable.put(playerId, new TreeMap<>(String.CASE_INSENSITIVE_ORDER));
		}
		else if (this._schemesTable.get(playerId).size() >= SchemeBufferConfig.BUFFER_MAX_SCHEMES)
		{
			return;
		}

		this._schemesTable.get(playerId).put(schemeName, list);
	}

	public Map<String, List<Integer>> getPlayerSchemes(int playerId)
	{
		return this._schemesTable.get(playerId);
	}

	public List<Integer> getScheme(int playerId, String schemeName)
	{
		return this._schemesTable.get(playerId) != null && this._schemesTable.get(playerId).get(schemeName) != null ? this._schemesTable.get(playerId).get(schemeName) : Collections.emptyList();
	}

	public boolean getSchemeContainsSkill(int playerId, String schemeName, int skillId)
	{
		List<Integer> skills = this.getScheme(playerId, schemeName);
		if (skills.isEmpty())
		{
			return false;
		}
		for (int id : skills)
		{
			if (id == skillId)
			{
				return true;
			}
		}

		return false;
	}

	public List<Integer> getSkillsIdsByType(String groupType)
	{
		List<Integer> skills = new ArrayList<>();

		for (BuffSkillHolder skill : this._availableBuffs.values())
		{
			if (skill.getType().equalsIgnoreCase(groupType))
			{
				skills.add(skill.getId());
			}
		}

		return skills;
	}

	public List<String> getSkillTypes()
	{
		List<String> skillTypes = new ArrayList<>();

		for (BuffSkillHolder skill : this._availableBuffs.values())
		{
			if (!skillTypes.contains(skill.getType()))
			{
				skillTypes.add(skill.getType());
			}
		}

		return skillTypes;
	}

	public BuffSkillHolder getAvailableBuff(int skillId)
	{
		return this._availableBuffs.get(skillId);
	}

	public static SchemeBufferTable getInstance()
	{
		return SchemeBufferTable.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final SchemeBufferTable INSTANCE = new SchemeBufferTable();
	}
}
