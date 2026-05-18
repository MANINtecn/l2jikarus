package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;
import org.w3c.dom.Document;

public class PetTypeData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(PetTypeData.class.getName());
	private final Map<Integer, SkillHolder> _skills = new HashMap<>();
	private final Map<Integer, String> _names = new HashMap<>();

	protected PetTypeData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this._skills.clear();
		this.parseDatapackFile("data/PetTypes.xml");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._skills.size() + " pet types.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		this.forEach(document, "list", listNode -> this.forEach(listNode, "pet", petNode -> {
			StatSet set = new StatSet(this.parseAttributes(petNode));
			int id = set.getInt("id");
			this._skills.put(id, new SkillHolder(set.getInt("skillId", 0), set.getInt("skillLvl", 0)));
			this._names.put(id, set.getString("name"));
		}));
	}

	public SkillHolder getSkillByName(String name)
	{
		for (Entry<Integer, String> entry : this._names.entrySet())
		{
			if (name.startsWith(entry.getValue()))
			{
				return this._skills.get(entry.getKey());
			}
		}

		return null;
	}

	public int getIdByName(String name)
	{
		if (name == null)
		{
			return 0;
		}
		int spaceIndex = name.indexOf(32);
		String searchName;
		if (spaceIndex != -1)
		{
			searchName = name.substring(spaceIndex + 1);
		}
		else
		{
			searchName = name;
		}

		for (Entry<Integer, String> entry : this._names.entrySet())
		{
			if (searchName.endsWith(entry.getValue()))
			{
				return entry.getKey();
			}
		}

		return 0;
	}

	public String getNamePrefix(Integer id)
	{
		return this._names.get(id);
	}

	public String getRandomName()
	{
		String result = null;
		List<Entry<Integer, String>> entryList = new ArrayList<>(this._names.entrySet());

		while (result == null)
		{
			Entry<Integer, String> temp = entryList.get(Rnd.get(entryList.size()));
			if (temp.getKey() > 100)
			{
				result = temp.getValue();
			}
		}

		return result;
	}

	public Entry<Integer, SkillHolder> getRandomSkill()
	{
		Entry<Integer, SkillHolder> result = null;
		List<Entry<Integer, SkillHolder>> entryList = new ArrayList<>(this._skills.entrySet());

		while (result == null)
		{
			Entry<Integer, SkillHolder> temp = entryList.get(Rnd.get(entryList.size()));
			if (temp.getValue().getSkillId() > 0)
			{
				result = temp;
			}
		}

		return result;
	}

	public static PetTypeData getInstance()
	{
		return PetTypeData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final PetTypeData INSTANCE = new PetTypeData();
	}
}
