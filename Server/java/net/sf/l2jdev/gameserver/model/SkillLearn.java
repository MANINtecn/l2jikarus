package net.sf.l2jdev.gameserver.model;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Race;
import net.sf.l2jdev.gameserver.model.actor.enums.player.SocialClass;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;

public class SkillLearn
{
	private final String _skillName;
	private final int _skillId;
	private final int _skillLevel;
	private final int _getLevel;
	private final int _getDualClassLevel;
	private final boolean _autoGet;
	private final long _levelUpSp;
	private final List<List<ItemHolder>> _requiredItems = new ArrayList<>(1);
	private final Set<Race> _races = EnumSet.noneOf(Race.class);
	private final Set<SkillHolder> _preReqSkills = new HashSet<>(1);
	private SocialClass _socialClass;
	private final boolean _residenceSkill;
	private final Set<Integer> _residenceIds = new HashSet<>(1);
	private final boolean _learnedByNpc;
	private final boolean _learnedByFS;
	private final Set<Integer> _removeSkills = new HashSet<>(1);
	private final int _treeId;
	private final int _row;
	private final int _column;
	private final int _pointsRequired;

	public SkillLearn(StatSet set)
	{
		this._skillName = set.getString("skillName");
		this._skillId = set.getInt("skillId");
		this._skillLevel = set.getInt("skillLevel");
		this._getLevel = set.getInt("getLevel");
		this._getDualClassLevel = set.getInt("getDualClassLevel", 0);
		this._autoGet = set.getBoolean("autoGet", false);
		this._levelUpSp = set.getLong("levelUpSp", 0L);
		this._residenceSkill = set.getBoolean("residenceSkill", false);
		this._learnedByNpc = set.getBoolean("learnedByNpc", false);
		this._learnedByFS = set.getBoolean("learnedByFS", false);
		this._treeId = set.getInt("treeId", 0);
		this._row = set.getInt("row", 0);
		this._column = set.getInt("row", 0);
		this._pointsRequired = set.getInt("pointsRequired", 0);
	}

	public String getName()
	{
		return this._skillName;
	}

	public int getSkillId()
	{
		return this._skillId;
	}

	public int getSkillLevel()
	{
		return this._skillLevel;
	}

	public int getGetLevel()
	{
		return this._getLevel;
	}

	public int getDualClassLevel()
	{
		return this._getDualClassLevel;
	}

	public long getLevelUpSp()
	{
		return this._levelUpSp;
	}

	public boolean isAutoGet()
	{
		return this._autoGet;
	}

	public List<List<ItemHolder>> getRequiredItems()
	{
		return this._requiredItems;
	}

	public void addRequiredItem(List<ItemHolder> list)
	{
		this._requiredItems.add(list);
	}

	public Set<Race> getRaces()
	{
		return this._races;
	}

	public void addRace(Race race)
	{
		this._races.add(race);
	}

	public Set<SkillHolder> getPreReqSkills()
	{
		return this._preReqSkills;
	}

	public void addPreReqSkill(SkillHolder skill)
	{
		this._preReqSkills.add(skill);
	}

	public SocialClass getSocialClass()
	{
		return this._socialClass;
	}

	public void setSocialClass(SocialClass socialClass)
	{
		if (this._socialClass == null)
		{
			this._socialClass = socialClass;
		}
	}

	public boolean isResidencialSkill()
	{
		return this._residenceSkill;
	}

	public Set<Integer> getResidenceIds()
	{
		return this._residenceIds;
	}

	public void addResidenceId(Integer id)
	{
		this._residenceIds.add(id);
	}

	public boolean isLearnedByNpc()
	{
		return this._learnedByNpc;
	}

	public boolean isLearnedByFS()
	{
		return this._learnedByFS;
	}

	public void addRemoveSkills(int skillId)
	{
		this._removeSkills.add(skillId);
	}

	public Set<Integer> getRemoveSkills()
	{
		return this._removeSkills;
	}

	public int getTreeId()
	{
		return this._treeId;
	}

	public int getRow()
	{
		return this._row;
	}

	public int getColumn()
	{
		return this._column;
	}

	public int getPointsRequired()
	{
		return this._pointsRequired;
	}

	@Override
	public String toString()
	{
		Skill skill = SkillData.getInstance().getSkill(this._skillId, this._skillLevel);
		return "[" + skill + " treeId: " + this._treeId + " row: " + this._row + " column: " + this._column + " pointsRequired:" + this._pointsRequired + "]";
	}
}
