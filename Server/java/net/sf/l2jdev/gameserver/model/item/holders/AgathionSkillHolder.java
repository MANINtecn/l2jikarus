package net.sf.l2jdev.gameserver.model.item.holders;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.sf.l2jdev.gameserver.model.skill.Skill;

public class AgathionSkillHolder
{
	private final Map<Integer, List<Skill>> _mainSkill;
	private final Map<Integer, List<Skill>> _subSkill;

	public AgathionSkillHolder(Map<Integer, List<Skill>> mainSkill, Map<Integer, List<Skill>> subSkill)
	{
		this._mainSkill = mainSkill;
		this._subSkill = subSkill;
	}

	public Map<Integer, List<Skill>> getMainSkills()
	{
		return this._mainSkill;
	}

	public Map<Integer, List<Skill>> getSubSkills()
	{
		return this._subSkill;
	}

	public List<Skill> getMainSkills(int enchantLevel)
	{
		return !this._mainSkill.containsKey(enchantLevel) ? Collections.emptyList() : this._mainSkill.get(enchantLevel);
	}

	public List<Skill> getSubSkills(int enchantLevel)
	{
		return !this._subSkill.containsKey(enchantLevel) ? Collections.emptyList() : this._subSkill.get(enchantLevel);
	}
}
