package net.sf.l2jdev.gameserver.data.holders;

import java.util.ArrayList;
import java.util.List;

public class AdenLabStageHolder
{
	private int _stageLevel;
	private int _combatPower;
	private float _stageChance;
	private final ArrayList<AdenLabSkillHolder> _skills = new ArrayList<>();

	public int getStageLevel()
	{
		return this._stageLevel;
	}

	public int getCombatPower()
	{
		return this._combatPower;
	}

	public float getStageChance()
	{
		return this._stageChance;
	}

	public List<AdenLabSkillHolder> getSkills()
	{
		return this._skills;
	}

	public void setStageLevel(int stageLevel)
	{
		this._stageLevel = stageLevel;
	}

	public void setCombatPower(int combatPower)
	{
		this._combatPower = combatPower;
	}

	public void setStageChance(float stageChance)
	{
		this._stageChance = stageChance;
	}

	public void addSkills(List<AdenLabSkillHolder> skills)
	{
		this._skills.addAll(skills);
	}

	public void addSkill(AdenLabSkillHolder skill)
	{
		this._skills.add(skill);
	}
}
