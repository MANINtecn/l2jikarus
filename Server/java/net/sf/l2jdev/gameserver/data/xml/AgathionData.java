package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.item.holders.AgathionSkillHolder;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import org.w3c.dom.Document;

public class AgathionData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(AgathionData.class.getName());
	private static final Map<Integer, AgathionSkillHolder> AGATHION_SKILLS = new HashMap<>();

	protected AgathionData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		AGATHION_SKILLS.clear();
		this.parseDatapackFile("data/AgathionData.xml");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + AGATHION_SKILLS.size() + " agathion data.");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void parseDocument(Document document, File file)
	{
		this.forEach(document, "list", listNode -> this.forEach(listNode, "agathion", agathionNode -> {
			StatSet set = new StatSet(this.parseAttributes(agathionNode));
			int id = set.getInt("id");
			if (ItemData.getInstance().getTemplate(id) == null)
			{
				LOGGER.info(this.getClass().getSimpleName() + ": Could not find agathion with id " + id + ".");
			}
			else
			{
				int enchant = set.getInt("enchant", 0);
				Map<Integer, List<Skill>> mainSkills = (Map<Integer, List<Skill>>) (AGATHION_SKILLS.containsKey(id) ? AGATHION_SKILLS.get(id).getMainSkills() : new HashMap<>());
				List<Skill> mainSkillList = new ArrayList<>();
				String main = set.getString("mainSkill", "");

				for (String ids : main.split(";"))
				{
					if (!ids.isEmpty())
					{
						String[] split = ids.split(",");
						int skillId = Integer.parseInt(split[0]);
						int level = Integer.parseInt(split[1]);
						Skill skill = SkillData.getInstance().getSkill(skillId, level);
						if (skill == null)
						{
							LOGGER.info(this.getClass().getSimpleName() + ": Could not find agathion skill id " + skillId + ".");
							return;
						}

						mainSkillList.add(skill);
					}
				}

				mainSkills.put(enchant, mainSkillList);
				Map<Integer, List<Skill>> subSkills = (Map<Integer, List<Skill>>) (AGATHION_SKILLS.containsKey(id) ? AGATHION_SKILLS.get(id).getSubSkills() : new HashMap<>());
				List<Skill> subSkillList = new ArrayList<>();
				String sub = set.getString("subSkill", "");

				for (String idsx : sub.split(";"))
				{
					if (!idsx.isEmpty())
					{
						String[] split = idsx.split(",");
						int skillId = Integer.parseInt(split[0]);
						int level = Integer.parseInt(split[1]);
						Skill skill = SkillData.getInstance().getSkill(skillId, level);
						if (skill == null)
						{
							LOGGER.info(this.getClass().getSimpleName() + ": Could not find agathion skill id " + skillId + ".");
							return;
						}

						subSkillList.add(skill);
					}
				}

				subSkills.put(enchant, subSkillList);
				AGATHION_SKILLS.put(id, new AgathionSkillHolder(mainSkills, subSkills));
			}
		}));
	}

	public AgathionSkillHolder getSkills(int agathionId)
	{
		return AGATHION_SKILLS.get(agathionId);
	}

	public static AgathionData getInstance()
	{
		return AgathionData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final AgathionData INSTANCE = new AgathionData();
	}
}
